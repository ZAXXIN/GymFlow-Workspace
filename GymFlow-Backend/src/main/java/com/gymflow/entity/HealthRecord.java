package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("health_record")
public class HealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long memberId;

    @TableField("record_date")
    private LocalDate recordDate;

    private BigDecimal weight;

    @TableField("body_fat_percentage")
    private BigDecimal bodyFatPercentage;

    @TableField("muscle_mass")
    private BigDecimal muscleMass;

    private BigDecimal bmi;

    @TableField("chest_circumference")
    private BigDecimal chestCircumference;

    @TableField("waist_circumference")
    private BigDecimal waistCircumference;

    @TableField("hip_circumference")
    private BigDecimal hipCircumference;

    @TableField("blood_pressure")
    private String bloodPressure;

    @TableField("heart_rate")
    private Integer heartRate;

    @TableField("recorded_by")
    private String recordedBy;

    private String notes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}