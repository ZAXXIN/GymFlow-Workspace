package com.gymflow.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("permission")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("permission_name")
    private String permissionName;

    @TableField("permission_code")
    private String permissionCode;

    @TableField("parent_id")
    private Long parentId;

    private String module;

    private Integer type;  // 1-菜单，2-页面，3-按钮

    private String path;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private List<Permission> children;
}