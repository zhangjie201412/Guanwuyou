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
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by H151136 on 5/17/2018.
 */

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "ZHS#Base";
    private Button mMenuButton;

 /*   @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        //友盟错误统计
        MobclickAgent.setDebugMode(true);
        // SDK在统计Fragment时，需要关闭Activity自带的页面统计，
        // 然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
        MobclickAgent.openActivityDurationTrack(false);

        // 设置为U-APP场景
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);

        ViewGroup viewGroup = (ViewGroup)getWindow().getDecorView();
        LinearLayout menuLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mMenuButton = new Button(this);
        mMenuButton.setBackgroundResource(R.mipmap.ic_menu);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<SlaveDevice> slaveDeviceList = new ArrayList<>();
                List<SlaveDevice> savedDeviceList = DataSupport.findAll(SlaveDevice.class);
                //主机
                SlaveDevice masterDevice = new SlaveDevice();
                masterDevice.setSerialNumber(MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn());
                masterDevice.setSlaveOrMaster("0");
                masterDevice.setOnline("1");
                masterDevice.setAlarm("0");
                masterDevice.setComm("1");
                masterDevice.setBattery(MyApplication.getInstance().getSpUtils().getKeyMasterBattery()+"");
                slaveDeviceList.add(0, masterDevice);
                //从机
                String[] serialNumberList = MyApplication.getInstance().getSpUtils().getKeyMatchList();
                for(String sn: serialNumberList) {
                    Log.d(TAG, "### saved serial list " + sn);
                    boolean isExist = false;
                    int i;
                    for(i = 0; i < savedDeviceList.size(); i++) {
                        if(sn.equals(savedDeviceList.get(i).getSerialNumber())) {
                            isExist = true;
                            break;
                        }
                    }
                    if(isExist) {
                        slaveDeviceList.add(savedDeviceList.get(i));
                    } else {
                        SlaveDevice lostDevice = new SlaveDevice();
                        lostDevice.setSerialNumber(sn);
                        lostDevice.setSlaveOrMaster("1");
                        lostDevice.setOnline("0");
                        lostDevice.setAlarm("0");
                        lostDevice.setComm("0");
                        lostDevice.setBattery("0");
                        lostDevice.setVersionStatus("0");
                        lostDevice.setSensorStatus("0");
                        lostDevice.setMotorStatus("0");
                        slaveDeviceList.add(lostDevice);
                    }
                }
                //标定仪
                int j=0;
                for(j = 0; j < savedDeviceList.size(); j++) {
                    String slaveSn=savedDeviceList.get(j).getSerialNumber();
                    if(!MyApplication.getInstance().getSpUtils().getKeyCalMac().equals("")){
                        if(slaveSn.equals(MyApplication.getInstance().getSpUtils().getKeyCalMac())){//区分标定仪
                            savedDeviceList.get(j).setSlaveOrMaster("2");
                            slaveDeviceList.add(savedDeviceList.get(j));
                            break;
                        }
                    }
                }

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

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
