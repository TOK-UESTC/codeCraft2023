package com.huawei.codecraft;

import java.io.*;
import java.util.*;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.task.Despatcher;
import com.huawei.codecraft.utils.Action;
import com.huawei.codecraft.utils.Coordinate;
import com.huawei.codecraft.utils.MagneticForce;
import com.huawei.codecraft.utils.MagneticForceModel;

public class Context {
    private Scanner inStream;
    private PrintStream outStream;
    private boolean saveLog;
    private int frameId;
    private int money;

    // log用
    private String inFilePath = "./log/input.txt";
    private String outFilePath = "./log/output.txt";
    private FileOutputStream loginStream;
    private FileOutputStream logoutStream;

    // key: 机器人ID value: 机器人对象
    private List<Robot> robotList = new ArrayList<Robot>();
    // key: 工作台ID value: 工作台对象
    private List<Workbench> workbenchList = new ArrayList<Workbench>();
    // key: 工作台类型 value: 工作台对象列表
    private Map<Integer, List<Workbench>> workbenchTypeMap = new HashMap<Integer, List<Workbench>>();

    private Despatcher despatcher;

    Context(Scanner inStream, PrintStream outStream, boolean saveLog) {
        frameId = 0;
        money = 0;

        this.saveLog = saveLog;
        this.inStream = inStream;
        this.outStream = outStream;

        // 创建输入输出日志文件
        if (saveLog) {
            initLogger();
        }
    }

    public void init() {
        int row = 0; // 地图行数
        double x, y; // 地图坐标
        int robotCount = 0; // 机器人数量
        int workbenchCount = 0; // 工作台数量

        String line;

        while (inStream.hasNextLine()) {
            line = readLine();

            // 地图数据读取完毕
            if (line.equals("OK")) {
                break;
            }

            // 坐标
            for (int col = 0; col < line.length(); col++) {
                // 第一行第一列为(0.25, 49.75)
                x = col * 0.5 + 0.25;
                y = 49.75 - row * 0.5;

                switch (line.charAt(col)) {
                    // 空地
                    case '.':
                        break;
                    // 机器人
                    case 'A':
                        Robot robot = new Robot(new Coordinate(x, y), robotCount++);
                        robotList.add(robot);
                        break;
                    // 工作台
                    default:
                        int workbenchType = line.charAt(col) - '0';
                        Workbench workbench = new Workbench(new Coordinate(x, y), workbenchType, workbenchCount++);
                        workbenchList.add(workbench);
                }

            }
            row++;
        }

        despatcher = new Despatcher(robotList, workbenchList, workbenchTypeMap);

        endStep();
    }

    // 与判题器交互，更新信息
    public void update() {
        String line;
        line = readLine();

        // 更新state
        String[] parts = line.split(" ");
        frameId = Integer.parseInt(parts[0]);
        money = Integer.parseInt(parts[1]);

        // 更新工作台信息
        int k = Integer.parseInt(readLine());
        for (int i = 0; i < k; i++) {
            line = readLine();

            // 按照顺序读取
            Workbench wb = workbenchList.get(i);
            wb.update(line.split(" "));
        }

        // 更新机器人信息
        for (int i = 0; i < 4; i++) {
            line = readLine();

            // 按照顺序读取
            Robot rb = robotList.get(i);
            rb.update(line.split(" "));
        }

        // 更新结尾异常
        if (!readLine().equals("OK")) {
            System.err.println("update failed");
        }
    }

    public void step() {
        printLine(String.format("%d", frameId));

        // 调度器分配任务
        despatcher.dispatch();

        /*
         * TODO: 机器人类封装doActiono(), 给出每个机器人该帧的动作
         */

        int lineSpeed = 3;
        double angleSpeed = 1.5;
        // Coordinate destination = new Coordinate(10, 10);
        // int intevel = 700;
        // // 绘制正方形
        // if (frameId < intevel) {
        // destination = new Coordinate(10, 38.75);
        // } else if (frameId < intevel * 2) {
        // destination = new Coordinate(30, 38.75);
        // } else if (frameId < intevel * 3) {
        // destination = new Coordinate(10, 38.75);
        // } else if (frameId < intevel * 4) {
        // destination = new Coordinate(40, 10);
        // } else if (frameId < intevel * 5) {
        // destination = new Coordinate(10, 10);
        // }
        // // 继续绘制三角形
        // else if (frameId < intevel * 6) {
        // destination = new Coordinate(10, 40);
        // } else if (frameId < intevel * 7) {
        // destination = new Coordinate(40, 10);
        // } else if (frameId < intevel * 8) {
        // destination = new Coordinate(10, 10);
        // }
        // // 绘制五角星
        // else if (frameId < intevel * 9) {
        // destination = new Coordinate(10, 40);
        // } else if (frameId < intevel * 10) {
        // destination = new Coordinate(40, 40);
        // } else if (frameId < intevel * 11) {
        // destination = new Coordinate(40, 10);
        // } else if (frameId < intevel * 12) {
        // destination = new Coordinate(10, 10);
        // } else if (frameId < intevel * 13) {
        // destination = new Coordinate(10, 40);
        // } else if (frameId < intevel * 14) {
        // destination = new Coordinate(40, 10);
        // } else if (frameId < intevel * 15) {
        // destination = new Coordinate(10, 10);
        // } else if (frameId < intevel * 16) {
        // destination = new Coordinate(40, 40);
        // } else if (frameId < intevel * 17) {
        // destination = new Coordinate(10, 10);
        // }
        for (int i = 0; i < 4; i++) {
            Robot rb = robotList.get(i);
            // 分别计算三个虚拟力
            // 计算机器人间的力
            MagneticForce magneticForce = new MagneticForce();
            for (Robot robot : robotList) {
                if (robot != rb) {
                    magneticForce = magneticForce.add(MagneticForceModel.robotMagneticForceEquation(rb, robot));
                }
            }
            // // 叠加墙体斥力
            // magneticForce =
            // magneticForce.add(MagneticForceModel.wallMagneticForceEquation(rb));
            // 叠加工作台引力
            magneticForce = magneticForce.add(MagneticForceModel.workbenchMagneticForceEquation(rb,
                    workbenchList.get(20)));
            // // 决策

            rb.step(magneticForce);

            // 打印决策
            for (Action a : rb.getActions()) {
                printLine(a.toString(i));
            }
        }

        // Robot rb = robotList.get(0);
        // // 决策
        // rb.step(destination);

        // // 打印决策
        // for (Action a : rb.getActions()) {
        // printLine(a.toString(0));
        // }

        endStep();
    }

    public int getFrame() {
        return frameId;
    }

    public int getMoney() {
        return money;
    }

    public void endStep() {
        printLine("OK");
        outStream.flush();
    }

    public void initLogger() {
        try {
            File inFile = new File(inFilePath);
            File outFile = new File(outFilePath);
            // 不存在文件，创建目录
            if (!inFile.exists()) {
                File dir = new File(inFile.getParent());
                dir.mkdirs();
            } else {
                // 文件存在，删除文件
                inFile.delete();
                outFile.delete();
            }
            // 创建新的日志文件
            inFile.createNewFile();
            outFile.createNewFile();

            loginStream = new FileOutputStream(inFile);
            logoutStream = new FileOutputStream(outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readLine() {
        String line = inStream.nextLine();
        if (saveLog) {
            try {
                loginStream.write((line + '\n').getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }

    public void printLine(String out) {
        outStream.println(out);
        if (saveLog) {
            try {
                logoutStream.write((out + '\n').getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}