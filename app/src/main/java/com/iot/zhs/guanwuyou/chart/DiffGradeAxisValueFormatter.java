package com.iot.zhs.guanwuyou.chart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.List;

/**
 * Created by Administrator on 2018/3/11.
 */

public class DiffGradeAxisValueFormatter implements IAxisValueFormatter {

    private List<String> mXValues;

    public void setXValues(List<String> values) {
        mXValues = values;
    }
    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mXValues.get((int)value);
    }
}
