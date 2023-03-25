package com.huawei.codecraft;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import com.huawei.codecraft.utils.Statistics;

public class Main {

    private static final Scanner inStream = new Scanner(new BufferedInputStream(System.in));

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    // 时间开关
    private static final boolean showTime = false;

    private static final int totalFrame = 50 * 60 * 3;
    private static final Context ctx = new Context(inStream, outStream);
    private static final Statistics statistics = new Statistics(totalFrame);

    public static void main(String[] args) throws IOException, InterruptedException {
        // 如果在本地调试时不需要重启，在启动参数中添加restart，如：java -jar main.jar restart
        if (args.length <= 0) {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("java", "-jar", "-Xmn512m", "-Xms1024m", "-Xmx1024m", "-XX:MaxGCPauseMillis=1", "main.jar",
                    "restart");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        } else if (!args[0].equals("restart")) {
            System.out.println("err");
        } else {
            ctx.init(args); // 初始化地图
            ctx.step(true); // 初始化过地图就开始计算任务链，避免跳帧

            statistics.start(); // 开始计时
            while (ctx.getFrame() < totalFrame) {
                // 更新信息
                ctx.update();
                // 输出策略
                ctx.step(false);

                if (showTime) {
                    statistics.showTime(ctx.getFrame());
                }
            }

            // 显示统计信息
            statistics.showStatic();
        }
    }
}
