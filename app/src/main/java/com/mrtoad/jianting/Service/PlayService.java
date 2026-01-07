package com.mrtoad.jianting.Service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.mrtoad.jianting.Constants.ControlTypeConstants;
import com.mrtoad.jianting.Constants.MediaPlayModelConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnFinishListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnSequencePlayListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnMediaSessionControlListener;
import com.mrtoad.jianting.Interface.ServiceManagerInterface.OnMediaSessionManagerControlListener;
import com.mrtoad.jianting.Service.Manager.MediaSessionManager;
import com.mrtoad.jianting.Service.Manager.NotificationManager;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.MusicUtils;
import com.mrtoad.jianting.Utils.SPDataUtils;

import java.util.List;

public class PlayService extends Service implements OnMediaSessionManagerControlListener {

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

    /**
     * 服务管理类
     */
    private MediaSessionManager mediaSessionManager;
    private com.mrtoad.jianting.Service.Manager.NotificationManager notificationManager;

    /**
     * 接口设置
     */
    private OnFinishListener onFinishListener;
    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }
    private OnSequencePlayListener onSequencePlayListener;
    public void setOnSequencePlayListener(OnSequencePlayListener onSequencePlayListener) {
        this.onSequencePlayListener = onSequencePlayListener;
    }
    private OnMediaSessionControlListener onMediaSessionControlListener;
    public void setOnMediaSessionControlListener(OnMediaSessionControlListener onMediaSessionControlListener) {
        this.onMediaSessionControlListener = onMediaSessionControlListener;
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
         * 问题：我在切换上一首或下一首歌曲时，会重复调用 setOnCompletionListener，导致一下子切了两首歌曲
         * 原因：因为在歌曲播放的时候，突然切歌 MediaPlayer 会报错，这个错误又会回调别的方法，导致最后触发 setOnCompletionListener
         * 解决：添加 setOnErrorListener 错误监听，返回为 true，手动拦截错误
         */
        player.setOnErrorListener((mediaPlayer, what, extra) -> {
            return true;
        });

        /**
         * 播放完成监听
         */
        player.setOnCompletionListener((mediaPlayer) -> {
            isPrepared = false;  // 播放完成后重置状态

            int currentPlayModel = GlobalDataManager.getInstance().getCurrentPlayModel(this);
            // 根据不同的播放模式采取不同的播放策略
            if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_SEQUENCE) {
                // 顺序播放
                sequencePlay();
            } else if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_CYCLE) {
                // 单曲循环
                play(currentPlayEntity);
            }
            // 暂时不需要用到播完暂停。但是代码先保留
//            if (currentPlayEntity != null) {
//                onFinishListener.onFinish(currentPlayEntity);
//            }
        });

        // 初始化媒体会话管理器
        mediaSessionManager = new MediaSessionManager(this);
        mediaSessionManager.init(this);
        notificationManager = new NotificationManager(this);

        // 创建音乐播放会话渠道，如果已经创建，则不再创建
        if (!notificationManager.chanelIsExist(notificationManager.getChannelId())) {
            notificationManager.createMediaSessionChannel();
        }

        // 判断之前是否播放过音乐
        if (SPDataUtils.getStorageInformation(this , SPDataConstants.LAST_PLAY) != null) {
            String lastPlayMusicName = SPDataUtils.getStorageInformation(this, SPDataConstants.LAST_PLAY);
            currentPlayEntity = GlobalMethodsUtils.getMusicEntityByMusicName(this , lastPlayMusicName);
        }

        // 设置初始播放状态停止（开始默认为停止状态，播放位置默认为0）
        updateMediaSessionAndNotification(PlaybackStateCompat.STATE_STOPPED , 0);

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
                updateMediaSessionAndNotification(PlaybackStateCompat.STATE_PLAYING, player.getCurrentPosition());
            }
            return;
        }

        // 如果是新歌，那么则需要重新播放
        resetPlayer();
        // 设置播放源并准备
        setAndPreparePlayer(iLikedMusicEntity.getMusicFilePath());
        player.start();
        // 设置音乐后播放需要的数据
        setPlayData(iLikedMusicEntity);
        // 更新 MediaSession 和 Notification
        updateMediaSessionAndNotification(PlaybackStateCompat.STATE_PLAYING , 0);
    }

    /**
     * 设置播放源并准备
     * @param musicFilePath 音乐文件路径
     */
    private void setAndPreparePlayer(String musicFilePath) {
        try {
            player.setDataSource(musicFilePath);
            player.prepare();
        } catch (Exception e) {
            Log.d("@@@" , "准备失败: " + e.getMessage());
        }
    }

    /**
     * 设置播放后的数据
     */
    private void setPlayData(ILikedMusicEntity iLikedMusicEntity) {
        isPrepared = true;
        GlobalDataManager.getInstance().setPlayer(player);
        currentPlayEntity = iLikedMusicEntity;
        currentFilePath = iLikedMusicEntity.getMusicFilePath();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (player != null) {
            player.pause();
            updateMediaSessionAndNotification(PlaybackStateCompat.STATE_PAUSED, player.getCurrentPosition());
        }
    }

    /**
     * 切换播放音乐（用于上、下首歌曲切换播放）
     * @param iLikedMusicEntity 音乐实体
     */
    public void switchPlay(ILikedMusicEntity iLikedMusicEntity) {
        play(iLikedMusicEntity);
    }

    /**
     * 设置音乐播放进度
     * @param progress 进度
     */
    public void setProgress(int progress) {
        if (player != null && isPrepared) {
            player.seekTo(progress);
            updateMediaSessionAndNotification(PlaybackStateCompat.STATE_PLAYING , progress);
        }
    }

    /**
     * 顺序播放，播放下一首歌曲
     */
    public void sequencePlay() {
        ILikedMusicEntity nextMusicEntity = MusicUtils.getNextOrPreviousMusic(this, currentPlayEntity.getMusicName(), MusicUtils.NEXT_MUSIC);
        play(nextMusicEntity);

        SPDataUtils.storageInformation(this , SPDataConstants.LAST_PLAY , nextMusicEntity.getMusicName());
        onSequencePlayListener.onSequencePlay(nextMusicEntity);
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 就算因系统内存不足被杀死，那么系统会重新启动服务。可以确保服务一直处于运行状态
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // 返回 true，表示告诉系统，我可能还会回来，回来后就可以重新绑定服务
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAllResources();
    }

    /**
     * 释放所有资源
     */
    private void releaseAllResources() {
        // 1. 释放 MediaPlayer
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }

        // 2. 释放 MediaSession
        if (mediaSessionManager != null) {
            mediaSessionManager.release();
        }

        // 3. 停止前台服务
        stopForeground(true);

        // 4. 取消通知
        if (notificationManager != null) {
            notificationManager.cannel();
        }

        // 5. 重置状态
        resetPlayer();

        // 6. 清空监听器
        onFinishListener = null;
        onSequencePlayListener = null;
        onMediaSessionControlListener = null;

        // 7. 清空当前播放实体
        currentPlayEntity = null;
        currentFilePath = null;
    }

    /**
     * 媒体会话控制接口
     * @param controlType 控制类型
     */
    @Override
    public void onMediaSessionManagerControl(int controlType) {
            // 播放控制：播放
        if (controlType == ControlTypeConstants.MEDIA_CONTROL_TYPE_PLAY) {
            play(currentPlayEntity);
            onMediaSessionControlListener.onMediaSessionControl(currentPlayEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_PLAY);
            // 播放控制：暂停
        } else if (controlType == ControlTypeConstants.MEDIA_CONTROL_TYPE_PAUSE) {
            pause();
            onMediaSessionControlListener.onMediaSessionControl(currentPlayEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_PAUSE);
            SPDataUtils.storageInformation(this , SPDataConstants.LAST_PLAY_POSITION , currentPlayEntity.getMusicName() + "_" + player.getCurrentPosition());
            // 播放控制：上一曲
        } else if (controlType == ControlTypeConstants.MEDIA_CONTROL_TYPE_PREVIOUS) {
            ILikedMusicEntity previousMusicEntity = MusicUtils.getNextOrPreviousMusic(PlayService.this, currentPlayEntity.getMusicName(), MusicUtils.PREVIOUS_MUSIC);
            switchPlay(previousMusicEntity);
            SPDataUtils.storageInformation(this , SPDataConstants.LAST_PLAY , currentPlayEntity.getMusicName());
            onMediaSessionControlListener.onMediaSessionControl(previousMusicEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_PREVIOUS);
            // 播放控制：下一曲
        } else if (controlType == ControlTypeConstants.MEDIA_CONTROL_TYPE_NEXT) {
            ILikedMusicEntity nextMusicEntity = MusicUtils.getNextOrPreviousMusic(PlayService.this , currentPlayEntity.getMusicName() , MusicUtils.NEXT_MUSIC);
            switchPlay(nextMusicEntity);
            SPDataUtils.storageInformation(this , SPDataConstants.LAST_PLAY , currentPlayEntity.getMusicName());
            onMediaSessionControlListener.onMediaSessionControl(nextMusicEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_NEXT);
            // 播放控制：进度条控制
        } else if (controlType == ControlTypeConstants.MEDIA_CONTROL_TYPE_SEEK_TO) {
            setProgress((int) mediaSessionManager.getMediaPlayPosition());
        }
    }

    /**
     * 更新音乐会话 和 MediaSession 元数据
     */
    private void updateMediaSessionAndNotification(int state , long position) {
        if (currentPlayEntity != null) {
            // 更新通知
            Notification notification = notificationManager.buildMediaNotification(currentPlayEntity , player.isPlaying() , mediaSessionManager.getMediaSessionToken());
            startForeground(notificationManager.getNotificationId() , notification , ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            // 更新 MediaSession 元数据
            mediaSessionManager.updateMediaSession(currentPlayEntity , state , position);
        }
    }
}