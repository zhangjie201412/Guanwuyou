package com.iot.zhs.guanwuyou;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.comm.http.SelectPileOfAppInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by H151136 on 1/23/2018.
 */

public class PileListActivity extends AppCompatActivity implements BGARefreshLayout.BGARefreshLayoutDelegate {

    private static final String TAG = "ZHS.IOT";
    private Toolbar mToolbar;
    private SearchView mSearchView;
    private TextView mSerarchTextView;
    private BGARefreshLayout bgaRefreshLayout;

    private WaitProgressDialog mProgressDialog;
    private MyApplication mApplication;
    private SharedPreferenceUtils mSpUtils;

    private final Lock mLock = new ReentrantLock();
    private final Condition mCond = mLock.newCondition();
    private boolean mIsLast = false;
    private List<SelectPileOfAppInfo.Data.Page.PileInfo> mPileInfoList = new ArrayList<>();
    ;
    private PileInfoAdapter mAdapter;
    private ListView mListView;

    private ImageView mBackImageView;
    private ImageView mSwitchImageView;
    private ImageView mAdvanceImageView;

    private static final int CMD_START = 0x01;
    private static final int CMD_END = 0x02;
    private static final int CMD_UPDATE = 0x03;

    private int curPage = 1;
    private int totalPageNum;
    private Toast mToast;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pile_list);
        mToolbar = findViewById(R.id.tb_top);
        setSupportActionBar(mToolbar);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mSearchView = findViewById(R.id.sv_search);
        mListView = findViewById(R.id.lv_pile);
        mBackImageView = findViewById(R.id.iv_back);
        mSwitchImageView = findViewById(R.id.iv_switch);
        mAdvanceImageView = findViewById(R.id.iv_advance);
        bgaRefreshLayout = findViewById(R.id.refreshLayout);
        bgaRefreshLayout.setDelegate(this);
        bgaRefreshLayout.setRefreshViewHolder(new BGANormalRefreshViewHolder(PileListActivity.this, true));

        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mSwitchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mAdvanceImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        SpannableString spanText = new SpannableString("请输入桩号/系统编号");
        spanText.setSpan(new AbsoluteSizeSpan(20, true), 0, spanText.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new ForegroundColorSpan(Color.WHITE), 0, spanText.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mSearchView.setQueryHint(spanText);
        mSearchView.setIconifiedByDefault(false);
        spanText.setSpan(new AbsoluteSizeSpan(20, true), 0, spanText.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        spanText.setSpan(new ForegroundColorSpan(Color.WHITE), 0,
                spanText.length(),
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        mSearchView.setQueryHint(spanText);
        mSerarchTextView = mSearchView.findViewById(R.id.search_src_text);
        mSerarchTextView.setTextColor(Color.WHITE);// 设置输入字的显示
        mProgressDialog = new WaitProgressDialog(this);
        mApplication = MyApplication.getInstance();
        mSpUtils = mApplication.getSpUtils();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SelectPileOfAppInfo.Data.Page.PileInfo info = mPileInfoList.get(i);
                //send info to next activity
                Intent intent = new Intent(PileListActivity.this, PileDetailActivity.class);
                intent.putExtra("pileId", info.pileId);
                intent.putExtra("projectId", info.projectId);
                startActivity(intent);
            }
        });
        mListView.setTextFilterEnabled(true);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                curPage = 1;
                mPileInfoList.clear();
                doSelectPileOfAppInfo();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


    }

    private void setListAdapter() {
        mAdapter = new PileInfoAdapter(this, mPileInfoList);
        mListView.setAdapter(mAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        curPage = 1;
        mPileInfoList.clear();
        doSelectPileOfAppInfo();
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        curPage = 1;
        mPileInfoList.clear();
        doSelectPileOfAppInfo();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        curPage++;
        if (curPage <= totalPageNum) {
            doSelectPileOfAppInfo();

        } else {
            bgaRefreshLayout.endLoadingMore();
            showToast("无更多数据");
            return false;
        }
        return true;
    }

    private class DoSelectPileOfAppInfoCallback extends StringCallback {

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
            SelectPileOfAppInfo info = gson.fromJson(response, SelectPileOfAppInfo.class);
            if (info.data.page.list.size() != 0) {
                mPileInfoList.addAll(info.data.page.list);
            }
            totalPageNum = info.data.page.allPage;
            mProgressDialog.dismiss();
            bgaRefreshLayout.endLoadingMore();
            bgaRefreshLayout.endRefreshing();
            setListAdapter();
        }
    }


    private void doSelectPileOfAppInfo() {
        JSONObject json = new JSONObject();
        try {
            json.put("projectId", mSpUtils.getKeyLoginProjectId());
            json.put("pileNumber", mSerarchTextView.getText().toString().trim());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String token = mSpUtils.getKeyLoginToken();
        String userName = mSpUtils.getKeyLoginUserId();

        String url = Utils.SERVER_ADDR + "/pile/doSelectPileOfAppInfo/cc/" + token + "/" + userName;
        mProgressDialog.show();
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", json.toString())
                .addParams("pageNo", "" + curPage)
                .addParams("pageSize", "20")
                .build()
                .connTimeOut(Utils.HTTP_TIMEOUT)
                .readTimeOut(Utils.HTTP_TIMEOUT)
                .writeTimeOut(Utils.HTTP_TIMEOUT)
                .execute(new DoSelectPileOfAppInfoCallback());
    }


    private class PileInfoAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater mInflater;
        private List<SelectPileOfAppInfo.Data.Page.PileInfo> mDatas;
        private List<SelectPileOfAppInfo.Data.Page.PileInfo> mDataBak;
        private MyFilter mFilter;

        public PileInfoAdapter(Context context, List<SelectPileOfAppInfo.Data.Page.PileInfo> datas) {
            mInflater = LayoutInflater.from(context);
            mDatas = datas;
            mDataBak = datas;
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
                view = mInflater.inflate(R.layout.item_pile_list, viewGroup, false);
                holder = new ViewHolder();

                holder.pileId = view.findViewById(R.id.item_pile_id);
                holder.constructionState = view.findViewById(R.id.item_construction_state);
                holder.constructionStateIcon = view.findViewById(R.id.item_construction_state_icon);
                holder.pileLength = view.findViewById(R.id.item_pile_length);
                holder.pileDiameter = view.findViewById(R.id.item_pile_diameter);
                holder.concrete = view.findViewById(R.id.item_concrete);
                holder.pileType = view.findViewById(R.id.item_pile_type);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.pileId.setText(mDatas.get(i).pileNumber);
            holder.constructionState.setText(mDatas.get(i).constructionStateName);
            int state = Integer.valueOf(mDatas.get(i).constructionState);
            if (state == 0) {
                holder.constructionStateIcon.setImageResource(R.mipmap.ic_location_orange);
            } else if (state == 1) {
                holder.constructionStateIcon.setImageResource(R.mipmap.ic_location_red);
            } else if (state == 2) {
                holder.constructionStateIcon.setImageResource(R.mipmap.ic_location_green);
            } else if (state == 3) {
                holder.constructionStateIcon.setImageResource(R.mipmap.ic_location_yellow);
            }
            holder.pileLength.setText("桩长: " + mDatas.get(i).pileLength + "mm");
            holder.pileDiameter.setText("桩径: " + mDatas.get(i).pileDiameter + "mm");

            if (mDatas.get(i).conGrade == null)
                mDatas.get(i).conGrade = "";
            if (mDatas.get(i).pileTypeName == null)
                mDatas.get(i).pileTypeName = "";
            holder.concrete.setText("砼标号: " + mDatas.get(i).conGrade);
            holder.pileType.setText("桩类型: " + mDatas.get(i).pileTypeName);

            return view;
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new MyFilter();
            }

            return mFilter;
        }

        private class ViewHolder {
            TextView pileId;
            TextView constructionState;
            ImageView constructionStateIcon;
            TextView pileLength;
            TextView pileDiameter;
            TextView concrete;
            TextView pileType;
        }

        class MyFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                List<SelectPileOfAppInfo.Data.Page.PileInfo> list;
                if (TextUtils.isEmpty(charSequence)) {
                    list = mDataBak;
                } else {
                    list = new ArrayList<>();
                    for (SelectPileOfAppInfo.Data.Page.PileInfo info : mDataBak) {
                        if (info.pileNumber.contains(charSequence) ||
                                info.systemNumber.contains(charSequence)) {
                            list.add(info);
                        }
                    }
                }

                results.values = list;
                results.count = list.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mDatas = (List<SelectPileOfAppInfo.Data.Page.PileInfo>) filterResults.values;
                if (filterResults.count > 0) {
                    notifyDataSetChanged();//通知数据发生了改变
                } else {
                    notifyDataSetInvalidated();//通知数据失效
                }
            }
        }
    }

    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
