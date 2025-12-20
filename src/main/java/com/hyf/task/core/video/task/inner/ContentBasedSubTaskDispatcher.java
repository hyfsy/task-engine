package com.hyf.task.core.video.task.inner;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.constants.TaskConstants;
import com.hyf.task.core.video.task.VideoComputeTask;

import java.util.List;

import static com.hyf.task.core.video.constants.VideoConstants.VIDEO_ID;

/**
 * @author baB_hyf
 * @date 2023/12/10
 */
@NeedAttribute(TaskConstants.TASK_PREVIOUS_PROCESS_RESULT)
@PutAttribute(VIDEO_ID)
public abstract class ContentBasedSubTaskDispatcher extends VideoComputeTask<Void> {

    @Override
    public Void process(TaskContext context) throws Exception {

        String videoId = getVideoId(context);
        String content = context.getAttribute(TaskConstants.TASK_PREVIOUS_PROCESS_RESULT);

        List<String> resourceList = parseResources(context, content);
        doProcess(context, resourceList);

        // reset
        context.putAttribute(VIDEO_ID, videoId);

        return null;
    }

    protected abstract List<String> parseResources(TaskContext context, String content);

    protected abstract void doProcess(TaskContext context, List<String> resourceList);

}
