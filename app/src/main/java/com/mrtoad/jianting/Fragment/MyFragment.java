package com.mrtoad.jianting.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.mrtoad.jianting.Activity.ILikedMusicActivity;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.MusicUtils;


public class MyFragment extends Fragment {

    private static final int REQUEST_CODE_IMPORT_MUSIC = 1;

    private ImageView avatarImage;
    private LinearLayout importMusicArea;
    private LinearLayout ILikedMusicArea;


    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        avatarImage = view.findViewById(R.id.avatar_image);
        importMusicArea = view.findViewById(R.id.import_music_area);
        ILikedMusicArea = view.findViewById(R.id.i_liked_music_area);

        Glide.with(this).load(R.mipmap.avatar).circleCrop().into(avatarImage);

        /**
         * 跳转到我喜欢的音乐
         */
        ILikedMusicArea.setOnClickListener((v) -> {
            Intent intent = new Intent(getContext() , ILikedMusicActivity.class);
            startActivity(intent);
        });

        /**
         * 导入音乐
         */
        importMusicArea.setOnClickListener((v) -> {
            Intent importMusicIntent = new Intent(Intent.ACTION_GET_CONTENT);
            importMusicIntent.setType("audio/*");
            importMusicIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(importMusicIntent, REQUEST_CODE_IMPORT_MUSIC);
        });

        return view;
    }

    /**
     * 获取导入的音乐
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_MUSIC && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // 保存音乐
                MusicUtils.saveMusic(getContext() , uri);
            }
        }
    }

}