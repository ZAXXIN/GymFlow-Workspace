package com.gymflow.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    // 会员卡类型（去重后的所有卡类型）
    private List<Integer> cardTypes;

    // 会员卡状态：有效/过期（只要有一张有效卡就是有效）
    private String cardStatus;
    private String cardStatusDesc;

    // 统计信息
    private Integer totalCheckins;
    private Integer totalCourseHours;
    private BigDecimal totalSpent;

    public String getGenderDesc() {
        if (gender == null) return "未知";
        return gender == 1 ? "男" : "女";
    }

    /**
     * 获取卡类型显示文本
     */
    public String getCardTypesDesc() {
        if (cardTypes == null || cardTypes.isEmpty()) {
            return "-";
        }
        return cardTypes.stream()
                .map(this::getCardTypeName)
                .collect(Collectors.joining("、"));
    }

    /**
     * 获取卡类型名称
     */
    private String getCardTypeName(Integer cardType) {
        if (cardType == null) return "未知";
        switch (cardType) {
            case 0: return "会籍卡";
            case 1: return "私教课";
            case 2: return "团课";
            case 3: return "相关产品";
            default: return "其他";
        }
    }

    public String getCardStatusDesc() {
        if (cardStatus == null) return "";
        switch (cardStatus) {
            case "ACTIVE": return "有效";
            case "EXPIRED": return "过期";
//            case "USED_UP": return "用完";
            default: return "未知";
        }
    }
}