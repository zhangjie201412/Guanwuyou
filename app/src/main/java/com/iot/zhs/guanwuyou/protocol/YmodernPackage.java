package com.iot.zhs.guanwuyou.protocol;

import android.util.Log;

import com.iot.zhs.guanwuyou.MyApplication;
import com.iot.zhs.guanwuyou.comm.http.ProcessProtocolInfo;
import com.iot.zhs.guanwuyou.database.SlaveDevice;
import com.iot.zhs.guanwuyou.utils.FileUtils;
import com.iot.zhs.guanwuyou.utils.MessageEvent;
import com.iot.zhs.guanwuyou.utils.Utils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;

/**
 * Created by H151136 on 1/25/2018.
 */

public class YmodernPackage {
    private static final String TAG = "ZSH.IOT";
    public static final String MESG_START_TO_UPDATE_MASTER = "start to update master firmware";
    public static final String MESG_START_TO_UPDATE_SLAVE = "start to update slave firmware";
    public static final String MESG_UART_SWITCH_TO_SLAVE = "uart switch to slave";
    public static final String MESG_MASTER_UPDATE_FAIL = "master update fail";
    public static final String MESG_MASTER_UPDATE_SUCCESS = "master update success";
    public static final String MESG_SLAVE_UPDATE_FAIL = "slave update fail";
    public static final String MESG_SLAVE_UPDATE_SUCCESS = "slave update success";
    public static final String MESG_SLAVE_SN_ERR = "Sn is not match";

    public static final String MESG_ACK = "ACK";
    public static final String MESG_C = "STARTC";
    public static final String MESG_NAK = "NAK";
    public static final String MESG_CA = "CA";

    public static final byte SOH = 0x01;  /* start of 128-byte data packet */
    public static final byte STX = 0x02;  /* start of 1024-byte data packet */
    public static final byte EOT = 0x04;  /* end of transmission */
    public static final byte ACK = 0x06;  /* acknowledge */
    public static final byte NAK = 0x15; /* negative acknowledge */
    public static final byte CA = 0x18; /* two of these in succession aborts transfer */
    public static final byte C = 0x43;  /* 'C' == 0x43, request 16-bit CRC */

    public int PACKET_HEADER = 3;
    public int PACKET_CRC_H = 1;
    public int PACKET_CRC_L = 1;
    public int PACKET_TRAILER = (PACKET_CRC_H + PACKET_CRC_L);    //for CRC16
    public int PACKET_OVERHEAD = (PACKET_HEADER + PACKET_TRAILER);    // 5 bytes

    public int STX_PACKET_SIZE = 1024;
    public int SOH_PACKET_SIZE = 128;

    public int PACKET_1K_SIZE = STX_PACKET_SIZE;
    public int SEND_PACKET_SIZE = (PACKET_1K_SIZE + PACKET_OVERHEAD);    //1029 bytes
    public int SOH_TYPE_GROUP_SIZE = (PACKET_OVERHEAD + SOH_PACKET_SIZE);    //133 bytes
    public int STX_TYPE_GROUP_SIZE = (PACKET_OVERHEAD + STX_PACKET_SIZE); //1029 bytes

    public byte m_package[] = new byte[SOH_TYPE_GROUP_SIZE];

    private String fileName = "";
    private String filePath;
    private int updateFlag = -1;
    public File file;//文件
    private String mRawData;
    private int count = 0;// 文件大小/1024 取整
    private int lastPackageSize = 0;//文件大小/1024 取余，即最后一笔文件数据的大小
    private byte[] fileByte;//文件的字节数组
    private String deviceSN;//sn号

    private int iIndexOfPackage = 0;//数据发送的index
    private boolean isEOTSended = false;//EOT数据是否发送
    private boolean isZeroSended = false;//zero数据是否发送
    private boolean isSOHSended = false;

    private boolean isUart=false;


    private YmodernPackage() {
    }

    private static volatile YmodernPackage instance = null;

    public static YmodernPackage getInstance() {
        if (instance == null) {
            synchronized (YmodernPackage.class) {
                if (instance == null) {
                    instance = new YmodernPackage();
                }
            }
        }
        return instance;
    }

