package com.gymflow.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "订单项DTO")
public class OrderItemDTO {

    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID必须为正数")
    @Schema(description = "商品ID", example = "1", required = true)
    private Long productId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200")
    @Schema(description = "商品名称", example = "私教10节课", required = true)
    private String productName;

    @NotNull(message = "商品类型不能为空")
    @Min(value = 0, message = "商品类型值只能是0-3")
    @Max(value = 3, message = "商品类型值只能是0-3")
    @Schema(description = "商品类型：0-会籍卡，1-私教课，2-团课，3-相关产品", example = "1", required = true)
    private Integer productType;

    @Min(value = 1, message = "数量不能小于1")
    @Schema(description = "数量", example = "1", required = true)
    private Integer quantity;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价不能小于0.01")
    @Schema(description = "单价", example = "4200.00", required = true)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.01", message = "总价不能小于0.01")
    @Schema(description = "总价", example = "4200.00")
    private BigDecimal totalPrice;

    @Schema(description = "有效期开始日期", example = "2024-03-01")
    private LocalDate validityStartDate;

    @Schema(description = "有效期结束日期", example = "2024-09-01")
    private LocalDate validityEndDate;

    @Schema(description = "总课时数", example = "10")
    private Integer totalSessions;

    @Schema(description = "剩余课时数", example = "10")
    private Integer remainingSessions;

    @Size(max = 200, message = "商品图片URL长度不能超过200")
    @Schema(description = "商品图片URL", example = "/images/pt10.jpg")
    private String productImage;
}