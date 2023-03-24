package com.huawei.codecraft;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.huawei.codecraft.action.Action;
import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.motion.MotionFrag;
import com.huawei.codecraft.motion.MotionState;
import com.huawei.codecraft.pid.PIDModel;
import com.huawei.codecraft.task.Dispatcher;
import com.huawei.codecraft.task.TaskChain;
import com.huawei.codecraft.utils.Utils;
import com.huawei.codecraft.vector.Coordinate;
import com.huawei.codecraft.vector.Velocity;

public class Context {
    private Scanner inStream;
    private PrintStream outStream;

    private int frameId;
    private int money;

    // 日志开关
    private static final boolean saveLog = false;

    // log用
    private String inFilePath = "./log/input.txt";
    private String outFilePath = "./log/output.txt";
    private FileOutputStream loginStream = null;
    private FileOutputStream logoutStream = null;

    // key: 机器人ID value: 机器人对象
    private List<Robot> robotList = new ArrayList<Robot>();
    private List<Robot> sortRobotList = new ArrayList<>();
    // key: 工作台ID value: 工作台对象
    private List<Workbench> workbenchList = new ArrayList<Workbench>();
    // key: 工作台类型 value: 工作台对象列表
    private Map<Integer, List<Workbench>> workbenchTypeMap = new HashMap<Integer, List<Workbench>>();

    private Dispatcher dispatcher;

    private ObjectPool<MotionState> statePool;
    private ObjectPool<MotionFrag> fragPool;
    private ObjectPool<Coordinate> coordPool;
    private ObjectPool<PIDModel> pidPool;
    private ObjectPool<TaskChain> chainPool;

    Context(Scanner inStream, PrintStream outStream) {
        frameId = 0;
        money = 0;

        this.inStream = inStream;
        this.outStream = outStream;

        // 创建输入输出日志文件
        if (saveLog) {
            loginStream = Utils.getFileStream(inFilePath);
            logoutStream = Utils.getFileStream(outFilePath);
        }

        // 初始化对象池，传入生成函数
        this.statePool = new ObjectPool<>(200,
                () -> new MotionState(new Coordinate(0, 0), 0, new Velocity(0, 0), 0, false));
        this.fragPool = new ObjectPool<>(200, () -> new MotionFrag(0, 0, 0));
        this.coordPool = new ObjectPool<>(200, () -> new Coordinate(0, 0));
        this.pidPool = new ObjectPool<>(200, () -> new PIDModel((Robot) null));
        this.chainPool = new ObjectPool<>(200, () -> new TaskChain(0));
    }

    /**
     * 进行初始化，读取地图
     * 
     * @throws IOException
     */
    public void init(String[] args) {
        int row = 0; // 地图行数
        double x, y; // 地图坐标
        int workbenchCount = 0; // 工作台数量
        int robotCount = 0; // 机器人数量

        String line;

        while (true) {
            line = inStream.nextLine();

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
                        Robot robot = new Robot(
                                new Coordinate(x, y),
                                robotList, args, statePool, fragPool, coordPool, pidPool, robotCount++);
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

        sortRobotList.addAll(robotList);

        for (int t = 1; t <= 9; t++) {
            if (workbenchTypeMap.get(t) == null) {
                Const.workbenchMapper.put(t, 0);
                continue;
            }
            Const.workbenchMapper.put(t, workbenchTypeMap.get(t).size());
        }

        dispatcher = new Dispatcher(robotList, workbenchList, workbenchTypeMap, chainPool);

        // 根据地图工作台情况，动态调整pid
        for (Robot rb : robotList) {
            rb.updatePID(workbenchCount);
        }
    }

    /**
     * 与判题器交互，更新信息
     * 
     * @throws IOException
     */
    public void update() throws IOException {
        String line;
        line = inStream.nextLine();

        // 更新state
        String[] parts = line.split(" ");
        frameId = Integer.parseInt(parts[0]);
        money = Integer.parseInt(parts[1]);

        // 更新工作台信息
        int k = Integer.parseInt(inStream.nextLine());
        for (int i = 0; i < k; i++) {
            line = inStream.nextLine();

            // 按照顺序读取
            Workbench wb = workbenchList.get(i);
            wb.update(line.split(" "));
        }

        // 更新机器人信息
        for (int i = 0; i < 4; i++) {
            line = inStream.nextLine();

            // 按照顺序读取
            Robot rb = robotList.get(i);
            rb.update(line.split(" "), frameId);

            // 根据买卖情况修改task
            rb.checkDeal(Const.DURATION_OF_GAME * Const.FRAME_PER_SECOND - frameId);
        }

        // 更新结尾异常
        if (!inStream.nextLine().equals("OK")) {
            System.err.println("update failed");
        }
    }

    /** 发起决策过程 */
    public void step(boolean init) {
        if (frameId == 3770) {
            int i = 0;
        }
        if (init) {
            dispatcher.dispatch();

            for (Robot rb : robotList) {
                // 预决策，热池子
                rb.step();
            }
        } else {
            printLine(String.format("%d", frameId));

            // 调度器分配任务
            dispatcher.dispatch();

            // 按照优先级进行执行
            Collections.sort(sortRobotList);
            for (Robot rb : sortRobotList) {
                // 决策
                rb.step();
            }

            for (Robot rb : robotList) {
                // 打印决策
                for (Action a : rb.getActions()) {
                    printLine(a.toString(rb.getId()));
                }
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
     */
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