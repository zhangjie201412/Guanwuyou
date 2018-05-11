package com.iot.zhs.guanwuyou.utils;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Created by star on 2018/5/10.
 */

public class MyValueFormatter implements IValueFormatter
{

    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("######0");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return mFormat.format(value) + "";
    }
}