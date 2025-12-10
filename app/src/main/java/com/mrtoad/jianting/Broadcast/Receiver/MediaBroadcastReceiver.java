package com.mrtoad.jianting.Broadcast.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;

import java.util.Objects;

public class MediaBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_PLAY_KEY_FILE_PATH = "filePath";

    private onPlayListener onPlayListener;
    public interface onPlayListener {
        void onPlay(String filePath);
    }

    public void setOnPlayListener(onPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_PLAY)) {
            String filePath = intent.getStringExtra(ACTION_PLAY_KEY_FILE_PATH);
            onPlayListener.onPlay(filePath);
        }
    }
}
