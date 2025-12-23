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
import com.mrtoad.jianting.Constants.LocalListConstants;
import com.mrtoad.jianting.Constants.MediaPlayModelConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnFinishListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnSequencePlayListener;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.SPDataUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
    private ILikedMusicEntity currentPlayEntity;
    private List<String> musicList;

    private OnFinishListener onFinishListener;
    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }
    private OnSequencePlayListener onSequencePlayListener;
    public void setOnSequencePlayListener(OnSequencePlayListener onSequencePlayListener) {
        this.onSequencePlayListener = onSequencePlayListener;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
        player.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        /**
         * 播放完成监听
         */
        player.setOnCompletionListener((mediaPlayer) -> {
            isPrepared = false;  // 播放完成后重置状态

            int currentPlayModel = GlobalDataManager.getInstance().getCurrentPlayModel(this);
            // 根据不同的播放模式采取不同的播放策略
            if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_SEQUENCE) {
                playNextMusic();
            } else if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_CYCLE) {
                play(currentPlayEntity);
            }
            // 暂时不需要用到播完暂停。但是代码先保留
//            if (currentPlayEntity != null) {
//                onFinishListener.onFinish(currentPlayEntity);
//            }
        });
    }

    /**
     * 播放音乐
     * @param iLikedMusicEntity 音乐实体
     */
    public void play(ILikedMusicEntity iLikedMusicEntity) {
        // 如果是同一首歌，并且已经播放过了。则直接播放
        if (player != null && iLikedMusicEntity.getMusicFilePath().equals(currentFilePath) && isPrepared) {
            if (!player.isPlaying()) {
                player.start();
            }
            return;
        }

        // 如果是新歌，那么则需要重新播放
        resetPlayer();
        currentFilePath = iLikedMusicEntity.getMusicFilePath();

        try {
            player.setDataSource(iLikedMusicEntity.getMusicFilePath());
            player.prepareAsync();  // 使用异步准备防止ANR

            player.setOnPreparedListener((mediaPlayer -> {
                isPrepared = true;
                mediaPlayer.start();
                GlobalDataManager.getInstance().setPlayer(player);
                currentPlayEntity = iLikedMusicEntity;
            }));
        } catch (IOException e) {
            Log.d("@@@" , "播放失败 : " + e.getMessage());
        }
    }

    /**
     * 播放下一首歌曲
     */
    public void playNextMusic() {
        musicList = SPDataUtils.getLocalList(this , LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC);
        Collections.reverse(musicList);

        int playIndex = musicList.indexOf(currentPlayEntity.getMusicName());
        String nextMusicName;
        if (playIndex + 1 < musicList.size()) {
            nextMusicName = musicList.get(playIndex + 1);
        } else {
            nextMusicName = musicList.get(0);
        }

        ILikedMusicEntity nextMusicEntity = GlobalMethodsUtils.getMusicEntityByMusicName(this , nextMusicName);
        play(nextMusicEntity);
        onSequencePlayListener.onSequencePlay(nextMusicEntity);
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
     * 设置音乐播放进度
     * @param progress 进度
     */
    public void setProgress(int progress) {
        if (player != null) {
            player.seekTo(progress);
        }
    }

    /**
     * 切换播放音乐
     * @param iLikedMusicEntity 音乐实体
     */
    public void switchPlay(ILikedMusicEntity iLikedMusicEntity) {
        play(iLikedMusicEntity);
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