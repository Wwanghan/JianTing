package com.mrtoad.jianting.Utils;

import android.graphics.drawable.GradientDrawable;
import android.view.View;

public class GradientUtils {

    /**
     * 为 View 设置动态渐变背景
     * @param view 需要设置背景的 View
     * @param startColor 起始颜色 (int 类型，如 0xFFRRGGBB)
     * @param endColor 结束颜色 (int 类型，如 0xFFRRGGBB)
     * @param angle 渐变角度 (0-360度，顺时针方向，0表示从左到右)
     */
    public static void setGradientBackground(View view, int startColor, int endColor, int angle) {
        if (view == null) {
            return;
        }

        // 创建渐变 Drawable
        GradientDrawable gradientDrawable = new GradientDrawable();

        // 设置渐变方向
        // 将角度转换为 GradientDrawable.Orientation
        GradientDrawable.Orientation orientation = angleToOrientation(angle);
        gradientDrawable.setOrientation(orientation);

        // 设置渐变颜色
        gradientDrawable.setColors(new int[]{startColor, endColor});

        // 设置为背景
        view.setBackground(gradientDrawable);
    }

    /**
     * 将角度转换为 GradientDrawable.Orientation
     * @param angle 渐变角度 (0-360度，顺时针方向)
     * @return GradientDrawable.Orientation 方向枚举
     */
    private static GradientDrawable.Orientation angleToOrientation(int angle) {
        // 将角度归一化到 0-360 范围内
        int normalizedAngle = ((angle % 360) + 360) % 360;

        // Android 的 GradientDrawable 使用逆时针方向的枚举
        // 所以需要将顺时针角度转换为对应的方向枚举
        if (normalizedAngle >= 0 && normalizedAngle < 45) {
            // 0-44度，从左到右
            return GradientDrawable.Orientation.LEFT_RIGHT;
        } else if (normalizedAngle >= 45 && normalizedAngle < 90) {
            // 45-89度，从左上到右下
            return GradientDrawable.Orientation.TL_BR;
        } else if (normalizedAngle >= 90 && normalizedAngle < 135) {
            // 90-134度，从上到下
            return GradientDrawable.Orientation.TOP_BOTTOM;
        } else if (normalizedAngle >= 135 && normalizedAngle < 180) {
            // 135-179度，从右上到左下
            return GradientDrawable.Orientation.TR_BL;
        } else if (normalizedAngle >= 180 && normalizedAngle < 225) {
            // 180-224度，从右到左
            return GradientDrawable.Orientation.RIGHT_LEFT;
        } else if (normalizedAngle >= 225 && normalizedAngle < 270) {
            // 225-269度，从右下到左上
            return GradientDrawable.Orientation.BR_TL;
        } else if (normalizedAngle >= 270 && normalizedAngle < 315) {
            // 270-314度，从下到上
            return GradientDrawable.Orientation.BOTTOM_TOP;
        } else {
            // 315-359度，从左下到右上
            return GradientDrawable.Orientation.BL_TR;
        }
    }

    /**
     * 带圆角设置的重载方法
     * @param view 需要设置背景的 View
     * @param startColor 起始颜色
     * @param endColor 结束颜色
     * @param angle 渐变角度
     * @param cornerRadius 圆角半径 (单位：像素)
     */
    public static void setGradientBackground(View view, int startColor, int endColor, int angle, float cornerRadius) {
        setGradientBackground(view, startColor, endColor, angle);

        // 获取背景并设置圆角
        GradientDrawable drawable = (GradientDrawable) view.getBackground();
        if (drawable != null) {
            drawable.setCornerRadius(cornerRadius);
        }
    }

    /**
     * 带多种圆角设置的重载方法
     * @param view 需要设置背景的 View
     * @param startColor 起始颜色
     * @param endColor 结束颜色
     * @param angle 渐变角度
     * @param radii 圆角半径数组 (8个值，每个角2个值：[左上x, 左上y, 右上x, 右上y, 右下x, 右下y, 左下x, 左下y])
     */
    public static void setGradientBackground(View view, int startColor, int endColor, int angle, float[] radii) {
        setGradientBackground(view, startColor, endColor, angle);

        // 获取背景并设置圆角
        GradientDrawable drawable = (GradientDrawable) view.getBackground();
        if (drawable != null) {
            drawable.setCornerRadii(radii);
        }
    }

    /**
     * 带描边设置的重载方法
     * @param view 需要设置背景的 View
     * @param startColor 起始颜色
     * @param endColor 结束颜色
     * @param angle 渐变角度
     * @param strokeWidth 描边宽度 (单位：像素)
     * @param strokeColor 描边颜色
     */
    public static void setGradientBackground(View view, int startColor, int endColor, int angle,
                                             int strokeWidth, int strokeColor) {
        setGradientBackground(view, startColor, endColor, angle);

        // 获取背景并设置描边
        GradientDrawable drawable = (GradientDrawable) view.getBackground();
        if (drawable != null) {
            drawable.setStroke(strokeWidth, strokeColor);
        }
    }
}