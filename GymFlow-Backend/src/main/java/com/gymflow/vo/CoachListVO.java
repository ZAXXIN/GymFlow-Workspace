package com.gymflow.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoachListVO {

    private Long id;
    private String realName;
    private String phone;
    private Integer gender;
    private String genderDesc;
    private String specialty;
    private List<String> certifications;
    private Integer yearsOfExperience;
    private Integer status;
    private String statusDesc;
    private Integer totalCourses;
    private BigDecimal rating;
    private LocalDateTime createTime;

    // 获取性别描述
    public String getGenderDesc() {
        if (gender == null) return "未知";
        switch (gender) {
            case 0: return "女";
            case 1: return "男";
            default: return "未知";
        }
    }
}