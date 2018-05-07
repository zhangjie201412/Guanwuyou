package com.iot.zhs.guanwuyou;

import android.app.Application;

import com.iot.zhs.guanwuyou.comm.http.EndPourInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import org.litepal.LitePal;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by H151136 on 1/15/2018.
 */

public class MyApplication extends Application {
    private static MyApplication mApplication;
    public static final String SP_FILE_NAME = "IOT_SETTINGS_SP";
    public SharedPreferenceUtils mSpUtils;
    private int mSyncId;
    private EndPourInfo mEndPourInfo;

    public synchronized static MyApplication getInstance() {
        return mApplication;
    }

    public synchronized void setEndPourInfo(EndPourInfo info) {
        mEndPourInfo = info;
    }

    public synchronized EndPourInfo getEndpourInfo() {
        return mEndPourInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        mSpUtils = new SharedPreferenceUtils(this, SP_FILE_NAME);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("ZHS.IOT"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
        LitePal.initialize(this);
    }

    public synchronized SharedPreferenceUtils getSpUtils() {
        if(mSpUtils == null) {
            mSpUtils = new SharedPreferenceUtils(this, SP_FILE_NAME);
        }
        return mSpUtils;
    }

    public synchronized int getSyncId() {
        ++mSyncId;
        if(mSyncId >= 65535)
            mSyncId = 0;
       return mSyncId;
    }

    public synchronized void setSyncId(int id) {
        mSyncId = id;
    }

}
