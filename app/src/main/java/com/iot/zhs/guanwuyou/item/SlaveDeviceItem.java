package com.iot.zhs.guanwuyou.item;

/**
 * Created by H151136 on 2/25/2018.
 */

public class SlaveDeviceItem {
    public int index;
    public int slaveOrMaster;
    public String sn;
    public int online;
    public int commStatus;
    public int alarmStatus;
    public int battery;

    public SlaveDeviceItem(int index, int slaveOrMaster,
                           String sn,
                           int online,
                           int comm,
                           int alarm,
                           int battery) {
        this.index = index;
        this.slaveOrMaster = slaveOrMaster;
        this.sn = sn;
        this.online = online;
        this.commStatus = comm;
        this.alarmStatus = alarm;
        this.battery = battery;
    }
}
