package com.example.a11084919.musicplayerdemo.general;

import com.example.a11084919.musicplayerdemo.model.FavoriteMusic;
import com.example.a11084919.musicplayerdemo.model.Music;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublicObject {
    public static List<Music> musicList;

    public static List<Music> allMusicList;
    //某个专辑下的歌曲
    public static List<Music> albumMusicList;
    public static List<FavoriteMusic> favoriteMusicList;

    public static List<String> albumList = new ArrayList<>();
    public static Map<String,List<Music>> musicMap = new HashMap<>();
    //播放歌曲的前一首与后一首位置
    public static int[] musicIndex = new int[2];

    public static boolean indexFlag = false;

    public static boolean threadFlag = true;
    //微信
    public static IWXAPI api;
}
