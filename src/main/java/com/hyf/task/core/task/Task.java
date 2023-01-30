package com.hyf.task.core.task;

import com.hyf.task.core.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

public abstract class Task<R> {

    protected static final Logger log = LoggerFactory.getLogger(Task.class);

    private final String taskId = UUID.randomUUID().toString();

    // 任务链执行是根据任务来的，不是根据上下文来的（fork）
    private volatile Task<?> next;

    public abstract R process(TaskContext context) throws Exception;

    public ExecutorService getExecutor() {
        return null;
    }

    public Task<?> getNextStep() {
        return next;
    }

    public <T> void setNextStep(Task<T> task) {
        this.next = task;
    }

    public String getTaskId() {
        return taskId;
    }
}
