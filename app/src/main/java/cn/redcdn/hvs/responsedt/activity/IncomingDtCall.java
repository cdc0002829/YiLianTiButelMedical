package cn.redcdn.hvs.responsedt.activity;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.responsedt.adapter.IndicatorExpandableListAdapter;
import cn.redcdn.hvs.udtroom.configs.UDTDataConstant;
import cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity;
import cn.redcdn.incoming.HostAgent;
import cn.redcdn.incoming.IncomingDialog;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.log.CustomLog;
import cn.redcdn.util.MResource;
import cn.redcdn.util.RoundImageView;
import cn.redcdn.util.SystemManger;


public class IncomingDtCall extends BaseActivity {
    public static final String INCOMING_DT_CALL_INVITER_NAME = "incomingDtCallInviterName";
    public static final String INCOMING_DT_CALL_HEADURL = "incomingDtCallHeadUrl";
    public static final String INCOMING_DT_CALL_DTID = "incomingDtCallDtId";
    private ImageButton joinBtn;
    private ImageButton ignoreBtn;
    private ImageView headPicBg;
    private ImageView headPicBgSec;
    private RoundImageView headPic;
    private String inviterName;
    private String headUrl;
    private String dtId;
    private boolean openSound = true;
    private boolean openTTS;

    private int ringTimelong = 70 * 1000; // 70秒
    private int ttsDelayTime = 3000; // 3秒
    Timer ignoreTimer;

    private MediaPlayer mp;
    private TextToSpeech tts;

    private int PLAY_ALARM_MESSAGE = 1;
    private int PLAY_TTS_MESSAGE = 2;
    private int PLAY_ALARM_FINISH_MESSAGE = 3;
    private int PLAY_TTS_FINISH_MESSAGE = 4;
    private int TIMEOUT_INCOMING_RING_MESSAGE = 5;
    private int TIMEOUT_STOP = 6;

    private final int DELAY_UPDATE_SECOND = 500;

    private String playText;

    private Ringtone ringtone = null;

    private Uri audioUri = null;
    //    // 记录铃音的大小
//    private int ringVolume;
    // 记录播放模式
    private boolean isRing = false;
    private Vibrator vibrator = null;

    enum VoicePlayState {
        NONE, PLAYING
    }

    ;

    VoicePlayState state = VoicePlayState.NONE;

    SystemManger mSystemManager;

    AudioManager audio;
    // 记录播放模式
    private int audioMode = -1;

    private MediaPlayer player;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (PLAY_ALARM_MESSAGE == msg.what) {
                if (!openSound) {
                    CustomLog.d(TAG, "来电铃声关闭. ignore play sound");
                    handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
                    return;
                }

                if (VoicePlayState.NONE == state) {
                    CustomLog.i(TAG, "state == VoicePlayState.NONE, return");
                    return;
                }

                if (playAlarm() < 0) {
                    handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
                }
            } else if (PLAY_ALARM_FINISH_MESSAGE == msg.what) {
                CustomLog.i(TAG, "play alarm finish!");
                if (openTTS) {
                    handler.sendEmptyMessage(PLAY_TTS_MESSAGE);
                } else if (!openTTS && openSound) {
                    handler.sendEmptyMessage(PLAY_ALARM_MESSAGE);
                }
            } else if (PLAY_TTS_MESSAGE == msg.what) {
                if (!openTTS) {
                    CustomLog.d(TAG, "TTS语音播报关闭. ignore play tts");
                    return;
                }

                CustomLog.i(TAG, "tts text:" + playText);

                if (VoicePlayState.NONE == state) {
                    return;
                }

            } else if (PLAY_TTS_FINISH_MESSAGE == msg.what) {
                CustomLog.i(TAG, "play tts finish and release tts");

                if (VoicePlayState.NONE == state) {
                    return;
                }

                if (null != tts) {
                    tts.shutdown();
                    tts = null;
                }

                if (openSound) {
                    handler.sendEmptyMessage(PLAY_ALARM_MESSAGE);
                } else {
                    handler.sendEmptyMessageDelayed(PLAY_TTS_MESSAGE,
                            ttsDelayTime);
                }

            } else if (TIMEOUT_INCOMING_RING_MESSAGE == msg.what) {
                CustomLog.i(TAG,
                        "release play resource and finish incoming activity");

                state = VoicePlayState.NONE;
                releasePlayResource();

                ignoreTimer = null;
                IncomingDtCall.this.finish();
            } else if (TIMEOUT_STOP == msg.what) {
                CustomLog.i(TAG,
                        "timeout tostop");

                stopIncomingDialog();
            } else {
                CustomLog.i(TAG, "invalidate play type");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_dt_call);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 保持视频时，窗口一直是高亮显示
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        inviterName = getIntent().getStringExtra(INCOMING_DT_CALL_INVITER_NAME);
        headUrl = getIntent().getStringExtra(INCOMING_DT_CALL_HEADURL);
        dtId = getIntent().getStringExtra(INCOMING_DT_CALL_DTID);

