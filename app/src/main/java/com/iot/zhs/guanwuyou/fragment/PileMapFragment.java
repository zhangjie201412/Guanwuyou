package com.iot.zhs.guanwuyou.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.iot.zhs.guanwuyou.CalibrationActivity;
import com.iot.zhs.guanwuyou.FillingActivity;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.PileDetailActivity;
import com.iot.zhs.guanwuyou.PileListActivity;
import com.iot.zhs.guanwuyou.PileSearchActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.comm.http.PileMapInfo;
import com.iot.zhs.guanwuyou.utils.Constant;
import com.iot.zhs.guanwuyou.utils.GsonUtils;
import com.iot.zhs.guanwuyou.utils.PileInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;
import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;

/**
 * Created by H151136 on 1/21/2018.
 */

public class PileMapFragment extends Fragment {
    private static final String TAG = "ZHS.IOT";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Toast mToast;
    private ImageView mSwitchImageView;
    private WebView mPileMapWebView;
    private ImageView mZoomRestoreImageView;
    private ImageView mZoomInImageView;
    private ImageView mZoomOutImageView;
    private TextView mProjectNameTv;
    private TextView mSearchTv;

    private PileMapInfo mPileMapInfo;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;

    private List<PileInfo> mPileInfoList;
    private boolean mIsQueried = false;
    private WaitProgressDialog mProgressDialog;
    private NotificationDialog mNotificationDialog;
    private String mNoFinishPileNumber;
    private int mNoFinishState;
    private String mNoFinishPileId;

    private String mPoliceUrl = "file:///android_asset/pilemap.html";
    private float mCurScale;
    private float mInitScale;
    private boolean mIsInit = true;
    private boolean mIsReset = false;
    private final static int UPDATE_SYS_NUM_CODE = 106;

    private float minX;
    private float maxX;
    private float minY;
    private float maxY;
    private float avgPileDiameter;

    private int jumpFlag=0;
    private ImageView loginOutIv;
    public static PileMapFragment pileMapFragment;
    public static PileMapFragment getIntance(){
        return pileMapFragment;
    }


    @SuppressLint("HandlerLeak")
    private Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String response = (String) msg.obj;
            Log.d(TAG, "Response: " + response);

