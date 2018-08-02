package com.iot.zhs.guanwuyou.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.iot.serialport.SerialPort;
import com.iot.zhs.guanwuyou.ISerialPort;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
import com.iot.zhs.guanwuyou.protocol.SerialPackage;
import com.iot.zhs.guanwuyou.protocol.YmodernPackage;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by H151136 on 1/21/2018.
 */

public class YmodernService extends Service {
    private static final String TAG = "ZSH.IOT";
    private InputStream mInput;
    private OutputStream mOutput;
    private SerialPort mSerialPort;
    private String mRecvBuffer;
    private SerialPortMonitorThread mMonitorThread;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;

    private YmodernPackage ymodernPkg;

    private ISerialPort.Stub mBinder = new ISerialPort.Stub() {
        @Override
        public void ping(int count) throws RemoteException {
            Log.d(TAG, "Ping: " + count);
        }

        @Override
        public void setPowerUp() throws RemoteException {

        }

        //没有调用
        @Override
        public void setPowerDown() throws RemoteException {

        }

        @Override
        public void requestCalMac() throws RemoteException {

        }

        @Override
        public void matchList() throws RemoteException {

        }

        @Override
        public void requestMode() throws RemoteException {

        }

        /**
         * 发送apk 版本号
         * @throws RemoteException
         */
        @Override
        public void sendApkVersion() throws RemoteException {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
        mRecvBuffer = "";
        try {
            mSerialPort = new SerialPort(new File("/dev/ttymxc1"), 115200);
            mInput = mSerialPort.getInputStream();
            mOutput = mSerialPort.getOutputStream();
            Log.d(TAG, "open serial port 1 done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMonitorThread = new SerialPortMonitorThread();
        mMonitorThread.setStart();
        mMonitorThread.start();

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
        if (event.type == MessageEvent.EVENT_TYPE_SERIAL_UPDATE_WRITE) {
            try {
                if (!Utils.stringIsEmpty(event.message)) {
                    Log.d(TAG, "YmodernPackage中控发送给主机的升级协议：" + event.message);

                    mOutput.write(event.message.getBytes());
                }

                if (event.chars != null && event.chars.length > 0) {
                    mOutput.write(event.chars);
                    Log.d(TAG,"YmodernPackage数据帧："+Arrays.toString(event.chars));
                    Log.d(TAG,"YmodernPackage数据帧："+Utils.bytesToHexString(event.chars));

                }

            } catch (IOException e) {
                Log.d(TAG, "中控发送给主机升级协议error:" + e.getMessage());
                e.printStackTrace();
            }
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


            ymodernPkg = YmodernPackage.getInstance();
            ymodernPkg.setmRawData(cutString);
            ymodernPkg.parse();

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
}
