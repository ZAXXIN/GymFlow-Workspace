package com.gymflow.common.annotation;

import java.lang.annotation.*;

/**
 * 权限验证注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreAuthorize {
    /**
     * 权限编码，多个权限用逗号分隔
     */
    String value();

    /**
     * 逻辑关系：AND-需要所有权限，OR-满足一个即可
     */
    Logical logical() default Logical.OR;

    enum Logical {
        AND, OR
    }
}