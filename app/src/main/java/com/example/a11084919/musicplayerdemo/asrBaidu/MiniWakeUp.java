package com.example.a11084919.musicplayerdemo.asrBaidu;

import android.content.Context;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

public class MiniWakeUp implements EventListener {

    private Context mContext;
    private EventManager wakeup;

    public MiniWakeUp(Context context){
        mContext = context;
        wakeup = EventManagerFactory.create(mContext,"up");
        wakeup.registerListener(this);
    }

    public void start(){
        Map<String,Object> params = new TreeMap<>();
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");


        String json = null; // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        wakeup.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
    }

    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        try {
            JSONObject jsonObject = new JSONObject(params);
            String errorDesc = jsonObject.getString("errorDesc");
            String errorCode = jsonObject.getString("errorCode");
            String word = jsonObject.getString("word");

            Log.d("AIRAN", errorDesc + " " + errorCode + " " + word);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
