package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;

/**
 * Created by Jepson on 2020/1/10.
 */
public class SocketAndroidActivity extends AppCompatActivity {

    private static final String TAG = "SocketAndroidActivity";

    private static final int CONNECTING = 0;
    private static final int SENDING = 1;
    private static final int CLOSE = 2;
    private static final int RECEIVE = 3;
    private static final int DELTATIME = 4;
    private static final int AVGDELTATIME = 5;
    private static final int FILESENDING = 6;
    private static final int FILESENDDONE = 7;

    private TextView tvRecieve;
    private EditText etInput, etIpAddress, etPort, etFileAddress;
    private Button btSend, btSendFile, btTest;
    Socket socket;

    String addStr, sendMsg, receiveMsg;
    int portStr;

    private int deltaTime = -1;
    private double avgDeltaTime = 0;
    //private Timer timer;

    private SharedPreferences sp;

    private String wavName = "null";
    private String accName = "null";
    private String graName = "null";
    private String gyrName = "null";

    private boolean isTest = false;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_android);

        btSend = (Button) this.findViewById(R.id.btSend);
        tvRecieve = (TextView) findViewById(R.id.tvRecieve);
        etInput = (EditText) findViewById(R.id.etInput);
        etIpAddress = (EditText) findViewById(R.id.etIpAddress);
        etPort = (EditText) findViewById(R.id.etPort);
        etIpAddress.setText("192.168.43.95");
        etPort.setText("8896");

        etFileAddress = (EditText)findViewById(R.id.etFileAddress);
        sp = getSharedPreferences("User", Context.MODE_PRIVATE);
        wavName = sp.getString("wavName", "null");
        accName = sp.getString("accName", "null");
        graName = sp.getString("graName", "null");
        gyrName = sp.getString("gyrName", "null");
        etFileAddress.setText(wavName);

        btSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addStr = etIpAddress.getText().toString().trim();
                portStr = Integer.parseInt(etPort.getText().toString().trim());

                new WorkThread().start();
            }
        });

        btTest = (Button)findViewById(R.id.btTest);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStr = etIpAddress.getText().toString().trim();
                portStr = Integer.parseInt(etPort.getText().toString().trim());
                new TestThread().start();
            }
        });

        btSendFile = (Button)findViewById(R.id.btSendFile);
        btSendFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStr = etIpAddress.getText().toString().trim();
                portStr = Integer.parseInt(etPort.getText().toString().trim());

                new SendFileThread().start();
                //new WorkThread().start();
            }
        });


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private long getTodayMS(){
        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        int ms = calendar.get(Calendar.MILLISECOND);
        long res = ((h*60+m)*60+s)*1000+ms;
        return res;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CONNECTING) {
                tvRecieve.append("\n正在连接中......");
            } else if (msg.what == SENDING) {
                tvRecieve.append("\n正在发送信息: '" + sendMsg + "'");
            } else if (msg.what == CLOSE) {
                tvRecieve.append("\n关闭");
            } else if (msg.what == RECEIVE) {
                tvRecieve.append("\n正在接受信息: '" + receiveMsg + "'");
            } else if (msg.what == DELTATIME){
                Toast.makeText(SocketAndroidActivity.this,"deltaTime:"+deltaTime, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AVGDELTATIME) {
                Toast.makeText(SocketAndroidActivity.this, "avgDeltaTime:"+avgDeltaTime, Toast.LENGTH_SHORT).show();
            } else if (msg.what == FILESENDING) {
                tvRecieve.append("\nFile "+wavName+" sending...");
            } else if (msg.what == FILESENDDONE) {
                tvRecieve.append("\nsend file "+wavName+" successfully!");
            }


        }
    };

    //SendFile线程
    private class SendFileThread extends Thread{
        @Override
        public void run(){
            try {
                //tvRecieve.append("\nFile "+wavName+" sending...");
                Message msg = new Message();
                msg.what = FILESENDING;
                handler.sendMessage(msg);

                FileTransferClient ftc;

                ftc = new FileTransferClient(addStr, 8899, accName);
                ftc.sendFile();
                ftc = new FileTransferClient(addStr, 8899, graName);
                ftc.sendFile();
                ftc = new FileTransferClient(addStr, 8899, gyrName);
                ftc.sendFile();

                ftc = new FileTransferClient(addStr, 8899, wavName);
                ftc.sendFile();

            } catch (Exception e){
                e.printStackTrace();
            }
            //tvRecieve.append("\nsend file "+wavName+" successfully!");
        }
    }

    //Test线程
    private class TestThread extends Thread{
        @Override
        public void run(){
            isTest = true;
            int n = 10;
            for (int i=1; i<=n ; i++){
                new WorkThread().start();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                avgDeltaTime += deltaTime;

                Message msg = new Message();
                msg.what = DELTATIME;
                handler.sendMessage(msg);
            }
            avgDeltaTime/=n;
            //Toast.makeText(SocketAndroidActivity.this, "avgDeltaTime:"+avgDeltaTime, Toast.LENGTH_SHORT).show();
            Message msg = new Message();
            msg.what = AVGDELTATIME;
            handler.sendMessage(msg);
            isTest = false;
            /**
             * 获取SharedPreferenced对象
             * 第一个参数是生成xml的文件名
             * 第二个参数是存储的格式
             */
            sp = getSharedPreferences("User", Context.MODE_PRIVATE);
            //获取到edit对象
            SharedPreferences.Editor editor = sp.edit();
            //通过editor对象写入数据
            editor.putString("avgDeltaTime", avgDeltaTime+"");
            //提交数据存入到xml文件中
            editor.commit();
        }
    }


    //private Boolean flag = true;
    //工作线程
    private class WorkThread extends Thread {
        @Override
        public void run() {
            Log.d(TAG, "Thread run: ");
            //处理比较耗时的操作
            //数据处理完成后，关于UI的更新要通过handler发送消息
            Message msg = new Message();
            Message msg1 = new Message();
            Message msg2 = new Message();
            Message msg3 = new Message();
            msg.what = CONNECTING;
            handler.sendMessage(msg);
            try {
                Log.e("myerror", "addStr: "+addStr);
                Log.e("myerror", "portStr: "+portStr);
                socket = new Socket(addStr, portStr);

                //Toast.makeText(SocketAndroidActivity.this, "addStr:"+addStr+" portStr:"+portStr, Toast.LENGTH_SHORT).show();
                Log.e("myerror", "socket: ok");
                if (socket == null) {
                    Log.e("myerror", "socket  null");
                    return;
                }
                //发送给服务端的消息
                if(isTest){
                    sendMsg = "TIME:" + getTodayMS();
                }
                else {
                    sendMsg = etInput.getText().toString();
                }
                msg1.what = SENDING;
                handler.sendMessage(msg1);
                //socket.getOutputStream  out是个字符输出流，后面true说明执行后自动刷新
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(
                                socket.getOutputStream())), true);
                out.println(sendMsg);
                Log.e("myerror",  "sendMsg: "+sendMsg);


                // 接收服务器信息
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                // 得到服务器信息
                receiveMsg = in.readLine();
                Log.e("myerror",  "receiveMsg: "+receiveMsg);
                if(receiveMsg.substring(0,"deltaTime: ".length()).equals("deltaTime: ")){
                    deltaTime = Integer.parseInt(receiveMsg.substring("deltaTime: ".length(),receiveMsg.length()-2));
                    //Toast.makeText(SocketAndroidActivity.this, "deltaTime: "+ deltaTime+"ms", Toast.LENGTH_SHORT).show();
                    //tvTime.setText("deltaTime: "+ deltaTime+"ms");
                }
                msg3.what = RECEIVE;
                handler.sendMessage(msg3);


            } catch (IOException e) {
                //e.printStackTrace();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                e.printStackTrace(new PrintStream(baos));
                String exception = baos.toString();
                Log.e("myerror", exception);
            } finally {
                //关闭Socket
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                msg2.what = CLOSE;
                handler.sendMessage(msg2);
            }

        }
    }
}