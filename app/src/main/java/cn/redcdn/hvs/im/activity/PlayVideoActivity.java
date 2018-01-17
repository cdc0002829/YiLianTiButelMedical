package cn.redcdn.hvs.im.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import java.io.File;

/**
 * <dl>
 * <dt>VideoPlayActivity.java</dt>
 * <dd>Description:简单视频播放界面</dd>
 * <dd>Copyright: Copyright (C) 2014</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014-3-26 下午3:55:43</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class PlayVideoActivity extends BaseActivity {

    // 视频播放控件
    private VideoView videoView;

    // 播放进度条
    private ProgressBar videoProgressBar;
    // 播放进度handler
    private Handler videoProgressHandler;
    // 视频长度
    private int videoDuration;

    //静音播放
    private boolean isSilentPlay;

    //静音播放
    public static final String SILENT_PLAY = "SILENT_PLAY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // logBegin();
        setContentView(R.layout.play_video);

        String videoPath = getIntent().getStringExtra(
            RecordedVideoActivity.KEY_VIDEO_FILE_PATH);
        videoDuration = getIntent().getIntExtra(
            RecordedVideoActivity.KEY_VIDEO_FILE_DURATION, 0);
        isSilentPlay = getIntent().getBooleanExtra(SILENT_PLAY,false);

        videoView = (VideoView) findViewById(R.id.video_view);
        videoProgressBar = (ProgressBar) findViewById(R.id.video_progressbar);
        getTitleBar().enableBack();
        getTitleBar().setTitle(R.string.scan_vedio);
        if (isValidFilePath(videoPath)) {
            videoView.setVideoPath(videoPath);
            // videoView.setMediaController(new MediaController(
            // PlayVideoActivity.this));
            videoView
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        CustomLog.d("TAG","播放完毕，退出播放界面");
                        // 播放完毕，退出播放界面
                        videoProgressBar.setProgress(videoDuration * 1000);
                        finish();
                    }
                });

            //静音播放
            if (isSilentPlay){
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    @Override
                    public void onPrepared(MediaPlayer player) {

                        player.setVolume(0, 0);
                    }
                });
            }

            videoView.requestFocus();
            videoView.start();
            videoProgressBar.setVisibility(View.VISIBLE);
            CustomLog.d(TAG,"duration:" + videoDuration + "s");
            videoProgressBar.setMax(videoDuration * 1000);
            videoProgressBar.setProgress(0);
            videoProgressHandler = new Handler();
            videoProgressHandler.postDelayed(runProgress, 500);
        }else {
            CustomToast.show(getApplicationContext(),getString(R.string.vedio_error_cannot_play), Toast.LENGTH_SHORT);
            CustomLog.d(TAG,"showToast：视频文件出错，无法播放");
        }
        // logEnd();
    }

    private boolean isValidFilePath(String filePath) {
        LogUtil.d("filePath:"+filePath);
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            LogUtil.d("文件不存在");
            return false;
        }
        if (file.length() == 0) {
            if (file.delete()){
                LogUtil.d("删除0B的文件————成功");
            }else {
                LogUtil.d("删除0B的文件————失败");
            }
            return false;
        }
        if (filePath.endsWith(".temp")){
            return false;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (getTitleBar().isShowing()) {
                getTitleBar().hide();
                videoProgressBar.setVisibility(View.GONE);
            } else {
                getTitleBar().show();
                videoProgressBar.setVisibility(View.VISIBLE);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // logBegin();
        finish();
        // logEnd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // logBegin();
        videoView = null;
        if(runProgress != null){
            videoProgressHandler.removeCallbacks(runProgress);
            videoProgressHandler = null;
        }
        // logEnd();
    }

    /**
     * 进度条变化
     */
    private Runnable runProgress = new Runnable() {

        @Override
        public void run() {
            if (videoView != null && videoProgressBar != null
                && videoProgressHandler != null) {
                if (videoView.isPlaying()) {
                    int curPos = videoView.getCurrentPosition();
                    CustomLog.d("TAG","currentPosition:" + curPos + "ms");
                    // 更新进度条
                    videoProgressBar.setProgress(curPos);
                }
                videoProgressHandler.postDelayed(runProgress, 500);
            }
        }
    };
}
