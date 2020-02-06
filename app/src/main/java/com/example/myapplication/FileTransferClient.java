package com.example.myapplication;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.PublicKey;

/**
 * 文件传输Client端<br>
 */
public class FileTransferClient extends Socket {
    private static final String TAG = "FileTransferClient";
 
    private static String SERVER_IP = "127.0.0.1"; // 服务端IP
    private static int SERVER_PORT = 8899; // 服务端端口
    private String filename;
 
    private Socket client;
 
    private FileInputStream fis;
 
    private DataOutputStream dos;


    /*public void setServerIp(String IP){
        this.SERVER_IP = IP;
        Log.e("myerror", "IP: "+IP);
    }

    public void setServerPort(int port){
        this.SERVER_PORT = port;
        Log.e("myerror", "port: "+port);
    }*/

    public void setFilename(String filename){
        this.filename = filename;
        Log.e("myerror", "filename: "+filename);
    }
 
    /**
     * 构造函数<br/>
     * 与服务器建立连接
     * @throws Exception
     */
    public FileTransferClient(String SERVER_IP, int SERVER_PORT, String filename) throws Exception {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        this.SERVER_IP = SERVER_IP;
        Log.e("myerror", "SERVER_IP: "+SERVER_IP);
        this.SERVER_PORT = SERVER_PORT;
        Log.e("myerror", "SERVER_PORT: "+SERVER_PORT);
        this.filename = filename;
        Log.e("myerror", "filename: "+filename);

        Log.e("myerror",  "Cliect[port:" + client.getLocalPort() + "] 成功连接服务端");
    }
 
    /**
     * 向服务端传输文件
     * @throws Exception
     */
    public void sendFile() throws Exception {
        try {
            File file = new File(filename);
            if(file.exists()) {
                fis = new FileInputStream(file);
                dos = new DataOutputStream(client.getOutputStream());
 
                // 文件名和长度
                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();
 
                // 开始传输文件
                Log.e("myerror",  "======== 开始传输文件 ========");
                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    Log.e("myerror",  new String(bytes));
                    dos.write(bytes, 0, length);
                    dos.flush();
                    progress += length;
                    Log.e("myerror", "| " + (100*progress/file.length()) + "% |");
                }
                Log.e("myerror", "" );
                Log.e("myerror",  "======== 文件传输成功 ========");
            }
            else{
                Log.e("myerror",  "file does not exist!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fis != null)
                fis.close();
            if(dos != null)
                dos.close();
            client.close();
        }
    }
 
    /**
     * 入口
     * @param args
     */
    /*public static void main(String[] args) {
        try {
            FileTransferClient client = new FileTransferClient(); // 启动客户端连接
            client.sendFile(); // 传输文件
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}