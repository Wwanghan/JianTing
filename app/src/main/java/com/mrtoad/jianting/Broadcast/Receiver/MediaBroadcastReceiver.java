package com.mrtoad.jianting.Broadcast.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnFinishListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnPauseListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnPlayListener;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnProgressChanged;
import com.mrtoad.jianting.Interface.MediaBroadcastInterface.OnSwitchPlayListener;

import java.util.Objects;

public class MediaBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_KEY_I_LIKED_MUSIC_ENTITY = "iLikedMusicEntity";
    public static final String ACTION_KEY_PROGRESS_CHANGED = "progressChanged";

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

    private OnProgressChanged onProgressChanged;
    public void setOnProgressChanged(OnProgressChanged onProgressChanged) {
        this.onProgressChanged = onProgressChanged;
    }

    private OnSwitchPlayListener onSwitchPlayListener;
    public void setOnSwitchPlayListener(OnSwitchPlayListener onSwitchPlayListener) {
        this.onSwitchPlayListener = onSwitchPlayListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_PLAY)) {
            ILikedMusicEntity iLikedMusicEntity = intent.getParcelableExtra(ACTION_KEY_I_LIKED_MUSIC_ENTITY);
            onPlayListener.onPlay(iLikedMusicEntity);
        } else if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_PAUSE)) {
            onPauseListener.onPause();
        } else if (Objects.equals(intent.getAction(), MediaBroadcastAction.ACTION_FINISH)) {
            ILikedMusicEntity iLikedMusicEntity = intent.getParcelableExtra(ACTION_KEY_I_LIKED_MUSIC_ENTITY);
            onFinishListener.onFinish(iLikedMusicEntity);
        } else if (intent.getAction() == MediaBroadcastAction.ACTION_PROGRESS_CHANGED) {
            int progress = intent.getIntExtra(ACTION_KEY_PROGRESS_CHANGED, 0);
            onProgressChanged.onProgressChanged(progress);
        } else {
            ILikedMusicEntity iLikedMusicEntity = intent.getParcelableExtra(ACTION_KEY_I_LIKED_MUSIC_ENTITY);
            onSwitchPlayListener.onSwitchPlay(iLikedMusicEntity);
        }
    }
}
