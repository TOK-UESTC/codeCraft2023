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

        // 如果上一轮存在预测，那么就保存预测误差
        // if (lastPredictPos != null) {
        // // 横轴误差
        // double prediffX = lastPredictPos.getX() - pos.getX();
        // // 纵轴误差
        // double prediffY = lastPredictPos.getY() - pos.getY();
        // // 计算距离误差
        // double predistanceError = Math.sqrt(Math.pow(prediffX, 2) +
        // Math.pow(prediffY, 2));
        // // 计算角度误差
        // double preangle = getHeading() - lastPredictHeading;
        // // // 将四个误差保存到txt文件
        // // try {
        // // FileWriter fw = new FileWriter("..\\PID\\predict.txt", true);
        // // fw.append(prediffX + " " + prediffY + " " + predistanceError + " " +
        // // preangle);
        // // fw.append("\r");
        // // fw.close();
        // // } catch (IOException e) {
        // // e.printStackTrace();
        // // }
        // }

        // 获取当前目标工作台,根据是否持有物品判断
        Workbench wb = rb.getProductType() == 0 ? rb.getTask().getFrom() : rb.getTask().getTo();

        double[] controlFactor = rb.control(new MotionState(rb), wb.getPos());
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
                // double posAngle =
                // task.getTo().getPos().sub(task.getFrom().getPos()).getAngle();

                // if (Math.abs(posAngle - heading) < Math.PI / 32) {
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
