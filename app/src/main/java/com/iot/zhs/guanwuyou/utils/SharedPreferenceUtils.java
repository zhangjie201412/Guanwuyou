package com.iot.zhs.guanwuyou.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by H151136 on 1/16/2018.
 */

public class SharedPreferenceUtils {
    public static final String KEY_KEEP_ACCOUNT = "key_keep_account";
    public static final String KEY_USERNAME = "key_username";
    public static final String KEY_PASSWORD = "key_password";
    public static final String KEY_LOGIN_TOKEN = "key_login_token";
    public static final String KEY_LOGIN_COMPANY_ID = "key_login_company_id";
    public static final String KEY_LOGIN_COMPANY_NAME = "key_login_company_name";
    public static final String KEY_LOGIN_MASTER_DEVICE_SN = "key_login_master_device_sn";
    public static final String KEY_LOGIN_PROJECT_ID = "key_login_project_id";
    public static final String KEY_LOGIN_PROJECT_NAME = "key_login_project_name";
    public static final String KEY_LOGIN_USER_ID = "key_login_user_id";
    public static final String KEY_LOGIN_USER_NAME = "key_login_user_name";
    public static final String KEY_LATEST_RAW = "key_latest_raw";
    public static final String KEY_LATEST_MSPEED = "key_latest_mspeed";
    public static final String KEY_END_POUR_INFO = "key_end_pour_info";
  //  public static final String KEY_SLAVE_ALARM = "key_slave_alarm";
    public static final String KEY_SLAVE_PREALARM = "key_slave_prealarm";
    public static final String KEY_CAL_CON = "key_cal_con";
    public static final String KEY_CAL_SLURRY = "key_cal_slurry";
    public static final String KEY_ALARM_STATUS = "key_alarm_status";
    public static final String KEY_USE_LIST = "key_use_list";
    public static final String KEY_USE_NODESTA = "key_use_nodesta";
    public static final String KEY_MASTER_BATTERY = "key_master_battery";
    //mtrsd 3 data
    public static final String KEY_MTRSD0 = "key_trsd0";
    public static final String KEY_MTRSD1 = "key_trsd1";
    public static final String KEY_MTRSD2 = "key_trsd2";

    //cal&trsd
    public static final String KEY_CAL_TRSD0 = "key_cal_trsd0";
    public static final String KEY_CAL_TRSD1 = "key_cal_trsd1";
    public static final String KEY_CAL_TRSD2 = "key_cal_trsd2";
    public static final String KEY_CAL_TRSD3 = "key_cal_trsd3";

    public static final String KEY_CAL_MAC = "key_cal_mac";

    public static final String KEY_MODE = "key_mode";

    public static final String KEY_MATCH_LIST = "key_match_list";
    public static final String KEY_ALMSTA = "key_almsta";

    //sensorid
    public static final String KEY_SENSORID = "key_sensorid";


    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    public SharedPreferenceUtils(Context context, String file) {
        mSp = context.getSharedPreferences(file, Context.MODE_PRIVATE);
        mEditor = mSp.edit();
    }


    public String getKeySensorid() {
        return mSp.getString(KEY_SENSORID, "0");
    }

    public  void setKeySensorid(String keySensorid) {
        mEditor.putString(KEY_SENSORID, keySensorid);
        mEditor.commit();
    }

    public boolean getKeyKeepAccount() {
        return mSp.getBoolean(KEY_KEEP_ACCOUNT, false);
    }

    public String getKeyUsername() {
        return mSp.getString(KEY_USERNAME, "");
    }

    public String getKeyPassword() {
        return mSp.getString(KEY_PASSWORD, "");
    }

    public String getKeyLoginToken() {
        return mSp.getString(KEY_LOGIN_TOKEN, "");
    }

    public String getKeyLoginCompanyId() {
        return mSp.getString(KEY_LOGIN_COMPANY_ID, "");
    }

    public String getKeyLoginCompanyName() {
        return mSp.getString(KEY_LOGIN_COMPANY_NAME, "");
    }

    public String getKeyLoginiMasterDeviceSn() {
        return mSp.getString(KEY_LOGIN_MASTER_DEVICE_SN, "");
    }

    public String getKeyLoginProjectId() {
        return mSp.getString(KEY_LOGIN_PROJECT_ID, "");
    }

    public String getKeyLoginProjectName() {
        return mSp.getString(KEY_LOGIN_PROJECT_NAME, "");
    }


