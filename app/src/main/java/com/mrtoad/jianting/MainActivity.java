package com.mrtoad.jianting;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kongzue.dialogx.DialogX;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Action.StandardBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.Receiver.StandardBroadcastReceiver;
import com.mrtoad.jianting.Fragment.BottomPlayerFragment;
import com.mrtoad.jianting.Fragment.FrontPageFragment;
import com.mrtoad.jianting.Fragment.MyFragment;
import com.mrtoad.jianting.Interface.OnBottomPlayerReadyListener;
import com.mrtoad.jianting.Service.PlayService;
import com.mrtoad.jianting.Utils.FragmentUtils;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.ToastUtils;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        /**
         * 加载底部导航栏 和 底部播放器
         */
        fragmentManager = getSupportFragmentManager();
        // 设置默认的 Fragment
        FragmentUtils.loadFragment(fragmentManager , R.id.fragment_container , new FrontPageFragment());

        bottomNavigationView.setOnItemSelectedListener((item) -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.nav_front_page) {
                fragment = new FrontPageFragment();
            } else if (item.getItemId() == R.id.nav_my) {
                fragment = new MyFragment();
            }
            return FragmentUtils.loadFragment(fragmentManager , R.id.fragment_container , fragment);
        });

        bottomPlayerFragment = BottomPlayerFragment.newInstance();
        FragmentUtils.loadFragment(fragmentManager , R.id.bottom_player_fragment , bottomPlayerFragment);

        // 当底部播放器准备好时，设置底部播放器
        bottomPlayerFragment.setOnBottomPlayerReadyListener(() -> {
            GlobalMethodsUtils.setBottmPlayerFragment(MainActivity.this , fragmentManager , bottomPlayerFragment);
        });

        /**
         * 监听底部播放器的更新事件
         */
        standardBroadcastReceiver.setOnUpdateUiListener(new StandardBroadcastReceiver.onUpdateBottomPlayerUiListener() {
            @Override
            public void updateUi(String musicName, String musicFilePath) {
                if (bottomPlayerFragment.isHidden()) {
                    // 使用 commitAllowingStateLoss 提交，用于在后台中做更新操作
                    getSupportFragmentManager().beginTransaction().show(bottomPlayerFragment).commitAllowingStateLoss();
                }
                bottomPlayerFragment.updateUi(musicName , musicFilePath);
            }
        });


        // 绑定音乐播放服务（全局唯一，只需要绑定一次）
        Intent bindServiceIntent = new Intent(this , PlayService.class);
        bindService(bindServiceIntent , serviceConnection , BIND_AUTO_CREATE);

        /**
         * 监听音乐播放
         */
        mediaBroadcastReceiver.setOnPlayListener((filePath) -> {
            playService.play(filePath);
        });

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
        registerReceiver(mediaBroadcastReceiver , intentFilter , RECEIVER_EXPORTED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        isServiceBound = false;

        /**
         * 解注册广播
         */
        unregisterReceiver(mediaBroadcastReceiver);
        unregisterReceiver(standardBroadcastReceiver);
    }
}