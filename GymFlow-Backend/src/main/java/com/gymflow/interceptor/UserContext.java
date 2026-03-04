package com.gymflow.interceptor;

/**
 * 用户上下文
 * 用于在当前线程中存储和获取当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> userHolder = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setCurrentUser(UserInfo userInfo) {
        userHolder.set(userInfo);
    }

    /**
     * 获取当前用户
     */
    public static UserInfo getCurrentUser() {
        return userHolder.get();
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        UserInfo userInfo = userHolder.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        UserInfo userInfo = userHolder.get();
        return userInfo != null ? userInfo.getRoleCode() : null;
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        userHolder.remove();
    }

    /**
     * 用户信息类
     */
    public static class UserInfo {
        private Long userId;
        private String username;
        private String roleCode;
        private Long roleId; // 统一为 Long 类型

        // 构造方法的 roleId 改为 Long 类型
        public UserInfo(Long userId, String username, String roleCode, Long roleId) {
            this.userId = userId;
            this.username = username;
            this.roleCode = roleCode;
            this.roleId = roleId;
        }

        // getters and setters - 全部统一为 Long 类型
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getRoleCode() { return roleCode; }
        public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

        public Long getRoleId() { return roleId; } // 改为 Long
        public void setRoleId(Long roleId) { this.roleId = roleId; } // 改为 Long
    }
}