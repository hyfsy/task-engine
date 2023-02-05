package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.utils.StringUtils;

import java.io.File;

import static com.hyf.task.core.video.task.CheckFileExistTask.CHECK_FILE_PATH;

/**
 * 检查文件是否已存在，存在则不执行后续任务
 *
 * @author baB_hyf
 * @date 2022/10/18
 */
@NeedAttribute(CHECK_FILE_PATH)
public class CheckFileExistTask extends VideoCommonTask<Void> {

    public static final String CHECK_FILE_PATH = "CHECK_FILE_PATH";

    @Override
    public Void process(TaskContext context) throws Exception {
        // TODO string or file type
        String checkFilePath = context.removeAttribute(CHECK_FILE_PATH);

        File file = new File(checkFilePath);
        if (StringUtils.isBlank(checkFilePath) || file.exists()) {
            return null;
        }

        context.triggerNextStep();
        return null;
    }
}
