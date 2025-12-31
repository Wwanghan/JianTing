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
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicUtils {

    private static final String TAG = "MusicUtils";
    public static final String NEXT_MUSIC = "next_music";
    public static final String PREVIOUS_MUSIC = "previous_music";

        /**
         * 保存音乐，其中包含两部分
         * 1. 保存音乐文件
         * 2. 保存音乐信息
         * @param context context
         * @param uris uri 列表
         */
        public static void saveMusic(Context context, List<Uri> uris) {
            for (Uri uri : uris) {
                // 保存音乐文件
                File file = saveMusicFile(context, uri);
                if (file != null) {
                    // 保存音乐信息
                    saveMusicInfo(context , file);
                }
            }
            TipDialog.show(DialogConstants.TIP_DIALOG_IMPORT_MUSIC_SUCCESS , WaitDialog.TYPE.SUCCESS);
    }

    @SuppressLint("ResourceType")
    private static void saveMusicInfo(Context context , File file) {
        Map<String , String> musicInfoMap = new HashMap<>();

        // 元数据提取
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file.getAbsolutePath());
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String author = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        author = author == null ? "未知作者" : author;

        String fileName = file.getName().split("\\.")[0];
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_NAME , fileName);
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_AUTHOR , author);
        musicInfoMap.put(MusicInfoConstants.MUSIC_INFO_COVER , null);
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
        if (file.exists()) {
            return null;
        }

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

    /**
     * 获取下一首或上一首歌曲
     * @param context context
     * @param musicName 歌曲名
     * @param type 类型
     * @return 歌曲实体信息
     */
    public static ILikedMusicEntity getNextOrPreviousMusic(Context context , String musicName , String type) {
        List<String> localMusicList = SPDataUtils.getLocalList(context, LocalListConstants.LOCAL_LIST_I_LIKED_MUSIC);
        Collections.reverse(localMusicList);

        int resultIndex = 0;
        int currentIndex = localMusicList.indexOf(musicName);

        if (type.equals(NEXT_MUSIC)) {
            resultIndex = currentIndex + 1 < localMusicList.size() ? currentIndex + 1 : 0;
        } else if (type.equals(PREVIOUS_MUSIC)) {
            resultIndex = currentIndex - 1 >= 0 ? currentIndex - 1 : localMusicList.size() - 1;
        }

        String name = localMusicList.get(resultIndex);
        ILikedMusicEntity resultMusicEntity = GlobalMethodsUtils.getMusicEntityByMusicName(context, name);

        return resultMusicEntity;
    }

}
