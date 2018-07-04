package com.iot.zhs.guanwuyou.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by H151136 on 6/20/2018.
 */

public class NetworkMonitorService extends Service {
    private static final String TAG = "NetWorkMonitor";
    private ConnectivityManager mConnectivityManager;
    private static final String NETWORK_LED_PATH = "/sys/class/leds/network/brightness";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "+onCreate+");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                       // Thread.sleep(10000);
                        try {
                            mConnectivityManager = (ConnectivityManager) NetworkMonitorService.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo woin = mConnectivityManager.getActiveNetworkInfo();
                            if (woin != null) {
                                //有网络的状态表进行设置有网络的状态
                                Log.d(TAG, "Network OK");
                                setNetworkLedOn();
                            } else {
                                //有网络的状态表进行设置有网络的状态
                                Log.d(TAG, "Network NG");
                                setNetworkLedOff();
                            }
                        } catch (Exception e) {
                            Toast.makeText(NetworkMonitorService.this, "你没有这个权限", Toast.LENGTH_LONG).show();
                        }

                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "+onDestory+");
    }

    private void setNetworkLedOn() {
        File file = new File(NETWORK_LED_PATH);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("255");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setNetworkLedOff() {
        File file = new File(NETWORK_LED_PATH);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("0");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
