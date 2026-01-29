package com.gymflow.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "健康记录DTO")
public class HealthRecordDTO {

    @NotNull(message = "记录日期不能为空")
    @Schema(description = "记录日期", example = "2026-01-28", required = true)
    private LocalDate recordDate;

    @DecimalMin(value = "0.01", message = "体重不能小于0.01")
    @Schema(description = "体重（kg）", example = "65.5", required = true)
    private BigDecimal weight;

    @DecimalMin(value = "0.00", message = "体脂率不能小于0")
    @DecimalMin(value = "100.00", message = "体脂率不能大于100")
    @Schema(description = "体脂率（%）", example = "18.5")
    private BigDecimal bodyFatPercentage;

    @DecimalMin(value = "0.01", message = "肌肉量不能小于0.01")
    @Schema(description = "肌肉量（kg）", example = "28.0")
    private BigDecimal muscleMass;

    @DecimalMin(value = "0.01", message = "BMI值不能小于0.01")
    @Schema(description = "BMI值", example = "22.3")
    private BigDecimal bmi;

    @DecimalMin(value = "0.01", message = "胸围不能小于0.01")
    @Schema(description = "胸围（cm）", example = "95.0")
    private BigDecimal chestCircumference;

    @DecimalMin(value = "0.01", message = "腰围不能小于0.01")
    @Schema(description = "腰围（cm）", example = "80.0")
    private BigDecimal waistCircumference;

    @DecimalMin(value = "0.01", message = "臀围不能小于0.01")
    @Schema(description = "臀围（cm）", example = "98.0")
    private BigDecimal hipCircumference;

    @Size(max = 20, message = "血压长度不能超过20")
    @Schema(description = "血压（格式：收缩压/舒张压）", example = "120/80")
    private String bloodPressure;

    @Range(min = 30, max = 200, message = "心率需在30-200之间")
    @Schema(description = "心率（次/分钟）", example = "75")
    private Integer heartRate;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(description = "备注信息", example = "今日运动后心率略高")
    private String notes;
}