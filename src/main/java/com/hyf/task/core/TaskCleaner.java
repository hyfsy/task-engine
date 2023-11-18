package com.hyf.task.core;

import com.hyf.task.core.utils.FileUtils;

import java.io.File;

/**
 * @author baB_hyf
 * @date 2023/01/15
 */
public class TaskCleaner {

    public static void main(String[] args) {
        cleanCache();
        // System.out.println(FileCache.getCacheHome());
    }

    public static void cleanCache() {
        FileCache.clearCache();
    }

    public static void cleanProduct(String path) {
        if (path == null) {
            return;
        }
        File file = new File(path);
        // 防止误点后悔
        FileUtils.moveToTrash(file);
    }
}
