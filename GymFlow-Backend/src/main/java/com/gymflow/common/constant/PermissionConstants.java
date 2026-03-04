package com.gymflow.common.constant;

/**
 * 权限编码常量
 */
public interface PermissionConstants {

    // ========== 会员管理 ==========
    String MEMBER_VIEW = "member:view";
    String MEMBER_DETAIL = "member:detail";
    String MEMBER_ADD = "member:add";
    String MEMBER_EDIT = "member:edit";
    String MEMBER_DELETE = "member:delete";
    String MEMBER_BATCH_DELETE = "member:batch:delete";
    String MEMBER_CARD_RENEW = "member:card:renew";
    String MEMBER_HEALTH_VIEW = "member:health:view";
    String MEMBER_HEALTH_ADD = "member:health:add";

    // ========== 教练管理 ==========
    String COACH_VIEW = "coach:view";
    String COACH_DETAIL = "coach:detail";
    String COACH_ADD = "coach:add";
    String COACH_EDIT = "coach:edit";
    String COACH_DELETE = "coach:delete";
    String COACH_BATCH_DELETE = "coach:batch:delete";
    String COACH_SCHEDULE_VIEW = "coach:schedule:view";
    String COACH_SCHEDULE_SET = "coach:schedule:set";

    // ========== 课程管理 ==========
    String COURSE_VIEW = "course:view";
    String COURSE_DETAIL = "course:detail";
    String COURSE_ADD = "course:add";
    String COURSE_EDIT = "course:edit";
    String COURSE_DELETE = "course:delete";
    String COURSE_SCHEDULE_VIEW = "course:schedule:view";
    String COURSE_SCHEDULE_SET = "course:schedule:set";
    String COURSE_BOOKING_ADD = "course:booking:add";
    String COURSE_BOOKING_CANCEL = "course:booking:cancel";

    // ========== 签到管理 ==========
    String CHECKIN_VIEW = "checkIn:view";
    String CHECKIN_DETAIL = "checkIn:detail";
    String CHECKIN_MEMBER_ADD = "checkIn:member:add";
    String CHECKIN_COURSE_ADD = "checkIn:course:add";
    String CHECKIN_EDIT = "checkIn:edit";
    String CHECKIN_DELETE = "checkIn:delete";
    String CHECKIN_VERIFY = "checkIn:verify";

    // ========== 订单管理 ==========
    String ORDER_VIEW = "order:view";
    String ORDER_DETAIL = "order:detail";
    String ORDER_ADD = "order:add";
    String ORDER_EDIT = "order:edit";
    String ORDER_CANCEL = "order:cancel";
    String ORDER_DELETE = "order:delete";
    String ORDER_PAY = "order:pay";
    String ORDER_REFUND = "order:refund";

    // ========== 商品管理 ==========
    String PRODUCT_VIEW = "product:view";
    String PRODUCT_DETAIL = "product:detail";
    String PRODUCT_ADD = "product:add";
    String PRODUCT_EDIT = "product:edit";
    String PRODUCT_DELETE = "product:delete";
    String PRODUCT_STATUS = "product:status";
    String PRODUCT_STOCK = "product:stock";
    String PRODUCT_CATEGORY_VIEW = "product:category:view";
    String PRODUCT_CATEGORY_MANAGE = "product:category:manage";

    // ========== 系统设置 ==========
    String SETTINGS_USER_VIEW = "settings:user:view";
    String SETTINGS_USER_ADD = "settings:user:add";
    String SETTINGS_USER_EDIT = "settings:user:edit";
    String SETTINGS_USER_DELETE = "settings:user:delete";
    String SETTINGS_USER_STATUS = "settings:user:status";
    String SETTINGS_USER_RESETPWD = "settings:user:resetpwd";
    String SETTINGS_CONFIG_VIEW = "settings:config:view";
    String SETTINGS_CONFIG_EDIT = "settings:config:edit";

    // ========== 角色权限管理 ==========
    String SETTINGS_ROLE_VIEW = "settings:role:view";
    String SETTINGS_ROLE_ADD = "settings:role:add";
    String SETTINGS_ROLE_EDIT = "settings:role:edit";
    String SETTINGS_ROLE_DELETE = "settings:role:delete";

    // ========== 通用 ==========
    String COMMON_UPLOAD = "common:upload";
}