package com.mrtoad.jianting.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrtoad.jianting.Adapter.ILikedMusicAdapter;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Constants.LocalListConstants;
import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.SPDataUtils;
import com.mrtoad.jianting.Utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ILikedMusicActivity extends AppCompatActivity {

    private RecyclerView iLikedMusicRecyclerView;
    private List<ILikedMusicEntity> iLIkedMusicList = new ArrayList<>();
    private ImageView biggerImageCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_iliked_music);

        getWindow().setStatusBarColor(Color.BLACK);
        // 设置状态栏图标为浅色（白色）
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        iLikedMusicRecyclerView = findViewById(R.id.i_liked_music_recycler_view);
        biggerImageCover = findViewById(R.id.bigger_music_cover);

        Glide.with(this).load(R.mipmap.avatar).circleCrop().into(biggerImageCover);

        List<String> musicNameList = SPDataUtils.getLocalList(this, LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC);
        // 反转列表，最新导入的显示在最上方
        Collections.reverse(musicNameList);
        for (String musicName : musicNameList) {
            Map<String, String> musicInfoMap = SPDataUtils.getMapInformation(this, musicName);

            String resId = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_COVER);
            Bitmap bitmap = getBitmapFromVectorDrawable(Integer.parseInt(resId));

            String name = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_NAME);
            String author = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_AUTHOR);
            String filePath = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_FILE_PATH);

            iLIkedMusicList.add(new ILikedMusicEntity(bitmap , name , author , filePath));
        }

        ILikedMusicAdapter iLikedMusicAdapter = new ILikedMusicAdapter(this , iLIkedMusicList);
        iLikedMusicRecyclerView.setAdapter(iLikedMusicAdapter);
        iLikedMusicRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        /**
         * 监听 Item 的点击事件
         */
        iLikedMusicAdapter.setOnItemClickListener(new ILikedMusicAdapter.onItemClickListener() {
            @Override
            public void onItemClick(ILikedMusicEntity item) {
                Intent playMusicIntent = new Intent(MediaBroadcastAction.ACTION_PLAY);
                playMusicIntent.putExtra(MediaBroadcastReceiver.ACTION_PLAY_KEY_FILE_PATH , item.getMusicFilePath());
                playMusicIntent.setPackage(getPackageName());
                sendBroadcast(playMusicIntent);
            }
        });

    }

    /**
     * 将矢量图转为 Bitmap
     * @param drawableResId 矢量图的资源 ID
     * @return Bitmap
     */
    private Bitmap getBitmapFromVectorDrawable(@DrawableRes int drawableResId) {
        Drawable drawable = AppCompatResources.getDrawable(this, drawableResId);

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
            ToastUtils.showToast(this , "默认图片加载失败");
            Log.d("@@@" , e.getMessage());
        }

        return bitmap;
    }
}