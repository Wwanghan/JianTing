package com.mrtoad.jianting.Service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.mrtoad.jianting.Broadcast.MediaMethods;
import com.mrtoad.jianting.Broadcast.StandardBroadcastMethods;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnFinishListener;

import java.io.IOException;

public class PlayService extends Service {

    private MediaPlayer player;
    private String currentFilePath;
    private boolean isPrepared = false;
    private final IBinder myBinder = new MyBinder();
    public class MyBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }
    private OnFinishListener onFinishListener;
    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());
    }

    /**
     * 播放音乐
     * @param musicName 音乐名称
     * @param musicFilePath 文件路径
     */
    public void play(String musicName , String musicFilePath) {
        // 如果是同一首歌，并且已经播放过了。则直接播放
        if (player != null && musicFilePath.equals(currentFilePath) && isPrepared) {
            if (!player.isPlaying()) {
                player.start();
            }
            return;
        }

        // 如果是新歌，那么则需要重新播放
        resetPlayer();
        currentFilePath = musicFilePath;

        try {
            player.setDataSource(musicFilePath);
            player.prepareAsync();  // 使用异步准备防止ANR

            player.setOnPreparedListener((mediaPlayer -> {
                isPrepared = true;
                mediaPlayer.start();
            }));
        } catch (IOException e) {
            Log.d("@@@" , "播放失败 : " + e.getMessage());
        }

        player.setOnCompletionListener((mediaPlayer) -> {
            isPrepared = false;  // 播放完成后重置状态
            GlobalDataManager.getInstance().setPlaying(false);

            onFinishListener.onFinish(musicName, musicFilePath);
        });
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    /**
     * 重置播放器状态
     */
    private void resetPlayer() {
        if (player != null) {
            player.reset();
        }
        isPrepared = false;
        currentFilePath = null;
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
        resetPlayer();
        return super.onUnbind(intent);
    }

}