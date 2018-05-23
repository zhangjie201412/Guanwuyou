package com.iot.zhs.guanwuyou.comm.http;

/**
 * Created by H151136 on 1/31/2018.
 */

public class ProcessProtocolInfo {
    public String clientType;
    public String code;
    public DataModel data;
    public String message;
    public String msgCode;

    public class DataModel{
        public String protocol;
        public String downloadUrl;

    }
}
