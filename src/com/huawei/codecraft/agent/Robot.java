package com.huawei.codecraft.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.codecraft.ObjectPool;
import com.huawei.codecraft.action.Action;
import com.huawei.codecraft.action.ActionModel;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.motion.MotionFrag;
import com.huawei.codecraft.motion.MotionModel;
import com.huawei.codecraft.motion.MotionState;
import com.huawei.codecraft.pid.PIDModel;
import com.huawei.codecraft.task.Task;
import com.huawei.codecraft.task.TaskChain;
import com.huawei.codecraft.utils.Utils;
import com.huawei.codecraft.vector.Coordinate;
import com.huawei.codecraft.vector.Velocity;

public class Robot implements Comparable<Robot> {
    private int workbenchIdx; // 所处工作台下标, -1表示没有处于任何工作台, [0, K-1]表是某工作台下标
    private int productType; // 携带物品类型[0, 7], 0表示未携带物品
    private int lastProductType; // 上一帧携带的品
    private double timeCoefficients; // 时间价值系数 [0.8, 1]
    private double collisionCoefficients; // 碰撞价值系数 [0.8, 1]
    private double angularVelocity; // 角速度 单位：弧度每秒， 正数表示顺时针， 负数表示逆时针
    private Velocity velocity; // 线速度， 二维向量描述, m/s
    private double heading; // 朝向 [-pi, pi] 0 表示右方向, pi/2表示上方向
    private Coordinate pos; // 机器人坐标位置

    private long lastTime;

    private Task task; // 机器人当前任务
    private TaskChain taskChain; // 任务链
    private ArrayList<Action> actions; // 机器人当前动作序列
    private int id;
    private int frameId;
    private List<Robot> robotList;
    private Map<Integer, MotionState> motionStates; // 机器人运动状态序列

    private boolean newTask = false; // 标志位，表明是否新领取了chain

    private ObjectPool<MotionState> statePool;
    private ObjectPool<Coordinate> coordPool;
    private ObjectPool<PIDModel> pidPool;

    // 各种控制器
    private PIDModel PID;
    private ActionModel actionModel;
    private MotionModel motionModel;

    public void clearStates() {
        for (MotionState state : motionStates.values()) {
            statePool.release(state);
        }
        motionStates.clear();
    }

    /** 更新PID */
    public void updatePID(int count) {
        if (count == 43) {
            PID.update(6.0, 0.17, 0.9, 6.0, 0.17, 0.9);
        } else if (count == 25) {
            PID.update(7.2, 0.10, 1.3, 7.2, 0.10, 1.3);
        } else if (count == 50) {
            PID.update(7.1, 0.15, 0.7, 7.1, 0.15, 0.7);
        } else if (count == 18) {
            PID.update(7.0, 0.05, 0.8, 7.0, 0.05, 0.8);
        }
    }

    public int getId() {
        return id;
    }

    public Robot(Coordinate pos, List<Robot> robotList, String[] args, ObjectPool<MotionState> statePool,
            ObjectPool<MotionFrag> fragPool, ObjectPool<Coordinate> coordPool, ObjectPool<PIDModel> pidPool, int id) {
        this.pos = pos;
        this.workbenchIdx = -1;
        this.productType = 0;
        this.lastProductType = 0;
        this.timeCoefficients = 0;
        this.collisionCoefficients = 0;
        this.angularVelocity = 0;
        this.velocity = new Velocity(0, 0);
        this.heading = 0;
        this.task = null;
        this.taskChain = new TaskChain(0);
        this.actions = new ArrayList<Action>();
        this.frameId = 0;
        this.robotList = robotList;
        this.id = id;
        this.motionStates = new HashMap<>();

        this.PID = new PIDModel(this);
        // 搜索用
        if (args.length == 7) {
            this.PID = new PIDModel(this, args);
        }
        this.actionModel = new ActionModel(this, statePool, coordPool);
        this.motionModel = new MotionModel(statePool, fragPool);

        // 对象池
        this.statePool = statePool;
        this.coordPool = coordPool;
        this.pidPool = pidPool;
    }

