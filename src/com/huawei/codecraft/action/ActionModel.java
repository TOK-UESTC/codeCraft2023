package com.huawei.codecraft.action;

import com.huawei.codecraft.ObjectPool;
import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.ActionType;
import com.huawei.codecraft.motion.MotionState;
import com.huawei.codecraft.vector.Coordinate;

public class ActionModel {
    private Robot rb;

    Action rotateAction;
    Action forwardAction;
    Action buyAction;
    Action sellAction;
    private ObjectPool<MotionState> statePool;
    private ObjectPool<Coordinate> coordPool;

    public ActionModel(Robot rb, ObjectPool<MotionState> statePool, ObjectPool<Coordinate> coordPool) {
        this.rb = rb;
        // TODO:不优雅的实现
        this.rotateAction = new Action(ActionType.ROTATE);
        this.forwardAction = new Action(ActionType.FORWARD);
        this.buyAction = new Action(ActionType.BUY);
        this.sellAction = new Action(ActionType.SELL);

        this.statePool = statePool;
        this.coordPool = coordPool;
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

        // 获取state
        MotionState state = statePool.acquire();
        state.update(rb);

        Coordinate next = rb.predict();
        double[] controlFactor = rb.control(state, next);
        coordPool.release(next);
        statePool.release(state);
        // 产生转向动作
        rb.addAction(this.rotateAction.update(ActionType.ROTATE, controlFactor[1]));
        // 产生前进动作
        rb.addAction(this.forwardAction.update(ActionType.FORWARD, controlFactor[0]));
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
                rb.addAction(this.buyAction.update(ActionType.BUY));
            }
        } else {
            // 去售出
            wb = rb.getTask().getTo();
            if (rb.getWorkbenchIdx() == wb.getWorkbenchIdx()) {
                // 售出行为
                rb.addAction(this.sellAction.update(ActionType.SELL));
            }
        }
    }

}
