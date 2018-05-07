package com.iot.zhs.guanwuyou.item;

import java.io.Serializable;

/**
 * Created by H151136 on 1/21/2018.
 */

public class DeviceItem implements Serializable {
    private int deviceType;
    private String deviceSN;
    private String lastTime;

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public int getCodeOrSensor() {
        return codeOrSensor;
    }

    public void setCodeOrSensor(int codeOrSensor) {
        this.codeOrSensor = codeOrSensor;
    }

    public int getCodeOrSensorValue() {
        return codeOrSensorValue;
    }

    public void setCodeOrSensorValue(int codeOrSensorValue) {
        this.codeOrSensorValue = codeOrSensorValue;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    private int codeOrSensor;
    private int codeOrSensorValue;
    private int batteryLevel;

    public DeviceItem(int deviceType, String deviceSN, String lastTime, int codeOrSensor,
                      int codeOrSensorValue, int batteryLevel) {
        this.deviceSN = deviceSN;
        this.deviceType = deviceType;
        this.lastTime = lastTime;
        this.codeOrSensor = codeOrSensor;
        this.codeOrSensorValue = codeOrSensorValue;
        this.batteryLevel = batteryLevel;
    }

}
