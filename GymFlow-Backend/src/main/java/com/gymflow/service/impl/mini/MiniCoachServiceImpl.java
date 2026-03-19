package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.coach.CoachFullDTO;
import com.gymflow.dto.member.HealthRecordDTO;
import com.gymflow.dto.member.MemberFullDTO;
import com.gymflow.dto.mini.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.mini.MiniCoachService;
import com.gymflow.utils.BCryptUtil;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniCoachServiceImpl implements MiniCoachService {

    private final CoachMapper coachMapper;
    private final MemberMapper memberMapper;
    private final CourseMapper courseMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final HealthRecordMapper healthRecordMapper;
    private final OrderItemMapper orderItemMapper;
    private final BCryptUtil bCryptUtil;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public CoachFullDTO getMyInfo(Long coachId) {
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        CoachFullDTO dto = new CoachFullDTO();
        BeanUtils.copyProperties(coach, dto);
        return dto;
    }

    @Override
    public List<MiniScheduleDTO> getMySchedule(Long coachId, LocalDate date) {
        LocalDate queryDate = date != null ? date : LocalDate.now();

        // 查询教练的课程
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCoachId, coachId)
                .eq(Course::getCourseDate, queryDate)
                .orderByAsc(Course::getStartTime);

        List<Course> courses = courseMapper.selectList(queryWrapper);
        List<MiniScheduleDTO> scheduleList = new ArrayList<>();

        for (Course course : courses) {
            MiniScheduleDTO schedule = new MiniScheduleDTO();
            schedule.setCourseId(course.getId());
            schedule.setCourseName(course.getCourseName());
            schedule.setCourseType(course.getCourseType());
            schedule.setCourseDate(course.getCourseDate());
            schedule.setStartTime(course.getStartTime());
            schedule.setEndTime(course.getEndTime());
            schedule.setLocation(course.getLocation());
            schedule.setMaxCapacity(course.getMaxCapacity());
            schedule.setCurrentEnrollment(course.getCurrentEnrollment());
            schedule.setRemainingSlots(course.getMaxCapacity() - course.getCurrentEnrollment());

            // 获取该课程的学员列表
            schedule.setStudents(getCourseStudents(coachId, course.getId()));
            scheduleList.add(schedule);
        }

        return scheduleList;
    }

    @Override
    public List<MiniCourseStudentDTO> getCourseStudents(Long coachId, Long courseId) {
        // 验证课程是否属于该教练
        Course course = courseMapper.selectById(courseId);
        if (course == null || !course.getCoachId().equals(coachId)) {
            throw new BusinessException("无权查看该课程的学员");
        }

        // 查询课程预约
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getCourseId, courseId)
                .in(CourseBooking::getBookingStatus, 0, 1, 2) // 待上课、已签到、已完成
                .orderByAsc(CourseBooking::getBookingTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
        List<MiniCourseStudentDTO> studentList = new ArrayList<>();

        for (CourseBooking booking : bookings) {
            Member member = memberMapper.selectById(booking.getMemberId());
            if (member == null) continue;

            MiniCourseStudentDTO student = new MiniCourseStudentDTO();
            student.setMemberId(member.getId());
            student.setMemberName(member.getRealName());
            student.setMemberPhone(member.getPhone());
            student.setMemberNo(member.getMemberNo());
            student.setBookingId(booking.getId());
            student.setBookingStatus(booking.getBookingStatus());
            student.setBookingTime(booking.getBookingTime());
            student.setCheckinTime(booking.getCheckinTime());
            student.setMembershipEndDate(member.getMembershipEndDate() != null ?
                    member.getMembershipEndDate().toString() : "");

            // 获取会员剩余课时
            student.setRemainingSessions(getMemberRemainingSessions(member.getId(), course.getCourseType()));

            studentList.add(student);
        }

        return studentList;
    }

    @Override
    public MemberFullDTO getMemberDetail(Long coachId, Long memberId) {
        // 验证教练是否有权查看该会员（通过课程关联）
        if (!canCoachViewMember(coachId, memberId)) {
            throw new BusinessException("无权查看该会员信息");
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        MemberFullDTO dto = new MemberFullDTO();
        BeanUtils.copyProperties(member, dto);
        dto.setUsername(member.getPhone());

        // 获取该教练所教课程中该会员的预约记录
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getMemberId, memberId);

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
        List<Course> coachCourses = getCoachCourses(coachId);
        if (!CollectionUtils.isEmpty(coachCourses)) {
            List<Long> courseIds = coachCourses.stream().map(Course::getId).collect(Collectors.toList());
            bookingQuery.in(CourseBooking::getCourseId, courseIds);
            // 这里可以添加课程记录转换逻辑
        }

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHealthRecord(Long coachId, Long memberId, HealthRecordDTO healthRecordDTO) {
        // 验证教练是否有权为该会员添加健康档案
        if (!canCoachViewMember(coachId, memberId)) {
            throw new BusinessException("无权为该会员添加健康档案");
        }

        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        HealthRecord record = new HealthRecord();
        BeanUtils.copyProperties(healthRecordDTO, record);
        record.setMemberId(memberId);
        record.setRecordedBy(coach.getRealName());

        int result = healthRecordMapper.insert(record);
        if (result <= 0) {
            throw new BusinessException("添加健康档案失败");
        }
    }

    @Override
    public MiniFinanceStatsDTO getFinanceStats(Long coachId, String period) {
        MiniFinanceStatsDTO stats = new MiniFinanceStatsDTO();
        stats.setPeriod(period);

        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            return stats;
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate, endDate;

        switch (period) {
            case "day":
                startDate = now;
                endDate = now;
                break;
            case "month":
                startDate = now.withDayOfMonth(1);
                endDate = now;
                break;
            case "year":
                startDate = now.withDayOfYear(1);
                endDate = now;
                break;
            default:
                startDate = now.withDayOfMonth(1);
                endDate = now;
        }

        // 查询该时间范围内的课程
        LambdaQueryWrapper<Course> courseQuery = new LambdaQueryWrapper<>();
        courseQuery.eq(Course::getCoachId, coachId)
                .between(Course::getCourseDate, startDate, endDate);

        List<Course> courses = courseMapper.selectList(courseQuery);

        // 计算本月数据
        int monthCourseHours = courses.stream()
                .mapToInt(c -> c.getDuration() != null ? c.getDuration() : 0)
                .sum() / 60; // 转换为小时

        // 本月服务会员数（去重）
        List<Long> memberIds = new ArrayList<>();
        for (Course course : courses) {
            LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
            bookingQuery.eq(CourseBooking::getCourseId, course.getId())
                    .in(CourseBooking::getBookingStatus, 1, 2); // 已签到或已完成
            List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
            memberIds.addAll(bookings.stream().map(CourseBooking::getMemberId).collect(Collectors.toList()));
        }
        long monthMemberCount = memberIds.stream().distinct().count();

        // 本月收入
        BigDecimal monthIncome = coach.getHourlyRate() != null ?
                BigDecimal.valueOf(monthCourseHours).multiply(coach.getHourlyRate()) :
                BigDecimal.ZERO;

        stats.setMonthCourseHours(monthCourseHours);
        stats.setMonthMemberCount((int) monthMemberCount);
        stats.setMonthIncome(monthIncome);

        // 总数据
        stats.setTotalCourseHours(coach.getTotalCourses() != null ? coach.getTotalCourses() : 0);
        stats.setTotalMemberCount(coach.getTotalStudents() != null ? coach.getTotalStudents() : 0);
        stats.setTotalIncome(coach.getTotalIncome() != null ? coach.getTotalIncome() : BigDecimal.ZERO);

        // 生成趋势数据（示例，实际需要按日期分组统计）
        stats.setCourseTrend(generateTrendData(startDate, endDate, "course"));
        stats.setIncomeTrend(generateTrendData(startDate, endDate, "income"));
        stats.setMemberTrend(generateTrendData(startDate, endDate, "member"));

        return stats;
    }

    @Override
    public MiniReminderDTO getCurrentReminder(Long coachId) {
        MiniReminderDTO reminder = new MiniReminderDTO();
        reminder.setHasReminder(false);

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // 查询教练当前时段的课程
        LambdaQueryWrapper<Course> courseQuery = new LambdaQueryWrapper<>();
        courseQuery.eq(Course::getCoachId, coachId)
                .eq(Course::getCourseDate, today)
                .le(Course::getStartTime, currentTime.plusMinutes(30)) // 即将开始的课程
                .ge(Course::getEndTime, currentTime); // 尚未结束的课程

        List<Course> courses = courseMapper.selectList(courseQuery);
        if (CollectionUtils.isEmpty(courses)) {
            return reminder;
        }

        Course course = courses.get(0); // 取第一个
        LocalDateTime courseStart = LocalDateTime.of(today, course.getStartTime());

        // 查询该课程的预约学员
        LambdaQueryWrapper<CourseBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(CourseBooking::getCourseId, course.getId())
                .eq(CourseBooking::getBookingStatus, 0); // 待上课

        List<CourseBooking> bookings = courseBookingMapper.selectList(bookingQuery);
        if (!CollectionUtils.isEmpty(bookings)) {
            CourseBooking booking = bookings.get(0);
            Member member = memberMapper.selectById(booking.getMemberId());
            if (member != null) {
                reminder.setHasReminder(true);
                reminder.setType("COACH");
                reminder.setMemberId(member.getId());
                reminder.setMemberName(member.getRealName());
                reminder.setBookedCourseName(course.getCourseName());
                reminder.setBookedStartTime(courseStart);
                reminder.setBookedEndTime(LocalDateTime.of(today, course.getEndTime()));
            }
        }

        return reminder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPassword(Long coachId, String oldPassword, String newPassword) {
        Coach coach = coachMapper.selectById(coachId);
        if (coach == null) {
            throw new BusinessException("教练不存在");
        }

        // 验证旧密码
        if (!bCryptUtil.matches(oldPassword, coach.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新新密码
        coach.setPassword(bCryptUtil.encodePassword(newPassword));
        coachMapper.updateById(coach);
        log.info("教练密码修改成功，教练ID：{}", coachId);
    }

    /**
     * 获取教练的所有课程
     */
    private List<Course> getCoachCourses(Long coachId) {
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getCoachId, coachId);
        return courseMapper.selectList(queryWrapper);
    }

    /**
     * 判断教练是否可以查看会员信息
     */
    private boolean canCoachViewMember(Long coachId, Long memberId) {
        List<Course> courses = getCoachCourses(coachId);
        if (CollectionUtils.isEmpty(courses)) {
            return false;
        }

        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());

        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CourseBooking::getCourseId, courseIds)
                .eq(CourseBooking::getMemberId, memberId);

        Long count = courseBookingMapper.selectCount(queryWrapper);
        return count > 0;
    }

    /**
     * 获取会员剩余课时
     */
    private Integer getMemberRemainingSessions(Long memberId, Integer courseType) {
        // 需要根据订单项查询
        // 此方法需要您根据实际业务实现
        return 0;
    }

    /**
     * 生成趋势数据
     */
    private List<ChartDataDTO> generateTrendData(LocalDate startDate, LocalDate endDate, String type) {
        List<ChartDataDTO> trend = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            ChartDataDTO point = new ChartDataDTO();
            point.setDate(current.format(DATE_FORMATTER));
            point.setLabel(current.format(DateTimeFormatter.ofPattern("MM-dd")));
            point.setValue(BigDecimal.ZERO);
            trend.add(point);
            current = current.plusDays(1);
        }
        return trend;
    }
}