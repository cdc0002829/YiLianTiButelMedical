package cn.redcdn.hvs.profiles.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.profiles.voicedetection.AudioDetectHelper;
import cn.redcdn.hvs.profiles.voicedetection.SoundCheckManager;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/2/27.
 */

public class VoiceDetectActivity extends BaseActivity {
    private ImageView iv ;
    private Button start_voice_detect_btn;
    private Button restart_voice_detect;
    private Button voice_detect_back;


    private RelativeLayout rl1;
    private RelativeLayout rl2;
    private RelativeLayout rl3;
    private RelativeLayout rl4;

    public static final int MSG_START_VOICE_DETECT = 0x00000013;
    public static final int MSG_VOICE_DETECT_FAILURE = 0x00000014;
    public static final int MSG_VOICE_DETECT_SUCCESS = 0x00000015;
    public static final int MSG_VOICE_DETECT_ING = 0x00000016;
    public static final int MSG_VOICE_DETECT_SUCC_TWO_SECONDS = 0x00000017;
    public static final int MSG_VOICE_DETECT_TIMEOUT = 0x00000018;
    public static final int MSG_VOICE_DETECT_FINISH = 0x00000019;

    private boolean isVoiceDetecting = false;
    private boolean resultReached = false;
    private String voiceDetectStatus = "";
    private boolean isFinish = false;
    private boolean hasWrite = false;//标记是否已经写过结果到SharePrefence里面
    TimerTask timertask1 = null;
    Timer timer1 = null;
    TimerTask timertask = null;
    Timer timer = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_START_VOICE_DETECT:
                    if(!isFinish){
                        CustomLog.d(TAG,"开始声音检测消息 ");
                    }
                    break;
                case MSG_VOICE_DETECT_FAILURE:
                    if(!isFinish){
                        if(timertask1 != null){
                            CustomLog.d(TAG,"20秒超时timertask1取消");
                            timertask1.cancel();
                            timertask1 = null;
                        }
                        if(timer1 != null){
                            System.out.println("20秒超时timer1取消");
                            timer1.cancel();
                            timer1 = null;
                        }
                        CustomLog.d(TAG,"声音检测失败消息");
                        isVoiceDetecting = false;
                        if(!hasWrite){
                            voiceDetectStatus = getString(R.string.deny);
                            rl2.setVisibility(View.INVISIBLE);
                            rl4.setVisibility(View.VISIBLE);
                            SharedPreferences mySharedPreferences= getSharedPreferences("VDS",Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mySharedPreferences.edit();
                            editor.putString("vds", voiceDetectStatus);
                            editor.commit();
                            hasWrite = true;
                        }
                    }
                    break;
                case MSG_VOICE_DETECT_SUCCESS:
                    if(!isFinish){
                        SoundCheckManager.getInstance().saveDetectedResultToFile(msg.arg1);
                        if(timertask1 != null){
                            CustomLog.d(TAG,"20秒超时timertask1取消");
                            timertask1.cancel();
                            timertask1 = null;
                        }
                        if(timer1 != null){
                            CustomLog.d(TAG,"20秒超时timer1取消");
                            timer1.cancel();
                            timer1 = null;
                        }
                        CustomLog.d(TAG,"声音检测成功消息");
                        isVoiceDetecting = false;
                        voiceDetectStatus = getString(R.string.has_passed);
                        rl2.setVisibility(View.INVISIBLE);
                        rl3.setVisibility(View.VISIBLE);
                        if(!hasWrite){
                            SharedPreferences my= getSharedPreferences("VDS",Activity.MODE_PRIVATE);
                            SharedPreferences.Editor e = my.edit();
                            e.putString("vds", voiceDetectStatus);
                            e.commit();
                            hasWrite = true;
                        }
                    }
                    break;
                case MSG_VOICE_DETECT_ING:
                    if(!isFinish){
                        CustomLog.d(TAG,"正在进行声音检测消息 ");
                    }
                    break;
                case MSG_VOICE_DETECT_SUCC_TWO_SECONDS:
                    if(!isFinish){
                        CustomLog.d(TAG,"声音检测成功2秒钟消息 ");
                        Intent in = new Intent();
                        in.setClass(VoiceDetectActivity.this, SettingActivity.class);
                        startActivity(in);
                        VoiceDetectActivity.this.finish();
                    }
                    break;
                case MSG_VOICE_DETECT_TIMEOUT:
                    if(!isFinish){
                        CustomLog.d(TAG,"声音检测20秒超时消息 ");
                        // 认为检测失败
                        resultReached = true;
                        isVoiceDetecting = false;
                        File mfile = new File(Environment.getExternalStorageDirectory().getPath() + "/meeting/asyRecord.pcm");
                        if (!mfile.exists()) {
                            CustomToast.show(VoiceDetectActivity.this,getString(R.string.open_mic),CustomToast.LENGTH_SHORT);
                        } else if (mfile.length()==0){
                            CustomToast.show(VoiceDetectActivity.this,getString(R.string.open_mic),CustomToast.LENGTH_SHORT);
                        }
                        if(!hasWrite){
                            voiceDetectStatus = getString(R.string.deny);
                            rl2.setVisibility(View.INVISIBLE);
                            rl4.setVisibility(View.VISIBLE);
                            SharedPreferences mysharedPreferences= getSharedPreferences("VDS",Activity.MODE_PRIVATE);
                            SharedPreferences.Editor edit = mysharedPreferences.edit();
                            edit.putString("vds", voiceDetectStatus);
                            edit.commit();
                            hasWrite = true;
                        }
                    }
                    break;
                case MSG_VOICE_DETECT_FINISH:
                    if(!isFinish){
                        CustomLog.d(TAG,"声音检测页面Finish消息 ");
                        isFinish = true;
                        if(!hasWrite){
                            voiceDetectStatus = getString(R.string.deny);
                            SharedPreferences mysharedPreferences= getSharedPreferences("VDS", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor edit = mysharedPreferences.edit();
                            edit.putString("vds", voiceDetectStatus);
                            edit.commit();
                            hasWrite = true;
                        }
                    }
            }
        };
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_detect);
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setBack("", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d(TAG,"UI返回");
             
                    clearTimer();
                    Intent in = new Intent();
                    in.putExtra("VOICE_RES", voiceDetectStatus);
                    in.setClass(VoiceDetectActivity.this, SettingActivity.class);
                    startActivity(in);
                    VoiceDetectActivity.this.finish();

            }
        });
        titleBar.setTitle(getString(R.string.voice_detect));

        initWidget();
    }

    private void initWidget() {

        rl1 = (RelativeLayout)findViewById(R.id.voice_detect_rl_1);
        rl2 = (RelativeLayout)findViewById(R.id.voice_detect_rl_2);
        rl3 = (RelativeLayout)findViewById(R.id.voice_detect_rl_3);
        rl4 = (RelativeLayout)findViewById(R.id.voice_detect_rl_4);
        rl1.setVisibility(View.VISIBLE);

        iv = (ImageView)findViewById(R.id.rotate_iv);
        start_voice_detect_btn = (Button)findViewById(R.id.start_voice_detect);
        start_voice_detect_btn.setOnClickListener(mbtnHandleEventListener) ;
        restart_voice_detect= (Button)findViewById(R.id.restart_voice_detect);
        restart_voice_detect.setOnClickListener(mbtnHandleEventListener) ;
    }

    @Override
    public void todoClick(int id) {
        super.todoClick(id);
        switch (id) {
            case R.id.restart_voice_detect:
                CustomLog.d(TAG,"重新检测");
                if(mHandler != null){
                    mHandler.sendEmptyMessage(MSG_START_VOICE_DETECT);}
                start_voice_detect();
                break;
            case R.id.start_voice_detect:
                CustomLog.d(TAG,"开始声音检测");
                if(mHandler != null){
                    mHandler.sendEmptyMessage(MSG_START_VOICE_DETECT);}
                start_voice_detect();
                break;
            default:
                break;
        }
    }

    private void start_voice_detect(){
        if (isVoiceDetecting ) {
            CustomLog.e(TAG, "BootManager::startVoiceDetect() 正在进行声音检测,请勿重复点击");
            if(mHandler != null){
                mHandler.sendEmptyMessage(MSG_VOICE_DETECT_ING);}
            return;
        }
        rl1.setVisibility(View.INVISIBLE);
        rl3.setVisibility(View.INVISIBLE);
        rl4.setVisibility(View.INVISIBLE);
        rl2.setVisibility(View.VISIBLE);
        Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.voice_detect_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        iv.startAnimation(operatingAnim);

        isVoiceDetecting = true;//声音检测正在进行
        AudioDetectHelper.getInstance(this)
                .setAudioDetectCallBack(new AudioDetectHelper.AudioDetectCallBack() {
                    @Override
                    public void onDetectSuccess(int ret) {
                        if(resultReached){
                            CustomLog.e(TAG," 声音检测 已经20秒超时，成功结果不保存: "+ ret);
                            KeyEventWrite.write(KeyEventConfig.VOICE_CHECK + "_fail" + "_"
                                    + AccountManager.getInstance(getApplicationContext())
                                    .getAccountInfo().nube+ "_" + "声音检测20秒超时,返回的检测结果为"+ret);// 将声音检测超时结果写文件
                            resultReached = false;
                            return;
                        }else{
                            CustomLog.e(TAG," 声音检测成功: "+ ret);
                            KeyEventWrite.write(KeyEventConfig.VOICE_CHECK + "_ok" + "_"
                                    + AccountManager.getInstance(getApplicationContext())
                                    .getAccountInfo().nube);// 将成功结果写文件

                            Message msg = new Message();
                            msg.arg1 = ret;
                            msg.what = MSG_VOICE_DETECT_SUCCESS;
                            if(mHandler != null){
                                mHandler.sendMessage(msg);}

                            timertask = new TimerTask(){
                                public void run(){
                                    //execute the task
                                    if(mHandler!= null){
                                        mHandler.sendEmptyMessage(MSG_VOICE_DETECT_SUCC_TWO_SECONDS);
                                    }
                                }
                            };
                            timer = new Timer();
                            timer.schedule(timertask, 2000);
                        }

                    }
                    @Override
                    public void onDetectFail(int reason) {
                        if(resultReached){
                            CustomLog.e(TAG," 声音检测 已经20秒超时，失败结果不保存");
                            KeyEventWrite.write(KeyEventConfig.VOICE_CHECK + "_fail" + "_"
                                    + AccountManager.getInstance(getApplicationContext())
                                    .getAccountInfo().nube+ "_" + "声音检测20秒超时,返回的检测结果为"+reason);// 将声音检测超时结果写文件
                            resultReached = false;
                            return;
                        }else{
                            CustomLog.e(TAG,"声音检测失败: "+ reason);
                            KeyEventWrite.write(KeyEventConfig.VOICE_CHECK + "_fail" + "_"
                                    + AccountManager.getInstance(getApplicationContext())
                                    .getAccountInfo().nube+ "_" + reason);// 将失败结果写文件
                            //	SoundCheckManager.getInstance().saveDetectedResultToFile(reason);
                            if(mHandler != null){
                                mHandler.sendEmptyMessage(MSG_VOICE_DETECT_FAILURE);}
                        }

                    }
                });
        AudioDetectHelper.getInstance(this).startDetect();
        timertask1 = new TimerTask(){
            public void run(){
                //execute the task
                if(mHandler!= null){
                    mHandler.sendEmptyMessage(MSG_VOICE_DETECT_TIMEOUT);
                }
            }
        };
        timer1 = new Timer();
        timer1.schedule(timertask1, 20000);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        AudioDetectHelper.getInstance(this).release();

        if(isVoiceDetecting == true){
            if(mHandler!= null){
                mHandler.sendEmptyMessage(MSG_VOICE_DETECT_FINISH);//发送页面销毁消息
            }
        }else if(isVoiceDetecting == false){

        }

        CustomLog.e(TAG,"onStop");
        VoiceDetectActivity.this.finish();
        super.onStop();
    }

    private void clearTimer(){
        if(timertask != null){
            CustomLog.d(TAG,"2秒自动返回timertask取消");
            timertask.cancel();
            timertask = null;
        }
        if(timer != null){
            CustomLog.d(TAG,"2秒自动返回timer取消");
            timer.cancel();
            timer = null;
        }
        if(timertask1 != null){
            CustomLog.d(TAG,"20秒自动返回timertask取消");
            timertask1.cancel();
            timertask1 = null;
        }
        if(timer1 != null){
            CustomLog.d(TAG,"20秒自动返回timer取消");
            timer1.cancel();
            timer1 = null;
        }
    }

    @Override
    public void onBackPressed() {
        CustomLog.d(TAG,"系统返回键");

            clearTimer();
            Intent in = new Intent();
            in.putExtra("VOICE_RES", voiceDetectStatus);
            in.setClass(VoiceDetectActivity.this, SettingActivity.class);
            startActivity(in);
            VoiceDetectActivity.this.finish();


    }
}
