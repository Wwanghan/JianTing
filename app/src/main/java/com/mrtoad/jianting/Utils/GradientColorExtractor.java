package com.mrtoad.jianting.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import androidx.palette.graphics.Palette;

import java.io.File;

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
     * 异步方式：从图片文件路径提取两种主要颜色并设置渐变背景
     * 与 setGradientFromBitmap 功能一致，仅输入参数替换为文件路径字符串
     * @param view 需要设置背景的 View
     * @param filePath 图片文件的路径字符串（如 "/sdcard/test.jpg"）
     * @param angle 渐变角度
     * @param listener 回调接口（可选）
     */
    public static void setGradientFromFilePath(View view, String filePath, int angle,
                                               OnColorsExtractedListener listener) {
        // 1. 空值与有效性判断
        if (view == null || filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        File imageFile = new File(filePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            // 文件不存在或不是有效文件，直接返回
            return;
        }

        // 2. 从文件路径加载 Bitmap
        Bitmap bitmap = loadBitmapFromFilePath(filePath);
        if (bitmap == null) {
            return;
        }

        // 3. 复用原有异步颜色提取逻辑
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                // 提取主次颜色
                int color1 = extractPrimaryColor(palette);
                int color2 = extractSecondaryColor(palette);

                // 设置渐变背景
                GradientUtils.setGradientBackground(view, color1, color2, angle);

                // 回调颜色信息
                if (listener != null) {
                    listener.onColorsExtracted(color1, color2);
                }

                // 4. 释放 Bitmap 资源，避免内存泄漏
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
        });
    }

    /**
     * 同步方式：从图片文件路径提取两种主要颜色并设置渐变背景
     * 适用于小图片或后台线程，避免主线程阻塞
     * @param view 需要设置背景的 View
     * @param filePath 图片文件的路径字符串
     * @param angle 渐变角度
     */
    public static void setGradientFromFilePathSync(View view, String filePath, int angle) {
        // 1. 空值与有效性判断
        if (view == null || filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        File imageFile = new File(filePath);
        if (!imageFile.exists() || !imageFile.isFile()) {
            return;
        }

        // 2. 从文件路径加载 Bitmap
        Bitmap bitmap = loadBitmapFromFilePath(filePath);
        if (bitmap == null) {
            return;
        }

        try {
            // 3. 复用原有同步颜色提取逻辑
            Palette palette = Palette.from(bitmap).generate();
            int color1 = extractPrimaryColor(palette);
            int color2 = extractSecondaryColor(palette);

            // 设置渐变背景
            GradientUtils.setGradientBackground(view, color1, color2, angle);
        } finally {
            // 4. 无论是否异常，都释放 Bitmap 资源
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * 私有工具方法：从文件路径加载 Bitmap
     * 处理了加载异常，避免程序崩溃，同时简单优化避免 OOM
     * @param filePath 图片文件路径
     * @return 加载成功的 Bitmap，失败返回 null
     */
    private static Bitmap loadBitmapFromFilePath(String filePath) {
        try {
            // 配置 Bitmap 加载选项，避免大图片导致 OOM
            BitmapFactory.Options options = new BitmapFactory.Options();
            // 先只解码边界信息，不加载完整 Bitmap
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            // 简单压缩：如果图片宽高超过 1000px，进行采样压缩（可根据需求调整阈值）
            int maxSize = 1000;
            int width = options.outWidth;
            int height = options.outHeight;
            int inSampleSize = 1;
            if (width > maxSize || height > maxSize) {
                int halfWidth = width / 2;
                int halfHeight = height / 2;
                while ((halfWidth / inSampleSize) >= maxSize && (halfHeight / inSampleSize) >= maxSize) {
                    inSampleSize *= 2;
                }
            }

            // 正式加载 Bitmap，启用采样压缩
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            // 启用硬件加速兼容，减少内存占用
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            return BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            // 捕获解码异常、内存不足等异常，避免程序崩溃
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 颜色提取完成回调接口
     */
    public interface OnColorsExtractedListener {
        void onColorsExtracted(int primaryColor, int secondaryColor);
    }
}
