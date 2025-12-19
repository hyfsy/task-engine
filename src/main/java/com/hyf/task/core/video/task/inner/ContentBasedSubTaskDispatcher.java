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
@NeedAttribute(value = ContentBasedSubTaskDispatcher.HINT_RANGE, required = false)
@PutAttribute(VIDEO_ID)
public abstract class ContentBasedSubTaskDispatcher extends VideoComputeTask<Void> {

    public static final String HINT_RANGE = "HINT_RANGE";

    @Override
    public Void process(TaskContext context) throws Exception {

        String videoId = getVideoId(context);
        String content = context.getAttribute(TaskConstants.TASK_PREVIOUS_PROCESS_RESULT);

        List<String> resourceList = parseResources(context, content);
        HintRange hintRange = getHintRange(context, resourceList);
        resourceList = resourceList.subList(hintRange.getStart(), hintRange.getEnd());
        doProcess(context, resourceList);

        // reset
        context.putAttribute(VIDEO_ID, videoId);

        return null;
    }

    protected abstract List<String> parseResources(TaskContext context, String content);

    protected abstract void doProcess(TaskContext context, List<String> resourceList);

    private HintRange getHintRange(TaskContext context, List<String> resourceList) {
        // 配置指定范围
        HintRange hintRange = context.getAttribute(HINT_RANGE, new HintRange());
        if (hintRange.getStart() > hintRange.getEnd() && (hintRange.getStart() != -1 && hintRange.getEnd() != - 1)) {
            throw new IllegalStateException("hintRange illegal, start: " + hintRange.getStart() + ", end: " + hintRange.getEnd());
        }
        int size = resourceList.size();
        int start = hintRange.getStart() <= 0 ? 0 : hintRange.getStart() <= size ? hintRange.getStart() - 1 : size - 1;
        int end = hintRange.getEnd() <= 0 ? size - 1 : hintRange.getEnd() <= size ? hintRange.getEnd() - 1 : size - 1;
        return new HintRange(start, end);
    }

}
