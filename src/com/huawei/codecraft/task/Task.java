package com.huawei.codecraft.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huawei.codecraft.agent.Workbench;
import com.huawei.codecraft.constants.Const;
import com.huawei.codecraft.utils.Utils;

/**
 * @description: 任务对象
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
    // 为了产出4，5，6均衡, 我们为4，5，6号工作台产能计数
    private static Map<Integer, Integer> balanceMap;

    static {
        balanceMap = new HashMap<>();
        balanceMap.put(4, 1);
        balanceMap.put(5, 1);
        balanceMap.put(6, 1);
        balanceMap.put(10, 1); // 表示产出最大值
    }

    public static void updateBalanceMap(int key){
        balanceMap.put(key, balanceMap.get(key)+1);
        if(balanceMap.get(key) > balanceMap.get(10)){
            balanceMap.put(10, balanceMap.get(key));
        }
    }

    public static Map<Integer, Integer> getBalanceMap(int key){
        return balanceMap;
    }

    public Task(Workbench from, Workbench to) {
        this.from = from;
        this.to = to;

        // 根据控制台类型确定价格
        Integer[] priceInfo = Const.priceMapper.get(from.getType());
        this.price = priceInfo[0];
        this.sellPrice = priceInfo[1];

        // 计算工作台距离图1
        this.distance = Utils.computeDistance(from.getPos(), to.getPos());

        /*
         * 给同等类型但是距离较短的任务较高的优先级
         * 给不同类型但是生产成品类型更复杂的任务较高的优先级
         */
        this.priority = (sellPrice - price) / distance + from.getType();

        this.visited = false;
        this.postTaskList = new ArrayList<>();
    }

    public Task(Task source) {
        this.from = source.from;
        this.to = source.to;
    }

    /** 获取后续任务列表 */
    public List<Task> getPostTaskList() {
        return postTaskList;
    }

    /** 设定后续任务 */
    public void setPostTaskList(List<Task> postTaskList) {
        if (postTaskList == null) {
            return;
        }
        this.postTaskList = postTaskList;
    }

    /** 获取任务来源工作台 */
    public Workbench getFrom() {
        return from;
    }

    /** 获取任务目标工作台 */
    public Workbench getTo() {
        return to;
    }

    /** 获取来源工作台索引 */
    public int getFromIdx() {
        return from.getWorkbenchIdx();
    }

    /** 获取目标工作台索引 */
    public int getToIdx() {
        return to.getWorkbenchIdx();
    }

    /** 返回生产物品，实则就是来源工作台类型 */
    public int getProductType() {
        return from.getType();
    }

    /** TODO: 获取任务优先级？ */
    public double getPriority() {
        return priority;
    }

    /** 设置任务优先级 */
    public void setPriority(double priority) {
        this.priority = priority;
    }

    /** 根据给定因子获取收益 */
    public double getProfit(double timeCoefficients, double collisionCoefficients) {
        return sellPrice * timeCoefficients * collisionCoefficients - price;
    }

    /**
     * @description: 根据当前已知信息预测到达时的盈利价格
     *               当前假定速度为最大，计算时间因子
     * @return 最大收益
     */
    public double makePredict() {
        double timeCoefficient;
        double predictedFrame = distance / Const.MAX_FORWARD_FRAME;
        if (predictedFrame >= 9000) {
            timeCoefficient = 0.8;
        } else {
            timeCoefficient = (1 - Math.sqrt(1 - Math.pow(1 - (predictedFrame / 9000), 2))) * (1 - 0.8) + 0.8;
        }

        return getWeight()*((sellPrice * timeCoefficient) - price);
    }

    private double getWeight(){
        // 1号地图, 生产者少， 消费者多
        if(Const.workbenchMapper.get(1) == 1 && Const.workbenchMapper.get(2) == 1){
            int status = to.getPlanMaterialStatus() | to.getMaterialStatus();
            double weightCommand = 1;
            for(int i=1; i<7; i++){
                weightCommand += (((status&(1<<i))!=0)?1:0);
            }
            // 添加跨生产线衰减
            double weightCross = isCross();
            // 4,5,6均衡生产
            double weightBalance = 1.;
            if (to.getType() == 4 || to.getType() == 5 || to.getType() == 6) {
                weightBalance = balanceMap.get(to.getType()) == getMinValue()?2.:1.;
            }
            return weightCommand / weightCross*weightBalance;

        }
        // 2号地图,
        if(Const.workbenchMapper.get(9) == 0 || Const.workbenchMapper.get(7) == 2){
            int status = to.getPlanMaterialStatus() | to.getMaterialStatus();
            double weightCommand = 1;
            for(int i=1; i<7; i++){
                weightCommand += (((status&(1<<i))!=0)?1:0);
            }
            // 4,5,6均衡生产
            double weightBalance = 1.;
            if (to.getType() == 4 || to.getType() == 5 || to.getType() == 6) {
                weightBalance = balanceMap.get(to.getType()) == getMinValue()?4.:1.;
            }

            // 稀缺性
            double weightScarcity = 1.;
            if(from.getType() == 4){
                weightScarcity *= 4.;
            }
            return weightScarcity*weightCommand*weightBalance;

        }

        // 3号地图
        if(Const.workbenchMapper.get(7) == 0){
            int status = to.getMaterialStatus();
            double weightCommand = 1;
            for(int i=1; i<7; i++){
                weightCommand += (((status&(1<<i))!=0)?1:0);
            }

            // 阻塞资源
            double weightBlock = 1.;
            if(from.isBlocked()){
                weightBlock = 4.;
            }
            // // 只要56
            // double weightSelect = 1.;
            // if(to.getType() == 6 || to.getType() == 5){
            //     weightSelect = 4.;
            // }

            // 跨级损失
            double cross = isCross();

            double weightScarcity = getWeightScarcity();

            return weightScarcity*weightBlock*weightCommand/cross;
        }
        // 4号地图
        if(Const.workbenchMapper.get(9) == 0 || Const.workbenchMapper.get(7) == 1){
            int status = to.getPlanMaterialStatus() | to.getMaterialStatus();
            double weightCommand = 1;
            for(int i=1; i<7; i++){
                weightCommand += (((status&(1<<i))!=0)?1:0);
            }
            // 4,5,6均衡生产
            double weightBalance = 1.;
            if (to.getType() == 4 || to.getType() == 5 || to.getType() == 6) {
                weightBalance = balanceMap.get(to.getType()) == getMinValue()?4.:1.;
            }
            

            return weightCommand*weightBalance;
        }

        return 1.;
    }

    private int getMinValue(){
        int min1 = balanceMap.get(4)<balanceMap.get(5)?balanceMap.get(4):balanceMap.get(5);
        int min2 = balanceMap.get(4)<balanceMap.get(6)?balanceMap.get(4):balanceMap.get(6);
        return min1<min2?min1:min2;
    }

    private double getWeightScarcity(){

        if(to.getType() == 4 || to.getType() == 5 || to.getType() == 6 ){
            if(Const.workbenchMapper.get(to.getType())<3){
                return 2.;
            }
        }
        return 1.;

    }

    /** 获取任务距离 */
    public double getDistance() {
        return distance;
    }

    /** 获取从from到to的直线连线角度 */
    public double getAngle() {
        double x = to.getPos().getX() - from.getPos().getX();
        double y = to.getPos().getY() - from.getPos().getY();
        double quadrant = 1.; // 象限
        if (y < 0) {
            quadrant = -1.;
        }

        // 避免除0
        double mod = Math.sqrt(x * x + y * y);
        if (mod < 0.000000001) {
            return 0.;
        } else {
            return quadrant * Math.acos(x / mod); // (-pi/2, pi/2)
        }
    }

    /**  */
    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /** 判断是否跨生产线生产 */
    public double isCross() {
        if(from.getType() == 1 || from.getType() == 2 || from.getType() == 3){
            if(to.getType() == 4 || to.getType() == 5 || to.getType() == 6){
                return 1.;
            }

            if(to.getType() == 9){
                return 4.;
            }
        }

        if(from.getType() == 4 || from.getType() == 5 || from.getType() == 6){
            if(to.getType() == 7){
                return 1.;
            }

            if(Const.workbenchMapper.get(7) == 0){
                // 没有7,9就就不算跨级
                return 1.;

            }
            if(to.getType() == 9){
                return 4.;
            }
        }

        return 1.;
    }

}