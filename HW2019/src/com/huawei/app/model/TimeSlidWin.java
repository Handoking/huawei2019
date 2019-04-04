package com.huawei.app.model;

/**
 * @author :Handoking
 * @date : 2019/3/17
 */
public class TimeSlidWin {
    private int curTime ;
    private SlidWin sw ;
    private SlidWin curSW;
    public TimeSlidWin(int size){
        curTime =-1;
        sw = new SlidWin(size);
        curSW =new SlidWin(size);
    }
    private void updateTime(int t) {
        if(curTime <0) curTime =t;
        else if(curTime <t) {
            while(curTime <t) { sw.next(); curSW.next();
                curTime++;}
        }
        else if(curTime >t)
            throw new IllegalArgumentException("curTime>T: "+ curTime +","+t);
    }
    public void add(int t, int v) {
        updateTime(t);
        curSW.add(1);
        sw.add(v);
    }
    public double getAvg(int t) {
        updateTime(t);
        if(curSW.getAvg()==0.0) return 0.0;
        return sw.getAvg()/ curSW.getAvg();
    }
}
