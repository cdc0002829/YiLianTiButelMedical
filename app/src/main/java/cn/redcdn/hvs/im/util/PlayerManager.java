package cn.redcdn.hvs.im.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

import cn.redcdn.hvs.MedicalApplication;

/**
 * Desc
 * Created by wangkai on 2017/5/19.
 */

public class PlayerManager {

    private static PlayerManager playerManager;

    private MediaPlayer mediaPlayer;
    private PlayCallback callback;
    private Context context;
    private String filePath;

    private AudioManager audioManager;

    public static PlayerManager getManager(){
        if (playerManager == null){
            synchronized (PlayerManager.class){
                playerManager = new PlayerManager();
            }
        }
        return playerManager;
    }

    private PlayerManager(){
        this.context = MedicalApplication.getContext();
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 播放回调接口
     */
    public interface PlayCallback{

        /** 音乐准备完毕 */
        void onPrepared();

        /** 音乐播放完成 */
        void onComplete();

        /** 音乐停止播放 */
        void onStop();
    }

    /**
     * 播放音乐
     * @param path 音乐文件路径
     * @param callback 播放回调函数
     */
    public void play(String path, final PlayCallback callback){
        this.filePath = path;
        this.callback = callback;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.parse(path));
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    callback.onPrepared();
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    // 播放完成后，停止播放动画
                    callback.onStop();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stop(){
        if (isPlaying()){
            try {;
                mediaPlayer.stop();
                callback.onStop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否正在播放
     * @return 正在播放返回true,否则返回false
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }


    /**
     * 切换到耳机模式
     */
    public void changeToHeadset(){
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver(){
        if (isPlaying()){
            if (isSamsungPhone() || isHuaweiPhone()){
                stop();
                changeToReceiverNoStop();;
                play(filePath, callback);
            } else {
                changeToReceiverNoStop();
            }
        } else {
            changeToReceiverNoStop();
        }
    }

    /**
     * 切换到外放
     */
    public void changeToSpeaker(){
        if (isPlaying()){
            if (isSamsungPhone() || isHuaweiPhone()){
                stop();
                changeToSpeakerNoStop();
                play(filePath, callback);
            } else {
                changeToSpeakerNoStop();
            }
        } else {
            changeToSpeakerNoStop();
        }
    }

    public void changeToSpeakerNoStop(){
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    public void changeToReceiverNoStop(){
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

    private boolean isSamsungPhone(){
        if(Build.MANUFACTURER.toLowerCase().contains("samsung")){
            return true;
        }
        return false;
    }

    private boolean isHuaweiPhone(){
        if(Build.MANUFACTURER.toLowerCase().contains("huawei")){
            return true;
        }
        return false;
    }
}