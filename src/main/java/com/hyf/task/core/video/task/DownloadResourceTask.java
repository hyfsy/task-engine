package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.exception.TaskException;
import com.hyf.task.core.utils.FileUtils;
import com.hyf.task.core.utils.HttpClient;
import com.hyf.task.core.utils.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static com.hyf.task.core.video.constants.VideoConstants.*;

/**
 * 下载资源文件的任务
 */
@NeedAttribute(VIDEO_SAVE_PATH)
@NeedAttribute(DOWNLOAD_RESOURCE_URL)
@NeedAttribute(value = VIDEO_NAME, required = false)
@NeedAttribute(value = DOWNLOAD_RESOURCE_PATH, required = false)
@PutAttribute(DOWNLOAD_RESOURCE_PATH)
public class DownloadResourceTask extends VideoDownloadTask<File> {

    public static String getDownloadResourcePath(TaskContext context) {
        String resourceSavePath = context.getAttribute(VIDEO_SAVE_PATH);
        String downloadResourcePath = context.getAttribute(DOWNLOAD_RESOURCE_PATH);
        if (StringUtils.isBlank(downloadResourcePath) && StringUtils.isNotBlank(context.getAttribute(VIDEO_NAME))) {
            downloadResourcePath = resourceSavePath + File.separator + context.getAttribute(VIDEO_NAME);
            context.putAttribute(DOWNLOAD_RESOURCE_PATH, downloadResourcePath);
        }
        if (StringUtils.isBlank(downloadResourcePath)) {
            downloadResourcePath = resourceSavePath + File.separator + UUID.randomUUID();
            context.putAttribute(DOWNLOAD_RESOURCE_PATH, downloadResourcePath);
        }
        return downloadResourcePath;
    }

    @Override
    public File process(TaskContext context) throws Exception {

        try {
            String resourceUrl = context.getAttribute(DOWNLOAD_RESOURCE_URL);

            if (StringUtils.isBlank(resourceUrl)) {
                throw new IllegalStateException("DOWNLOAD_RESOURCE_URL must has text");
            }

            String downloadResourcePath = getDownloadResourcePath(context);
            File downloadFile = new File(downloadResourcePath, extractFileNameFromUrl(resourceUrl));

            if (log.isDebugEnabled()) {
                log.debug("==> download video start, filePath: " + downloadFile.getAbsolutePath());
            }

            if (downloadFile.exists()) { // idempotent
                context.triggerNextStep();
                return downloadFile;
            }

            // TODO 此处有时会阻塞住，没有任何反应
            // TODO 大文件情况，此处看不到任何情况、是否可支持断点续传，临时文件续传
            try (CloseableHttpResponse response = HttpClient.get(resourceUrl)) {
                if (response.getCode() >= 300) { // such as 502 504 404(sometimes)
                    throw new IOException("Failed to get resource, code: " + response.getCode() + ", url: " + resourceUrl);
                }
                // TODO 此处有时会阻塞住，没有任何反应
                try (InputStream is = response.getEntity().getContent()) {
                    if (is == null) {
                        log.warn("Download video but no content");
                    }
                    else {
                        FileUtils.writeFileSafely(is, downloadFile.getAbsolutePath());
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("==> download video finished");
            }

            context.triggerNextStep();
            return downloadFile;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new TaskException("Failed to download ts resource, id: " + getVideoId(context), e);
        }
    }

    public String extractFileNameFromUrl(String resourceUrl) {
        int queryIdx = resourceUrl.indexOf("?");
        if (queryIdx > 0) {
            resourceUrl = resourceUrl.substring(0, queryIdx);
        }

        if (resourceUrl.endsWith("/")) {
            resourceUrl = resourceUrl.substring(0, resourceUrl.length() - 1);
        }
        return resourceUrl.substring(resourceUrl.lastIndexOf("/") + 1);
    }
}
