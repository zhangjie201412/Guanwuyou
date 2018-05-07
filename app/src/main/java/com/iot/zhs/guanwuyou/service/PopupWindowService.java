package com.iot.zhs.guanwuyou.service;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.NavigationActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.adapter.DeviceListAdapter;
import com.iot.zhs.guanwuyou.database.SlaveDevice;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by H151136 on 5/6/2018.
 */

public class PopupWindowService extends Service {
    private static final String TAG = "ZHS#Float";
//    private Dialog mDialog;
    private PopupWindow mPopupWindow;
    private WindowManager.LayoutParams mWindowManagerParams;
    private LayoutInflater mInflater;
    private WindowManager mWindowManager;
    private LinearLayout mLayout;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    private void initWindow() {
//        mDialog = new Dialog(PopupWindowService.this);
//        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        mInflater = LayoutInflater.from(getApplication());

        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        mWindowManagerParams = getParams(mWindowManagerParams);//设置好悬浮窗的参数
        // 悬浮窗默认显示以左上角为起始坐标
        mWindowManagerParams.gravity = Gravity.LEFT| Gravity.TOP;
        //悬浮窗的开始位置，因为设置的是从左上角开始，所以屏幕左上角是x=0;y=0
        mWindowManagerParams.x = 40;
        mWindowManagerParams.y = 150;
        mWindowManagerParams.width = 600;
        mWindowManagerParams.height = 450;
        //得到容器，通过这个inflater来获得悬浮窗控件
        mInflater = LayoutInflater.from(getApplication());
        // 获取浮动窗口视图所在布局
        mLayout = (LinearLayout) mInflater.inflate(R.layout.dialog_device_list, null);

        List<SlaveDevice> slaveDeviceList = DataSupport.findAll(SlaveDevice.class);
        SlaveDevice masterDevice = new SlaveDevice();
        masterDevice.setSerialNumber(MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn());
        masterDevice.setSlaveOrMaster(0);
        masterDevice.setOnline(1);
        masterDevice.setAlarm(0);
        masterDevice.setComm(1);
        masterDevice.setBattery(MyApplication.getInstance().getSpUtils().getKeyMasterBattery());

        slaveDeviceList.add(0, masterDevice);
        for (SlaveDevice device : slaveDeviceList) {
            Log.d(TAG, "### serialNumber: " + device.getSerialNumber());
            Log.d(TAG, "### online: " + device.getOnline());
            Log.d(TAG, "### alarm: " + device.getAlarm());
            Log.d(TAG, "### comm: " + device.getComm());
            Log.d(TAG, "### battery: " + device.getBattery());
            Log.d(TAG, "### ----------------------------------");
        }

        ListView listView = mLayout.findViewById(R.id.lv_popup);
        DeviceListAdapter adapter = new DeviceListAdapter(PopupWindowService.this, slaveDeviceList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        mWindowManager.addView(mLayout, mWindowManagerParams);
    }


    public WindowManager.LayoutParams getParams(WindowManager.LayoutParams wmParams){
        wmParams = new WindowManager.LayoutParams();
        //设置window type 下面变量2002是在屏幕区域显示，2003则可以显示在状态栏之上
        //wmParams.type = LayoutParams.TYPE_PHONE;
        //wmParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        //wmParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        //设置可以显示在状态栏上
        wmParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN| WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR|
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        return wmParams;
    }
}