    public String getKeyLoginUserId() {
        return mSp.getString(KEY_LOGIN_USER_ID, "");
    }

    public String getKeyLoginUserName() {
        return mSp.getString(KEY_LOGIN_USER_NAME, "");
    }

    public String getKeyLatestRaw() {
        return mSp.getString(KEY_LATEST_RAW, "-1");
    }

    public String getKeyLatestMspeed() {
        return mSp.getString(KEY_LATEST_MSPEED, "-1");
    }

   /* public boolean getKeySlaveAlarm() {
        return mSp.getBoolean(KEY_SLAVE_ALARM, false);
    }*/

    public boolean getKeySlavePrealarm() {
        return mSp.getBoolean(KEY_SLAVE_PREALARM, false);
    }

    public String getKeyEndPourInfo() {
        return mSp.getString(KEY_END_POUR_INFO, "");
    }

   /* public String getKeyCalCon() {
        return mSp.getString(KEY_CAL_CON, "");
    }

    public String getKeyCalSlurry() {
        return mSp.getString(KEY_CAL_SLURRY, "");
    }*/

   /* public String getKeyAlarmStatus() {
        return mSp.getString(KEY_ALARM_STATUS, "0");
    }*/

    public int getKeyMasterBattery() {
        return mSp.getInt(KEY_MASTER_BATTERY, 0);
    }

    public String[] getKeyUseList() {
        String listString = mSp.getString(KEY_USE_LIST, "");
        return listString.split(",");
    }

    public List<SlaveStatusList.SlaveStatus> getKeySlaveStatusList() {
        String slaveStatusString = mSp.getString(KEY_USE_NODESTA, "");
        if (slaveStatusString.isEmpty())
            return null;
        Gson gson = new Gson();
        SlaveStatusList statusList = gson.fromJson(slaveStatusString, SlaveStatusList.class);

        return statusList.slaveStatusList;
    }

    public String getKeyMtrsd0() {
        return mSp.getString(KEY_MTRSD0, "");
    }

    public String getKeyMtrsd1() {
        return mSp.getString(KEY_MTRSD1, "");
    }

    public String getKeyMtrsd2() {
        return mSp.getString(KEY_MTRSD2, "");
    }

    public String getKeyMode() {
        return mSp.getString(KEY_MODE, "");
    }

    public String getKeyCalTrsd0() {
        return mSp.getString(KEY_CAL_TRSD0, "");
    }

    public String getKeyCalTrsd1() {
        return mSp.getString(KEY_CAL_TRSD1, "");
    }

    public String getKeyCalTrsd2() {
        return mSp.getString(KEY_CAL_TRSD2, "");
    }

    public String getKeyCalTrsd3() {
        return mSp.getString(KEY_CAL_TRSD3, "");
    }

    public String getKeyCalMac() {
        return mSp.getString(KEY_CAL_MAC, "");
    }

    public String[] getKeyMatchList() {
        String val = mSp.getString(KEY_MATCH_LIST, "");
        String[] matchList = val.split(",");

        return matchList;
    }

    public String getKeyAlmsta() {
        return mSp.getString(KEY_ALMSTA, "0");
    }

    public void setKeyKeepAccount(boolean val) {
        mEditor.putBoolean(KEY_KEEP_ACCOUNT, val);
        mEditor.commit();
    }

    public void setKeyUsername(String val) {
        mEditor.putString(KEY_USERNAME, val);
        mEditor.commit();
    }

    public void setKeyPassword(String val) {
        mEditor.putString(KEY_PASSWORD, val);
        mEditor.commit();
    }

    public void setKeyLoginToken(String val) {
        mEditor.putString(KEY_LOGIN_TOKEN, val);
        mEditor.commit();
    }

    public void setKeyLoginCompanyId(String val) {
        mEditor.putString(KEY_LOGIN_COMPANY_ID, val);
        mEditor.commit();
    }

    public void setKeyLoginCompanyName(String val) {
        mEditor.putString(KEY_LOGIN_COMPANY_NAME, val);
        mEditor.commit();
    }

    public void setKeyLoginMasterDeviceSn(String val) {
        mEditor.putString(KEY_LOGIN_MASTER_DEVICE_SN, val);
        mEditor.commit();
    }

    public void setKeyLoginProjectId(String val) {
        mEditor.putString(KEY_LOGIN_PROJECT_ID, val);
        mEditor.commit();
    }

