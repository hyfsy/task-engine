package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.task.FileCacheCleanTask;

/**
 * 清除下载的html文件缓存
 *
 * @author baB_hyf
 * @date 2023/01/29
 */
@NeedAttribute(value = DownloadHtmlTask.CACHE_IDENTITY_DOWNLOAD_HTML, required = false)
public class DownloadHtmlFileCacheCleanTask extends FileCacheCleanTask {
    @Override
    protected String getIdentity(TaskContext context) {
        return DownloadHtmlTask.CACHE_IDENTITY_DOWNLOAD_HTML;
    }
}
