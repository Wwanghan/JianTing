package com.mrtoad.jianting;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Fragment.FrontPageFragment;
import com.mrtoad.jianting.Fragment.MyFragment;
import com.mrtoad.jianting.Service.PlayService;
import com.mrtoad.jianting.Utils.ToastUtils;

public class MainActivity extends AppCompatActivity {

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

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 设置默认的 Fragment
        loadFragment(new FrontPageFragment());

        bottomNavigationView.setOnItemSelectedListener((item) -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.nav_front_page) {
                fragment = new FrontPageFragment();
            } else if (item.getItemId() == R.id.nav_my) {
                fragment = new MyFragment();
            }
            return loadFragment(fragment);
        });

        // 绑定音乐播放服务（全局唯一，只需要绑定一次）
        Intent bindServiceIntent = new Intent(this , PlayService.class);
        bindService(bindServiceIntent , serviceConnection , BIND_AUTO_CREATE);

        // 创建媒体广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MediaBroadcastAction.ACTION_PLAY);
        registerReceiver(mediaBroadcastReceiver , intentFilter , RECEIVER_EXPORTED);

        /**
         * 监听音乐播放
         */
        mediaBroadcastReceiver.setOnPlayListener((uri) -> {
            playService.play(uri);
        });

    }

    /**
     * 加载fragment
     * @param fragment Fragment 对象
     * @return Boolean 结果
     */
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        isServiceBound = false;

        unregisterReceiver(mediaBroadcastReceiver);
    }
}