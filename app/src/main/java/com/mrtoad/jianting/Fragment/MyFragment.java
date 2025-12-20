package com.mrtoad.jianting.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.mrtoad.jianting.Activity.ILikedMusicActivity;
import com.mrtoad.jianting.Broadcast.Action.MediaBroadcastAction;
import com.mrtoad.jianting.Constants.DialogConstants;
import com.mrtoad.jianting.Constants.SPDataConstants;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.MusicUtils;
import com.mrtoad.jianting.Utils.SPDataUtils;
import com.mrtoad.jianting.Utils.ToastUtils;


public class MyFragment extends Fragment {
    private ImageView avatarImage;
    private LinearLayout importMusicArea;
    private LinearLayout ILikedMusicArea;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> filePickerLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        avatarImage = view.findViewById(R.id.avatar_image);
        importMusicArea = view.findViewById(R.id.import_music_area);
        ILikedMusicArea = view.findViewById(R.id.i_liked_music_area);

        // 注册活动结果监听器
        registerActivityResult();

        // 设置头像
        if (SPDataUtils.getStorageInformation(getContext() , SPDataConstants.AVATAR_IMAGE_KEY) != null) {
            Uri avatarUri = Uri.parse(SPDataUtils.getStorageInformation(getContext(), SPDataConstants.AVATAR_IMAGE_KEY));
            Glide.with(getContext()).load(avatarUri).circleCrop().into(avatarImage);
        } else {
            Glide.with(this).load(R.mipmap.avatar).circleCrop().into(avatarImage);
        }

        /**
         * 点击头像，跳转到图片选择器
         */
        avatarImage.setOnClickListener((v) -> {
            imagePickerLauncher.launch("image/*");
        });

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
            filePickerLauncher.launch("audio/*");
        });

        return view;
    }

    /**
     * 注册活动结果监听器
     */
    private void registerActivityResult() {
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent() , (uri) -> {
            if (uri != null) {
                // 更新头像，并将 Uri 存储在本地
                Glide.with(getContext()).load(uri).circleCrop().into(avatarImage);
                SPDataUtils.storageInformation(getContext() , SPDataConstants.AVATAR_IMAGE_KEY , uri.toString());
            }
        });

        filePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent() , (uri) -> {
            if (uri != null) {
                WaitDialog.show(DialogConstants.WAIT_DIALOG_IMPORT_MUSIC);
                // 保存音乐
                new Thread(() -> {
                    MusicUtils.saveMusic(getContext() , uri);
                }).start();
            }
        });
    }

}