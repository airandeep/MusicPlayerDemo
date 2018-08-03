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
import com.example.a11084919.musicplayerdemo.model.FavoriteMusic;
import com.example.a11084919.musicplayerdemo.model.Music;
import com.example.a11084919.musicplayerdemo.musicAdapter.AlbumAdapterRecycle;
import com.example.a11084919.musicplayerdemo.musicAdapter.FavoriteMusicAdapterRecycle;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterRecycle;
import com.example.a11084919.musicplayerdemo.play.IPlay;
import com.example.a11084919.musicplayerdemo.play.PlayService;

import org.litepal.crud.DataSupport;

public class MusicListActivity extends BaseActivity implements IPlay.Callback{

    private Handler navDrawerRunnable = new Handler();

    private static String TAG = "MusicListActivity";
    private AlbumAdapterRecycle albumAdapterRecycle;
    private MusicAdapterRecycle musicAdapterRecycle;
    private FavoriteMusicAdapterRecycle favoriteMusicAdapterRecycle;
    private LinearLayout linearBottomButtons;
    private Button btnChooseAll;
    private Button btnDelete;
    private RecyclerView recyclerView;

    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private Toolbar toolbar;

    private NavigationView navView;
    private View headerView;
    private ImageView navImgHeadShow;
    private TextView navTxtHeadPlayInfo;
    private TextView navTxtHeadPlaySinger;

    private FrameLayout bottomView;
    private LinearLayout bottomNavInfo;
    private ImageView bottomImgShow;
    private ImageView bottomImgControl;
    private ImageView bottomImgNext;
    private TextView bottomTxtPlayInfo;
    private TextView bottomTxtPlaySinger;
    private ProgressBar musicProgress;

    private Bitmap bmpMp3;

    public static int stateNow;
    public static final int STATE_PLAY_ENABLE = 0;
    public static final int STATE_MANAGE = 1;

    public static int adapterNow;
    public static final int ADAPTER_MUSIC = 0;
    public static final int ADAPTER_FAVORITE_MUSIC = 1;
    public static final int ADAPTER_ALBUM = 2;

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


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        //适配器处理，默认是0也就是是ADAPTER_MUSIC
        switch (adapterNow){
            case ADAPTER_MUSIC:{
                navView.setCheckedItem(R.id.nav_play_list);
                bottomView.setVisibility(View.VISIBLE);
                actionBar.setTitle("播放列表");
                recyclerView.setLayoutManager(linearLayoutManager);
                break;
            }
            case ADAPTER_FAVORITE_MUSIC:{
                navView.setCheckedItem(R.id.nav_music_like);
                bottomView.setVisibility(View.VISIBLE);
                actionBar.setTitle("喜爱歌曲");
                recyclerView.setLayoutManager(linearLayoutManager);
                break;
            }
            case ADAPTER_ALBUM:{
                navView.setCheckedItem(R.id.nav_album_list);
                bottomView.setVisibility(View.GONE);
                actionBar.setTitle("专辑列表");
                recyclerView.setLayoutManager(gridLayoutManager);
                break;
            }
        }


        recyclerView.setNestedScrollingEnabled(false);
        //异步实例化适配器
        new LoadAdapter().execute("");
        //将本活动与服务绑定
        bindPlayService();

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
        //侧边栏点击事件注册
        setupDrawerContent(navView);

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
                            Functivity.initAlbumListAndMusicMap(PublicObject.musicList);
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

