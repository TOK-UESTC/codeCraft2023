package com.huawei.codecraft.motion;

public class MotionFrag {
    private double t;
    private double linearAcc;
    private double angularAcc;

    public MotionFrag(double t, double linearAcc, double angularAcc) {
        this.t = t;
        this.linearAcc = linearAcc;
        this.angularAcc = angularAcc;
    }

    public double getT() {
        return t;
    }

    public double getLinearAcc() {
        return linearAcc;
    }

    public double getAngularAcc() {
        return angularAcc;
    }

    public void update(double t, double linearAcc, double angularAcc) {
        this.t = t;
        this.linearAcc = linearAcc;
        this.angularAcc = angularAcc;
    }
}
