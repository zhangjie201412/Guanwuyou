package com.iot.zhs.guanwuyou.comm.http;

import java.util.List;

/**
 * Created by H151136 on 3/7/2018.
 */

public class SelectProgressAndDiffGradeInfo {
    public String clientType;
    public String code;
    public Data data;
    public static class Data {
        public List<DiffGrade> diffGradeList;
        public static class DiffGrade {
            public String diffGrade;
            public String pileSumNum;
        }

        public PileProgress pileProgress;
        public static class PileProgress {
            public String calFinish;
            public String constructing;
            public String otherFinish;
            public String unconstruct;
        }
    }

    public String message;
    public String token;
}
