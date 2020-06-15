package com.cyndre.huecenter.time;

import java.util.Objects;

public class TimeData implements TimeDataView {
    private long startTime;
    private long frameLength;
    private long frame;
    private long now;

    private static final long DEFAULT_START_TIME = 0L;

    public TimeData() {
        this(DEFAULT_START_TIME);
    }

    public TimeData(long startTime) {
        this(startTime, startTime);
    }

    public TimeData(long startTime, long now) {
        this.startTime = startTime;
        this.now = now;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getFrameLength() {
        return frameLength;
    }

    public void setFrameLength(long frameLength) {
        this.frameLength = frameLength;
    }

    @Override
    public long getFrame() {
        return frame;
    }

    public void setFrame(long frame) {
        this.frame = frame;
    }

    @Override
    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeData timeData = (TimeData) o;
        return startTime == timeData.startTime &&
                frameLength == timeData.frameLength &&
                frame == timeData.frame &&
                now == timeData.now;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, frameLength, frame, now);
    }

    @Override
    public String toString() {
        return "{" +
                "  \"startTime\": " + startTime +
                ", \"frameLength\":" + frameLength +
                ", \"frame\":" + frame +
                ", \"now\":" + now +
                '}';
    }
}
