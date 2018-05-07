package com.iot.zhs.guanwuyou;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.PileMapInfo;
import com.iot.zhs.guanwuyou.utils.PileInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by H151136 on 1/18/2018.
 */

public class PileMapListActivity extends AppCompatActivity {
    private static final String TAG = "ZHS.IOT";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private Toast mToast;
    private ListView mListView;
    private PileMapInfo mPileMapInfo;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;

    private List<PileInfo> mPileInfoList;
    private PileInfpAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pile_map_list);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();
        mPileInfoList = new ArrayList<>();
        mAdapter = new PileInfpAdapter(this, R.layout.item_pile_info, mPileInfoList);
        mListView = findViewById(R.id.lv_pile_map);
        mListView.setAdapter(mAdapter);
        queryPileMap(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserName(),
                mSpUtils.getKeyLoginProjectId(),
                mSpUtils.getKeyLoginiMasterDeviceSn());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PileInfo info = mPileInfoList.get(i);
                //send info to next activity
                Intent intent = new Intent(PileMapListActivity.this, PileDetailActivity.class);
                intent.putExtra("pileId", info.pileId);
                intent.putExtra("projectId", info.projectId);
                startActivity(intent);
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
                            String data = object.getString("data");
                            String message = object.getString("message");
                            Log.d(TAG, "message: " + message);
                            Gson gson = new Gson();
                            mPileMapInfo = gson.fromJson(data, PileMapInfo.class);
                            Log.d(TAG, "max x = " + mPileMapInfo.coRange.maxCoordinatex);
                            Log.d(TAG, "max y = " + mPileMapInfo.coRange.maxCoordinatey);
                            Log.d(TAG, "map size = " + mPileMapInfo.pileMap.size());
                            for (PileMapInfo.PileMap map : mPileMapInfo.pileMap) {
////                                Log.d(TAG, "constructionState: " + map.constructionState);
////                                Log.d(TAG, "coordinatex: " + map.coordinatex);
////                                Log.d(TAG, "coordinatey: " + map.coordinatey);
                                Log.d(TAG, "pileId: " + map.pileId);
                                Log.d(TAG, "pileNumber: " + map.pileNumber);
                                Log.d(TAG, "projectId: " + map.projectId);
//                                Log.d(TAG, "state: " + map.state);
//                                Log.d(TAG, "systemNumber: " + map.systemNumber);
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
                            return message;
                        } else {
                            Log.e(TAG, "Failed to parse response");
                        }
                        return null;

                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.e(TAG, "onError!!");
                        e.printStackTrace();
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onResponse(Object response, int id) {
                        mAdapter.notifyDataSetChanged();
                        showToast((String)response);
                    }
                });
    }

    private class PileInfpAdapter extends ArrayAdapter<PileInfo> {
        private int resId;
        public PileInfpAdapter(Context context, int id, List<PileInfo> objs) {
            super(context, id, objs);
            resId = id;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            PileInfo info = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(resId, null);
            TextView pileId = view.findViewById(R.id.tv_pile_id);
            TextView pileNumber = view.findViewById(R.id.tv_pile_number);
            TextView projectId = view.findViewById(R.id.tv_project_id);

            pileId.setText(info.pileId);
            pileNumber.setText(info.pileNumber);
            projectId.setText(info.projectId);

            return view;
        }
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
