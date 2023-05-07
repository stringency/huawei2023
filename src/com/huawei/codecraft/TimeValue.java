package com.huawei.codecraft;

/**
 * 时间价值系数类
 */
public class TimeValue {
    public static final double maxX = 9000;
    public static final double minRate = 0.8;
    public static double getTimeValue(int frameCount){
        if(frameCount>maxX){
            return minRate;
        }else{
            return (1-Math.sqrt(  1-  Math.pow(1-frameCount/maxX,2)  ))  *  (1-minRate) + minRate;
        }
    }
}
