package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "小程序消息DTO")
public class MiniMessageDTO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "消息类型描述")
    private String typeDesc;

    @Schema(description = "消息标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "关联ID")
    private Long relatedId;

    @Schema(description = "是否已读：0-未读，1-已读")
    private Integer isRead;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "时间描述（刚刚、几分钟前等）")
    private String timeDesc;

    @Schema(description = "关联数据（预约详情、课程详情等）")
    private Object relatedData;

    public String getTypeDesc() {
        if (messageType == null) return "系统通知";
        switch (messageType) {
            case "BOOKING_SUCCESS": return "预约成功";
            case "BOOKING_CANCEL": return "预约取消";
            case "CHECKIN_SUCCESS": return "签到成功";
            case "COURSE_REMINDER": return "课程提醒";
            default: return "系统通知";
        }
    }
}