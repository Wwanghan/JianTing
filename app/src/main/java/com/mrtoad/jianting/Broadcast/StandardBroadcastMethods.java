package com.mrtoad.jianting.Broadcast;

import android.app.Activity;
import android.content.Intent;

import com.mrtoad.jianting.Broadcast.Action.StandardBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.StandardBroadcastReceiver;

public class StandardBroadcastMethods {

    /**
     * 更新底部音乐导航栏信息
     * @param activity Activity
     * @param musicName 音乐名
     * @param musicFilePath 音乐文件路径
     */
    public static void updateBottomPlayerUi(Activity activity , String musicName , String musicFilePath) {
        Intent updateBottomPlayerUiIntent = new Intent(StandardBroadcastAction.ACTION_UPDATE_UI);
        updateBottomPlayerUiIntent.putExtra(StandardBroadcastReceiver.ACTION_KEY_MUSIC_NAME , musicName);
        updateBottomPlayerUiIntent.putExtra(StandardBroadcastReceiver.ACTION_KEY_MUSIC_FILE_PATH , musicFilePath);
        activity.sendBroadcast(updateBottomPlayerUiIntent.setPackage(activity.getPackageName()));
    }
}
