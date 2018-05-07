package com.iot.zhs.guanwuyou.protocol;

import android.util.Log;

import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by H151136 on 1/31/2018.
 */

public class ProtocolPackage {
    private static final String TAG = "ZSH.IOT";

    public static final String SPLIT = ",";
    public static final String FRAME_HEAD = ">>";
    public static final String PROTOCOL_ID = "cssp";
    public static final String FRAME_END = "\n";

    private int mSyncId;
    private String mDevice0;
    private String mDevice1;
    private String mDevice2;
    private String mType;
    private String mOperation;
    private int mDatNum;
    private List<String> mData;

    private String mRawData;

    public ProtocolPackage(int syncId, String device0, String device1, String device2,
                           String type, String operation,
                           int num, List<String> data) {
        mSyncId = syncId;
        mDevice0 = device0;
        mDevice1 = device1;
        mDevice2 = device2;
        mType = type;
        mOperation = operation;
        mDatNum = num;
        mData = data;
    }

    public ProtocolPackage(String rawData) {
        mRawData = rawData;
        mData = new ArrayList<>();
    }

    public int getSyncId() {
        return mSyncId;
    }

    public int getDataNum() {
        return mDatNum;
    }

    public List<String> getData() {
        return mData;
    }

    public boolean parse() {
        Log.d(TAG, "parse: " + mRawData);
        if(mRawData == null) {
            Log.e(TAG, "mRawData is null!!!!");
            return false;
        }
        String[] msg = mRawData.split(",");
        if (msg.length < 8) {
            for (int i = 0; i < msg.length; i++) {
                Log.d(TAG, String.format("[%d] = %s", i, msg[i]));
            }
            Log.e(TAG, "#### parse raw data invalid");
            return false;
        }
        mSyncId = Integer.valueOf(msg[1]);
        mDevice0 = msg[2];
        mDevice1 = msg[3];
        mDevice2 = msg[4];
        mType = msg[5];
        mDatNum = Integer.valueOf(msg[6]);
        for (int i = 0; i < mDatNum; i++) {
            mData.add(msg[7 + i]);
        }

        if (mType.equals("cal&trsd")) {
            Log.d(TAG, "cal&trsd: " + mData.get(0)
                    + ", " + mData.get(1) + ", " + mData.get(2) +
                    ", " + mData.get(3));
            MyApplication.getInstance().getSpUtils().setKeyCalTrsd0(mData.get(0));
            MyApplication.getInstance().getSpUtils().setKeyCalTrsd1(mData.get(1));
            MyApplication.getInstance().getSpUtils().setKeyCalTrsd2(mData.get(2));
            MyApplication.getInstance().getSpUtils().setKeyCalTrsd3(mData.get(3));
        } else if(mType.equals("calmac")) {
            Log.d(TAG, "calmac: " + mData.get(0));
            MyApplication.getInstance().getSpUtils().setKeyCalMac(mData.get(0));
        } else if (mType.equals("matchlist")) {
            for(int i = 0; i < mDatNum; i++) {
                Log.d(TAG, String.format("[%d] => %s", i, mData.get(i)));
            }
            String[] matchList = new String[mDatNum];
            for(int i = 0; i < mDatNum; i++) {
                matchList[i] = mData.get(i);
            }
            MyApplication.getInstance().getSpUtils().setKeyMatchList(matchList);
        } else if(mType.equals("raw")) {

        } else if(mType.equals("mtrsd")) {
            if(mDatNum == 3) {
                MyApplication.getInstance().getSpUtils().setKeyMtrsd0(mData.get(0));
                MyApplication.getInstance().getSpUtils().setKeyMtrsd1(mData.get(1));
                MyApplication.getInstance().getSpUtils().setKeyMtrsd2(mData.get(2));
            } else {
                Log.e(TAG, "mtrsd response data size: " + mDatNum);
            }
        } else if(mType.equals("mode")) {
            MyApplication.getInstance().getSpUtils().setKeyMode(mData.get(0));
        }
        return true;
    }

    public String toString() {
        String value = "";
        value += "" + FRAME_HEAD + PROTOCOL_ID + SPLIT;
        value += "" + mSyncId + SPLIT;
        value += "" + mDevice0 + SPLIT;
        value += "" + mDevice1 + SPLIT;
        value += "" + mDevice2 + SPLIT;
        value += "" + mType + SPLIT;
        value += "" + mOperation + SPLIT;
        value += "" + mDatNum + SPLIT;
        for (String data : mData) {
            value += "" + data + SPLIT;
        }
        int sum = Utils.calcCheckSum(value, value.length());
        String sum_fmt = String.format("%02x", sum);
        value += "" + sum_fmt + FRAME_END;

        return value;
    }
}
