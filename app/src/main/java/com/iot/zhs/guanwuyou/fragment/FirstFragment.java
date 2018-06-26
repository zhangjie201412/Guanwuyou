package com.iot.zhs.guanwuyou.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.PileListActivity;
import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.chart.DiffGradeAxisValueFormatter;
import com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo;
import com.iot.zhs.guanwuyou.utils.MyAxisValueFormatter;
import com.iot.zhs.guanwuyou.utils.MyPercentFormatter;
import com.iot.zhs.guanwuyou.utils.MyValueFormatter;
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
    private TextView mBarXNameTv;
    private int visibleXMax = 6;
    private com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo info;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        myApplication = MyApplication.getInstance();
        mSpUtils = myApplication.getSpUtils();

        mBarXNameTv=view.findViewById(R.id.bar_x_name_tv);

        mPieChart = view.findViewById(R.id.chart_pie);
        mPieChart.setUsePercentValues(true);//设置value是否用显示百分数,默认为false
        mPieChart.getDescription().setEnabled(false);//是否设置设置描述
        mPieChart.setExtraOffsets(0, 0, 0, 0);//设置饼状图距离上下左右的偏移量
        mPieChart.setDrawSliceText(false);//设置隐藏饼图上文字，只显示百分比
        mPieChart.setDragDecelerationFrictionCoef(0.95f);//设置阻尼系数,范围在[0,1]之间,越小饼状图转动越困难
        mPieChart.setNoDataText("暂无数据");
        mPieChart.setNoDataTextColor(Color.parseColor("#4e585c"));

        mPieChart.setDrawHoleEnabled(false);//是否绘制饼状图中间的圆
        mPieChart.setTransparentCircleColor(Color.WHITE);//设置圆环的颜色
        mPieChart.setTransparentCircleAlpha(110);//设置圆环的透明度[0,255]
        mPieChart.setTransparentCircleRadius(55f);//设置圆环的半径值

        mPieChart.setRotationEnabled(false);///设置饼状图是否可以旋转(默认为true)
        mPieChart.setRotationAngle(0);//设置饼状图旋转的角度
        mPieChart.setRotationAngle(0);

        mPieChart.setHighlightPerTapEnabled(true);//设置旋转的时候点中的tab是否高亮(默认为true)
        mPieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);//设置动画
        mPieChart.getLegend().setEnabled(true);//图例显示;

        mPieChart.setEntryLabelColor(Color.WHITE);//设置绘制Label的颜色
        mPieChart.setEntryLabelTextSize(18f);//设置绘制Label的字体大小

        mPieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index= (int) h.getX();
                Intent intent=new Intent(FirstFragment.this.getActivity(),PileListActivity.class);
                intent.putExtra("index",index+"");
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });


        // enable rotation of the chart by touch
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);
        Legend l = mPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setXEntrySpace(0f);
        l.setYEntrySpace(0f);
        l.setTextSize(14f);
        l.setTextColor(Color.parseColor("#696969"));
        l.setYOffset(0f);
        l.setXOffset(0f);

        /*------------------柱状图----------------------*/
        mBarChart = view.findViewById(R.id.chart_bar);
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setPinchZoom(false);
        mBarChart.setDoubleTapToZoomEnabled(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setDrawBarShadow(false);
        mBarChart.setDrawValueAboveBar(true);//数值位于柱状图上
        //mBarChart.setHighlightFullBarEnabled(false);
        //mBarChart.setHighlightPerDragEnabled(false);
        //mBarChart.setHighlightPerTapEnabled(false);
        mBarChart.setTouchEnabled(true);
        mBarChart.setExtraOffsets(0, 0, 0, 0);//设置饼状图距离上下左右的偏移量
        mBarChart.setNoDataText("暂无数据");
        mBarChart.setNoDataTextColor(Color.parseColor("#4e585c"));
        mBarChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener(){

            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int index= (int) e.getX();

                Intent intent=new Intent(FirstFragment.this.getActivity(), PileListActivity.class);
                intent.putExtra("diffGrade",info.data.diffGradeList.get(index).diffGrade);//差异等级
                startActivity(intent);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        // y轴
        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setValueFormatter(new MyAxisValueFormatter());//y周的标注
        leftAxis.setAxisMinimum(0); //setStartAtZero(true) 从0开始
        leftAxis.setTextColor(Color.parseColor("#696969"));
        leftAxis.setTextSize(14f);
        leftAxis.setDrawGridLines(true);
        // leftAxis.setLabelCount(5, true); // force 6 labels
        mBarChart.getAxisRight().setEnabled(false);//右侧y轴没有
        mBarChart.getLegend().setEnabled(true);

        Legend ll = mBarChart.getLegend();
        ll.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        ll.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        ll.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        ll.setDrawInside(false);
        ll.setForm(Legend.LegendForm.NONE);
        ll.setTextSize(18);
        ll.setXEntrySpace(0f);
        ll.setTextColor(Color.parseColor("#696969"));

        doSelectProgressAndDiffGradeInfo(mSpUtils.getKeyLoginToken(),
                mSpUtils.getKeyLoginUserId(),
                mSpUtils.getKeyLoginProjectId());
        return view;
    }


    private void setBarData(final List<com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo.Data.DiffGrade> diffs) {
        if (Utils.listIsEmpty(diffs)) {
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
            if (diffs.size() <= visibleXMax) {
                xLabels.setLabelCount(diffs.size());//设置x轴显示的标签个数
            }
            xLabels.setValueFormatter(new IAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    int i = (int) value;
                    //  LogUtil.i("aa","回调value="+value+",i="+i);
                    String result = "";
                    if (!Utils.listIsEmpty(diffs)) {
                        result = diffs.get(i).diffGrade;
                    }
                    return result;
                }
            });

            //x轴数据
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < diffs.size(); i++) {
                xVals.add(i, diffs.get(i).diffGrade);
            }

            //y轴数据
            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
            for (int i = 0; i < diffs.size(); i++) {
                com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo.Data.DiffGrade DiffGrade = diffs.get(i);
                float val1 = Utils.stringToFloat(DiffGrade.pileSumNum);//非检测完成桩数
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
                set1.setColors(Color.parseColor("#B143FE"));//橘色  黄色
                set1.setStackLabels(new String[]{"检测完成数量"});//图例名称

                ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
                dataSets.add(set1);

                //图上显示的数据
                BarData data = new BarData(dataSets);
                data.setValueFormatter(new MyValueFormatter());
                data.setValueTextColor(Color.parseColor("#A0A9FF"));
                data.setValueTextSize(18f);
                data.setBarWidth(0.3f);
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

    private class SelectProgressAndDiffGradeInfo extends StringCallback {

        @Override
        public void onError(Call call, Exception e, int id) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(String response, int id) {
            Log.d(TAG, response);
            Gson gson = new Gson();
            info = gson.fromJson(response, com.iot.zhs.guanwuyou.comm.http.SelectProgressAndDiffGradeInfo.class);
            //draw the chart
            int calFinish = Utils.stringToInt(info.data.pileProgress.calFinish);
            int constructing = Utils.stringToInt(info.data.pileProgress.constructing);
            int otherFinish = Utils.stringToInt(info.data.pileProgress.otherFinish);
            int unconstruct =Utils.stringToInt(info.data.pileProgress.unconstruct);
            setData(calFinish , constructing , otherFinish , unconstruct );
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

    private void setData(int calFinish, int constructing, int otherFinish, int unconstruct) {
        if (calFinish == 0 && constructing == 0 && otherFinish == 0 && unconstruct == 0) {
            mPieChart.setData(null);
        }else {
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(constructing, "灌注中桩数 "+constructing+"根", null));
            entries.add(new PieEntry(unconstruct, "未施工桩数 "+unconstruct+"根", null));
            entries.add(new PieEntry(calFinish, "检测完成桩数 "+calFinish+"根", null));
            entries.add(new PieEntry(otherFinish, "非检测完成桩数 "+otherFinish+"根", null));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setDrawIcons(false);
            dataSet.setSliceSpace(1f);
//        dataSet.setIconsOffset(new MPPointF(0, 40));
            dataSet.setSelectionShift(5f);
            ArrayList<Integer> colors = new ArrayList<Integer>();
            colors.add(Color.parseColor("#6369d8"));
            colors.add(Color.parseColor("#ed6663"));
            colors.add(Color.parseColor("#0fd2ae"));
            colors.add(Color.parseColor("#fdd100"));

            dataSet.setColors(colors);
            //dataSet.setSelectionShift(0f);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new MyPercentFormatter());
            data.setValueTextSize(18f);
            data.setValueTextColor(Color.WHITE);
            mPieChart.setData(data);

            // undo all highlights
            mPieChart.highlightValues(null);
        }
        mPieChart.invalidate();
    }
}
