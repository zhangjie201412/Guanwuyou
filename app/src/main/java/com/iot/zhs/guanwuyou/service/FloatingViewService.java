package com.iot.zhs.guanwuyou.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

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

public class FloatingViewService extends Service {
    private static final String TAG = "ZHS#Float";
    private LinearLayout mLayout;
    private LinearLayout mPopLayout;

    private ImageView mFloatingImageView;
    private WindowManager.LayoutParams mWindowManagerParams;
    private WindowManager.LayoutParams mPopWindowManagerParams;

    private LayoutInflater mInflater;
    private WindowManager mWindowManager;

    private boolean isShow=false;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initFloating();
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public FloatingViewService getFloatingViewService(){
            return FloatingViewService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initFloating();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initWindow() {
        mWindowManager = (WindowManager)getApplication().getSystemService(Context.WINDOW_SERVICE);
        mWindowManagerParams =getParams(mWindowManagerParams);
        mWindowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWindowManagerParams.x = 50;
        mWindowManagerParams.y = 600;
        mInflater = LayoutInflater.from(getApplication());
        mLayout = (LinearLayout)mInflater.inflate(R.layout.floating_layout, null);
        mWindowManager.addView(mLayout, mWindowManagerParams);
    }

    public WindowManager.LayoutParams getParams(WindowManager.LayoutParams params) {
        params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        return params;
    }

    private void initFloating() {
        mFloatingImageView = (ImageButton)mLayout.findViewById(R.id.floating_imageView);
        mFloatingImageView.getBackground().setAlpha(0);
        mFloatingImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShow){
                    if(mPopLayout!=null) {
                        mWindowManager.removeViewImmediate(mPopLayout);
                    }
                }else{
                    initPopupwindow();
                }
                isShow=!isShow;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mWindowManager!=null){
            mWindowManager.removeViewImmediate(mLayout);
        }
    }

    private void initPopupwindow(){
        mPopWindowManagerParams = getParams(mPopWindowManagerParams);//设置好悬浮窗的参数
        // 悬浮窗默认显示以左上角为起始坐标
        mPopWindowManagerParams.gravity = Gravity.LEFT| Gravity.TOP;
        //悬浮窗的开始位置，因为设置的是从左上角开始，所以屏幕左上角是x=0;y=0
        mPopWindowManagerParams.x = 40;
        mPopWindowManagerParams.y = 150;
        mPopWindowManagerParams.width = 600;
        mPopWindowManagerParams.height = 450;
        //得到容器，通过这个inflater来获得悬浮窗控件
        mInflater = LayoutInflater.from(getApplication());
        // 获取浮动窗口视图所在布局
        mPopLayout = (LinearLayout) mInflater.inflate(R.layout.dialog_device_list, null);

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

        ListView listView = mPopLayout.findViewById(R.id.lv_popup);
        DeviceListAdapter adapter = new DeviceListAdapter(FloatingViewService.this, slaveDeviceList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        mWindowManager.addView(mPopLayout, mPopWindowManagerParams);
    }

    public void  dismissPop(){
        if(isShow){
            if(mPopLayout!=null) {
                mWindowManager.removeViewImmediate(mPopLayout);
            }
        }
    }





}
