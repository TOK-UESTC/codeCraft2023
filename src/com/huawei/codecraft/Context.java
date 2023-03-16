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
import com.huawei.codecraft.action.Force;
import com.huawei.codecraft.action.ForceModel;
import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.task.Dispatcher;
import com.huawei.codecraft.task.Task;
import com.huawei.codecraft.task.TaskChain;
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
        // 下一帧开始前判断上一帧买卖是否成功
        for(Robot rb:robotList){
            // 没有任务就没有买卖
            if(rb.getTask() == null ){
                continue;
            }
            // 检查买卖成功
            // 买成功
            if(rb.getTask().getFrom().getType() == rb.getProductType()){
                rb.getTask().getFrom().setInTaskChain(false);
            }
            // 卖成功
            if(!rb.getTask().getFrom().isInTaskChain() && rb.getProductType() != rb.getTask().getFrom().getType()){
                rb.getTask().getTo().setInTaskChain(false);
                rb.getTaskChain().getTasks().remove(0);
                if(rb.getTaskChain().getTasks().size() > 0){
                    rb.setTask(rb.getTaskChain().getTasks().get(0));
                }else{
                    rb.setTask(null);
                    continue;
                }

            }
            // 如果任务链检查不通过，那么放弃任务链，假设在没有
            boolean isBlock = false;
            for(Task t:rb.getTaskChain().getTasks()){
                if(t.getTo().hasMaterial(t.getFrom().getType())){
                    isBlock = true;
                    break;
                }
            }
            if(isBlock){
                // 当前任务链阻塞
                // 如果没携带东西,丢弃任务
                for(Task t:rb.getTaskChain().getTasks()){
                    if(t!=rb.getTaskChain().getTasks().get(0)){
                        t.getFrom().setInTaskChain(false);
                    }
                    t.getTo().setInTaskChain(false);
                }
                if(rb.getProductType() == 0){
                    rb.setTask(null);
                }else{
                    // 找一个最近的空闲的送出去

                    double minDistance = 1000;
                    Workbench to = null;
                    for(Workbench wb:workbenchTypeMap.get(rb.getTask().getTo().getType())){
                        if(wb == rb.getTask().getTo() || wb.isInTaskChain()){
                            continue;
                        }
                        double dis = Utils.computeDistance(rb.getPos(), wb.getPos());
                        to = minDistance > dis?wb:to;
                        minDistance = minDistance > dis?dis:minDistance;
                    }
                    if(to == null){
                        to = rb.getTask().getTo();
                    }
                    to.setInTaskChain(true);                        
                    Task temp= new Task(rb.getTask().getFrom(), to);
                    TaskChain tc = new TaskChain(rb, 0.1);
                    tc.addTask(temp);
                    rb.bindChain(tc);

                }
            }
        }
        printLine(String.format("%d", frameId));

        // 调度器分配任务
        dispatcher.dispatch();

        for (int i = 0; i < 4; i++) {
            Robot rb = robotList.get(i);
            if(rb.getTask() == null){
                continue;
            }

            // 获取合力
            Force force = ForceModel.getForce(rb, robotList, workbenchList);

            // 决策
            rb.step(force);

            // 打印决策
            for (Action a : rb.getActions()) {
                printLine(a.toString(i));
            }
        }
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