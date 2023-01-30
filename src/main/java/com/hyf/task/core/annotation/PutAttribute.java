package com.hyf.task.core.annotation;

import java.lang.annotation.*;

/**
 * @author baB_hyf
 * @date 2023/01/28
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PutAttributes.class)
public @interface PutAttribute {
    String value();

    boolean required() default true;

    String desc() default "";
}
