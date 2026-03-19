package com.gymflow.vo.settings;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalTime;

@Data
@Schema(description = "系统配置视图对象")
public class SystemConfigVO {

    // ========== 基本设置 ==========

    @Schema(description = "系统名称", example = "GymFlow健身管理系统")
    private String systemName;

    @Schema(description = "系统logo", example = "/logo.png")
    private String systemLogo;

    // ========== 业务设置 ==========

    @Schema(description = "营业开始时间", example = "08:00")
    private LocalTime businessStartTime;

    @Schema(description = "营业结束时间", example = "22:00")
    private LocalTime businessEndTime;

    @Schema(description = "课程提前续约时间（天）", example = "7")
    private Integer courseRenewalDays;

    @Schema(description = "课程取消时间限制（小时）", example = "2")
    private Integer courseCancelHours;

    @Schema(description = "最低开课人数", example = "5")
    private Integer minClassSize;

    @Schema(description = "最大课程容量", example = "30")
    private Integer maxClassCapacity;

    // ========== 签到相关配置 ==========

    @Schema(description = "课程开始前多少分钟可签到", example = "30")
    private Integer checkinStartMinutes;

    @Schema(description = "课程开始后多少分钟截止签到", example = "0")
    private Integer checkinEndMinutes;

    @Schema(description = "课程结束后多少小时自动完成", example = "1")
    private Integer autoCompleteHours;

    @Schema(description = "最后更新时间", example = "2026-03-04 10:30:00")
    private String updateTime;

    // 格式化后的显示
    @Schema(description = "营业时间显示", example = "08:00 - 22:00")
    public String getBusinessHoursDisplay() {
        if (businessStartTime == null || businessEndTime == null) {
            return "";
        }
        return businessStartTime.toString().substring(0, 5) + " - " +
                businessEndTime.toString().substring(0, 5);
    }

    // ========== 签到规则显示 ==========

    @Schema(description = "签到规则显示", example = "课前30分钟可签到")
    public String getCheckinRuleDisplay() {
        if (checkinStartMinutes == null) return "";

        if (checkinEndMinutes == null || checkinEndMinutes == 0) {
            return "课前" + checkinStartMinutes + "分钟可签到";
        } else {
            return "课前" + checkinStartMinutes + "分钟至课后" +
                    checkinEndMinutes + "分钟可签到";
        }
    }

    @Schema(description = "自动完成规则显示", example = "课后1小时自动完成")
    public String getAutoCompleteDisplay() {
        if (autoCompleteHours == null) return "";
        return "课后" + autoCompleteHours + "小时自动完成";
    }
}