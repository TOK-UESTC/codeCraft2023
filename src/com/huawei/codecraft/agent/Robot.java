package com.huawei.codecraft.agent;

import com.huawei.codecraft.utils.Coordinate;
import com.huawei.codecraft.utils.Velocity;

public class Robot {

    private int robotIdx; // 机器人下标
    private int workbenchIdx; // 所处工作台下标, -1表示没有处于任何工作台, [0, K-1]表是某工作台下标
    private int productType; // 携带物品类型[0, 7], 0表示未携带物品
    private double timeCoefficients; // 时间价值系数 [0.8, 1]
    private double collisionCoefficients; // 碰撞价值系数 [0.8, 1]
    private double angularVelocity; // 角速度 单位：弧度每秒， 正数表示顺时针， 负数表示逆时针
    private Velocity velocity; // 线速度， 二维向量描述, m/s
    private double forward; // 朝向 [-pi, pi] 0 表示右方向, pi/2表示上方向
    private Coordinate pos; // 机器人坐标位置

    public Robot(Coordinate pos, int robotIdx) {
        this.pos = pos;
        this.robotIdx = robotIdx;
        this.workbenchIdx = -1;
        this.productType = 0;
        this.timeCoefficients = 1;
        this.collisionCoefficients = 1;
        this.angularVelocity = 0;
        this.velocity = null;
        this.forward = 0;
    }

    // 更新所有数据
    public void update(String[] info) {
        this.workbenchIdx = Integer.parseInt(info[0]);
        this.productType = Integer.parseInt(info[1]);
        this.timeCoefficients = Double.parseDouble(info[2]);
        this.collisionCoefficients = Double.parseDouble(info[3]);
        this.angularVelocity = Double.parseDouble(info[4]);
        this.velocity = new Velocity(Double.parseDouble(info[5]), Double.parseDouble(info[6]));
        this.forward = Double.parseDouble(info[7]);
        this.pos = new Coordinate(Double.parseDouble(info[8]), Double.parseDouble(info[9]));
    }

    public int getworkbenchIdx() {
        return workbenchIdx;
    }

    public void setworkbenchIdx(int workbenchIdx) {
        this.workbenchIdx = workbenchIdx;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public double getTimeCoefficients() {
        return timeCoefficients;
    }

    public void setTimeCoefficients(double timeCoefficients) {
        this.timeCoefficients = timeCoefficients;
    }

    public double getCollisionCoefficients() {
        return collisionCoefficients;
    }

    public void setCollisionCoefficients(double collisionCoefficients) {
        this.collisionCoefficients = collisionCoefficients;
    }

    public double getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(double angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public Velocity getVelocity() {
        return velocity;
    }

    public void setVelocity(Velocity velocity) {
        this.velocity = velocity;
    }

    public double getForward() {
        return forward;
    }

    public void setForward(double forward) {
        this.forward = forward;
    }

    public Coordinate getPos() {
        return pos;
    }

    public void setPos(Coordinate pos) {
        this.pos = pos;
    }

    public int getRobotIdx() {
        return robotIdx;
    }

    public void setRobotId(int robotId) {
        this.robotIdx = robotId;
    }

}
