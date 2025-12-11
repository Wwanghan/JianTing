package com.mrtoad.jianting.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.kongzue.dialogx.dialogs.TipDialog;
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

    private static final int REQUEST_CODE_IMPORT_MUSIC = 1;
    private static final int REQUEST_CODE_IMAGE_PICK = 2;

    private ImageView avatarImage;
    private LinearLayout importMusicArea;
    private LinearLayout ILikedMusicArea;


    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container , Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        avatarImage = view.findViewById(R.id.avatar_image);
        importMusicArea = view.findViewById(R.id.import_music_area);
        ILikedMusicArea = view.findViewById(R.id.i_liked_music_area);

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
            openImagePicker();
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
            Intent importMusicIntent = new Intent(Intent.ACTION_GET_CONTENT);
            importMusicIntent.setType("audio/*");
            importMusicIntent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(importMusicIntent, REQUEST_CODE_IMPORT_MUSIC);
        });

        return view;
    }

    /**
     * 处理从子活动返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMPORT_MUSIC && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                WaitDialog.show(DialogConstants.WAIT_DIALOG_IMPORT_MUSIC);
                // 保存音乐
                new Thread(() -> {
                    MusicUtils.saveMusic(getContext() , uri);
                }).start();

            }
        } else if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                // 更新头像，并将 Uri 存储在本地
                Glide.with(getContext()).load(uri).circleCrop().into(avatarImage);
                SPDataUtils.storageInformation(getContext() , SPDataConstants.AVATAR_IMAGE_KEY , uri.toString());
            }
        }
    }

    /**
     * 打开图片选择器
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");  // 只选择图片文件
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // 可选：限制选择单个文件
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

        // 可选：设置选择器标题
        intent.putExtra(Intent.EXTRA_TITLE, "选择图片");

        try {
            startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
        } catch (Exception e) {
            ToastUtils.showToast(getContext() , "没有找到可用的文件选择器");
        }
    }

}