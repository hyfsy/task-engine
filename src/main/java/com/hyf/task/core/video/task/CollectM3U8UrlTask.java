package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.NeedAttribute;
import com.hyf.task.core.video.constants.M3U8Constants;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author baB_hyf
 * @date 2023/08/27
 */
@NeedAttribute(M3U8Constants.DOWNLOAD_URL_M3U8_FILE)
public class CollectM3U8UrlTask extends VideoCommonTask<Void> {

    private final AtomicInteger                 count;
    private final AtomicBoolean                 executed = new AtomicBoolean(false);
    private final Map<String, String>           urlMap;
    // videoId -> url (ordered)
    private final Consumer<Map<String, String>> callback;
    private final Comparator<String>            comparator;

    public CollectM3U8UrlTask(int count, Consumer<Map<String, String>> callback) {
        this(count, callback, null);
    }

    public CollectM3U8UrlTask(int count, Consumer<Map<String, String>> callback, Comparator<String> comparator) {
        this.count = new AtomicInteger(count);
        this.urlMap = new ConcurrentHashMap<>(count);
        this.callback = callback;
        this.comparator = comparator;
    }

    @Override
    public Void process(TaskContext context) throws Exception {
        String url = context.getPreviousResult();
        int c = count.decrementAndGet();
        if (c >= 0) {
            urlMap.put(getVideoId(context), url);
        }
        if (c == 0 && executed.compareAndSet(false, true)) {
            TreeMap<String, String> sortedMap = new TreeMap<>(comparator);
            sortedMap.putAll(urlMap);
            callback.accept(sortedMap);
        }
        return null;
    }
}
