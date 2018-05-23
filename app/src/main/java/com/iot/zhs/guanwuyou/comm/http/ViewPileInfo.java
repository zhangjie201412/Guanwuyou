package com.iot.zhs.guanwuyou.comm.http;

/**
 * Created by H151136 on 1/26/2018.
 */

public class ViewPileInfo {
    public String clientType;
    public String code;
    public Data data;
    public String loginName;
    public String message;
    public String msgCode;
    public String token;

    public static class Data {
        public Pile pile;
        public static class Pile {
            public String conGrade;
            public String conGradeId;
            public String constructionState;
            public String constructionStateName;
            public String coordinatex;
            public String coordinatey;
            public String emptyPile;
            public String fillEndTime;
            public String isHasMasterDeviceRep;
            public String pileDiameter;
            public String pileId;
            public String pileLength;
            public String pileNumber;
            public String pileTypeId;
            public String pileTypeName;
            public String projectId;
            public String projectName;
            public String state;
            public String systemNumber;
            public String reporter;
        }
    }
}
