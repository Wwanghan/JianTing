package com.mrtoad.jianting.Activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.mrtoad.jianting.Constants.MapConstants;
import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Constants.ViewAnimationConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.Fragment.BottomPlayerFragment;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.FragmentUtils;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.KMeansColorExtractor;
import com.mrtoad.jianting.Utils.SPDataUtils;
import com.mrtoad.jianting.Utils.ToastUtils;
import com.mrtoad.jianting.Utils.ViewAnimationUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ILikedMusicActivity extends AppCompatActivity {

    private RecyclerView iLikedMusicRecyclerView;
    private ILikedMusicAdapter iLikedMusicAdapter;
    private List<ILikedMusicEntity> iLIkedMusicList = new ArrayList<>();
    private ImageView biggerImageCover;
    private ImageView playButton;
    private FragmentManager fragmentManager;
    private BottomPlayerFragment bottomPlayerFragment;
    private MediaBroadcastReceiver mediaBroadcastReceiver = new MediaBroadcastReceiver();
    private StandardBroadcastReceiver standardBroadcastReceiver = new StandardBroadcastReceiver();
    private ActivityResultLauncher imagePickerLauncher;
    private String longClickedMusicName;
    private RelativeLayout noAddMusicArea;

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
        playButton = findViewById(R.id.play_button);
        noAddMusicArea = findViewById(R.id.no_add_music_area);

        // 注册活动结果监听器
        registerActivityResult();
        Glide.with(this).load(R.mipmap.avatar).circleCrop().into(biggerImageCover);

        fragmentManager = getSupportFragmentManager();
        bottomPlayerFragment = BottomPlayerFragment.newInstance();
        FragmentUtils.loadFragment(fragmentManager , R.id.bottom_player_fragment , bottomPlayerFragment);

        List<String> musicNameList = SPDataUtils.getLocalList(this, LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC);
        // 反转列表，最新导入的显示在最上方
        Collections.reverse(musicNameList);
        if (!musicNameList.isEmpty()) {
            for (String musicName : musicNameList) {
                Map<String, String> musicInfoMap = SPDataUtils.getMapInformation(this, musicName);

                String cover = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_COVER);
                String name = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_NAME);
                String author = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_AUTHOR);
                String filePath = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_FILE_PATH);
                String duration = musicInfoMap.get(MusicInfoConstants.MUSIC_INFO_DURATION);

                iLIkedMusicList.add(new ILikedMusicEntity(cover , name , author , filePath , duration));
            }
            GlobalMethodsUtils.setMusicCover(this , biggerImageCover , iLIkedMusicList.get(0).getMusicCover());
        } else {
            // 没有音乐时，隐藏所有控件
            iLikedMusicRecyclerView.setVisibility(GONE);
            noAddMusicArea.setVisibility(VISIBLE);
            biggerImageCover.setVisibility(GONE);
            playButton.setVisibility(GONE);

        }


        iLikedMusicAdapter = new ILikedMusicAdapter(this , iLIkedMusicList);
        iLikedMusicRecyclerView.setAdapter(iLikedMusicAdapter);
        iLikedMusicRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bottomPlayerFragment.setOnBottomPlayerReadyListener(() -> {
            GlobalMethodsUtils.setBottmPlayerFragment(this , fragmentManager , bottomPlayerFragment);
        });

        /**
         * 点击上方播放按钮，播放当前类表第一首歌曲
         */
        playButton.setOnClickListener((v) -> {
            ViewAnimationUtils.waterRipplesAnimation(playButton , ViewAnimationConstants.WATER_RIPPLES_DURATION);
            MediaMethods.playMusic(this , iLIkedMusicList.get(0));
            updateUiAndData(iLIkedMusicList.get(0));
        });

        /**
         * 监听音乐项的点击事件
         */
        iLikedMusicAdapter.setOnClickListener((iLikedMusicEntity) -> {
            MediaMethods.playMusic(ILikedMusicActivity.this , iLikedMusicEntity);
            updateUiAndData(iLikedMusicEntity);
        });

        /**
         * 监听音乐项的长按事件
         */
        iLikedMusicAdapter.setOnLongClickListener((iLikedMusicEntity -> {
            longClickedMusicName = iLikedMusicEntity.getMusicName();
            imagePickerLauncher.launch("image/*");
        }));

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

    /**
     * 更新 UI 和数据
     */
    private void updateUiAndData(ILikedMusicEntity iLikedMusicEntity) {
        if (bottomPlayerFragment.isHidden()) { FragmentUtils.showFragment(fragmentManager , bottomPlayerFragment); }
        GlobalDataManager.getInstance().setPlaying(true);

        // 更新当前页面底部音乐导航 UI 和 MainActivity 底部音乐导航 UI，并将当前播放的音乐存储起来
        bottomPlayerFragment.updateUi(iLikedMusicEntity);
        StandardBroadcastMethods.updateBottomPlayerUi(this , iLikedMusicEntity);

        SPDataUtils.storageInformation(this , SPDataConstants.LAST_PLAY , iLikedMusicEntity.getMusicName());
    }

    /**
     * 注册活动结果监听器
     */
    private void registerActivityResult() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent() , (uri) -> {
            if (uri != null) {
                try {
                    /**
                     * 创建文件保存图片内容，并将文件路径存储在本地便于后续访问
                     */
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if (inputStream == null) { return; }

                    // 创建输出文件
                    String fileName = "music_cover_" + System.currentTimeMillis() + ".jpg";
                    File outputFIle = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) , fileName);
                    // 创建输出流
                    FileOutputStream fileOutputStream = new FileOutputStream(outputFIle);
                    // 复制数据到结果文件中
                    FileUtils.copy(inputStream , fileOutputStream);

                    inputStream.close();
                    fileOutputStream.close();
                    SPDataUtils.updateMapInformation(this , longClickedMusicName , MusicInfoConstants.MUSIC_INFO_COVER , outputFIle.getAbsolutePath());

                    /**
                     * 获取图片主次颜色，并保存在本地
                     */
                    KMeansColorExtractor.extractColorsOnlyFromFilePath(outputFIle.getAbsolutePath() , (primaryColor , secondaryColor) -> {
                        Map<String , String> musicCoverColorsMap = new HashMap<>();
                        musicCoverColorsMap.put(MusicInfoConstants.MUSIC_INFO_PRIMARY_COLOR , String.valueOf(primaryColor));
                        musicCoverColorsMap.put(MusicInfoConstants.MUSIC_INFO_SECONDARY_COLOR , String.valueOf(secondaryColor));

                        String mapKey = longClickedMusicName + MapConstants.MUSIC_COVER_COLOR_MAP_SUFFIX;
                        SPDataUtils.storeMapInformation(this , mapKey , musicCoverColorsMap);
                    });

                    /**
                     * 更新 UI，更新底部播放器 UI
                     */
                    String lastPlayMusic = SPDataUtils.getStorageInformation(this, SPDataConstants.LAST_PLAY);
                    int count = 0;
                    for (ILikedMusicEntity iLikedMusicEntity : iLIkedMusicList) {
                        if (iLikedMusicEntity.getMusicName().equals(longClickedMusicName)) {
                            iLikedMusicEntity.setMusicCover(outputFIle.getAbsolutePath());
                            iLikedMusicAdapter.notifyItemChanged(count);

                            // 如果底部播放器正在显示，并且我要修改的项目正好是我正在播放的，那么更新底部播放器 UI
                            if (!bottomPlayerFragment.isHidden() && lastPlayMusic.equals(longClickedMusicName)) {
                                StandardBroadcastMethods.updateBottomPlayerUi(ILikedMusicActivity.this , iLikedMusicEntity);
                            }
                            // 如果修改的项是第一个，那么更新大图片
                            if (count == 0) {
                                GlobalMethodsUtils.setMusicCover(this , biggerImageCover , iLikedMusicEntity.getMusicCover());
                            }
                        }
                        count += 1;
                    }

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });
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