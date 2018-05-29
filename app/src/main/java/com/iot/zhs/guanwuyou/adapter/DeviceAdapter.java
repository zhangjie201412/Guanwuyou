package com.iot.zhs.guanwuyou.adapter;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.comm.http.DeviceModel;
import com.iot.zhs.guanwuyou.database.DeviceVersion;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.item.DeviceItem;
import com.iot.zhs.guanwuyou.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by H151136 on 1/21/2018.
 */

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    public static final int DEVICE_TYPE_SLAVE = 0x00;
    public static final int DEVICE_TYPE_CALIBRATOR = 0x01;
    private Context mContext;

    public DeviceModel.Data.MasterDevice masterDevice;
    public List<DeviceModel.Data.SlaveDevice> slaveDevices = new ArrayList<>();
    private List<DeviceVersion> deviceVersionList=new ArrayList<>();//数据库


    public DeviceAdapter(Context context) {
        mContext = context;
    }

    public void setDeviceVersionList(List<DeviceVersion> deviceVersionList) {
        this.deviceVersionList = deviceVersionList;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_slave_device, parent, false);
        DeviceViewHolder holder = new DeviceViewHolder(v);

        return holder;
    }

    public void setMasterDevice(DeviceModel.Data.MasterDevice masterDevice) {
        this.masterDevice = masterDevice;
    }

    public void setSlaveDevices(List<DeviceModel.Data.SlaveDevice> slaveDevices) {
        this.slaveDevices = slaveDevices;
    }

    private boolean hasInDataBase(String sn){
        if(Utils.stringIsEmpty(sn)){
            return false;
        }
        for(DeviceVersion deviceVersion:deviceVersionList){
            if(deviceVersion.getSerialNumber().equals(sn)){
                if(!Utils.stringIsEmpty(deviceVersion.getVersion())
                        &&!Utils.stringIsEmpty(deviceVersion.getLocalURL())) {//版本号和下载后存储的地址 都有
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, final int position) {
        Float elec;
        DeviceModel.Data.SlaveDevice slaveDevice = null;
        if (position == 0) {
            holder.deviceIocnIv.setImageResource(R.mipmap.ic_device_master);
            holder.deviceTypeTv.setText("[主机]");
            holder.deviceSNTv.setText(masterDevice.masterDeviceSN);
            holder.deviceLastRunTimeTv.setText(masterDevice.lastRunTime);
            holder.deviceLastErrorKeyTv.setText("最后的错误代码");
            holder.deviceLastErrorTv.setText(masterDevice.errCode);
            if(!Utils.stringIsEmpty(masterDevice.deviceVer)) {
                holder.deviceVerTv.setText("V" + masterDevice.deviceVer);
            }else{
                holder.deviceVerTv.setText("");
            }
            holder.deviceEleTv.setText(masterDevice.elcMany + "%");
            elec= Utils.stringToFloat(masterDevice.elcMany);
            if(hasInDataBase(masterDevice.masterDeviceSN)){
                masterDevice.isUpdate="0";
                holder.updateIv.setVisibility(View.VISIBLE);
            }else{
                holder.updateIv.setVisibility(View.INVISIBLE);
                masterDevice.isUpdate="1";
            }
        } else {

            slaveDevice = slaveDevices.get(position - 1);
            switch (slaveDevice.deviceType) {
                case DEVICE_TYPE_CALIBRATOR:
                    holder.deviceIocnIv.setImageResource(R.mipmap.ic_device_calibrator);
                    holder.deviceTypeTv.setText("[标定仪]");
                    break;
                case DEVICE_TYPE_SLAVE:
                    holder.deviceIocnIv.setImageResource(R.mipmap.ic_device_slave);
                    holder.deviceTypeTv.setText("[从机]");
                    break;
                default:
                    break;
            }
            holder.deviceSNTv.setText(slaveDevice.slaveDeviceSN);
            holder.deviceLastRunTimeTv.setText(slaveDevice.lastRunTime);
            holder.deviceLastErrorKeyTv.setText("传感器使用次数");
            holder.deviceLastErrorTv.setText(slaveDevice.runTimes);
            holder.deviceVerTv.setText(slaveDevice.deviceVer);

            holder.deviceEleTv.setText(slaveDevice.elcMany + "%");
            elec= Utils.stringToFloat(slaveDevice.elcMany);

            if(hasInDataBase(slaveDevice.slaveDeviceSN)){
                slaveDevice.isUpdate="0";
                holder.updateIv.setVisibility(View.VISIBLE);
            }else{
                holder.updateIv.setVisibility(View.INVISIBLE);
                slaveDevice.isUpdate="1";
            }
        }


        Bitmap fillBitmap = null;
        if(elec<=20f){
            holder.elecBgIv.setBackground(mContext.getResources().getDrawable(R.mipmap.icon_elec_red));
            fillBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon_elec_red_fill);
            holder.deviceEleTv.setTextColor(Color.parseColor("#ED6663"));
        }else if(elec>20f &&elec<60f){
            holder.elecBgIv.setBackground(mContext.getResources().getDrawable(R.mipmap.icon_elec_yello));
            fillBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon_elec_yello_fill);
            holder.deviceEleTv.setTextColor(Color.parseColor("#FBBC05"));
        } else{
            holder.elecBgIv.setBackground(mContext.getResources().getDrawable(R.mipmap.icon_elec_green));
            fillBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon_elec_green_fill);
            holder.deviceEleTv.setTextColor(Color.parseColor("#6DD1B7"));
        }
        Float payPercent = elec/(100.0f);
        if(Math.round(fillBitmap.getWidth() * payPercent)==0){
            holder. elecForeIv.setVisibility(View.GONE);
        }else{
            Bitmap newbm = Bitmap.createBitmap(fillBitmap, 0, 0, Math.round(fillBitmap.getWidth() * payPercent), fillBitmap.getHeight());
            holder.elecForeIv.setScaleType(ImageView.ScaleType.FIT_START);
            holder.elecForeIv.setImageBitmap(newbm);
            holder.elecForeIv.setVisibility(View.VISIBLE);
        }

        final DeviceModel.Data.SlaveDevice finalSlaveDevice = slaveDevice;
        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(onItemClickListener!=null){
                    onItemClickListener.onItemClick(position,masterDevice, finalSlaveDevice);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (masterDevice != null && !Utils.listIsEmpty(slaveDevices)) {
            count = slaveDevices.size() + 1;
        }
        return count;
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout contentLayout;
        ImageView updateIv;
        ImageView deviceIocnIv;
        TextView deviceTypeTv;
        TextView deviceSNTv;
        TextView deviceVerTv;
        TextView deviceLastRunTimeTv;
        TextView deviceLastErrorKeyTv;
        TextView deviceLastErrorTv;
        ImageView elecBgIv;
        ImageView elecForeIv;
        TextView deviceEleTv;

        public DeviceViewHolder(final View itemView) {
            super(itemView);
            deviceIocnIv = itemView.findViewById(R.id.device_icon_iv);
            deviceTypeTv = itemView.findViewById(R.id.device_type_tv);
            deviceSNTv = itemView.findViewById(R.id.device_sn_tv);
            deviceVerTv = itemView.findViewById(R.id.device_ver_tv);
            deviceLastRunTimeTv = itemView.findViewById(R.id.device_last_run_time_tv);
            deviceLastErrorKeyTv = itemView.findViewById(R.id.device_last_error_key_tv);
            deviceLastErrorTv = itemView.findViewById(R.id.device_last_error_tv);
            elecBgIv = itemView.findViewById(R.id.electric_bg_iv);
            elecForeIv = itemView.findViewById(R.id.electric_fore_iv);
            deviceEleTv = itemView.findViewById(R.id.device_ele_tv);
            contentLayout=itemView.findViewById(R.id.content_layout);
            updateIv=itemView.findViewById(R.id.update_iv);
        }
    }

    public interface OnItemClickListener{
        public void onItemClick(int position, DeviceModel.Data.MasterDevice masterDevice, DeviceModel.Data.SlaveDevice slaveDevice);
    }

    public  OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
