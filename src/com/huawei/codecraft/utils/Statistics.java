package com.huawei.codecraft.utils;

public class Statistics {
    private long startTime;
    private long lastTime;
    private int totalFrame;

    public Statistics(int totalFrame) {
        this.totalFrame = totalFrame;
    }

    public void start() {
        startTime = System.nanoTime();
        lastTime = System.nanoTime();
    }

    /** 显示每轮的计算时间 */
    public void showTime(int frame) {
        long endTime = System.nanoTime();

        double time = (endTime - lastTime) / 1000000.0;
        if (time >= 10) {
            System.err.printf("time: [%.3f]ms at frame %d\n", time, frame);
            System.err.flush();
        }
        lastTime = endTime;
    }

    /** 显示时间统计信息 */
    public void showStatic() {
        double totalTime = (System.nanoTime() - startTime) / 1000000.0;
        double avgTime = totalTime / totalFrame;
        System.err.printf("*** total: [%.3f]ms ***\n", totalTime);
        System.err.printf("*** avg: [%.3f]ms ***\n", avgTime);
        System.err.flush();
    }
}
