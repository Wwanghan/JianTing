package com.mrtoad.jianting.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mrtoad.jianting.Activity.PlayActivity;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Broadcast.MediaMethods;
import com.mrtoad.jianting.Broadcast.Receiver.MediaBroadcastReceiver;
import com.mrtoad.jianting.Broadcast.StandardBroadcastMethods;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.GlobalDataManager;
import com.mrtoad.jianting.Interface.OnBottomPlayerReadyListener;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.ToastUtils;

public class BottomPlayerFragment extends Fragment {

    private LinearLayout bottomPlayerArea;
    private ImageView musicCover;
    private TextView musicName;
    private TextView musicAuthor;
    private ImageView playButton;
    private String musicFilePath;
    private OnBottomPlayerReadyListener onBottomPlayerReadyListener;
    private ILikedMusicEntity iLikedMusicEntity;

    public static BottomPlayerFragment newInstance() {
        BottomPlayerFragment fragment = new BottomPlayerFragment();
        return fragment;
    }

    public BottomPlayerFragment() {
        // Required empty public constructor
    }

    public void setOnBottomPlayerReadyListener(OnBottomPlayerReadyListener onBottomPlayerReadyListener) {
        this.onBottomPlayerReadyListener = onBottomPlayerReadyListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_player, container, false);

        bottomPlayerArea = view.findViewById(R.id.bottom_player_area);
        musicCover = view.findViewById(R.id.music_cover);
        musicName = view.findViewById(R.id.music_name);
        musicAuthor = view.findViewById(R.id.music_author);
        playButton = view.findViewById(R.id.play_button);

        if (onBottomPlayerReadyListener != null) {
            onBottomPlayerReadyListener.onBottomPlayerReady();
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomPlayerArea.setOnClickListener((v) -> {
            Intent intent = new Intent(getActivity() , PlayActivity.class);
            intent.putExtra(PlayActivity.ACTION_KEY_I_LIKED_MUSIC_ENTITY , iLikedMusicEntity);
            startActivity(intent);
        });

        musicCover.setImageResource(R.drawable.music_cover_default);
        musicAuthor.setText("未知作者");

        playButton.setOnClickListener((v) -> {
            if (GlobalDataManager.getInstance().isPlaying()) {
                GlobalDataManager.getInstance().setPlaying(false);
                MediaMethods.pauseMusic(getActivity());
            } else {
                GlobalDataManager.getInstance().setPlaying(true);
                MediaMethods.playMusic(getActivity() , iLikedMusicEntity);
            }
            // 更新底部播放器 UI，同时如果用户不在 MainActivity，下面代码则会通知 MainActivity 更新 UI
            setPlayButton();
            StandardBroadcastMethods.updateBottomPlayerUi(getActivity() , iLikedMusicEntity);
        });
    }

    public void updateUi(ILikedMusicEntity iLikedMusicEntity) {
        this.iLikedMusicEntity = iLikedMusicEntity;
        this.musicName.setText(iLikedMusicEntity.getMusicName());
        this.musicFilePath = iLikedMusicEntity.getMusicFilePath();
        setPlayButton();
    }

    /**
     * 设置播放按钮状态
     */
    private void setPlayButton() {
        if (GlobalDataManager.getInstance().isPlaying()) {
            playButton.setImageResource(R.drawable.pause_button);
        } else {
            playButton.setImageResource(R.drawable.play_button);
        }
    }

}