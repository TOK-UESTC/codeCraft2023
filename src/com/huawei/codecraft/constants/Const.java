package com.huawei.codecraft.constants;

import java.util.HashMap;
import java.util.Map;


public class Const {
    public static final int DURATION_OF_GAME = 180; // 比赛时长，单位: s
    public static final int ROBOT_NUMBER = 4; // 机器人数量
    public static final int FRAME_PER_SECOND = 50; // 每秒帧数
    public static final int INIT_FUND = 200000; // 初始资金
    public static final double ROBOT_IN_WORKBENCH = 0.4; // 机器人在工作台范围判断， 单位 m
    public static final double ROBOT_RADIUS_UNLOAD = 0.45; // 机器人空载半径
    public static final double ROBOT_RADIUS_LOADED = 0.53; // 机器人负载半径
    public static final double ROBOT_DENSITY = 20; // 机器人密度， 20kg/m^2
    public static final double MAX_FORWARD_VELOCITY = 6; // 机器人最大前进速度
    public static final double MAX_BACKWARD_VELOCITY = -2; // 机器人最大后退速度
    public static final double MAX_ANGULAR_VELOCITY = Math.PI;// 机器人最大角速度
    public static final double MAX_ROTATION_VELOCITY = Math.PI; // 机器人最大后退速度
    public static final double MAX_DISTANCE = 100; // 最大距离100， 大于50*sqrt(2)即可
    public static final double MAP_LENGTH = 50.0; // 地图长度
    // 机器人最大前进速度/frame
    public static final double MAX_FORWARD_FRAME = MAX_FORWARD_VELOCITY / FRAME_PER_SECOND;

    // key: 物品类型， value: [购买价格， 售出价格]
    public static final Map<Integer, Integer[]> priceMapper;
    // key: 工作台类型类型， value: [工作台数量]
    public static final Map<Integer, Integer> workbenchMapper;
    public static int leftFrame = 0;

    static {
        priceMapper = new HashMap<Integer, Integer[]>();
        priceMapper.put(1, new Integer[] { 3000, 6000 });
        priceMapper.put(2, new Integer[] { 4400, 7600 });
        priceMapper.put(3, new Integer[] { 5800, 9200 });
        priceMapper.put(4, new Integer[] { 15400, 22500 });
        priceMapper.put(5, new Integer[] { 17200, 25000 });
        priceMapper.put(6, new Integer[] { 19200, 27500 });
        priceMapper.put(7, new Integer[] { 76000, 105000 });
    }

    static {
        workbenchMapper = new HashMap<Integer, Integer>();
        leftFrame = 9000;
    }
}
