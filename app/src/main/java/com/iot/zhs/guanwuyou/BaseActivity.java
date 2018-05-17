package com.iot.zhs.guanwuyou;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.iot.zhs.guanwuyou.adapter.DeviceListAdapter;
import com.iot.zhs.guanwuyou.database.SlaveDevice;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by H151136 on 5/17/2018.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "ZHS#Base";
    private Button mMenuButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ViewGroup viewGroup = (ViewGroup)getWindow().getDecorView();
        LinearLayout menuLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mMenuButton = new Button(this);
        mMenuButton.setBackgroundResource(R.mipmap.ic_menu);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                View popupView = BaseActivity.this.getLayoutInflater().inflate(R.layout.dialog_device_list, null);

                ListView listView = popupView.findViewById(R.id.lv_popup);
                DeviceListAdapter adapter = new DeviceListAdapter(BaseActivity.this, slaveDeviceList);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                PopupWindow window = new PopupWindow(popupView, 600, 450);
                window.setFocusable(true);
                window.setBackgroundDrawable(BaseActivity.this.getResources().getDrawable(R.mipmap.bg_popupwindow));
                window.setOutsideTouchable(true);
                window.update();
                window.showAsDropDown(mMenuButton, -20, 10);
            }
        });
        menuLayout.setLayoutParams(lp);
        menuLayout.setGravity(Gravity.LEFT | Gravity.BOTTOM);
        menuLayout.setPadding(100, 0, 0, 50);
        menuLayout.addView(mMenuButton);
        viewGroup.addView(menuLayout);
    }
}
