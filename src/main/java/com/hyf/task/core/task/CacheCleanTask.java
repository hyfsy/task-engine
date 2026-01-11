package com.hyf.task.core.task;

import com.hyf.task.core.FileCache;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.annotation.PutAttribute;
import com.hyf.task.core.utils.StringUtils;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 清理缓存标识对应的文件缓存
 *
 * @author baB_hyf
 * @date 2023/01/29
 */
@PutAttribute(CacheCleanTask.CACHE_KEY)
public abstract class CacheCleanTask extends Task<Void> {

    public static final String CACHE_KEY = "KEY_CacheCleanTask";

    public static void putCache(TaskContext context, String relativeCacheFilePath) {
        Set<String> cache = context.getAttribute(CACHE_KEY);
        if (cache == null) {
            cache = new LinkedHashSet<>();
            context.putAttribute(CACHE_KEY, cache);
        }
        cache.add(relativeCacheFilePath);
    }

    @Override
    public Void process(TaskContext context) throws Exception {

        Set<String> cache = context.getAttribute(CACHE_KEY);
        if (cache != null) {
            for (String cacheRelativePath : cache) {
                if (StringUtils.isNotBlank(cacheRelativePath)) {
                    FileCache.clearCache(cacheRelativePath);
                }
            }
            context.removeAttribute(CACHE_KEY);
        }

        String cacheIdentity = getIdentity(context);
        String cacheRelativePath = context.getAttribute(cacheIdentity);
        if (StringUtils.isNotBlank(cacheRelativePath)) {
            FileCache.clearCache(cacheRelativePath);
        }
        String cacheRelativePath2 = getIdentityContent(context);
        if (StringUtils.isNotBlank(cacheRelativePath2)) {
            FileCache.clearCache(cacheRelativePath2);
        }
        context.triggerNextStep();
        return null;
    }

    protected String getIdentity(TaskContext context) {
        return null;
    }

    protected String getIdentityContent(TaskContext context) {
        return null;
    }
}
