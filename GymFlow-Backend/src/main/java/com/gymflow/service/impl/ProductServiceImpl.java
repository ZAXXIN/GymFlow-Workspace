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
    private final OrderItemMapper orderItemMapper;
    private final ObjectMapper objectMapper;
    private final SystemConfigValidator configValidator;

    @Override
    public PageResultVO<ProductListVO> getProductList(ProductQueryDTO queryDTO) {
        log.info("查询商品列表，查询条件：{}", queryDTO);

        Page<Product> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getProductType() != null) {
            queryWrapper.eq(Product::getProductType, queryDTO.getProductType());
        }
        if (StringUtils.hasText(queryDTO.getProductName())) {
            queryWrapper.like(Product::getProductName, queryDTO.getProductName());
        }
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(Product::getStatus, queryDTO.getStatus());
        }
        if (Boolean.FALSE.equals(queryDTO.getIncludeZeroStock())) {
            queryWrapper.gt(Product::getStockQuantity, 0);
        }

        queryWrapper.orderByDesc(Product::getCreateTime);
        IPage<Product> productPage = productMapper.selectPage(page, queryWrapper);

        List<ProductListVO> voList = productPage.getRecords().stream()
                .map(this::convertToProductListVO)
                .collect(Collectors.toList());

        return new PageResultVO<>(voList, productPage.getTotal(),
                queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public List<ProductListVO> getProductsByType(Integer productType) {
        log.info("根据类型查询商品列表，商品类型：{}", productType);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductType, productType);
        queryWrapper.eq(Product::getStatus, 1);
        queryWrapper.gt(Product::getStockQuantity, 0);
        queryWrapper.orderByDesc(Product::getCreateTime);

        List<Product> products = productMapper.selectList(queryWrapper);
        return products.stream()
                .map(this::convertToProductListVO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductFullDTO getProductDetail(Long productId) {
        log.info("获取商品详情，商品ID：{}", productId);

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        return convertToProductFullDTO(product);
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

        // 根据商品类型验证必要字段
        if (productDTO.getProductType() == 0) {
            // 会籍卡：需要有效期天数
            if (productDTO.getValidityDays() == null) {
                throw new BusinessException("会籍卡需要填写有效期天数");
            }
        } else if (productDTO.getProductType() == 1 || productDTO.getProductType() == 2) {
            // 私教课/团课：需要总课时数
            if (productDTO.getTotalSessions() == null) {
                throw new BusinessException("课程类商品需要填写总课时数");
            }
        }

        // 创建商品记录
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

//        LocalDateTime now = LocalDateTime.now();
//        product.setCreateTime(now);
//        product.setUpdateTime(now);

        // 处理图片列表
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

        // 处理会籍权益（JSON格式）
        if (!CollectionUtils.isEmpty(productDTO.getMembershipBenefits())) {
            try {
                product.setMembershipBenefits(
                        objectMapper.writeValueAsString(productDTO.getMembershipBenefits()));
            } catch (Exception e) {
                log.error("转换会籍权益失败", e);
                throw new BusinessException("会籍权益格式错误");
            }
        } else {
            product.setMembershipBenefits("[]");
        }

        // 处理规格信息
        if (StringUtils.hasText(productDTO.getSpecifications())) {
            product.setSpecifications(productDTO.getSpecifications());
        } else {
            product.setSpecifications("{}");
        }

        // 设置默认值
        product.setSalesVolume(0);
        if (product.getMaxPurchaseQuantity() == null) {
            product.setMaxPurchaseQuantity(10);
        }

        int result = productMapper.insert(product);
        if (result <= 0) {
            throw new BusinessException("添加商品失败");
        }

        log.info("添加商品成功，商品ID：{}", product.getId());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Long productId, ProductBasicDTO productDTO) {
        log.info("开始更新商品，商品ID：{}", productId);

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

        // 根据商品类型验证必要字段
        if (productDTO.getProductType() == 0) {
            if (productDTO.getValidityDays() == null) {
                throw new BusinessException("会籍卡需要填写有效期天数");
            }
        }

        // 检查商品是否已被购买
        checkProductCanBeModified(productId);

        // 更新商品信息
        BeanUtils.copyProperties(productDTO, product, "id", "salesVolume");

        product.setUpdateTime(LocalDateTime.now());

        // 处理图片列表
        if (!CollectionUtils.isEmpty(productDTO.getImages())) {
            try {
                product.setImages(objectMapper.writeValueAsString(productDTO.getImages()));
            } catch (Exception e) {
                log.error("转换商品图片列表失败", e);
                throw new BusinessException("商品图片格式错误");
            }
        }

        // 处理会籍权益
        if (!CollectionUtils.isEmpty(productDTO.getMembershipBenefits())) {
            try {
                product.setMembershipBenefits(
                        objectMapper.writeValueAsString(productDTO.getMembershipBenefits()));
            } catch (Exception e) {
                log.error("转换会籍权益失败", e);
                throw new BusinessException("会籍权益格式错误");
            }
        } else {
            product.setMembershipBenefits("[]");
        }

        // 处理规格信息
        if (StringUtils.hasText(productDTO.getSpecifications())) {
            product.setSpecifications(productDTO.getSpecifications());
        }

        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("更新商品失败");
        }

        log.info("更新商品成功，商品ID：{}", productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long productId) {
        log.info("开始删除商品，商品ID：{}", productId);

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        checkProductHasBeenPurchased(productId);

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

        if (status != 0 && status != 1) {
            throw new BusinessException("状态值只能是0（下架）或1（在售）");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        product.setStatus(status);
        product.setUpdateTime(LocalDateTime.now());

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

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new BusinessException("库存不能小于0");
        }

        product.setStockQuantity(newStock);
        product.setUpdateTime(LocalDateTime.now());

        int result = productMapper.updateById(product);
        if (result <= 0) {
            throw new BusinessException("更新商品库存失败");
        }

        log.info("更新商品库存成功，商品ID：{}，新库存：{}", productId, newStock);
    }

    // ========== 私有辅助方法 ==========

    private ProductListVO convertToProductListVO(Product product) {
        ProductListVO vo = new ProductListVO();

        // 基础字段
        vo.setId(product.getId());
        vo.setProductName(product.getProductName());
        vo.setProductType(product.getProductType());
        vo.setProductTypeDesc(getProductTypeDesc(product.getProductType()));
        vo.setDescription(product.getDescription());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCurrentPrice(product.getCurrentPrice());
        vo.setStockQuantity(product.getStockQuantity());
        vo.setSalesVolume(product.getSalesVolume());
        vo.setSpecifications(product.getSpecifications());
        vo.setStatus(product.getStatus());
        vo.setStatusDesc(product.getStatus() == 1 ? "在售" : "下架");
        vo.setCreateTime(product.getCreateTime());

        // 新增字段
        vo.setValidityDays(product.getValidityDays());
        vo.setTotalSessions(product.getTotalSessions());
        vo.setMaxPurchaseQuantity(product.getMaxPurchaseQuantity());
        vo.setRefundPolicy(product.getRefundPolicy());
        vo.setUsageRules(product.getUsageRules());

        // 计算折扣
        if (product.getOriginalPrice() != null && product.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = product.getCurrentPrice()
                    .divide(product.getOriginalPrice(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(10));
            vo.setDiscount(discount);
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

        // 解析会籍权益
        if (StringUtils.hasText(product.getMembershipBenefits())) {
            try {
                List<String> benefits = objectMapper.readValue(product.getMembershipBenefits(),
                        new TypeReference<List<String>>() {});
                vo.setMembershipBenefits(benefits);
            } catch (Exception e) {
                log.error("解析会籍权益失败，商品ID：{}", product.getId(), e);
                vo.setMembershipBenefits(new ArrayList<>());
            }
        } else {
            vo.setMembershipBenefits(new ArrayList<>());
        }

        return vo;
    }

    private ProductFullDTO convertToProductFullDTO(Product product) {
        ProductFullDTO dto = new ProductFullDTO();
        BeanUtils.copyProperties(product, dto);

        dto.setProductTypeDesc(getProductTypeDesc(product.getProductType()));
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

        // 解析会籍权益
        if (StringUtils.hasText(product.getMembershipBenefits())) {
            try {
                List<String> benefits = objectMapper.readValue(product.getMembershipBenefits(),
                        new TypeReference<List<String>>() {});
                dto.setMembershipBenefits(benefits);
            } catch (Exception e) {
                log.error("解析会籍权益失败，商品ID：{}", product.getId(), e);
                dto.setMembershipBenefits(new ArrayList<>());
            }
        } else {
            dto.setMembershipBenefits(new ArrayList<>());
        }

        // 规格信息直接使用，不需要解析
        dto.setSpecifications(product.getSpecifications());

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

    private void checkProductCanBeModified(Long productId) {
        Long orderCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getProductId, productId)
        );
        if (orderCount > 0) {
            log.warn("商品ID {} 已被购买，部分字段可能不允许修改", productId);
        }
    }

    private void checkProductHasBeenPurchased(Long productId) {
        Long orderCount = orderItemMapper.selectCount(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getProductId, productId)
        );
        if (orderCount > 0) {
            throw new BusinessException("商品已被购买，不能删除");
        }
    }
}