package com.hyf.task.core;

import com.hyf.task.core.task.Task;
import com.hyf.task.core.utils.ExecuteUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class TaskPipeline {

    public static final String CURRENT_TASK = "CURRENT_TASK";

    private final List<PipelineTask<?>> pipelines = new LinkedList<>();

    public TaskPipeline add(Task<?> task) {
        return add(task.getClass().getName(), task);
    }

    public TaskPipeline add(String taskName, Task<?> task) {
        PipelineTask<?> current = new PipelineTask<>(taskName, task);
        if (pipelines.size() != 0) {
            PipelineTask<?> before = pipelines.get(pipelines.size() - 1);
            before.setNext(current);
        }
        pipelines.add(current);
        return this;
    }

    public List<PipelineTask<?>> getPipelines() {
        return pipelines;
    }

    public void triggerFirstStep(TaskContext context) {
        Task<?> firstTask = getFirstTask(context);
        doTrigger(firstTask, context);
    }

    public Task<?> getFirstTask(TaskContext context) {
        Task<?> videoTask = pipelines == null || pipelines.isEmpty() ? null : pipelines.get(0).task;
        context.putAttribute(CURRENT_TASK, videoTask);
        return videoTask;
    }

    public void triggerNextStep(TaskContext context) {
        Task<?> currentTask = context.getAttribute(CURRENT_TASK);
        if (currentTask == null) {
            return;
        }

        currentTask = currentTask.getNextStep();

        if (currentTask != null) {
            context.putAttribute(CURRENT_TASK, currentTask);
            doTrigger(currentTask, context);
        }
    }

    private void doTrigger(Task<?> task, TaskContext context) {
        if (task == null || context == null) {
            return;
        }
        ExecutorService executor = task.getExecutor();
        if (executor == null) {
            executor = ExecuteUtils.commonExecutor;
        }
        context.submit(executor, new TaskCallable<>(task, context));
    }

    public static class PipelineTask<T> {
        private String  taskName;
        private Task<T> task;

        public PipelineTask(String taskName, Task<T> task) {
            this.taskName = taskName;
            this.task = task;
        }

        public void setNext(PipelineTask<?> pipelineTask) {
            task.setNextStep(pipelineTask.task);
        }
    }
}
