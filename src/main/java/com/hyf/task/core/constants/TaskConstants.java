package com.hyf.task.core.constants;

/**
 * @author baB_hyf
 * @date 2023/02/04
 */
public interface TaskConstants {

    /**
     * 任务执行失败时的重试次数
     */
    String TASK_FAILED_RETRY_TIME_KEY = "TASK_FAILED_RETRY_TIME_KEY";
    int    TASK_FAILED_RETRY_TIME     = 3;

    /**
     * 代表前一个任务的返回值
     */
    String TASK_PREVIOUS_PROCESS_RESULT = "TASK_PREVIOUS_PROCESS_RESULT";

}
