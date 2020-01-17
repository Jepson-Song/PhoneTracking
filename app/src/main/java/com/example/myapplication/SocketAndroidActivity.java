package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import java.util.Timer;
import java.util.TimerTask;

public class SocketAndroidActivity extends AppCompatActivity {

    private static final String TAG = "SocketAndroidActivity";

    private static final int CONNECTING = 0;
    private static final int SENDING = 1;
    private static final int CLOSE = 2;
    private static final int RECEIVE = 3;

    TextView text,tvTime;
    EditText input, address, port;
    Socket socket;

    String addStr, sendMsg, receiveMsg;
    int portStr;

    private int deltaTime = -1;
    private double avgDeltaTime = 0;
    //private Timer timer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_android);
        tvTime = (TextView)findViewById(R.id.tvTime);
        Button button = (Button) this.findViewById(R.id.btn_send);
        text = (TextView) findViewById(R.id.receive);
        input = (EditText) findViewById(R.id.input);
        address = (EditText) findViewById(R.id.address);
        port = (EditText) findViewById(R.id.port);
        address.setText("192.168.43.95");
        port.setText("8896");
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addStr = address.getText().toString().trim();
                portStr = Integer.parseInt(port.getText().toString().trim());
                new WorkThread().start();
            }
        });


        class DelayTask extends TimerTask {
            @Override
            public void run() {

            }
        }

        Button btTest = (Button)findViewById(R.id.btTest);
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStr = address.getText().toString().trim();
                portStr = Integer.parseInt(port.getText().toString().trim());

                int n = 10;
                for (int i=1; i<=n ; i++){

                    long time = getTodayMS();
                    input.setText("TIME:" + time);

                    new WorkThread().start();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //Toast.makeText(SocketAndroidActivity.this, "time:"+time, Toast.LENGTH_SHORT).show();
                    Toast.makeText(SocketAndroidActivity.this, "time:"+time+"  deltaTime:"+deltaTime, Toast.LENGTH_SHORT).show();
                    avgDeltaTime += deltaTime;
;
                }
                avgDeltaTime/=n;
                Toast.makeText(SocketAndroidActivity.this, "avgDeltaTime:"+avgDeltaTime, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        /*//设置返回的数据
        Intent intent = new Intent();
        intent.putExtra("data", deltaTime+"");
        setResult(11, intent);
        //关闭当前activity
        finish();*/
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
                text.setText("正在连接中......");
            } else if (msg.what == SENDING) {
                text.setText("Client Sending: '" + sendMsg + "'");
            } else if (msg.what == CLOSE) {
                text.append("\nsocket close");
            } else if (msg.what == RECEIVE) {
                text.setText("Client Receiveing: '" + receiveMsg + "'");
            }


        }
    };


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
                sendMsg = input.getText().toString();
                msg1.what = SENDING;
                handler.sendMessage(msg1);
                //socket.getOutputStream  out是个字符输出流，后面true说明执行后自动刷新
                PrintWriter out = new PrintWriter(
                        new BufferedWriter(new OutputStreamWriter(
                                socket.getOutputStream())), true);
                out.println(sendMsg);


                // 接收服务器信息
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                // 得到服务器信息
                receiveMsg = in.readLine();
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