package com.huawei.codecraft.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.huawei.codecraft.agent.Robot;
import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.utils.Coordinate;
import com.huawei.codecraft.utils.Utils;

/*
 * @Description: 调度器类
 */
public class Dispatcher {
    private List<Robot> robotList;
    private List<Workbench> workbenchList;
    private List<TaskChain> taskChainList;

    private Map<Integer, List<Workbench>> workbenchTypeMap;
    // key: 工作台种类, value: 以工作台产物为原料的所有任务
    private Map<Integer, List<Task>> taskTypeMap;

    // 初始化调度器，按照自己的想法储存Task
    public Dispatcher(List<Robot> robotList, List<Workbench> workbenchList,
            Map<Integer, List<Workbench>> workbenchTypeMap) {
        this.robotList = robotList;
        this.workbenchList = workbenchList;

        this.workbenchTypeMap = workbenchTypeMap;
        this.taskTypeMap = new HashMap<>();

        init();
    }

    // 任务分配
    public void dispatch() {
    }

    /**
     * @description: 通过任务建图
     */

    /**
     * 获得任务链列表
     * 
     * @param unLoadRobotList 空闲机器人列表
     * @return 任务链列表的映射图 key: 执行任务的机器人对象 value: 机器人可执行的任务链队列
     */
    public Map<Robot, PriorityQueue<TaskChain>> getTaskChainListMap(List<Robot> unLoadRobotList) {
        // 如果没有空闲的机器人，直接返回
        if (unLoadRobotList.isEmpty()) {
            return null;
        }
        // key: 执行任务的机器人 value：任务链列表
        Map<Robot, PriorityQueue<TaskChain>> taskChainQueueMap = new HashMap<>();
        // new PriorityQueue<TaskChain>((a, b)-> Double.compare(b.getReward(),
        // a.getReward())); // 降序

        for (int i = 1; i < 8; i++) {
            // taskType是指类型为type的工作台生产出来的任务, 如果没有该类型工作台, 那么访问下一个工作台类型
            List<Task> taskTypeList = taskTypeMap.get(i);
            if (taskTypeList == null) {
                continue;
            }
            for (Task taskType : taskTypeList) {
                // taskType选择最近的机器人
                if (taskType.getFrom().getRest() == -1
                        || ((1 << taskType.getFrom().getType()) & taskType.getTo().getMaterialStatus()) != 0
                        || taskType.getFrom().isInTaskChain()) {
                    // 工作台未生产 或者 该任务的接受处已满 或者 这个工作已经被选中正在执行
                    continue;
                }

                // 如果工作台投入生产
                double minTime = (double) Const.FRAME_PER_SECOND * 180;
                Robot taskReceiver = null;
                // 遍历空闲机器人列表，选取最近的任务链
                for (Robot unloadRobot : unLoadRobotList) {
                    Coordinate robotPos = unloadRobot.getPos();
                    Coordinate taskPos = taskType.getFrom().getPos();
                    // 这里的receiveTaskTime指得是机器人到初始任务所需最短时间
                    double receiveTaskTime = Utils.computeDistance(robotPos, taskPos) / Const.MAX_BACKWARD_VELOCITY
                            * Const.FRAME_PER_SECOND;
                    // 运输时间小于生产剩余时间，那么需要等待，直接放弃该任务
                    if (receiveTaskTime < taskType.getFrom().getRest()) {
                        continue;
                    }
                    // 运输时间大于生产剩余时间，那么到达该任务后直接可以接收该任务，该把这个任务分给哪个机器人？
                    if (receiveTaskTime < minTime) {
                        taskReceiver = unloadRobot;
                        minTime = receiveTaskTime;
                    }
                }
                // 更新该任务最早完成时间
                double finishTime = minTime
                        + taskType.getDistance() / Const.MAX_BACKWARD_VELOCITY * Const.FRAME_PER_SECOND;
                TaskChain taskChain = new TaskChain(taskReceiver);
                taskChain.getTaskChain().add(taskType);
                taskChain.setFinishTime(finishTime);
                if (taskChainQueueMap.get(taskReceiver) == null) {
                    // 未添加该对象， TODO: Heap
                    PriorityQueue<TaskChain> taskChainQueue = new PriorityQueue<TaskChain>(
                            (a, b) -> Double.compare(b.getReward(), a.getReward())); // 降序
                    taskChainQueue.add(taskChain);
                    taskChainQueueMap.put(taskReceiver, taskChainQueue);
                } else {
                    taskChainQueueMap.get(taskReceiver).add(taskChain);
                }
            }
        }

        // 这里更新两次是因为最长链长为3，减去初始链长1, 所以两次
        updateTaskChain(unLoadRobotList, taskChainQueueMap);
        updateTaskChain(unLoadRobotList, taskChainQueueMap);

        return taskChainQueueMap;
    }

    /**
     * @description: 在获取到初始任务链后，更新任务链，增加任务链长度
     */
    private void updateTaskChain(List<Robot> unLoadRobotList, Map<Robot, PriorityQueue<TaskChain>> taskChainQueueMap) {
        // 以机器人的初始任务链为单位, 添加后续任务
        for (Robot rb : unLoadRobotList) {
            PriorityQueue<TaskChain> oldTaskChainList = taskChainQueueMap.get(rb);
            PriorityQueue<TaskChain> newTaskChainList = new PriorityQueue<TaskChain>(
                    (a, b) -> Double.compare(b.getReward(), a.getReward())); // 降序
            for (TaskChain taskChain : oldTaskChainList) {
                boolean addNewTaskChain = false;
                Task lastTask = taskChain.getTaskChain().get(taskChain.getTaskChain().size() - 1);
                // 遍历任务链中最后一个任务的后续任务,如果没有后续任务进行下一次遍历
                if (lastTask.getPostTaskList().isEmpty()) {
                    continue;
                }
                for (Task postTask : lastTask.getPostTaskList()) {
                    // 未生产，直接访问下个后续任务 或者 lastTask生产的产品已经出现在产品格中
                    if (postTask.getFrom().getRest() == -1
                            || (((1 << lastTask.getFrom().getType()) & lastTask.getTo().getMaterialStatus()) != 0)
                            || postTask.getFrom().isInTaskChain()) {
                        // 后继任务未生产 或者 后续任务接受栏未满 或者 后续任务已经被执行
                        continue;
                    }
                    // 开始生产, 如果生产剩余时间比机器人最快到达时间更久，说明会出现等待
                    if (postTask.getFrom().getRest() > taskChain.getFinishTime()) {
                        continue;
                    }
                    // 更新任务最早完成时间，并把该任务加入到这条任务链中
                    TaskChain newTaskChain = new TaskChain(taskChain);
                    newTaskChain.setFinishTime(taskChain.getFinishTime()
                            + postTask.getDistance() / Const.MAX_BACKWARD_VELOCITY * Const.FRAME_PER_SECOND);
                    newTaskChain.getTaskChain().add(postTask);
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

    /*
     * 初始化每个工作台的内容，主要完成每个工作台的产品去向以及他们之间的距离初始化，方便后面复用。
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

        generateTaskLinks();
    }

    public void generateTaskLinks() {

    }
}
