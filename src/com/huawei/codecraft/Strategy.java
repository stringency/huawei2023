package com.huawei.codecraft;

import java.util.*;


/**
 * 初始状态策略计算模块
 * @author LewisQuKeyang
 * @date 2023/03/19
 */
public class Strategy {
    /**
     * 物品买入的成本
     */
    private static final int[] itemBuyPrice = { 0, 3000, 4400, 5800 , 15400, 17200, 19200, 76000 };
    /**
     * 物品卖出的原始价格
     */
    private static final int[] itemSellPrice = { 0, 6000, 7600, 9200 , 22500, 25000, 27500, 105000 };
    /**
     * Map<起点工作台为Index，能到达能够到达的所有工作台类型集合>
     */
    private static final Map<Integer, Set<Integer>> workbenckTargetTypeMap = new HashMap<Integer, Set<Integer>>(){{
        put(1, new HashSet<Integer>(){{add(4);add(5);add(9);}});
        put(2, new HashSet<Integer>(){{add(4);add(6);add(9);}});
        put(3, new HashSet<Integer>(){{add(5);add(6);add(9);}});
        put(4, new HashSet<Integer>(){{add(7);add(9);}});
        put(5, new HashSet<Integer>(){{add(7);add(9);}});
        put(6, new HashSet<Integer>(){{add(7);add(9);}});
        put(7, new HashSet<Integer>(){{add(8);add(9);}});
        put(8, new HashSet<Integer>());
        put(9, new HashSet<Integer>());
    }};

    /**
     * Map<目标工作台为Index，所有起点工作台类型集合>
     */
    public static final Map<Integer, Set<Integer>> workbenckSourceTypeMap = new HashMap<Integer, Set<Integer>>(){{
        put(1, new HashSet<Integer>());
        put(2, new HashSet<Integer>());
        put(3, new HashSet<Integer>());
        put(4, new HashSet<Integer>(){{add(1);add(2);}});
        put(5, new HashSet<Integer>(){{add(1);add(3);}});
        put(6, new HashSet<Integer>(){{add(2);add(3);}});
        put(7, new HashSet<Integer>(){{add(4);add(5);add(6);}});
        put(8, new HashSet<Integer>(){{add(7);}});
        put(9, new HashSet<Integer>(){{for(int i=1;i<=7;i++){add(i);}}});
    }};

    /**
     * 工作台列表，<序号，工作台对象>，序号同每帧中数据的序号；
     * 注意：序号从0开始
     */
    private Map<Integer,Workbench> workbenchList;
    /*
     * 工作台类型Type，和对应的类型的工作台id集合
     */
    private static Map<Integer, Set<Integer>> workbenchTypeIdSetMap = new HashMap<Integer, Set<Integer>>();
    /*
     * 该工作台的maxProfit,<工作台序号，到该工作台的最大收益>
     */
    public Map<Integer, Integer> maxProfit = new HashMap<>();
    /**
     * 任意两个工作台之间的收益表，idx与工作台序号保持一致(0行0列用于占位)
     */
    public int[][] allProfit;
    /**
     * 任意两个工作台之间的耗时表，idx与工作台序号保持一致(0行0列用于占位)
     */
    public int[][] allFrameCost;
    

    /**
     * 构造函数
     * 获取工作台分布情况、初始化收益和代价数组
     */
    public Strategy(Map<Integer,Workbench> workbenchList, Map<Integer, Set<Integer>> workbenchTypeIdSetMap) {
        this.workbenchList = workbenchList;
        this.workbenchTypeIdSetMap = workbenchTypeIdSetMap;
        this.allProfit = new int[workbenchList.size()][workbenchList.size()];
        this.allFrameCost = new int[workbenchList.size()][workbenchList.size()];
        calcFrameCost();
    }

