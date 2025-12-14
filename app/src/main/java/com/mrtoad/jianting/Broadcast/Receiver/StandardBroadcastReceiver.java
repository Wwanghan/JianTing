package com.mrtoad.jianting.Broadcast.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mrtoad.jianting.Broadcast.Action.StandardBroadcastAction;

import java.util.Objects;

public class StandardBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_KEY_MUSIC_NAME = "musicName";
    public static final String ACTION_KEY_MUSIC_FILE_PATH = "musicFilePath";

    private onUpdateBottomPlayerUiListener onUpdateUiListener;

    public interface onUpdateBottomPlayerUiListener {
        void updateUi(String musicName , String musicFilePath);
    }

    public void setOnUpdateUiListener(onUpdateBottomPlayerUiListener onUpdateUiListener) {
        this.onUpdateUiListener = onUpdateUiListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), StandardBroadcastAction.ACTION_UPDATE_UI)) {
            String musicName = intent.getStringExtra(ACTION_KEY_MUSIC_NAME);
            String musicFilePath = intent.getStringExtra(ACTION_KEY_MUSIC_FILE_PATH);
            onUpdateUiListener.updateUi(musicName , musicFilePath);
        }
    }
}
