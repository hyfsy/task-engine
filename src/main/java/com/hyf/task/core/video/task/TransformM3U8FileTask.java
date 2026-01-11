package com.hyf.task.core.video.task;

import com.hyf.task.core.FileCache;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.utils.FileUtils;
import com.hyf.task.core.utils.IOUtils;
import com.hyf.task.core.utils.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hyf.task.core.video.constants.M3U8Constants.*;

/**
 * 将m3u8文件重新转换为本地可使用的文件（例如ts文件路径等）并迁移到指定目录下
 */
@NeedAttribute(CACHE_IDENTITY_DOWNLOAD_M3U8_FILE)
public class TransformM3U8FileTask extends VideoCommonTask<Void> {

    @Override
    public Void process(TaskContext context) throws Exception {

        try {
            String downloadResourcePath = DownloadResourceTask.getDownloadResourcePath(context);
            String m3u8FileCacheIdentity = context.getAttribute(CACHE_IDENTITY_DOWNLOAD_M3U8_FILE);
            if (StringUtils.isBlank(m3u8FileCacheIdentity)) {
                throw new IllegalStateException("CACHE_IDENTITY_DOWNLOAD_M3U8_FILE must has text");
            }

            File cacheFile = FileCache.getCache(m3u8FileCacheIdentity);

            String m3u8FileContent = "";
            try (FileInputStream fis = new FileInputStream(cacheFile)) {
                m3u8FileContent = IOUtils.readAsString(fis);
            }

            String cleanedM3u8FileContent = Arrays.stream(m3u8FileContent.split("\\n")).map(row -> {
                // get key
                if (row.startsWith(SECRET_KEY_PREFIX) && !row.startsWith(SECRET_KEY_NONE)) {

                    Pattern secretKeyUrlPattern = context.getAttribute(M3U8_FILE_SECRET_KEY_URL_PATTERN) == null ?
                            SECRET_KEY_URL_PATTERN : context.getAttribute(M3U8_FILE_SECRET_KEY_URL_PATTERN);

                    // object script
                    Matcher matcher = secretKeyUrlPattern.matcher(row);

                    // clean key url path transform to local relative path
                    if (matcher.find()) {
                        String keyUrl = matcher.group(1);
                        row = row.replace(keyUrl, keyUrl.substring(keyUrl.lastIndexOf("/") + 1));
                        return row;
                    }
                }
                if (!row.startsWith(M3U8_FILE_COMMENT)) {
                    row = row.substring(row.lastIndexOf("/") + 1);
                    if (row.indexOf('?') != -1) {
                        row = row.substring(0, row.indexOf('?'));
                    }
                    if (!row.endsWith(".ts") && !row.endsWith(".m3u8") && !row.endsWith(".key")) {
                        int i = row.lastIndexOf(".");
                        if (i == -1) { // add .ts suffix directly?
                            throw new RuntimeException("Illegal url: " + row);
                        }
                        // 下载的对象后缀不对，这边兼容处理，ffmpeg只支持ts后缀的，内部解码器要匹配的
                        else {
                            File downloadedFile = new File(downloadResourcePath, row);
                            String suffix = row.substring(i + 1);
                            String newRow = row.substring(0, i) + ".ts";
                            File updatedFile = new File(downloadResourcePath, newRow);
                            if (!updatedFile.exists()) {
                                if (!downloadedFile.exists()) {
                                    throw new IllegalStateException("Rename file not exist, src: " + downloadedFile.getAbsolutePath());
                                }
                                try {
                                    TsFileFixManager.fix(suffix, downloadedFile);
                                    FileUtils.copySafely(downloadedFile, updatedFile);
                                } catch (IOException e) {
                                    throw new IllegalStateException("Copy file failed, src: " + downloadedFile.getAbsolutePath() + ", dest: " + updatedFile.getAbsolutePath());
                                }
                            }
                            row = newRow;
                        }
                    }
                    return row;
                }

                return row;
            }).collect(Collectors.joining("\n"));

            String m3u8FilePath = downloadResourcePath + File.separator + M3U8_FILE_NAME;
            FileUtils.writeFileSafely(new ByteArrayInputStream(cleanedM3u8FileContent.getBytes(StandardCharsets.UTF_8)), m3u8FilePath);

            if (log.isDebugEnabled()) {
                log.debug("==> success generate index.m3u8 file, path: " + new File(m3u8FilePath).getAbsolutePath());
            }

            context.triggerNextStep();
            return null;
        } catch (Exception e) {
            log.error("Failed to download cleaned index.m3u8 file", e);
            return null;
        }
    }

