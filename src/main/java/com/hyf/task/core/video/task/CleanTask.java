package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.utils.FileUtils;

import java.io.File;

import static com.hyf.task.core.video.constants.VideoConstants.DOWNLOAD_RESOURCE_PATH;

/**
 * 清理任务执行过程中产生的缓存
 *
 * @author baB_hyf
 * @date 2023/01/29
 */
@NeedAttribute(DOWNLOAD_RESOURCE_PATH)
public class CleanTask extends VideoComputeTask<File> {

    @Override
    public File process(TaskContext context) throws Exception {

        String tempFileSavePath = context.getAttribute(DOWNLOAD_RESOURCE_PATH);
        File dir = new File(tempFileSavePath);

        if (log.isDebugEnabled()) {
            log.debug("==> clean resource......, path: " + dir.getAbsolutePath());
        }

        FileUtils.delete(dir);

        context.fork(new DownloadHtmlFileCacheCleanTask());
        context.fork(new DownloadM3U8FileCacheCleanTask());
        context.fork(new DownloadM3U8ResourceListFileCacheCleanTask());

        context.triggerNextStep();
        return context.getPreviousResult();
    }
}
