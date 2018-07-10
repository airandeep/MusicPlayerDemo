package com.example.a11084919.musicplayerdemo.play;


import android.media.MediaPlayer;
import android.util.Log;

import com.example.a11084919.musicplayerdemo.musicAdapter.Music;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Player implements IPlay {

    private static volatile Player sInstance;
    private MediaPlayer mediaPlayer;

    private boolean isPaused;
    //当前歌曲位置
    private int position;
    //当前歌曲路径
    private String musicPath;
    private String musicName;


    //因为PlayService也继承了此接口mCallbacks，所以可以将服务实例化对象存在此集合mCallbacks中
    //然后在使用
    private List<Callback> mCallbacks = new ArrayList<>(2);

    private Player(){
        mediaPlayer = new MediaPlayer();//media刚创建时处于Idle状态
    }

    public int getPosition() {
        return position;
    }

    public String getMusicPath(){
        return musicPath;
    }

    public String getMusicName(){
        return musicName;
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

    public boolean play(String musicPath,String musicName,int position,boolean notiFlag){

        //
        if(notiFlag || musicPath.equals(this.musicPath)){
            return true;
        }else{
            this.musicPath = musicPath;
            this.musicName = musicName;
            this.position = position;
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicPath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                notifyPlayStatusChanged();
            } catch (IOException e) {
                // Log.e(TAG, "play: ", e);
                notifyPlayStatusChanged();
                return false;
            }
            return true;
        }

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
            isPaused = true;
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
        mediaPlayer.reset();
        if(position == 0){
            position = Music.musicList.size();
        }
        position--;
        musicPath = Music.musicList.get(position).getPath();
        musicName = Music.musicList.get(position).getName();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            notifyPlayLast();//通过服务中存储的活动的实例化对象调用活动中的重写的onSwitchLast方法
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean playNext(){
        mediaPlayer.reset();
        if(position == Music.musicList.size()-1){
            position = -1;
        }
        position++;

        musicPath = Music.musicList.get(position).getPath();
        musicName = Music.musicList.get(position).getName();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            notifyPlayNext();//通过服务中存储的活动的实例化对象调用活动中的重写的onSwitchNext方法
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public void initNotification(){}


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
