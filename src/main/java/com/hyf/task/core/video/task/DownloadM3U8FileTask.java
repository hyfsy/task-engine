package com.hyf.task.core.video.task;

import com.hyf.task.core.FileCache;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.utils.HttpClient;
import com.hyf.task.core.utils.IOUtils;
import com.hyf.task.core.utils.StringUtils;
import com.hyf.task.core.video.M3U8;
import com.hyf.task.core.video.constants.VideoConstants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hyf.task.core.video.constants.M3U8Constants.*;

/**
 * 下载m3u8文件的任务
 */
@NeedAttribute(DOWNLOAD_URL_M3U8_FILE)
@NeedAttribute(value = NESTED_M3U8, required = false)
@NeedAttribute(value = M3U8_FILE_SECRET_KEY_URL_PATTERN, required = false)
@PutAttribute(CACHE_IDENTITY_DOWNLOAD_M3U8_FILE)
@PutAttribute(CACHE_IDENTITY_DOWNLOAD_M3U8_RESOURCE_LIST_FILE)
public class DownloadM3U8FileTask extends VideoDownloadTask<List<String>> {

    // TODO 抽成通用方法
    public static String getFileIdentity(TaskContext context, String fileSuffix) {
        String videoId = context.getAttribute(VideoConstants.VIDEO_ID);
        String siteType = context.getAttribute(VideoConstants.VIDEO_SITE_TYPE);
        String identity = videoId + "." + fileSuffix;
        if (siteType != null) {
            identity = siteType + "-" + identity;
        }
        return "m3u8" + File.separator + identity;
    }

    @Override
    public List<String> process(TaskContext context) throws Exception {
        try {
            String m3u8FileUrl = context.getAttribute(DOWNLOAD_URL_M3U8_FILE);

            if (StringUtils.isBlank(m3u8FileUrl)) {
                throw new IllegalStateException("DOWNLOAD_URL_M3U8_FILE must has text");
            }

            if (log.isDebugEnabled()) {
                log.debug("==> start to download index.m3u8 file, url path: " + m3u8FileUrl);
            }

            String m3u8FileContent = downloadM3u8FileContent(context, m3u8FileUrl);
            List<String> resourceList = parseResourceList(context, m3u8FileUrl, m3u8FileContent);
            List<M3U8> m3u8s = M3U8.parse(m3u8FileContent);
            // TODO
            if (!m3u8s.isEmpty() && m3u8s.get(m3u8s.size() - 1) instanceof M3U8.MainM3U8) {
                String url = getCorrectResourceUrl(m3u8FileUrl, ((M3U8.MainM3U8) m3u8s.get(m3u8s.size() - 1)).source);
                resourceList = new ArrayList<>();
                resourceList.add(url);
            }

            // 一个文件内内嵌多个m3u8文件
            Iterator<String> it;
            List<String> downloadedNew = null;
            boolean downloadNew;
            do {
                it = resourceList.iterator();
                downloadNew = false;
                while (it.hasNext()) {
                    String resourceUrl = it.next();
                    // http://index.m3u8?xxx=xxx
                    if (resourceUrl.contains("." + M3U8_FILE_SUFFIX_NAME)) {

                        DownloadM3U8FileTask subTask = new DownloadM3U8FileTask();
                        TaskContext videoContext = context.copy();
                        videoContext.putAttribute(NESTED_M3U8, true);
                        videoContext.putAttribute(DOWNLOAD_URL_M3U8_FILE, resourceUrl);
                        List<String> processedList = subTask.process(videoContext); // TODO async
                        if (downloadedNew == null) {
                            downloadedNew = new ArrayList<>(processedList);
                        }
                        else {
                            downloadedNew.addAll(processedList);
                        }

                        it.remove();
                    }
                }

                if (downloadedNew != null && !downloadedNew.isEmpty()) {
                    resourceList.addAll(downloadedNew);
                    downloadedNew = null;
                    downloadNew = true;
                }
            } while (downloadNew);

            context.triggerNextStep();
            return resourceList;
        } catch (Exception e) {
            log.error("Failed to download m3u8 file, id: " + getVideoId(context) + " error: " + e.getMessage(), e);
            return null;
        }
    }