    public boolean isUart() {
        return isUart;
    }

    public void setUpdateFlag(int updateFlag) {
        this.updateFlag = updateFlag;

        if (updateFlag == 0) {//主机
            fileName = "gz02Project-master.bin";

        } else if (updateFlag == 1) {//从机
            fileName = "gz02Project-slave.bin";
        }
    }

    public void setFilePath(String filePath) {
        //adb shell 目录 /storage/emulated/legacy/AndroidGZZ20
        // filePath = "/storage/emulated/0/AndroidGZZ20/15308452348744b3987a4-07bd-4881-a5e3-7f8b0cc96a3b.bin";

        this.filePath=filePath;
        File oldFile = new File(filePath);
        file = new File(oldFile.getParent() + "/" + fileName);
        copyFile(oldFile, file);

        fileByte = toByteArray(file);
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public void setmRawData(String rawData) {
        this.mRawData = rawData.replaceAll("\r", "").replaceAll("\n", "").trim();
       /* Log.d(TAG,"YmodernPackage---mRawData=="+mRawData);

        if(mRawData.equals("")){
            Log.d(TAG,"YmodernPackage---mRawData==空");
        }*/
    }


    public void parse() {

        //parse the raw data
        if (!mRawData.contains(",")) {
            Log.d(TAG, "YmodernPackage--parse: " + mRawData);
            switch (mRawData) {
                case MESG_START_TO_UPDATE_MASTER://主机准备升级
                    clearData();
                    break;
                case MESG_START_TO_UPDATE_SLAVE://从机准备升级
                    clearData();
                    break;
                case MESG_UART_SWITCH_TO_SLAVE://主机切换串口
                    isUart=true;
                    final Timer timer=new Timer();
                    timer.schedule(new TimerTask() {
                        int i=0;
                            public void run() {
                                if(i++==5){
                                    timer.cancel();
                                }
                                MessageEvent event_uart = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_UPDATE_WRITE);
                                event_uart.message = "firmware update "+deviceSN+"\r\n";
                                EventBus.getDefault().post(event_uart);
                            }
                        }, 0,1000);// 设定指定的时间time,此处为2000毫秒


                    break;
                case MESG_MASTER_UPDATE_FAIL://主机升级失败
                    Log.d(TAG, "Ym-主机升级失败");
                    //通知界面dialog销毁
                    MessageEvent m_event_fail = new MessageEvent(MessageEvent.EVENT_TYPE_MASTERL_UPDATE_FAIL);
                    m_event_fail.message = deviceSN;
                    EventBus.getDefault().post(m_event_fail);

                    break;
                case MESG_MASTER_UPDATE_SUCCESS://主机升级成功
                    //通知界面dialog销毁
                    Log.d(TAG, "Ym--主机升级成功");
                    //对应的本地文件也删除
                    deleteFile(filePath);
                    MessageEvent m_event_success = new MessageEvent(MessageEvent.EVENT_TYPE_MASTER_UPDATE_SUCCESS);
                    m_event_success.message = deviceSN;
                    EventBus.getDefault().post(m_event_success);
                    break;
                case MESG_SLAVE_UPDATE_FAIL://从机升级失败
                    //通知界面dialog销毁
                    Log.d(TAG, "Ym--从机升级失败");
                    MessageEvent s_event_fail = new MessageEvent(MessageEvent.EVENT_TYPE_SLAVE_UPDATE_FAIL);
                    s_event_fail.message = deviceSN;
                    EventBus.getDefault().post(s_event_fail);
                    break;
                case MESG_SLAVE_UPDATE_SUCCESS://从机升级成功
                    //对应的本地文件也删除
                    deleteFile(filePath);
                    //通知界面dialog销毁
                    Log.d(TAG, "Ym--从机升级成功");
                    MessageEvent s_event_success = new MessageEvent(MessageEvent.EVENT_TYPE_SLAVE_UPDATE_SUCCESS);
                    s_event_success.message = deviceSN;
                    EventBus.getDefault().post(s_event_success);

                    break;
                case MESG_SLAVE_SN_ERR://从机SN号不匹配
                    //通知界面dialog销毁
                    Log.d(TAG, "Ym--从机升级失败-SN不匹配");
                    MessageEvent s_event_err = new MessageEvent(MessageEvent.EVENT_TYPE_SLAVE_SN_ERR);
                    s_event_err.message = deviceSN;
                    EventBus.getDefault().post(s_event_err);
                    break;
                case MESG_C:
                    Log.d(TAG,"YmodernPackage--startc");
                    if (isLastPackage()) {
                        if (!isZeroSended) {
                            constructZeroPackage();
                            isZeroSended = true;
                        }
                    } else {
                        if (!isSOHSended) {
                            if (iIndexOfPackage == 0) {
                                constuctSOHPackage();
                                isSOHSended = true;
                            } else {
                                constructRealDataPackage(iIndexOfPackage);//iIndexOfPackage从1开始
                                isSOHSended = true;
                            }
                            iIndexOfPackage++;
                        }
                    }
                    break;
                case MESG_ACK:
                    isSOHSended = false;
                    if (isLastPackage()) {
                        if (!isEOTSended) {
                            constructEOTPackage();
                            isEOTSended = true;
                        }
                    } else {
                        if (iIndexOfPackage == 1) {//发送完文件后 准备发送第一个数据帧的时候，ack不响应

                        } else {
                            constructRealDataPackage(iIndexOfPackage);
                            iIndexOfPackage++;
                        }
                    }

                    break;
                case MESG_NAK:
                    constructEOTPackage();
                    isEOTSended = true;
                    break;
                case MESG_CA:
                    break;

            }
        }
    }


