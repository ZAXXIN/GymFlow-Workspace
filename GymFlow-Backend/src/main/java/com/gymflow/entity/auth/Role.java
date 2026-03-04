package com.gymflow.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("role_name")
    private String roleName;

    @TableField("role_code")
    private String roleCode;  // 角色编码：BOSS, RECEPTIONIST

    private String description;

    private Integer status;  // 状态：0-禁用，1-启用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}