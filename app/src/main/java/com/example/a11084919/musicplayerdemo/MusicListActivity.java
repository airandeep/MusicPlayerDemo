package com.example.a11084919.musicplayerdemo;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.model.Music;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterList;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterRecycle;

import org.litepal.crud.DataSupport;

public class MusicListActivity extends BaseActivity {


    private static String TAG = "MusicListActivity";
    private Button btnManage;
    private MusicAdapterRecycle musicAdapterRecycle;
    private MusicAdapterList musicAdapterList;
    private LinearLayout LinOutButton;
    private Button btnChooseAll;
    private Button btnDelete;
    private Button btnBack;
    private Button btnHome;

    public static final int STATE_PLAY_ENABLE = 0;
    public static final int STATE_MANAGE = 1;
    public static int stateNow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);



        RecyclerView recyclerView = findViewById(R.id.musicRecycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        musicAdapterRecycle = new MusicAdapterRecycle(MusicListActivity.this, PublicObject.musicList);
        recyclerView.setAdapter(musicAdapterRecycle);

//        musicAdapterList = new MusicAdapterList(MusicListActivity.this,R.layout.music_item,PublicObject.musicList);
//        ListView listView = findViewById(R.id.musicListView);
//        listView.setAdapter(musicAdapterList);

        btnManage = findViewById(R.id.btnManage);
        LinOutButton = findViewById(R.id.LinOutButton);
        btnChooseAll = findViewById(R.id.btnChooseAll);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.back_button);
        btnHome = findViewById(R.id.home_button);

        int n = PublicObject.musicList.size(),count = 0;
        for(int i = 0;i<n;i++){
            if(PublicObject.musicList.get(i).isSelect()){
                count++;
            }
        }
        if(count == n){
            btnChooseAll.setText("全不选");
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

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onKeyDown(KeyEvent.KEYCODE_BACK,null );
            }
        });


        btnHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicListActivity.this,MainActivity.class);
                intent.putExtra("extra_flag", "true");
                startActivity(intent);
                //finish();
            }
        });
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
