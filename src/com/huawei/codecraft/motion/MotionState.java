package com.huawei.codecraft.motion;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.vector.Coordinate;
import com.huawei.codecraft.vector.Velocity;

public class MotionState {
    private Coordinate pos; // 时刻t机器人位置
    private double heading; // 时刻t机器人位置朝向
    private Velocity velocity;
    private double w; // 时刻t机器人角速度
    private boolean loaded; // 时刻t机器人负载状态， true: 负载
    private double targetVelocity; // 时刻t机器人目标速度
    private double targetAngularVelocity; // 时刻t机器人目标角速度

    public MotionState(Coordinate pos, double heading, Velocity v, double w, boolean loaded) {
        this.pos = pos;
        this.heading = heading;
        this.velocity = v;
        this.w = w;
        this.loaded = loaded;
    }

    public MotionState(Robot rb) {
        this.pos = rb.getPos();
        this.heading = rb.getHeading();
        this.velocity = rb.getVelocity();
        this.w = rb.getAngularVelocity();
        this.loaded = rb.isLoaded();
        this.targetVelocity = 0;
        this.targetAngularVelocity = 0;
    }

    public void update(Robot rb) {
        this.pos.setValue(rb.getPos());
        this.heading = rb.getHeading();
        this.velocity.setValue(rb.getVelocity());
        this.w = rb.getAngularVelocity();
        this.loaded = rb.isLoaded();
        this.targetVelocity = 0;
        this.targetAngularVelocity = 0;
    }

    public void update(MotionState state) {
        this.pos.setValue(state.pos);
        this.heading = state.heading;
        this.velocity.setValue(state.velocity);
        this.w = state.w;
        this.loaded = state.loaded;
        this.targetVelocity = 0;
        this.targetAngularVelocity = 0;
    }

    public double vMod() {
        return velocity.mod();
    }

    public Coordinate getPos() {
        return pos;
    }

    public double getHeading() {
        return heading;
    }

    public Velocity getVelocity() {
        return velocity;
    }

    public double getW() {
        return w;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setPos(Coordinate pos) {
        this.pos = pos;
    }

    public void setPos(double x, double y) {
        this.pos.setValue(x, y);
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public void setVelocity(Velocity v) {
        this.velocity = v;
    }

    public void setVelocity(double x, double y) {
        this.velocity.setValue(x, y);
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
