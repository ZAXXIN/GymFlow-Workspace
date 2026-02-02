package com.gymflow.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "商品详情DTO")
public class ProductDetailDTO {

    @Schema(description = "会籍类型：0-私教课，1-团课，2-月卡，3-年卡，4-其他")
    private Integer membershipType;

    @Schema(description = "关联教练ID")
    private Long coachId;

    @Schema(description = "课时长（分钟）")
    private Integer courseDuration;

    @Schema(description = "总节数")
    private Integer totalSessions;

    @Schema(description = "可用节数")
    private Integer availableSessions;

    @Size(max = 20, message = "课程级别长度不能超过20")
    @Schema(description = "课程级别", example = "入门")
    private String courseLevel;

    @Schema(description = "会籍权益")
    private List<String> membershipBenefits;

    @Min(value = 0, message = "有效期天数不能小于0")
    @Schema(description = "有效期天数", example = "30")
    private Integer validityDays;

    @Min(value = 0, message = "默认总课时数不能小于0")
    @Schema(description = "默认总课时数", example = "10")
    private Integer defaultTotalSessions;

    @Min(value = 1, message = "最大购买数量不能小于1")
    @Schema(description = "最大购买数量", example = "10")
    private Integer maxPurchaseQuantity;

    @Size(max = 500, message = "退款政策长度不能超过500")
    @Schema(description = "退款政策")
    private String refundPolicy;

    @Size(max = 500, message = "使用规则长度不能超过500")
    @Schema(description = "使用规则")
    private String usageRules;

    @Schema(description = "适用教练ID列表")
    private List<Long> coachIds;
}