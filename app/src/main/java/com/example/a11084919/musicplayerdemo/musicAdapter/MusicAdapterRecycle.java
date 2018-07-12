package com.example.a11084919.musicplayerdemo.musicAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a11084919.musicplayerdemo.MusicListActivity;
import com.example.a11084919.musicplayerdemo.PlayerActivity;
import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MusicAdapterRecycle extends RecyclerView.Adapter<MusicAdapterRecycle.ViewHolder> {
    public List<Music> mMusicList;

    int mEditMode = 0;
    private static String TAG = "MusicAdapterRecycle";
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {
        //View musicView;
        LinearLayout btnOk;
        TextView txtMusicName;
        FrameLayout btnMusicDelete;
        CheckBox ckChoose;
//        ImageView imgAlbum;
//        ImageView imgPlaying;


        public ViewHolder(View view) {
            super(view);
            //musicView = view;
            btnOk = view.findViewById(R.id.btnOk);
            txtMusicName = view.findViewById(R.id.music_name);
            btnMusicDelete = view.findViewById(R.id.btnMusicDelete);
            ckChoose = view.findViewById(R.id.ckChoose);
//            imgAlbum = view.findViewById(R.id.imgAlbum);
//            imgPlaying = view.findViewById(R.id.imgPlaying);
        }
    }

    //像适配器中传入数据集合
    public MusicAdapterRecycle(Context context,List<Music> musicList) {
        mMusicList = musicList;
        mContext = context;
    }


    //此方法是资源文件musicList里有有多少就执行多少次
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        //此纵向布局文件包括文本框控件和多选框控件
        holder.btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (MusicListActivity.stateNow == MusicListActivity.STATE_PLAY_ENABLE) {
                    Music music = mMusicList.get(position);
                    String name = music.getName();
                    String path = music.getPath();
                    Context context = v.getContext();
                    Intent intent = new Intent();
                    intent.putExtra("extra_name", name);
                    intent.putExtra("extra_path", path);
                    intent.putExtra("extra_position", String.valueOf(position));
                    intent.setClass(context, PlayerActivity.class);

                    //PublicObject.playingIndex = position;
                    context.startActivity(intent);
                } else {
                    if (holder.ckChoose.isChecked()) {
                        holder.ckChoose.setChecked(false);
                    } else {
                        holder.ckChoose.setChecked(true);
                    }
                }
            }
        });


        holder.btnMusicDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                showPopupMenu(v,position);
            }
        });

        //当多选框
        holder.ckChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = holder.getAdapterPosition();
                if (compoundButton.isChecked()) {
                    mMusicList.get(position).setSelect(true);
                } else {
                    mMusicList.get(position).setSelect(false);
                }


            }
        });

        return holder;
    }

    //交互的关键所在
    public void onBindViewHolder(ViewHolder holder, int position) {
        Music music = mMusicList.get(position);
        holder.txtMusicName.setText(music.getTitle());

//        Bitmap bitmap = Functivity.getCover(music.getPic());
//        if(bitmap == null){
//            holder.imgAlbum.setImageResource(R.drawable.picture_default);
//        }else{
//            holder.imgAlbum.setImageBitmap(bitmap);
//        }
//
//        if(position == PublicObject.playingIndex){
//            holder.imgPlaying.setVisibility(View.VISIBLE);
//        }else{
//            holder.imgPlaying.setVisibility(View.GONE);
//        }
        //
        if (mMusicList.get(position).isSelect()) {
            holder.ckChoose.setChecked(true);
        } else {
            holder.ckChoose.setChecked(false);
        }


        if (mEditMode == 0) {
            holder.ckChoose.setVisibility(View.GONE);
            holder.btnMusicDelete.setVisibility(View.VISIBLE);
        } else {
            holder.ckChoose.setVisibility(View.VISIBLE);
            holder.btnMusicDelete.setVisibility(View.GONE);
        }
    }

    public int getItemCount() {
        return mMusicList.size();
    }

    public void setEditMode(int editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();//此操作会导致适配器中每个元素都执行一遍onBindViewHolder
    }


    public List<Music> getMyMusicList() {
        return mMusicList;
    }


    private void showPopupMenu(View view, final int position) {
        // View当前PopupMenu显示的相对View的位置
        PopupMenu popupMenu = new PopupMenu(mContext, view,Gravity.END | Gravity.BOTTOM);
        // menu布局
        popupMenu.inflate(R.menu.main);
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.music_name_item:{
                        Music music = mMusicList.get(position);
                        mMusicList.remove(position);
                        //移除适配器中的内容即使notifyDataSetChanged刷新一下
                        Functivity.deleteFile(music.getPath());
                        DataSupport.deleteAll(Music.class,"path = ?",music.getPath());
                        notifyDataSetChanged();
                        Toast.makeText(mContext,"歌曲" + music.getName() + "删除成功",Toast.LENGTH_SHORT).show();
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
