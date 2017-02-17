#include <jni.h>
#include "bspatch.h"
#include <stdlib.h>
#include "log.h"

JNIEXPORT jint JNICALL Java_com_dodola_patcher_utils_AppUtils_patcher(JNIEnv* env,
		jobject othis, jstring argv1, jstring argv2, jstring argv3) {
	const char ** argv;
	int loopVar,result;

	LOGD("Patcher","开始合并");
	argv = (const char**) malloc(4 * sizeof(char*));
	for (loopVar = 0; loopVar < 4; loopVar++) {
		argv[loopVar] = (char*) malloc(100 * sizeof(char));
	}
	argv[0] = "bspatch";
	argv[1] = (*env)->GetStringUTFChars(env, argv1, 0);
	argv[2] = (*env)->GetStringUTFChars(env, argv2, 0);
	argv[3] = (*env)->GetStringUTFChars(env, argv3, 0);

	result=bs_patch(argv);

	//Important:释放内存，防止内存泄露
	(*env)->ReleaseStringUTFChars(env, argv1, argv[1]);
	(*env)->ReleaseStringUTFChars(env, argv2, argv[2]);
	(*env)->ReleaseStringUTFChars(env, argv3, argv[3]);
	return result;
}
