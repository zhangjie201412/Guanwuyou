package com.iot.zhs.guanwuyou;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.LoginUserModel;
import com.iot.zhs.guanwuyou.service.NetworkMonitorService;
import com.iot.zhs.guanwuyou.utils.Constant;
import com.iot.zhs.guanwuyou.utils.DisplayUtil;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.umeng.analytics.MobclickAgent;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Response;

/**
 * Created by H151136 on 12/14/2017.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "ZHS.IOT";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private EditText mUserEditText;
    private EditText mPasswordEditText;
    private Button mLoginButton;
    private CheckBox mRememberCheckBox;
    private TextView versionTv;
    private Toast mToast;
    private WaitProgressDialog mProgressDialog;
    public static LoginActivity loginActivity;

    public static LoginActivity getIntance() {
        return loginActivity;
    }


    private ISerialPort mSerialManager;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mSerialManager = ISerialPort.Stub.asInterface(iBinder);
            try {
                mSerialManager.sendApkVersion();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSerialManager = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //友盟错误统计
        MobclickAgent.setDebugMode(true);
        // SDK在统计Fragment时，需要关闭Activity自带的页面统计，
        // 然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
        MobclickAgent.openActivityDurationTrack(false);

        // 设置为U-APP场景
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL);


        /*int a=0;
        int b=a/0;*/

        loginActivity = this;
        mLoginButton = findViewById(R.id.bt_login);
        mUserEditText = findViewById(R.id.et_username);
        mPasswordEditText = findViewById(R.id.et_password);
        mRememberCheckBox = findViewById(R.id.cb_keep);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUserEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                if (username.isEmpty() || password.isEmpty()) {
                    showToast("请输入正确的用户名和密码");
                    return;
                }

                MyApplication.getInstance().getSpUtils().setKeyUsername(username);
                if (MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn().isEmpty()) {
                    showToast("还未得到master设备号");
                } else {
                    Log.d(TAG, "mamster sn: " + MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn());
                    login(username, password);
                }
            }
        });
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mRememberCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                MyApplication.getInstance().getSpUtils().setKeyKeepAccount(b);
            }
        });
        mRememberCheckBox.setChecked(MyApplication.getInstance().getSpUtils().getKeyKeepAccount());
        if (mRememberCheckBox.isChecked()) {
            mUserEditText.setText(MyApplication.getInstance().getSpUtils().getKeyUsername());
            mPasswordEditText.setText(MyApplication.getInstance().getSpUtils().getKeyPassword());
        }
        mProgressDialog = new WaitProgressDialog(this);


        String md51 = Utils.encrypt("123456");
        Log.d(TAG, "password: " + Uri.encode(md51));
        getWidthHeight();

        Intent intent = new Intent("com.iot.zhs.guanwuyou.service.SerialService");
        intent.setPackage("com.iot.zhs.guanwuyou");
        boolean bound = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bound = " + bound);

        versionTv = findViewById(R.id.version_tv);
        String version = Utils.getAppVersionName(this);
        if (!TextUtils.isEmpty(version)) {
            versionTv.setText("©江苏中海昇物联科技有限公司 版本号: V" + version);
        } else {
            versionTv.setText("©江苏中海昇物联科技有限公司 版本号: V1.0.0");
        }

        Intent service = new Intent(this, NetworkMonitorService.class);
        startService(service);


    }

    @Override
    protected void onStart() {
        super.onStart();
        //SlaveUtils.resetSlave();
    }

    /**
     * 获取屏幕的宽高
     */
    private void getWidthHeight() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Constant.display.widthPixels = dm.widthPixels;//宽度
        Constant.display.heightPixels = dm.heightPixels;//高度
        Constant.display.statusBarHeightPixels = DisplayUtil.getStatusBarHeight(this);//顶部状态栏
        Constant.display.bottomBarHeightPixels = DisplayUtil.getDaoHangHeight(this);//底部导航栏
        Log.d("aa", "宽=" + Constant.display.widthPixels + ",高=" + Constant.display.heightPixels
                + ",顶部状态栏=" + Constant.display.statusBarHeightPixels + ",底部导航栏=" + Constant.display.bottomBarHeightPixels);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);

    }

    private void login(String username, final String password) {
        String encodePassword = Utils.encrypt(Utils.encrypt(password));
        Log.d(TAG, "encode password: " + encodePassword);
        mProgressDialog.show();
        OkHttpUtils.post().url(Utils.SERVER_ADDR + "/login/login/cc/")
                .addParams("mobileNo", username)
                .addParams("password", encodePassword)//14e1b600b1fd579f47433b88e8d85291
                .addParams("masterDeviceSN", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn())
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        if (response.isSuccessful()) {
                            String rsp = response.body().string();
                            Log.d(TAG, "RSP: " + rsp);
                            JSONObject object = new JSONObject(rsp);
                            String clientType = object.getString("clientType");
                            String code = object.getString("code");
                            String message = object.getString("message");
                            if (!code.equals(Utils.MSG_CODE_OK)) {
                                showToast(message);
                            }
                            Log.d(TAG, "clientType: " + clientType);
                            Log.d(TAG, "code: " + code);
                            Log.d(TAG, "message: " + message);

                            if (object.has("token")) {
                                String token = object.getString("token");
                                Log.d(TAG, "token: " + token);
                                MyApplication.getInstance().getSpUtils().setKeyLoginToken(token);
                            }

                            LoginUserModel userModel = null;
                            if (object.has("data")) {
                                String data = object.getString("data");
                                Log.d(TAG, "data: " + data);

                                Gson gson = new Gson();
                                JSONObject userObject = new JSONObject(data);
                                userModel = gson.fromJson(userObject.getString("loginUserModel"), LoginUserModel.class);
                                showToast(message);
                                MyApplication.getInstance().getSpUtils().setKeyPassword(password);
                            }
                            return userModel;
                        } else {
                            Log.e(TAG, "Failed to parse response");
                            return null;
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        showToast("网络连接中，请稍候");
                        e.printStackTrace();
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        LoginUserModel userModel = (LoginUserModel) response;
                        if (userModel != null) {
                            Constant.curUser = userModel;
                            MyApplication.getInstance().getSpUtils().setKeyLoginCompanyId(userModel.companyId);
                            MyApplication.getInstance().getSpUtils().setKeyLoginCompanyName(userModel.companyName);
                            MyApplication.getInstance().getSpUtils().setKeyLoginMasterDeviceSn(userModel.masterDeviceSN);
                            MyApplication.getInstance().getSpUtils().setKeyLoginProjectId(userModel.projectId);
                            MyApplication.getInstance().getSpUtils().setKeyLoginProjectName(userModel.projectName);
                            MyApplication.getInstance().getSpUtils().setKeyLoginUserId(userModel.userId);
                            MyApplication.getInstance().getSpUtils().setKeyLoginUserName(userModel.userName);

                            mProgressDialog.dismiss();

                            try {
                                mSerialManager.setPowerUp();
                                //   mSerialManager.sendApkVersion();
                                mSerialManager.matchList();
                                mSerialManager.requestCalMac();
                                mSerialManager.requestMode();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }


                            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                            startActivity(intent);
                        }
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                });
    }


    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


}
