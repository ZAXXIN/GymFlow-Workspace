package com.gymflow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MemberBasicDTO {

    // 用户信息
    private String username;
    private String password;
    private String phone;
    private String realName;
    private Integer gender;
    private LocalDate birthday;
    private String department;
    private String position;

    // 会员信息
    private String memberNo;
    private String idCard;
    private BigDecimal height;
    private BigDecimal weight;
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private Long personalCoachId;
    private String address;
}