package com.iot.zhs.guanwuyou.database;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * Created by H151136 on 2/25/2018.
 */

public class SlaveDevice extends DataSupport {
    @Column(unique =  true, defaultValue = "snxxxx")
    private String serialNumber;
    private int slaveOrMaster;
    private int online;
    private int alarm;
    private int comm;
    private int battery;
    private int latestData;

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setSlaveOrMaster(int slaveOrMaster) {
        this.slaveOrMaster = slaveOrMaster;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public void setAlarm(int alarm) {
        this.alarm = alarm;
    }

    public void setComm(int comm) {
        this.comm = comm;
    }

    public void setLatestData(int data) {
        this.latestData = data;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getSlaveOrMaster() {
        return slaveOrMaster;
    }

    public int getOnline() {
        return online;
    }

    public int getAlarm() {
        return alarm;
    }

    public int getComm() {
        return comm;
    }

    public int getBattery() {
        return battery;
    }

    public int getLatestData() {
         return latestData;
    }
}
