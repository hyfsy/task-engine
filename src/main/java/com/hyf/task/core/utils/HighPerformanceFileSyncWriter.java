package com.hyf.task.core.utils;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HighPerformanceFileSyncWriter {

    // ===================== 配置参数 =====================
    private static final int DIRECT_BUFFER_CACHE_CAPACITY = 64;   // 每个线程缓存 DirectByteBuffer 数量

    // ThreadLocal 缓存堆外内存，避免频繁分配 DirectByteBuffer
    private static final ThreadLocal<Queue<ByteBuffer>> directBufferCache = ThreadLocal.withInitial(
            () -> new ArrayDeque<>(DIRECT_BUFFER_CACHE_CAPACITY)
    );

    /**
     * 主方法：高并发写入小文件到指定绝对路径，返回时文件必须已落盘并可读
     */
    public static void writeFile(InputStream is, String fileSavePath) throws IOException {
        // Objects.requireNonNull(is, "InputStream cannot be null");
        // Objects.requireNonNull(fileSavePath, "fileSavePath cannot be null");

        byte[] data = readToByteArray(is);

        // 🔥 核心：同步写入目标路径，并强制刷盘
        writeToFileDirect(data, fileSavePath);
    }

    // -------------------- 核心写入逻辑 --------------------

    private static void writeToFileDirect(byte[] data, String fileSavePath) throws IOException {
        if (data.length == 0) return;

        Path path = Paths.get(fileSavePath);
        FileChannel channel = null;
        ByteBuffer buffer = null;

        try {
            // 创建父目录（如果不存在）
            // Files.createDirectories(path.getParent());

            // 获取或创建 DirectByteBuffer（堆外内存复用）
            buffer = takeOrCreateDirectBuffer(data.length);
            buffer.clear();
            buffer.put(data);
            buffer.flip();

            // 打开通道：创建/覆盖写入
            channel = FileChannel.open(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            // 写入数据
            channel.write(buffer);

            // 🔥 强制刷盘：保证操作系统和磁盘缓存都落盘（fsync）
            channel.force(true); // 包括文件内容和元数据（如 mtime, size）

        } finally {
            // 关闭资源
            closeQuietly(channel);
            // 归还 buffer
            if (buffer != null) {
                offerDirectBuffer(buffer);
            }
        }
    }

    // -------------------- 内存管理：堆外缓冲区复用 --------------------

    private static ByteBuffer takeOrCreateDirectBuffer(int neededSize) {
        Queue<ByteBuffer> cache = directBufferCache.get();
        ByteBuffer buf = cache.poll();
        if (buf != null && buf.capacity() >= neededSize) {
            return buf;
        } else {
            // 创建新的堆外内存（DirectByteBuffer）
            return ByteBuffer.allocateDirect(neededSize);
        }
    }

    private static void offerDirectBuffer(ByteBuffer buffer) {
        buffer.clear();
        Queue<ByteBuffer> cache = directBufferCache.get();
        if (cache.size() < DIRECT_BUFFER_CACHE_CAPACITY) {
            cache.offer(buffer);
        }
        // 超出容量则丢弃，由 JVM Cleaner 回收
    }

    // -------------------- 工具方法 --------------------

    private static byte[] readToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
        byte[] tmp = new byte[4096];
        int n;
        while ((n = is.read(tmp)) != -1) {
            buf.write(tmp, 0, n);
        }
        is.close();
        return buf.toByteArray();
    }

    private static void closeQuietly(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {}
        }
    }

}


