package com.simple.springmvc.anno;

import java.lang.annotation.*;

/**
 * @author simple
 * @version 1.0
 * @date 2019-01-17 09:46
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleController {
    String value() default "";
}
