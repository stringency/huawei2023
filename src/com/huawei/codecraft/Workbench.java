package com.huawei.codecraft;

/*
 *  工作台类
 *  @author LewisQuKeyang
 *  @Data 2023/03/19
 */
public class Workbench {
    /*
     * 工作台的序号，与每帧返回数据一致
     */
    public int workbenchId;

    /*
     * 工作台的x方向位置；往右为X轴正方向，单位为米。地图左下角坐标为原点(0,0)，右上角坐标为(50,50)。
     */
    public double x;
    /*
     * 工作台的y方向位置；往上为Y轴正方向，单位为米。地图左下角坐标为原点(0,0)，右上角坐标为(50,50)。
     */
    public double y;
    /*
     * 工作台类型
     */
    public int workbenchType;
    /*
     * 剩余生产时间
     */
    public int produceFrameLeft;
    /*
     * 原材料格状态
     */
    public int meterialStatus;
    /*
     * 产品格状态
     */
    public int productStatus;

    /*
     * 构造函数：设置工作台的基本属性
     */
    public Workbench(int workbenchId, int workbenchType,double x,double y) {
        this.workbenchId = workbenchId;
        this.workbenchType = workbenchType;
        this.x = x;
        this.y = y;
    }

    public int getWorkbenchId() {
        return workbenchId;
    }

    public void setWorkbenchId(int workbenchId) {
        this.workbenchId = workbenchId;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getWorkbenchType() {
        return workbenchType;
    }

    public void setWorkbenchType(int workbenchType) {
        this.workbenchType = workbenchType;
    }

    public int getProduceFrameLeft() {
        return produceFrameLeft;
    }

    public void setProduceFrameLeft(int produceFrameLeft) {
        this.produceFrameLeft = produceFrameLeft;
    }

    public int getMeterialStatus() {
        return meterialStatus;
    }

    public void setMeterialStatus(int meterialStatus) {
        this.meterialStatus = meterialStatus;
    }

    public int getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(int productStatus) {
        this.productStatus = productStatus;
    }

    @Override
    public String toString() {
        return "Workbench{" +
                "workbenchId=" + workbenchId +
                ", x=" + x +
                ", y=" + y +
                ", workbenchType=" + workbenchType +
                '}';
    }
}
