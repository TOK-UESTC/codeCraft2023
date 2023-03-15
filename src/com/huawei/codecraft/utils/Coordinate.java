package com.huawei.codecraft.utils;

public class Coordinate {

    private double x; // 坐标x
    private double y; // 坐标y

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Coordinate other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    public double angle(Coordinate other) {
        return Math.atan2(other.y - y, other.x - x);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

}
