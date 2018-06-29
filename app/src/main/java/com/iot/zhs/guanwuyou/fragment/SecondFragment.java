package com.iot.zhs.guanwuyou.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
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
import com.iot.zhs.guanwuyou.NewTaskActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.chart.MonthAxisValueFormatter;
import com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo;
import com.iot.zhs.guanwuyou.utils.MyAxisValueFormatter;
import com.iot.zhs.guanwuyou.utils.MyValueFormatter;
import com.iot.zhs.guanwuyou.utils.SharedPreferenceUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
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
    private TextView mBarXNameTv;
    private AppCompatSpinner mYearSpinner;
    private int visibleXMax = 12;

    private String[] yearArray;

    public static  SecondFragment secondFragment;

    public static SecondFragment getIntance() {
        return secondFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);
        secondFragment=this;
        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();
        mBarChart=view.findViewById(R.id.chart_bar);
        mBarXNameTv=view.findViewById(R.id.bar_x_name_tv);
        mYearSpinner=view.findViewById(R.id.sp_year);
        //年份
        yearArray=getActivity().getResources().getStringArray(R.array.year);
        String currYear=Calendar.getInstance().get(Calendar.YEAR) + "";
        for(int i=0;i<yearArray.length;i++){
            if(yearArray[i].equals(currYear)){
                mYearSpinner.setSelection(i);
                break;
            }
        }

        mYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String aa=mYearSpinner.getSelectedItem().toString();
                doSelectPileFinishedByPeriodInfo(mSpUtils.getKeyLoginToken(),
                        mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginProjectId(), mYearSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBarChart.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        //barChart.setMaxVisibleValueCount(20);
        // scaling can now only be done on x- and y-axis separately
        mBarChart.setPinchZoom(false);
        mBarChart.setDoubleTapToZoomEnabled(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawValueAboveBar(true);//数值位于柱状图上
        mBarChart.setHighlightFullBarEnabled(false);
        mBarChart.setHighlightPerDragEnabled(false);
        mBarChart.setHighlightPerTapEnabled(false);
        mBarChart.setExtraOffsets(0, 0, 0, 0);//设置饼状图距离上下左右的偏移量
        mBarChart.setNoDataText("暂无数据");
        mBarChart.setNoDataTextColor(Color.parseColor("#4E585C"));

        // y轴
        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setValueFormatter(new MyAxisValueFormatter());//y周的标注
        leftAxis.setAxisMinimum(0); //setStartAtZero(true) 从0开始
        leftAxis.setTextColor(Color.parseColor("#696969"));
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextSize(14f);
        mBarChart.getAxisRight().setEnabled(false);//右侧y轴没有

        mBarChart.getLegend().setEnabled(true);

        Legend ll = mBarChart.getLegend();
        ll.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        ll.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        ll.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        ll.setDrawInside(false);
        ll.setForm(Legend.LegendForm.NONE);
        ll.setTextSize(18f);
        ll.setXEntrySpace(0f);
        ll.setTextColor(Color.parseColor("#696969"));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG,"--onStart--");
    }

    public void  doQuery(){
        doSelectPileFinishedByPeriodInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(), mSpUtils.getKeyLoginProjectId(), mYearSpinner.getSelectedItem().toString());
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

    private void setBarData(final List<com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo.Data.PileFinished> finishedList) {
        if (Utils.listIsEmpty(finishedList)) {
            mBarChart.setData(null);
            mBarXNameTv.setVisibility(View.GONE);
        } else {
            mBarXNameTv.setVisibility(View.VISIBLE);
            //x轴
            XAxis xLabels = mBarChart.getXAxis();
            xLabels.setGranularity(1f);
            xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);//x轴的位置
            xLabels.setTextColor(Color.parseColor("#696969"));
            xLabels.setTextSize(14f);
            xLabels.setDrawGridLines(false);
            if (finishedList.size() <= visibleXMax) {
                xLabels.setLabelCount(finishedList.size());//设置x轴显示的标签个数
            }
            xLabels.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    int i = (int) value;
                    //  LogUtil.i("aa","回调value="+value+",i="+i);
                    String result = "";
                    if (!Utils.listIsEmpty(finishedList)) {
                        result = finishedList.get(i).month;
                    }
                    return result;
                }
            });

            //x轴数据
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < finishedList.size(); i++) {
                xVals.add(i, finishedList.get(i).month);
            }

            //y轴数据
            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            for (int i = 0; i < finishedList.size(); i++) {
                com.iot.zhs.guanwuyou.comm.http.SelectPileFinishedByPeriodInfo.Data.PileFinished pileFinished = finishedList.get(i);
                float val1 = Utils.stringToFloat(pileFinished.pileSumNum);//非检测完成桩数
                yVals1.add(new BarEntry(i, new float[]{val1}));
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
                set1.setColors(Color.parseColor("#B143FE"));//柱状图颜色
                set1.setStackLabels(new String[]{"检测完成数量"});//图例名称

                ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                dataSets.add(set1);

                //图上显示的数据
                BarData data = new BarData(dataSets);
                data.setValueFormatter(new MyValueFormatter());
                data.setValueTextColor(Color.parseColor("#A0A9FF"));
                data.setValueTextSize(18f);
                data.setBarWidth(0.4f);
                mBarChart.setData(data);
            }
            mBarChart.setVisibleXRangeMaximum(visibleXMax);
            mBarChart.setFitBars(true);
        }
        mBarChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
