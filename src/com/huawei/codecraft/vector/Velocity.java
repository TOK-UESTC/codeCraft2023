package com.huawei.codecraft.vector;

public class Velocity extends Vector {
    public Velocity() {
        super();
    }

    public Velocity(double x, double y) {
        super(x, y);
    }

    public Velocity add(Force v) {
        return new Velocity(x + v.x, y + v.y);
    }

    public Velocity sub(Force v) {
        return new Velocity(x - v.x, y - v.y);
    }
}
