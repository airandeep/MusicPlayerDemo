package com.example.a11084919.musicplayerdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.play.IPlay;
import com.example.a11084919.musicplayerdemo.play.PlayService;
import com.example.a11084919.musicplayerdemo.play.Player;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Thread.sleep;

public class PlayerActivity extends BaseActivity implements IPlay.Callback,EventListener {

    private static final int NEXTSONG = 0;
    private static final int LASTSONG = 1;
    private static final int PAUSESONG = 2;
    private static final int PLAYSONG = 3;
    private static final int SHOWINFO = 4;
    //MediaPlayer实例化的对象必须手动mediaPlayer.stop(); mediaPlayer.release();释放，即使当前活动
    ////
    private static String TAG = "PlayerActivity";

    private TextView txtMusicName;
    private TextView txtMusic;
    private SeekBar SBMusicInfo;
    private TextView txtTimeNow;
    private TextView txtTimeMax;
    private TextView txtVoiceInfo;

    private String strShow;


    private Button btnPlay;
    private Button btnPause;
    private Button btnPre;
    private Button btnNext;
    private Button btnBack;
    private Button btnPlayWay;
    private Button btnStartVoice;
    private ImageView imgShow;
    private Bitmap bmpMp3;

    private boolean notiFlag;
    private boolean isUseVoice;
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

    //语音识别
    private EventManager wakeup;

    //语音回调通过Handler机制进行反应
    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case SHOWINFO:{
                    txtVoiceInfo.setText(strShow);
                    break;
                }
                case NEXTSONG:{
                    mPlayer.playNext();
                    Toast.makeText(PlayerActivity.this,"已通过语音切换到下一首",Toast.LENGTH_SHORT).show();
                    break;
                }
                case LASTSONG:{
                    mPlayer.playLast();
                    Toast.makeText(PlayerActivity.this,"已通过语音切换到上一首",Toast.LENGTH_SHORT).show();
                    break;
                }
                case PAUSESONG:{
                    if(mPlayer.isPlaying()){
                        mPlayer.pause();
                        btnPause.setVisibility(View.GONE);
                        btnPlay.setVisibility(View.VISIBLE);
                        imgShow.clearAnimation();
                        Toast.makeText(PlayerActivity.this,"已通过语音暂停本歌曲",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(PlayerActivity.this,"目前已在暂停状态",Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case PLAYSONG:{
                    if(mPlayer.isPlaying()){
                        Toast.makeText(PlayerActivity.this,"目前已在播放状态",Toast.LENGTH_SHORT).show();
                    }else{
                        mPlayer.rePlay();
                        btnPause.setVisibility(View.VISIBLE);
                        btnPlay.setVisibility(View.GONE);
                        imgShow.startAnimation(animation);
                        Toast.makeText(PlayerActivity.this,"已通过语音播放本歌曲",Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                default:{
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();

        isUseVoice = true;

        //初始化语音操作
        initPermission();
        wakeup = EventManagerFactory.create(this, "wp");
        wakeup.registerListener(this); //  EventListener 中 onEvent方法

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

        btnStartVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isUseVoice){
                    String strFlag = btnStartVoice.getText().toString();
                    if(strFlag.equals("开启语音")){
                        start();
                        btnStartVoice.setText("关闭语音");
                    }else{
                        stop();
                        btnStartVoice.setText("开启语音");
                    }
                }else{
                    Toast.makeText(PlayerActivity.this,"      请授予相关权限\n方可使用语音相关功能",Toast.LENGTH_SHORT).show();
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

    private void initView(){
        txtMusicName = findViewById(R.id.txtMusicName);
        txtMusic = findViewById(R.id.txtMusic);
        SBMusicInfo = findViewById(R.id.SBMusicInfo);
        txtTimeNow = findViewById(R.id.txtTimeNow);
        txtTimeMax = findViewById(R.id.txtTimeMax);
        txtVoiceInfo = findViewById(R.id.txt_voice_info);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.back_button);
        btnPlayWay = findViewById(R.id.btnPlayWay);
        btnStartVoice = findViewById(R.id.btn_start_voice);
        imgShow = findViewById(R.id.imgShow);
    }

    /**
     * android 6.0 以上需要动态申请权限//申请录音权限和录音权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    private void bindPlayService(){
        Intent bindIntent = new Intent(this,PlayService.class);
        startService(bindIntent);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }

    //开启语音操作
    private void start() {

        Map<String, Object> params = new TreeMap();

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        String json = null; // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        wakeup.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
        txtVoiceInfo.setText("已开启语音识别功能");
        txtVoiceInfo.setTextColor(Color.parseColor("#00FF00"));
        Toast.makeText(PlayerActivity.this,"已开启语音识别功能",Toast.LENGTH_SHORT).show();
    }
    private void stop() {
        wakeup.send(SpeechConstant.WAKEUP_STOP, null, null, 0, 0); //
        txtVoiceInfo.setText("已停止语音识别功能");
        txtVoiceInfo.setTextColor(Color.parseColor("#FF0000"));
        Toast.makeText(PlayerActivity.this,"已停止语音识别功能",Toast.LENGTH_SHORT).show();
    }


    //   EventListener  回调方法
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        try {
            JSONObject jsonObject = new JSONObject(params);
            String errorDesc = jsonObject.getString("errorDesc");
            String errorCode = jsonObject.getString("errorCode");
            String word = jsonObject.getString("word");

            mHandler.sendEmptyMessage(SHOWINFO);
            strShow = "AIRAN识别结果" + errorDesc + " " + errorCode + " " + word;
            //Log.d("AIRAN", errorDesc + " " + errorCode + " " + word);
            if(word.equals("下一首")){
                mHandler.sendEmptyMessage(NEXTSONG);
            }else if(word.equals("上一首")){
                mHandler.sendEmptyMessage(LASTSONG);
            }else if(word.equals("暂停")){
                mHandler.sendEmptyMessage(PAUSESONG);
            }else if(word.equals("播放")){
                mHandler.sendEmptyMessage(PLAYSONG);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    public void initMusicMedia(){
        //开启服务内部线程//但是内部方法只会执行一次
        mPlayer.startThread();

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
        super.onDestroy();
        //将本活动创建的子线程中的循环参数设置为false，跳出此子线程//一定要跳出，否则每次此线程会存在，下一次创建活动会再次起一个新线程导致
        //cycleFlag = false;
        //取消绑定，然后将活动实例在服务中容器移除
        unbindPlaybackService();
        //停止百度语音引擎
        wakeup.send(SpeechConstant.WAKEUP_STOP, "{}", null, 0, 0);
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



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        switch (requestCode){
            case 123:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }else{
                    Toast.makeText(this,"未能获取智能语音使用相关权限\n授权后方可使用智能语音相关功能",Toast.LENGTH_SHORT).show();
                    isUseVoice = false;
                }
                break;
            default:
                break;
        }
    }


}
