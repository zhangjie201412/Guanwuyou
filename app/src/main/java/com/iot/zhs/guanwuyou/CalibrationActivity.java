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
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;

/**
 * Created by H151136 on 1/20/2018.
 */

public class CalibrationActivity extends AppCompatActivity {
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

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if(what == Utils.UI_SHOW_TOAST) {
                showToast(msg.getData().getString("message"));
            }
            Bundle data = msg.getData();
            String code = data.getString("code");
            if(code.equals(Utils.MSG_CODE_OK)) {
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
        mConGradeCalEditText = findViewById(R.id.et_con_grade_calibration);
        mSlurryEditText = findViewById(R.id.et_slurry_calibration);

        mButton = findViewById(R.id.bt_start_filling);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mConGradeCalEditText.getText().toString().isEmpty()) {
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
        mConGradeCalEditText.setText(mSpUtils.getKeyCalCon());
        mSlurryEditText.setText(mSpUtils.getKeyCalSlurry());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Log.d(TAG, "event: " + event.type + ", message: " + event.message);
        if (event.type == MessageEvent.EVENT_TYPE_UPDATE_CALIBRATION) {
            updateView();
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
