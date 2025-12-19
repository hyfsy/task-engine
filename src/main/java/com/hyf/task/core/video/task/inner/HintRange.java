package com.hyf.task.core.video.task.inner;

/**
 * @author baB_hyf
 * @date 2024/06/01
 */
public class HintRange {

    int start = -1;
    int end = -1;

    public HintRange() {
    }

    public HintRange(int start) {
        this.start = start;
    }

    public HintRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
