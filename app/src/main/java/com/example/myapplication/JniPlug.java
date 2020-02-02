package jni;

public class JniPlug {
    static{
        System.loadLibrary("MyApplicationJniLibrary");//加载生成so文件名称
    }
    public  String get_Jni_Test(){
        return JniTest();
    }
    native  private String JniTest();//底层映射
}