    public static class TsFileFixManager {

        private static final Map<String, TsFileFixer> fixers = new ConcurrentHashMap<>();

        public static void addFixer(TsFileFixer fixer) {
            fixers.put(fixer.getSuffix(), fixer);
        }

        public static void fix(String type, File file) throws IOException {
            TsFileFixer fixer = fixers.get(type);
            if (fixer == null) {
                if (!defaultCheckCorrect(file)) {
                    // throw new IllegalStateException(file.getAbsolutePath());
                }
                return;
            }
            fixer.fix(file);
        }

        private static boolean defaultCheckCorrect(File file) throws IOException {
            FileChannel channel = FileChannel.open(
                    file.toPath(),
                    StandardOpenOption.READ
            );
            channel.position(0);
            ByteBuffer test = ByteBuffer.allocate(4);
            channel.read(test);
            test.flip();
            for (int i = 0; i < test.limit(); i++) {
                if (test.get() != TsFileFixer.TS_HEADER[i]) {
                    return false;
                }
            }
            return true;
        }

    }

    public static interface TsFileFixer {
        public static final byte[] TS_HEADER = new byte[]{0x47, 0x40, 0x00, 0x1F};
        String getSuffix();
        void fix(File file) throws IOException;
    }

    public static class IgnoredTsFileFixer implements TsFileFixer {
        private String suffix;

        public IgnoredTsFileFixer(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public String getSuffix() {
            return suffix;
        }

        @Override
        public void fix(File file) throws IOException {
            // 仅注册，不做任何操作
        }
    }

    public static class DefaultTsFileFixer implements TsFileFixer {

        private String suffix;
        private int stripLength;
        private boolean fillTsHeader;

        public DefaultTsFileFixer(String suffix, int stripLength, boolean fillTsHeader) {
            this.suffix = suffix;
            this.stripLength = stripLength;
            this.fillTsHeader = fillTsHeader;
        }

        @Override
        public String getSuffix() {
            return suffix;
        }

        @Override
        public void fix(File file) throws IOException {
            if (stripLength == 4 && fillTsHeader) {
                replace(file);
            }
            else {
                stripAndFill(file);
            }
        }

        private void replace(File file) throws IOException {
            FileChannel channel = FileChannel.open(
                    file.toPath(),
                    StandardOpenOption.WRITE
            );
            channel.position(0);
            channel.write(ByteBuffer.wrap(TS_HEADER));
        }

        private void stripAndFill(File file) throws IOException {
            File stripedFile = new File(file.getAbsolutePath(), ".strip");
            try (FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                 FileOutputStream fos = new FileOutputStream(stripedFile.getAbsolutePath())) {
                byte[] bytes = IOUtils.readAsByteArray(fis);
                byte[] newBytes = new byte[bytes.length - stripLength + (fillTsHeader ? TS_HEADER.length : 0)];
                if (fillTsHeader) {
                    System.arraycopy(TS_HEADER, 0, newBytes, 0, TS_HEADER.length);
                }
                System.arraycopy(bytes, stripLength, newBytes, (fillTsHeader ? TS_HEADER.length : 0), newBytes.length);
                IOUtils.writeTo(new ByteArrayInputStream(newBytes), fos);
            }
            FileUtils.replaceSafely(stripedFile, file, true);
        }
    }
}
