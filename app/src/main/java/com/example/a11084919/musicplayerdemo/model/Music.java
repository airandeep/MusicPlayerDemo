package com.example.a11084919.musicplayerdemo.model;


import org.litepal.crud.DataSupport;

public class Music extends DataSupport{



    //当前歌曲是否被选中
    private boolean isSelect;

    private String path;
    //演唱者和歌曲名//临时使用
    private String name;
    //演唱者
    private String artist;
    //歌曲名
    private String title;
    //专辑名
    private String album;
    //歌曲图片
    private byte[] pic;
    //音频时长
    private int duration;
    public Music(){}
    public Music(FavoriteMusic favoriteMusic){
        this.isSelect = favoriteMusic.isSelect();
        this.path = favoriteMusic.getPath();
        this.name = favoriteMusic.getName();
        this.artist = favoriteMusic.getArtist();
        this.title = favoriteMusic.getTitle();
        this.album = favoriteMusic.getAlbum();
        this.pic = favoriteMusic.getPic();
        this.duration = favoriteMusic.getDuration();
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public byte[] getPic() {
        return pic;
    }

    public void setPic(byte[] pic) {
        this.pic = pic;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

}
