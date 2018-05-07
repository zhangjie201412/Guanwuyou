package com.iot.zhs.guanwuyou.comm.http;

import java.util.List;

/**
 * Created by Administrator on 2018/3/11.
 */

public class SelectPileFinishedByPeriodInfo {
    public String clientType;
    public String code;
    public Data data;
    public static class Data {
        public List<PileFinished> pileFinishedList;
        public static class PileFinished {
            public String month;
            public String pileSumNum;
        }
    }
    public String message;
    public String msgCode;
}
