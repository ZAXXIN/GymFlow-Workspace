package com.gymflow.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MemberFullDTO {

    // 会员基本信息
    private Long id;
    private Long userId;
    private String memberNo;
    private String username;
    private String phone;
    private String realName;
    private Integer gender;
    private LocalDateTime createTime;
    private Integer status;

    // 扩展信息
    private String idCard;
    private BigDecimal height;
    private BigDecimal weight;
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private String personalCoachName;
    private Integer totalCheckins;
    private Integer totalCourseHours;
    private BigDecimal totalSpent;
    private String address;

    // 健康档案列表
    private List<HealthRecordDTO> healthRecords;

    // 会员卡列表
    private List<MemberCardDTO> memberCards;

    // 课程记录列表
    private List<CourseRecordDTO> courseRecords;

    // 签到记录列表
    private List<CheckinRecordDTO> checkinRecords;
}