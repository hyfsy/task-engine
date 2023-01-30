package com.hyf.task.core.video.constants;

import java.util.regex.Pattern;

/**
 * m3u8文件相关常量
 */
public interface M3U8Constants {

    /**
     * m3u8文件的后缀
     */
    String M3U8_FILE_SUFFIX_NAME = "m3u8";
    /**
     * 下载、保存的m3u8文件名
     */
    String M3U8_FILE_NAME        = "index." + M3U8_FILE_SUFFIX_NAME;
    /**
     * m3u8文件注释符
     */
    String M3U8_FILE_COMMENT     = "#";
    /**
     * ts文件解密秘钥前缀
     */
    String SECRET_KEY_PREFIX     = M3U8_FILE_COMMENT + "EXT-X-KEY:";
    /**
     * ts文件无需秘钥解密的内容标识
     */
    String SECRET_KEY_NONE       = SECRET_KEY_PREFIX + "METHOD=NONE";

    /**
     * m3u8文件的URL下载地址
     */
    String  DOWNLOAD_URL_M3U8_FILE                          = "DOWNLOAD_URL_M3U8_FILE";
    /**
     * 标记为嵌套的m3u8解析任务
     */
    String  NESTED_M3U8                                     = "NESTED_M3U8";
    /**
     * m3u8_url文件的缓存标识
     */
    String  CACHE_IDENTITY_DOWNLOAD_M3U8_FILE               = "CACHE_IDENTITY_DOWNLOAD_M3U8_FILE";
    /**
     * m3u8_url解析后的资源文件列表的缓存标识
     */
    String  CACHE_IDENTITY_DOWNLOAD_M3U8_RESOURCE_LIST_FILE = "CACHE_IDENTITY_DOWNLOAD_M3U8_RESOURCE_LIST_FILE";
    /**
     * m3u8_key regex模式匹配，默认为 {@link #SECRET_KEY_URL_PATTERN}
     */
    String  M3U8_FILE_SECRET_KEY_URL_PATTERN                = "M3U8_FILE_SECRET_KEY_URL_PATTERN";
    /**
     * 默认的m3u8_key regex模式匹配
     */
    Pattern SECRET_KEY_URL_PATTERN                          = Pattern.compile("URI=\"(.*?)\"");

}
