package com.hyf.task.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskInstruction {

    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    public void putAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public TaskInstruction copy() {
        TaskInstruction instruction = new TaskInstruction();
        instruction.setAttributes(new HashMap<>(instruction.getAttributes()));
        return instruction;
    }

    @Override
    public String toString() {
        return "TaskInstruction{" +
                "attributes=" + attributes +
                '}';
    }
}
