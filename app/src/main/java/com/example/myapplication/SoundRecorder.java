package com.example.myapplication;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SoundRecorder {
    private static final String TAG = "SoundRecorder";
    MediaRecorder mMediaRecorder;
    private String fileName = "null";
    private String test;
    boolean isRecording;

    public String startRecording() {
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        fileName = newFileName();
        mMediaRecorder.setOutputFile(fileName);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();
        return fileName;
    }


    public void stopRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    private String newFileName() {
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();

        String s = new SimpleDateFormat("yyyy-MM-dd_hhmmss").format(new Date());
        return mFileName += "/test/rcd_" + s + ".mp3";
    }
}
