package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.mini.MiniAvailableCourseDTO;
import com.gymflow.dto.product.ProductFullDTO;
import com.gymflow.entity.Course;
import com.gymflow.entity.Product;
import com.gymflow.entity.Coach;
import com.gymflow.mapper.CourseMapper;
import com.gymflow.mapper.ProductMapper;
import com.gymflow.mapper.CoachMapper;
import com.gymflow.service.mini.MiniSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniSearchServiceImpl implements MiniSearchService {

    private final ProductMapper productMapper;
    private final CourseMapper courseMapper;
    private final CoachMapper coachMapper;

    @Override
    public List<ProductFullDTO> searchProducts(Long userId, String keyword, Integer categoryId,
                                               Integer pageNum, Integer pageSize) {
        // 创建分页对象
        Page<Product> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1); // 只搜索在售商品

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
        }

        if (categoryId != null) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Product::getCreateTime);

        // 执行分页查询
        IPage<Product> productPage = productMapper.selectPage(page, queryWrapper);

        // 转换为DTO
        List<ProductFullDTO> result = new ArrayList<>();
        for (Product product : productPage.getRecords()) {
            result.add(convertToProductFullDTO(product));
        }

        return result;
    }

    @Override
    public List<MiniAvailableCourseDTO> searchCourses(Long userId, String keyword, Integer courseType,
                                                      Integer pageNum, Integer pageSize) {
        LocalDate today = LocalDate.now();

        // 创建分页对象
        Page<Course> page = new Page<>(pageNum, pageSize);

        // 构建查询条件 - 先查询状态正常且日期在今天的课程
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getStatus, 1) // 正常状态
                .ge(Course::getCourseDate, today); // 今天及以后的课程

        if (courseType != null) {
            queryWrapper.eq(Course::getCourseType, courseType);
        }

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Course::getCourseName, keyword)
                    .or()
                    .like(Course::getDescription, keyword));
        }

        // 按课程日期和开始时间排序
        queryWrapper.orderByAsc(Course::getCourseDate)
                .orderByAsc(Course::getStartTime);

        // 执行分页查询
        IPage<Course> coursePage = courseMapper.selectPage(page, queryWrapper);

        // 过滤还有名额的课程并转换为DTO
        List<MiniAvailableCourseDTO> result = new ArrayList<>();
        for (Course course : coursePage.getRecords()) {
            // 在内存中过滤还有名额的课程
            if (course.getCurrentEnrollment() < course.getMaxCapacity()) {
                MiniAvailableCourseDTO dto = convertToAvailableCourseDTO(course);
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * 转换为商品完整DTO
     */
    private ProductFullDTO convertToProductFullDTO(Product product) {
        ProductFullDTO dto = new ProductFullDTO();
        BeanUtils.copyProperties(product, dto);

        // 设置类型描述
        if (product.getProductType() != null) {
            switch (product.getProductType()) {
                case 0:
                    dto.setProductTypeDesc("会籍卡");
                    break;
                case 1:
                    dto.setProductTypeDesc("私教课");
                    break;
                case 2:
                    dto.setProductTypeDesc("团课");
                    break;
                case 3:
                    dto.setProductTypeDesc("相关产品");
                    break;
                default:
                    dto.setProductTypeDesc("未知");
            }
        }

        // 设置状态描述
        dto.setStatusDesc(product.getStatus() == 1 ? "在售" : "下架");

        // 注意：ProductFullDTO 没有 discount 字段，所以不设置折扣

        return dto;
    }

    /**
     * 转换为可预约课程DTO
     */
    private MiniAvailableCourseDTO convertToAvailableCourseDTO(Course course) {
        MiniAvailableCourseDTO dto = new MiniAvailableCourseDTO();
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        dto.setCourseTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");
        dto.setCourseDate(course.getCourseDate());
        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        dto.setPrice(course.getPrice());
        dto.setOriginalPrice(course.getPrice());
        dto.setDiscount(BigDecimal.valueOf(10)); // MiniAvailableCourseDTO 有 discount 字段
        dto.setMaxCapacity(course.getMaxCapacity());
        dto.setCurrentEnrollment(course.getCurrentEnrollment());
        dto.setRemainingSlots(course.getMaxCapacity() - course.getCurrentEnrollment());
        dto.setLocation(course.getLocation());
        dto.setDescription(course.getDescription());

        // 获取教练信息
        if (course.getCoachId() != null) {
            Coach coach = coachMapper.selectById(course.getCoachId());
            if (coach != null) {
                dto.setCoachId(coach.getId());
                dto.setCoachName(coach.getRealName());
            }
        }

        // 默认可预约
        dto.setCanBook(true);

        return dto;
    }
}