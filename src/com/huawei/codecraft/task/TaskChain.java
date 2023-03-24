package com.huawei.codecraft.task;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.constants.Const;

public class TaskChain implements Comparable<TaskChain> {
    // 用来存储任务链中的任务
    private List<Task> taskChain;
    // 在已有任务链的条件下，完成任务所需最快帧数
    private double totalFrame;

    private static int count = 0;

    public TaskChain(double totalFrame) {
        this.taskChain = new ArrayList<Task>();
        this.totalFrame = totalFrame;

        count += 1;

        // System.err.println(count);
    }

    public TaskChain(TaskChain chain) {
        this.taskChain = new ArrayList<Task>();
        this.taskChain.addAll(chain.taskChain);
        this.totalFrame = chain.totalFrame;
    }

    public void update(TaskChain chain) {
        this.taskChain.clear();
        this.taskChain.addAll(chain.taskChain);
        this.totalFrame = chain.totalFrame;
    }

    public void update(double totalFrame) {
        this.taskChain.clear();
        this.totalFrame = totalFrame;
    }

    /** 将该任务链上的工作台都置为使用中，避免后续机器人重复领取 */
    public void occupy() {
        // 任务链包括规划产品格状态和规划原料格状态
        // 1. 生产工作台规划产品格被占用：task.getFrom().setPlanProductStatus(1);
        // 2. 消费工作台对应原料格被占用:
        // task.getTo().updatePlanMaterialStatus(task.getFrom().getType(), false);
        for (Task task : taskChain) {
            task.getFrom().setPlanProductStatus(1);
            task.getTo().updatePlanMaterialStatus(task.getFrom().getType(), false);
        }
    }

    /** 判断该链条上是否有工作台被占用 */
    public boolean isOccupied() {
        // 判断该任务链是否被占据
        // 1. 生产工作台产品格未被占据: task.getFrom().getPlanProductStatus() == 0
        // 2. 消费工作台原料格未被占据: task.getTo().hasPlanMaterial(task.getFrom().getType())
        for (Task task : taskChain) {
            if (task.getFrom().getPlanProductStatus() != 0 || task.getTo().hasPlanMaterial(task.getFrom().getType())) {
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
        // 移动时间
        this.totalFrame += task.getDistance() / Const.MAX_FORWARD_FRAME;
    }

    /** 删除taskChain[index] */
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