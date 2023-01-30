package com.hyf.task.core;


import com.hyf.task.core.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FileCache {

    public interface CacheOperation<R> {
        InputStream getSourceInputStream() throws IOException;
        R doOperation(InputStream is) throws IOException;
    }

    public static <R> R doWithCache(String cacheRelativePath, CacheOperation<R> operation) throws IOException {
        File cacheFile = new File(getCacheHome(), cacheRelativePath);

        if (!cacheFile.exists()) {
            try (InputStream is = operation.getSourceInputStream()) {
                FileUtils.writeFileSafely(is, cacheFile.getAbsolutePath());
            }
        }

        try (InputStream fis = Files.newInputStream(cacheFile.toPath())) {
            return operation.doOperation(fis);
        }
    }

    public static File getCache(String cacheRelativePath) {
        return new File(getCacheHome(), cacheRelativePath);
    }

    public static void clearCache(String cacheRelativePath) {
        File file = getCache(cacheRelativePath);
        FileUtils.delete(file);
    }

    public static void clearCache() {
        File file = getCacheHome();
        FileUtils.delete(file);
    }

    public static File getCacheHome() {
        File file = new File(System.getProperty("user.home"), ".video-cache");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
