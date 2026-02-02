package com.gymflow.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "商品基础信息DTO")
public class ProductBasicDTO {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200")
    @Schema(description = "商品名称", example = "健身月卡", required = true)
    private String productName;

    @NotNull(message = "商品类型不能为空")
    @Min(value = 0, message = "商品类型值只能是0-3")
    @Max(value = 3, message = "商品类型值只能是0-3")
    @Schema(description = "商品类型：0-会籍卡，1-私教课，2-团课，3-相关产品", example = "0", required = true)
    private Integer productType;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Size(max = 1000, message = "商品描述长度不能超过1000")
    @Schema(description = "商品描述", example = "健身月卡，包含器械区和团课使用权")
    private String description;

    @Schema(description = "商品图片列表")
    private List<String> images;

    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.00", message = "原价不能小于0")
    @Schema(description = "原价", example = "399.00", required = true)
    private BigDecimal originalPrice;

    @NotNull(message = "现价不能为空")
    @DecimalMin(value = "0.00", message = "现价不能小于0")
    @Schema(description = "现价", example = "299.00", required = true)
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.00", message = "成本价不能小于0")
    @Schema(description = "成本价", example = "100.00")
    private BigDecimal costPrice;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存数量不能小于0")
    @Schema(description = "库存数量", example = "100", required = true)
    private Integer stockQuantity;

    @Schema(description = "单位", example = "张")
    private String unit;

    @Schema(description = "规格信息")
    private String specifications;

    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值只能是0或1")
    @Max(value = 1, message = "状态值只能是0或1")
    @Schema(description = "状态：0-下架，1-在售", example = "1", required = true)
    private Integer status;

    @Schema(description = "商品详情信息")
    private ProductDetailDTO detailDTO;
}