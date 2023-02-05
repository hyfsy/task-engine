package com.hyf.task.core;

import com.hyf.task.core.constants.TaskConstants;
import com.hyf.task.core.exception.TaskFailureHandler;
import com.hyf.task.core.task.Task;

import java.util.concurrent.Callable;

public class TaskCallable<T> implements Callable<T> {
    private final Task<T>     task;
    private final TaskContext context;

    public TaskCallable(Task<T> task, TaskContext context) {
        this.task = task;
        this.context = context;
    }

    @Override
    public T call() throws Exception {
        try {

            T result = task.process(context);

            if (result == null) {
                context.removeAttribute(TaskConstants.TASK_PREVIOUS_PROCESS_RESULT);
            }
            else {
                context.putAttribute(TaskConstants.TASK_PREVIOUS_PROCESS_RESULT, result);
            }

            if (context.isNeedTriggerNextStep()) {
                context.resetNeedTriggerNextStep();
                context.fireTriggerNextStep();
            }
            return result;
        } catch (Throwable t) {
            TaskFailureHandler failureHandler = context.getFailureHandler();
            if (failureHandler != null) {
                return failureHandler.handle(task, context, t);
            }
            throw t; // 抛给线程池
        }
    }
}
