package com.gymflow.service.mini;

import com.gymflow.dto.mini.MiniAvailableCourseDTO;
import com.gymflow.dto.product.ProductFullDTO;

import java.util.List;

public interface MiniSearchService {

    /**
     * 搜索商品
     */
    List<ProductFullDTO> searchProducts(Long userId, String keyword, Integer categoryId, Integer pageNum, Integer pageSize);

    /**
     * 搜索可预约课程
     */
    List<MiniAvailableCourseDTO> searchCourses(Long userId, String keyword, Integer courseType, Integer pageNum, Integer pageSize);
}