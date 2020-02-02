#include <string.h>
#include <jni.h>

jstring
Java_jni_JniPlug_getNativeSring(JNIEnv *env, jobject thiz) {
    return  (*env)->NewStringUTF(env, "I`m Str !");
}
