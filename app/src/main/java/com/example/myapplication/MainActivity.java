package com.example.myapplication;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.content.BroadcastReceiver;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private TextView tvAccelerometer, tvGravity, tvGyroscope, tvTime;
    private Button btAllSensors, btSocket, btStart, btPlay, btTime, btStartTime, btJni;
    private EditText etStartTime;
    private boolean startClickFlag = true;
    private String fileName = "null";
    private boolean playClickFlag = true;

    private Thread thread;
    private Boolean RUN = true;
    private Calendar calendar;
    private long s, ms;

    final String color1 = "#9BA8A8";
    final String color2 = "#00cccc";
    private IntentFilter intentFilter;

    private SharedPreferences sp;
    private double avgDeltaTime;
    private int startM;

    private MyAudioRecorder mAudioRecorder;
    private MyAudioPlayer mAudioPlayer;

    private static final int STOPRECORD = 0;
    private static final int STARTRECORD = 1;

    public String newFileName() {
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();

        String s = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
        return mFileName += "/test/rcd_" + s + ".wav";
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String str = data.getStringExtra("data");
        tvTime.setText(str);
    }

    private void startRecord(){
        btStart.setText("STOP");
        btStartTime.setText("STOP");
        btStart.setBackgroundColor(Color.parseColor(color2));
        btStartTime.setBackgroundColor(Color.parseColor(color2));

        fileName = newFileName();
        mAudioRecorder = new MyAudioRecorder(fileName);
        mAudioRecorder.startRecord();
    }

    private void stopRecord(){
        btStart.setText("START");
        btStartTime.setText("START2");
        btStart.setBackgroundColor(Color.parseColor(color1));
        btStartTime.setBackgroundColor(Color.parseColor(color1));

        mAudioRecorder.stopRecord();

        /**
         * 获取SharedPreferenced对象
         * 第一个参数是生成xml的文件名
         * 第二个参数是存储的格式
         */
        sp = getSharedPreferences("User", Context.MODE_PRIVATE);
        //获取到edit对象
        SharedPreferences.Editor editor = sp.edit();
        //通过editor对象写入数据
        editor.putString("WavFileName", fileName);
        //提交数据存入到xml文件中
        editor.commit();
    }

    private void startPlay(){
        Toast.makeText(MainActivity.this, fileName, Toast.LENGTH_SHORT).show();
        btPlay.setText("STOP");
        playClickFlag = !playClickFlag;
        btPlay.setBackgroundColor(Color.parseColor(color2));

        mAudioPlayer = new MyAudioPlayer(btPlay, fileName);
        mAudioPlayer.startPlay();
    }

    private void stopPlay(){
        btPlay.setText("PLAY");
        playClickFlag = !playClickFlag;
        btPlay.setBackgroundColor(Color.parseColor(color1));

        mAudioPlayer.stopPlay();
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == STARTRECORD){
                startRecord();
            }
            else if(msg.what == STOPRECORD){
                stopRecord();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");

        btAllSensors = (Button)findViewById(R.id.btAllSensors);
        btAllSensors.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SensorListActivity.class);
                startActivity(intent);
            }
        });

        btSocket = (Button)findViewById(R.id.btSocket);
        btSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SocketAndroidActivity.class);
                startActivity(intent);
                //用一种特殊方式开启Activity
                //startActivityForResult(intent, 11);
            }
        });

        btTime = (Button)findViewById(R.id.btTime);
        btTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp = getSharedPreferences("User", Context.MODE_PRIVATE);;
                String strAvgDeltaTime = sp.getString("avgDeltaTime", "null");
                avgDeltaTime = Double.valueOf(strAvgDeltaTime);
                tvTime.setText("avgDeltaTime: "+strAvgDeltaTime);
            }
        });


        btStart =(Button) findViewById(R.id.btStart);
        btStart.setBackgroundColor(Color.parseColor(color1));
        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startClickFlag){
                    startRecord();
                }
                else{
                    stopRecord();
                }
                startClickFlag = !startClickFlag;
            }
        });


        btPlay =(Button)findViewById(R.id.btPlay);
        btPlay.setBackgroundColor(Color.parseColor(color1));
        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileName == "null"){
                    Toast.makeText(MainActivity.this, "NO SOUND RECORDER!", Toast.LENGTH_SHORT).show();
                }
                else if(playClickFlag){
                    startPlay();
                }
                else{
                    stopPlay();
                }
            }
        });

        etStartTime = (EditText)findViewById(R.id.etStartTime);
        btStartTime = (Button)findViewById(R.id.btStartTime);
        btStartTime.setBackgroundColor(Color.parseColor(color1));
        btStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startM = Integer.parseInt(etStartTime.getText().toString().trim());

                if(avgDeltaTime < 0) {
                    startM -= 1;
                    if(startM < 0) startM = 59;
                    avgDeltaTime += 60*1000;
                }

                Calendar calendar;
                int m, s, ms;
                while (true) {
                    calendar = Calendar.getInstance();
                    m = calendar.get(Calendar.MINUTE);
                    s = calendar.get(Calendar.SECOND);
                    ms = calendar.get(Calendar.MILLISECOND);
                    if (m >= startM && s * 1000 + ms >= avgDeltaTime) break;
                }

                if(startClickFlag){
                    startRecord();
                }
                else{
                    stopRecord();
                }
                startClickFlag = !startClickFlag;

            }
        });

        /**
         * 调用C程序
         */
        btJni = (Button)findViewById(R.id.btJni);
        btJni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTime.setText(jni.JniPlug.getNativeSring(1, 2));
            }
        });


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); //获取系统传感器服务权限

        tvAccelerometer = (TextView)findViewById(R.id.tvAccelerometer);
        tvGravity = (TextView)findViewById(R.id.tvGravity);
        tvGyroscope = (TextView)findViewById(R.id.tvGyroscope);

        tvTime = (TextView)findViewById(R.id.tvTime);

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                String contentAccelerometer = "加速度传感器：   \n"+outputFormat(event.values[0], event.values[1], event.values[2])+"\n";
                tvAccelerometer.setText(contentAccelerometer);
                break;
            case Sensor.TYPE_GRAVITY:
                String contentGravity = "重力传感器：   \n"+"x:"+outputFormat(event.values[0], event.values[1], event.values[2])+"\n";
                tvGravity.setText(contentGravity);
                break;
            case Sensor.TYPE_GYROSCOPE:
                String contentGyroscope = "陀螺仪：   \n"+outputFormat(event.values[0], event.values[1], event.values[2])+"\n";
                tvGyroscope.setText(contentGyroscope);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    private NumberFormat formatter = new DecimalFormat("0.000000");
    private String outputFormat(float x, float y, float z){
        String   sx   =   formatter.format(x);
        String   sy   =   formatter.format(y);
        String   sz   =   formatter.format(z);
        return "x:"+sx+"  y:"+sy+"  z:"+sz;
    }

    /**
     * 界面获取焦点
     */
    protected void onResume() {
        super.onResume();

        //注册加速度传感器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),//传感器TYPE类型
                SensorManager.SENSOR_DELAY_UI);//采集频率
        //注册重力传感器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_FASTEST);
        //注册陀螺仪
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    /**
     * 暂停Activity，界面获取焦点
     */
    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        thread = null;
        //unregisterReceiver(receiver);
    }

}
