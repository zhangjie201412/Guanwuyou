package com.iot.zhs.guanwuyou.utils;

/**
 * Created by H151136 on 2/25/2018.
 */

public class MessageEvent {
    public static final int EVENT_TYPE_ALARM_STATUS = 0;
    public static final int EVENT_TYPE_SERIAL_WRITE = 1;
    public static final int EVENT_TYPE_ERROR_UART = 2;
    public static final int EVENT_TYPE_UPDATE_CAL_CON = 3;
    public static final int EVENT_TYPE_UPDATE_CAL_SLURRY = 4;

    public static final int EVENT_TYPE_SERIAL_UPDATE_WRITE = 5;//主机从机在线升级
    public static final int EVENT_TYPE_MASTER_UPDATE_SUCCESS = 6;//主机从机在线升级-成功
    public static final int EVENT_TYPE_MASTERL_UPDATE_FAIL = 7;//主机从机在线升级-失败



    public int type;
    public String message;
    public byte[] chars;

    public MessageEvent(int type) {
        this.type = type;
    }
}
