package com.huawei.codecraft.agent;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.task.Task;
import com.huawei.codecraft.vector.Coordinate;

public class Workbench {
    private int workbenchIdx; // 工作台ID
    private int type; // 工作台类型 [1-9]
    private Coordinate pos; // 工作台位置
    private int rest; // 生产剩余时间 -1表示没有生产；0表示生产因输出格满而受阻塞；>=0 表示剩余生产帧数
    private int productStatus; // 产品格状态 0 表示无 1 表示有
    private int planProductStatus; // 规划产品格状态，由选手程序维护；不仅包含环境目前的状态，还包括预测状态。
    private int materialStatus; // 原材料格状态；二进制为表示，例如 48(110000),表示拥有物品4和5
    private int lastMaterialStatus; //
    private int planMaterialStatus; // 规划原材料格状态；不仅包含环境目前的状态，还包括预测状态。
    private List<Task> optionalTasks; // 工作台可发布的任务

    public Workbench(Coordinate pos, int type, int workbenchIdx) {
        this.type = type;
        this.pos = pos;
        this.workbenchIdx = workbenchIdx;
        this.rest = -1;
        this.productStatus = 0;
        this.lastMaterialStatus = 0;
        this.planProductStatus = 0;
        this.materialStatus = 0;
        this.planMaterialStatus = 0;
        this.optionalTasks = new ArrayList<>();

    }

    public int getLastMaterialStatus() {
        return lastMaterialStatus;
    }

    public void setLastMaterialStatus(int lastMaterialStatus) {
        this.lastMaterialStatus = lastMaterialStatus;
    }

    public int getMaterialStatus() {
        return materialStatus;
    }

    public int getPlanProductStatus() {
        return planProductStatus;
    }

    public void setPlanProductStatus(int planProductStatus) {
        this.planProductStatus = planProductStatus;
    }

    public int getPlanMaterialStatus() {
        return planMaterialStatus;
    }

    public void setPlanMaterialStatus(int planMaterialStatus) {
        this.planMaterialStatus = planMaterialStatus;
    }

    /** 只更新剩余生产时间，原材料状态以及产品格状态 */
    public void update(String[] info) {
        this.rest = Integer.parseInt(info[3]);
        this.lastMaterialStatus = this.materialStatus;
        this.materialStatus = Integer.parseInt(info[4]);
        this.productStatus = Integer.parseInt(info[5]);
    }

    /** 查看是否含有某个原材料 */
    public boolean hasMaterial(int type) {
        return ((1 << type) & materialStatus) != 0;
    }

    /** 查看是否规划中已经占用该工作台原料格 */
    public boolean hasPlanMaterial(int type) {
        return ((1 << type) & planMaterialStatus) != 0;
    }

    /**
     * 更新原料格状态
     * 
     * @param type:    原材料类型
     * @param isClear: 是否清空规划原材料格, true: 表示所有原料格清空投入生产， false: 占据原料格
     */
    public void updatePlanMaterialStatus(int type, boolean isClear) {
        if (isClear) {
            planMaterialStatus = 0;
        } else {
            planMaterialStatus |= (1 << type);
        }
    }

    /** 是否空闲，rest==-1 */
    public boolean isFree() {
        return rest == -1;
    }

    /** 是否已经有产物, productStatus==1 */
    public boolean isReady() {
        return productStatus == 1;
    }

    /** 添加该工作台的可选任务 */
    public void addTask(Task task) {
        optionalTasks.add(task);
    }

    /** 获取该工作台的所有可选任务 */
    public List<Task> getTasks() {
        return optionalTasks;
    }

    /** 获取工作台类型 */
    public int getType() {
        return type;
    }

    /** 获取工作台位置 */
    public Coordinate getPos() {
        return pos;
    }

    /** 获取工作台剩余工作时间 */
    public int getRest() {
        return rest;
    }

    /** 获取工作台index */
    public int getWorkbenchIdx() {
        return workbenchIdx;
    }

    /** 当前工作台是否拥塞 */
    public boolean isBlocked() {
        boolean ret = false;
        switch (type) {
            case 4:
                if (planMaterialStatus == 0b110) {
                    ret = true;
                }
                break;
            case 5:
                if (planMaterialStatus == 0b1010) {
                    ret = true;
                }
                break;
            case 6:
                if (planMaterialStatus == 0b1100) {
                    ret = true;
                }
                break;
            case 7:
                if (planMaterialStatus == 0b1110000) {
                    ret = true;
                }
                break;
            default:
                ret = false;
                break;
        }
        return ret;
    }
}
