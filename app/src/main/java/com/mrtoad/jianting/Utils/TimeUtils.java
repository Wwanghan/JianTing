package com.mrtoad.jianting.Utils;

import android.annotation.SuppressLint;

public class TimeUtils {
    /**
     * 毫秒转换成00:00格式
     * @param millis 毫秒数
     * @return 时间字符串
     */
    @SuppressLint("DefaultLocale")
    public static String MillisToTime(long millis) {
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
}
