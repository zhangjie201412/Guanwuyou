package com.iot.zhs.guanwuyou.comm.http;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by star on 2018/5/14.
 */

public class DeviceModel {
    public String clientType;
    public String code;
    public String message;
    public String token;
    public Data data;

    public static class Data {
        public MasterDevice  masterDevice;

        public List<SlaveDevice> slaveDevices=new ArrayList<>();

        public static class MasterDevice {
            public String masterDeviceId;//masterDeviceId设备id
            public String masterDeviceSN;//主机设备sn
            public String lastRunTime;//最后使用时间
            public String projectId;//项目id
            public String errCode;//错误代码
            public String projectName;//项目名称
            public String state;//设备状态
            public String companyId;//公司id
            public String projectState;//项目状态
            public String elcMany="0";//电量
            public String deviceVer;//版本号
            public String isUpdate;
            public String channel;//信道值
        }


        public static class SlaveDevice {
            public String id;//从机Id
            public String slaveDeviceId;//从机Id
            public String slaveDeviceSN;//从机的sn编号
            public String lastRunTime;//最后使用时间
            public String elcMany="0";//电量
            public String errCode;//从机的错误代码
            public String sensorSN;//传感器编号
            public String runTimes;//传感器的使用次数
            public String state;//状态
            public String masterDeviceSN;//主机设备SN
            public int deviceType;//从机设备类型 0--丛机 1--标定仪
            public String remark;//备注
            public String netWorkTime;//加入组网时间
            public String deviceVer;//版本号
            public String isUpdate;
        }
    }

}
