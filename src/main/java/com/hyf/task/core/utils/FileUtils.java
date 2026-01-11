package com.hyf.task.core.utils;

import com.hyf.hotrefresh.common.util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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

        // try (OutputStream os = Files.newOutputStream(tmpFile.toPath())) {
        //     // TODO 大文件情况，循环拉取文件流时获取文件下载进度
        //     IOUtils.writeTo(is, os);
        // }
        HighPerformanceFileSyncWriter.writeFile(is, tmpFile.getAbsolutePath());

        if (!tmpFile.renameTo(saveFile)) {
            throw new IOException("Failed to rename file: " + tmpFile.getAbsolutePath());
        }
    }

    public static void replaceSafely(File src, File dest, boolean deleteDestIfExists) throws IOException {
        boolean copiedExistDest = false;
        File tempFile = new File(dest.getAbsolutePath(), ".replace.tmp");
        if (dest.exists()) {
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    throw new IOException("delete exist temp dest failed, path: " + dest.getAbsolutePath());
                }
            }
            copySafely(dest, tempFile);
            if (!dest.delete()) {
                throw new IOException("delete exist dest failed, path: " + dest.getAbsolutePath());
            }
            copiedExistDest = true;
        }
        if (!src.renameTo(dest)) {
            throw new IOException("rename to dest failed, src: " + src.getAbsolutePath() + ", dest: " + dest.getAbsolutePath());
        }
        if (copiedExistDest && deleteDestIfExists) {
            if (!tempFile.delete()) {
                throw new IOException("delete exist temp dest failed, path: " + dest.getAbsolutePath());
            }
        }
    }

    public static void copySafely(File src, File dest) throws IOException {
        safelyOp(src, dest, new BiConsumer<File, File>() {
            @Override
            public void accept(File src, File dest) {
                copy(src, dest);
            }
        });
    }

    private static void safelyOp(File src, File dest, BiConsumer<File, File> op) throws IOException {
        if (dest.exists()) {
            return;
        }
        File tmpFile = getTempFile(dest.getAbsolutePath());
        if (!tmpFile.exists()) {
            if (!tmpFile.getParentFile().exists() && !tmpFile.getParentFile().mkdirs()) {
                throw new IOException("Failed to create parent directory for temp file: " + tmpFile.getAbsolutePath());
            }
            if (!tmpFile.createNewFile()) {
                throw new IOException("Failed to create temp file: " + tmpFile.getAbsolutePath());
            }
        }

        op.accept(src, tmpFile);

        if (!tmpFile.renameTo(dest)) {
            throw new IOException("Failed to rename file: " + tmpFile.getAbsolutePath());
        }
    }

    public static void moveToTrash(File... files) {
        com.sun.jna.platform.FileUtils instance = com.sun.jna.platform.FileUtils.getInstance();
        if (instance.hasTrash()) {
            try {
                instance.moveToTrash(files);
            } catch (IOException e) {
                throw new RuntimeException("Failed to move path to trash: " + Arrays.toString(files), e);
            }
        }
    }
}
