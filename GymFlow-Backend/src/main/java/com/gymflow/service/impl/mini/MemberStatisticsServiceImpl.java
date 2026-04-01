package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.dto.mini.MemberStatisticsDTO;
import com.gymflow.entity.*;
import com.gymflow.mapper.*;
import com.gymflow.service.mini.MemberStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberStatisticsServiceImpl implements MemberStatisticsService {

    private final CheckInRecordMapper checkInRecordMapper;
    private final CourseBookingMapper courseBookingMapper;
    private final CourseMapper courseMapper;
    private final MemberMapper memberMapper;
    private final OrderItemMapper orderItemMapper;

    @Override
    public MemberStatisticsDTO getMemberStatistics(Long memberId, Long orderItemId) {
        log.info("获取会员统计数据，会员ID：{}，订单项ID：{}", memberId, orderItemId);

        // 验证会员是否存在
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }

        // 验证订单项是否存在
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        if (orderItem == null) {
            throw new RuntimeException("卡片不存在");
        }

        MemberStatisticsDTO dto = new MemberStatisticsDTO();

        // 根据商品类型决定显示什么
        if (orderItem.getProductType() == 0) {
            // 会籍卡：显示签到记录
            log.info("会籍卡，显示签到记录");
            dto.setCheckinStats(getCheckinStatistics(memberId));
            dto.setSessionStats(new MemberStatisticsDTO.SessionStatistics());
        } else {
            // 课程卡：根据 orderItemId 查询该课程包的课时明细
            log.info("课程卡，显示课时明细");
            dto.setCheckinStats(new MemberStatisticsDTO.CheckinStatistics());
            dto.setSessionStats(getSessionStatisticsByOrderItem(orderItemId));
        }

        return dto;
    }

    /**
     * 获取签到统计（自由训练签到）
     */
    private MemberStatisticsDTO.CheckinStatistics getCheckinStatistics(Long memberId) {
        // 查询自由训练签到记录（不关联课程预约）
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckinRecord::getMemberId, memberId)
                .isNull(CheckinRecord::getCourseBookingId)
                .orderByDesc(CheckinRecord::getCheckinTime);

        List<CheckinRecord> checkins = checkInRecordMapper.selectList(queryWrapper);

        MemberStatisticsDTO.CheckinStatistics stats = new MemberStatisticsDTO.CheckinStatistics();
        stats.setTotalCheckins(checkins.size());

        // 转换最近签到记录（只取最近10条）
        List<MemberStatisticsDTO.CheckinRecordDTO> recentCheckins = checkins.stream()
                .limit(10)
                .map(this::convertToCheckinRecordDTO)
                .collect(Collectors.toList());
        stats.setRecentCheckins(recentCheckins);

        return stats;
    }

    /**
     * 根据订单项ID获取课时统计
     */
    private MemberStatisticsDTO.SessionStatistics getSessionStatisticsByOrderItem(Long orderItemId) {
        MemberStatisticsDTO.SessionStatistics stats = new MemberStatisticsDTO.SessionStatistics();
        List<MemberStatisticsDTO.SessionRecordDTO> details = new ArrayList<>();

        // 获取订单项信息
        OrderItem orderItem = orderItemMapper.selectById(orderItemId);
        if (orderItem == null) {
            stats.setTotalConsumed(0);
            stats.setTotalReturned(0);
            stats.setDetails(details);
            return stats;
        }

        // 1. 课时消耗记录（已签到或已完成的预约，且关联到该订单项）
        LambdaQueryWrapper<CourseBooking> consumedWrapper = new LambdaQueryWrapper<>();
        consumedWrapper.eq(CourseBooking::getOrderItemId, orderItemId)
                .in(CourseBooking::getBookingStatus, 1, 2)  // 已签到、已完成
                .orderByDesc(CourseBooking::getCheckinTime);

        List<CourseBooking> consumedBookings = courseBookingMapper.selectList(consumedWrapper);

        int totalConsumed = 0;
        for (CourseBooking booking : consumedBookings) {
            Course course = courseMapper.selectById(booking.getCourseId());
            if (course == null) continue;

            Integer sessionCost = course.getSessionCost() != null ? course.getSessionCost() : 1;
            totalConsumed += sessionCost;

            MemberStatisticsDTO.SessionRecordDTO record = new MemberStatisticsDTO.SessionRecordDTO();
            record.setType("CONSUME");
            record.setTime(booking.getCheckinTime() != null ? booking.getCheckinTime() : booking.getUpdateTime());
            record.setCourseName(course.getCourseName());
            record.setSessions(sessionCost);
            record.setBookingId(booking.getId());
            details.add(record);
        }

        // 2. 课时归还记录（已取消的预约，且关联到该订单项）
        LambdaQueryWrapper<CourseBooking> returnedWrapper = new LambdaQueryWrapper<>();
        returnedWrapper.eq(CourseBooking::getOrderItemId, orderItemId)
                .eq(CourseBooking::getBookingStatus, 3)  // 已取消
                .orderByDesc(CourseBooking::getCancellationTime);

        List<CourseBooking> returnedBookings = courseBookingMapper.selectList(returnedWrapper);

        int totalReturned = 0;
        for (CourseBooking booking : returnedBookings) {
            Course course = courseMapper.selectById(booking.getCourseId());
            if (course == null) continue;

            Integer sessionCost = course.getSessionCost() != null ? course.getSessionCost() : 1;
            totalReturned += sessionCost;

            MemberStatisticsDTO.SessionRecordDTO record = new MemberStatisticsDTO.SessionRecordDTO();
            record.setType("RETURN");
            record.setTime(booking.getCancellationTime());
            record.setCourseName(course.getCourseName());
            record.setSessions(sessionCost);
            record.setBookingId(booking.getId());
            record.setReason(booking.getCancellationReason());
            details.add(record);
        }

        // 按时间倒序排序
        details.sort((a, b) -> b.getTime().compareTo(a.getTime()));

        stats.setTotalConsumed(totalConsumed);
        stats.setTotalReturned(totalReturned);
        stats.setDetails(details);

        return stats;
    }

    /**
     * 转换为签到记录DTO
     */
    private MemberStatisticsDTO.CheckinRecordDTO convertToCheckinRecordDTO(CheckinRecord record) {
        MemberStatisticsDTO.CheckinRecordDTO dto = new MemberStatisticsDTO.CheckinRecordDTO();
        dto.setCheckinTime(record.getCheckinTime());
        dto.setCheckinMethod(record.getCheckinMethod());
        dto.setCheckinMethodDesc(record.getCheckinMethod() == 0 ? "教练签到" : "前台签到");
        return dto;
    }
}