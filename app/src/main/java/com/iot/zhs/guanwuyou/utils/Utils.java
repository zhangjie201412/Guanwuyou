package com.iot.zhs.guanwuyou.utils;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static final String SERVER_ADDR = "http://61.177.48.122:8685";

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

}
