package com.example.a11084919.musicplayerdemo.musicAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.a11084919.musicplayerdemo.AlbumMusicListActivity;
import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.general.Functivity;
import com.example.a11084919.musicplayerdemo.general.PublicObject;
import com.example.a11084919.musicplayerdemo.model.Music;

import java.util.List;

public class AlbumAdapterRecycle extends RecyclerView.Adapter<AlbumAdapterRecycle.ViewHolder>{

    private List<String> mAlbumList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout albumInfo;
        ImageView imgAlbumShow;
        TextView txtAlbumName;
        TextView txtMusicNum;
        TextView txtMusicArtist;
        public ViewHolder(View view){
            super(view);
            albumInfo = view.findViewById(R.id.album_info);
            imgAlbumShow = view.findViewById(R.id.album_show);
            txtAlbumName = view.findViewById(R.id.album_name);
            txtMusicNum = view.findViewById(R.id.music_num);
            txtMusicArtist = view.findViewById(R.id.music_artist);
        }
    }

    public AlbumAdapterRecycle(Context context, List<String> albumList){
        mContext = context;
        mAlbumList = albumList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item_grid,parent,false);
        final ViewHolder holder = new ViewHolder(view);

        holder.albumInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                Context context = view.getContext();
                Intent intent = new Intent();
                intent.putExtra("extra_album_name",mAlbumList.get(position));
                intent.setClass(context, AlbumMusicListActivity.class);
                context.startActivity(intent);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String albumName = mAlbumList.get(position);
        holder.txtAlbumName.setText(albumName);

        int num = PublicObject.musicMap.get(albumName).size();
        holder.txtMusicNum.setText(num + "首");

        Music music = PublicObject.musicMap.get(albumName).get(0);
        holder.txtMusicArtist.setText(music.getArtist());

                //异步加载图片
        LoadPicture task = new LoadPicture(holder.imgAlbumShow);
        task.execute(music);


    }

        class LoadPicture extends AsyncTask<Music,Void,Bitmap> {
        private ImageView mImageView;
        private Music mMusic;

        public LoadPicture(ImageView imageView){
            mImageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Music... music) {
            mMusic = music[0];
            Bitmap bitmap = Functivity.getCover(mMusic.getPic());
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap == null){
                mImageView.setImageResource(R.drawable.picture_default);
            }else{
                mImageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mAlbumList.size();
    }
}
