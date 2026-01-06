package com.mrtoad.jianting;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentManager;

import com.kongzue.dialogx.DialogX;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Action.StandardBroadcastAction;
import com.mrtoad.jianting.Broadcast.MediaMethods;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.Receiver.StandardBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.StandardBroadcastMethods;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.Fragment.BottomPlayerFragment;
import com.mrtoad.jianting.Fragment.MainFragment;
import com.mrtoad.jianting.Service.PlayService;
import com.mrtoad.jianting.Utils.FragmentUtils;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.SPDataUtils;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragmentManager;
    private BottomPlayerFragment bottomPlayerFragment;
    private PlayService playService;
    private boolean isServiceBound = false;
    // 绑定服务，获取服务实例
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayService.MyBinder myBinder = (PlayService.MyBinder) iBinder;
            playService = myBinder.getService();
            isServiceBound = true;

            // 播放完毕后更新 UI
            playService.setOnFinishListener((iLikedMusicEntity) -> {
                StandardBroadcastMethods.updateBottomPlayerUi(MainActivity.this , iLikedMusicEntity);
                MediaMethods.finishMusic(MainActivity.this , iLikedMusicEntity);
            });

            // 发送一条顺序播放的播放事件
            playService.setOnSequencePlayListener((iLikedMusicEntity -> {
                StandardBroadcastMethods.updateBottomPlayerUi(MainActivity.this , iLikedMusicEntity);
                MediaMethods.sequencePlay(MainActivity.this , iLikedMusicEntity);
            }));

            // 监听媒体会话控制时间（播放、暂停、上、下首歌曲）
            playService.setOnMediaSessionControlListener((iLikedMusicEntity , controlType) -> {
                StandardBroadcastMethods.updateBottomPlayerUi(MainActivity.this , iLikedMusicEntity);
                MediaMethods.mediaSessionControl(MainActivity.this , iLikedMusicEntity , controlType);
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            playService = null;
            isServiceBound = false;
        }
    };

    private MediaBroadcastReceiver mediaBroadcastReceiver = new MediaBroadcastReceiver();
    private StandardBroadcastReceiver standardBroadcastReceiver = new StandardBroadcastReceiver();

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // TODO 修改状态栏图标颜色为白色。已达到正常在黑色背景下显示的目的。但这个方法需要在每个 Activity 下添加。还没找到更好的方法，暂时先这样
        getWindow().setStatusBarColor(Color.BLACK);
        // 设置状态栏图标为浅色（白色）
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);

        // 初始化 DialogX，方便后续使用
        DialogX.init(this);

        /**
         * 加载底部导航栏 和 底部播放器
         */
        fragmentManager = getSupportFragmentManager();
        // 设置默认的 Fragment
        FragmentUtils.loadFragment(fragmentManager , R.id.fragment_container , new MainFragment());
        // 设置底部音乐播放导航
        bottomPlayerFragment = BottomPlayerFragment.newInstance();
        FragmentUtils.loadFragment(fragmentManager , R.id.bottom_player_fragment , bottomPlayerFragment);
        // 当底部播放器准备好时，设置底部播放器
        bottomPlayerFragment.setOnBottomPlayerReadyListener(() -> {
            GlobalMethodsUtils.setBottmPlayerFragment(MainActivity.this , fragmentManager , bottomPlayerFragment);
        });

        /**
         * 监听底部播放器的更新事件
         */
        standardBroadcastReceiver.setOnUpdateBottomPlayerListener((iLikedMusicEntity -> {
            if (bottomPlayerFragment.isHidden()) {
                // 使用 commitAllowingStateLoss 提交，用于在后台中做更新操作
                getSupportFragmentManager().beginTransaction().show(bottomPlayerFragment).commitAllowingStateLoss();
            }
            bottomPlayerFragment.updateUi(iLikedMusicEntity);
        }));


        // 绑定音乐播放服务（全局唯一，只需要绑定一次）
        Intent bindServiceIntent = new Intent(this , PlayService.class);
        bindService(bindServiceIntent , serviceConnection , BIND_AUTO_CREATE);

        /**
         * 监听音乐播放
         */
        mediaBroadcastReceiver.setOnPlayListener((iLikedMusicEntity) -> {
            playService.play(iLikedMusicEntity);
        });

        /**
         * 监听音乐暂停
         */
        mediaBroadcastReceiver.setOnPauseListener(() -> {
            playService.pause();
        });

        /**
         * 监听音乐进度条进度改变
         */
        mediaBroadcastReceiver.setOnProgressChanged((progress) -> {
            playService.setProgress(progress);
        });

        /**
         * 监听音乐切换播放
         */
        mediaBroadcastReceiver.setOnSwitchPlayListener((iLikedMusicEntity -> {
            playService.switchPlay(iLikedMusicEntity);
        }));

    }

    @Override
    protected void onStart() {
        super.onStart();

        /**
         * 注册广播
         */
        // 注册标准广播接收器中的更新底部播放器 UI 事件广播
        registerReceiver(standardBroadcastReceiver , new IntentFilter(StandardBroadcastAction.ACTION_UPDATE_UI) , RECEIVER_EXPORTED);

        // 创建媒体广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaBroadcastAction.ACTION_PLAY);
        intentFilter.addAction(MediaBroadcastAction.ACTION_PAUSE);
        intentFilter.addAction(MediaBroadcastAction.ACTION_PROGRESS_CHANGED);
        intentFilter.addAction(MediaBroadcastAction.ACTION_SWITCH_PLAY);
        registerReceiver(mediaBroadcastReceiver , intentFilter , RECEIVER_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // TODO 记录一下。这个保存音乐播放位置的代码并不是最好的解决方案
        // TODO 这里在 onStop 下填写保存音乐播放位置的代码，是保证用户退出时一定可以执行到，如果用户直接上划退出，那么程序不会执行 onDestory
        // TODO 但这却是不是一个很好的解决办法。后面有空可以修改通过全局定时器来保存播放位置，修改的话别忘了把 ILikedMusicActivity 中的 onStop 也去掉
        String lastPlayMusicName = SPDataUtils.getStorageInformation(this, SPDataConstants.LAST_PLAY);
        MediaPlayer player = GlobalDataManager.getInstance().getPlayer();
        if (lastPlayMusicName != null && player != null && player.isPlaying()) {
            SPDataUtils.storageInformation(this , SPDataConstants.LAST_PLAY_POSITION , lastPlayMusicName + "_" + player.getCurrentPosition());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);

        /**
         * 解注册广播
         */
        unregisterReceiver(mediaBroadcastReceiver);
        unregisterReceiver(standardBroadcastReceiver);
    }
}