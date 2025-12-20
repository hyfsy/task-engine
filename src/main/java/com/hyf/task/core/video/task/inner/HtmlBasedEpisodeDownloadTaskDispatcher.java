package com.hyf.task.core.video.task.inner;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.task.Task;
import com.hyf.task.core.video.constants.VideoConstants;
import com.hyf.task.core.video.task.DownloadHtmlTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * @author baB_hyf
 * @date 2023/12/10
 */
@NeedAttribute(value = DownloadHtmlTask.CACHE_IDENTITY_DOWNLOAD_HTML, required = false)
@NeedAttribute(value = HtmlBasedEpisodeDownloadTaskDispatcher.HINT_RANGE, required = false)
@PutAttribute(HtmlBasedEpisodeDownloadTaskDispatcher.HTML_DOCUMENT)
@PutAttribute(VideoConstants.VIDEO_ID)
@PutAttribute(VideoConstants.VIDEO_NAME)
@PutAttribute(HtmlBasedEpisodeDownloadTaskDispatcher.RESOURCE_IDX)
public abstract class HtmlBasedEpisodeDownloadTaskDispatcher extends ContentBasedSubTaskDispatcher {

    public static final String HINT_RANGE = "HINT_RANGE";
    public static final String HTML_DOCUMENT = "HTML_DOCUMENT";
    public static final String RESOURCE_IDX = "RESOURCE_IDX"; // 特殊处理，页面名称都是一样的

    @Override
    public Void process(TaskContext context) throws Exception {
        Void result = super.process(context);

        // TODO 弱网环境暂时注释，不删缓存
        // String identity = context.removeAttribute(DownloadHtmlTask.CACHE_IDENTITY_DOWNLOAD_HTML);
        // CleanTask.setCleanTask(context, new DownloadEpisodeHtmlFileCacheCleanTask(identity));

        return result;
    }

    @Override
    protected List<String> parseResources(TaskContext context, String html) {
        Document document = Jsoup.parse(html);
        context.putAttribute(HTML_DOCUMENT, document);
        return parseResourceURLs(context, document);
    }

    @Override
    protected void doProcess(TaskContext context, List<String> resourceList) {

        HintRange hintRange = getHintRange(context, resourceList);

        for (int i = hintRange.getStart(); i < hintRange.getEnd(); i++) {
            String resource = resourceList.get(i);
            Task<?> task = getDispatchTask(context, resource, i);
            prepareTaskContext(context, resource, i);
            task.setNextStep(getNextStep());
            context.fork(task);
        }
    }

    protected abstract List<String> parseResourceURLs(TaskContext context, Document html);

    protected abstract Task<?> getDispatchTask(TaskContext context, String resource, int resourceIdx);

    protected void prepareTaskContext(TaskContext context, String resource, int resourceIdx) {
        String videoId = getVideoId(context);
        setVideoId(context, videoId + "-" + (resourceIdx + 1));
        setVideoName(context, videoId + "-" + (resourceIdx + 1));
        context.putAttribute(RESOURCE_IDX, resourceIdx);
    }

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
