package com.gymflow.service.impl.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gymflow.dto.mini.MiniAvailableCourseDTO;
import com.gymflow.dto.product.ProductFullDTO;
import com.gymflow.entity.*;
import com.gymflow.mapper.*;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiniSearchServiceImpl implements MiniSearchService {

    private final ProductMapper productMapper;
    private final CourseMapper courseMapper;
    private final CourseScheduleMapper courseScheduleMapper;
    private final CoachMapper coachMapper;

    @Override
    public List<ProductFullDTO> searchProducts(Long userId, String keyword, Integer categoryId,
                                               Integer pageNum, Integer pageSize) {
        // 创建分页对象
        Page<Product> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getStatus, 1);

        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
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

        // 先查询符合条件的课程模板
        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getStatus, 1);

        if (courseType != null) {
            courseWrapper.eq(Course::getCourseType, courseType);
        }

        if (StringUtils.hasText(keyword)) {
            courseWrapper.and(wrapper -> wrapper
                    .like(Course::getCourseName, keyword)
                    .or()
                    .like(Course::getDescription, keyword));
        }

        List<Course> courses = courseMapper.selectList(courseWrapper);
        if (courses.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());

        // 查询这些课程今天及以后的排课
        LambdaQueryWrapper<CourseSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.in(CourseSchedule::getCourseId, courseIds)
                .ge(CourseSchedule::getScheduleDate, today)
                .eq(CourseSchedule::getStatus, 1)
                .orderByAsc(CourseSchedule::getScheduleDate)
                .orderByAsc(CourseSchedule::getStartTime);

        // 分页查询排课
        Page<CourseSchedule> page = new Page<>(pageNum, pageSize);
        IPage<CourseSchedule> schedulePage = courseScheduleMapper.selectPage(page, scheduleWrapper);

        // 过滤还有名额的排课并转换为DTO
        List<MiniAvailableCourseDTO> result = new ArrayList<>();
        for (CourseSchedule schedule : schedulePage.getRecords()) {
            if (schedule.getCurrentEnrollment() < schedule.getMaxCapacity()) {
                Course course = courses.stream()
                        .filter(c -> c.getId().equals(schedule.getCourseId()))
                        .findFirst()
                        .orElse(null);

                if (course != null) {
                    MiniAvailableCourseDTO dto = convertToAvailableCourseDTO(schedule, course);
                    result.add(dto);
                }
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

        return dto;
    }

    /**
     * 转换为可预约课程DTO
     */
    private MiniAvailableCourseDTO convertToAvailableCourseDTO(CourseSchedule schedule, Course course) {
        MiniAvailableCourseDTO dto = new MiniAvailableCourseDTO();

        // 排课信息
        dto.setScheduleId(schedule.getId());
        dto.setCourseDate(schedule.getScheduleDate());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMaxCapacity(schedule.getMaxCapacity());
        dto.setCurrentEnrollment(schedule.getCurrentEnrollment());
        dto.setRemainingSlots(schedule.getMaxCapacity() - schedule.getCurrentEnrollment());

        // 课程信息
        dto.setCourseId(course.getId());
        dto.setCourseName(course.getCourseName());
        dto.setCourseType(course.getCourseType());
        dto.setCourseTypeDesc(course.getCourseType() == 0 ? "私教课" : "团课");
        dto.setDescription(course.getDescription());
        dto.setSessionCost(course.getSessionCost());

        // 获取教练信息
        Coach coach = coachMapper.selectById(schedule.getCoachId());
        if (coach != null) {
            dto.setCoachId(coach.getId());
            dto.setCoachName(coach.getRealName());
        }

        // 默认可预约
        dto.setCanBook(true);

        return dto;
    }
}