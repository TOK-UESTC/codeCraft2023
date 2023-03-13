package com.huawei.codecraft.Task;

import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.utils.Utils;

/*
 * @Description: 任务类，在初始化的过程中生成，在调度器中使用
 * 若需要拷贝，请使用Task对象内置的copy()方法
 * TODO: 将Robot中的价格因子给转移到任务对象上来？
 */
public class Task {
    private Workbench from; // 任务来源
    private Workbench to; // 任务目的
    private int productType;// 物品型号
    private double priority;// 优先级
    private int award; // 完成任务所获得的最大奖励
    private double distance; // 完成任务所需要的距离

    public Task(Workbench from, Workbench to, int productType, double priority, int award) {
        this.from = from;
        this.to = to;
        this.productType = productType;
        this.priority = priority;
        this.award = award;

        this.distance = Utils.computeDistance(from.getPos(), to.getPos());
    }

    public Workbench from() {
        return from;
    }

    public Workbench to() {
        return to;
    }

    public int getProductType() {
        return productType;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public int getAward() {
        return award;
    }

    public double getDistance() {
        return distance;
    }

    public Task copy() {
        return new Task(from, to, productType, priority, award);
    }
}
