package com.hyf.task.core.video.constants;

public interface VideoConstants {

    // basic

    /**
     * 打印视频下载的全链路日志
     */
    String DEBUG                            = "DEBUG"; // TODO log print 全下载链路日志追踪
    /**
     * 默认的视频保存路径值的系统属性key
     */
    String VIDEO_DOWNLOAD_PATH_PROPERTY_KEY = "video.download.path";
    /**
     * 默认的视频保存路径值
     */
    String DEFAULT_VIDEO_SAVE_PATH          = System.getProperty(VIDEO_DOWNLOAD_PATH_PROPERTY_KEY, "F:\\video");

    // metadata

    /**
     * 视频ID
     */
    String VIDEO_ID        = "VIDEO_ID"; // required
    /**
     * 视频名称
     */
    String VIDEO_NAME      = "VIDEO_NAME"; // runtime get
    /**
     * 视频保存路径
     */
    String VIDEO_SAVE_PATH = "VIDEO_SAVE_PATH"; // required
    /**
     * 视频网站类型
     */
    String VIDEO_SITE_TYPE = "VIDEO_SITE_TYPE"; // identity

    // other

    /**
     * 下载资源的URL地址
     */
    String DOWNLOAD_RESOURCE_URL        = "DOWNLOAD_RESOURCE_URL";
    /**
     * 下载资源的图片URL下载路径
     */
    String DOWNLOAD_RESOURCE_IMAGE_URL  = "DOWNLOAD_RESOURCE_IMAGE_URL";
    /**
     * 下载资源的保存路径
     */
    String DOWNLOAD_RESOURCE_PATH       = "DOWNLOAD_RESOURCE_PATH";

    /**
     * 视频网站的域名
     */
    String VIDEO_SITE_DOMAIN = "VIDEO_SITE_DOMAIN";
    /**
     * 视频最终保存的文件名称
     */
    String VIDEO_SAVE_NAME   = "VIDEO_SAVE_NAME";
}
