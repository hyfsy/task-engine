package com.hyf.task.core.utils;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;

/**
 * @author baB_hyf
 * @date 2024/06/16
 */
public interface RequestCustomizer {
    HttpUriRequestBase customize(HttpUriRequestBase request);
}
