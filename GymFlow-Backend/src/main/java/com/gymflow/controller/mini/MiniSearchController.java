package com.gymflow.controller.mini;

import com.gymflow.common.Result;
import com.gymflow.dto.mini.MiniAvailableCourseDTO;
import com.gymflow.dto.product.ProductFullDTO;
import com.gymflow.service.mini.MiniSearchService;
import com.gymflow.utils.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mini/search")
@Tag(name = "小程序-搜索", description = "搜索相关接口")
@Validated
@RequiredArgsConstructor
public class MiniSearchController {

    private final MiniSearchService miniSearchService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 从token获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    @GetMapping("/products")
    @Operation(summary = "搜索商品")
    public Result<List<ProductFullDTO>> searchProducts(
            HttpServletRequest request,
            @RequestParam String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getCurrentUserId(request);
        List<ProductFullDTO> products = miniSearchService.searchProducts(userId, keyword, categoryId, pageNum, pageSize);
        return Result.success("搜索成功", products);
    }

    @GetMapping("/courses")
    @Operation(summary = "搜索可预约课程")
    public Result<List<MiniAvailableCourseDTO>> searchCourses(
            HttpServletRequest request,
            @RequestParam String keyword,
            @RequestParam(required = false) Integer courseType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long userId = getCurrentUserId(request);
        List<MiniAvailableCourseDTO> courses = miniSearchService.searchCourses(userId, keyword, courseType, pageNum, pageSize);
        return Result.success("搜索成功", courses);
    }
}