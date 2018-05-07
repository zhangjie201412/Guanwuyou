package com.iot.zhs.guanwuyou.comm.http;

/**
 * Created by H151136 on 2/8/2018.
 */

public class EndPourData {
    public String code;
    public String message;
    public String token;
    public Data data;

    public static class Data {
        public String diffGrade;
    }
}
