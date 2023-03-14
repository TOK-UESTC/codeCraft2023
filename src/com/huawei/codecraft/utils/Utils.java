package com.huawei.codecraft.utils;

import java.util.List;
import java.util.Map;

import com.huawei.codecraft.agent.Workbench;

public class Utils {

    public static void initWorkbench(Map<Integer, Workbench> workbenchMap,
            Map<Integer, List<Workbench>> workbenchTypeMap) {
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

    public static double computeDistance(Coordinate p1, Coordinate p2) {
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

    public static void initWorkbenchConsumerInfo(int[] ids, Map<Integer, List<Workbench>> workbenchTypeMap,
            Workbench wb) {
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

    public static void computePriority(){
        /* 计算优先级
         * 
        */
    }

    public static double computeAward(Workbench wb, double timeCoefficient){
        /* 计算收益
         * 
        */
        double award=0.0;
        // TODO:下面的实现不优雅，但暂时不打算在类中添加属性，因此使用原始数据
        switch(wb.getType()){
            case 1:
                award = 6000*timeCoefficient - 3000;
                break;
            case 2:
                award = 7600*timeCoefficient - 4400;
                break;
            case 3:
                award = 9200*timeCoefficient - 5800;
                break;
            case 4:
                award = 22500*timeCoefficient - 15400;
                break;
            case 5:
                award = 25000*timeCoefficient - 17200; 
                break;
            case 6:
                award = 27500*timeCoefficient - 19200;
                break;
            case 7:
                award = 105000*timeCoefficient - 76000;
                break;
            default:
                break;
        }

        return award;
    }

    public static double timeCoefficient(double frame){
        /* 时间
         * 
        */

        if(frame >= 9000){
            return 0.8;
        }

        return (1-Math.sqrt(1-Math.pow((1 - frame/9000), 2)))*0.2+0.8;
    }

}
