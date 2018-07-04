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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.comm.http.SaveAccountReportInfo;
import com.iot.zhs.guanwuyou.comm.http.StartPourInfo;
import com.iot.zhs.guanwuyou.database.PileCalValue;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
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

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;

/**
 * Created by H151136 on 1/20/2018.
 */

public class CalibrationActivity extends BaseActivity {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private EditText mConGradeCalEditText;
    private EditText mSlurryEditText;

    private Button mButton;
    private ImageView mBackImageView;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;
    private Toast mToast;
    private String mPileId;
    private NotificationDialog mNotificationDialog;

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == Utils.UI_SHOW_TOAST) {
                showToast(msg.getData().getString("message"));
            }
            Bundle data = msg.getData();
            String code = data.getString("code");
            if (code.equals(Utils.MSG_CODE_OK)) {
                //开始灌注成功后，删除数据库pileId的数据
                DataSupport.deleteAll(PileCalValue.class, "pileId = ?", mPileId);

                Intent intent = new Intent(CalibrationActivity.this, FillingActivity.class);
                intent.putExtra("pileId", mPileId);
                startActivity(intent);
                CalibrationActivity.this.finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        //标定成功后弹框提示成功
        mNotificationDialog = new NotificationDialog();
        mNotificationDialog.init("提醒",
                "确定",
                "",
                new NotificationDialog.NotificationDialogListener() {
                    @Override
                    public void onButtonClick(int id) {
                        //响应左边的button
                        mNotificationDialog.dismiss();

                    }
                });


        mConGradeCalEditText = findViewById(R.id.et_con_grade_calibration);
        mSlurryEditText = findViewById(R.id.et_slurry_calibration);

        mButton = findViewById(R.id.bt_start_filling);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mConGradeCalEditText.getText().toString().isEmpty()) {
                    showToast("请先进行砼标定!");
                    return;
                }

                doStartPourInfo(mSpUtils.getKeyLoginToken(),
                        mSpUtils.getKeyLoginUserId(),
                        mPileId,
                        mConGradeCalEditText.getText().toString(),
                        mSlurryEditText.getText().toString());
            }
        });
        mBackImageView = findViewById(R.id.iv_back);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CalibrationActivity.this.finish();
            }
        });

        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mPileId = getIntent().getStringExtra("pileId");
        mConGradeCalEditText.setEnabled(false);
        mSlurryEditText.setEnabled(false);
        updateView();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void updateView() {
        if (DataSupport.where("pileId = ?", mPileId).find(PileCalValue.class).size() != 0) {
            PileCalValue pileCalValue=DataSupport.where("pileId = ?", mPileId).find(PileCalValue.class).get(0);
            //砼标定
            if(pileCalValue.getCalCon()!=null){
                if(pileCalValue.getCalCon().equals("0")){//失败
                    mConGradeCalEditText.setText("");
                }else{
                    mConGradeCalEditText.setText(pileCalValue.getCalCon());
                }
            }
            //泥浆标定
            if(pileCalValue.getCalSlurry()!=null){
                if(pileCalValue.getCalSlurry().equals("0")){//失败
                    mSlurryEditText.setText("");
                }else{
                    mSlurryEditText.setText(pileCalValue.getCalSlurry());
                }
            }
        }else{
            mConGradeCalEditText.setText("");
            mSlurryEditText.setText("");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "event: " + event.type + ", message: " + event.message);
        if (event.type == MessageEvent.EVENT_TYPE_UPDATE_CAL_CON) {//砼标定值
            if(event.message.equals("0")){//标定失败
                mConGradeCalEditText.setText("");
                if (mNotificationDialog != null && !mNotificationDialog.isAdded()) {
                    mNotificationDialog.setMessage("砼标定失败,请重新标定!");
                    mNotificationDialog.show(getSupportFragmentManager(), "Notification");
                }
            }else {
                mConGradeCalEditText.setText(event.message);

                if (mNotificationDialog != null && !mNotificationDialog.isAdded()) {
                    mNotificationDialog.setMessage("恭喜您,砼标定成功!");
                    mNotificationDialog.show(getSupportFragmentManager(), "Notification");
                }
            }

            PileCalValue pileCalValue = new PileCalValue();
            pileCalValue.setCalCon(event.message);
            if (DataSupport.where("pileId = ?", mPileId).find(PileCalValue.class).size() == 0) {
                //insert new data
                pileCalValue.setPileId(mPileId);
                pileCalValue.save();
            } else {
                pileCalValue.updateAll("pileId = ?", mPileId);
            }

        }
        if (event.type == MessageEvent.EVENT_TYPE_UPDATE_CAL_SLURRY) {//泥浆标定值
            if(event.message.equals("0")){
                mSlurryEditText.setText("");
                if (mNotificationDialog != null && !mNotificationDialog.isAdded()) {
                    mNotificationDialog.setMessage("泥浆标定失败,请重新标定!");
                    mNotificationDialog.show(getSupportFragmentManager(), "Notification");
                }
            }else {
                mSlurryEditText.setText(event.message);
                if (mNotificationDialog != null && !mNotificationDialog.isAdded()) {
                    mNotificationDialog.setMessage("恭喜您,泥浆标定成功!");
                    mNotificationDialog.show(getSupportFragmentManager(), "Notification");
                }
            }
            PileCalValue pileCalValue = new PileCalValue();
            pileCalValue.setCalSlurry(event.message);
            if (DataSupport.where("pileId = ?", mPileId).find(PileCalValue.class).size() == 0) {
                //insert new data
                pileCalValue.setPileId(mPileId);
                pileCalValue.save();
            } else {
                pileCalValue.updateAll("pileId = ?", mPileId);
            }
        }
    }

    private class StartPourInfoCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            StartPourInfo info = gson.fromJson(response, StartPourInfo.class);
            Bundle data = new Bundle();
            data.putString("message", info.message);
            data.putString("code", info.code);
            Message msg = new Message();
            msg.what = Utils.UI_SHOW_TOAST;
            msg.setData(data);
            mUiHandler.sendMessage(msg);
        }
    }

    private void doStartPourInfo(String token, String loginName, String pileId, String conCal, String slurryCal) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("pileId", pileId);
            jsonObject.put("conCalValue", conCal);
            jsonObject.put("slurryCalValue", slurryCal);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "开始灌注post: " + jsonObject.toString());
        String url = Utils.SERVER_ADDR + "/pile/doStartPourInfo/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", jsonObject.toString())
                .build()
                .execute(
                        new StartPourInfoCallback()
                );
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
