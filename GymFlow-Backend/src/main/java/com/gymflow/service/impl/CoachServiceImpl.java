package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.coach.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.CoachService;
import com.gymflow.utils.BCryptUtil;
import com.gymflow.vo.CoachListVO;
import com.gymflow.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachMapper coachMapper;
    private final CoachScheduleMapper coachScheduleMapper;
    private final CourseMapper courseMapper;
    private final CourseBookingMapper courseBookingMapper;

    private final BCryptUtil bCryptUtil;

    @Override
    public PageResultVO<CoachListVO> getCoachList(CoachQueryDTO queryDTO) {
        // 创建分页对象
        Page<Coach> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Coach> queryWrapper = new LambdaQueryWrapper<>();

        // 教练姓名查询
        if (StringUtils.hasText(queryDTO.getRealName())) {
            queryWrapper.like(Coach::getRealName, queryDTO.getRealName());
        }

        // 手机号查询
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.like(Coach::getPhone, queryDTO.getPhone());
        }

        // 专业特长查询
        if (StringUtils.hasText(queryDTO.getSpecialty())) {
            queryWrapper.like(Coach::getSpecialty, queryDTO.getSpecialty());
        }

        // 状态查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Coach::getStatus, queryDTO.getStatus());
        }

        // 从业年限范围查询
        if (queryDTO.getMinExperience() != null) {
            queryWrapper.ge(Coach::getYearsOfExperience, queryDTO.getMinExperience());
        }
        if (queryDTO.getMaxExperience() != null) {
            queryWrapper.le(Coach::getYearsOfExperience, queryDTO.getMaxExperience());
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Coach::getCreateTime);

        // 执行分页查询
        IPage<Coach> coachPage = coachMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<CoachListVO> voList = coachPage.getRecords().stream()
                .map(this::convertToCoachListVO)
                .collect(Collectors.toList());

        // 构建返回结果
        return new PageResultVO<>(voList, coachPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public CoachFullDTO getCoachDetail(Long coachId) {
        // 获取教练基本信息
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 构建完整DTO
        CoachFullDTO fullDTO = new CoachFullDTO();

        // 设置教练基本信息
        BeanUtils.copyProperties(coach, fullDTO);

        // 解析证书信息
        if (StringUtils.hasText(coach.getCertifications())) {
            try {
                // 假设certifications存储为JSON字符串，如["证书1","证书2"]
                String certStr = coach.getCertifications();
                certStr = certStr.replace("[", "").replace("]", "").replace("\"", "");
                fullDTO.setCertificationList(Arrays.asList(certStr.split(",")));
            } catch (Exception e) {
                log.error("解析教练证书信息失败，教练ID：{}", coachId, e);
                fullDTO.setCertificationList(new ArrayList<>());
            }
        }

        // 获取教练排班列表
        fullDTO.setSchedules(getCoachSchedules(coachId));

        // 获取教练关联课程列表
        fullDTO.setCourses(getCoachCourses(coachId));

        return fullDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCoach(CoachBasicDTO basicDTO) {
        log.info("开始添加教练，姓名：{}，手机号：{}", basicDTO.getRealName(), basicDTO.getPhone());

        // 1. 检查手机号是否已存在
        LambdaQueryWrapper<Coach> phoneQuery = new LambdaQueryWrapper<>();
        phoneQuery.eq(Coach::getPhone, basicDTO.getPhone());
        Long phoneCount = coachMapper.selectCount(phoneQuery);
        if (phoneCount > 0) {
            throw new BusinessException("该手机号已注册为教练");
        }

        // 2. 创建教练记录
        Coach coach = new Coach();

        // 设置基本信息
        coach.setRealName(basicDTO.getRealName());
        coach.setPhone(basicDTO.getPhone());

        // 密码处理：如果有传入密码则使用，否则使用默认密码
        String password = StringUtils.hasText(basicDTO.getPassword()) ?
                basicDTO.getPassword() : "123456";
        coach.setPassword(bCryptUtil.encodePassword(password));

        coach.setSpecialty(basicDTO.getSpecialty());

        // 处理证书信息
        if (!CollectionUtils.isEmpty(basicDTO.getCertificationList())) {
            String certStr = basicDTO.getCertificationList().toString();
            coach.setCertifications(certStr);
        }

        coach.setYearsOfExperience(basicDTO.getYearsOfExperience());
        coach.setHourlyRate(basicDTO.getHourlyRate());
        coach.setCommissionRate(basicDTO.getCommissionRate());
        coach.setStatus(1); // 默认在职
        coach.setIntroduction(basicDTO.getIntroduction());

        // 初始化统计信息
        coach.setTotalStudents(0);
        coach.setTotalCourses(0);
        coach.setTotalIncome(BigDecimal.ZERO);
        coach.setRating(new BigDecimal("5.00")); // 默认评分5.0

        // 3. 保存教练
        int result = coachMapper.insert(coach);
        if (result <= 0) {
            throw new BusinessException("添加教练失败");
        }

        log.info("添加教练成功，ID：{}，姓名：{}", coach.getId(), coach.getRealName());

        return coach.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoach(Long coachId, CoachBasicDTO basicDTO) {
        log.info("开始更新教练，ID：{}", coachId);

        // 1. 检查教练是否存在
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 2. 检查手机号是否已被其他教练使用
        if (!coach.getPhone().equals(basicDTO.getPhone())) {
            LambdaQueryWrapper<Coach> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Coach::getPhone, basicDTO.getPhone());
            queryWrapper.ne(Coach::getId, coachId);
            Long count = coachMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException("该手机号已被其他教练使用");
            }
        }

        // 3. 更新教练基本信息
        coach.setRealName(basicDTO.getRealName());
        coach.setPhone(basicDTO.getPhone());
        coach.setSpecialty(basicDTO.getSpecialty());

        // 处理证书信息
        if (!CollectionUtils.isEmpty(basicDTO.getCertificationList())) {
            String certStr = basicDTO.getCertificationList().toString();
            coach.setCertifications(certStr);
        } else {
            coach.setCertifications(null);
        }

        coach.setYearsOfExperience(basicDTO.getYearsOfExperience());
        coach.setHourlyRate(basicDTO.getHourlyRate());
        coach.setCommissionRate(basicDTO.getCommissionRate());
        coach.setIntroduction(basicDTO.getIntroduction());

        // 只有密码不为空时才更新密码
        if (StringUtils.hasText(basicDTO.getPassword())) {
            coach.setPassword(bCryptUtil.encodePassword(basicDTO.getPassword()));
        }

        // 4. 更新教练记录
        int result = coachMapper.updateById(coach);
        if (result <= 0) {
            throw new BusinessException("更新教练失败");
        }

        log.info("更新教练成功，ID：{}", coachId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoach(Long coachId) {
        log.info("开始删除教练，ID：{}", coachId);

        // 1. 检查教练是否存在
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 2. 检查教练是否有未完成的课程
        checkUnfinishedCourses(coachId);

        // 3. 检查教练是否有会员关联
        checkMemberAssociations(coachId);

        // 4. 软删除：更新状态为离职（不删除记录）
        coach.setStatus(0); // 离职状态

        int result = coachMapper.updateById(coach);
        if (result <= 0) {
            throw new BusinessException("删除教练失败");
        }

        log.info("删除教练成功，ID：{}", coachId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteCoach(List<Long> coachIds) {
        log.info("开始批量删除教练，ID列表：{}", coachIds);

        if (CollectionUtils.isEmpty(coachIds)) {
            log.warn("批量删除教练ID列表为空");
            return;
        }

        int successCount = 0;
        int failCount = 0;

        for (Long coachId : coachIds) {
            try {
                deleteCoach(coachId);
                successCount++;
            } catch (Exception e) {
                log.error("删除教练失败，ID：{}，错误：{}", coachId, e.getMessage());
                failCount++;
            }
        }

        log.info("批量删除教练完成，成功：{}，失败：{}", successCount, failCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoachStatus(Long coachId, Integer status) {
        log.info("更新教练状态，教练ID：{}，状态：{}", coachId, status);

        // 检查状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（离职）或1（在职）");
        }

        // 检查教练是否存在
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 更新状态
        coach.setStatus(status);

        int result = coachMapper.updateById(coach);
        if (result <= 0) {
            throw new BusinessException("更新教练状态失败");
        }

        log.info("更新教练状态成功，教练ID：{}，新状态：{}", coachId, status);
    }

    @Override
    public List<CoachScheduleDTO> getCoachSchedules(Long coachId) {
        log.info("获取教练排班列表，教练ID：{}", coachId);

        // 查询教练排班
        LambdaQueryWrapper<CoachSchedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoachSchedule::getCoachId, coachId);
        queryWrapper.ge(CoachSchedule::getScheduleDate, LocalDate.now()); // 只查询未来排班
        queryWrapper.orderByAsc(CoachSchedule::getScheduleDate)
                .orderByAsc(CoachSchedule::getStartTime);

        List<CoachSchedule> schedules = coachScheduleMapper.selectList(queryWrapper);

        return schedules.stream()
                .map(this::convertToCoachScheduleDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCoachSchedule(Long coachId, CoachScheduleDTO scheduleDTO) {
        log.info("开始添加教练排班，教练ID：{}", coachId);

        // 1. 检查教练是否存在
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 2. 检查排班时间冲突
        checkScheduleConflict(coachId, scheduleDTO);

        // 3. 创建排班记录
        CoachSchedule schedule = new CoachSchedule();
        schedule.setCoachId(coachId);
        schedule.setScheduleDate(scheduleDTO.getScheduleDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setScheduleType(scheduleDTO.getScheduleType());
        schedule.setStatus("SCHEDULED");
        schedule.setNotes(scheduleDTO.getNotes());

        // 4. 保存排班
        int result = coachScheduleMapper.insert(schedule);
        if (result <= 0) {
            throw new BusinessException("添加教练排班失败");
        }

        log.info("添加教练排班成功，排班ID：{}", schedule.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoachSchedule(Long scheduleId, CoachScheduleDTO scheduleDTO) {
        log.info("开始更新教练排班，排班ID：{}", scheduleId);

        // 1. 检查排班是否存在
        CoachSchedule schedule = coachScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }

        // 2. 检查排班时间冲突（排除自身）
        checkScheduleConflict(schedule.getCoachId(), scheduleDTO, scheduleId);

        // 3. 更新排班信息
        schedule.setScheduleDate(scheduleDTO.getScheduleDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setScheduleType(scheduleDTO.getScheduleType());
        schedule.setNotes(scheduleDTO.getNotes());

        // 4. 保存更新
        int result = coachScheduleMapper.updateById(schedule);
        if (result <= 0) {
            throw new BusinessException("更新教练排班失败");
        }

        log.info("更新教练排班成功，排班ID：{}", scheduleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoachSchedule(Long scheduleId) {
        log.info("开始删除教练排班，排班ID：{}", scheduleId);

        // 1. 检查排班是否存在
        CoachSchedule schedule = coachScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排班不存在");
        }

        // 2. 检查是否已经有课程关联
        checkCourseAssociations(scheduleId);

        // 3. 删除排班
        int result = coachScheduleMapper.deleteById(scheduleId);
        if (result <= 0) {
            throw new BusinessException("删除教练排班失败");
        }

        log.info("删除教练排班成功，排班ID：{}", scheduleId);
    }

    @Override
    public List<CoachCourseDTO> getCoachCourses(Long coachId) {
        log.info("获取教练关联课程列表，教练ID：{}", coachId);

        List<CoachCourseDTO> courseList = new ArrayList<>();

        try {
            // 查询教练教授的课程
            LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Course::getCoachId, coachId);
            queryWrapper.ge(Course::getCourseDate, LocalDate.now()); // 只查询未来课程
            queryWrapper.orderByDesc(Course::getCourseDate)
                    .orderByAsc(Course::getStartTime);

            List<Course> courses = courseMapper.selectList(queryWrapper);

            for (Course course : courses) {
                CoachCourseDTO courseDTO = new CoachCourseDTO();
                BeanUtils.copyProperties(course, courseDTO);

                // 获取课程报名人数
                LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
                bookingQuery.eq(CourseBooking::getCourseId, course.getId());
                bookingQuery.in(CourseBooking::getBookingStatus, 0, 1); // 待上课、已签到
                Long enrollment = courseBookingMapper.selectCount(bookingQuery);

                courseDTO.setCurrentEnrollment(enrollment.intValue());
                courseDTO.setEnrollmentRate(course.getMaxCapacity() > 0 ?
                        (double) enrollment / course.getMaxCapacity() * 100 : 0);

                courseList.add(courseDTO);
            }
        } catch (Exception e) {
            log.error("获取教练课程列表失败，教练ID：{}", coachId, e);
        }

        return courseList;
    }

    /**
     * 将Coach实体转换为CoachListVO
     */
    private CoachListVO convertToCoachListVO(Coach coach) {
        CoachListVO vo = new CoachListVO();

        // 设置基本信息
        vo.setId(coach.getId());
        vo.setRealName(coach.getRealName());
        vo.setPhone(coach.getPhone());
        vo.setSpecialty(coach.getSpecialty());
        vo.setYearsOfExperience(coach.getYearsOfExperience());
        vo.setHourlyRate(coach.getHourlyRate());
        vo.setStatus(coach.getStatus());
        vo.setStatusDesc(coach.getStatus() == 1 ? "在职" : "离职");
        vo.setTotalStudents(coach.getTotalStudents());
        vo.setTotalCourses(coach.getTotalCourses());
        vo.setTotalIncome(coach.getTotalIncome());
        vo.setRating(coach.getRating());
        vo.setCreateTime(coach.getCreateTime());

        // 设置证书信息
        if (StringUtils.hasText(coach.getCertifications())) {
            try {
                String certStr = coach.getCertifications();
                certStr = certStr.replace("[", "").replace("]", "").replace("\"", "");
                vo.setCertifications(Arrays.asList(certStr.split(",")));
            } catch (Exception e) {
                log.error("解析教练证书信息失败，教练ID：{}", coach.getId(), e);
                vo.setCertifications(new ArrayList<>());
            }
        }

        return vo;
    }

    /**
     * 将CoachSchedule实体转换为CoachScheduleDTO
     */
    private CoachScheduleDTO convertToCoachScheduleDTO(CoachSchedule schedule) {
        CoachScheduleDTO dto = new CoachScheduleDTO();
        BeanUtils.copyProperties(schedule, dto);
        dto.setScheduleTypeDesc(schedule.getScheduleType() == 0 ? "私教课" : "团课");
        return dto;
    }

    /**
     * 检查排班时间冲突
     */
    private void checkScheduleConflict(Long coachId, CoachScheduleDTO scheduleDTO) {
        checkScheduleConflict(coachId, scheduleDTO, null);
    }

    /**
     * 检查排班时间冲突（排除指定排班ID）
     */
    private void checkScheduleConflict(Long coachId, CoachScheduleDTO scheduleDTO, Long excludeScheduleId) {
        LambdaQueryWrapper<CoachSchedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoachSchedule::getCoachId, coachId);
        queryWrapper.eq(CoachSchedule::getScheduleDate, scheduleDTO.getScheduleDate());
        queryWrapper.ne(CoachSchedule::getStatus, "CANCELLED");  // 排除已取消的排班

        if (excludeScheduleId != null) {
            queryWrapper.ne(CoachSchedule::getId, excludeScheduleId);
        }

        List<CoachSchedule> existingSchedules = coachScheduleMapper.selectList(queryWrapper);

        for (CoachSchedule existing : existingSchedules) {
            // 检查时间是否重叠
            if (isTimeOverlap(
                    existing.getStartTime(), existing.getEndTime(),
                    scheduleDTO.getStartTime(), scheduleDTO.getEndTime()
            )) {
                throw new BusinessException("排班时间与已有排班冲突：" +
                        existing.getStartTime() + "-" + existing.getEndTime());
            }
        }
    }

    /**
     * 检查时间是否重叠
     */
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return (start1.isBefore(end2) && end1.isAfter(start2));
    }

    /**
     * 检查教练是否有未完成的课程
     */
    private void checkUnfinishedCourses(Long coachId) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCoachId, coachId);
        queryWrapper.ge(Course::getCourseDate, LocalDate.now()); // 未来课程
        queryWrapper.eq(Course::getStatus, 1); // 正常状态的课程

        Long count = courseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("教练有未完成的课程，不能删除");
        }
    }

    /**
     * 检查教练是否有会员关联
     */
    private void checkMemberAssociations(Long coachId) {
        // 根据数据库表结构，检查是否有会员选择该教练作为专属教练
        // 如果member表有personal_coach_id字段，可以添加检查逻辑
        // 示例代码（如果member表有personal_coach_id字段）：
        /*
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Member::getPersonalCoachId, coachId);
        Long count = memberMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该教练还有关联的会员，不能删除");
        }
        */
    }

    /**
     * 检查排班是否有关联课程
     */
    private void checkCourseAssociations(Long scheduleId) {
        // 查询是否有课程关联此排班
        // 这里需要根据业务逻辑实现，如果排班已有课程关联，不能删除
        // 示例代码：
        /*
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCoachScheduleId, scheduleId); // 假设course表有coach_schedule_id字段
        Long count = courseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该排班已有课程关联，不能删除");
        }
        */
    }
}