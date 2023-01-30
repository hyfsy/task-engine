package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.task.FileCacheCleanTask;
import com.hyf.task.core.video.constants.M3U8Constants;

/**
 * 清除下载的m3u8资源列表文件缓存
 *
 * @author baB_hyf
 * @date 2023/01/29
 */
@NeedAttribute(value = M3U8Constants.CACHE_IDENTITY_DOWNLOAD_M3U8_RESOURCE_LIST_FILE, required = false)
public class DownloadM3U8ResourceListFileCacheCleanTask extends FileCacheCleanTask {
    @Override
    protected String getIdentity(TaskContext context) {
        return M3U8Constants.CACHE_IDENTITY_DOWNLOAD_M3U8_RESOURCE_LIST_FILE;
    }
}
