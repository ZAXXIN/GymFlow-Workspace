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
    private String personalCoachName;
    private LocalDateTime createTime;

    // 会员卡信息
    private Integer cardType; // 主要会员卡类型
    private String cardTypeDesc; // 类型描述
    private String cardStatus; // 状态：ACTIVE-有效，EXPIRED-过期，USED_UP-用完
    private String cardStatusDesc;
    private LocalDateTime cardEndDate; // 有效期结束时间
    private Integer remainingSessions; // 剩余课时数（仅私教课、团课）

    // 统计信息
    private Integer totalCheckins;
    private Integer totalCourseHours;
    private BigDecimal totalSpent;

    // 会员卡状态计算方法
    public String getCardTypeDesc() {
        if (cardType == null) return "";
        switch (cardType) {
            case 0: return "私教课";
            case 1: return "团课";
            case 2: return "月卡";
            case 3: return "年卡";
            case 4: return "周卡";
            case 5: return "其他";
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