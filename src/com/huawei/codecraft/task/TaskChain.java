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
        this.taskChain = new ArrayList<>();
        this.taskChain.addAll(chain.getTasks());
        this.totalFrame = chain.getTotalFrame();
    }

    /** 将该任务链上的工作台都置为使用中，避免后续机器人重复领取 */
    public void occupy() {
        for (Task task : taskChain) {
            task.getFrom().setInTaskChain(true);
            task.getTo().setInTaskChain(true);
        }
    }

    /** 判断该链条上是否有工作台被占用 */
    public boolean isOccupied() {
        for (Task task : taskChain) {
            if (task.getFrom().isInTaskChain() || task.getTo().isInTaskChain()) {
                return true;
            }
        }
        return false;
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

    public Task getFirstTask() {
        if (taskChain.size() != 0) {
            return taskChain.get(0);
        }
        return null;
    }

    public void removeTask(int index) {
        taskChain.remove(index);
    }

    public Task getNextTask() {
        if (taskChain.size() != 0) {
            return taskChain.get(0);
        }
        return null;
    }

    /** 任务链长度 */
    public int length() {
        return this.taskChain.size();
    }

    /** 获取任务链 */
    public List<Task> getTasks() {
        return taskChain;
    }

    /** 设定任务链 */
    public void setTaskChain(List<Task> taskChain) {
        this.taskChain = taskChain;
    }

    /** 完成任务预估需要的帧数 */
    public double getTotalFrame() {
        return totalFrame;
    }

    /** 设定预估帧数 */
    public void setTotalFrame(double totalFrame) {
        this.totalFrame = totalFrame;
    }

    /** 获取当前chain派发的机器人 */
    public Robot getRobot() {
        return Robot;
    }

    /** 设定负责机器人 */
    public void setRobot(Robot robot) {
        Robot = robot;
    }

    /** 当前task的预估收益 */
    public double getProfit() {
        double profit = 0.0;

        for (Task task : taskChain) {
            profit += task.makePredict();
        }
        profit /= totalFrame;
        return profit;
    }

    /**
     * 实现任务链排序，当前只是使用了任务链的预估收益
     *
     * @param o 比较对象
     * @return
     */
    @Override
    public int compareTo(TaskChain o) {
        return Double.compare(o.getProfit(), this.getProfit());
    }

    /** toString，方便写入Log */
    public String toString() {
        return "0";
    }

}