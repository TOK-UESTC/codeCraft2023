package com.huawei.codecraft.task;

import java.util.List;
import java.util.Map;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;

/*
 * @Description: 调度器类
 */
public class Dispatcher {
    private List<Robot> robotList;
    private List<Workbench> workbenchList;
    private Map<Integer, List<Workbench>> workbenchTypeMap;
    private List<TaskLink> taskLinks;

    private Map<Integer, List<Task>> graph;

    // 初始化调度器，按照自己的想法储存Task
    public Dispatcher(List<Robot> robotList, List<Workbench> workbenchList,
            Map<Integer, List<Workbench>> workbenchTypeMap) {
        this.robotList = robotList;
        this.workbenchList = workbenchList;
        this.workbenchTypeMap = workbenchTypeMap;

        init();
    }

    public void dispatch() {
        // for (Robot robot : robotList) {
        // robot.T
        // }
    }

    /*
     * 初始化每个工作台的内容，主要完成每个工作台的产品去向以及他们之间的距离初始化，方便后面复用。
     *
     */
    public void init() {
        for (Workbench wb : workbenchList) {
            int[] types = null;
            switch (wb.getType()) {
                case 1: // 类型4，5，9可回收1类型工作台的产品
                    types = new int[] { 4, 5, 9 };
                    break;
                case 2: // 类型4，6，9可回收2类型工作台的产品
                    types = new int[] { 4, 6, 9 };
                    break;
                case 3: // 类型5，6，9可回收3类型工作台的产品
                    types = new int[] { 5, 6, 9 };
                    break;
                case 4: // 类型7， 9可回收4，5，6类型工作台的产品
                case 5:
                case 6:
                    types = new int[] { 7, 9 };
                    break;
                case 7: // 类型8，9可回收7类型工作台的产品
                    types = new int[] { 8, 9 };
                    break;
                default:
                    types = new int[] {};
                    break;
            }

            // 根据工作台类型以及可以接收产物的工作台创建task
            for (int type : types) {
                // 如果当前地图上不含某些工作台，直接跳过
                if (!workbenchTypeMap.containsKey(type)) {
                    continue;
                }

                // 生成任务
                for (Workbench target : workbenchTypeMap.get(type)) {
                    wb.addTask(new Task(wb, target));
                }
            }
        }

        generateTaskLinks();
    }

    public void generateTaskLinks() {

    }
}
