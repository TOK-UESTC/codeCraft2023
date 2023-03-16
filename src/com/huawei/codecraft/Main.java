package com.huawei.codecraft;

import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import com.huawei.codecraft.utils.Statistics;

public class Main {

    private static final Scanner inStream = new Scanner(System.in);

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    // 时间开关
    private static final boolean showTime = false;
    // 日志开关
    private static final boolean saveLog = false;
    // 任务链记录开关
    private static final boolean saveChain = false;

    private static final int totalFrame = 50 * 60 * 3;
    private static final Context ctx = new Context(inStream, outStream, saveLog, saveChain);
    private static final Statistics statistics = new Statistics(totalFrame);

    public static void main(String[] args) {
        ctx.init(); // 初始化地图
        // 经验证，答题器并不会输出第0帧的信息，故可以忽略控制台输出的
        // player skipped frames: 0
        while (ctx.getFrame() < totalFrame) {
            if(ctx.getFrame() > 1000){
                int i=0;
            }
            // 更新信息
            ctx.update();
            // 输出策略
            ctx.step();
            // try {
            //     Thread.sleep(5);
            // } catch (Exception e) {
            //     // TODO: handle exception
            // }
            if (showTime) {
                statistics.showTime();
            }
        }

        // 显示统计信息
        statistics.showStatic();
    }
}
