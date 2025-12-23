package com.mrtoad.jianting;

import android.content.Context;
import android.media.MediaPlayer;

import com.mrtoad.jianting.Constants.MediaPlayModelConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Utils.SPDataUtils;

public class GlobalDataManager {

    private boolean isPlaying = false;
    private MediaPlayer player = null;
    // 设置播放模式，默认为顺序播放
    private int currentPlayModel = MediaPlayModelConstants.PLAY_MODEL_SEQUENCE;

    private static GlobalDataManager instance;
    private GlobalDataManager() {}

    public static synchronized GlobalDataManager getInstance() {
        if (instance == null) {
            instance = new GlobalDataManager();
        }
        return instance;
    }

    // 提供线程安全的 getter/setter
    public synchronized boolean isPlaying() {
        return isPlaying;
    }

    public synchronized void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public synchronized MediaPlayer getPlayer() {
        return player;
    }

    public synchronized void setPlayer(MediaPlayer player) {
        this.player = player;
    }

    public int getCurrentPlayModel(Context context) {
        if (SPDataUtils.getStorageInformation(context , SPDataConstants.PLAY_MODEL) != null) {
            return Integer.parseInt(SPDataUtils.getStorageInformation(context , SPDataConstants.PLAY_MODEL));
        }
        return currentPlayModel;
    }

    public void setCurrentPlayModel(int currentPlayModel) {
        this.currentPlayModel = currentPlayModel;
    }
}
