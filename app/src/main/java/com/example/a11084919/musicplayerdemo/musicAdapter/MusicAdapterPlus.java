package com.example.a11084919.musicplayerdemo.musicAdapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.a11084919.musicplayerdemo.MusicListActivity;
import com.example.a11084919.musicplayerdemo.PlayerViewActivity;
import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.functivity.Functivity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.content.ContextCompat.startActivity;

public class MusicAdapterPlus extends RecyclerView.Adapter<MusicAdapterPlus.ViewHolder> {
    public List<Music> mMusicList;


    int mEditMode = 0;
    private static String TAG = "MusicAdapterPlus";
    static class ViewHolder extends RecyclerView.ViewHolder{
        View musicView;
        TextView txtMusicName;
        Button btnMusicDelete;
        CheckBox ckChoose;



        public ViewHolder(View view){
            super(view);
            musicView = view;
            txtMusicName = view.findViewById(R.id.music_name);
            btnMusicDelete = view.findViewById(R.id.btnMusicDelete);
            ckChoose = view.findViewById(R.id.ckChoose);

        }
    }

    //像适配器中传入数据集合
    public MusicAdapterPlus(List<Music> musicList){
        mMusicList = musicList;
    }



    //此方法是资源文件musicList里有有多少就执行多少次
    public ViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);

        holder.txtMusicName.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                int position = holder.getAdapterPosition();
                Music music = mMusicList.get(position);
                String name = music.getName();
                String path = music.getPath();
                Context context = v.getContext();
                Intent intent = new Intent();
                intent.putExtra("extra_name",name);
                intent.putExtra("extra_path",path);
                intent.putExtra("extra_position",String.valueOf(position));
                intent.setClass(context,PlayerViewActivity.class);

                context.startActivity(intent);

            }
        });

        holder.btnMusicDelete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                final AlertDialog dialog = new AlertDialog.Builder(v.getContext()).create();
                dialog.show();
                dialog.getWindow().setContentView(R.layout.pop_user);
                TextView msg =  dialog.findViewById(R.id.tv_msg);
                Button cancel = dialog.findViewById(R.id.btn_cancle);
                Button sure = dialog.findViewById(R.id.btn_sure);
                if (msg == null || cancel == null || sure == null) return;

                cancel.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });

                sure.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View v){
                        int position = holder.getAdapterPosition();
                        Music music = mMusicList.get(position);
                        mMusicList.remove(position);
                        //移除适配器中的内容即使notifyDataSetChanged刷新一下
                        Functivity.deleteFile(music.getPath());
                        notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });

            }
        });

        holder.ckChoose.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = holder.getAdapterPosition();
                if(compoundButton.isChecked()){
                    mMusicList.get(position).setSelect(true);
                }else{
                    mMusicList.get(position).setSelect(false);
                }


            }
        });

        return holder;
    }


    public void onBindViewHolder(ViewHolder holder,int position){
        Music music = mMusicList.get(position);
        holder.txtMusicName.setText(music.getName());

        //这里必须匹配？？？？
        if(mMusicList.get(position).isSelect()){
            holder.ckChoose.setChecked(true);
        }else{
            holder.ckChoose.setChecked(false);
        }


        if(mEditMode == 0){
            holder.ckChoose.setVisibility(View.GONE);
            holder.btnMusicDelete.setVisibility(View.VISIBLE);
        }else{
            holder.ckChoose.setVisibility(View.VISIBLE);
            holder.btnMusicDelete.setVisibility(View.GONE);
        }
    }

    public int getItemCount(){
        return mMusicList.size();
    }

    public void setEditMode(int editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();//此操作会导致适配器中每个元素都执行一遍onBindViewHolder
    }


    public List<Music> getMyMusicList(){
        return mMusicList;
    }

}
