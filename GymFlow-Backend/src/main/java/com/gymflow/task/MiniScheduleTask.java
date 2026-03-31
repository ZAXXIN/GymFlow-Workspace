package com.gymflow.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gymflow.entity.CourseBooking;
import com.gymflow.entity.CourseSchedule;
import com.gymflow.mapper.CourseBookingMapper;
import com.gymflow.mapper.CourseScheduleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务
 * 1. 自动完成课程（课程结束后自动将状态从已签到改为已完成）
 * 2. 未签到过期课程处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MiniScheduleTask {

    private final CourseBookingMapper courseBookingMapper;
    private final CourseScheduleMapper courseScheduleMapper;

    /**
     * 应用启动时立即执行一次所有任务
     */
    @PostConstruct
    public void init() {
        log.info("========== 定时任务初始化，启动时立即执行 ==========");
        // 启动时立即执行一次
        expireMissedCourses();
        autoCompleteCourses();
        log.info("========== 定时任务初始化完成 ==========");
    }

    /**
     * 每半小时执行一次，自动完成课程
     * cron 含义：每小时的第0分钟和第30分钟执行
     * 例如：00:00, 00:30, 01:00, 01:30...
     */
    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoCompleteCourses() {
        log.info("开始执行自动完成课程任务 - {}", LocalDateTime.now());

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
     * 每半小时执行一次，将未签到的已结束课程标记为已过期
     */
    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void expireMissedCourses() {
        log.info("定时任务：开始处理未签到的过期课程 - {}", LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();

        // 查询所有待上课的预约
        List<CourseBooking> pendingBookings = courseBookingMapper.selectList(
                new LambdaQueryWrapper<CourseBooking>()
                        .eq(CourseBooking::getBookingStatus, 0) // 待上课
        );

        int expiredCount = 0;

        for (CourseBooking booking : pendingBookings) {
            CourseSchedule schedule = courseScheduleMapper.selectById(booking.getScheduleId());
            if (schedule == null) continue;

            LocalDateTime courseEnd = LocalDateTime.of(schedule.getScheduleDate(), schedule.getEndTime());

            // 课程已结束，但未签到
            if (now.isAfter(courseEnd)) {
                booking.setBookingStatus(4); // 已过期
                booking.setCancellationReason("课程已结束，未签到");
                booking.setCancellationTime(now);
                courseBookingMapper.updateById(booking);
                expiredCount++;

                log.debug("预约已过期，预约ID：{}，课程ID：{}，课程结束时间：{}",
                        booking.getId(), schedule.getCourseId(), courseEnd);
            }
        }

        log.info("未签到过期课程处理完成，共处理 {} 条记录", expiredCount);
    }
}