package com.gymflow.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "商品完整信息DTO")
public class ProductFullDTO {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品类型：0-会籍卡，1-私教课，2-团课，3-相关产品")
    private Integer productType;

    @Schema(description = "商品类型描述")
    private String productTypeDesc;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品图片列表")
    private List<String> images;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "现价")
    private BigDecimal currentPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "库存数量")
    private Integer stockQuantity;

    @Schema(description = "销量")
    private Integer salesVolume;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "有效期（天）")
    private Integer validityDays;

    @Schema(description = "规格信息")
    private String specifications;

    @Schema(description = "状态：0-下架，1-在售")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "总课时数（用于私教课、团课）")
    private Integer totalSessions;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "商品详情信息")
    private ProductDetailDTO detailDTO;
}