package com.iot.zhs.guanwuyou;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.ViewAccountReportInfo;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * Created by H151136 on 3/4/2018.
 */

public class WorkReportActivity extends BaseActivity {

    private static final String TAG = "ZHS.IOT";
    private ImageView mBackImageView;
    private TextView mProjectNameTextView;
    private TextView mFillingMachineTextView;
    private TextView mSystemNumberTextView;
    private TextView mFillingStartTimeTextView;
    private TextView mPileNumberTextView;
    private TextView mFillingEndTimeTextView;
    private TextView mPileTypeTextView;
    private TextView mConCalValueTextView;
    private TextView mCoordinatexTextView;
    private TextView mSlurryCalValueTextView;
    private TextView mCoordinateyTextView;
    private TextView mConGradeDesignTextView;
    private TextView mPileDiameterTextView;
    private TextView mConGradeActualTextView;
    private TextView mPileLengthTextView;
    private TextView mPileDiffLevelTextView;
    private TextView mConGradeNumberTextView;
    private TextView mReporterNameTextView;
    private TextView mEmptyPileLengthTextView;
    private TextView mReportTimeTextView;

    private String mPileId = "";
    private Toast mToast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_report);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mPileId = getIntent().getStringExtra("pileId");
        mBackImageView = findViewById(R.id.iv_back);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WorkReportActivity.this.finish();
            }
        });
        mProjectNameTextView = findViewById(R.id.tv_project_name);
        mFillingMachineTextView = findViewById(R.id.tv_filling_machine);
        mSystemNumberTextView = findViewById(R.id.tv_system_number);
        mFillingStartTimeTextView = findViewById(R.id.tv_filling_start_time);
        mPileNumberTextView = findViewById(R.id.tv_pile_number);
        mFillingEndTimeTextView = findViewById(R.id.tv_filling_end_time);
        mPileTypeTextView = findViewById(R.id.tv_pile_type);
        mConCalValueTextView = findViewById(R.id.tv_con_cal_value);
        mCoordinatexTextView = findViewById(R.id.tv_coordinatex);
        mSlurryCalValueTextView = findViewById(R.id.tv_slurry_cal_value);
        mCoordinateyTextView = findViewById(R.id.tv_coordinatey);
        mConGradeDesignTextView = findViewById(R.id.tv_con_grade_design);
        mPileDiameterTextView = findViewById(R.id.tv_pile_diameter);
        mConGradeActualTextView = findViewById(R.id.tv_con_grade_actual);
        mPileLengthTextView = findViewById(R.id.tv_pile_length);
        mPileDiffLevelTextView = findViewById(R.id.tv_pile_diff_level);
        mConGradeNumberTextView = findViewById(R.id.tv_con_grade_number);
        mReporterNameTextView = findViewById(R.id.tv_reporter_name);
        mEmptyPileLengthTextView = findViewById(R.id.tv_empty_pile_length);
        mReportTimeTextView = findViewById(R.id.tv_report_time);

        doViewAccountReportInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                MyApplication.getInstance().getSpUtils().getKeyLoginUserId());
    }

    private class DoViewAccountReportInfoCallback extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {

        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            ViewAccountReportInfo info = gson.fromJson(response, ViewAccountReportInfo.class);

            if (info.code.equals(Utils.MSG_CODE_OK)) {
                if (info != null) {
                    mProjectNameTextView.setText(info.data.accountReport.projectName);
                    mFillingMachineTextView.setText(info.data.accountReport.pillingMachineName);
                    mSystemNumberTextView.setText(info.data.accountReport.systemNumber);
                    mFillingStartTimeTextView.setText(info.data.accountReport.fillStartTime);
                    mPileNumberTextView.setText(info.data.accountReport.pileNumber);
                    mFillingEndTimeTextView.setText(info.data.accountReport.fillEndTime);
                    mPileTypeTextView.setText(info.data.accountReport.pileTypeName);
                    mConCalValueTextView.setText(info.data.accountReport.conCalValue);
                    mCoordinatexTextView.setText(info.data.accountReport.coordinatex);
                    mSlurryCalValueTextView.setText(info.data.accountReport.slurryCalValue);
                    mCoordinateyTextView.setText(info.data.accountReport.coordinatey);
                    mConGradeDesignTextView.setText(info.data.accountReport.designOfConcrete);
                    mPileDiameterTextView.setText(info.data.accountReport.pileDiameter);
                    mConGradeActualTextView.setText(info.data.accountReport.actualUseConcrete);
                    mPileLengthTextView.setText(info.data.accountReport.pileLength);
                    mPileDiffLevelTextView.setText(info.data.accountReport.diffGrade);
                    mConGradeNumberTextView.setText(info.data.accountReport.conGrade);
                    mReporterNameTextView.setText(info.data.accountReport.reporter);
                    mEmptyPileLengthTextView.setText(info.data.accountReport.emptyPile);
                    mReportTimeTextView.setText(info.data.accountReport.reportTime);
                }
            } else {
                showToast(info.message);
            }
        }
    }

    ;

    private void doViewAccountReportInfo(String token, String loginName) {
//        JSONObject object = new JSONObject();
//        try {
//            object.put("pileId", mPileId);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        Log.d(TAG, "info: " + object.toString());
        String url = Utils.SERVER_ADDR + "/pile/doViewAccountReportInfo/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("pileId", mPileId)
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new DoViewAccountReportInfoCallback());
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
