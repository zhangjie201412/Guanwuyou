package com.iot.zhs.guanwuyou.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.item.DeviceItem;

import java.util.List;

/**
 * Created by H151136 on 1/21/2018.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public static final int DEVICE_TYPE_MASTER = 0x01;
    public static final int DEVICE_TYPE_SLAVE = 0x02;
    public static final int DEVICE_TYPE_CALIBRATOR = 0x03;

    private List<DeviceItem> mDeviceList;
    private Context mContext;

    public DeviceAdapter(List<DeviceItem> list, Context context) {
        mDeviceList = list;
        mContext = context;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_device, parent, false);
        DeviceViewHolder holder = new DeviceViewHolder(v);

        return holder;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        DeviceItem deviceItem = mDeviceList.get(position);
        switch (deviceItem.getDeviceType()) {
            case DEVICE_TYPE_CALIBRATOR:
                holder.deviceType.setImageResource(R.mipmap.ic_device_calibrator);
                break;
            case DEVICE_TYPE_MASTER:
                holder.deviceType.setImageResource(R.mipmap.ic_device_master);
                break;
            case DEVICE_TYPE_SLAVE:
                holder.deviceType.setImageResource(R.mipmap.ic_device_slave);
                break;
            default:
                break;
        }
        holder.deviceSN.setText(deviceItem.getDeviceSN());
        holder.lastTime.setText(deviceItem.getLastTime());
//        holder.codeOrSensorValue.setText(deviceItem.getCodeOrSensorValue());
        holder.batteryText.setText(deviceItem.getBatteryLevel() + "%");
        Log.d("####", "position = " +position);
    }

    @Override
    public int getItemCount() {
        Log.d("ZHS", "size = " + mDeviceList.size());
        return mDeviceList.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView deviceType;
        TextView deviceSN;
        TextView lastTime;
        TextView codeOrSensor;
        TextView codeOrSensorValue;
        ImageView batteryImage;
        TextView batteryText;

        public DeviceViewHolder(final View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            deviceType = itemView.findViewById(R.id.item_iv_device);
            deviceSN = itemView.findViewById(R.id.item_tv_sn);
            lastTime = itemView.findViewById(R.id.item_tv_last_time);
            codeOrSensor = itemView.findViewById(R.id.item_tv_code_or_sensor);
            codeOrSensorValue = itemView.findViewById(R.id.item_tv_error_code);
            batteryImage = itemView.findViewById(R.id.item_iv_battery);
            batteryText = itemView.findViewById(R.id.item_tv_battery);
        }
    }
}
