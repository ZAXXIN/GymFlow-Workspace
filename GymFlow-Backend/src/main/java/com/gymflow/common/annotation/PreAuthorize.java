package com.gymflow.common.annotation;

import java.lang.annotation.*;

/**
 * 权限注解 - 只用于PC端
 * 小程序端不需要此注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreAuthorize {

    /**
     * 权限编码，多个用逗号分隔
     */
    String value();

    /**
     * 逻辑关系：AND-所有权限都必须有，OR-有一个即可
     */
    String logical() default "OR";
}