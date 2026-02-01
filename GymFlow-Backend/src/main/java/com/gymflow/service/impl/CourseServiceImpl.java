package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.coach.CoachBasicDTO;
import com.gymflow.dto.course.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.CourseService;
import com.gymflow.vo.CourseListVO;
import com.gymflow.vo.CourseScheduleVO;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final MemberMapper memberMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final OrderItemMapper orderItemMapper;
    private final CoachScheduleMapper coachScheduleMapper;
    private final CourseCoachMapper courseCoachMapper;

    @Override
    public PageResultVO<CourseListVO> getCourseList(CourseQueryDTO queryDTO) {
        // 创建分页对象
        Page<Course> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();

        // 课程类型查询
        if (queryDTO.getCourseType() != null) {
            queryWrapper.eq(Course::getCourseType, queryDTO.getCourseType());
        }

        // 课程名称查询
        if (StringUtils.hasText(queryDTO.getCourseName())) {
            queryWrapper.like(Course::getCourseName, queryDTO.getCourseName());
        }

        // 教练ID查询
        if (queryDTO.getCoachId() != null) {
            queryWrapper.eq(Course::getCoachId, queryDTO.getCoachId());
        }

        // 课程日期范围查询
        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge(Course::getCourseDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            queryWrapper.le(Course::getCourseDate, queryDTO.getEndDate());
        }

        // 状态查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Course::getStatus, queryDTO.getStatus());
        }

        // 按课程日期倒序排列
        queryWrapper.orderByDesc(Course::getCourseDate).orderByAsc(Course::getStartTime);

        // 执行分页查询
        IPage<Course> coursePage = courseMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<CourseListVO> voList = coursePage.getRecords().stream()
                .map(course -> {
                    CourseListVO vo = convertToCourseListVO(course);

                    // 获取所有绑定的教练姓名
                    List<String> coachNames = getCourseCoachNames(course.getId());
                    vo.setCoachNames(coachNames);

                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, coursePage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public CourseFullDTO getCourseDetail(Long courseId) {
        // 获取课程信息
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 构建完整DTO
        CourseFullDTO fullDTO = new CourseFullDTO();
        BeanUtils.copyProperties(course, fullDTO);

        // 设置课程类型描述
        fullDTO.setCourseTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");

        // 获取所有绑定的教练信息
        List<CoachBasicDTO> coachDTOs = getCourseCoaches(courseId);
        fullDTO.setCoaches(coachDTOs);

        // 获取预约情况
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getCourseId, courseId);
        bookingQuery.orderByDesc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);

        List<CourseBookingDTO> bookingDTOs = bookings.stream()
                .map(booking -> {
                    CourseBookingDTO dto = new CourseBookingDTO();
                    BeanUtils.copyProperties(booking, dto);

                    // 获取会员信息
                    Member member = memberMapper.selectById(booking.getMemberId());
                    if (member != null) {
                        dto.setMemberName(member.getRealName());
                        dto.setMemberPhone(member.getPhone());
                    }

                    // 设置预约状态描述
                    dto.setBookingStatusDesc(getBookingStatusDesc(booking.getBookingStatus()));

                    return dto;
                })
                .collect(Collectors.toList());

        fullDTO.setBookings(bookingDTOs);

        return fullDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCourse(CourseBasicDTO courseDTO) {
        log.info("开始添加课程，课程名称：{}，教练IDs：{}", courseDTO.getCourseName(), courseDTO.getCoachIds());

        // 验证教练信息
        if (CollectionUtils.isEmpty(courseDTO.getCoachIds())) {
            throw new BusinessException("必须绑定至少一个教练");
        }

        // 验证教练是否存在
        for (Long coachId : courseDTO.getCoachIds()) {
            Coach coach = coachMapper.selectById(coachId);
            if (coach == null) {
                throw new BusinessException("教练ID " + coachId + " 不存在");
            }
        }

        // 创建课程记录（默认绑定第一个教练为主教练）
        Course course = new Course();
        course.setCourseType(courseDTO.getCourseType());
        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setCoachId(courseDTO.getCoachIds().get(0)); // 绑定第一个教练为主教练
        course.setMaxCapacity(courseDTO.getMaxCapacity());
        course.setDuration(courseDTO.getDuration());
        course.setPrice(courseDTO.getPrice());
        course.setLocation(courseDTO.getLocation());
        course.setStatus(1); // 默认启用
        course.setCurrentEnrollment(0); // 初始报名人数为0

        // 保存课程
        int result = courseMapper.insert(course);
        if (result <= 0) {
            throw new BusinessException("添加课程失败");
        }

        Long courseId = course.getId();
        log.info("课程保存成功，课程ID：{}", courseId);

        // 添加教练绑定关系
        addCourseCoaches(courseId, courseDTO.getCoachIds());

        log.info("添加课程成功，课程ID：{}，绑定了 {} 个教练", courseId, courseDTO.getCoachIds().size());

        return courseId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long courseId, CourseBasicDTO courseDTO) {
        log.info("开始更新课程，课程ID：{}，教练IDs：{}", courseId, courseDTO.getCoachIds());

        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 验证教练信息
        if (CollectionUtils.isEmpty(courseDTO.getCoachIds())) {
            throw new BusinessException("必须绑定至少一个教练");
        }

        // 验证教练是否存在
        for (Long coachId : courseDTO.getCoachIds()) {
            Coach coach = coachMapper.selectById(coachId);
            if (coach == null) {
                throw new BusinessException("教练ID " + coachId + " 不存在");
            }
        }

        // 更新课程信息
        course.setCourseType(courseDTO.getCourseType());
        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setCoachId(courseDTO.getCoachIds().get(0)); // 绑定第一个教练为主教练
        course.setMaxCapacity(courseDTO.getMaxCapacity());
        course.setDuration(courseDTO.getDuration());
        course.setPrice(courseDTO.getPrice());
        course.setLocation(courseDTO.getLocation());

        // 更新课程
        int result = courseMapper.updateById(course);
        if (result <= 0) {
            throw new BusinessException("更新课程失败");
        }

        // 更新教练绑定关系
        updateCourseCoaches(courseId, courseDTO.getCoachIds());

        log.info("更新课程成功，课程ID：{}，绑定了 {} 个教练", courseId, courseDTO.getCoachIds().size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        log.info("开始删除课程，课程ID：{}", courseId);

        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 检查是否有预约记录
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getCourseId, courseId);
        bookingQuery.in(CourseBooking::getBookingStatus, 0, 1); // 待上课、已签到

        Long bookingCount = courseBookingMapper.selectCount(bookingQuery);
        if (bookingCount > 0) {
            throw new BusinessException("课程还有未完成的预约，不能删除");
        }

        // 删除教练绑定关系
        LambdaQueryWrapper<CourseCoach> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CourseCoach::getCourseId, courseId);
        int deleteCount = courseCoachMapper.delete(deleteWrapper);
        log.info("已删除课程ID为 {} 的 {} 条教练绑定关系", courseId, deleteCount);

        // 软删除：更新状态为禁用
        course.setStatus(0);

        if (courseMapper.updateById(course) <= 0) {
            throw new BusinessException("删除课程失败");
        }

        log.info("删除课程成功，课程ID：{}", courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseStatus(Long courseId, Integer status) {
        log.info("更新课程状态，课程ID：{}，状态：{}", courseId, status);

        // 检查状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（禁用）或1（正常）");
        }

        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 更新状态
        course.setStatus(status);

        int result = courseMapper.updateById(course);
        if (result <= 0) {
            throw new BusinessException("更新课程状态失败");
        }

        log.info("更新课程状态成功，课程ID：{}，新状态：{}", courseId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scheduleCourse(CourseScheduleDTO scheduleDTO) {
        log.info("开始排课，课程ID：{}，教练ID：{}", scheduleDTO.getCourseId(), scheduleDTO.getCoachId());

        // 检查课程是否存在
        Course course = courseMapper.selectById(scheduleDTO.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 只能为团课排课
        if (course.getCourseType() != 1) {
            throw new BusinessException("只能为团课排课");
        }

        // 检查教练是否存在
        Coach coach = coachMapper.selectById(scheduleDTO.getCoachId());
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 检查教练时间冲突
        checkCoachScheduleConflict(scheduleDTO);

        // 检查课程时间冲突
        checkCourseScheduleConflict(scheduleDTO);

        // 创建课程实例
        Course scheduledCourse = new Course();
        BeanUtils.copyProperties(course, scheduledCourse);
        scheduledCourse.setId(null); // 新的实例
        scheduledCourse.setCoachId(scheduleDTO.getCoachId());
        scheduledCourse.setCourseDate(scheduleDTO.getCourseDate());
        scheduledCourse.setStartTime(scheduleDTO.getStartTime());
        scheduledCourse.setEndTime(scheduleDTO.getEndTime());
        scheduledCourse.setCurrentEnrollment(0);

        // 设置签到码
        scheduledCourse.setSignCode(generateSignCode());
        scheduledCourse.setSignCodeExpireTime(LocalDateTime.now().plusDays(1));

        // 保存排课
        int result = courseMapper.insert(scheduledCourse);
        if (result <= 0) {
            throw new BusinessException("排课失败");
        }

        Long scheduledCourseId = scheduledCourse.getId();

        // 获取原课程的所有教练绑定关系
        List<CourseCoach> originalCourseCoaches = courseCoachMapper.selectList(
                new LambdaQueryWrapper<CourseCoach>().eq(CourseCoach::getCourseId, scheduleDTO.getCourseId())
        );

        // 为新排课复制教练绑定关系
        if (!CollectionUtils.isEmpty(originalCourseCoaches)) {
            for (CourseCoach original : originalCourseCoaches) {
                CourseCoach newCourseCoach = new CourseCoach();
                newCourseCoach.setCourseId(scheduledCourseId);
                newCourseCoach.setCoachId(original.getCoachId());
                courseCoachMapper.insert(newCourseCoach);
            }
            log.info("为新排课ID {} 复制了 {} 个教练绑定关系", scheduledCourseId, originalCourseCoaches.size());
        }

        // 创建教练排班记录
        createCoachSchedule(scheduleDTO, scheduledCourseId);

        log.info("排课成功，排课ID：{}", scheduledCourseId);
    }

    @Override
    public List<CourseScheduleVO> getCourseSchedules(Long courseId) {
        log.info("获取课程排课列表，课程ID：{}", courseId);

        // 查询该课程的所有排课
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getId, courseId);
        queryWrapper.ge(Course::getCourseDate, LocalDate.now()); // 只查询未来排课
        queryWrapper.orderByAsc(Course::getCourseDate).orderByAsc(Course::getStartTime);

        List<Course> schedules = courseMapper.selectList(queryWrapper);

        return schedules.stream()
                .map(this::convertToCourseScheduleVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseScheduleVO> getCourseTimetable(LocalDate startDate, LocalDate endDate) {
        log.info("获取课程表，开始日期：{}，结束日期：{}", startDate, endDate);

        // 查询指定日期范围内的所有课程
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge(Course::getCourseDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(Course::getCourseDate, endDate);
        }
        queryWrapper.eq(Course::getStatus, 1);
        queryWrapper.orderByAsc(Course::getCourseDate).orderByAsc(Course::getStartTime);

        List<Course> courses = courseMapper.selectList(queryWrapper);

        return courses.stream()
                .map(course -> {
                    CourseScheduleVO vo = convertToCourseScheduleVO(course);

                    // 获取该课程的预约信息
                    LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
                    bookingQuery.eq(CourseBooking::getCourseId, course.getId());
                    bookingQuery.in(CourseBooking::getBookingStatus, 0, 1); // 待上课、已签到

                    List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
                    vo.setBookings(bookings.stream()
                            .map(this::convertToCourseBookingVO)
                            .collect(Collectors.toList()));

                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookPrivateCourse(Long memberId, Long coachId, LocalDate courseDate, LocalTime startTime) {
        log.info("会员预约私教课，会员ID：{}，教练ID：{}", memberId, coachId);

        // 检查会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 检查教练是否存在
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 检查会员是否有可用的私教课程
        checkMemberPrivateCourseSessions(memberId);

        // 创建私教课程实例
        Course privateCourse = new Course();
        privateCourse.setCourseType(0); // 私教课
        privateCourse.setCourseName(coach.getRealName() + "私教课");
        privateCourse.setDescription("私教一对一课程");
        privateCourse.setCoachId(coachId);
        privateCourse.setMaxCapacity(1);
        privateCourse.setCurrentEnrollment(1);
        privateCourse.setCourseDate(courseDate);
        privateCourse.setStartTime(startTime);
        privateCourse.setEndTime(startTime.plusHours(1)); // 默认1小时
        privateCourse.setDuration(60);
        privateCourse.setPrice(coach.getHourlyRate());
        privateCourse.setLocation("私教区");
        privateCourse.setStatus(1);
        privateCourse.setSignCode(generateSignCode());
        privateCourse.setSignCodeExpireTime(LocalDateTime.now().plusDays(1));

        // 保存课程
        int result = courseMapper.insert(privateCourse);
        if (result <= 0) {
            throw new BusinessException("创建私教课失败");
        }

        Long privateCourseId = privateCourse.getId();

        // 为私教课添加教练绑定
        CourseCoach courseCoach = new CourseCoach();
        courseCoach.setCourseId(privateCourseId);
        courseCoach.setCoachId(coachId);
        courseCoachMapper.insert(courseCoach);

        // 创建预约记录
        createCourseBooking(memberId, privateCourseId);

        // 创建教练排班记录
        CourseScheduleDTO scheduleDTO = new CourseScheduleDTO();
        scheduleDTO.setCourseId(privateCourseId);
        scheduleDTO.setCoachId(coachId);
        scheduleDTO.setCourseDate(courseDate);
        scheduleDTO.setStartTime(startTime);
        scheduleDTO.setEndTime(startTime.plusHours(1));
        scheduleDTO.setMaxParticipants(1);
        scheduleDTO.setNotes("会员预约私教课");

        createCoachSchedule(scheduleDTO, privateCourseId);

        // 扣减会员私教课时
        deductMemberPrivateCourseSession(memberId);

        log.info("私教课预约成功，课程ID：{}", privateCourseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookGroupCourse(Long memberId, Long scheduleId) {
        log.info("会员预约团课，会员ID：{}，排课ID：{}", memberId, scheduleId);

        // 检查课程是否存在
        Course course = courseMapper.selectById(scheduleId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 只能预约团课
        if (course.getCourseType() != 1) {
            throw new BusinessException("只能预约团课");
        }

        // 检查会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 检查会员是否有可用的团课程
        checkMemberGroupCourseSessions(memberId);

        // 检查课程是否已满
        if (course.getCurrentEnrollment() >= course.getMaxCapacity()) {
            throw new BusinessException("课程已满");
        }

        // 检查是否已经预约
        LambdaQueryWrapper<CourseBooking> existingQuery = new LambdaQueryWrapper<>();
        existingQuery.eq(CourseBooking::getMemberId, memberId);
        existingQuery.eq(CourseBooking::getCourseId, scheduleId);
        existingQuery.in(CourseBooking::getBookingStatus, 0, 1); // 待上课、已签到

        Long existingCount = courseBookingMapper.selectCount(existingQuery);
        if (existingCount > 0) {
            throw new BusinessException("已经预约了该课程");
        }

        // 创建预约记录
        createCourseBooking(memberId, scheduleId);

        // 更新课程当前报名人数
        course.setCurrentEnrollment(course.getCurrentEnrollment() + 1);
        courseMapper.updateById(course);

        // 扣减会员团课时
        deductMemberGroupCourseSession(memberId);

        log.info("团课预约成功，课程ID：{}，会员ID：{}", scheduleId, memberId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyCourseBooking(Long bookingId, Integer checkinMethod) {
        log.info("核销课程预约，预约ID：{}，签到方式：{}", bookingId, checkinMethod);

        // 检查预约是否存在
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约记录不存在");
        }

        // 检查预约状态
        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("预约状态不正确，无法核销");
        }

        // 更新预约状态为已签到
        booking.setBookingStatus(1);
        booking.setCheckinTime(LocalDateTime.now());

        int result = courseBookingMapper.updateById(booking);
        if (result <= 0) {
            throw new BusinessException("核销失败");
        }

        // 创建签到记录
        createCheckinRecord(booking, checkinMethod);

        // 更新会员签到次数
        updateMemberCheckinStats(booking.getMemberId());

        log.info("核销成功，预约ID：{}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCourseBooking(Long bookingId, String reason) {
        log.info("取消课程预约，预约ID：{}", bookingId);

        // 检查预约是否存在
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约记录不存在");
        }

        // 检查预约状态
        if (booking.getBookingStatus() == 2) {
            throw new BusinessException("已完成的预约不能取消");
        }
        if (booking.getBookingStatus() == 3) {
            throw new BusinessException("预约已取消");
        }

        // 更新预约状态为已取消
        booking.setBookingStatus(3);
        booking.setCancellationReason(reason);
        booking.setCancellationTime(LocalDateTime.now());

        int result = courseBookingMapper.updateById(booking);
        if (result <= 0) {
            throw new BusinessException("取消预约失败");
        }

        // 如果是团课，减少当前报名人数
        Course course = courseMapper.selectById(booking.getCourseId());
        if (course != null && course.getCourseType() == 1) {
            course.setCurrentEnrollment(Math.max(0, course.getCurrentEnrollment() - 1));
            courseMapper.updateById(course);
        }

        // 返还会员课时
        returnMemberCourseSession(booking.getMemberId(), course.getCourseType());

        log.info("取消预约成功，预约ID：{}", bookingId);
    }

    // ========== 私有辅助方法 ==========

    private CourseListVO convertToCourseListVO(Course course) {
        CourseListVO vo = new CourseListVO();
        BeanUtils.copyProperties(course, vo);

        // 设置课程类型描述
        vo.setCourseTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");

        // 设置状态描述
        vo.setStatusDesc(course.getStatus() == 1 ? "正常" : "禁用");

        // 计算报名率
        if (course.getMaxCapacity() > 0) {
            BigDecimal enrollmentRate = BigDecimal.valueOf(course.getCurrentEnrollment())
                    .divide(BigDecimal.valueOf(course.getMaxCapacity()), 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            vo.setEnrollmentRate(enrollmentRate);
        }

        // 教练姓名在调用方法中设置
        return vo;
    }

    private CourseScheduleVO convertToCourseScheduleVO(Course course) {
        CourseScheduleVO vo = new CourseScheduleVO();
        vo.setScheduleId(course.getId());
        vo.setCourseId(course.getId());
        vo.setCourseName(course.getCourseName());
        vo.setCourseType(course.getCourseType() == 0 ? "私教课" : "团课");
        vo.setCoachId(course.getCoachId());
        vo.setScheduleDate(course.getCourseDate());
        vo.setStartTime(course.getStartTime());
        vo.setEndTime(course.getEndTime());
        vo.setMaxParticipants(course.getMaxCapacity());
        vo.setCurrentParticipants(course.getCurrentEnrollment());
        vo.setRemainingSlots(Math.max(0, course.getMaxCapacity() - course.getCurrentEnrollment()));
        vo.setStatus(course.getStatus() == 1 ? "正常" : "禁用");

        // 获取教练姓名
        Coach coach = coachMapper.selectById(course.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getRealName());
        }

        return vo;
    }

    private com.gymflow.vo.CourseBookingVO convertToCourseBookingVO(CourseBooking booking) {
        com.gymflow.vo.CourseBookingVO vo = new com.gymflow.vo.CourseBookingVO();
        BeanUtils.copyProperties(booking, vo);

        // 获取会员信息
        Member member = memberMapper.selectById(booking.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
            vo.setMemberPhone(member.getPhone());
        }

        // 设置预约状态描述
        vo.setBookingStatusDesc(getBookingStatusDesc(booking.getBookingStatus()));

        return vo;
    }

    private String getBookingStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待上课";
            case 1: return "已签到";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }

    private void checkCoachScheduleConflict(CourseScheduleDTO scheduleDTO) {
        // 检查教练在指定时间是否已有排班
        LambdaQueryWrapper<CoachSchedule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CoachSchedule::getCoachId, scheduleDTO.getCoachId());
        queryWrapper.eq(CoachSchedule::getScheduleDate, scheduleDTO.getCourseDate());
        queryWrapper.ne(CoachSchedule::getStatus, "CANCELLED");

        List<CoachSchedule> schedules = coachScheduleMapper.selectList(queryWrapper);

        for (CoachSchedule schedule : schedules) {
            if (isTimeOverlap(
                    schedule.getStartTime(), schedule.getEndTime(),
                    scheduleDTO.getStartTime(), scheduleDTO.getEndTime()
            )) {
                throw new BusinessException("教练在该时间段已有排班：" +
                        schedule.getStartTime() + "-" + schedule.getEndTime());
            }
        }
    }

    private void checkCourseScheduleConflict(CourseScheduleDTO scheduleDTO) {
        // 检查同一地点在指定时间是否已有课程
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getLocation, getCourseLocation(scheduleDTO.getCourseId()));
        queryWrapper.eq(Course::getCourseDate, scheduleDTO.getCourseDate());
        queryWrapper.eq(Course::getStatus, 1);

        List<Course> courses = courseMapper.selectList(queryWrapper);

        for (Course course : courses) {
            if (isTimeOverlap(
                    course.getStartTime(), course.getEndTime(),
                    scheduleDTO.getStartTime(), scheduleDTO.getEndTime()
            )) {
                throw new BusinessException("该地点在该时间段已有课程：" +
                        course.getStartTime() + "-" + course.getEndTime());
            }
        }
    }

    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return (start1.isBefore(end2) && end1.isAfter(start2));
    }

    private String getCourseLocation(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        return course != null ? course.getLocation() : "";
    }

    private void createCoachSchedule(CourseScheduleDTO scheduleDTO, Long courseId) {
        CoachSchedule coachSchedule = new CoachSchedule();
        coachSchedule.setCoachId(scheduleDTO.getCoachId());
        coachSchedule.setScheduleDate(scheduleDTO.getCourseDate());
        coachSchedule.setStartTime(scheduleDTO.getStartTime());
        coachSchedule.setEndTime(scheduleDTO.getEndTime());
        coachSchedule.setScheduleType(1); // 课程排班
        coachSchedule.setStatus("SCHEDULED");
        coachSchedule.setNotes(scheduleDTO.getNotes() != null ? scheduleDTO.getNotes() : "课程排班");

        coachScheduleMapper.insert(coachSchedule);
    }

    private void createCourseBooking(Long memberId, Long courseId) {
        CourseBooking booking = new CourseBooking();
        booking.setMemberId(memberId);
        booking.setCourseId(courseId);
        booking.setBookingTime(LocalDateTime.now());
        booking.setBookingStatus(0); // 待上课

        courseBookingMapper.insert(booking);
    }

    private void checkMemberPrivateCourseSessions(Long memberId) {
        // 查询会员的私教课卡
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, 1); // 私教课
        queryWrapper.gt(OrderItem::getRemainingSessions, 0); // 剩余课时大于0
        queryWrapper.ge(OrderItem::getValidityEndDate, LocalDate.now()); // 未过期
        queryWrapper.eq(OrderItem::getStatus, "ACTIVE");

        // 这里需要根据实际订单查询会员的私教课卡
        // 简化实现：假设会员有课时
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (orderItems.isEmpty()) {
            throw new BusinessException("没有可用的私教课时");
        }
    }

    private void checkMemberGroupCourseSessions(Long memberId) {
        // 查询会员的团课卡
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, 2); // 团课
        queryWrapper.gt(OrderItem::getRemainingSessions, 0); // 剩余课时大于0
        queryWrapper.ge(OrderItem::getValidityEndDate, LocalDate.now()); // 未过期
        queryWrapper.eq(OrderItem::getStatus, "ACTIVE");

        // 这里需要根据实际订单查询会员的团课卡
        // 简化实现：假设会员有课时
        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (orderItems.isEmpty()) {
            throw new BusinessException("没有可用的团课时");
        }
    }

    private void deductMemberPrivateCourseSession(Long memberId) {
        // 扣减会员私教课时
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, 1); // 私教课
        queryWrapper.gt(OrderItem::getRemainingSessions, 0); // 剩余课时大于0
        queryWrapper.ge(OrderItem::getValidityEndDate, LocalDate.now()); // 未过期
        queryWrapper.eq(OrderItem::getStatus, "ACTIVE");
        queryWrapper.orderByAsc(OrderItem::getValidityEndDate); // 先用快要过期的

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (!orderItems.isEmpty()) {
            OrderItem orderItem = orderItems.get(0);
            orderItem.setRemainingSessions(orderItem.getRemainingSessions() - 1);

            // 如果课时用完，更新状态
            if (orderItem.getRemainingSessions() <= 0) {
                orderItem.setStatus("USED_UP");
            }

            orderItemMapper.updateById(orderItem);
        }
    }

    private void deductMemberGroupCourseSession(Long memberId) {
        // 扣减会员团课时
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, 2); // 团课
        queryWrapper.gt(OrderItem::getRemainingSessions, 0); // 剩余课时大于0
        queryWrapper.ge(OrderItem::getValidityEndDate, LocalDate.now()); // 未过期
        queryWrapper.eq(OrderItem::getStatus, "ACTIVE");
        queryWrapper.orderByAsc(OrderItem::getValidityEndDate); // 先用快要过期的

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (!orderItems.isEmpty()) {
            OrderItem orderItem = orderItems.get(0);
            orderItem.setRemainingSessions(orderItem.getRemainingSessions() - 1);

            // 如果课时用完，更新状态
            if (orderItem.getRemainingSessions() <= 0) {
                orderItem.setStatus("USED_UP");
            }

            orderItemMapper.updateById(orderItem);
        }
    }

    private void returnMemberCourseSession(Long memberId, Integer courseType) {
        // 返还会员课时
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItem::getProductType, courseType == 0 ? 1 : 2);
        queryWrapper.ge(OrderItem::getValidityEndDate, LocalDate.now());
        queryWrapper.eq(OrderItem::getStatus, "ACTIVE");
        queryWrapper.orderByDesc(OrderItem::getRemainingSessions);

        List<OrderItem> orderItems = orderItemMapper.selectList(queryWrapper);
        if (!orderItems.isEmpty()) {
            OrderItem orderItem = orderItems.get(0);
            orderItem.setRemainingSessions(orderItem.getRemainingSessions() + 1);
            orderItemMapper.updateById(orderItem);
        }
    }

    private void createCheckinRecord(CourseBooking booking, Integer checkinMethod) {
        CheckinRecord checkinRecord = new CheckinRecord();
        checkinRecord.setMemberId(booking.getMemberId());
        checkinRecord.setCourseBookingId(booking.getId());
        checkinRecord.setCheckinTime(LocalDateTime.now());
        checkinRecord.setCheckinMethod(checkinMethod);
        checkinRecord.setNotes("课程签到");

        checkInRecordMapper.insert(checkinRecord);
    }

    private void updateMemberCheckinStats(Long memberId) {
        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            member.setTotalCheckins((member.getTotalCheckins() != null ? member.getTotalCheckins() : 0) + 1);
            memberMapper.updateById(member);
        }
    }

    private String generateSignCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    /**
     * 更新课程教练绑定关系
     */
    private void updateCourseCoaches(Long courseId, List<Long> coachIds) {
        log.info("开始更新课程ID {} 的教练绑定关系，新教练IDs：{}", courseId, coachIds);

        // 检查现有的教练绑定
        LambdaQueryWrapper<CourseCoach> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCoach::getCourseId, courseId);
        List<CourseCoach> existingCoaches = courseCoachMapper.selectList(queryWrapper);
        log.info("课程ID {} 现有的教练绑定关系：{}", courseId, existingCoaches);

        // 删除原有的教练绑定
        LambdaQueryWrapper<CourseCoach> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CourseCoach::getCourseId, courseId);
        int deleteCount = courseCoachMapper.delete(deleteWrapper);
        log.info("删除课程ID为 {} 的原有教练绑定关系，删除了 {} 条记录", courseId, deleteCount);

        // 添加新的教练绑定
        addCourseCoaches(courseId, coachIds);
    }

    /**
     * 添加课程教练绑定关系
     */
    private void addCourseCoaches(Long courseId, List<Long> coachIds) {
        log.info("开始为课程ID {} 添加教练绑定关系，教练IDs：{}", courseId, coachIds);

        List<CourseCoach> courseCoaches = new ArrayList<>();
        for (Long coachId : coachIds) {
            CourseCoach courseCoach = new CourseCoach();
            courseCoach.setCourseId(courseId);
            courseCoach.setCoachId(coachId);
            courseCoaches.add(courseCoach);
        }

        // 批量插入
        if (!CollectionUtils.isEmpty(courseCoaches)) {
            int successCount = 0;
            for (CourseCoach courseCoach : courseCoaches) {
                try {
                    log.debug("正在插入教练绑定关系：课程ID={}, 教练ID={}", courseCoach.getCourseId(), courseCoach.getCoachId());
                    int result = courseCoachMapper.insert(courseCoach);
                    if (result > 0) {
                        successCount++;
                        log.debug("成功插入教练绑定关系：课程ID={}, 教练ID={}", courseCoach.getCourseId(), courseCoach.getCoachId());
                    } else {
                        log.error("插入教练绑定关系失败：课程ID={}, 教练ID={}", courseCoach.getCourseId(), courseCoach.getCoachId());
                    }
                } catch (Exception e) {
                    log.error("插入教练绑定关系异常，课程ID: {}, 教练ID: {}", courseId, courseCoach.getCoachId(), e);
                }
            }
            log.info("为课程ID {} 成功添加了 {} 个教练绑定关系，总共尝试了 {} 个", courseId, successCount, courseCoaches.size());

            if (successCount == 0) {
                throw new BusinessException("添加教练绑定关系失败");
            }

            // 验证插入的数据
            LambdaQueryWrapper<CourseCoach> verifyWrapper = new LambdaQueryWrapper<>();
            verifyWrapper.eq(CourseCoach::getCourseId, courseId);
            List<CourseCoach> verifyList = courseCoachMapper.selectList(verifyWrapper);
            log.info("验证课程ID {} 的教练绑定关系，数据库中有 {} 条记录", courseId, verifyList.size());
        } else {
            log.warn("没有教练需要绑定到课程ID {}", courseId);
        }
    }

    /**
     * 获取课程绑定的所有教练
     */
    private List<CoachBasicDTO> getCourseCoaches(Long courseId) {
        log.info("开始获取课程ID {} 的教练信息", courseId);

        // 查询课程绑定的所有教练
        LambdaQueryWrapper<CourseCoach> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCoach::getCourseId, courseId);

        List<CourseCoach> courseCoaches = courseCoachMapper.selectList(queryWrapper);
        log.info("查询课程ID {} 的教练绑定关系，找到 {} 条记录", courseId, courseCoaches.size());

        List<CoachBasicDTO> coachDTOs = new ArrayList<>();

        for (CourseCoach courseCoach : courseCoaches) {
            Coach coach = coachMapper.selectById(courseCoach.getCoachId());
            if (coach != null) {
                CoachBasicDTO coachDTO = new CoachBasicDTO();
                BeanUtils.copyProperties(coach, coachDTO);
                // 确保设置教练ID
                coachDTO.setId(coach.getId());
                coachDTOs.add(coachDTO);
                log.debug("找到教练: ID={}, Name={}", coach.getId(), coach.getRealName());
            } else {
                log.warn("教练ID {} 不存在", courseCoach.getCoachId());
            }
        }

        log.info("课程ID {} 共绑定了 {} 个教练", courseId, coachDTOs.size());
        return coachDTOs;
    }

    /**
     * 获取课程绑定的所有教练姓名
     */
    private List<String> getCourseCoachNames(Long courseId) {
        LambdaQueryWrapper<CourseCoach> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseCoach::getCourseId, courseId);

        List<CourseCoach> courseCoaches = courseCoachMapper.selectList(queryWrapper);

        List<String> coachNames = new ArrayList<>();
        for (CourseCoach courseCoach : courseCoaches) {
            Coach coach = coachMapper.selectById(courseCoach.getCoachId());
            if (coach != null && StringUtils.hasText(coach.getRealName())) {
                coachNames.add(coach.getRealName());
            }
        }

        return coachNames;
    }

    // 以下方法需要根据业务需求实现
    @Override
    public void updateCourseSchedule(Long scheduleId, CourseScheduleDTO scheduleDTO) {
        // 实现更新排课逻辑
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    public void deleteCourseSchedule(Long scheduleId) {
        // 实现删除排课逻辑
        throw new UnsupportedOperationException("待实现");
    }
}