package com.iot.zhs.guanwuyou.utils;

import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.database.SlaveDevice;

import org.litepal.crud.DataSupport;

import java.util.List;

public class SlaveUtils {

    //重新登录或者开机默认主从机在线状态为离线,是否异常为正常,待收到usenodesta时及时更新状态
    public static void resetSlave() {
        List<SlaveDevice> savedDeviceList = DataSupport.findAll(SlaveDevice.class);
        for (SlaveDevice slaveDevice : savedDeviceList) {
            slaveDevice.setOnline("0");//离线
            slaveDevice.setComm("2");//异常--未获取
            slaveDevice.setVersionStatus("2");
            slaveDevice.setSensorStatus("2");
            slaveDevice.setMotorStatus("2");
            slaveDevice.setSlaveOrMaster("1");
            if (!MyApplication.getInstance().getSpUtils().getKeyCalMac().equals("")) {//区分标定仪
                if (slaveDevice.getSerialNumber().equals(MyApplication.getInstance().getSpUtils().getKeyCalMac())) {
                    slaveDevice.setSlaveOrMaster("2");
                    slaveDevice.setMotorStatus("2");
                }
            }
            slaveDevice.updateAll("serialNumber = ?", slaveDevice.getSerialNumber());
        }
    }
}
