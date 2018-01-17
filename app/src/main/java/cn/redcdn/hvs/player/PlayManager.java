package cn.redcdn.hvs.player;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.WindowManager;


import com.butel.media.ADDatas;
import com.butel.media.X1Player;

import cn.redcdn.log.CustomLog;


/**
 * Created by KevinZhang on 2017/3/11.
 */

public class PlayManager implements
        X1Player.OnLoadingListener, X1Player.OnBufferingUpdateListener, X1Player.OnPlayingUpdateListener,
        X1Player.OnPreparedListener, X1Player.OnCompletionListener, X1Player.OnErrorListener,
        X1Player.OnStopCompleteListener, X1Player.OnExtraDataListener, X1Player.OnSeekCompleteListener,
        X1Player.OnMediaFormatListener {
    private Context mContext;
    private String TAG = getClass().getName();

    private int mVideoType;
    public static int TYPE_VIDEO_VOD = 1;   //点播
    public static int TYPE_VIDEO_LIVE = 2; //直播

    private final static int MSG_LODING_COMPLETE = 1;
    private final static int MSG_PLAYING_UPDATE = 2;
    private final static int MSG_BUFFERING_UPDATE = 3;
    private final static int MSG_SEEK_COMPLETED = 4;
    private final static int MSG_PLAYING_COMPLETED = 5;
    private final static int MSG_STOP_COMPLETED = 6;
    private final static int MSG_PREPARE = 7;
    private final static int MSG_ERROR = 8;
    private final static int MSG_MEDIA_FORMAT = 9;

    private PlayerListener mListener;
    private X1Player mPlayer;
    private PLAY_STATE mState = PLAY_STATE.NULL;

    //设置目标状态，在合适时机进行状态恢复
    //记录seek前的状态，seek完成后恢复状态；切换到后台时，置为暂停状态；
    private PLAY_STATE mRequestState = PLAY_STATE.NULL; //记录seek操作前的状态，由于seek后播放器内部会自动开始播放，根据该状态，恢复到seek前

    public enum PLAY_STATE {
        NULL,
        PREPARE_PLAY,
        PLAYING,
        PAUSE,
        SEEKING,
        BUFFERING,
        STOPPING
    }

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARE:
                    CustomLog.d(TAG, "MSG_PREPARE,width: " + mPlayer.getVideoWidth() + " | height:" + mPlayer.getVideoHeight() + " |state: " + mState + " videoType(1点播，2直播) :" + getVideoType());

                    mPlayer.start(); //准备好数据后统一调用start方法，即使之前是暂停状态
                    setPlayState(PLAY_STATE.PLAYING);

                    //seek后会触发到prepare
                    if (mRequestState == PLAY_STATE.PAUSE) { //目前状态为暂停状态，依然进行暂停
                        CustomLog.d(TAG, "requestState 状态为暂停状态，置位到暂停");
                        pause();
                        markRequestState(PLAY_STATE.NULL);
                    }

                    mListener.onMediaFormat(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
                    mListener.onSeekCompleted();
                    break;
                case MSG_LODING_COMPLETE:
                    CustomLog.d(TAG, "MSG_LODING_COMPLETE: " + msg.arg1);
                    mListener.onLoadingCompleted(msg.arg1);
                    break;
                case MSG_BUFFERING_UPDATE:
                    //TODO 根据buffer更新数据做适当过滤
//                    CustomLog.d(TAG, "MSG_BUFFERING_UPDATE: " + msg.arg1);
                    if (msg.arg1 < 100) {
                        setPlayState(PLAY_STATE.BUFFERING);
                    }
                    mListener.onBufferingUpdate(msg.arg1);
                    break;
                case MSG_PLAYING_UPDATE:
                    mListener.onPlayingUpdate(msg.arg1);
                    break;
                case MSG_SEEK_COMPLETED:
                    CustomLog.d(TAG, "MSG_SEEK_COMPLETED: " + msg.arg1);
                    break;
                case MSG_PLAYING_COMPLETED:
                    CustomLog.d(TAG, "MSG_PLAYING_COMPLETED: " + msg.arg1);
                    mListener.onPlayingCompleted();
                    break;
                case MSG_STOP_COMPLETED:
                    CustomLog.d(TAG, "MSG_STOP_COMPLETED");
                    setPlayState(PLAY_STATE.NULL);
                    mPlayer.release();
                    break;
                case MSG_ERROR:
                    CustomLog.e(TAG, "MSG_ERROR code: " + msg.arg1 + " |info: " + msg.arg2);
                    mListener.onError(msg.arg1, msg.arg2);
                    break;
                case MSG_MEDIA_FORMAT:
                    CustomLog.e(TAG, "MSG_MEDIA_FORMAT width:" + msg.arg1 + " |height: " + msg.arg2);
                    mListener.onMediaFormat(msg.arg1, msg.arg2);
                    break;
            }
        }
    };

    public PlayManager(Context activityContext, PlayerListener listener) {
        mContext = activityContext;
        mListener = listener;

        mPlayer = X1Player.getInstance();
        CustomLog.d(TAG, "PlayManager::PlayManager()" + " |cur state: " + mPlayer.getState());

        if (mPlayer.getState() != X1Player.PS_NONE) {

            (new Thread(new Runnable() {
                @Override
                public void run() {
                    mPlayer.stop();
                }
            })).start();
        }
    }

    /**
     * 【直播&点播】开始播放,当前状态如果播放未停止，直接返回播放失败
     *
     * @param window 播放渲染窗口
     * @param srcUrl 播放地址
     * @param type   播放类型：直播和点播
     * @return -1: 播放失败，0：成功
     */
    public int startPlay(SurfaceHolder window, String srcUrl, int type) {
        if (mState != PLAY_STATE.NULL) {
            CustomLog.e(TAG, "startPlay error! mState != PLAY_STATE.NULL state:" + mState);
            return -1;
        }

        if (window == null || TextUtils.isEmpty(srcUrl)) {
            CustomLog.e(TAG, "startPlay error! window == null || TextUtils.isEmpty(srcUrl) url:" + srcUrl);
            return -1;
        }

        setPlayState(PLAY_STATE.PREPARE_PLAY);
        mVideoType = type;
        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 保持屏幕常亮

        mPlayer.setOnLoadListener(this);
        mPlayer.setOnBufferingUpdateListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnPlayingUpdateListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnStopCompleteListener(this);
        mPlayer.setExtraDataListener(this);
        mPlayer.setOnSeekCompleteListener(this);

        mPlayer.init();

        mPlayer.setDisplay(window);
        mPlayer.setDataSource(srcUrl);

        mPlayer.prepareAsync();
        return 0;
    }

    public int markRequestState(PLAY_STATE state) {
        CustomLog.d(TAG, "PlayManager::markRequestState() :" + state + " |curState: " + getPlayState());
        if (getPlayState() == PLAY_STATE.NULL || getPlayState() == PLAY_STATE.STOPPING) {
            CustomLog.d(TAG, "PlayManager::markRequestState() 当前状态是NULL和STOPING状态，不支持标记状态");
            return -1;
        }
        if (state != PLAY_STATE.NULL && state != PLAY_STATE.PAUSE && state != PLAY_STATE.PLAYING) {
            CustomLog.d(TAG, "PlayManager::markRequestState() 只记录NULL、PAUSE、PLAYING三种可恢复的状态");
            return -1;
        }
        mRequestState = state;
        return 0;
    }

    public PLAY_STATE getRequestState() {
        return mRequestState;
    }

    /**
     * 【直播 & 点播】暂停播放
     *
     * @return -1：失败，0：成功
     */
    public int pause() {
        CustomLog.d(TAG, "PlayManager::pause()");
        if (mState != PLAY_STATE.PLAYING) {
            CustomLog.d(TAG, "PlayManager::pause() 非播放状态 state: " + mState);
            return -1;
        }
        mPlayer.pause();
        setPlayState(PLAY_STATE.PAUSE);
        return 0;
    }

    /**
     * 【直播&点播】播放停止，异步方法，在播放未停止前，不能调用其他接口，同时触发资源释放，下次再播放需要重新start
     *
     * @return -1:失败，0：成功
     */
    public int stop() {
        CustomLog.d(TAG, "PlayManager::stop()");
        if (mState == PLAY_STATE.STOPPING || mState == PLAY_STATE.NULL) {
            CustomLog.e(TAG, "PlayManager::stop() 状态非法，无法调用停止接口 state: " + mState);
            return -1;
        }

        (new Thread(new Runnable() {
            @Override
            public void run() {
                mPlayer.stop();
            }
        })).start();
        setPlayState(PLAY_STATE.STOPPING);
        return 0;
    }

    /**
     * 【点播】继续播放
     *
     * @return -1:失败，0：成功
     */
    public int resume() {
        CustomLog.d(TAG, "PlayManager::resume()");
        if (mState != PLAY_STATE.PAUSE) {
            CustomLog.d(TAG, "PlayManager::resume() 非暂停状态 state: " + mState);
            return -1;
        }

        if (mState == PLAY_STATE.PLAYING) {
            CustomLog.d(TAG, "PlayManager::resume() 已是播放状态 state: " + mState);
            return -2;
        }

        mPlayer.resume();
        setPlayState(PLAY_STATE.PLAYING);
        markRequestState(PLAY_STATE.NULL);
        return 0;
    }

    /**
     * 【直播】重新开始直播播放
     *
     * @return -1:失败，0：成功
     */
    public int restartLive() {
        CustomLog.d(TAG, "PlayManager::restartLive()");
        if (mState != PLAY_STATE.PAUSE) {
            CustomLog.d(TAG, "PlayManager::restartLive() 非暂停状态 state: " + mState);
            return -1;
        }
        mPlayer.restart();
        setPlayState(PLAY_STATE.PLAYING);
        markRequestState(PLAY_STATE.NULL);
        return 0;
    }


    /**
     * 获取当前播放类型：点播或直播
     *
     * @return TYPE_VIDEO_LIVE：直播； TYPE_VIDEO_VOD：点播
     */
    public int getVideoType() {
        if (mPlayer.isLive()) {
            return TYPE_VIDEO_LIVE;
        } else {
            return TYPE_VIDEO_VOD;
        }
    }

    /**
     * 设置到指定位置播放
     *
     * @param pos
     * @return
     */
    public int seekTo(int pos) {
        CustomLog.d(TAG, "PlayManager::seekTo() " + pos);
        if (mState != PLAY_STATE.PLAYING && mState != PLAY_STATE.PAUSE && mState != PLAY_STATE.SEEKING && mState != PLAY_STATE.BUFFERING) {
            CustomLog.d(TAG, "PlayManager::seekTo() 当前状态不支持seek state: " + mState);
            return -1;
        }

        if (mVideoType != TYPE_VIDEO_VOD) {
            CustomLog.d(TAG, "PlayManager::seekTo() 非点播视频！不支持seek");
            return -2;
        }

        mPlayer.seekTo(pos);
        CustomLog.d(TAG, "PlayManager::seekTo() pos"+pos);
        setPlayState(PLAY_STATE.SEEKING);
        return 0;
    }

    public int getDuration() {
        int duration = mPlayer.getDuration();
        CustomLog.d(TAG, "PlayManager::prepareAsync() " + duration);
        return duration;
    }

    private void setPlayState(PLAY_STATE state) {
        CustomLog.d(TAG, "PlayManager::setPlayState() org state: " + mState + " | target state: " + state);
        mState = state;
        if (mState == PLAY_STATE.NULL) {
            markRequestState(PLAY_STATE.NULL);
        }
    }

    public PLAY_STATE getPlayState() {
        return mState;
    }

    @Override
    public void onBufferingUpdate(X1Player x1Player, int i) {
        Message msg = new Message();
        msg.what = MSG_BUFFERING_UPDATE;
        msg.arg1 = i;
        mHandle.sendMessage(msg);
    }

    @Override
    public void onCompletion(X1Player x1Player) {
        Message msg = new Message();
        msg.what = MSG_PLAYING_COMPLETED;
        mHandle.sendMessage(msg);
    }

    @Override
    public boolean onError(X1Player x1Player, int i, int i1) {
        Message msg = new Message();
        msg.what = MSG_ERROR;
        msg.arg1 = i;
        msg.arg2 = i1;
        mHandle.sendMessage(msg);
        return true;
    }

    @Override
    public void onExtraData(X1Player x1Player, byte[] bytes, int i, short i1, short i2, int i3) {

    }

    @Override
    public void onExtraData2(X1Player x1Player, int i, short i1, short i2, int i3, ADDatas adDatas) {

    }





    @Override
    public void onLoading(int i) {
        Message msg = new Message();
        msg.what = MSG_LODING_COMPLETE;
        msg.arg1 = i;
        mHandle.sendMessage(msg);
    }

    @Override
    public void onMediaFormat(int width, int height) {
        Message msg = new Message();
        msg.what = MSG_MEDIA_FORMAT;
        msg.arg1 = width;
        msg.arg2 = height;
        mHandle.sendMessage(msg);
    }

    @Override
    public void onPlayingUpdate(X1Player x1Player, int i) {
        Message msg = new Message();
        msg.what = MSG_PLAYING_UPDATE;
        msg.arg1 = i;
        mHandle.sendMessage(msg);
    }

    @Override
    public void onPrepared(X1Player x1Player) {
        CustomLog.d(TAG, "onPrepared");
        Message msg = new Message();
        msg.what = MSG_PREPARE;
        mHandle.sendMessage(msg);
    }

    @Override
    public void onSeekComplete(X1Player x1Player) {
        Message msg = new Message();
        msg.what = MSG_SEEK_COMPLETED;
        mHandle.sendMessage(msg);
    }

    @Override
    public void onStopCompleteListener(X1Player x1Player) {
        Message msg = new Message();
        msg.what = MSG_STOP_COMPLETED;
        mHandle.sendMessage(msg);
    }

    public interface PlayerListener {
        /**
         * 加载完成：媒体格式已经可以解析、长度、点播直播、编码格式
         *
         * @param state 1:加载成功
         */
        void onLoadingCompleted(int state);

        /**
         * 播放进度更新
         *
         * @param currentPos 当前播放进度
         */
        void onPlayingUpdate(int currentPos);

        /**
         * 缓冲加载进度更新
         *
         * @param percent 进度百分比
         */
        void onBufferingUpdate(int percent);

        /**
         * seek完成
         */
        void onSeekCompleted();

        /**
         * 点播播放完成
         */
        void onPlayingCompleted();

        /**
         * 出错异常
         *
         * @param code  错误码
         * @param extra 额外信息
         */
        void onError(int code, int extra);

        void onMediaFormat(int width, int height);
    }

    /**
     * 获取格式化时间
     *
     * @param time 时间戳，单位毫秒
     * @return hh:mm:ss
     */
    public static String getFormatTime(int time) {
        int hour, min, sec;
        sec = time / 1000;
        hour = sec / 3600; // 计算时 3600进制
        min = (sec % 3600) / 60; // 计算分 60进制
        sec = (sec % 3600) % 60; // 计算秒 余下的全为秒数
        return String.format("%02d:%02d:%02d", hour, min, sec);
    }
    public int getCurrentPosition(){
        return mPlayer.getCurrentPosition();
    }
}
