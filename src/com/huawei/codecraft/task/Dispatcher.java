package com.huawei.codecraft.task;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.utils.Utils;

/**
 * @description: 调度器类
 */
public class Dispatcher {
    private boolean saveChain;
    private String chainPath = "./log/chain.txt";
    private FileOutputStream chainStream;

    private List<Robot> robotList;
    private List<Robot> freeRobots;
    private List<Workbench> workbenchList;

    private Map<Integer, List<Workbench>> workbenchTypeMap;
    // key: 工作台种类, value: 以工作台产物为原料的所有任务
    private Map<Integer, List<Task>> taskTypeMap;

    public Dispatcher(List<Robot> robotList, List<Workbench> workbenchList,
            Map<Integer, List<Workbench>> workbenchTypeMap, boolean saveChain) {
        this.saveChain = saveChain;

        this.robotList = robotList;
        this.freeRobots = new ArrayList<>();
        this.workbenchList = workbenchList;

        this.workbenchTypeMap = workbenchTypeMap;
        this.taskTypeMap = new HashMap<>();

        if (saveChain) {
            chainStream = Utils.getFileStream(chainPath);
        }

        // 初始化所有任务
        init();
    }

    /** 开始任务分配 */
    public void dispatch() {
        // 筛选空闲机器人，供后续使用
        updateFreeBot();

        // 有空闲，构建任务链并分配给机器人
        if (!freeRobots.isEmpty()) {
            // 构建任务链
            Map<Robot, PriorityQueue<TaskChain>> chainsMap = generateTaskChains();

            if (chainsMap == null) {
                return;
            }

            // 分配任务链
            while (chainsMap.size() != 0) {
                double max = 0.;
                Robot receiver = null;
                TaskChain bindchain = null;
                for (Robot rb : chainsMap.keySet()) {
                    while (true) {
                        TaskChain chain = chainsMap.get(rb).peek();
                        if (chain == null) {
                            bindchain = chain;
                            receiver = rb;
                            break;
                        }
                        if (chain.isOccupied()) {
                            chainsMap.get(rb).poll();
                            continue;
                        }

                        if (chain.getProfit() > max) {
                            receiver = rb;
                            bindchain = chain;
                            max = chain.getProfit();
                            break;
                        }
                    }
                }

                if (bindchain == null) {
                    chainsMap.remove(receiver);
                    continue;
                }

                receiver.bindChain(bindchain);
                bindchain.occupy();
                chainsMap.remove(receiver);
            }
        }
    }

    /** 筛选空闲机器人 */
    public void updateFreeBot() {
        this.freeRobots = robotList.stream()
                .filter((Robot rb) -> rb.isFree())
                .collect(Collectors.toList());
    }

    /** 在获取到初始任务链后，更新任务链，增加任务链长度 */
    private void updateTaskChain(Map<Robot, PriorityQueue<TaskChain>> taskChainQueueMap) {
        // 以机器人的初始任务链为单位, 添加后续任务
        for (Robot rb : freeRobots) {
            PriorityQueue<TaskChain> oldTaskChainList = taskChainQueueMap.get(rb);
            PriorityQueue<TaskChain> newTaskChainList = new PriorityQueue<TaskChain>();

            if (oldTaskChainList == null) {
                continue;
            }

            for (TaskChain taskChain : oldTaskChainList) {
                boolean addNewTaskChain = false;
                Task lastTask = taskChain.getTasks().get(taskChain.length() - 1);

                // 遍历任务链中最后一个任务的后续任务,如果没有后续任务进行下一次遍历
                if (lastTask.getPostTaskList().isEmpty()) {
                    newTaskChainList.add(taskChain);
                    continue;
                }

                for (Task postTask : lastTask.getPostTaskList()) {
                    Workbench postFrom = postTask.getFrom(), postTo = postTask.getTo(), lastFrom = lastTask.getFrom();

                    // 未生产，直接访问下个后续任务 或者 lastTask生产的产品已经出现在产品格中
                    // 假设我们能够维护好预测的原料格状态和生产格状态，那么在生成任务链中
                    /*
                     * 环境应满足：
                     * 1. postFrom工作台必须已经投入生产: postFrom.isFree() true： 表示未生产
                     * 2. postTo工作台的原料格没被占用
                     * 
                     * 规划应满足：
                     * 
                     * 1. postFrom工作台产品格未被占据:postFrom.getPlanProductStatus() == 1 true表示被占据
                     * 2. postFrom工作台的规划原料格没被占用：postFrom.hasPlanMaterial(lastFrom.getType())
                     * true表示被占据
                     * 3. postTo工作台的规划原料格没被占用：postTo.hasPlanMaterial(postFrom.getType()) true表示被占据
                     */
                    if (postFrom.isFree() || postTo.hasMaterial(postFrom.getType())
                            || postFrom.getPlanProductStatus() == 1
                            || postFrom.hasPlanMaterial(lastFrom.getType())
                            || postTo.hasPlanMaterial(postFrom.getType())) {
                        // 后继任务未生产 或者 后续任务接受栏未满 或者 后续任务已经被执行
                        continue;
                    }

                    // 开始生产, 如果生产剩余时间比机器人最快到达时间更久，说明会出现等待
                    if (postFrom.getRest() > taskChain.getTotalFrame()) {
                        continue;
                    }

                    // 更新任务最早完成时间，并把该任务加入到这条任务链中
                    TaskChain newTaskChain = new TaskChain(taskChain);

                    newTaskChain.addTask(postTask);

                    // 保存
                    newTaskChainList.add(newTaskChain);
                    addNewTaskChain = true;
                }

                if (!addNewTaskChain) {
                    newTaskChainList.add(taskChain);
                }
            }
            taskChainQueueMap.put(rb, newTaskChainList);
        }
    }

