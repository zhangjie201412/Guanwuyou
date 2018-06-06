package com.iot.zhs.guanwuyou.database;

import org.litepal.crud.DataSupport;

/**
 * 报警状态
 *
 * Created by star on 2018/5/22.
 */

public class AlarmState extends DataSupport {
    private String pileId;//
    private String alarmValue="";//0--黄灯闪烁  1--绿灯闪烁  2--绿灯常亮

    public String getPileId() {
        return pileId;
    }

    public void setPileId(String pileId) {
        this.pileId = pileId;
    }

    public String getAlarmValue() {
        return alarmValue;
    }

    public void setAlarmValue(String alarmValue) {
        this.alarmValue = alarmValue;
    }
}
