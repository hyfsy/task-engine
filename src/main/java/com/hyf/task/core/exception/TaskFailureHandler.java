package com.hyf.task.core.exception;

import com.hyf.task.core.task.Task;
import com.hyf.task.core.TaskContext;

/**
 * @author baB_hyf
 * @date 2023/01/30
 */
public interface TaskFailureHandler {

    <T> T handle(Task<T> videoTask, TaskContext taskContext, Throwable t);

}
