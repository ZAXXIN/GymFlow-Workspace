package com.gymflow.utils;

import com.gymflow.dto.settings.System.BusinessConfigDTO;
import com.gymflow.exception.BusinessException;
import com.gymflow.service.settings.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 系统配置验证工具类
 * 用于在业务逻辑中验证时间、人数等是否符合系统配置
 */
@Component
@RequiredArgsConstructor
public class SystemConfigValidator {

    private final SystemConfigService systemConfigService;

    /**
     * 获取业务配置
     */
    private BusinessConfigDTO getBusinessConfig() {
        return systemConfigService.getConfig().getBusiness();
    }

    /**
     * 获取系统名称
     */
    public String getSystemName() {
        return systemConfigService.getConfig().getBasic().getSystemName();
    }

    /**
     * 获取系统logo
     */
    public String getSystemLogo() {
        return systemConfigService.getConfig().getBasic().getSystemLogo();
    }

    /**
     * 获取课程提前预约时间（小时）
     * 课程开始前多少小时内不可预约
     */
    public int getAdvanceBookingHours() {
        BusinessConfigDTO config = getBusinessConfig();
        return config.getCourseAdvanceBookingHours() != null ? config.getCourseAdvanceBookingHours() : 2;
    }

    /**
     * 获取课程取消小时数
     */
    public int getCourseCancelHours() {
        return getBusinessConfig().getCourseCancelHours();
    }

    /**
     * 获取最低开课人数
     */
    public int getMinClassSize() {
        return getBusinessConfig().getMinClassSize();
    }

    /**
     * 获取最大课程容量
     */
    public int getMaxClassCapacity() {
        return getBusinessConfig().getMaxClassCapacity();
    }

    /**
     * 获取营业开始时间
     */
    public LocalTime getBusinessStartTime() {
        return getBusinessConfig().getBusinessStartTime();
    }

    /**
     * 获取营业结束时间
     */
    public LocalTime getBusinessEndTime() {
        return getBusinessConfig().getBusinessEndTime();
    }

    // ========== 新增：签到相关方法 ==========

    /**
     * 获取签到开始时间（课程开始前多少分钟可签到）
     */
    public int getCheckinStartMinutes() {
        BusinessConfigDTO config = getBusinessConfig();
        return config.getCheckinStartMinutes() != null ? config.getCheckinStartMinutes() : 30;
    }

    /**
     * 获取签到截止时间（课程开始后多少分钟截止）
     */
    public int getCheckinEndMinutes() {
        BusinessConfigDTO config = getBusinessConfig();
        return config.getCheckinEndMinutes() != null ? config.getCheckinEndMinutes() : 0;
    }

    /**
     * 获取自动完成时间（课程结束后多少小时自动完成）
     */
    public int getAutoCompleteHours() {
        BusinessConfigDTO config = getBusinessConfig();
        return config.getAutoCompleteHours() != null ? config.getAutoCompleteHours() : 1;
    }

    /**
     * 验证课程时间是否在营业时间内
     */
    public void validateBusinessHours(LocalTime startTime, LocalTime endTime) {
        LocalTime businessStart = getBusinessStartTime();
        LocalTime businessEnd = getBusinessEndTime();

        if (startTime.isBefore(businessStart) || endTime.isAfter(businessEnd)) {
            throw new BusinessException(String.format(
                    "课程时间必须在营业时间内（%s - %s）",
                    businessStart.toString().substring(0, 5),
                    businessEnd.toString().substring(0, 5)
            ));
        }
    }

    /**
     * 验证课程容量是否符合配置
     * @param currentSize 当前报名人数
     * @param maxSize 课程最大容量
     */
    public void validateClassCapacity(int currentSize, int maxSize) {
        int maxCapacity = getMaxClassCapacity();

        if (currentSize > maxCapacity) {
            throw new BusinessException("课程人数不能超过最大容量：" + maxCapacity + "人");
        }

        if (maxSize > maxCapacity) {
            throw new BusinessException("课程最大容量不能超过系统配置：" + maxCapacity + "人");
        }
    }

    /**
     * 验证团课是否可以开课（人数是否达到最低要求）
     */
    public void validateGroupClassStart(int currentEnrollment) {
        int minSize = getMinClassSize();

        if (currentEnrollment < minSize) {
            throw new BusinessException("当前报名人数" + currentEnrollment + "人，未达到最低开课人数" + minSize + "人");
        }
    }

    /**
     * 验证课程是否可以预约
     * @param courseDateTime 课程开始时间
     * @return true-可以预约，false-已过预约截止时间
     */
    public boolean canBookCourse(LocalDateTime courseDateTime) {
        int advanceHours = getAdvanceBookingHours();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime latestBookingTime = courseDateTime.minusHours(advanceHours);
        return now.isBefore(latestBookingTime);
    }

    /**
     * 验证课程预约时间限制（如果不能预约则抛出异常）
     */
    public void validateCourseBooking(LocalDateTime courseDateTime) {
        if (!canBookCourse(courseDateTime)) {
            int advanceHours = getAdvanceBookingHours();
            throw new BusinessException("已超过预约时间，课程开始前" + advanceHours + "小时内不可预约");
        }
    }

    /**
     * 验证课程是否可以取消
     * @param courseDateTime 课程开始时间
     * @return true-可以取消，false-已过取消时限
     */
    public boolean canCancelCourse(LocalDateTime courseDateTime) {
        int cancelHours = getCourseCancelHours();

        LocalDateTime now = LocalDateTime.now();
        long hoursUntilCourse = java.time.Duration.between(now, courseDateTime).toHours();

        return hoursUntilCourse >= cancelHours;
    }

    /**
     * 验证课程取消时间限制（如果不能取消则抛出异常）
     */
    public void validateCourseCancellation(LocalDateTime courseDateTime) {
        if (!canCancelCourse(courseDateTime)) {
            int cancelHours = getCourseCancelHours();
            throw new BusinessException("课程开始前" + cancelHours + "小时内不能取消");
        }
    }

    // ========== 新增：签到时间验证方法 ==========

    /**
     * 验证签到时间是否有效
     * @param courseDateTime 课程开始时间
     * @return true-可以签到，false-不在签到时间范围内
     */
    public boolean canCheckIn(LocalDateTime courseDateTime) {
        LocalDateTime now = LocalDateTime.now();
        int startMinutes = getCheckinStartMinutes();
        int endMinutes = getCheckinEndMinutes();

        // 签到开始时间 = 课程开始时间 - startMinutes
        LocalDateTime checkinStartTime = courseDateTime.minusMinutes(startMinutes);

        // 签到截止时间 = 课程开始时间 + endMinutes
        LocalDateTime checkinEndTime = endMinutes == 0 ?
                courseDateTime : courseDateTime.plusMinutes(endMinutes);

        // 如果endMinutes为0，表示课程开始后不可签到
        return !now.isBefore(checkinStartTime) && !now.isAfter(checkinEndTime);
    }

    /**
     * 验证签到时间是否有效（带异常抛出）
     */
    public void validateCheckInTime(LocalDateTime courseDateTime) {
        if (!canCheckIn(courseDateTime)) {
            int startMinutes = getCheckinStartMinutes();
            int endMinutes = getCheckinEndMinutes();

            String message;
            if (endMinutes == 0) {
                message = String.format("请在课程开始前%d分钟内签到", startMinutes);
            } else {
                message = String.format("请在课程开始前%d分钟至开始后%d分钟内签到",
                        startMinutes, endMinutes);
            }
            throw new BusinessException(message);
        }
    }
}