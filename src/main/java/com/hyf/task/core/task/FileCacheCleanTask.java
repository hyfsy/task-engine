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
public abstract class FileCacheCleanTask extends ComputeTask<Void> {

    @Override
    public Void process(TaskContext context) throws Exception {
        String cacheIdentity = getIdentity(context);
        if (StringUtils.isNotBlank(cacheIdentity)) {
            FileCache.clearCache(cacheIdentity);
        }
        context.triggerNextStep();
        return null;
    }

    protected abstract String getIdentity(TaskContext context);
}