    /** 更新所有数据 */
    public void update(String[] info, int frameId) {
        this.workbenchIdx = Integer.parseInt(info[0]);
        this.lastProductType = this.productType;
        this.productType = Integer.parseInt(info[1]);
        this.timeCoefficients = Double.parseDouble(info[2]);
        this.collisionCoefficients = Double.parseDouble(info[3]);
        this.angularVelocity = Double.parseDouble(info[4]);
        this.heading = Double.parseDouble(info[7]);
        this.velocity.setValue(Double.parseDouble(info[5]), Double.parseDouble(info[6]));
        this.pos.setValue(Double.parseDouble(info[8]), Double.parseDouble(info[9]));
        this.frameId = frameId;
    }

    /** 机器人根据当前任务和状态进行动作决策。将决策Action输入到列表中，等待执行 */
    public void step() {
        // 清空动作列表
        actions.clear();
        // 更新action列表
        actionModel.generate();
    }

    public double getPriority() {
        if (task == null) {
            return 1000.;
        }
        Workbench wb = productType == 0 ? task.getFrom() : task.getTo();
        return Utils.computeDistance(pos, wb.getPos());
    }

    /**
     * 根据当前任务预估没有碰撞的最快到达frame
     * 
     * @apiNote: 该方法会改变传入的motionState，以方便连续调用
     */
    public int predFrame(MotionState ms, Coordinate target) {
        int frame = 0;

        // 复制状态，避免直接对原数据进行操作
        PIDModel pidModel = pidPool.acquire();
        pidModel.update(PID);

        MotionState nextState;
        while (true) {
            if (Utils.computeDistance(ms.getPos(), target) < 0.2) {
                break;
            }

            double[] controlFactor = pidModel.control(ms, target);
            nextState = motionModel.predict(ms, controlFactor[0], controlFactor[1]);
            statePool.release(ms);
            ms = nextState;
            frame++;
        }

        pidPool.release(pidModel);
        return frame;
    }

    /**
     * 根据当前任务产生预测，暴露在外供给robot直接调用
     *
     */
    public Coordinate predict() {
        if (task == null) {
            return null;
        }
        // System.err.println(statePool.usedSize() + statePool.availableSize());
        clearStates();
        Workbench wb = productType == 0 ? task.getFrom() : task.getTo();
        // 复制状态，避免直接对原数据进行操作
        MotionState state = statePool.acquire();
        state.update(this);
        motionStates.put(frameId, state);

        PIDModel pidModel = pidPool.acquire();
        pidModel.update(PID);

        int nextFrameId = frameId + 1;
        int predictFrameLength = 200;

        for (int i = 0; i < predictFrameLength; i++) {
            double[] controlFactor = pidModel.control(state, wb.getPos());
            state = motionModel.predict(state, controlFactor[0], controlFactor[1]);
            motionStates.put(nextFrameId, state);
            // 检查是否有碰撞
            boolean isCollided = false;
            MotionState otherState = null;
            for (Robot rb : robotList) {
                if (rb == this) {
                    continue;
                }
                // 获取同帧下其他机器人状态，判断是否碰撞
                otherState = rb.motionStates.get(nextFrameId);
                if (otherState == null) {
                    continue;
                }
                if (Utils.computeDistance(state.getPos(), otherState.getPos()) < 1.5) {
                    isCollided = true;
                    break;
                }
            }

            if (isCollided) {
                pidPool.release(pidModel);
                return findMiddle(state);
            }
            nextFrameId++;
        }
        pidPool.release(pidModel);
        Coordinate next = coordPool.acquire();
        next.setValue(wb.getPos());
        return next;
    }

