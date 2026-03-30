package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.dto.coach.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.CoachService;
import com.gymflow.utils.BCryptUtil;
import com.gymflow.utils.SystemConfigValidator;
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
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final ObjectMapper objectMapper;
    private final SystemConfigValidator configValidator;
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
        fullDTO.setGender(coach.getGender());

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

        // 获取教练排班列表（从 course_schedule 查询）
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
        coach.setGender(basicDTO.getGender());

        // 密码处理：如果有传入密码则使用，否则使用默认密码
        String password = StringUtils.hasText(basicDTO.getPassword()) ?
                basicDTO.getPassword() : "123456";
        coach.setPassword(bCryptUtil.encodePassword(password));

        coach.setSpecialty(basicDTO.getSpecialty());

        // 处理证书信息 - 转换为JSON格式
        if (!CollectionUtils.isEmpty(basicDTO.getCertificationList())) {
            try {
                String certJson = objectMapper.writeValueAsString(basicDTO.getCertificationList());
                coach.setCertifications(certJson);
            } catch (Exception e) {
                log.error("证书列表转JSON失败", e);
                throw new BusinessException("证书格式错误");
            }
        } else {
            coach.setCertifications(null);
        }

        coach.setYearsOfExperience(basicDTO.getYearsOfExperience());
        coach.setStatus(1); // 默认在职
        coach.setIntroduction(basicDTO.getIntroduction());

        // 初始化统计信息
        coach.setRating(new BigDecimal("5.00")); // 默认评分5.0

        // 设置创建时间和更新时间
