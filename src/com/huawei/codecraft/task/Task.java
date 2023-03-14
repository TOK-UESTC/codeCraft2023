package com.huawei.codecraft.task;

import java.util.HashMap;
import java.util.Map;

import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.utils.Utils;

/*
 * @Description: 任务类，在初始化的过程中生成，在调度器中使用
 * 若需要拷贝，请使用Task对象内置的copy()方法
 * TODO: 将Robot中的价格因子给转移到任务对象上来？
 */
public class Task {
    private Workbench from; // 任务来源
    private Workbench to; // 任务目的
    private double priority;// 优先级
    private double price; // 原始价格
    private double sellPrice; // 完成任务所获得的最大奖励
    private double distance; // 完成任务所需要的距离

    public Task(Workbench from, Workbench to) {
        this.from = from;
        this.to = to;

        // 根据控制台类型确定价格
        Integer[] priceInfo = Const.priceMapper.get(from.getType());
        this.price = priceInfo[0];
        this.sellPrice = priceInfo[1];

        // 计算工作台距离
        this.distance = Utils.computeDistance(from.getPos(), to.getPos());

        /*
         * 给同等类型但是距离较短的任务较高的优先级
         * 给不同类型但是生产成品类型更复杂的任务较高的优先级
         */
        this.priority = (sellPrice - price) / distance + from.getType();
    }

    public Workbench from() {
        return from;
    }

    public Workbench to() {
        return to;
    }

    // 返回生产物品，实则就是工作台类型
    public int getProductType() {
        return from.getType();
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getProfit(double timeCoefficients, double collisionCoefficients) {
        return sellPrice * timeCoefficients * collisionCoefficients - price;
    }

    /*
     * 根据当前已知信息预测到达时的盈利价格
     * 当前假定速度为最大，计算时间因子
     */
    public double makePredict() {
        double predictedFrame = distance / Const.MAX_FORWARD_VELOCITY * Const.FRAME_PER_SECOND;
        if (predictedFrame >= 9000) {
            return 0.8;
        } else {
            return (1 - Math.sqrt(1 - Math.pow(1 - (predictedFrame / 9000), 2))) * (1 - 0.8) + 0.8;
        }
    }

    public double getPrice() {
        return price;
    }

    public double getDistance() {
        return distance;
    }

    public Task copy() {
        return new Task(from, to);
    }
}
