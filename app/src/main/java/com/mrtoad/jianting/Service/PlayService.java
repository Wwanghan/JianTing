package com.mrtoad.jianting.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.mrtoad.jianting.Activity.PlayActivity;
import com.mrtoad.jianting.Constants.ControlTypeConstants;
import com.mrtoad.jianting.Constants.MediaPlayModelConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnFinishListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnSequencePlayListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnMediaSessionControlListener;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.MusicUtils;
import com.mrtoad.jianting.Utils.SPDataUtils;

import java.io.IOException;
import java.util.List;

public class PlayService extends Service {

    private MediaPlayer player;
    private String currentFilePath;
    private boolean isPrepared = false;
    private boolean isManualSwitch = false;
    private final IBinder myBinder = new MyBinder();
    public class MyBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }
    private MediaSessionCompat mediaSession;
    private static final String MUSIC_PLAY_SESSION = "com.mrtoad.jianting.MUSIC_PLAY_SESSION";
    private PlaybackStateCompat playbackState;
    private NotificationManagerCompat notificationManager;
    private static final String MUSIC_PLAY_SESSION_CHANNEL_ID = "com.mrtoad.jianting.MUSIC_PLAY_SESSION_CHANNEL";
    private static final String MUSIC_PLAY_SESSION_CHANNEL_NAME = "音乐播放会话";
    private static final String MUSIC_PLAY_SESSION_CHANNEL_DESCRIPTION = "音乐播放会话渠道";
    private static final int MUSIC_PLAY_SESSION_NOTIFICATION_ID = 100;

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
                playNextMusic();
            } else if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_CYCLE) {
                play(currentPlayEntity);
            }
            // 暂时不需要用到播完暂停。但是代码先保留
