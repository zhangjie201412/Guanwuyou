package com.iot.zhs.guanwuyou;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.ViewPileInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;
import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;

/**
 * Created by H151136 on 1/19/2018.
 */

public class PileDetailActivity extends BaseActivity {
    private static final String TAG = "ZHS.IOT";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Toast mToast;
    private String mPileId;
    private String mProjectId;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;

    private TextView mSystemNumberTextView;
    private TextView mPileNumberTextView;
    private TextView mPileTypeTextView;
    private TextView mCoordinatexTextView;
    private TextView mCoordinateyTextView;
    private TextView mConstructionStateTextView;
    private TextView mPileDiameterTextView;
    private TextView mPileLengthTextView;
    private TextView mConGradeTextView;
    private TextView mFillEndTimeTextView;

    private Button mNewTaskButton;
    private ImageView mBackImageView;

    private WaitProgressDialog mProgressDialog;
    private int mHasReport = 0;

    private static final int BUTTON_EVENT_NEW_TASK = 0;
    private static final int BUTTON_EVENT_CONTINUE = 1;
    private static final int BUTTON_EVENT_REPORT = 2;
    private static final int BUTTON_EVENT_REPORT2 = 3;

    private int mButtonEvent = -1;
  //  private int mNoFinishState;
    private String pileId;
    private int constructionState;

    private NotificationDialog mNotificationDialog;
    private String noFinishPile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pile_detail);
        mSystemNumberTextView = findViewById(R.id.tv_system_number);
        mPileNumberTextView = findViewById(R.id.tv_pile_number);
        mPileTypeTextView = findViewById(R.id.tv_pile_type);
        mCoordinatexTextView = findViewById(R.id.tv_coordinatex);
        mCoordinateyTextView = findViewById(R.id.tv_coordinatey);
        mConstructionStateTextView = findViewById(R.id.tv_construction_state);
        mPileDiameterTextView = findViewById(R.id.tv_pile_diameter);
        mPileLengthTextView = findViewById(R.id.tv_pile_length);
        mConGradeTextView = findViewById(R.id.tv_con_grade);
        mFillEndTimeTextView = findViewById(R.id.tv_fill_end_time);
        mBackImageView = findViewById(R.id.iv_back);
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PileDetailActivity.this.finish();
            }
        });

        mNewTaskButton = findViewById(R.id.bt_new_task);
        //mNoFinishState = getIntent().getIntExtra("noFinishState", -1);
        //mNoFinishPileId = getIntent().getStringExtra("noFinishPileId");
        mNewTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mButtonEvent == BUTTON_EVENT_NEW_TASK) {
                    Intent intent = new Intent(PileDetailActivity.this, NewTaskActivity.class);
                    intent.putExtra("pileId", mPileId);
                    intent.putExtra("projectId", mProjectId);
                    startActivity(intent);
                } else if (mButtonEvent == BUTTON_EVENT_REPORT) {
                    showToast("查看工作报告");
                    Intent intent = new Intent(PileDetailActivity.this, WorkReportActivity.class);
                    intent.putExtra("pileId", mPileId);
                    startActivity(intent);
                } else if (mButtonEvent == BUTTON_EVENT_CONTINUE) {
                    //get no finish status
                    if (constructionState ==1) {//未施工
                        //跳转到标定界面
                        Intent intent = new Intent(PileDetailActivity.this, CalibrationActivity.class);
                        intent.putExtra("pileId", pileId);
                        startActivity(intent);
                    } else if(constructionState==0) {//施工中
                        //跳转到开始灌注
                        Intent intent = new Intent(PileDetailActivity.this, FillingActivity.class);
                        intent.putExtra("pileId", pileId);
                        startActivity(intent);
                    }
                }else if(mButtonEvent==BUTTON_EVENT_REPORT2){//显示新建工作单，但其实有灌注中的桩，是不可以做后续操作的
                    if(mNotificationDialog==null) {
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
                    }
                    if (mNotificationDialog != null && !mNotificationDialog.isAdded()) {
                        if(noFinishPile!=null&&!noFinishPile.equals("")) {
                            mNotificationDialog.setMessage("本设备有"+noFinishPile+"桩未灌注完成,请先结束灌注！");
                            mNotificationDialog.show(getSupportFragmentManager(), "Notification");
                        }
                    }
                }
            }
        });

        mProgressDialog = new WaitProgressDialog(this);
        mPileId = getIntent().getStringExtra("pileId");
        mProjectId = getIntent().getStringExtra("projectId");
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
      //  showToast("Pile ID: " + mPileId);
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
//        doViewPileInfo(mSpUtils.getKeyLoginToken(),
//                mSpUtils.getKeyLoginUserId(),
//                mPileId,
//                mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "++onResume++");
        doViewPileInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(),
                mPileId,
                mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    private class DoViewPileInfoCallback extends StringCallback {

        @Override
        public void onBefore(Request request, int id) {
            super.onBefore(request, id);
        }

        @Override
        public void onAfter(int id) {
            super.onAfter(id);
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            ViewPileInfo info = gson.fromJson(response, ViewPileInfo.class);
            pileId=info.data.pile.pileId;
            mSystemNumberTextView.setText(info.data.pile.systemNumber);
            mPileNumberTextView.setText(info.data.pile.pileNumber);
            mPileTypeTextView.setText(info.data.pile.pileTypeName);
            mCoordinatexTextView.setText(info.data.pile.coordinatex);
            mCoordinateyTextView.setText(info.data.pile.coordinatey);
            mConstructionStateTextView.setText(info.data.pile.constructionStateName);
            mPileDiameterTextView.setText(info.data.pile.pileDiameter);
            mPileLengthTextView.setText(info.data.pile.pileLength);
            mConGradeTextView.setText(info.data.pile.conGrade);
            mFillEndTimeTextView.setText(info.data.pile.fillEndTime);

            constructionState = Integer.valueOf(info.data.pile.constructionState);
            noFinishPile=info.data.pile.noFinishPile;
            mHasReport = Integer.valueOf(info.data.pile.isHasMasterDeviceRep);
            Log.d(TAG, "has report = " + mHasReport);
            if (mHasReport == Utils.MASTER_HAS_REPORT_CHECK_REPORT1) {
                //goto report
                mNewTaskButton.setVisibility(View.VISIBLE);
                if ((constructionState == 2) || (constructionState == 3)) {
                    mNewTaskButton.setText("查看工作报告");
                    mButtonEvent = BUTTON_EVENT_REPORT;
                } else if (constructionState == 0||constructionState == 1) {
                    mNewTaskButton.setText("继续");
                    mButtonEvent = BUTTON_EVENT_CONTINUE;
                }
            } else if (mHasReport == Utils.MASTER_HAS_REPORT_NEW_TASK) {
                //goto new task
                mNewTaskButton.setVisibility(View.VISIBLE);
                mNewTaskButton.setText("新建任务单");
                mButtonEvent = BUTTON_EVENT_NEW_TASK;
            } else if (mHasReport == Utils.MASTER_HAS_REPORT_CHECK_REPORT2) {
                mNewTaskButton.setVisibility(View.VISIBLE);
                mNewTaskButton.setText("新建任务单");
                mButtonEvent = BUTTON_EVENT_REPORT2;
            }

        }
    }

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

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
