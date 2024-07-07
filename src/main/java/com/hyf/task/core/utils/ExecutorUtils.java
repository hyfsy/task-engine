package com.hyf.task.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorUtils {

    public static final ThreadPoolExecutor commonExecutor =
            new ThreadPoolExecutor(0, Integer.MAX_VALUE, 3, TimeUnit.SECONDS, new SynchronousQueue<>(),
                    new ThreadFactory() {
                        final AtomicInteger i = new AtomicInteger(0);

                        @Override
                        public Thread newThread(Runnable r) {
                            Thread t = new Thread(r);
                            t.setName("task-common-thread-" + i.getAndIncrement());
                            return t;
                        }
                    });
    public static final ThreadPoolExecutor cpuExecutor    =
            new ThreadPoolExecutor(8, 8, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100000), new ThreadFactory() {
                final AtomicInteger i = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("task-compute-thread-" + i.getAndIncrement());
                    return t;
                }
            }, new ThreadPoolExecutor.CallerRunsPolicy());
    // 55 在弱网环境使用，105 在网络通畅的环境使用
    public static final ThreadPoolExecutor ioExecutor     =
            new ThreadPoolExecutor(55, 55, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100000), new ThreadFactory() {
                final AtomicInteger i = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("task-io-thread-" + i.getAndIncrement());
                    return t;
                }
            }, new ThreadPoolExecutor.CallerRunsPolicy());

    private static final LocalDateTime startTime = LocalDateTime.now();
    private static final Logger        log       = LoggerFactory.getLogger(ExecutorUtils.class);

    public static volatile boolean _computer_shutdown_when_finished = false;

    static {
        initExecutors();
        setShutdownGracefullyThread();
        startCaptureThread();
    }

    private static void initExecutors() {
        cpuExecutor.allowCoreThreadTimeOut(true);
        commonExecutor.allowCoreThreadTimeOut(true);
        ioExecutor.allowCoreThreadTimeOut(true);
    }

    private static void setShutdownGracefullyThread() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            if (_computer_shutdown_when_finished && atLeisure()) {
                computerShutdown();
            }
            else {
                try {
                    commonExecutor.shutdown();
                    if (!commonExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        commonExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    cpuExecutor.shutdown();
                    if (!cpuExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        cpuExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {
                    ioExecutor.shutdown();
                    if (!ioExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        ioExecutor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    private static void startCaptureThread() {
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                ExecutorUtils.capture();
            }
        }, "task-executor-capture");
        t.setDaemon(true);
        t.start();
    }

    public static void capture() {

        Duration duration = Duration.between(startTime, LocalDateTime.now());
        String durationText = duration.toHours() + "h" + (duration.toMinutes() % 60) + "m" + (duration.getSeconds() % 60) + "s";

        long taskCount = commonExecutor.getTaskCount() + cpuExecutor.getTaskCount() + ioExecutor.getTaskCount();
        long completedTaskCount = commonExecutor.getCompletedTaskCount() + cpuExecutor.getCompletedTaskCount() + ioExecutor.getCompletedTaskCount();

        BigDecimal ctc = new BigDecimal(String.valueOf(completedTaskCount));
        BigDecimal tc = new BigDecimal(String.valueOf(taskCount));

        String progress = BigDecimal.ZERO.compareTo(tc) == 0 ? "0" :
                ctc.multiply(new BigDecimal("100"), MathContext.DECIMAL64)
                        .divide(tc, 2, BigDecimal.ROUND_DOWN)
                        .stripTrailingZeros().toPlainString();

        // System.out.printf(durationText + " - common[q: %s, t: %s], cpu[q: %s, t: %s], io[q: %s, t: %s], stat[t: %s, f: %s, p: %s%]%n",
        //         commonExecutor.getQueue().size(), commonExecutor.getActiveCount(),
        //         cpuExecutor.getQueue().size(), cpuExecutor.getActiveCount(),
        //         ioExecutor.getQueue().size(), ioExecutor.getActiveCount(),
        //         taskCount, completedTaskCount, progress
        // );

        log.info(durationText + " - common[q: {}, t: {}], cpu[q: {}, t: {}], io[q: {}, t: {}], stat[t: {}, f: {}, p: {}%]",
                commonExecutor.getQueue().size(), commonExecutor.getActiveCount(),
                cpuExecutor.getQueue().size(), cpuExecutor.getActiveCount(),
                ioExecutor.getQueue().size(), ioExecutor.getActiveCount(),
                taskCount, completedTaskCount, progress
        );
    }

    public static boolean atLeisure() {
        long taskCount = commonExecutor.getTaskCount() + cpuExecutor.getTaskCount() + ioExecutor.getTaskCount();
        long completedTaskCount = commonExecutor.getCompletedTaskCount() + cpuExecutor.getCompletedTaskCount() + ioExecutor.getCompletedTaskCount();
        return taskCount == completedTaskCount;
    }

    private static void computerShutdown() {
        String shutdownCommand = "shutdown /s /t 10 /d u:0:0 /c 资源下载完毕自动关机中...";
        String ideaCommand = "taskkill /F /T /IM idea64.exe";

        ProcessUtils.exec(shutdownCommand);
        ProcessUtils.exec(ideaCommand);
    }
}

