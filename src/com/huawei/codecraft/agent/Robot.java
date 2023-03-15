package com.huawei.codecraft.agent;

import java.util.ArrayList;

import com.huawei.codecraft.action.Action;
import com.huawei.codecraft.action.MagneticForce;
import com.huawei.codecraft.constants.ActionType;
import com.huawei.codecraft.task.Task;
import com.huawei.codecraft.task.TaskChain;
import com.huawei.codecraft.utils.Coordinate;
import com.huawei.codecraft.utils.Velocity;

public class Robot {

    private int workbenchIdx; // 所处工作台下标, -1表示没有处于任何工作台, [0, K-1]表是某工作台下标
    private int productType; // 携带物品类型[0, 7], 0表示未携带物品
    private double timeCoefficients; // 时间价值系数 [0.8, 1]
    private double collisionCoefficients; // 碰撞价值系数 [0.8, 1]
    private double angularVelocity; // 角速度 单位：弧度每秒， 正数表示顺时针， 负数表示逆时针
    private Velocity velocity; // 线速度， 二维向量描述, m/s
    private double heading; // 朝向 [-pi, pi] 0 表示右方向, pi/2表示上方向
    private Coordinate pos; // 机器人坐标位置
    private Task task; // 机器人当前任务
    private TaskChain taskChain; // 任务链
    private ArrayList<Action> actions; // 机器人当前动作序列

    // PID参数
    private double Kp = 0.7;
    private double Ki = 0.0;
    private double Kd = 0.0;
    private double lastError = 0;
    private double integral = 0;
    // 积分值上限
    private double integralMax = 0.5;

    public Robot(Coordinate pos) {
        this.pos = pos;
        this.workbenchIdx = -1;
        this.productType = 0;
        this.timeCoefficients = 1;
        this.collisionCoefficients = 1;
        this.angularVelocity = 0;
        this.velocity = null;
        this.heading = 0;
        this.task = null;
        this.taskChain = null;
        this.actions = new ArrayList<Action>();
    }

    /** 更新所有数据 */
    public void update(String[] info) {
        this.workbenchIdx = Integer.parseInt(info[0]);
        this.productType = Integer.parseInt(info[1]);
        this.timeCoefficients = Double.parseDouble(info[2]);
        this.collisionCoefficients = Double.parseDouble(info[3]);
        this.angularVelocity = Double.parseDouble(info[4]);
        this.velocity = new Velocity(Double.parseDouble(info[5]), Double.parseDouble(info[6]));
        this.heading = Double.parseDouble(info[7]);
        this.pos = new Coordinate(Double.parseDouble(info[8]), Double.parseDouble(info[9]));
    }

    /** 机器人根据当前任务和状态进行动作决策。将决策Action输入到列表中，等待执行 */
    public void step() {
        // 清空动作列表
        actions.clear();

        // TODO: 决策，添加到Actions中
        actions.add(new Action(ActionType.FORWARD, 6));
    }

    // 动作规划测试使用
    public void step(Coordinate destination) {
        // 清空动作列表
        actions.clear();
        // // 测试代码，直接前进至(0, 0)
        // actions.addAll(moveTo(destination));
    }

    // 动作规划测试使用
    public void step(MagneticForce magneticForce) {
        // 清空动作列表
        actions.clear();
        // // 测试代码，直接前进至(0, 0)
        actions.addAll(moveTo(magneticForce));

    }

    // 输入目标虚拟力，产生需要的Action(前进与转向)
    private ArrayList<Action> moveTo(MagneticForce magneticForce) {
        ArrayList<Action> actions = new ArrayList<Action>();
        // 比较当前位置与目标位置的距离
        double distance = magneticForce.getMod();
        // 如果距离小于0.4,就停止,死区,这一帧实际上应该执行任务,获取新任务,运动可以不静止
        if (distance < 0.4) {
            // 速度归零,角速度归零
            actions.add(new Action(ActionType.FORWARD, 0));
            actions.add(new Action(ActionType.ROTATE, 0));
        } else {
            // 比较当前朝向与目标朝向的角度,用坐标计算得到的也是依照原有坐标轴的角度
            double angle = magneticForce.getAngle();
            double error = heading - angle;
            if (error > Math.PI) {
                error = error - 2 * Math.PI;
            } else if (error < -Math.PI) {
                error = error + 2 * Math.PI;
            }
            // 角度环控制
            double angularVelocity = -(Kp * error + Ki * integral + Kd * (error - lastError));
            lastError = error;
            integral += error;
            // 积分值上限
            integral = Math.min(integral, integralMax);
            integral = Math.max(integral, -integralMax);
            // // 产生转向动作
            // actions.add(new Action("rotate", angularVelocity));
            // // 角度大的时候速度小防止转圈
            // double linespeed = 6 * Math.abs(1 / angularVelocity);
            // // 产生前进动作
            // actions.add(new Action("forward", linespeed));
            // 区分钝角锐角情况
            if (Math.abs(error) > Math.PI / 32) {
                // 如果角速度变化过大，就不进行角度修正,判断标准是角速度发生了正负的大变化
                // 产生转向动作
                actions.add(new Action(ActionType.ROTATE, angularVelocity));
                actions.add(new Action(ActionType.FORWARD, 0));
                // 速度满足角度差越大速度就越小
                // double linespeed = -6 * Math.abs(1 / angularVelocity);
                // // // 产生前进动作
                // actions.add(new Action("forward", linespeed));
            } else {
                // 产生转向动作
                actions.add(new Action(ActionType.ROTATE, angularVelocity));
                // 角度大的时候速度小防止转圈
                double linespeed = 6 * Math.abs(1 / angularVelocity);
                // 产生前进动作
                actions.add(new Action(ActionType.FORWARD, linespeed));
            }
            // PID观察文件
            // File file = new
            // File("C:\\Users\\cyz\\Desktop\\软挑\\WindowsRelease\\PID\\pid.txt");
            // try {
            // BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            // bw.write(angle + " " + forward + " " + error + " " + angularVelocity + " " +
            // distance + "\r");
            // bw.flush();
            // bw.close();
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
        }
        return actions;
    }

