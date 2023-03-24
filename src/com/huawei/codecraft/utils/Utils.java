package com.huawei.codecraft.utils;

import java.io.File;
import java.io.FileOutputStream;

import com.huawei.codecraft.vector.Vector;
import com.huawei.codecraft.vector.Coordinate;

public class Utils {
    /** 计算距离 */
    public static double computeDistance(Vector v1, Vector v2) {
        return Math.sqrt(Math.pow((v1.getX() - v2.getX()), 2) + Math.pow((v1.getY() - v2.getY()), 2));
    }

    /** 计算角度差，范围为(0-PI) */
    public static double computeAngle(double a1, double a2) {
        double diff = Math.abs(a1 - a2);
        return diff >= Math.PI ? diff - Math.PI : diff;
    }

    /** 计算角度差 */
    public static double angleDiff(double angle1, double angle2) {
        double result = angle1 - angle2;

        return result > Math.PI ? result - Math.PI : result;
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

    /** 判断是否在地图外 */
    public static boolean isOutMap(Coordinate pos) {
        double x = pos.getX();
        double y = pos.getY();

        if (x < 0.53 || x > 50 - 0.53 || y < 0.53 || y > 50 - 0.53) {
            return true;
        }

        return false;
    }

    /** 当前位置，中间位置和碰撞位置是否在同一条连线上，当前判断角度为10° */
    public static boolean online(Coordinate curr, Coordinate pos, Coordinate crash) {
        return getAngleDiff(curr, pos, crash) < 10. * Math.PI / 180.0;
    }

    /** 获取从from到to的直线连线角度 */
    public static double getAngle(Coordinate from, Coordinate to) {
        double x = from.getX() - to.getX();
        double y = from.getY() - to.getY();
        double quadrant = 1.; // 象限
        if (y < 0) {
            quadrant = -1.;
        }

        // 避免除0
        double mod = Math.sqrt(x * x + y * y);
        if (mod < 0.0001) {
            return 0.;
        } else {
            return quadrant * Math.acos(x / mod); // (-pi/2, pi/2)
        }
    }

    /** 获取当前位置, 中间位置, 和目标位置连线夹角差 */
    public static double getAngleDiff(Coordinate curr, Coordinate middle, Coordinate target) {
        double diff = getAngle(middle, curr) - getAngle(target, middle);
        if (diff > Math.PI) {
            diff -= 2 * Math.PI;
        } else if (diff < -Math.PI) {
            diff += 2 * Math.PI;
        }

        return diff;
    }
}
