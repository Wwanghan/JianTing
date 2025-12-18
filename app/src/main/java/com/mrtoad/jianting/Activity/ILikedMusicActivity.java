package com.mrtoad.jianting.Activity;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrtoad.jianting.Adapter.ILikedMusicAdapter;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Action.StandardBroadcastAction;
import com.mrtoad.jianting.Broadcast.MediaMethods;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.Receiver.StandardBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.StandardBroadcastMethods;
import com.mrtoad.jianting.Constants.LocalListConstants;
import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.Fragment.BottomPlayerFragment;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.FragmentUtils;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
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
    private FragmentManager fragmentManager;
    private BottomPlayerFragment bottomPlayerFragment;
    private MediaBroadcastReceiver mediaBroadcastReceiver = new MediaBroadcastReceiver();
    private StandardBroadcastReceiver standardBroadcastReceiver = new StandardBroadcastReceiver();

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

        fragmentManager = getSupportFragmentManager();
        bottomPlayerFragment = BottomPlayerFragment.newInstance();
        FragmentUtils.loadFragment(fragmentManager , R.id.bottom_player_fragment , bottomPlayerFragment);

        List<String> musicNameList = SPDataUtils.getLocalList(this, LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC);
        // 反转列表，最新导入的显示在最上方
        Collections.reverse(musicNameList);
        for (String musicName : musicNameList) {
            Map<String, String> musicInfoMap = SPDataUtils.getMapInformation(this, musicName);

            String resId = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_COVER);
            Bitmap bitmap = GlobalMethodsUtils.getBitmapFromVectorDrawable(ILikedMusicActivity.this , Integer.parseInt(resId));

            String name = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_NAME);
            String author = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_AUTHOR);
            String filePath = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_FILE_PATH);
            String duration = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_DURATION);

            iLIkedMusicList.add(new ILikedMusicEntity(bitmap , name , author , filePath , duration));
        }

        ILikedMusicAdapter iLikedMusicAdapter = new ILikedMusicAdapter(this , iLIkedMusicList);
        iLikedMusicRecyclerView.setAdapter(iLikedMusicAdapter);
        iLikedMusicRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomPlayerFragment.setOnBottomPlayerReadyListener(() -> {
            GlobalMethodsUtils.setBottmPlayerFragment(this , fragmentManager , bottomPlayerFragment);
        });

        /**
         * 监听 Item 的点击事件
         */
        iLikedMusicAdapter.setOnItemClickListener((iLikedMusicEntity) -> {
            MediaMethods.playMusic(ILikedMusicActivity.this , iLikedMusicEntity);

            if (bottomPlayerFragment.isHidden()) { FragmentUtils.showFragment(fragmentManager , bottomPlayerFragment); }
            GlobalDataManager.getInstance().setPlaying(true);
            // 更新底部音乐导航 UI，并将正在播放的音乐名保存起来。最后通知 MainActivity 那边更新 UI
            bottomPlayerFragment.updateUi(iLikedMusicEntity);
            SPDataUtils.storageInformation(ILikedMusicActivity.this , SPDataConstants.LAST_PLAY , iLikedMusicEntity.getMusicName());
            StandardBroadcastMethods.updateBottomPlayerUi(ILikedMusicActivity.this , iLikedMusicEntity);
        });

        /**
         * 监听 MediaPlayer 播放完成事件
         */
        mediaBroadcastReceiver.setOnFinishListener((iLikedMusicEntity) -> {
            bottomPlayerFragment.updateUi(iLikedMusicEntity);
        });

        /**
         * 监听底部音乐播放器更新
         */
        standardBroadcastReceiver.setOnUpdateBottomPlayerListener((iLikedMusicEntity -> {
            if (bottomPlayerFragment.isHidden()) {
                // 使用 commitAllowingStateLoss 提交，用于在后台中做更新操作
                getSupportFragmentManager().beginTransaction().show(bottomPlayerFragment).commitAllowingStateLoss();
            }
            bottomPlayerFragment.updateUi(iLikedMusicEntity);
        }));

    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mediaBroadcastReceiver , new IntentFilter(MediaBroadcastAction.ACTION_FINISH) , RECEIVER_EXPORTED);
        registerReceiver(standardBroadcastReceiver , new IntentFilter(StandardBroadcastAction.ACTION_UPDATE_UI) , RECEIVER_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mediaBroadcastReceiver);
        unregisterReceiver(standardBroadcastReceiver);
    }
}