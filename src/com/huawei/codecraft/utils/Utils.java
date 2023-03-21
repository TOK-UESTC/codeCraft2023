package com.huawei.codecraft.utils;

import java.io.File;
import java.io.FileOutputStream;

import com.huawei.codecraft.vector.Vector;

public class Utils {
    /** 计算距离 */
    public static double computeDistance(Vector v1, Vector v2) {
        return v1.sub(v2).mod();
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

    /** 计算两个向量的innerProduct */
    public double computeCosin(Vector v1, Vector v2) {
        if (v1.mod() < 0.001 || v2.mod() < 0.001) {
            // 存在零向量
            return 1.;
        } else {

        }
    }
}
