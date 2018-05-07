package com.iot.zhs.guanwuyou.comm.http;

import java.util.List;

/**
 * Created by H151136 on 1/24/2018.
 */

public class SelectPileOfAppInfo {
    public String clientType;
    public String code;
    public Data data;
    public String loginName;
    public String message;
    public String msgCode;
    public String token;

    public static class Data {
        public Page page;

        public static class Page {
            public int allPage;
            public int count;
            public boolean disabled;
            public int first;
            public boolean firstPage;
            public int firstResult;
            public String funcName;
            public int last;
            public boolean lastPage;
            public int lastResult;
            public List<PileInfo> list;
            public int maxResults;
            public int next;
            public boolean notCount;
            public String orderBy;
            public int pageNo;
            public int pageSize;
            public int prev;
            public int totalPage;

            public static class PileInfo {
                public String conGrade;
                public String conGradeId;
                public String constructionState;
                public String constructionStateName;
                public String coordinatex;
                public String coordinatey;
                public String emptyPile;
                public String fillEndTime;
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
}
