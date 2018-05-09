package com.iot.zhs.guanwuyou.utils;

/**
 * Created by star on 2017/4/25.
 */

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;

/**
 * 自定义的ScrollView，解决了onTouchEvent冲突的问题
 * <p/>
 * Created by star on 2016/1/6.
 */
public class OnTouchScrollView extends ScrollView {
    private float downY = 0;
    private float upY = 0;
    private Boolean flag = false;

    public OnTouchScrollView(Context context) {
        super(context);
        this.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    public OnTouchScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOverScrollMode(View.OVER_SCROLL_NEVER);

    }

    public OnTouchScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {//这个方法如果 true 则整个Activity 的 onTouchEvent() 不会被系统回调
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                upY = event.getY();
                //相等为false
                flag = compareFloat();
                if (!flag) {
                    Activity activity = (Activity) getContext();
                    //收键盘
                    InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE); //初始化InputMethodManager类
                    if (activity.getCurrentFocus() != null
                            && activity.getCurrentFocus().getWindowToken() != null) {
                        manager.hideSoftInputFromWindow(activity.getCurrentFocus()
                                .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 比较y轴的起始点和终点的差距，判断是否是在同一个点上，允许200的误差
     * @return
     */
    private Boolean compareFloat() {
        //不相等
        if (Math.abs(upY - downY) > 200) {
            return true;
        } else {//相等  拦截设置false
            return false;
        }
    }
}
