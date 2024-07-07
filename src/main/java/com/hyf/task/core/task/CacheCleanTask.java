package com.hyf.task.core.task;

import com.hyf.task.core.FileCache;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.utils.StringUtils;

/**
 * 清理缓存标识对应的文件缓存
 *
 * @author baB_hyf
 * @date 2023/01/29
 */
public abstract class CacheCleanTask extends Task<Void> {

    @Override
    public Void process(TaskContext context) throws Exception {
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
