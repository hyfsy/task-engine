package com.hyf.task.core.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * @author baB_hyf
 * @date 2023/08/12
 */
public class ProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtils.class);

    public static int exec(String command) {
        return exec(command, ExecutorUtils.commonExecutor);
    }

    public static int exec(String command, Executor executor) {

        Runtime rt = Runtime.getRuntime();
        int extCode = -2;
        try {
            Process process = rt.exec(command);
            Runnable inputRunnable = () -> {
                try (InputStream is = process.getInputStream()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(IOUtils.readAsString(is));
                    }
                    else {
                        IOUtils.readAsString(is);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            Runnable errorRunnable = () -> {
                try (InputStream es = process.getErrorStream()) {
                    LOG.error(IOUtils.readAsString(es));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };

            executor.execute(inputRunnable);
            executor.execute(errorRunnable);

            extCode = process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return extCode;
    }
}
