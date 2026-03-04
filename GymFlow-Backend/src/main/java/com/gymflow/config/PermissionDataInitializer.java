package com.gymflow.config;

import com.gymflow.mapper.auth.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 权限数据初始化器
 * 注意：实际权限数据已在SQL文件中配置，此类仅用于启动时检查
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionDataInitializer implements CommandLineRunner {

    private final PermissionMapper permissionMapper;

    @Override
    public void run(String... args) throws Exception {
        // 仅用于启动时检查权限数据是否存在
        checkPermissionData();
    }

    /**
     * 检查权限数据是否存在
     */
    private void checkPermissionData() {
        try {
            Long count = permissionMapper.selectCount(null);
            if (count == 0) {
                log.warn("警告：权限表(permission)为空，请确保已执行SQL初始化脚本");
            } else {
                log.info("权限数据检查完成，当前共有 {} 条权限记录", count);
            }
        } catch (Exception e) {
            log.error("检查权限数据失败", e);
        }
    }
}