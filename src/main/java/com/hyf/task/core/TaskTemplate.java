package com.hyf.task.core;

import com.hyf.task.core.video.Env;
import com.hyf.task.core.video.task.*;
import com.hyf.task.core.video.task.inner.HtmlBasedM3U8FileUrlParseTask;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hyf.task.core.video.constants.VideoConstants.*;

/**
 * @author baB_hyf
 * @date 2025/08/17
 */
public class TaskTemplate {

    private final Map<String, Range> series       = new LinkedHashMap<>();
    private       String             savePath     = DEFAULT_VIDEO_SAVE_PATH;
    private       boolean            disableLimit = true;

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void setDisableLimit(boolean disableLimit) {
        this.disableLimit = disableLimit;
    }

    public void addSeries(String videoId, int num) {
        series.put(videoId, new Range(num));
    }

    public void addSeries(String videoId, int start, int end) {
        series.put(videoId, new Range(start, end));
    }

    public void execute(ResourceParser resourceParser) {

        if (disableLimit) {
            Env.disableLimit();
        }

        // Env.setVideoDownloadPath("C:\\Users\\baB_hyf\\Desktop\\sx");
        // Env.setComputerShutdownWhenFinished();
        //
        // int episodeSize = 67;
        //
        // Consumer<Map<String, String>> consumer = (map) -> {
        //     map.forEach((k, v) -> System.out.println(k + ": " + v));
        //     System.out.println(String.join(";", map.values()));
        // };
        // Comparator<String> comparator = (s1, s2) -> {
        //     String episodeNumber = s1.substring(s1.indexOf("-") + 1);
        //     String episodeNumber2 = s2.substring(s2.indexOf("-") + 1);
        //     return Integer.valueOf(episodeNumber).compareTo(Integer.valueOf(episodeNumber2));
        // };

        TaskPipeline pipeline = new TaskPipeline()
                .add(new DownloadHtmlTask())
                .add(new HtmlBasedM3U8FileUrlParseTask() {

                    @Override
                    protected Pattern getObjectPattern() {
                        return resourceParser.m3u8Pattern();
                    }

                    @Override
                    protected String parseM3u8UrlFromPattern(Matcher matcher, TaskContext context) {
                        return matcher.group(resourceParser.m3u8PatternGroupIndex());
                    }
                })
                // .add(new CollectM3U8UrlTask(episodeSize, consumer, comparator))
                .add(new CheckFileExistTask())
                .add(new DownloadM3U8FileTask())
                .add(new DownloadResourceTaskDispatcher())
                .add(new TransformM3U8FileTask())
                .add(new MergeTSResourceTask())
                .add(new TransformProductTask())
                .add(new CleanTask());

        TaskEngine engine = new TaskEngine(pipeline);


        for (Map.Entry<String, Range> entry : series.entrySet()) {

            String videoId = entry.getKey();

            Range range = entry.getValue();

            int i = range.start;
            while (i <= range.end) {

                TaskInstruction instruction = new TaskInstruction();

                instruction.putAttribute(VIDEO_ID, videoId + "-" + i);
                instruction.putAttribute(VIDEO_SAVE_PATH, savePath);
                instruction.putAttribute(DownloadHtmlTask.DOWNLOAD_URL_VIDEO_HTML, resourceParser.getHtmlUrl(videoId, i));
                instruction.putAttribute(VIDEO_SITE_TYPE, resourceParser.siteType());

                engine.submit(instruction);

                i++;
            }

        }
    }

    public interface ResourceParser {
        String getHtmlUrl(String videoId, int i);

        Pattern m3u8Pattern();

        int m3u8PatternGroupIndex();

        String siteType();
    }

    // 1-x
    private static class Range {
        int start;
        int end;

        public Range(int num) {
            this(1, num);
        }

        public Range(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
