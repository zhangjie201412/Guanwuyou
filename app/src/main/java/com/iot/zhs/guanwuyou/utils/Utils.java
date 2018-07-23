package com.iot.zhs.guanwuyou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.protocol.ProtocolPackage;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by H151136 on 1/22/2018.
 */

public class Utils {
    public static final int HTTP_TIMEOUT = 20000;

    public static final int UI_SHOW_TOAST = 0x1001;
    public static final String MSG_CODE_OK = "1";

    public static final int MASTER_HAS_REPORT_NEW_TASK = 0;
    public static final int MASTER_HAS_REPORT_CHECK_REPORT1 = 1;
    public static final int MASTER_HAS_REPORT_CHECK_REPORT2 = 2;

    //public static final String SERVER_ADDR = "http://61.177.48.122:8181";
     public static final String SERVER_ADDR = "http://10.10.18.128:8081";//星星
    // public static final String SERVER_ADDR = "http://10.10.58.242:8081";//小童


    public static final String SLAVE_DEVICE_RECORD = "ZHS";
    private static final String TAG = "ZSH.IOT";

    public interface ResponseCallback {
        void onResponse(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse);
    }
    public interface HttpCallback {
        void onResponseCallback(String response, ProcessProtocolInfo processProtocolInfo, ProtocolPackage pkgResponse);
    }


    public static void doProcessProtocolInfo(ProtocolPackage pkg, final ResponseCallback responseCallback) {
        String url = Utils.SERVER_ADDR + "/protocol/doProcessProtocolInfo/cc/";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());


        HttpClient httpClient= new HttpClient(pkg,responseCallback);
        httpClient.doSendProtocolInfo();
    }




    public static int calcCheckSum(String data, int len) {
        int cs = 0x00;
        byte[] bytes = data.getBytes();

        for (int i = 0; i < len; i++) {
            cs = cs ^ (char) bytes[i];
        }
        return cs;
    }

    public final static String encrypt(String plaintext) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] btInput = plaintext.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 字符串转int
     *
     * @param string
     * @return
     */
    public static int stringToInt(String string) {
        if (string == null || string.equals("")) {
            string = "0";
        }
        return Integer.parseInt(string);
    }

    /**
     * 判断list是否为空
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> boolean listIsEmpty(List<T> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 字符串转浮点
     *
     * @param string
     * @return
     */
    public static float stringToFloat(String string) {
        if (string == null || string.equals("")) {
            string = "0";
        }
        return Float.parseFloat(string);
    }

    /**
     * 隐藏键盘
     *
     * @param context
     */
    public static void hideSoftKeyboard(Activity context) {
        // 先隐藏键盘
        InputMethodManager manager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);

        if (context.getCurrentFocus() != null && context.getCurrentFocus().getWindowToken() != null) {
            manager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 深拷贝
     *
     * @param src
     * @param <T>
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> List<T> deepCopy(List<T> src) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        List<T> dest = null;
        try {
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            dest = (List<T>) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dest;
    }

    /**
     * 判断字符串是否为空
     *
     * @param string
     * @return
     */
    public static boolean stringIsEmpty(String string) {
        if (string == null || string.equals("")) {
            return true;
        }
        return false;
    }


    /**
     * 获取当前程序的版本名
     *
     * @return
     * @throws Exception
     */
    public static String getVersionName(Context context) {
        //获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        //getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("TAG", "版本号" + packInfo.versionCode);
        Log.d("TAG", "版本名" + packInfo.versionName);
        return packInfo.versionName;
    }

    /**
     * 判断两个集合里面的元素是否一致
     *
     * @param a
     * @param b
     * @param <T>
     * @return
     */
    public static <T extends Comparable<T>> boolean compare(List<T> a, List<T> b) {
        if (a.size() != b.size())
            return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i)))
                return false;
        }
        return true;
    }

    /**
     * 字符串集合转string
     *
     * @param stringList
     * @param splitStr
     * @return
     */
    public static String listToString(List<String> stringList, String splitStr) {
        if (stringList == null || stringList.size() == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < stringList.size(); i++) {
            buffer.append(stringList.get(i)).append(splitStr);
        }
        String value = buffer.substring(0, buffer.length() - 1);
        return value;
    }
    /**
     * 获取app的版本号
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        try {
            String pkName = context.getPackageName();
            String versionName = context.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;
            return  versionName ;
        } catch (Exception e) {
        }
        return null;
    }

}
