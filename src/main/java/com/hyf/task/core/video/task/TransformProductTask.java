package com.hyf.task.core.video.task;

import com.hyf.task.core.task.CommonTask;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.exception.TaskException;
import com.hyf.task.core.utils.StringUtils;

import java.io.File;

import static com.hyf.task.core.video.constants.VideoConstants.*;

/**
 * 将任务执行后的结果物转移到用户指定的保存路径下
 */
@NeedAttribute(DOWNLOAD_RESOURCE_PATH)
@NeedAttribute(VIDEO_SAVE_PATH)
@NeedAttribute(value = VIDEO_SAVE_NAME, required = false)
public class TransformProductTask extends CommonTask<File> {

    public static File getSaveFile(TaskContext context) {
        String videoSavePath = context.getVideoSavePath();
        String videoSaveName = context.getAttribute(VIDEO_SAVE_NAME);
        if (videoSaveName == null) {
            videoSaveName = context.getVideoId() + (StringUtils.isBlank(context.getVideoName()) ? "" : "---" + context.getVideoName()) + ".mp4";
        }
        return new File(videoSavePath, videoSaveName);
    }

    @Override
    public File process(TaskContext context) throws Exception {

        File tempSaveFile = context.getPreviousResult();
        if (tempSaveFile == null) {
            throw new IllegalStateException("SaveFile is null");
        }

        String tempFileSavePath = context.getAttribute(DOWNLOAD_RESOURCE_PATH);
        File dir = new File(tempFileSavePath);
        File[] files = dir.listFiles();

        File output = null;
        if (files != null) {
            for (File file : files) {
                if (tempSaveFile.getName().equals(file.getName())) {
                    output = file;
                    break;
                }
            }
        }

        if (output == null) {
            throw new RuntimeException("Cannot find decoded file: " + tempFileSavePath);
        }

        File saveFile = getSaveFile(context);

        // if (!FileUtils.copy(output, saveFile)) {
        //     throw new VideoException("Failed to rename file: " + output.getAbsolutePath());
        // }

        if (!saveFile.exists()) {
            // TODO 重命名时文件损坏情况
            if (!output.renameTo(saveFile)) {
                throw new TaskException("Failed to rename file: " + output.getAbsolutePath());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("==> success download video: " + context.getVideoId() + ", file path: " + saveFile.getAbsolutePath());
        }

        context.triggerNextStep();
        return saveFile;
    }
}
