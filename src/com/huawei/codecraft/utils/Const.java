package com.huawei.codecraft.utils;

public class Const {
    public static final int DURATION_OF_GAME = 180000; // 比赛时长，单位: ms
    public static final int ROBOT_NUMBER = 4; // 机器人数量
    public static final int FRAME_PER_SECOND = 50; // 每秒帧数
    public static final int INIT_FUND = 200000; // 初始资金
    public static final double ROBOT_IN_WORKBENCH = 0.4; // 机器人在工作台范围判断， 单位 m
    public static final double ROBOT_RADIUS_UNLOAD = 0.45; // 机器人空载半径
    public static final double ROBOT_RADIUS_LOADED = 0.53; // 机器人负载半径
    public static final double ROBOT_DENSITY = 20; // 机器人密度， 20kg/m^2
    public static final double MAX_FORWARD_VELOCITY = 6; // 机器人最大前进速度
    public static final double MAX_BACKWARD_VELOCITY = 2; // 机器人最大后退速度
    public static final double MAX_ROTATION_VELOCITY = Math.PI; // 机器人最大后退速度
    public static final double MAX_DISTANCE = 100; // 最大距离100， 大于50*sqrt(2)即可 
}
