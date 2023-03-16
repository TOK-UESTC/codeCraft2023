package com.huawei.codecraft.agent;

import java.util.ArrayList;

import com.huawei.codecraft.action.Action;
import com.huawei.codecraft.constants.ActionType;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.task.Task;
import com.huawei.codecraft.task.TaskChain;
import com.huawei.codecraft.vector.Coordinate;
import com.huawei.codecraft.vector.Force;
import com.huawei.codecraft.vector.Velocity;

public class Robot {

    private int workbenchIdx; // 所处工作台下标, -1表示没有处于任何工作台, [0, K-1]表是某工作台下标
    private int productType; // 携带物品类型[0, 7], 0表示未携带物品
    private int lastProductType; // 上一帧携带的品
    private double timeCoefficients; // 时间价值系数 [0.8, 1]
    private double collisionCoefficients; // 碰撞价值系数 [0.8, 1]
    private double angularVelocity; // 角速度 单位：弧度每秒， 正数表示顺时针， 负数表示逆时针
    private Velocity velocity; // 线速度， 二维向量描述, m/s
    private double heading; // 朝向 [-pi, pi] 0 表示右方向, pi/2表示上方向
    private Coordinate pos; // 机器人坐标位置
    private Task task; // 机器人当前任务
    private TaskChain taskChain; // 任务链
    private ArrayList<Action> actions; // 机器人当前动作序列
    private int id;

    public static int robotID = 0;

    // PID参数
    private double Kp = 10;
    private double Ki = 0.1;
    private double Kd = 0.0;
    private double lastError = 0;
    private double integral = 0;
    // 积分值上限
    private double integralMax = 0.5;

    public Robot(Coordinate pos) {
        this.pos = pos;
        this.workbenchIdx = -1;
        this.productType = 0;
        this.lastProductType = 0;
        this.timeCoefficients = 1;
        this.collisionCoefficients = 1;
        this.angularVelocity = 0;
        this.velocity = null;
        this.heading = 0;
        this.task = null;
        this.taskChain = null;
        this.actions = new ArrayList<Action>();
        id = robotID;
        robotID += 1;
    }

    /** 更新所有数据 */
    public void update(String[] info) {
        this.workbenchIdx = Integer.parseInt(info[0]);
        this.lastProductType = this.productType;
        this.productType = Integer.parseInt(info[1]);
        this.timeCoefficients = Double.parseDouble(info[2]);
        this.collisionCoefficients = Double.parseDouble(info[3]);
        this.angularVelocity = Double.parseDouble(info[4]);
        this.velocity = new Velocity(Double.parseDouble(info[5]), Double.parseDouble(info[6]));
        this.heading = Double.parseDouble(info[7]);
        this.pos = new Coordinate(Double.parseDouble(info[8]), Double.parseDouble(info[9]));
    }

    /** 机器人根据当前任务和状态进行动作决策。将决策Action输入到列表中，等待执行 */
    public void step(Force force) {
        // 清空动作列表
        actions.clear();

        // 更新action列表
        generateShopActions();
        generateMoveActions(force);
    }

    /**
     * 根据当前任务链执行情况，生成货物Action
     * 考虑先转向调整姿态，再进行购买操作
     */
    public void generateShopActions() {
        if (task == null) {
            return;
        }

        Workbench wb;

        // 购买
        if (productType == 0) {
            wb = task.getFrom();
            // 判断是否在目标工作台附近
            if (workbenchIdx == wb.getWorkbenchIdx()) {
                // 购买行为
                addAction(new Action(ActionType.BUY));
            }
        } else {
            // 去售出
            wb = task.getTo();
            if (workbenchIdx == wb.getWorkbenchIdx()) {
                // 售出行为
                addAction(new Action(ActionType.SELL));
                // 判断是否有后续任务 TODO:买卖出问题
            }
        }
    }

    /** 检查售卖情况，根据判题器返回更改状态 */
    public void checkDeal() {
        // 没有任务
        if (task == null) {
            return;
        }

        Workbench from = task.getFrom();
        Workbench to = task.getTo();

        // 买成功，不持有->持有
        if (lastProductType == 0 && productType != 0) {
            // 放开原料购买控制台
            from.setInTaskChain(false);
        }

        // 同一帧先卖后买，持有A->持有B
        if (lastProductType != 0 && productType != 0 && lastProductType != productType) {
            taskChain.removeTask(0);
            to.setInTaskChain(false);
            task = taskChain.getNextTask();
        }

        // 卖成功，持有->不持有
        if (lastProductType != 0 && productType == 0) {
            taskChain.removeTask(0);
            task = taskChain.getNextTask();
            // 检查是否是最后一个任务，是的话，释放目标工作台
            if (task == null) {
                to.setInTaskChain(false);
            }
        }
    }

