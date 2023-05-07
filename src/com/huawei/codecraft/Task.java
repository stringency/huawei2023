package com.huawei.codecraft;

import java.util.Comparator;

/**
 * Task 任务类
 */
public class Task {
//    【起点id、终点id、起点工作台Type、终点工作台type、优先级(大的优先)、预计耗时、预计收益、知否可做+剩余时间】
    /**
     * 任务的起始工作台Id
     */
    private int sourceId;
    /**
     * 任务的目标工作台Id
     */
    private int targetId;
    /**
     * 任务的起始工作台类型
     */
    private int sourceType;
    /**
     * 任务的目标工作台类型
     */
    private int targetType;
    /**
     * 任务的优先级，越小越优先
     */
    private int priority;
    /**
     * 同样类型工作台/任务的优先级，在有多重任务时，优先完成优先级数值小的；
     */
    private int typePriority;

    /**
     * 创建该任务的帧序号
     */
    private int createFrameId = 0;
    /**
     * 该任务的预计耗时，单位：帧
     */
    private int frameCost = 0;

    /**
     * 该任务的预计收益
     */
    private int profit = 0;

    /**
     * 该任务是否为初始任务；
     */
    private Boolean isStartTask;

    /**
     * 任务优先级比较器，数值小的、优先级高、排在前面
     */
    public static final Comparator<Task> priorityComparator = new Comparator<Task>() {
        public int compare(Task task1, Task task2) {
            if(task1.getPriority() < task2.getPriority()){
                return -1;
            }else if(task1.getPriority() > task2.getPriority()){
                return 1;
            }else{
                return 0;
            }
        }
    };
    /**
     * 任务创建时间(createFrameId)比较器，数值小的、创建早、排在前面
     */
    public static final Comparator<Task> creatFrameIdComparator = new Comparator<Task>() {
        public int compare(Task task1, Task task2) {
            if(task1.getCreateFrameId() < task2.getCreateFrameId()){
                return -1;
            }else if(task1.getCreateFrameId() > task2.getCreateFrameId()){
                return 1;
            }else{
                return 0;
            }
        }
    };
    /**
     * 潜在收益(profit)比较器，数值大的、排在前面
     */
    public static final Comparator<Task> profitComparator = new Comparator<Task>() {
        public int compare(Task task1, Task task2) {
            if(task1.getProfit() < task2.getProfit()){
                return 1;
            }else if(task1.getProfit() > task2.getProfit()){
                return -1;
            }else{
                return 0;
            }
        }
    };

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getSourceType() {
        return sourceType;
    }

    public void setSourceType(int sourceType) {
        this.sourceType = sourceType;
    }

    public int getTargetType() {
        return targetType;
    }

    public void setTargetType(int targetType) {
        this.targetType = targetType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCreateFrameId() {
        return createFrameId;
    }

    public void setCreateFrameId(int createFrameId) {
        this.createFrameId = createFrameId;
    }

    public int getFrameCost() {
        return frameCost;
    }

    public void setFrameCost(int frameCost) {
        this.frameCost = frameCost;
    }

    public int getProfit() {
        return profit;
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }


    public Boolean getStartTask() {
        return isStartTask;
    }

    public void setStartTask(Boolean startTask) {
        isStartTask = startTask;
    }


}
