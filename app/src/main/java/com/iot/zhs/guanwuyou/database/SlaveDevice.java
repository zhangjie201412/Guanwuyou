package com.iot.zhs.guanwuyou.database;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by H151136 on 2/25/2018.
 */

public class SlaveDevice extends DataSupport {
    @Column(unique =  true, defaultValue = "snxxxx")
    private String serialNumber;
    private String slaveOrMaster;
    private String online;
    private String alarm;
    private String comm;
    private String battery;
    private String latestData;

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setSlaveOrMaster(String slaveOrMaster) {
        this.slaveOrMaster = slaveOrMaster;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public void setAlarm(String alarm) {
        this.alarm = alarm;
    }

    public void setComm(String comm) {
        this.comm = comm;
    }

    public void setLatestData(String data) {
        this.latestData = data;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getSerialNumber() {
        return serialNumber==null?"":serialNumber;
    }

    public String getSlaveOrMaster() {
        return slaveOrMaster==null?"0":slaveOrMaster;
    }

    public String getOnline() {
        return online==null?"0":online;//默认离线
    }

    public String getAlarm() {
        return alarm==null?"0":alarm;
    }

    public String getComm() {
        return comm==null?"0":comm;
    }



    public String getBattery() {
        return battery==null?"0":battery;
    }

    public String getLatestData() {
         return latestData;
    }
}
