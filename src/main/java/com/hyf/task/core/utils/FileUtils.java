package com.hyf.task.core.utils;

import com.hyf.hotrefresh.common.util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileUtils extends com.hyf.hotrefresh.common.util.FileUtils {

    public static final String TMP_SUFFIX = ".tmp";

    public static File getTempFile(String filePath) {
        return new File(filePath + TMP_SUFFIX);
    }

    public static void writeFileSafely(String content, String fileSavePath) throws IOException {
        writeFileSafely(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), fileSavePath);
    }

    /**
     * 防止下载文件损坏
     */
    public static void writeFileSafely(InputStream is, String fileSavePath) throws IOException {
        File saveFile = new File(fileSavePath);
        if (saveFile.exists()) {
            return;
        }
        File tmpFile = getTempFile(fileSavePath);
        if (!tmpFile.exists()) {
            if (!tmpFile.getParentFile().exists() && !tmpFile.getParentFile().mkdirs()) {
                throw new IOException("Failed to create parent directory for temp file: " + tmpFile.getAbsolutePath());
            }
            if (!tmpFile.createNewFile()) {
                throw new IOException("Failed to create temp file: " + tmpFile.getAbsolutePath());
            }
        }

        try (OutputStream os = Files.newOutputStream(tmpFile.toPath())) {
            // TODO 大文件情况，循环拉取文件流时获取文件下载进度
            IOUtils.writeTo(is, os);
        }

        if (!tmpFile.renameTo(saveFile)) {
            throw new IOException("Failed to rename file: " + tmpFile.getAbsolutePath());
        }
    }
}
