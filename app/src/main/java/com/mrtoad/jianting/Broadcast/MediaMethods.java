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
}
