package com.gymflow.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;


@Data
@Schema(description = "课程分类统计DTO")
public class CourseCategoryStatsDTO {

    @Schema(description = "分类名称")
    private String category;

    @Schema(description = "课程数量")
    private Integer value;

    @Schema(description = "占比")
    private BigDecimal percentage;

    @Schema(description = "图表颜色")
    private String color;
}