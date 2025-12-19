package com.hyf.task.core.video.task.inner;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.task.Task;
import com.hyf.task.core.video.task.DownloadHtmlTask;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

import static com.hyf.task.core.video.constants.VideoConstants.VIDEO_ID;
import static com.hyf.task.core.video.constants.VideoConstants.VIDEO_NAME;

/**
 * @author baB_hyf
 * @date 2023/12/10
 */
@NeedAttribute(value = DownloadHtmlTask.CACHE_IDENTITY_DOWNLOAD_HTML, required = false)
@PutAttribute(HtmlBasedEpisodeDownloadTaskDispatcher.HTML_DOCUMENT)
@PutAttribute(HtmlBasedEpisodeDownloadTaskDispatcher.RESOURCE_IDX)
public abstract class HtmlBasedEpisodeDownloadTaskDispatcher extends ContentBasedSubTaskDispatcher {

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
        for (int i = 0; i < resourceList.size(); i++) {
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
        context.putAttribute(VIDEO_ID, videoId + "-" + (resourceIdx + 1));
        context.putAttribute(VIDEO_NAME, videoId + "-" + (resourceIdx + 1));
        context.putAttribute(RESOURCE_IDX, resourceIdx);
    }

}
