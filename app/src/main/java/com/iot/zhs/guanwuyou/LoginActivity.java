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
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.HttpResponse;
import com.iot.zhs.guanwuyou.comm.http.LoginUserModel;
import com.iot.zhs.guanwuyou.comm.http.PileMapInfo;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
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
    private Toast mToast;
    private WaitProgressDialog mProgressDialog;

    private ISerialPort mSerialManager;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mSerialManager = ISerialPort.Stub.asInterface(iBinder);
            try {
                mSerialManager.setPowerUp();
                mSerialManager.requestCalMac();
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
        mLoginButton = findViewById(R.id.bt_login);
        mUserEditText = findViewById(R.id.et_username);
        mPasswordEditText = findViewById(R.id.et_password);
        mRememberCheckBox = findViewById(R.id.cb_keep);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUserEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                if(username.isEmpty() || password.isEmpty()) {
                    showToast("请输入正确的用户名和密码");
                    return;
                }

                MyApplication.getInstance().getSpUtils().setKeyUsername(username);
                if(MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn().isEmpty()) {
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
        if(mRememberCheckBox.isChecked()) {
            mUserEditText.setText(MyApplication.getInstance().getSpUtils().getKeyUsername());
            mPasswordEditText.setText(MyApplication.getInstance().getSpUtils().getKeyPassword());
        }
        mProgressDialog = new WaitProgressDialog(this);

        Intent intent = new Intent("com.iot.zhs.guanwuyou.service.SerialService");
        intent.setPackage("com.iot.zhs.guanwuyou");
        boolean bound = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "bound = " + bound);

        String md51 = Utils.encrypt("123456");
        Log.d(TAG, "password: " + Uri.encode(md51));
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
//                .addParams("mobileNo", "12345678999")
//                .addParams("mobileNo", "18000000001")
//                .addParams("mobileNo", "13300001111")
//                .addParams("password", md51"14e1b600b1fd579f47433b88e8d85291")
                .addParams("mobileNo", username)
                .addParams("password", encodePassword)
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
                    if(!code.equals(Utils.MSG_CODE_OK)) {
                        showToast(message);
                    }

                    String data = object.getString("data");
                    String token = object.getString("token");
                    Log.d(TAG, "clientType: " + clientType);
                    Log.d(TAG, "code: " + code);
                    Log.d(TAG, "token: " + token);
                    Log.d(TAG, "data: " + data);
                    Log.d(TAG, "message: " + message);
                    Gson gson = new Gson();
                    JSONObject userObject = new JSONObject(data);
                    LoginUserModel userModel = gson.fromJson(userObject.getString("loginUserModel"), LoginUserModel.class);
                    MyApplication.getInstance().getSpUtils().setKeyLoginToken(token);
                    showToast(message);
                    MyApplication.getInstance().getSpUtils().setKeyPassword(password);
                    return userModel;
                } else {
                    Log.e(TAG, "Failed to parse response");
                    return null;
                }
            }

            @Override
            public void onError(Call call, Exception e, int id) {
//                showToast("无法从服务器获取回复!");
                e.printStackTrace();
                if(mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onResponse(Object response, int id) {
                LoginUserModel userModel = (LoginUserModel) response;
                if (userModel != null) {
                    MyApplication.getInstance().getSpUtils().setKeyLoginCompanyId(userModel.companyId);
                    MyApplication.getInstance().getSpUtils().setKeyLoginCompanyName(userModel.companyName);
                    MyApplication.getInstance().getSpUtils().setKeyLoginMasterDeviceSn(userModel.masterDeviceSN);
                    MyApplication.getInstance().getSpUtils().setKeyLoginProjectId(userModel.projectId);
                    MyApplication.getInstance().getSpUtils().setKeyLoginProjectName(userModel.projectName);
                    MyApplication.getInstance().getSpUtils().setKeyLoginUserId(userModel.userId);
                    MyApplication.getInstance().getSpUtils().setKeyLoginUserName(userModel.userName);

                    mProgressDialog.dismiss();
                    try {
                        mSerialManager.matchList();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void queryPileMap(String token, String userName, String projectId, final String masterSN) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("projectId", projectId);
//            jsonObject.put("pileNumber", pileNumber);
            jsonObject.put("masterDeviceSN", masterSN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        String url = Utils.SERVER_ADDR + "/pile/doSelectPileMapInfo/cc/" + token + "/" + userName;
        Log.d(TAG, "url: " + url);
        Log.d(TAG, "post: " + jsonObject.toString());
        OkHttpUtils.post().url(url)
//                .content(jsonObject.toString())
//                .mediaType(JSON)
                .addParams("jsonStr", jsonObject.toString())
                .build()
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        if (response.isSuccessful()) {
//                            Log.d(TAG, response.body().string());
                            JSONObject object = new JSONObject(response.body().string());
                            String clientType = object.getString("clientType");
                            String code = object.getString("code");
                            String data = object.getString("data");
                            String token = object.getString("token");
                            Gson gson = new Gson();
                            JSONObject userObject = new JSONObject(data);
                            PileMapInfo mapInfo = gson.fromJson(data, PileMapInfo.class);
                            Log.d(TAG, "max x = " + mapInfo.coRange.maxCoordinatex);
                            Log.d(TAG, "max y = " + mapInfo.coRange.maxCoordinatey);
                            Log.d(TAG, "map size = " + mapInfo.pileMap.size());
                            for (PileMapInfo.PileMap map : mapInfo.pileMap) {
                                Log.d(TAG, "=====================================");
//                                Log.d(TAG, "constructionState: " + map.constructionState);
//                                Log.d(TAG, "coordinatex: " + map.coordinatex);
//                                Log.d(TAG, "coordinatey: " + map.coordinatey);
                                Log.d(TAG, "pileId: " + map.pileId);
                                Log.d(TAG, "pileNumber: " + map.pileNumber);
                                Log.d(TAG, "projectId: " + map.projectId);
//                                Log.d(TAG, "state: " + map.state);
//                                Log.d(TAG, "systemNumber: " + map.systemNumber);
                                Log.d(TAG, "=====================================");
                            }
                        } else {
                            Log.e(TAG, "Failed to parse response");
                        }
                        return null;

                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError!!");
                    }

                    @Override
                    public void onResponse(Object response, int id) {

                    }
                });
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
