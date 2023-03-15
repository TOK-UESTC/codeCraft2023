package com.huawei.codecraft;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.huawei.codecraft.action.Action;
import com.huawei.codecraft.action.MagneticForce;
import com.huawei.codecraft.action.MagneticForceModel;
import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.ActionType;
import com.huawei.codecraft.task.Dispatcher;
import com.huawei.codecraft.utils.Coordinate;
import com.huawei.codecraft.utils.Utils;

public class Context {
    private Scanner inStream;
    private PrintStream outStream;
    private boolean saveLog;
    private boolean saveChain;

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

    private Dispatcher dispatcher;

    Context(Scanner inStream, PrintStream outStream, boolean saveLog, boolean saveChain) {
        frameId = 0;
        money = 0;

        this.saveLog = saveLog;
        this.inStream = inStream;
        this.outStream = outStream;

        // 创建输入输出日志文件
        if (saveLog) {
            loginStream = Utils.getFileStream(inFilePath);
            logoutStream = Utils.getFileStream(outFilePath);
        }
    }

    /** 进行初始化，读取地图 */
    public void init() {
        int row = 0; // 地图行数
        double x, y; // 地图坐标
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
                        Robot robot = new Robot(new Coordinate(x, y));
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

    /** 与判题器交互，更新信息 */
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

    /** 发起决策过程 */
    public void step() {
        printLine(String.format("%d", frameId));

        // 调度器分配任务
        dispatcher.dispatch();

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

            Workbench wb;
            if (rb.getTask() == null) {
                continue;
            }
            if (rb.getProductType() == 0) {
                wb = rb.getTask().getFrom();
                // 判断是否在目标工作台附近
                if (rb.getWorkbenchIdx() == wb.getWorkbenchIdx()) {
                    // 购买行为
                    // rb.getActions().add(new Action(ActionType.BUY));
                    rb.addAction(new Action(ActionType.BUY));
                }
            } else {
                wb = rb.getTask().getTo();
                if (rb.getWorkbenchIdx() == wb.getWorkbenchIdx()) {
                    // 售出行为
                    // rb.getActions().add(new Action(ActionType.SELL));
                    rb.addAction(new Action(ActionType.SELL));
                    // 如果有后续任务链，进行购买
                    wb.setInTaskChain(false);
                    rb.getTaskChain().getTasks().remove(0);
                    // 判断是否存在后续任务
                    if (rb.getTaskChain().getTasks().size() > 0) {
                        // 设置任务，进行购买
                        rb.setTask(rb.getTaskChain().getTasks().get(0));
                        // rb.getActions().add(new Action(ActionType.BUY));
                        rb.addAction(new Action(ActionType.BUY));
                    } else {
                        // 任务链完成，清空任务链
                        rb.setTask(null);
                    }
                }
            }
            magneticForce = magneticForce.add(MagneticForceModel.workbenchMagneticForceEquation(rb,
                    wb));
            // 机器人根据虚拟力进行动作控制
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

    /** 获取当前frame */
    public int getFrame() {
        return frameId;
    }

    /** 获取当前资金 */
    public int getMoney() {
        return money;
    }

    /** 指令结束，发送OK */
    public void endStep() {
        printLine("OK");
        outStream.flush();
    }

    /** readline包装，方便log */
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