package com.mrtoad.jianting.Utils;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class ViewAnimationUtils {
    /**
     * 水波纹动画效果
     * @param view view
     */
    public static void waterRipplesAnimation(View view , long duration) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(duration)
                            .setInterpolator(new OvershootInterpolator())
                            .start();
                })
                .start();
    }
}
