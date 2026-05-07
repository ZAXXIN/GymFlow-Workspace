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

    // 订单状态常量
    private static final String STATUS_PAID = "PAID";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_WAIT_PAY = "WAIT_PAY";

    @Override
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate firstDayOfLastMonth = firstDayOfMonth.minusMonths(1);

        stats.setTotalMembers(memberMapper.selectCount(null).intValue());
        stats.setTotalCoaches(coachMapper.selectCount(null).intValue());
        stats.setTotalCourses(courseMapper.selectCount(null).intValue());

        stats.setTodayRevenue(getRevenueByDate(today));
        stats.setYesterdayRevenue(getRevenueByDate(yesterday));
        stats.setTodayCheckIns(getCheckInsByDate(today));
        stats.setYesterdayCheckIns(getCheckInsByDate(yesterday));

        stats.setMonthRevenue(getRevenueByDateRange(firstDayOfMonth, today));
        stats.setLastMonthRevenue(getRevenueByDateRange(firstDayOfLastMonth, firstDayOfMonth.minusDays(1)));
        stats.setMonthNewMembers(getNewMembersByDateRange(firstDayOfMonth, today));
        stats.setLastMonthNewMembers(getNewMembersByDateRange(firstDayOfLastMonth, firstDayOfMonth.minusDays(1)));
        stats.setMonthCheckIns(getCheckInsByDateRange(firstDayOfMonth, today));

        return stats;
    }

    @Override
    public RevenueTrendDTO getRevenueTrend(String period, LocalDate startDate, LocalDate endDate) {
        RevenueTrendDTO trend = new RevenueTrendDTO();

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

        LambdaQueryWrapper<Course> privateQuery = new LambdaQueryWrapper<>();
        privateQuery.eq(Course::getCourseType, 0);
        Integer privateCount = courseMapper.selectCount(privateQuery).intValue();

        CourseCategoryStatsDTO privateStats = new CourseCategoryStatsDTO();
        privateStats.setCategory("私教课");
        privateStats.setValue(privateCount);
        privateStats.setColor("#409EFF");
        stats.add(privateStats);

        LambdaQueryWrapper<Course> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.eq(Course::getCourseType, 1);
        Integer groupCount = courseMapper.selectCount(groupQuery).intValue();

        CourseCategoryStatsDTO groupStats = new CourseCategoryStatsDTO();
        groupStats.setCategory("团课");
        groupStats.setValue(groupCount);
        groupStats.setColor("#67C23A");
        stats.add(groupStats);

        Integer total = privateCount + groupCount;
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
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(CourseSchedule::getScheduleDate, today)
                .eq(CourseSchedule::getStatus, 1)
                .orderByAsc(CourseSchedule::getStartTime);

        List<CourseSchedule> schedules = courseScheduleMapper.selectList(scheduleWrapper);

        return schedules.stream().map(schedule -> {
            Course course = courseMapper.selectById(schedule.getCourseId());
            Coach coach = coachMapper.selectById(schedule.getCoachId());

            TodayCourseDTO dto = new TodayCourseDTO();
            dto.setScheduleId(schedule.getScheduleId());
            dto.setCourseId(course.getCourseId());
            dto.setCourseName(course.getCourseName());
            dto.setCoachId(schedule.getCoachId());
            dto.setCoachName(coach != null ? coach.getRealName() : "");
            dto.setStartTime(schedule.getStartTime().toString());
            dto.setEndTime(schedule.getEndTime().toString());
            dto.setCapacity(schedule.getMaxCapacity());
            dto.setCurrentBookings(schedule.getCurrentEnrollment());

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

        // 待处理订单数 = 待支付
        LambdaQueryWrapper<Order> pendingOrderQuery = new LambdaQueryWrapper<>();
        pendingOrderQuery.eq(Order::getStatus, STATUS_WAIT_PAY);
        stats.setPendingOrders(orderMapper.selectCount(pendingOrderQuery).intValue());

        // 待签到课程数
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
        lowStockQuery.lt(Product::getStockQuantity, 10);
        stats.setLowStockProducts(productMapper.selectCount(lowStockQuery).intValue());

        return stats;
    }

    // ========== 私有辅助方法 ==========

    private BigDecimal getRevenueByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Order::getPaymentTime, startOfDay, endOfDay)
                .in(Order::getStatus, STATUS_PAID, STATUS_COMPLETED);   // 已支付或已完成

        List<Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(Order::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Order::getPaymentTime, start, end)
                .in(Order::getStatus, STATUS_PAID, STATUS_COMPLETED);

        List<Order> orders = orderMapper.selectList(queryWrapper);
        return orders.stream()
                .map(Order::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer getCheckInsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(CheckinRecord::getCheckinTime, startOfDay, endOfDay);
        return checkInRecordMapper.selectCount(queryWrapper).intValue();
    }

    private Integer getCheckInsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(CheckinRecord::getCheckinTime, start, end);
        return checkInRecordMapper.selectCount(queryWrapper).intValue();
    }

    private Integer getNewMembersByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(Member::getCreateTime, start, end);
        return memberMapper.selectCount(queryWrapper).intValue();
    }

    private boolean isLastDayOfMonth(LocalDate date) {
        return date.getDayOfMonth() == date.lengthOfMonth();
    }
}