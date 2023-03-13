package com.huawei.codecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Utils {

    static void initWorkbench(Map<Integer, Workbench> workbenchMap, Map<Integer, List<Workbench>> workbenchTypeMap) {
        /*
         * 初始化每个工作台的内容，主要完成每个工作台的产品去向以及他们之间的距离初始化，方便后面复用。
         * 
         * Args:
         * workbenchMap: 一个hashmap, key：工作台ID, value: 工作台对象
         * workbenchTypeMap: 一个hashmap, key: 工作台类型，value: 该类型工作台对象列表
         */
        for (Integer WorkbenchId : workbenchMap.keySet()) {
            Workbench wb = workbenchMap.get(WorkbenchId);
            int[] ids = null;
            switch (wb.getType()) {
                case 1: // 类型4，5，9可回收1类型工作台的产品
                    ids = new int[] { 4, 5, 9 };
                    break;
                case 2: // 类型4，6，9可回收2类型工作台的产品
                    ids = new int[] { 4, 6, 9 };
                    break;
                case 3: // 类型5，6，9可回收3类型工作台的产品
                    ids = new int[] { 5, 6, 9 };
                    break;
                case 4: // 类型7， 9可回收4，5，6类型工作台的产品
                case 5:
                case 6:
                    ids = new int[] { 7, 9 };
                    break;
                case 7: // 类型8，9可回收7类型工作台的产品
                    ids = new int[] { 8, 9 };
                    break;
                default:
                    break;
            }
            initWorkbenchConsumerInfo(ids, workbenchTypeMap, wb);
        }
    }

    static double computeDistance(Coordinate p1, Coordinate p2) {
        /*
         * 计算点p1 与 p2的距离
         * Args:
         * p1: 一个Coordinate对象
         * p2: 一个Coordinate对象
         * 
         * Returns:
         * 点p1 与 p2的距离
         */
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

    static void initWorkbenchConsumerInfo(int[] ids, Map<Integer, List<Workbench>> workbenchTypeMap, Workbench wb) {
        if (ids == null) {
            return;
        }
        for (int i : ids) {
            try { // 如果不存在某些工作台，就会出现异常，这里直接忽略即可
                for (Workbench e : workbenchTypeMap.get(i)) {
                    wb.getConsumerIdList().add(e.getType());
                    wb.getConsumerDistanceList().add(computeDistance(wb.getPos(), e.getPos()));
                }
            } catch (Exception e) {

            }
        }
    }

    static List<Robot> collectUnloadRobot(List<Robot> robotList) {
        /*
         * 得到空载机器人列表, 用于后面接受任务。
         * 
         */
        List<Robot> unloadRobotList = new ArrayList<Robot>();
        for (Robot robot : robotList) {
            if (robot.getProductType() == 0) {
                unloadRobotList.add(robot);
            }
        }
        return unloadRobotList;
    }

    static List<Robot> collectLoadedRobot(List<Robot> robotList) {
        /*
         * 得到负载机器人列表, 用于后面接受任务。
         * 
         */
        List<Robot> loadedRobotList = new ArrayList<Robot>();
        for (Robot robot : robotList) {
            if (robot.getProductType() != 0) {
                loadedRobotList.add(robot);
            }
        }
        return loadedRobotList;
    }

    static void assignTask(List<Robot> unloadRobotList, Map<Integer, Workbench> workbenchMap) {
        /*
         * 分配任务，将最近的任务分配给机器人
         * 
         */
        if (unloadRobotList.size() == 0) {
            return;
        }
        for (Robot robot : unloadRobotList) {
            robot.setTaskDistance(Const.MAX_DISTANCE);
        }
        for (Integer WorkbenchId : workbenchMap.keySet()) {
            Workbench wb = workbenchMap.get(WorkbenchId);
            if (wb.getRest() == -1) {
                continue;
            }
            List<Double> distanceList = new ArrayList<Double>();
            for (Robot robot : unloadRobotList) {
                distanceList.add(computeDistance(robot.getPos(), wb.getPos()));
            }
            List<Double> copyDistanceList = new ArrayList<Double>(distanceList);
            Collections.sort(distanceList);

            for (double distance : copyDistanceList) {
                int index = copyDistanceList.indexOf(distance);
                if (unloadRobotList.get(index).getTaskDistance() > distance) {
                    unloadRobotList.get(index).setTaskDistance(distance);
                    unloadRobotList.get(index).setTaskWorkbenchId(wb.getWorkbenchId());
                    break;
                }
            }

        }
    }

    static void collectBlockWorkbench() {
        /*
         * 收集阻塞的工作台或者即将阻塞的工作台
         * 
         */
    }

    static void dealBlockWorkbench() {
        /*
         * 处理阻塞的工作台或者即将阻塞的工作台
         * 
         */
    }

    static void dealLoadedRobot() {

    }

}
