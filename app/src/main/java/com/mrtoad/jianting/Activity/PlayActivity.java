package com.mrtoad.jianting.Activity;

import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.MediaMethods;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.StandardBroadcastMethods;
import com.mrtoad.jianting.Constants.LocalListConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Constants.ToastConstants;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.GradientColorExtractor;
import com.mrtoad.jianting.Utils.GradientUtils;
import com.mrtoad.jianting.Utils.SPDataUtils;
import com.mrtoad.jianting.Utils.TimeUtils;
import com.mrtoad.jianting.Utils.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

        iLikedMusicEntity = getIntent().getParcelableExtra(ACTION_KEY_I_LIKED_MUSIC_ENTITY);

        setData();

        // 获取音乐播放列表
        musicList = SPDataUtils.getLocalList(PlayActivity.this, LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC);
        Collections.reverse(musicList);

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
            int currentIndex = musicList.indexOf(iLikedMusicEntity.getMusicName());
            if (currentIndex - 1 >= 0) {
                int preIndex = currentIndex - 1;
                switchPlayAndUpdateData(preIndex);
            } else {
                ToastUtils.showToast(PlayActivity.this , ToastConstants.NO_PREVIOUS_MUSIC);
            }
        });

        /**
         * 切换下一首歌曲
         */
        nextMusic.setOnClickListener((v) -> {
            int currentIndex = musicList.indexOf(iLikedMusicEntity.getMusicName());
            if (currentIndex + 1 < musicList.size()) {
                int nextIndex = currentIndex + 1;
                switchPlayAndUpdateData(nextIndex);
            } else {
                ToastUtils.showToast(PlayActivity.this , ToastConstants.NO_NEXT_MUSIC);
            }
        });
    }

    /**
     * 根据索引获取歌曲名称，最后将歌曲名当做 Key，获取上一首歌曲的实体对象
     * @param index 歌曲索引
     */
    private void switchPlayAndUpdateData(int index) {
        String musicName = musicList.get(index);
        ILikedMusicEntity musicEntity = GlobalMethodsUtils.getMusicEntityByMusicName(PlayActivity.this, musicName);
        MediaMethods.switchPlay(PlayActivity.this , musicEntity);
        this.iLikedMusicEntity = musicEntity;

        GlobalDataManager.getInstance().setPlaying(true);
        GlobalMethodsUtils.setPlayButton(playButton);
        setData();
        cannelPlayerTimer();
        startPlayerTimer();

        SPDataUtils.storageInformation(PlayActivity.this , SPDataConstants.LAST_PLAY , musicName);
        StandardBroadcastMethods.updateBottomPlayerUi(PlayActivity.this , iLikedMusicEntity);
    }

    /**
     * 启动音乐播放计时器
     */
    private void startPlayerTimer() {
        if (timer == null) { timer = new Timer(); }

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
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * 初始设置
     * 设置动态渐变背景、设置基本数据、设置播放按钮显示
     */
    private void setData() {
        // 设置动态渐变背景
        if (iLikedMusicEntity.getMusicCover() != null) {
            GradientColorExtractor.setGradientFromFilePathSync(root , iLikedMusicEntity.getMusicCover() , 135);
        } else {
            Bitmap bitmap = GlobalMethodsUtils.getBitmapFromVectorDrawable(PlayActivity.this, R.drawable.music_cover_default);
            GradientColorExtractor.setGradientFromBitmapSync(root , bitmap , 135);
        }

        GlobalMethodsUtils.setMusicCover(PlayActivity.this , musicCover , iLikedMusicEntity.getMusicCover());
        musicName.setText(iLikedMusicEntity.getMusicName());
        musicAuthor.setText(iLikedMusicEntity.getMusicAuthor());
        playButton.setImageResource(R.drawable.play_button);
        musicSeekBar.setMax(Integer.parseInt(iLikedMusicEntity.getDuration()));
        totalPlayTime.setText(TimeUtils.MillisToTime(Integer.parseInt(iLikedMusicEntity.getDuration())));

        // 先设置播放按钮显示
        GlobalMethodsUtils.setPlayButton(playButton);
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(mediaBroadcastReceiver , new IntentFilter(MediaBroadcastAction.ACTION_FINISH) , RECEIVER_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mediaBroadcastReceiver);
    }
}