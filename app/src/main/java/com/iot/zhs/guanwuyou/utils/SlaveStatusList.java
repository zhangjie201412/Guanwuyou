package com.iot.zhs.guanwuyou.utils;

import java.util.List;

/**
 * Created by H151136 on 2/10/2018.
 */

public class SlaveStatusList {
    public List<SlaveStatus> slaveStatusList;

    public static class SlaveStatus {
        public String slaveSerialNumber;
        public String online;
        public String versionStatus;
        public String commStatus;
        public String thresholdStatus;
        public String networkStatus;
    }
}
