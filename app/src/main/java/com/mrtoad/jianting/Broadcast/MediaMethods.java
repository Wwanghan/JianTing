package com.mrtoad.jianting.Broadcast;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;

public class MediaMethods {

    /**
     * 发送一条播放音乐的广播
     * @param activity Activity
     * @param iLikedMusicEntity 音乐实体
     */
    public static void playMusic(Activity activity , ILikedMusicEntity iLikedMusicEntity) {
        Intent playIntent = new Intent(MediaBroadcastAction.ACTION_PLAY);
        playIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_I_LIKED_MUSIC_ENTITY , iLikedMusicEntity);
        activity.sendBroadcast(playIntent.setPackage(activity.getPackageName()));
    }

    /**
     * 发送一条暂停音乐的广播
     * @param activity Activity
     */
    public static void pauseMusic(Activity activity) {
        Intent pauseIntent = new Intent(MediaBroadcastAction.ACTION_PAUSE);
        activity.sendBroadcast(pauseIntent.setPackage(activity.getPackageName()));
    }

    /**
     * 发送一条音乐播放完毕的广播
     * @param activity Activity
     * @param iLikedMusicEntity 音乐实体
     */
    public static void finishMusic(Activity activity , ILikedMusicEntity iLikedMusicEntity) {
        Intent finishIntent = new Intent(MediaBroadcastAction.ACTION_FINISH);
        finishIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_I_LIKED_MUSIC_ENTITY , iLikedMusicEntity);
        activity.sendBroadcast(finishIntent.setPackage(activity.getPackageName()));
    }

    /**
     * 发送一条音乐进度条改变的广播
     * @param activity Activity
     * @param progress 进度
     */
    public static void setProgress(Activity activity , int progress) {
        Intent setProgressIntent = new Intent(MediaBroadcastAction.ACTION_PROGRESS_CHANGED);
        setProgressIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_PROGRESS_CHANGED , progress);
        activity.sendBroadcast(setProgressIntent.setPackage(activity.getPackageName()));
    }

    /**
     * 发送一条切换播放的广播
     * @param activity Activity
     * @param iLikedMusicEntity 音乐实体
     */
    public static void switchPlay(Activity activity , ILikedMusicEntity iLikedMusicEntity) {
        Intent switchPlayIntent = new Intent(MediaBroadcastAction.ACTION_SWITCH_PLAY);
        switchPlayIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_I_LIKED_MUSIC_ENTITY , iLikedMusicEntity);
        activity.sendBroadcast(switchPlayIntent.setPackage(activity.getPackageName()));
    }

    /**
     * 发送一条顺序播放的广播
     * @param activity Activity
     * @param iLikedMusicEntity 音乐实体
     */
    public static void sequencePlay(Activity activity , ILikedMusicEntity iLikedMusicEntity) {
        Intent sequencePlayIntent = new Intent(MediaBroadcastAction.ACTION_SEQUENCE_PLAY);
        sequencePlayIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_I_LIKED_MUSIC_ENTITY , iLikedMusicEntity);
        activity.sendBroadcast(sequencePlayIntent.setPackage(activity.getPackageName()));
    }

    /**
     * 发送一条媒体会话控制广播
     * @param activity Activity
     * @param iLikedMusicEntity 音乐实体
     */
    public static void mediaSessionControl(Activity activity , ILikedMusicEntity iLikedMusicEntity , int controlType) {
        Intent mediaSessionControlIntent = new Intent(MediaBroadcastAction.ACTION_MEDIA_SESSION_CONTROL);
        mediaSessionControlIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_I_LIKED_MUSIC_ENTITY , iLikedMusicEntity);
        mediaSessionControlIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_MEDIA_SESSION_CONTROL_TYPE , controlType);
        activity.sendBroadcast(mediaSessionControlIntent.setPackage(activity.getPackageName()));
    }

    /**
     * 发送一条媒体会话更新广播
     * @param activity Activity
     */
    public static void mediaSessionUpdate(Activity activity , int position) {
        Intent mediaSessionUpdateIntent = new Intent(MediaBroadcastAction.ACTION_MEDIA_SESSION_UPDATE);
        mediaSessionUpdateIntent.putExtra(MediaBroadcastReceiver.ACTION_KEY_PROGRESS_CHANGED , position);
        activity.sendBroadcast(mediaSessionUpdateIntent.setPackage(activity.getPackageName()));
    }
}
