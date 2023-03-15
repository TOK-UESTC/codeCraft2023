package com.huawei.codecraft.utils;

enum ActionType {
    FORWARD,
    ROTATE,
    BUY,
    SELL,
    DESTROY
}

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

    // 可能可以优化，直接使用内部枚举类型
    public Action(String string, double angularVelocity) {
        switch (string) {
            case "forward":
                this.type = ActionType.FORWARD;
                break;
            case "rotate":
                this.type = ActionType.ROTATE;
                break;
            case "buy":
                this.type = ActionType.BUY;
                break;
            case "sell":
                this.type = ActionType.SELL;
                break;
            case "destroy":
                this.type = ActionType.DESTROY;
                break;
            default:
                this.type = ActionType.FORWARD;
                break;
        }
        this.value = angularVelocity;
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
