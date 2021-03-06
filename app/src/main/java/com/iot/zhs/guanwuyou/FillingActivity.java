package com.iot.zhs.guanwuyou;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.EndPourData;
import com.iot.zhs.guanwuyou.comm.http.EndPourInfo;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.comm.http.ViewPileInfo;
import com.iot.zhs.guanwuyou.database.AlarmState;
import com.iot.zhs.guanwuyou.database.PileCalValue;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
import com.iot.zhs.guanwuyou.protocol.SerialPackage;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.internal.Util;

/**
 * Created by H151136 on 1/20/2018.
 */

public class FillingActivity extends BaseActivity {
    private static final String TAG = "FillingZHS.IOT";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private TextView mSystemNumberTextView;
    private TextView mPileNumberTextView;
    private TextView mPileTypeTextView;
    private TextView mCoordinatexTextView;
    private TextView mCoordinateyTextView;
    private TextView mPileDiameterTextView;
    private TextView mPileLengthTextView;
    private TextView mConGradeTextView;
    private TextView mReporterTextView;

    private Button mButton;
    private ImageView mBackImageView;
    private ImageView mAnimationImageView;

    private MyApplication myApplication;
    private SharedPreferenceUtils mSpUtils;
    private String mPileId;
    private List<String> mFinalCheckData;
    private Toast mToast;
    private NotificationDialog mNotificationDialog;
    private int mAnimationStage = 0;
    private boolean mAnimationRunning = false;
    private AnimationThread mAnimationThread;

