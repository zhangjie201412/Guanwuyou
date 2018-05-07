package com.iot.zhs.guanwuyou;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.iot.zhs.guanwuyou.comm.http.SaveAccountReportData;
import com.iot.zhs.guanwuyou.comm.http.SaveAccountReportInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by H151136 on 1/20/2018.
 */

public class NewTaskActivity extends AppCompatActivity {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private TextView mSystemNumberTextView;
    private TextView mPileNumberTextView;
    private AppCompatSpinner mPileTypeSpinner;
    private EditText mCoordinatexEditText;
    private EditText mCoordinateyEditText;
    private EditText mPileDiameteEditText;
    private EditText mPileLengthEditText;
    private AppCompatSpinner mConGradeNumberSpinner;
    private EditText mEmptyPileLengthEditText;
    private AppCompatSpinner mPillingMachineIdSpinner;
    private EditText mDesignOfConcreteEditText;

    private Button mButton;
    private ImageView mBackImageView;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;
    private String mPileId;
    private String mProjectId;

    private static final int NEW_TASK_UPDATE = 0x01;

    private SaveAccountReportData mSaveAccountReportData;

    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == NEW_TASK_UPDATE) {
                mSystemNumberTextView.setText(mSaveAccountReportData.data.pile.systemNumber);
                mPileNumberTextView.setText(mSaveAccountReportData.data.pile.pileNumber);
                String[] pileTypeObjects = new String[mSaveAccountReportData.data.pileTypeList.size()];
                for (int i = 0; i < mSaveAccountReportData.data.pileTypeList.size(); i++)
                    pileTypeObjects[i] = mSaveAccountReportData.data.pileTypeList.get(i).showName;
                ArrayAdapter<String> pileTypeAdapter = new ArrayAdapter<String>(
                        NewTaskActivity.this, android.R.layout.simple_spinner_dropdown_item, pileTypeObjects);
                mPileTypeSpinner.setAdapter(pileTypeAdapter);
                mCoordinatexEditText.setText(mSaveAccountReportData.data.pile.coordinatex);
                mCoordinatexEditText.setEnabled(false);
                mCoordinateyEditText.setText(mSaveAccountReportData.data.pile.coordinatey);
                mCoordinateyEditText.setEnabled(false);
                mPileDiameteEditText.setText(mSaveAccountReportData.data.pile.pileDiameter);
                mPileLengthEditText.setText(mSaveAccountReportData.data.pile.pileLength);
                String[] conGradeObjects = new String[mSaveAccountReportData.data.conGradeList.size()];
                for (int i = 0; i < mSaveAccountReportData.data.conGradeList.size(); i++)
                    conGradeObjects[i] = mSaveAccountReportData.data.conGradeList.get(i).showName;
                ArrayAdapter<String> conGradeAdapter = new ArrayAdapter<String>(
                        NewTaskActivity.this, android.R.layout.simple_spinner_dropdown_item, conGradeObjects);
                mConGradeNumberSpinner.setAdapter(conGradeAdapter);
                String[] pillingMachineObjects = new String[mSaveAccountReportData.data.pillingMachineList.size()];
                for (int i = 0; i < mSaveAccountReportData.data.pillingMachineList.size(); i++)
                    pillingMachineObjects[i] = mSaveAccountReportData.data.pillingMachineList.get(i).showName;
                ArrayAdapter<String> pillingMachineAdapter = new ArrayAdapter<String>(
                        NewTaskActivity.this, android.R.layout.simple_spinner_dropdown_item, pillingMachineObjects);
                mPillingMachineIdSpinner.setAdapter(pillingMachineAdapter);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        mSystemNumberTextView = findViewById(R.id.tv_system_number);
        mPileNumberTextView = findViewById(R.id.tv_pile_number);
        mPileTypeSpinner = findViewById(R.id.sp_pile_type);
        mCoordinatexEditText = findViewById(R.id.et_coordinatex);
        mCoordinateyEditText = findViewById(R.id.et_coordinatey);
        mPileDiameteEditText = findViewById(R.id.et_pile_diameter);
        mPileLengthEditText = findViewById(R.id.et_pile_length);
        mConGradeNumberSpinner = findViewById(R.id.sp_con_grade);
        mEmptyPileLengthEditText = findViewById(R.id.et_empty_pile_length);
        mPillingMachineIdSpinner = findViewById(R.id.sp_pilling_machine_id);
        mDesignOfConcreteEditText = findViewById(R.id.et_design_of_concrete);
        mBackImageView = findViewById(R.id.iv_back);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewTaskActivity.this.finish();
            }
        });
        mButton = findViewById(R.id.bt_sure);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emptyPileLength = mEmptyPileLengthEditText.getText().toString();
                String designOfConcrete = mDesignOfConcreteEditText.getText().toString();

                if (emptyPileLength.isEmpty() ||
                        designOfConcrete.isEmpty()) {
                    Toast.makeText(NewTaskActivity.this, "请输入正确的数据", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject object = new JSONObject();

                try {
                    object.put("pileId", mPileId);
                    object.put("pileTypeId", mSaveAccountReportData.data.pileTypeList.get(mPileTypeSpinner.getSelectedItemPosition()).id);

                    object.put("pileDiameter", mSaveAccountReportData.data.pile.pileDiameter);
                    object.put("pileLength", mSaveAccountReportData.data.pile.pileLength);
                    object.put("conGradeId", mSaveAccountReportData.data.conGradeList.get(mConGradeNumberSpinner.getSelectedItemPosition()).id);
                    object.put("emptyPile", emptyPileLength);
                    object.put("pillingMachineId", mSaveAccountReportData.data.pillingMachineList.get(mPillingMachineIdSpinner.getSelectedItemPosition()).id);
                    object.put("designOfConcrete", designOfConcrete);
                    object.put("masterDeviceSN", mSpUtils.getKeyLoginiMasterDeviceSn());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "JSON: " + object.toString());
                doSaveAccountReportInfo(mSpUtils.getKeyLoginToken(),
                        mSpUtils.getKeyLoginUserId(),
                        object.toString());
//                Intent intent = new Intent(NewTaskActivity.this, CalibrationActivity.class);
//                intent.putExtra("jsonStr", object.toString());
//                startActivity(intent);
            }
        });
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
        mPileId = getIntent().getStringExtra("pileId");
        mProjectId = getIntent().getStringExtra("projectId");
        doSaveAccountReportData(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(),
                mPileId,
                mProjectId,
                mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    private class SaveAccountReportDataCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            mSaveAccountReportData = gson.fromJson(response, SaveAccountReportData.class);
            if (mSaveAccountReportData != null) {
                Message message = new Message();
                message.what = Utils.UI_SHOW_TOAST;
                Bundle bundle = new Bundle();
                bundle.putString("message", mSaveAccountReportData.message);
                Log.d(TAG, "message: " + mSaveAccountReportData.message);
                message.setData(bundle);
                mUiHandler.sendMessage(message);
                if (mSaveAccountReportData.code.equals(Utils.MSG_CODE_OK)) {
                    mUiHandler.sendEmptyMessage(NEW_TASK_UPDATE);
                }
            }
        }
    }

    private void doSaveAccountReportData(String token, String loginName, String pileId, String projectId, String masterSN) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("pileId", pileId);
            jsonObject.put("projectId", projectId);
            jsonObject.put("masterDeviceSN", masterSN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        String url = Utils.SERVER_ADDR + "/pile/doSaveAccountReportData/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", jsonObject.toString())
                .build()
                .execute(
                        new SaveAccountReportDataCallback()
                );
    }

    private class SaveAccountReportInfoCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            SaveAccountReportInfo info = gson.fromJson(response, SaveAccountReportInfo.class);
            Bundle bundle = new Bundle();
            bundle.putString("message", info.message);
            Log.d(TAG, "message: " + info.message);
            Message message = new Message();
            message.what = Utils.UI_SHOW_TOAST;
            message.setData(bundle);
            mUiHandler.sendMessage(message);
            if (info.code.equals(Utils.MSG_CODE_OK)) {
                Intent intent = new Intent(NewTaskActivity.this, CalibrationActivity.class);
                intent.putExtra("pileId", mPileId);
                startActivity(intent);
                NewTaskActivity.this.finish();
            }
        }
    }

    private void doSaveAccountReportInfo(String token, String loginName, String json) {
        String url = Utils.SERVER_ADDR + "/pile/doSaveAccountReportInfo/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", json)
                .build()
                .execute(
                        new SaveAccountReportInfoCallback()
                );
    }

}