    private void sendPackage() {
        MessageEvent event = new MessageEvent(MessageEvent.EVENT_TYPE_SERIAL_UPDATE_WRITE);
        event.chars = m_package;
        EventBus.getDefault().post(event);
    }

    //发送数据的字数>取整+1  例如 6>4196/1024+1
    private boolean isLastPackage() {
        if (lastPackageSize > 0) {
            return iIndexOfPackage > count + 1;
        }
        if (iIndexOfPackage == 0 && count == 0) {
            return false;
        }
        return iIndexOfPackage > count;
    }

    //起始帧  采用128
    public void constuctSOHPackage() {
        Log.d(TAG, "YmodernPackage--constuctSOHPackage");

        //SOH : 01 00 FF (file name) 00 (file size)........CRC_H CRC_L
        //total 133 bytes
        m_package = null;
        m_package = new byte[SOH_TYPE_GROUP_SIZE];
        m_package[0] = SOH;
        m_package[1] = 0x00;
        m_package[2] = (byte) 0xFF;
        //文件名 foo.c
        char[] fileNameChar = fileName.toCharArray();
        // char[] fileNameChar = file.getName().toCharArray();
        byte[] fileNameByte = new String(fileNameChar).getBytes();
        int fileNameLength = fileNameByte.length;
        System.arraycopy(fileNameByte, 0, m_package, PACKET_HEADER, fileNameLength);
        m_package[PACKET_HEADER + fileNameLength] = 0x00;
        //文件大小
        //  long fileLength = 1024;
        long fileLength = getFileSize(file);
        count = (int) (fileLength / STX_PACKET_SIZE);
        lastPackageSize = (int) (fileLength % STX_PACKET_SIZE);
       /* char[] fileSizeChar = Long.toHexString(fileLength).toCharArray();//400
        byte[] fileSizeByte = new String(fileSizeChar).getBytes();*/ //先转成16进制，在转ascii
        byte[] fileSizeByte = (fileLength + "").getBytes();
        int fileSizeLength = fileSizeByte.length;
        System.arraycopy(fileSizeByte, 0, m_package, PACKET_HEADER + fileNameLength + 1, fileSizeLength);
        m_package[PACKET_HEADER + fileNameLength + 1 + fileSizeLength] = 0x00;
        //剩下的字节都用00填充，数据部分大小为128字节，除去文件名与文件大小占用的空间外，剩余的字节全部用00填充
        int fileSize = fileNameLength + 1 + fileSizeLength + 1;
        for (int i = 0; i < SOH_PACKET_SIZE - fileSize; i++) {
            m_package[PACKET_HEADER + fileSize + i] = 0x00;
        }

        int crc = CRC16(m_package, SOH_PACKET_SIZE);
        m_package[PACKET_HEADER + SOH_PACKET_SIZE + 1] = (byte) crc;//lower bytes
        m_package[PACKET_HEADER + SOH_PACKET_SIZE] = (byte) (crc >> 8);//high bytes

        //  Log.d(TAG,"YmodernPackage数据帧："+ Arrays.toString(m_package));
        //   Log.d(TAG,"YmodernPackage数据帧："+ bytesToHexString(m_package));
        sendPackage();

    }

