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

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.NavigationActivity;
import com.iot.zhs.guanwuyou.PileListActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.adapter.DeviceAdapter;
import com.iot.zhs.guanwuyou.comm.http.DeviceModel;
import com.iot.zhs.guanwuyou.database.DeviceVersion;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.item.DeviceItem;
import com.iot.zhs.guanwuyou.service.DownLoadService;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 1/21/2018.
 */

public class DeviceFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate,DeviceAdapter.OnItemClickListener {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private MyApplication myApplication;
    private SharedPreferenceUtils mSpUtils;
    private BGARefreshLayout bgaRefreshLayout;


    private TextView projectNameTv;
    private RecyclerView mRecyclerView;
    private DeviceAdapter mAdapter;
    private com.iot.zhs.guanwuyou.comm.http.DeviceModel info;
    private List<DeviceVersion> deviceVersionList=new ArrayList<>();//数据库
    private NotificationDialog mNotificationDialog;

    private ImageView loginOutIv;

    public static  DeviceFragment deviceFragment;
    public static  DeviceFragment getIntance(){
        return  deviceFragment;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        deviceFragment=this;
        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();

        projectNameTv=view.findViewById(R.id.tv_project_title);
        projectNameTv.setText(mSpUtils.getKeyLoginProjectName());

        mRecyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        bgaRefreshLayout = view.findViewById(R.id.refreshLayout);
        bgaRefreshLayout.setDelegate(this);
        bgaRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(this.getContext(), false));

        deviceVersionList = DataSupport.findAll(DeviceVersion.class);

        loginOutIv=view.findViewById(R.id.login_out_iv);
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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG,"--onStart--");
    }

    public void doQuery(){
        doSelectSlaveDeviceInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setListAdapter(){
        if(mAdapter==null){
            mAdapter = new DeviceAdapter(getContext());
            mAdapter.setSlaveDevices(info.data.slaveDevices);
            mAdapter.setMasterDevice(info.data.masterDevice);
            mAdapter.setDeviceVersionList(deviceVersionList);
            mAdapter.setOnItemClickListener(this);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);
        }else{
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
    public void onItemClick(int position,DeviceModel.Data.MasterDevice masterDevice, DeviceModel.Data.SlaveDevice slaveDevice) {
        String ver;
        if(position==0){//主机
            if(masterDevice.isUpdate.equals("0")) {
                for(DeviceVersion deviceVersion:deviceVersionList){
                    if(masterDevice.masterDeviceSN.equals(deviceVersion.getSerialNumber())){
                        showTips(0,"[主机]有新版本"+deviceVersion.getVersion()+"可用，是否安装更新?",deviceVersion);
                        break;
                    }
                }
            }

        }else{//从机
            if(slaveDevice.isUpdate.equals("0")){
                for(DeviceVersion deviceVersion:deviceVersionList){
                    if(slaveDevice.slaveDeviceSN.equals(deviceVersion.getSerialNumber())){
                        if(slaveDevice.deviceType==1){
                            showTips(1,"[标定仪]有新版本"+deviceVersion.getVersion()+"可用，是否安装更新?",deviceVersion);
                        }else{
                            showTips(2,"[从机]有新版本"+deviceVersion.getVersion()+"可用，是否安装更新?",deviceVersion);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void showTips(int flag, String message, final DeviceVersion deviceVersion){
        mNotificationDialog = new NotificationDialog();
        mNotificationDialog.init("提醒",
                "是",
                "否",
                new NotificationDialog.NotificationDialogListener() {
                    @Override
                    public void onButtonClick(int id) {
                        //响应左边的button
                        if (id == 1) {
                            /*String rsp = makeResponse("matchlist", dataList);
                            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
                            event.message = rsp;
                            EventBus.getDefault().post(event);*/

                          //  DataSupport.deleteAll(DeviceVersion.class, "serialNumber = ?", deviceVersion.getSerialNumber());

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
            if(info.code.equals(Utils.MSG_CODE_OK)) {
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
}
