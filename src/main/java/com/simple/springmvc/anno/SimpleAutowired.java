package com.simple.springmvc.anno;

import java.lang.annotation.*;

/**
 * @author simple
 * @version 1.0
 * @date 2019-01-17 09:49
 * @since 1.0
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleAutowired {
    String value() default "";
}
