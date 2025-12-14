package com.mrtoad.jianting.Utils;

import android.app.Activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Fragment.BottomPlayerFragment;

import java.util.Map;

public class GlobalMethodsUtils {

    /**
     * 设置底部音乐导航栏信息
     * @param activity Activity
     * @param fragmentManager FragmentManager
     * @param bottomPlayerFragment 底部音乐导航栏
     */
    public static void setBottmPlayerFragment(Activity activity , FragmentManager fragmentManager , BottomPlayerFragment bottomPlayerFragment) {
        if (SPDataUtils.getStorageInformation(activity , SPDataConstants.LAST_PLAY) == null) {
            FragmentUtils.hideFragment(fragmentManager , bottomPlayerFragment);
        } else {
            String lastPlay = SPDataUtils.getStorageInformation(activity, SPDataConstants.LAST_PLAY);
            Map<String, String> lastPlayMap = SPDataUtils.getMapInformation(activity, lastPlay);
            String musicName = lastPlayMap.get(MusicInfoConstants.MUSIC_INFO_NAME);
            String musicFilePath = lastPlayMap.get(MusicInfoConstants.MUSIC_INFO_FILE_PATH);
            bottomPlayerFragment.updateUi(musicName , musicFilePath);
        }
    }
}
