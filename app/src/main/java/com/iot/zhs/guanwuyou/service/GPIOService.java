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

import com.iot.jnitest.JNITest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by H151136 on 6/20/2018.
 */

public class GPIOService extends Service {
    private static final String TAG = "ZSH.IOT";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "+onCreate+");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Log.d(TAG, "gpio");

                    JNITest jniTest=new JNITest();
                    int powerOnPin=jniTest.getPowerOnPinValue();//检测到关机
                    if(powerOnPin==0){
                        jniTest.setCpuAndLoraValue(0,0);
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



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
