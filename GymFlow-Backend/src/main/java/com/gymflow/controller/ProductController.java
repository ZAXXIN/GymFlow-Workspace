package com.gymflow.controller;

import com.gymflow.common.Result;
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
    @Operation(summary = "分页查询商品列表", description = "根据条件分页查询商品列表")
    public Result<PageResultVO<ProductListVO>> getProductList(@Valid @RequestBody ProductQueryDTO queryDTO) {
        try {
            PageResultVO<ProductListVO> result = productService.getProductList(queryDTO);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询商品列表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/detail/{productId}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取商品详细信息")
    public Result<ProductFullDTO> getProductDetail(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId) {
        try {
            ProductFullDTO detail = productService.getProductDetail(productId);
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            log.error("获取商品详情失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/add")
    @Operation(summary = "新增商品", description = "添加新商品，包含基础信息和详情")
    public Result<Long> addProduct(@Valid @RequestBody ProductBasicDTO productDTO) {
        try {
            Long productId = productService.addProduct(productDTO);
            return Result.success("添加成功", productId);
        } catch (Exception e) {
            log.error("添加商品失败：{}", e.getMessage(), e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/update/{productId}")
    @Operation(summary = "编辑商品", description = "更新商品信息")
    public Result<Void> updateProduct(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId,
            @Valid @RequestBody ProductBasicDTO productDTO) {
        try {
            productService.updateProduct(productId, productDTO);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新商品失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{productId}")
    @Operation(summary = "删除商品", description = "根据商品ID删除商品")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId) {
        try {
            productService.deleteProduct(productId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除商品失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/updateStatus/{productId}")
    @Operation(summary = "更新商品状态", description = "更新商品状态：0-下架，1-在售")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId,
            @RequestParam @NotNull Integer status) {
        try {
            productService.updateProductStatus(productId, status);
            return Result.success("更新状态成功");
        } catch (Exception e) {
            log.error("更新商品状态失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PutMapping("/updateStock/{productId}")
    @Operation(summary = "更新商品库存", description = "更新商品库存数量")
    public Result<Void> updateProductStock(
            @Parameter(description = "商品ID", required = true)
            @PathVariable @NotNull Long productId,
            @RequestParam @NotNull Integer quantity) {
        try {
            productService.updateProductStock(productId, quantity);
            return Result.success("更新库存成功");
        } catch (Exception e) {
            log.error("更新商品库存失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/categories")
    @Operation(summary = "获取所有商品分类", description = "获取所有商品分类（树形结构）")
    public Result<List<ProductCategoryDTO>> getAllCategories() {
        try {
            List<ProductCategoryDTO> categories = productService.getAllCategories();
            return Result.success("查询成功", categories);
        } catch (Exception e) {
            log.error("获取商品分类失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/category/detail/{categoryId}")
    @Operation(summary = "获取分类详情", description = "根据分类ID获取分类详细信息")
    public Result<ProductCategoryDTO> getCategoryDetail(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId) {
        try {
            ProductCategoryDTO detail = productService.getCategoryDetail(categoryId);
            return Result.success("查询成功", detail);
        } catch (Exception e) {
            log.error("获取分类详情失败：{}", e.getMessage(), e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/category/add")
    @Operation(summary = "新增分类", description = "添加新商品分类")
    public Result<Long> addCategory(@Valid @RequestBody ProductCategoryDTO categoryDTO) {
        try {
            Long categoryId = productService.addCategory(categoryDTO);
            return Result.success("添加成功", categoryId);
        } catch (Exception e) {
            log.error("添加分类失败：{}", e.getMessage(), e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    @PutMapping("/category/update/{categoryId}")
    @Operation(summary = "编辑分类", description = "更新商品分类信息")
    public Result<Void> updateCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId,
            @Valid @RequestBody ProductCategoryDTO categoryDTO) {
        try {
            productService.updateCategory(categoryId, categoryDTO);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新分类失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/category/delete/{categoryId}")
    @Operation(summary = "删除分类", description = "根据分类ID删除分类")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId) {
        try {
            productService.deleteCategory(categoryId);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除分类失败：{}", e.getMessage(), e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/category/updateStatus/{categoryId}")
    @Operation(summary = "更新分类状态", description = "更新分类状态：0-禁用，1-启用")
    public Result<Void> updateCategoryStatus(
            @Parameter(description = "分类ID", required = true)
            @PathVariable @NotNull Long categoryId,
            @RequestParam @NotNull Integer status) {
        try {
            productService.updateCategoryStatus(categoryId, status);
            return Result.success("更新状态成功");
        } catch (Exception e) {
            log.error("更新分类状态失败：{}", e.getMessage(), e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}