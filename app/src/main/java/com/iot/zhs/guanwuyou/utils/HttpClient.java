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
    private Utils.ResponseCallback responseCallback;
    private static final String TAG = "ZSH.IOT";

    private int count = 0;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (event.type == MessageEvent.EVENT_TYPE_RE_SEND_PROTOCOL) {
            doSendProtocolInfo();
        }
    }

    public HttpClient(String protocolStr, Utils.ResponseCallback responseCallback) {
        this.protocolStr = protocolStr;
        this.responseCallback = responseCallback;
        count = 0;
        EventBus.getDefault().register(this);
    }


    public void doSendProtocolInfo() {
        count++;
        String url = Utils.SERVER_ADDR + "/protocol/doProcessProtocolInfo/cc/";
        SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());

        OkHttpUtils.post().url(url)
                .addParams("protocolStr", protocolStr)
                .addParams("timeStamp", date)
                .build()
                .execute(
                        new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                if (count < 3) {
                                    MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_RE_SEND_PROTOCOL);
                                    EventBus.getDefault().post(event);
                                }
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                if (Utils.stringIsEmpty(response)) {
                                    if (count < 3) {
                                        MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_RE_SEND_PROTOCOL);
                                        EventBus.getDefault().post(event);
                                    }
                                } else {
                                    Log.d(TAG, "server onResponse 服务器返回数据：" + response);

                                    Gson gson = new Gson();
                                    ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);

                                    if (info.code.equals("1")) {//成功
                                        ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                        if (pkgResponse.parse()) {
                                            //重发数据
                                            if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                                int syncid = pkgResponse.getSyncId() + 1;
                                                MyApplication.getInstance().setSyncId(syncid);

                                                MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_RE_SEND_PROTOCOL);
                                                EventBus.getDefault().post(event);

                                            } else {
                                                if (!Utils.stringIsEmpty(pkgResponse.getmType())) {
                                                    Log.d(TAG, pkgResponse.getmType() + " ack ok!");
                                                }
                                                if (responseCallback != null) {
                                                    responseCallback.onResponse(response, info, pkgResponse);
                                                }
                                            }
                                        }
                                    } else {

                                    }
                                }
                            }
                        }
                );
    }
}
