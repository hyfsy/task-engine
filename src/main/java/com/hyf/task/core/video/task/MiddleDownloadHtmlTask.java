package com.hyf.task.core.video.task;

import com.alibaba.fastjson.JSONObject;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.task.CacheCleanTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author baB_hyf
 * @date 2026/01/11
 */
public class MiddleDownloadHtmlTask extends DownloadHtmlTask {

    private final Pattern DEFAULT_NAME_PATTERN = Pattern.compile("<title>(.*?)</title>");

    private final Pattern pattern = Pattern.compile("var player_aaaa=(.*?)<");

    private String identity;

    public MiddleDownloadHtmlTask(String identity) {
        this.identity = identity;
    }

    protected String parseTitle(TaskContext context, String htmlContent) {
        Matcher matcher1 = DEFAULT_NAME_PATTERN.matcher(htmlContent);
        if (!matcher1.find()) {
            throw new RuntimeException("Cannot find video title in html, videoId: " + getVideoId(context) + ", content: " + htmlContent);
        }

        // 有需求只能在这边手动设置title，后续不在查找title
        String title = matcher1.group(1);
        return title;
    }

    protected String parseMiddleHtmlUrl(TaskContext context, String htmlContent) {

        Matcher matcher = pattern.matcher(htmlContent);
        if (!matcher.find()) {
            throw new RuntimeException("Cannot find video object in html, videoId: " + getVideoId(context) + ", content: " + htmlContent);
        }

        JSONObject data = JSONObject.parseObject(matcher.group(1));
        String url = data.getString("url");
        return "https://vip.fcdmjx.com/index.php?url=" + url;
    }

    @Override
    public String process(TaskContext context) throws Exception {
        String htmlContent = super.process(context);

        // 临时文件封存
        String htmlCachePath = context.getAttribute(CACHE_IDENTITY_DOWNLOAD_HTML);
        CacheCleanTask.putCache(context, htmlCachePath);

        String title = parseTitle(context, htmlContent);
        if (title != null) {
            setVideoName(context, title);
        }

        String url = parseMiddleHtmlUrl(context, htmlContent);
        if (url == null) {
            throw new RuntimeException("Cannot find video url in html, videoId: " + getVideoId(context) + ", content: " + htmlContent);
        }

        // 切换url
        context.putAttribute(DOWNLOAD_URL_VIDEO_HTML, url);

        return htmlContent;
    }

    @Override
    protected String getFileIdentity(TaskContext context) {
        return super.getFileIdentity(context) + "." + identity;
    }
}
