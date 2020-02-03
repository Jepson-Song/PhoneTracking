#include <string.h>
#include <jni.h>

jstring
Java_jni_JniPlug_getNativeSring(JNIEnv *env, jobject thiz, int x, int y) {
    return  (*env)->NewStringUTF(env, "I`m Str !!");
}
