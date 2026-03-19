package com.gymflow.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.entity.CourseBooking;
import com.gymflow.entity.mini.MiniCheckinCode;
import com.gymflow.mapper.CourseBookingMapper;
import com.gymflow.mapper.mini.MiniCheckinCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 小程序定时任务
 * 1. 自动完成课程（课程结束后自动将状态从已签到改为已完成）
 * 2. 过期签到码处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MiniScheduleTask {

    private final CourseBookingMapper courseBookingMapper;
    private final MiniCheckinCodeMapper miniCheckinCodeMapper;

    /**
     * 每小时执行一次，自动完成课程
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoCompleteCourses() {
        log.info("开始执行自动完成课程任务");

        LocalDateTime now = LocalDateTime.now();

        // 查询需要自动完成的预约（已签到且到达自动完成时间）
        LambdaQueryWrapper<CourseBooking> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseBooking::getBookingStatus, 1) // 已签到
                .le(CourseBooking::getAutoCompleteTime, now) // 到达自动完成时间
                .isNotNull(CourseBooking::getAutoCompleteTime);

        List<CourseBooking> bookings = courseBookingMapper.selectList(queryWrapper);

        int count = 0;
        for (CourseBooking booking : bookings) {
            booking.setBookingStatus(2); // 已完成
            courseBookingMapper.updateById(booking);
            count++;
        }

        log.info("自动完成课程任务执行完成，共处理 {} 条记录", count);
    }

    /**
     * 每小时执行一次，处理过期的签到码
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expireCheckinCodes() {
        log.info("开始处理过期签到码");

        LocalDateTime now = LocalDateTime.now();

        // 查询过期的签到码
        List<MiniCheckinCode> expiredCodes = miniCheckinCodeMapper.selectExpiredCodes(now);

        int count = 0;
        for (MiniCheckinCode code : expiredCodes) {
            code.setStatus(2); // 已过期
            miniCheckinCodeMapper.updateById(code);
            count++;
        }

        log.info("过期签到码处理完成，共处理 {} 条记录", count);
    }
}