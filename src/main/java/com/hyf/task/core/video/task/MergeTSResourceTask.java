package com.hyf.task.core.video.task;

import com.hyf.hotrefresh.common.util.IOUtils;
import com.hyf.task.core.TaskContext;
import com.hyf.task.core.exception.TaskException;
import com.hyf.task.core.utils.FileUtils;
import com.hyf.task.core.video.constants.M3U8Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 多个TS文件合并的任务
 */
public class MergeTSResourceTask extends VideoComputeTask<File> {

    public static final String OUTPUT_FILE_NAME = "000000_OUTPUT.mp4";

    // ffmpeg -allowed_extensions ALL -protocol_whitelist file,http,crypto,tcp -i index.m3u8 -c copy 000000_OUTPUT.mp4

    @Override
    public File process(TaskContext context) throws Exception {

        String downloadResourcePath = DownloadResourceTask.getDownloadResourcePath(context);

        File saveFile = new File(downloadResourcePath, OUTPUT_FILE_NAME);
        if (saveFile.exists()) {
            context.triggerNextStep();
            return saveFile;
        }

        File tempFile = new File(FileUtils.getTempFile(saveFile.getAbsolutePath()) + ".mp4");
        if (tempFile.exists()) {
            if (!tempFile.delete()) {
                throw new RuntimeException("Failed to delete output mp4 temp file");
            }
        }

        // need use high version ffmpeg, otherwise it will take lots of memory, e.g. v5.0.1
        String decodeCommand = "ffmpeg -allowed_extensions ALL -protocol_whitelist file,http,crypto,tcp -i " + M3U8Constants.M3U8_FILE_NAME + " -c copy " + tempFile.getName();

        try {

            int extCode = -2;

            try {

                if (log.isDebugEnabled()) {
                    log.debug("==> start to decode video, videoId: " + getVideoId(context));
                }

                Process process = Runtime.getRuntime().exec(decodeCommand, null, new File(downloadResourcePath));

                handleOutputStream(context, process);

                extCode = process.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            if (extCode != 0) {
                throw new RuntimeException("ffmpeg process failed, extCode: " + extCode);
            }
            else {
                if (!tempFile.renameTo(saveFile)) {
                    throw new IOException("Failed to rename file: " + tempFile.getAbsolutePath());
                }
            }

        } catch (Exception e) {
            throw new TaskException("Failed to run ffmpeg command", e);
        }

        context.triggerNextStep();
        return saveFile;
    }

    public void handleOutputStream(TaskContext context, Process process) {
        context.submit(() -> {
            try (InputStream is = process.getInputStream()) {
                if (log.isDebugEnabled()) {
                    log.debug(IOUtils.readAsString(is));
                }
                else {
                    IOUtils.readAsString(is);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        context.submit(() -> {
            try (InputStream es = process.getErrorStream()) {
                log.error(IOUtils.readAsString(es));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
