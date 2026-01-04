package com.mrtoad.jianting.Utils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class ViewAnimationUtils {
    /**
     * 水波纹动画效果
     * @param view view
     * @param duration 动画时长
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

    /**
     * 淡入动画
     * @param view view
     */
    public static void fadeInAnimation(View view , long duration) {
        if (view.getVisibility() == VISIBLE) { return; }

        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .withStartAction(() -> { view.setVisibility(View.VISIBLE); })
                .start();
    }

    /**
     * 淡出动画
     * @param view view
     */
    public static void fadeOutAnimation(View view , long duration , Runnable endAction) {
        if (view.getVisibility() == GONE) { return; }

        view.setAlpha(1f);
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new LinearInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(GONE);
                    endAction.run();
                })
                .start();
    }
}
