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
    private int productStatus; // 产品格状态 0 表示无 1 表示有
    private List<Integer> materialStatus; // 原材料格状态；二进制为表示，例如 48(110000),表示拥有物品4和5
    private List<Task> optionalTasks; // 工作台可发布的任务

    private boolean inTaskChain; // true: 说明已经有机器人领取了任务，后面派遣任务需要跳过此工作台，每次提交任务都应该重置标志位

    public Workbench(Coordinate pos, int type, int workbenchIdx) {
        this.type = type;
        this.pos = pos;
        this.workbenchIdx = workbenchIdx;
        this.rest = -1;
        this.productStatus = 0;
        this.materialStatus = new ArrayList<>();
        this.optionalTasks = new ArrayList<>();

        this.inTaskChain = false;
    }

    /** 只更新剩余生产时间，原材料状态以及产品格状态 */
    public void update(String[] info) {
        this.rest = Integer.parseInt(info[3]);
        this.materialStatus = translateMaterialStatus(info[4]);
        this.productStatus = Integer.parseInt(info[5]);
    }

    /** 将二进制表示的原材料转换为对应的type，并存储到list中 */
    public List<Integer> translateMaterialStatus(String stringStatus) {
        int intStatus = Integer.parseInt(stringStatus);
        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            // 左移开始判断
            if (((1 << i) & intStatus) != 0) {
                result.add(i);
            }
        }

        return result;
    }

    /** 查看是否含有某个原材料 */
    public boolean hasMaterial(int type) {
        return materialStatus.contains(type);
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

    /** 当前工作台是否在工作链中 */
    public boolean isInTaskChain() {
        return inTaskChain;
    }

    /** 设定当前工作台是否在工作链中 */
    public void setInTaskChain(boolean inTaskChain) {
        this.inTaskChain = inTaskChain;
    }
}
