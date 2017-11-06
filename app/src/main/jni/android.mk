LOCAL_PATH := $(call my-dir) #my-dir就是该Android.mk所在目录，本项目中即jni目录

include $(CLEAR_VARS)
LOCAL_MODULE    := serial_port  #自己由源文件编译库的模块名
LOCAL_SRC_FILES := SerialPort.c    #将被编译的源文件
LOCAL_LDLIBS += -llog
include $(BUILD_SHARED_LIBRARY) #表示编译成.so共享库，即动态库