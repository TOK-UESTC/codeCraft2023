package com.huawei.codecraft.motion;

import com.huawei.codecraft.agent.Robot;

public class MotionState {
    private double posX; // 时刻t机器人位置X
    private double posY; // 时刻t机器人位置Y
    private double heading; // 时刻t机器人位置朝向
    private double vx; // 时刻t机器人x轴方向速度
    private double vy; // 时刻t机器人y轴方向速度
    private double w; // 时刻t机器人角速度
    private boolean loaded; // 时刻t机器人负载状态， true: 负载
    private int time; // 时刻t
    private double targetVelocity; // 时刻t机器人目标速度
    private double targetAngularVelocity; // 时刻t机器人目标角速度

    public MotionState(double posX, double posY, double heading, double vx, double vy, double w, boolean loaded) {
        this.posX = posX;
        this.posY = posY;
        this.heading = heading;
        this.vx = vx;
        this.vy = vy;
        this.w = w;
        this.loaded = loaded;
    }

    public MotionState(MotionState state) {
        this.posX = state.posX;
        this.posY = state.posY;
        this.heading = state.heading;
        this.vx = state.vx;
        this.vy = state.vy;
        this.w = state.w;
        this.loaded = state.loaded;
    }

    public MotionState(Robot rb) {
        this.posX = rb.getPos().getX();
        this.posY = rb.getPos().getY();
        this.heading = rb.getHeading();
        this.vx = rb.getVelocity().getX();
        this.vy = rb.getVelocity().getY();
        this.w = rb.getAngularVelocity();
        this.loaded = rb.isLoaded();
        this.time = rb.getFrame();
        this.targetVelocity = 0;
        this.targetAngularVelocity = 0;
    }

    public double vMod() {
        return Math.sqrt(vx * vx + vy * vy);
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getHeading() {
        return heading;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getW() {
        return w;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public void setW(double w) {
        this.w = w;
    }

    public void setTargetVelocity(double targetVelocity) {
        this.targetVelocity = targetVelocity;
    }

    public void setTargetAngularVelocity(double targetAngularVelocity) {
        this.targetAngularVelocity = targetAngularVelocity;
    }

    public double getTargetVelocity() {
        return targetVelocity;
    }

    public double getTargetAngularVelocity() {
        return targetAngularVelocity;
    }

}
