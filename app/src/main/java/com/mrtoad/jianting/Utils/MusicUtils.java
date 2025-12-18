package com.mrtoad.jianting.Utils;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Log;

import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.mrtoad.jianting.Constants.DialogConstants;
import com.mrtoad.jianting.Constants.LocalListConstants;
import com.mrtoad.jianting.Constants.MusicInfoConstants;
import com.mrtoad.jianting.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class MusicUtils {

    private static final String TAG = "MusicUtils";

    /**
     * 保存音乐，其中包含两部分
     * 1. 保存音乐文件
     * 2. 保存音乐信息
     * @param context context
     * @param uri uri
     */
    public static void saveMusic(Context context, Uri uri) {
        // 保存音乐文件
        File file = saveMusicFile(context, uri);

        // 保存音乐信息
        assert file != null;
        saveMusicInfo(context , file);

        TipDialog.show(DialogConstants.TIP_DIALOG_IMPORT_MUSIC_SUCCESS , WaitDialog.TYPE.SUCCESS);
    }

    @SuppressLint("ResourceType")
    private static void saveMusicInfo(Context context , File file) {
        Map<String , String> musicInfoMap = new HashMap<>();

        // 元数据提取
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file.getAbsolutePath());
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        String fileName = file.getName().split("\\.")[0];
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_NAME , fileName);
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_AUTHOR , "未知作者");
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_COVER , String.valueOf(R.drawable.music_cover_default));
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_FILE_PATH , file.getAbsolutePath());
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_DURATION , duration);
        SPDataUtils.storeMapInformation(context , fileName , musicInfoMap);

        SPDataUtils.addLocalList(context , LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC , fileName);


    }

    /**
     * 保存音乐文件
     * @param context context
     * @param uri uri
     * @return 文件名
     */
    private static File saveMusicFile(Context context , Uri uri) {
        // 获取文件名
        String fileName = getFileNameFromUri(context, uri);

        // 防止文件名为空，如果为空，则生成一个随机的文件名
        if (fileName == null) {
            fileName = "music_" + System.currentTimeMillis() + ".mp3";
        }

        // 创建文件
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName);

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            FileUtils.copy(inputStream, fileOutputStream);
            return file;
        } catch (FileNotFoundException e) {
            ToastUtils.showToast(context , "文件未找到");
            Log.d(TAG , "文件未找到: " + e.getMessage());
        } catch (IOException e) {
            ToastUtils.showToast(context , "文件保存失败");
            Log.d(TAG , "文件保存失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 根据 Uri 获取文件名
     * @param context context
     * @param uri Uri
     * @return 文件名
     */
    private static String getFileNameFromUri(Context context , Uri uri) {
        String fileName = null;

        Cursor query = context.getContentResolver().query(uri, null, null, null, null);

        if (query != null) {
            query.moveToFirst();
            fileName = query.getString(query.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            query.close();
        }
        return fileName;
    }

}
