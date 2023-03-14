package com.huawei.codecraft.task;

import java.util.ArrayList;
import java.util.Collection;
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

/**
 * @Description: 模型结构，主要对地图信息数据结构建模
 */
public class Task {
    private Workbench from; // 任务来源
    private Workbench to; // 任务目的
    private double priority;// 优先级
    private double price; // 原始价格
    private double sellPrice; // 完成任务所获得的最大奖励
    private double distance; // 完成任务所需要的距离

    private boolean visited; // 是否被访问
    private List<Task> postTaskList; // 后继任务列表

    public Task(Workbench from, Workbench to) {
        this.from = from;
        this.to = to;

        // 根据控制台类型确定价格
        Integer[] priceInfo = Const.priceMapper.get(from.getType());
        this.price = priceInfo[0];
        this.sellPrice = priceInfo[1];

        // 计算工作台距离
        this.distance = Utils.computeDistance(from.getPos(), to.getPos());

        /*
         * 给同等类型但是距离较短的任务较高的优先级
         * 给不同类型但是生产成品类型更复杂的任务较高的优先级
         */
        this.priority = (sellPrice - price) / distance + from.getType();

        this.visited = false;
        this.postTaskList = new ArrayList<>();

        // TODO: check
        buildGraph();

    /**
     * @description: 通过任务建图
     */
    public void buildGraph() {
        for (int key : workbenchTypeMap.keySet()) {
            // 如果是类型为8或9的工作台，只回收而不生产，那么它们不参与产生任务
            // TODO: 这里的写法只适用于目前类型属性，换类型的属性将不适用，实现不易维护
            if (key == 9 || key == 8) {
                continue;
            }

            // 其他类型工作台会产生任务, 在使用graph时必须判断这类工作台的任务是否为空
            graph.put(key, new ArrayList<Node>());
            for (Workbench from : workbenchTypeMap.get(key)) {
                int[] ids = null;
                workbenchNodeMap.put(from.getworkbenchIdx(), new ArrayList<Node>());
                switch (from.getType()) {
                    case 1: // 类型4，5，9可回收1类型工作台的产品
                        ids = new int[] { 4, 5, 9 };
                        break;
                    case 2: // 类型4，6，9可回收2类型工作台的产品
                        ids = new int[] { 4, 6, 9 };
                        break;
                    case 3: // 类型5，6，9可回收3类型工作台的产品
                        ids = new int[] { 5, 6, 9 };
                        break;
                    case 4: // 类型7， 9可回收4，5，6类型工作台的产品
                    case 5:
                    case 6:
                        ids = new int[] { 7, 9 };
                        break;
                    case 7: // 类型8，9可回收7类型工作台的产品
                        ids = new int[] { 8, 9 };
                        break;
                    default:
                        break;
                }
                for (int wbType : ids) {
                    if(!workbenchTypeMap.containsKey(wbType)) {
                        continue;
                    }
                    for (Workbench to : workbenchTypeMap.get(wbType)) {
                        // 计算任务运输距离
                        double distance = Utils.computeDistance(from.getPos(), to.getPos());
                        // 计算奖励，不考虑碰撞，以最速的方式运输的收益
                        double award = Utils.computeAward(from,
                                Utils.timeCoefficient(distance / Const.MAX_FORWARD_VELOCITY));
                        Node node = new Node(from, to, from.getType(), 0, award, distance);
                        graph.get(key).add(node);
                        workbenchNodeMap.get(from.getworkbenchIdx()).add(node);
                    }
                }
            }
        }

        // 为后续任务按距离排序
        for(int key:workbenchNodeMap.keySet()){
            Collections.sort(workbenchNodeMap.get(key), new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return Double.compare(o1.getDistance(), o2.getDistance());
                }
            });
        }

