package com.example.a11084919.musicplayerdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.a11084919.musicplayerdemo.general.Util;
import com.example.a11084919.musicplayerdemo.model.Msg;
import com.example.a11084919.musicplayerdemo.musicAdapter.MsgAdapter;
import com.example.a11084919.musicplayerdemo.play.IPlay;
import com.example.a11084919.musicplayerdemo.play.PlayService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VoiceRecognitionActivity extends AppCompatActivity implements EventListener{
    private List<Msg> msgList = new ArrayList();


    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;
    private Button btnBack;
    private Button btnVoiceControl;

    private EventManager asr;
    private boolean enableOffline = false; // 测试离线命令词，需要改成true
    private String strShow;
    private Msg mMsg;
    private Msg mMsg1;

    private boolean isUseVoice;

    private IPlay mPlayer;
    private PlayService playService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            playService = ((PlayService.LocalBinder)iBinder).getService();
            mPlayer = playService;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    //语音回调通过Handler机制进行反应
    Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                //语音识别信息//resultType 是 partial_result
                case 0:{
                    mMsg.setContent(strShow);
                    adapter.notifyDataSetChanged();
                    break;
                }
                //resultType 是 final_result
                case 1:{
                    mMsg.setContent(strShow);
                    adapter.notifyDataSetChanged();
                    break;
                }
                //识别结果信息//
                case 2:{
                    btnVoiceControl.setText("开始识别");
                    if(strShow!=null){//代码鲁莽性//不知道什么情况时会出现strShow为null
                        if(strShow.indexOf("播放器")!=-1){
                            mMsg1.setContent("进入播放页面");
                            adapter.notifyDataSetChanged();
                            Intent intent = new Intent(VoiceRecognitionActivity.this,PlayerActivity.class);
                            intent.putExtra("extra_position",String.valueOf(mPlayer.getPosition()));
                            startActivity(intent);
                        }else if(strShow.indexOf("下")!=-1){
                            mMsg1.setContent("已成功识别为下一首切换至下一首歌曲");
                            adapter.notifyDataSetChanged();
                            mPlayer.playNext();
                        }else if(strShow.indexOf("上")!=-1){
                            mMsg1.setContent("已成功识别为上一首切换至下一首歌曲");
                            adapter.notifyDataSetChanged();
                            mPlayer.playLast();
                        }else{
                            mMsg1.setContent("已经识别结束请从新开启语音识别");
                            adapter.notifyDataSetChanged();
                        }
                        strShow = "";
                    }
                    break;
                }
                default:{
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recognition);
        initView();



        bindPlayService();
        //初始化语音操作
        asr = EventManagerFactory.create(this, "asr");
        asr.registerListener(this); //  EventListener 中 onEvent方法\
        initPermission();
        initMsgs(); // 初始化消息数据
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setAdapter(adapter);

        btnBack.setOnClickListener((view)->{finish();});

        btnVoiceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Util.checkNetworkState(VoiceRecognitionActivity.this)){
                    if(isUseVoice){
                        String strFlag = btnVoiceControl.getText().toString();
                        if(strFlag.equals("开始识别")){
                            btnVoiceControl.setText("关闭识别");
                            new Thread(()->start()).start();

                        }else{
                            new Thread(()->stop()).start();

                        }
                    }else{
                        Toast.makeText(VoiceRecognitionActivity.this,"由于权限未获取，所以无法开启语音识别",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(VoiceRecognitionActivity.this,"请去设置中心打开网络连接",Toast.LENGTH_SHORT).show();
                }



            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0);
        unbindService(connection);
    }

    private void initView(){
        msgRecyclerView = findViewById(R.id.msg_recycler_view);
        btnVoiceControl = findViewById(R.id.btn_voice_control);
        btnBack = findViewById(R.id.btn_back);
    }

    private void bindPlayService(){
        Intent bindIntent = new Intent(this,PlayService.class);
        startService(bindIntent);
        bindService(bindIntent,connection,BIND_AUTO_CREATE);
    }


    private void initMsgs() {
        mMsg = new Msg("",Msg.TYPE_SENT);
        mMsg1 = new Msg("Hi，你可以通过简单的语音对播放器进行控制", Msg.TYPE_RECEIVED);
        msgList.add(mMsg);
        msgList.add(mMsg1);
    }

    private void start(){
        Map<String,Object> params = new LinkedHashMap<>();
        String event;
        event = SpeechConstant.ASR_START; // 替换成测试的event
        if (enableOffline) {
            params.put(SpeechConstant.DECODER, 2);
        }
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        String json = null; // 可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        asr.send(event, json, null, 0, 0);
    }

    private void stop(){
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0); //
    }

    /**
     * android 6.0 以上需要动态申请权限//申请录音权限和录音权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }else{
            isUseVoice = true;
        }

    }

    @Override
    public void onEvent(String name, String params, byte[] bytes, int i, int i1) {
        if(params!=null && name.equals("asr.partial")){
            try {
                JSONObject jsonObject = new JSONObject(params);

                String resultType = jsonObject.getString("result_type");
                String bestResult = jsonObject.getString("best_result");
                if(resultType.equals("partial_result")){
                    strShow = bestResult;
                    mHandler.sendEmptyMessage(0);
                }else if(resultType.equals("final_result")){
                    strShow = bestResult;
                    mHandler.sendEmptyMessage(1);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }else if(name.equals("asr.exit")){
            mHandler.sendEmptyMessage(2);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        switch (requestCode){
            case 123:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    isUseVoice = true;
                }else{
                    isUseVoice = false;
                    Toast.makeText(this,"未能获取智能语音使用相关权限\n授权后方可使用智能语音相关功能",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

}
