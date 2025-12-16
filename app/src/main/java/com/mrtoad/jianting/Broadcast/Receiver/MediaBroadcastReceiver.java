package com.mrtoad.jianting.Broadcast.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnFinishListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnPauseListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnPlayListener;

import java.util.Objects;

public class MediaBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_PLAY_KEY_FILE_PATH = "filePath";
    public static final String ACTION_PLAY_KEY_MUSIC_NAME = "musicName";

    private OnPlayListener onPlayListener;
    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    private OnPauseListener onPauseListener;
    public void setOnPauseListener(OnPauseListener onPauseListener) {
        this.onPauseListener = onPauseListener;
    }

    private OnFinishListener onFinishListener;
    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_PLAY)) {
            String musicName = intent.getStringExtra(ACTION_PLAY_KEY_MUSIC_NAME);
            String musicFilePath = intent.getStringExtra(ACTION_PLAY_KEY_FILE_PATH);
            onPlayListener.onPlay(musicName , musicFilePath);
        } else if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_PAUSE)) {
            onPauseListener.onPause();
        } else if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_FINISH)) {
            String musicName = intent.getStringExtra(ACTION_PLAY_KEY_MUSIC_NAME);
            String musicFilePath = intent.getStringExtra(ACTION_PLAY_KEY_FILE_PATH);
            onFinishListener.onFinish(musicName , musicFilePath);
        }
    }
}