    //iIndex从1开始
    //数据传递的数据帧
    public void constructRealDataPackage(int iIndex) {
        Log.d(TAG, "YmodernPackage--constructRealDataPackage");
        Log.d(TAG, "YmodernPackage--constructRealDataPackage--iIndex=" + iIndex);
        Log.d(TAG, "YmodernPackage--constructRealDataPackage--count=" + count);

        if (iIndex <= count) {
            m_package = null;
            m_package = new byte[STX_TYPE_GROUP_SIZE];
            m_package[0] = STX;
            int sendIndex = iIndex;
            if (iIndex >= 256) {
                sendIndex = iIndex - 256;
            }
            m_package[1] = (byte) sendIndex;
            m_package[2] = (byte) (0xFF - sendIndex);

            System.arraycopy(fileByte, (iIndex - 1) * STX_PACKET_SIZE, m_package, PACKET_HEADER, STX_PACKET_SIZE);

            int crc = CRC16(m_package, STX_PACKET_SIZE);
            m_package[PACKET_HEADER + STX_PACKET_SIZE + 1] = (byte) crc;//lower bytes
            m_package[PACKET_HEADER + STX_PACKET_SIZE] = (byte) (crc >> 8);//high bytes

            sendPackage();
        }

        //如果文件数据的最后剩余的数据在128~1024之前，则还是使用STX的1024字节传输，但是剩余空间全部用0x1A填充
        //如果文件大小小于等于128字节或者文件数据最后剩余的数据小于128字节，则YModem会选择SOH数据帧用128字节来传输数据，如果数据不满128字节，剩余的数据用0x1A填充这是数据正的结构就变成
        if (lastPackageSize > 0 && iIndex == count + 1) {//最后一笔 128
            Log.d(TAG, "YmodernPackage--constructRealDataPackage--最后一笔lastPackageSize=" + lastPackageSize);

            if (lastPackageSize <= SOH_PACKET_SIZE) {
                Log.d(TAG, "YmodernPackage--constructRealDataPackage--最后一笔--128");

                m_package = null;
                m_package = new byte[SOH_TYPE_GROUP_SIZE];
                m_package[0] = SOH;
                int sendIndex = iIndex;
                if (iIndex >= 256) {
                    sendIndex = iIndex - 256;
                }

                m_package[1] = (byte) sendIndex;
                m_package[2] = (byte) (0xFF - sendIndex);
                System.arraycopy(fileByte, count * STX_PACKET_SIZE, m_package, PACKET_HEADER, lastPackageSize);

                if (lastPackageSize < SOH_PACKET_SIZE) {
                    for (int i = 0; i < SOH_PACKET_SIZE - lastPackageSize; i++) {
                        m_package[PACKET_HEADER + lastPackageSize + i] = 0x1A;
                    }
                }

                int crc = CRC16(m_package, SOH_PACKET_SIZE);
                m_package[PACKET_HEADER + SOH_PACKET_SIZE + 1] = (byte) crc;//lower bytes
                m_package[PACKET_HEADER + SOH_PACKET_SIZE] = (byte) (crc >> 8);//high bytes
            } else {//1024
                Log.d(TAG, "YmodernPackage--constructRealDataPackage--最后一笔--1024");

                m_package = null;
                m_package = new byte[STX_TYPE_GROUP_SIZE];
                m_package[0] = STX;
                int sendIndex = iIndex;
                if (iIndex >= 256) {
                    sendIndex = iIndex - 256;
                }
                m_package[1] = (byte) sendIndex;
                m_package[2] = (byte) (0xFF - sendIndex);
                System.arraycopy(fileByte, count * STX_PACKET_SIZE, m_package, PACKET_HEADER, lastPackageSize);

                for (int i = 0; i < STX_PACKET_SIZE - lastPackageSize; i++) {
                    m_package[PACKET_HEADER + lastPackageSize + i] = 0x1A;
                }

                int crc = CRC16(m_package, STX_PACKET_SIZE);
                m_package[PACKET_HEADER + STX_PACKET_SIZE + 1] = (byte) crc;//lower bytes
                m_package[PACKET_HEADER + STX_PACKET_SIZE] = (byte) (crc >> 8);//high bytes
            }

            sendPackage();
        }
    }

