package com.mrtoad.jianting.Service.Manager;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.mrtoad.jianting.Constants.ControlTypeConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.Interface.ServiceManagerInterface.OnMediaSessionManagerControlListener;
import com.mrtoad.jianting.Service.Constants.MediaSessionManagerConstants;
import com.mrtoad.jianting.Service.PlayService;
import com.mrtoad.jianting.Utils.SPDataUtils;

public class MediaSessionManager {
    private Context context;
    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat playbackState;
    private ILikedMusicEntity currentPlayEntity;
    private OnMediaSessionManagerControlListener onMediaSessionManagerControlListener;
    public long MEDIA_PLAY_POSITION = 0;

    /**
     * 构造函数
     * @param context Context
     */
    public MediaSessionManager(Context context) {
        this.context = context;
    }

    /**
     * 初始化操作
     * @param onMediaSessionManagerControlListener 会话控制接口
     */
    public void init(OnMediaSessionManagerControlListener onMediaSessionManagerControlListener) {
        this.onMediaSessionManagerControlListener = onMediaSessionManagerControlListener;
        initMediaSession();
    }

    /**
     * 获取当前播放位置
     * @return 当前播放位置
     */
    public long getMediaPlayPosition() {
        return MEDIA_PLAY_POSITION;
    }

    /**
     * 获取 MediaSessionToken
     * @return
     */
    public MediaSessionCompat.Token getMediaSessionToken() {
        return mediaSession.getSessionToken();
    }

    /**
     * 初始化 MediaSession
     */
    private void initMediaSession() {
        // 创建 MediaSessionCompat 实例
        mediaSession = new MediaSessionCompat(context , MediaSessionManagerConstants.MUSIC_PLAY_SESSION);
        // 设置会话回调，用于接收播放控制指令
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                GlobalDataManager.getInstance().setPlaying(true);
                onMediaSessionManagerControlListener.onMediaSessionManagerControl(ControlTypeConstants.MEDIA_CONTROL_TYPE_PREVIOUS);
            }

            @Override
            public void onPlay() {
                super.onPlay();
                GlobalDataManager.getInstance().setPlaying(true);
                onMediaSessionManagerControlListener.onMediaSessionManagerControl(ControlTypeConstants.MEDIA_CONTROL_TYPE_PLAY);
            }

            @Override
            public void onPause() {
                super.onPause();
                GlobalDataManager.getInstance().setPlaying(false);
                onMediaSessionManagerControlListener.onMediaSessionManagerControl(ControlTypeConstants.MEDIA_CONTROL_TYPE_PAUSE);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                GlobalDataManager.getInstance().setPlaying(true);
                onMediaSessionManagerControlListener.onMediaSessionManagerControl(ControlTypeConstants.MEDIA_CONTROL_TYPE_NEXT);
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING , pos);
                MEDIA_PLAY_POSITION = pos;
                onMediaSessionManagerControlListener.onMediaSessionManagerControl(ControlTypeConstants.MEDIA_CONTROL_TYPE_SEEK_TO);
            }
        });
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
    public void setPlaybackState(int state , long position) {
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
     * 设置媒体元数据
     */
    public void setMediaMetadata(ILikedMusicEntity entity) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE , entity.getMusicName() != null ? entity.getMusicName() : MediaSessionManagerConstants.MUSIC_TITLE_UNKNOWN)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST , entity.getMusicAuthor() != null ? entity.getMusicAuthor() : MediaSessionManagerConstants.MUSIC_AUTHOR_UNKNOWN)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION , Long.parseLong(entity.getDuration()) != 0 ? Long.parseLong(entity.getDuration()) : 0)
                .build();
        mediaSession.setMetadata(metadata);
    }

    /**
     * 更新 MediaSession
     * @param entity 用于更新媒体元数据
     * @param state 更新播放状态
     * @param position 更新播放位置
     */
    public void updateMediaSession(ILikedMusicEntity entity , int state , long position) {
        setMediaMetadata(entity);
        setPlaybackState(state , position);
    }

    /**
     * 释放 MediaSession
     */
    public void release() {
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
    }
}
