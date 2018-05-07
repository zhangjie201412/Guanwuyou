package com.iot.zhs.guanwuyou.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.youngkaaa.yviewpager.YFragmentPagerAdapter;
import cn.youngkaaa.yviewpager.YViewPager;
import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 1/21/2018.
 */

public class StatisticsFragment extends Fragment {

    private YViewPager mPager;

    private FirstFragment mFirstFragment;
    private SecondFragment mSecondFragment;
    private int mCurIndex = 0;
    private TextView mSwitchTextView;
    private ImageView mSwitchImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        mPager = view.findViewById(R.id.pager);

        mFirstFragment = new FirstFragment();
        mSecondFragment = new SecondFragment();
        mPager.setAdapter(new FragmentAdapter(getFragmentManager()));

        mSwitchTextView = view.findViewById(R.id.tv_switch);
        mSwitchImageView = view.findViewById(R.id.iv_switch);
        mSwitchTextView.setText("查看各月检测完成情况");
        mSwitchImageView.setImageResource(R.mipmap.ic_arrow_down);
        LinearLayout linearLayout = view.findViewById(R.id.layout_bottom);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPager.getCurrentItem() == 0) {
                    mSwitchTextView.setText("回到顶部");
                    mSwitchImageView.setImageResource(R.mipmap.ic_arrow_up);
                    mPager.setCurrentItem(1);
                } else {
                    mSwitchTextView.setText("查看各月检测完成情况");
                    mSwitchImageView.setImageResource(R.mipmap.ic_arrow_down);
                    mPager.setCurrentItem(0);
                }

            }
        });

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private class FragmentAdapter extends YFragmentPagerAdapter {

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if(i == 0) {
                return mFirstFragment;
            } else if(i == 1) {
                return mSecondFragment;
            } else {
                return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
