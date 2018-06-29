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
import android.view.MotionEvent;
import android.view.View;

import com.hjm.bottomtabbar.BottomTabBar;
import com.iot.zhs.guanwuyou.fragment.DeviceFragment;
import com.iot.zhs.guanwuyou.fragment.FirstFragment;
import com.iot.zhs.guanwuyou.fragment.PileMapFragment;
import com.iot.zhs.guanwuyou.fragment.SecondFragment;
import com.iot.zhs.guanwuyou.fragment.StatisticsFragment;
import com.iot.zhs.guanwuyou.view.NotificationDialog;

/**
 * Created by H151136 on 1/21/2018.
 */

public class NavigationActivity extends BaseActivity {
    private static final String TAG = "ZHS.IOT";
    private BottomTabBar mBottomTabBar;
    private NotificationDialog mNotificationDialog;


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

                .addTabItem("桩位图", R.mipmap.ic_tab_pile, PileMapFragment.class)
                .addTabItem("统计图", R.mipmap.ic_tab_statistics, StatisticsFragment.class)
                .addTabItem("设备", R.mipmap.ic_tab_device, DeviceFragment.class)
                .setOnTabChangeListener(new BottomTabBar.OnTabChangeListener() {
                    @Override
                    public void onTabChange(int position, String name, View view) {
                        if (position == 0) {
                            if (PileMapFragment.getIntance() != null) {
                                PileMapFragment.getIntance().doQuery();
                            }
                        }
                        if (position == 1) {
                            if(FirstFragment.getIntance()!=null){
                                FirstFragment.getIntance().doQuery();
                            }
                            if(SecondFragment.getIntance()!=null){
                                SecondFragment.getIntance().doQuery();
                            }
                        }
                        if (position == 2) {
                            if (DeviceFragment.getIntance() != null) {
                                DeviceFragment.getIntance().doQuery();
                            }
                        }
                    }
                });

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
                        } else if (id == 2) {
                            mNotificationDialog.dismiss();
                        }
                    }
                });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