    /**
     * 计算所有工作台之间的预计耗时；
     * @return void
     * @author LewisQuKeyang
     * @date 2023/03/22
     */
    private void calcFrameCost(){
        int size = workbenchList.size();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                // 计算两工作台之间的距离,Idx保持与工作台序号一致，行0、列0仅由于占位
                double distX = workbenchList.get(i).x - workbenchList.get(j).x;
                double distY = workbenchList.get(i).y - workbenchList.get(j).y;
                double dist = Math.sqrt( distX*distX + distY*distY);
                // 按照最大速度计算用时(帧)，机器人最大速度为6m/s = 0.12 m/f帧
                int frameToReach = (int) (dist / 0.12);
                allFrameCost[i][j] = frameToReach;
            }
        }
    }

    /**
     * 计算两个工作台之间的预计收益
     * @return profit
     * @param workbenchFrom
     * @param workbenchTo
     */
    private int calcProfit(Workbench workbenchFrom, Workbench workbenchTo){
        int frameToReach = allFrameCost[workbenchFrom.workbenchId][workbenchTo.workbenchId];
        // 获得时间价值系数
        double valueFactor = TimeValue.getTimeValue(frameToReach);
        // 计算预估总收益，根据出发点获取的物品计算 = 原始卖出价 * 系数 - 购入价
        double finalProfit = itemSellPrice[workbenchFrom.workbenchType] * valueFactor - itemBuyPrice[workbenchFrom.workbenchType];
        return (int) finalProfit;
    }

    /**
     * 计算各个工作台能够获得的最大收益
     */
    public void calcStrategy(){
        // 第一维为From起点工作台，第二维为To终点工作台;序号从0开始用
        // 枚举From和To工作台，如果可达(有买卖关系)
        // TODO只需要计算可达工作台之间的，可以不用两层for
        for(int i=0; i<workbenchList.size();i++){
            for(int j=0; j<workbenchList.size();j++){
//                System.out.println("From workbench:"+workbenchList.get(i)+"=>To:"+workbenchList.get(j));
                if(isReachable(workbenchList.get(i),workbenchList.get(j))){
                    // calc profit and store in array
                    allProfit[i][j] = calcProfit(workbenchList.get(i),workbenchList.get(j));
                }
            }
        }

        // 用动态规划/递归 计算到达各个节点的的最佳链路；从最高级(9)的工作台开始
        // 首先将type=1 2 3工作台的maxProfit初始化为0；
        for(int i=0;i<workbenchList.size();i++){
            int workbenchType = workbenchList.get(i).workbenchType;
            if( workbenchType==1 || workbenchType==2 | workbenchType==3 ){
                maxProfit.put(workbenchList.get(i).workbenchId,0 );
            }
        }
        // 一旦找到最高级的type,只需要计算一次即可，下级的工作台全部会被算到；
        // 例如9，下级工作台包括全部工作台
        // 例如8，目前有8就一定有7，有7，下级也都有
        // 因此目前只需要从最高级开始递归即可，减少计算次数
        for(int type =9; type>3;type--){
            if(workbenchTypeIdSetMap.get(type).size()>0){
                // 有该类型工作台，从该类开始计算
                for( Integer workbenchId:workbenchTypeIdSetMap.get(type)){
                    calcMaxProfit( workbenchList.get(workbenchId));
                }
                break;
            }
        }
    }

    /**
     * 判断起点工作台到目标工作台是否有效
     * @param workbenchFrom
     * @param workbenchTo
     * @return boolean,true表示有效，false表示无意义
     */
    private boolean isReachable(Workbench workbenchFrom, Workbench workbenchTo){
        int workbenchfromType = workbenchFrom.workbenchType;
        Set reachableSet = workbenckTargetTypeMap.get(workbenchfromType);
        if(reachableSet.contains(workbenchTo.workbenchType)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 计算到达这个工作台的最大收益；
     * @param workbenchTarget
     * @return [int]targetMaxProfit，到达这个工作台的最大收益
     */
    private int calcMaxProfit(Workbench workbenchTarget){
        // 获取该工作台类型
        int targetWorkbenchType = workbenchTarget.workbenchType;
        // 类型为1 2 3，没有下级工作台，maxProfit=0,已经初始化到maxProfit中了
        if( maxProfit.containsKey(workbenchTarget.workbenchId)){
            // 该maxProfit已经计算过了
            return maxProfit.get(workbenchTarget.workbenchId);
        }
        // 不是1 2 3工作台，且maxProfit还未计算
        // 获取下级工作台类型、和对应的ID，即<Type,Set<Integer>>reachableTypeIdSetMap对应映射；
        Set<Integer> sourceTypeSet = workbenckSourceTypeMap.get(targetWorkbenchType);
        Map<Integer,Set<Integer>> sourceTypeIdSetMap = new HashMap<>();
        Set<Integer> sourceIdSet = new HashSet<>();
        for(Integer sourceType:sourceTypeSet){
            sourceTypeIdSetMap.put( sourceType, workbenchTypeIdSetMap.get(sourceType));
            sourceIdSet.addAll( workbenchTypeIdSetMap.get(sourceType)) ;
        }
        // 对于每个类型，求出最大的Profit
//        Map<Integer,Integer> typeMaxProfit = new HashMap<>();
        // 计算目标工作台的最大profit = 各个类型的typeMaxProfit相加
        int targetMaxProfit = 0;
        for(Integer type:sourceTypeSet){
            int typeMaxProfitTemp = 0;
            for(Integer Id:sourceTypeIdSetMap.get(type)){
                typeMaxProfitTemp = Math.max(typeMaxProfitTemp, calcMaxProfit(workbenchList.get(Id)) + calcProfit( workbenchList.get(Id), workbenchTarget ));
            }
//            typeMaxProfit.put(type,typeMaxProfitTemp );
            targetMaxProfit = targetMaxProfit + typeMaxProfitTemp;
        }
        // 更新到maxProfit
        maxProfit.put( workbenchTarget.workbenchId, targetMaxProfit);
        return targetMaxProfit;
    }

    /**
     * 获取到达该工作台的最大收益链路对应的任务;
     * TODO: 如果后续需要计算其他的工作台对应的最优链路，那么可以初始化时全部计算完，后续可以直接调用
     * @param targetWorkbench
     * @return 返回任务列表：【起点id、终点id、起点工作台Type、终点工作台type、优先级、任务创建的frameID、预计耗时、预计收益、知否可做+剩余时间】
     */
    public List<Task> getWorkbenchChain(Workbench targetWorkbench){
        if(maxProfit.isEmpty()){
            // 还没计算出最优链路
            return null;
        }
        int targetType = targetWorkbench.workbenchType;
        if(targetType==1 || targetType==2 || targetType==3){
            // 123工作台最为目标时，没有对应的任务
            return null;
        }
        // 正常计算流程
        Set<Integer> searchIdSet = new HashSet<>();
        List<Task> taskList = new ArrayList<>();
        // 1. targetId添加到搜索集合中
        searchIdSet.add(targetWorkbench.workbenchId);
        while(searchIdSet.size()>0){
            int targetId = searchIdSet.iterator().next();
            // 2. 对其source type，逐个找出（source工作台收益+source->target线路收益）最大的，作为target工作台对应链路
            targetType = workbenchList.get(targetId).workbenchType;
            Set<Integer> sourceTypeSet = workbenckSourceTypeMap.get(targetType);
            // 3. 对其每个类型，找出收益最大的一个ID
            for(Integer sourceType : sourceTypeSet){
                Set<Integer> typeIdSet = workbenchTypeIdSetMap.get(sourceType);
                if(typeIdSet.size()>0){
                    // 没有这一类型时需要跳过。
                    int selectedSourceId = 0;
                    int totalProfit = 0;
                    for( int sourceId : typeIdSet ){
                        int tempProfit = maxProfit.get(sourceId) + allProfit[sourceId][targetId];
                        if( tempProfit > totalProfit ){
                            selectedSourceId = sourceId;
                            totalProfit = tempProfit;
                        }
                    }
                    // 常见任务列表：【起点id、终点id、起点工作台Type、终点工作台type、优先级(大的优先)、预计耗时、预计收益、知否可做+剩余时间】
                    Task task = new Task();
                    task.setSourceId(selectedSourceId);
                    task.setSourceType(sourceType);
                    task.setTargetId(targetId);
                    task.setTargetType(targetType);
                    task.setPriority(getPriority(sourceType, targetType));
                    task.setCreateFrameId(0);
                    task.setFrameCost(allFrameCost[selectedSourceId][targetId]);
                    task.setProfit(totalProfit);
                    // 123类工作台的工作是一开始就干的；
                    if(sourceType ==1 || sourceType==2 || sourceType ==3){
                        task.setStartTask(true);
                    }else{
                        task.setStartTask(false);
                    }
                    taskList.add(task);
                    searchIdSet.add(selectedSourceId);
                }
            }
            searchIdSet.remove(targetId);
        }
        return taskList;
    }

    /**
     * 计算任务的优先级
     * @param sourceType
     * @param targetType
     * @return int priority 越大优先级越高
     */
    public int getPriority(int sourceType, int targetType){
        int priority = 10;  //默认最低优先级
        if(sourceType ==7 ){
            priority = 1;
        }
        if(targetType == 7){
            priority =  3;
        }if(targetType ==9 && sourceType>=4){
            priority =  5;
        }
        if(sourceType <=3){
            if(targetType == 9){
                priority = 9;
            }else{
                priority = 7;
            }
        }
        return priority;
    }

    public Workbench searchNextStartWorkbench(Workbench currentWorkbench){
        // 从同一类别的工作台开始搜索
        int currentWorkbenchType = currentWorkbench.workbenchType;
        if(workbenchTypeIdSetMap.get(currentWorkbenchType).size()==1){
            // 同一类型的只有一个工作台,返回null，需要继续搜索原料工作台
            return null;
        }else{
            // 还有其他同类型的工作台、返回收益第二的工作台即可；
            return null;
        }
    }
}