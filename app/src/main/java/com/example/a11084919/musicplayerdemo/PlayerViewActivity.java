package com.example.a11084919.musicplayerdemo;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.a11084919.musicplayerdemo.musicAdapter.Music;

import java.io.File;

import static java.lang.Thread.sleep;

public class PlayerViewActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private TextView txtMusic;
    private SeekBar SBMusicInfo;
    private TextView txtTimeNow;
    private TextView txtTimeMax;
    private ProgressBar proMusicInfo;
    private Button btnPlay;
    private Button btnPause;
    private Button btnPre;
    private Button btnNext;

    private boolean flag;

    private String path;
    private int position;

    final int milliseconds = 100;

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    //必须写在try语句块里面，否则会报错
                    try{
                        if(mediaPlayer != null){
                            int position = mediaPlayer.getCurrentPosition();
                            int time = mediaPlayer.getDuration();
                            int max = SBMusicInfo.getMax();
                            //proMusicInfo.setProgress(position*max/time);
                            int timeTemp = position/1000;
                            String str = String.format("%02d:%02d", timeTemp / 60 % 60, timeTemp % 60);
                            txtTimeNow.setText(str);
                            SBMusicInfo.setProgress(max*position/time);

                        }else{
                            break;
                        }
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }

                    default:
                        break;
            }
        }
    };

//    private String tran(){
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_view);

        Intent intent = getIntent();
        String name = intent.getStringExtra("extra_name");
        path = intent.getStringExtra("extra_path");
        position =Integer.parseInt(intent.getStringExtra("extra_position")) ;

        txtMusic = findViewById(R.id.txtMusic);
        SBMusicInfo = findViewById(R.id.SBMusicInfo);
        txtTimeNow = findViewById(R.id.txtTimeNow);
        txtTimeMax = findViewById(R.id.txtTimeMax);
        //proMusicInfo = findViewById(R.id.proMusicInfo);
        btnPlay = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnPre = findViewById(R.id.btnPre);
        btnNext = findViewById(R.id.btnNext);


        txtMusic.setText(name);
        initMediaPlayer(path);

        flag = true;
        //每隔0.1秒更新一波进度条
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag){
                    try{
                        sleep(milliseconds);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(0);
                }
            }
        }).start();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.start();

                btnPause.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.GONE);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                mediaPlayer.pause();

                btnPause.setVisibility(View.GONE);
                btnPlay.setVisibility(View.VISIBLE);
            }
        });

        btnPre.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btnPause.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.GONE);
                mediaPlayer.reset();
                if(position == 0){
                    position = Music.musicList.size();
                }
                position--;
                txtMusic.setText( Music.musicList.get(position).getName());
                String tempPath = Music.musicList.get(position).getPath();
                try{
                    File file = new File(tempPath);
                    mediaPlayer.setDataSource(file.getPath());
                    mediaPlayer.prepare();
                }catch (Exception e){
                    e.printStackTrace();
                }
                mediaPlayer.start();

                int time = mediaPlayer.getDuration()/1000;
                String str = String.format("%02d:%02d", time / 60 % 60, time % 60);
                txtTimeMax.setText(str);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btnPause.setVisibility(View.VISIBLE);
                btnPlay.setVisibility(View.GONE);
                mediaPlayer.reset();//reset将播放器变为Idle状态后才能使用setDataSource换另一首
                if(position == Music.musicList.size()-1){
                    position = -1;
                }
                position++;
                txtMusic.setText( Music.musicList.get(position).getName());
                String tempPath = Music.musicList.get(position).getPath();
                try{
                    File file = new File(tempPath);
                    mediaPlayer.setDataSource(file.getPath());
                    mediaPlayer.prepare();
                }catch (Exception e){
                    e.printStackTrace();
                }
                mediaPlayer.start();

                int time = mediaPlayer.getDuration()/1000;
                String str = String.format("%02d:%02d", time / 60 % 60, time % 60);
                txtTimeMax.setText(str);
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
                int time = mediaPlayer.getDuration();

                //指定播放器播放位置
                mediaPlayer.seekTo(time*dest/max);

            }
        });


    }


    private void initMediaPlayer(String path){
        try{
            File file = new File(path);
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            int time = mediaPlayer.getDuration()/1000;
            String str = String.format("%02d:%02d", time / 60 % 60, time % 60);
            txtTimeMax.setText(str);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onStop(){
        super.onStop();
    }

    public void onDestroy(){

        flag = false;
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
