package com.iot.zhs.guanwuyou.comm.http;

/**
 * Created by H151136 on 3/4/2018.
 */

public class ViewAccountReportInfo {
    public String clientType;
    public String code;
    public Data data;

    public static class Data {

        public AccountReport accountReport;
        public static class AccountReport {
            public String accountReportId;
            public String actualUseConcrete;
            public String companyId;
            public String companyName;
            public String conCalValue;
            public String conGrade;
            public String conGradeId;
            public String constructionState;
            public String coordinatex;
            public String coordinatey;
            public String designOfConcrete;
            public String diffGrade;
            public String emptyPile;
            public String fillEndTime;
            public String fillStartTime;
            public String pileDiameter;
            public String pileId;
            public String pileLength;
            public String pileNumber;
            public String pileTypeId;
            public String pileTypeName;
            public String pillingMachineId;
            public String pillingMachineName;
            public String prealarmThDef;
            public String projectId;
            public String projectName;
            public String reportState;
            public String reporter;
            public String slurryCalValue;
            public String systemNumber;
            public String reportTime;
        }
    }
    public String message;
    public String token;
}
