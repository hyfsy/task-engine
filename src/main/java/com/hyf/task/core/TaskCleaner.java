package com.hyf.task.core;

import com.hyf.task.core.utils.FileUtils;

import java.io.File;

import static com.hyf.task.core.video.constants.VideoConstants.DEFAULT_VIDEO_SAVE_PATH;

/**
 * @author baB_hyf
 * @date 2023/01/15
 */
public class TaskCleaner {

    public static void main(String[] args) {
        clearAll();
    }

    public static void clearAll() {
        clearAll(null);
    }

    public static void clearAll(String path) {
        clearCache();
        clearVideo(path);
    }

    public static void clearCache() {
        FileCache.clearCache();
    }

    public static void clearVideo() {
        clearVideo(null);
    }

    public static void clearVideo(String path) {
        if (path == null) {
            path = DEFAULT_VIDEO_SAVE_PATH;
        }
        File file = new File(path);
        FileUtils.delete(file);
    }
}
