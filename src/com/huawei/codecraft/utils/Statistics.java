package com.huawei.codecraft.utils;

public class Statistics {
    private long startTime;
    private long lastTime;
    private int totalFrame;

    public Statistics(int totalFrame) {
        this.totalFrame = totalFrame;

        startTime = System.nanoTime();
        lastTime = System.nanoTime();
    }

    /** 显示每轮的计算时间 */
    public void showTime() {
        long endTime = System.nanoTime();
        System.err.printf("time: [%.3f]ms\n", (endTime - lastTime) / 1000000.0);
        System.err.flush();
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