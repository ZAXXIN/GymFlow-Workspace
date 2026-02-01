package com.gymflow.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "课程基础信息DTO")
public class CourseBasicDTO {

    @NotNull(message = "课程类型不能为空")
    @Schema(description = "课程类型：0-私教课，1-团课", example = "1", required = true)
    private Integer courseType;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100")
    @Schema(description = "课程名称", example = "瑜伽入门", required = true)
    private String courseName;

    @Size(max = 500, message = "课程描述长度不能超过500")
    @Schema(description = "课程描述", example = "适合初学者的基础瑜伽课程")
    private String description;

    @NotNull(message = "绑定教练不能为空")
    @Schema(description = "绑定教练ID列表", required = true)
    private List<Long> coachIds;

    @NotNull(message = "最大容量不能为空")
    @Min(value = 1, message = "最大容量不能小于1")
    @Schema(description = "最大容量", example = "20", required = true)
    private Integer maxCapacity;

    @NotNull(message = "课时长不能为空")
    @Min(value = 1, message = "课时长不能小于1分钟")
    @Schema(description = "课时长（分钟）", example = "60", required = true)
    private Integer duration;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.00", message = "价格不能小于0")
    @Schema(description = "价格（元）", example = "80.00", required = true)
    private BigDecimal price;

    @Size(max = 200, message = "上课地点长度不能超过200")
    @Schema(description = "上课地点", example = "团课室A")
    private String location;

    @Size(max = 1000, message = "课程须知长度不能超过1000")
    @Schema(description = "课程须知")
    private String notice;
}