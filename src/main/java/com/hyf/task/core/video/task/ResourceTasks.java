package com.hyf.task.core.video.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.utils.FileUtils;
import com.hyf.task.core.utils.HttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.InputStream;

/**
 * @author baB_hyf
 * @date 2024/12/09
 */
public class ResourceTasks {

    public static VideoTask<?> createJpgTask(String url) {
        return createDownloadTask(url, "jpg");
    }

    public static VideoTask<?> createPngTask(String url) {
        return createDownloadTask(url, "png");
    }

    private static VideoTask<?> createDownloadTask(String url, String suffix) {
        return new DownloadTask(url) {
            @Override
            protected String getSuffix() {
                return suffix;
            }
        };
    }

    private static abstract class DownloadTask extends VideoDownloadTask<Void> {

        private final String url;

        public DownloadTask(String url) {
            this.url = url;
        }

        @Override
        public Void process(TaskContext context) throws Exception {

            String downloadPath = getDownloadPath(context);

            CloseableHttpResponse response = HttpClient.get(url);

            try (InputStream is = response.getEntity().getContent()) {
                FileUtils.writeFileSafely(is, downloadPath);
            }

            return null;
        }

        protected String getDownloadPath(TaskContext context) {
            String saveFilePath = TransformProductTask.getSaveFile(context).getAbsolutePath();
            String saveFilePrefix = saveFilePath.substring(0, saveFilePath.lastIndexOf("."));
            return saveFilePrefix + "." + getSuffix();
        }

        protected abstract String getSuffix();

    }
}