    public void finishTask() {

    }

    /**
     * 根据虚拟力，计算前进与转向Action
     *
     * @param 虚拟力
     *
     */
    private void generateMoveActions(Force force) {
        // 计算角度误差
        double angle = force.getAngle();
        double error = heading - angle;

        if (error > Math.PI) {
            error = error - 2 * Math.PI;
        } else if (error < -Math.PI) {
            error = error + 2 * Math.PI;
        }

        // 角度环控制
        double angularVelocity = -(Kp * error + Ki * integral + Kd * (error - lastError));
        lastError = error;
        integral += error;
        // 积分值上限
        integral = Math.min(integral, integralMax);
        integral = Math.max(integral, -integralMax);
        // 如果角速度变化过大，就不进行角度修正,判断标准是角速度发生了正负的大变化
        if (Math.abs(error) > Math.PI / 16) {
            // 产生转向动作
            actions.add(new Action(ActionType.ROTATE, angularVelocity));
            actions.add(new Action(ActionType.FORWARD, 0));
            // 速度满足角度差越大速度就越小
            // double lineSpeed = -6 * Math.abs(1 / angularVelocity);
            // // // 产生前进动作
            // actions.add(new Action("forward", lineSpeed));
        } else {
            // 产生转向动作
            actions.add(new Action(ActionType.ROTATE, angularVelocity));
            // 防止撞墙，进行修正
            double lineSpeed = 6;
            // 判断是否与墙近
            // double minDsitance2WallThreshold = 0.25;
            // double minDistance2WallX = getPos().getX() < 25 ? getPos().getX() : 50 -
            // getPos().getX();
            // double minDistance2WallY = getPos().getY() < 25 ? getPos().getY() : 50 -
            // getPos().getY();
            // if (minDistance2WallX < minDsitance2WallThreshold) {
            // lineSpeed -= 1 / minDistance2WallX;
            // }

            // 产生前进动作
            actions.add(new Action(ActionType.FORWARD, lineSpeed));
        }
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public void addAction(Action actions) {
        this.actions.add(actions);
    }

    /** 绑定任务链 */
    public void bindChain(TaskChain taskChain) {
        this.taskChain = taskChain;
        // 机器人绑定任务链的时候就会分配任务
        this.task = taskChain.getTasks().get(0);
    }

    /** 获取当前任务链 */
    public TaskChain getTaskChain() {
        return taskChain;
    }

    /**
     * 当前机器人是否分配有任务
     * 
     * @return: true = free
     */
    public boolean isFree() {
        return task == null;
    }

    /**
     * 获取机器人当前收益
     *
     * @param: Max，当两个系数都为1的时候的最大收益
     */
    public double profit(boolean max) {
        if (task == null) {
            return -1;
        } else if (max) {
            return task.getProfit(1, 1);
        } else {
            return task.getProfit(timeCoefficients, collisionCoefficients);
        }
    }

    /** 返回当前所处的工作台id */
    public int getWorkbenchIdx() {
        return workbenchIdx;
    }

    /** 通过负载情况返回机器人半径 */
    public double getRadius() {
        return productType == 0 ? Const.ROBOT_RADIUS_UNLOAD : Const.ROBOT_RADIUS_LOADED;
    }

    /** 获取当前携带的物品种类 */
    public int getProductType() {
        return productType;
    }

    /** 获取时间因子 */
    public double getTimeCoefficients() {
        return timeCoefficients;
    }

    /** 获取碰撞因子 */
    public double getCollisionCoefficients() {
        return collisionCoefficients;
    }

    /** 获取角速度 */
    public double getAngularVelocity() {
        return angularVelocity;
    }

    /** 获取线速度 */
    public Velocity getVelocity() {
        return velocity;
    }

    /** 获取当前朝向 */
    public double getHeading() {
        return heading;
    }

    /** 获取位置 */
    public Coordinate getPos() {
        return pos;
    }

    /** 获取机器人当前任务 */
    public Task getTask() {
        return task;
    }

    /** hashcode */
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        Robot r = (Robot) o;
        return this.id == r.id;
    }
}
