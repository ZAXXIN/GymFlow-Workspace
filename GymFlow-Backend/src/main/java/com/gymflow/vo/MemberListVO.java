package com.gymflow.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MemberListVO {

    private Long id;
    private String memberNo;
    private String username;
    private String phone;
    private String realName;
    private Integer gender;
    private String genderDesc;
    private Integer age;
    private LocalDate birthday;
    private String personalCoachName;
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private Integer status;
    private String statusDesc;
    private Integer totalCheckins;
    private Integer totalCourseHours;
    private BigDecimal totalSpent;
    private LocalDateTime createTime;

    public String getGenderDesc() {
        if (gender == null) return "未知";
        return gender == 1 ? "男" : "女";
    }

    public String getStatusDesc() {
        if (status == null) return "未知";
        return status == 1 ? "正常" : "禁用";
    }

    public Integer getAge() {
        if (birthday == null) return null;
        return LocalDate.now().getYear() - birthday.getYear();
    }
}