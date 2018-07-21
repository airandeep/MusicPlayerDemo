package com.example.a11084919.musicplayerdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.play.IPlay;
import com.example.a11084919.musicplayerdemo.play.PlayService;
import com.example.a11084919.musicplayerdemo.play.Player;


import static java.lang.Thread.sleep;

public class PlayerActivity extends BaseActivity implements IPlay.Callback{

    //MediaPlayer实例化的对象必须手动mediaPlayer.stop(); mediaPlayer.release();释放，即使当前活动
    ////
    private static String TAG = "PlayerActivity";

    private TextView txtMusicName;
    private TextView txtMusic;
    private SeekBar SBMusicInfo;
    private TextView txtTimeNow;
    private TextView txtTimeMax;


    private Button btnPlay;
    private Button btnPause;
    private Button btnPre;
    private Button btnNext;
    private Button btnBack;
    private Button btnPlayWay;
    private ImageView imgShow;
    Bitmap bmpMp3;

    private boolean notiFlag;

    //private String path;
    //private String name;
    private int position;


    private IPlay mPlayer;
    //旋转操作
    private Animation animation;

    private PlayService playService;
    private ServiceConnection connection = new ServiceConnection() {
        //活动创建时绑定此服务
        //iBinder实际上就是绑定服务中onBind方法返回值
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            playService = ((PlayService.LocalBinder)iBinder).getService();
            mPlayer = playService;
            mPlayer.registerCallback(PlayerActivity.this);
            initMusicMedia();
        }
        //活动销毁时取消绑定
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        txtMusicName = findViewById(R.id.txtMusicName);
        txtMusic = findViewById(R.id.txtMusic);
        SBMusicInfo = findViewById(R.id.SBMusicInfo);
        txtTimeNow = findViewById(R.id.txtTimeNow);
        txtTimeMax = findViewById(R.id.txtTimeMax);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.back_button);
        btnPlayWay = findViewById(R.id.btnPlayWay);
        imgShow = findViewById(R.id.imgShow);

        //使图片按照anim中img_animation.xml设置的参数进行旋转··
        animation = AnimationUtils.loadAnimation(this,R.anim.img_animation);
        LinearInterpolator lin = new LinearInterpolator();
        animation.setInterpolator(lin);


       //绑定本活动与服务
        bindPlayService();



        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mPlayer.rePlay();
                btnPause.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.GONE);
                imgShow.startAnimation(animation);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                mPlayer.pause();
                btnPause.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
                imgShow.clearAnimation();


            }
        });

        btnPre.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mPlayer.playLast();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mPlayer.playNext();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onKeyDown(KeyEvent.KEYCODE_BACK,null );
            }
        });

        btnPlayWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String flag = btnPlayWay.getText().toString();
                if(flag.equals("列表循环")){
                    mPlayer.setPlayMode(Player.SINGLELOOP);
                    btnPlayWay.setText("单曲循环");
                }else if(flag.equals("单曲循环")){
                    mPlayer.setPlayMode(Player.RANDLOOP);
                    btnPlayWay.setText("随机循环");
                }else if(flag.equals("随机循环")){
                    mPlayer.setPlayMode(Player.QUICKRANDLOOP);
                    btnPlayWay.setText("快速随机");
                }else if(flag.equals("快速随机")){
                    mPlayer.setPlayMode(Player.LISTLOOP);
                    btnPlayWay.setText("列表循环");
                }

            }
        });

        SBMusicInfo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            //一定要在此处修改MediaPlay播放进度
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int dest = seekBar.getProgress();
                int max = seekBar.getMax();
                int time = mPlayer.getDuration();
                //指定播放器播放位置
                mPlayer.seekTo(time*dest/max);

            }
        });


    }

    public void initMusicMedia(){


        int flag = mPlayer.getPlayMode();
        if(flag == Player.LISTLOOP){
            btnPlayWay.setText("列表循环");
        }else if(flag == Player.SINGLELOOP){
            btnPlayWay.setText("单曲循环");
        }else if(flag == Player.RANDLOOP){
            btnPlayWay.setText("随机循环");
        }else if(flag == Player.QUICKRANDLOOP){
            btnPlayWay.setText("快速随机");
        }else{
            btnPlayWay.setText("列表循环");
        }
        //初期处理
        Intent intent = getIntent();
        String strPosition = intent.getStringExtra("extra_position");

        //name = intent.getStringExtra("extra_name");
        if(strPosition == null){//当由通知点进来时只需要更新播放类中position即可，其他不需要更新
            notiFlag = true;
            String tempPath = mPlayer.getCurrentMusic().getPath();
            boolean tempFlag = false;
            int n = PublicObject.musicList.size();
            //因为有可能删除导致mPlayer中保存的position不是歌曲在静态成员变量musicList中歌曲实际位置
            for(int i = 0;i<n;i++){
                if(tempPath.equals(PublicObject.musicList.get(i).getPath())){
                    position = i;
                    tempFlag = true;
                    break;
                }
            }
            if(!tempFlag){
                position = -1;
            }

            //position = mPlayer.getPosition();
            if(!mPlayer.isPlaying()){
                btnPause.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
            }
        }else{
            notiFlag = false;
            position =Integer.parseInt(strPosition) ;

        }
        String tempMusicPath;
        if(notiFlag){
            tempMusicPath = "";
            mPlayer.play(position,tempMusicPath,true);
        }else{
            tempMusicPath = PublicObject.musicList.get(position).getPath();
            mPlayer.play(position,tempMusicPath,false);
        }


        if(mPlayer.isPlaying()){
            imgShow.startAnimation(animation);
        }else{
            imgShow.clearAnimation();
        }

        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(bmpMp3 == null){
            imgShow.setImageResource(R.drawable.picture_default);
        }else{
            imgShow.setImageBitmap(bmpMp3);
        }

        txtMusicName.setText(mPlayer.getCurrentMusic().getTitle());
        txtMusic.setText(mPlayer.getCurrentMusic().getArtist());



        int time = mPlayer.getDuration();
        //播放器暂停状态进入时刷新一下界面
        if(!mPlayer.isPlaying()){
            btnPause.setVisibility(View.GONE);
            btnPlay.setVisibility(View.VISIBLE);

            int position = mPlayer.getProgress();
            int max = SBMusicInfo.getMax();
            int timeTemp = position / 1000;
            String str = String.format("%02d:%02d", timeTemp / 60 % 60, timeTemp % 60);
            txtTimeNow.setText(str);
            SBMusicInfo.setProgress(max * position / time);
        }
        //显示通知栏
        mPlayer.initNotification();
        //设置当前音乐总时间
        time = time/1000;
        String str = String.format("%02d:%02d", time / 60 % 60, time % 60);
        txtTimeMax.setText(str);


    }

    private void bindPlayService(){
        Intent bindIntent = new Intent(this,PlayService.class);
        startService(bindIntent);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }

    private void unbindPlaybackService(){
        //并不会调用onServiceDisconnected方法
        unbindService(connection);
        mPlayer.unregisterCallback(PlayerActivity.this);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager  = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                finish();
                Intent intent = new Intent(PlayerActivity.this,MusicListActivity.class);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,AudioManager.FX_FOCUS_NAVIGATION_UP);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,AudioManager.FX_FOCUS_NAVIGATION_UP);
            default:
                break;
        }
        return true;
    }

    public void onDestroy(){
        //将本活动创建的子线程中的循环参数设置为false，跳出此子线程//一定要跳出，否则每次此线程会存在，下一次创建活动会再次起一个新线程导致
        //cycleFlag = false;
        //取消绑定，然后将活动实例在服务中容器移除
        unbindPlaybackService();

        super.onDestroy();
    }
