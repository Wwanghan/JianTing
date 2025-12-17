package com.mrtoad.jianting.Activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mrtoad.jianting.Entity.ILikedMusicEntity;
import com.mrtoad.jianting.R;
import com.mrtoad.jianting.Utils.GlobalMethodsUtils;
import com.mrtoad.jianting.Utils.GradientColorExtractor;
import com.mrtoad.jianting.Utils.GradientUtils;

public class PlayActivity extends AppCompatActivity {

    public static final String ACTION_KEY_I_LIKED_MUSIC_ENTITY = "iLikedMusicEntity";
    private LinearLayout root;
    private ImageView musicCover;
    private TextView musicName;
    private TextView musicAuthor;
    private ImageView playButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play);

        root = findViewById(R.id.root);
        musicCover = findViewById(R.id.music_cover);
        musicName = findViewById(R.id.music_name);
        musicAuthor = findViewById(R.id.music_author);
        playButton = findViewById(R.id.play_button);

        ILikedMusicEntity iLikedMusicEntity = getIntent().getParcelableExtra(ACTION_KEY_I_LIKED_MUSIC_ENTITY);

        // 设置动态渐变背景
        Bitmap bitmap = GlobalMethodsUtils.getBitmapFromVectorDrawable(PlayActivity.this, R.drawable.music_cover_default);
        GradientColorExtractor.setGradientFromBitmapSync(root , bitmap , 135);

        musicCover.setImageBitmap(iLikedMusicEntity.getMusicCover());
        musicName.setText(iLikedMusicEntity.getMusicName());
        musicAuthor.setText(iLikedMusicEntity.getMusicAuthor());
        playButton.setImageResource(R.drawable.play_button);

    }
}