//            if (currentPlayEntity != null) {
//                onFinishListener.onFinish(currentPlayEntity);
//            }
        });

        initMediaSession();

        // 创建音乐播放会话渠道，如果已经创建，则不再创建
        if (!chanelIsExist(this , MUSIC_PLAY_SESSION_CHANNEL_ID)) {
            createSessionChannel();
        }

        // 判断之前是否播放过音乐
        if (SPDataUtils.getStorageInformation(this , SPDataConstants.LAST_PLAY) != null) {
            String lastPlayMusicName = SPDataUtils.getStorageInformation(this, SPDataConstants.LAST_PLAY);
            currentPlayEntity = GlobalMethodsUtils.getMusicEntityByMusicName(this , lastPlayMusicName);
        }
        updateNotificationAndMetadata();
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
                updateNotificationAndMetadata();
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING, player.getCurrentPosition());
            }
            return;
        }

        // 如果是新歌，那么则需要重新播放
        resetPlayer();
        currentFilePath = iLikedMusicEntity.getMusicFilePath();

        try {
            player.setDataSource(iLikedMusicEntity.getMusicFilePath());
            player.prepare();
            player.start();

            isPrepared = true;
            GlobalDataManager.getInstance().setPlayer(player);
            currentPlayEntity = iLikedMusicEntity;

            updateNotificationAndMetadata();
            setPlaybackState(PlaybackStateCompat.STATE_PLAYING , 0);

        } catch (IOException e) {
            Log.d("@@@" , "播放失败 : " + e.getMessage());
        }
    }

    /**
     * 播放下一首歌曲（主要用于顺序播放）
     */
    public void playNextMusic() {
        ILikedMusicEntity nextMusicEntity = MusicUtils.getNextOrPreviousMusic(this, currentPlayEntity.getMusicName(), MusicUtils.NEXT_MUSIC);
        play(nextMusicEntity);
        SPDataUtils.storageInformation(this , SPDataConstants.LAST_PLAY , nextMusicEntity.getMusicName());
        onSequencePlayListener.onSequencePlay(nextMusicEntity);

        setPlaybackState(PlaybackStateCompat.STATE_PLAYING , 0);
        updateNotificationAndMetadata();
    }

    /**
     * 切换播放音乐
     * @param iLikedMusicEntity 音乐实体
     */
    public void switchPlay(ILikedMusicEntity iLikedMusicEntity) {
        isManualSwitch = true;
        play(iLikedMusicEntity);
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (player != null) {
            player.pause();

            setPlaybackState(PlaybackStateCompat.STATE_PAUSED, player.getCurrentPosition());
            updateNotificationAndMetadata();
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
     * 更新媒体会话状态
     * @param position 播放进度
     */
    public void updateMediaSession(int position) {
        setPlaybackState(PlaybackStateCompat.STATE_PLAYING , position);
        updateNotificationAndMetadata();
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

        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }

        stopForeground(STOP_FOREGROUND_REMOVE);
        notificationManager.cancel(MUSIC_PLAY_SESSION_NOTIFICATION_ID);
        return super.onUnbind(intent);
    }

    private void initMediaSession() {
        // 创建 MediaSessionCompat 实例
        mediaSession = new MediaSessionCompat(this , MUSIC_PLAY_SESSION);
        // 设置会话回调，用于接收播放控制指令
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                ILikedMusicEntity previousMusicEntity = MusicUtils.getNextOrPreviousMusic(PlayService.this, currentPlayEntity.getMusicName(), MusicUtils.PREVIOUS_MUSIC);
                switchPlay(previousMusicEntity);
                GlobalDataManager.getInstance().setPlaying(true);
                onMediaSessionControlListener.onMediaSessionControl(previousMusicEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_PREVIOUS);
                SPDataUtils.storageInformation(PlayService.this , SPDataConstants.LAST_PLAY , previousMusicEntity.getMusicName());
            }

            @Override
            public void onPlay() {
                super.onPlay();
                play(currentPlayEntity);
                GlobalDataManager.getInstance().setPlaying(true);
                onMediaSessionControlListener.onMediaSessionControl(currentPlayEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_PLAY);
            }

            @Override
            public void onPause() {
                super.onPause();
                pause();
                GlobalDataManager.getInstance().setPlaying(false);
                onMediaSessionControlListener.onMediaSessionControl(currentPlayEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_PAUSE);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                ILikedMusicEntity nextMusicEntity = MusicUtils.getNextOrPreviousMusic(PlayService.this , currentPlayEntity.getMusicName() , MusicUtils.NEXT_MUSIC);
                switchPlay(nextMusicEntity);
                GlobalDataManager.getInstance().setPlaying(true);
                onMediaSessionControlListener.onMediaSessionControl(nextMusicEntity , ControlTypeConstants.MEDIA_CONTROL_TYPE_NEXT);
                SPDataUtils.storageInformation(PlayService.this , SPDataConstants.LAST_PLAY , nextMusicEntity.getMusicName());
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                setProgress((int) pos);
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING , pos);
            }
            
        });
        // 设置初始播放状态停止
        setPlaybackState(PlaybackStateCompat.STATE_STOPPED , 0);
        // 设置会话可接收媒体按钮事件（比如蓝牙设备的播放按钮）
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // 激活 MediaSession，必须激活，否则无法接收指令
        mediaSession.setActive(true);
    }

    /**
     * 设置播放状态
     * @param state 播放状态
     * @param position 当前播放位置
     */
    private void setPlaybackState(int state , long position) {
        playbackState = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_STOP |
                                PlaybackStateCompat.ACTION_SEEK_TO
                )
                .setState(state , position , 1.0f , SystemClock.elapsedRealtime())
                .build();
        mediaSession.setPlaybackState(playbackState);
    }

    /**
     * 更新音乐播放通知 和 MediaSession 元数据
     */
    private void updateNotificationAndMetadata() {
        if (currentPlayEntity != null) {
            showMediaNotification();
            setMediaMetadata();
        }
    }

    /**
     * 设置媒体元数据
     */
    private void setMediaMetadata() {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE , currentPlayEntity.getMusicName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST , currentPlayEntity.getMusicAuthor())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION , Long.parseLong(currentPlayEntity.getDuration()))
                .build();
        mediaSession.setMetadata(metadata);
    }

    /**
     * 创建音乐播放会话渠道
     */
    private void createSessionChannel() {
        notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    MUSIC_PLAY_SESSION_CHANNEL_ID,
                    MUSIC_PLAY_SESSION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(MUSIC_PLAY_SESSION_CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 判断音乐播放会话渠道是否存在
     * @param channelId 渠道ID
     * @return 渠道是否存在 boolean
     */
    private boolean chanelIsExist(Context context , String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ContextCompat.getSystemService(context , NotificationManager.class).getNotificationChannel(channelId) != null;
        }
        return false;
    }

    /**
     * 显示音乐播放通知
     */
    @SuppressLint("ForegroundServiceType")
    private void showMediaNotification() {
        if (mediaSession == null) {
            return;
        }

        Notification notification = buildMediaNotification();
        startForeground(MUSIC_PLAY_SESSION_NOTIFICATION_ID , notification , ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
    }

    /**
     * 构建音乐播放通知
     * @return 通知对象
     */
    private Notification buildMediaNotification() {
        // 判断当前播放状态，设置对应按钮（播放/暂停切换）
        String playPauseText;
        int playPauseAction;
        if (player != null && player.isPlaying()) {
            playPauseText = "暂停";
            playPauseAction = Math.toIntExact(PlaybackStateCompat.ACTION_PAUSE); // 当前播放中，按钮触发暂停
        } else {
            playPauseText = "播放";
            playPauseAction = Math.toIntExact(PlaybackStateCompat.ACTION_PLAY); // 当前暂停，按钮触发播放
        }

        // 创建跳转到 PlayActivity 的 Intent/ 创建跳转到 PlayActivity 的 Intent
        Intent playIntent = new Intent(this , PlayActivity.class);
        playIntent.putExtra(PlayActivity.ACTION_KEY_I_LIKED_MUSIC_ENTITY, currentPlayEntity);
        playIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // 使用 PendingIntent.getActivity() 的第四个参数设置 FLAG_UPDATE_CURRENT
        // 确保每次更新时都能获取到最新的实体
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,  // 自定义请求码，确保唯一性
                playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this , MUSIC_PLAY_SESSION_CHANNEL_ID)
                .setContentTitle(currentPlayEntity.getMusicName())
                .setContentText(currentPlayEntity.getMusicAuthor())
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        /**
         * 设置当前音乐封面，两种情况
         * 如果当前音乐有封面，则使用封面当大图标和应用图表当小图标
         * 如果当前音乐没有封面，则使用默认图标当大图标和小图标
         */
        String filePath = currentPlayEntity.getMusicCover();
        if (filePath != null && !filePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if (bitmap != null) {
                // 缩放图片到合适大小（大图标推荐 64x64 dp 或 128x128 像素）
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true);
                builder.setLargeIcon(scaledBitmap);
                builder.setSmallIcon(R.mipmap.app_icon);
            }
        } else {
            builder.setSmallIcon(R.drawable.music_cover_default);
        }

        builder.addAction(
                R.drawable.previous_music,
                "上一首",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this , PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        );

        builder.addAction(
                playPauseAction,
                playPauseText,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this , playPauseAction)
        );

        builder.addAction(
                R.drawable.next_music,
                "下一首",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this , PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        );
        // 关键。设置MediaStyle，绑定MediaSession，实现通知与MediaSession联动
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this , PlaybackStateCompat.ACTION_STOP))
        );

        return builder.build();
    }

}