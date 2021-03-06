package com.example.a11084919.musicplayerdemo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11084919.musicplayerdemo.general.Util;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.model.Music;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanningActivity extends BaseActivity {

    private static String TAG = "ScanningActivity";

    //微信
    private static final String APP_ID = "wxa51fa5cc5dc34e87";


    private String strShow;

    private Button btnBack;
    private ImageView imgScan;
    private ProgressBar progressScanning;
    private TextView txtScanning;
    private TextView txtScanResult;
    private Button btnLocalMusic;

    private MediaMetadataRetriever mmr;
    private List<Music> tempMusicList;

    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                //扫描刚开始
                case 0:
                    txtScanResult.setText(strShow);
                    break;
                    //扫描结束
                case 1:
                    imgScan.setVisibility(View.VISIBLE);
                    progressScanning.setVisibility(View.GONE);

                    imgScan.setImageResource(R.drawable.local_scan_ok);
                    txtScanning.setText("已扫描"+ PublicObject.allMusicList.size() + "首歌曲");
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
        setContentView(R.layout.activity_scanning);
        initView();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "music";
            String channelName = "音乐通知";
            int importance = NotificationManager.IMPORTANCE_LOW;
            createNotificationChannel(channelId, channelName, importance);

        }
        Intent intentJus = getIntent();
        String flag = intentJus.getStringExtra("extra_flag");

        //判断是不是由播放列表活动点击过来的
        //flag等于null说明正常点进来需要判断数据库中是否存有相关数据决定是否扫描
        if(flag == null){
            if(PublicObject.allMusicList != null){//如果静态变量allMusicList不为null说明albumList与musicMap 也为null
                Intent intent = new Intent(ScanningActivity.this,MusicListActivity.class);
                startActivity(intent);
                finish();
                return;
            }else{
                //将自己ID注册到微信终端
                regToWx();

                tempMusicList = DataSupport.findAll(Music.class);
                //存储专辑静态变量
                if(tempMusicList.size()>0){
                    PublicObject.allMusicList = tempMusicList;
                    //将Player中的集合list引用赋值，防止语音直接播放报错
                    PublicObject.musicList = tempMusicList;
                    Util.initAlbumListAndMusicMap(tempMusicList);

                    Intent intent = new Intent(ScanningActivity.this,MusicListActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        }

        tempMusicList = new ArrayList<>();
        btnLocalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btnTxt = btnLocalMusic.getText().toString();
                //再次点击此控件时，静态成员变量musicList中已经存在元素直接跳到播放列表界面
                if(btnTxt.equals("进入播放列表")){
                    Intent intent = new Intent(ScanningActivity.this,MusicListActivity.class);
                    //必须关闭掉播放列表活动，然后重新创建，因为数据已经刷新了//由于集合List已经更新，不能在用之前的适配器了
                    ActivityCollector.finishAll();
                    startActivity(intent);
                }else{
                    DataSupport.deleteAll(Music.class);
                    //初始化MediaMetadataRetriever类获取歌曲相关信息
                    mmr =  new MediaMetadataRetriever();
                    if(ContextCompat.checkSelfPermission(ScanningActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(ScanningActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    }else{
                        btnLocalMusic.setVisibility(View.GONE);
                        txtScanning.setVisibility(View.VISIBLE);
                        imgScan.setVisibility(View.GONE);
                        progressScanning.setVisibility(View.VISIBLE);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
//                                File path = Environment.getExternalStorageDirectory();// 获得SD卡路径
//                                final File[] files = path.listFiles();// 读取
//                                getFileMusic(files);
                                getMusicFileByMediaStore();
                                PublicObject.allMusicList = tempMusicList;//静态引用指向另一个集合时，之前指向的集合如果没有引用指向时会自动被系统回收
                                PublicObject.musicList = tempMusicList;
                                Util.initAlbumListAndMusicMap(tempMusicList);
                                mHandler.sendEmptyMessage(1);
                            }
                        }).start();
                    }

                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


    }

    private void initView(){
        btnBack = findViewById(R.id.btn_back);
        imgScan = findViewById(R.id.img_scan);
        progressScanning = findViewById(R.id.progress_scanning);
        txtScanning = findViewById(R.id.txt_scanning);
        txtScanResult = findViewById(R.id.txt_scan_result);
        btnLocalMusic = findViewById(R.id.btn_local_music);
    }

    private void getMusicFileByMediaStore()
    {
        ContentResolver musicResolver = getContentResolver();
        Cursor c = null;
        try {
            c = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                 MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            while (c.moveToNext()) {
                String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));// 路径
                //有时候会扫描出来不存在的歌曲
                if(!Util.isExists(path)){
                    continue;
                }
                Music music = new Music();
                mmr.setDataSource(path);

                String fileName = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)); // 文件名
                String name = fileName.substring(0,fileName.lastIndexOf(".")).toString();//歌曲名
                String album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)); // 专辑
                String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)); // 作者
                String title = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));//歌曲名字
                //String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int duration = c.getInt(c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));//歌曲时长
                byte[] pic = mmr.getEmbeddedPicture();
                //mmr.getFrameAtTime(3);

                music.setPath(path);
                music.setName(name);
                if(artist.equals("<unknown>")){
                    artist = fileName.substring(0,fileName.lastIndexOf("-")-1);
                    title = fileName.substring(fileName.lastIndexOf("-") + 2,fileName.lastIndexOf(".")).toString();
                }
                music.setAlbum(album);
                music.setArtist(artist);
                music.setTitle(title);
                music.setPic(pic);
                music.setDuration(duration);
                music.save();
                tempMusicList.add(music);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }

    }

    private void regToWx(){
        PublicObject.api = WXAPIFactory.createWXAPI(this,APP_ID,true);
        PublicObject.api.registerApp(APP_ID);
    }

    //只获取特定文件夹和根目录下文件的文件名，专辑图片，文件信息
    private void getFileMusic(File[] files){
        try
        {
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
                        if(fileName.endsWith(".mp3") || fileName.endsWith(".flac")||fileName.endsWith(".MP3")){

                                mmr.setDataSource(file.toString());
                                Music music = new Music();
                                String name = fileName.substring(0,fileName.lastIndexOf(".")).toString();
                                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                                String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                                String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                byte[] pic = mmr.getEmbeddedPicture();

                                music.setPath(file.toString());
                                music.setName(name);
                                if(title == null){
                                    title = fileName.substring(fileName.lastIndexOf("-") + 2,fileName.lastIndexOf(".")).toString();
                                    artist = fileName.substring(0,fileName.lastIndexOf("-")-1);
                                }
                                music.setTitle(title);
                                music.setAlbum(album);
                                music.setArtist(artist);
                                if(pic != null){
                                    music.setPic(pic);
                                }
                                music.save();
                                tempMusicList.add(music);
                            }
                        }
                    }

                }
            }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    //应用权限申请回调
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantsResults){
        switch (requestCode){
            case 1:
                if(grantsResults.length > 0 && grantsResults[0] == PackageManager.PERMISSION_GRANTED){
                    btnLocalMusic.setVisibility(View.GONE);
                    txtScanning.setVisibility(View.VISIBLE);

                    imgScan.setVisibility(View.GONE);
                    progressScanning.setVisibility(View.VISIBLE);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            getMusicFileByMediaStore();
                            PublicObject.allMusicList = tempMusicList;
                            PublicObject.musicList = tempMusicList;
                            Util.initAlbumListAndMusicMap(tempMusicList);
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