    // 输入目标地址，产生需要的Action(前进与转向)
    private ArrayList<Action> moveTo(Coordinate target) {
        ArrayList<Action> actions = new ArrayList<Action>();
        // 比较当前位置与目标位置的距离
        double distance = pos.distance(target);
        // 如果距离小于0.4,就停止,死区,这一帧实际上应该执行任务,获取新任务,运动可以不静止
        if (distance < 0.4) {
            // 速度归零,角速度归零
            actions.add(new Action(ActionType.FORWARD, 0));
            actions.add(new Action(ActionType.ROTATE, 0));
        } else {
            // 比较当前朝向与目标朝向的角度,用坐标计算得到的也是依照原有坐标轴的角度
            double angle = pos.angle(target);
            double error = heading - angle;
            if (error > Math.PI) {
                error = error - 2 * Math.PI;
            } else if (error < -Math.PI) {
                error = error + 2 * Math.PI;
            }
            // 角度环控制
            double angularVelocity = -(Kp * error + Ki * integral + Kd * (error - lastError));
            lastError = error;
            integral += error;
            // 积分值上限
            integral = Math.min(integral, integralMax);
            integral = Math.max(integral, -integralMax);
            // // 产生转向动作
            // actions.add(new Action("rotate", angularVelocity));
            // // 角度大的时候速度小防止转圈
            // double linespeed = 6 * Math.abs(1 / angularVelocity);
            // // 产生前进动作
            // actions.add(new Action("forward", linespeed));
            // 区分钝角锐角情况
            if (Math.abs(error) > Math.PI / 32) {
                // 如果角速度变化过大，就不进行角度修正,判断标准是角速度发生了正负的大变化
                // 产生转向动作
                actions.add(new Action(ActionType.ROTATE, angularVelocity));
                actions.add(new Action(ActionType.FORWARD, 0));
                // 速度满足角度差越大速度就越小
                // double linespeed = -6 * Math.abs(1 / angularVelocity);
                // // // 产生前进动作
                // actions.add(new Action("forward", linespeed));
            } else {
                // 产生转向动作
                actions.add(new Action(ActionType.ROTATE, angularVelocity));
                // 角度大的时候速度小防止转圈
                double linespeed = 6 * Math.abs(1 / angularVelocity);
                // 产生前进动作
                actions.add(new Action(ActionType.FORWARD, linespeed));
            }
            // PID观察文件
            // File file = new
            // File("C:\\Users\\cyz\\Desktop\\软挑\\WindowsRelease\\PID\\pid.txt");
            // try {
            // BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            // bw.write(angle + " " + forward + " " + error + " " + angularVelocity + " " +
            // distance + "\r");
            // bw.flush();
            // bw.close();
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
        }
        return actions;
    }

    public ArrayList<Action> getActions() {
        return actions;
    }

    public void addAction(Action actions) {
        this.actions.add(actions);
    }

    /** 绑定任务链 */
    public void bindChain(TaskChain taskChain) {
        this.taskChain = taskChain;
        // 机器人绑定任务链的时候就会分配任务
        this.task = taskChain.getTasks().get(0);
    }

    /** 获取当前任务链 */
    public TaskChain getTaskChain() {
        return taskChain;
    }

    /**
     * 当前机器人是否分配有任务
     * 
     * @return: true = free
     */
    public boolean isFree() {
        return task == null;
    }

    /**
     * 获取机器人当前收益
     *
     * @param: Max，当两个系数都为1的时候的最大收益
     */
    public double profit(boolean max) {
        if (task == null) {
            return -1;
        } else if (max) {
            return task.getProfit(1, 1);
        } else {
            return task.getProfit(timeCoefficients, collisionCoefficients);
        }
    }

    /** 返回当前所处的工作台id */
    public int getWorkbenchIdx() {
        return workbenchIdx;
    }

    /** 获取当前携带的物品种类 */
    public int getProductType() {
        return productType;
    }

    /** 获取时间因子 */
    public double getTimeCoefficients() {
        return timeCoefficients;
    }

    /** 获取碰撞因子 */
    public double getCollisionCoefficients() {
        return collisionCoefficients;
    }

    /** 获取角速度 */
    public double getAngularVelocity() {
        return angularVelocity;
    }

    /** 获取线速度 */
    public Velocity getVelocity() {
        return velocity;
    }

    /** 获取当前朝向 */
    public double getHeading() {
        return heading;
    }

    /** 获取位置 */
    public Coordinate getPos() {
        return pos;
    }

    /** 设定机器人当前任务 */
    public void setTask(Task task) {
        this.task = task;
    }

    /** 获取机器人当前任务 */
    public Task getTask() {
        return task;
    }

}
