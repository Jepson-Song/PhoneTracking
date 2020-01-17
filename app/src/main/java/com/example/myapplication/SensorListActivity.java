package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telecom.CallAudioState;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class SensorListActivity extends AppCompatActivity {
    private static final String TAG = "SensorListActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_list);
        Log.d(TAG, "onCreate: ");

        TextView tvSensors = (TextView) findViewById(R.id.tvSensors);
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors){
            switch (sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER: tvSensors.append("加速度传感器"); break;
                case Sensor.TYPE_AMBIENT_TEMPERATURE: tvSensors.append("温度传感器"); break;
                case Sensor.TYPE_GYROSCOPE: tvSensors.append("陀螺仪传感器"); break;
                case Sensor.TYPE_LIGHT: tvSensors.append("光线传感器"); break;
                case Sensor.TYPE_MAGNETIC_FIELD: tvSensors.append("磁场传感器"); break;
                case Sensor.TYPE_PRESSURE: tvSensors.append("压力传感器"); break;
                case Sensor.TYPE_PROXIMITY: tvSensors.append("临近传感器"); break;
                case Sensor.TYPE_RELATIVE_HUMIDITY: tvSensors.append("湿度传感器"); break;
                case Sensor.TYPE_ORIENTATION: tvSensors.append("方向传感器"); break;
                case Sensor.TYPE_GRAVITY: tvSensors.append("重力传感器"); break;
                case Sensor.TYPE_LINEAR_ACCELERATION: tvSensors.append("线性加速传感器"); break;
                case Sensor.TYPE_ROTATION_VECTOR: tvSensors.append("旋转向量传感器"); break;
                default: tvSensors.append(sensor.getType()+"");break;
            }
            tvSensors.append(": "+sensor.getName()+"\n");
        }
    }
}
