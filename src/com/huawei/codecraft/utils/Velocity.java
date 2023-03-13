package com.huawei.codecraft.utils;

public class Velocity {

    private double vx; // x方向上线速度 m/s
    private double vy; // y方向上线速度 m/s

    public Velocity() {
        vx = 0;
        vy = 0;
    }

    public Velocity(double vx, double vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

}
