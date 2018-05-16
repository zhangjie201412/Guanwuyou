package com.iot.zhs.guanwuyou.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by star on 2017/4/12.
 */

public class FileUtils {

    final static String[][] MIME_MapTable={
            //{后缀名，MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bin",    "application/octet-stream"},
            {".bmp",    "image/bmp"},
            {".c",      "text/plain"},
            {".class",  "application/octet-stream"},
            {".conf",   "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls",    "application/vnd.ms-excel"},
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe",    "application/octet-stream"},
            {".gif",    "image/gif"},
            {".gtar",   "application/x-gtar"},
            {".gz",     "application/x-gzip"},
            {".h",      "text/plain"},
            {".htm",    "text/html"},
            {".html",   "text/html"},
            {".jar",    "application/java-archive"},
            {".java",   "text/plain"},
            {".jpeg",   "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js",     "application/x-javascript"},
            {".log",    "text/plain"},
            {".m3u",    "audio/x-mpegurl"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp2",    "audio/x-mpeg"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpc",    "application/vnd.mpohun.certificate"},
            {".mpe",    "video/mpeg"},
            {".mpeg",   "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",   "video/mp4"},
            {".mpga",   "audio/mpeg"},
            {".msg",    "application/vnd.ms-outlook"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".pptx",   "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop",   "text/plain"},
            {".rc",     "text/plain"},
            {".rmvb",   "audio/x-pn-realaudio"},
            {".rtf",    "application/rtf"},
            {".sh",     "text/plain"},
            {".tar",    "application/x-tar"},
            {".tgz",    "application/x-compressed"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            {".xml",    "text/plain"},
            {".z",      "application/x-compress"},
            {".zip",    "application/x-zip-compressed"},
            {"",        "*/*"}
    };



    /**
     * 获取网络文件大小
     *
     * @param path
     *            文件链接
     * @return 文件大小
     */
    public int downloadFileSize(final Context context, final String path) {
        final int[] length = {0};
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(path);     //创建url对象
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();     //建立连接
                    conn.setRequestMethod("GET");    //设置请求方法
                    conn.setReadTimeout(5000);       //设置响应超时时间
                    conn.setConnectTimeout(5000);   //设置连接超时时间
                    conn.connect();   //发送请求
                    int responseCode = conn.getResponseCode();    //获取响应码
                    if (responseCode == 200) {   //响应码是200(固定值)就是连接成功，否者就连接失败
                        length[0] = conn.getContentLength();    //获取文件的大小
                    } else {
                        Toast.makeText(context, "连接失败", Toast.LENGTH_LONG)
                                .show();
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        }.start();
        return length[0];
    }


    /**
     * 获取手机剩余内存大小
     *
     * @return 手机剩余内存(单位：byte)
     */
    public static long availableSize() {
        // 取得SD卡文件路径
        File file = Environment.getExternalStorageDirectory();
        StatFs fs = new StatFs(file.getPath());
        // 获取单个数据块的大小(Byte)
        int blockSize = fs.getBlockSize();
        // 空闲的数据块的数量
        long availableBlocks = fs.getAvailableBlocks();
        return blockSize * availableBlocks;
    }


    /**
     * 获取声音文件名字
     *
     * @return 假如当前录制声音时间是2016年6月1日11点30分30秒。得到的文件名字就是20160601113030.这样保证文件名的唯一性
     */
    public static String geFileName() {
        long getNowTimeLong = System.currentTimeMillis();
        SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHHmmss");
        String result = time.format(getNowTimeLong);
        return result;
    }


    /**
     * 根据文件后缀名获得对应的MIME类型。
     * @param fileName
     */
    public static String getMIMEType(String fileName) {

        String type="*/*";
      //  String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fileName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
    /* 获取文件的后缀名*/
        String end=fileName.substring(dotIndex,fileName.length()).toLowerCase();
        if(end=="")return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0;i<MIME_MapTable.length;i++){ //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if(end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }


    /*
 * searchFile 查找文件
 *  @String keyword 查找的关键词
 *  @File filepath  查找的目录
 * */
    public static boolean searchOldFile(String keyword,File filepath) {

        //判断SD卡是否存在
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File[] files = filepath.listFiles();
            if(files==null){
                return false;
            }
            if (files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        //如果目录可读就执行（一定要加，不然会挂掉）
                        if(file.canRead()){
                            searchOldFile(keyword,file);  //如果是目录，递归查找
                        }
                    } else {
                        //判断是文件，则进行文件名判断
                        try {
                            if (file.getName().equals(keyword) ) {
                                return true;
                                // HashMap rowItem = new HashMap<String, Object>();
                               // rowItem.put("bookName", file.getName());// 加入名称
                               // rowItem.put("path", file.getPath());  // 加入路径
                               // rowItem.put("size", file.length());   // 加入文件大小
                            }
                        } catch(Exception e) {
                           // Toast.makeText(this,"查找发生错误", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }
}
