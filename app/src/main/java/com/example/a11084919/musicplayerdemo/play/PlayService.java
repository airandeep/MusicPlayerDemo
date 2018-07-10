package com.example.a11084919.musicplayerdemo.play;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.a11084919.musicplayerdemo.PlayerActivity;
import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.publicObjective.Functivity;
import com.example.a11084919.musicplayerdemo.musicAdapter.Music;

import static android.app.Notification.DEFAULT_LIGHTS;
import static java.lang.Thread.sleep;


//最终通过在活动
public class PlayService extends Service implements IPlay,IPlay.Callback{

    private static final String ACTION_PLAY_TOGGLE = "io.github.ryanhoo.music.ACTION.PLAY_TOGGLE";
    private static final String ACTION_PLAY_LAST = "io.github.ryanhoo.music.ACTION.PLAY_LAST";
    private static final String ACTION_PLAY_NEXT = "io.github.ryanhoo.music.ACTION.PLAY_NEXT";
    private static final String ACTION_STOP_SERVICE = "io.github.ryanhoo.music.ACTION.STOP_SERVICE";

    private static String TAG = "PlayService";
    private Player mPlayer;
   // private Notification notification;
    //通知相关
    private RemoteViews mContentViewSmall;
    private boolean cycleFlag;
    final int milliseconds = 100;

    private final Binder binder= new LocalBinder();
    //活动通过此内部类获得此服务对象，从而调用服务对象中的方法
    public class LocalBinder extends Binder{
        public PlayService getService(){
            return PlayService.this;
        }
    }

    public PlayService() {}

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    if(mPlayer.isPlaying()){

                        mPlayer.updateProgressBar();
                    }

                    break;
                default:
                    break;
            }
        }
    };

    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        mPlayer = Player.getInstance();
        mPlayer.registerCallback(this);//将本服务存到mPlay实例化对象中容器mCallbacks
        cycleFlag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (cycleFlag){
                    try{
                        sleep(milliseconds);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(0);
                }
            }
        }).start();
    }

    public int onStartCommand(Intent intent,int flags,int startId){
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_PLAY_TOGGLE.equals(action)) {
                if(mPlayer.isPlaying()){
                    pause();
                }else{
                    rePlay();
                }
            } else if (ACTION_PLAY_NEXT.equals(action)) {
                playNext();
            } else if (ACTION_PLAY_LAST.equals(action)) {
                playLast();
            }
        }
        return START_STICKY;
    }

    //活动就是通过与服务绑定然后利用此方法进行交互返回BInder内部类的实例然后交互
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return binder;
    }

    public boolean play(String musicPath,String musicName,int position,boolean notiFlag){
        return mPlayer.play(musicPath,musicName,position,notiFlag);
    }

    public int getPosition() {
        return mPlayer.getPosition();
    }

    public String getMusicPath(){
        return mPlayer.getMusicPath();
    }

    public String getMusicName(){
        return mPlayer.getMusicName();
    }

    public void registerCallback(Callback callback) {
        mPlayer.registerCallback(callback);
    }

    public void unregisterCallback(Callback callback){
        mPlayer.unregisterCallback(callback);
    }

    public boolean pause() {
        return mPlayer.pause();
    }

    public void rePlay(){
        mPlayer.rePlay();
    }

    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    public int getProgress(){
        return mPlayer.getProgress();
    }

    public int getDuration(){
        return  mPlayer.getDuration();
    }

    public void seekTo(int progress){
        mPlayer.seekTo(progress);
    }

    public boolean playLast(){
        return mPlayer.playLast();
    }

    public boolean playNext(){
        return mPlayer.playNext();
    }

    public void initNotification(){
        showNotification();
    }

    //////////////////////////////////////////////////////////////////

    public void onSwitchLast(){
        showNotification();
    }
    public void onSwitchNext(){
        showNotification();
    }
    public void onPlayStatusChanged() {
        showNotification();
    }

    //服务中此方法用于列表循环
    public void onUpdateProgressBar(){
        int position = mPlayer.getProgress();
        int time = mPlayer.getDuration();
        if(position > time-1000){//不要用等于，因为子线程是每隔0.1秒执行一次，有可能跳过相等的时候
            mPlayer.playNext();
        }
    }
    //创建通知，使本服务为前台服务，从而不至于被系统回收
    private void showNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PlayerActivity.class), 0);
        //if(notification == null){
            // Set the info for the views that show in the notification panel.
        Notification notification = new NotificationCompat.Builder(this,"music")
                    .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked//当点击通知跳到那首歌曲
                    .setCustomContentView(getSmallContentView())
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setOngoing(true)
                    .setSound(null)
                    .build();
        startForeground(1, notification);
    }



    //通知栏绑定UI
    private RemoteViews getSmallContentView() {
        if (mContentViewSmall == null) {
            mContentViewSmall = new RemoteViews(getPackageName(), R.layout.remote_view_music_player_small);
            setUpRemoteView(mContentViewSmall);
        }
        updateRemoteViews(mContentViewSmall);
        return mContentViewSmall;
    }



    private void setUpRemoteView(RemoteViews remoteView) {
        remoteView.setImageViewResource(R.id.image_view_play_last, R.drawable.ic_remote_view_play_last);
        remoteView.setImageViewResource(R.id.image_view_play_next, R.drawable.ic_remote_view_play_next);


        remoteView.setOnClickPendingIntent(R.id.button_play_last, getPendingIntent(ACTION_PLAY_LAST));
        remoteView.setOnClickPendingIntent(R.id.button_play_next, getPendingIntent(ACTION_PLAY_NEXT));
        remoteView.setOnClickPendingIntent(R.id.button_play_toggle, getPendingIntent(ACTION_PLAY_TOGGLE));
    }

    private void updateRemoteViews(RemoteViews remoteView) {
        remoteView.setImageViewResource(R.id.image_view_play_toggle, isPlaying()
                ? R.drawable.ic_remote_view_pause : R.drawable.ic_remote_view_play);
        String tempPath = Music.musicList.get(getPosition()).getPath();
        Bitmap bmpMp3 = Functivity.getCover(tempPath);
        if(bmpMp3 == null){
            remoteView.setImageViewResource(R.id.image_view_album,R.drawable.picture_default);
        }else{
            remoteView.setImageViewBitmap(R.id.image_view_album,bmpMp3 );
        }
        String tempName = Music.musicList.get(getPosition()).getName();
        remoteView.setTextViewText(R.id.text_view_name, tempName);
        remoteView.setTextViewText(R.id.text_view_artist, "AIRAN");
    }


    //getService表示会重新启动此服务
    private PendingIntent getPendingIntent(String action) {
        final ComponentName serviceName = new ComponentName(this, PlayService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(this, 0, intent, 0);
       //return PendingIntent.getService(this, 0, new Intent(action), 0);
    }

}
