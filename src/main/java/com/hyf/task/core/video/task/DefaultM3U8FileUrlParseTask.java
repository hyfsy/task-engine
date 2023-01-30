package com.hyf.task.core.video.task;

import com.hyf.task.core.task.CommonTask;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;

import static com.hyf.task.core.video.constants.M3U8Constants.DOWNLOAD_URL_M3U8_FILE;

/**
 * m3u8文件的url解析任务，默认URL从{@link TaskContext}中获取
 *
 * @author baB_hyf
 * @date 2023/01/28
 */
@NeedAttribute(DOWNLOAD_URL_M3U8_FILE)
public class DefaultM3U8FileUrlParseTask extends CommonTask<Void> {
    @Override
    public Void process(TaskContext context) throws Exception {
        try {
            parseM3u8FileUrl(context);
            return null;
        } catch (Exception e) {
            log.error("Failed to get m3u8 file, id: " + context.getVideoId() + " error: " + e.getMessage(), e);
        }
        return null;
    }

    protected void parseM3u8FileUrl(TaskContext context) throws Exception {
        // by default, getting from context
    }
}
