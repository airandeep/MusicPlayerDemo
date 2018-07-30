package com.example.a11084919.musicplayerdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.model.Music;
import com.example.a11084919.musicplayerdemo.musicAdapter.AlbumAdapterRecycle;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterList;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterRecycle;
import com.example.a11084919.musicplayerdemo.play.IPlay;
import com.example.a11084919.musicplayerdemo.play.PlayService;

import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicListActivity extends BaseActivity implements IPlay.Callback{

    private Handler navDrawerRunnable = new Handler();

    private static String TAG = "MusicListActivity";
    private AlbumAdapterRecycle albumAdapterRecycle;
    private MusicAdapterRecycle musicAdapterRecycle;
    private LinearLayout LinOutButton;
    private Button btnChooseAll;
    private Button btnDelete;
    private RecyclerView recyclerView;


    private NavigationView navigationView;
    private View headerView;
    private ImageView imgNavHeadShow;
    private TextView txtNavHeadPlayInfo;
    private TextView txtNavHeadPlaySinger;

    private FrameLayout bottomNav;
    private LinearLayout bottomNavInfo;
    private ImageView imgShow;
    private ImageView imgNavControl;
    private ImageView imgNavNext;
    private TextView txtPlayInfo;
    private TextView txtPlaySinger;
    private ProgressBar musicProgress;

    private Bitmap bmpMp3;

    public static int stateNow;
    public static final int STATE_PLAY_ENABLE = 0;
    public static final int STATE_MANAGE = 1;

    public static int adapterNow;
    public static final int ADAPTER_MUSIC = 0;
    public static final int ADAPTER_ALBUM = 1;

    private DrawerLayout drawerLayout;
    private ActionBar actionBar;

    private IPlay mPlayer;
    private PlayService playService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            playService = ((PlayService.LocalBinder)iBinder).getService();
            mPlayer = playService;
            mPlayer.registerCallback(MusicListActivity.this);
            //刷新界面
            onPlayStatusChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        initView();

        PublicObject.musicList = PublicObject.allMusicList;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //将nav_call设置为默认选中
        switch (adapterNow){
            case ADAPTER_MUSIC:{
                navigationView.setCheckedItem(R.id.nav_play_list);
                bottomNav.setVisibility(View.VISIBLE);
                actionBar.setTitle("播放列表");
                break;
            }
            case ADAPTER_ALBUM:{
                navigationView.setCheckedItem(R.id.nav_album_list);
                bottomNav.setVisibility(View.GONE);
                actionBar.setTitle("专辑列表");
                break;
            }
        }

        setupDrawerContent(navigationView);
        //将本活动与服务绑定
        bindPlayService();
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(this,2);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        if(adapterNow == ADAPTER_ALBUM){
//            recyclerView.setLayoutManager(gridLayoutManager);
//        }else{
            recyclerView.setLayoutManager(linearLayoutManager);
 //       }



        recyclerView.setNestedScrollingEnabled(false);

        //异步实例化适配器
        new LoadAdapter().execute("");

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

        btnChooseAll.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                int n = PublicObject.allMusicList.size();
                String flag = btnChooseAll.getText().toString();
                if(flag.equals("全选")){
                    for(int i = 0;i < n;i++){
                        PublicObject.allMusicList.get(i).setSelect(true);
                    }
                    btnChooseAll.setText("全不选");
                }else{
                    for(int i = 0;i < n;i++){
                        PublicObject.allMusicList.get(i).setSelect(false);
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
                            Functivity.initAlbumList(PublicObject.musicList);
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

        bottomNavInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PublicObject.threadFlag){
                    Toast.makeText(view.getContext(),"当前未播放歌曲",Toast.LENGTH_SHORT).show();
                }else{
                    if(stateNow == STATE_PLAY_ENABLE){
                        Intent intent = new Intent(MusicListActivity.this,PlayerActivity.class);
                        startActivity(intent);
                    }
                }

            }
        });

        imgNavControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PublicObject.threadFlag){
                    Toast.makeText(view.getContext(),"当前未播放歌曲",Toast.LENGTH_SHORT).show();
                }else{
                    if(mPlayer.isPlaying()){
                        mPlayer.pause();
                    }else{
                        mPlayer.rePlay();
                    }
                }
            }
        });

        imgNavNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(view.getContext(),"下一首歌曲",Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent();
//                intent.setClassName("com.zhihu.android","com.zhihu.android.ui.activity.GuideActivity");
//                startActivity(intent);
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                ComponentName cn = new ComponentName("com.zhihu.android","com.zhihu.android.app.ui.activity.ScanningActivity");
//                intent.setComponent(cn);
//                startActivity(intent);
                if(PublicObject.threadFlag){
                    Toast.makeText(view.getContext(),"当前未播放歌曲",Toast.LENGTH_SHORT).show();
                }else{
                    mPlayer.playNext();
                }
            }
        });

    }

    private void initView(){
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.music_list);
        //btnManage = findViewById(R.id.btnManage);
        LinOutButton = findViewById(R.id.LinOutButton);
        btnChooseAll = findViewById(R.id.btnChooseAll);
        btnDelete = findViewById(R.id.btnDelete);

        bottomNav = findViewById(R.id.bottom_nav);
        navigationView = findViewById(R.id.nav_view);
        //headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        headerView = navigationView.getHeaderView(0);

        imgNavHeadShow = headerView.findViewById(R.id.nav_head_show);
        txtNavHeadPlayInfo = headerView.findViewById(R.id.nav_head_play_info);
        txtNavHeadPlaySinger = headerView.findViewById(R.id.nav_head_play_singer);
        bottomNavInfo = findViewById(R.id.bottom_nav_info);
        imgShow = findViewById(R.id.img_show);
        imgNavControl = findViewById(R.id.nav_img_control);
        imgNavNext = findViewById(R.id.nav_img_next);
        txtPlayInfo = findViewById(R.id.play_info);
        txtPlaySinger = findViewById(R.id.play_singer);
        musicProgress = findViewById(R.id.music_progress);
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch(item.getItemId()){
                    case R.id.nav_play_list:{
                        if(adapterNow != ADAPTER_MUSIC){
                            //尽管延迟0.5秒执行run中方法降低了反应速度，但是可以防止侧滑掉帧数
                            navDrawerRunnable.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    adapterNow = ADAPTER_MUSIC;
                                    recyclerView.setAdapter(musicAdapterRecycle);
                                    bottomNav.setVisibility(View.VISIBLE);
                                    actionBar.setTitle("播放列表");
                                    if(stateNow == STATE_MANAGE){
                                        bottomNav.setVisibility(View.GONE);
                                        LinOutButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            }, 300);

                        }
                        break;
                    }
                    case R.id.nav_album_list:{
                        if(adapterNow != ADAPTER_ALBUM){

                            navDrawerRunnable.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    adapterNow = ADAPTER_ALBUM;
                                    recyclerView.setAdapter(albumAdapterRecycle);
                                    bottomNav.setVisibility(View.GONE);
                                    actionBar.setTitle("专辑列表");
                                    if(stateNow == STATE_MANAGE){
                                        LinOutButton.setVisibility(View.GONE);
                                    }
                                }
                            }, 300);

                        }
                        break;
                    }
                    default:{

                        break;
                    }
                }
                return true;
            }
        });
    }


    private class LoadAdapter extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            musicAdapterRecycle = new MusicAdapterRecycle(MusicListActivity.this,PublicObject.musicList);
            albumAdapterRecycle = new AlbumAdapterRecycle(MusicListActivity.this,PublicObject.albumList);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            switch (adapterNow){
                case ADAPTER_MUSIC:{
                    recyclerView.setAdapter(musicAdapterRecycle);
                    break;
                }
                case ADAPTER_ALBUM:{
                    recyclerView.setAdapter(albumAdapterRecycle);
                    break;
                }
            }

        }
    }

    private void bindPlayService(){
        Intent bindIntent = new Intent(this,PlayService.class);
        startService(bindIntent);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }


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
                Intent intent = new Intent(MusicListActivity.this,ScanningActivity.class);
                intent.putExtra("extra_flag", "true");
                startActivity(intent);
                break;
            }
            case R.id.delete:{

                break;
            }
            case R.id.settings:{
                if(adapterNow == ADAPTER_MUSIC){
                    if(stateNow == STATE_PLAY_ENABLE){
                        bottomNav.setVisibility(View.GONE);
                        LinOutButton.setVisibility(View.VISIBLE);
                        musicAdapterRecycle.setEditMode(1);
                        stateNow = STATE_MANAGE;
                    }
                }
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
                if(stateNow == STATE_MANAGE && adapterNow == ADAPTER_MUSIC){
                    bottomNav.setVisibility(View.VISIBLE);
                    //btnManage.setVisibility(View.VISIBLE);
                    LinOutButton.setVisibility(View.GONE);
                    musicAdapterRecycle.setEditMode(0);
                    stateNow = STATE_PLAY_ENABLE;

                }else{
                    stateNow = STATE_PLAY_ENABLE;
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
        unbindPlaybackService();
    }


    private void unbindPlaybackService(){
        //并不会调用onServiceDisconnected方法
        unbindService(connection);
        mPlayer.unregisterCallback(MusicListActivity.this);
    }

    public void onSwitchLast(){
        txtNavHeadPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        txtNavHeadPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        txtPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        txtPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        if(mPlayer.isPlaying()){
            imgNavControl.setImageResource(R.drawable.btn_pause);
        }else{
            imgNavControl.setImageResource(R.drawable.btn_play);
        }
        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(bmpMp3 == null){
            imgNavHeadShow.setImageResource(R.drawable.picture_default);
            imgShow.setImageResource(R.drawable.picture_default);
        }else{
            imgNavHeadShow.setImageBitmap(bmpMp3);
            imgShow.setImageBitmap(bmpMp3);
        }

        if(adapterNow == ADAPTER_MUSIC){
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[0]);
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[1]);
        }

    }

    public void onSwitchNext(){
        txtNavHeadPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        txtNavHeadPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        txtPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        txtPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(mPlayer.isPlaying()){
            imgNavControl.setImageResource(R.drawable.btn_pause);
        }else{
            imgNavControl.setImageResource(R.drawable.btn_play);
        }
        if(bmpMp3 == null){
            imgNavHeadShow.setImageResource(R.drawable.picture_default);
            imgShow.setImageResource(R.drawable.picture_default);
        }else{
            imgNavHeadShow.setImageBitmap(bmpMp3);
            imgShow.setImageBitmap(bmpMp3);
        }

        if(adapterNow == ADAPTER_MUSIC){
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[0]);
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[1]);
        }

    }

    public void onPlayStatusChanged(){


        if(!PublicObject.threadFlag){
            txtNavHeadPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
            txtNavHeadPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
            txtPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
            txtPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
            if(mPlayer.isPlaying()){
                imgNavControl.setImageResource(R.drawable.btn_pause);
            }else{
                imgNavControl.setImageResource(R.drawable.btn_play);
            }
            bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
            if(bmpMp3 == null){
                imgNavHeadShow.setImageResource(R.drawable.picture_default);
                imgShow.setImageResource(R.drawable.picture_default);
            }else{
                imgNavHeadShow.setImageBitmap(bmpMp3);
                imgShow.setImageBitmap(bmpMp3);
            }
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[0]);
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[1]);
        }



    }
    //更新活动中进度条//
    public void onUpdateProgressBar(){
        if(adapterNow == ADAPTER_MUSIC){
            int currentTime = mPlayer.getProgress();
            int maxTime = mPlayer.getDuration();
            int max = musicProgress.getMax();
            musicProgress.setProgress(max * currentTime/maxTime);
        }



    }
}
