package com.gymflow.dto.settings.rolePermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Schema(description = "角色DTO")
public class RoleDTO {

    @Schema(description = "角色ID")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    @Schema(description = "角色名称", example = "管理员", required = true)
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    @Pattern(regexp = "^[A-Z_]+$", message = "角色编码只能包含大写字母和下划线")
    @Size(max = 50, message = "角色编码长度不能超过50")
    @Schema(description = "角色编码", example = "ADMIN", required = true)
    private String roleCode;

    @Size(max = 200, message = "角色描述长度不能超过200")
    @Schema(description = "角色描述", example = "系统管理员，拥有所有权限")
    private String description;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态：0-禁用，1-启用", example = "1", required = true)
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}