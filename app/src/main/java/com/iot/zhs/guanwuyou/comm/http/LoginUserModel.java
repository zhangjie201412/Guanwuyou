package com.iot.zhs.guanwuyou.comm.http;

import java.util.List;

/**
 * Created by H151136 on 1/15/2018.
 */

public class LoginUserModel {
    public String companyId;
    public String companyName;
    public List<ConstructState> constructStateList;
    public String masterDeviceSN;
    public String mobileNO;
    public String projectId;
    public String userId;
    public String userName;
    public static class ConstructState {
        public String id;
        public String showName;
    }
}
