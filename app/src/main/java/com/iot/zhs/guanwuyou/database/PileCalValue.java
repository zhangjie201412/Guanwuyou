package com.iot.zhs.guanwuyou.database;

import org.litepal.crud.DataSupport;

/**
 * 存储桩的标定值
 */
public class PileCalValue extends DataSupport {
    public String pileId;
    public String calCon="";//砼标定值
    public String calSlurry="";//泥浆标定值

    public String getPileId() {
        return pileId;
    }

    public void setPileId(String pileId) {
        this.pileId = pileId;
    }

    public String getCalCon() {
        return calCon;
    }

    public void setCalCon(String calCon) {
        this.calCon = calCon;
    }

    public String getCalSlurry() {
        return calSlurry;
    }

    public void setCalSlurry(String calSlurry) {
        this.calSlurry = calSlurry;
    }
}
