package com.huawei.codecraft;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class Main {

    private static final BufferedReader inStream = new BufferedReader(new InputStreamReader(System.in));

    private static final PrintStream outStream = new PrintStream(new BufferedOutputStream(System.out), true);

    public static void main(String[] args) throws IOException, InterruptedException {
        schedule();
    }
    /**
     * 机器人列表，顺序同每帧顺序
     */
    private static Map<Integer,Robot> robotMap = new HashMap();
    /**
     * 工作台列表，<序号，工作台对象>，序号同每帧中数据的序号；因为序号从1开始，所以选用了map
     */
    private static Map<Integer,Workbench> workbenchList = new HashMap<>();
    /**
     * 工作台类型Type，和对应的类型的工作台id集合
     */
    private static Map<Integer, Set<Integer>> workbenchTypeIdSetMap = new HashMap<Integer, Set<Integer>>(){{
        for(int i=1;i<=9;i++){
            put(i,new HashSet<Integer>());
        }
    }};
    /*
     * 当前帧序号
     */
    private static int frameID;


    /**
     * 所有任务列表，剪枝后的任务，提出了低收益链路，保留高收益链路
     */
    private static List<Task> taskListAll;
    /**
     * 当前任务列表：目前可做的，等待分配给机器人的任务
     */
    private static List<Task> taskListCurrent;
    /**
     * 全局策略对象
     */
    private static Strategy strategy;
    /**
     * 当前策略中，使用的所有工作台Id集合
     */
    private static Set<Integer> strategyWorkbenchIdSet;


    private static void schedule() throws IOException, InterruptedException {
        readMapOK();
        // 计算时间价值系数；
        strategy = new Strategy(workbenchList,workbenchTypeIdSetMap);
        strategy.calcStrategy();
        int maxProfitId =  strategy.maxProfit.entrySet().stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .orElse(null).getKey();
        // 获取初始化好的任务列表，不可完成的任务timeLeft = -1，其他任务有冷却时间倒计时；
        taskListAll= strategy.getWorkbenchChain(workbenchList.get(maxProfitId));
        // 添加两倍的初始任务量
        // 根据优先级+timeLeft+createFrameId排序，分配任务给机器人
        // 1.获取isStartTask ==1 的所有任务[初始任务]
        taskListCurrent = taskListAll.stream()
                .filter(task -> task.getStartTask() == true).collect(Collectors.toList());
        strategyWorkbenchIdSet = taskListAll.stream().map(Task::getSourceId).collect(Collectors.toSet());
        strategyWorkbenchIdSet.addAll(taskListAll.stream().map(Task::getTargetId).collect(Collectors.toSet()));

        // 排序完成后,taskListCurrent按照优先级排序、优先级一致时，按照createFrameId从小到大排序
        // 给机器人分配初始任务，放到每帧中进行
        // 初始化完成后，输出OK告知判题器
        outStream.print("OK\n");
        while(readUtilOK()){
            StringBuilder builder = new StringBuilder();
            builder.append(frameID).append('\n');
            // 1. 读取地图时：根据工作台状态变化更新任务列表：在工作台生产完成时，添加运送产品任务；开始生产时，添加运送原材料任务；
            //    读取地图时：机器人若当前位于目标工作台，则执行买卖任务
            // 2. 根据优先级和createFrameId排序
            for (int robotId=0;robotId<4;robotId++) {
                Robot currentRobot = robotMap.get(robotId);
                // 机器人空闲时，考虑给机器人分配任务
                if( currentRobot.isOccupied() == false ){
                    Task selectedTask = currentRobot.selectAndSetOneTask(taskListCurrent, workbenchList);
                    if(selectedTask !=null){
                        // 有任务被分配掉了
                        taskListCurrent.remove(selectedTask);
                    }
                }
                if(currentRobot.isOccupied()){
                    // 当前有任务则执行导航
                    currentRobot.calPID();   //PID控制
                    builder.append("forward").append(' ').append(robotId).append(' ').append(currentRobot.V).append('\n');
                    builder.append("rotate").append(' ').append(robotId).append(' ').append(currentRobot.VRad).append('\n');
                }else{
                    // 否则停下来
                    builder.append("forward").append(' ').append(robotId).append(' ').append(0).append('\n');
                }
                // 执行买入卖出操作
                if(currentRobot.toBuy){
                    // 计算耗时
                    int sourceId = currentRobot.taskList.get(0).workbenchId;
                    int targetId = currentRobot.taskList.get(1).workbenchId;
                    int frameCost = strategy.allFrameCost[sourceId][targetId];
                    if( frameCost+100 < 9000-frameID){
                        // 预留了100帧，能够完成卖出任务，则买入；否则不买入
                        builder.append("buy").append(' ').append(robotId).append('\n');
                    }
                }
                if(currentRobot.toSell){
                    builder.append("sell").append(' ').append(robotId).append('\n');
                }
            }
            builder.append("OK").append('\n');
            outStream.print(builder);
            // 在当前任务为空时且机器人有空闲时，添加新链路->添加all任务和当前可做任务表
            int freeRobotCount = 0;
            for(int i=0;i<4;i++){
                Robot currentRobot = robotMap.get(i);
                if(currentRobot.taskList.size()==0){
                    freeRobotCount++;
                }
            }
        }
    }

    /**
     * 读取地图，提取出机器人和工作台坐标，存储至robotList和workbenchList
     * @return boolean, true表示读取完成
     * @author LewisQuKeyang
     * @date 2023/03/20
     */
    private static boolean readMapOK() throws IOException {
        int lineIdx= 99;
        int colIdx;
        String line;
        int workbenchCount = 0;
        int robotCount = 0;
        while ((line = inStream.readLine()) != null) {
            if ("OK".equals(line)) {
                return true;
            }else{
                for(colIdx=0;colIdx<100;colIdx++){
                    // 读取每行数据，遇到机器人和工作台的，就执行初始化
                    if (line.charAt(colIdx)!='.'){
                        // 需要初始化
                        if (line.charAt(colIdx) == 'A') {
                            // 需要初始化机器人
                            robotMap.put(robotCount++,new Robot(colIdx/2.0+0.25,lineIdx/2.0+0.25));
                        } else {
                            // 需要初始化工作台
                            int workbenchType = line.charAt(colIdx)-48;
                            // workbench序号从1开始
                            workbenchList.put(workbenchCount,new Workbench(workbenchCount, workbenchType,colIdx/2.0+0.25,lineIdx/2.0+0.25));
                            // 维护一个type和对应type的工作台id集合,workbenchTypeIdSetMap已经全部初始化了
                            workbenchTypeIdSetMap.get(workbenchType).add(workbenchCount);
                            workbenchCount++;
                        }
                    }
                }
                lineIdx--;
            }
        }
        return false;
    }

    /**
     * 读取每帧数据
     * @return boolean,true表示读取完成
     * @author LewisQuKeyang
     * @date 2023/03/22
     * TODO 每帧数据中，部分数据被丢弃，如有需要，请添加
     */
    private static boolean readUtilOK() throws IOException {
        String line;
        while ((line = inStream.readLine()) != null) {
            if ("OK".equals(line)) {
                return true;
            }else{
                String[] parts = line.split(" ");
                frameID = Integer.parseInt(parts[0]);
                inStream.readLine(); // 场上工作台数量，跳过
                for (int i = 0; i < workbenchList.size(); i++) {
                    // 工作台信息行，0:类型; 1:x;   2:y;  3:剩余生产时间; 4:原材料格状态;  5：第六个产品格状态;
                    String workbenchLine = inStream.readLine();
                    String[] workbenchInfo = workbenchLine.split(" ");
                    int workbenchType = Integer.parseInt(workbenchInfo[0]);
                    int produceFrameLeft = Integer.parseInt(workbenchInfo[3]);
                    int meterialStatus = Integer.parseInt(workbenchInfo[4]);
                    int productStatus = Integer.parseInt(workbenchInfo[5]);
                    // 456工作台刚刚生产完毕，将运送产品的任务添加到currentTaskList中
                    if(workbenchType>3 && workbenchType<7 && workbenchList.get(i).productStatus ==0 && productStatus==1){
                        // 找到对应任务、添加到currentTaskList中
                        for(int j = 0;j<taskListAll.size();j++){
                            // 添加运送产品任务
                            if( taskListAll.get(j).getSourceId()==i){
                                taskListAll.get(j).setCreateFrameId(frameID);
                                taskListCurrent.add(taskListAll.get(j));
                            }
                        }
                    }
                    // 不关心123的冷却时间，456工作台开始生产，将运送原料的任务添加到currentTaskList中
                    if(workbenchType>3 && workbenchType<7 && workbenchList.get(i).meterialStatus >0 && meterialStatus==0){
                        // 找到对应任务、添加到currentTaskList中
                        for(int j = 0;j<taskListAll.size();j++){
                            // 添加运送原料任务
                            if( taskListAll.get(j).getTargetId()==i) {
                                taskListAll.get(j).setCreateFrameId(frameID);
                                taskListCurrent.add(taskListAll.get(j));
                            }
                        }
                    }
                    // 7工作台刚刚生产完毕，将运送产品的任务添加到currentTaskList中
                    if(workbenchType==7 && workbenchList.get(i).productStatus ==0 && productStatus==1){
                        // 找到对应任务、添加到currentTaskList中
                        for(int j = 0;j<taskListAll.size();j++){
                            // 添加运送产品任务
                            if( taskListAll.get(j).getSourceId()==i){
                                taskListAll.get(j).setCreateFrameId(frameID);
                                taskListCurrent.add(taskListAll.get(j));
                            }
                        }
                    }
                    workbenchList.get(i).produceFrameLeft = produceFrameLeft;
                    workbenchList.get(i).meterialStatus = meterialStatus;
                    workbenchList.get(i).productStatus = productStatus;
                }
                for (int robotId = 0; robotId < 4; robotId++) {
                    // 机器人信息获取，调用机器人updateRobotInfoPerFrame更新数据，
                    String robotLine = inStream.readLine();
                    String[] robotInfo = robotLine.split(" ");
                    Robot currentRobot = robotMap.get(robotId);
                    currentRobot.updateRobotInfoPerFrame(robotInfo,workbenchList);
                }
            }
        }
        return false;
    }
}