            int what = msg.what;
            switch (what) {
                case 0:
                    mPileMapWebView.loadUrl("javascript:alert(pielDataToMap('" + response + "'))");
                    break;
                case 1:
                    mPileMapWebView.loadUrl("javascript:alert(focusPile('" + response + "'))");
                    break;
            }

        }
    };

    public static PileMapFragment newInstance() {

        Bundle args = new Bundle();

        PileMapFragment fragment = new PileMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pile_map, container, false);
        pileMapFragment=this;
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
        mPileInfoList = new ArrayList<>();
        mProjectNameTv = view.findViewById(R.id.tv_project_title);
        mProjectNameTv.setText(mSpUtils.getKeyLoginProjectName());
        mSearchTv = view.findViewById(R.id.search_tv);
        mSearchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PileSearchActivity.class);
                jumpFlag=1;
                startActivityForResult(intent, UPDATE_SYS_NUM_CODE);
            }
        });
        mPileMapWebView = view.findViewById(R.id.wv_pile_map);
        mSwitchImageView = view.findViewById(R.id.iv_switch);
        mSwitchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PileListActivity.class);
                jumpFlag=1;
                startActivity(intent);
            }
        });
        mZoomInImageView = view.findViewById(R.id.iv_zoom_in);
        mZoomOutImageView = view.findViewById(R.id.iv_zoom_out);
        mZoomRestoreImageView = view.findViewById(R.id.iv_zoom_restore);
        mZoomInImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPileMapWebView.zoomOut();
            }
        });

        mZoomOutImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPileMapWebView.zoomIn();
            }
        });
        mZoomRestoreImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsReset = true;
                mPileMapWebView.zoomOut();
            }
        });
        mNotificationDialog = new NotificationDialog();
        mNotificationDialog.init("提醒",
                "继续",
                "结束灌注",
                new NotificationDialog.NotificationDialogListener() {
                    @Override
                    public void onButtonClick(int id) {
                        //响应左边的button
                        if (id == 1) {
                            if (mNoFinishState == 0) {
                                //跳转到标定界面
                                Intent intent = new Intent(getContext(), CalibrationActivity.class);
                                intent.putExtra("pileId", mNoFinishPileId);
                                jumpFlag=1;
                                startActivity(intent);
                            } else if (mNoFinishState == 1) {
                                //跳转到开始灌注
                                Intent intent = new Intent(getContext(), FillingActivity.class);
                                intent.putExtra("pileId", mNoFinishPileId);
                                jumpFlag=1;
                                startActivity(intent);
                            }
                        } else if (id == 2) {
                            //结束灌注
                            Intent intent = new Intent(getContext(), FillingActivity.class);
                            intent.putExtra("pileId", mNoFinishPileId);
                            intent.putExtra("ACTIVITY_BY_PILE_MAP", true);
                            jumpFlag=1;
                            startActivity(intent);
                        }
                    }
                });
        mProgressDialog = new WaitProgressDialog(getContext());
        WebSettings webSettings = mPileMapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mPileMapWebView.setVerticalScrollBarEnabled(false);
        mPileMapWebView.setHorizontalScrollBarEnabled(false);
        mPileMapWebView.addJavascriptInterface(new JsInterface(getActivity()), "AndroidWebView");
        //设置Web视图  //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        mPileMapWebView.setWebViewClient(new PileWebViewClient());
        mPileMapWebView.setWebChromeClient(new PileWebChromeClient());
        //加载需要显示的网页
        mPileMapWebView.setInitialScale(100);

        mInitScale = 1.0f;
        mCurScale = mInitScale;
        jumpFlag=0;
        mPileMapWebView.loadUrl(mPoliceUrl);

        loginOutIv=view.findViewById(R.id.login_out_iv);
        loginOutIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final NotificationDialog loginOutDialog = new NotificationDialog();
                loginOutDialog.init("提醒",
                        "是",
                        "否",
                        new NotificationDialog.NotificationDialogListener() {
                            @Override
                            public void onButtonClick(int id) {
                                //响应左边的button
                                if (id == 1) {
                                    loginOutDialog.dismiss();
                                    PileMapFragment.this.getActivity().finish();
                                } else if (id == 2) {
                                    loginOutDialog.dismiss();
                                }
                            }
                        });
                loginOutDialog.setMessage("是否确认退出登录?");
                loginOutDialog.show(PileMapFragment.this.getActivity().getSupportFragmentManager(), "Notification");
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == UPDATE_SYS_NUM_CODE) {//修改系统编号
                String systemNumber = data.getStringExtra("systemNumber");
                mSearchTv.setText(systemNumber);
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "++onStart++");
        if(jumpFlag==1){
            jumpFlag=0;
            doQuery();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "++onStop++");
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private class JsInterface {
        private Context mContext;

        public JsInterface(Context context) {
            this.mContext = context;
        }

        //在js中调用window.AndroidWebView.showInfoFromJs(name)，便会触发此方法。
        @JavascriptInterface
        public void showInfoFromJs(String name) {
            Log.d(TAG, "showInfoFromJs: " + name);
        }

        @JavascriptInterface
        public void jumpToPileDetail(String jsonStr) {
            Log.d(TAG, "jumpToPileDetail: " + jsonStr);
            Gson gson = new Gson();
            PileMapInfo.PileMap map = gson.fromJson(jsonStr, PileMapInfo.PileMap.class);
            Intent intent = new Intent(getContext(), PileDetailActivity.class);
            intent.putExtra("pileId", map.pileId);
            intent.putExtra("projectId", map.projectId);
            intent.putExtra("noFinishState", mNoFinishState);
            intent.putExtra("noFinishPileId", mNoFinishPileId);
            jumpFlag=1;
            startActivity(intent);
        }

        @JavascriptInterface
        public float getScale() {
            Log.d(TAG, "scale = " + mCurScale);
            return mCurScale;
        }
    }

    //Web视图
    private class PileWebViewClient extends WebViewClient {
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            Log.d(TAG, "oldScale = " + oldScale + ", newScale = " + newScale);

            if (mIsInit) {
                mInitScale = newScale;
            }
            mIsInit = false;

            mCurScale = newScale;
            //重置
            if (mCurScale <= mInitScale) {
                mIsReset = false;
            }
            if (mIsReset) {
                if (mCurScale > mInitScale) {
                    boolean out = mPileMapWebView.zoomOut();
                    if (!out) {
                        mIsReset = false;
                    }
                } else {
                    mIsReset = false;
                }
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d(TAG, "onPageStarted");
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d(TAG, "onPageFinished");
            doQuery();

        }
    }

    private class PileWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            Log.d(TAG, "message: " + message);
