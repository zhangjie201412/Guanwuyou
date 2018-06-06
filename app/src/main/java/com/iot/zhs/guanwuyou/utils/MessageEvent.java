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




    public int type;
    public String message;

    public MessageEvent(int type) {
        this.type = type;
    }
}
