package com.example.a11084919.musicplayerdemo.play;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.a11084919.musicplayerdemo.PlayerActivity;
import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.musicAdapter.Music;

import static java.lang.Thread.sleep;


//最终通过在活动
public class PlayService extends Service implements IPlay,IPlay.Callback{

    private static final String ACTION_PLAY_TOGGLE = "airan.music.ACTION.PLAY_TOGGLE";
    private static final String ACTION_PLAY_LAST = "airan.music.ACTION.PLAY_LAST";
    private static final String ACTION_PLAY_NEXT = "airan.music.ACTION.PLAY_NEXT";
    private static final String ACTION_STOP_SERVICE = "airan.music.ACTION.STOP_SERVICE";

    private static String TAG = "PlayService";
    private Player mPlayer;

    //注:千万不要在Service类中定义这2歌类的引用，否则会导致通知和通知栏显示的实例化对象由于强引用无法释放资源造成内存泄漏
    //private RemoteViews mContentViewSmall
    //private Notification notification

    private boolean cycleFlag;
    final int milliseconds = 100;
    private NotificationManager manager;

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

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleCommandIntent(intent);
        }
    };

    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
        manager = (NotificationManager)getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        mPlayer = Player.getInstance();
        mPlayer.registerCallback(this);//将本服务存到mPlay实例化对象中容器mCallbacks

        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_TOGGLE);
        filter.addAction(ACTION_PLAY_LAST);
        filter.addAction(ACTION_PLAY_NEXT);
        registerReceiver(mIntentReceiver,filter);


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

    private void handleCommandIntent(Intent intent){
        final String command = intent.getAction();
        switch (command){
            case ACTION_PLAY_TOGGLE:{
                if(mPlayer.isPlaying()){
                    pause();
                }else{
                    rePlay();
                }
                break;
            }
            case ACTION_PLAY_LAST:{
                playLast();
                break;
            }
            case ACTION_PLAY_NEXT:{
                playNext();
                break;
            }
            default:{
                break;
            }
        }
    }

//    public int onStartCommand(Intent intent,int flags,int startId){
//        if (intent != null) {
//            String action = intent.getAction();
//            if (ACTION_PLAY_TOGGLE.equals(action)) {
//                if(mPlayer.isPlaying()){
//                    pause();
//                }else{
//                    rePlay();
//                }
//            } else if (ACTION_PLAY_NEXT.equals(action)) {
//                playNext();
//            } else if (ACTION_PLAY_LAST.equals(action)) {
//                playLast();
//            }
//        }
//        return START_STICKY;
//    }

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


        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, PlayerActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(this,"music")
                .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked//当点击通知跳到那首歌曲
                .setContent(getSmallContentView())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        manager.notify(1000,notification);
    }



    //通知栏绑定UI
    private RemoteViews getSmallContentView() {
        RemoteViews mContentViewSmall = new RemoteViews(getPackageName(), R.layout.remote_view_music_player_small);
        setUpRemoteView(mContentViewSmall);
        updateRemoteViews(mContentViewSmall);
        return mContentViewSmall;
    }



    private void setUpRemoteView(RemoteViews remoteView) {
        remoteView.setImageViewResource(R.id.image_view_play_last, R.drawable.ic_remote_view_play_last);
        remoteView.setImageViewResource(R.id.image_view_play_next, R.drawable.ic_remote_view_play_next);


        remoteView.setOnClickPendingIntent(R.id.button_play_last, getPendingIntentBroadcast(ACTION_PLAY_LAST));
        remoteView.setOnClickPendingIntent(R.id.button_play_next, getPendingIntentBroadcast(ACTION_PLAY_NEXT));
        remoteView.setOnClickPendingIntent(R.id.button_play_toggle, getPendingIntentBroadcast(ACTION_PLAY_TOGGLE));

        remoteView.setImageViewResource(R.id.image_view_play_toggle, R.drawable.ic_remote_view_play);
        remoteView.setTextViewText(R.id.text_view_name, "AIRAN");
        remoteView.setTextViewText(R.id.text_view_artist, "AIRANNNNNNARIA");
    }

    private void updateRemoteViews(RemoteViews remoteView) {
        remoteView.setImageViewResource(R.id.image_view_play_toggle, isPlaying()
                ? R.drawable.ic_remote_view_pause : R.drawable.ic_remote_view_play);
        String tempPath = PublicObject.musicList.get(getPosition()).getPath();
        Bitmap bmpMp3 = Functivity.getCover(tempPath);
        if(bmpMp3 == null){
            remoteView.setImageViewResource(R.id.image_view_album,R.drawable.picture_default);
        }else{
            remoteView.setImageViewBitmap(R.id.image_view_album,bmpMp3 );
        }
        remoteView.setTextViewText(R.id.text_view_name, PublicObject.musicList.get(getPosition()).getTitle());
        remoteView.setTextViewText(R.id.text_view_artist, PublicObject.musicList.get(getPosition()).getArtist());
    }


    private PendingIntent getPendingIntentBroadcast(String action){
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        return pendingIntent;
    }

    //getService表示会重新启动此服务，因为服务已经创建了，所以
    private PendingIntent getPendingIntent(String action) {
        final ComponentName serviceName = new ComponentName(this, PlayService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(this, 0, intent, 0);
    }

}
