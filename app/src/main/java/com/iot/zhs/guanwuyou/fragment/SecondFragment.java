package com.iot.zhs.guanwuyou.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.chart.MonthAxisValueFormatter;
import com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 1/21/2018.
 */

public class SecondFragment extends Fragment {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private MyApplication myApplication;
    private SharedPreferenceUtils mSpUtils;
    private BarChart mBarChart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();

        mBarChart = view.findViewById(R.id.chart_bar);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setMaxVisibleValueCount(60);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setPinchZoom(false);

        IAxisValueFormatter xAxisFormatter = new MonthAxisValueFormatter(mBarChart);
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setTextSize(18);
        xAxis.setTextColor(Color.parseColor("#696969"));
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setLabelCount(3, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setTextSize(18);
        leftAxis.setTextColor(Color.parseColor("#696969"));
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mBarChart.getAxisLeft();
        rightAxis.setLabelCount(3, false);
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        rightAxis.setTextSize(18);
        rightAxis.setTextColor(Color.parseColor("#696969"));
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend ll = mBarChart.getLegend();
        ll.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        ll.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        ll.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        ll.setDrawInside(false);
        ll.setForm(Legend.LegendForm.SQUARE);
        ll.setFormSize(9f);
        ll.setTextSize(18);
        ll.setXEntrySpace(4f);
        ll.setTextColor(Color.parseColor("#696969"));

        doSelectPileFinishedByPeriodInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginProjectId(), "2017");
        return view;
    }

    private class SelectPileFinishedByPeriodInfo extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo info = gson.fromJson(response,
                    com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo.class);
            if(info.code.equals(Utils.MSG_CODE_OK)) {
                setBarData(info.data.pileFinishedList);
            } else {
                Toast.makeText(getContext(), info.message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void doSelectPileFinishedByPeriodInfo(String token, String loginName, String projectId, String year) {
        String url = Utils.SERVER_ADDR + "/pile/doSelectPileFinishedByPeriodInfo/cc/" + token + "/" + loginName;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("projectId", projectId);
            jsonObject.put("year", year);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpUtils.post().url(url)
                .addParams("jsonStr", jsonObject.toString())
                .build()
                .execute(
                        new SelectPileFinishedByPeriodInfo()
                );
    }

    private void setBarData(List<com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo.Data.PileFinished> finishedList) {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        for(int i = 0; i < finishedList.size(); i++) {
            yVals1.add(new BarEntry(i, Integer.valueOf(finishedList.get(i).pileSumNum)));
        }

        BarDataSet set1;

        if (mBarChart.getData() != null &&
                mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "检测完成数量");
            set1.setDrawIcons(false);

            ArrayList<Integer> colors = new ArrayList<Integer>();

            colors.add(Color.parseColor("#b143f1"));
            set1.setColors(colors);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(18.0f);
            data.setValueTextColor(Color.parseColor("#a0a9ff"));
            data.setBarWidth(0.34f);

            mBarChart.setData(data);
            mBarChart.invalidate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
