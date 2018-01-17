package cn.redcdn.hvs.profiles.collection;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2017/3/8.
 */

public class Player implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {


    public MediaPlayer mediaPlayer; // 媒体播放器
    private SeekBar seekBar; // 拖动条
    private Timer mTimer = new Timer(); // 计时器

    private MediaPlayer getMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        return mediaPlayer;
    }

    // 初始化播放器
    public Player(SeekBar seekBar) {
        super();
        this.seekBar = seekBar;
        try {
            mediaPlayer = getMediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
//            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnPreparedListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 每一秒触发一次
        mTimer.schedule(timerTask, 0, 1000);
    }

    // 计时器
    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            } else {
                if (mediaPlayer.isPlaying() && seekBar.isPressed() == false) {
                    handler.sendEmptyMessage(0); // 发送消息
                }
            }

        }
    };

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int position = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            if (duration > 0) {
                // 计算进度（获取进度条最大刻度*当前音乐播放位置 / 当前音乐时长）
                long pos = seekBar.getMax() * (position + 1000) / duration;
                seekBar.setProgress((int) pos);
            }
        }

        ;
    };

    public void play() {
        mediaPlayer.start();
    }

    /**
     * @param url url地址
     */
    public void playUrl(final String url) {
        try {
            final MediaPlayer mediaPlayer = getMediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url); // 设置数据源

            mediaPlayer.prepareAsync(); // prepare自动播放

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        getMediaPlayer().start();
    }


    // 暂停
    public void pause() {
        getMediaPlayer().pause();
    }

    // 停止
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 播放准备
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    // 播放完成
    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    // s
    public void release() {
        getMediaPlayer().release();
    }

//    /**
//     * 缓冲更新
//     */
//    @Override
//    public void onBufferingUpdate(MediaPlayer mp, int percent) {
////        seekBar.setSecondaryProgress(percent);
//        int currentProgress = seekBar.getMax()
//                * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
//    }


}
