package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "会员卡DTO")
public class MiniMemberCardDTO {
    @Schema(description = "订单项ID（卡片唯一标识）")
    private Long orderItemId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "卡类型：0-会籍卡，1-私教课，2-团课")
    private Integer cardType;

    @Schema(description = "卡类型描述")
    private String cardTypeDesc;

    @Schema(description = "有效期开始")
    private LocalDate startDate;

    @Schema(description = "有效期结束")
    private LocalDate endDate;

    @Schema(description = "总课时数")
    private Integer totalSessions;

    @Schema(description = "已用课时数")
    private Integer usedSessions;

    @Schema(description = "剩余课时数")
    private Integer remainingSessions;

    @Schema(description = "状态：ACTIVE-有效，EXPIRED-过期，USED_UP-用完")
    private String status;

    @Schema(description = "状态描述")
    private String statusDesc;

    public String getCardTypeDesc() {
        if (cardType == null) return "未知";
        switch (cardType) {
            case 0: return "会籍卡";
            case 1: return "私教课";
            case 2: return "团课";
            case 3: return "相关产品";
            default: return "其他";
        }
    }

    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case "ACTIVE": return "有效";
            case "EXPIRED": return "已过期";
            case "USED_UP": return "已用完";
            default: return status;
        }
    }
}