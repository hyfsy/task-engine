package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static com.hyf.task.core.video.constants.M3U8Constants.DOWNLOAD_URL_M3U8_FILE;

/**
 * m3u8文件的url解析任务，默认URL从{@link TaskContext}中获取
 *
 * @author baB_hyf
 * @date 2023/01/28
 */
@NeedAttribute(value = DOWNLOAD_URL_M3U8_FILE, required = false)
public class DefaultM3U8FileUrlParseTask extends VideoCommonTask<String> {
    @Override
    public final String process(TaskContext context) throws Exception {
        try {
            parseM3u8FileUrl(context);
        } catch (Exception e) {
            log.error("Failed to get m3u8 file, id: " + getVideoId(context) + " error: " + e.getMessage(), e);
        }
        // TODO context.triggerNextStep();
        return context.getAttribute(DOWNLOAD_URL_M3U8_FILE);
    }

    protected void parseM3u8FileUrl(TaskContext context) throws Exception {
        // by default, getting from context
    }

    protected String normalizeUrl(String str) {
        try {
            return StringEscapeUtils.unescapeJava(URLDecoder.decode(str, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            // log.warn("string unescape failed", e);
            return str;
        }
    }

    protected String normalizeVideoName(TaskContext context, String videoName) {
        String beforeVideoName = getVideoName(context);
        if (beforeVideoName != null) {
            return beforeVideoName;
        }

        videoName = videoName.trim();
        videoName = videoName.replace("\\", "-");
        videoName = videoName.replace("/", "-");
        videoName = videoName.replace(":", "-");
        videoName = videoName.replace("*", "-");
        videoName = videoName.replace("?", "-");
        videoName = videoName.replace("\"", "-");
        videoName = videoName.replace("<", "-");
        videoName = videoName.replace(">", "-");
        videoName = videoName.replace("|", "-");
        videoName = normalizeUrl(videoName);

        int windowsLimitMaxFilePathLength = 200;
        setVideoName(context, videoName); // 给 TransformProductTask 使用
        File saveFile = TransformProductTask.getSaveFile(context);
        String absolutePath = saveFile.getAbsolutePath();
        if (absolutePath.length() > windowsLimitMaxFilePathLength) {
            int i = absolutePath.length() - windowsLimitMaxFilePathLength;
            i = i < 50 ? videoName.length() > 50 ? 50 : i : i;
            videoName = videoName.substring(0, videoName.length() - i);
        }
        return videoName;
    }
}
