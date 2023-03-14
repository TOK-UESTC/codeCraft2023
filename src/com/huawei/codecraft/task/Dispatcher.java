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
    private Map<Integer, List<Node>> graph;
    

    // 初始化调度器，按照自己的想法储存Task
    public Dispatcher(List<Robot> robotList, Map<Integer, List<Node>> graph) {
        this.robotList = robotList;
        this.graph = graph;
    }

    public void dispatch() {

    }

    
}
