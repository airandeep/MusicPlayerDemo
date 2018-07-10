package com.example.a11084919.musicplayerdemo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11084919.musicplayerdemo.musicAdapter.Music;
import com.example.a11084919.musicplayerdemo.publicObjective.Functivity;

import java.io.File;
import java.util.List;

public class MainActivity extends BaseActivity {

    //private boolean cycleFlag;
    private String strShow;

    private Button btnLocalMusic;
    private TextView scan_result_txt;
    private TextView txtScanning;
    private ImageView imgScan;

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 0:
                    scan_result_txt.setText(strShow);
                    break;
                case 1:
                    imgScan.setImageResource(R.drawable.local_scan_ok);
                    txtScanning.setText("已扫描"+ Music.musicList.size() + "首歌曲");
                    btnLocalMusic.setVisibility(View.VISIBLE);
                    btnLocalMusic.setText("进入播放列表");
                    break;
                    default:
                        break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cycleFlag = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "music";
            String channelName = "音乐通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);

        }

        if(Music.musicList.size() > 0){
            Intent intent = new Intent(MainActivity.this,MusicListActivity.class);
            startActivity(intent);
        }

        scan_result_txt = findViewById(R.id.scan_result_txt);
        btnLocalMusic = findViewById(R.id.btnLocalMusic);
        txtScanning = findViewById(R.id.txtScanning);
        imgScan = findViewById(R.id.scan_icon);

        btnLocalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //再次点击此控件时，静态成员变量musicList中已经存在元素直接跳到播放列表界面
                if(Music.musicList.size() > 0){
                    Intent intent = new Intent(MainActivity.this,MusicListActivity.class);
                    startActivity(intent);
                }else{
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    }else{
                        btnLocalMusic.setVisibility(View.GONE);
                        txtScanning.setVisibility(View.VISIBLE);
                        File path = Environment.getExternalStorageDirectory();// 获得SD卡路径
                        final File[] files = path.listFiles();// 读取

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                getFileMusic(files);
                                mHandler.sendEmptyMessage(1);
                            }
                        }).start();
                    }
                }
            }
        });
    }

    //只获取特定文件夹和根目录下文件的文件名，专辑图片，文件信息
    private void getFileMusic(File[] files){
        if(files != null){
            for(File file : files){
                if(file.isDirectory()){
                    strShow = file.toString();
                    mHandler.sendEmptyMessage(0);
                    getFileMusic(file.listFiles());
                }else{
                    strShow = file.toString();
                    mHandler.sendEmptyMessage(0);
                    String fileName = file.getName();
                    if(fileName.endsWith(".mp3") || fileName.endsWith(".flac")){
                        //Log.d(TAG, "音乐名字为:"+fileName);
                        String s = fileName.substring(0,fileName.lastIndexOf(".")).toString();
                        Music temp = new Music(s,file.toString());

                        Music.musicList.add(temp);
                    }
                }

            }
        }
    }

    //应用权限申请回调
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantsResults){
        switch (requestCode){
            case 1:
                if(grantsResults.length > 0 && grantsResults[0] == PackageManager.PERMISSION_GRANTED){
                    btnLocalMusic.setVisibility(View.GONE);
                    txtScanning.setVisibility(View.VISIBLE);
                    File path = Environment.getExternalStorageDirectory();// 获得SD卡路径
                    final File[] files = path.listFiles();// 读取

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getFileMusic(files);
                            mHandler.sendEmptyMessage(1);
                        }
                    }).start();
                }else{
                    Toast.makeText(this,"未能获取SD卡访问权限,本地音乐扫描失败",Toast.LENGTH_SHORT).show();
                    //finish();
                }
                break;
            default:
                break;
        }
    }

    //Build.VERSION_CODES.O是静态常量值为26//最后一个是O
    //创建一个通知渠道至少需要渠道ID、渠道名称以及重要等级这三个参数
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
