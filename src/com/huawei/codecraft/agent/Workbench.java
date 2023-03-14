package com.huawei.codecraft.agent;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.task.Task;
import com.huawei.codecraft.utils.Coordinate;

public class Workbench {
    private int workbenchIdx; // 工作台ID
    private int type; // 工作台类型 [1-9]
    private Coordinate pos; // 工作台位置
    private int rest; // 生产剩余时间 -1表示没有生产；0表示生产因输出格满而受阻塞；>=0 表示剩余生产帧数
    private int materialStatus; // 原材料格状态；二进制为表示，例如 48(110000),表示拥有物品4和5
    private int productStatus; // 产品格状态 0 表示无 1 表示有
    private List<Task> optionalTasks; // 工作台可发布的任务

    private boolean inTaskChain; // true: 说明已经有机器人领取了任务，后面派遣任务需要跳过此工作台，每次提交任务都应该重置标志位

    public Workbench(Coordinate pos, int type, int workbenchIdx) {
        this.type = type;
        this.pos = pos;
        this.workbenchIdx = workbenchIdx;
        this.rest = -1;
        this.materialStatus = 0;
        this.productStatus = 0;
        this.optionalTasks = new ArrayList<Task>();

        this.inTaskChain = false;
    }

    /**
     * @apiNote 只更新剩余生产时间，原材料状态以及产品格状态
     */
    public void update(String[] info) {
        this.rest = Integer.parseInt(info[3]);
        this.materialStatus = Integer.parseInt(info[4]);
        this.productStatus = Integer.parseInt(info[5]);
    }

    public void addTask(Task task) {
        optionalTasks.add(task);
    }

    public List<Task> getTasks() {
        return optionalTasks;
    }

    public int getType() {
        return type;
    }

    public Coordinate getPos() {
        return pos;
    }

    public int getRest() {
        return rest;
    }

    public int getMaterialStatus() {
        return materialStatus;
    }

    public int getProductStatus() {
        return productStatus;
    }

    public int getWorkbenchIdx() {
        return workbenchIdx;
    }

    public boolean isInTaskChain() {
        return inTaskChain;
    }

    public void setInTaskChain(boolean inTaskChain) {
        this.inTaskChain = inTaskChain;
    }
}
