package com.example.a11084919.musicplayerdemo.play;


import android.media.MediaPlayer;

import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.model.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player implements IPlay {

    public static final int LISTLOOP = 0;
    public static final int SINGLELOOP = 1;
    public static final int RANDLOOP = 2;
    public static final int QUICKRANDLOOP = 3;

    private static volatile Player sInstance;
    private MediaPlayer mediaPlayer;

    //当前歌曲位置//一定要保证此变量在删除歌曲后保持正确
    private int position;
    //播放模式
    private int playMode;

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }


    private Music currentMusic;

    public Music getCurrentMusic() {
        return currentMusic;
    }
    public void setCurrentMusic(Music currentMusic) {
        this.currentMusic = currentMusic;
    }

    //因为PlayService也继承了此接口mCallbacks，所以可以将服务实例化对象存在此集合mCallbacks中
    //然后在使用
    private List<Callback> mCallbacks = new ArrayList<>(3);

    private Player(){
        mediaPlayer = new MediaPlayer();//media刚创建时处于Idle状态
        //mediaPlayer.setLooping(true);
    }

    public int getPosition() {
        return position;
    }


    public static Player getInstance(){
        if(sInstance == null){
            synchronized (Player.class){
                if(sInstance == null){
                    sInstance = new Player();
                }
            }
        }
        return sInstance;
    }

    public void startThread(){}

    public boolean play(int position,String musicPath,boolean notiFlag){
        if(notiFlag || musicPath.equals(getCurrentMusic() == null ? "" : getCurrentMusic().getPath())){
            //当再次有播放列表点进来时由于有可能删除导致歌曲在链表list中的位置产生变化，所以更新一波
            this.position = position;
            return true;
        }else{
            setCurrentMusic(PublicObject.musicList.get(position));
            this.position = position;

            PublicObject.musicIndexs[0] = PublicObject.musicIndexs[1];
            PublicObject.musicIndexs[1] = position;

            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(getCurrentMusic().getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                notifyPlayStatusChanged();
            } catch (IOException e) {
                notifyPlayStatusChanged();
                return false;
            }
            return true;
        }

    }

    public void release(){
        if(mediaPlayer != null){
            mediaPlayer.release();
        }
    }

    public boolean playCurrentSong(){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getCurrentMusic().getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void registerCallback(Callback callback){
        mCallbacks.add(callback);
    }

    public void unregisterCallback(Callback callback){
        mCallbacks.remove(callback);
    }

    public boolean pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            notifyPlayStatusChanged();
            return true;
        }
        return false;
    }

    public void rePlay(){
        mediaPlayer.start();
        notifyPlayStatusChanged();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getProgress() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public void seekTo(int progress){
        mediaPlayer.seekTo(progress);
    }

    public boolean playLast(){
        int flag = getPlayMode();
        if(flag == Player.LISTLOOP){

            //解决删除问题
            if(position == 0 ||position == -1 || position >= PublicObject.musicList.size()){
                position = PublicObject.musicList.size();
            }
            position--;

            setCurrentMusic(PublicObject.musicList.get(position));
            PublicObject.musicIndexs[0] = PublicObject.musicIndexs[1];
            PublicObject.musicIndexs[1] = position;
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(getCurrentMusic().getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                notifyPlayLast();//通过服务中存储的活动的实例化对象调用活动中的重写的onSwitchLast方法
            } catch (IOException e) {
                return false;
            }
            return true;
        }else if(flag == Player.SINGLELOOP){
            playCurrentSong();
        }else if(flag == Player.RANDLOOP || flag == Player.QUICKRANDLOOP){

            int positionRandom = (int)(Math.random() * PublicObject.musicList.size());
            play(positionRandom,PublicObject.musicList.get(positionRandom).getPath(),false);

        }
        return false;
    }

    public boolean playNext(){
        int flag = getPlayMode();

        if(flag == Player.LISTLOOP){
            //解决删除问题
            if(position >= PublicObject.musicList.size()-1){
                position = -1;
            }
            position++;

            setCurrentMusic(PublicObject.musicList.get(position));
            PublicObject.musicIndexs[0] = PublicObject.musicIndexs[1];
            PublicObject.musicIndexs[1] = position;
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(getCurrentMusic().getPath());
                mediaPlayer.prepare();
                mediaPlayer.start();
                notifyPlayNext();//通过服务中存储的活动的实例化对象调用活动中的重写的onSwitchNext方法
            } catch (IOException e) {
                return false;
            }
            return true;
        }else if(flag == Player.SINGLELOOP){
            playCurrentSong();
        }else if(flag == Player.RANDLOOP || flag == Player.QUICKRANDLOOP){
            int positionRandom = (int)(Math.random() * PublicObject.musicList.size());
            play(positionRandom,PublicObject.musicList.get(positionRandom).getPath(),false);
        }
        return false;



    }

    public void initNotification(){}

    @Override
    public void releasePlayer() {

        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
        sInstance = null;
    }

    private void notifyPlayStatusChanged() {
        for (Callback callback : mCallbacks) {
            callback.onPlayStatusChanged();
        }
    }

    private void notifyPlayLast(){
        for(Callback callback : mCallbacks){
            callback.onSwitchLast();
        }
    }

    private void notifyPlayNext(){
        for(Callback callback : mCallbacks){
            callback.onSwitchNext();
        }
    }

    public void updateProgressBar(){
        for(Callback callback : mCallbacks){
            callback.onUpdateProgressBar();
        }
    }
}
