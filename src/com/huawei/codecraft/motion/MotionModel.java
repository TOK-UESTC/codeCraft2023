package com.huawei.codecraft.motion;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.agent.Robot;

public class MotionModel {
    public static final double FRAME_TIME = 0.02; // 时间常量
    public static final double LINEAR_ACC = 19.591362775; // 单位：m/s
    public static final double LOADED_LINEAR_ACC = 14.081; // 单位：m/s
    public static final double ANGULAR_ACC = 38.695931425; // 单位：rad/s
    public static final double LOADED_ANGULAR_ACC = 20.130082965; // 单位：rad/s

    public static final double PI = Math.PI;
    public static final double sqrtPI = Math.sqrt(PI);
    public static final double MIN_ERROR = 0.0001;

    /**
     * 根据输入进行预测
     * 
     * @param state: 机器人当前状态
     * @param frag:  机器人下一个切片
     */
    private static MotionState predictFrag(MotionState state, MotionFrag frag) {
        // 预测角度
        double heading = state.getHeading();
        heading += state.getW() * frag.getT() + 0.5 * frag.getAngularAcc() * frag.getT() * frag.getT();
        // 恢复到-pai~pai
        if (heading > Math.PI) {
            heading -= 2 * Math.PI;
        } else if (heading < -Math.PI) {
            heading += 2 * Math.PI;
        }
        // 预测位置
        double x = state.getPosX();
        double y = state.getPosY();
        // 加速度不为零
        if (frag.getAngularAcc() >= MIN_ERROR) {
            x += getIntegralXFront(state.vMod(), frag.getAngularAcc(), state.getHeading(), state.getW(), frag.getT());
            x -= getIntegralXFront(state.vMod(), frag.getAngularAcc(), state.getHeading(), state.getW(), 0);

            x += getIntegralXBack(frag.getAngularAcc(), frag.getLinearAcc(), state.getHeading(), state.getW(),
                    frag.getT());
            x -= getIntegralXBack(frag.getAngularAcc(), frag.getLinearAcc(), state.getHeading(), state.getW(), 0);

            y += getIntegralYFront(state.vMod(), frag.getAngularAcc(), state.getHeading(), state.getW(), frag.getT());
            y -= getIntegralYFront(state.vMod(), frag.getAngularAcc(), state.getHeading(), state.getW(), 0);

            y += getIntegralYBack(frag.getAngularAcc(), frag.getLinearAcc(), state.getHeading(), state.getW(),
                    frag.getT());
            y -= getIntegralYBack(frag.getAngularAcc(), frag.getLinearAcc(), state.getHeading(), state.getW(), 0);
        } else if (frag.getAngularAcc() < MIN_ERROR && state.getW() >= MIN_ERROR) {
            // 加速度为0，且角速度不为0
            x += getIntegralXAngular0(state.vMod(), frag.getLinearAcc(), state.getHeading(), state.getW(), frag.getT());
            x -= getIntegralXAngular0(state.vMod(), frag.getLinearAcc(), state.getHeading(), state.getW(), 0);
            y += getIntegralYAngular0(state.vMod(), frag.getLinearAcc(), state.getHeading(), state.getW(), frag.getT());
            y -= getIntegralYAngular0(state.vMod(), frag.getLinearAcc(), state.getHeading(), state.getW(), 0);
        } else if (frag.getAngularAcc() < MIN_ERROR && state.getW() < MIN_ERROR) {
            // 加速度为0，且角速度为0
            x += (state.vMod() * frag.getT() + frag.getLinearAcc() * Math.pow(frag.getT(), 2))
                    * Math.cos(state.getHeading());
            y += (state.vMod() * frag.getT() + frag.getLinearAcc() * Math.pow(frag.getT(), 2))
                    * Math.sin(state.getHeading());
        }

        // 更新state并返回
        state.setPosX(x);
        state.setPosY(y);
        state.setHeading(heading);
        state.setVx((state.vMod() + frag.getLinearAcc() * frag.getT()) * Math.cos(state.getHeading()));
        state.setVy((state.vMod() + frag.getLinearAcc() * frag.getT()) * Math.sin(state.getHeading()));
        state.setW(state.getW() + frag.getAngularAcc() * frag.getT());

        return state;
    }

