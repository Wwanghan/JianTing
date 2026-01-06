package com.mrtoad.jianting.Activity;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.interfaces.OnDialogButtonClickListener;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.MediaMethods;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.StandardBroadcastMethods;
import com.mrtoad.jianting.Constants.ControlTypeConstants;
import com.mrtoad.jianting.Constants.LocalListConstants;
import com.mrtoad.jianting.Constants.MapConstants;
import com.mrtoad.jianting.Constants.MediaPlayModelConstants;
import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Constants.ViewAnimationConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.GradientUtils;
import com.mrtoad.jianting.Utils.KMeansColorExtractor;
import com.mrtoad.jianting.Utils.MusicUtils;
import com.mrtoad.jianting.Utils.SPDataUtils;
import com.mrtoad.jianting.Utils.TimeUtils;
import com.mrtoad.jianting.Utils.ToastUtils;
import com.mrtoad.jianting.Utils.ViewAnimationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PlayActivity extends AppCompatActivity {

    public static final String ACTION_KEY_I_LIKED_MUSIC_ENTITY = "iLikedMusicEntity";
    private LinearLayout root;
    private ImageView musicCover;
    private TextView musicName;
    private TextView musicAuthor;
    private ImageView playButton;
    private TextView currentPlayTime;
    private TextView totalPlayTime;
    private ImageView previousMusic;
    private ImageView nextMusic;
    private ILikedMusicEntity iLikedMusicEntity;
    private SeekBar musicSeekBar;
    private Timer timer;
    private boolean isTrackingTouch = false;
    private MediaBroadcastReceiver mediaBroadcastReceiver = new MediaBroadcastReceiver();
    private List<String> musicList = new ArrayList<>();
    private ImageView musicPlayListImageView;
    private String[] musicMenuList;
    private ImageView playModelIcon;
    // 获取当前播放模式
    private int currentPlayModel;
    private static final String MUSIC_LIST_STRING = "音乐列表";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);

        root = findViewById(R.id.root);
        musicCover = findViewById(R.id.music_cover);
        musicName = findViewById(R.id.music_name);
        musicAuthor = findViewById(R.id.music_author);
        playButton = findViewById(R.id.play_button);
        musicSeekBar = findViewById(R.id.music_seek_bar);
        currentPlayTime = findViewById(R.id.current_play_time);
        totalPlayTime = findViewById(R.id.total_play_time);
        previousMusic = findViewById(R.id.previous_music);
        nextMusic = findViewById(R.id.next_music);
        musicPlayListImageView = findViewById(R.id.music_play_list_image_view);
        playModelIcon = findViewById(R.id.play_model_icon);

        iLikedMusicEntity = getIntent().getParcelableExtra(ACTION_KEY_I_LIKED_MUSIC_ENTITY);
        currentPlayModel = GlobalDataManager.getInstance().getCurrentPlayModel(this);

        // 为视图设置数据
        setData();
        // 设置音乐列表和音乐菜单列表
        setMusicList();

        /**
         * 监听进度条事件
         */
        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentPlayTime.setText(TimeUtils.MillisToTime(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isTrackingTouch = false;
                MediaMethods.setProgress(PlayActivity.this , seekBar.getProgress());
            }
        });

        if (GlobalDataManager.getInstance().isPlaying()) {
            startPlayerTimer();
        }

        /**
         * 设置播放按钮点击事件
         */
        playButton.setOnClickListener((v) -> {
            ViewAnimationUtils.waterRipplesAnimation(playButton , ViewAnimationConstants.WATER_RIPPLES_DURATION);
            if (GlobalDataManager.getInstance().isPlaying()) {
                GlobalDataManager.getInstance().setPlaying(false);
                MediaMethods.pauseMusic(PlayActivity.this);
                cannelPlayerTimer();
            } else {
                GlobalDataManager.getInstance().setPlaying(true);
                MediaMethods.playMusic(PlayActivity.this , iLikedMusicEntity);
                startPlayerTimer();
            }
            // 设置播放按钮显示，并发送更新底部播放器 UI 广播
            GlobalMethodsUtils.setPlayButton(playButton);
            StandardBroadcastMethods.updateBottomPlayerUi(PlayActivity.this , iLikedMusicEntity);
        });

        /**
         * 设置音乐播放完成事件
         */
        mediaBroadcastReceiver.setOnFinishListener((item) -> {
            GlobalMethodsUtils.setPlayButton(playButton);
        });

        /**
         * 切换上一首歌曲
         */
        previousMusic.setOnClickListener((v) -> {
            switchPlay(MusicUtils.PREVIOUS_MUSIC);
        });

        /**
         * 切换下一首歌曲
         */
        nextMusic.setOnClickListener((v) -> {
            switchPlay(MusicUtils.NEXT_MUSIC);
        });

        /**
         * 监听顺序播放事件
         */
        mediaBroadcastReceiver.setOnSequencePlayListener((item) -> {
            ViewAnimationUtils.fadeOutAnimation(musicCover , ViewAnimationConstants.FADE_OUT_DURATION , () -> {
                iLikedMusicEntity = item;
                updateData();
            });
        });

        /**
         * 监听媒体会话控制事件
         */
        mediaBroadcastReceiver.setOnMediaSessionControlListener((item , controlType) -> {
            iLikedMusicEntity = item;
            // 如果我在前台通知上点击暂停，那么我不用直接调用 updateData。只需要更新数据和停止播放器即可
            if (controlType == ControlTypeConstants.MEDIA_CONTROL_TYPE_PAUSE) {
                StandardBroadcastMethods.updateBottomPlayerUi(PlayActivity.this , iLikedMusicEntity);
                setData();
                cannelPlayerTimer();
            } else {
                updateData();
            }
        });

        /**
         * 播放列表图标点击事件
         */
        musicPlayListImageView.setOnClickListener((v) -> {
            ViewAnimationUtils.waterRipplesAnimation(musicPlayListImageView , ViewAnimationConstants.WATER_RIPPLES_DURATION);

            /**
             * 音乐菜单列表
             */
            BottomMenu.show(musicMenuList)
                    .setMessage(MUSIC_LIST_STRING)
                    .setOnMenuItemClickListener((dialog , text , index) -> {
                        ViewAnimationUtils.fadeOutAnimation(musicCover , ViewAnimationConstants.FADE_OUT_DURATION , () -> {
                            // 获取点击的音乐的实体，并播放它
                            ILikedMusicEntity musicEntity = GlobalMethodsUtils.getMusicEntityByMusicName(PlayActivity.this, String.valueOf(text));
                            MediaMethods.switchPlay(PlayActivity.this , musicEntity);
                            // 更新数据
                            iLikedMusicEntity = musicEntity;
                            updateData();
                        });
                        return false;
                    });

        });

        /**
         * 播放模式切换（更新)
         */
        updatePlayModel();
        playModelIcon.setOnClickListener((v) -> {
            ViewAnimationUtils.waterRipplesAnimation(playModelIcon , ViewAnimationConstants.WATER_RIPPLES_DURATION);
            changePlayModel();
        });
    }

    /**
     * 切换播放（上一首、下一首）
     * @param direction
     */
    private void switchPlay(String direction) {
        ViewAnimationUtils.waterRipplesAnimation(
                direction == MusicUtils.NEXT_MUSIC ? nextMusic : previousMusic,
                ViewAnimationConstants.WATER_RIPPLES_DURATION
        );

        // 先让封面淡出
        ViewAnimationUtils.fadeOutAnimation(musicCover , ViewAnimationConstants.FADE_OUT_DURATION , () -> {
            // 获取下一首要播放的歌曲实体类
            ILikedMusicEntity nextMusicEntity = MusicUtils.getNextOrPreviousMusic(this, iLikedMusicEntity.getMusicName(), direction);
            MediaMethods.switchPlay(this , nextMusicEntity);
            // 更新数据
            iLikedMusicEntity = nextMusicEntity;
            updateData();
        });
    }

    /**
     * 更新数据
     */
    private void updateData() {
        // 更新全局数据
        GlobalDataManager.getInstance().setPlaying(true);
        StandardBroadcastMethods.updateBottomPlayerUi(PlayActivity.this , iLikedMusicEntity);
        SPDataUtils.storageInformation(PlayActivity.this , SPDataConstants.LAST_PLAY , iLikedMusicEntity.getMusicName());
        // 更新当前 UI
        startPlayerTimer();
        setData();
    }

    /**
     * 启动音乐播放计时器
     */
    private void startPlayerTimer() {
        // 确保取消之前的定时器
        cannelPlayerTimer();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isTrackingTouch) {
                    if (musicSeekBar.getProgress() >= musicSeekBar.getMax() && !GlobalDataManager.getInstance().isPlaying()) {
                        runOnUiThread(() -> {
                            musicSeekBar.setProgress(0);
                            currentPlayTime.setText("00:00");
                        });
                        cannelPlayerTimer();
                    } else {
                        if (GlobalDataManager.getInstance().getPlayer() != null) {
                            musicSeekBar.setProgress(GlobalDataManager.getInstance().getPlayer().getCurrentPosition());
                        }
                    }
                }
            }
        } , 0 , 1000);
    }

    /**
     * 取消音乐播放计时器
     */
    private void  cannelPlayerTimer() {
        if (timer != null) {
            try {
                timer.cancel();
                timer.purge();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                timer = null;
            }
        }
    }

    /**
     * 设置音乐列表（音乐列表 和 音乐菜单列表）
     */
    private void setMusicList() {
        // 音乐列表，音乐名列表
        musicList = SPDataUtils.getLocalList(PlayActivity.this, LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC);
        Collections.reverse(musicList);
        // 音乐菜单列表
        musicMenuList = new String[musicList.size()];
        for (int i = 0; i < musicList.size(); i++) {
            musicMenuList[i] = musicList.get(i);
        }
    }

    /**
     * 初始设置
     * 设置动态渐变背景、设置基本数据、设置播放按钮显示
     */
    private void setData() {
        // 设置动态渐变背景
        if (iLikedMusicEntity.getMusicCover() != null) {
            // 根据添加封面时，保存在本地的音乐主次颜色来直接设置。
            String mapKey = iLikedMusicEntity.getMusicName() + MapConstants.MUSIC_COVER_COLOR_MAP_SUFFIX;
            if (SPDataUtils.getMapInformation(this , mapKey) != null) {
                Map<String, String> musicCoverColorsMap = SPDataUtils.getMapInformation(this, mapKey);
                GradientUtils.setGradientBackground(
                        root,
                        Integer.parseInt(musicCoverColorsMap.get(MusicInfoConstants.MUSIC_INFO_PRIMARY_COLOR)),
                        Integer.parseInt(musicCoverColorsMap.get(MusicInfoConstants.MUSIC_INFO_SECONDARY_COLOR)),
                        90
                );
            } else {
                // 做两手准备。万一本地没有存储。万一有 BUG。在本地没有找到的情况下，直接从图片中提取
                KMeansColorExtractor.extractColorsFromFilePath(root , iLikedMusicEntity.getMusicCover() , 90 , null);
            }
        } else {
            Bitmap bitmap = GlobalMethodsUtils.getBitmapFromVectorDrawable(PlayActivity.this, R.drawable.music_cover_default);
            KMeansColorExtractor.extractColorsFromBitmapSync(root , bitmap , 90);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        ViewAnimationUtils.fadeInAnimation(musicCover , ViewAnimationConstants.FADE_IN_DURATION);
        GlobalMethodsUtils.setMusicCover(PlayActivity.this , musicCover , iLikedMusicEntity.getMusicCover());


        musicName.setText(iLikedMusicEntity.getMusicName());
        musicAuthor.setText(iLikedMusicEntity.getMusicAuthor());
        playButton.setImageResource(R.drawable.play_button);
        musicSeekBar.setMax(Integer.parseInt(iLikedMusicEntity.getDuration()));
        totalPlayTime.setText(TimeUtils.MillisToTime(Integer.parseInt(iLikedMusicEntity.getDuration())));

        // 先设置播放按钮显示
        GlobalMethodsUtils.setPlayButton(playButton);
    }

    /**
     * 更新播放模式到 UI
     */
    private void updatePlayModel() {
        if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_SEQUENCE) {
            playModelIcon.setImageResource(R.drawable.sequence_play);
        } else if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_CYCLE) {
            playModelIcon.setImageResource(R.drawable.cycle_play);
        }
        SPDataUtils.storageInformation(PlayActivity.this , SPDataConstants.PLAY_MODEL , String.valueOf(currentPlayModel));
    }

    private void changePlayModel() {
        currentPlayModel = currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_SEQUENCE ? MediaPlayModelConstants.PLAY_MODEL_CYCLE : MediaPlayModelConstants.PLAY_MODEL_SEQUENCE;
        if (currentPlayModel == MediaPlayModelConstants.PLAY_MODEL_SEQUENCE) {
            ToastUtils.showToast(PlayActivity.this , MediaPlayModelConstants.PLAY_MODEL_SEQUENCE_TEXT);
        } else {
            ToastUtils.showToast(PlayActivity.this , MediaPlayModelConstants.PLAY_MODEL_CYCLE_TEXT);
        }
        updatePlayModel();
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaBroadcastAction.ACTION_FINISH);
        intentFilter.addAction(MediaBroadcastAction.ACTION_SEQUENCE_PLAY);
        intentFilter.addAction(MediaBroadcastAction.ACTION_MEDIA_SESSION_CONTROL);
        registerReceiver(mediaBroadcastReceiver , intentFilter , RECEIVER_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 取消定时器
        cannelPlayerTimer();
        // 清理可能存在的引用
        if (musicCover != null) {
            musicCover.setImageDrawable(null);
        }

        unregisterReceiver(mediaBroadcastReceiver);
    }
}