        bottomImgControl.setOnClickListener(new View.OnClickListener() {
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

        bottomImgNext.setOnClickListener(new View.OnClickListener() {
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
        linearBottomButtons = findViewById(R.id.linear_bottom_buttons);
        btnChooseAll = findViewById(R.id.btn_choose_all);
        btnDelete = findViewById(R.id.btn_delete);

        bottomView = findViewById(R.id.bottom_view);
        navView = findViewById(R.id.nav_view);
        headerView = navView.getHeaderView(0);

        navImgHeadShow = headerView.findViewById(R.id.nav_img_head_show);
        navTxtHeadPlayInfo = headerView.findViewById(R.id.nav_txt_head_play_info);
        navTxtHeadPlaySinger = headerView.findViewById(R.id.nav_txt_head_play_singer);

        bottomNavInfo = findViewById(R.id.bottom_nav_info);
        bottomImgShow = findViewById(R.id.bottom_img_show);
        bottomTxtPlayInfo = findViewById(R.id.bottom_txt_play_info);
        bottomTxtPlaySinger = findViewById(R.id.bottom_txt_play_singer);
        bottomImgControl = findViewById(R.id.bottom_img_control);
        bottomImgNext = findViewById(R.id.bottom_img_next);


        musicProgress = findViewById(R.id.music_progress);
        gridLayoutManager = new GridLayoutManager(this,2);
        linearLayoutManager = new LinearLayoutManager(this);
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
                                    recyclerView.setLayoutManager(linearLayoutManager);
                                    recyclerView.setAdapter(musicAdapterRecycle);

                                    actionBar.setTitle("播放列表");
                                    if(stateNow == STATE_MANAGE){
                                        bottomView.setVisibility(View.GONE);
                                        linearBottomButtons.setVisibility(View.VISIBLE);
                                    }else{
                                        bottomView.setVisibility(View.VISIBLE);
                                        linearBottomButtons.setVisibility(View.GONE);
                                    }
                                }
                            }, 300);

                        }
                        break;
                    }
                    case R.id.nav_music_like:{
                        if(adapterNow != ADAPTER_FAVORITE_MUSIC){
                            navDrawerRunnable.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    adapterNow = ADAPTER_FAVORITE_MUSIC;
                                    PublicObject.favoriteMusicList = DataSupport.findAll(FavoriteMusic.class);
                                    recyclerView.setLayoutManager(linearLayoutManager);
                                    favoriteMusicAdapterRecycle.setMusicList(PublicObject.favoriteMusicList );
                                    recyclerView.setAdapter(favoriteMusicAdapterRecycle);

                                    actionBar.setTitle("喜欢列表");
                                    if(stateNow == STATE_MANAGE){
                                        bottomView.setVisibility(View.GONE);
                                        linearBottomButtons.setVisibility(View.VISIBLE);
                                    }else{
                                        bottomView.setVisibility(View.VISIBLE);
                                        linearBottomButtons.setVisibility(View.GONE);
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
                                    recyclerView.setLayoutManager(gridLayoutManager);
                                    recyclerView.setAdapter(albumAdapterRecycle);
                                    bottomView.setVisibility(View.GONE);
                                    actionBar.setTitle("专辑列表");
                                    if(stateNow == STATE_MANAGE){
                                        linearBottomButtons.setVisibility(View.GONE);
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
            musicAdapterRecycle = new MusicAdapterRecycle(MusicListActivity.this,PublicObject.allMusicList);
            favoriteMusicAdapterRecycle = new FavoriteMusicAdapterRecycle(MusicListActivity.this,PublicObject.favoriteMusicList);
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
                case ADAPTER_FAVORITE_MUSIC:{
                    recyclerView.setAdapter(favoriteMusicAdapterRecycle);
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
            case R.id.settings:{
                if(adapterNow != ADAPTER_ALBUM){
                    if(stateNow == STATE_PLAY_ENABLE){
                        bottomView.setVisibility(View.GONE);
                        linearBottomButtons.setVisibility(View.VISIBLE);
                        musicAdapterRecycle.setEditMode(1);
                        stateNow = STATE_MANAGE;
                    }else{
                        bottomView.setVisibility(View.VISIBLE);
                        linearBottomButtons.setVisibility(View.GONE);
                        musicAdapterRecycle.setEditMode(0);
                        stateNow = STATE_PLAY_ENABLE;
                    }
                }
                break;
            }
            case R.id.voice_recognition:{
                Intent intent = new Intent(MusicListActivity.this,VoiceRecognitionActivity.class);
                startActivity(intent);
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
                    bottomView.setVisibility(View.VISIBLE);
                    linearBottomButtons.setVisibility(View.GONE);
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
        navTxtHeadPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        navTxtHeadPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        bottomTxtPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        bottomTxtPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        if(mPlayer.isPlaying()){
            bottomImgControl.setImageResource(R.drawable.btn_pause);
        }else{
            bottomImgControl.setImageResource(R.drawable.btn_play);
        }
        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(bmpMp3 == null){
            navImgHeadShow.setImageResource(R.drawable.picture_default);
            bottomImgShow.setImageResource(R.drawable.picture_default);
        }else{
            navImgHeadShow.setImageBitmap(bmpMp3);
            bottomImgShow.setImageBitmap(bmpMp3);
        }

        if(adapterNow == ADAPTER_MUSIC){
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[0]);
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[1]);
        }

    }

    public void onSwitchNext(){
        navTxtHeadPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        navTxtHeadPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        bottomTxtPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
        bottomTxtPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
        bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
        if(mPlayer.isPlaying()){
            bottomImgControl.setImageResource(R.drawable.btn_pause);
        }else{
            bottomImgControl.setImageResource(R.drawable.btn_play);
        }
        if(bmpMp3 == null){
            navImgHeadShow.setImageResource(R.drawable.picture_default);
            bottomImgShow.setImageResource(R.drawable.picture_default);
        }else{
            navImgHeadShow.setImageBitmap(bmpMp3);
            bottomImgShow.setImageBitmap(bmpMp3);
        }

        if(adapterNow == ADAPTER_MUSIC){
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[0]);
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[1]);
        }

    }

    public void onPlayStatusChanged(){


        if(!PublicObject.threadFlag){
            navTxtHeadPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
            navTxtHeadPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
            bottomTxtPlayInfo.setText(mPlayer.getCurrentMusic().getTitle());
            bottomTxtPlaySinger.setText(mPlayer.getCurrentMusic().getArtist());
            if(mPlayer.isPlaying()){
                bottomImgControl.setImageResource(R.drawable.btn_pause);
            }else{
                bottomImgControl.setImageResource(R.drawable.btn_play);
            }
            bmpMp3 = Functivity.getCover(mPlayer.getCurrentMusic().getPic());
            if(bmpMp3 == null){
                navImgHeadShow.setImageResource(R.drawable.picture_default);
                bottomImgShow.setImageResource(R.drawable.picture_default);
            }else{
                navImgHeadShow.setImageBitmap(bmpMp3);
                bottomImgShow.setImageBitmap(bmpMp3);
            }
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[0]);
            musicAdapterRecycle.notifyItemChanged(PublicObject.musicIndex[1]);
        }



    }
    //更新活动中进度条//
    public void onUpdateProgressBar(){
        if(adapterNow != ADAPTER_ALBUM){
            int currentTime = mPlayer.getProgress();
            int maxTime = mPlayer.getDuration();
            int max = musicProgress.getMax();
            musicProgress.setProgress(max * currentTime/maxTime);
        }



    }
}
