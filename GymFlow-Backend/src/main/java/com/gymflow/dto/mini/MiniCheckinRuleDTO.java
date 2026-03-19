package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "签到规则DTO")
public class MiniCheckinRuleDTO {

    @Schema(description = "课程开始前多少分钟可签到", example = "30")
    private Integer checkinStartMinutes;

    @Schema(description = "课程开始后多少分钟截止签到", example = "0")
    private Integer checkinEndMinutes;

    @Schema(description = "课程结束后多少小时自动完成", example = "1")
    private Integer autoCompleteHours;

    @Schema(description = "签到规则显示文本", example = "课前30分钟可签到")
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