    /**
     * 根据输入进行预测
     * 
     * @param state:                 上一帧得到的预测状态
     * @param targetVelocity:        目标速度
     * @param targetAngularVelocity: 目标角度
     */
    public static MotionState predict(Robot rb, double targetVelocity, double targetAngularVelocity) {
        // 组装一个state
        MotionState state = new MotionState(rb);
        // 计算合速度v0
        double v0 = state.vMod();

        // 根据当前的负载情况确定加速度值
        double tempAngularAcc = 0;
        double tempLinearAcc = 0;
        if (!state.isLoaded()) {
            tempAngularAcc = targetAngularVelocity > state.getW() ? ANGULAR_ACC : -ANGULAR_ACC;
            tempLinearAcc = targetVelocity > v0 ? LINEAR_ACC : -LINEAR_ACC;
        } else {
            tempAngularAcc = targetAngularVelocity > state.getW() ? LOADED_ANGULAR_ACC : -LOADED_ANGULAR_ACC;
            tempLinearAcc = targetVelocity > v0 ? LOADED_LINEAR_ACC : -LOADED_LINEAR_ACC;
        }

        // 根据目标角速度与当前角速度以及加速度定值计算加速时间
        double tAngle = (targetAngularVelocity - state.getW()) / tempAngularAcc;
        // 根据加速时间计算线速度加速时间
        double tV = (targetVelocity - v0) / tempLinearAcc;

        List<MotionFrag> frags = new ArrayList<>();
        if (tV >= FRAME_TIME && tAngle >= FRAME_TIME) {
            // 均处于加速状态,分为四种，加速加速，加速减速，减速加速，减速减速
            frags.add(new MotionFrag(FRAME_TIME, tempLinearAcc, tempAngularAcc));
        } else if (tV < FRAME_TIME && tAngle < FRAME_TIME) {
            // 两个均处于匀速状态
            if (tV < MIN_ERROR && tAngle < MIN_ERROR) {
                // 两个均处于匀速状态
                frags.add(new MotionFrag(FRAME_TIME, 0, 0));
            } else if (tV >= tAngle) {
                // 两个均处于变匀速状态,判断分段情况
                // 说明角速度加速时间大于线速度加速时间，分为三段
                // 第一段，角速度加速，线速度加速
                frags.add(new MotionFrag(tAngle, tempLinearAcc, tempAngularAcc));
                // 第二段，角速度匀速，线速度加速
                frags.add(new MotionFrag(tV - tAngle, tempLinearAcc, 0));
                // 第三段，角速度匀速，线速度匀速
                frags.add(new MotionFrag(FRAME_TIME - tV, 0, 0));
            } else {
                // 说明线速度加速时间大于角速度加速时间，分为三段
                // 第一段，线速度加速，角速度加速
                frags.add(new MotionFrag(tV, tempLinearAcc, tempAngularAcc));
                // 第二段，线速度匀速，角速度加速
                frags.add(new MotionFrag(tAngle - tV, 0, tempAngularAcc));
                // 第三段，线速度匀速，角速度匀速
                frags.add(new MotionFrag(FRAME_TIME - tAngle, 0, 0));
            }
        } else {
            // 一个处于加速状态，一个处于变匀速状态
            if (tV >= FRAME_TIME) {
                // 线速度处于加速状态,角速度变匀速
                frags.add(new MotionFrag(tAngle, tempLinearAcc, tempAngularAcc));
                frags.add(new MotionFrag(FRAME_TIME - tAngle, tempLinearAcc, 0));
            } else if (tAngle >= FRAME_TIME) {
                // 角速度处于加速状态,线速度变匀速
                frags.add(new MotionFrag(tV, tempLinearAcc, tempAngularAcc));
                frags.add(new MotionFrag(FRAME_TIME - tV, 0, tempAngularAcc));
            }
        }

        MotionState result = new MotionState(state);
        for (MotionFrag frag : frags) {
            result = predictFrag(result, frag);
        }

        return result;
    }

    /**
     * 获取x轴积分后项结果,也就是对axcos(theta0+omega0t+alpha*t^2/2)求积分
     * 
     */
    private static double getIntegralXBack(double angularAcc, double linearAcc, double theta0, double omega0,
            double t) {
        // 根据公式计算
        // (1/(ANGULAR_ACC^(3/2)))*v0
        double result = (1 / pow(angularAcc, 1.5)) * linearAcc;

        double a = (pow(omega0, 2)) / (2 * angularAcc) - theta0;
        double b = (angularAcc * t + omega0) / (sqrt(angularAcc) * sqrtPI);
        double c = omega0 * sqrtPI;

        // -omega0*sqrtPI*cos(((omgea0^2)/(2*ANGULAR_ACC))-theta0)*FresnelC((ANGULAR_ACC*FRAME_TIME+omega0)/(Math.sqrt(ANGULAR_ACC)*sqrtPI))
        double item1 = -c * Math.cos(a) * FresnelC(b);
        // -omega0*sqrtPI*sin(((omgea0^2)/(2*ANGULAR_ACC))-theta0)*FresnelS((ANGULAR_ACC*FRAME_TIME+omega0)/(Math.sqrt(ANGULAR_ACC)*sqrtPI))
        double item2 = -c * Math.sin(a) * FresnelS(b);
        // Math.sqrt(ANGULAR_ACC)*sin((ANGULAR_ACC*pow(FRAME_TIME,2)/2)+theta0+omegea0*FRAME_TIME)
        double item3 = sqrt(angularAcc) * Math.sin((angularAcc * pow(t, 2) / 2) + theta0 + omega0 * t);
        return result * (item1 + item2 + item3);
    }

    // 获取x轴积分结果，加速度为0，角速度不为零
    private static double getIntegralXAngular0(double v0, double linearAcc, double theta0, double omega0, double t) {
        double item1 = omega0 * (v0 + linearAcc * t) * Math.sin(theta0 + omega0 * t);
        double item2 = linearAcc * Math.cos(theta0 + omega0 * t);
        return (item1 + item2) / Math.pow(omega0, 2);
    }

