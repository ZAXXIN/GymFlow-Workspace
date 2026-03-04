package com.gymflow.dto.settings.rolePermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Schema(description = "角色权限更新DTO")
public class RolePermissionUpdateDTO {

    @NotNull(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表", required = true)
    private List<Long> permissionIds;
}