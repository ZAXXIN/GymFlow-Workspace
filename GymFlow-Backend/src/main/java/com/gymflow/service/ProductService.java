package com.gymflow.service;

import com.gymflow.dto.product.*;
import com.gymflow.vo.PageResultVO;
import com.gymflow.vo.ProductListVO;

import java.util.List;

public interface ProductService {

    /**
     * 分页查询商品列表
     */
    PageResultVO<ProductListVO> getProductList(ProductQueryDTO queryDTO);

    /**
     * 根据类型查询商品列表（用于会员卡选择）
     */
    List<ProductListVO> getProductsByType(Integer productType);

    /**
     * 获取商品详情
     */
    ProductFullDTO getProductDetail(Long productId);

    /**
     * 新增商品
     */
    Long addProduct(ProductBasicDTO productDTO);

    /**
     * 编辑商品
     */
    void updateProduct(Long productId, ProductBasicDTO productDTO);

    /**
     * 删除商品
     */
    void deleteProduct(Long productId);

    /**
     * 更新商品状态
     */
    void updateProductStatus(Long productId, Integer status);

    /**
     * 更新库存
     */
    void updateProductStock(Long productId, Integer quantity);

    /**
     * 获取所有商品分类（树形结构）
     */
    List<ProductCategoryDTO> getAllCategories();

    /**
     * 获取分类详情
     */
    ProductCategoryDTO getCategoryDetail(Long categoryId);

    /**
     * 新增分类
     */
    Long addCategory(ProductCategoryDTO categoryDTO);

    /**
     * 编辑分类
     */
    void updateCategory(Long categoryId, ProductCategoryDTO categoryDTO);

    /**
     * 删除分类
     */
    void deleteCategory(Long categoryId);

    /**
     * 更新分类状态
     */
    void updateCategoryStatus(Long categoryId, Integer status);
}