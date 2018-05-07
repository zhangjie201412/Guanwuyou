package com.iot.zhs.guanwuyou.view;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iot.zhs.guanwuyou.R;

/**
 * Created by H151136 on 2/25/2018.
 */

public class DeviceListDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_device_list, container, false);

        return view;
    }
}
