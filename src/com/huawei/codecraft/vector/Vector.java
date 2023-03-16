package com.huawei.codecraft.vector;

public class Vector {
    protected double x;
    protected double y;

    public Vector() {
        x = 0;
        y = 0;
    }

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    /** 获取模 */
    public double mod() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    /** 获取角度，和判题器范围相同 */
    public double getAngle() {
        double quadrant = 1.; // 象限
        if (y < 0) {
            quadrant = -1.;
        }

        // TODO: 返回0 ？ 是否需要修正
        if (mod() < 0.000000001) {
            return 0.;
        } else {
            return quadrant * Math.acos(x / mod()); // (-pi/2, pi/2)
        }
    }

    public Vector add(Vector v) {
        return new Force(x + v.x, y + v.y);
    }

    public Vector sub(Vector v) {
        return new Force(x - v.x, y - v.y);
    }
}
