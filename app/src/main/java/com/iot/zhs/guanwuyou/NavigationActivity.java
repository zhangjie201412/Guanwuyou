package com.iot.zhs.guanwuyou;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.hjm.bottomtabbar.BottomTabBar;
import com.iot.zhs.guanwuyou.adapter.DeviceListAdapter;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.fragment.DeviceFragment;
import com.iot.zhs.guanwuyou.fragment.ExitFragment;
import com.iot.zhs.guanwuyou.fragment.PileMapFragment;
import com.iot.zhs.guanwuyou.fragment.StatisticsFragment;
import com.iot.zhs.guanwuyou.service.FloatingViewService;
import com.iot.zhs.guanwuyou.view.NotificationDialog;

import org.litepal.crud.DataSupport;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * Created by H151136 on 1/21/2018.
 */

public class NavigationActivity extends BaseActivity {
    private static final String TAG = "ZHS.IOT";
    private BottomTabBar mBottomTabBar;
    private ISerialPort mSerialManager;
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

        /*
        mMenuImageView = findViewById(R.id.iv_menu);
        mMenuImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "menu click!!");

                List<SlaveDevice> slaveDeviceList = DataSupport.findAll(SlaveDevice.class);
                SlaveDevice masterDevice = new SlaveDevice();
                masterDevice.setSerialNumber(MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn());
                masterDevice.setSlaveOrMaster(0);
                masterDevice.setOnline(1);
                masterDevice.setAlarm(0);
                masterDevice.setComm(1);
                masterDevice.setBattery(MyApplication.getInstance().getSpUtils().getKeyMasterBattery());

                slaveDeviceList.add(0, masterDevice);
                for(SlaveDevice device: slaveDeviceList) {
                    Log.d(TAG, "### serialNumber: " + device.getSerialNumber());
                    Log.d(TAG, "### online: " + device.getOnline());
                    Log.d(TAG, "### alarm: " + device.getAlarm());
                    Log.d(TAG, "### comm: " + device.getComm());
                    Log.d(TAG, "### battery: " + device.getBattery());
                    Log.d(TAG, "### ----------------------------------");
                }

                View popupView = NavigationActivity.this.getLayoutInflater().inflate(R.layout.dialog_device_list, null);

                ListView listView = popupView.findViewById(R.id.lv_popup);
                DeviceListAdapter adapter = new DeviceListAdapter(NavigationActivity.this, slaveDeviceList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                PopupWindow window = new PopupWindow(popupView, 600, 450);
                window.setFocusable(true);
                window.setBackgroundDrawable(NavigationActivity.this.getResources().getDrawable(R.mipmap.bg_popupwindow));
                window.setOutsideTouchable(true);
                window.update();
                window.showAsDropDown(mMenuImageView, -20, 10);
            }
        });
        */

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
