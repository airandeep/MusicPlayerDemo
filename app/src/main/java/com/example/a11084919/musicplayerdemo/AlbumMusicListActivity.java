package com.example.a11084919.musicplayerdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.musicAdapter.AlbumAdapterRecycle;
import com.example.a11084919.musicplayerdemo.musicAdapter.MusicAdapterRecycle;

public class AlbumMusicListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private MusicAdapterRecycle musicAdapterRecycle;

    private Button btnBack;
    private TextView txtAlbumName;
    private ImageView imgShow;
    private TextView txtAlbumName1;
    private TextView txtMusicNum;

    private Bitmap bmpMp3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_music_list);
        initView();
        MusicListActivity.stateNow = MusicListActivity.STATE_PLAY_ENABLE;
        //初期处理
        PublicObject.musicIndex[0] = PublicObject.musicIndex[1] = -1;
        Intent intent = getIntent();
        String strAlbumName = intent.getStringExtra("extra_album_name");
        PublicObject.albumMusicList = PublicObject.musicMap.get(strAlbumName);

        txtAlbumName.setText(strAlbumName);
        bmpMp3 = Functivity.getCover(PublicObject.albumMusicList.get(0).getPic());
        if(bmpMp3 == null){
            imgShow.setImageResource(R.drawable.picture_default);
        }else{
            imgShow.setImageBitmap(bmpMp3);
        }
        txtAlbumName1.setText(strAlbumName);
        int num = PublicObject.albumMusicList.size();
        txtMusicNum.setText(num + "首歌曲");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        //异步实例化适配器
        new LoadAdapter().execute("");


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initView(){
        recyclerView = findViewById(R.id.music_list);
        btnBack = findViewById(R.id.btn_back);
        txtAlbumName = findViewById(R.id.txt_album_name);
        imgShow = findViewById(R.id.img_show);
        txtAlbumName1 = findViewById(R.id.txt_album_name1);
        txtMusicNum = findViewById(R.id.txt_music_num);
    }

    private class LoadAdapter extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            musicAdapterRecycle = new MusicAdapterRecycle(AlbumMusicListActivity.this,PublicObject.albumMusicList);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(musicAdapterRecycle);
        }
    }
}
