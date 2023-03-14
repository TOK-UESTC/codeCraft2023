package com.huawei.codecraft.utils;

import java.util.List;
import java.util.Map;

import com.huawei.codecraft.agent.Workbench;

public class Utils {
    public static double computeDistance(Coordinate p1, Coordinate p2) {
        /*
         * 计算点p1 与 p2的距离
         * Args:
         * p1: 一个Coordinate对象
         * p2: 一个Coordinate对象
         * 
         * Returns:
         * 点p1 与 p2的距离
         */
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }
}
