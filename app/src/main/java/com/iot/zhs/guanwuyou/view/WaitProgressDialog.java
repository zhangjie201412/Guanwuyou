package com.iot.zhs.guanwuyou.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.iot.zhs.guanwuyou.R;

/**
 * Created by H151136 on 1/21/2018.
 */

public class WaitProgressDialog extends ProgressDialog
{
    public WaitProgressDialog(Context context)
    {
        super(context, R.style.CustomDialog);
    }

    public WaitProgressDialog(Context context, int theme)
    {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        init(getContext());
    }

    private void init(Context context)
    {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_progress);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }

    @Override
    public void show()
    {
        super.show();
    }
}