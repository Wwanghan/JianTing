package com.mrtoad.jianting.Broadcast;

import android.app.Activity;
import android.content.Intent;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;

public class MediaMethods {

    /**
     * 发送一条播放音乐的广播
     * @param activity Activity
     * @param musicName 音乐名称
     * @param musicFilePath 音乐文件路径
     */
    public static void playMusic(Activity activity , String musicName , String musicFilePath) {
        Intent playIntent = new Intent(MediaBroadcastAction.ACTION_PLAY);
        playIntent.putExtra(MediaBroadcastReceiver.ACTION_PLAY_KEY_FILE_PATH , musicFilePath);
        playIntent.putExtra(MediaBroadcastReceiver.ACTION_PLAY_KEY_MUSIC_NAME , musicName);
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
     * @param musicName 音乐名称
     * @param musicFilePath 音乐文件路径
     */
    public static void finishMusic(Activity activity , String musicName , String musicFilePath) {
        Intent finishIntent = new Intent(MediaBroadcastAction.ACTION_FINISH);
        finishIntent.putExtra(MediaBroadcastReceiver.ACTION_PLAY_KEY_MUSIC_NAME , musicName);
        finishIntent.putExtra(MediaBroadcastReceiver.ACTION_PLAY_KEY_FILE_PATH , musicFilePath);
        activity.sendBroadcast(finishIntent.setPackage(activity.getPackageName()));
    }
}
