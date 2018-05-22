package com.iot.zhs.guanwuyou.database;

import org.litepal.crud.DataSupport;

/**
 * 报警状态
 *
 * Created by star on 2018/5/22.
 */

public class AlarmState extends DataSupport {
    private String alarmId;//主机SN_项目id
    private String alarmValue;//0--黄灯闪烁  1--绿灯闪烁  2--绿灯常亮

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getAlarmValue() {
        return alarmValue;
    }

    public void setAlarmValue(String alarmValue) {
        this.alarmValue = alarmValue;
    }
}
