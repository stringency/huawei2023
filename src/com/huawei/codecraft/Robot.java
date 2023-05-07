package com.huawei.codecraft;

import javax.swing.text.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *  机器人类
 *  @author LewisQuKeyang
 *  @Data 2023/03/19
 */
public class Robot {
    /**
     * 机器人的x方向位置；往右为X轴正方向，单位为米。地图左下角坐标为原点(0,0)，右上角坐标为(50,50)。
     */
    public double x;
    /**
     * 机器人的y方向位置；往上为Y轴正方向，单位为米。地图左下角坐标为原点(0,0)，右上角坐标为(50,50)。
     */
    public double y;
    /**
     * 机器人的角速度；单位：弧度/秒。正数：表示逆时针。负数：表示顺时针。
     */
    public double VRad;
    /**
     * 机器人的线速度；单位：米/秒。
     */
    public double V;
    /**
     * 机器人的x方向线速度；单位：米/秒。
     */
    public double Vx;
    /**
     * 机器人的y方向线速度；单位：米/秒。
     */
    public double Vy;
    /**
     * 机器人的朝向：弧度[-π,π]。0表示右方向，π/2表示上方向，-π/2表示下方向。
     */
    public double directionRad;
    /**
     * 机器人的目标工作台，无任务时，请设为null
     */
    public Workbench targetWorkbench;
    /**
     * 机器人持有的物品Type,未持有时，标记为0
     */
    public int itemType;
    /**
     * 机器人需要买东西
     */
    public Boolean toBuy = false;
    /**
     * 机器人需要卖东西
     */
    public Boolean toSell = false;
    /**
     * 设置机器人是否被分配任务，无任务时targetWorkbench为null
     */
    public Boolean isOccupied() {
        return taskList.size()>0;
    }
    public List<Workbench> taskList = new ArrayList<>();

