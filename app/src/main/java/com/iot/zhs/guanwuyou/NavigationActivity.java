package com.iot.zhs.guanwuyou;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hjm.bottomtabbar.BottomTabBar;
import com.iot.zhs.guanwuyou.fragment.DeviceFragment;
import com.iot.zhs.guanwuyou.fragment.PileMapFragment;
import com.iot.zhs.guanwuyou.fragment.StatisticsFragment;
import com.iot.zhs.guanwuyou.view.NotificationDialog;

/**
 * Created by H151136 on 1/21/2018.
 */

public class NavigationActivity extends BaseActivity {
    private static final String TAG = "ZHS.IOT";
    private BottomTabBar mBottomTabBar;
    private NotificationDialog mNotificationDialog;

    private ISerialPort mSerialManager;

    @Override
    public void onBackPressed() {
        mNotificationDialog.setMessage("是否确认退出?");
        mNotificationDialog.show(getSupportFragmentManager(), "Notification");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        mBottomTabBar = findViewById(R.id.bottom_tab_bar);
        mBottomTabBar.init(getSupportFragmentManager())
                .setImgSize(40, 40)
                .setFontSize(24)
                .addTabItem("桩位图", R.mipmap.ic_tab_pile, PileMapFragment.class)
                .addTabItem("统计图", R.mipmap.ic_tab_statistics, StatisticsFragment.class)
                .addTabItem("设备", R.mipmap.ic_tab_device, DeviceFragment.class)
//                .addTabItem("退出", R.mipmap.ic_tab_exit, ExitFragment.class)
                .setTabBarBackgroundColor(Color.parseColor("#fcfafa"))
                .setDividerHeight(1)
                .setDividerColor(Color.parseColor("#e5e5e5"))
                .setTabPadding(8, 0, 8)
                .isShowDivider(true);

        mNotificationDialog = new NotificationDialog();
        mNotificationDialog.init("提醒",
                "是",
                "否",
                new NotificationDialog.NotificationDialogListener() {
                    @Override
                    public void onButtonClick(int id) {
                        //响应左边的button
                        if (id == 1) {
                            NavigationActivity.this.finish();
                        } else if(id == 2) {
                            mNotificationDialog.dismiss();
                        }
                    }
                });

        Intent intent = new Intent("com.iot.zhs.guanwuyou.service.SerialService");
        intent.setPackage("com.iot.zhs.guanwuyou");
        boolean bound = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bound = " + bound);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mSerialManager = ISerialPort.Stub.asInterface(iBinder);
            try {
                mSerialManager.setPowerUp();
                mSerialManager.sendApkVersion();
                mSerialManager.matchList();
                mSerialManager.requestCalMac();
                mSerialManager.requestMode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSerialManager = null;
        }
    };

}
