package com.example.a11084919.musicplayerdemo.general;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;

import com.example.a11084919.musicplayerdemo.R;
import com.example.a11084919.musicplayerdemo.model.Music;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Util {

    public static Util sInstance;

    public Util getInstance(){
        if(sInstance == null){
            synchronized (Util.class){
                if(sInstance == null){
                    sInstance = new Util();
                }
            }
        }
        return sInstance;
    }


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


    public static void shareMusic(int type,Context context,String url,String title,String description,Bitmap thumb){
        WXMusicObject music = new WXMusicObject();
        music.musicUrl=url;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = music;
        msg.title = title;
        msg.description = description;
        if(thumb == null){
            thumb = BitmapFactory.decodeResource(context.getResources(), R.drawable.picture_default);
        }
        msg.thumbData = bmpToByteArray(thumb,true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("music");

        req.message = msg;
        if(type == 0){
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
        }else if(type == 1){
            req.scene = SendMessageToWX.Req.WXSceneSession;
        }else if(type == 2){
            req.scene = SendMessageToWX.Req.WXSceneFavorite;
        }


        PublicObject.api.sendReq(req);

    }

    private static String buildTransaction(final String type) {
        return (type ==null) ?String.valueOf(System.currentTimeMillis()) :type+System.currentTimeMillis();
    }

    private static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    private static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        int i;
        int j;
        if (bmp.getHeight() > bmp.getWidth()) {
            i = bmp.getWidth();
            j = bmp.getWidth();
        }  else {
            i = bmp.getHeight();
            j = bmp.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
        Canvas localCanvas =  new Canvas(localBitmap);

        while ( true) {
            localCanvas.drawBitmap(bmp,  new Rect(0, 0, i, j),  new Rect(0, 0,i, j),  null);
            if (needRecycle)
                bmp.recycle();
            ByteArrayOutputStream localByteArrayOutputStream =  new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.JPEG, 10,
                    localByteArrayOutputStream);
            localBitmap.recycle();
            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
            try {
                localByteArrayOutputStream.close();
                return arrayOfByte;
            }  catch (Exception e) {
                // F.out(e);
            }
            i = bmp.getHeight();
            j = bmp.getHeight();
        }
    }

}
