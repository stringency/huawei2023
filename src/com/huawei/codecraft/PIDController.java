package com.huawei.codecraft;

public class PIDController {
//    private Robot robot;   //当前机器人对象
    private double Kp_x; // 比例项系数
    private double Ki_x; // 积分项系数
    private double Kd_x; // 微分项系数
//    private double T_x; // 计算周期
//    private double Ti_x; // 积分常数，积分时间
//    private double Td_x; // 微分常数，微分时间
    private double Out0_x; // 常数，避免失控的情况出现

    private double Kp_directionRad; // 比例项系数
    private double Ki_directionRad; // 积分项系数
    private double Kd_directionRad; // 微分项系数
//    private double T_directionRad; // 计算周期
//    private double Ti_directionRad; // 积分常数，积分时间
//    private double Td_directionRad; // 微分常数，微分时间
    private double Out0_directionRad; // 常数，避免失控的情况出现

    private double integral_x = 0.0; // 积分项累计误差
    private double Ek_x = Double.MAX_VALUE; // 本次的误差/当前位置与目标值的距离
    private double lastEk_x = Double.MAX_VALUE; // 上一次的误差/当前位置与目标值的距离
    private double lastlastEk_x = Double.MAX_VALUE; // 上上一次的误差/与目标值的距离

    private double integral_directionRad = 0.0; // 积分项累计误差（弧度）
    private double Ek_directionRad = 0.0; // 本次的误差/当前位置与目标值的（弧度）
    private double lastEk_directionRad = 0.0; // 上一次的误差/当前位置与目标值的（弧度）
    private double lastlastEk_directionRad = 0.0; // 上上一次的误差/与目标值的（弧度）

    // 输出上限
    private double outMax = Double.MAX_VALUE;
//    private double outMax_x = Double.MAX_VALUE;
//    private double outMax_y = Double.MAX_VALUE;
    private double outMax_directionRad = Double.MAX_VALUE;

    // 输出下限
    private double outMin = Double.MIN_VALUE;
//    private double outMin_x = Double.MIN_VALUE;
//    private double outMin_y = Double.MIN_VALUE;
    private double outMin_directionRad = Double.MIN_VALUE;

    // 目标x,y,角度值
    private double targetX;
    private double targetY;
    private double targetRad;

    // 当前x,y,角度值
    public double currentX;
    public double currentY;
    public double currentRad;

    //    // 构造函数，设置PID算法的四个常数，以确定三个重要系数
//    public PIDController(double Kp_xy, double T_xy, double Ti_xy, double Td_xy, double Out0_xy, double Kp_directionRad,
//                         double T_directionRad, double Ti_directionRad, double Td_directionRad, double Out0_directionRad) {
//        this.Kp_xy = Kp_xy;
//        this.Ki_xy = Kp_xy * (T_xy / Ti_xy);
//        this.Kd_xy = Kp_xy * (Td_xy / T_xy);
//        this.Out0_xy = Out0_xy;
//        this.Kp_directionRad = Kp_directionRad;
//        this.Ki_directionRad = Kp_directionRad * (T_directionRad / Ti_directionRad);
//        this.Kd_directionRad = Kp_directionRad * (Td_directionRad / T_directionRad);
//        this.Out0_directionRad = Out0_directionRad;
//    }
    // 构造函数，设置PID算法的四个常数，以确定三个重要系数
    public PIDController(double Kp_x, double Ki_x, double Kd_x, double Out0_x, double Kp_directionRad,
                         double Ki_directionRad, double Kd_directionRad, double Out0_directionRad) {

        this.Kp_x = Kp_x;
        this.Ki_x = Ki_x;
        this.Kd_x = Kd_x;
        this.Out0_x = Out0_x;
        this.Kp_directionRad = Kp_directionRad;
        this.Ki_directionRad = Ki_directionRad;
        this.Kd_directionRad = Kd_directionRad;
        this.Out0_directionRad = Out0_directionRad;
    }

    // 计算距离的函数
    public double calculateDistance(double x1, double y1, double x2, double y2) {
        if (Ek_x < 0.0001) {
            return 0;
        } else {
            return Math.sqrt(Math.abs(x2 - x1) * Math.abs(x2 - x1) + Math.abs(y2 - y1) * Math.abs(y2 - y1));
        }
    }

    /**
     设置当前位置
     */
    public void setCurrentState(double currentX, double currentY, double currentDirRad) {
//        this.robot = robot;
        this.currentX = currentX;
        this.currentY = currentY;
        this.currentRad = currentDirRad;
    }

