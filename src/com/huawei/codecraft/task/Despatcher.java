package com.huawei.codecraft.task;

import java.util.List;
import java.util.Map;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;

/*
 * @Description: 调度器类
 */
public class Despatcher {
    private List<Robot> robotList;
    private List<Workbench> workbenchList;
    private Map<Integer, List<Workbench>> workbenchTypeMap;

    private List<Task> optionalTask;

    // 初始化调度器，按照自己的想法储存Task
    public Despatcher(List<Robot> robotList, List<Workbench> workbenchList,
            Map<Integer, List<Workbench>> workbenchTypeMap) {
        this.robotList = robotList;
        this.workbenchList = workbenchList;
        this.workbenchTypeMap = workbenchTypeMap;
    }

    public void dispatch() {

    }

    // 初始化任务列表，装入可选区
    // optionalTask
    public void init() {

    }
}
