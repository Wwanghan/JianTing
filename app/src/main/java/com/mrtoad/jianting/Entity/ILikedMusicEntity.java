package com.mrtoad.jianting.Entity;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ILikedMusicEntity implements Parcelable {
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

    // Parcelable 实现
    protected ILikedMusicEntity(Parcel in) {
        // Bitmap 需要特殊处理
        musicCover = in.readParcelable(Bitmap.class.getClassLoader());
        musicName = in.readString();
        musicAuthor = in.readString();
        musicFilePath = in.readString();
    }

    public static final Creator<ILikedMusicEntity> CREATOR = new Creator<ILikedMusicEntity>() {
        @Override
        public ILikedMusicEntity createFromParcel(Parcel in) {
            return new ILikedMusicEntity(in);
        }

        @Override
        public ILikedMusicEntity[] newArray(int size) {
            return new ILikedMusicEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(musicCover, flags);
        dest.writeString(musicName);
        dest.writeString(musicAuthor);
        dest.writeString(musicFilePath);
    }
}
