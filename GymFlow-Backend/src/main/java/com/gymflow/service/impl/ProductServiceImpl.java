package com.gymflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymflow.dto.product.*;
import com.gymflow.entity.*;
import com.gymflow.exception.BusinessException;
import com.gymflow.mapper.*;
import com.gymflow.service.ProductService;
import com.gymflow.utils.SystemConfigValidator;
import com.gymflow.vo.PageResultVO;
import com.gymflow.vo.ProductListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductDetailMapper productDetailMapper;
    private final OrderItemMapper orderItemMapper;
    private final ObjectMapper objectMapper;

    // 注入系统配置验证器
    private final SystemConfigValidator configValidator;

    @Override
    public PageResultVO<ProductListVO> getProductList(ProductQueryDTO queryDTO) {
        log.info("查询商品列表，查询条件：{}", queryDTO);

        // 创建分页对象
        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        // 商品类型查询
        if (queryDTO.getProductType() != null) {
            queryWrapper.eq(Product::getProductType, queryDTO.getProductType());
        }

        // 商品名称查询
        if (StringUtils.hasText(queryDTO.getProductName())) {
            queryWrapper.like(Product::getProductName, queryDTO.getProductName());
        }

        // 分类ID查询
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(Product::getCategoryId, queryDTO.getCategoryId());
        }

        // 状态查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Product::getStatus, queryDTO.getStatus());
        }

        // 库存查询
        if (Boolean.FALSE.equals(queryDTO.getIncludeZeroStock())) {
            queryWrapper.gt(Product::getStockQuantity, 0);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Product::getCreateTime);

        // 执行分页查询
        IPage<Product> productPage = productMapper.selectPage(page, queryWrapper);

        // 转换为VO列表
        List<ProductListVO> voList = productPage.getRecords().stream()
                .map(this::convertToProductListVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, productPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 根据类型查询商品列表（用于会员卡选择）
     */
    @Override
    public List<ProductListVO> getProductsByType(Integer productType) {
        log.info("根据类型查询商品列表，商品类型：{}", productType);

        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductType, productType);
        queryWrapper.eq(Product::getStatus, 1); // 只在售商品
        queryWrapper.gt(Product::getStockQuantity, 0); // 库存大于0
        queryWrapper.orderByDesc(Product::getCreateTime);

        // 查询商品列表
        List<Product> products = productMapper.selectList(queryWrapper);

        // 转换为VO列表
        return products.stream()
                .map(this::convertToProductListVO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductFullDTO getProductDetail(Long productId) {
        log.info("获取商品详情，商品ID：{}", productId);

        // 获取商品信息
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 构建完整DTO
        ProductFullDTO fullDTO = convertToProductFullDTO(product);

        // 获取商品详情
        ProductDetail productDetail = productDetailMapper.selectOne(
                new LambdaQueryWrapper<ProductDetail>()
                        .eq(ProductDetail::getProductId, productId)
        );
        if (productDetail != null) {
            fullDTO.setDetailDTO(convertToProductDetailDTO(productDetail));

            // 设置总课时数到商品主表（用于前端显示）
            if (productDetail.getTotalSessions() != null) {
                fullDTO.setTotalSessions(productDetail.getTotalSessions());
            }
        }

        return fullDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addProduct(ProductBasicDTO productDTO) {
        log.info("开始添加商品，商品名称：{}，商品类型：{}",
                productDTO.getProductName(), productDTO.getProductType());

        // 验证商品名称是否已存在
        LambdaQueryWrapper<Product> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(Product::getProductName, productDTO.getProductName());
        Long count = productMapper.selectCount(nameQuery);
        if (count > 0) {
            throw new BusinessException("商品名称已存在");
        }

        // 验证价格
        if (productDTO.getCurrentPrice().compareTo(productDTO.getOriginalPrice()) > 0) {
            throw new BusinessException("现价不能高于原价");
        }

        // ========== 新增：验证课程类商品的容量是否符合系统配置 ==========
        if (productDTO.getProductType() == 2 && productDTO.getDetailDTO() != null) { // 团课
            configValidator.validateClassCapacity(0,
                    productDTO.getDetailDTO().getMaxPurchaseQuantity());
        }

        // 创建商品记录
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        // 处理图片列表（JSON格式）
        if (!CollectionUtils.isEmpty(productDTO.getImages())) {
            try {
                product.setImages(objectMapper.writeValueAsString(productDTO.getImages()));
            } catch (Exception e) {
                log.error("转换商品图片列表失败", e);
                throw new BusinessException("商品图片格式错误");
            }
        } else {
            product.setImages("[]");
        }

        // 处理规格信息（JSON格式）
        if (StringUtils.hasText(productDTO.getSpecifications())) {
            product.setSpecifications(productDTO.getSpecifications());
        } else {
            product.setSpecifications("{}");
        }

        // 设置默认值
        product.setSalesVolume(0);
        product.setValidityDays(getDefaultValidityDays(productDTO.getProductType()));

        // 保存商品
        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException("添加商品失败");
        }

        Long productId = product.getId();
        log.info("商品保存成功，商品ID：{}", productId);

        // 保存商品详情
        if (productDTO.getDetailDTO() != null) {
            saveProductDetail(productId, productDTO.getDetailDTO());
        }

        log.info("添加商品成功，商品ID：{}，商品名称：{}", productId, product.getProductName());
        return productId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long productId, ProductBasicDTO productDTO) {
        log.info("开始更新商品，商品ID：{}", productId);

        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 验证商品名称是否已被其他商品使用
        if (!product.getProductName().equals(productDTO.getProductName())) {
            LambdaQueryWrapper<Product> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(Product::getProductName, productDTO.getProductName());
            nameQuery.ne(Product::getId, productId);
            Long count = productMapper.selectCount(nameQuery);
            if (count > 0) {
                throw new BusinessException("商品名称已被其他商品使用");
            }
        }

        // 验证价格
        if (productDTO.getCurrentPrice().compareTo(productDTO.getOriginalPrice()) > 0) {
            throw new BusinessException("现价不能高于原价");
        }

        // ========== 新增：验证课程类商品的容量是否符合系统配置 ==========
        if (productDTO.getProductType() == 2 && productDTO.getDetailDTO() != null) { // 团课
            configValidator.validateClassCapacity(0,
                    productDTO.getDetailDTO().getMaxPurchaseQuantity());
        }

        // 检查商品是否已被购买（如果已被购买，某些字段不允许修改）
        checkProductCanBeModified(productId);

        // 更新商品信息
        BeanUtils.copyProperties(productDTO, product, "id", "salesVolume");

        // 处理图片列表
        if (!CollectionUtils.isEmpty(productDTO.getImages())) {
            try {
                product.setImages(objectMapper.writeValueAsString(productDTO.getImages()));
            } catch (Exception e) {
                log.error("转换商品图片列表失败", e);
                throw new BusinessException("商品图片格式错误");
            }
        }

        // 处理规格信息
        if (StringUtils.hasText(productDTO.getSpecifications())) {
            product.setSpecifications(productDTO.getSpecifications());
        }

        // 更新有效期天数
        product.setValidityDays(getDefaultValidityDays(productDTO.getProductType()));

        // 更新商品
        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("更新商品失败");
        }

        // 更新商品详情
        if (productDTO.getDetailDTO() != null) {
            updateProductDetail(productId, productDTO.getDetailDTO());
        }

        log.info("更新商品成功，商品ID：{}", productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId) {
        log.info("开始删除商品，商品ID：{}", productId);

        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查商品是否已被购买
        checkProductHasBeenPurchased(productId);

        // 删除商品详情
        LambdaQueryWrapper<ProductDetail> detailQuery = new LambdaQueryWrapper<>();
        detailQuery.eq(ProductDetail::getProductId, productId);
        productDetailMapper.delete(detailQuery);

        // 删除商品
        int result = productMapper.deleteById(productId);
        if (result <= 0) {
            throw new BusinessException("删除商品失败");
        }

        log.info("删除商品成功，商品ID：{}", productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(Long productId, Integer status) {
        log.info("更新商品状态，商品ID：{}，状态：{}", productId, status);

        // 检查状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（下架）或1（在售）");
        }

        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 如果商品下架，检查是否有未完成的订单
        if (status == 0) {
            checkProductHasActiveOrders(productId);
        }

        // 更新状态
        product.setStatus(status);

        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("更新商品状态失败");
        }

        log.info("更新商品状态成功，商品ID：{}，新状态：{}", productId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStock(Long productId, Integer quantity) {
        log.info("更新商品库存，商品ID：{}，变更数量：{}", productId, quantity);

        // 检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 计算新库存
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new BusinessException("库存不能小于0");
        }

        // 更新库存
        product.setStockQuantity(newStock);

        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("更新商品库存失败");
        }

        log.info("更新商品库存成功，商品ID：{}，新库存：{}", productId, newStock);
    }

    @Override
    public List<ProductCategoryDTO> getAllCategories() {
        log.info("获取所有商品分类");

        // 查询所有分类
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCategory::getStatus, 1);
        queryWrapper.orderByAsc(ProductCategory::getParentId).orderByAsc(ProductCategory::getCreateTime);

        List<ProductCategory> categories = productCategoryMapper.selectList(queryWrapper);

        // 构建树形结构
        return buildCategoryTree(categories);
    }

    @Override
    public ProductCategoryDTO getCategoryDetail(Long categoryId) {
        log.info("获取分类详情，分类ID：{}", categoryId);

        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        return convertToProductCategoryDTO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addCategory(ProductCategoryDTO categoryDTO) {
        log.info("开始添加分类，分类名称：{}", categoryDTO.getCategoryName());

        // 验证分类名称是否已存在
        LambdaQueryWrapper<ProductCategory> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(ProductCategory::getCategoryName, categoryDTO.getCategoryName());
        if (categoryDTO.getParentId() != null) {
            nameQuery.eq(ProductCategory::getParentId, categoryDTO.getParentId());
        }
        Long count = productCategoryMapper.selectCount(nameQuery);
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }

        // 创建分类记录
        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(categoryDTO, category);

        int result = productCategoryMapper.insert(category);
        if (result <= 0) {
            throw new BusinessException("添加分类失败");
        }

        log.info("添加分类成功，分类ID：{}，分类名称：{}", category.getId(), category.getCategoryName());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long categoryId, ProductCategoryDTO categoryDTO) {
        log.info("开始更新分类，分类ID：{}", categoryId);

        // 检查分类是否存在
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 验证分类名称是否已被其他分类使用
        if (!category.getCategoryName().equals(categoryDTO.getCategoryName())) {
            LambdaQueryWrapper<ProductCategory> nameQuery = new LambdaQueryWrapper<>();
            nameQuery.eq(ProductCategory::getCategoryName, categoryDTO.getCategoryName());
            nameQuery.ne(ProductCategory::getId, categoryId);
            if (categoryDTO.getParentId() != null) {
                nameQuery.eq(ProductCategory::getParentId, categoryDTO.getParentId());
            }
            Long count = productCategoryMapper.selectCount(nameQuery);
            if (count > 0) {
                throw new BusinessException("分类名称已被其他分类使用");
            }
        }

        // 检查是否修改了父分类
        if (!category.getParentId().equals(categoryDTO.getParentId())) {
            // 不能将父分类设置为自己的子分类（避免循环）
            if (isCircularReference(categoryId, categoryDTO.getParentId())) {
                throw new BusinessException("不能将分类设置为自己的子分类");
            }
        }

        // 更新分类信息
        BeanUtils.copyProperties(categoryDTO, category, "id");

        int result = productCategoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException("更新分类失败");
        }

        log.info("更新分类成功，分类ID：{}", categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long categoryId) {
        log.info("开始删除分类，分类ID：{}", categoryId);

        // 检查分类是否存在
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        LambdaQueryWrapper<ProductCategory> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(ProductCategory::getParentId, categoryId);
        Long childCount = productCategoryMapper.selectCount(childQuery);
        if (childCount > 0) {
            throw new BusinessException("该分类下有子分类，不能删除");
        }

        // 检查是否有商品使用该分类
        LambdaQueryWrapper<Product> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(Product::getCategoryId, categoryId);
        Long productCount = productMapper.selectCount(productQuery);
        if (productCount > 0) {
            throw new BusinessException("有商品使用该分类，不能删除");
        }

        // 删除分类
        int result = productCategoryMapper.deleteById(categoryId);
        if (result <= 0) {
            throw new BusinessException("删除分类失败");
        }

        log.info("删除分类成功，分类ID：{}", categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategoryStatus(Long categoryId, Integer status) {
        log.info("更新分类状态，分类ID：{}，状态：{}", categoryId, status);

        // 检查状态值
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（禁用）或1（启用）");
        }

        // 检查分类是否存在
        ProductCategory category = productCategoryMapper.selectById(categoryId);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 更新状态
        category.setStatus(status);

        int result = productCategoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException("更新分类状态失败");
        }

        log.info("更新分类状态成功，分类ID：{}，新状态：{}", categoryId, status);
    }

    // ========== 私有辅助方法 ==========

    private ProductListVO convertToProductListVO(Product product) {
        ProductListVO vo = new ProductListVO();
        BeanUtils.copyProperties(product, vo);

        // 设置商品类型描述
        vo.setProductTypeDesc(getProductTypeDesc(product.getProductType()));

        // 设置状态描述
        vo.setStatusDesc(product.getStatus() == 1 ? "在售" : "下架");

        // 计算折扣
        if (product.getOriginalPrice() != null && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = product.getCurrentPrice()
                    .divide(product.getOriginalPrice(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(10));
            vo.setDiscount(discount);
        }

        // 查询商品详情，获取总课时数
        try {
            ProductDetail productDetail = productDetailMapper.selectOne(
                    new LambdaQueryWrapper<ProductDetail>()
                            .eq(ProductDetail::getProductId, product.getId())
            );
            if (productDetail != null && productDetail.getTotalSessions() != null) {
                vo.setTotalSessions(productDetail.getTotalSessions());
            }
        } catch (Exception e) {
            log.error("查询商品详情失败，商品ID：{}", product.getId(), e);
        }

        // 解析图片列表
        if (StringUtils.hasText(product.getImages())) {
            try {
                List<String> images = objectMapper.readValue(product.getImages(),
                        new TypeReference<List<String>>() {});
                vo.setImages(images);
            } catch (Exception e) {
                log.error("解析商品图片列表失败，商品ID：{}", product.getId(), e);
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }

        return vo;
    }

    private ProductFullDTO convertToProductFullDTO(Product product) {
        ProductFullDTO dto = new ProductFullDTO();
        BeanUtils.copyProperties(product, dto);

        // 设置商品类型描述
        dto.setProductTypeDesc(getProductTypeDesc(product.getProductType()));

        // 设置状态描述
        dto.setStatusDesc(product.getStatus() == 1 ? "在售" : "下架");

        // 解析图片列表
        if (StringUtils.hasText(product.getImages())) {
            try {
                List<String> images = objectMapper.readValue(product.getImages(),
                        new TypeReference<List<String>>() {});
                dto.setImages(images);
            } catch (Exception e) {
                log.error("解析商品图片列表失败，商品ID：{}", product.getId(), e);
                dto.setImages(new ArrayList<>());
            }
        } else {
            dto.setImages(new ArrayList<>());
        }

        // 获取分类名称
        if (product.getCategoryId() != null) {
            ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getCategoryName());
            }
        }

        return dto;
    }

    private ProductDetailDTO convertToProductDetailDTO(ProductDetail productDetail) {
        ProductDetailDTO dto = new ProductDetailDTO();
        BeanUtils.copyProperties(productDetail, dto);

        // 解析会籍权益
        if (StringUtils.hasText(productDetail.getMembershipBenefits())) {
            try {
                List<String> benefits = objectMapper.readValue(
                        productDetail.getMembershipBenefits(),
                        new TypeReference<List<String>>() {});
                dto.setMembershipBenefits(benefits);
            } catch (Exception e) {
                log.error("解析会籍权益失败，商品详情ID：{}", productDetail.getId(), e);
                dto.setMembershipBenefits(new ArrayList<>());
            }
        } else {
            dto.setMembershipBenefits(new ArrayList<>());
        }

        // 解析教练ID列表
        if (StringUtils.hasText(productDetail.getCoachIds())) {
            try {
                List<Long> coachIds = objectMapper.readValue(
                        productDetail.getCoachIds(),
                        new TypeReference<List<Long>>() {});
                dto.setCoachIds(coachIds);
            } catch (Exception e) {
                log.error("解析教练ID列表失败，商品详情ID：{}", productDetail.getId(), e);
                dto.setCoachIds(new ArrayList<>());
            }
        } else {
            dto.setCoachIds(new ArrayList<>());
        }

        return dto;
    }

    private ProductCategoryDTO convertToProductCategoryDTO(ProductCategory category) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        BeanUtils.copyProperties(category, dto);

        // 设置状态描述
        dto.setStatusDesc(category.getStatus() == 1 ? "启用" : "禁用");

        return dto;
    }

    private String getProductTypeDesc(Integer productType) {
        if (productType == null) return "未知";
        switch (productType) {
            case 0: return "会籍卡";
            case 1: return "私教课";
            case 2: return "团课";
            case 3: return "相关产品";
            default: return "未知";
        }
    }

    private Integer getDefaultValidityDays(Integer productType) {
        if (productType == null) return null;
        switch (productType) {
            case 0: return 30;  // 会籍卡默认30天
            case 1: return 180; // 私教课默认180天
            case 2: return 90;  // 团课默认90天
            case 3: return null; // 相关产品无有效期
            default: return null;
        }
    }

    private void saveProductDetail(Long productId, ProductDetailDTO detailDTO) {
        ProductDetail productDetail = new ProductDetail();
        BeanUtils.copyProperties(detailDTO, productDetail);
        productDetail.setProductId(productId);

        // 处理会籍权益（JSON格式）
        if (!CollectionUtils.isEmpty(detailDTO.getMembershipBenefits())) {
            try {
                productDetail.setMembershipBenefits(
                        objectMapper.writeValueAsString(detailDTO.getMembershipBenefits()));
            } catch (Exception e) {
                log.error("转换会籍权益失败", e);
                throw new BusinessException("会籍权益格式错误");
            }
        } else {
            productDetail.setMembershipBenefits("[]");
        }

        // 处理教练ID列表（JSON格式）
        if (!CollectionUtils.isEmpty(detailDTO.getCoachIds())) {
            try {
                productDetail.setCoachIds(
                        objectMapper.writeValueAsString(detailDTO.getCoachIds()));
            } catch (Exception e) {
                log.error("转换教练ID列表失败", e);
                throw new BusinessException("教练ID列表格式错误");
            }
        } else {
            productDetail.setCoachIds("[]");
        }

        // 设置默认值
        if (productDetail.getTotalSessions() == null &&
                (detailDTO.getDefaultTotalSessions() != null)) {
            productDetail.setTotalSessions(detailDTO.getDefaultTotalSessions());
            productDetail.setAvailableSessions(detailDTO.getDefaultTotalSessions());
        }

        int result = productDetailMapper.insert(productDetail);
        if (result <= 0) {
            throw new BusinessException("保存商品详情失败");
        }
    }

    private void updateProductDetail(Long productId, ProductDetailDTO detailDTO) {
        // 查询现有商品详情
        ProductDetail existingDetail = productDetailMapper.selectOne(
                new LambdaQueryWrapper<ProductDetail>()
                        .eq(ProductDetail::getProductId, productId)
        );

        if (existingDetail == null) {
            // 如果不存在，创建新的
            saveProductDetail(productId, detailDTO);
        } else {
            // 更新现有详情
            BeanUtils.copyProperties(detailDTO, existingDetail, "id", "productId");

            // 处理会籍权益
            if (!CollectionUtils.isEmpty(detailDTO.getMembershipBenefits())) {
                try {
                    existingDetail.setMembershipBenefits(
                            objectMapper.writeValueAsString(detailDTO.getMembershipBenefits()));
                } catch (Exception e) {
                    log.error("转换会籍权益失败", e);
                    throw new BusinessException("会籍权益格式错误");
                }
            }

            // 处理教练ID列表
            if (!CollectionUtils.isEmpty(detailDTO.getCoachIds())) {
                try {
                    existingDetail.setCoachIds(
                            objectMapper.writeValueAsString(detailDTO.getCoachIds()));
                } catch (Exception e) {
                    log.error("转换教练ID列表失败", e);
                    throw new BusinessException("教练ID列表格式错误");
                }
            }

            int result = productDetailMapper.updateById(existingDetail);
            if (result <= 0) {
                throw new BusinessException("更新商品详情失败");
            }
        }
    }

    private void checkProductCanBeModified(Long productId) {
        // 查询是否有订单包含此商品
        Long orderCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getProductId, productId)
        );

        if (orderCount > 0) {
            log.warn("商品ID {} 已被购买，部分字段可能不允许修改", productId);
            // 这里可以根据业务需求决定哪些字段不允许修改
        }
    }

    private void checkProductHasBeenPurchased(Long productId) {
        // 查询是否有订单包含此商品
        Long orderCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getProductId, productId)
        );

        if (orderCount > 0) {
            throw new BusinessException("商品已被购买，不能删除");
        }
    }

    private void checkProductHasActiveOrders(Long productId) {
        // 查询是否有未完成的订单包含此商品
        // 这里需要根据业务需求实现具体逻辑
        log.info("商品ID {} 将被下架，建议检查是否有未完成的订单", productId);
    }

    private List<ProductCategoryDTO> buildCategoryTree(List<ProductCategory> categories) {
        // 构建ID到分类的映射
        Map<Long, ProductCategoryDTO> categoryMap = new HashMap<>();
        List<ProductCategoryDTO> rootCategories = new ArrayList<>();

        // 第一遍：创建所有DTO
        for (ProductCategory category : categories) {
            ProductCategoryDTO dto = convertToProductCategoryDTO(category);
            categoryMap.put(category.getId(), dto);

            if (category.getParentId() == 0L) {
                rootCategories.add(dto);
            }
        }

        // 第二遍：构建树形结构
        for (ProductCategory category : categories) {
            if (category.getParentId() != 0L) {
                ProductCategoryDTO parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(categoryMap.get(category.getId()));
                }
            }
        }

        return rootCategories;
    }

    private boolean isCircularReference(Long categoryId, Long parentId) {
        // 如果父分类ID等于当前分类ID，直接返回true
        if (categoryId.equals(parentId)) {
            return true;
        }

        // 检查父分类的父分类链中是否包含当前分类
        Long currentParentId = parentId;
        while (currentParentId != null && currentParentId != 0L) {
            if (currentParentId.equals(categoryId)) {
                return true;
            }
            ProductCategory parentCategory = productCategoryMapper.selectById(currentParentId);
            if (parentCategory == null) {
                break;
            }
            currentParentId = parentCategory.getParentId();
        }

        return false;
    }
}