package com.huawei.codecraft.pid;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.motion.MotionState;
import com.huawei.codecraft.utils.Utils;
import com.huawei.codecraft.vector.Coordinate;

public class PIDModel {
    // 角度PID参数
    private double Kp2Angle = 10;
    private double Ki2Angle = 0.1;
    private double Kd2Angle = 0.0;

    private double lastError2Angle = 0;
    private double integral2Angle = 0;
    // 积分值上限
    private double integralMax2Angle = 0.5;

    // 距离PID参数
    private double Kp2Distance = 4.2;
    private double Ki2Distance = 0.1;
    private double Kd2Distance = 1;

    private double lastError2Distance = 0;
    private double integral2Distance = 0;
    // 积分值上限
    private double integralMax2Distance = 0.7;

    // 机器人对象，方便直接调用
    private Robot rb;

    public PIDModel(Robot rb) {
        this.rb = rb;
    }

    public PIDModel(PIDModel model) {
        this.Kp2Angle = model.Kp2Angle;
        this.Ki2Angle = model.Ki2Angle;
        this.Kd2Angle = model.Kd2Angle;
        this.lastError2Angle = model.lastError2Angle;
        this.integral2Angle = model.integral2Angle;
        this.integralMax2Angle = model.integralMax2Angle;
        this.Kp2Distance = model.Kp2Distance;
        this.Ki2Distance = model.Ki2Distance;
        this.Kd2Distance = model.Kd2Distance;
        this.lastError2Distance = model.lastError2Distance;
        this.integral2Distance = model.integral2Distance;
        this.integralMax2Distance = model.integralMax2Distance;
        this.rb = model.rb;
    }

    /** 根据当前MotionState与目标POS，计算控制律 */
    public double[] control(MotionState ms, Coordinate targetPos) {
        Coordinate currPos = ms.getPos();
        double currX = currPos.getX();
        double currY = currPos.getY();
        double currHeading = ms.getHeading();

        // 获取距离误差
        double distanceError = Utils.computeDistance(currPos, targetPos);

        // 计算角度误差，根据两者的坐标
        double diffX = targetPos.getX() - currX;
        double diffY = targetPos.getY() - currY;
        double quadrant = 1.; // 象限
        if (diffY < 0) {
            quadrant = -1.;
        }
        double angle = 0;
        if (distanceError > 0.04) {
            angle = quadrant * Math.acos(diffX / distanceError);
        }

        double error = angle - ms.getHeading();

        if (error > Math.PI) {
            error = error - 2 * Math.PI;
        } else if (error < -Math.PI) {
            error = error + 2 * Math.PI;
        }

        // 判断离墙是否太近
        double minWallDistThreshold = 2;
        // 减速系数
        double deceleration = 0.4;
        double minDistance2WallX = currX < 25 ? currX : 50 - currX;
        double minDistance2WallY = currY < 25 ? currY : 50 - currY;

        // 根据当前角度与离墙距离的关系，进行距离误差的修正
        if (currX < minWallDistThreshold && currHeading > Math.PI * 3 / 4 && currHeading < -Math.PI * 3 / 4) {
            distanceError = Math.min(distanceError, deceleration * minDistance2WallX);
        } else if (currX > 50 - minWallDistThreshold && currHeading < Math.PI / 4 && currHeading > -Math.PI / 4) {
            distanceError = Math.min(distanceError, deceleration * minDistance2WallX);
        }
        if (currY < minWallDistThreshold && currHeading < -Math.PI / 4 && currHeading > -Math.PI * 3 / 4) {
            distanceError = Math.min(distanceError, deceleration * minDistance2WallY);
        } else if (currY > 50 - minWallDistThreshold && currHeading > Math.PI / 4 && currHeading < Math.PI * 3 / 4) {
            distanceError = Math.min(distanceError, deceleration * minDistance2WallY);
        }

        // // 根据角度偏差，进行距离误差的修正
        // if (Math.abs(error) > Math.PI / 4) {
        // distanceError = distanceError * deceleration;
        // }

        // 距离环控制
        double lineVelocity = (Kp2Distance * distanceError + Ki2Distance * integral2Distance
                + Kd2Distance * (distanceError - lastError2Distance));
        lastError2Distance = distanceError;
        integral2Distance += distanceError;
        // 积分值上限
        integral2Distance = Math.min(integral2Distance, integralMax2Distance);
        integral2Distance = Math.max(integral2Distance, -integralMax2Distance);

        // 角度环控制
        double angularVelocity = (Kp2Angle * error + Ki2Angle * integral2Angle
                + Kd2Angle * (error - lastError2Angle));
        lastError2Angle = error;
        integral2Angle += error;
        // 积分值上限
        integral2Angle = Math.min(integral2Angle, integralMax2Angle);
        integral2Angle = Math.max(integral2Angle, -integralMax2Angle);

        // 对输出值进行规范化，保证输出的最大值是规范的
        lineVelocity = lineVelocity > Const.MAX_FORWARD_VELOCITY ? Const.MAX_FORWARD_VELOCITY : lineVelocity;
        lineVelocity = lineVelocity < Const.MAX_BACKWARD_VELOCITY ? Const.MAX_BACKWARD_VELOCITY : lineVelocity;
        angularVelocity = angularVelocity > Const.MAX_ANGULAR_VELOCITY ? Const.MAX_ANGULAR_VELOCITY : angularVelocity;
        angularVelocity = angularVelocity < -Const.MAX_ANGULAR_VELOCITY ? -Const.MAX_ANGULAR_VELOCITY : angularVelocity;

        // 策略：角度大就先停止
        // if (Math.abs(error) > Math.PI / 32) {
        //     lineVelocity = 0;
        // }

        // 返回PID结果
        return new double[] { lineVelocity, angularVelocity };
    }
}
