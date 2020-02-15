package jni;

/**
 * Created by Jepson on 2020/1/15.
 */
public class JniPlug {
    static{
        System.loadLibrary("demo");//加载生成so文件名称
    }
    public static native  String getNativeSring(int x, int y);//底层映射
}
