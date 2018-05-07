package com.iot.zhs.guanwuyou;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

/**
 * Created by H151136 on 11/24/2016.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "ZHS.IOT";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "ACTION: " + intent.getAction());
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appIntent);
    }
}
