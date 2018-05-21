package com.iot.zhs.guanwuyou.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.PileListActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.adapter.DeviceAdapter;
import com.iot.zhs.guanwuyou.comm.http.DeviceModel;
import com.iot.zhs.guanwuyou.item.DeviceItem;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 1/21/2018.
 */

public class DeviceFragment extends Fragment implements BGARefreshLayout.BGARefreshLayoutDelegate {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private MyApplication myApplication;
    private SharedPreferenceUtils mSpUtils;
    private BGARefreshLayout bgaRefreshLayout;


    private TextView projectNameTv;
    private RecyclerView mRecyclerView;
    private DeviceAdapter mAdapter;
    private com.iot.zhs.guanwuyou.comm.http.DeviceModel info;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
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

        doSelectSlaveDeviceInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginiMasterDeviceSn());

        return view;
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
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {

        doSelectSlaveDeviceInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
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
