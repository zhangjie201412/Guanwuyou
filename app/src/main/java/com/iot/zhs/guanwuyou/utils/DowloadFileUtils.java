package com.iot.zhs.guanwuyou.utils;

import android.content.Context;
import android.nfc.Tag;
import android.os.StatFs;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.iot.zhs.guanwuyou.view.WaitProgressDialog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.litepal.util.LogUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by star on 2018/5/16.
 */

public class DowloadFileUtils {
    //文件
    private static final String FILE_FOLDER = "AndroidGZZ20";
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + FILE_FOLDER + "/";
    private String filePath = "";
    private Context context;
    private Toast mToast;
    private WaitProgressDialog mProgressDialog;

    public DowloadFileUtils(Context context) {
        this.context = context;
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        mProgressDialog = new WaitProgressDialog(context);

    }

    public void downloadFile(String token, String loginName, String url) {
        //  url = Utils.SERVER_ADDR + "/protocol/doProcessProtocolInfo/cc/";

        mProgressDialog.show();

        String[] urlArray=url.split("/");
        String fileName = urlArray[urlArray.length-1];
        File file = new File(BASE_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
        boolean flag = FileUtils.searchOldFile(fileName, file);
        if (flag) {//已经存在不需要下载，直接打开
            openFileApp(fileName);
        } else {
            OkHttpUtils.post().url(url)
                    .build()
                    .execute(new FileCallBack(BASE_PATH, fileName) {
                        @Override
                        public void onError(Call call, Exception e, int id) {
                            showToast("下载错误："+e.getMessage());
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                        }

                        @Override
                        public void onResponse(File response, int id) {
                            if (mProgressDialog.isShowing()) {
                                mProgressDialog.dismiss();
                            }
                            openFileApp(response.getName());

                        }

                        @Override
                        public void inProgress(float progress, long total, int id) {
                            super.inProgress(progress, total, id);
                            if(total>FileUtils.availableSize()){
                                //存储空间不足
                                showToast("存储空间不足");
                                return;
                            }
                        }
                    });
        }
    }

    /**
     * 用与文件匹配的应用打开文件
     *
     * @param fileName
     */
    private void openFileApp(String fileName) {
        String endStr = fileName.split("\\.")[1];

        if (endStr.toLowerCase().equals("apk")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //目前版本固定
                uri = FileProvider.getUriForFile(context, "com.iot.zhs.guanwuyou.fileprovider", new File(BASE_PATH, fileName));
                //添加这一句表示对目标应用临时授权该Uri所代表的文件
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(new File(BASE_PATH, fileName));
                String[] command = {"chmod", "777", new File(BASE_PATH, fileName).getPath() };
                ProcessBuilder builder = new ProcessBuilder(command);
                try {
                    builder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            Log.d("aa", "FileUtils.getMIMEType(fileName)=" + FileUtils.getMIMEType(fileName));
            intent.setDataAndType(uri, FileUtils.getMIMEType(fileName));

            try {
                ((Activity) context).startActivity(intent);
            } catch (Exception e) {
                showToast("无法打开后缀名为." + endStr.toLowerCase() + "的文件！");
            }
        }

        //  /storage/emulated/0/AndroidGZZ20/rdp.apk

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


    private void showToast(String msg) {
        mToast.setText(msg);
        mToast.show();
    }
}
