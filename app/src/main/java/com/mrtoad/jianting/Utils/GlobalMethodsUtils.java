package com.mrtoad.jianting.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.Fragment.BottomPlayerFragment;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.R;

import java.io.File;
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
            bottomPlayerFragment.updateUi(getMusicEntityByMusicName(activity, lastPlay));
        }
    }

    /**
     * 根据音乐名称（Key）获取音乐实体对象
     * @param context Context
     * @param musicName 音乐名称
     * @return 音乐实体对象
     */
    public static ILikedMusicEntity getMusicEntityByMusicName(Context context , String musicName) {
        Map<String, String> musicMapInformation = SPDataUtils.getMapInformation(context, musicName);
        ILikedMusicEntity iLikedMusicEntity = new ILikedMusicEntity(
                musicMapInformation.get(MusicInfoConstants.MUSIC_INFO_COVER),
                musicMapInformation.get(MusicInfoConstants.MUSIC_INFO_NAME),
                musicMapInformation.get(MusicInfoConstants.MUSIC_INFO_AUTHOR),
                musicMapInformation.get(MusicInfoConstants.MUSIC_INFO_FILE_PATH),
                musicMapInformation.get(MusicInfoConstants.MUSIC_INFO_DURATION)
        );
        return iLikedMusicEntity;
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

    /**
     * 设置播放按钮状态
     */
    public static void setPlayButton(ImageView playButton) {
        if (GlobalDataManager.getInstance().isPlaying()) {
            playButton.setImageResource(R.drawable.pause_button);
        } else {
            playButton.setImageResource(R.drawable.play_button);
        }
    }

    /**
     * 设置音乐封面
     * @param imageView 图片控件
     * @param musicCover 音乐封面数据
     */
    public static void setMusicCover(Context context , ImageView imageView , String musicCover) {
        if (musicCover != null) {
            File file = new File(musicCover);
            Glide.with(context).load(file).circleCrop().into(imageView);
        } else {
            imageView.setImageResource(R.drawable.music_cover_default);
        }
    }
}
