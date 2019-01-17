package com.simple.springmvc.anno;

import java.lang.annotation.*;

/**
 * @author simple
 * @version 1.0
 * @date 2019-01-17 09:51
 * @since 1.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleMapping {
    String value() default "";
}