    private boolean mActivedByPileMap = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filling);
        mSystemNumberTextView = findViewById(R.id.tv_system_number);
        mPileNumberTextView = findViewById(R.id.tv_pile_number);
        mPileTypeTextView = findViewById(R.id.tv_pile_type);
        mCoordinatexTextView = findViewById(R.id.tv_coordinatex);
        mCoordinateyTextView = findViewById(R.id.tv_coordinatey);
        mPileDiameterTextView = findViewById(R.id.tv_pile_diameter);
        mPileLengthTextView = findViewById(R.id.tv_pile_length);
        mConGradeTextView = findViewById(R.id.tv_con_grade);
        mButton = findViewById(R.id.bt_end_filling);
        mAnimationImageView = findViewById(R.id.iv_animation);
        mReporterTextView = findViewById(R.id.tv_last_user);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "最后一笔raw值--mFinalCheckData:" + Utils.listToString(mFinalCheckData, ","));
                if (mAnimationStage == 2) {
                    //结束灌注
                    doEndPourInfo(mSpUtils.getKeyLoginToken(),
                            mSpUtils.getKeyLoginUserId(),
                            mPileId,
                            mFinalCheckData);
                } else {
                    //计算差异等级
                    doEndPourData(mSpUtils.getKeyLoginToken(),
                            mSpUtils.getKeyLoginUserId(),
                            mPileId,
                            mFinalCheckData);
                }
            }
        });
        mBackImageView = findViewById(R.id.iv_back);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FillingActivity.this.finish();
            }
        });

        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();
        mPileId = getIntent().getStringExtra("pileId");
        Log.d(TAG, "####pile id = " + mPileId);

        // mAnimationStage=Utils.stringToInt(mSpUtils.getKeyAlarmStatus());
        //和pileId进行绑定，查询
        if (DataSupport.where("pileId = ?", mPileId).find(AlarmState.class).size() != 0) {
            AlarmState alarmState = DataSupport.where("pileId = ?", mPileId).find(AlarmState.class).get(0);
            mAnimationStage = Utils.stringToInt(alarmState.getAlarmValue());
        }

        mFinalCheckData = new ArrayList<>();
        mFinalCheckData.clear();
        List<SlaveDevice> slaveDeviceList = DataSupport.findAll(SlaveDevice.class);
        String[] useList = MyApplication.getInstance().getSpUtils().getKeyUseList();//uselist协议的值

        for (SlaveDevice device : slaveDeviceList) {//取在线从机的最后一笔
            if (useList != null && useList.length > 0) {
                for (int i = 0; i < useList.length; i++) {
                    if (device.getSerialNumber().equals(useList[i])) {
                        mFinalCheckData.add("" + device.getLatestData());
                        Log.d(TAG, "mFinalCheckData:" + device.getLatestData());
                    }
                }
            }
        }

        mNotificationDialog = new NotificationDialog();
        mNotificationDialog.init("提醒", "是", "否", new NotificationDialog.NotificationDialogListener() {
            @Override
            public void onButtonClick(int id) {
                if (id == 1) {
                    //结束灌注
                    doEndPourInfo(mSpUtils.getKeyLoginToken(),
                            mSpUtils.getKeyLoginUserId(),
                            mPileId,
                            mFinalCheckData);
                } else if (id == 2) {
                    mNotificationDialog.dismiss();
                }
            }
        });
        //初始化
        doViewPileInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(),
                mPileId,
                mSpUtils.getKeyLoginiMasterDeviceSn());
        mAnimationThread = new AnimationThread();
        mAnimationRunning = true;
        mAnimationThread.start();
       /* mActivedByPileMap = getIntent().getBooleanExtra("ACTIVITY_BY_PILE_MAP", false);
        if (mActivedByPileMap) {
            if (mSpUtils.getKeySlaveAlarm()) {
                doEndPourInfo(mSpUtils.getKeyLoginToken(),
                        mSpUtils.getKeyLoginUserId(),
                        mPileId,
                        mFinalCheckData);
            } else {
                doEndPourData(mSpUtils.getKeyLoginToken(),
                        mSpUtils.getKeyLoginUserId(),
                        mPileId,
                        mFinalCheckData);
            }
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAnimationRunning = false;
        try {
            mAnimationThread.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static final int STAGE0_ON = 0;
    private static final int STAGE0_OFF = 1;
    private static final int STAGE1_ON = 2;
    private static final int STAGE1_OFF = 3;
    private static final int STAGE2_ON = 4;
    private static final int STAGE2_OFF = 5;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (!mAnimationRunning)
                return;

            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Log.d(TAG, "almsta--灌注界面动画开始执行时间--" + dateStr + ",mAnimationStage=" + mAnimationStage);

            switch (what) {
                case STAGE0_ON:
                    mAnimationImageView.setImageResource(R.mipmap.ic_filling_1);
                    break;
                case STAGE0_OFF:
                    mAnimationImageView.setImageResource(R.mipmap.ic_filling_0);
                    break;
                case STAGE1_ON:
                    mAnimationImageView.setImageResource(R.mipmap.ic_filling_2);
                    break;
                case STAGE1_OFF:
                    mAnimationImageView.setImageResource(R.mipmap.ic_filling_1);
                    break;
                case STAGE2_ON:
                    mAnimationImageView.setImageResource(R.mipmap.ic_filling_2);
                    break;
                case STAGE2_OFF:
                    mAnimationImageView.setImageResource(R.mipmap.ic_filling_2);
                    break;

            }
        }
    };

    private class AnimationThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (mAnimationRunning) {
                try {
                    if (mAnimationStage == 0) {
                        mHandler.sendEmptyMessage(STAGE0_OFF);
                        Thread.sleep(1000);
                        mHandler.sendEmptyMessage(STAGE0_ON);
                        Thread.sleep(1000);
                    } else if (mAnimationStage == 1) {
                        mHandler.sendEmptyMessage(STAGE1_OFF);
                        Thread.sleep(1000);
                        mHandler.sendEmptyMessage(STAGE1_ON);
                        Thread.sleep(1000);
                    } else if (mAnimationStage == 2) {
                        mHandler.sendEmptyMessage(STAGE2_OFF);
                        Thread.sleep(1000);
                        mHandler.sendEmptyMessage(STAGE2_ON);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 初始化
     *
     * @param token
     * @param loginName
     * @param pileId
     * @param masterSN
     */
    private void doViewPileInfo(String token, String loginName, String pileId, String masterSN) {
        JSONObject object = new JSONObject();
        try {
            object.put("pileId", pileId);
            object.put("masterDeviceSN", masterSN);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Utils.SERVER_ADDR + "/pile/doViewPileInfo/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", object.toString())
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new DoViewPileInfoCallback());
    }

    private class DoViewPileInfoCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();

            ViewPileInfo info = gson.fromJson(response, ViewPileInfo.class);
            String code = info.code;
            if (code.equals(Utils.MSG_CODE_OK)) {
                mSystemNumberTextView.setText(info.data.pile.systemNumber);
                mPileNumberTextView.setText(info.data.pile.pileNumber);
                mPileTypeTextView.setText(info.data.pile.pileTypeName);
                mCoordinatexTextView.setText(info.data.pile.coordinatex);
                mCoordinateyTextView.setText(info.data.pile.coordinatey);
                mPileDiameterTextView.setText(info.data.pile.pileDiameter);
                mPileLengthTextView.setText(info.data.pile.pileLength);
                mConGradeTextView.setText(info.data.pile.conGrade);
                mReporterTextView.setText(info.data.pile.reporter);
            }
            showToast(info.message);
        }
    }


    /**
     * 结束灌注
     *
     * @param token
     * @param loginName
     * @param pileId
     * @param finalCheckData
     */

    private void doEndPourInfo(String token, String loginName, String pileId, List<String> finalCheckData) {
        JSONObject object = new JSONObject();
        try {
            object.put("pileId", pileId);
            if (!finalCheckData.equals("-1")) {
                object.put("finalCheckData", finalCheckData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Utils.SERVER_ADDR + "/pile/doEndPourInfo/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", object.toString())
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new DoEndPourInfoCallback());
    }

    private class DoEndPourInfoCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();

            EndPourInfo info = gson.fromJson(response, EndPourInfo.class);
            String code = info.code;

            if (code.equals(Utils.MSG_CODE_OK)) {
                mSpUtils.setKeyEndPourInfo(response);
                //结束灌注后清空该桩的信息
                DataSupport.deleteAll(AlarmState.class, "pileId = ?", mPileId);
                //结束灌注时发送关闭蜂鸣器的指令
                // >>cssp,0,SN0302201709230001,0,beep,1，index,checksum
                //index值为0和1  1表示关闭蜂鸣器。这条指令只在告警的时候使用，一旦从告警状态退出到其他状态，重新回到告警状态的话，蜂鸣器还是会响，此时需要再次发送该指令。
                SerialPackage serialPackage = new SerialPackage();
                serialPackage.setSyncId(myApplication.getSyncId());
                serialPackage.setDeviceId0(mSpUtils.getKeyLoginiMasterDeviceSn());
                serialPackage.setDeviceId1("0");
                List<String> dataList = new ArrayList<>();
                dataList.add("1");
                String rsp = serialPackage.makeResponse("beep", dataList);

                MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
                event.message = rsp;
                EventBus.getDefault().post(event);

                //jump to report activity
                Intent intent = new Intent(FillingActivity.this, WorkReportPreviewActivity.class);
                intent.putExtra("pileId", mPileId);
                startActivity(intent);
                FillingActivity.this.finish();
            } else {

            }
            showToast(info.message);
        }
    }


    /**
     * 计算差异等级
     *
     * @param token
     * @param loginName
     * @param pileId
     * @param finalCheckData
     */
    private void doEndPourData(String token, String loginName, String pileId, List<String> finalCheckData) {
        JSONObject object = new JSONObject();
        try {
            object.put("pileId", pileId);
            if (!finalCheckData.equals("-1")) {
                object.put("finalCheckData", finalCheckData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = Utils.SERVER_ADDR + "/pile/doEndPourData/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", object.toString())
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new DoEndPourDataCallback());
    }

    private class DoEndPourDataCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            EndPourData data = gson.fromJson(response, EndPourData.class);
            if (data.code.equals(Utils.MSG_CODE_OK)) {
                Log.d(TAG, "code: " + data.code);
                Log.d(TAG, "diffGrade: " + data.data.diffGrade);

                mNotificationDialog.setMessage("当前差异等级为: " + data.data.diffGrade + "\n"
                        + "当前混凝土与新鲜砼差异较大，可能会达不到预设强度，是否确认结束灌注！");
                mNotificationDialog.show(getSupportFragmentManager(), "diffGrade");
            }
            showToast(data.message);
        }
    }


    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "event: " + event.type + ", message: " + event.message);
        if (event.type == MessageEvent.EVENT_TYPE_ALARM_STATUS) {
            mAnimationStage = Utils.stringToInt(event.message);

            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Log.d(TAG, "almsta--灌注界面接受的时间--" + dateStr);


            //和pileId进行绑定，存储于数据库
            AlarmState alarmState = new AlarmState();
            alarmState.setAlarmValue(event.message);
            if (DataSupport.where("pileId = ?", mPileId).find(AlarmState.class).size() == 0) {
                //insert new data
                alarmState.setPileId(mPileId);
                alarmState.save();
            } else {
                alarmState.updateAll("pileId = ?", mPileId);
            }
        }
    }

}
