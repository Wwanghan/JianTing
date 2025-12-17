package com.mrtoad.jianting.Broadcast;

import android.app.Activity;
import android.content.Intent;

import com.mrtoad.jianting.Broadcast.Action.StandardBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.StandardBroadcastReceiver;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;

public class StandardBroadcastMethods {

    /**
     * 更新底部音乐导航栏信息
     * @param activity Activity
     * @param iLikedMusicEntity 音乐实体
     */
    public static void updateBottomPlayerUi(Activity activity , ILikedMusicEntity iLikedMusicEntity) {
        Intent updateBottomPlayerUiIntent = new Intent(StandardBroadcastAction.ACTION_UPDATE_UI);
        updateBottomPlayerUiIntent.putExtra(StandardBroadcastReceiver.ACTION_KEY_I_LIKED_MUSIC_ENTITY , iLikedMusicEntity);
        activity.sendBroadcast(updateBottomPlayerUiIntent.setPackage(activity.getPackageName()));
    }
}
