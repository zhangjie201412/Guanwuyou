package com.iot.zhs.guanwuyou.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.iot.serialport.SerialPort;
import com.iot.zhs.guanwuyou.ISerialPort;
import com.iot.zhs.guanwuyou.LoginActivity;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
import com.iot.zhs.guanwuyou.protocol.SerialPackage;
import com.iot.zhs.guanwuyou.protocol.YmodernPackage;
import com.iot.zhs.guanwuyou.utils.DowloadFileUtils;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.util.LogUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;

/**
 * Created by H151136 on 1/21/2018.
 */

public class SerialService extends Service {
    private static final String TAG = "ZSH.IOT";
    private InputStream mInput;
    private OutputStream mOutput;
    private SerialPort mSerialPort;
    private String mRecvBuffer;
    private SerialPortMonitorThread mMonitorThread;
    private HeartBeatThread mHeartBeatThread;
    private CalTrsdThread mCalTrsdThread;
    private MtrsdThread mMtrsdThread;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;

    private int gpioCount = 0;

    private ISerialPort.Stub mBinder = new ISerialPort.Stub() {
        @Override
        public void ping(int count) throws RemoteException {
            Log.d(TAG, "Ping: " + count);
        }

        @Override
        public void setPowerUp() throws RemoteException {
            Log.d(TAG, "setPowerUp");
            List<String> data = new ArrayList<>();
            data.add("100");
            ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                    "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                    "0", "power", "none", 1, data);

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        }

        //没有调用
        @Override
        public void setPowerDown() throws RemoteException {
            Log.d(TAG, "setPowerDown");
            List<String> data = new ArrayList<>();
            data.add("0");
            ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                    "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                    "0", "power", "none", 1, data);

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        }

        @Override
        public void requestCalMac() throws RemoteException {
            Log.d(TAG, "calmac");
            List<String> data = new ArrayList<>();
            data.add("1");
            ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                    "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                    "0", "calmac", "get", 1, data);

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        }

        @Override
        public void matchList() throws RemoteException {
            Log.d(TAG, "matchList");
            List<String> data = new ArrayList<>();
            data.add("0");
            ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                    "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                    "0", "matchlist", "get", 1, data);

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        }

        @Override
        public void requestMode() throws RemoteException {
            Log.d(TAG, "request mode");
            List<String> data = new ArrayList<>();
            data.add("1");
            ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                    "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                    "0", "mode", "get", 1, data);

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        }

        /**
         * 发送apk 版本号
         * @throws RemoteException
         */
        @Override
        public void sendApkVersion() throws RemoteException {
            Log.d(TAG, "send apk version");
            List<String> data = new ArrayList<>();
            String versionName = Utils.getVersionName(getApplicationContext());
            String[] dataArray = versionName.split("\\.");
            data = Arrays.asList(dataArray);
            final ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                    "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                    "0", "ver", "get", data.size(), data);

            Log.d(TAG, "-> " + pkg.toString());
            final List<String> finalData = data;
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {
                            pkgResponse.setUpdateVersionData(finalData, 0, processProtocolInfo.data.downloadUrl);
                        }
                    });
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
        mRecvBuffer = "";
        try {
            mSerialPort = new SerialPort(new File("/dev/ttymxc2"), 115200);
            mInput = mSerialPort.getInputStream();
            mOutput = mSerialPort.getOutputStream();
            Log.d(TAG, "open serial port done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMonitorThread = new SerialPortMonitorThread();
        mMonitorThread.setStart();
        mMonitorThread.start();

        mCalTrsdThread = new CalTrsdThread();
        mCalTrsdThread.start();
        mMtrsdThread = new MtrsdThread();
        mMtrsdThread.start();

        mHeartBeatThread = new HeartBeatThread();
        mHeartBeatThread.start();

       /* GPIOThread gpioThread = new GPIOThread();
        gpioThread.start();*/

        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSerialPort.close();
        mMonitorThread.setStop();
        try {
            mMonitorThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "event: " + event.type + ", message: " + event.message);
        if (event.type == MessageEvent.EVENT_TYPE_SERIAL_WRITE) {
            try {
                Log.d(TAG, "中控发送给主机的协议：" + event.message);
                mOutput.write(event.message.getBytes());
            } catch (IOException e) {
                Log.d(TAG, "中控发送给主机error:" + e.getMessage());
                e.printStackTrace();
            }
        } else if (event.type == MessageEvent.EVENT_TYPE_ERROR_UART) {
            List<String> data = new ArrayList<>();
            data.add(event.message);
            ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                    "0", mSpUtils.getKeyLoginiMasterDeviceSn(),
                    "0", "err", "none", 1, data);

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void process() {
        int endIndex = mRecvBuffer.indexOf("\n");
        if (endIndex > 0) {
            String cutString = mRecvBuffer.substring(0, endIndex);
            mRecvBuffer = mRecvBuffer.substring(endIndex + 1);
            SerialPackage pkg = new SerialPackage(cutString);
            pkg.parse();
        }
    }

    private class SerialPortMonitorThread extends Thread {

        private boolean start;

        public void setStart() {
            this.start = true;
        }

        public void setStop() {
            this.start = false;
        }

        @Override
        public void run() {
            super.run();
            while (start) {
                try {
                    int length = mInput.available();
                    if (length > 0) {
                        byte[] buffer = new byte[length];
                        mInput.read(buffer, 0, length);
                        String recv = new String(buffer);
                        mRecvBuffer += recv;
//                        Log.d(TAG, String.format("Read %d bytes: %s", length, new String(buffer)));
                        process();
                    } else {
                        // Log.d(TAG,"休眠");
                        // Thread.sleep(100);
                    }
                } catch (IOException e) {
                    MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_ERROR_UART);
                    Log.d(TAG, "Serial port communicate failed");
                    event.message = "1";
                    EventBus.getDefault().post(event);

                    e.printStackTrace();
                } /*catch (InterruptedException e) {
                    MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_ERROR_UART);
                    Log.d(TAG, "Serial port communicate failed");
                    event.message = "1";
                    EventBus.getDefault().post(event);

                    e.printStackTrace();
                }*/
            }
        }
    }

    public class CalTrsdThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                Log.d(TAG, "cal&trsd");
                List<String> data = new ArrayList<>();
                data.add("4");
                ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                        "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                        "0", "cal&trsd", "get", 1, data);

                Log.d(TAG, "-> " + pkg.toString());
                Utils.doProcessProtocolInfo(
                        pkg, new Utils.ResponseCallback() {
                            @Override
                            public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                            }
                        });
                try {
                    Thread.sleep(5 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    public class MtrsdThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                Log.d(TAG, "mtrsd");
                List<String> data = new ArrayList<>();
                data.add("3");
                ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                        "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                        "0", "mtrsd", "get", 1, data);

                Log.d(TAG, "-> " + pkg.toString());
                Utils.doProcessProtocolInfo(
                        pkg, new Utils.ResponseCallback() {
                            @Override
                            public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                            }
                        });
                try {
                    Thread.sleep(6 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class HeartBeatThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                Log.d(TAG, "heartb");
                List<String> data = new ArrayList<>();
                data.add("0");
                ProtocolPackage pkg = new ProtocolPackage(mApplication.getSyncId(),
                        "1", mSpUtils.getKeyLoginiMasterDeviceSn(),
                        "0", "heartb", "none", 1, data);

                Log.d(TAG, "-> " + pkg.toString());
                Utils.doProcessProtocolInfo(
                        pkg, new Utils.ResponseCallback() {
                            @Override
                            public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                            }
                        });
                try {
                    Thread.sleep(5 * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    private class GPIOThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
               // Log.d(TAG, "GPIO");

                //int powerOnPin = mSerialPort.getPowerOnPinValue();//检测
                /*Log.d("powerOnPin", "powerOnPin=" + powerOnPin);

                if (gpioCount == 0) {
                    gpioCount = -1;
                    if (powerOnPin == 1) {//用户关机
                        mSerialPort.setCpuAndLoraValue(1, 1);
                    }
                }
                if (powerOnPin == 0) {//用户开机
                    mSerialPort.setCpuAndLoraValue(0, 0);
                }*/
            }
        }
    }

}
