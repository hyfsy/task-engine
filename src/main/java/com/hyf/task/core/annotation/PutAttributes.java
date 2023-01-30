package com.hyf.task.core.annotation;

import java.lang.annotation.*;

/**
 * @author baB_hyf
 * @date 2023/01/28
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PutAttributes {
    PutAttribute[] value();
}
