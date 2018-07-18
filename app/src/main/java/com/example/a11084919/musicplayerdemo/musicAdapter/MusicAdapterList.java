package com.example.a11084919.musicplayerdemo.musicAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.general.Functivity;

import java.util.List;

public class MusicAdapterList extends ArrayAdapter<Music>{
    private int resourceId;//listView 自定义 xml

    int mEditMode = 0;

    public MusicAdapterList(Context context, int textViewResourceId, List<Music> objects){
        super(context,textViewResourceId,objects);
        resourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Music music = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){//如果当前无view
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.btnOk = view.findViewById(R.id.btnOk);
            //viewHolder.imgAlbum = view.findViewById(R.id.imgAlbum);
            viewHolder.txtMusicName = view.findViewById(R.id.music_name);
            viewHolder.ckChoose = view.findViewById(R.id.ckChoose);
            viewHolder.btnMusicDelete = view.findViewById(R.id.btnMusicDelete);

            Bitmap bitmap = Functivity.getCover(music.getPic());
            if(bitmap == null){
                viewHolder.imgAlbum.setImageResource(R.drawable.picture_default);
            }else{
                viewHolder.imgAlbum.setImageBitmap(bitmap);
            }
            viewHolder.txtMusicName.setText(music.getTitle());
            view.setTag(viewHolder);// 将ViewHolder存储在View中
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        if (mEditMode == 0) {
            viewHolder.ckChoose.setVisibility(View.GONE);
            viewHolder.btnMusicDelete.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ckChoose.setVisibility(View.VISIBLE);
            viewHolder.btnMusicDelete.setVisibility(View.GONE);
        }

        return view;
    }

    class ViewHolder{
        LinearLayout btnOk;
        ImageView imgAlbum;
        TextView txtMusicName;
        CheckBox ckChoose;
        FrameLayout btnMusicDelete;
    }

    public void setEditMode(int editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();//此操作会导致适配器中每个元素都执行一遍onBindViewHolder
    }


}
