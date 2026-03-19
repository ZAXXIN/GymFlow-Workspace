package com.gymflow.entity.mini;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 小程序签到码实体类
 * 用于存储预约生成的签到码（6位数字码和二维码）
 */
@Data
@TableName("mini_checkin_code")
public class MiniCheckinCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 预约ID
     */
    @TableField("booking_id")
    private Long bookingId;

    /**
     * 6位数字码
     */
    @TableField("digital_code")
    private String digitalCode;

    /**
     * 二维码图片URL
     */
    @TableField("qr_code_url")
    private String qrCodeUrl;

    /**
     * 状态：0-未使用，1-已使用，2-已过期
     */
    private Integer status;

    /**
     * 有效开始时间
     */
    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    /**
     * 有效结束时间
     */
    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    /**
     * 实际签到时间
     */
    @TableField("checkin_time")
    private LocalDateTime checkinTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    private String statusDesc;

    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未使用";
            case 1: return "已使用";
            case 2: return "已过期";
            default: return "未知";
        }
    }
}