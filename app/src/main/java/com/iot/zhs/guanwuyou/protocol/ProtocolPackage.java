package com.iot.zhs.guanwuyou.protocol;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;

import com.iot.zhs.guanwuyou.LoginActivity;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.utils.DowloadFileUtils;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.iot.zhs.guanwuyou.view.NotificationDialog;

import org.litepal.crud.DataSupport;

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

    private List<String> localVerData;
    private int flag;//0--中控 1--主机 2-从机
    private String updateFileURL;//下载文件的地址

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

            String slaveSn=mData.get(0);
            SlaveDevice device = new SlaveDevice();
            device.setSerialNumber(slaveSn);
            device.setSlaveOrMaster(2);//2--标定仪
            if (DataSupport.where("serialNumber = ?", slaveSn).find(SlaveDevice.class).size() == 0) {
                //insert new data
                device.setSerialNumber(slaveSn);
                device.save();
            } else {
                device.updateAll("serialNumber = ?", slaveSn);
            }

        } else if (mType.equals("matchlist")) {

            List<SlaveDevice> allDevices = DataSupport.findAll(SlaveDevice.class);
            int slaveNumber = Integer.parseInt(mData.get(0));
            for(int i = 0; i < mDatNum; i++) {
                Log.d(TAG, String.format("### [%d] => %s", i, mData.get(i)));
            }
            String[] matchList = new String[slaveNumber];
            for(int i = 0; i < slaveNumber; i++) {
                matchList[i] = mData.get(i + 1);
            }
            MyApplication.getInstance().getSpUtils().setKeyMatchList(matchList);

            for(int i = 0; i < allDevices.size(); i++) {
                if(allDevices.get(i).getSlaveOrMaster()!=2) {//2--标定仪 这里不需要处理标定仪
                    boolean isExist = false;
                    for (int j = 0; j < slaveNumber; j++) {
                        if (mData.get(j + 1).equals(allDevices.get(i).getSerialNumber())) {
                            isExist = true;
                        }
                    }
                    if (!isExist) {
                        String deleteSn = allDevices.get(i).getSerialNumber();
                        Log.d(TAG, "delete serial number: " + deleteSn);
                        DataSupport.deleteAll(SlaveDevice.class, "serialNumber = ?", deleteSn);
                    }
                }
            }
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
        } else if(mType.equals("ver")) {
            updateVersion();

        }
        return true;
    }

    public String getmType() {
        return mType;
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

    /**
     * 更新
     * @return
     */
    public void updateVersion(){
        if(Utils.stringIsEmpty(updateFileURL) || Utils.listIsEmpty(localVerData)){
            return;
        }
        if (!Utils.compare(mData, localVerData)) {//对比本地版本和服务器版本是否一致
            final NotificationDialog mNotificationDialog = new NotificationDialog();
            mNotificationDialog.init("提醒",
                    "取消",
                    "更新",
                    new NotificationDialog.NotificationDialogListener() {
                        @Override
                        public void onButtonClick(int id) {
                            //响应左边的button
                            if (id == 1) {
                                mNotificationDialog.dismiss();
                            } else if (id == 2) {
                                new DowloadFileUtils(LoginActivity.getIntance()).downloadFile("", "", updateFileURL);
                                mNotificationDialog.dismiss();
                            }
                        }
                    });
            String message = "";
            switch (flag){
                case 0:
                    message="app发现新版本(" + Utils.listToString(mData, ".") + ")，是否需要更新?";
                    break;
                case 1:
                    message="主机发现新版本(" + Utils.listToString(mData, ".") + ")，是否需要更新?";
                    break;
                case 2:
                    message="从机发现新版本(" + Utils.listToString(mData, ".") + ")，是否需要更新?";
                    break;
            }
            mNotificationDialog.setMessage(message);
            AppCompatActivity cur=MyApplication.getInstance().getCurrentActivity();
            mNotificationDialog.show(MyApplication.getInstance().getCurrentActivity().getSupportFragmentManager(), "Notification");
        }
    }

    public void setUpdateVersionData(List<String> localVerData, int flag, String  updateFileURL){
        this.localVerData=localVerData;
        this.flag=flag;
        this.updateFileURL=updateFileURL;
    }

}
