package com.example.a11084919.musicplayerdemo.musicAdapter;

import java.util.ArrayList;
import java.util.List;

public class Music {


    public static List<Music>  musicList = new ArrayList<>();


    private String name;
    private String path;
    private boolean isSelect;

    public Music(String name,String path){
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }

}
