package com.huawei.codecraft;

public class Velocity {

    private double vx; // x方向上线速度 m/s
    private double vy; // y方向上线速度 m/s

    Velocity() {
        vx = 0;
        vy = 0;
    }

    Velocity(double vx, double vy) {
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
