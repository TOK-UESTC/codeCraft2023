package com.huawei.codecraft.motion;

import java.util.List;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.utils.Utils;
import com.huawei.codecraft.vector.Force;

/**
 * @description: 磁力模型主要用来防止碰撞，满足以下要求
 *               1. 机器人之间存在斥力，斥力大小与机器人半径有关(负载变换，模型会有改变)
 *               2. 机器人与墙壁存在斥力，斥力大小与机器人半径有关
 *               3. 目的工作台对机器人存在引力，保持最大引力
 *
 *               以上的引力与斥力和题目中的速度是等价的。
 */

public class ForceModel {
    private static double K = 0.02; // 电场力常数，可调
    private static final double E = 0.1; // 墙体斥力常数
    private static final double MIN_DISTANCE = 0.1; // 除去半径之后的剩余距离

    static public Force getForce(Robot rb, List<Robot> robotList, List<Workbench> workbenchList) {
        Force force = new Force();

        // 计算机器人施加的合力
        /*
         * 1. 如果两个机器人都不携带任务，那么机器人之间没有斥力
         * 2. 如果其中一个机器人携带任务，那么存在单向的斥力，携带者给未携带者斥力
         * 3. 如果都携带任务，在目的地不同时，同时排斥对方
         * 
         * 综上：携带者产生斥力
         */
        for (Robot robot : robotList) {
            if (robot != rb) {
                if (robot.getProductType() == 0) {
                    continue;
                }
                if (rb.getTask() != null && robot.getTask() != null) {
                    if (rb.getTask().getTo().getWorkbenchIdx() == robot.getTask().getTo().getWorkbenchIdx()) {
                        continue;
                    }
                }
                force = (Force) force.add(ForceModel.getRobotForce(rb, robot));
            }
        }

        // 计算墙体施加的合力
        force.add(ForceModel.getWallForce(rb));

        // 计算工作台引力
        if (rb.getTask() == null) {
            return force;
        }

        Workbench wb;
        if (rb.getProductType() == 0) {
            wb = rb.getTask().getFrom();
        } else {
            wb = rb.getTask().getTo();
        }

        force = (Force) force.add(ForceModel.getWorkbenchForce(rb, wb));
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
        K = 0.03;
        K *= 94;
        if (r1Radius > 0.5 && r2Radius > 0.5) {
            K *= 110;
        }

        if (r1Radius < 0.5 && r2Radius < 0.5) {
            K *= 80;
        }
        // x不能过小
        x = x < MIN_DISTANCE ? MIN_DISTANCE : x;

        // F为斥力大小, K*q1*q2/r^2
        // double q1 = Const.ROBOT_DENSITY * Math.PI * Math.pow(r1Radius, 2);
        // double q2 = Const.ROBOT_DENSITY * Math.PI * Math.pow(r2Radius, 2);
        double F = K / Math.pow(x, 2);
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
        double F = 15.0;
        // 方向
        double dis = Utils.computeDistance(rb.getPos(), wb.getPos()) + 0.000001;
        double Fx = F * (wb.getPos().getX() - rb.getPos().getX()) / dis;
        double Fy = F * (wb.getPos().getY() - rb.getPos().getY()) / dis;

        return new Force(Fx, Fy);
    }
}
