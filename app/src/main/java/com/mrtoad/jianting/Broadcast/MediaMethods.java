package com.mrtoad.jianting.Broadcast;

import android.app.Activity;
import android.content.Intent;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;

public class MediaMethods {

    /**
     * 播放音乐
     * @param activity Activity
     * @param musicFilePath 音乐文件路径
     */
    public static void playMusic(Activity activity , String musicFilePath) {
        Intent playIntent = new Intent(MediaBroadcastAction.ACTION_PLAY);
        playIntent.putExtra(MediaBroadcastReceiver.ACTION_PLAY_KEY_FILE_PATH , musicFilePath);
        activity.sendBroadcast(playIntent.setPackage(activity.getPackageName()));
    }
}
