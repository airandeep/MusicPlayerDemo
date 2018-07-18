package com.example.a11084919.musicplayerdemo.general;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import java.io.File;

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


}
