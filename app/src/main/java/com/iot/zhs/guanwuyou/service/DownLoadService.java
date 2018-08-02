package com.iot.zhs.guanwuyou.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.database.DeviceVersion;
import com.iot.zhs.guanwuyou.utils.DowloadFileUtils;
import com.iot.zhs.guanwuyou.utils.Utils;

import org.litepal.crud.DataSupport;

public class DownLoadService extends Service {
    public String updateFileURL;
    public String serialNumber;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int a = 0;
    }

    /**
     * 如果多次执行了Context的startService方法，那么Service的onStartCommand方法也会相应的多次调用。
     * onStartCommand方法很重要，我们在该方法中根据传入的Intent参数进行实际的操作，
     * 比如会在此处创建一个线程用于下载数据或播放音乐等
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            if (intent.getStringExtra("updateFileURL") != null) {
                updateFileURL = intent.getStringExtra("updateFileURL");
            }
            if (intent.getStringExtra("serialNumber") != null) {
                serialNumber = intent.getStringExtra("serialNumber");
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    new DowloadFileUtils(MyApplication.getInstance().getCurrentActivity()).downloadFile(serialNumber, updateFileURL,
                            new DowloadFileUtils.DownLoadFileListener() {
                                @Override
                                public void success(String filePath) {
                                    DownLoadService.this.stopSelf();
                                }
                            });
                }
            }).start();

        }
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
