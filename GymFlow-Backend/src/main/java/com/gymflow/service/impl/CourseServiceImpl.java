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
import com.gymflow.utils.SystemConfigValidator;
import com.gymflow.vo.CourseBookingVO;
import com.gymflow.vo.CourseListVO;
import com.gymflow.vo.CourseScheduleVO;
import com.gymflow.vo.PageResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SystemConfigValidator configValidator;

    @Override
    public PageResultVO<CourseListVO> getCourseList(CourseQueryDTO queryDTO) {
        log.info("查询课程列表，查询条件：{}", queryDTO);

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

        // 状态查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Course::getStatus, queryDTO.getStatus());
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Course::getCreateTime);

        // 执行分页查询
        IPage<Course> coursePage = courseMapper.selectPage(page, queryWrapper);

        // 获取所有课程ID
        List<Long> courseIds = coursePage.getRecords().stream()
                .map(Course::getCourseId)
                .collect(Collectors.toList());

        // 批量查询所有课程的教练绑定关系
        Map<Long, List<Coach>> courseCoachMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            // 查询所有课程的教练关联
            LambdaQueryWrapper<CourseCoach> coachWrapper = new LambdaQueryWrapper<>();
            coachWrapper.in(CourseCoach::getCourseId, courseIds);
            List<CourseCoach> courseCoaches = courseCoachMapper.selectList(coachWrapper);

            if (!courseCoaches.isEmpty()) {
                // 获取所有教练ID
                List<Long> coachIds = courseCoaches.stream()
                        .map(CourseCoach::getCoachId)
                        .distinct()
                        .collect(Collectors.toList());

                // 批量查询教练信息
                List<Coach> coaches = coachMapper.selectBatchIds(coachIds);

                // 构建教练ID到教练对象的映射
                Map<Long, Coach> coachMap = new HashMap<>();
                for (Coach coach : coaches) {
                    coachMap.put(coach.getId(), coach);
                }

                // 构建课程ID -> 教练列表的映射
                for (CourseCoach cc : courseCoaches) {
                    Long courseId = cc.getCourseId();
                    Long coachIdValue = cc.getCoachId();
                    Coach coach = coachMap.get(coachIdValue);
                    if (coach != null) {
                        List<Coach> coachList = courseCoachMap.get(courseId);
                        if (coachList == null) {
                            coachList = new ArrayList<>();
                            courseCoachMap.put(courseId, coachList);
                        }
                        coachList.add(coach);
                    }
                }
            }
        }

        // 转换为VO列表
        List<CourseListVO> voList = coursePage.getRecords().stream()
                .map(course -> {
                    CourseListVO vo = new CourseListVO();
                    vo.setId(course.getCourseId());
                    vo.setCourseType(course.getCourseType());
                    vo.setCourseName(course.getCourseName());
                    vo.setDescription(course.getDescription());
                    vo.setDuration(course.getDuration());
                    vo.setSessionCost(course.getSessionCost());
                    vo.setStatus(course.getStatus());
                    vo.setNotice(course.getNotice());

                    // 设置教练列表（包含完整信息）
                    List<Coach> coaches = courseCoachMap.get(course.getCourseId());
                    if (coaches != null && !coaches.isEmpty()) {
                        // 转换为 CoachBasicDTO 列表
                        List<CoachBasicDTO> coachDTOs = new ArrayList<>();
                        for (Coach coach : coaches) {
                            coachDTOs.add(convertToCoachBasicDTO(coach));
                        }
                        vo.setCoaches(coachDTOs);
                    }

                    // 查询统计信息
                    LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
                    scheduleWrapper.eq(CourseSchedule::getCourseId, course.getCourseId());
                    Long totalSchedules = courseScheduleMapper.selectCount(scheduleWrapper);
                    vo.setTotalSchedules(totalSchedules.intValue());

                    LambdaQueryWrapper<CourseBooking> bookingWrapper = new LambdaQueryWrapper<>();
                    bookingWrapper.eq(CourseBooking::getCourseId, course.getCourseId());
                    Long totalBookings = courseBookingMapper.selectCount(bookingWrapper);
                    vo.setTotalBookings(totalBookings.intValue());

                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, coursePage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public CourseFullDTO getCourseDetail(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        CourseFullDTO dto = new CourseFullDTO();
        dto.setId(course.getCourseId());
        dto.setCourseType(course.getCourseType());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setSessionCost(course.getSessionCost());
        dto.setStatus(course.getStatus());
        dto.setNotice(course.getNotice());
        dto.setCreateTime(course.getCreateTime());
        dto.setUpdateTime(course.getUpdateTime());

        // 查询绑定教练
        List<Long> coachIds = courseCoachMapper.selectCoachIdsByCourseId(courseId);
        if (!coachIds.isEmpty()) {
            List<Coach> coaches = coachMapper.selectBatchIds(coachIds);
            // 将 List<Coach> 转换为 List<CoachBasicDTO>
            List<CoachBasicDTO> coachDTOs = new ArrayList<>();
            for (Coach coach : coaches) {
                coachDTOs.add(convertToCoachBasicDTO(coach));
            }
            dto.setCoaches(coachDTOs);
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
        course.setSessionCost(courseDTO.getSessionCost());
        course.setNotice(courseDTO.getNotice());
        course.setStatus(1); // 默认正常

        courseMapper.insert(course);

        // 绑定教练
        for (Long coachId : courseDTO.getCoachIds()) {
            CourseCoach courseCoach = new CourseCoach();
            courseCoach.setCourseId(course.getCourseId());
            courseCoach.setCoachId(coachId);
            courseCoachMapper.insert(courseCoach);
        }

        return course.getCourseId();
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
                .ne(Course::getCourseId, courseId);
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
        course.setSessionCost(courseDTO.getSessionCost());
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

        // 验证教练在该时间段是否已有排课（只检查正常状态的排课）
        LambdaQueryWrapper<CourseSchedule> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.eq(CourseSchedule::getCoachId, scheduleDTO.getCoachId())
                .eq(CourseSchedule::getScheduleDate, scheduleDTO.getScheduleDate())
                .eq(CourseSchedule::getStatus, 1)  // 只查正常状态的排课
                .and(w -> w.between(CourseSchedule::getStartTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime())
                        .or(b -> b.between(CourseSchedule::getEndTime, scheduleDTO.getStartTime(), scheduleDTO.getEndTime()))
                        .or(c -> c.le(CourseSchedule::getStartTime, scheduleDTO.getStartTime())
                                .ge(CourseSchedule::getEndTime, scheduleDTO.getEndTime())));

        if (courseScheduleMapper.selectCount(conflictWrapper) > 0) {
            throw new BusinessException("教练在该时间段已有排课");
        }

        // 验证营业时间
        configValidator.validateBusinessHours(scheduleDTO.getStartTime(), scheduleDTO.getEndTime());

        // 验证课程容量
        if (scheduleDTO.getMaxCapacity() != null) {
            configValidator.validateClassCapacity(0, scheduleDTO.getMaxCapacity());
        }

        // 创建排课
        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(scheduleDTO.getCourseId());
        schedule.setCourseName(course.getCourseName());
        schedule.setCoachId(scheduleDTO.getCoachId());
        schedule.setScheduleDate(scheduleDTO.getScheduleDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setMaxCapacity(scheduleDTO.getMaxCapacity() != null ? scheduleDTO.getMaxCapacity() : 20);
        schedule.setCurrentEnrollment(0);
        schedule.setStatus(1);

//        LocalDateTime now = LocalDateTime.now();
//        schedule.setCreateTime(now);
//        schedule.setUpdateTime(now);

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
                .ne(CourseSchedule::getScheduleId, scheduleId)
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
                .orderByDesc(CourseSchedule::getScheduleDate)
                .orderByDesc(CourseSchedule::getStartTime);

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

        LocalDateTime now = LocalDateTime.now();

        return schedules.stream()
                .map(this::convertToScheduleVO)
                // 过滤掉不可预约的课程
                .filter(schedule -> {
                    LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
                    // 课程已开始不可预约
                    if (scheduleDateTime.isBefore(now)) {
                        return false;
                    }
                    // 已过预约截止时间不可预约
                    if (!configValidator.canBookCourse(scheduleDateTime)) {
                        return false;
                    }
                    // 已满员不可预约
                    if (schedule.getCurrentEnrollment() >= schedule.getMaxCapacity()) {
                        return false;
                    }
                    // 排课已取消不可预约
                    if (schedule.getStatus() != 1) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookPrivateCourse(Long memberId, Long courseId, Long coachId, LocalDate scheduleDate, LocalTime startTime) {
        log.info("预约私教课，会员ID：{}，课程ID：{}，教练ID：{}，日期：{}，时间：{}",
                memberId, courseId, coachId, scheduleDate, startTime);

        // 1. 验证会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        // 2. 验证教练是否存在且在职
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }
        if (coach.getStatus() != 1) {
            throw new BusinessException("教练已离职，无法预约");
        }

        // 3. 验证课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (course.getStatus() != 1) {
            throw new BusinessException("课程已禁用");
        }
        if (course.getCourseType() != 0) {
            throw new BusinessException("只能预约私教课");
        }

        // 4. 验证教练是否可授课该课程
        LambdaQueryWrapper<CourseCoach> coachWrapper = new LambdaQueryWrapper<>();
        coachWrapper.eq(CourseCoach::getCourseId, courseId)
                .eq(CourseCoach::getCoachId, coachId);
        if (courseCoachMapper.selectCount(coachWrapper) == 0) {
            throw new BusinessException("该教练不能教授此课程");
        }

        // 5. 计算课程结束时间
        int duration = course.getDuration() != null ? course.getDuration() : 60;
        LocalTime endTimeLocal = startTime.plusMinutes(duration);

        // 6. 验证课程时间是否有效
        LocalDateTime scheduleDateTime = LocalDateTime.of(scheduleDate, startTime);
        LocalDateTime now = LocalDateTime.now();

        // 检查是否已开始
        if (scheduleDateTime.isBefore(now)) {
            throw new BusinessException("不能预约已开始的课程");
        }

        // 7. 验证营业时间
        configValidator.validateBusinessHours(startTime, endTimeLocal);

        // 8. 验证是否在可预约时间内
        configValidator.validateCourseBooking(scheduleDateTime);

        // 9. 验证教练在该时间段是否已有排课（只检查正常状态的排课）
        LambdaQueryWrapper<CourseSchedule> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.eq(CourseSchedule::getCoachId, coachId)
                .eq(CourseSchedule::getScheduleDate, scheduleDate)
                .eq(CourseSchedule::getStatus, 1)
                .and(w -> w.between(CourseSchedule::getStartTime, startTime, endTimeLocal)
                        .or(b -> b.between(CourseSchedule::getEndTime, startTime, endTimeLocal))
                        .or(c -> c.le(CourseSchedule::getStartTime, startTime)
                                .ge(CourseSchedule::getEndTime, endTimeLocal)));

        if (courseScheduleMapper.selectCount(conflictWrapper) > 0) {
            throw new BusinessException("教练在该时间段已有排课");
        }

        // 10. 查询可用的课时
        Integer sessionCost = course.getSessionCost() != null ? course.getSessionCost() : 1;

        // 查询该会员的订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getMemberId, memberId)
                .eq(Order::getPaymentStatus, 1)
                .in(Order::getOrderStatus, "COMPLETED", "PROCESSING");

        List<Order> orders = orderMapper.selectList(orderWrapper);
        if (orders.isEmpty()) {
            throw new BusinessException("您没有可用的课时");
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        // 查询订单项中的课时
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds)
                .eq(OrderItem::getProductType, 1) // 私教课 productType = 1
                .eq(OrderItem::getStatus, "ACTIVE")
                .gt(OrderItem::getRemainingSessions, 0)
                .ge(OrderItem::getValidityEndDate, LocalDate.now())
                .orderByDesc(OrderItem::getValidityEndDate);

        List<OrderItem> availableItems = orderItemMapper.selectList(itemWrapper);

        if (availableItems.isEmpty()) {
            throw new BusinessException("您没有可用的私教课时");
        }

        // 使用最早过期的课时
        OrderItem item = availableItems.get(0);
        if (item.getRemainingSessions() < sessionCost) {
            throw new BusinessException(String.format("课时不足，本课程需要 %d 课时，您只有 %d 课时",
                    sessionCost, item.getRemainingSessions()));
        }

        // 扣减课时
        item.setRemainingSessions(item.getRemainingSessions() - sessionCost);
        orderItemMapper.updateById(item);

        // 11. 创建私教课排课
        CourseSchedule schedule = new CourseSchedule();
        schedule.setCourseId(courseId);
        schedule.setCourseName(course.getCourseName());
        schedule.setCoachId(coachId);
        schedule.setScheduleDate(scheduleDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTimeLocal);
        schedule.setMaxCapacity(1);
        schedule.setCurrentEnrollment(0);
        schedule.setStatus(1);

        courseScheduleMapper.insert(schedule);

        // 12. 创建预约（记录使用的订单项ID）
        CourseBooking booking = new CourseBooking();
        booking.setMemberId(memberId);
        booking.setScheduleId(schedule.getScheduleId());
        booking.setCourseId(courseId);
        booking.setOrderItemId(item.getId());  // 记录使用的订单项ID
        booking.setBookingStatus(0);
        booking.setBookingTime(now);

        // 生成6位随机签到码
        String signCode = generateSignCode();
        booking.setSignCode(signCode);

        // 设置签到码过期时间
        booking.setSignCodeExpireTime(scheduleDateTime.plusMinutes(15));

        // 设置自动完成时间
        LocalDateTime courseEndDateTime = LocalDateTime.of(scheduleDate, endTimeLocal);
        booking.setAutoCompleteTime(courseEndDateTime.plusHours(configValidator.getAutoCompleteHours()));

        courseBookingMapper.insert(booking);

        // 更新排课当前人数
        schedule.setCurrentEnrollment(1);
        courseScheduleMapper.updateById(schedule);

        log.info("私教课预约成功，预约ID：{}，排课ID：{}，使用订单项ID：{}",
                booking.getId(), schedule.getScheduleId(), item.getId());
    }

    /**
     * 获取或创建私教课课程模板
     */
    private Course getOrCreatePrivateCourse(Long coachId) {
        // 查询是否已存在私教课模板
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getCourseType, 0) // 0-私教课
                .eq(Course::getCourseName, "私教课");

        Course course = courseMapper.selectOne(wrapper);
        if (course == null) {
            // 创建默认私教课模板
            course = new Course();
            course.setCourseType(0);
            course.setCourseName("私教课");
            course.setDescription("一对一私教课程");
            course.setDuration(60);
            course.setSessionCost(1);
            course.setStatus(1);
            course.setNotice("请提前10分钟到达");

            courseMapper.insert(course);

            // 绑定教练
            CourseCoach courseCoach = new CourseCoach();
            courseCoach.setCourseId(course.getCourseId());
            courseCoach.setCoachId(coachId);
            courseCoachMapper.insert(courseCoach);
        } else {
            // 检查教练是否已绑定该课程
            LambdaQueryWrapper<CourseCoach> coachWrapper = new LambdaQueryWrapper<>();
            coachWrapper.eq(CourseCoach::getCourseId, course.getCourseId())
                    .eq(CourseCoach::getCoachId, coachId);

            if (courseCoachMapper.selectCount(coachWrapper) == 0) {
                CourseCoach courseCoach = new CourseCoach();
                courseCoach.setCourseId(course.getCourseId());
                courseCoach.setCoachId(coachId);
                courseCoachMapper.insert(courseCoach);
            }
        }

        return course;
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

        // 检查是否还有名额
        if (schedule.getCurrentEnrollment() >= schedule.getMaxCapacity()) {
            throw new BusinessException("该课程已满员");
        }

        // 检查会员是否已预约过
        LambdaQueryWrapper<CourseBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseBooking::getMemberId, memberId)
                .eq(CourseBooking::getScheduleId, scheduleId)
                .ne(CourseBooking::getBookingStatus, 3);

        if (courseBookingMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("您已预约过该课程");
        }

        // 检查课程开始时间是否有效
        LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        LocalDateTime now = LocalDateTime.now();

        // 检查是否已开始
        if (scheduleDateTime.isBefore(now)) {
            throw new BusinessException("不能预约已开始的课程");
        }

        // 检查是否在可预约时间内
        configValidator.validateCourseBooking(scheduleDateTime);

        // 获取课程信息
        Course course = courseMapper.selectById(schedule.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }

        // 根据课程类型确定需要查询的课时类型
        Integer targetProductType = course.getCourseType() == 0 ? 1 : 2;

        // 查询可用的课时
        // 查询该会员的订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getMemberId, memberId)
                .eq(Order::getPaymentStatus, 1)
                .in(Order::getOrderStatus, "COMPLETED", "PROCESSING");

        List<Order> orders = orderMapper.selectList(orderWrapper);
        if (orders.isEmpty()) {
            throw new BusinessException("您没有可用的课时");
        }

        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());

        // 查询订单项中的课时
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds)
                .eq(OrderItem::getProductType, targetProductType)
                .eq(OrderItem::getStatus, "ACTIVE")
                .gt(OrderItem::getRemainingSessions, 0)
                .ge(OrderItem::getValidityEndDate, LocalDate.now())
                .orderByDesc(OrderItem::getValidityEndDate);

        List<OrderItem> availableItems = orderItemMapper.selectList(itemWrapper);

        // 计算需要消耗的课时
        Integer sessionCost = course.getSessionCost();
        if (sessionCost == null) {
            sessionCost = 1;
        }

        // 使用最早过期的课时
        OrderItem item = availableItems.get(0);
        if (item.getRemainingSessions() < sessionCost) {
            throw new BusinessException(String.format("课时不足，本课程需要 %d 课时，您只有 %d 课时",
                    sessionCost, item.getRemainingSessions()));
        }

        // 扣减课时
        item.setRemainingSessions(item.getRemainingSessions() - sessionCost);
        orderItemMapper.updateById(item);

        // 创建预约（记录使用的订单项ID）
        CourseBooking booking = new CourseBooking();
        booking.setMemberId(memberId);
        booking.setScheduleId(scheduleId);
        booking.setCourseId(schedule.getCourseId());
        booking.setOrderItemId(item.getId());  // 记录使用的订单项ID
        booking.setBookingStatus(0);
        booking.setBookingTime(LocalDateTime.now());

        // 生成6位随机签到码
        String signCode = generateSignCode();
        booking.setSignCode(signCode);

        // 设置签到码过期时间
        booking.setSignCodeExpireTime(scheduleDateTime.plusMinutes(15));

        // 设置自动完成时间
        LocalDateTime courseEndTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getEndTime());
        booking.setAutoCompleteTime(courseEndTime.plusHours(configValidator.getAutoCompleteHours()));

        courseBookingMapper.insert(booking);

        // 更新排课当前人数
        schedule.setCurrentEnrollment(schedule.getCurrentEnrollment() + 1);
        courseScheduleMapper.updateById(schedule);

        log.info("团课预约成功，预约ID：{}，排课ID：{}，使用订单项ID：{}",
                booking.getId(), scheduleId, item.getId());
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

        LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        configValidator.validateCheckInTime(scheduleDateTime);

        // 更新预约状态
        booking.setBookingStatus(1);
        booking.setCheckinTime(LocalDateTime.now());
        courseBookingMapper.updateById(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCourseBooking(Long bookingId, String reason) {
        log.info("取消课程预约，预约ID：{}，原因：{}", bookingId, reason);

        // 1. 验证预约记录是否存在
        CourseBooking booking = courseBookingMapper.selectById(bookingId);
        if (booking == null) {
            throw new BusinessException("预约记录不存在");
        }

        // 2. 验证预约状态（只有待上课的可以取消）
        if (booking.getBookingStatus() != 0) {
            throw new BusinessException("只有待上课的预约可以取消");
        }

        // 3. 获取排课信息
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule == null) {
            throw new BusinessException("排课不存在");
        }

        // 4. 验证取消时间（是否在允许取消的时间范围内）
        LocalDateTime scheduleDateTime = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
        configValidator.validateCourseCancellation(scheduleDateTime);

        // 5. 更新预约状态为已取消
        booking.setBookingStatus(3);
        booking.setCancellationReason(reason);
        booking.setCancellationTime(LocalDateTime.now());
        courseBookingMapper.updateById(booking);
        log.info("预约状态已更新为已取消，预约ID：{}", bookingId);

        // 6. 获取课程信息
        Course course = courseMapper.selectById(booking.getCourseId());
        Integer sessionCost = course != null ? (course.getSessionCost() != null ? course.getSessionCost() : 1) : 1;

        // 7. 更新排课状态
        if (course != null && course.getCourseType() == 0) {
            // 私教课：排课状态改为已取消（不修改 currentEnrollment）
            schedule.setStatus(0);
            courseScheduleMapper.updateById(schedule);
            log.info("私教课取消预约，排课状态改为已取消，排课ID：{}", schedule.getScheduleId());
        } else if (course != null && course.getCourseType() == 1) {
            // 团课：只减少当前预约人数
            schedule.setCurrentEnrollment(schedule.getCurrentEnrollment() - 1);
            courseScheduleMapper.updateById(schedule);
            log.info("团课取消预约，当前预约人数减1，排课ID：{}", schedule.getScheduleId());
        }

        // 8. 恢复课时（根据预约时记录的订单项ID）
        if (booking.getOrderItemId() != null) {
            OrderItem item = orderItemMapper.selectById(booking.getOrderItemId());
            if (item != null) {
                item.setRemainingSessions(item.getRemainingSessions() + sessionCost);
                orderItemMapper.updateById(item);
                log.info("课时恢复成功，订单项ID：{}，剩余课时：{}",
                        item.getId(), item.getRemainingSessions());
            } else {
                log.warn("未找到订单项，订单项ID：{}", booking.getOrderItemId());
            }
        } else {
            log.warn("预约记录没有关联订单项ID，预约ID：{}", bookingId);
        }

        log.info("取消预约成功，预约ID：{}", bookingId);
    }

    @Override
    public PageResultVO<CourseBookingVO> getMemberBookings(Long memberId, Integer pageNum, Integer pageSize, Integer bookingStatus) {
        log.info("获取会员预约列表，会员ID：{}，状态：{}", memberId, bookingStatus);

        // 构建分页对象
        Page<CourseBooking> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<CourseBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseBooking::getMemberId, memberId);

        if (bookingStatus != null) {
            wrapper.eq(CourseBooking::getBookingStatus, bookingStatus);
        }

        wrapper.orderByDesc(CourseBooking::getBookingTime);

        // 分页查询预约记录
        IPage<CourseBooking> bookingPage = courseBookingMapper.selectPage(page, wrapper);

        // 转换为VO
        List<CourseBookingVO> voList = bookingPage.getRecords().stream()
                .map(this::convertToCourseBookingVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, bookingPage.getTotal(), pageNum, pageSize);
    }

    /**
     * 将 CourseBooking 实体转换为 CourseBookingVO
     */
    private CourseBookingVO convertToCourseBookingVO(CourseBooking booking) {
        CourseBookingVO vo = new CourseBookingVO();

        vo.setBookingId(booking.getId());
        vo.setMemberId(booking.getMemberId());
        vo.setBookingTime(booking.getBookingTime());
        vo.setCheckinTime(booking.getCheckinTime());
        vo.setSignCode(booking.getSignCode());
        vo.setSignCodeExpireTime(booking.getSignCodeExpireTime());
        vo.setCancellationReason(booking.getCancellationReason());
        vo.setCancellationTime(booking.getCancellationTime());

        // 获取会员信息
        Member member = memberMapper.selectById(booking.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getRealName());
            vo.setMemberPhone(member.getPhone());
            vo.setMemberNo(member.getMemberNo());
        }

        // 获取排课信息
        CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
        if (schedule != null) {
            vo.setScheduleId(schedule.getScheduleId());
            vo.setScheduleDate(schedule.getScheduleDate());
            vo.setStartTime(schedule.getStartTime());
            vo.setEndTime(schedule.getEndTime());

            // 获取课程信息
            Course course = courseMapper.selectById(schedule.getCourseId());
            if (course != null) {
                vo.setCourseId(course.getCourseId());
                vo.setCourseName(course.getCourseName());
                vo.setCourseType(course.getCourseType());
                vo.setSessionCost(course.getSessionCost());
            }

            // 获取教练信息
            Coach coach = coachMapper.selectById(schedule.getCoachId());
            if (coach != null) {
                vo.setCoachId(coach.getId());
                vo.setCoachName(coach.getRealName());
            }

            // ✅ 动态判断状态
            Integer originalStatus = booking.getBookingStatus();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime courseStart = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
            LocalDateTime courseEnd = LocalDateTime.of(schedule.getScheduleDate(), schedule.getEndTime());

            // 状态判断优先级：已取消 > 已完成 > 已签到 > 已过期 > 待上课
            if (originalStatus == 3) {
                vo.setBookingStatus(3);
            } else if (originalStatus == 2) {
                vo.setBookingStatus(2);
            } else if (originalStatus == 1) {
                vo.setBookingStatus(1);
            } else if (originalStatus == 0) {
                // 待上课状态，判断是否已过期
                if (now.isAfter(courseEnd)) {
                    vo.setBookingStatus(4);
                } else {
                    vo.setBookingStatus(0);
                }
            } else {
                vo.setBookingStatus(originalStatus);
            }
        }

        return vo;
    }

    /**
     * 获取预约状态描述
     */
    private String getBookingStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待上课";
            case 1: return "已签到";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "已过期";
            default: return "未知";
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
        vo.setScheduleId(schedule.getScheduleId());
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
            // 添加课程类型描述
            if (course.getCourseType() != null) {
                vo.setCourseTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");
            }
            vo.setSessionCost(course.getSessionCost());  // 新增：设置课时消耗
        }

        // 查询教练信息
        Coach coach = coachMapper.selectById(schedule.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getRealName());
        }

        // 查询该排课的所有预约（包含会员信息）
        LambdaQueryWrapper<CourseBooking> bookingWrapper = new LambdaQueryWrapper<>();
        bookingWrapper.eq(CourseBooking::getScheduleId, schedule.getScheduleId())
                .orderByDesc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingWrapper);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime courseEnd = LocalDateTime.of(schedule.getScheduleDate(), schedule.getEndTime());

        List<CourseBookingVO> bookingVOs = bookings.stream().map(booking -> {
            CourseBookingVO bookingVO = new CourseBookingVO();
            bookingVO.setBookingId(booking.getId());
            bookingVO.setMemberId(booking.getMemberId());
            bookingVO.setBookingTime(booking.getBookingTime());
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

            // ✅ 动态判断状态（只设置 bookingStatus，不设置 bookingStatusDesc）
            Integer originalStatus = booking.getBookingStatus();
            if (originalStatus == 0 && now.isAfter(courseEnd)) {
                bookingVO.setBookingStatus(4);
            } else {
                bookingVO.setBookingStatus(originalStatus);
            }

            return bookingVO;
        }).collect(Collectors.toList());

        vo.setBookings(bookingVOs);

        return vo;
    }

    /**
     * 转换为教练基础DTO
     */
    private CoachBasicDTO convertToCoachBasicDTO(Coach coach) {
        CoachBasicDTO dto = new CoachBasicDTO();
        dto.setId(coach.getId());
        dto.setRealName(coach.getRealName());
        dto.setPhone(coach.getPhone());
        dto.setSpecialty(coach.getSpecialty());
        dto.setYearsOfExperience(coach.getYearsOfExperience());
        dto.setStatus(coach.getStatus());
        dto.setRating(coach.getRating());
        dto.setIntroduction(coach.getIntroduction());
        dto.setCreateTime(coach.getCreateTime());
        dto.setUpdateTime(coach.getUpdateTime());

        // 处理证书信息
        if (coach.getCertifications() != null) {
            String certStr = coach.getCertifications();
            List<String> certs = java.util.Arrays.asList(certStr.replace("[", "").replace("]", "").replace("\"", "").split(","));
            dto.setCertificationList(certs);
        }

        return dto;
    }
}