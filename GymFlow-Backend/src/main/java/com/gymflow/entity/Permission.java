package com.gymflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("permission")
public class Permission {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("permission_name")
    private String permissionName;

    @TableField("parent_id")
    private Long parentId;

    private String path;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
