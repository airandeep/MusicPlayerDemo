package com.example.a11084919.musicplayerdemo.play;


import com.example.a11084919.musicplayerdemo.model.Music;

public interface IPlay {
    void startThread();

    boolean play(int position,String musicPath,boolean notiFlag);

    void release();

    boolean playCurrentSong();
    //获取当前歌曲在歌曲列表中位置
    int getPosition();

    int getPlayMode();

    void setPlayMode(int playMode);

    Music getCurrentMusic();


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

    void releasePlayer();

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
