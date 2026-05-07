package com.gymflow.common.constant;

/**
 * 权限编码常量
 */
public interface PermissionConstants {

    // ========== 仪表盘 ==========
    String DASHBOARD_VIEW = "dashboard:view";

    // ========== 会员管理 ==========
    String MEMBER_MENU = "member:menu";
    String MEMBER_DETAIL = "member:detail";
    String MEMBER_ADD = "member:add";
    String MEMBER_EDIT = "member:edit";
    String MEMBER_DELETE = "member:delete";

    // ========== 教练管理 ==========
    String COACH_MENU = "coach:menu";
    String COACH_DETAIL = "coach:detail";
    String COACH_ADD = "coach:add";
    String COACH_EDIT = "coach:edit";
    String COACH_DELETE = "coach:delete";

    // ========== 课程管理 ==========
    String COURSE_MENU = "course:menu";
    String COURSE_DETAIL = "course:detail";
    String COURSE_SCHEDULE_VIEW = "course:schedule:view";
    String COURSE_ADD = "course:add";
    String COURSE_EDIT = "course:edit";
    String COURSE_DELETE = "course:delete";

    // ========== 签到管理 ==========
    String CHECKIN_MENU = "checkIn:menu";
    String CHECKIN_DETAIL = "checkIn:detail";

    // ========== 商城管理 ==========
    String PRODUCT_MENU = "product:menu";
    String PRODUCT_DETAIL = "product:detail";
    String PRODUCT_ADD = "product:add";
    String PRODUCT_EDIT = "product:edit";
    String PRODUCT_STATUS = "product:status";
    String PRODUCT_DELETE = "product:delete";

    // ========== 订单管理 ==========
    String ORDER_MENU = "order:menu";
    String ORDER_DETAIL = "order:detail";

    // ========== 系统设置 ==========
    String SETTINGS_MENU = "settings:menu";
    String SETTINGS_USER_VIEW = "settings:user:view";
    String SETTINGS_CONFIG_VIEW = "settings:config:view";
    String SETTINGS_ROLE_VIEW = "settings:role:view";
}