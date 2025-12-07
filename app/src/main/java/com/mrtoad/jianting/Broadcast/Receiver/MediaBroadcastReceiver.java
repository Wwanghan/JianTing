package com.mrtoad.jianting.Broadcast.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;

import java.util.Objects;

public class MediaBroadcastReceiver extends BroadcastReceiver {

    private onPlayListener onPlayListener;
    public interface onPlayListener {
        void onPlay(Uri uri);
    }

    public void setOnPlayListener(onPlayListener onPlayListener) {
        this.onPlayListener = onPlayListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_PLAY)) {
            String uri = intent.getStringExtra("uri");
            onPlayListener.onPlay(Uri.parse(uri));
        }
    }
}
