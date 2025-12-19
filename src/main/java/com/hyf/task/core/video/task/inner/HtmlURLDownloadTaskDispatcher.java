package com.hyf.task.core.video.task.inner;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.task.Task;
import com.hyf.task.core.video.task.DownloadHtmlTask;

/**
 * @author baB_hyf
 * @date 2023/12/10
 */
public abstract class HtmlURLDownloadTaskDispatcher extends HtmlBasedEpisodeDownloadTaskDispatcher {

    @Override
    protected Task<?> getDispatchTask(TaskContext context, String resource, int resourceIdx) {
        return new DownloadHtmlTask();
    }

    @Override
    protected void prepareTaskContext(TaskContext context, String resource, int resourceIdx) {
        super.prepareTaskContext(context, resource, resourceIdx);
        context.putAttribute(DownloadHtmlTask.DOWNLOAD_URL_VIDEO_HTML, resource);
    }

}
