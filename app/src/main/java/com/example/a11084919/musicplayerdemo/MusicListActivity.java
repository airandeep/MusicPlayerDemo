package com.example.a11084919.musicplayerdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11084919.musicplayerdemo.functivity.Functivity;
import com.example.a11084919.musicplayerdemo.musicAdapter.Music;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapter;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterPlus;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MusicListActivity extends AppCompatActivity {


    private static String TAG = "MusicListActivity";
    private Button btnManage;
    private MusicAdapterPlus adapterPlus;
    private LinearLayout LinOutButton;
    private Button btnChooseAll;
    private Button btnDelete;

    public static final int STATE_PLAY_ENABLE = 0;
    public static final int STATE_MANAGE = 1;
    public static int stateNow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);


        //将音乐容器中内容清空重新加载
        Music.musicList.clear();

        if(ContextCompat.checkSelfPermission(MusicListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MusicListActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            File path = Environment.getExternalStorageDirectory();// 获得SD卡路径
            File[] files = path.listFiles();// 读取
            showMusicList(files);
        }

        if(Music.musicList.size() == 0){
            Toast.makeText(this,"本地无MP3或flac格式的音乐文件",Toast.LENGTH_SHORT).show();
        }

        btnManage = findViewById(R.id.btnManage);
        LinOutButton = findViewById(R.id.LinOutButton);
        btnChooseAll = findViewById(R.id.btnChooseAll);
        btnDelete = findViewById(R.id.btnDelete);

        btnManage.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btnManage.setVisibility(View.GONE);
                LinOutButton.setVisibility(View.VISIBLE);
                adapterPlus.setEditMode(1);
                stateNow = STATE_MANAGE;
                //Music.selectNum = 0;
            }
        });

        btnChooseAll.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                int n = Music.musicList.size();
                String flag = btnChooseAll.getText().toString();
                if(flag.equals("全选")){
                    for(int i = 0;i < n;i++){
                        Music.musicList.get(i).setSelect(true);
                    }
                    btnChooseAll.setText("全不选");
                }else{
                    for(int i = 0;i < n;i++){
                        Music.musicList.get(i).setSelect(false);
                    }
                    btnChooseAll.setText("全选");
                }
                adapterPlus.notifyDataSetChanged();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                boolean flag = false;
                int n = Music.musicList.size();
                for(int i = 0;i < n;i++){
                    if(Music.musicList.get(i).isSelect()){
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    final AlertDialog dialog = new AlertDialog.Builder(MusicListActivity.this).create();
                    dialog.show();
                    dialog.getWindow().setContentView(R.layout.pop_user);
                    TextView msg =  dialog.findViewById(R.id.tv_msg);
                    Button cancel = dialog.findViewById(R.id.btn_cancle);
                    Button sure = dialog.findViewById(R.id.btn_sure);
                    if (msg == null || cancel == null || sure == null) return;

                    int num = 0;
                    for(int i = 0;i<n;i++){
                        if(Music.musicList.get(i).isSelect()){
                            num++;
                        }
                    }
                    msg.setText("确定要删除选中的"+num+"首歌曲么?");

                    cancel.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            dialog.dismiss();
                        }
                    });

                    sure.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View view){
                            for(int i = adapterPlus.getMyMusicList().size()-1;i>=0;i--){
                                Music music = adapterPlus.getMyMusicList().get(i);
                                if(music.isSelect()){
                                    adapterPlus.getMyMusicList().remove(music);
                                    Functivity.deleteFile(music.getPath());
                                }
                            }
                            adapterPlus.notifyDataSetChanged();

                            //adapterPlus.setEditMode(1);
                            dialog.dismiss();
                            Toast.makeText(view.getContext(),"删除成功",Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(view.getContext(),"请选中歌曲",Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(stateNow == STATE_MANAGE){
                    btnManage.setVisibility(View.VISIBLE);
                    LinOutButton.setVisibility(View.GONE);
                    adapterPlus.setEditMode(0);
                    stateNow = STATE_PLAY_ENABLE;
                }else{
                    finish();
                }

                break;
                default:
                    break;
        }
        return true;
    }



    //在获取权限后判断的2个分支中均执行此方法刷新播放列表
    private void showMusicList(File[] files){
        getFileMusicData(files);

        RecyclerView recyclerView = findViewById(R.id.musicRecycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapterPlus = new MusicAdapterPlus(Music.musicList);
        recyclerView.setAdapter(adapterPlus);
    }
    //只获取特定文件夹和根目录下文件的文件名，专辑图片，文件信息
    private void getFileMusicData(File[] files){
        if(files != null){
            for(File file : files){
                if(file.isDirectory() && file.getName().equals("musicFile")){
                    getFileMusicData(file.listFiles());
                }else{
                    String fileName = file.getName();
                    if(fileName.endsWith(".mp3") || fileName.endsWith(".flac") || fileName.endsWith(".txt")){
                        //Log.d(TAG, "音乐名字为:"+fileName);
                        String s = fileName.substring(0,fileName.lastIndexOf(".")).toString();
                        Music temp = new Music(s,file.toString());

                        Music.musicList.add(temp);
                    }
                }

            }
        }
    }


    //递归方式检测sd文件夹中所有后缀为.mp3的文件
    private void getFileName(File[] files) {
        if (files != null) {// 先判断目录是否为空，否则会报空指针
            for (File file : files) {
                if (file.isDirectory()) {
                    Log.i(TAG, "若是文件目录。继续读1" + file.getName().toString()
                            + file.getPath().toString());

                    getFileName(file.listFiles());
                    Log.i(TAG, "若是文件目录。继续读2" + file.getName().toString()
                            + file.getPath().toString());
                } else {
                    String fileName = file.getName();
                    if (fileName.endsWith(".mp3") || fileName.endsWith(".flac")) {
                        HashMap map = new HashMap();
                        String s = fileName.substring(0,
                                fileName.lastIndexOf(".")).toString();
                        Log.i(TAG, "文件名mp3：：   " + s);
                        map.put("Name", fileName.substring(0,
                                fileName.lastIndexOf(".")));
                        //name.add(map);
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
                    File path = Environment.getExternalStorageDirectory();// 获得SD卡路径
                    File[] files = path.listFiles();// 读取
                    showMusicList(files);
                }else{
                    Toast.makeText(this,"本地音乐加载失败",Toast.LENGTH_SHORT).show();
                    //finish();
                }
                break;
            default:
                break;
        }
    }

}
