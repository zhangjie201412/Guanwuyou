// ISerialPort.aidl
package com.iot.zhs.guanwuyou;

// Declare any non-default types here with import statements

interface ISerialPort {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void ping(int count);
    void setPowerUp();
    void setPowerDown();
    void requestCalMac();
    void matchList();
    void requestMode();
}
