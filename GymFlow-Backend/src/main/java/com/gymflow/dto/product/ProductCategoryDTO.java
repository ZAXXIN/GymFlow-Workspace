package com.gymflow.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "商品分类DTO")
public class ProductCategoryDTO {

    @Schema(description = "分类ID")
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100")
    @Schema(description = "分类名称", example = "会籍卡", required = true)
    private String categoryName;

    @Schema(description = "父分类ID", example = "0")
    private Long parentId = 0L;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-启用", example = "1", required = true)
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "子分类列表")
    private List<ProductCategoryDTO> children;
}