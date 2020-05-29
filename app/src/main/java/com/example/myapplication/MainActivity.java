package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Jepson on 2019/12/25.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";

    private SensorManager mSensorManager;
    private TextView tvAccelerometer, tvGravity, tvGyroscope, tvTime;
    private Button btAllSensors, btSocket, btStart, btPlay, btTime, btStartTime, btJni;
    private EditText etStartTime, etSamplePeriod;
    private String wavName = "null";
    private boolean isRecording = false;
    private boolean isWaiting = false;
    private boolean isPlaying = false;

    private static final String color1 = "#9BA8A8";
    private static final String color2 = "#00CCCC";
    private static final String color3 = "#4EBABA";

    private SharedPreferences sp;
    private double avgDeltaTime;
    private int startM;

    private MyAudioRecorder mAudioRecorder;
    private MyAudioPlayer mAudioPlayer;

    private static final int STOPRECORD = 0;
    private static final int STARTRECORD = 1;
    private static final int WAITRECORD = 2;

    private float accData[] = new float[3];
    private float graData[] = new float[3];
    private float gyrData[] = new float[3];

    private Timer timer;

    ArrayList<Float> accList = new ArrayList<Float>();
    ArrayList<Float> graList = new ArrayList<Float>();
    ArrayList<Float> gyrList = new ArrayList<Float>();

    private String name = "null";
    private String accName = "null";
    private String graName = "null";
    private String gyrName = "null";

    private int collectNum = 0;
    private int sensorChangeNum = 0;
    private boolean isCollect = false;

    private int startTime = 0;
    private int stopTime = 0;

    final private int collectPeriod = 10;
    private int samplePeriod = 10000;

    private String timName = "null";
    ArrayList<Long> timList = new ArrayList<Long>();

    private float magData[] = new float[3];
    private float oriData[] = new float[3];

    private String oriName = "null";
    ArrayList<Float> oriList = new ArrayList<Float>();
    private float iniOriData[] = new float[3];

    public void newFileName() {
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();

        String s = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());

        wavName = mFileName + "/test/rcd_" + s + ".wav";
        accName = mFileName + "/test/rcd_" + s + ".acc";
        graName = mFileName + "/test/rcd_" + s + ".gra";
        gyrName = mFileName + "/test/rcd_" + s + ".gyr";
        timName = mFileName + "/test/rcd_" + s + ".tim";
        oriName = mFileName + "/test/rcd_" + s + ".ori";
    }

    private void startRecord(){

        accList.clear();
        graList.clear();
        gyrList.clear();
        timList.clear();
        oriList.clear();

        startTime = getTodayMS();

        btStart.setText("STOP");
        btStartTime.setText("STOP");
        btStart.setBackgroundColor(Color.parseColor(color2));
        btStartTime.setBackgroundColor(Color.parseColor(color2));

        newFileName();
        mAudioRecorder = new MyAudioRecorder(wavName);
        mAudioRecorder.startRecord();

        startCollect();

    }

    private void stopRecord(){
        stopTime = getTodayMS();

        btStart.setText("START");
        btStartTime.setText("START2");
        btStart.setBackgroundColor(Color.parseColor(color1));
        btStartTime.setBackgroundColor(Color.parseColor(color1));

        mAudioRecorder.stopRecord();

        stopCollect();
        /**
         * 获取SharedPreferenced对象
         * 第一个参数是生成xml的文件名
         * 第二个参数是存储的格式
         */
        sp = getSharedPreferences("User", Context.MODE_PRIVATE);
        //获取到edit对象
        SharedPreferences.Editor editor = sp.edit();
        //通过editor对象写入数据
        editor.putString("wavName", wavName);
        editor.putString("accName", accName);
        editor.putString("graName", graName);
        editor.putString("gyrName", gyrName);
        editor.putString("timName", timName);
        editor.putString("oriName", oriName);
        //提交数据存入到xml文件中
        editor.commit();
    }

    private void waitRecord(){
        btStart.setText("WAIT");
        btStartTime.setText("WAIT");
        btStart.setBackgroundColor(Color.parseColor(color3));
        btStartTime.setBackgroundColor(Color.parseColor(color3));
    }

    private void startPlay(){
        Toast.makeText(MainActivity.this, wavName, Toast.LENGTH_SHORT).show();
        btPlay.setText("STOP");
        isPlaying = !isPlaying;
        btPlay.setBackgroundColor(Color.parseColor(color2));

        mAudioPlayer = new MyAudioPlayer(btPlay, wavName);
        mAudioPlayer.startPlay();
    }

    private void stopPlay(){
        btPlay.setText("PLAY");
        isPlaying = !isPlaying;
        btPlay.setBackgroundColor(Color.parseColor(color1));

        mAudioPlayer.stopPlay();
    }

    private class WaitThread extends Thread{
        @Override
        public void run(){
            isWaiting = true;
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

            Message msg = new Message();
            msg.what = STARTRECORD;
            handler.sendMessage(msg);

            isWaiting = false;
        }
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            if(msg.what == STARTRECORD){
                if (isRecording){
                    Toast.makeText(MainActivity.this, "is recording now!", Toast.LENGTH_SHORT).show();
                }
                else{
                    startRecord();
                    isRecording = !isRecording;
                }
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
                if (isWaiting){
                    Toast.makeText(MainActivity.this, "is waiting !", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isRecording){
                    startRecord();
                }
                else{
                    stopRecord();
                }
                isRecording = !isRecording;
            }
        });

        etStartTime = (EditText)findViewById(R.id.etStartTime);
        btStartTime = (Button)findViewById(R.id.btStartTime);
        btStartTime.setBackgroundColor(Color.parseColor(color1));
        btStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWaiting){
                    Toast.makeText(MainActivity.this, "is waiting !", Toast.LENGTH_SHORT).show();
                    return;
                }
                Calendar calendar = Calendar.getInstance();
                int m = calendar.get(Calendar.MINUTE)+1;
                if (m == 60){
                    m = 0;
                }
                if (true){//TextUtils.isEmpty(etStartTime.getText().toString().trim())){
                    etStartTime.setText(m+"");
                    Log.e(TAG, "onClick: "+m );
                }
                if(!isRecording){
                    waitRecord();
                    new WaitThread().start();
                    //startRecord();
                }
                else{
                    stopRecord();
                    isRecording = !isRecording;
                }

            }
        });

        btPlay =(Button)findViewById(R.id.btPlay);
        btPlay.setBackgroundColor(Color.parseColor(color1));
        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wavName == "null"){
                    Toast.makeText(MainActivity.this, "NO SOUND RECORDER!", Toast.LENGTH_SHORT).show();
                }
                else if(!isPlaying){
                    startPlay();
                }
                else{
                    stopPlay();
                }
            }
        });


        /**
         * 调用C程序
         */
        /*
        btJni = (Button)findViewById(R.id.btJni);
        btJni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvTime.setText(jni.JniPlug.getNativeSring(1, 2));
            }
        });
         */

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE); //获取系统传感器服务权限

        tvAccelerometer = (TextView)findViewById(R.id.tvAccelerometer);
        tvGravity = (TextView)findViewById(R.id.tvGravity);
        tvGyroscope = (TextView)findViewById(R.id.tvGyroscope);

        tvTime = (TextView)findViewById(R.id.tvTime);

        etSamplePeriod = (EditText)findViewById(R.id.etSamplePeriod);
        etSamplePeriod.setText(samplePeriod+"");

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                //String contentAccelerometer = "加速度传感器："+outputFormat(event.values[0], event.values[1], event.values[2])+"\n";
                //tvAccelerometer.setText(contentAccelerometer);
                accData = event.values.clone();
                /*if(isCollect){
                    sensorChangeNum ++;

                    accList.add(accData[0]);accList.add(accData[1]);accList.add(accData[2]);
                    //Long nanoTime = System.nanoTime();
                    //timList.add(nanoTime);
                    collectNum ++;
                }
                calculateOrientation();*/
                break;
            case Sensor.TYPE_GRAVITY:
                //String contentGravity = "重力传感器："+outputFormat(event.values[0], event.values[1], event.values[2])+"\n";
                //tvGravity.setText(contentGravity);
                graData = event.values.clone();
                /*if(isCollect){
                    graList.add(graData[0]);graList.add(graData[1]);graList.add(graData[2]);
                }*/
                break;
            case Sensor.TYPE_GYROSCOPE:
                //String contentGyroscope = "陀螺仪："+outputFormat(event.values[0], event.values[1], event.values[2])+"\n";
                //tvGyroscope.setText(contentGyroscope);
                gyrData = event.values.clone();
                /*if(isCollect){
                    gyrList.add(gyrData[0]);gyrList.add(gyrData[1]);gyrList.add(gyrData[2]);
                }*/
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magData = event.values.clone();
                //calculateOrientation();
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    private void calculateOrientation(){
        float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, accData, magData);
        SensorManager.getOrientation(R, values);

        // 要经过一次数据格式的转换，转换为度
        /*oriData[0] = (float) Math.toDegrees(values[0]);
        Log.i(TAG, values[0]+"");
        oriData[1] = (float) Math.toDegrees(values[1]);
        oriData[2] = (float) Math.toDegrees(values[2]);*/



        if(isCollect){
            oriData = values.clone();
            if(oriData[0] < 0) {
                oriData[0] += 2*3.1415926535897932384626;
            }
            oriList.add(oriData[0]);
            oriList.add(oriData[1]);
            oriList.add(oriData[2]);

            Long nanoTime = System.nanoTime();
            timList.add(nanoTime);

            /*String contentOri = "相对方向："+(oriData[0]-iniOriData[0])+" "+
                    (oriData[1]-iniOriData[1])+" "+(oriData[2]-iniOriData[2])+"\n";
            tvGyroscope.setText(contentOri);*/
        } else {
            iniOriData = values.clone();

            /*String contentIniOri = "初始方向："+iniOriData[0]+" "+ iniOriData[1]+" "+iniOriData[2]+"\n";
            tvGravity.setText(contentIniOri);*/
        }

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

        samplePeriod = Integer.parseInt(etSamplePeriod.getText().toString());
        /*
         * 第一个参数：SensorEventListener接口的实例对象
         * 第二个参数：需要注册的传感器实例
         * 第三个参数：传感器获取传感器事件event值频率：
         *              SensorManager.SENSOR_DELAY_FASTEST = 0：对应10000微秒的更新间隔，最快
         *              SensorManager.SENSOR_DELAY_GAME = 1：对应20000微秒的更新间隔，游戏中常用
         *              SensorManager.SENSOR_DELAY_UI = 2：对应60000微秒的更新间隔
         *              SensorManager.SENSOR_DELAY_NORMAL = 3：对应200000微秒的更新间隔
         *              键入自定义的int值x时：对应x微秒的更新间隔
         *
         */
        //注册加速度传感器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),//传感器TYPE类型
                samplePeriod);//SensorManager.SENSOR_DELAY_FASTEST);//采集频率
        //注册重力传感器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                samplePeriod);
        //注册陀螺仪
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                samplePeriod);
        //注册磁场传感器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                samplePeriod);

        //calculateOrientation();

    }

    private void startCollect(){
        collectNum = 0;
        sensorChangeNum = 0;

        isCollect = true;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                accList.add(accData[0]);accList.add(accData[1]);accList.add(accData[2]);
                //graList.add(graData[0]);graList.add(graData[1]);graList.add(graData[2]);
                //gyrList.add(gyrData[0]);gyrList.add(gyrData[1]);gyrList.add(gyrData[2]);
                calculateOrientation();

                collectNum ++;
            }
        },0, collectPeriod);

    }

    private void stopCollect(){
        isCollect = false;

        //销毁timer
        timer.cancel();
        timer = null;

        //将传感器数据写入文件
        try {
            acc2File(accList, accName);
            acc2File(graList, graName);
            acc2File(gyrList, gyrName);
            tim2File(timList, timName);
            ori2File(oriList, oriName);
        } catch (Exception e) {
            e.printStackTrace();
        }


        Log.e(TAG, "stopCollect: continueTime: "+ (stopTime-startTime) );

        Log.e(TAG, "stopCollect: collectNum: " + collectNum );

        Log.e(TAG, "stopCollect: sensorChangeNum: " + sensorChangeNum );

    }

    private void acc2File(List<Float> dataList, String path) throws Exception{
        File file = new File(path);
        //如果没有文件就创建
        if (!file.isFile()) {
            file.createNewFile();
        }
        if (dataList.size() == 0){
            return ;
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));


        for (int i = 0; i < dataList.size(); i++){
            writer.write(dataList.get(i) + " ");
            if((i+1)%3 == 0) {
                writer.write("\n");
            }
        }
        writer.close();
    }

    private void tim2File(List<Long> dataList, String path) throws Exception{
        File file = new File(path);
        //如果没有文件就创建
        if (!file.isFile()) {
            file.createNewFile();
        }

        if (dataList.size() == 0){
            return ;
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(path));


        for (int i = 0; i < dataList.size(); i++){
            writer.write(dataList.get(i) + "");
            //if((i+1)%3 == 0) {
            writer.write("\n");
            //}
        }
        writer.close();
    }
    private void ori2File(List<Float> dataList, String path) throws Exception{
        File file = new File(path);
        //如果没有文件就创建
        if (!file.isFile()) {
            file.createNewFile();
        }
        if (dataList.size() == 0){
            return ;
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));


        for (int i = 0; i < dataList.size(); i++){
            writer.write(dataList.get(i) + " ");
            if((i+1)%3 == 0) {
               writer.write("\n");
            }
        }
        writer.close();
    }

    private int getTodayMS(){
        Calendar calendar = Calendar.getInstance();
        int h = calendar.get(Calendar.HOUR);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        int ms = calendar.get(Calendar.MILLISECOND);
        int res = ((h*60+m)*60+s)*1000+ms;
        return res;
    }

    /**
     * 暂停Activity，界面获取焦点
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(mSensorManager != null){
            mSensorManager.unregisterListener(this);
        }

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //unregisterReceiver(receiver);
    }

}
