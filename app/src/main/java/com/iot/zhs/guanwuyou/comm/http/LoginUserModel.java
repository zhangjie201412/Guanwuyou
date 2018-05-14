package com.iot.zhs.guanwuyou.comm.http;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by H151136 on 1/15/2018.
 */

public class LoginUserModel implements Serializable {
    public String companyId;
    public String companyName;
    public List<ConstructState> constructStateList;
    public String masterDeviceSN;
    public String mobileNO;
    public String projectId;
    public String projectName;
    public String userId;
    public String userName;
    public List<ShowModel> diffDegreeList=new ArrayList<>();//等级差异
    public static class ConstructState implements Serializable {
        public String id;
        public String showName;

        public ConstructState(String id, String showName) {
            this.id = id;
            this.showName = showName;
        }
    }

    public static class ShowModel implements Serializable {
        public String id;
        public String showName;

        public ShowModel(String id, String showName) {
            this.id = id;
            this.showName = showName;
        }
    }




}
