package com.huawei.codecraft.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.huawei.codecraft.action.Action;
import com.huawei.codecraft.action.ActionModel;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.motion.MotionModel;
import com.huawei.codecraft.motion.MotionState;
import com.huawei.codecraft.pid.PIDModel;
import com.huawei.codecraft.task.Task;
import com.huawei.codecraft.task.TaskChain;
import com.huawei.codecraft.utils.Utils;
import com.huawei.codecraft.vector.Coordinate;
import com.huawei.codecraft.vector.Vector;
import com.huawei.codecraft.vector.Velocity;

public class Robot {
    private int workbenchIdx; // 所处工作台下标, -1表示没有处于任何工作台, [0, K-1]表是某工作台下标
    private int productType; // 携带物品类型[0, 7], 0表示未携带物品
    private int lastProductType; // 上一帧携带的品
    private double timeCoefficients; // 时间价值系数 [0.8, 1]
    private double collisionCoefficients; // 碰撞价值系数 [0.8, 1]
    private double angularVelocity; // 角速度 单位：弧度每秒， 正数表示顺时针， 负数表示逆时针
    private Velocity velocity; // 线速度， 二维向量描述, m/s
    private double heading; // 朝向 [-pi, pi] 0 表示右方向, pi/2表示上方向
    private Coordinate pos; // 机器人坐标位置

    private Task task; // 机器人当前任务
    private TaskChain taskChain; // 任务链
    private ArrayList<Action> actions; // 机器人当前动作序列
    private int id;
    private int frameId;
    private List<Robot> robotList;
    private Map<Integer, MotionState> motionStates; // 机器人运动状态序列

    private int counter = 0;

    public static int robotID = 0;

    // 路径点，将到task的路径拆解为一个个点
    private List<Coordinate> waypoints;

    // 上一轮预测的位置与朝向
    private Coordinate lastPredictPos = null;
    private double lastPredictHeading = 0;

    // 各种控制器
    private PIDModel PID;
    private ActionModel actionModel;

    public Robot(Coordinate pos, List<Robot> robotList) {
        this.pos = pos;
        this.workbenchIdx = -1;
        this.productType = 0;
        this.lastProductType = 0;
        this.timeCoefficients = 1;
        this.collisionCoefficients = 1;
        this.angularVelocity = 0;
        this.velocity = null;
        this.heading = 0;
        this.task = null;
        this.taskChain = null;
        this.actions = new ArrayList<Action>();
        this.frameId = 0;
        this.robotList = robotList;
        id = robotID;
        robotID += 1;
        motionStates = new HashMap<>();
        this.waypoints = new ArrayList<>();
        this.PID = new PIDModel(this);
        this.actionModel = new ActionModel(this);
    }

    /** 更新所有数据 */
    public void update(String[] info, int frameId) {
        this.workbenchIdx = Integer.parseInt(info[0]);
        this.lastProductType = this.productType;
        this.productType = Integer.parseInt(info[1]);
        this.timeCoefficients = Double.parseDouble(info[2]);
        this.collisionCoefficients = Double.parseDouble(info[3]);
        this.angularVelocity = Double.parseDouble(info[4]);
        this.velocity = new Velocity(Double.parseDouble(info[5]), Double.parseDouble(info[6]));
        this.heading = Double.parseDouble(info[7]);
        this.pos = new Coordinate(Double.parseDouble(info[8]), Double.parseDouble(info[9]));
        this.frameId = frameId;

        counter++;
    }

    /** 机器人根据当前任务和状态进行动作决策。将决策Action输入到列表中，等待执行 */
    public void step() {
        // 清空动作列表
        actions.clear();

        // counter >
        // predict();
        //

        // 更新action列表
        actionModel.generate();
    }

    public Coordinate getCurrentTarget() {
        return waypoints.get(0);
    }

    /**
     * 判断是否到达当前waypoints第一个点位
     * 中转点要距离0.1才到达
     */
    public boolean isReached() {
        // if (task.getFrom().getPos() != getCurrentTarget() && task.getTo().getPos() !=
        // getCurrentTarget()
        // && task.getTo().getPos() != getCurrentTarget()) {
        // return Utils.computeDistance(pos, waypoints.get(0)) < 0.1;
        // }
        return Utils.computeDistance(pos, waypoints.get(0)) < 0.1;
    }

