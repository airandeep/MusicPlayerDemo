package com.example.a11084919.musicplayerdemo.play;

import android.content.Context;

public interface IPlay {
    boolean play(String musicPath,String musicName,int position,boolean notiFlag);

    //获取当前歌曲在歌曲列表中位置
    int getPosition();
    //
    //获取当前歌曲播放路径
    String getMusicPath();
    //获取当前歌曲播放名字
    String getMusicName();

    void registerCallback(Callback callback);

    void unregisterCallback(Callback callback);

    boolean pause();

    void rePlay();

    boolean isPlaying();

    int getProgress();

    int getDuration();

    void seekTo(int progress);

    boolean playLast();

    boolean playNext();

    void initNotification();

    //此接口中的接口只有PlayService继承
    interface Callback{
        //这2个接口分别在服务和活动中重写，服务中是为了切通知显示，活动中是为了切活动显示
        void onSwitchLast();

        void onSwitchNext();

        void onPlayStatusChanged();
        //更新活动中进度条//
        void onUpdateProgressBar();
    }

}
