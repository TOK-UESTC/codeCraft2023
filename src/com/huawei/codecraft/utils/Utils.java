package com.huawei.codecraft.utils;

import java.util.List;
import java.util.Map;

import com.huawei.codecraft.agent.Workbench;

public class Utils {

    static void initWorkbench(Map<Integer, Workbench> workbenchMap, Map<Integer, List<Workbench>> workbenchTypeMap) {
        /*
         * 初始化每个工作台的内容，主要完成每个工作台的产品去向以及他们之间的距离初始化，方便后面复用。
         * 
         * Args:
         * workbenchMap: 一个hashmap, key：工作台ID, value: 工作台对象
         * workbenchTypeMap: 一个hashmap, key: 工作台类型，value: 该类型工作台对象列表
         */
        for (Integer workbenchIdx : workbenchMap.keySet()) {
            Workbench wb = workbenchMap.get(workbenchIdx);
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

}