    /** 预测使用 */
    public boolean isReached(MotionState s, Coordinate p) {
        if (s.getPos() != p && s.getPos() != p && s.getPos() != p) {
            return Utils.computeDistance(s.getPos(), p) < 0.1;
        }
        return Utils.computeDistance(s.getPos(), p) < 0.4;
    }

    /** 到达目标点之后，删除第一个点位 */
    public void reach() {
        waypoints.remove(0);
    }

    /** 按照帧数获取motionstate，进行碰撞预判 */
    public MotionState getStateByFrame(int frameId) {
        MotionState result = motionStates.get(frameId);

        if (motionStates.size() == 0) {
            // System.err.printf("states当前为空， frameId: %d\n", frameId);
            return new MotionState(this);
        }

        // 如果当前没有motionState，则以最后的预测位置封装一个新的出来
        if (result == null) {
            Set<Integer> keySet = motionStates.keySet();

            // 将HashMap键集合转换为数组
            Integer[] keys = keySet.toArray(new Integer[keySet.size()]);

            // 定义变量保存当前最大值及其对应的键
            int maxKey = keys[0];

            // 遍历数组以找到最大的键
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] > maxKey) {
                    maxKey = keys[i];
                }
            }

            return new MotionState(motionStates.get(maxKey));
        }

        return result;
    }

    /**
     * 根据当前任务产生预测，暴露在外供给robot直接调用
     * 
     */
    public void predict() {
        if (task == null) {
            return;
        }

        // 在predct中管理waypoint
        waypoints.clear();
        if (productType == 0) {
            waypoints.add(task.getFrom().getPos());
            waypoints.add(task.getTo().getPos());
        } else {
            waypoints.add(task.getTo().getPos());
        }

        motionStates.clear();

        // 复制状态，避免直接对原数据进行操作
        MotionState state = new MotionState(this);
        PIDModel pidModel = new PIDModel(PID);
        int nextFrameId = frameId + 1;

        Map<Integer, PIDModel> pidMap = new HashMap<>();
        pidMap.put(frameId, pidModel);
        // 进行路径查找，只查找到当前waypoint目标完成
        while (true) {
            int K = 5;

            // 检查是否到达waypoints，到达的话，代表探索结束
            if (isReached(state, getCurrentTarget())) {
                break;
            }

            // 得到pid控制量并根据此控制量进行预测
            double[] controlFactor = pidModel.control(state, waypoints.get(0));
            state = MotionModel.predict(state, controlFactor[0], controlFactor[1]);

            // 检查是否有碰撞
            boolean isCollided = false;
            MotionState otherState = null;
            for (Robot rb : robotList) {
                if (rb == this) {
                    continue;
                }
                // 获取同帧下其他机器人状态，判断是否碰撞
                otherState = rb.getStateByFrame(nextFrameId);
                if (Utils.computeDistance(state.getPos(), otherState.getPos()) < 1.2) {
                    isCollided = true;
                    break;
                }
            }

            // 有碰撞，寻找下一个可能的中间点，并放到waypoints的首位
            Coordinate nextWayPointAfterCollided = null;
            if (isCollided) {
                // 遍历下一个可能的节点
                double range = 1.5;
                while (true) {
                    // 回退
                    while (K != 0) {
                        if (motionStates.size() == 1) {
                            // 已经回溯到出发位置
                            break;
                        }
                        motionStates.remove(nextFrameId - 1);
                        pidMap.remove(nextFrameId - 1);
                        nextFrameId--;
                        K--;
                    }
                    boolean isFindNextWaypoint = false;

                    List<Coordinate> nextWaypoints = null;

                    nextWaypoints = searchNextWaypoints(motionStates.get(nextFrameId - 1), state, range);

                    for (Coordinate next : nextWaypoints) {
                        MotionState s = new MotionState(motionStates.get(nextFrameId - 1));
                        PIDModel p = new PIDModel(pidMap.get(nextFrameId - 1));
                        int searchNextFrameId = nextFrameId;
                        while (true) {
                            if (isReached(s, next)) {
                                isFindNextWaypoint = true;
                                nextWayPointAfterCollided = next;
                                break;
                            }
                            double[] searchNextControlFactor = p.control(s, next);
                            s = MotionModel.predict(s, searchNextControlFactor[0], searchNextControlFactor[1]);
                            boolean isSearchCollided = false;
                            MotionState searchOtherState = null;

                            // 检测新点是否会碰撞，内部遍历
                            for (Robot r : robotList) {
                                if (r == this) {
                                    continue;
                                }

                                searchOtherState = r.getStateByFrame(searchNextFrameId);
                                searchNextFrameId++;
                                if (Utils.computeDistance(s.getPos(), searchOtherState.getPos()) < 1.2) {
                                    isSearchCollided = true;
                                    break;
                                }
                            }
                            // 如果没有碰撞，那么继续
                            if (isSearchCollided) {
                                break;
                            }
                        }
                    }
                    if (isFindNextWaypoint) {
                        break;
                    }
                    range += 1.0;
                    if (range > 5) {
                        K = 5;
                        range = 0;
                    }
                }
            }

            if (isCollided) {
                waypoints.add(0, nextWayPointAfterCollided);
                waypoints.add(0, motionStates.get(nextFrameId - 1).getPos());
                break;
            }

            motionStates.put(nextFrameId, new MotionState(state));
            pidMap.put(nextFrameId, new PIDModel(pidModel));
            nextFrameId += 1;
            // // 保存当前state的位置与朝向
            // try {
            // FileWriter fw = new FileWriter("..\\PID\\state.txt", true);
            // fw.write(state.getPosX() + " " + state.getPosY() + " " +
            // state.getHeading() +
            // "\r");
            // fw.close();
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
        }

        // 保存预测的目标与帧数
        // try {
        // FileWriter fw = new FileWriter("..\\PID\\taskpredict.txt", true);
        // fw.write("id:" + this.id + "->" + wb.getType() + "frame" +
        // (frameId - 1 + motionStates.size()) + "\r");
        // fw.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    /**
     * @param state1: 低优先级回退后的状态
     * @param state2: 碰撞时高优先级的机器人状态
     *
     */
    private List<Coordinate> searchNextWaypoints(MotionState state1, MotionState state2, double range) {
        List<Coordinate> nextWaypoints = new ArrayList<>();
        Coordinate pos = state2.getPos();
        Velocity v = state2.getVelocity();
        Workbench wb = productType == 0 ? task.getFrom() : task.getTo();
        if (v.mod() < 0.001) {
            // 机器人当前状态为移动，速度为零
            // 沿速度方向的单位向量
            Velocity eH = new Velocity(0., 1.);
            // 沿速度垂直方向的单位向量
            Velocity eV = new Velocity(1., 0.);
            // 沿速度45方向的单位向量
            Velocity e45 = new Velocity(Math.sqrt(2) / 2, Math.sqrt(2) / 2);
            // 沿速度135方向的单位向量
            Velocity e135 = new Velocity(-Math.sqrt(2) / 2, Math.sqrt(2) / 2);
            for (double offset : new double[] { -range, range }) {
                nextWaypoints.add(new Coordinate(pos.getX() + offset * eH.getX(), pos.getY() + offset * eH.getY()));
                nextWaypoints.add(new Coordinate(pos.getX() + offset * eV.getX(), pos.getY() + offset * eV.getY()));
                nextWaypoints.add(new Coordinate(pos.getX() + offset * e45.getX(), pos.getY() + offset * e45.getY()));
                nextWaypoints.add(new Coordinate(pos.getX() + offset * e135.getX(), pos.getY() + offset * e135.getY()));
            }

        } else {
            // 沿速度方向的单位向量
            Velocity eH = new Velocity(v.getX() / v.mod(), v.getY() / v.mod());
            // 沿速度垂直方向的单位向量
            Velocity eV = new Velocity(-v.getY() / v.mod(), v.getX() / v.mod());
            // 沿速度45方向的单位向量
            Velocity e45 = new Velocity(Math.sqrt(2) / 2 * v.getX() / v.mod(), Math.sqrt(2) / 2 * v.getY() / v.mod());
            // 沿速度135方向的单位向量
            Velocity e135 = new Velocity(-Math.sqrt(2) / 2 * v.getY() / v.mod(), Math.sqrt(2) / 2 * v.getX() / v.mod());
            // 机器人当前状态正在移动，移动垂直方向搜索
            for (double offset : new double[] { -range, range }) {
                nextWaypoints.add(new Coordinate(pos.getX() + offset * eH.getX(), pos.getY() + offset * eH.getY()));
                nextWaypoints.add(new Coordinate(pos.getX() + offset * eV.getX(), pos.getY() + offset * eV.getY()));
                nextWaypoints.add(new Coordinate(pos.getX() + offset * e45.getX(), pos.getY() + offset * e45.getY()));
                nextWaypoints.add(new Coordinate(pos.getX() + offset * e135.getX(), pos.getY() + offset * e135.getY()));
            }
        }

        // 组装 当前点，目标点，中间点 点位，方便后续排序
        List<List<Coordinate>> groupCoordinate = new ArrayList<>();
        for (Coordinate next : nextWaypoints) {
            Coordinate curr = state1.getPos();
            Coordinate target = wb.getPos();

            // 在同一条线上或者超出地图，都抛弃掉
            if (Utils.online(curr, next, target) || Utils.isOutMap(next)) {
                continue;
            }

            List<Coordinate> a = new ArrayList<>();
            a.add(curr);
            a.add(target);
            a.add(next);

            groupCoordinate.add(a);
        }

        // 为搜索点排序
        Collections.sort(groupCoordinate, new Comparator<List<Coordinate>>() {
            public int compare(List<Coordinate> a1, List<Coordinate> a2) {
                Vector v10 = new Vector(a1.get(0).getX() - a1.get(2).getX(), a1.get(0).getY() - a1.get(2).getY());
                Vector v11 = new Vector(a1.get(1).getX() - a1.get(2).getX(), a1.get(1).getY() - a1.get(2).getY());

                Vector v20 = new Vector(a2.get(0).getX() - a2.get(2).getX(), a2.get(0).getY() - a2.get(2).getY());
                Vector v21 = new Vector(a2.get(1).getX() - a2.get(2).getX(), a2.get(1).getY() - a2.get(2).getY());

                return Double.compare(Utils.computeCosin(v10, v11), Utils.computeCosin(v20, v21));
            }
        });

        nextWaypoints = new ArrayList<>();
        for (List<Coordinate> gc : groupCoordinate) {
            nextWaypoints.add(gc.get(2));
        }

        return nextWaypoints;
    }

    /**
     * 寻找路径
     * 回退帧数 k
     */
    // private List<MotionState> findRoad(int frameId) {
    // // 获取当前帧信息
    // MotionState currState = stateMap.get(frameId);

    // // 查看是否满足结束条件，是的话，返回
    // if (到达终点 || frameId已经到底) {
    // // 根据情况进行返回
    // return;
    // }

    // List<Coordinate> positions = new ArrayList<Coordinate>();
    // if (碰撞) {
    // positions.addAll(getRelativePos(currState));
    // } else {
    // positions.add(new Coordinate(currState.getPosX(), currState.getPosY()));
    // }

    // for (Coordinate pos : positions) {
    // // 复制当前状态
    // MotionState state = new MotionState(currState);
    // // 产生PID结果
    // double[] pidVelocityResult = getPIDResult(state, pos);
    // // 产生预测
    // state = MotionModel.predict(state, pidVelocityResult[0],
    // pidVelocityResult[1]);

    // // 将state添加到list中及逆行判断
    // stateMap.put(state, frameId + 1);
    // // 对每个位置进行回退，检测是否会碰撞
    // findRoad(frameId + 1);

    // stateMap.remove(frameId + 1);
    // }
    // }

    /**
     * 检查售卖情况，根据判题器返回更改状态
     * 
     * @param leftFrame: 游戏剩余时间
     */
    public void checkDeal(int leftFrame) {
        // 没有任务
        if (task == null) {
            return;
        }

        Workbench from = task.getFrom();
        Workbench to = task.getTo();

        // 买成功，不持有->持有
        // 生产工作台规划产品格被释放:from.setPlanProductStatus(0);
        // 如果这时规划原料格已满，那么清除原料格状态
        if (lastProductType == 0 && productType != 0) {
            // 放开原料购买控制台
            from.setPlanProductStatus(0);

            // 删除当前waypoint，应该删除的是from的位置
            if (getCurrentTarget() != task.getFrom().getPos()) {
                System.err.println("error: 错误的waypoint删除");
            }

            reach();

            predict();

            counter = 0;
        }

        // 同一帧先卖后买，持有A->持有B
        /*
         * 卖出时：
         * 1. 工作台此时在生产
         * 那么系统返回的原料格信息不为0, 不改变占据状态
         * 2. 工作台此时未生产
         * 2.1 系统返回的原料格信息为0：
         * 说明卖出后立即清空原料格投入生产，那么此时的规划原料格也应清空
         * (注：是否会出现卖出前原料格差一个，但此时有两个机器人向这里运输原料，而导致卖出后清空原料格损失另外一个机器人的占据信息？
         * 不会出现，当共工作台只有一个规划原料格空位时，任务只会派遣给一个机器人
         * )
         * 2.2 系统返回的原料格信息不为0
         * 那么原料格未占满，生产未启动，不改变规划原料格占据状态
         * 买入时：
         * 释放规划产品格状态
         */
        // if (lastProductType != 0 && productType != 0 && lastProductType !=
        // productType) {
        // if (to.getMaterialStatus() == 0) {
        // to.updatePlanMaterialStatus(0, true);
        // }
        // to.setPlanProductStatus(0);
        // }

        // 卖成功，持有->不持有
        // 如果系统返回的原料格信息为0，那么清空规划原料格信息
        if (lastProductType != 0 && productType == 0) {
            to.updatePlanMaterialStatus(from.getType(), true);
            taskChain.removeTask(0);
            // 到达目标之后，删除所有waypoints
            waypoints.clear();

            task = taskChain.getNextTask();

            // 时间不足时，不继续执行任务链
            if (task != null) {
                // 运动需要的frame
                double moveFrame = task.getDistance() / Const.MAX_FORWARD_FRAME;

                if (moveFrame > leftFrame) {
                    task = null;
                }
            }

            // 检查是否有相同的TO, 如果有的话，通知对方repredict
            // for (Robot rb : robotList) {
            // if (rb == this) {
            // continue;
            // }

            // if (rb.task != null && task != null && rb.task.getTo() == task.getTo()) {
            // rb.predict();
            // }
            // }

            predict();
            counter = 0;
        }
    }

    public double[] control(MotionState ms, Coordinate pos) {
        return PID.control(ms, pos);
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

        // 检查是否有相同的TO, 如果有的话，通知对方repredict
        // for (Robot rb : robotList) {
        // if (rb == this) {
        // continue;
        // }

        // if (rb.task != null && task != null && rb.task.getTo() == task.getTo()) {
        // rb.predict();
        // }
        // }
        // 更换任务后，进行预测
        predict();

        counter = 0;
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
     * 当前机器人是否负载
     * 
     * @return: true = loaded
     */
    public boolean isLoaded() {
        return productType != 0;
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

    /** 通过负载情况返回机器人半径 */
    public double getRadius() {
        return productType == 0 ? Const.ROBOT_RADIUS_UNLOAD : Const.ROBOT_RADIUS_LOADED;
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

    /** 获取机器人当前任务 */
    public Task getTask() {
        return task;
    }

    /** 获取机器人frameID */
    public int getFrame() {
        return frameId;
    }

    /** hashcode */
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        Robot r = (Robot) o;
        return this.id == r.id;
    }
}