//////////////////////////////////////////
    //这里只负责切换显示，切换歌曲已经在服务中完成了
    public void onSwitchLast(){
        imgShow.clearAnimation();
        //使图片按照anim中img_animation.xml设置的参数进行旋转··
        imgShow.startAnimation(animation);

        btnPause.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.GONE);

        txtMusicName.setText(mPlayer.getCurrentMusic().getTitle());
        txtMusic.setText(mPlayer.getCurrentMusic().getArtist());

        position = mPlayer.getPosition();
        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(bmpMp3 == null){
            imgShow.setImageResource(R.drawable.picture_default);
        }else{
            imgShow.setImageBitmap(bmpMp3);
        }

        //修改当前歌曲显示
        int time = mPlayer.getDuration()/1000;
        String str = String.format("%02d:%02d", time / 60 % 60, time % 60);
        txtTimeMax.setText(str);
    }
    public void onSwitchNext(){
        imgShow.clearAnimation();
        //使图片按照anim中img_animation.xml设置的参数进行旋转··
        imgShow.startAnimation(animation);

        btnPause.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.GONE);

        txtMusicName.setText(mPlayer.getCurrentMusic().getTitle());
        txtMusic.setText(mPlayer.getCurrentMusic().getArtist());

        position = mPlayer.getPosition();

        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(bmpMp3 == null){
            imgShow.setImageResource(R.drawable.picture_default);
        }else{
            imgShow.setImageBitmap(bmpMp3);
        }

        //修改当前歌曲显示
        int time = mPlayer.getDuration()/1000;
        String str = String.format("%02d:%02d", time / 60 % 60, time % 60);
        txtTimeMax.setText(str);
    }
    public void onPlayStatusChanged() {
        imgShow.clearAnimation();
        if(mPlayer.isPlaying()){
            //使图片按照anim中img_animation.xml设置的参数进行旋转··
            imgShow.startAnimation(animation);
        }

        btnPause.setVisibility(View.VISIBLE);
        btnPlay.setVisibility(View.GONE);

        txtMusicName.setText(mPlayer.getCurrentMusic().getTitle());
        txtMusic.setText(mPlayer.getCurrentMusic().getArtist());

        position = mPlayer.getPosition();

        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(bmpMp3 == null){
            imgShow.setImageResource(R.drawable.picture_default);
        }else{
            imgShow.setImageBitmap(bmpMp3);
        }

        //修改当前歌曲显示
        int time = mPlayer.getDuration()/1000;
        String str = String.format("%02d:%02d", time / 60 % 60, time % 60);
        txtTimeMax.setText(str);

        if(mPlayer.isPlaying()){
            btnPause.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(View.GONE);
        }else{
            btnPause.setVisibility(View.GONE);
            btnPlay.setVisibility(View.VISIBLE);
        }
    }

    public void onUpdateProgressBar(){

        int position = mPlayer.getProgress();
        int time = mPlayer.getDuration();
        int max = SBMusicInfo.getMax();
        int timeTemp = position / 1000;
        String str = String.format("%02d:%02d", timeTemp / 60 % 60, timeTemp % 60);
        txtTimeNow.setText(str);
        SBMusicInfo.setProgress(max * position / time);
    }
}
