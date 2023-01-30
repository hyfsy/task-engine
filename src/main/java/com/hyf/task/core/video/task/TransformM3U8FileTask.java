package com.hyf.task.core.video.task;

import com.hyf.task.core.task.CommonTask;
import com.hyf.task.core.FileCache;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.utils.FileUtils;
import com.hyf.task.core.utils.IOUtils;
import com.hyf.task.core.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hyf.task.core.video.constants.M3U8Constants.*;

/**
 * 将m3u8文件重新转换为本地可使用的文件（例如ts文件路径等）并迁移到指定目录下
 */
@NeedAttribute(CACHE_IDENTITY_DOWNLOAD_M3U8_FILE)
public class TransformM3U8FileTask extends CommonTask<Void> {

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
                    return row.substring(row.lastIndexOf("/") + 1);
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
}
