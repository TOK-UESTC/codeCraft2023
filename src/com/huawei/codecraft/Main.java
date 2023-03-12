package com.huawei.codecraft;

import java.io.*;
import java.util.*;

public class Main {

    private static final Scanner inStream = new Scanner(System.in);

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    private static final boolean showTime = false;
    private static final boolean saveLog = false;

    private static final Context ctx = new Context(inStream, outStream, saveLog);

    public static void main(String[] args) {
        ctx.init();

        int totalFrame = 50 * 60 * 3;
        long startTime = System.nanoTime();
        long lastTime = System.nanoTime();

        // 经验证，答题器并不会输出第0帧的信息，故可以忽略控制台输出的
        // player skipped frames: 0
        while (ctx.getFrame() < totalFrame) {
            ctx.update();

            ctx.step();

            if (showTime) {
                long endTime = System.nanoTime();
                System.err.printf("time: [%.3f]ms\n", (endTime - lastTime) / 1000000.0);
                System.err.flush();
                lastTime = endTime;
            }
        }

        double totalTime = (System.nanoTime() - startTime) / 1000000.0;
        double avgTime = totalTime / totalFrame;
        System.err.printf("* total: [%.3f]ms\n", totalTime);
        System.err.printf("* avg: [%.3f]ms\n", avgTime);
        System.err.flush();
    }
}
