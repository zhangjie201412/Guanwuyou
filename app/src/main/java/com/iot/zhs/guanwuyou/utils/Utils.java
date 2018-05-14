package com.iot.zhs.guanwuyou.utils;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

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

   // public static final String SERVER_ADDR = "http://61.177.48.122:8685";
    public static final String SERVER_ADDR = "http://10.10.18.128:8081";//星星


    public static final String SLAVE_DEVICE_RECORD = "ZHS";

    public static void doProcessProtocolInfo(String token, String loginName, String str, StringCallback callback) {
        String url = Utils.SERVER_ADDR + "/protocol/doProcessProtocolInfo/cc/";
        SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());

        OkHttpUtils.post().url(url)
                .addParams("protocolStr", str)
                .addParams("timeStamp", date)
                .build()
                .execute(
                        callback
                );
    }

    public static void resendProtocolInfo() {

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
}
