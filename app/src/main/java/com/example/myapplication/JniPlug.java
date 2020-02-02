package jni;

public class JniPlug {
    static{
        System.loadLibrary("demo");//加载生成so文件名称
    }
    public static native  String getNativeSring();//底层映射
}
