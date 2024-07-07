package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.task.Task;
import com.hyf.task.core.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.hyf.task.core.video.constants.VideoConstants.DOWNLOAD_RESOURCE_PATH;

/**
 * 清理任务执行过程中产生的缓存
 *
 * @author baB_hyf
 * @date 2023/01/29
 */
@NeedAttribute(DOWNLOAD_RESOURCE_PATH)
public class CleanTask extends VideoComputeTask<File> {

    public static final String CLEAN_TASK_LIST = "CLEAN_TASK_LIST";

    public static void setCleanTask(TaskContext context, Task<?> task) {
        List<Task<?>> cleanTaskList = context.getAttribute(CLEAN_TASK_LIST);
        if (cleanTaskList == null) {
            synchronized (CleanTask.class) {
                cleanTaskList = context.getAttribute(CLEAN_TASK_LIST);
                if (cleanTaskList == null) {
                    cleanTaskList = new CopyOnWriteArrayList<>();
                }
            }
        }
        cleanTaskList.add(task);
    }

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

        List<Task<?>> cleanTaskList = context.getAttribute(CLEAN_TASK_LIST);
        if (cleanTaskList != null && !cleanTaskList.isEmpty()) {
            for (Task<?> task : cleanTaskList) {
                context.fork(task);
            }
        }

        context.triggerNextStep();
        return context.getPreviousResult();
    }
}
