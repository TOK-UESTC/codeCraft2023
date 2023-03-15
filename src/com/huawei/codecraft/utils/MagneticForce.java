package com.huawei.codecraft.utils;

public class MagneticForce {
    private double Fx;
    private double Fy;

    public MagneticForce() {
        this.Fx = 0;
        this.Fy = 0;
    }

    public MagneticForce(double fx, double fy) {
        this.Fx = fx;
        this.Fy = fy;
    }

    public double getFx() {
        return Fx;
    }

    public void setFx(double fx) {
        Fx = fx;
    }

    public double getFy() {
        return Fy;
    }

    public void setFy(double fy) {
        Fy = fy;
    }

    public MagneticForce add(MagneticForce m) {
        return new MagneticForce(Fx + m.Fx, Fy + m.Fy);
    }

    public MagneticForce sub(MagneticForce m) {
        return new MagneticForce(Fx - m.Fx, Fy - m.Fy);
    }

    // 返回力的模
    public double getMod() {
        return Math.sqrt(Math.pow(Fx, 2) + Math.pow(Fy, 2));
    }

    // 返回力的方向，也就是fx与fy的夹角，范围是(0, pi)
    public double getAngle() {
        double quadrant = 1.; // 象限
        if (Fx > 0 && Fy < 0 || Fx < 0 && Fy < 0) {
            quadrant = -1.;
        }
        return quadrant * Math.acos(Fx / getMod()); // (-pi/2, pi/2)
    }
}
