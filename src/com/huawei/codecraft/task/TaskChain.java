package com.huawei.codecraft.task;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.agent.Robot;

public class TaskChain {

    // 执行任务链的机器人
    private Robot Robot;
    // 用来存储任务链中的任务
    private List<Task> taskChain;
    // 在已有任务链的条件下，完成任务最快的时间
    private double finishTime;

    public TaskChain(Robot Robot) {
        this.Robot = Robot;
        this.taskChain = new ArrayList<Task>();
        this.finishTime = 0.;
    }

    public TaskChain(TaskChain chain) {
        this.Robot = chain.getRobot();
        this.taskChain = chain.getTaskChain();
        this.finishTime = chain.getFinishTime();
    }

    public List<Task> getTaskChain() {
        return taskChain;
    }

    public void setTaskChain(List<Task> taskChain) {
        this.taskChain = taskChain;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }

    public Robot getRobot() {
        return Robot;
    }

    public void setRobot(Robot robot) {
        Robot = robot;
    }

    // 在初始化的时候调用
    public double getReward() {
        double reward = 0.0;

        for (Task metaTask : taskChain) {
            reward = reward + metaTask.makePredict();
        }
        return reward;
    }

}