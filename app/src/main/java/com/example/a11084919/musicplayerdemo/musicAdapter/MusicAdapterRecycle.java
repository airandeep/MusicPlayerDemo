package com.example.a11084919.musicplayerdemo.musicAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.example.a11084919.musicplayerdemo.model.Music;
import com.example.a11084919.musicplayerdemo.play.PlayService;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MusicAdapterRecycle extends RecyclerView.Adapter<MusicAdapterRecycle.ViewHolder> {

    public List<Music> mMusicList;

    int mEditMode = 0;
    private static String TAG = "MusicAdapterRecycle";
    private Context mContext;
    private ServiceConnection mServiceConnection;


    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout btnOk;
        TextView txtMusicName;
        TextView txtMusicArtist;
        TextView txtTimeTotal;
        CheckBox ckChoose;
        FrameLayout btnMusicDelete;
        ImageView imgPlaying;
        //ImageView imgAlbum;

        public ViewHolder(View view) {
            super(view);
            btnOk = view.findViewById(R.id.btnOk);
            txtMusicName = view.findViewById(R.id.music_name);
            txtMusicArtist = view.findViewById(R.id.music_artist);
            txtTimeTotal = view.findViewById(R.id.txt_total_time);
            btnMusicDelete = view.findViewById(R.id.btnMusicDelete);
            ckChoose = view.findViewById(R.id.ckChoose);
            imgPlaying = view.findViewById(R.id.imgPlaying);
           // imgAlbum = view.findViewById(R.id.imgAlbum);
        }
    }

    //像适配器中传入数据集合
    public MusicAdapterRecycle(final ServiceConnection serviceConnection,Context context, List<Music> musicList) {
        mServiceConnection = serviceConnection;
        mMusicList = musicList;
        mContext = context;
    }


    //private  ViewHolder[] tempHolder = new ViewHolder[2];
    //此方法是资源文件musicList里有有多少就执行多少次？？？
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);
        //此纵向布局文件包括文本框控件和多选框控件
        holder.btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (MusicListActivity.stateNow == MusicListActivity.STATE_PLAY_ENABLE) {
                    int position = holder.getAdapterPosition();

//                    tempHolder[0] = tempHolder[1];
//                    tempHolder[1] = holder;
//                    if(tempHolder[0] != null){
//                        //tempHolder[0].txtMusicName.setTextColor(Color.parseColor("#545454"));
//                        tempHolder[0].imgPlaying.setVisibility(View.GONE);
//                    }
//                    //tempHolder[1].txtMusicName.setTextColor(Color.parseColor("#0000FF"));
//                    tempHolder[1].imgPlaying.setVisibility(View.VISIBLE);



//                    Intent binIntent = new Intent(mContext, PlayService.class);
//                    mContext.startService(binIntent);
//                    mContext.bindService(binIntent,mServiceConnection,Context.BIND_AUTO_CREATE);


                    Context context = v.getContext();
                    Intent intent = new Intent();
                    intent.putExtra("extra_position", String.valueOf(position));
                    intent.setClass(context, PlayerActivity.class);
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

        //当多选框状态改变时触发
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
    //此方法是只要视野内有新的item出现，新出现的item就会执行一次
    public void onBindViewHolder(ViewHolder holder, int position) {
        Music music = mMusicList.get(position);

        holder.txtMusicName.setText(music.getTitle());
        holder.txtMusicArtist.setText(music.getArtist());
        int time = music.getDuration()/1000;
        String str = String.format("%02d:%02d", time/ 60 % 60, time % 60);
        holder.txtTimeTotal.setText(str);
//        //异步加载图片
//        LoadPicture task = new LoadPicture(holder.imgAlbum);
//        task.execute(music);

        if(PublicObject.indexFlag){
//            if(position == PublicObject.musicIndexs[0]){
//                holder.txtMusicName.setTextColor(Color.parseColor("#545454"));
            if(position == PublicObject.musicIndexs[1]){
                //holder.txtMusicName.setTextColor(Color.parseColor("#0000FF"));
                holder.imgPlaying.setVisibility(View.VISIBLE);
            }else{
                //holder.txtMusicName.setTextColor(Color.parseColor("#545454"));
                holder.imgPlaying.setVisibility(View.GONE);
            }
        }

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

//    class LoadPicture extends AsyncTask<Music,Void,Bitmap>{
//        private ImageView mImageView;
//        private Music mMusic;
//
//        public LoadPicture(ImageView imageView){
//            mImageView = imageView;
//        }
//
//        @Override
//        protected Bitmap doInBackground(Music... music) {
//            mMusic = music[0];
//            Bitmap bitmap = Functivity.getCover(mMusic.getPic());
//            return bitmap;
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            if(bitmap == null){
//                mImageView.setImageResource(R.drawable.picture_default);
//            }else{
//                mImageView.setImageBitmap(bitmap);
//            }
//        }
//    }
    //此处重写的函数返回值决定RecycleView中有多少行
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
        popupMenu.inflate(R.menu.menu);
        // menu的item点击事件
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_item:{
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
