package com.hyf.task.core.video.task;

import com.hyf.task.core.utils.ExecutorUtils;
import com.hyf.task.core.video.task.VideoTask;

import java.util.concurrent.ExecutorService;

public abstract class VideoCommonTask<R> extends VideoTask<R> {

    @Override
    public ExecutorService getExecutor() {
        return ExecutorUtils.commonExecutor;
    }
}
