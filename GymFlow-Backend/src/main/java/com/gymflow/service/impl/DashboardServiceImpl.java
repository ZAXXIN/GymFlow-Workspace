package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.dashboard.*;
import com.gymflow.entity.*;
import com.gymflow.mapper.*;
import com.gymflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final MemberMapper memberMapper;
    private final CoachMapper coachMapper;
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final ProductMapper productMapper;
    private final CheckInRecordMapper checkInRecordMapper;
    private final OrderMapper orderMapper;
    private final CourseBookingMapper courseBookingMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // 获取当前日期
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate firstDayOfLastMonth = firstDayOfMonth.minusMonths(1);

        // 1. 总会员数
        stats.setTotalMembers(memberMapper.selectCount(null).intValue());

        // 2. 总教练数
        stats.setTotalCoaches(coachMapper.selectCount(null).intValue());

        // 3. 总课程数（模板数）
        stats.setTotalCourses(courseMapper.selectCount(null).intValue());

        // 4. 今日营收
        BigDecimal todayRevenue = getRevenueByDate(today);
        stats.setTodayRevenue(todayRevenue);

        // 5. 昨日营收
        BigDecimal yesterdayRevenue = getRevenueByDate(yesterday);
        stats.setYesterdayRevenue(yesterdayRevenue);

        // 6. 今日签到数
        Integer todayCheckIns = getCheckInsByDate(today);
        stats.setTodayCheckIns(todayCheckIns);

        // 7. 昨日签到数
        Integer yesterdayCheckIns = getCheckInsByDate(yesterday);
        stats.setYesterdayCheckIns(yesterdayCheckIns);

        // 8. 本月营收
        BigDecimal monthRevenue = getRevenueByDateRange(firstDayOfMonth, today);
        stats.setMonthRevenue(monthRevenue);

        // 9. 上月营收
        BigDecimal lastMonthRevenue = getRevenueByDateRange(
                firstDayOfLastMonth,
                firstDayOfMonth.minusDays(1)
        );
        stats.setLastMonthRevenue(lastMonthRevenue);

        // 10. 本月新增会员
        Integer monthNewMembers = getNewMembersByDateRange(firstDayOfMonth, today);
        stats.setMonthNewMembers(monthNewMembers);

        // 11. 上月新增会员
        Integer lastMonthNewMembers = getNewMembersByDateRange(
                firstDayOfLastMonth,
                firstDayOfMonth.minusDays(1)
        );
        stats.setLastMonthNewMembers(lastMonthNewMembers);

        // 12. 本月签到数
        Integer monthCheckIns = getCheckInsByDateRange(firstDayOfMonth, today);
        stats.setMonthCheckIns(monthCheckIns);

        return stats;
    }

    @Override
    public RevenueTrendDTO getRevenueTrend(String period, LocalDate startDate, LocalDate endDate) {
        RevenueTrendDTO trend = new RevenueTrendDTO();

        // 设置默认日期范围
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            switch (period) {
                case "week":
                    startDate = today.minusDays(6);
                    endDate = today;
                    break;
                case "month":
                    startDate = today.minusDays(29);
                    endDate = today;
                    break;
                case "year":
                    startDate = today.minusMonths(11).withDayOfMonth(1);
                    endDate = today;
                    break;
                default:
                    startDate = today.minusDays(6);
                    endDate = today;
            }
        }

        trend.setStartDate(startDate.toString());
        trend.setEndDate(endDate.toString());
        trend.setPeriod(period);

        List<RevenueDataPointDTO> dataPoints = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            BigDecimal revenue = getRevenueByDate(currentDate);

            // 按周期分组
            if ("year".equals(period)) {
                String monthKey = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                if (isLastDayOfMonth(currentDate) || currentDate.equals(endDate)) {
                    categories.add(currentDate.format(DateTimeFormatter.ofPattern("MM月")));
                    values.add(revenue);

                    RevenueDataPointDTO point = new RevenueDataPointDTO();
                    point.setDate(monthKey);
                    point.setRevenue(revenue);
                    point.setLabel(currentDate.format(DateTimeFormatter.ofPattern("MM月")));
                    dataPoints.add(point);
                }
            } else {
                categories.add(currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));
                values.add(revenue);

                RevenueDataPointDTO point = new RevenueDataPointDTO();
                point.setDate(currentDate.toString());
                point.setRevenue(revenue);
                point.setLabel(currentDate.format(DateTimeFormatter.ofPattern("MM-dd")));
                dataPoints.add(point);
            }

            currentDate = currentDate.plusDays(1);
        }

        trend.setCategories(categories);
        trend.setValues(values);
        trend.setDataPoints(dataPoints);

        return trend;
    }

    @Override
    public List<CourseCategoryStatsDTO> getCourseCategoryStats() {
        List<CourseCategoryStatsDTO> stats = new ArrayList<>();

        // 统计私教课数量
        LambdaQueryWrapper<Course> privateQuery = new LambdaQueryWrapper<>();
        privateQuery.eq(Course::getCourseType, 0); // 0-私教课
        Integer privateCount = courseMapper.selectCount(privateQuery).intValue();

        CourseCategoryStatsDTO privateStats = new CourseCategoryStatsDTO();
        privateStats.setCategory("私教课");
        privateStats.setValue(privateCount);
        privateStats.setColor("#409EFF");
        stats.add(privateStats);

        // 统计团课数量
        LambdaQueryWrapper<Course> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(Course::getCourseType, 1); // 1-团课
        Integer groupCount = courseMapper.selectCount(groupQuery).intValue();

        CourseCategoryStatsDTO groupStats = new CourseCategoryStatsDTO();
        groupStats.setCategory("团课");
        groupStats.setValue(groupCount);
        groupStats.setColor("#67C23A");
        stats.add(groupStats);

        // 计算总课程数
        Integer total = privateCount + groupCount;

        // 计算占比
        for (CourseCategoryStatsDTO stat : stats) {
            if (total > 0) {
                double percentage = (stat.getValue() * 100.0) / total;
                stat.setPercentage(new BigDecimal(percentage).setScale(2, RoundingMode.HALF_UP));
            } else {
                stat.setPercentage(BigDecimal.ZERO);
            }
        }

        return stats;
    }

    @Override
    public List<TodayCourseDTO> getTodayCourses() {
        LocalDate today = LocalDate.now();

        // 查询今天的排课
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CourseSchedule::getScheduleDate, today)
                .eq(CourseSchedule::getStatus, 1)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(scheduleWrapper);

        return schedules.stream().map(schedule -> {
            Course course = courseMapper.selectById(schedule.getCourseId());
            Coach coach = coachMapper.selectById(schedule.getCoachId());

            TodayCourseDTO dto = new TodayCourseDTO();
            dto.setScheduleId(schedule.getScheduleId());  // 使用排课ID
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setCoachId(schedule.getCoachId());
            dto.setCoachName(coach != null ? coach.getRealName() : "");
            dto.setStartTime(schedule.getStartTime().toString());
            dto.setEndTime(schedule.getEndTime().toString());
            dto.setCapacity(schedule.getMaxCapacity());
            dto.setCurrentBookings(schedule.getCurrentEnrollment());

            // 计算状态
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime courseStart = LocalDateTime.of(schedule.getScheduleDate(), schedule.getStartTime());
            LocalDateTime courseEnd = LocalDateTime.of(schedule.getScheduleDate(), schedule.getEndTime());

            if (now.isBefore(courseStart)) {
                dto.setStatus("UPCOMING");
                dto.setStatusText("待开始");
            } else if (now.isAfter(courseEnd)) {
                dto.setStatus("FINISHED");
                dto.setStatusText("已结束");
            } else {
                dto.setStatus("ONGOING");
                dto.setStatusText("进行中");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public QuickStatsDTO getQuickStats() {
        QuickStatsDTO stats = new QuickStatsDTO();
        LocalDate today = LocalDate.now();

        // 待处理订单数
        LambdaQueryWrapper<Order> pendingOrderQuery = new LambdaQueryWrapper<>();
        pendingOrderQuery.eq(Order::getOrderStatus, "PENDING");
        stats.setPendingOrders(orderMapper.selectCount(pendingOrderQuery).intValue());

        // 待签到课程数（查询今天待上课的排课）
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CourseSchedule> pendingCheckInQuery = new LambdaQueryWrapper<>();
        pendingCheckInQuery.eq(CourseSchedule::getScheduleDate, today)
                .gt(CourseSchedule::getStartTime, now.toLocalTime())
                .eq(CourseSchedule::getStatus, 1);
        stats.setPendingCheckIns(courseScheduleMapper.selectCount(pendingCheckInQuery).intValue());

        // 即将过期的会员数
        LocalDate thirtyDaysLater = today.plusDays(30);
        LambdaQueryWrapper<Member> expiringMemberQuery = new LambdaQueryWrapper<>();
        expiringMemberQuery.between(Member::getMembershipEndDate, today, thirtyDaysLater);
        stats.setExpiringMembers(memberMapper.selectCount(expiringMemberQuery).intValue());

        // 库存预警商品数
        LambdaQueryWrapper<Product> lowStockQuery = new LambdaQueryWrapper<>();
        lowStockQuery.lt(Product::getStockQuantity, 10); // 库存小于10预警
        stats.setLowStockProducts(productMapper.selectCount(lowStockQuery).intValue());

        return stats;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取指定日期的营收
     */
    private BigDecimal getRevenueByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Order::getPaymentTime, startOfDay, endOfDay)
                .eq(Order::getPaymentStatus, 1); // 已支付

        List<Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(Order::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取日期范围内的营收
     */
    private BigDecimal getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Order::getPaymentTime, start, end)
                .eq(Order::getPaymentStatus, 1);

        List<Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(Order::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 获取指定日期的签到数
     */
    private Integer getCheckInsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);

        return checkInRecordMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 获取日期范围内的签到数
     */
    private Integer getCheckInsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(CheckinRecord::getCheckinTime, start, end);

        return checkInRecordMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 获取日期范围内的新增会员数
     */
    private Integer getNewMembersByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Member::getCreateTime, start, end);

        return memberMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 判断是否为当月的最后一天
     */
    private boolean isLastDayOfMonth(LocalDate date) {
        return date.getDayOfMonth() == date.lengthOfMonth();
    }
}