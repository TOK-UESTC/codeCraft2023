package com.huawei.codecraft;

import java.io.*;
import java.util.*;

import com.huawei.codecraft.utils.MyTimer;

public class Main {

    private static final Scanner inStream = new Scanner(System.in);

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    // 时间开关
    private static final boolean showTime = false;
    // 日志开关
    private static final boolean saveLog = false;

    private static final int totalFrame = 50 * 60 * 3;
    private static final Context ctx = new Context(inStream, outStream, saveLog);
    private static final MyTimer timer = new MyTimer(totalFrame);

    public static void main(String[] args) {
        ctx.init();

        // 经验证，答题器并不会输出第0帧的信息，故可以忽略控制台输出的
        // player skipped frames: 0
        while (ctx.getFrame() < totalFrame) {
            // 更新信息
            ctx.update();
            // 输出策略
            ctx.step();

            if (showTime) {
                timer.showTime();
            }
        }

        timer.showStatic();
    }
}
