package com.hyf.task.core;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author baB_hyf
 * @date 2026/01/11
 * @deprecated 存在时序问题，可能检查的时候还没放入table
 */
@Deprecated
public class FileChecker {

    private static final Map<String, String> mapTable = new ConcurrentHashMap<>();

    public static void addPathMapper(String originPath, String currentPath) {
        mapTable.put(originPath, currentPath);
    }

    // public static void removePathMapper(String originPath, String currentPath) {
    //     mapTable.remove(originPath, currentPath);
    // }

    public static boolean exist(String path) {
        File file = new File(path);
        return exist(file);
    }

    public static boolean exist(File file) {
        if (file.exists()) {
            return true;
        }
        String mapPath = mapTable.get(file.getAbsolutePath());
        if (mapPath == null) {
            return false;
        }
        return exist(mapPath);
    }

}
