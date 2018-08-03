package com.example.a11084919.musicplayerdemo.musicAdapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11084919.musicplayerdemo.PlayerActivity;
import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.model.FavoriteMusic;
import com.example.a11084919.musicplayerdemo.model.Music;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class FavoriteMusicAdapterRecycle extends RecyclerView.Adapter<FavoriteMusicAdapterRecycle.ViewHolder>{
    public List<FavoriteMusic> mMusicList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout musicInfo;
        TextView txtMusicName;
        TextView txtMusicArtist;
        TextView txtTimeTotal;
        CheckBox ckChoose;
        FrameLayout musicDelete;
        ImageView imgPlaying;
        //ImageView imgAlbum;

        public ViewHolder(View view) {
            super(view);
            musicInfo = view.findViewById(R.id.music_info);
            txtMusicName = view.findViewById(R.id.txt_music_name);
            txtMusicArtist = view.findViewById(R.id.txt_music_artist);
            txtTimeTotal = view.findViewById(R.id.txt_total_time);
            musicDelete = view.findViewById(R.id.music_delete);
            ckChoose = view.findViewById(R.id.ck_choose);
            imgPlaying = view.findViewById(R.id.img_playing);
            // imgAlbum = view.findViewById(R.id.imgAlbum);
        }
    }

    public FavoriteMusicAdapterRecycle(Context context,List<FavoriteMusic> favoriteMusicList){
        mContext = context;
        mMusicList = favoriteMusicList;
    }

    public void setMusicList(List<FavoriteMusic> favoriteMusicList){
        mMusicList = favoriteMusicList;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        holder.musicInfo.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                List<Music> musics = new ArrayList<>();
                int n = mMusicList.size();
                for(int i = 0;i<n;i++){
                    Music music = new Music(mMusicList.get(i));
                    musics.add(music);
                }
                PublicObject.musicList = musics;
                int position = holder.getAdapterPosition();
                // Context context = v.getContext();
                Intent intent = new Intent();
                intent.putExtra("extra_position", String.valueOf(position));
                intent.setClass(mContext, PlayerActivity.class);
                mContext.startActivity(intent);
            }
        });

        holder.musicDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                showPopupMenu(v,position);
            }
        });

        return holder;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        FavoriteMusic music = mMusicList.get(position);

        holder.txtMusicName.setText(music.getTitle());
        holder.txtMusicArtist.setText(music.getArtist());
        int time = music.getDuration()/1000;
        String str = String.format("%02d:%02d", time/ 60 % 60, time % 60);
        holder.txtTimeTotal.setText(str);


    }
    //此处重写的函数返回值决定RecycleView中有多少行
    public int getItemCount() {
        return mMusicList.size();
    }

    private void showPopupMenu(View view, final int position) {
        // View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(mContext, view, Gravity.END | Gravity.BOTTOM);
        // menu布局
        popupMenu.inflate(R.menu.menu_favorite);

        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.remove_item:{
                        FavoriteMusic music = mMusicList.get(position);
                        mMusicList.remove(position);
                        DataSupport.deleteAll(FavoriteMusic.class,"path = ?",music.getPath());
                        notifyDataSetChanged();
                        Toast.makeText(mContext,"取消到喜欢",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:{
                        break;
                    }
                }
                return false;
            }
        });
        popupMenu.show();
    }

}