        joinBtn = (ImageButton) findViewById(R.id.incoming_dt_call_right_button);
        joinBtn.setOnClickListener(mbtnHandleEventListener);
        joinBtn.setFocusable(true);
        joinBtn.requestFocus();

        ignoreBtn = (ImageButton) findViewById(R.id.incoming_dt_call_left_button);
        ignoreBtn.setOnClickListener(mbtnHandleEventListener);
        ignoreBtn.setFocusable(true);

        setFinishOnTouchOutside(false);
        TextView response_doctor_name = (TextView) findViewById(R.id.incoming_dt_call_dialog_title);
        response_doctor_name.setText(inviterName);

        TextView info_textView = (TextView) findViewById(R.id.incoming_dt_call_dialog_nube);
        info_textView.setText(getString(R.string.have_response_dt));
        headPicBg = (ImageView) findViewById(R.id.incoming_dt_call_pic_bg);
        headPicBgSec = (ImageView) findViewById(R.id.incoming_dt_call_pic_bg_sec);
        headPic = (RoundImageView) findViewById(R.id.incoming_dt_call_pic);
        AlphaAnimation alphaAnimation1 = new AlphaAnimation(0.01f, 1.0f);
        alphaAnimation1.setDuration(1500);
        alphaAnimation1.setRepeatCount(Animation.INFINITE);
        alphaAnimation1.setRepeatMode(Animation.REVERSE);
        headPicBg.setAnimation(alphaAnimation1);

