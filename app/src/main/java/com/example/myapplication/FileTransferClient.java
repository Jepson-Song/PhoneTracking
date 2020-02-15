package com.example.myapplication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.PublicKey;
import java.util.logging.Handler;

/**
 * Created by Jepson on 2020/1/15.
 */
public class FileTransferClient extends Socket {
    private static final String TAG = "FileTransferClient";
 
    private static String SERVER_IP = "127.0.0.1"; // 服务端IP
    private static int SERVER_PORT = 8899; // 服务端端口
    private String filename;
 
    private Socket client;
 
    private FileInputStream fis;
 
    private DataOutputStream dos;
 
    /**
     * 构造函数
     * 与服务器建立连接
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

    public void setServerIp(String IP){
        this.SERVER_IP = IP;
        Log.e("myerror", "IP: "+IP);
    }

    public void setServerPort(int port){
        this.SERVER_PORT = port;
        Log.e("myerror", "port: "+port);
    }

    public void setFilename(String filename){
        this.filename = filename;
        Log.e("myerror", "filename: "+filename);
    }
 
    /**
     * 向服务端传输文件
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
                    //Log.e("myerror",  new String(bytes));
                    dos.write(bytes, 0, length);
                    dos.flush();
                    progress += length;
                    Log.e("myerror", String.format("传输进度： | %.3f%% |", 100.0*progress/file.length()) );
                }
                Log.e("myerror",  "======== 文件传输成功 ========");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                // 得到服务器信息
                String receiveMsg = in.readLine();
                Log.e("myerror",  "receiveMsg: "+receiveMsg);
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
}