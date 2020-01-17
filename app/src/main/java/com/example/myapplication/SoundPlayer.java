package com.example.myapplication;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class SoundPlayer {
    private static final String TAG = "SoundPlayer";
    MediaPlayer mMediaPlayer;

    public void startPlaying(String fileName) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(fileName);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void stopPlaying() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}