    private String downloadM3u8FileContent(TaskContext context, String m3u8FileUrl) throws IOException {
        // TODO 嵌套多个的情况
        String identity = getFileIdentity(context, "m3u8");
        context.putAttribute(CACHE_IDENTITY_DOWNLOAD_M3U8_FILE, identity);

        Boolean nestedM3u8 = context.getAttribute(NESTED_M3U8);
        if (nestedM3u8 != null && nestedM3u8) {
            FileCache.clearCache(identity);
        }

        return FileCache.doWithCache(identity, new FileCache.CacheOperation<String>() {
            @Override
            public InputStream getSourceInputStream() throws IOException {
                String htmlContent = HttpClient.getString(m3u8FileUrl);
                // TODO 自定义的 replace 操作
                // jiujiure里有此问题
                // m3u8FileContent = m3u8FileContent.replaceAll("#EXT-X-KEY:METHOD=NONE\n" + "#EXTINF:3,\n"
                //         + "https://vip2.bfbfhao.com/20220602/gGCdbUnN/500kb/hls/j6tlvdmA.ts\n" + "#EXTINF:3,\n"
                //         + "https://vip2.bfbfhao.com/20220602/gGCdbUnN/500kb/hls/AhDL2Y6v.ts\n" + "#EXTINF:0.96,\n"
                //         + "https://vip2.bfbfhao.com/20220602/gGCdbUnN/500kb/hls/KMqP4IHE.ts\n", "");
                // m3u8FileContent = m3u8FileContent.replaceAll("#EXT-X-DISCONTINUITY\n", "");
                return new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public String doOperation(InputStream is) throws IOException {
                return IOUtils.readAsString(is);
            }
        });
    }

    private List<String> parseResourceList(TaskContext context, String m3u8FileUrl, String m3u8FileContent) throws IOException {
        Pattern secretKeyUrlPattern = context.getAttribute(M3U8_FILE_SECRET_KEY_URL_PATTERN) == null ?
                SECRET_KEY_URL_PATTERN : context.getAttribute(M3U8_FILE_SECRET_KEY_URL_PATTERN);
        String identity = getFileIdentity(context, "resource-list");
        context.putAttribute(CACHE_IDENTITY_DOWNLOAD_M3U8_RESOURCE_LIST_FILE, identity);

        Boolean nestedM3u8 = context.getAttribute(NESTED_M3U8);
        if (nestedM3u8 != null && nestedM3u8) {
            FileCache.clearCache(identity);
        }

        return FileCache.doWithCache(identity, new FileCache.CacheOperation<List<String>>() {

            @Override
            public InputStream getSourceInputStream() throws IOException {

                List<String> resourceList = new ArrayList<>();

                // TODO generify

                // parse index.m3u8 file
                for (String row : m3u8FileContent.split("\\n")) {

                    // get key
                    if (row.startsWith(SECRET_KEY_PREFIX) && !row.startsWith(SECRET_KEY_NONE)) {
                        // object script
                        Matcher matcher = secretKeyUrlPattern.matcher(row);

                        // perhaps not exist key if not find
                        if (matcher.find()) {
                            String keyUrl = matcher.group(1);
                            resourceList.add(getCorrectResourceUrl(m3u8FileUrl, keyUrl));
                        }
                    }

                    // get resource
                    if (!row.startsWith("#")) {
                        resourceList.add(getCorrectResourceUrl(m3u8FileUrl, row));
                    }
                }

                return new ByteArrayInputStream(String.join("\n", resourceList).getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public List<String> doOperation(InputStream is) throws IOException {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    IOUtils.writeTo(is, baos);
                    return new ArrayList<>(Arrays.asList(baos.toString().split("\n").clone()));
                }
            }
        });

    }

    private String getCorrectResourceUrl(String m3u8FileUrl, String row) {
        if (row.startsWith("http")) {
            return row;
        }

        // http://www.baidu.com/xxx/xxx.m3u8
        // xxx.m3u8
        // /xxx/xxx.m3u8
        // xxx/xxx.m3u8
        // xxx.png

        if (row.startsWith("/")) {
            row = row.substring(1);
        }

        if (row.contains("/")) {
            String samePath = row.substring(0, row.indexOf("/") + 1);
            // 此特殊情况在哪用到？
            int samePathIdx = m3u8FileUrl.lastIndexOf(samePath);
            if (samePathIdx != -1) {
                String prefix = m3u8FileUrl.substring(0, samePathIdx);
                return prefix + row;
            }
        }

        // other suffix
        if (!row.endsWith(".ts") && !row.endsWith(".m3u8") && !row.endsWith(".key")) {
            int i = row.lastIndexOf(".");
            if (i == -1) { // add .ts suffix directly?
                throw new RuntimeException("Illegal url: " + row);
            }
            else {
                row = row.substring(0, i) + ".ts";
            }
        }

        // relative path
        return m3u8FileUrl.substring(0, m3u8FileUrl.lastIndexOf("/") + 1) + row;
    }

}
