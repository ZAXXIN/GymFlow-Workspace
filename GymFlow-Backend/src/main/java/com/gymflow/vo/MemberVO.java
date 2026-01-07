package com.gymflow.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gymflow.enums.MemberStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MemberVO {

    private Long id;

    private Long userId;

    private String memberNo;

    private String username;

    private String phone;

    private String email;

    private String avatar;

    private String realName;

    private Integer gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    private BigDecimal height;

    private BigDecimal weight;

    private String membershipType;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate membershipStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate membershipEndDate;

    private Long personalCoachId;

    private String coachName;

    private Integer totalCheckins;

    private Integer totalCourseHours;

    private MemberStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createTime;
}