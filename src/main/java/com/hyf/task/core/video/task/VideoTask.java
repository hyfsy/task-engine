package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.task.Task;
import com.hyf.task.core.video.constants.VideoConstants;

/**
 * @author baB_hyf
 * @date 2023/02/04
 */
public abstract class VideoTask<R> extends Task<R> {

    public String getVideoId(TaskContext context) {
        return context.getAttribute(VideoConstants.VIDEO_ID);
    }

    public void setVideoId(TaskContext context, String videoId) {
        context.putAttribute(VideoConstants.VIDEO_ID, videoId);
    }

    public String getVideoName(TaskContext context) {
        return context.getAttribute(VideoConstants.VIDEO_NAME);
    }

    public void setVideoName(TaskContext context, String videoName) {
        context.putAttribute(VideoConstants.VIDEO_NAME, videoName);
    }

    public String getVideoSiteType(TaskContext context) {
        return context.getAttribute(VideoConstants.VIDEO_SITE_TYPE);
    }

    public void setVideoSiteType(TaskContext context, String videoSiteType) {
        context.putAttribute(VideoConstants.VIDEO_SITE_TYPE, videoSiteType);
    }

    public String getVideoSavePath(TaskContext context) {
        return context.getAttribute(VideoConstants.VIDEO_SAVE_PATH);
    }

    public void setVideoSavePath(TaskContext context, String videoSavePath) {
        context.putAttribute(VideoConstants.VIDEO_SAVE_PATH, videoSavePath);
    }

}
