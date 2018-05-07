package com.iot.zhs.guanwuyou.comm.http;

import java.util.List;

/**
 * Created by H151136 on 1/20/2018.
 */

public class SaveAccountReportData {
    public String clientType;
    public String code;
    public Data data;
    public String message;
    public String token;

    public static class Data {
        public List<PillingMachine> pillingMachineList;
        public List<PileType> pileTypeList;
        public List<ConGrade> conGradeList;
        public Pile pile;

        public static class PillingMachine {
            public String id;
            public String showName;
        }
        public static class PileType {
            public String id;
            public String showName;
        }
        public static class ConGrade {
            public String id;
            public String showName;
        }
        public static class Pile {
            public String companyId;
            public String companyName;
            public String conGrade;
            public String conGradeId;
            public String constructionState;
            public String coordinatex;
            public String coordinatey;
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
        }
    }
}
