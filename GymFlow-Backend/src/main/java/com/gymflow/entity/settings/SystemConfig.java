package com.gymflow.entity.settings;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("system_config")
public class SystemConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ========== 基本设置 ==========

    /**
     * 系统名称
     */
    @TableField("system_name")
    private String systemName;

    /**
     * 系统logo URL
     */
    @TableField("system_logo")
    private String systemLogo;

    // ========== 业务设置 ==========

    /**
     * 营业开始时间
     */
    @TableField("business_start_time")
    private LocalTime businessStartTime;

    /**
     * 营业结束时间
     */
    @TableField("business_end_time")
    private LocalTime businessEndTime;

    /**
     * 课程提前续约时间（天）
     */
    @TableField("course_renewal_days")
    private Integer courseRenewalDays;

    /**
     * 课程取消时间限制（小时）
     * 课程开始前几小时内可以取消
     */
    @TableField("course_cancel_hours")
    private Integer courseCancelHours;

    /**
     * 最低开课人数（团课）
     */
    @TableField("min_class_size")
    private Integer minClassSize;

    /**
     * 最大课程容量（团课最多参与人数）
     */
    @TableField("max_class_capacity")
    private Integer maxClassCapacity;

    /**
     * 最后更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}