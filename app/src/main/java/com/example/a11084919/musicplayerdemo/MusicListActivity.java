package com.example.a11084919.musicplayerdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11084919.musicplayerdemo.publicObjective.Functivity;
import com.example.a11084919.musicplayerdemo.musicAdapter.Music;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterRecycle;

import java.io.File;

public class MusicListActivity extends BaseActivity {


    private static String TAG = "MusicListActivity";
    private Button btnManage;
    private MusicAdapterRecycle adapterPlus;
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


        RecyclerView recyclerView = findViewById(R.id.musicRecycleView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapterPlus = new MusicAdapterRecycle(Music.musicList);
        recyclerView.setAdapter(adapterPlus);



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
                    ActivityCollector.finishAll();
                }

                break;
                default:
                    break;
        }
        return true;
    }





}
