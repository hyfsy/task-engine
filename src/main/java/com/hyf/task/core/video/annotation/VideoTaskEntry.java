package com.hyf.task.core.video.annotation;

import com.hyf.task.core.annotation.NeedAttribute;

import java.lang.annotation.*;

import static com.hyf.task.core.video.constants.VideoConstants.*;

/**
 * mark annotation
 *
 * @author baB_hyf
 * @date 2023/01/30
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@NeedAttribute(value = VIDEO_ID, required = false)
@NeedAttribute(value = VIDEO_NAME, required = false)
@NeedAttribute(value = VIDEO_SAVE_PATH, required = false)
@NeedAttribute(value = VIDEO_SITE_TYPE, required = false)
public @interface VideoTaskEntry {
}
