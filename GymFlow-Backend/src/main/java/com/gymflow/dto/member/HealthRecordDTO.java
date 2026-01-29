package com.gymflow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HealthRecordDTO {

    private LocalDate recordDate;
    private BigDecimal weight;
    private BigDecimal bodyFatPercentage;
    private BigDecimal muscleMass;
    private BigDecimal bmi;
    private BigDecimal chestCircumference;
    private BigDecimal waistCircumference;
    private BigDecimal hipCircumference;
    private String bloodPressure;
    private Integer heartRate;
    private String notes;
}