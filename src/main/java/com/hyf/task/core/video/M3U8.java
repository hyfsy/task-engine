package com.hyf.task.core.video;

import com.hyf.task.core.utils.StringUtils;

import java.util.*;

/**
 * http://mirrors.nju.edu.cn/rfc/beta/errata/rfc8216.html
 * <p>
 * http://masikkk.com/article/HLS/
 *
 * @author baB_hyf
 * @date 2023/01/31
 */
public abstract class M3U8 implements Comparable<M3U8> {

    public static List<M3U8> parse(String content) {

        List<M3U8> m3u8List = new ArrayList<>();

        String[] rows = content.split("\\n");
        for (int i = 0; i < rows.length; i++) {
            String row = rows[i];

            if (StringUtils.isBlank(row)) {
                continue;
            }

            if (i == 0) {
                if (!"#EXTM3U".equals(row)) {
                    throw new RuntimeException("m3u8 file format illegal");
                }
                continue;
            }

            // main
            if (row.startsWith("#EXT-X-STREAM-INF:")) {
                String[] params = row.substring("#EXT-X-STREAM-INF:".length()).split(",");
                Map<String, String> paramMap = new HashMap<>();
                for (String param : params) {
                    String[] kv = param.split("=");
                    paramMap.put(kv[0].trim(), kv[1].trim());
                }
                MainM3U8 mainM3U8 = new MainM3U8();
                mainM3U8.programId = Long.parseLong(paramMap.get("PROGRAM-ID"));
                mainM3U8.bandwidth = Long.parseLong(paramMap.get("BANDWIDTH"));
                mainM3U8.resolution = Resolution.valueOf(paramMap.get("RESOLUTION"));
                mainM3U8.source = rows[++i];
                m3u8List.add(mainM3U8);
                continue;
            }

            // sub
            SubM3U8 subM3U8 = new SubM3U8();
            while (i < rows.length) {
                String innerRow = rows[i];
                if (innerRow.startsWith("#EXT-X-VERSION:")) {
                    subM3U8.version = Integer.parseInt(innerRow.substring("#EXT-X-VERSION:".length()));
                }
                else if (innerRow.startsWith("#EXT-X-TARGETDURATION:")) {
                    subM3U8.targetDuration = Double.parseDouble(innerRow.substring("#EXT-X-TARGETDURATION:".length()));
                }
                else if (innerRow.startsWith("#EXT-X-MEDIA-SEQUENCE:")) {
                    subM3U8.mediaSequence = Integer.parseInt(innerRow.substring("#EXT-X-MEDIA-SEQUENCE:".length()));
                }
                else if (innerRow.startsWith("#EXT-X-PLAYLIST-TYPE:")) {
                    subM3U8.playListType = PlayListType.valueOf(innerRow.substring("#EXT-X-PLAYLIST-TYPE:".length()));
                }
                else if (innerRow.startsWith("EXT-X-KEY:")) {
                    subM3U8.key = Key.parse(innerRow.substring("#EXT-X-KEY:".length()));
                }
                else if (innerRow.startsWith("#EXTINF:")) {
                    if (subM3U8.infList == null) {
                        subM3U8.infList = new ArrayList<>();
                    }
                    Inf inf = new Inf();
                    int commaIdx = innerRow.indexOf(",");
                    if (commaIdx != -1) {
                        inf.duration = Double.parseDouble(innerRow.substring("#EXTINF:".length(), commaIdx));
                    }
                    inf.source = rows[++i];
                    subM3U8.infList.add(inf);
                }
                else if ("#EXT-X-ENDLIST".equals(row)) {
                    break;
                }
                i++;

                // // get resource
                // if (!row.startsWith("#")) {
                //     m3u8List.add(getCorrectResourceUrl(m3u8FileUrl, row));
                // }

            }

            if (subM3U8.infList != null && subM3U8.infList.isEmpty()) {
                m3u8List.add(subM3U8);
            }
        }

        Collections.sort(m3u8List);

        return m3u8List;
    }

    public enum KeyMethod {
        NONE("NONE"),
        AES_128("AES-128"),
        SAMPLE_AES("SAMPLE-AES"),
        ;
        String type;

        KeyMethod(String type) {
            this.type = type;
        }

        public static KeyMethod parse(String type) {
            for (KeyMethod value : values()) {
                if (value.type.equals(type)) {
                    return value;
                }
            }
            throw new UnsupportedOperationException("KeyMethod type: " + type);
        }
    }

    public enum PlayListType {
        VOD, // 点播
        EVENT // 直播
    }

    public static class MainM3U8 extends M3U8 { // 多码率适配
        public long       programId;
        public long       bandwidth;
        public Resolution resolution;
        public String     source;
        public SubM3U8    subM3U8;

        @Override
        public int compareTo(M3U8 o) {
            if (o instanceof SubM3U8) {
                return 1;
            }
            MainM3U8 mainM3U8 = (MainM3U8) o;
            int i = resolution.compareTo(mainM3U8.resolution);
            if (i != 0) {
                return i;
            }
            return (int) (bandwidth - mainM3U8.bandwidth);
        }
    }

    public static class SubM3U8 extends M3U8 {
        public int          version;
        public double       targetDuration;
        public int          mediaSequence;
        public PlayListType playListType;
        public Key          key;
        public List<Inf>    infList;

        @Override
        public int compareTo(M3U8 o) {
            if (o instanceof MainM3U8) {
                return -1;
            }
            return 0;
        }
    }

    public static class Inf {
        public double duration;
        public String source;
    }

    public static class Key {
        public KeyMethod method = KeyMethod.NONE;
        public String    uri;
        public String    iv;

        public static Key parse(String keyText) {
            if (StringUtils.isBlank(keyText)) {
                return new Key();
            }

            String[] params = keyText.split(",");
            Map<String, String> paramMap = new HashMap<>();
            for (String param : params) {
                String[] kv = param.split("=");
                paramMap.put(kv[0].trim(), kv[1].trim());
            }

            Key key = new Key();
            key.method = KeyMethod.parse(paramMap.get("METHOD"));
            key.uri = paramMap.get("URI");
            key.iv = paramMap.get("IV");
            return key;
        }
    }

    public static class Resolution implements Comparable<Resolution> {
        public int width;
        public int height;

        public static Resolution valueOf(String resolutionText) {
            if (StringUtils.isBlank(resolutionText)) {
                return new Resolution();
            }
            Resolution resolution = new Resolution();
            String[] wh = resolutionText.split("x");
            resolution.width = Integer.parseInt(wh[0]);
            resolution.height = Integer.parseInt(wh[1]);
            return resolution;
        }

        @Override
        public int compareTo(Resolution o) {
            return width - o.width + height - o.height;
        }
    }
}
