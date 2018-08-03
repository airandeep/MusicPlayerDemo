package com.example.a11084919.musicplayerdemo.general;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;

import com.example.a11084919.musicplayerdemo.model.Music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Functivity {
    public static boolean deleteFile(String filePath){
        File file = new File(filePath);
        if(file.isFile() && file.exists()){
            return file.delete();
        }
        return  false;
    }

    public static Bitmap getCover(String mediaUri){
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(mediaUri);
        byte[] picture = mediaMetadataRetriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if(picture != null){
            bitmap= BitmapFactory.decodeByteArray(picture,0,picture.length);
        }
        return bitmap;
    }

    public static Bitmap getCover(byte[] mediaByte){
        Bitmap bitmap = null;
        if(mediaByte == null){
            return bitmap;
        }
        bitmap= BitmapFactory.decodeByteArray(mediaByte,0,mediaByte.length);
        return bitmap;
    }

    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void initAlbumListAndMusicMap(final List<Music> musicListPara){
        if(PublicObject.albumList != null){
            PublicObject.albumList.clear();
            PublicObject.musicMap.clear();
        }
        int n = musicListPara.size();
        for(int i = 0;i < n;i++){
            String key = musicListPara.get(i).getAlbum();
            List<Music> value;
            if(PublicObject.musicMap.containsKey(key)){
                PublicObject.musicMap.get(key).add(musicListPara.get(i));
            }else{
                PublicObject.albumList.add(key);

                value = new ArrayList<>();
                value.add(musicListPara.get(i));
                PublicObject.musicMap.put(key,value);
            }
        }
    }

    /**
     * 检测网络是否连接
     * @return
     */
    public static boolean checkNetworkState(Context context) {
        boolean flag = false;
        //得到网络连接信息
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        return flag;
    }


}
