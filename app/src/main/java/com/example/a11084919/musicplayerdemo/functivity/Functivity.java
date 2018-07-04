package com.example.a11084919.musicplayerdemo.functivity;

import java.io.File;

public class Functivity {
    public static boolean deleteFile(String filePath){
        File file = new File(filePath);
        if(file.isFile() && file.exists()){
            return file.delete();
        }
        return  false;
    }
}
