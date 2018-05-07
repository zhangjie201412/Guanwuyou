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

import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.adapter.DeviceAdapter;
import com.iot.zhs.guanwuyou.item.DeviceItem;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 1/21/2018.
 */

public class DeviceFragment extends Fragment {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private MyApplication myApplication;
    private SharedPreferenceUtils mSpUtils;

    private RecyclerView mRecyclerView;
    private DeviceAdapter mAdapter;
    private List<DeviceItem> mDeviceList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);

        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();

        mRecyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mDeviceList = new ArrayList<>();
        mAdapter = new DeviceAdapter(mDeviceList, getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

//        hello();
        doSelectSlaveDeviceInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginiMasterDeviceSn());
        return view;
    }

    private void hello() {
        mDeviceList.add(new DeviceItem(DeviceAdapter.DEVICE_TYPE_MASTER, "[主机]123456789", "2017.10.7", 1, 100, 50));
        mDeviceList.add(new DeviceItem(DeviceAdapter.DEVICE_TYPE_SLAVE, "[从机]222222222", "2017.10.22", 1, 100, 80));
        mDeviceList.add(new DeviceItem(DeviceAdapter.DEVICE_TYPE_CALIBRATOR, "[标定仪]333333333", "2017.12.7", 1, 100, 20));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private class SelectSlaveDeviceInfo extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
//            Gson gson = new Gson();
//            com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo info = gson.fromJson(response,
//                    com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo.class);
//            if(info.code.equals(Utils.MSG_CODE_OK)) {
//                setBarData(info.data.pileFinishedList);
//            } else {
//                Toast.makeText(getContext(), info.message, Toast.LENGTH_SHORT).show();
//            }
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