    //EOT
    public void constructEOTPackage() {
        Log.d(TAG, "YmodernPackage--constructEOTPackage");

        m_package = null;
        m_package = new byte[1];
        m_package[0] = EOT;
        sendPackage();
    }

    //空的数据帧
    public void constructZeroPackage() {
        Log.d(TAG, "YmodernPackage--constructZeroPackage");
        m_package = null;
        m_package = new byte[SOH_TYPE_GROUP_SIZE];
        m_package[0] = SOH;
        m_package[1] = 0x00;
        m_package[2] = (byte) 0xFF;
        for (int i = 0; i < SOH_PACKET_SIZE; i++) {
            m_package[PACKET_HEADER + i] = 0x00;
        }

        int crc = CRC16(m_package, SOH_PACKET_SIZE);
        m_package[PACKET_HEADER + SOH_PACKET_SIZE + 1] = (byte) crc;//lower bytes
        m_package[PACKET_HEADER + SOH_PACKET_SIZE] = (byte) (crc >> 8);//high bytes
        sendPackage();
    }


    //crc校验  去除帧头3个字节后 剩余128或者1024的校验
    public int CRC16(byte[] mbytes, int len) {

        byte[] bytes = new byte[len];
        System.arraycopy(mbytes, PACKET_HEADER, bytes, 0, len);

        int crc = 0;
        int i, j;
        for (i = 0; i < len; i++) {
            crc = crc ^ bytes[i] << 8;
            for (j = 0; j < 8; j++) {
                if ((crc & ((int) 0x8000)) != 0)
                    crc = crc << 1 ^ 0x1021;
                else
                    crc = crc << 1;
            }
        }
        return (crc & 0xFFFF);
    }

    /*读取文件的字节数组*/
    public static byte[] toByteArray(File file) {
        File f = file;
        if (!f.exists()) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取文件长度
     *
     * @param file
     */
    public static long getFileSize(File file) {
        if (file == null) {
            return 0;
        }
        if (file.exists() && file.isFile()) {
            return file.length();
        }
        return 0;
    }

    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    /**
     * 数组转换成十六进制字符串
     *
     * @param
     * @return HexString
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase() + " ");
        }
        return sb.toString();
    }

    public void clearData() {
        count = 0;// 文件大小/1024 取整
        lastPackageSize = 0;//文件大小/1024 取余，即最后一笔文件数据的大小
        iIndexOfPackage = 0;//数据发送的index
        isEOTSended = false;//EOT数据是否发送
        isZeroSended = false;//zero数据是否发送
        isSOHSended = false;
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static boolean copyFile(File fromFile, File toFile) {
        if (!fromFile.exists()) {
            return false;
        }
        if (toFile.exists()) {
            toFile.delete();
        }

        try {
            toFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 删除单个文件
     *
     * @param filePath
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static void deleteFile(String filePath) {
        Log.d(TAG,"Ym-filePath--"+filePath);

        if(!Utils.stringIsEmpty(filePath)) {
            File file = new File(filePath);
            // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
            if (file.exists() && file.isFile()) {
               if(file.delete()){
                   Log.d(TAG,"Ym-filePath--删除成功");
               }else{
                   Log.d(TAG,"Ym-filePath--删除失败");
               }

            }
        }
    }



}
