package com.huawei.codecraft.utils;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;

/**
 * @description: 磁力模型主要用来防止碰撞，满足以下要求
 *               1. 机器人之间存在斥力，斥力大小与机器人半径有关(负载变换，模型会有改变)
 *               2. 机器人与墙壁存在斥力，斥力大小与机器人半径有关
 *               3. 目的工作台对机器人存在引力，保持最大引力
 *
 *               以上的引力与斥力和题目中的速度是等价的。
 */

public class MagneticForceModel {

    /**
     * 计算机器人r2对r1的斥力,
     */
    static public MagneticForce robotMagneticForceEquation(Robot r1, Robot r2) {
        double dis = Utils.computeDistance(r1.getPos(), r2.getPos());
        // TODO: 这里先不为机器人添加属性，直接用题目中给的数据
        double r1Radius = r1.getProductType() == 0 ? 0.45 : 0.53;
        double r2Radius = r2.getProductType() == 0 ? 0.45 : 0.53;

        // 根据电场力，K为常数, 可调
        double K = 0.03;
        double x = dis - r1Radius - r2Radius;
        // x不能过小
        x = x < 0.5 ? 0.5 : x;
        // F为斥力大小, K*q1*q2/r^2
        double F = K * (20 * Math.PI * (Math.pow(r1Radius, 2) * 20 * Math.PI * Math.pow(r2Radius, 2))) / Math.pow(x, 2);
        // x轴方向斥力
        double Fx = F * (r1.getPos().getX() - r2.getPos().getX()) / dis;
        double Fy = F * (r1.getPos().getY() - r2.getPos().getY()) / dis;

        return new MagneticForce(Fx, Fy);
    }

    /**
     * @description: 计算墙体对机器人的斥力
     */
    static public MagneticForce wallMagneticForceEquation(Robot r) {
        double E = 0.05;
        double radius = r.getProductType() == 0 ? 0.45 : 0.53;
        double numerator = E * (20 * Math.PI * (Math.pow(radius, 2)));
        double MIN_DISTANCE = 0.1;
        // 计算公式：Eq E为电场强度, 但并非匀强电场，与位置有关
        double xLeft = (r.getPos().getX() - radius) < MIN_DISTANCE ? MIN_DISTANCE : (r.getPos().getX() - radius);
        double left = numerator / Math.pow(xLeft, 2);

        double xRight = (50.0 - r.getPos().getX() - radius) < MIN_DISTANCE ? MIN_DISTANCE
                : (50.0 - r.getPos().getX() - radius);
        double right = numerator / Math.pow(xRight, 2);

        double xTop = (50.0 - r.getPos().getY() - radius) < MIN_DISTANCE ? MIN_DISTANCE
                : (50.0 - r.getPos().getY() - radius);
        double top = numerator / Math.pow(xTop, 2);

        double xBottom = (r.getPos().getY() - radius) < MIN_DISTANCE ? MIN_DISTANCE : (r.getPos().getY() - radius);
        double bottom = numerator / Math.pow(xBottom, 2);

        double Fx = left - right;
        double Fy = bottom - top;

        return new MagneticForce(Fx, Fy);
    }

    /**
     * @description: 计算目标工作台对机器人的吸引力
     */
    static public MagneticForce workbenchMagneticForceEquation(Robot r, Workbench wb) {
        // 任何目标工作台对机器人都是满吸引力
        double F = 6.0;
        // 方向
        double dis = Utils.computeDistance(r.getPos(), wb.getPos()) + 0.000001;
        double Fx = F * (wb.getPos().getX() - r.getPos().getX()) / dis;
        double Fy = F * (wb.getPos().getY() - r.getPos().getY()) / dis;

        return new MagneticForce(Fx, Fy);
    }

    /**
     * @description:
     * @param r:      机器人
     * @param target: 目标运动状态
     */
    static public MoveInstruct computeMoveInstruct(Robot r, MagneticForce target) {
        // 工作台到机器人x轴方向
        double x = target.getFx();
        // 工作台到机器人y轴方向
        double y = target.getFy();

        double mod = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double quadrant = 0.; // 象限
        if (x < 0 && y > 0) {
            quadrant = 1.;
        }
        if (x < 0 && y < 0) {
            quadrant = -1.;
        }
        // TODO: mod为0, 说明机器人指令是机器人停下
        double targetRadian = Math.acos(x / mod) + quadrant * Math.PI; // (-pi/2, pi/2)
        // TODO:机器人的forwar和指令中的forward意义不一致，需要注意
        double radian = targetRadian - r.getHeading();

        return new MoveInstruct(radian, mod);
    }

}

class MoveInstruct {

    // 旋转弧度
    private double radian;
    // 前进速度
    private double forward;

    public MoveInstruct(double radian, double forward) {
        this.radian = radian;
        this.forward = forward;
    }

    public double getRadian() {
        return radian;
    }

    public void setRadian(double radian) {
        this.radian = radian;
    }

    public double getForward() {
        return forward;
    }

    public void setForward(double forward) {
        this.forward = forward;
    }

}
