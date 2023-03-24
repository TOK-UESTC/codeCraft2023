package com.huawei.codecraft.task;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.utils.Utils;

/**
 * @description: 任务对象
 */
public class Task {
    private Workbench from; // 任务来源
    private Workbench to; // 任务目的
    private double priority;// 优先级
    private double price; // 原始价格
    private double sellPrice; // 完成任务所获得的最大奖励
    private double distance; // 完成任务所需要的距离

    private boolean visited; // 是否被访问
    private List<Task> postTaskList; // 后继任务列表

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

        this.visited = false;
        this.postTaskList = new ArrayList<>();
    }

    public Task(Task source) {
        this.from = source.from;
        this.to = source.to;
    }

    /** 获取后续任务列表 */
    public List<Task> getPostTaskList() {
        return postTaskList;
    }

    /** 设定后续任务 */
    public void setPostTaskList(List<Task> postTaskList) {
        if (postTaskList == null) {
            return;
        }
        this.postTaskList = postTaskList;
    }

    /** 获取任务来源工作台 */
    public Workbench getFrom() {
        return from;
    }

    /** 获取任务目标工作台 */
    public Workbench getTo() {
        return to;
    }

    /** 获取来源工作台索引 */
    public int getFromIdx() {
        return from.getWorkbenchIdx();
    }

    /** 获取目标工作台索引 */
    public int getToIdx() {
        return to.getWorkbenchIdx();
    }

    /** 返回生产物品，实则就是来源工作台类型 */
    public int getProductType() {
        return from.getType();
    }

    /** TODO: 获取任务优先级？ */
    public double getPriority() {
        return priority;
    }

    /** 设置任务优先级 */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    /** 根据给定因子获取收益 */
    public double getProfit(double timeCoefficients, double collisionCoefficients) {
        return sellPrice * timeCoefficients * collisionCoefficients - price;
    }

    /**
     * @description: 根据当前已知信息预测到达时的盈利价格
     *               当前假定速度为最大，计算时间因子
     * @return 最大收益
     */
    public double makePredict() {
        double timeCoefficient;
        double predictedFrame = distance / Const.MAX_FORWARD_FRAME;
        if (predictedFrame >= 9000) {
            timeCoefficient = 0.8;
        } else {
            timeCoefficient = (1 - Math.sqrt(1 - Math.pow(1 - (predictedFrame / 9000), 2))) * (1 - 0.8) + 0.8;
        }

        return (sellPrice * timeCoefficient) - price;
    }

    /** 获取任务距离 */
    public double getDistance() {
        return distance;
    }

    /** 获取从from到to的直线连线角度 */
    public double getAngle() {
        double x = to.getPos().getX() - from.getPos().getX();
        double y = to.getPos().getY() - from.getPos().getY();
        double quadrant = 1.; // 象限
        if (y < 0) {
            quadrant = -1.;
        }

        // 避免除0
        double mod = Math.sqrt(x * x + y * y);
        if (mod < 0.000000001) {
            return 0.;
        } else {
            return quadrant * Math.acos(x / mod); // (-pi/2, pi/2)
        }
    }

    /**  */
    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

}