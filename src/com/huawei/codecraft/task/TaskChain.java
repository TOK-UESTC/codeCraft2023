package com.huawei.codecraft.task;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.constants.Const;

public class TaskChain implements Comparable<TaskChain> {

    // 执行任务链的机器人
    private Robot Robot;
    // 用来存储任务链中的任务
    private List<Task> taskChain;
    // 在已有任务链的条件下，完成任务所需最快帧数
    private double totalFrame;

    public TaskChain(Robot Robot, double totalFrame) {
        this.Robot = Robot;
        this.taskChain = new ArrayList<Task>();
        this.totalFrame = totalFrame;
    }

    public TaskChain(TaskChain chain) {
        this.Robot = chain.getRobot();
        this.taskChain = chain.getTasks();
        this.totalFrame = chain.getTotalFrame();
    }

    /**
     * 为任务链添加任务，同时更新任务链完成所需要的总帧数
     * 
     * @param task
     */
    public void addTask(Task task) {
        this.taskChain.add(task);
        this.totalFrame += task.getDistance() / Const.MAX_FORWARD_FRAME;
    }

    public int length() {
        return this.taskChain.size();
    }

    public List<Task> getTasks() {
        return taskChain;
    }

    public void setTaskChain(List<Task> taskChain) {
        this.taskChain = taskChain;
    }

    public double getTotalFrame() {
        return totalFrame;
    }

    public void setTotalFrame(double totalFrame) {
        this.totalFrame = totalFrame;
    }

    public Robot getRobot() {
        return Robot;
    }

    public void setRobot(Robot robot) {
        Robot = robot;
    }

    // 在初始化的时候调用
    public double getProfit() {
        double profit = 0.0;

        for (Task task : taskChain) {
            profit += task.makePredict();
        }
        return profit;
    }

    /**
     * 实现任务链排序，当前只是使用了任务链的总收益
     *
     * @param o 比较对象
     * @return
     */
    @Override
    public int compareTo(TaskChain o) {
        return Double.compare(this.getProfit(), o.getProfit());
    }

}