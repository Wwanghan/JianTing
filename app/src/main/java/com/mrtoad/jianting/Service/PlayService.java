package com.mrtoad.jianting.Service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.mrtoad.jianting.Utils.ToastUtils;

import java.io.File;
import java.io.IOException;

public class PlayService extends Service {

    private MediaPlayer player;
    private final IBinder myBinder = new MyBinder();
    public class MyBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
    }

    /**
     * 播放音乐
     */
    public void play(String filePath) {
        player.reset();

        try {
            assert player != null;
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            player.setDataSource(filePath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.d("@@@" , e.getMessage());
        }

        player.setOnCompletionListener((mediaPlayer) -> {
            Log.d("@@@" , "播放完成");
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (player != null) {
            player.release();
            player = null;
        }
        return super.onUnbind(intent);
    }

}