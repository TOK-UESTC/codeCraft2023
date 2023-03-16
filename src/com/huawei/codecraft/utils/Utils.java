package com.huawei.codecraft.utils;

import java.io.File;
import java.io.FileOutputStream;

public class Utils {
    /** 计算距离 */
    public static double computeDistance(Coordinate p1, Coordinate p2) {
        return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));
    }

    /** 计算角度，返回（-pi/2 - pi/2）范围内的角 */
    public static double computeAngle(double x, double y) {
        double mod = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        double quadrant = 0.; // 象限
        if (x < 0 && y > 0) {
            quadrant = 1.;
        }
        if (x < 0 && y < 0) {
            quadrant = -1.;
        }

        // (-pi/2, pi/2)
        return Math.acos(x / mod) + quadrant * Math.PI;
    }

    /** 返回可收购给定类型工作台产物的工作台类型 */
    public static int[] getDeliverableType(int wbType) {
        int[] types = null;
        switch (wbType) {
            case 1: // 类型4，5，9可回收1类型工作台的产品
                types = new int[] { 4, 5, 9 };
                break;
            case 2: // 类型4，6，9可回收2类型工作台的产品
                types = new int[] { 4, 6, 9 };
                break;
            case 3: // 类型5，6，9可回收3类型工作台的产品
                types = new int[] { 5, 6, 9 };
                break;
            case 4: // 类型7， 9可回收4，5，6类型工作台的产品
            case 5:
            case 6:
                types = new int[] { 7, 9 };
                break;
            case 7: // 类型8，9可回收7类型工作台的产品
                types = new int[] { 8, 9 };
                break;
            default:
                types = new int[] {};
                break;
        }

        return types;
    }

    /** 返回Log所用文件的stream，方便写入 */
    public static FileOutputStream getFileStream(String path) {
        try {
            File file = new File(path);
            // 不存在文件，创建目录
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
            } else {
                // 文件存在，删除文件
                file.delete();
            }
            // 创建新的日志文件
            file.createNewFile();

            return new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
