package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user")
@Schema(description = "用户实体")
public class User {

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "手机号")
    private String phone;

    @TableField("real_name")
    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "性别：0-女，1-男")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "部门")
    private String department;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "角色：0-老板，1-前台，2-教练，3-会员")
    private Integer role;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}