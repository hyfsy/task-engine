package com.hyf.task.core.video;

import com.hyf.task.core.utils.ExecutorUtils;

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
}
