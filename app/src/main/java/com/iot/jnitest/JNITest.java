package com.iot.jnitest;

public class JNITest {
    public native int getPowerOnPinValue();

    public native int setCpuAndLoraValue(int cpu,int lora);

    static {
        System.loadLibrary("iot_sample");
    }


}
