package com.mrtoad.jianting.Broadcast.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mrtoad.jianting.Activity.ILikedMusicActivity;
import com.mrtoad.jianting.Broadcast.Action.StandardBroadcastAction;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.Interface.StandardBroadcastInterface.OnUpdateBottomPlayerListener;

import java.io.Serializable;
import java.util.Objects;

public class StandardBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_KEY_I_LIKED_MUSIC_ENTITY = "iLikedMusicEntity";

    private OnUpdateBottomPlayerListener onUpdateBottomPlayerListener;
    public void setOnUpdateBottomPlayerListener(OnUpdateBottomPlayerListener onUpdateBottomPlayerListener) {
        this.onUpdateBottomPlayerListener = onUpdateBottomPlayerListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), StandardBroadcastAction.ACTION_UPDATE_UI)) {
            ILikedMusicEntity iLikedMusicEntity = intent.getParcelableExtra(ACTION_KEY_I_LIKED_MUSIC_ENTITY);
            onUpdateBottomPlayerListener.onUpdateBottomPlayer(iLikedMusicEntity);
        }
    }
}
