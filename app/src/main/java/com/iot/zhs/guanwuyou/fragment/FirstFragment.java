package com.iot.zhs.guanwuyou.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.chart.DiffGradeAxisValueFormatter;
import com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by H151136 on 1/21/2018.
 */

public class FirstFragment extends Fragment {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final String TAG = "ZHS.IOT";
    private MyApplication myApplication;
    private SharedPreferenceUtils mSpUtils;

    private PieChart mPieChart;
    private BarChart mBarChart;
    private DiffGradeAxisValueFormatter mFormatter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();

        mPieChart = view.findViewById(R.id.chart_pie);
        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setDrawHoleEnabled(false);
        mPieChart.setTransparentCircleColor(Color.WHITE);
        mPieChart.setTransparentCircleAlpha(110);

//        mPieChart.setHoleRadius(58f);
//        mPieChart.setTransparentCircleRadius(61f);

        mPieChart.setDrawCenterText(false);

        mPieChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);
        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        mBarChart = view.findViewById(R.id.chart_bar);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setMaxVisibleValueCount(60);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setPinchZoom(false);
        mBarChart.setBackgroundColor(Color.WHITE);

//        IAxisValueFormatter xAxisFormatter = new MonthAxisValueFormatter(mBarChart);
        mFormatter = new DiffGradeAxisValueFormatter();
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setTextSize(18);
        xAxis.setTextColor(Color.parseColor("#696969"));
        xAxis.setLabelCount(12);
//        xAxis.setValueFormatter(xAxisFormatter);

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

//        setBarData(12, 50);
//        setData(12, 50);

        doSelectProgressAndDiffGradeInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(),
                mSpUtils.getKeyLoginProjectId());
        return view;
    }


    private void setBarData(List<com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo.Data.DiffGrade> diffs) {
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        List<String> xvalues = new ArrayList<>();

        for(int i = 0; i < diffs.size(); i++) {
            yVals1.add(new BarEntry(i, Float.valueOf(diffs.get(i).pileSumNum)));
            xvalues.add(diffs.get(i).diffGrade);
        }
        mFormatter.setXValues(xvalues);
        mBarChart.getXAxis().setValueFormatter(mFormatter);

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

    private class SelectProgressAndDiffGradeInfo extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo info = gson.fromJson(response, com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo.class);
            //draw the chart
            float calFinish = Float.valueOf(info.data.pileProgress.calFinish);
            float constructing = Float.valueOf(info.data.pileProgress.constructing);
            float otherFinish = Float.valueOf(info.data.pileProgress.otherFinish);
            float unconstruct = Float.valueOf(info.data.pileProgress.unconstruct);
            float total = calFinish + constructing + otherFinish + unconstruct;
            setData(calFinish * 100f / total,
                    constructing * 100f / total,
                    otherFinish * 100f / total,
                    unconstruct * 100f / total);
            mPieChart.animateY(800, Easing.EasingOption.EaseInOutQuad);
            setBarData(info.data.diffGradeList);
        }
    }

    private void doSelectProgressAndDiffGradeInfo(String token, String loginName, String projectId) {
        String url = Utils.SERVER_ADDR + "/pile/doSelectProgressAndDiffGradeInfo/cc/" + token + "/" + loginName;
        OkHttpUtils.post().url(url)
                .addParams("projectId", projectId)
                .build()
                .execute(
                        new SelectProgressAndDiffGradeInfo()
                );
    }

    private void setData(float calFinish, float constructing, float otherFinish, float unconstruct) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(calFinish, "检测完成", null));
        entries.add(new PieEntry(constructing, "施工中", null));
        entries.add(new PieEntry(otherFinish, "非检测完成", null));
        entries.add(new PieEntry(unconstruct, "未施工", null));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
//        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        colors.add(Color.parseColor("#0fd2ae"));
        colors.add(Color.parseColor("#fcab1f"));
        colors.add(Color.parseColor("#fdd100"));
        colors.add(Color.parseColor("#ed6663"));

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
//        data.setValueTypeface(mTfLight);
        mPieChart.setData(data);

        // undo all highlights
        mPieChart.highlightValues(null);

        mPieChart.invalidate();
    }
}
