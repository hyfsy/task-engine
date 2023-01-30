package com.hyf.task.core.video.task;

import com.hyf.task.core.task.CommonTask;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.task.CompositeTask;
import com.hyf.task.core.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.hyf.task.core.video.constants.VideoConstants.DOWNLOAD_RESOURCE_URL;
import static com.hyf.task.core.video.constants.VideoConstants.PREVIOUS_PROCESS_RESULT;

/**
 * 多资源文件下载任务派发器，所有资源下载完后才执行后续任务
 */
@NeedAttribute(PREVIOUS_PROCESS_RESULT)
@NeedAttribute(DOWNLOAD_RESOURCE_URL)
public class DownloadResourceTaskDispatcher extends CommonTask<Void> {

    @Override
    public Void process(TaskContext context) throws Exception {
        String videoSavePath = context.getVideoSavePath();
        List<String> downloadUrlList = context.getAttribute(PREVIOUS_PROCESS_RESULT);

        if (downloadUrlList == null || downloadUrlList.isEmpty()) {
            return null;
        }

        if (StringUtils.isBlank(videoSavePath)) {
            throw new IllegalStateException("VIDEO_SAVE_PATH must has text");
        }

        // TODO get final video name
        // if (videoAlreadyExist() && log.isDebugEnabled()) {
        //     log.debug("==> video file already exist, videoId: " + context.videoId);
        // }

        List<String> resourceUrls = downloadUrlList.stream()
                .filter(url -> url != null && !"".equals(url.trim()))
                .map(url -> url.endsWith("/") ? url.substring(0, url.length() - 1) : url)
                .collect(Collectors.toList());

        if (resourceUrls.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("cannot find resource list in video obj, videoId: " + context.getVideoId());
            }
            return null;
        }

        // TODO 嵌套情況
        // if (log.isDebugEnabled()) {
        //     log.debug("==> process resource size: " + resourceUrls.size());
        // }

        List<CompositeTask.CompositeSubTask<File>> tasks = new ArrayList<>();

        for (String resourceUrl : resourceUrls) {
            DownloadResourceTask downloadResourceTask = new DownloadResourceTask();
            TaskContext newContext = context.copy();
            newContext.putAttribute(DOWNLOAD_RESOURCE_URL, resourceUrl);
            tasks.add(new CompositeTask.CompositeSubTask<>(downloadResourceTask, newContext));
        }

        CompositeTask<File> compositeTask = new CompositeTask<>(tasks, (r1, r2) -> null, (r, t) -> {
            if (t != null) {
                log.error("==> file down load failed, id: " + context.getVideoId(), t);
                return;
            }
            // TODO result
            // TODO 此处再触发context是无效的
            // TODO 获取触发的task
            context.fireTriggerNextStep(); // TODO unrecommended
        });

        context.fork(compositeTask);

        return null;
    }
}
