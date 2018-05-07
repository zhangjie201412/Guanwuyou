package com.iot.zhs.guanwuyou;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.EndPourInfo;
import com.iot.zhs.guanwuyou.comm.http.UpdatePileReportFinish;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 2/8/2018.
 */

public class WorkReportPreviewActivity extends AppCompatActivity {

    private static final String TAG = "ZHS.IOT";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Button mButton;
    private TextView mSystemNumberTextView;
    private TextView mPileNumberTextView;
    private AppCompatSpinner mPileTypeSpinner;
    private EditText mCoordinatexEditText;
    private EditText mCoordinateyEditText;
    private EditText mPileDiameterEditText;
    private EditText mPileLengthEditText;
    private AppCompatSpinner mConGradeNumberSpinner;
    private EditText mEmptyPileLengthEditText;
    private AppCompatSpinner mPillingMachineIdSpinner;
    private EditText mDesignOfConcreteEditText;
    private EditText mActualUseConcreteEditText;
    private ImageView mBackImageView;
    private EndPourInfo mEndPourInfo;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;
    private Toast mToast;
    private String mPileId;
    private NotificationDialog mNotificationDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_report_preview);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mPileId = getIntent().getStringExtra("pileId");
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
//        mEndPourInfo = MyApplication.getInstance().getEndpourInfo();
        String endPourInfoString = mSpUtils.getKeyEndPourInfo();
        Log.d(TAG, "endPourInfo in work report: " + endPourInfoString);
        Gson gson = new Gson();
        mEndPourInfo = gson.fromJson(endPourInfoString, EndPourInfo.class);
        if(mEndPourInfo == null) {
            showToast("没有获取报告内容");
        }

        mButton = findViewById(R.id.bt_sure);
        mSystemNumberTextView = findViewById(R.id.tv_system_number);
        mPileNumberTextView = findViewById(R.id.tv_pile_number);
        mPileTypeSpinner = findViewById(R.id.sp_pile_type);
        mCoordinatexEditText = findViewById(R.id.et_coordinatex);
        mCoordinateyEditText = findViewById(R.id.et_coordinatey);
        mPileDiameterEditText = findViewById(R.id.et_pile_diameter);
        mPileLengthEditText = findViewById(R.id.et_pile_length);
        mConGradeNumberSpinner = findViewById(R.id.sp_con_grade);
        mEmptyPileLengthEditText = findViewById(R.id.et_empty_pile_length);
        mPillingMachineIdSpinner = findViewById(R.id.sp_pilling_machine_id);
        mDesignOfConcreteEditText = findViewById(R.id.et_design_of_concrete);
        mActualUseConcreteEditText = findViewById(R.id.et_actial_of_concrete);
        mBackImageView = findViewById(R.id.iv_back);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkReportPreviewActivity.this.finish();
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String actualUseCongrete = mActualUseConcreteEditText.getText().toString();
                if(actualUseCongrete.isEmpty()) {
                    showToast("请输入有效的值");
                    return;
                }
                mNotificationDialog.setMessage("桩 " + mSystemNumberTextView.getText().toString() + " 已完成");
                mNotificationDialog.show(getSupportFragmentManager(), "Notification");
            }
        });

        initView();
    }

    private void initView() {
        mSystemNumberTextView.setText(mEndPourInfo.data.accountReport.systemNumber);
        mPileNumberTextView.setText(mEndPourInfo.data.accountReport.pileNumber);
        String[] pileTypeObjects = new String[mEndPourInfo.data.pileTypeList.size()];
        for (int i = 0; i < mEndPourInfo.data.pileTypeList.size(); i++)
            pileTypeObjects[i] = mEndPourInfo.data.pileTypeList.get(i).showName;
        ArrayAdapter<String> pileTypeAdapter = new ArrayAdapter<String>(
                WorkReportPreviewActivity.this, android.R.layout.simple_spinner_dropdown_item, pileTypeObjects);
        mPileTypeSpinner.setAdapter(pileTypeAdapter);
        mCoordinatexEditText.setText(mEndPourInfo.data.accountReport.coordinatex);
        mCoordinatexEditText.setEnabled(false);
        mCoordinateyEditText.setText(mEndPourInfo.data.accountReport.coordinatey);
        mCoordinateyEditText.setEnabled(false);
        mPileDiameterEditText.setText(mEndPourInfo.data.accountReport.pileDiameter);
        mPileLengthEditText.setText(mEndPourInfo.data.accountReport.pileLength);
        String[] conGradeObjects = new String[mEndPourInfo.data.conGradeList.size()];
        for (int i = 0; i < mEndPourInfo.data.conGradeList.size(); i++)
            conGradeObjects[i] = mEndPourInfo.data.conGradeList.get(i).showName;
        ArrayAdapter<String> conGradeAdapter = new ArrayAdapter<String>(
                WorkReportPreviewActivity.this, android.R.layout.simple_spinner_dropdown_item, conGradeObjects);
        mConGradeNumberSpinner.setAdapter(conGradeAdapter);
        String[] pillingMachineObjects = new String[mEndPourInfo.data.pillingMachineList.size()];
        for (int i = 0; i < mEndPourInfo.data.pillingMachineList.size(); i++)
            pillingMachineObjects[i] = mEndPourInfo.data.pillingMachineList.get(i).showName;
        ArrayAdapter<String> pillingMachineAdapter = new ArrayAdapter<String>(
                WorkReportPreviewActivity.this, android.R.layout.simple_spinner_dropdown_item, pillingMachineObjects);
        mPillingMachineIdSpinner.setAdapter(pillingMachineAdapter);
        mDesignOfConcreteEditText.setText(mEndPourInfo.data.accountReport.designOfConcrete);
        mNotificationDialog = new NotificationDialog();
        mNotificationDialog.init("提醒",
                "确定",
                "取消",
                new NotificationDialog.NotificationDialogListener() {
                    @Override
                    public void onButtonClick(int id) {
                        //响应左边的button
                        if (id == 1) {
                            String actualUseCongrete = mActualUseConcreteEditText.getText().toString();
                            doUpdatePileReportFinishInfo(mSpUtils.getKeyLoginToken(),
                                    mSpUtils.getKeyLoginUserId(), actualUseCongrete);
                        } else if(id == 2) {
                            mNotificationDialog.dismiss();
                        }
                    }
                });
    }

    private class DoUpdatePileReportFinishInfoCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            UpdatePileReportFinish report = gson.fromJson(response, UpdatePileReportFinish.class);
            showToast(report.message);
            if(report.code.equals(Utils.MSG_CODE_OK)) {
                WorkReportPreviewActivity.this.finish();
            }
        }
    };

    private void doUpdatePileReportFinishInfo(String token, String loginName, String actualUseCongrete) {
        JSONObject object = new JSONObject();
        try {
            object.put("pileId", mPileId);
            object.put("pileTypeId", mEndPourInfo.data.accountReport.pileTypeId);
            object.put("pileDiameter", mEndPourInfo.data.accountReport.pileDiameter);
            object.put("pileLength", mEndPourInfo.data.accountReport.pileLength);
            object.put("conGradeId", mEndPourInfo.data.accountReport.conGradeId);
            object.put("emptyPile", mEndPourInfo.data.accountReport.emptyPile);
            object.put("pillingMachineId", mEndPourInfo.data.pillingMachineList.get(0).id);
            object.put("designOfConcrete", mEndPourInfo.data.accountReport.designOfConcrete);
            object.put("actualUseConcrete", actualUseCongrete);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "info: " + object.toString());

        String url = Utils.SERVER_ADDR + "/pile/doUpdatePileReportFinishInfo/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", object.toString())
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new DoUpdatePileReportFinishInfoCallback());
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }


}
