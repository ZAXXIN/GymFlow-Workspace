package com.gymflow.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberListVO {

    private Long id;
    private String memberNo;
    private String phone;
    private String realName;
    private Integer gender;
    private String genderDesc;
    private Integer age;
    private LocalDateTime createTime;

    // 会员卡信息
    private Integer cardType;
    private String cardTypeDesc;
    private String cardStatus;
    private String cardStatusDesc;
    private LocalDateTime cardEndDate;
    private Integer remainingSessions;

    // 统计信息
    private Integer totalCheckins;
    private Integer totalCourseHours;
    private BigDecimal totalSpent;

    public String getGenderDesc() {
        if (gender == null) return "未知";
        return gender == 1 ? "男" : "女";
    }

    public String getCardTypeDesc() {
        if (cardType == null) return "";
        switch (cardType) {
            case 0: return "会籍卡";
            case 1: return "私教课";
            case 2: return "团课";
            case 3: return "相关产品";
            default: return "未知";
        }
    }

    public String getCardStatusDesc() {
        if (cardStatus == null) return "";
        switch (cardStatus) {
            case "ACTIVE": return "有效";
            case "EXPIRED": return "过期";
            case "USED_UP": return "用完";
            default: return "未知";
        }
    }
}