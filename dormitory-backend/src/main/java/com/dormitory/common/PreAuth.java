package com.dormitory.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解：标注在 Controller 方法或类上，限定可访问角色。
 * 不标注则只要登录即可访问。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PreAuth {

    /**
     * 允许访问的角色，满足其一即可。空表示仅需登录。
     */
    String[] value() default {};
}
