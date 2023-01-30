package com.hyf.task.core.task;

import com.hyf.task.core.TaskContext;
import com.hyf.task.core.video.constants.VideoConstants;
import com.hyf.task.core.utils.ExecuteUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;

/**
 * 组合多个任务，可达到所有任务执行完毕后再进行后续任务的能力
 *
 * @param <R> 任务返回结果
 */
public class CompositeTask<R> extends CommonTask<R> {

    private static final ConcurrentHashMap<String, CompositeResult<?>> processResultTable = new ConcurrentHashMap<>();

    public final  List<CompositeSubTask<R>> tasks;
    private final BinaryOperator<R>         mergeResultFunction;
    private final BiConsumer<R, Throwable>  callback;

    public CompositeTask(List<CompositeSubTask<R>> tasks, BinaryOperator<R> mergeResultFunction, BiConsumer<R, Throwable> callback) {
        this.tasks = tasks;
        this.mergeResultFunction = mergeResultFunction;
        this.callback = callback;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R process(TaskContext context) throws Exception {

        CompositeResult<R> compositeResult = (CompositeResult<R>) processResultTable.compute(getTaskId(), (id, result) -> {
            if (result == null) {
                result = new CompositeResult<>(getTaskId(), tasks.size(), mergeResultFunction, callback);
            }
            return result;
        });

        tasks.forEach(task -> task.getContext().fork(new CompositeResultSubTask<>(task, compositeResult)));
        return null;
    }

    @Override
    public ExecutorService getExecutor() {
        // return tasks.size() > 0 ? tasks.get(0).getExecutor() : super.getExecutor();
        return ExecuteUtils.commonExecutor;
    }

    public static class CompositeSubTask<R> extends CommonTask<R> {

        private final Task<R>     delegate;
        private final TaskContext context; // 外部的context仅用于触发fork

        public CompositeSubTask(Task<R> delegate, TaskContext context) {
            this.delegate = delegate;
            this.context = context;
        }

        @Override
        public R process(TaskContext context) throws Exception {
            return delegate.process(context);
        }

        @Override
        public ExecutorService getExecutor() {
            return delegate.getExecutor();
        }

        public TaskContext getContext() {
            return context;
        }
    }

    private static class CompositeResult<R> {

        private final String taskId;

        private final int           allTaskCount;
        private final AtomicInteger remainTaskCount;

        private final BlockingQueue<R>         resultQueue = new LinkedBlockingQueue<>();
        private final BinaryOperator<R>        resultMergeFunction;
        private final BiConsumer<R, Throwable> callback;

        private volatile R         result;
        private volatile Throwable t;

        public CompositeResult(String taskId, int size, BinaryOperator<R> mergeResultFunction, BiConsumer<R, Throwable> callback) {
            this.taskId = taskId;
            this.allTaskCount = size;
            this.remainTaskCount = new AtomicInteger(size);
            this.resultMergeFunction = mergeResultFunction;
            this.callback = callback;
        }

        public int succeed(R result) {

            synchronized (resultQueue) {
                if (this.result == null) {
                    this.result = result;
                }
                else {
                    this.result = resultMergeFunction.apply(this.result, result);
                }
            }

            int i = remainTaskCount.decrementAndGet();
            if (i == 0) {
                try {
                    callback.accept(this.result, this.t);
                } finally {
                    processResultTable.remove(taskId);
                }
            }
            return i;
        }

        public void fail(Throwable t) {
            this.t = t;
            try {
                callback.accept(null, t);
            } finally {
                processResultTable.remove(taskId); // TODO ? 是否需要可观测功能
            }
        }

        public String getDebugString() {
            long allCount = getAllTaskCount();
            long remainCount = getRemainTaskCount();
            long finishedCount = allCount - remainCount;
            return "==> download progress: 【" + (finishedCount * 100 / allCount) + "%】, total: 【" + allCount + "】, remain: 【" + remainCount + "】, finished: 【" + finishedCount + "】";
        }

        public int getRemainTaskCount() {
            return remainTaskCount.get();
        }

        public int getAllTaskCount() {
            return allTaskCount;
        }
    }

    private static class CompositeResultSubTask<R> extends CommonTask<Void> {

        private final Task<R>            originTask;
        private final CompositeResult<R> compositeResult;

        public CompositeResultSubTask(Task<R> originTask, CompositeResult<R> compositeResult) {
            this.originTask = originTask;
            this.compositeResult = compositeResult;
            setNextStep(originTask.getNextStep());
        }

        @Override
        public Void process(TaskContext context) throws Exception {

            int retryTime = context.getAttribute(VideoConstants.DOWNLOAD_RESOURCE_RETRY_TIME, 3);

            R result = null;
            Throwable t = null;

            boolean finished = false;
            try {
                do {
                    try {
                        result = originTask.process(context);
                        finished = true;
                    } catch (IOException e) { // only io exception retry
                        t = e;
                    }
                } while (!finished && retryTime-- > 0);

            } catch (Throwable ex) {
                t = ex;
            } finally {
                if (finished) {
                    compositeResult.succeed(result);
                }
                else {
                    compositeResult.fail(t);
                }
            }

            return null;
        }

        @Override
        public ExecutorService getExecutor() {
            return originTask.getExecutor();
        }
    }
}
