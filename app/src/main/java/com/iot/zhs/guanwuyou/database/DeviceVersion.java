package com.iot.zhs.guanwuyou.database;

import org.litepal.crud.DataSupport;

public class DeviceVersion extends DataSupport {
    public String serialNumber;
    public String version;
    public String localURL;//本地存储地址

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLocalURL() {
        return localURL;
    }

    public void setLocalURL(String localURL) {
        this.localURL = localURL;
    }
}
