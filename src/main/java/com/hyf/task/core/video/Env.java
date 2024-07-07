package com.hyf.task.core.video;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.utils.ExecutorUtils;
import com.hyf.task.core.video.task.DownloadHtmlTask;

import static com.hyf.task.core.video.constants.VideoConstants.VIDEO_DOWNLOAD_PATH_PROPERTY_KEY;

/**
 * @author baB_hyf
 * @date 2023/01/28
 */
public class Env {

    public static void setVideoDownloadPath(String path) {
        System.setProperty(VIDEO_DOWNLOAD_PATH_PROPERTY_KEY, path);
    }

    public static void setComputerShutdownWhenFinished() {
        ExecutorUtils._computer_shutdown_when_finished = true;
    }

    // 针对于二次下载的情况，需要disable，因为很多任务都是已经完成的
    public static void disableLimit() {
        TaskContext._limit_enabled = false;
        DownloadHtmlTask.limit_enabled = false;
    }

    // default 1000
    public static void setLimitRandomMillis(int limitRandomMillis) {
        TaskContext._limit_random_ms = limitRandomMillis;
    }
}
