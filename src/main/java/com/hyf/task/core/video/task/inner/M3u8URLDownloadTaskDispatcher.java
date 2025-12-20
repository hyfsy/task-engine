package com.hyf.task.core.video.task.inner;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.task.Task;
import com.hyf.task.core.video.constants.M3U8Constants;
import com.hyf.task.core.video.task.CheckFileExistTask;
import com.hyf.task.core.video.task.DefaultM3U8FileUrlParseTask;
import com.hyf.task.core.video.task.TransformProductTask;

import static com.hyf.task.core.video.constants.M3U8Constants.DOWNLOAD_URL_M3U8_FILE;

/**
 * @author baB_hyf
 * @date 2023/12/10
 */
@PutAttribute(M3U8Constants.DOWNLOAD_URL_M3U8_FILE)
public abstract class M3u8URLDownloadTaskDispatcher extends HtmlBasedEpisodeDownloadTaskDispatcher {

    @Override
    protected Task<?> getDispatchTask(TaskContext context, String resource, int resourceIdx) {
        return new DefaultM3U8FileUrlParseTask();
    }

    @Override
    protected void prepareTaskContext(TaskContext context, String resource, int resourceIdx) {
        super.prepareTaskContext(context, resource, resourceIdx);
        context.putAttribute(M3U8Constants.DOWNLOAD_URL_M3U8_FILE, resource);
        context.putAttribute(CheckFileExistTask.CHECK_FILE_PATH, TransformProductTask.getSaveFile(context).getAbsolutePath());
    }

}