        // 为每个任务添加后续任务
        for(int key:graph.keySet()){
            for(Node metaTask : graph.get(key)){
                metaTask.setPostTaskList(workbenchNodeMap.get(metaTask.getTo().getworkbenchIdx()));
            }

        }

    }

    /**获得任务链列表
     * 
     * @param unLoadRobotList 空闲机器人列表
     * @return 任务链列表的映射图  key: 执行任务的机器人对象 value: 机器人可执行的任务链队列
     */
    public Map<Robot, PriorityQueue<NodeChain>> getTaskChainListMap(List<Robot> unLoadRobotList) {
        // 如果没有空闲的机器人，直接返回
        if(unLoadRobotList.isEmpty()){
            return null;
        }
        // key: 执行任务的机器人 value：任务链列表
        Map<Robot, PriorityQueue<NodeChain>> taskChainQueueMap = new HashMap<>();
        // new PriorityQueue<NodeChain>((a, b)-> Double.compare(b.getReward(), a.getReward())); // 降序

        for(int i = 1; i < 8; i++){
            // taskType是指类型为type的工作台生产出来的任务, 如果没有该类型工作台, 那么访问下一个工作台类型
            List<Node> taskTypeList = graph.get(i);
            if(taskTypeList == null){
                continue;
            }
            for(Node taskType:taskTypeList){
                // taskType选择最近的机器人
                if(taskType.getFrom().getRest() == -1 || ((1<<taskType.getFrom().getType()) & taskType.getTo().getMaterialStatus())!=0 || taskType.getFrom().isInTaskChain()){
                    // 工作台未生产 或者 该任务的接受处已满 或者 这个工作已经被选中正在执行
                    continue;
                }
                
                // 如果工作台投入生产
                double minTime=(double)Const.FRAME_PER_SECOND*180;
                Robot taskReceiver=null;
                // 遍历空闲机器人列表，选取最近的任务链
                for(Robot unloadRobot:unLoadRobotList){
                    Coordinate robotPos = unloadRobot.getPos();
                    Coordinate taskPos = taskType.getFrom().getPos();
                    // 这里的receiveTaskTime指得是机器人到初始任务所需最短时间
                    double receiveTaskTime = Utils.computeDistance(robotPos, taskPos)/Const.MAX_BACKWARD_VELOCITY*Const.FRAME_PER_SECOND;
                    // 运输时间小于生产剩余时间，那么需要等待，直接放弃该任务
                    if(receiveTaskTime<taskType.getFrom().getRest()){
                        continue;
                    }
                    // 运输时间大于生产剩余时间，那么到达该任务后直接可以接收该任务，该把这个任务分给哪个机器人？
                    if(receiveTaskTime < minTime){
                        taskReceiver = unloadRobot;
                        minTime = receiveTaskTime;
                    }
                }
                // 更新该任务最早完成时间
                double finishTime = minTime + taskType.getDistance()/Const.MAX_BACKWARD_VELOCITY*Const.FRAME_PER_SECOND;
                NodeChain taskChain = new NodeChain(taskReceiver);
                taskChain.getTaskChain().add(taskType);
                taskChain.setFinishTime(finishTime);
                if(taskChainQueueMap.get(taskReceiver)==null){
                    // 未添加该对象
                    PriorityQueue<NodeChain> taskChainQueue = new PriorityQueue<NodeChain>((a, b)-> Double.compare(b.getReward(), a.getReward())); // 降序
                    taskChainQueue.add(taskChain);
                    taskChainQueueMap.put(taskReceiver, taskChainQueue);
                }else{
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
    private void updateTaskChain(List<Robot> unLoadRobotList, Map<Robot, PriorityQueue<NodeChain>> taskChainQueueMap){
        // 以机器人的初始任务链为单位, 添加后续任务
        for(Robot rb:unLoadRobotList){
            PriorityQueue<NodeChain> oldTaskChainList =  taskChainQueueMap.get(rb);
            PriorityQueue<NodeChain> newTaskChainList = new PriorityQueue<NodeChain>((a, b)-> Double.compare(b.getReward(), a.getReward())); // 降序
            for(NodeChain taskChain:oldTaskChainList){
                boolean addNewTaskChain = false;
                Node lastTask = taskChain.getTaskChain().get(taskChain.getTaskChain().size() - 1);
                // 遍历任务链中最后一个任务的后续任务,如果没有后续任务进行下一次遍历
                if(lastTask.getPostTaskList().isEmpty()){
                    continue;
                }
                for(Node postTask:lastTask.getPostTaskList()){
                    // 未生产，直接访问下个后续任务 或者 lastTask生产的产品已经出现在产品格中
                    if(postTask.getFrom().getRest()==-1 || (((1<<lastTask.getFrom().getType()) & lastTask.getTo().getMaterialStatus() )!=0) || postTask.getFrom().isInTaskChain()){
                        // 后继任务未生产 或者 后续任务接受栏未满 或者 后续任务已经被执行
                        continue;
                    }
                    // 开始生产, 如果生产剩余时间比机器人最快到达时间更久，说明会出现等待
                    if(postTask.getFrom().getRest() > taskChain.getFinishTime()){
                        continue;
                    }
                    // 更新任务最早完成时间，并把该任务加入到这条任务链中
                    NodeChain newTaskChain = new NodeChain(taskChain);
                    newTaskChain.setFinishTime(taskChain.getFinishTime() + postTask.getDistance()/Const.MAX_BACKWARD_VELOCITY*Const.FRAME_PER_SECOND);
                    newTaskChain.getTaskChain().add(postTask);
                    newTaskChainList.add(newTaskChain);
                    addNewTaskChain = true;
                }
                if(!addNewTaskChain){
                    newTaskChainList.add(taskChain);
                }
            }
            taskChainQueueMap.put(rb, newTaskChainList);
            
        }

    }

    /**
     * 
     */

     public Map<Robot, NodeChain> getTaskChainMap(Map<Robot, PriorityQueue<NodeChain>> taskChainQueueMap){
        return null;
     }
}


    
    }

    public List<Node> getPostTaskList() {
        return postTaskList;
    }

    public void setPostTaskList(List<Node> postTaskList) {
        if(postTaskList == null) {
            return;
        }
        this.postTaskList = postTaskList;
    }

    public Workbench getFrom() {
        return from;
    }

    public Workbench getTo() {
        return to;
    }

    // 返回生产物品，实则就是工作台类型
    public int getProductType() {
        return from.getType();
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public double getProfit(double timeCoefficients, double collisionCoefficients) {
        return sellPrice * timeCoefficients * collisionCoefficients - price;
    }

    /*
     * 根据当前已知信息预测到达时的盈利价格
     * 当前假定速度为最大，计算时间因子
     */
    public double makePredict() {
        double predictedFrame = distance / Const.MAX_FORWARD_VELOCITY * Const.FRAME_PER_SECOND;
        if (predictedFrame >= 9000) {
            return 0.8;
        } else {
            return (1 - Math.sqrt(1 - Math.pow(1 - (predictedFrame / 9000), 2))) * (1 - 0.8) + 0.8;
        }
    }

    public double getPrice() {
        return price;
    }

    public void setAward(double award) {
        this.award = award;
    }

    public double getDistance() {
        return distance;
    }

    public Task copy() {
        return new Task(from, to);
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

}

class NodeChain {

    // 执行任务链的机器人
    private Robot Robot;
    // 用来存储任务链中的任务
    private List<Node> taskChain;
    // 在已有任务链的条件下，完成任务最快的时间
    private double finishTime;

    public NodeChain(Robot Robot) {
        this.Robot = Robot;
        this.taskChain = new ArrayList<Node>();
        this.finishTime = 0.;
    }

    public NodeChain(NodeChain chain) {
        this.Robot = chain.getRobot();
        this.taskChain = chain.getTaskChain();
        this.finishTime = chain.getFinishTime();
    }


    public List<Node> getTaskChain() {
        return taskChain;
    }

    public void setTaskChain(List<Node> taskChain) {
        this.taskChain = taskChain;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(double finishTime) {
        this.finishTime = finishTime;
    }


    public Robot getRobot() {
        return Robot;
    }


    public void setRobot(Robot robot) {
        Robot = robot;
    } 

    public double getReward(){
        double reward = 0.0;

        for(Node metaTask:taskChain){
            reward = reward + metaTask.getAward();
        }
        return reward;
    }
    
}