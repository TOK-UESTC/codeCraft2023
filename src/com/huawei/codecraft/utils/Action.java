package com.huawei.codecraft.utils;

public class Action {
    private ActionType type;
    private double value;

    public Action(ActionType type, double value) {
        this.type = type;
        this.value = value;
    }

    // 给不需要参数的动作使用, e.g. buy, sell, destroy
    public Action(ActionType type) {
        this.type = type;
        this.value = 0;
    }

    public String toString(int robotId) {
        switch (type) {
            case FORWARD:
                return String.format("forward %d %f", robotId, value);
            case ROTATE:
                return String.format("rotate %d %f", robotId, value);
            case BUY:
                return String.format("buy %d", robotId);
            case SELL:
                return String.format("sell %d", robotId);
            case DESTROY:
                return String.format("destroy %d", robotId);
            default:
                return "";
        }
    }
}
