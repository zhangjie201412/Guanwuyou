package com.iot.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by H151136 on 1/17/2018.
 */

public class SerialPort {
    private static final String TAG = "SerialPort";

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOputputStream;

    public SerialPort(File device, int baudrate) throws SecurityException, IOException {
        Log.d(TAG, "read = " + device.canRead() + ", write = " + device.canWrite());
        if(!device.canRead() || !device.canWrite()) {
            Log.e(TAG, "Device " + device.getName() + " permission error!");
            throw new SecurityException();
        }

        mFd = open(device.getAbsolutePath(), baudrate);
        if(mFd == null) {
            Log.e(TAG, "Native open return null");
            throw new IOException();
        }

        mFileInputStream = new FileInputStream(mFd);
        mFileOputputStream = new FileOutputStream(mFd);
    }

    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOputputStream;
    }

    private native static FileDescriptor open(String path, int baudrate);
    public native void close();
    static {
        System.loadLibrary("serial_port");
    }
}
