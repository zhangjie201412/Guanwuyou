package com.iot.zhs.guanwuyou.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.NavigationActivity;
import com.iot.zhs.guanwuyou.PileListActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.adapter.DeviceAdapter;
import com.iot.zhs.guanwuyou.comm.http.DeviceModel;
import com.iot.zhs.guanwuyou.database.AlarmState;
import com.iot.zhs.guanwuyou.database.DeviceVersion;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.item.DeviceItem;
import com.iot.zhs.guanwuyou.protocol.YmodernPackage;
import com.iot.zhs.guanwuyou.service.DownLoadService;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;
import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 1/21/2018.
 */

public class DeviceFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate, DeviceAdapter.OnItemClickListener {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private MyApplication myApplication;
    private SharedPreferenceUtils mSpUtils;
    private BGARefreshLayout bgaRefreshLayout;


    private String serialSN;//
    private TextView projectNameTv;
    private RecyclerView mRecyclerView;
    private DeviceAdapter mAdapter;
    private com.iot.zhs.guanwuyou.comm.http.DeviceModel info;
    private List<DeviceVersion> deviceVersionList = new ArrayList<>();//数据库
    private NotificationDialog mNotificationDialog;

    private ImageView loginOutIv;
    private WaitProgressDialog mProgressDialog;

    public static DeviceFragment deviceFragment;

    public static DeviceFragment getIntance() {
        return deviceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        deviceFragment = this;
        mToast = Toast.makeText(this.getActivity(), "", Toast.LENGTH_SHORT);

        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();
        mProgressDialog = new WaitProgressDialog(getContext());


        projectNameTv = view.findViewById(R.id.tv_project_title);
        projectNameTv.setText(mSpUtils.getKeyLoginProjectName());

        mRecyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        bgaRefreshLayout = view.findViewById(R.id.refreshLayout);
        bgaRefreshLayout.setDelegate(this);
        bgaRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(this.getContext(), false));

        deviceVersionList = DataSupport.findAll(DeviceVersion.class);

