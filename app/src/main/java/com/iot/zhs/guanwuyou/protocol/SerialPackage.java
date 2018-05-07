package com.iot.zhs.guanwuyou.protocol;

import android.util.Log;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.item.SlaveDeviceItem;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.SlaveStatusList;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

    public SerialPackage(String rawData) {
        mRawData = rawData.replaceAll("\r", "").replaceAll("\n", "").trim();
        mData = new ArrayList<>();
    }

    public void parse() {
        //parse the raw data
        Log.d(TAG, "parse: " + mRawData);
        String[] msg = mRawData.split(",");
        if (msg.length < 10) {
            for (int i = 0; i < msg.length; i++) {
                Log.d(TAG, String.format("[%d] = %s", i, msg[i]));
            }
            Log.e(TAG, "#### parse raw data invalid");
            return;
        }
        mSyncId = Integer.valueOf(msg[1]);
        mDeviceId0 = msg[2];
//        mDeviceId0 = "SN0301201601010001";
//        mDeviceId0 = "SN0303201611091873";
        mDeviceId1 = msg[3];
        mHandle = Integer.valueOf(msg[4]);
        mOperation = msg[5];
        if (msg[6].equals("none"))
            mType = TYPE_NONE;
        else if (msg[6].equals("get"))
            mType = TYPE_GET;
        else if (msg[6].equals("set"))
            mType = TYPE_SET;
        mDataNum = Integer.valueOf(msg[7]);
        for (int i = 0; i < mDataNum; i++) {
            mData.add(msg[8 + i]);
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
            if(mDeviceId1.equals("0")) {
                Log.d(TAG, "####MASTER VER");
                pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        "0", "ver", "get", 3, mData);
            } else {
                Log.d(TAG, "####SLAVER VER");
                pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        mDeviceId1, "ver", "get", 3, mData);
            }

            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "ver ack ok!");
                            } else {
                                Log.e(TAG, "ver response message: " + info.message);
                            }

                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {

                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        ProtocolPackage pkg = null;
                                        if (mDeviceId1.equals("0")) {
                                            Log.d(TAG, "####MASTER VER");
                                            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                    "0", "ver", "get", 3, mData);
                                        } else {
                                            Log.d(TAG, "####SLAVER VER");
                                            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                    mDeviceId1, "ver", "get", 3, mData);
                                        }
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "ver ack ok!");
                                                        } else {
                                                            Log.e(TAG, "ver response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
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
            dataList.add("" + month + 1);
            dataList.add("" + day + 1);
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
                    "0", "calmac", "get", 1, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "calmac ack ok!");
                            } else {
                                Log.e(TAG, "calmac response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "1", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                "0", "calmac", "get", 1, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "calmac ack ok!");
                                                        } else {
                                                            Log.e(TAG, "calmac response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
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
            device.setLatestData(Integer.valueOf(mData.get(mDataNum - 1)));
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
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "raw ack ok!");
                            } else {
                                Log.e(TAG, "almfactor response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        String slaveSn = mDeviceId1;
                                        SlaveDevice device = new SlaveDevice();
                                        device.setLatestData(Integer.valueOf(mData.get(mDataNum - 1)));
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
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "raw ack ok!");
                                                        } else {
                                                            Log.e(TAG, "almfactor response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("prealarm")) {
            MyApplication.getInstance().getSpUtils().setKeySlavePrealarm(true);
            Log.d(TAG, "######prealarm#######");
            //save slave
            String slaveSn = mDeviceId1;
            SlaveDevice device = new SlaveDevice();
            device.setAlarm(1);
            if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(slaveSn);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", slaveSn);
            }
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_ALARM_STATUS);
            event.message = "prealarm";
            EventBus.getDefault().post(event);
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "prealarm", "none", 3, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "prealarm ack ok!");
                            } else {
                                Log.e(TAG, "prealarm response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                mDeviceId1, "prealarm", "none", 3, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "prealarm ack ok!");
                                                        } else {
                                                            Log.e(TAG, "prealarm response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("alarm")) {
            MyApplication.getInstance().getSpUtils().setKeySlaveAlarm(true);
            Log.d(TAG, "######alarm#######");
            //save slave
            String slaveSn = mDeviceId1;
            SlaveDevice device = new SlaveDevice();
            device.setAlarm(2);
            if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(slaveSn);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", slaveSn);
            }
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_ALARM_STATUS);
            event.message = "alarm";
            EventBus.getDefault().post(event);
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "alarm", "none", 3, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "alarm ack ok!");
                            } else {
                                Log.e(TAG, "alarm response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                mDeviceId1, "alarm", "none", 3, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "alarm ack ok!");
                                                        } else {
                                                            Log.e(TAG, "alarm response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("cal.con")) {
            Log.d(TAG, "set cal con: " + mData.get(0));
            MyApplication.getInstance().getSpUtils().setKeyCalCon(mData.get(0));
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_UPDATE_CALIBRATION);
            EventBus.getDefault().post(event);
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "cal.con", "set", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "cal.con ack ok!");
                            } else {
                                Log.e(TAG, "cal.con response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        MyApplication.getInstance().getSpUtils().setKeyCalCon(mData.get(0));
                                        MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_UPDATE_CALIBRATION);
                                        EventBus.getDefault().post(event);
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                mDeviceId1, "cal.con", "set", mDataNum, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "cal.con ack ok!");
                                                        } else {
                                                            Log.e(TAG, "cal.con response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("cal.slurry")) {
            Log.d(TAG, "set cal slurry: " + mData.get(0));
            MyApplication.getInstance().getSpUtils().setKeyCalSlurry(mData.get(0));
            MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_UPDATE_CALIBRATION);
            EventBus.getDefault().post(event);
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "cal.slurry", "set", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "cal.slurry ack ok!");
                            } else {
                                Log.e(TAG, "cal.slurry response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        MyApplication.getInstance().getSpUtils().setKeyCalSlurry(mData.get(0));
                                        MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_UPDATE_CALIBRATION);
                                        EventBus.getDefault().post(event);
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                mDeviceId1, "cal.slurry", "set", mDataNum, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "cal.slurry ack ok!");
                                                        } else {
                                                            Log.e(TAG, "cal.slurry response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("almsta")) {
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    "0", "almsta", "none", 1, mData);
            Log.d(TAG, "-> " + pkg.toString());
            String alarm = mData.get(0);
            String orgAlarm = MyApplication.getInstance().getSpUtils().getKeyAlarmStatus();
            if (!alarm.equals(orgAlarm)) {
                MyApplication.getInstance().getSpUtils().setKeyAlarmStatus(alarm);
                Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                        MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                        pkg.toString(),
                        new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.d(TAG, response);
                                Gson gson = new Gson();
                                ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                if (info.code.equals("1")) {
                                    Log.d(TAG, "almsta ack ok!");
                                } else {
                                    Log.e(TAG, "almsta response message: " + info.message);
                                }
                                if(mNeedResend) {
                                    ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                    if(pkgResponse.parse()) {
                                        if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                            //resend the package
                                            int syncid = pkgResponse.getSyncId() + 1;
                                            MyApplication.getInstance().setSyncId(syncid);
                                            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                    "0", "almsta", "none", 1, mData);
                                            Log.d(TAG, "-> " + pkg.toString());
                                            String alarm = mData.get(0);
                                            String orgAlarm = MyApplication.getInstance().getSpUtils().getKeyAlarmStatus();
                                            if (!alarm.equals(orgAlarm)) {
                                                MyApplication.getInstance().getSpUtils().setKeyAlarmStatus(alarm);
                                                Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                        MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                        pkg.toString(),
                                                        new StringCallback() {
                                                            @Override
                                                            public void onError(Call call, Exception e, int id) {
                                                                e.printStackTrace();
                                                            }

                                                            @Override
                                                            public void onResponse(String response, int id) {
                                                                Log.d(TAG, response);
                                                                Gson gson = new Gson();
                                                                ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                                if (info.code.equals("1")) {
                                                                    Log.d(TAG, "almsta ack ok!");
                                                                } else {
                                                                    Log.e(TAG, "almsta response message: " + info.message);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                }
                            }
                        });
            }
        } else if (mOperation.equals("uselist")) {
            Log.d(TAG, "uselist " + mDataNum + " slave devices");
            MyApplication.getInstance().getSpUtils().setKeyUseList(mData);
            ProtocolPackage pkg;
            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    "0", "uselist", "none", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "uselist ack ok!");
                            } else {
                                Log.e(TAG, "uselist response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        MyApplication.getInstance().getSpUtils().setKeyUseList(mData);
                                        ProtocolPackage pkg;
                                        pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                "0", "uselist", "none", mDataNum, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "uselist ack ok!");
                                                        } else {
                                                            Log.e(TAG, "uselist response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("battery")) {
            ProtocolPackage pkg;
            if (mDeviceId1.equals("0")) {
                pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                        "1", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                        "0", "battery", "none", 1, mData);
                MyApplication.getInstance().getSpUtils().setKeyMasterBattery(Integer.valueOf(mData.get(0)));
            }
            //send slave battery
            else {
                //save slave
                String slaveSn = mDeviceId1;
                SlaveDevice device = new SlaveDevice();
                device.setBattery(Integer.valueOf(mData.get(0)));
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
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "battery ack ok!");
                            } else {
                                Log.e(TAG, "battery response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        ProtocolPackage pkg;
                                        if (mDeviceId1.equals("0")) {
                                            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                    "1", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                    "0", "battery", "none", 1, mData);
                                            MyApplication.getInstance().getSpUtils().setKeyMasterBattery(Integer.valueOf(mData.get(0)));
                                        }
                                        //send slave battery
                                        else {
                                            //save slave
                                            String slaveSn = mDeviceId1;
                                            SlaveDevice device = new SlaveDevice();
                                            device.setBattery(Integer.valueOf(mData.get(0)));
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
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "battery ack ok!");
                                                        } else {
                                                            Log.e(TAG, "battery response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("usenodesta")) {
            SlaveStatusList.SlaveStatus status = new SlaveStatusList.SlaveStatus();
            status.slaveSerialNumber = mDeviceId1;
            status.online = mData.get(0);
            status.versionStatus = mData.get(1);
            status.commStatus = mData.get(2);
            status.thresholdStatus = mData.get(3);
            status.networkStatus = mData.get(4);

            //save slave
            String slaveSn = mDeviceId1;
            SlaveDevice device = new SlaveDevice();
            device.setOnline(Integer.valueOf(mData.get(0)));
            device.setComm(Integer.valueOf(mData.get(2)));
            device.setSlaveOrMaster(1);
            if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(slaveSn);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", slaveSn);
            }

            List<SlaveStatusList.SlaveStatus> list = MyApplication.getInstance().getSpUtils().getKeySlaveStatusList();
            if (list == null) {
                list = new ArrayList<>();
            } else {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).slaveSerialNumber.equals(status.slaveSerialNumber)) {
                        list.remove(i);
                    }
                }
            }
            list.add(status);
            MyApplication.getInstance().getSpUtils().setKeySlaveStatusList(list);
            mData.add(0, mDeviceId1);
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    "0", "usenodesta", "none", mDataNum + 1, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "usenodesta ack ok!");
                            } else {
                                Log.e(TAG, "usenodesta response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        SlaveStatusList.SlaveStatus status = new SlaveStatusList.SlaveStatus();
                                        status.slaveSerialNumber = mDeviceId1;
                                        status.online = mData.get(0);
                                        status.versionStatus = mData.get(1);
                                        status.commStatus = mData.get(2);
                                        status.thresholdStatus = mData.get(3);
                                        status.networkStatus = mData.get(4);

                                        //save slave
                                        String slaveSn = mDeviceId1;
                                        SlaveDevice device = new SlaveDevice();
                                        device.setOnline(Integer.valueOf(mData.get(0)));
                                        device.setComm(Integer.valueOf(mData.get(2)));
                                        device.setSlaveOrMaster(1);
                                        if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                                            //insert new data
                                            device.setSerialNumber(slaveSn);
                                            device.save();
                                        } else {
                                            device.updateAll("serialNumber = ?", slaveSn);
                                        }

                                        List<SlaveStatusList.SlaveStatus> list = MyApplication.getInstance().getSpUtils().getKeySlaveStatusList();
                                        if (list == null) {
                                            list = new ArrayList<>();
                                        } else {
                                            for (int i = 0; i < list.size(); i++) {
                                                if (list.get(i).slaveSerialNumber.equals(status.slaveSerialNumber)) {
                                                    list.remove(i);
                                                }
                                            }
                                        }
                                        list.add(status);
                                        MyApplication.getInstance().getSpUtils().setKeySlaveStatusList(list);
                                        mData.add(0, mDeviceId1);
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                "0", "usenodesta", "none", mDataNum + 1, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "usenodesta ack ok!");
                                                        } else {
                                                            Log.e(TAG, "usenodesta response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("matchlist")) {
            Log.d(TAG, "matchlist!!");
            List<String> dataList = new ArrayList<>();
            String[] matchList = MyApplication.getInstance().getSpUtils().getKeyMatchList();
            if (matchList == null) {
                Log.d(TAG, "match list is null");
                dataList.add("1");
                dataList.add("0");
            } else {
                for (int i = 0; i < matchList.length; i++) {
                    dataList.add(matchList[i]);
                }
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
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "mspeed ack ok!");
                            } else {
                                Log.e(TAG, "mspeed response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        MyApplication.getInstance().getSpUtils().setKeyLatestRaw(mData.get(mDataNum - 1));
                                        //send to server
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                mDeviceId1, "mspeed", "none", mDataNum, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "mspeed ack ok!");
                                                        } else {
                                                            Log.e(TAG, "mspeed response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        } else if (mOperation.equals("almfactor")) {
            //send to server
            ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "almfactor", "none", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "almfactor ack ok!");
                            } else {
                                Log.e(TAG, "almfactor response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                mDeviceId1, "almfactor", "none", mDataNum, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "almfactor ack ok!");
                                                        } else {
                                                            Log.e(TAG, "almfactor response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
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
                    "0", "mode", "get", 1, modeData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "mode ack ok!");
                            } else {
                                Log.e(TAG, "mode response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        //send to server
                                        ProtocolPackage pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "1", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                "0", "mode", "get", 1, modeData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "mode ack ok!");
                                                        } else {
                                                            Log.e(TAG, "mode response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
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
            ProtocolPackage pkg;
            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "sensorid", "get", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "sensorid ack ok!");
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    List<String> dataList = pkgResponse.getData();
                                    String rsp = makeResponse("sensorid", dataList);
                                    MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_WRITE);
                                    event.message = rsp;
                                    EventBus.getDefault().post(event);
                                }
                            } else {
                                Log.e(TAG, "sensorid response message: " + info.message);
                            }
                        }
                    });
        } else if(mOperation.equals("fin")) {
            ProtocolPackage pkg;
            pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                    "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                    mDeviceId1, "fin", "none", mDataNum, mData);
            Log.d(TAG, "-> " + pkg.toString());
            Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                    MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                    pkg.toString(),
                    new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            Gson gson = new Gson();
                            ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                            if (info.code.equals("1")) {
                                Log.d(TAG, "fin ack ok!");
                            } else {
                                Log.e(TAG, "fin response message: " + info.message);
                            }
                            if(mNeedResend) {
                                ProtocolPackage pkgResponse = new ProtocolPackage(info.data);
                                if(pkgResponse.parse()) {
                                    if (pkgResponse.getData().get(0) != null && pkgResponse.getData().get(0).equals("reject")) {
                                        //resend the package
                                        int syncid = pkgResponse.getSyncId() + 1;
                                        MyApplication.getInstance().setSyncId(syncid);
                                        ProtocolPackage pkg;
                                        pkg = new ProtocolPackage(MyApplication.getInstance().getSyncId(),
                                                "0", MyApplication.getInstance().getSpUtils().getKeyLoginiMasterDeviceSn(),
                                                mDeviceId1, "fin", "none", mDataNum, mData);
                                        Log.d(TAG, "-> " + pkg.toString());
                                        Utils.doProcessProtocolInfo(MyApplication.getInstance().getSpUtils().getKeyLoginToken(),
                                                MyApplication.getInstance().getSpUtils().getKeyLoginUserId(),
                                                pkg.toString(),
                                                new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        Log.d(TAG, response);
                                                        Gson gson = new Gson();
                                                        ProcessProtocolInfo info = gson.fromJson(response, ProcessProtocolInfo.class);
                                                        if (info.code.equals("1")) {
                                                            Log.d(TAG, "fin ack ok!");
                                                        } else {
                                                            Log.e(TAG, "fin response message: " + info.message);
                                                        }
                                                    }
                                                });
                                    }
                                }
                            }
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
