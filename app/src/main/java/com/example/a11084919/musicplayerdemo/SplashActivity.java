package com.example.a11084919.musicplayerdemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.security.Permission;

public class SplashActivity extends BaseActivity {

    private Context mContext;
    //private PermissionHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        setContentView(R.layout.activity_splash);
    }
}