        AlphaAnimation alphaAnimation2 = new AlphaAnimation(1.0f, 0.01f);
        alphaAnimation2.setDuration(1500);
        alphaAnimation2.setRepeatCount(Animation.INFINITE);
        alphaAnimation2.setRepeatMode(Animation.REVERSE);
        headPicBgSec.setAnimation(alphaAnimation2);
        alphaAnimation1.start();
        alphaAnimation2.start();
        ImageLoader imageLoader = ImageLoader.getInstance();
        // 若options没有传递给ImageLoader.displayImage(…)方法，那么从配置默认显示选项(ImageLoaderConfiguration.defaultDisplayImageOptions(…))将被使用
        imageLoader.displayImage(headUrl, headPic, displayImageOpt(),
                new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                        CustomLog.i(TAG, "displayImage onLoadingComplete ");
                    }
                });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        audio = (AudioManager) getSystemService(AUDIO_SERVICE);
        mSystemManager = new SystemManger(this);
        // 停止音乐播放,获取音频服务焦点
        mSystemManager.pauseMusic(true);

        // 释放键盘锁等
        //  mSystemManager.reenableKeyguard();
        // 先解锁，再点亮屏幕
        mSystemManager.disableKeyguard();
        MedicalMeetingManage.getInstance().isHaveIncomingCall = true;
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.incoming_dt_call_right_button:
                CustomLog.d(TAG, "incomingDtCall 点击接听按钮");
                ignoreTimer.cancel();
                Intent intent = new Intent(IncomingDtCall.this, UDTChatRoomActivity.class);
                intent.putExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG, dtId);
                startActivity(intent);
                if (VoicePlayState.NONE != state) {
                    state = VoicePlayState.NONE;
                    releasePlayResource();
                }

                IncomingDtCall.this.finish();
                break;
            case R.id.incoming_dt_call_left_button:
                CustomLog.d(TAG, "incomingDtCall 点击拒绝按钮");
                ignoreTimer.cancel();
                if (VoicePlayState.NONE != state) {
                    state = VoicePlayState.NONE;
                    releasePlayResource();
                }
                IncomingDtCall.this.finish();
                break;
            default:
                break;
        }
    }

    private DisplayImageOptions displayImageOpt() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(
                        MResource.getIdByName(IncomingDtCall.this,
                                MResource.DRAWABLE,
                                "jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片在下载期间显示的图片
                .showImageForEmptyUri(
                        MResource.getIdByName(IncomingDtCall.this,
                                MResource.DRAWABLE,
                                "jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(
                        MResource.getIdByName(IncomingDtCall.this,
                                MResource.DRAWABLE,
                                "jmeetingsdk_custom_oprate_dialog_normal_pic"))// 设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)// 是否緩存都內存中
                .cacheOnDisc(true)// 是否緩存到sd卡上
                .displayer(new RoundedBitmapDisplayer(20))// 设置图片的显示方式 : 设置圆角图片
                // int //
                // // roundPixels
                .bitmapConfig(Bitmap.Config.RGB_565)// 设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)// 载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        return options;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        switch (keyCode) {
            // 音量减小
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                state = VoicePlayState.NONE;
                releasePlayResource();
                return true;
            // 音量增大
            case KeyEvent.KEYCODE_VOLUME_UP:
                state = VoicePlayState.NONE;
                releasePlayResource();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        CustomLog.d(TAG, "incoming activity init");

        if (state == VoicePlayState.PLAYING) {
            CustomLog.d(TAG, "current play state is PLAYING, ignore");
            super.onStart();
            return;
        }

        ignoreTimer = new Timer();
        ignoreTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CustomLog.i(TAG, "deadtime, finish IncomingActivity");

                handler.sendEmptyMessage(TIMEOUT_INCOMING_RING_MESSAGE);
            }
        }, ringTimelong);

        state = VoicePlayState.PLAYING;
        handler.sendEmptyMessage(PLAY_ALARM_MESSAGE);

        super.onStart();
    }

    @Override
    protected void onStop() {
        CustomLog.i(TAG, "handle IncomingActivity onStop method");

//		CustomLog.i(tag, "ignore incoming meeting and release play resource");
//		if(ignoreTimer!=null){
//			ignoreTimer.cancel();
//		}

//		Intent ignoreIntent = new Intent();
//		ignoreIntent.setAction(IGNORE_MEETING_BROADCAST);
//		sendBroadcast(ignoreIntent);
//


        handler.sendEmptyMessageDelayed(TIMEOUT_STOP,
                DELAY_UPDATE_SECOND);
        CustomLog.i(TAG, "sendEmptyMessageDelayed TIMEOUT stop");
//		IncomingDialog.this.finish();
        super.onStop();
    }

    private void stopIncomingDialog() {
        CustomLog.i(TAG, "hhandle IncomingActivity stopIncomingDialog method");
//		CustomLog.i(tag, "ignore incoming meeting and release play resource");
        if (VoicePlayState.PLAYING == state) {
            releasePlayResource();
            state = VoicePlayState.NONE;
        }
        if (ignoreTimer != null) {
            ignoreTimer.cancel();
        }

        IncomingDtCall.this.finish();
    }

    @Override
    protected void onDestroy() {
        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        if (VoicePlayState.PLAYING == state) {
            releasePlayResource();
            state = VoicePlayState.NONE;
        }
        if (mSystemManager != null) {
            // 释放休眠锁等
            mSystemManager.releaseScreenOffWakeLock();
            mSystemManager.releaseWakeLock();
            // 释放键盘锁等
            mSystemManager.reenableKeyguard();
        }
        MedicalMeetingManage.getInstance().isHaveIncomingCall = false;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        CustomLog.i(TAG, "ignore backpress key");
        return;
    }

    protected void onMenuBtnPressed() {
        CustomLog.i(TAG, "ignore menu key");
        return;
    }

    private int playAlarm() {
        CustomLog.i(TAG, "play alarm, init mediaplayer");
        if (Build.MODEL.equals("ZTE U930HD")) {
            startPlay(Uri.parse("android.resource://"
                    + this.getPackageName()
                    + "/"
                    + MResource.getIdByName(getApplicationContext(),
                    MResource.RAW, "jmeetingsdk_incoming")));
        } else {
            startRing(Uri.parse("android.resource://"
                    + this.getPackageName()
                    + "/"
                    + MResource.getIdByName(getApplicationContext(),
                    MResource.RAW, "jmeetingsdk_incoming")));
        }

        return 0;
    }

    public void startRing(Uri ringUri) {

        CustomLog.d(TAG, "AudioPlayerService start ring");
        if (ringUri == null) {
            return;
        }

        AudioManager audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringtone = RingtoneManager.getRingtone(this, ringUri);
        // 获取系统的振动模式
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //        ringVolume = audioMgr.getStreamVolume(AudioManager.STREAM_RING);

        int mode = audioMgr.getRingerMode();
        int vibratorSetR = audioMgr
                .getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
        boolean bVibrateRing = audioMgr
                .shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER);
        CustomLog.d(TAG, "ringerMode:" + mode + " | vibratorSetR:" + vibratorSetR
                + " | bVibrateRing:" + bVibrateRing);

        if (mode == AudioManager.RINGER_MODE_NORMAL) {
            audioMode = audioMgr.getMode();
            audioMgr.setMode(AudioManager.MODE_RINGTONE);
            ringtone.play();
        } else if (mode == AudioManager.RINGER_MODE_VIBRATE) {
            vibrator.vibrate(new long[]{0, 400, 100, 400, 100}, 0);
            bVibrateRing = false;
        } else if (mode == AudioManager.RINGER_MODE_SILENT) {
        }

        if (bVibrateRing) {
            vibrator.vibrate(new long[]{0, 400, 100, 400, 100}, 0);
        }

        isRing = true;

        CustomLog.d(TAG, "AudioPlayerService end ring");

    }


    public void stopPlay() {

        CustomLog.d(TAG, "AudioPlayerService stop play");
        if (isRing) {
            // stop ring
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
                ringtone = null;
            }
            if (vibrator != null) {
                vibrator.cancel();
                vibrator = null;
            }
            // 还原系统原来的模式
            if (audioMode != -1) {
                CustomLog.d(TAG, "ring,还原系统audioMode：" + audioMode);
                ((AudioManager) getSystemService(Context.AUDIO_SERVICE))
                        .setMode(audioMode);
                audioMode = -1;
            }
            isRing = false;
        }


    }

    public void startPlay(Uri ringUri) {

        CustomLog.d(TAG, "Mediaplayer start play");
        audioMode = audio.getMode();
        CustomLog.d(TAG, "system audio mode：" + audioMode);

        if (!Build.MODEL.equalsIgnoreCase("ZTE U930HD")) {
            //	  audioMgr.setMode(AudioManager.MODE_IN_CALL);
            audio.setMode(AudioManager.MODE_RINGTONE);
        } else {
            // 中兴U930HD手机，设置MODE_IN_CALL无法播放出声音，故使用外放
            audio.setMode(AudioManager.MODE_NORMAL);
        }

        mp = new MediaPlayer();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (null != mp) {
                    mp.release();
                    mp = null;
                }
                CustomLog.i(TAG, "play alarm finish and release mp");

                handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
            }
        });

        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (null != mp) {
                    mp.release();
                    mp = null;
                }

                CustomLog.i(TAG, "play alarm failed and release mp");

                handler.sendEmptyMessage(PLAY_ALARM_FINISH_MESSAGE);
                return false;
            }
        });

        try {
            mp.setDataSource(
                    this,
                    ringUri);
            mp.prepare();
            mp.start();
        } catch (IllegalArgumentException e) {
            CustomLog.e(TAG, "play alarm error:" + e.getMessage());
            e.printStackTrace();
            //	return -1;
        } catch (IllegalStateException e) {
            CustomLog.e(TAG, "play alarm error:" + e.getMessage());
            e.printStackTrace();
            //	return -1;
        } catch (IOException e) {
            CustomLog.e(TAG, "play alarm error:" + e.getMessage());
            e.printStackTrace();
            //	return -1;
        }
    }

    private void releasePlayResource() {
        CustomLog.i(TAG, "release play resource");
        if (Build.MODEL.equals("ZTE U930HD")) {
            // 还原系统原来的模式
            if (audioMode != -1) {
                CustomLog.d(TAG, "ring,还原系统audioMode：" + audioMode);
                ((AudioManager) getSystemService(Context.AUDIO_SERVICE))
                        .setMode(audioMode);
                audioMode = -1;
            }
            if (null != mp) {
                try {
                    mp.stop();
                    CustomLog.d(TAG, "释放mideaplayer");
                } catch (IllegalStateException e) {
                    CustomLog.e(TAG, "stop mediaplayer error:" + e.getMessage());
                }

                mp.release();
                mp = null;
            }
        } else {


            stopPlay();
        }

//
//		if (null != tts) {
//			tts.stop();
//			tts.shutdown();
//			tts = null;
//		}

    }

    @Override
    protected void onResume() {
        CustomLog.i(TAG, "handle IncomingActivity onResume method");
        mSystemManager.releaseWakeLock();
        mSystemManager.releaseScreenOffWakeLock();
        mSystemManager.acquireWakeLock();
//	    mHandler.sendEmptyMessageDelayed(MSG_HEART,
//				DELAY_UPDATE_SECOND);
        handler.removeMessages(TIMEOUT_STOP);
        CustomLog.i(TAG, "remove TIMEOUT stop");
        super.onResume();
    }

}
