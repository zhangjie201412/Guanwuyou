package com.iot.zhs.guanwuyou.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;

/**
 * Created by star on 2018/5/10.
 */

public class MyPercentFormatter extends PercentFormatter {

    public MyPercentFormatter() {
        super();
    }

    public MyPercentFormatter(DecimalFormat format) {
        super(format);
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        if(mFormat.format(value).equals("0.0")){
            return "";
        }
        return mFormat.format(value) + " %";
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return super.getFormattedValue(value, axis);
    }

    @Override
    public int getDecimalDigits() {
        return super.getDecimalDigits();
    }
}


