#ifndef MICRORAPID_LOG_H
#define MICRORAPID_LOG_H

#include<android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,__VA_ARGS__)
#endif

