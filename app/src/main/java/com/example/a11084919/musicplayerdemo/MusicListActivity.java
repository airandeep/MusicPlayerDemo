package com.example.a11084919.musicplayerdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.a11084919.musicplayerdemo.asrBaidu.MiniWakeUp;
import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.model.Music;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterList;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterRecycle;
import com.example.a11084919.musicplayerdemo.play.PlayService;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MusicListActivity extends BaseActivity {


    private static String TAG = "MusicListActivity";
    private Button btnManage;
    private MusicAdapterRecycle musicAdapterRecycle;
    private MusicAdapterList musicAdapterList;
    private LinearLayout LinOutButton;
    private Button btnChooseAll;
    private Button btnDelete;
    private RecyclerView recyclerView;

    private ImageView imgNext;

    public static final int STATE_PLAY_ENABLE = 0;
    public static final int STATE_MANAGE = 1;
    public static int stateNow;

    private DrawerLayout drawerLayout;


    private PlayService playService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        //将nav_call设置为默认选中
        navigationView.setCheckedItem(R.id.nav_play_list);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch(item.getItemId()){
                    case R.id.nav_play_list:{
                        Toast.makeText(MusicListActivity.this,"播放列表",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:{

                        break;
                    }
                }
                ;
                return true;
            }
        });



        recyclerView = findViewById(R.id.musicRecycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        musicAdapterRecycle = new MusicAdapterRecycle(connection,MusicListActivity.this, PublicObject.musicList);
        recyclerView.setAdapter(musicAdapterRecycle);


//        musicAdapterList = new MusicAdapterList(MusicListActivity.this,R.layout.music_item,PublicObject.musicList);
//        ListView listView = findViewById(R.id.musicListView);
//        listView.setAdapter(musicAdapterList);

        btnManage = findViewById(R.id.btnManage);
        LinOutButton = findViewById(R.id.LinOutButton);
        btnChooseAll = findViewById(R.id.btnChooseAll);
        btnDelete = findViewById(R.id.btnDelete);
        imgNext = findViewById(R.id.nav_btn_next);

        if(PublicObject.musicList != null){
            int n = PublicObject.musicList.size(),count = 0;
            for(int i = 0;i<n;i++){
                if(PublicObject.musicList.get(i).isSelect()){
                    count++;
                }
            }
            if(count == n){
                btnChooseAll.setText("全不选");
            }

        }


        btnManage.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                btnManage.setVisibility(View.GONE);
                LinOutButton.setVisibility(View.VISIBLE);
                musicAdapterRecycle.setEditMode(1);
                stateNow = STATE_MANAGE;
            }
        });

        btnChooseAll.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                int n = PublicObject.musicList.size();
                String flag = btnChooseAll.getText().toString();
                if(flag.equals("全选")){
                    for(int i = 0;i < n;i++){
                        PublicObject.musicList.get(i).setSelect(true);
                    }
                    btnChooseAll.setText("全不选");
                }else{
                    for(int i = 0;i < n;i++){
                        PublicObject.musicList.get(i).setSelect(false);
                    }
                    btnChooseAll.setText("全选");
                }
                musicAdapterRecycle.notifyDataSetChanged();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                boolean flag = false;
                int n = PublicObject.musicList.size();
                for(int i = 0;i < n;i++){
                    if(PublicObject.musicList.get(i).isSelect()){
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
                        if(PublicObject.musicList.get(i).isSelect()){
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
                            for(int i = musicAdapterRecycle.getMyMusicList().size()-1;i>=0;i--){
                                Music music = musicAdapterRecycle.getMyMusicList().get(i);
                                if(music.isSelect()){
                                    musicAdapterRecycle.getMyMusicList().remove(music);
                                    Functivity.deleteFile(music.getPath());
                                    DataSupport.deleteAll(Music.class,"path = ?",music.getPath());
                                }
                            }
                            musicAdapterRecycle.notifyDataSetChanged();

                            dialog.dismiss();
                            Toast.makeText(view.getContext(),"删除成功",Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(view.getContext(),"请选中歌曲",Toast.LENGTH_SHORT).show();
                }
            }
        });

        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(),"下一首歌曲",Toast.LENGTH_SHORT).show();
            }
        });

    }

//    protected void onPause(){
//        recyclerView.setAdapter(musicAdapterRecycle);
//        super.onPause();
//    }

//    protected void onRestart(){
//        super.onRestart();
//
//    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            }
            case R.id.homeScanning:{
                Intent intent = new Intent(MusicListActivity.this,MainActivity.class);
                intent.putExtra("extra_flag", "true");
                startActivity(intent);
                break;
            }
            case R.id.delete:{

                break;
            }
            case R.id.settings:{

                break;
            }
            default:{
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audioManager  = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                if(stateNow == STATE_MANAGE){
                    btnManage.setVisibility(View.VISIBLE);
                    LinOutButton.setVisibility(View.GONE);
                    musicAdapterRecycle.setEditMode(0);
                    stateNow = STATE_PLAY_ENABLE;
                }else{
                    ActivityCollector.finishAll();
                }
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

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu1,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.scanning_item:{
//
//                break;
//            }
//            default:{
//                break;
//            }
//
//        }
//        return true;
//    }
}
