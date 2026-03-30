package com.gymflow.dto.coach;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoachFullDTO {

    // 基础信息
    private Long id;
    private String realName;
    private String phone;
    private Integer gender;
    private String genderDesc;
    private String specialty;
    private List<String> certificationList;
    private Integer yearsOfExperience;
    private Integer status;
    private BigDecimal rating;
    private String introduction;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 用户类型（用于小程序端）
    private Integer userType = 1;  // 1-教练
    private String userTypeDesc = "教练";

    // 扩展信息
    private List<CoachScheduleDTO> schedules;
    private List<CoachCourseDTO> courses;

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