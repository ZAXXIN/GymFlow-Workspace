package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("coach")
public class Coach {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("real_name")
    private String realName;

    private String phone;

    private String password;

    // 0-女，1-男
    private Integer gender;

    private String specialty;

    private String certifications;

    @TableField("years_of_experience")
    private Integer yearsOfExperience;

    private Integer status;

    private BigDecimal rating;

    private String introduction;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}