    /** 初始化每个工作台的内容，主要完成每个工作台的产品去向以及他们之间的距离初始化，方便后面复用。 */
    public void init() {
        for (Workbench wb : workbenchList) {
            int wbType = wb.getType();

            int[] types = Utils.getDeliverableType(wbType); // 以工作台产物为原料可以生产的产品

            // 根据工作台类型以及可以接收产物的工作台创建task
            for (int type : types) {
                // 如果当前地图上不含某些工作台，直接跳过
                if (!workbenchTypeMap.containsKey(type)) {
                    continue;
                }

                // 生成任务
                for (Workbench target : workbenchTypeMap.get(type)) {
                    wb.addTask(new Task(wb, target));
                }
            }

            // 将同原料的所有任务储存到一起，方便产生任务链
            if (taskTypeMap.containsKey(wbType)) {
                taskTypeMap.get(wbType).addAll(wb.getTasks());
            } else {
                List<Task> tasks = new ArrayList<Task>();
                tasks.addAll(wb.getTasks());
                taskTypeMap.put(wbType, tasks);
            }
        }

        // 为每个任务添加后续任务
        for (List<Task> tasks : taskTypeMap.values()) {
            for (Task task : tasks) {
                // 将task目标工作台的所有任务都链接到当前task上，以方便后续调用
                task.setPostTaskList(task.getTo().getTasks());
            }
        }
    }

    /** 生成初始任务链 */
    public Map<Robot, PriorityQueue<TaskChain>> generateTaskChains() {
        // key: 执行任务的机器人, value: 任务链列表
        Map<Robot, PriorityQueue<TaskChain>> taskChainQueueMap = new HashMap<>();

        // 遍历所有task
        for (List<Task> taskList : taskTypeMap.values()) {
            for (Task task : taskList) {
                Workbench from = task.getFrom();
                Workbench to = task.getTo();

                // 工作台未生产 或者 该任务的接受处已满 或者 这个工作已经被选中正在执行
                // 假设我们能够维护好预测的原料格状态和生产格状态，那么在最初生成任务链中
                /*
                 * 环境应满足：
                 * 1. from工作台必须已经投入生产: from.isFree() true 表示未生产
                 * 2. to工作台原材料格没有被占用: to.hasMaterial(from.getType())
                 * 
                 * 规划应满足:
                 * 1. from工作台规划产品格没被占领:from.getPlanProductStatus() == 1 true表示被占据
                 * 2. to工作台的规划原料格(planMaterialStatus)没被占用:to.hasPlanMaterial(from.getType())
                 */
                if (from.isFree() || from.getPlanProductStatus() == 1 || to.hasPlanMaterial(from.getType())
                        || to.hasMaterial(from.getType())) {
                    continue;
                }

                // 如果工作台可以投入生产，继续进行判断

                // 遍历机器人列表，选择最近的任务链
                for (Robot rb : freeRobots) {
                    double distance = Utils.computeDistance(from.getPos(), rb.getPos());
                    double receiveTaskFrame = distance / Const.MAX_FORWARD_FRAME;

                    // 接收时间小于生产时间，需要等待，直接放弃
                    if (receiveTaskFrame < from.getRest()) {
                        continue;
                    }

                    // 更新任务最快完成时间
                    TaskChain taskChain = new TaskChain(rb, receiveTaskFrame);
                    taskChain.addTask(task);

                    // 保存任务链
                    if (taskChainQueueMap.containsKey(rb)) {
                        taskChainQueueMap.get(rb).add(taskChain);
                    } else {
                        PriorityQueue<TaskChain> taskChainQueue = new PriorityQueue<TaskChain>();
                        taskChainQueue.add(taskChain);
                        taskChainQueueMap.put(rb, taskChainQueue);
                    }
                }

            }
        }

        // 如果没有任务链，直接返回
        if (taskChainQueueMap.isEmpty()) {
            return null;
        }

        // 这里更新两次是因为最长链长为3，减去初始链长1, 所以两次
        updateTaskChain(taskChainQueueMap);
        updateTaskChain(taskChainQueueMap);

        return taskChainQueueMap;

    }
}
