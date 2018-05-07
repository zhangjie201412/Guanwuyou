//
// Created by H151136 on 1/17/2018.
//

#include <jni.h>
#include <string>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/types.h>
#include <fcntl.h>
#include <android/log.h>
#include <string.h>
#include <errno.h>

#define TAG     "SerialPort"

#define LOGI(fmt, args...) __android_log_print(ANDROID_LOG_INFO,  TAG, fmt, ##args)
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)

static speed_t getBaudrate(jint baudrate) {
    switch (baudrate) {
        case 0:
            return B0;
        case 50:
            return B50;
        case 75:
            return B75;
        case 110:
            return B110;
        case 134:
            return B134;
        case 150:
            return B150;
        case 200:
            return B200;
        case 300:
            return B300;
        case 600:
            return B600;
        case 1200:
            return B1200;
        case 1800:
            return B1800;
        case 2400:
            return B2400;
        case 4800:
            return B4800;
        case 9600:
            return B9600;
        case 19200:
            return B19200;
        case 38400:
            return B38400;
        case 57600:
            return B57600;
        case 115200:
            return B115200;
        case 230400:
            return B230400;
        case 460800:
            return B460800;
        case 500000:
            return B500000;
        case 576000:
            return B576000;
        case 921600:
            return B921600;
        case 1000000:
            return B1000000;
        case 1152000:
            return B1152000;
        case 1500000:
            return B1500000;
        case 2000000:
            return B2000000;
        case 2500000:
            return B2500000;
        case 3000000:
            return B3000000;
        case 3500000:
            return B3500000;
        case 4000000:
            return B4000000;
        default:
            return -1;
    }
}

extern "C"
JNIEXPORT jobject
JNICALL
Java_com_iot_serialport_SerialPort_open(
        JNIEnv *env,
        jobject /* this */,
        jstring path,
        jint baundrate) {
    int fd;
    speed_t speed;
    jobject fileDescriptor;
    LOGD("%s: E", __func__);
    {
        speed = getBaudrate(baundrate);
        if(speed == -1) {
            LOGE("%s: Invalid baudrate %d", __func__, baundrate);
            return NULL;
        }
    }
    {
        jboolean isCopy;
        const char *pathUtf = env->GetStringUTFChars(path, &isCopy);
        LOGD("%s: Opening serial port %s", __func__, pathUtf);
        fd = ::open("/dev/ttymxc2", O_RDWR/* | O_DIRECT | O_SYNC*/);
        env->ReleaseStringUTFChars(path, pathUtf);
        if(fd < 0) {
            LOGE("%s: Failed to open %s, fd = %d, %s", __func__, pathUtf, fd, ::strerror(errno));
            return NULL;
        }
    }
    //config device
    {
        struct termios cfg;
        LOGD("%s: Configuring serial port", __func__);
        if(::tcgetattr(fd, &cfg)) {
//            LOGE("%s: Failed to tcgetattr", __func__);
//            ::close(fd);
//            return NULL;
            ::memset(&cfg, 0x00, sizeof(cfg));
        }

//        cfsetispeed(&cfg, speed);
//        cfsetospeed(&cfg, speed);
        cfg.c_cflag = speed | CS8 | CLOCAL | CREAD;
        cfg.c_oflag &= ~OPOST;
        cfg.c_iflag = IGNPAR;
        cfg.c_lflag = 0;
        cfg.c_cc[VTIME] = 0;
        cfg.c_cc[VMIN] = 1;

        if(::tcsetattr(fd, TCSANOW, &cfg)) {
            LOGE("%s: Failed to tcsetattr", __func__);
            ::close(fd);
            return NULL;
        }
        ::tcflush(fd, TCIFLUSH);
    }
    /* Create a corresponding file descriptor */
    {
        jclass cFileDescriptor = env->FindClass("java/io/FileDescriptor");
        jmethodID iFileDescriptor = env->GetMethodID(cFileDescriptor, "<init>", "()V");
        jfieldID descriptorID = env->GetFieldID(cFileDescriptor, "descriptor", "I");
        fileDescriptor = env->NewObject(cFileDescriptor, iFileDescriptor);
        env->SetIntField(fileDescriptor, descriptorID, (jint)fd);
    }
    LOGD("%s: X", __func__);

    return fileDescriptor;
}

extern "C"
JNIEXPORT void JNICALL Java_com_iot_serialport_SerialPort_close
        (JNIEnv *env, jobject thiz)
{
    jclass SerialPortClass = env->GetObjectClass(thiz);
    jclass FileDescriptorClass = env->FindClass("java/io/FileDescriptor");

    jfieldID mFdID = env->GetFieldID(SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
    jfieldID descriptorID = env->GetFieldID(FileDescriptorClass, "descriptor", "I");

    jobject mFd = env->GetObjectField(thiz, mFdID);
    jint descriptor = env->GetIntField(mFd, descriptorID);

    LOGD("close(fd = %d)", descriptor);
    close(descriptor);
}