    // 获取y轴积分结果，加速度为0，角速度不为零
    private static double getIntegralYAngular0(double v0, double linearAcc, double theta0, double omega0, double t) {
        double item1 = -omega0 * (v0 + linearAcc * t) * Math.cos(theta0 + omega0 * t);
        double item2 = linearAcc * Math.sin(theta0 + omega0 * t);
        return (item1 + item2) / Math.pow(omega0, 2);
    }

    // 获取x轴积分前项结果,也就是对v0cos(theta0+omega0t+alpha*t^2/2)求积分
    private static double getIntegralXFront(double v0, double angularAcc, double theta0, double omega0, double t) {
        // 根据公式计算
        // sqrtPI*v0/Math.sqrt(ANGULAR_ACC)
        double result = sqrtPI * v0 / sqrt(angularAcc);

        double a = (pow(omega0, 2)) / (2 * angularAcc) - theta0;
        double b = (angularAcc * t + omega0) / (sqrt(angularAcc) * sqrtPI);

        // cos((Math.pow(ANGULAR_ACC,2)/2*ANGULAR_ACC)-theta0)*FresnelC((ANGULAR_ACC*FRAME_TIME+omega0)/(Math.sqrt(ANGULAR_ACC)*sqrtPI))
        double item1 = Math.cos(a) * FresnelC(b);
        // sin((Math.pow(ANGULAR_ACC,2)/2*ANGULAR_ACC)-theta0)*FresnelS((ANGULAR_ACC*FRAME_TIME+omega0)/(Math.sqrt(ANGULAR_ACC)*sqrtPI))
        double item2 = Math.sin(a) * FresnelS(b);
        return result * (item1 + item2);
    }

    // 获取y轴积分前项结果，也就是对v0sin(theta0+omega0t+alpha*t^2/2)求积分
    private static double getIntegralYFront(double v0, double angularAcc, double theta0, double omega0, double t) {
        // 根据公式计算
        // sqrtPI*v0/Math.sqrt(ANGULAR_ACC)
        double result = sqrtPI * v0 / sqrt(angularAcc);

        double a = (pow(omega0, 2)) / (2 * angularAcc) - theta0;
        double b = (angularAcc * t + omega0) / (sqrt(angularAcc) * sqrtPI);

        // cos((Math.pow(ANGULAR_ACC,2)/2*ANGULAR_ACC)-theta0)*FresnelS((ANGULAR_ACC*FRAME_TIME+omega0)/(Math.sqrt(ANGULAR_ACC)*sqrtPI))
        double item1 = Math.cos(a) * FresnelS(b);
        // sin((pow(ANGULAR_ACC,2)/2*ANGULAR_ACC)-theta0)*FresnelC((ANGULAR_ACC*FRAME_TIME+omega0)/(Math.sqrt(ANGULAR_ACC)*sqrtPI))
        double item2 = -Math.sin(a) * FresnelC(b);
        return result * (item1 + item2);
    }

    // 获取y轴积分后项结果，也就是对axsin(theta0+omega0t+alpha*t^2/2)求积分
    private static double getIntegralYBack(double angularAcc, double linearAcc, double theta0, double omega0,
            double t) {
        // 根据公式计算
        // (1/(ANGULAR_ACC^(3/2)))*v0
        double result = -(1 / pow(angularAcc, 1.5)) * linearAcc;

        double a = (pow(omega0, 2)) / (2 * angularAcc) - theta0;
        double b = (angularAcc * t + omega0) / (sqrt(angularAcc) * sqrtPI);
        double c = omega0 * sqrtPI;

        double item1 = -c * Math.sin(a) * FresnelC(b);
        double item2 = c * Math.cos(a) * FresnelS(b);
        double item3 = sqrt(angularAcc) * Math.cos((angularAcc * pow(t, 2) / 2) + theta0 + omega0 * t);
        return result * (item1 + item2 + item3);
    }

    // 菲涅尔函数C
    private static double FresnelC(double x) {
        // 根据公式计算
        double k1 = x;
        double k2 = pow(PI, 2) * pow(x, 5) / 40;
        double k3 = pow(PI, 4) * pow(x, 9) / 3456;
        double k4 = pow(PI, 6) * pow(x, 13) / 599040;

        return k1 - k2 + k3 - k4;
    }

    // 菲涅尔函数S
    private static double FresnelS(double x) {
        // 根据公式计算
        double xp3 = pow(x, 3);
        double xp4 = pow(x, 4);
        double PI2 = pow(PI, 2);
        double PI2xp4 = PI2 * xp4;

        return PI * xp3 * (1 / 6 + PI2xp4 * (-1 / 336 + PI2xp4 * (1 / 42240 - PI2xp4 / 9676800)));
    }

    private static double pow(double x, double p) {
        if (x < 0) {
            return -Math.pow(-x, p);
        }
        return Math.pow(x, p);
    }

    private static double sqrt(double x) {
        if (x < 0) {
            return -Math.sqrt(-x);
        }
        return Math.sqrt(x);
    }
}