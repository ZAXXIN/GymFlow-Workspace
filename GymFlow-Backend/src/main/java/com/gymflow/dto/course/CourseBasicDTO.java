package com.gymflow.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.List;

@Data
@Schema(description = "课程基础信息DTO")
public class CourseBasicDTO {

    @Schema(description = "课程ID（编辑时必填）")
    private Long id;

    @NotNull(message = "课程类型不能为空")
    @Schema(description = "课程类型：0-私教课，1-团课", required = true)
    private Integer courseType;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 100, message = "课程名称长度不能超过100")
    @Schema(description = "课程名称", required = true)
    private String courseName;

    @Size(max = 500, message = "课程描述长度不能超过500")
    @Schema(description = "课程描述")
    private String description;

    @NotNull(message = "绑定教练不能为空")
    @Schema(description = "绑定教练ID列表", required = true)
    private List<Long> coachIds;

    @NotNull(message = "课时长不能为空")
    @Min(value = 1, message = "课时长不能小于1分钟")
    @Schema(description = "课时长（分钟）", required = true)
    private Integer duration;

    @NotNull(message = "课时消耗不能为空")
    @Min(value = 1, message = "课时消耗不能小于1")
    @Schema(description = "预约消耗课时数", required = true)
    private Integer sessionCost;

    @Size(max = 1000, message = "课程须知长度不能超过1000")
    @Schema(description = "课程须知")
    private String notice;
}