package com.gymflow.interceptor;

/**
 * 用户上下文，用于存储当前登录用户信息
 * 只用于PC端请求，小程序端请求不设置用户上下文
 */
public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    private static final ThreadLocal<Integer> roleHolder = new ThreadLocal<>();

    /**
     * 设置当前用户
     */
    public static void setCurrentUser(Long userId, String username, Integer role) {
        userIdHolder.set(userId);
        usernameHolder.set(username);
        roleHolder.set(role);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return userIdHolder.get();
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        return usernameHolder.get();
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getCurrentRole() {
        return roleHolder.get();
    }

    /**
     * 判断是否是PC端请求（有用户上下文）
     */
    public static boolean isPcRequest() {
        return userIdHolder.get() != null;
    }

    /**
     * 清除当前用户
     */
    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        roleHolder.remove();
    }
}