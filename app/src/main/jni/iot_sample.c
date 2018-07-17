#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>

#include <sys/ioctl.h>
#include <linux/ioctl.h>
#include <jni.h>

#define IOT_POWER_ON_PIN    171
#define IOT_CPU_ON_PIN      103
#define IOT_LORA_EN_PIN     38

struct iot_gpio_cmd {
    int gpio;
    int value;
};

#define IOT_MAGIC 'I'
#define IOT_SET_GPIO        _IOR(IOT_MAGIC, 0x01, struct iot_gpio_cmd)
#define IOT_GET_GPIO        _IOWR(IOT_MAGIC, 0x02, struct iot_gpio_cmd)

#define IOT_DEV_PATH        "/dev/iot"

int gpio_set_value(int fd, struct iot_gpio_cmd *cmd)
{
    return ioctl(fd, IOT_SET_GPIO, cmd);
}

int gpio_get_value(int fd, struct iot_gpio_cmd *cmd)
{
    return ioctl(fd, IOT_GET_GPIO, cmd);
}

int main(void)
{
    int fd = -1;
    struct iot_gpio_cmd cmd;

    fd = open(IOT_DEV_PATH, O_RDWR);
    if(fd < 0) {
        printf("failed to open %s\n", IOT_DEV_PATH);
        return -1;
    }

    cmd.gpio = IOT_LORA_EN_PIN;
    cmd.value = 1;
    gpio_set_value(fd, &cmd);

    cmd.gpio = IOT_POWER_ON_PIN;
    cmd.value = 0;
    gpio_get_value(fd, &cmd);
    printf("power on pin get value: %d\n", cmd.value);

    close(fd);
    return 0;
}


JNIEXPORT jint JNICALL
Java_com_iot_jnitest_JNITest_setCpuAndLoraValue(JNIEnv *env, jobject instance, jint cpu,
                                                jint lora) {

    // TODO
    int fd = -1;
    struct iot_gpio_cmd cmd;

    fd = open(IOT_DEV_PATH, O_RDWR);
    if(fd < 0) {
        printf("failed to open %s\n", IOT_DEV_PATH);
        return -1;
    }

    cmd.gpio = IOT_LORA_EN_PIN;
    cmd.value = cpu;
    gpio_set_value(fd, &cmd);


    cmd.gpio = IOT_CPU_ON_PIN;
    cmd.value = lora;
    gpio_set_value(fd, &cmd);

    close(fd);
    return 0;
}


JNIEXPORT jint JNICALL
Java_com_iot_jnitest_JNITest_getPowerOnPinValue(JNIEnv *env, jobject instance) {

    // TODO
    int fd = -1;
    struct iot_gpio_cmd cmd;

    fd = open(IOT_DEV_PATH, O_RDWR);
    if(fd < 0) {
        printf("failed to open %s\n", IOT_DEV_PATH);
        return -1;
    }

    cmd.gpio = IOT_POWER_ON_PIN;
    cmd.value = 0;
    gpio_get_value(fd, &cmd);
    printf("power on pin get value: %d\n", cmd.value);

    close(fd);
    return cmd.value;

}

