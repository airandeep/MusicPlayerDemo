package com.example.a11084919.musicplayerdemo.musicAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.a11084919.musicplayerdemo.R;

import java.util.List;

public class MusicAdapterList extends ArrayAdapter<Music>{
    private int resourceId;//listView 自定义 xml

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
            viewHolder.MusicName = view.findViewById(R.id.music_name);
            viewHolder.MusicName.setText(music.getName());

//            viewHolder.btnMusicDelete = view.findViewById(R.id.btnMusicDelete);
//            viewHolder.btnMusicDelete.setText(music.getName()+"删除");

            view.setTag(viewHolder);// 将ViewHolder存储在View中
        }else{
            view = convertView;
            //viewHolder = (ViewHolder) view.getTag(); // 重新获取ViewHolder
        }

        return view;
    }

    class ViewHolder{
        TextView MusicName;
        Button btnMusicDelete;
    }
}