    /**
     * 设置目标位置
     */
    public void SetTargetState(double targetX, double targetY, double targetDirRad) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetRad = targetDirRad;
    }

    /**
     * 计算当前误差
     */
    public void calcError() {
        Ek_x = calculateDistance(currentX, currentY, targetX, targetY);
        Ek_directionRad = targetRad - currentRad;
    }

    // 设置PID算法的输出上限和下限
    public void setOutputLimit() {
//		double c = Math.cos(Pv_directionRad);
//		double s = Math.sin(Pv_directionRad);
//		outMax_x = 6 * Math.cos(Pv_directionRad);
//		outMin_x = -2 * Math.cos(Pv_directionRad);
//		outMax_y = 6 * Math.sin(Pv_directionRad);
//		outMin_y = -2 * Math.sin(Pv_directionRad);
        outMax = 6;
        outMin = -2;
        outMax_directionRad = Math.PI;
        outMin_directionRad = -Math.PI;
    }

    // PID算法的核心函数，根据当前误差/当前位置与目标值的距离来计算输出值
    public double compute_x() {
        // 计算比例项输出
        double outputP = Kp_x * Ek_x + Out0_x;

        // 计算积分项输出
        integral_x = integral_x + Ek_x;
        double outputI = Ki_x * integral_x + Out0_x;

        // 计算微分项输出
        double deriv = (lastEk_x - Ek_x) - (lastlastEk_x - lastEk_x);
        double outputD = Kd_x * deriv + Out0_x;

        // 计算总的输出值
        double output = outputP + outputI + outputD;

        // 调整输出值范围
        if (output > outMax) {
            output = outMax;
        } else if (output < outMin) {
            output = outMin;
        }

        // 保存上次误差/当前位置与目标值的距离作为后面的上上次误差/当前位置与目标值的距离
        lastlastEk_x = lastEk_x;
        // 保存当前误差/当前位置与目标值的距离作为后面的上次误差/当前位置与目标值的距离
        lastEk_x = Ek_x;

        return output;// 输出的值就是速度
    }

    public double compute_directionRad() {
        // 计算比例项输出
        double outputP = Kp_directionRad * Ek_directionRad + Out0_directionRad;

        // 计算积分项输出
        integral_directionRad = integral_directionRad + Ek_directionRad;
        double outputI = Ki_directionRad * integral_directionRad + Out0_directionRad;

        // 计算微分项输出
        double deriv = (lastEk_directionRad - Ek_directionRad) - (lastlastEk_directionRad - lastEk_directionRad);
        double outputD = Kd_directionRad * deriv + Out0_directionRad;

        // 计算总的输出值
        double output = outputP + outputI + outputD;

        // 调整输出值范围
        if (output > outMax_directionRad) {
            output = outMax_directionRad;
        } else if (output < outMin_directionRad) {
            output = outMin_directionRad;
        }

        // 保存上次误差/当前位置与目标值的距离作为后面的上上次误差/当前位置与目标值的距离
        lastlastEk_directionRad = lastEk_directionRad;
        // 保存当前误差/当前位置与目标值的距离作为后面的上次误差/当前位置与目标值的距离
        lastEk_directionRad = Ek_directionRad;

        return output;// 输出的值就是速度
    }

    //任务完成
    public void finishWork() {
        integral_x = 0.0; // 积分项累计误差
        Ek_x = Double.MAX_VALUE; // 本次的误差/当前位置与目标值的距离
        lastEk_x = Double.MAX_VALUE; // 上一次的误差/当前位置与目标值的距离
        lastlastEk_x = Double.MAX_VALUE; // 上上一次的误差/与目标值的距离
    }

    public double vR;  //角速度
    public double v;   //线速度
    public double v_x; //线速度x分量
    public double v_y;//线速度y分量
    // PID迭代
    public void iterPID() {
        calcError(); // 计算误差
        setOutputLimit(); // 设置最大最小值

        // 在地图边缘(距离小于6m/s时最小转弯半径)，则先转再动
        // 角速度最大为pi，那么转一圈要2s 最大速度6m/s，半径 =1.909859m
        Boolean isOnEdge = currentX<2 || currentX>48 || currentY<2 || currentY>48;
        // 角度转一圈要2s至少，若此时速度为6,
        if(isOnEdge || calculateDistance(currentX,currentY,targetX,targetY)<12){
            if (Math.abs(targetRad - currentRad) < 0.1) {
                this.v = compute_x(); // 线速度
                this.v_x = v * Math.cos(currentRad);
                this.v_y = v * Math.sin(currentRad);
                this.vR = compute_directionRad();
            } else {
                this.vR = compute_directionRad();
//        }
            }
        }else{
            // 机器人不在地图边缘,且距离目标较远，角速度大的时候，线速度小；否则全速前进！
            // 角速度>2.1时，线速度=1；角速度<0.1时，线速度=6，插值
            this.vR = compute_directionRad();
            if(this.vR> 0.21){
                this.v = 1; // 线速度
            } else if (this.vR<0.01) {
                this.v = 6; // 线速度
            }else{
                this.v = 6.25-25*this.vR;
            }
        }
    }
}
