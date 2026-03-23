package com.gymflow.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Data
@Schema(description = "商品查询DTO")
public class ProductQueryDTO {

    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码（默认1）")
    private Integer pageNum = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 100, message = "页大小不能超过100")
    @Schema(description = "页大小（默认10）")
    private Integer pageSize = 10;

    @Schema(description = "商品类型：0-会籍卡，1-私教课，2-团课，3-相关产品")
    private Integer productType;

    @Size(max = 200, message = "商品名称长度不能超过200")
    @Schema(description = "商品名称（模糊查询）")
    private String productName;

    @Schema(description = "状态：0-下架，1-在售")
    private Integer status;

    @Schema(description = "是否包含库存为0的商品")
    private Boolean includeZeroStock = false;
}