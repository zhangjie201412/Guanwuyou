package com.iot.zhs.guanwuyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
import com.iot.zhs.guanwuyou.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 关机
 * Created by H151136 on 11/24/2016.
 */

public class ShutdownBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ZHS.IOT";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "关机 ACTION: " + intent.getAction());

        Log.d(TAG, "setPowerDown");
        List<String> data = new ArrayList<>();
        data.add("0");
        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                "1", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                "0", "power", "none", 1, data);

        Log.d(TAG, "-> " + pkg.toString());
        Utils.doProcessProtocolInfo(
                pkg, new Utils.ResponseCallback() {
                    @Override
                    public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                    }
                });

    }
}
