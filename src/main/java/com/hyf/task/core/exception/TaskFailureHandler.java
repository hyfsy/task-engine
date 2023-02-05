package com.hyf.task.core.exception;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.task.Task;

/**
 * @author baB_hyf
 * @date 2023/01/30
 */
public interface TaskFailureHandler {

    <T> T handle(Task<T> task, TaskContext taskContext, Throwable t);

}
