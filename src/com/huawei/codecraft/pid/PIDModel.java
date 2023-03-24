package com.huawei.codecraft.pid;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.motion.MotionState;
import com.huawei.codecraft.utils.Utils;
import com.huawei.codecraft.vector.Coordinate;

public class PIDModel {
    // 角度PID参数
    private double KpAngle = 10;
    private double KiAngle = 0.1;
    private double KdAngle = 0.0;

    private double lastErrAngle = 0;
    private double intAngle = 0;
    // 积分值上限
    private double intMaxAngle = 0.5;

    // 距离PID参数
    private double KpDist = 6.2;
    private double KiDist = 0.1;
    private double KdDist = 1;

    private double KpDistLoad = 6.2;
    private double KiDistLoad = 0.1;
    private double KdDistLoad = 1;

    private double lastErrDist = 0;
    private double intDist = 0;
    // 积分值上限
    private double intMaxDist = 5.7;

    private Robot rb;

    public PIDModel(Robot rb) {
        this.rb = rb;
    }

    public PIDModel(Robot rb, String[] args) {
        this.rb = rb;
        KpDist = Double.parseDouble(args[1]);
        KiDist = Double.parseDouble(args[2]);
        KdDist = Double.parseDouble(args[3]);

        KpDistLoad = Double.parseDouble(args[4]);
        KiDistLoad = Double.parseDouble(args[5]);
        KdDistLoad = Double.parseDouble(args[6]);
    }

    public void update(PIDModel model) {
        this.KpAngle = model.KpAngle;
        this.KiAngle = model.KiAngle;
        this.KdAngle = model.KdAngle;
        this.lastErrAngle = model.lastErrAngle;
        this.intAngle = model.intAngle;
        this.intMaxAngle = model.intMaxAngle;
        this.KpDist = model.KpDist;
        this.KiDist = model.KiDist;
        this.KdDist = model.KdDist;
        this.KpDistLoad = model.KpDistLoad;
        this.KiDistLoad = model.KiDistLoad;
        this.KdDistLoad = model.KdDistLoad;
        this.lastErrDist = model.lastErrDist;
        this.intDist = model.intDist;
        this.intMaxDist = model.intMaxDist;
        this.rb = model.rb;
    }

    public void update(double KpDist, double KiDist, double KdDist, double KpDistLoad, double KiDistLoad,
            double KdDistLoad) {
        this.KpDist = KpDist;
        this.KiDist = KiDist;
        this.KdDist = KdDist;
        this.KpDistLoad = KpDistLoad;
        this.KiDistLoad = KiDistLoad;
        this.KdDistLoad = KdDistLoad;
    }

    /** 根据当前MotionState与目标POS，计算控制律 */
    public double[] control(MotionState ms, Coordinate targetPos) {
        Coordinate pos = ms.getPos();
        double posX = pos.getX();
        double posY = pos.getY();
        double heading = ms.getHeading();

        // 获取距离误差
        double distErr = Utils.computeDistance(pos, targetPos);

        // 计算角度误差，根据两者的坐标
        double diffX = targetPos.getX() - posX;
        double diffY = targetPos.getY() - posY;
        double quadrant = 1.; // 象限
        if (diffY < 0) {
            quadrant = -1.;
        }
        double angle = 0;
        if (distErr > 0.04) {
            angle = quadrant * Math.acos(diffX / distErr);
        }

        double angleErr = angle - ms.getHeading();

        if (angleErr > Math.PI) {
            angleErr = angleErr - 2 * Math.PI;
        } else if (angleErr < -Math.PI) {
            angleErr = angleErr + 2 * Math.PI;
        }

        /// 判断离墙是否太近
        double minWallDist = 4;
        // 减速系数
        double deceleration = 0.3;
        double minDistWallX = posX < 25 ? posX : (50 - posX);
        double minDistWallY = posY < 25 ? posY : (50 - posY);
        boolean closetoX = false;
        boolean closetoY = false;
        // 根据当前角度与离墙距离的关系，进行距离误差的修正
        if (posX < minWallDist && (heading > Math.PI * 5 / 8 || heading < -Math.PI * 5 / 8)) {
            closetoX = true;
        } else if (posX > 50 - minWallDist && heading < Math.PI * 3 / 8 && heading > -Math.PI * 3 / 8) {
            closetoX = true;
        }
        if (posY < minWallDist && heading < -Math.PI / 8 && heading > -Math.PI * 7 / 8) {
            closetoY = true;
        } else if (posY > 50 - minWallDist && heading > Math.PI / 8 && heading < Math.PI * 7 / 8) {
            closetoY = true;
        }

        if (closetoX || closetoY) {
            distErr = Math.min(distErr, deceleration * Math.min(minDistWallX, minDistWallY));
        }

        // 距离环控制
        double lineVelocity = 0;

        // 根据载重情况决定使用何种pid
        if (rb.isLoaded()) {
            lineVelocity = (KpDistLoad * distErr + KiDistLoad * intDist + KdDistLoad * (distErr - lastErrDist));
        } else {
            lineVelocity = (KpDist * distErr + KiDist * intDist + KdDist * (distErr - lastErrDist));
        }

        // 记录误差
        lastErrDist = distErr;
        intDist += distErr;
        intDist = Math.min(intDist, intMaxDist);
        intDist = Math.max(intDist, -intMaxDist);

        // 角度环控制
        double angularVelocity = (KpAngle * angleErr + KiAngle * intAngle + KdAngle * (angleErr - lastErrAngle));

        // 记录误差
        lastErrAngle = angleErr;
        intAngle += angleErr;
        intAngle = Math.min(intAngle, intMaxAngle);
        intAngle = Math.max(intAngle, -intMaxAngle);

        // 对输出值进行规范化，保证输出的最大值是规范的
        lineVelocity = lineVelocity > Const.MAX_FORWARD_VELOCITY ? Const.MAX_FORWARD_VELOCITY : lineVelocity;
        lineVelocity = lineVelocity < Const.MAX_BACKWARD_VELOCITY ? Const.MAX_BACKWARD_VELOCITY : lineVelocity;
        angularVelocity = angularVelocity > Const.MAX_ANGULAR_VELOCITY ? Const.MAX_ANGULAR_VELOCITY : angularVelocity;
        angularVelocity = angularVelocity < -Const.MAX_ANGULAR_VELOCITY ? -Const.MAX_ANGULAR_VELOCITY : angularVelocity;

        // 策略：角度大就先停止
        // if (Math.abs(angleErr) > Math.PI / 32) {
        // lineVelocity = 0;
        // }

        // 策略:角度越大,速度越小
        if (Math.abs(angleErr) > Math.PI / 4) {
            lineVelocity = lineVelocity * deceleration / Math.abs(angleErr);
        }

        // 返回PID结果
        return new double[] { lineVelocity, angularVelocity };
    }
}
