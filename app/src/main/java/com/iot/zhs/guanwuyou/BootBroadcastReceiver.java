package com.iot.zhs.guanwuyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

import com.iot.zhs.guanwuyou.database.PileCalValue;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.service.NetworkMonitorService;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Created by H151136 on 11/24/2016.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ZHS.IOT";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ACTION: " + intent.getAction());
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appIntent);
        //开机默认主从机在线状态为离线,是否异常为正常,待收到usenodesta时及时更新状态
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

        Intent service = new Intent(context, NetworkMonitorService.class);
        context.startService(service);

        Log.d(TAG, "开机更新成功");
    }
}
