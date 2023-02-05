package com.hyf.task.core.video.task;

import com.hyf.hotrefresh.common.util.IOUtils;
import com.hyf.task.core.FileCache;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.utils.HttpClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.hyf.task.core.video.task.DownloadHtmlTask.CACHE_IDENTITY_DOWNLOAD_HTML;
import static com.hyf.task.core.video.task.DownloadHtmlTask.DOWNLOAD_URL_VIDEO_HTML;

/**
 * 下载html文件的任务
 */
@NeedAttribute(DOWNLOAD_URL_VIDEO_HTML)
@PutAttribute(CACHE_IDENTITY_DOWNLOAD_HTML)
public class DownloadHtmlTask extends VideoDownloadTask<String> {

    /**
     * 下载视频所在的html的URL地址
     */
    public static final String DOWNLOAD_URL_VIDEO_HTML      = "DOWNLOAD_URL_VIDEO_HTML";
    /**
     * 下载视频所在的html文件的缓存标识
     */
    public static final String CACHE_IDENTITY_DOWNLOAD_HTML = "CACHE_IDENTITY_DOWNLOAD_HTML";

    @Override
    public String process(TaskContext context) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("==> start load video: " + getVideoId(context));
        }

        String htmlContent = getHtmlContent(context);

        if (log.isDebugEnabled()) {
            log.debug("==> video html content: " + htmlContent);
        }

        context.triggerNextStep();
        return htmlContent;
    }

    public String getHtmlContent(TaskContext context) throws IOException {
        String htmlUrl = getHtmlUrl(context);

        String identity = getFileIdentity(context);
        context.putAttribute(CACHE_IDENTITY_DOWNLOAD_HTML, identity);
        return FileCache.doWithCache(identity, new FileCache.CacheOperation<String>() {
            @Override
            public InputStream getSourceInputStream() throws IOException {
                String htmlContent = HttpClient.getString(htmlUrl);
                return new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public String doOperation(InputStream is) throws IOException {
                return IOUtils.readAsString(is);
            }
        });
    }

    private String getHtmlUrl(TaskContext context) {
        String htmlUrl = context.getAttribute(DOWNLOAD_URL_VIDEO_HTML);
        if (htmlUrl.contains(",")) {
            String[] urls = htmlUrl.split(",");
            htmlUrl = urls[Math.abs((int) System.currentTimeMillis()) % urls.length];
        }
        return htmlUrl;
    }

    private String getFileIdentity(TaskContext context) {
        String videoId = getVideoId(context);
        String siteType = getVideoSiteType(context);
        String identity = videoId + ".html";
        if (siteType != null) {
            identity = siteType + "-" + identity;
        }
        return "html" + File.separator + identity;
    }
}
