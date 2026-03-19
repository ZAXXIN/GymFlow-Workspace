package com.gymflow.dto.mini;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "我的课程包DTO")
public class MyCourseDTO {

    @Schema(description = "订单明细ID")
    private Long orderItemId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品类型：1-私教课，2-团课")
    private Integer productType;

    @Schema(description = "商品类型描述")
    private String productTypeDesc;

    @Schema(description = "总课时数")
    private Integer totalSessions;

    @Schema(description = "剩余课时数")
    private Integer remainingSessions;

    @Schema(description = "已用课时数")
    private Integer usedSessions;

    @Schema(description = "有效期开始")
    private LocalDate validityStartDate;

    @Schema(description = "有效期结束")
    private LocalDate validityEndDate;

    @Schema(description = "状态：ACTIVE-有效，USED_UP-已用完，EXPIRED-已过期")
    private String status;

    @Schema(description = "状态描述")
    private String statusDesc;

    public String getProductTypeDesc() {
        if (productType == null) return "未知";
        switch (productType) {
            case 1: return "私教课";
            case 2: return "团课";
            default: return "未知";
        }
    }

    public String getStatusDesc() {
        if (status == null) return "未知";
        switch (status) {
            case "ACTIVE": return "有效";
            case "USED_UP": return "已用完";
            case "EXPIRED": return "已过期";
            default: return status;
        }
    }

    public Integer getUsedSessions() {
        if (totalSessions == null || remainingSessions == null) return 0;
        return totalSessions - remainingSessions;
    }
}