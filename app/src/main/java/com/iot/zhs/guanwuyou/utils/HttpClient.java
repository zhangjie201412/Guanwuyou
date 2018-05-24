package com.iot.zhs.guanwuyou.utils;

import android.util.Log;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;

/**
 * Created by star on 2018/5/21.
 */


/**
 * 服务器没有反应，重发3次
 * 服务器返回reject，需要syncid+1后重发
 */
public class HttpClient {

    private String protocolStr;
    private ProtocolPackage pkg;
    private Utils.ResponseCallback responseCallback;
    private static final String TAG = "ZSH.IOT";

    public HttpClient(ProtocolPackage pkg, Utils.ResponseCallback responseCallback) {
        this.pkg = pkg;
        this.protocolStr = pkg.toString();
        this.responseCallback = responseCallback;
    }

    public void doSendProtocolInfo() {
        int count=0;
        http(count);
    }

    private void http( int count ){
        count++;
        Log.d(TAG, pkg.getmType() + "发送给服务器的次数：" + count);
        Log.d(TAG, pkg.getmType() + "发送给服务器的协议：" + protocolStr);

        String url = Utils.SERVER_ADDR + "/protocol/doProcessProtocolInfo/cc/";
        SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());

        final int finalCount = count;

        OkHttpUtils.post().url(url)
                .addParams("protocolStr", protocolStr)
                .addParams("timeStamp", date)
                .build()
                .execute(
                        new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.d(TAG, "count="+ finalCount +","+pkg.getmType()+"---"+e.getMessage());
                                if (finalCount < 3) {
                                    Log.d(TAG, "count="+ finalCount +","+pkg.getmType()+"---onError 无反应重发");
                                    http(finalCount);
                                }
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                if (Utils.stringIsEmpty(response)) {
                                    Log.d(TAG, "count="+ finalCount +","+pkg.getmType()+"---response为空");
                                    if (finalCount < 3) {
                                        Log.d(TAG, "count="+ finalCount +","+pkg.getmType()+"---onResponse 无反应重发");
                                        http(finalCount);
                                    }
                                } else {
                                    Log.d(TAG, "count="+ finalCount +","+pkg.getmType()+"---server onResponse 服务器返回数据：" + response);

                                    Gson gson = new Gson();
                                    ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);

                                    ProtocolPackage pkgResponse = new ProtocolPackage(info.data.protocol);
                                    if (pkgResponse.parse()) {
                                        //重发数据
                                        if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                            int syncid = pkgResponse.getSyncId();
                                            MyApplication.getInstance().setSyncId(syncid);
                                            Log.d(TAG, "count="+ finalCount +","+pkg.getmType()+"---reject 无反应重发");

                                            pkg.setmSyncId(MyApplication.getInstance().getSyncId());
                                            protocolStr=pkg.toString();
                                            http(finalCount);
                                        }
                                    }

                                    if (info.code.equals("1")) {//成功
                                        if (!Utils.stringIsEmpty(pkgResponse.getmType())) {
                                            Log.d(TAG, pkgResponse.getmType() + " ack ok!");
                                        }
                                        if (responseCallback != null) {
                                            responseCallback.onResponse(response, info, pkgResponse);
                                        }
                                    }
                                }
                            }
                        }
                );
    }

}
