package com.iot.zhs.guanwuyou.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

/**
 * Created by star on 2018/5/10.
 */

public class MyAxisValueFormatter implements IAxisValueFormatter
{

    private DecimalFormat mFormat;

    public MyAxisValueFormatter() {
        mFormat = new DecimalFormat("#############0.0");
        // mFormat = new DecimalFormat("######");

    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
       /* int a= (int) value;
        Log.i("aa","y----value====="+value);
        if(a==value){
            return a+ "根";  //mFormat.format(value)
        }
        return "";*/
        return mFormat.format(value)+"";
    }
}