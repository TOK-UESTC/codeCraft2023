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

    public void setValue(Vector s) {
        this.x = s.x;
        this.y = s.y;
    }

    public void setValue(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** 获取模 */
    public double mod() {
        return Math.sqrt(x * x + y * y);
    }

    /** 获取角度，和判题器范围相同 */
    public double getAngle() {
        double quadrant = 1.; // 象限
        if (y < 0) {
            quadrant = -1.;
        }

        // 避免除0
        if (mod() < 0.000000001) {
            return 0.;
        } else {
            return quadrant * Math.acos(x / mod()); // (-pi/2, pi/2)
        }
    }
}
