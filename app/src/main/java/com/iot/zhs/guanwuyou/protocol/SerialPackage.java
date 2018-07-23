package com.iot.zhs.guanwuyou.protocol;

import android.util.Log;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.database.AlarmState;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.item.SlaveDeviceItem;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SlaveStatusList;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;
import org.litepal.util.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import okhttp3.Call;

/**
 * Created by H151136 on 1/25/2018.
 */

public class SerialPackage {
    private static final String TAG = "ZSH.IOT";
    public static final String SPLIT = ",";
    public static final String FRAME_HEAD = ">>";
    public static final String PROTOCOL_ID = "cssp";
    public static final String FRAME_END = "\n";

    public static final int TYPE_NONE = 0;
    public static final int TYPE_GET = 1;
    public static final int TYPE_SET = 2;

    public static final boolean mNeedResend = true;

    private int mSyncId;
    private String mDeviceId0;
    private String mDeviceId1;
    private int mHandle;
    private int mType;
    private String mOperation;
    private int mDataNum;
    private List<String> mData;
    private int mChecksum;

    private String mRawData;

    public int getSyncId() {
        return mSyncId;
    }

    public String getDeviceId0() {
        return mDeviceId0;
    }

    public String getDeviceId1() {
        return mDeviceId1;
    }

    public int getHandle() {
        return mHandle;
    }

    public int getType() {
        return mType;
    }

    public String getOperation() {
        return mOperation;
    }

    public int getDataNum() {
        return mDataNum;
    }

    public List<String> getData() {
        return mData;
    }

    public int getChecksum() {
        return mChecksum;
    }

    public void setSyncId(int id) {
        mSyncId = id;
    }

    public void setDeviceId0(String id) {
        mDeviceId0 = id;
    }

    public void setDeviceId1(String id) {
        mDeviceId1 = id;
    }

    public void setHandle(int handle) {
        mHandle = handle;
    }

    public void setType(int type) {
        mType = type;
    }

    public void setDataNum(int num) {
        mDataNum = num;
    }

    public void setData(List<String> d) {
        mData = d;
    }

    public void setChecksum(int cs) {
        mChecksum = cs;
    }

    public SerialPackage() {
    }

    public SerialPackage(String rawData) {
        mRawData = rawData.replaceAll("\r", "").replaceAll("\n", "").trim();
        mData = new ArrayList<>();
    }