//            if (netDialog != null && netDialog.isShowing()) {
//                netDialog.dismiss();
//            }
            result.confirm();
            return true;
        }
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }


    public void doQuery(){
        if (mNotificationDialog.isAdded()) {
            mNotificationDialog.dismiss();
        }
        mNoFinishPileNumber = "";
        mNoFinishState = -1;
        mNoFinishPileId = "";
        mProgressDialog.show();
        doQueryPileMap(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(),
                mSpUtils.getKeyLoginProjectId(),
                mSpUtils.getKeyLoginiMasterDeviceSn());
    }

    private void doQueryPileMap(String token, String userName, String projectId, final String masterSN) {
        Log.d(TAG, "user name = " + userName);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("projectId", projectId);
            jsonObject.put("masterDeviceSN", masterSN);
            jsonObject.put("pileNumber", mSearchTv.getText().toString().trim());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Utils.SERVER_ADDR + "/pile/doSelectPileMapInfo/cc/" + token + "/" + userName;
        Log.d(TAG, "url: " + url);
        Log.d(TAG, "post: " + jsonObject.toString());
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", jsonObject.toString())
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new Callback() {
                    @Override
                    public Object parseNetworkResponse(Response response, int id) throws Exception {
                        if (response.isSuccessful()) {
                            String rsp = response.body().string();
                            Log.d(TAG, rsp);
                            JSONObject object = new JSONObject(rsp);
                            String data = object.getString("data");
                            String message = object.getString("message");
                            String code = object.getString("code");

                            if (code.equals("1")) {

                                Gson gson = new Gson();
                                mPileMapInfo = gson.fromJson(data, PileMapInfo.class);
                                for (PileMapInfo.PileMap map : mPileMapInfo.pileMap) {
                                    PileInfo info = new PileInfo();
                                    info.constructionState = map.constructionState;
                                    info.coordinatex = map.coordinatex;
                                    info.coordinatey = map.coordinatey;
                                    info.pileId = map.pileId;
                                    info.pileNumber = map.pileNumber;
                                    info.projectId = map.projectId;
                                    info.state = map.state;
                                    info.systemNumber = map.systemNumber;
                                    mPileInfoList.add(info);
                                }

                                if (mPileMapInfo.noFinishPile != null) {
                                    Log.d(TAG, "noFinishPile -> reportState: " + mPileMapInfo.noFinishPile.reportState);
                                    Log.d(TAG, "noFinishPile -> pileNumber: " + mPileMapInfo.noFinishPile.pileNumber);
                                    mNoFinishPileNumber = mPileMapInfo.noFinishPile.pileNumber;
                                    mNoFinishState = Integer.valueOf(mPileMapInfo.noFinishPile.reportState);
                                    //show no finish pile notification
                                    mNotificationDialog.setMessage("本设备有" + mPileMapInfo.noFinishPile.pileNumber + "桩未灌注完成,请先结束灌注！");
                                    mNotificationDialog.show(getFragmentManager(), "Notification");

                                    boolean foundNoFinishPileId = false;
                                    String noFinishPileNum = mNoFinishPileNumber; //= Float.valueOf(mNoFinishPileNumber);
                                    for (PileInfo info : mPileInfoList) {
                                        Log.d(TAG, " -> " + info.systemNumber);
                                        if (info.systemNumber.equals(noFinishPileNum)) {
                                            mNoFinishPileId = info.pileId;
                                            foundNoFinishPileId = true;
                                            break;
                                        }
                                    }
                                    if (!foundNoFinishPileId) {
                                        Log.e(TAG, "Cannot find no finish pile id by " + mNoFinishPileNumber);
                                    }

                                } else {
                                    Log.d(TAG, "No no finish pile task");
                                }

                                if (mPileMapInfo.pileMap != null && mPileMapInfo.coRange != null) {
                                    minX = Utils.stringToFloat(mPileMapInfo.coRange.minCoordinatex);
                                    maxX = Utils.stringToFloat(mPileMapInfo.coRange.maxCoordinatex);
                                    minY = Utils.stringToFloat(mPileMapInfo.coRange.minCoordinatey);
                                    maxY = Utils.stringToFloat(mPileMapInfo.coRange.maxCoordinatey);

                                    avgPileDiameter = Utils.stringToFloat(mPileMapInfo.coRange.avgPileDiameter) / 10f;

                                    float sacle = 1;
                                    if (avgPileDiameter != 0) {
                                        float width = Math.abs(maxX - minX) / Constant.display.widthPixels;
                                        float height = Math.abs(maxY - minY) / Constant.display.heightPixels;

                                        sacle = Math.min(width, height);
                                        if(sacle<=0){
                                            sacle=1;
                                        }else {
                                            sacle = sacle / mPileMapInfo.pileMap.size() * avgPileDiameter;
                                        }
                                    }

                                    float screenScale = getActivity().getResources().getDisplayMetrics().density;

                                    for (PileMapInfo.PileMap pileMap : mPileMapInfo.pileMap) {
                                        float x = Utils.stringToFloat(pileMap.coordinatex) / sacle / screenScale;
                                        pileMap.coordinatex = x + "";

                                        float y = Utils.stringToFloat(pileMap.coordinatey) / sacle / screenScale;
                                        pileMap.coordinatey = y + "";
                                    }

                                    float minX = Utils.stringToFloat(mPileMapInfo.coRange.minCoordinatex) / sacle / screenScale;
                                    float maxX = Utils.stringToFloat(mPileMapInfo.coRange.maxCoordinatex) / sacle / screenScale;
                                    float minY = Utils.stringToFloat(mPileMapInfo.coRange.minCoordinatey) / sacle / screenScale;
                                    float maxY = Utils.stringToFloat(mPileMapInfo.coRange.maxCoordinatey) / sacle / screenScale;

                                    mPileMapInfo.coRange.minCoordinatex = minX + "";
                                    mPileMapInfo.coRange.maxCoordinatex = maxX + "";
                                    mPileMapInfo.coRange.minCoordinatey = minY + "";
                                    mPileMapInfo.coRange.maxCoordinatey = maxY + "";

                                    Map<String, Object> dataMap = new HashMap<String, Object>();
                                    Map<String, Object> map = new HashMap<String, Object>();
                                    map.put("coRange", mPileMapInfo.coRange);
                                    map.put("pileMap", mPileMapInfo.pileMap);

                                    dataMap.put("data", map);

                                    String dataMapStr = GsonUtils.objectToString(dataMap);
                                    JsonObject pileObject = GsonUtils.structureGson(dataMapStr);
                                    String pileJsonStr = pileObject.toString();

                                    Message msg = new Message();
                                    msg.obj = pileJsonStr;
                                    msg.what = 0;
                                    mUiHandler.sendMessage(msg);
                                }

                                if(!Utils.stringIsEmpty(mSearchTv.getText().toString().trim())) {
                                    if (mPileMapInfo.searchValue == null) {
                                        showToast("您查找的桩不存在!");
                                    } else {
                                        Message msg = new Message();
                                        msg.obj = mPileMapInfo.searchValue.systemNumber;
                                        msg.what = 1;
                                        mUiHandler.sendMessage(msg);
                                    }
                                }

                                return message;
                            } else {
                                Log.e(TAG, "Failed to parse response");
                            }
                        }
                        return null;
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError!!");
                        mProgressDialog.dismiss();
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        mProgressDialog.dismiss();
                        if (response != null) {
//                            mAdapter.notifyDataSetChanged();
                            showToast((String) response);
                            mIsQueried = true;
                        } else {
                            showToast("查询桩位图失败!");
                        }
                    }
                });
    }


}
