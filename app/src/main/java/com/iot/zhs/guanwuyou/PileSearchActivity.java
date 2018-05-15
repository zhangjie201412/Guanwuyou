package com.iot.zhs.guanwuyou;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.PileNumInfo;
import com.iot.zhs.guanwuyou.comm.http.SelectPileOfAppInfo;
import com.iot.zhs.guanwuyou.utils.GsonUtils;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import okhttp3.Call;
import okhttp3.Request;
import com.iot.zhs.guanwuyou.comm.http.PileNumInfo.Data.Page.ShowModel;

/**
 * Created by H151136 on 1/23/2018.
 */

public class PileSearchActivity extends AppCompatActivity implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private static final String TAG = "ZHS.IOT";
    private Toolbar mToolbar;
    private EditText mSearchView;
    private BGARefreshLayout bgaRefreshLayout;

    private WaitProgressDialog mProgressDialog;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;

    private final Lock mLock = new ReentrantLock();
    private final Condition mCond = mLock.newCondition();
    private boolean mIsLast = false;
    private List<ShowModel> showModelList = new ArrayList<>();
    ;
    private PileInfoAdapter mAdapter;
    private ListView mListView;
    private ImageView mBackImageView;

    private int curPage = 1;
    private int totalPageNum;
    private Toast mToast;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pile_search);
        mToolbar = findViewById(R.id.tb_top);
        setSupportActionBar(mToolbar);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mSearchView = findViewById(R.id.sv_search);
        mListView = findViewById(R.id.lv_sysnumber);
        mBackImageView = findViewById(R.id.iv_back);
        bgaRefreshLayout = findViewById(R.id.refreshLayout);
        bgaRefreshLayout.setDelegate(this);
        bgaRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(PileSearchActivity.this, true));

        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mProgressDialog = new WaitProgressDialog(this);
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ShowModel showModel = showModelList.get(i);

                Intent intent = new Intent();
                intent.putExtra("systemNumber", showModel.id);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mListView.setTextFilterEnabled(true);
        mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Utils.hideSoftKeyboard(PileSearchActivity.this);
                    curPage = 1;
                    showModelList.clear();
                    doSelectSystemNumberInfo();
                    return true;
                }
                return false;
            }
        });
    }

    private void setListAdapter() {
        if(mAdapter==null) {
            mAdapter = new PileInfoAdapter(this, showModelList);
            mListView.setAdapter(mAdapter);
        }else{
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        curPage = 1;
        showModelList.clear();
        doSelectSystemNumberInfo();
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        curPage = 1;
        showModelList.clear();
        doSelectSystemNumberInfo();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        curPage++;
        if (curPage <= totalPageNum) {
            doSelectSystemNumberInfo();

        } else {
            bgaRefreshLayout.endLoadingMore();
            showToast("无更多数据");
            return false;
        }
        return true;
    }

    private class DoSelectSystemNumberInfoCallback extends StringCallback {

        @Override
        public void onBefore(Request request, int id) {
            super.onBefore(request, id);
            Log.d(TAG, "onBefore");
        }

        @Override
        public void onAfter(int id) {
            super.onAfter(id);
            Log.d(TAG, "onAfter");
        }

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
            mProgressDialog.dismiss();
            bgaRefreshLayout.endLoadingMore();
            bgaRefreshLayout.endRefreshing();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            PileNumInfo info = gson.fromJson(response, PileNumInfo.class);
            if (info.data.page.list.size() != 0) {
                showModelList.addAll(info.data.page.list);
            }
            totalPageNum = info.data.page.allPage;
            setListAdapter();
            mProgressDialog.dismiss();
            bgaRefreshLayout.endLoadingMore();
            bgaRefreshLayout.endRefreshing();
        }
    }


    private void doSelectSystemNumberInfo() {
        Map<String,String> jsonMap=new HashMap<>();
        jsonMap.put("projectId", mSpUtils.getKeyLoginProjectId());
        jsonMap.put("pileNumber", mSearchView.getText().toString().trim());

        String token = mSpUtils.getKeyLoginToken();
        String userName = mSpUtils.getKeyLoginUserId();

        String url = Utils.SERVER_ADDR + "/pile/doSearchPileNumberInfo/cc/" + token + "/" + userName;
        mProgressDialog.show();
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", GsonUtils.objectToString(jsonMap))
                .addParams("pageNo", "" + curPage)
                .addParams("pageSize", "20")
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new DoSelectSystemNumberInfoCallback());
    }


    private class PileInfoAdapter extends BaseAdapter   {
        private LayoutInflater mInflater;
        private List<ShowModel> mDatas;
        public PileInfoAdapter(Context context, List<ShowModel> datas) {
            mInflater = LayoutInflater.from(context);
            mDatas = datas;
        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int i) {
            return mDatas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                view = mInflater.inflate(R.layout.item_pile_num_list, viewGroup, false);
                holder = new ViewHolder();

                holder.pileNumTv = view.findViewById(R.id.pile_num_tv);

                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.pileNumTv.setText(mDatas.get(i).showName);
            return view;
        }
        private class ViewHolder {
            TextView pileNumTv;

        }
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
