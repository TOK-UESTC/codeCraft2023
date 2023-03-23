package com.huawei.codecraft;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.huawei.codecraft.utils.Statistics;

public class Main {

    private static final BufferedReader inStream = new BufferedReader(new InputStreamReader(System.in));

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    // 时间开关
    private static final boolean showTime = true;

    private static final int totalFrame = 50 * 60 * 3;
    private static final Context ctx = new Context(inStream, outStream);
    private static final Statistics statistics = new Statistics(totalFrame);

    public static void main(String[] args) throws IOException, InterruptedException {
        // 如果在本地调试时不需要重启，在启动参数中添加restart，如：java -jar main.jar restart
        if (args.length <= 0) {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("java", "-jar", "-Xmn256m", "-Xms1024m", "-Xmx1024m",
                    "-XX:TieredStopAtLevel=1", "main.jar", "restart");
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        } else if (!args[0].equals("restart")) {
            System.out.println("err");
        } else {
            ctx.init(args); // 初始化地图
            // 经验证，答题器并不会输出第0帧的信息，故可以忽略控制台输出的
            // player skipped frames: 0
            while (ctx.getFrame() < totalFrame) {
                // 更新信息
                ctx.update();
                // 输出策略
                ctx.step();

                if (showTime) {
                    statistics.showTime();
                }
            }

            // 显示统计信息
            statistics.showStatic();
        }

    }
}
