package com.mrtoad.jianting.Entity;

import android.graphics.Bitmap;

public class ILikedMusicEntity {
    private Bitmap musicCover;
    private String musicName;
    private String musicAuthor;
    private String musicFilePath;


    public ILikedMusicEntity() {
    }

    public ILikedMusicEntity(Bitmap musicCover, String musicName, String musicAuthor, String musicFilePath) {
        this.musicCover = musicCover;
        this.musicName = musicName;
        this.musicAuthor = musicAuthor;
        this.musicFilePath = musicFilePath;
    }

    /**
     * 获取
     * @return musicCover
     */
    public Bitmap getMusicCover() {
        return musicCover;
    }

    /**
     * 设置
     * @param musicCover
     */
    public void setMusicCover(Bitmap musicCover) {
        this.musicCover = musicCover;
    }

    /**
     * 获取
     * @return musicName
     */
    public String getMusicName() {
        return musicName;
    }

    /**
     * 设置
     * @param musicName
     */
    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    /**
     * 获取
     * @return musicAuthor
     */
    public String getMusicAuthor() {
        return musicAuthor;
    }

    /**
     * 设置
     * @param musicAuthor
     */
    public void setMusicAuthor(String musicAuthor) {
        this.musicAuthor = musicAuthor;
    }

    /**
     * 获取
     * @return musicFilePath
     */
    public String getMusicFilePath() {
        return musicFilePath;
    }

    /**
     * 设置
     * @param musicFilePath
     */
    public void setMusicFilePath(String musicFilePath) {
        this.musicFilePath = musicFilePath;
    }

    public String toString() {
        return "ILikedMusicEntity{musicCover = " + musicCover + ", musicName = " + musicName + ", musicAuthor = " + musicAuthor + ", musicFilePath = " + musicFilePath + "}";
    }
}
