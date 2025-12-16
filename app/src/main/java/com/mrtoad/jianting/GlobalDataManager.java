package com.mrtoad.jianting;

public class GlobalDataManager {

    private boolean isPlaying = false;

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
}