    /**
     * 寻找可以到达的中间点
     * 
     * @param crash: 碰撞点state
     */
    private Coordinate findMiddle(MotionState crash) {
        int predictFrameLength = 200;
        double range = 1.5;
        boolean isFindNextWaypoint = false;

        while (!isFindNextWaypoint) {
            MotionState s = statePool.acquire();
            s.update(this);
            List<Coordinate> nextWaypoints = searchNextWaypoints(s, crash, range);
            // 寻找完后迅速释放
            statePool.release(s);

            for (Coordinate next : nextWaypoints) {
                clearStates();
                s = statePool.acquire();
                s.update(this);
                motionStates.put(frameId, s);

                PIDModel p = pidPool.acquire();
                p.update(PID);
                int searchNextFrameId = frameId + 1;
                for (int j = 0; j < predictFrameLength; j++) {
                    double[] searchNextControlFactor = p.control(s, next);
                    s = motionModel.predict(s, searchNextControlFactor[0], searchNextControlFactor[1]);
                    boolean isSearchCollided = false;
                    // 检测新点是否会碰撞，内部遍历
                    for (Robot r : robotList) {
                        if (r == this) {
                            continue;
                        }

                        MotionState searchOtherState = r.motionStates.get(searchNextFrameId);
                        if (searchOtherState == null) {
                            continue;
                        }

                        if (Utils.computeDistance(s.getPos(), searchOtherState.getPos()) < 1.5) {
                            isSearchCollided = true;
                            break;
                        }
                    }
                    motionStates.put(searchNextFrameId, s);
                    searchNextFrameId++;

                    if (isSearchCollided) {
                        break;
                    }
                    if (j == predictFrameLength - 1) {
                        pidPool.release(p);

                        for (Coordinate points : nextWaypoints) {
                            if (points != next) {
                                coordPool.release(points);
                            }
                        }
                        return next;
                    }
                }
                // 每一轮进行释放
                pidPool.release(p);
            }

            for (Coordinate next : nextWaypoints) {
                coordPool.release(next);
            }

            range += 0.5;
            if (range > 4) {
                break;
            }
        }
        // 到这里什么都没有找到
        Workbench wb = productType == 0 ? task.getFrom() : task.getTo();
        // System.err.println(statePool.usedSize() + statePool.availableSize());
        Coordinate next = coordPool.acquire();
        next.setValue(wb.getPos());
        return next;
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

        // 根据速度生成待遍历的点位
        if (v.mod() < 0.001) {
            // 机器人当前状态为移动，速度为零
            // 沿速度方向的单位向量
            Coordinate eH = coordPool.acquire();
            eH.setValue(0, 1);
            // 沿速度垂直方向的单位向量
            Coordinate eV = coordPool.acquire();
            eV.setValue(1, 0);
            // 沿速度45方向的单位向量
            Coordinate e45 = coordPool.acquire();
            e45.setValue(Math.sqrt(2) / 2, Math.sqrt(2) / 2);
            // 沿速度135方向的单位向量
            Coordinate e135 = coordPool.acquire();
            e135.setValue(-Math.sqrt(2) / 2, Math.sqrt(2) / 2);

            for (double offset : new double[] { -range, range }) {
                Coordinate item = coordPool.acquire();
                item.setValue(pos.getX() + offset * eH.getX(), pos.getY() + offset * eH.getY());
                nextWaypoints.add(item);
                item = coordPool.acquire();
                item.setValue(pos.getX() + offset * eV.getX(), pos.getY() + offset * eV.getY());
                nextWaypoints.add(item);
                item = coordPool.acquire();
                item.setValue(pos.getX() + offset * e45.getX(), pos.getY() + offset * e45.getY());
                nextWaypoints.add(item);
                item = coordPool.acquire();
                item.setValue(pos.getX() + offset * e135.getX(), pos.getY() + offset * e135.getY());
                nextWaypoints.add(item);
            }

            coordPool.release(eH);
            coordPool.release(eV);
            coordPool.release(e45);
            coordPool.release(e135);
        } else {
            // 机器人当前状态为移动，速度为零
            // 沿速度方向的单位向量
            Coordinate eH = coordPool.acquire();
            eH.setValue(v.getX() / v.mod(), v.getY() / v.mod());
            // 沿速度垂直方向的单位向量
            Coordinate eV = coordPool.acquire();
            eV.setValue(-v.getY() / v.mod(), v.getX() / v.mod());
            // 沿速度45方向的单位向量
            Coordinate e45 = coordPool.acquire();
            e45.setValue(Math.sqrt(2) / 2 * v.getX() / v.mod(), Math.sqrt(2) / 2 * v.getY() / v.mod());
            // 沿速度135方向的单位向量
            Coordinate e135 = coordPool.acquire();
            e135.setValue(-Math.sqrt(2) / 2 * v.getY() / v.mod(), Math.sqrt(2) / 2 * v.getX() / v.mod());

            // 机器人当前状态正在移动，移动垂直方向搜索
            for (double offset : new double[] { -range, range }) {
                Coordinate item = coordPool.acquire();
                item.setValue(pos.getX() + offset * eH.getX(), pos.getY() + offset * eH.getY());
                nextWaypoints.add(item);
                item = coordPool.acquire();
                item.setValue(pos.getX() + offset * eV.getX(), pos.getY() + offset * eV.getY());
                nextWaypoints.add(item);
                item = coordPool.acquire();
                item.setValue(pos.getX() + offset * e45.getX(), pos.getY() + offset * e45.getY());
                nextWaypoints.add(item);
                item = coordPool.acquire();
                item.setValue(pos.getX() + offset * e135.getX(), pos.getY() + offset * e135.getY());
                nextWaypoints.add(item);
            }

            coordPool.release(eH);
            coordPool.release(eV);
            coordPool.release(e45);
            coordPool.release(e135);
        }

        // 组装 当前点，目标点，中间点 点位，方便后续排序
        List<List<Coordinate>> groupCoordinate = new ArrayList<>();
        for (Coordinate next : nextWaypoints) {
            Coordinate curr = state1.getPos();
            Coordinate target = wb.getPos();

            // 在同一条线上或者超出地图，都抛弃掉
            if (Utils.online(curr, next, target) || Utils.isOutMap(next)) {
                // 释放不满足条件的point
                coordPool.release(next);
                continue;
            }

            List<Coordinate> a = new ArrayList<>();
            a.add(curr);
            a.add(next);
            a.add(target);

            groupCoordinate.add(a);
        }

        // 为搜索点排序
        Collections.sort(groupCoordinate, new Comparator<List<Coordinate>>() {
            public int compare(List<Coordinate> a1, List<Coordinate> a2) {
                return Double.compare(
                        Utils.getAngleDiff(a1.get(0), a1.get(1), a1.get(2)),
                        Utils.getAngleDiff(a2.get(0), a2.get(1), a2.get(2)));
            }
        });

        nextWaypoints = new ArrayList<>();
        for (List<Coordinate> gc : groupCoordinate) {
            Coordinate nextpoint = coordPool.acquire();
            nextpoint.setValue(gc.get(1));
            nextWaypoints.add(nextpoint);

            // 释放所有groupCoordinate
            for (Coordinate cd : gc) {
                coordPool.release(cd);
            }
        }

        return nextWaypoints;
    }

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

