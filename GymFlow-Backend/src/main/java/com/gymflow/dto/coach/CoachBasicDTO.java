package com.gymflow.dto.coach;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "教练基础信息DTO")
public class CoachBasicDTO {

    @Schema(description = "教练ID")
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "专长领域")
    private String specialty;

    @Schema(description = "资格证书列表")
    private List<String> certificationList;

    @Schema(description = "经验年限")
    private Integer yearsOfExperience;

    @Schema(description = "时薪")
    private BigDecimal hourlyRate;

    @Schema(description = "提成比例")
    private BigDecimal commissionRate;

    @Schema(description = "个人简介")
    private String introduction;

    @Schema(description = "状态：0-离职，1-在职")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "学员总数")
    private Integer totalStudents;

    @Schema(description = "课程总数")
    private Integer totalCourses;

    @Schema(description = "总收入")
    private BigDecimal totalIncome;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}