package com.iot.zhs.guanwuyou.utils;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditInputFilter implements InputFilter {
    /**
     * 最大数字
     */
    public static final int MAX_VALUE = 10000;
    /**
     * 小数点后的数字的位数
     */
    public  int PONTINT_LENGTH = 2;
    Pattern p;



    public EditInputFilter(  int  point ) {
        this.PONTINT_LENGTH=point;
        p = Pattern.compile("[0-9]*");   //除数字外的其他的

    }


    /**
     * source    新输入的字符串
     * start    新输入的字符串起始下标，一般为0
     * end    新输入的字符串终点下标，一般为source长度-1
     * dest    输入之前文本框内容
     * dstart    原内容起始坐标，一般为0
     * dend    原内容终点坐标，一般为dest长度-1
     */

    @Override
    public CharSequence filter(CharSequence src, int start, int end,
                               Spanned dest, int dstart, int dend) {
        String oldtext = dest.toString();

        //验证删除等按键
        if ("".equals(src.toString())) {
            /*int position=oldtext.indexOf(".");
            //有小数点
            if(position>=0){
                if(position==dstart){

                    editText.setText(oldtext.substring(0,position));
                    editText.setSelection(position);
                    /*if(type==0){
                        collectionCluase.setCollectionAmount(oldtext.substring(0,position));
                    }else if (type==1){
                        collectionCluase.setCollectionPercent(oldtext.substring(0,position));
                    }*/
                  //  return null;
               // }

            return null;
        }
        //验证非数字或者小数点的情况
        Matcher m = p.matcher(src);
        if (oldtext.contains(".")) {
            //已经存在小数点的情况下，只能输入数字
            if (!m.matches()) {
                return null;
            }
        } else {
            //未输入小数点的情况下，可以输入小数点和数字
            if (!m.matches() && !src.equals(".")) {
                return null;
            }
        }



        //验证小数位精度是否正确
        if (oldtext.contains(".")) {
            int index = oldtext.indexOf(".");
            int len = dend - index;
            String []str=oldtext.split("\\.");


            //小数位只能2位
            if (len > PONTINT_LENGTH) {
                CharSequence newText = dest.subSequence(dstart, dend);
                return newText;
            }
            //有小数点，光标移到整数部分，大于10位不让输
            if(index>=10){
                if(dstart<=index){
                    return "";
                }
            }

        }else {
            //输入整数，光标移动到中间，小数点后大于3位不让输
            if(src.equals(".")){
                if((oldtext.length()-1-dend>=2)){
                    return "";
                }
            }
        }

        if (dest.length() == 0 && src.equals(".")) {
            return "0.";
        }


        //只能为1-10位数字加上两位小数！
        String dValue = dest.toString();
            //限制10 位  9
           if(!dValue.equals("")&&!dValue.contains(".")&&dValue.length()>9){
               if(!src.equals(".")){
                   src="";
               }
           }else{
               //限制13 为12
               if(!dValue.equals("")&&dValue.contains(".")&&dValue.length()>12){
                    src="";
               }
           }
        return dest.subSequence(dstart, dend) + src.toString();
    }
}