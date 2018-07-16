package com.iot.zhs.guanwuyou.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iot.zhs.guanwuyou.R;
import com.iot.zhs.guanwuyou.database.SlaveDevice;

import java.util.List;

/**
 * Created by H151136 on 2/25/2018.
 */

public class DeviceListAdapter extends BaseAdapter {

    private List<SlaveDevice> mSlaveDeviceList;
    private Context mContext;

    public DeviceListAdapter(Context context, List<SlaveDevice> deviceList) {
        mSlaveDeviceList = deviceList;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mSlaveDeviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return mSlaveDeviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_devicelist, null);
            holder = new ViewHolder();
            holder.title = view.findViewById(R.id.item_tv_title);
            holder.serialNumber = view.findViewById(R.id.item_tv_sn);
            holder.online = view.findViewById(R.id.item_tv_online);
            holder.alarm = view.findViewById(R.id.item_iv_alarm);
            holder.comm = view.findViewById(R.id.item_tv_comm_status);
            holder.battery = view.findViewById(R.id.item_tv_battery);
            holder.commDetail=view.findViewById(R.id.item_tv_comm_status_detail);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        holder.serialNumber.setText(mSlaveDeviceList.get(i).getSerialNumber());
        holder.battery.setText("" + mSlaveDeviceList.get(i).getBattery() + "%");
        holder.battery.setTextColor(Color.parseColor("#0FD2AE"));

        if(mSlaveDeviceList.get(i).getOnline() .equals("1")) {
            holder.online.setText("[在线]");
        } else {
            holder.online.setText("[离线]");
        }
        if(mSlaveDeviceList.get(i).getAlarm() .equals("0")) {
            holder.alarm.setVisibility(View.INVISIBLE);
        } else if(mSlaveDeviceList.get(i).getAlarm() .equals("1")) {
            holder.alarm.setVisibility(View.VISIBLE);
            holder.alarm.setImageResource(R.mipmap.ic_red);
        } else if(mSlaveDeviceList.get(i).getAlarm() .equals("2")) {
            holder.alarm.setVisibility(View.VISIBLE);
            holder.alarm.setImageResource(R.mipmap.ic_green);
        }
        if(mSlaveDeviceList.get(i).getComm().equals("1") ) {
            holder.comm.setText("正常");
            holder.comm.setTextColor(Color.parseColor("#0FD2AE"));
            holder.commDetail.setVisibility(View.GONE);
        } else  if(mSlaveDeviceList.get(i).getComm().equals("0")){
            holder.comm.setText("异常");
            holder.comm.setTextColor(Color.parseColor("#ED6663"));
            holder.commDetail.setVisibility(View.VISIBLE);
            //(主从机版本不一致,传感器异常,稳流器异常)
            String detailStr="";
            if(mSlaveDeviceList.get(i).getVersionStatus().equals("0")){//异常
                detailStr="主从机版本不一致,";
            }
            if(mSlaveDeviceList.get(i).getSensorStatus().equals("0")){//异常
                detailStr=detailStr+"传感器异常,";
            }
            if(mSlaveDeviceList.get(i).getMotorStatus().equals("0")){//异常
                detailStr=detailStr+"稳流器异常,";
            }
            if(detailStr.endsWith(",")){
                detailStr=detailStr.substring(0,detailStr.length()-1);
            }
            if(!detailStr.equals("")){
                holder.commDetail.setText("("+detailStr+")");
            }else{
                holder.commDetail.setText(detailStr);
            }
        } else if(mSlaveDeviceList.get(i).getComm().equals("2")){//未获取
            holder.comm.setText("未获取");
            holder.comm.setTextColor(Color.parseColor("#ED6663"));
            holder.commDetail.setVisibility(View.GONE);
            holder.battery.setText("未获取");
            holder.battery.setTextColor(Color.parseColor("#ED6663"));
        }

        if(mSlaveDeviceList.get(i).getSlaveOrMaster() .equals("1")) {
            holder.title.setText("从机");
        } else if(mSlaveDeviceList.get(i).getSlaveOrMaster() .equals("0")) {
            holder.title.setText("主机");
        }else if(mSlaveDeviceList.get(i).getSlaveOrMaster() .equals("2")) {
            holder.title.setText("标定仪");
        }
        return view;
    }

    public class ViewHolder {
        TextView title;
        TextView serialNumber;
        TextView online;
        ImageView alarm;
        TextView comm;
        TextView battery;
        TextView commDetail;
    }
}
