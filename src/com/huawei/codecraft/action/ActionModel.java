package com.huawei.codecraft.action;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.ActionType;
import com.huawei.codecraft.motion.MotionState;

public class ActionModel {
    private Robot rb;

    public ActionModel(Robot rb) {
        this.rb = rb;
    }

    public void generate() {
        generateShopActions();
        generateMoveActions();
    }

    /** 距离加角度PID */
    private void generateMoveActions() {
        // 首先判断是否有任务
        if (rb.getTask() == null) {
            return;
        }

        double[] controlFactor = rb.control(new MotionState(rb), rb.predict());
        // 产生转向动作
        rb.addAction(new Action(ActionType.ROTATE, controlFactor[1]));
        // 产生前进动作
        rb.addAction(new Action(ActionType.FORWARD, controlFactor[0]));
    }

    /**
     * 根据当前任务链执行情况，生成货物Action
     * 考虑先转向调整姿态，再进行购买操作
     */
    public void generateShopActions() {
        if (rb.getTask() == null) {
            return;
        }

        Workbench wb;

        // 购买
        if (rb.getProductType() == 0) {
            wb = rb.getTask().getFrom();
            // 判断是否在目标工作台附近，并且当前已经调转，开始朝向下一个工作台
            if (rb.getWorkbenchIdx() == wb.getWorkbenchIdx()) {
                // 购买行为
                rb.addAction(new Action(ActionType.BUY));
                // }
            }
        } else {
            // 去售出
            wb = rb.getTask().getTo();
            if (rb.getWorkbenchIdx() == wb.getWorkbenchIdx()) {
                // 售出行为
                rb.addAction(new Action(ActionType.SELL));
            }
        }
    }

}
