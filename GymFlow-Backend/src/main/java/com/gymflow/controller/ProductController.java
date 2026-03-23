package com.gymflow.controller;

import com.gymflow.common.Result;
import com.gymflow.common.annotation.PreAuthorize;
import com.gymflow.dto.product.*;
import com.gymflow.service.ProductService;
import com.gymflow.vo.PageResultVO;
import com.gymflow.vo.ProductListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/product")
@Tag(name = "商品管理", description = "商品管理相关接口")
@Validated
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/list")
    @Operation(summary = "分页查询商品列表")
    @PreAuthorize("product:view")
    public Result<PageResultVO<ProductListVO>> getProductList(@Valid @RequestBody ProductQueryDTO queryDTO) {
        PageResultVO<ProductListVO> result = productService.getProductList(queryDTO);
        return Result.success("查询成功", result);
    }

    @GetMapping("/list-by-type")
    @Operation(summary = "根据类型查询商品列表")
    @PreAuthorize("product:view")
    public Result<List<ProductListVO>> getProductsByType(
            @Parameter(description = "商品类型：0-会籍卡，1-私教课，2-团课，3-相关产品", required = true)
            @RequestParam @NotNull Integer productType) {
        List<ProductListVO> products = productService.getProductsByType(productType);
        return Result.success("查询成功", products);
    }

    @GetMapping("/detail/{productId}")
    @Operation(summary = "获取商品详情")
    @PreAuthorize("product:detail")
    public Result<ProductFullDTO> getProductDetail(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId) {
        ProductFullDTO detail = productService.getProductDetail(productId);
        return Result.success("查询成功", detail);
    }

    @PostMapping("/add")
    @Operation(summary = "新增商品")
    @PreAuthorize("product:add")
    public Result<Long> addProduct(@Valid @RequestBody ProductBasicDTO productDTO) {
        Long productId = productService.addProduct(productDTO);
        return Result.success("添加成功", productId);
    }

    @PutMapping("/update/{productId}")
    @Operation(summary = "编辑商品")
    @PreAuthorize("product:edit")
    public Result<Void> updateProduct(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId,
            @Valid @RequestBody ProductBasicDTO productDTO) {
        productService.updateProduct(productId, productDTO);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{productId}")
    @Operation(summary = "删除商品")
    @PreAuthorize("product:delete")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId) {
        productService.deleteProduct(productId);
        return Result.success("删除成功");
    }

    @PutMapping("/updateStatus/{productId}")
    @Operation(summary = "更新商品状态")
    @PreAuthorize("product:status")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId,
            @RequestParam @NotNull Integer status) {
        productService.updateProductStatus(productId, status);
        return Result.success("更新状态成功");
    }

    @PutMapping("/updateStock/{productId}")
    @Operation(summary = "更新商品库存")
    @PreAuthorize("product:stock")
    public Result<Void> updateProductStock(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId,
            @RequestParam @NotNull Integer quantity) {
        productService.updateProductStock(productId, quantity);
        return Result.success("更新库存成功");
    }
}