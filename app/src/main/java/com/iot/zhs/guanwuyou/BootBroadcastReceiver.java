package com.iot.zhs.guanwuyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iot.zhs.guanwuyou.service.NetworkMonitorService;
import com.iot.zhs.guanwuyou.utils.SlaveUtils;

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

        SlaveUtils.resetSlave();

        Intent service = new Intent(context, NetworkMonitorService.class);
        context.startService(service);

        Log.d(TAG, "开机更新成功");
    }
}
