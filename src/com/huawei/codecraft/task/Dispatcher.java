package com.huawei.codecraft.task;

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

/*
 * @description: 调度器类
 */
public class Dispatcher {
    private List<Robot> robotList;
    private List<Robot> freeRobots;
    private List<Workbench> workbenchList;

    private Map<Integer, List<Workbench>> workbenchTypeMap;
    // key: 工作台种类, value: 以工作台产物为原料的所有任务
    private Map<Integer, List<Task>> taskTypeMap;

    public Dispatcher(List<Robot> robotList, List<Workbench> workbenchList,
            Map<Integer, List<Workbench>> workbenchTypeMap) {
        this.robotList = robotList;
        this.freeRobots = new ArrayList<>();
        this.workbenchList = workbenchList;

        this.workbenchTypeMap = workbenchTypeMap;
        this.taskTypeMap = new HashMap<>();

        // 初始化所有任务
        init();
    }

    // 任务分配
    public void dispatch() {
        // 筛选空闲机器人，供后续使用
        updateFreeBot();

        // 有空闲，构建任务链并分配给机器人
        if (freeRobots.isEmpty()) {
            // 构建任务链
            generateTaskChains();
        }
    }

    public void updateFreeBot() {
        // 筛选空闲机器人
        this.freeRobots = robotList.stream()
                .filter((Robot rb) -> rb.isBusy())
                .collect(Collectors.toList());
    }

    /**
     * @description: 在获取到初始任务链后，更新任务链，增加任务链长度
     */
    private void updateTaskChain(Map<Robot, PriorityQueue<TaskChain>> taskChainQueueMap) {
        // 以机器人的初始任务链为单位, 添加后续任务
        for (Robot rb : freeRobots) {
            PriorityQueue<TaskChain> oldTaskChainList = taskChainQueueMap.get(rb);
            PriorityQueue<TaskChain> newTaskChainList = new PriorityQueue<TaskChain>();

            for (TaskChain taskChain : oldTaskChainList) {
                boolean addNewTaskChain = false;
                Task lastTask = taskChain.getTasks().get(taskChain.length() - 1);

                // 遍历任务链中最后一个任务的后续任务,如果没有后续任务进行下一次遍历
                if (lastTask.getPostTaskList().isEmpty()) {
                    continue;
                }

                for (Task postTask : lastTask.getPostTaskList()) {
                    Workbench postFrom = postTask.getFrom(), lastTo = lastTask.getTo(), lastFrom = lastTask.getFrom();

                    // 未生产，直接访问下个后续任务 或者 lastTask生产的产品已经出现在产品格中
                    if (postFrom.isFree() || lastTo.hasMaterial(lastFrom.getType()) || postFrom.isInTaskChain()) {
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

    /**
     * @description: 初始化每个工作台的内容，主要完成每个工作台的产品去向以及他们之间的距离初始化，方便后面复用。
     *
     */
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
                // TODO: 给每一个工作台添加完可选task之后按照距离进行排序
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

    public Map<Robot, PriorityQueue<TaskChain>> generateTaskChains() {
        // key: 执行任务的机器人, value: 任务链列表
        Map<Robot, PriorityQueue<TaskChain>> taskChainQueueMap = new HashMap<>();

        // 遍历所有task
        for (List<Task> taskList : taskTypeMap.values()) {
            for (Task task : taskList) {
                Workbench from = task.getFrom();
                Workbench to = task.getTo();

                // 工作台未生产 或者 该任务的接受处已满 或者 这个工作已经被选中正在执行
                if (from.isFree() || to.hasMaterial(from.getType()) || from.isInTaskChain()) {
                    continue;
                }

                // 如果工作台可以投入生产，继续进行判断
                double minFrame = 180 * Const.FRAME_PER_SECOND;
                Robot taskReceiver = null;

                // 遍历机器人列表，选择最近的任务链
                for (Robot rb : freeRobots) {
                    double distance = Utils.computeDistance(from.getPos(), rb.getPos());
                    double receiveTaskFrame = distance / Const.MAX_FORWARD_FRAME;

                    // 接收时间小于生产时间，需要等待，直接放弃
                    if (receiveTaskFrame < from.getRest()) {
                        continue;
                    }
                    // 接受时间大于生产剩余时间，到达之后可以直接接受任务
                    if (receiveTaskFrame < minFrame) {
                        taskReceiver = rb;
                        minFrame = receiveTaskFrame;
                    }
                }

                // 更新任务最快完成时间
                double finishFrame = minFrame + task.getDistance() / Const.MAX_FORWARD_FRAME;
                TaskChain taskChain = new TaskChain(taskReceiver, finishFrame);
                taskChain.addTask(task);

                // 保存任务链
                if (taskChainQueueMap.containsKey(taskReceiver)) {
                    taskChainQueueMap.get(taskReceiver).add(taskChain);
                } else {
                    PriorityQueue<TaskChain> taskChainQueue = new PriorityQueue<TaskChain>();
                    taskChainQueue.add(taskChain);
                    taskChainQueueMap.put(taskReceiver, taskChainQueue);
                }
            }
        }

        // 这里更新两次是因为最长链长为3，减去初始链长1, 所以两次
        updateTaskChain(taskChainQueueMap);
        updateTaskChain(taskChainQueueMap);

        return taskChainQueueMap;

    }
}
