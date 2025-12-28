package com.mrtoad.jianting.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class KMeansColorExtractor {

    // 颜色聚类结果
    public static class ColorCluster {
        public int color;
        public int population;
        public List<int[]> pixels;

        public ColorCluster(int color, int population) {
            this.color = color;
            this.population = population;
            this.pixels = new ArrayList<>();
        }
    }

    // 回调接口
    public interface OnColorsExtractedListener {
        void onColorsExtracted(int primaryColor, int secondaryColor);
    }

    // 仅返回颜色的回调接口（不设置背景）
    public interface OnColorsOnlyExtractedListener {
        void onColorsExtracted(int primaryColor, int secondaryColor);
    }

    // 新增：颜色结果对象，用于同步返回
    public static class ColorResult {
        public int primaryColor;
        public int secondaryColor;

        public ColorResult(int primary, int secondary) {
            this.primaryColor = primary;
            this.secondaryColor = secondary;
        }
    }

    /**
     * 从 Bitmap 提取两种主要颜色（异步）
     */
    public static void extractColorsFromBitmap(View view, Bitmap bitmap, int angle,
                                               OnColorsExtractedListener listener) {
        if (view == null || bitmap == null) return;

        // 在后台线程执行 K-Means 聚类
        new Thread(() -> {
            try {
                // 压缩图片以加快处理速度
                Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);

                // 提取主要颜色（获取前5个主要颜色）
                List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);

                // 选择最佳颜色对
                int[] colors = selectBestColorPair(clusters);

                // 在主线程更新 UI
                view.post(() -> {
                    GradientUtils.setGradientBackground(view, colors[0], colors[1], angle);
                    if (listener != null) {
                        listener.onColorsExtracted(colors[0], colors[1]);
                    }
                });

                // 清理内存
                if (processedBitmap != bitmap && !processedBitmap.isRecycled()) {
                    processedBitmap.recycle();
                }

            } catch (Exception e) {
                e.printStackTrace();
                // 失败时使用默认颜色
                view.post(() -> {
                    GradientUtils.setGradientBackground(view,
                            Color.parseColor("#2196F3"),
                            Color.parseColor("#4CAF50"),
                            angle);
                });
            }
        }).start();
    }

    /**
     * 从 Bitmap 提取两种主要颜色（同步，适合后台线程）
     */
    public static void extractColorsFromBitmapSync(View view, Bitmap bitmap, int angle) {
        if (view == null || bitmap == null) return;

        try {
            Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);
            List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);
            int[] colors = selectBestColorPair(clusters);

            GradientUtils.setGradientBackground(view, colors[0], colors[1], angle);

            if (processedBitmap != bitmap && !processedBitmap.isRecycled()) {
                processedBitmap.recycle();
            }

        } catch (Exception e) {
            e.printStackTrace();
            GradientUtils.setGradientBackground(view,
                    Color.parseColor("#2196F3"),
                    Color.parseColor("#4CAF50"),
                    angle);
        }
    }

    /**
     * 新增：从Bitmap异步提取颜色（不设置背景，仅返回颜色）
     *
     * @param bitmap 图片
     * @param listener 颜色提取完成回调
     */
    public static void extractColorsOnlyFromBitmap(Bitmap bitmap,
                                                   OnColorsOnlyExtractedListener listener) {
        if (bitmap == null || bitmap.isRecycled()) {
            // Bitmap无效，返回默认颜色
            if (listener != null) {
                listener.onColorsExtracted(
                        Color.parseColor("#2196F3"),
                        Color.parseColor("#4CAF50")
                );
            }
            return;
        }

        new Thread(() -> {
            try {
                // 压缩图片以加快处理速度
                Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);

                // 提取主要颜色（获取前5个主要颜色）
                List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);

                // 选择最佳颜色对
                int[] colors = selectBestColorPair(clusters);

                // 在主线程回调结果
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        listener.onColorsExtracted(colors[0], colors[1]);
                    });
                }

                // 清理内存（只回收处理的副本，不回收原始bitmap）
                if (processedBitmap != bitmap && !processedBitmap.isRecycled()) {
                    processedBitmap.recycle();
                }

            } catch (Exception e) {
                e.printStackTrace();
                // 发生异常，返回默认颜色
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        listener.onColorsExtracted(
                                Color.parseColor("#2196F3"),
                                Color.parseColor("#4CAF50")
                        );
                    });
                }
            }
        }).start();
    }

    /**
     * 从文件路径提取颜色（异步）
     */
    public static void extractColorsFromFilePath(View view, String filePath, int angle,
                                                 OnColorsExtractedListener listener) {
        if (view == null || filePath == null || filePath.trim().isEmpty()) return;

        new Thread(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) return;

                Bitmap bitmap = loadBitmapFromFilePath(filePath);
                if (bitmap == null) return;

                Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);
                List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);
                int[] colors = selectBestColorPair(clusters);

                view.post(() -> {
                    GradientUtils.setGradientBackground(view, colors[0], colors[1], angle);
                    if (listener != null) {
                        listener.onColorsExtracted(colors[0], colors[1]);
                    }
                });

                // 清理内存
                if (!bitmap.isRecycled()) bitmap.recycle();
                if (!processedBitmap.isRecycled()) processedBitmap.recycle();

            } catch (Exception e) {
                e.printStackTrace();
                view.post(() -> {
                    GradientUtils.setGradientBackground(view,
                            Color.parseColor("#2196F3"),
                            Color.parseColor("#4CAF50"),
                            angle);
                });
            }
        }).start();
    }

    /**
     * 从文件路径提取颜色（同步）
     */
    public static void extractColorsFromFilePathSync(View view, String filePath, int angle) {
        if (view == null || filePath == null || filePath.trim().isEmpty()) return;

        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) return;

            Bitmap bitmap = loadBitmapFromFilePath(filePath);
            if (bitmap == null) return;

            Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);
            List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);
            int[] colors = selectBestColorPair(clusters);

            GradientUtils.setGradientBackground(view, colors[0], colors[1], angle);

            // 清理内存
            if (!bitmap.isRecycled()) bitmap.recycle();
            if (!processedBitmap.isRecycled()) processedBitmap.recycle();

        } catch (Exception e) {
            e.printStackTrace();
            GradientUtils.setGradientBackground(view,
                    Color.parseColor("#2196F3"),
                    Color.parseColor("#4CAF50"),
                    angle);
        }
    }

    /**
     * 从文件路径异步提取颜色（不设置背景，仅返回颜色）（异步）
     * 适合在需要保存颜色值的场景使用
     *
     * @param filePath 图片文件路径
     * @param listener 颜色提取完成回调
     */
    public static void extractColorsOnlyFromFilePath(String filePath,
                                                     OnColorsOnlyExtractedListener listener) {
        if (filePath == null || filePath.trim().isEmpty()) {
            // 路径为空，返回默认颜色
            if (listener != null) {
                listener.onColorsExtracted(
                        Color.parseColor("#2196F3"),
                        Color.parseColor("#4CAF50")
                );
            }
            return;
        }

        new Thread(() -> {
            try {
                File file = new File(filePath);
                if (!file.exists() || !file.isFile()) {
                    // 文件不存在，返回默认颜色
                    if (listener != null) {
                        listener.onColorsExtracted(
                                Color.parseColor("#2196F3"),
                                Color.parseColor("#4CAF50")
                        );
                    }
                    return;
                }

                // 加载并处理图片
                Bitmap bitmap = loadBitmapFromFilePath(filePath);
                if (bitmap == null) {
                    // 图片加载失败，返回默认颜色
                    if (listener != null) {
                        listener.onColorsExtracted(
                                Color.parseColor("#2196F3"),
                                Color.parseColor("#4CAF50")
                        );
                    }
                    return;
                }

                // 压缩图片以加快处理速度
                Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);

                // 提取主要颜色（获取前5个主要颜色）
                List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);

                // 选择最佳颜色对
                int[] colors = selectBestColorPair(clusters);

                // 在主线程回调结果
                if (listener != null) {
                    // 使用Handler在主线程回调
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        listener.onColorsExtracted(colors[0], colors[1]);
                    });
                }

                // 清理内存
                if (!bitmap.isRecycled()) bitmap.recycle();
                if (!processedBitmap.isRecycled()) processedBitmap.recycle();

            } catch (Exception e) {
                e.printStackTrace();
                // 发生异常，返回默认颜色
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        listener.onColorsExtracted(
                                Color.parseColor("#2196F3"),
                                Color.parseColor("#4CAF50")
                        );
                    });
                }
            }
        }).start();
    }

    /**
     * 从 Drawable 资源提取颜色（同步）
     */
    public static void extractColorsFromResourceSync(View view, android.content.Context context,
                                                     int drawableId, int angle) {
        if (view == null || context == null) return;

        try {
            // 从资源加载 Bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
            if (bitmap == null) return;

            Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);
            List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);
            int[] colors = selectBestColorPair(clusters);

            GradientUtils.setGradientBackground(view, colors[0], colors[1], angle);

            // 清理内存
            if (!bitmap.isRecycled()) bitmap.recycle();
            if (!processedBitmap.isRecycled()) processedBitmap.recycle();

        } catch (Exception e) {
            e.printStackTrace();
            GradientUtils.setGradientBackground(view,
                    Color.parseColor("#2196F3"),
                    Color.parseColor("#4CAF50"),
                    angle);
        }
    }

    /**
     * 新增：从资源ID异步提取颜色（不设置背景，仅返回颜色）
     *
     * @param context 上下文
     * @param drawableId 资源ID
     * @param listener 颜色提取完成回调
     */
    public static void extractColorsOnlyFromResource(android.content.Context context,
                                                     int drawableId,
                                                     OnColorsOnlyExtractedListener listener) {
        if (context == null) {
            if (listener != null) {
                listener.onColorsExtracted(
                        Color.parseColor("#2196F3"),
                        Color.parseColor("#4CAF50")
                );
            }
            return;
        }

        new Thread(() -> {
            try {
                // 从资源加载 Bitmap
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);
                if (bitmap == null) {
                    // 资源加载失败，返回默认颜色
                    if (listener != null) {
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> {
                            listener.onColorsExtracted(
                                    Color.parseColor("#2196F3"),
                                    Color.parseColor("#4CAF50")
                            );
                        });
                    }
                    return;
                }

                // 压缩图片以加快处理速度
                Bitmap processedBitmap = resizeBitmap(bitmap, 200, 200);

                // 提取主要颜色（获取前5个主要颜色）
                List<ColorCluster> clusters = extractDominantColors(processedBitmap, 5);

                // 选择最佳颜色对
                int[] colors = selectBestColorPair(clusters);

                // 在主线程回调结果
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        listener.onColorsExtracted(colors[0], colors[1]);
                    });
                }

                // 清理内存
                if (!bitmap.isRecycled()) bitmap.recycle();
                if (!processedBitmap.isRecycled()) processedBitmap.recycle();

            } catch (Exception e) {
                e.printStackTrace();
                // 发生异常，返回默认颜色
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        listener.onColorsExtracted(
                                Color.parseColor("#2196F3"),
                                Color.parseColor("#4CAF50")
                        );
                    });
                }
            }
        }).start();
    }

    /**
     * 核心 K-Means 算法：提取主要颜色
     */
    private static List<ColorCluster> extractDominantColors(Bitmap bitmap, int k) {
        // 1. 获取图片的所有像素（跳过透明像素）
        List<int[]> pixels = getPixels(bitmap);

        if (pixels.size() < k) {
            return new ArrayList<>();
        }

        // 2. 使用 K-Means++ 初始化聚类中心
        List<int[]> centroids = initializeCentroids(pixels, k);

        // 3. 迭代聚类（最多10次迭代）
        List<ColorCluster> clusters = null;
        for (int iteration = 0; iteration < 10; iteration++) {
            // 分配像素到最近的聚类
            clusters = assignPixelsToClusters(pixels, centroids);

            // 计算新的聚类中心
            List<int[]> newCentroids = calculateNewCentroids(clusters);

            // 检查是否收敛（中心点变化很小）
            if (hasConverged(centroids, newCentroids, 2.0)) {
                break;
            }

            centroids = newCentroids;
        }

        // 4. 转换聚类结果为颜色
        List<ColorCluster> result = new ArrayList<>();
        for (ColorCluster cluster : clusters) {
            if (cluster.pixels.isEmpty()) continue;

            int[] centroid = calculateCentroid(cluster.pixels);
            int color = Color.rgb(centroid[0], centroid[1], centroid[2]);
            cluster.color = color;
            result.add(cluster);
        }

        // 按簇大小排序
        Collections.sort(result, (c1, c2) ->
                Integer.compare(c2.population, c1.population));

        return result;
    }

    /**
     * 获取图片的像素数据
     */
    private static List<int[]> getPixels(Bitmap bitmap) {
        List<int[]> pixels = new ArrayList<>();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // 采样获取像素（每隔2个像素取一次，加快速度）
        for (int x = 0; x < width; x += 2) {
            for (int y = 0; y < height; y += 2) {
                int pixel = bitmap.getPixel(x, y);

                // 忽略透明像素（alpha < 128）
                if (Color.alpha(pixel) > 128) {
                    pixels.add(new int[]{
                            Color.red(pixel),
                            Color.green(pixel),
                            Color.blue(pixel)
                    });
                }
            }
        }

        return pixels;
    }

    /**
     * K-Means++ 初始化聚类中心
     */
    private static List<int[]> initializeCentroids(List<int[]> pixels, int k) {
        List<int[]> centroids = new ArrayList<>();

        // 第一个中心点随机选择
        centroids.add(pixels.get((int) (Math.random() * pixels.size())));

        // 后续中心点根据距离概率选择
        for (int i = 1; i < k; i++) {
            double[] distances = new double[pixels.size()];
            double totalDistance = 0;

            for (int j = 0; j < pixels.size(); j++) {
                double minDist = Double.MAX_VALUE;

                for (int[] centroid : centroids) {
                    double dist = colorDistance(pixels.get(j), centroid);
                    minDist = Math.min(minDist, dist);
                }

                distances[j] = minDist * minDist; // 平方距离
                totalDistance += distances[j];
            }

            // 根据距离概率选择下一个中心点
            double randomValue = Math.random() * totalDistance;
            double cumulativeDistance = 0;

            for (int j = 0; j < pixels.size(); j++) {
                cumulativeDistance += distances[j];
                if (cumulativeDistance >= randomValue) {
                    centroids.add(pixels.get(j));
                    break;
                }
            }
        }

        return centroids;
    }

    /**
     * 分配像素到最近的聚类
     */
    private static List<ColorCluster> assignPixelsToClusters(List<int[]> pixels, List<int[]> centroids) {
        List<ColorCluster> clusters = new ArrayList<>();
        for (int i = 0; i < centroids.size(); i++) {
            clusters.add(new ColorCluster(0, 0));
        }

        for (int[] pixel : pixels) {
            int nearestClusterIndex = 0;
            double minDistance = Double.MAX_VALUE;

            // 找到最近的聚类中心
            for (int i = 0; i < centroids.size(); i++) {
                double distance = colorDistance(pixel, centroids.get(i));
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestClusterIndex = i;
                }
            }

            // 将像素添加到最近的聚类
            ColorCluster nearestCluster = clusters.get(nearestClusterIndex);
            nearestCluster.pixels.add(pixel);
            nearestCluster.population++;
        }

        return clusters;
    }

    /**
     * 计算新的聚类中心
     */
    private static List<int[]> calculateNewCentroids(List<ColorCluster> clusters) {
        List<int[]> newCentroids = new ArrayList<>();

        for (ColorCluster cluster : clusters) {
            if (cluster.pixels.isEmpty()) {
                // 如果聚类为空，随机生成一个中心点
                newCentroids.add(new int[]{
                        (int) (Math.random() * 256),
                        (int) (Math.random() * 256),
                        (int) (Math.random() * 256)
                });
            } else {
                int[] centroid = calculateCentroid(cluster.pixels);
                newCentroids.add(centroid);
            }
        }

        return newCentroids;
    }

    /**
     * 计算聚类的中心点
     */
    private static int[] calculateCentroid(List<int[]> pixels) {
        long sumR = 0, sumG = 0, sumB = 0;

        for (int[] pixel : pixels) {
            sumR += pixel[0];
            sumG += pixel[1];
            sumB += pixel[2];
        }

        int count = pixels.size();
        return new int[]{
                (int) (sumR / count),
                (int) (sumG / count),
                (int) (sumB / count)
        };
    }

    /**
     * 检查 K-Means 是否收敛
     */
    private static boolean hasConverged(List<int[]> oldCentroids, List<int[]> newCentroids, double threshold) {
        double totalDistance = 0;

        for (int i = 0; i < oldCentroids.size(); i++) {
            totalDistance += colorDistance(oldCentroids.get(i), newCentroids.get(i));
        }

        return totalDistance < threshold;
    }

    /**
     * 计算颜色距离（使用感知距离公式）
     */
    private static double colorDistance(int[] color1, int[] color2) {
        // 使用加权欧氏距离，模拟人眼感知
        double rMean = (color1[0] + color2[0]) / 2.0;
        int deltaR = color1[0] - color2[0];
        int deltaG = color1[1] - color2[1];
        int deltaB = color1[2] - color2[2];

        double weightR = 2 + rMean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rMean) / 256.0;

        return Math.sqrt(
                weightR * deltaR * deltaR +
                        weightG * deltaG * deltaG +
                        weightB * deltaB * deltaB
        );
    }

    /**
     * 从聚类结果中选择最佳颜色对
     */
    private static int[] selectBestColorPair(List<ColorCluster> clusters) {
        if (clusters.isEmpty()) {
            return new int[]{Color.parseColor("#2196F3"), Color.parseColor("#4CAF50")};
        }

        // 至少需要一个颜色
        int primaryColor = clusters.get(0).color;
        int secondaryColor;

        if (clusters.size() >= 2) {
            // 如果有多个颜色，选择对比度最大的
            secondaryColor = findBestContrastColor(primaryColor, clusters);
        } else {
            // 只有一个颜色，生成互补色
            secondaryColor = generateComplementaryColor(primaryColor);
        }

        return new int[]{primaryColor, secondaryColor};
    }

    /**
     * 寻找最佳对比色
     */
    private static int findBestContrastColor(int baseColor, List<ColorCluster> clusters) {
        float maxContrast = 0;
        int bestColor = generateComplementaryColor(baseColor); // 默认互补色

        for (int i = 1; i < clusters.size() && i < 5; i++) { // 最多检查前5个颜色
            int candidate = clusters.get(i).color;
            float contrast = calculateContrast(baseColor, candidate);

            if (contrast > maxContrast) {
                maxContrast = contrast;
                bestColor = candidate;
            }
        }

        // 如果对比度不够，使用生成的互补色
        if (maxContrast < 4.5) { // WCAG建议的最小对比度
            return generateComplementaryColor(baseColor);
        }

        return bestColor;
    }

    /**
     * 计算颜色对比度（WCAG标准）
     */
    private static float calculateContrast(int color1, int color2) {
        double luminance1 = calculateLuminance(color1) + 0.05;
        double luminance2 = calculateLuminance(color2) + 0.05;

        return (float) (Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2));
    }

    /**
     * 计算颜色亮度
     */
    private static double calculateLuminance(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;

        // sRGB转换到线性空间
        r = r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /**
     * 生成互补色
     */
    private static int generateComplementaryColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // 色相旋转180度
        hsv[0] = (hsv[0] + 180) % 360;

        // 调整饱和度和亮度以获得更好的对比
        hsv[1] = Math.min(1.0f, hsv[1] * 1.2f); // 增加饱和度
        hsv[2] = hsv[2] > 0.5f ? 0.3f : 0.7f; // 调整亮度以形成对比

        return Color.HSVToColor(hsv);
    }

    /**
     * 图片缩放
     */
    private static Bitmap resizeBitmap(Bitmap src, int maxWidth, int maxHeight) {
        if (src == null) return null;

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        if (srcWidth <= maxWidth && srcHeight <= maxHeight) {
            return src;
        }

        float widthRatio = (float) maxWidth / srcWidth;
        float heightRatio = (float) maxHeight / srcHeight;
        float ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (srcWidth * ratio);
        int newHeight = (int) (srcHeight * ratio);

        return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
    }

    /**
     * 从文件路径加载Bitmap（优化内存）
     */
    private static Bitmap loadBitmapFromFilePath(String filePath) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            // 计算采样率
            int width = options.outWidth;
            int height = options.outHeight;
            int maxSize = 800;
            int inSampleSize = 1;

            if (width > maxSize || height > maxSize) {
                int halfWidth = width / 2;
                int halfHeight = height / 2;
                while ((halfWidth / inSampleSize) >= maxSize && (halfHeight / inSampleSize) >= maxSize) {
                    inSampleSize *= 2;
                }
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            return BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