        // 确认是否是刚接到的任务
        if (leftFrame < 800 && productType == 0 && newTask == true) {
            MotionState state = statePool.acquire();
            state.update(this);
            // 时间不足时，不继续执行任务链
            if (task != null && predFrame(state, task.getFrom().getPos())
                    + predFrame(state, task.getTo().getPos()) > leftFrame + 5) {
                task = null;
            }

            statePool.release(state);

            newTask = false;
        }

        // 买成功，不持有->持有
        // 生产工作台规划产品格被释放:from.setPlanProductStatus(0);
        // 如果这时规划原料格已满，那么清除原料格状态
        if (lastProductType == 0 && productType != 0) {
            // 放开原料购买控制台
            from.setPlanProductStatus(0);
        }
        // 卖成功，持有->不持有
        // 如果系统返回的原料格信息为0，那么清空规划原料格信息
        if (lastProductType != 0 && productType == 0) {
            to.updatePlanMaterialStatus(from.getType(), true);
            taskChain.removeTask(0);

            task = taskChain.getNextTask();

            MotionState state = statePool.acquire();
            state.update(this);
            // 时间不足时，不继续执行任务链
            if (task != null && predFrame(state, task.getTo().getPos()) > leftFrame + 5) {
                task = null;
            }
            statePool.release(state);
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
        this.taskChain.update(taskChain);
        // 机器人绑定任务链的时候就会分配任务
        this.task = this.taskChain.getTasks().get(0);
        this.newTask = true;
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

    /**
     * 实现任务链排序，当前只是使用了任务链的预估收益
     *
     * @param o 比较对象
     * @return
     */
    @Override
    public int compareTo(Robot o) {
        return Double.compare(this.getPriority(), o.getPriority());
    }

}
