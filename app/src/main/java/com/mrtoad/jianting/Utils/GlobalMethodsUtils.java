package com.mrtoad.jianting.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
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
            ILikedMusicEntity iLikedMusicEntity = new ILikedMusicEntity(
                    getBitmapFromVectorDrawable(activity , Integer.parseInt(lastPlayMap.get(MusicInfoConstants.MUSIC_INFO_COVER))),
                    lastPlayMap.get(MusicInfoConstants.MUSIC_INFO_NAME),
                    lastPlayMap.get(MusicInfoConstants.MUSIC_INFO_AUTHOR),
                    lastPlayMap.get(MusicInfoConstants.MUSIC_INFO_FILE_PATH)
            );
            bottomPlayerFragment.updateUi(iLikedMusicEntity);
        }
    }

    /**
     * 将矢量图转为 Bitmap
     * @param drawableResId 矢量图的资源 ID
     * @return Bitmap
     */
    public static Bitmap getBitmapFromVectorDrawable(Activity activity , @DrawableRes int drawableResId) {
        Drawable drawable = AppCompatResources.getDrawable(activity, drawableResId);

        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            ToastUtils.showToast(activity , "默认图片加载失败");
            Log.d("@@@" , e.getMessage());
        }

        return bitmap;
    }
}