    /*
     * 构造函数：设置机器人的初始属性
     */
    public Robot(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /*
     * 获取机器人的当前线速度和其分量
     */
    public void forward(double V, double Vx, double Vy) {
        this.V = V;
        this.Vx = 0;
        this.Vy = 0;
    }

    /*
     * 获取机器人的当前角度和角速度
     */
    public void rotate(double directionRad, double VRad) {
        this.directionRad = directionRad;
        this.VRad = VRad;
    }

    /*
     * 机器人的购买操作
     */
    public void setToSell(Boolean status) {
        toSell = status;
    }

    /*
     * 机器人的售出操作
     */
    public void setToBuy(Boolean status) {
        toBuy = status;
    }

    /*
     * 机器人的销毁操作
     */
    public void setToDestroy() {

    }

    /*
     * 机器人获取目标工作台
     */
    public void getWorkbenchList(Workbench workben) {
        this.targetWorkbench = workben;
    }
    /*
     * 机器人修改目标工作台
     */
    public void setTargetWorkbench(Workbench workben) {
        this.targetWorkbench = workben;
        // 根据计算，id=0时，在距离为1.36m时，要开始减速。速度kP最大为 = F/(3ρπr^2) = 4.721577
        // 根据计算，id=0时，在弧度距离为π^3ρr^4/2M=0.489(50°)时，要开始减速；角速度kP最大为 = 2M/(π^2r^4ρ) = 6.42046940
        PID = new PIDController(4.6, 0.0001, 0, 0, 6.3, 0.00001, 0, 0);
    }

    /**
     * 机器人每帧数据更新
     * 1：所处工作台id；2：携带物品类型；3：时间价值系数；4：碰撞价值系数；5：角速度
     * 6:线速度x  ; 7:线速度y； 8：朝向；  9：坐标x；  10：坐标y
     * 同时根据策略和工作台数据更新：是否需要买入和卖出物品
     */
    public void updateRobotInfoPerFrame(String[] robotInfo, Map<Integer,Workbench> workbenchList ) {
        // 所处工作台id；2：携带物品类型；3：时间价值系数；4：碰撞价值系数暂时略过
        int atWorkbenchId = Integer.parseInt(robotInfo[0]);
        int carryItemType = Integer.parseInt(robotInfo[1]);
//                double timeValueFactor = Double.parseDouble(robotInfo[2]);
//                double collisionValueFactor = Double.parseDouble(robotInfo[3]);
        double vRad = Double.parseDouble(robotInfo[4]);
        double vx = Double.parseDouble(robotInfo[5]);
        double vy = Double.parseDouble(robotInfo[6]);
        double directionRad = Double.parseDouble(robotInfo[7]);
        double positionX = Double.parseDouble(robotInfo[8]);
        double positionY = Double.parseDouble(robotInfo[9]);
        //买入成功
        if( this.itemType==0 && carryItemType>0 ){
            this.taskList.remove(0);
            this.setTargetWorkbench(this.taskList.get(0));
            this.setToBuy(false);
        }
        // 卖出成功
        if( this.itemType>0 && carryItemType==0 ){
            this.taskList.remove(0);
            this.setToSell(false);
        }
        // 买卖操作;位于目标工作台
        // 持有目标物品、原料格空则卖出；位于目标工作台且没持有物品、则买入；
        if(this.isOccupied()){
            if(this.targetWorkbench.workbenchId == atWorkbenchId){
                if( carryItemType!=0 && (workbenchList.get(atWorkbenchId).meterialStatus & (1<<carryItemType))==0){
                    // 设置卖出标记
                    this.setToSell(true);
                }
                if( carryItemType ==0 ){
                    // 买入相关操作
                    this.setToBuy(true);
                }
            }
        }
        this.itemType = carryItemType;
        this.x = positionX;
        this.y = positionY;
        this.Vx = vx;
        this.Vy = vy;
        this.V = Math.sqrt( vx * vx + vy * vy );
        this.VRad = vRad;
        this.directionRad = directionRad;
    }

    /*
     * 机器人的PID控制
     */
    PIDController PID;

    public void calPID() {
        PID.setCurrentState(x, y, directionRad);
        // 三角函数象限调节
        double targetDirRad = Math.atan((targetWorkbench.y - y) / (targetWorkbench.x - x));
        if( targetDirRad<0 && targetWorkbench.x < x && targetWorkbench.y>y){
            targetDirRad += Math.PI;
        }else if( targetDirRad>0 && targetWorkbench.x < x && targetWorkbench.y<y){
            targetDirRad -= Math.PI;
        }
        PID.SetTargetState(targetWorkbench.x, targetWorkbench.y, targetDirRad);
        PID.iterPID();
        V = PID.v;   //线速度
        VRad = PID.vR ;  //角速度

    }

    /**
     * 从任务列表中选择能够完成优先级最高的任务，设置到自己的任务列表中
     * 若无可完成任务则跳过
     * @param taskList
     * @return Task,被分配掉的任务
     */
    public Task selectAndSetOneTask(List<Task> taskList, Map<Integer,Workbench> workbenchList) {
        // 首先根据距离，提取该机器人可完成的任务(目标工作台的冷却时间<最大速度对应耗时)
        List<Task> canDoList = new ArrayList<>();
        taskList.forEach(task -> {
            Workbench targetworkbench = workbenchList.get(task.getTargetId());
            double distant = Math.sqrt(Math.pow((targetworkbench.x - this.x), 2) + Math.pow((targetworkbench.y - this.y), 2));
            int frameCost = (int) (distant / 0.12);
            int leftTime = targetworkbench.produceFrameLeft;
            if (leftTime <= frameCost || targetWorkbench.productStatus==1) {
                // 产品格有东西或者冷却时间小于耗时
                canDoList.add(task);
            }
        });
        if (canDoList.size() > 0) {
            // 机器人有可以去做的任务
            // 高优先级的优先、相同优先级按照机器人距离和createframeId先后分配
            canDoList.sort(Task.creatFrameIdComparator);
            canDoList.sort(Task.priorityComparator);
            // 机器人空闲+任务列表有任务，则给机器人分配任务并删除已分配任务
            // 设置目标工作台
            int sourceWorkbenchId = canDoList.get(0).getSourceId();
            int targetWorkbenchId = canDoList.get(0).getTargetId();
            this.taskList.add(workbenchList.get(sourceWorkbenchId));
            this.taskList.add(workbenchList.get(targetWorkbenchId));
            this.setTargetWorkbench(workbenchList.get(sourceWorkbenchId));
            // 用于删除任务
            return canDoList.get(0);
        }
        return null;
    }
}
