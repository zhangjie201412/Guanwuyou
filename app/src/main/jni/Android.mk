LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := iot_sample.c

LOCAL_MODULE_TAGS := optional
LOCAL_MODULE := iot_sample

include $(BUILD_EXECUTABLE)
