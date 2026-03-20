package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.coach.CoachBasicDTO;
import com.gymflow.dto.course.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.CourseService;
import com.gymflow.utils.SystemConfigValidator;
import com.gymflow.vo.CourseBookingVO;
import com.gymflow.vo.CourseListVO;
import com.gymflow.vo.CourseScheduleVO;
import com.gymflow.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final CourseCoachMapper courseCoachMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final OrderItemMapper orderItemMapper;
    private final SystemConfigValidator configValidator;

    @Override
    public PageResultVO<CourseListVO> getCourseList(CourseQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getCourseType() != null) {
            wrapper.eq(Course::getCourseType, queryDTO.getCourseType());
        }
        if (queryDTO.getCourseName() != null && !queryDTO.getCourseName().isEmpty()) {
            wrapper.like(Course::getCourseName, queryDTO.getCourseName());
        }
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Course::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Course::getCreateTime);

        // 分页查询
        Page<Course> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Course> coursePage = courseMapper.selectPage(page, wrapper);

        // 获取所有课程ID
        List<Long> courseIds = coursePage.getRecords().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        // 如果有教练筛选条件，需要过滤课程
        if (queryDTO.getCoachId() != null && !courseIds.isEmpty()) {
            // 查询包含指定教练的课程ID
            LambdaQueryWrapper<CourseCoach> coachWrapper = new LambdaQueryWrapper<>();
            coachWrapper.in(CourseCoach::getCourseId, courseIds)
                    .eq(CourseCoach::getCoachId, queryDTO.getCoachId());

            List<CourseCoach> courseCoaches = courseCoachMapper.selectList(coachWrapper);
            Set<Long> validCourseIds = courseCoaches.stream()
                    .map(CourseCoach::getCourseId)
                    .collect(Collectors.toSet());

            // 只保留包含指定教练的课程
            List<Course> filteredCourses = coursePage.getRecords().stream()
                    .filter(course -> validCourseIds.contains(course.getId()))
                    .collect(Collectors.toList());

            // 重新设置分页数据
            coursePage.setRecords(filteredCourses);
            coursePage.setTotal(filteredCourses.size());
        }

        // 转换为VO
        List<CourseListVO> list = coursePage.getRecords().stream().map(course -> {
            CourseListVO vo = new CourseListVO();
            vo.setId(course.getId());
            vo.setCourseType(course.getCourseType());
            vo.setCourseName(course.getCourseName());
            vo.setDescription(course.getDescription());
            vo.setDuration(course.getDuration());
            vo.setPrice(course.getPrice());
            vo.setStatus(course.getStatus());

            // 查询绑定教练
            List<Long> coachIds = courseCoachMapper.selectCoachIdsByCourseId(course.getId());
            if (!coachIds.isEmpty()) {
                List<Coach> coaches = coachMapper.selectBatchIds(coachIds);
                List<String> coachNames = coaches.stream()
                        .map(Coach::getRealName)
                        .collect(Collectors.toList());
                vo.setCoachNames(coachNames);
            }

            // 查询统计信息
            LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
            scheduleWrapper.eq(CourseSchedule::getCourseId, course.getId());
            Long totalSchedules = courseScheduleMapper.selectCount(scheduleWrapper);
            vo.setTotalSchedules(totalSchedules.intValue());

            LambdaQueryWrapper<CourseBooking> bookingWrapper = new LambdaQueryWrapper<>();
            bookingWrapper.eq(CourseBooking::getCourseId, course.getId());
            Long totalBookings = courseBookingMapper.selectCount(bookingWrapper);
            vo.setTotalBookings(totalBookings.intValue());

            return vo;
        }).collect(Collectors.toList());

        return new PageResultVO<>(list, coursePage.getTotal(), queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public CourseFullDTO getCourseDetail(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        CourseFullDTO dto = new CourseFullDTO();
        dto.setId(course.getId());
        dto.setCourseType(course.getCourseType());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setPrice(course.getPrice());
        dto.setStatus(course.getStatus());
        dto.setNotice(course.getNotice());
        dto.setCreateTime(course.getCreateTime());
        dto.setUpdateTime(course.getUpdateTime());

        // 查询绑定教练
        List<Long> coachIds = courseCoachMapper.selectCoachIdsByCourseId(courseId);
        if (!coachIds.isEmpty()) {
            List<Coach> coaches = coachMapper.selectBatchIds(coachIds);
            dto.setCoaches(convertToCoachBasicDTO(coaches));
        }

        // 查询排课列表（包含预约信息）
        List<CourseScheduleVO> schedules = getCourseSchedules(courseId);
        dto.setSchedules(schedules);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCourse(CourseBasicDTO courseDTO) {
        // 验证课程名称是否重复
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getCourseName, courseDTO.getCourseName());
        if (courseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("课程名称已存在");
        }

        // 验证教练ID是否有效
        for (Long coachId : courseDTO.getCoachIds()) {
            Coach coach = coachMapper.selectById(coachId);
            if (coach == null) {
                throw new BusinessException("教练ID " + coachId + " 不存在");
            }
        }

        // 创建课程
        Course course = new Course();
        course.setCourseType(courseDTO.getCourseType());
        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setDuration(courseDTO.getDuration());
        course.setPrice(courseDTO.getPrice());
        course.setNotice(courseDTO.getNotice());
        course.setStatus(1); // 默认正常

        courseMapper.insert(course);

        // 绑定教练
        for (Long coachId : courseDTO.getCoachIds()) {
            CourseCoach courseCoach = new CourseCoach();
            courseCoach.setCourseId(course.getId());
            courseCoach.setCoachId(coachId);
            courseCoachMapper.insert(courseCoach);
        }

        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long courseId, CourseBasicDTO courseDTO) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 验证课程名称是否重复（排除自己）
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getCourseName, courseDTO.getCourseName())
                .ne(Course::getId, courseId);
        if (courseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("课程名称已存在");
        }

        // 验证教练ID是否有效
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
        course.setDuration(courseDTO.getDuration());
        course.setPrice(courseDTO.getPrice());
        course.setNotice(courseDTO.getNotice());

        courseMapper.updateById(course);

        // 更新教练绑定（先删除后添加）
        LambdaQueryWrapper<CourseCoach> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(CourseCoach::getCourseId, courseId);
        courseCoachMapper.delete(deleteWrapper);

        for (Long coachId : courseDTO.getCoachIds()) {
            CourseCoach courseCoach = new CourseCoach();
            courseCoach.setCourseId(courseId);
            courseCoach.setCoachId(coachId);
            courseCoachMapper.insert(courseCoach);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        // 检查是否有排课
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CourseSchedule::getCourseId, courseId);
        Long scheduleCount = courseScheduleMapper.selectCount(scheduleWrapper);
        if (scheduleCount > 0) {
            throw new BusinessException("该课程已有排课，无法删除");
        }

        // 检查是否有预约记录
        LambdaQueryWrapper<CourseBooking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(CourseBooking::getCourseId, courseId);
        Long bookingCount = courseBookingMapper.selectCount(bookingWrapper);
        if (bookingCount > 0) {
            throw new BusinessException("该课程已有预约记录，无法删除");
        }

        // 删除课程
        courseMapper.deleteById(courseId);

        // 删除教练关联
        LambdaQueryWrapper<CourseCoach> coachWrapper = new LambdaQueryWrapper<>();
        coachWrapper.eq(CourseCoach::getCourseId, courseId);
        courseCoachMapper.delete(coachWrapper);
    }

    @Override
    public void updateCourseStatus(Long courseId, Integer status) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        course.setStatus(status);
        courseMapper.updateById(course);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scheduleCourse(CourseScheduleDTO scheduleDTO) {
        // 验证课程是否存在
        Course course = courseMapper.selectById(scheduleDTO.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 验证教练是否可授课该课程
        LambdaQueryWrapper<CourseCoach> coachWrapper = new LambdaQueryWrapper<>();
        coachWrapper.eq(CourseCoach::getCourseId, scheduleDTO.getCourseId())
                .eq(CourseCoach::getCoachId, scheduleDTO.getCoachId());
        if (courseCoachMapper.selectCount(coachWrapper) == 0) {
            throw new BusinessException("该教练不能教授此课程");
        }

        // 验证教练在该时间段是否已有排课
        LambdaQueryWrapper<CourseSchedule> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.eq(CourseSchedule::getCoachId, scheduleDTO.getCoachId())
                .eq(CourseSchedule::getScheduleDate, scheduleDTO.getScheduleDate())
                .and(w -> w.between(CourseSchedule::getStartTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime())
                        .or(b -> b.between(CourseSchedule::getEndTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime())
                                .or(c -> c.le(CourseSchedule::getStartTime, scheduleDTO.getStartTime())
                                        .ge(CourseSchedule::getEndTime, scheduleDTO.getEndTime()))));

        if (courseScheduleMapper.selectCount(conflictWrapper) > 0) {
            throw new BusinessException("教练在该时间段已有排课");
        }

        // 验证营业时间
        configValidator.validateBusinessHours(scheduleDTO.getStartTime(), scheduleDTO.getEndTime());

        // 验证课程容量 - 从 scheduleDTO 获取
        if (scheduleDTO.getMaxCapacity() != null) {
            configValidator.validateClassCapacity(0, scheduleDTO.getMaxCapacity());
        }

        // 创建排课
        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(scheduleDTO.getCourseId());
        schedule.setCoachId(scheduleDTO.getCoachId());
        schedule.setScheduleDate(scheduleDTO.getScheduleDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        // 从 DTO 获取 maxCapacity，如果没有则使用默认值 20
        schedule.setMaxCapacity(scheduleDTO.getMaxCapacity() != null ? scheduleDTO.getMaxCapacity() : 20);
        schedule.setCurrentEnrollment(0);
        schedule.setStatus(1); // 正常

        LocalDateTime now = LocalDateTime.now();
        schedule.setCreateTime(now);
        schedule.setUpdateTime(now);

        courseScheduleMapper.insert(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourseSchedule(Long scheduleId, CourseScheduleDTO scheduleDTO) {
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        // 如果有预约，不能修改关键信息
        if (schedule.getCurrentEnrollment() > 0) {
            throw new BusinessException("该排课已有会员预约，无法修改");
        }

        // 验证教练是否可授课该课程
        LambdaQueryWrapper<CourseCoach> coachWrapper = new LambdaQueryWrapper<>();
        coachWrapper.eq(CourseCoach::getCourseId, scheduleDTO.getCourseId())
                .eq(CourseCoach::getCoachId, scheduleDTO.getCoachId());
        if (courseCoachMapper.selectCount(coachWrapper) == 0) {
            throw new BusinessException("该教练不能教授此课程");
        }

        // 验证教练在该时间段是否已有排课（排除自己）
        LambdaQueryWrapper<CourseSchedule> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.eq(CourseSchedule::getCoachId, scheduleDTO.getCoachId())
                .eq(CourseSchedule::getScheduleDate, scheduleDTO.getScheduleDate())
                .ne(CourseSchedule::getId, scheduleId)
                .and(w -> w.between(CourseSchedule::getStartTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime())
                        .or(b -> b.between(CourseSchedule::getEndTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime())
                                .or(c -> c.le(CourseSchedule::getStartTime, scheduleDTO.getStartTime())
                                        .ge(CourseSchedule::getEndTime, scheduleDTO.getEndTime()))));

        if (courseScheduleMapper.selectCount(conflictWrapper) > 0) {
            throw new BusinessException("教练在该时间段已有排课");
        }

        // 验证营业时间
        configValidator.validateBusinessHours(scheduleDTO.getStartTime(), scheduleDTO.getEndTime());

        // 更新时间等
        schedule.setCoachId(scheduleDTO.getCoachId());
        schedule.setScheduleDate(scheduleDTO.getScheduleDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        if (scheduleDTO.getMaxCapacity() != null) {
            schedule.setMaxCapacity(scheduleDTO.getMaxCapacity());
        }

        courseScheduleMapper.updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseSchedule(Long scheduleId) {
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        // 检查是否有预约
        if (schedule.getCurrentEnrollment() > 0) {
            throw new BusinessException("该排课已有会员预约，无法删除");
        }

        courseScheduleMapper.deleteById(scheduleId);
    }

    @Override
    public List<CourseScheduleVO> getCourseSchedules(Long courseId) {
        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSchedule::getCourseId, courseId)
                .orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(wrapper);

        return schedules.stream().map(this::convertToScheduleVO).collect(Collectors.toList());
    }

    @Override
    public List<CourseScheduleVO> getCourseTimetable(LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<CourseSchedule> wrapper = new LambdaQueryWrapper<>();

        if (startDate != null) {
            wrapper.ge(CourseSchedule::getScheduleDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(CourseSchedule::getScheduleDate, endDate);
        }

        wrapper.orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(wrapper);

        return schedules.stream().map(this::convertToScheduleVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookPrivateCourse(Long memberId, Long coachId, LocalDate courseDate, LocalTime startTime) {
        // 查询会员是否有可用的私教课课时
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getProductType, 1) // 私教课
                .eq(OrderItem::getStatus, "ACTIVE")
                .gt(OrderItem::getRemainingSessions, 0);

        List<OrderItem> availableItems = orderItemMapper.selectList(itemWrapper);
        if (availableItems.isEmpty()) {
            throw new BusinessException("您没有可用的私教课时");
        }

        // 查询教练在该时间段是否有空
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CourseSchedule::getCoachId, coachId)
                .eq(CourseSchedule::getScheduleDate, courseDate)
                .le(CourseSchedule::getStartTime, startTime)
                .ge(CourseSchedule::getEndTime, startTime.plusMinutes(60)); // 假设私教课60分钟

        if (courseScheduleMapper.selectCount(scheduleWrapper) > 0) {
            throw new BusinessException("教练在该时间段已有课程");
        }

        // 创建临时排课
        // 这里简化处理，直接创建预约记录
        CourseBooking booking = new CourseBooking();
        booking.setMemberId(memberId);
        // 私教课没有scheduleId，需要特殊处理
        booking.setBookingStatus(0);
        booking.setBookingTime(LocalDateTime.now());

        // 生成签到码
        String signCode = generateSignCode();
        booking.setSignCode(signCode);

        LocalDateTime courseDateTime = LocalDateTime.of(courseDate, startTime);
        booking.setSignCodeExpireTime(courseDateTime.plusMinutes(15));

        courseBookingMapper.insert(booking);

        // 扣减课时
        OrderItem item = availableItems.get(0);
        item.setRemainingSessions(item.getRemainingSessions() - 1);
        orderItemMapper.updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookGroupCourse(Long memberId, Long scheduleId) {
        CourseSchedule schedule = courseScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        // 检查排课状态
        if (schedule.getStatus() != 1) {
            throw new BusinessException("该排课已取消");
        }

        // 检查是否还有名额 - 从 schedule 获取 maxCapacity
        if (schedule.getCurrentEnrollment() >= schedule.getMaxCapacity()) {
            throw new BusinessException("该课程已满员");
        }

        // 检查会员是否已预约过
        LambdaQueryWrapper<CourseBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseBooking::getMemberId, memberId)
                .eq(CourseBooking::getScheduleId, scheduleId)
                .ne(CourseBooking::getBookingStatus, 3); // 排除已取消

        if (courseBookingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已预约过该课程");
        }

        // 检查课程开始时间是否有效
        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        if (courseDateTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("不能预约已开始的课程");
        }

        // 创建预约
        CourseBooking booking = new CourseBooking();
        booking.setMemberId(memberId);
        booking.setScheduleId(scheduleId);
        booking.setCourseId(schedule.getCourseId());
        booking.setBookingStatus(0); // 待上课
        booking.setBookingTime(LocalDateTime.now());

        // 生成6位随机签到码
        String signCode = generateSignCode();
        booking.setSignCode(signCode);

        // 设置签到码过期时间（课前30分钟到课后15分钟）
        booking.setSignCodeExpireTime(courseDateTime.plusMinutes(15));

        // 设置自动完成时间（课程结束后1小时）
        LocalDateTime courseEndTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getEndTime());
        booking.setAutoCompleteTime(courseEndTime.plusHours(1));

        courseBookingMapper.insert(booking);

        // 更新排课当前人数
        schedule.setCurrentEnrollment(schedule.getCurrentEnrollment() + 1);
        courseScheduleMapper.updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyCourseBooking(Long bookingId, Integer checkinMethod) {
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("该预约已无法签到");
        }

        // 验证签到时间
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        configValidator.validateCheckInTime(courseDateTime);

        // 更新预约状态
        booking.setBookingStatus(1); // 已签到
        booking.setCheckinTime(LocalDateTime.now());
        courseBookingMapper.updateById(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCourseBooking(Long bookingId, String reason) {
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("只有待上课的预约可以取消");
        }

        // 验证取消时间
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        LocalDateTime courseDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        configValidator.validateCourseCancellation(courseDateTime);

        // 更新预约状态
        booking.setBookingStatus(3); // 已取消
        booking.setCancellationReason(reason);
        booking.setCancellationTime(LocalDateTime.now());
        courseBookingMapper.updateById(booking);

        // 更新排课当前人数
        schedule.setCurrentEnrollment(schedule.getCurrentEnrollment() - 1);
        courseScheduleMapper.updateById(schedule);

        // 如果是私教课，需要恢复课时
        Course course = courseMapper.selectById(booking.getCourseId());
        if (course.getCourseType() == 0) { // 私教课
            // 查询该会员最近使用的私教课时订单项，恢复剩余课时
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getProductType, 1)
                    .eq(OrderItem::getStatus, "ACTIVE")
                    .orderByDesc(OrderItem::getUpdateTime)
                    .last("LIMIT 1");

            OrderItem item = orderItemMapper.selectOne(itemWrapper);
            if (item != null) {
                item.setRemainingSessions(item.getRemainingSessions() + 1);
                orderItemMapper.updateById(item);
            }
        }
    }

    /**
     * 生成6位随机签到码
     */
    private String generateSignCode() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    /**
     * 转换为排课VO（包含预约信息）
     */
    private CourseScheduleVO convertToScheduleVO(CourseSchedule schedule) {
        CourseScheduleVO vo = new CourseScheduleVO();
        vo.setScheduleId(schedule.getId());
        vo.setCourseId(schedule.getCourseId());
        vo.setCoachId(schedule.getCoachId());
        vo.setScheduleDate(schedule.getScheduleDate());
        vo.setStartTime(schedule.getStartTime());
        vo.setEndTime(schedule.getEndTime());
        vo.setMaxCapacity(schedule.getMaxCapacity());
        vo.setCurrentEnrollment(schedule.getCurrentEnrollment());
        vo.setStatus(schedule.getStatus());

        // 查询课程信息
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getCourseName());
            vo.setCourseType(course.getCourseType());
        }

        // 查询教练信息
        Coach coach = coachMapper.selectById(schedule.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getRealName());
        }

        // 查询该排课的所有预约（包含会员信息）
        LambdaQueryWrapper<CourseBooking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(CourseBooking::getScheduleId, schedule.getId())
                .orderByDesc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingWrapper);

        List<CourseBookingVO> bookingVOs = bookings.stream().map(booking -> {
            CourseBookingVO bookingVO = new CourseBookingVO();
            bookingVO.setBookingId(booking.getId());
            bookingVO.setMemberId(booking.getMemberId());
            bookingVO.setBookingTime(booking.getBookingTime());
            bookingVO.setBookingStatus(booking.getBookingStatus());
            bookingVO.setCheckinTime(booking.getCheckinTime());
            bookingVO.setSignCode(booking.getSignCode());
            bookingVO.setSignCodeExpireTime(booking.getSignCodeExpireTime());
            bookingVO.setCancellationReason(booking.getCancellationReason());
            bookingVO.setCancellationTime(booking.getCancellationTime());

            // 查询会员信息
            Member member = memberMapper.selectById(booking.getMemberId());
            if (member != null) {
                bookingVO.setMemberName(member.getRealName());
                bookingVO.setMemberPhone(member.getPhone());
                bookingVO.setMemberNo(member.getMemberNo());
            }

            return bookingVO;
        }).collect(Collectors.toList());

        vo.setBookings(bookingVOs);

        return vo;
    }

    /**
     * 转换为教练基础DTO
     */
    private List<CoachBasicDTO> convertToCoachBasicDTO(List<Coach> coaches) {
        return coaches.stream().map(coach -> {
            CoachBasicDTO dto = new CoachBasicDTO();
            dto.setId(coach.getId());
            dto.setRealName(coach.getRealName());
            dto.setPhone(coach.getPhone());
            dto.setGender(coach.getGender());
            dto.setSpecialty(coach.getSpecialty());

            // 将JSON字符串转换为List<String>
            if (coach.getCertifications() != null) {
                String certStr = coach.getCertifications();
                List<String> certs = List.of(certStr.replace("[", "").replace("]", "").replace("\"", "").split(","));
                dto.setCertificationList(certs);
            }

            dto.setYearsOfExperience(coach.getYearsOfExperience());
            dto.setHourlyRate(coach.getHourlyRate());
            dto.setCommissionRate(coach.getCommissionRate());
            dto.setStatus(coach.getStatus());
            dto.setRating(coach.getRating());
            dto.setIntroduction(coach.getIntroduction());
            dto.setCreateTime(coach.getCreateTime());
            dto.setUpdateTime(coach.getUpdateTime());

            return dto;
        }).collect(Collectors.toList());
    }
}