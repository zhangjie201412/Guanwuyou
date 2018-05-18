package com.iot.zhs.guanwuyou.comm.http;

import java.util.List;

/**
 * Created by H151136 on 1/16/2018.
 */

public class PileMapInfo {
    public NoFinishPileNumber noFinishPile;
    public CoRange coRange;
    public List<PileMap> pileMap;
    public SearchValue searchValue;

    public static class NoFinishPileNumber {
        public String reportState;
        public String pileNumber;
    }

    public static class CoRange {
        public String maxCoordinatex;
        public String maxCoordinatey;
        public String minCoordinatex;
        public String minCoordinatey;
        public String projectId;
        public String avgPileDiameter;

    }

    public static class PileMap {
        public String constructionState;
        public String coordinatex;
        public String coordinatey;
        public String pileId;
        public String pileNumber;
        public String projectId;
        public String state;
        public String systemNumber;
    }

    public static class SearchValue {
        public String constructionState;
        public String coordinatex;
        public String coordinatey;
        public String pileId;
        public String pileNumber;
        public String projectId;
        public String systemNumber;
    }
}
