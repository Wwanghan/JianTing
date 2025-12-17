package com.mrtoad.jianting.Utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.palette.graphics.Palette;

public class GradientColorExtractor {

    /**
     * 从 Bitmap 中提取两种主要颜色并设置渐变背景
     * @param view 需要设置背景的 View
     * @param bitmap 用于提取颜色的图片
     * @param angle 渐变角度
     * @param listener 回调接口（可选）
     */
    public static void setGradientFromBitmap(View view, Bitmap bitmap, int angle,
                                             OnColorsExtractedListener listener) {
        if (view == null || bitmap == null) {
            return;
        }

        // 使用 Palette 提取颜色
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                // 从调色板中提取颜色
                int color1 = extractPrimaryColor(palette);
                int color2 = extractSecondaryColor(palette);

                // 设置渐变背景
                GradientUtils.setGradientBackground(view, color1, color2, angle);

                // 回调颜色信息
                if (listener != null) {
                    listener.onColorsExtracted(color1, color2);
                }
            }
        });
    }

    /**
     * 提取主要颜色（优先选择鲜艳的颜色）
     */
    private static int extractPrimaryColor(Palette palette) {
        int color;

        // 优先选择鲜艳的颜色
        if (palette.getVibrantSwatch() != null) {
            color = palette.getVibrantSwatch().getRgb();
        } else if (palette.getMutedSwatch() != null) {
            color = palette.getMutedSwatch().getRgb();
        } else if (palette.getLightVibrantSwatch() != null) {
            color = palette.getLightVibrantSwatch().getRgb();
        } else if (palette.getDarkVibrantSwatch() != null) {
            color = palette.getDarkVibrantSwatch().getRgb();
        } else if (palette.getLightMutedSwatch() != null) {
            color = palette.getLightMutedSwatch().getRgb();
        } else if (palette.getDarkMutedSwatch() != null) {
            color = palette.getDarkMutedSwatch().getRgb();
        } else {
            // 如果没有提取到颜色，使用默认颜色
            color = Color.parseColor("#2196F3");
        }

        return color;
    }

    /**
     * 提取次要颜色（优先选择对比色）
     */
    private static int extractSecondaryColor(Palette palette) {
        int color;

        // 优先选择对比色
        if (palette.getDarkVibrantSwatch() != null && palette.getVibrantSwatch() != null) {
            // 如果主要颜色是亮色，次要颜色选择暗色
            color = palette.getDarkVibrantSwatch().getRgb();
        } else if (palette.getLightVibrantSwatch() != null && palette.getVibrantSwatch() != null) {
            // 如果主要颜色是暗色，次要颜色选择亮色
            color = palette.getLightVibrantSwatch().getRgb();
        } else if (palette.getMutedSwatch() != null) {
            color = palette.getMutedSwatch().getRgb();
        } else if (palette.getVibrantSwatch() != null) {
            // 如果有鲜艳色，使用它的变体
            color = adjustColorBrightness(palette.getVibrantSwatch().getRgb(), -0.3f);
        } else {
            // 如果没有提取到颜色，使用默认颜色
            color = Color.parseColor("#4CAF50");
        }

        return color;
    }

    /**
     * 调整颜色亮度
     * @param color 原始颜色
     * @param factor 调整因子 (-1.0到1.0，负值变暗，正值变亮)
     * @return 调整后的颜色
     */
    private static int adjustColorBrightness(int color, float factor) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.red(color);

        red = clamp((int) (red * (1 + factor)));
        green = clamp((int) (green * (1 + factor)));
        blue = clamp((int) (blue * (1 + factor)));

        return Color.argb(alpha, red, green, blue);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * 从 ImageView 中获取 Bitmap 并设置渐变背景
     * @param view 需要设置背景的 View
     * @param imageView 包含图片的 ImageView
     * @param angle 渐变角度
     */
    public static void setGradientFromImageView(View view, ImageView imageView, int angle,
                                                OnColorsExtractedListener listener) {
        if (imageView.getDrawable() == null) {
            return;
        }

        // 从 ImageView 获取 Bitmap
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(imageView.getDrawingCache());
        imageView.setDrawingCacheEnabled(false);

        if (bitmap != null) {
            setGradientFromBitmap(view, bitmap, angle, listener);
            bitmap.recycle();
        }
    }

    /**
     * 同步方式提取颜色（适用于小图片或后台线程）
     */
    public static void setGradientFromBitmapSync(View view, Bitmap bitmap, int angle) {
        if (view == null || bitmap == null) {
            return;
        }

        Palette palette = Palette.from(bitmap).generate();
        int color1 = extractPrimaryColor(palette);
        int color2 = extractSecondaryColor(palette);

        GradientUtils.setGradientBackground(view, color1, color2, angle);
    }

    /**
     * 颜色提取完成回调接口
     */
    public interface OnColorsExtractedListener {
        void onColorsExtracted(int primaryColor, int secondaryColor);
    }
}
