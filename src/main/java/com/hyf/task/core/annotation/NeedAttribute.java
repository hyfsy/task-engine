package com.hyf.task.core.annotation;

import java.lang.annotation.*;

/**
 * @author baB_hyf
 * @date 2023/01/28
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(NeedAttributes.class)
public @interface NeedAttribute {
    String value();

    boolean required() default true;

    String desc() default "";
}
