package com.huawei.codecraft;

public class Robot {

    private int robotId; // 机器人编号
    private int workbenchId; // 所处工作台ID, -1表示没有处于任何工作台, [0, K-1]表是某工作台下标
    private int productType; // 携带物品类型[0, 7], 0表示未携带物品
    private double timeCoefficients; // 时间价值系数 [0.8, 1]
    private double collisionCoefficients; // 碰撞价值系数 [0.8, 1]
    private double angularVelocity; // 角速度 单位：弧度每秒， 正数表示顺时针， 负数表示逆时针
    private Velocity velocity; // 线速度， 二维向量描述, m/s
    private double forward; // 朝向 [-pi, pi] 0 表示右方向, pi/2表示上方向
    private Coordinate pos; // 机器人坐标位置

    private int taskWorkbenchId; // 任务工作台ID
    private double taskDistance; // 到任务工作台距离


    public int getTaskWorkbenchId() {
        return taskWorkbenchId;
    }

    public void setTaskWorkbenchId(int taskWorkbenchId) {
        this.taskWorkbenchId = taskWorkbenchId;
    }


    Robot(Coordinate pos, int robotId) {
        this.pos = pos;
        this.robotId = robotId;
        this.workbenchId = -1;
        this.productType = 0;
        this.timeCoefficients = 1;
        this.collisionCoefficients = 1;
        this.angularVelocity = 0;
        this.velocity = null;
        this.forward = 0;
    }

    public int getWorkbenchId() {
        return workbenchId;
    }

    public void setWorkbenchId(int workbenchId) {
        this.workbenchId = workbenchId;
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

    public int getRobotId() {
        return robotId;
    }

    public void setRobotId(int robotId) {
        this.robotId = robotId;
    }

    public void setTaskDistance(double taskDistance) {
        this.taskDistance = taskDistance;
    }

    public double getTaskDistance() {
        return taskDistance;
    }

}
