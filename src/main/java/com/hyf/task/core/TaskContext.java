package com.hyf.task.core;

import com.hyf.task.core.constants.TaskConstants;
import com.hyf.task.core.exception.TaskFailureHandler;
import com.hyf.task.core.task.Task;
import com.hyf.task.core.utils.ExecutorUtils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskContext {

    private final    Map<String, Object> attributes;
    private          TaskEngine          engine;
    private          TaskPipeline        pipeline;
    private          TaskFailureHandler  failureHandler;
    private volatile boolean             needTriggerNextStep = false;

    public TaskContext(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public <T> void submit(Runnable runnable) {
        submit(ExecutorUtils.commonExecutor, Executors.callable(runnable));
    }

    public <T> void submit(Callable<T> callable) {
        submit(ExecutorUtils.commonExecutor, callable);
    }

    public <T> void submit(ExecutorService executorService, Callable<T> callable) {
        if (executorService == null) {
            executorService = ExecutorUtils.commonExecutor;
        }
        executorService.submit(callable);
    }

    public <T> T getAttribute(String key, T defaultValue) {
        T value = getAttribute(key);
        return value == null ? defaultValue : value;
    }

    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public <T> T removeAttribute(String key) {
        return (T) attributes.remove(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public <T> T getPreviousResult() {
        return getAttribute(TaskConstants.TASK_PREVIOUS_PROCESS_RESULT);
    }

    public TaskContext copy() {
        Map<String, Object> attributes = new ConcurrentHashMap<>(this.attributes);
        TaskContext context = new TaskContext(attributes);
        context.setEngine(engine);
        context.setPipeline(pipeline);
        return context;
    }

    public <T> void fork(Task<T> task) {
        TaskContext copy = copy();
        copy.putAttribute(TaskPipeline.CURRENT_TASK, task);
        submit(task.getExecutor(), new TaskCallable<>(task, copy));
    }

    public void triggerNextStep() {
        needTriggerNextStep = true;
    }

    boolean isNeedTriggerNextStep() {
        return needTriggerNextStep;
    }

    void resetNeedTriggerNextStep() {
        this.needTriggerNextStep = false;
    }

    public /* 异步控制触发需要public */ void fireTriggerNextStep() {
        getPipeline().triggerNextStep(this);
    }

    TaskEngine getEngine() {
        return engine;
    }

    void setEngine(TaskEngine engine) {
        this.engine = engine;
    }

    TaskPipeline getPipeline() {
        return pipeline;
    }

    void setPipeline(TaskPipeline pipeline) {
        this.pipeline = pipeline;
    }

    TaskFailureHandler getFailureHandler() {
        return failureHandler;
    }

    void setFailureHandler(TaskFailureHandler taskFailureHandler) {
        this.failureHandler = taskFailureHandler;
    }
}
