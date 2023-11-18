package com.hyf.task.core.video.task.inner;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.constants.TaskConstants;
import com.hyf.task.core.utils.StringUtils;
import com.hyf.task.core.video.task.CheckFileExistTask;
import com.hyf.task.core.video.task.DefaultM3U8FileUrlParseTask;
import com.hyf.task.core.video.task.TransformProductTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hyf.task.core.video.constants.M3U8Constants.DOWNLOAD_URL_M3U8_FILE;

/**
 * @author baB_hyf
 * @date 2023/08/19
 */
public abstract class HtmlBasedM3U8FileUrlParseTask extends DefaultM3U8FileUrlParseTask {

    private static final Pattern DEFAULT_NAME_PATTERN = Pattern.compile("<title>(.*?)</title>");

    @Override
    protected final void parseM3u8FileUrl(TaskContext context) throws Exception {

        String htmlContent = context.getAttribute(TaskConstants.TASK_PREVIOUS_PROCESS_RESULT);
        htmlContent = htmlContent.replaceAll("\\n", "");
        htmlContent = htmlContent.replaceAll("\\r", "");
        if (StringUtils.isBlank(htmlContent)) {
            throw new IllegalStateException("HtmlContent is blank, videoId: " + getVideoId(context));
        }

        // object script
        Matcher matcher = getObjectPattern().matcher(htmlContent);
        if (!matcher.find()) {
            throw new RuntimeException("Cannot find video object in html, videoId: " + getVideoId(context) + ", content: " + htmlContent);
        }

        // title
        Matcher matcher2 = getNamePattern().matcher(htmlContent);
        if (!matcher2.find()) {
            throw new RuntimeException("Cannot find video title in html, videoId: " + getVideoId(context) + ", content: " + htmlContent);
        }

        preProcessContext(context);

        String videoName = normalizeVideoName(context, parseVideoNameFromPattern(matcher2, context));
        String m3u8Url = normalizeUrl(parseM3u8UrlFromPattern(matcher, context));

        setVideoName(context, videoName);
        context.putAttribute(DOWNLOAD_URL_M3U8_FILE, m3u8Url);
        context.putAttribute(CheckFileExistTask.CHECK_FILE_PATH, TransformProductTask.getSaveFile(context).getAbsolutePath());

        postProcessContext(context);

        context.triggerNextStep();
    }

    protected Pattern getNamePattern() {
        return DEFAULT_NAME_PATTERN;
    }

    protected abstract Pattern getObjectPattern();

    protected String parseVideoNameFromPattern(Matcher matcher, TaskContext context) {
        return matcher.group(1);
    }

    protected abstract String parseM3u8UrlFromPattern(Matcher matcher, TaskContext context);

    protected void preProcessContext(TaskContext context) {
    }

    protected void postProcessContext(TaskContext context) {
    }

}