    public void setKeyLoginProjectName(String val) {
        mEditor.putString(KEY_LOGIN_PROJECT_NAME, val);
        mEditor.commit();
    }

    public void setKeyLoginUserId(String val) {
        mEditor.putString(KEY_LOGIN_USER_ID, val);
        mEditor.commit();
    }

    public void setKeyLoginUserName(String val) {
        mEditor.putString(KEY_LOGIN_USER_NAME, val);
        mEditor.commit();
    }

    public void setKeyLatestRaw(String raw) {
        mEditor.putString(KEY_LATEST_RAW, raw);
        mEditor.commit();
    }

    public void setKeyLatestMspeed(String mspeed) {
        mEditor.putString(KEY_LATEST_MSPEED, mspeed);
        mEditor.commit();
    }

   /* public void setKeySlaveAlarm(boolean alarm) {
        mEditor.putBoolean(KEY_SLAVE_ALARM, alarm);
        mEditor.commit();
    }*/

    public void setKeySlavePrealarm(boolean alarm) {
        mEditor.putBoolean(KEY_SLAVE_PREALARM, alarm);
        mEditor.commit();
    }

    public void setKeyEndPourInfo(String info) {
        mEditor.putString(KEY_END_POUR_INFO, info);
        mEditor.commit();
    }

   /* public void setKeyCalCon(String val) {
        mEditor.putString(KEY_CAL_CON, val);
        mEditor.commit();
    }*/

    /*public void setKeyCalSlurry(String val) {
        mEditor.putString(KEY_CAL_SLURRY, val);
        mEditor.commit();
    }*/

    /*public void setKeyAlarmStatus(String val) {
        mEditor.putString(KEY_ALARM_STATUS, val);
        mEditor.commit();
    }*/

    public void setKeyMasterBattery(int batt) {
        mEditor.putInt(KEY_MASTER_BATTERY, batt);
        mEditor.commit();
    }

    public void setKeyUseList(List<String> val) {
        String listString = "";
        for (int i = 0; i < val.size() - 1; i++) {
            listString += val.get(i) + ",";
        }
        listString += val.get(val.size() - 1);
        mEditor.putString(KEY_USE_LIST, listString);
        mEditor.commit();
    }

    public void setKeySlaveStatusList(List<SlaveStatusList.SlaveStatus> statusList) {
        Gson gson = new Gson();
        SlaveStatusList slaveStatusList = new SlaveStatusList();
        slaveStatusList.slaveStatusList = statusList;
        String slaveListString = gson.toJson(slaveStatusList, SlaveStatusList.class);
        mEditor.putString(KEY_USE_NODESTA, slaveListString);
        mEditor.commit();
    }

    public void setKeyMtrsd0(String val) {
        mEditor.putString(KEY_MTRSD0, val);
        mEditor.commit();
    }

    public void setKeyMtrsd1(String val) {
        mEditor.putString(KEY_MTRSD1, val);
        mEditor.commit();
    }

    public void setKeyMtrsd2(String val) {
        mEditor.putString(KEY_MTRSD2, val);
        mEditor.commit();
    }

    public void setKeyMode(String val) {
        mEditor.putString(KEY_MODE, val);
        mEditor.commit();
    }

    public void setKeyCalTrsd0(String val) {
        mEditor.putString(KEY_CAL_TRSD0, val);
        mEditor.commit();
    }

    public void setKeyCalTrsd1(String val) {
        mEditor.putString(KEY_CAL_TRSD1, val);
        mEditor.commit();
    }

    public void setKeyCalTrsd2(String val) {
        mEditor.putString(KEY_CAL_TRSD2, val);
        mEditor.commit();
    }

    public void setKeyCalTrsd3(String val) {
        mEditor.putString(KEY_CAL_TRSD3, val);
        mEditor.commit();
    }

    public void setKeyCalMac(String val) {
        mEditor.putString(KEY_CAL_MAC, val);
        mEditor.commit();
    }

    public void setKeyMatchList(String[] list) {
        String val = "";
        if(list.length > 0) {
            for(int i = 0; i < list.length; i++) {
                val += list[i] + ",";
            }
            //val += list[list.length - 1];
            val=val.substring(0,val.length()-1);//star--去除最后一个,
            mEditor.putString(KEY_MATCH_LIST, val);
            mEditor.commit();
        }
    }

    public void setKeyAlmsta(String val) {
        mEditor.putString(KEY_ALMSTA, val);
        mEditor.commit();
    }

}
