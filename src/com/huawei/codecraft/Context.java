package com.huawei.codecraft;

import java.io.*;
import java.util.*;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.utils.Coordinate;

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

    Context(Scanner inStream, PrintStream outStream, boolean saveLog) {
        frameId = 0;
        money = 0;

        this.saveLog = saveLog;
        this.inStream = inStream;
        this.outStream = outStream;

        // 创建输入输出日志文件
        if (saveLog) {
            initLog();
        }
    }

    public void init() {
        int row = 0; // 地图行数
        double x, y; // 地图坐标
        int robotCount = 0;
        int workbenchCount = 0;

        String line;

        while (inStream.hasNextLine()) {
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

        endStep();
    }

    // 与判题器交互，更新信息
    public void update() {
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
            rb.update(line.split(" "));
        }

        // 更新结尾异常
        if (!inStream.nextLine().equals("OK")) {
            System.err.println("update failed");
        }
    }

    public void step() {
        outStream.printf("%d\n", frameId);
        int lineSpeed = 3;
        double angleSpeed = 1.5;

        for (int i = 0; i < 4; i++) {
            outStream.printf("forward %d %d\n", i, lineSpeed);
            outStream.printf("rotate %d %f\n", i, angleSpeed);
        }

        endStep();
    }

    public int getFrame() {
        return frameId;
    }

    public void endStep() {
        outStream.println("OK");
        outStream.flush();
    }

    public void initLog() {
        try {
            File inFile = new File(inFilePath);
            File outFile = new File(outFilePath);
            // 不存在文件，创建目录
            if (!inFile.exists()) {
                File dir = new File(inFile.getParent());
                dir.mkdirs();
                inFile.createNewFile();
                outFile.createNewFile();
            }

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
                loginStream.write(line.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }
}