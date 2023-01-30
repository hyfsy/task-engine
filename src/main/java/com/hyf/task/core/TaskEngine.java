package com.hyf.task.core;

import com.hyf.task.core.exception.DefaultTaskFailureHandler;
import com.hyf.task.core.exception.TaskException;
import com.hyf.task.core.exception.TaskFailureHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskEngine {

    private static final Logger log = LoggerFactory.getLogger(TaskEngine.class);

    private final TaskPipeline       pipeline;
    private       TaskFailureHandler failureHandler;

    public TaskEngine(TaskPipeline pipeline) {
        this.pipeline = pipeline;
        this.failureHandler = DefaultTaskFailureHandler.INSTANCE;
    }

    public void setFailureHandler(TaskFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    public void submit(TaskInstruction instruction) throws TaskException {

        TaskContext context = new TaskContext(instruction.getAttributes());
        context.setEngine(this);
        context.setPipeline(pipeline);
        context.setFailureHandler(failureHandler);

        try {
            if (log.isDebugEnabled()) {
                log.debug("==> submit instruction: " + instruction);
            }
            pipeline.triggerFirstStep(context);
        } catch (Exception e) {
            throw new TaskException("Failed to submit instruction", e);
        }
    }
}