//        LocalDateTime now = LocalDateTime.now();
//        coach.setCreateTime(now);
//        coach.setUpdateTime(now);

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
        coach.setGender(basicDTO.getGender());
        coach.setSpecialty(basicDTO.getSpecialty());

        // 处理证书信息 - 转换为JSON格式
        if (!CollectionUtils.isEmpty(basicDTO.getCertificationList())) {
            try {
                String certJson = objectMapper.writeValueAsString(basicDTO.getCertificationList());
                coach.setCertifications(certJson);
            } catch (Exception e) {
                log.error("证书列表转JSON失败", e);
                throw new BusinessException("证书格式错误");
            }
        } else {
            coach.setCertifications(null);
        }

        coach.setYearsOfExperience(basicDTO.getYearsOfExperience());
        coach.setIntroduction(basicDTO.getIntroduction());

        // 只有密码不为空时才更新密码
        if (StringUtils.hasText(basicDTO.getPassword())) {
            coach.setPassword(bCryptUtil.encodePassword(basicDTO.getPassword()));
        }

        // 设置更新时间
        coach.setUpdateTime(LocalDateTime.now());

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

        // 2. 检查教练是否有未完成的课程排班
        checkUnfinishedCourses(coachId);

        // 3. 软删除：更新状态为离职（不删除记录）
        coach.setStatus(0);
        coach.setUpdateTime(LocalDateTime.now());

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
        coach.setUpdateTime(LocalDateTime.now());

        int result = coachMapper.updateById(coach);
        if (result <= 0) {
            throw new BusinessException("更新教练状态失败");
        }

        log.info("更新教练状态成功，教练ID：{}，新状态：{}", coachId, status);
    }

    @Override
    public List<CoachScheduleDTO> getCoachSchedules(Long coachId) {
        log.info("获取教练排班列表，教练ID：{}", coachId);

        // 从 course_schedule 表查询该教练的排课
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCoachId, coachId)
                .ge(CourseSchedule::getScheduleDate, LocalDate.now())
                .orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(queryWrapper);

        // 转换为 CoachScheduleDTO
        return schedules.stream().map(schedule -> {
            Course course = courseMapper.selectById(schedule.getCourseId());

            CoachScheduleDTO dto = new CoachScheduleDTO();
            dto.setScheduleId(schedule.getScheduleId());
            dto.setCoachId(schedule.getCoachId());
            dto.setScheduleDate(schedule.getScheduleDate());
            dto.setStartTime(schedule.getStartTime());
            dto.setEndTime(schedule.getEndTime());
            dto.setMaxCapacity(schedule.getMaxCapacity());
            dto.setCurrentEnrollment(schedule.getCurrentEnrollment());
            dto.setStatus(schedule.getStatus() == 1 ? "SCHEDULED" : "CANCELLED");

            if (course != null) {
                dto.setCourseId(course.getCourseId());
                dto.setCourseName(course.getCourseName());
                dto.setScheduleType(course.getCourseType());
                dto.setScheduleTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCoachSchedule(Long coachId, CoachScheduleDTO scheduleDTO) {
        // 教练排班通过课程排课实现，不需要单独的排班表
        // 如果需要添加排班，应该创建课程排课
        throw new BusinessException("教练排班请通过课程排课功能添加");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoachSchedule(Long scheduleId, CoachScheduleDTO scheduleDTO) {
        // 教练排班通过课程排课实现，不需要单独的排班表
        throw new BusinessException("教练排班请通过课程排课功能更新");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoachSchedule(Long scheduleId) {
        // 教练排班通过课程排课实现，不需要单独的排班表
        throw new BusinessException("教练排班请通过课程排课功能删除");
    }

    @Override
    public List<CoachCourseDTO> getCoachCourses(Long coachId) {
        // 查询教练的所有排课
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CourseSchedule::getCoachId, coachId)
                .ge(CourseSchedule::getScheduleDate, LocalDate.now())
                .orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(scheduleWrapper);

        return schedules.stream().map(schedule -> {
            Course course = courseMapper.selectById(schedule.getCourseId());

            CoachCourseDTO dto = new CoachCourseDTO();
            dto.setId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setCourseType(course.getCourseType());
            dto.setDescription(course.getDescription());
            dto.setScheduleDate(schedule.getScheduleDate());
            dto.setStartTime(schedule.getStartTime());
            dto.setEndTime(schedule.getEndTime());
            dto.setDuration(course.getDuration());
            dto.setMaxCapacity(schedule.getMaxCapacity());
            dto.setCurrentEnrollment(schedule.getCurrentEnrollment());

            if (schedule.getMaxCapacity() != null && schedule.getMaxCapacity() > 0) {
                double rate = (double) schedule.getCurrentEnrollment() / schedule.getMaxCapacity() * 100;
                dto.setEnrollmentRate(Math.round(rate * 100) / 100.0);
            }

            dto.setStatus(course.getStatus());

            return dto;
        }).collect(Collectors.toList());
    }

    // ========== 辅助方法 ==========

    /**
     * 将Coach实体转换为CoachListVO
     */
    private CoachListVO convertToCoachListVO(Coach coach) {
        CoachListVO vo = new CoachListVO();

        // 设置基本信息
        vo.setId(coach.getId());
        vo.setRealName(coach.getRealName());
        vo.setPhone(coach.getPhone());
        vo.setGender(coach.getGender());
        vo.setSpecialty(coach.getSpecialty());
        vo.setYearsOfExperience(coach.getYearsOfExperience());
        vo.setStatus(coach.getStatus());
        vo.setStatusDesc(coach.getStatus() == 1 ? "在职" : "离职");
        vo.setRating(coach.getRating());
        vo.setCreateTime(coach.getCreateTime());

        // 设置证书信息
        if (StringUtils.hasText(coach.getCertifications())) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> certList = objectMapper.readValue(coach.getCertifications(),
                        new TypeReference<List<String>>() {});
                vo.setCertifications(certList);
            } catch (Exception e) {
                log.error("解析教练证书信息失败，教练ID：{}", coach.getId(), e);
                vo.setCertifications(new ArrayList<>());
            }
        }

        return vo;
    }

    /**
     * 检查教练是否有未完成的课程排班
     */
    private void checkUnfinishedCourses(Long coachId) {
        // 查询该教练未来的排课
        LambdaQueryWrapper<CourseSchedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseSchedule::getCoachId, coachId)
                .ge(CourseSchedule::getScheduleDate, LocalDate.now())
                .eq(CourseSchedule::getStatus, 1);

        Long count = courseScheduleMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("教练有未完成的课程排班，不能删除");
        }
    }
}