    public void parse() {
        //parse the raw data
        Log.d(TAG, "SerialPackage--parse: " + mRawData);
        String[] msg = mRawData.split(",");
        if (msg.length < 10) {
            for (int i = 0; i < msg.length; i++) {
                Log.d(TAG, String.format("[%d] = %s", i, msg[i]));
            }
            Log.e(TAG, "#### parse raw data invalid");
            return;
        }
        try {
            mSyncId = Integer.valueOf(msg[1]);
        } catch (Exception e) {
            return;
        }
        mDeviceId0 = msg[2];
//        mDeviceId0 = "SN0301201601010001";
//        mDeviceId0 = "SN0303201611091873";
        mDeviceId1 = msg[3];
        try {
            mHandle = Integer.valueOf(msg[4]);
        } catch (Exception e) {
            return;
        }
        mOperation = msg[5];
        if (msg[6].equals("none"))
            mType = TYPE_NONE;
        else if (msg[6].equals("get"))
            mType = TYPE_GET;
        else if (msg[6].equals("set"))
            mType = TYPE_SET;
        try {
            mDataNum = Integer.valueOf(msg[7]);
        } catch (Exception e) {
            return;
        }

        for (int i = 0; i < mDataNum; i++) {
            try {
                mData.add(msg[8 + i]);
            } catch (Exception e) {
                return;
            }
        }

        MyApplication.getInstance().getSpUtils().setKeyLoginMasterDeviceSn(mDeviceId0);
        if (mOperation.equals("sn")) {
            Log.d(TAG, "set master sn: " + mDeviceId0);
        } else if (mOperation.equals("mainip")) {
            Log.d(TAG, "set mainip: " + mData.get(0) + ", port: " + mData.get(1));
        } else if (mOperation.equals("devip")) {
            Log.d(TAG, "set devip: " + mData.get(0) + ", port: " + mData.get(1));
        } else if (mOperation.equals("updateip")) {
            Log.d(TAG, "set updateip: " + mData.get(0) + ", port: " + mData.get(1));
        } else if (mOperation.equals("mfilename")) {
            Log.d(TAG, "set master file: " + mData.get(0));
        } else if (mOperation.equals("sfilename")) {
            Log.d(TAG, "set slave file: " + mData.get(0));
        } else if (mOperation.equals("ver")) {
            Log.d(TAG, "get version from server: " + mData.get(0) + ","
                    + mData.get(1) + ", " + mData.get(2));
            //request controller's version
            ProtocolPackage pkg = null;
            if (mDeviceId1.equals("0")) {
                Log.d(TAG, "####MASTER VER");
                pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        "0", "ver", "get", mData.size(), mData);
            } else {
                Log.d(TAG, "####SLAVER VER");
                pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        mDeviceId1, "ver", "get", mData.size(), mData);
            }

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {
                            if (mDeviceId1.equals("0")) {
                                Log.d(TAG, "update master version");
                                pkgResponse.setUpdateVersionData(mData, 1, processProtocolInfo.data.downloadUrl);

                            } else {
                                Log.d(TAG, "update slave version");
                                pkgResponse.setUpdateVersionData(mData, 2, processProtocolInfo.data.downloadUrl);

                            }
                        }
                    });
        } else if (mOperation.equals("systime")) {
            Log.d(TAG, "get system time from Android board");
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR);
            int min = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);

            List<String> dataList = new ArrayList<>();
            dataList.add("" + (year - 2000));
            dataList.add("" + (month + 1));
            dataList.add("" + day);
            dataList.add("" + hour);
            dataList.add("" + min);
            dataList.add("" + second);
            dataList.add("+8");
            String rsp = makeResponse("systime", dataList);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
            event.message = rsp;
            EventBus.getDefault().post(event);
        } else if (mOperation.equals("calmac")) {
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "1", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    "0", "calmac", "get", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

            Log.d(TAG, "###send calmac to master");
            List<String> dataList = new ArrayList<>();
            if (MyApplication.getInstance().getSpUtils().getKeyCalMac().isEmpty()) {
                dataList.add("0");
            } else {
                dataList.add("1");
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyCalMac());
            }
            String rsp = makeResponse("calmac", dataList);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
            event.message = rsp;
            EventBus.getDefault().post(event);
        } else if (mOperation.equals("cal&trsd")) {
            Log.d(TAG, "###send cal&trsd to master");
            List<String> dataList = new ArrayList<>();
            if (MyApplication.getInstance().getSpUtils().getKeyCalTrsd0().isEmpty()) {
                dataList.add("0");
            } else {
                dataList.add("1");
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyCalTrsd0());
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyCalTrsd1());
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyCalTrsd2());
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyCalTrsd3());
            }
            String rsp = makeResponse("cal&trsd", dataList);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
            event.message = rsp;
            EventBus.getDefault().post(event);
        } else if (mOperation.equals("raw")) {
            Log.d(TAG, "raw data size: " + mDataNum);
            Log.d(TAG, "raw data latest: " + mData.get(mDataNum - 1));
            //save slave
            String slaveSn = mDeviceId1;
            SlaveDevice device = new SlaveDevice();
            device.setLatestData(mData.get(mDataNum - 1));
            if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(slaveSn);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", slaveSn);
            }
            //save the latest data
            MyApplication.getInstance().getSpUtils().setKeyLatestRaw(mData.get(mDataNum - 1));
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "raw", "none", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

        } else if (mOperation.equals("prealarm")) {
            MyApplication.getInstance().getSpUtils().setKeySlavePrealarm(true);
            Log.d(TAG, "######prealarm#######");
            //save slave
            String slaveSn = mDeviceId1;
            SlaveDevice device = new SlaveDevice();
            device.setAlarm("1");
            if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(slaveSn);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", slaveSn);
            }
//            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_ALARM_STATUS);
//            event.message = "prealarm";
//            EventBus.getDefault().post(event);
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "prealarm", "none", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        } else if (mOperation.equals("alarm")) {
            // MyApplication.getInstance().getSpUtils().setKeySlaveAlarm(true);
            Log.d(TAG, "######alarm#######");
            //save slave
            String slaveSn = mDeviceId1;
            SlaveDevice device = new SlaveDevice();
            device.setAlarm("2");
            if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(slaveSn);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", slaveSn);
            }
