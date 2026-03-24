package com.gymflow.dto.course;

import com.gymflow.dto.coach.CoachBasicDTO;
import com.gymflow.vo.CourseScheduleVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "课程完整信息DTO")
public class CourseFullDTO {

    @Schema(description = "课程ID")
    private Long id;

    @Schema(description = "课程类型：0-私教课，1-团课")
    private Integer courseType;

    @Schema(description = "课程类型描述")
    private String courseTypeDesc;

    @Schema(description = "课程名称")
    private String courseName;

    @Schema(description = "课程须知")
    private String notice;

    @Schema(description = "课程描述")
    private String description;

    @Schema(description = "绑定教练信息列表")
    private List<CoachBasicDTO> coaches;

    @Schema(description = "课时长（分钟）")
    private Integer duration;

    @Schema(description = "预约消耗课时数")
    private Integer sessionCost;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "排课列表（包含预约信息）")
    private List<CourseScheduleVO> schedules;
}