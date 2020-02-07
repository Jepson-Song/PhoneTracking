package com.example.myapplication;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;

public class MyAudioPlayer {
    private static final String TAG = "MyAudioPlayer";
    private Button btPlay;
    private String fileName;
    private MediaPlayer mMediaPlayer;
    private boolean playClickFlag = true;
    final String color1 = "#9BA8A8";
    final String color2 = "#00cccc";

    MyAudioPlayer(){

    }

    MyAudioPlayer(Button bt, String filename){
        this.btPlay = bt;
        this.fileName = filename;
    }

    public void startPlay() {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btPlay.setText("PLAY");
                playClickFlag = !playClickFlag;
                btPlay.setBackgroundColor(Color.parseColor(color1));
                Log.d(TAG, "播放完毕");
            }
        });
    }

    public void stopPlay() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}

