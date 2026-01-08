package com.mrtoad.jianting.Service.Manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.mrtoad.jianting.Activity.PlayActivity;
import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.MainActivity;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Service.Constants.NotificationManagerConstants;

public class NotificationManager {
    private Context context;
    private NotificationManagerCompat notificationManager;

    /**
     * 构造函数
     * @param context
     */
    public NotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    /**
     * 创建音乐播放会话渠道
     */
    public void createMediaSessionChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NotificationManagerConstants.MUSIC_PLAY_SESSION_CHANNEL_ID,
                    NotificationManagerConstants.MUSIC_PLAY_SESSION_CHANNEL_NAME,
                    // 媒体通知优先级使用低即可，不需要打扰用户
                    android.app.NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription(NotificationManagerConstants.MUSIC_PLAY_SESSION_CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * 判断音乐播放会话渠道是否存在
     * @param channelId 渠道ID
     * @return 渠道是否存在 boolean
     */
    public boolean chanelIsExist(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ContextCompat.getSystemService(context , android.app.NotificationManager.class).getNotificationChannel(channelId) != null;
        }
        return false;
    }

    /**
     * 构建音乐播放通知
     * @return 通知对象
     */
    public Notification buildMediaNotification(ILikedMusicEntity entity , boolean isPlaying , MediaSessionCompat.Token token) {
        // 判断当前播放状态，设置对应按钮（播放/暂停切换）
        String playPauseText;
        int playPauseAction;
        if (isPlaying) {
            playPauseText = "暂停";
            playPauseAction = Math.toIntExact(PlaybackStateCompat.ACTION_PAUSE); // 当前播放中，按钮触发暂停
        } else {
            playPauseText = "播放";
            playPauseAction = Math.toIntExact(PlaybackStateCompat.ACTION_PLAY); // 当前暂停，按钮触发播放
        }

        // 创建跳转到 PlayActivity 的 Intent/ 创建跳转到 PlayActivity 的 Intent
        // 使用 TaskStackBuilder 来构建完整的回退栈，确保点击跳转到 PlayActivity 后，返回可以返回到 MainActivity
        // 也就是保证了。MainActivity 一定存在。
        Intent mainIntent = new Intent(context , MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent playIntent = new Intent(context , PlayActivity.class);
        playIntent.putExtra(PlayActivity.ACTION_KEY_I_LIKED_MUSIC_ENTITY, entity);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntent(mainIntent);
        taskStackBuilder.addNextIntent(playIntent);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context , NotificationManagerConstants.MUSIC_PLAY_SESSION_CHANNEL_ID)
                // 通知标题（歌曲名）
                .setContentTitle(entity.getMusicName())
                // 通知内容（歌曲作者名）
                .setContentText(entity.getMusicAuthor())
                // 设置通知点击后的跳转
                .setContentIntent(pendingIntent)
                // 通知优先级（媒体通知优先级使用低即可，不需要打扰用户）
                .setPriority(NotificationCompat.PRIORITY_LOW)
                // 点击通知后是否自动消失（不需要消失，通知常驻）
                .setAutoCancel(false)
                // 锁屏显示（Android 5.0+）显示通知内容和控制按钮
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        /**
         * 设置当前音乐封面，两种情况
         * 如果当前音乐有封面，则使用封面当大图标和应用图标当小图标
         * 如果当前音乐没有封面，则使用默认图标当大图标和小图标
         */
        String filePath = entity.getMusicCover();
        if (filePath != null && !filePath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            if (bitmap != null) {
                // 缩放图片到合适大小（大图标推荐 64x64 dp 或 128x128 像素）
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 128, 128, true);
                // 设置大、小图标
                builder.setLargeIcon(scaledBitmap);
                builder.setSmallIcon(R.mipmap.app_icon);
            }
        } else {
            // 大、小图标均使用默认图标
            builder.setSmallIcon(R.drawable.music_cover_default);
        }

        /**
         * 添加控制按钮
         */
        builder.addAction(
                R.drawable.previous_music,
                "上一首",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context , PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        );

        builder.addAction(
                playPauseAction,
                playPauseText,
                MediaButtonReceiver.buildMediaButtonPendingIntent(context , playPauseAction)
        );

        builder.addAction(
                R.drawable.next_music,
                "下一首",
                MediaButtonReceiver.buildMediaButtonPendingIntent(context , PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
        );

        /**
         * 关键。设置MediaStyle，绑定MediaSession，实现通知与MediaSession联动
         * 设置后，MediaSession 才会和通知进行联动
         */
        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(token)
                .setShowActionsInCompactView(0, 1, 2)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context , PlaybackStateCompat.ACTION_STOP))
        );

        return builder.build();
    }

    /**
     * 取消音乐播放通知
     */
    public void cannel() {
        notificationManager.cancel(NotificationManagerConstants.MUSIC_PLAY_SESSION_NOTIFICATION_ID);
    }

    /**
     * 获取音乐播放通知的ID
     * @return 音乐播放通知的ID
     */
    public int getNotificationId() {
        return NotificationManagerConstants.MUSIC_PLAY_SESSION_NOTIFICATION_ID;
    }

    /**
     * 获取音乐会话渠道的ID
     * @return 音乐会话渠道的ID
     */
    public String getChannelId() {
        return NotificationManagerConstants.MUSIC_PLAY_SESSION_CHANNEL_ID;
    }
}
