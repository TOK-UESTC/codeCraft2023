package com.huawei.codecraft;

import java.io.*;
import java.util.*;
import java.util.concurrent.Exchanger;

public class Main {

    private static final Scanner inStream = new Scanner(System.in);

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out));

    // key: 机器人ID value: 机器人对象
    private static Map<Integer, Robot> robotMap = new HashMap<Integer, Robot>();
    // key: 工作台ID value: 工作台对象
    private static Map<Integer, Workbench> workbenchMap = new HashMap<Integer, Workbench>();
    // key: 工作台类型 value: 工作台对象列表
    private static Map<Integer, List<Workbench>> workbenchTypeMap = new HashMap<Integer, List<Workbench>>();

    public static void main(String[] args) {
        // outStream.println("[INFO] 12343124");
        schedule();
    }

    private static void schedule() {
        readUtilOK(true);
        outStream.println("OK");
        outStream.flush();

        int frameID;
        while (true) {
            System.out.println("12312");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // while (inStream.hasNextLine()) {
        // String line = inStream.nextLine();
        // String[] parts = line.split(" ");
        // frameID = Integer.parseInt(parts[0]);
        // readUtilOK(false);

        // outStream.printf("%d\n", frameID);
        // int lineSpeed = 3;
        // double angleSpeed = 1.5;

        // for (int robotId = 0; robotId < 4; robotId++) {
        // outStream.printf("forward %d %d\n", robotId, lineSpeed);
        // outStream.printf("rotate %d %f\n", robotId, angleSpeed);
        // }
        // outStream.print("OK\n");
        // outStream.flush();
        // }
    }

    private static boolean readUtilOK(boolean init) {
        String line;
        int row = 100; // 地图行数
        int robotId = 0; // 机器人ID
        int workbenchId = 0; // 工作台ID
        int K = -1; // 工作台数目
        while (inStream.hasNextLine()) {
            line = inStream.nextLine();
            if ("OK".equals(line)) {
                if (init) {
                    // 收集完地图数据，下面计算交互过程中复用的数据
                    Utils.initWorkbench(workbenchMap, workbenchTypeMap);
                }
                return true;
            }
            if (init) {
                // do something;
                double x; // 位置x坐标, 以地图参照系为准
                double y; // 位置y坐标, 以地图参照系为准

                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == '.') { // 空地
                        continue;
                    }

                    x = i * 0.5 + 0.25;
                    y = row * 0.5 - 0.25;
                    if (line.charAt(i) == 'A') { // 该位置是机器人
                        Robot robot = new Robot(new Coordinate(x, y), robotId);
                        robotMap.put(robotId, robot);
                        robotId = robotId + 1;
                        continue;
                    }

                    // 位置是工作台
                    int workbenchType = line.charAt(i) - '0';
                    Workbench workbench = new Workbench(workbenchType, new Coordinate(x, y), workbenchId);
                    workbenchMap.put(workbenchId, workbench);
                    workbenchId = workbenchId + 1;
                    try {
                        workbenchTypeMap.get(workbenchType).add(workbench);
                    } catch (Exception e) {
                        workbenchTypeMap.put(workbenchType, new LinkedList<Workbench>());
                        workbenchTypeMap.get(workbenchType).add(workbench);
                    }

                }
                row = row - 1;
            } else {
                // 判题器与程序交互阶段
                if (K == -1) { // 读取工作台个数
                    K = Integer.parseInt(line);
                    continue;
                }
                if (K > 0) { // 读取工作台信息
                    String[] parts = line.split(" ");
                    // 下面更新工作台信息
                    Workbench wb = workbenchMap.get(workbenchId);
                    // wb.setType(Integer.parseInt(parts[0])); // 解析工作台类别 工作台类型 [1-9]
                    // wb.setPos(new Coordinate(Double.parseDouble(parts[1]),Double.parseDouble(parts[2]))); // 解析工作台位置
                    wb.setRest(Integer.parseInt(parts[3])); // 解析生产剩余时间 -1表示没有生产；0表示生产因输出格满而受阻塞；>=0 表示剩余生产帧数
                    wb.setMaterialStatus(Integer.parseInt(parts[4])); // 解析原材料格状态；二进制为表示，例如 48(110000),表示拥有物品4和5
                    wb.setProductStatus(Integer.parseInt(parts[5])); // 解析产品格状态 0 表示无 1 表示有
                    workbenchId++;
                    K--;
                    continue;
                }

                // 读取机器人信息
                String[] parts = line.split(" ");
                Robot rbt = robotMap.get(robotId);
                rbt.setWorkbenchId(Integer.parseInt(parts[0])); // 解析所处工作台ID,-1表示没有处于任何工作台, [0, K-1]表是某工作台下标
                rbt.setProductType(Integer.parseInt(parts[1])); // 解析携带物品类型信息, 携带物品类型[0, 7], 0表示未携带物品
                rbt.setTimeCoefficients(Double.parseDouble(parts[2])); // 解析时间价值系数
                rbt.setCollisionCoefficients(Double.parseDouble(parts[3])); // 解析碰撞价值系数
                rbt.setAngularVelocity(Double.parseDouble(parts[4])); // 解析角速度，单位：弧度每秒， 正数表示顺时针， 负数表示逆时针
                rbt.setVelocity(new Velocity(Double.parseDouble(parts[5]), Double.parseDouble(parts[6]))); // 解析线速度，
                                                                                                           // 二维向量描述,
                                                                                                           // m/s
                rbt.setForward(Double.parseDouble(parts[7])); // 解析机器人朝向，[-pi, pi] 0 表示右方向, pi/2表示上方向
                rbt.setPos(new Coordinate(Double.parseDouble(parts[8]), Double.parseDouble(parts[9]))); // 机器人坐标位置
            }

        }
        return false;
    }
}