//            boolean isAllSlaveAlarmed = true;
//            List<SlaveDevice> slaveDeviceList = DataSupport.findAll(SlaveDevice.class);
//            for (SlaveDevice dev : slaveDeviceList) {
//                if(dev.getAlarm() != 2) {
//                    isAllSlaveAlarmed = false;
//                }
//            }
//
//            if(isAllSlaveAlarmed) {
//                MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_ALARM_STATUS);
//                event.message = "alarm";
//                EventBus.getDefault().post(event);
//            }
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "alarm", "none", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

        } else if (mOperation.equals("cal.con")) {
            Log.d(TAG, "set cal con: " + mData.get(0));
            //   MyApplication.getInstance().getSpUtils().setKeyCalCon(mData.get(0));
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_UPDATE_CAL_CON);
            event.message = mData.get(0);
            EventBus.getDefault().post(event);

            if(!mData.get(0).equals("0")){//标定失败
                ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        mDeviceId1, "cal.con", "set", mDataNum, mData);
                Log.d(TAG, "-> " + pkg.toString());
                Utils.doProcessProtocolInfo(
                        pkg, new Utils.ResponseCallback() {
                            @Override
                            public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                            }
                        });
            }

        } else if (mOperation.equals("cal.slurry")) {
            Log.d(TAG, "set cal slurry: " + mData.get(0));
            //  MyApplication.getInstance().getSpUtils().setKeyCalSlurry(mData.get(0));
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_UPDATE_CAL_SLURRY);
            event.message = mData.get(0);
            EventBus.getDefault().post(event);

            if(!mData.get(0).equals("0")) {//标定失败
                ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        mDeviceId1, "cal.slurry", "set", mDataNum, mData);
                Log.d(TAG, "-> " + pkg.toString());
                Utils.doProcessProtocolInfo(
                        pkg, new Utils.ResponseCallback() {
                            @Override
                            public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                            }
                        });
            }

        } else if (mOperation.equals("almsta")) {
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    "0", "almsta", "none", 1, mData);
            Log.d(TAG, "-> " + pkg.toString());
            //不需要存储数据库，只需要存储当前的最新值
            // MyApplication.getInstance().getSpUtils().setKeyAlarmStatus(alarm);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_ALARM_STATUS);
            event.message = mData.get(0);
            EventBus.getDefault().post(event);

            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            Log.d(TAG, "almsta--接收到主机的时间--" + dateStr);

            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });


        } else if (mOperation.equals("uselist")) {
            Log.d(TAG, "uselist " + mDataNum + " slave devices");
            MyApplication.getInstance().getSpUtils().setKeyUseList(mData);
            ProtocolPackage pkg;
            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    "0", "uselist", "none", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });
        } else if (mOperation.equals("battery")) {
            ProtocolPackage pkg;
            if (mDeviceId1.equals("0")) {//主机
                pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        "0", "battery", "none", 1, mData);
                MyApplication.getInstance().getSpUtils().setKeyMasterBattery(Integer.valueOf(mData.get(0)));
            } else { //send slave battery 从机
                //save slave
                String slaveSn = mDeviceId1;
                SlaveDevice device = new SlaveDevice();
                device.setBattery(mData.get(0));
                if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                    //insert new data
                    device.setSerialNumber(slaveSn);
                    device.save();
                } else {
                    device.updateAll("serialNumber = ?", slaveSn);
                }

                pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        mDeviceId1, "battery", "none", 1, mData);
            }
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

        } else if (mOperation.equals("usenodesta")) {
            for (int i = 0; i < mDataNum; i++) {
                Log.d(TAG, "### useno " + mDeviceId1 + " -> i=" + i + " -> " + mData.get(i));
            }
            //save slave
            SlaveDevice device = new SlaveDevice();
            device.setComm(mData.get(0));
            device.setVersionStatus(mData.get(1));
            device.setSensorStatus(mData.get(2));
            device.setMotorStatus(mData.get(3));
            device.setOnline(mData.get(4));
            if (DataSupport.where("serialNumber = ?", mDeviceId1).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(mDeviceId1);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", mDeviceId1);
            }

           // List<SlaveDevice> savedDeviceList = DataSupport.findAll(SlaveDevice.class);

            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "usenodesta", "none", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

        } else if (mOperation.equals("matchlist")) {
            Log.d(TAG, "matchlist!!");
            List<String> dataList = new ArrayList<>();
            String[] matchList = MyApplication.getInstance().getSpUtils().getKeyMatchList();
            if (matchList == null) {
                Log.d(TAG, "match list is null");
                dataList.add("0");
            } else {
                for (int i = 0; i < matchList.length; i++) {
                    dataList.add(matchList[i]);
                }
                dataList.add(0, "1");

            }
            String rsp = makeResponse("matchlist", dataList);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
            event.message = rsp;
            EventBus.getDefault().post(event);
        } else if (mOperation.equals("mspeed")) {
            Log.d(TAG, "mspeed data size: " + mDataNum);
            MyApplication.getInstance().getSpUtils().setKeyLatestRaw(mData.get(mDataNum - 1));
            //send to server
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "mspeed", "none", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

        } else if (mOperation.equals("almfactor")) {
            //send to server
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "almfactor", "none", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

        } else if (mOperation.equals("mode")) {
            List<String> dataList = new ArrayList<>();
            if (MyApplication.getInstance().getSpUtils().getKeyMode().isEmpty()) {
                dataList.add("1");
                dataList.add("0");
            } else {
                dataList.add("1");
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyMode());
            }
            String rsp = makeResponse("mode", dataList);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
            event.message = rsp;
            EventBus.getDefault().post(event);

            final List<String> modeData = new ArrayList<>();
            modeData.add("1");
            //send to server
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "1", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    "0", "mode", "get", modeData.size(), modeData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                        }
                    });

        } else if (mOperation.equals("mtrsd")) {
            List<String> dataList = new ArrayList<>();
            if (MyApplication.getInstance().getSpUtils().getKeyMtrsd0().isEmpty()) {
                dataList.add("0");
            } else {
                dataList.add("1");
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyMtrsd0());
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyMtrsd1());
                dataList.add(MyApplication.getInstance().getSpUtils().getKeyMtrsd2());
            }
            String rsp = makeResponse("mtrsd", dataList);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
            event.message = rsp;
            EventBus.getDefault().post(event);
        } else if (mOperation.equals("sensorid")) {
            List<String> dataList = new ArrayList<>();
            dataList.add("0");
            dataList.add(MyApplication.getInstance().getSpUtils().getKeySensorid());//上一次的
            String rsp = makeResponse("sensorid", dataList);
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
            event.message = rsp;
            EventBus.getDefault().post(event);

            ProtocolPackage pkg;
            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "sensorid", "get", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(
                    pkg, new Utils.ResponseCallback() {
                        @Override
                        public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {
                            //{"clientType":"cc","code":"1","data":{"protocol":">>cssp,608,0,SN0301201304260001,SN0303201504230001,sensorid,1,0,6\n"},"message":"操作成功！","msgCode":1}
                            List<String> dataList = pkgResponse.getData();
                            dataList.add(0, "1");
                            String rsp = makeResponse("sensorid", dataList);
                            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
                            event.message = rsp;
                            EventBus.getDefault().post(event);
                        }
                    });

        } else if (mOperation.equals("fin")) {
            ProtocolPackage pkg;
            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "fin", "none", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(pkg, new Utils.ResponseCallback() {
                @Override
                public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                }
            });
        } else if (mOperation.equals("lorafreq")) {//信道处理
            //信道 [1,4]
            String url = Utils.SERVER_ADDR + "/device/doGetMasterDeviceChannel/cc";
            OkHttpUtils.post().url(url)
                    .addParams("masterDeviceSN", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn())
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            //如果失败，则获取上一次的信道值，如果上次也没有，则默认给出1
                            String channel=MyApplication.getInstance().getSpUtils().getKeyMasterChannel();
                            List<String> dataList = new ArrayList<>();
                            if(!Utils.stringIsEmpty(channel)){
                                dataList.add(channel);
                            }else{
                                dataList.add("1");
                            }
                            String rsp = makeResponse("lorafreq", dataList);
                            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
                            event.message = rsp;
                            EventBus.getDefault().post(event);

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, "信道返回：" + response);
                            if (!Utils.stringIsEmpty(response)) {
                                JSONObject object = null;
                                try {
                                    object = new JSONObject(response);
                                    String code = object.getString("code");
                                    if (code.equals(Utils.MSG_CODE_OK)) {
                                        if (object.has("data")) {
                                            String channel = object.getString("data");
                                            if(!Utils.stringIsEmpty(channel)){
                                                MyApplication.getInstance().getSpUtils().setKeyMasterChannel(channel);

                                                List<String> dataList = new ArrayList<>();
                                                dataList.add(channel);
                                                String rsp = makeResponse("lorafreq", dataList);
                                                MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
                                                event.message = rsp;
                                                EventBus.getDefault().post(event);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }else if(mOperation.equals("motor0")||mOperation.equals("motor1")||mOperation.equals("motor2")){
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, mOperation, "none", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());//cssp,4516,0,SN0301201304260001,SN0302201504230003,motor0,none,3,0,0,93,63
            Utils.doProcessProtocolInfo(pkg, new Utils.ResponseCallback() {
                @Override
                public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                }
            });
        } else if(mOperation.equals("err")){
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "err", "none", mData.size(), mData);
            Log.d(TAG, "-> " + pkg.toString());//>>cssp,2596,0,SN0301201304260001,SN0302201504230003,err,none,1,15,5f
            Utils.doProcessProtocolInfo(pkg, new Utils.ResponseCallback() {
                @Override
                public void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse) {

                }
            });
        }
    }

    public String makeResponse(String type, List<String> dataList) {
        String value = "";
        value += "" + FRAME_HEAD + PROTOCOL_ID + SPLIT;
        value += "" + mSyncId + SPLIT;
        value += "" + mDeviceId0 + SPLIT;
        value += "" + mDeviceId1 + SPLIT;
        value += "" + type + SPLIT;
        value += "" + dataList.size() + SPLIT;
        for (String data : dataList) {
            value += "" + data + SPLIT;
        }
        int sum = Utils.calcCheckSum(value, value.length());
        String sum_fmt = String.format("%02x", sum);
        value += "" + sum_fmt + FRAME_END;

        return value;
    }

}
