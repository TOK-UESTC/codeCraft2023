package com.huawei.codecraft.vector;

public class Force extends Vector {
    public Force() {
        super();
    }

    public Force(double x, double y) {
        super(x, y);
    }

    public Force add(Force v) {
        return new Force(x + v.x, y + v.y);
    }

    public Force sub(Force v) {
        return new Force(x - v.x, y - v.y);
    }
}
