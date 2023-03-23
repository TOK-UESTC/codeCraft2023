package com.huawei.codecraft;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.huawei.codecraft.action.Action;
import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.task.Dispatcher;
import com.huawei.codecraft.utils.Utils;
import com.huawei.codecraft.vector.Coordinate;

public class Context {
    private BufferedReader inStream;
    private PrintStream outStream;

    private int frameId;
    private int money;

    // 日志开关
    private static final boolean saveLog = false;
    // 任务链记录开关
    private static final boolean saveChain = false;

    // log用
    private String inFilePath = "./log/input.txt";
    private String outFilePath = "./log/output.txt";
    private FileOutputStream loginStream = null;
    private FileOutputStream logoutStream = null;

    // key: 机器人ID value: 机器人对象
    private List<Robot> robotList = new ArrayList<Robot>();
    // key: 工作台ID value: 工作台对象
    private List<Workbench> workbenchList = new ArrayList<Workbench>();
    // key: 工作台类型 value: 工作台对象列表
    private Map<Integer, List<Workbench>> workbenchTypeMap = new HashMap<Integer, List<Workbench>>();

    private Dispatcher dispatcher;

    Context(BufferedReader inStream, PrintStream outStream) {
        frameId = 0;
        money = 0;

        this.inStream = inStream;
        this.outStream = outStream;

        // 创建输入输出日志文件
        if (saveLog) {
            loginStream = Utils.getFileStream(inFilePath);
            logoutStream = Utils.getFileStream(outFilePath);
        }
    }

    /**
     * 进行初始化，读取地图
     * 
     * @throws IOException
     */
    public void init(String[] args) throws IOException {
        int row = 0; // 地图行数
        double x, y; // 地图坐标
        int workbenchCount = 0; // 工作台数量

        String line;

        while ((line = inStream.readLine()) != null) {
            // line = readLine();

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
                        Robot robot = new Robot(new Coordinate(x, y), robotList, args);
                        robotList.add(robot);
                        break;
                    // 工作台
                    default:
                        int workbenchType = line.charAt(col) - '0';
                        Workbench workbench = new Workbench(new Coordinate(x, y), workbenchType, workbenchCount++);
                        workbenchList.add(workbench);

                        // 将同一型号的工作台放置到map中
                        if (workbenchTypeMap.containsKey(workbenchType)) {
                            workbenchTypeMap.get(workbenchType).add(workbench);
                        } else {
                            ArrayList<Workbench> storage = new ArrayList<>();
                            storage.add(workbench);
                            workbenchTypeMap.put(workbenchType, storage);
                        }
                }
            }
            row++;
        }

        dispatcher = new Dispatcher(robotList, workbenchList, workbenchTypeMap, saveChain);
        endStep();
    }

    /**
     * 与判题器交互，更新信息
     * 
     * @throws IOException
     */
    public void update() throws IOException {
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
            rb.update(line.split(" "), frameId);

            // 根据买卖情况修改task
            rb.checkDeal(Const.DURATION_OF_GAME * Const.FRAME_PER_SECOND - frameId);
        }

        // 更新结尾异常
        if (!readLine().equals("OK")) {
            System.err.println("update failed");
        }
    }

    /** 发起决策过程 */
    public void step() {
        if (frameId == 1675) {
            int i = 0;
        }

        printLine(String.format("%d", frameId));

        // 调度器分配任务
        dispatcher.dispatch();

        List<Robot> rbList = new ArrayList<Robot>();
        rbList.addAll(robotList);
        Collections.sort(rbList, new Comparator<Robot>() {
            @Override
            public int compare(Robot r1, Robot r2) {
                return Double.compare(r1.getPriority(), r2.getPriority());
            }
        });
        // for (Robot r : rbList) {
        // r.clear();
        // }
        for (int i = 0; i < 4; i++) {
            Robot rb = rbList.get(i);

            // 决策
            rb.step();

            // 打印决策
            for (Action a : rb.getActions()) {
                printLine(a.toString(rb.getId()));
            }
        }
        endStep();
    }

    /** 获取当前frame */
    public int getFrame() {
        return frameId;
    }

    public int getMoney() {
        return money;
    }

    /** 指令结束，发送OK */
    public void endStep() {
        printLine("OK");
        outStream.flush();
    }

    /**
     * readline包装，方便log
     * 
     * @throws IOException
     */
    public String readLine() throws IOException {
        String line = inStream.readLine();
        if (saveLog) {
            try {
                loginStream.write((line + '\n').getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }

    /** printline包装，方便log */
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