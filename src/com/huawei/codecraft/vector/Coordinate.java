package com.huawei.codecraft.vector;

public class Coordinate extends Vector {
    public Coordinate() {
        super();
    }

    public Coordinate(double x, double y) {
        super(x, y);
    }

    public Coordinate add(Force v) {
        return new Coordinate(x + v.x, y + v.y);
    }

    public Coordinate sub(Force v) {
        return new Coordinate(x - v.x, y - v.y);
    }
}
