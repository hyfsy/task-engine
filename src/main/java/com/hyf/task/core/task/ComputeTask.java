package com.hyf.task.core.task;

import com.hyf.task.core.utils.ExecuteUtils;

import java.util.concurrent.ExecutorService;

public abstract class ComputeTask<R> extends Task<R> {

    @Override
    public ExecutorService getExecutor() {
        return ExecuteUtils.cpuExecutor;
    }
}
