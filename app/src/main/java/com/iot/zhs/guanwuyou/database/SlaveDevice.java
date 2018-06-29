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
    private String online;//通讯状态-在线、离线
    private String alarm;//报警
    private String comm;//设备状态
    private String battery;//电量
    private String latestData;//raw最后一笔
    private String versionStatus;//版本号状况
    private String sensorStatus;//传感器
    private String motorStatus;//问六七

    public String getVersionStatus() {
        return versionStatus==null?"0":versionStatus;
    }

    public void setVersionStatus(String versionStatus) {
        this.versionStatus = versionStatus;
    }

    public String getSensorStatus() {
        return sensorStatus==null?"0":sensorStatus;
    }

    public void setSensorStatus(String sensorStatus) {
        this.sensorStatus = sensorStatus;
    }

    public String getMotorStatus() {
        return motorStatus==null?"0":motorStatus;
    }

    public void setMotorStatus(String motorStatus) {
        this.motorStatus = motorStatus;
    }

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
