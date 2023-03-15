package com.huawei.codecraft.action;

import java.util.List;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.utils.Utils;

/**
 * @description: 磁力模型主要用来防止碰撞，满足以下要求
 *               1. 机器人之间存在斥力，斥力大小与机器人半径有关(负载变换，模型会有改变)
 *               2. 机器人与墙壁存在斥力，斥力大小与机器人半径有关
 *               3. 目的工作台对机器人存在引力，保持最大引力
 *
 *               以上的引力与斥力和题目中的速度是等价的。
 */

public class ForceModel {
    private static final double K = 0.03; // 电场力常数，可调
    private static final double E = 0.05; // 墙体斥力常数
    private static final double MIN_DISTANCE = 0.5; // 除去半径之后的剩余距离

    static public Force getForce(Robot rb, List<Robot> robotList, List<Workbench> workbenchList) {
        Force force = new Force();

        // 计算机器人施加的合力
        for (Robot robot : robotList) {
            if (robot != rb) {
                force = force.add(ForceModel.getRobotForce(rb, robot));
            }
        }

        // 计算墙体施加的合力
        // force.add(forceModel.getWallForce(rb));

        // 计算工作台引力
        force = force.add(ForceModel.getWorkbenchForce(rb,
                workbenchList.get(20)));
        return force;
    }

    /**
     * 计算机器人r2对r1的斥力,
     */
    static public Force getRobotForce(Robot r1, Robot r2) {
        double distance = Utils.computeDistance(r1.getPos(), r2.getPos());

        double r1Radius = r1.getRadius(), r2Radius = r2.getRadius();

        // 计算除去半径之后的剩余距离
        double x = distance - r1Radius - r2Radius;
        // x不能过小
        x = x < MIN_DISTANCE ? MIN_DISTANCE : x;

        // F为斥力大小, K*q1*q2/r^2
        double q1 = Const.ROBOT_DENSITY * Math.PI * Math.pow(r1Radius, 2);
        double q2 = Const.ROBOT_DENSITY * Math.PI * Math.pow(r2Radius, 2);
        double F = K * q1 * q2 / Math.pow(x, 2);

        // 分解斥力
        double Fx = F * (r1.getPos().getX() - r2.getPos().getX()) / distance;
        double Fy = F * (r1.getPos().getY() - r2.getPos().getY()) / distance;

        return new Force(Fx, Fy);
    }

    /**
     * @description: 计算墙体对机器人的斥力
     */
    static public Force getWallForce(Robot rb) {
        double radius = rb.getRadius();
        double numerator = E * (Const.ROBOT_DENSITY * Math.PI * (Math.pow(radius, 2)));

        double x = rb.getPos().getX();
        double y = rb.getPos().getY();

        // 计算公式：Eq E为电场强度, 但并非匀强电场，与位置有关
        double left = (x - radius) < MIN_DISTANCE ? MIN_DISTANCE : (x - radius);
        double fLeft = numerator / Math.pow(left, 2);

        double right = (Const.MAP_LENGTH - x - radius) < MIN_DISTANCE ? MIN_DISTANCE : (Const.MAP_LENGTH - x - radius);
        double fRight = numerator / Math.pow(right, 2);

        double top = (Const.MAP_LENGTH - y - radius) < MIN_DISTANCE ? MIN_DISTANCE : (Const.MAP_LENGTH - y - radius);
        double fTop = numerator / Math.pow(top, 2);

        double bottom = (y - radius) < MIN_DISTANCE ? MIN_DISTANCE : (y - radius);
        double FBottom = numerator / Math.pow(bottom, 2);

        double Fx = fLeft - fRight;
        double Fy = FBottom - fTop;

        return new Force(Fx, Fy);
    }

    /**
     * @description: 计算目标工作台对机器人的吸引力
     */
    static public Force getWorkbenchForce(Robot rb, Workbench wb) {
        // 任何目标工作台对机器人都是满吸引力
        double F = 6.0;
        // 方向
        double dis = Utils.computeDistance(rb.getPos(), wb.getPos()) + 0.000001;
        double Fx = F * (wb.getPos().getX() - rb.getPos().getX()) / dis;
        double Fy = F * (wb.getPos().getY() - rb.getPos().getY()) / dis;

        return new Force(Fx, Fy);
    }

    /**
     * @description:
     * @param rb:     机器人
     * @param target: 目标运动状态
     */
    static public MoveInstruct computeMoveInstruct(Robot rb, Force target) {
        // 工作台到机器人x轴方向
        double fx = target.getFx();
        // 工作台到机器人y轴方向
        double fy = target.getFy();

        double forceMod = target.getMod();

        double quadrant = 0.; // 象限
        if (fx < 0 && fy > 0) {
            quadrant = 1.;
        }
        if (fx < 0 && fy < 0) {
            quadrant = -1.;
        }

        // TODO: mod为0, 说明机器人指令是机器人停下
        // (-pi/2, pi/2)
        double targetHeading = Math.acos(fx / forceMod) + quadrant * Math.PI;
        double radian = targetHeading - rb.getHeading();

        return new MoveInstruct(radian, forceMod);
    }
}

class MoveInstruct {
    private double radian; // 旋转弧度
    private double forward; // 前进速度

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
