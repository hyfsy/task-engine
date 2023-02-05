package com.hyf.task.core.video.task;

import com.hyf.task.core.utils.ExecutorUtils;

import java.util.concurrent.ExecutorService;

public abstract class VideoDownloadTask<R> extends VideoTask<R> {

    @Override
    public ExecutorService getExecutor() {
        return ExecutorUtils.ioExecutor;
    }
}
