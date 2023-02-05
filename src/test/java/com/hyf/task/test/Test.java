package com.hyf.task.test;

import com.hyf.task.core.TaskEngine;
import com.hyf.task.core.TaskInstruction;
import com.hyf.task.core.TaskPipeline;
import com.hyf.task.core.video.Env;
import com.hyf.task.core.video.task.*;

import java.util.UUID;

import static com.hyf.task.core.video.constants.M3U8Constants.DOWNLOAD_URL_M3U8_FILE;
import static com.hyf.task.core.video.constants.VideoConstants.*;

/**
 * @author baB_hyf
 * @date 2023/01/30
 */
public class Test {

    public static void main(String[] args) throws Exception {

        Env.setVideoDownloadPath("C:\\Users\\baB_hyf\\Desktop");

        // mp4Task();
        // m3u8Task();
    }

    private static void mp4Task() {

        String url = "https://baikevideo.cdn.bcebos.com/media/mda-Ofj5DOML8EqV0Umy/da4f175769bcf693d0d0e205e1546015.mp4";

        TaskPipeline pipeline = new TaskPipeline()
                .add(new DownloadResourceTask())
                .add(new TransformProductTask())
                .add(new CleanTask());

        TaskEngine videoEngine = new TaskEngine(pipeline);

        TaskInstruction instruction = new TaskInstruction();

        instruction.putAttribute(VIDEO_ID, UUID.randomUUID().toString());
        instruction.putAttribute(VIDEO_NAME, "xxx");
        instruction.putAttribute(VIDEO_SAVE_PATH, DEFAULT_VIDEO_SAVE_PATH);
        instruction.putAttribute(VIDEO_SITE_TYPE, "test");
        instruction.putAttribute(DOWNLOAD_RESOURCE_URL, url);

        videoEngine.submit(instruction);
    }

    private static void m3u8Task() {

        String url = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8";

        TaskPipeline pipeline = new TaskPipeline()
                .add(new DownloadM3U8FileTask())
                .add(new DownloadResourceTaskDispatcher())
                .add(new TransformM3U8FileTask())
                .add(new MergeTSResourceTask())
                .add(new TransformProductTask())
                .add(new CleanTask());

        TaskEngine videoEngine = new TaskEngine(pipeline);

        TaskInstruction instruction = new TaskInstruction();

        instruction.putAttribute(VIDEO_ID, UUID.randomUUID().toString());
        instruction.putAttribute(VIDEO_NAME, "xxx2");
        instruction.putAttribute(VIDEO_SAVE_PATH, DEFAULT_VIDEO_SAVE_PATH);
        instruction.putAttribute(VIDEO_SITE_TYPE, "test");
        instruction.putAttribute(DOWNLOAD_URL_M3U8_FILE, url);

        videoEngine.submit(instruction);
    }
}
