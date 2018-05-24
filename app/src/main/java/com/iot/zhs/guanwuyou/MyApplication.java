package com.iot.zhs.guanwuyou;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.iot.zhs.guanwuyou.comm.http.EndPourInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
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
    private AppCompatActivity app_activity = null;

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
        initGlobeActivity();

        /**
         * 初始化common库
         * 参数1:上下文，不能为空
         * 参数2:【友盟+】 AppKey
         * 参数3:【友盟+】 Channel
         * 参数4:设备类型，UMConfigure.DEVICE_TYPE_PHONE为手机、UMConfigure.DEVICE_TYPE_BOX为盒子，默认为手机
         * 参数5:Push推送业务的secret
         */
        UMConfigure.init(this, "5b065deba40fa37e470000f8", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
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


    private void initGlobeActivity() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }

            /** Unused implementation **/
            @Override
            public void onActivityStarted(Activity activity) {
                app_activity = (AppCompatActivity)activity;
                Log.d("onActivityStarted===", app_activity + "");
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }
        });
    }

    /**
     * 公开方法，外部可通过 MyApplication.getInstance().getCurrentActivity() 获取到当前最上层的activity
     */
    public AppCompatActivity getCurrentActivity() {
        return app_activity;
    }

}