        loginOutIv = view.findViewById(R.id.login_out_iv);
        loginOutIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final NotificationDialog loginOutDialog = new NotificationDialog();
                loginOutDialog.init("提醒",
                        "是",
                        "否",
                        new NotificationDialog.NotificationDialogListener() {
                            @Override
                            public void onButtonClick(int id) {
                                //响应左边的button
                                if (id == 1) {
                                    loginOutDialog.dismiss();
                                    DeviceFragment.this.getActivity().finish();
                                } else if (id == 2) {
                                    loginOutDialog.dismiss();
                                }
                            }
                        });
                loginOutDialog.setMessage("是否确认退出登录?");
                loginOutDialog.show(DeviceFragment.this.getActivity().getSupportFragmentManager(), "Notification");
            }
        });

        doSelectSlaveDeviceInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginiMasterDeviceSn());

        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "--onStart--");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void doQuery() {
        doSelectSlaveDeviceInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setListAdapter() {
        if (mAdapter == null) {
            mAdapter = new DeviceAdapter(getContext());
            mAdapter.setSlaveDevices(info.data.slaveDevices);
            mAdapter.setMasterDevice(info.data.masterDevice);
            mAdapter.setDeviceVersionList(deviceVersionList);
            mAdapter.setOnItemClickListener(this);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setSlaveDevices(info.data.slaveDevices);
            mAdapter.setMasterDevice(info.data.masterDevice);
            mAdapter.setDeviceVersionList(deviceVersionList);
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        deviceVersionList.clear();
        deviceVersionList = DataSupport.findAll(DeviceVersion.class);

        doSelectSlaveDeviceInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    @Override
    public void onItemClick(int position, DeviceModel.Data.MasterDevice masterDevice, DeviceModel.Data.SlaveDevice slaveDevice) {
        String ver;
        if (position == 0) {//主机
            if (masterDevice.isUpdate.equals("0")) {
                for (DeviceVersion deviceVersion : deviceVersionList) {
                    if (masterDevice.masterDeviceSN.equals(deviceVersion.getSerialNumber())) {
                        showTips(0, "[主机]有新版本" + deviceVersion.getVersion() + "可用，是否安装更新?", deviceVersion);

                        break;
                    }
                }
            }

        } else {//从机
            if (slaveDevice.isUpdate.equals("0")) {
                for (DeviceVersion deviceVersion : deviceVersionList) {
                    if (slaveDevice.slaveDeviceSN.equals(deviceVersion.getSerialNumber())) {
                        if (slaveDevice.deviceType == 1) {
                            showTips(1, "[标定仪]有新版本" + deviceVersion.getVersion() + "可用，是否安装更新?", deviceVersion);
                        } else {
                            showTips(2, "[从机]有新版本" + deviceVersion.getVersion() + "可用，是否安装更新?", deviceVersion);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void showTips(final int flag, String message, final DeviceVersion deviceVersion) {
        mNotificationDialog = new NotificationDialog();
        mNotificationDialog.init("提醒",
                "是",
                "否",
                new NotificationDialog.NotificationDialogListener() {
                    @Override
                    public void onButtonClick(int id) {
                        //响应左边的button
                        if (id == 1) {
                            mProgressDialog.show();
                            YmodernPackage ymodernPackage = YmodernPackage.getInstance();

                            if (flag == 0) {//主机
                                ymodernPackage.setUpdateFlag(0);
                                ymodernPackage.setFilePath(deviceVersion.getLocalURL());
                                ymodernPackage.setDeviceSN(deviceVersion.getSerialNumber());
                                MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_UPDATE_WRITE);
                                event.message = "firmware update master\r\n";
                                EventBus.getDefault().post(event);
                            } else {//从机
                                ymodernPackage.setUpdateFlag(1);
                                ymodernPackage.setFilePath(deviceVersion.getLocalURL());
                                ymodernPackage.setDeviceSN(deviceVersion.getSerialNumber());

                                String message="";
                                if(ymodernPackage.isUart()){//切换串口
                                    message="firmware update "+deviceVersion.getSerialNumber()+"\r\n";

                                }else{
                                    message="firmware update slave" +"\r\n";
                                    //message="firmware update "+deviceVersion.getSerialNumber()+"\r\n";

                                }
                                final Timer timer = new Timer();
                                final String finalMessage = message;
                                timer.schedule(new TimerTask() {
                                    int i = 0;

                                    public void run() {
                                        if (i++ == 5) {
                                            timer.cancel();
                                        }
                                        MessageEvent event_uart = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_UPDATE_WRITE);
                                        event_uart.message = finalMessage;
                                        EventBus.getDefault().post(event_uart);
                                    }
                                }, 0,1000);// 设定指定的时间time,此处为2000毫秒

                            }
                            mNotificationDialog.dismiss();
                        } else if (id == 2) {
                            mNotificationDialog.dismiss();
                        }
                    }
                });
        mNotificationDialog.setMessage(message);
        mNotificationDialog.show(DeviceFragment.this.getActivity().getSupportFragmentManager(), "Notification");
    }


    private class SelectSlaveDeviceInfo extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            bgaRefreshLayout.endRefreshing();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            info = gson.fromJson(response,
                    com.iot.zhs.guanwuyou.comm.http.DeviceModel.class);
            if (info.code.equals(Utils.MSG_CODE_OK)) {
                setListAdapter();
            }
            bgaRefreshLayout.endRefreshing();

        }
    }

    private void doSelectSlaveDeviceInfo(String token, String loginName, String masterSn) {
        String url = Utils.SERVER_ADDR + "/device/doSelectSlaveDeviceInfo/cc/" + token + "/" + loginName;

        OkHttpUtils.post().url(url)
                .addParams("masterDeviceSN", masterSn)
                .build()
                .execute(
                        new SelectSlaveDeviceInfo()
                );
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "event: " + event.type + ", message: " + event.message);

        //主机升级成功
        if (event.type == MessageEvent.EVENT_TYPE_MASTER_UPDATE_SUCCESS) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            showToast("主机升级成功");
            //删除数据库对应的SN号数据，同时更新adapter图标去掉
            String deviceSN=event.message;
            Log.d(TAG, "Ym-数据库中的删除的SN: " + deviceSN);
            DataSupport.deleteAll(DeviceVersion.class, "serialNumber = ?", deviceSN);
            deviceVersionList = DataSupport.findAll(DeviceVersion.class);
            //更新界面
            setListAdapter();
        }
        //从机升级成功
        if (event.type == MessageEvent.EVENT_TYPE_SLAVE_UPDATE_SUCCESS) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            showToast("从机升级成功");
            //删除数据库对应的SN号数据，同时更新adapter图标去掉
            String deviceSN=event.message;
            Log.d(TAG, "Ym-数据库中的删除的SN: "+ deviceSN);
            DataSupport.deleteAll(DeviceVersion.class, "serialNumber = ?", deviceSN);
            deviceVersionList = DataSupport.findAll(DeviceVersion.class);

            //更新界面
            setListAdapter();

        }
        //主机升级失败
        if (event.type == MessageEvent.EVENT_TYPE_MASTERL_UPDATE_FAIL) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            showToast("主机升级失败");
        }
        //从机升级失败
        if (event.type == MessageEvent.EVENT_TYPE_SLAVE_UPDATE_FAIL) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            showToast("从机升级失败");
        }
        //从机SN号不匹配
        if(event.type==MessageEvent.EVENT_TYPE_SLAVE_SN_ERR){
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            showToast("从机SN号不匹配");
        }

    }

    private Toast mToast;

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }

}
