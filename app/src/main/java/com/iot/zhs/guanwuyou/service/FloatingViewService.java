package com.iot.zhs.guanwuyou.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
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
    private ImageView mFloatingImageView;
    private WindowManager.LayoutParams mWindowManagerParams;
    private LayoutInflater mInflater;
    private WindowManager mWindowManager;
    private boolean mIsShowing = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                startService(new Intent(FloatingViewService.this, PopupWindowService.class));
            }
        });
    }
}
