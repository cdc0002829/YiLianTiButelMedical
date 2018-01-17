package cn.redcdn.hvs.im.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.activity.ViewPhotosActivity;
import cn.redcdn.hvs.im.activity.ViewUDTPhotosActivity;

/**
 * Created by Administrator on 2017/5/23.
 */

public class MyMediaController extends FrameLayout {
    boolean show = false;
    boolean isHiding;
    boolean isShowing;
    private boolean isGoingHide = false;
    private ImageView play1;
    private LinearLayout bottomControllers;
    private LinearLayout topControllers;
    private Animator showAnimator;
    private Animator hideAnimator;
    private static Handler handler = new Handler();
    private TextView totalTimeTv;
    private TextView passedTimeTv;
    private ImageView playButton;
    //静音播放
    private boolean isSilentPlay;
    private VideoView videoView;
    private ImageView cancelImageView;
    private ViewPhotosActivity.CloseCalback closeCallbackListener;
    private ViewUDTPhotosActivity.CloseCalback closeUDTCallbackListener;
    private SeekBar seekBar;
    boolean isPlayingOnDown = false;
    boolean isTouchingPositionSb = false;
    private MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer player) {
            videoView.requestFocus();
            videoView.start();
            seekBar.setMax(videoView.getDuration());
            totalTimeTv.setText(formatDuration(videoView.getDuration()));
            updatePausePauseUI();
            if (isSilentPlay) {
                player.setVolume(0, 0);
            }
        }
    };
    private RelativeLayout controlLayoutRl;

    public String formatDuration(long durationInMs) {
        return String.format("%02d:%02d", durationInMs / 1000 / 60 % 60, durationInMs / 1000 % 60);
    }


    private OnClickListener playPauseOcl = new OnClickListener() {
        @Override
        public void onClick(View v) {
            play1.setVisibility(INVISIBLE);
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }

            updatePausePauseUI();
        }
    };


    private SeekBar.OnSeekBarChangeListener positionSbListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoView.seekTo(progress);
            }
            passedTimeTv.setText(formatDuration(progress));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTouchingPositionSb = true;
            isPlayingOnDown = videoView.isPlaying();
            if (isPlayingOnDown) {
                videoView.pause();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isTouchingPositionSb = false;
            if (isPlayingOnDown) {
                videoView.start();
            }
        }
    };


    private void updatePausePauseUI() {
        if (videoView.isPlaying()) {
            playButton.setImageResource(R.drawable.pause_v);
            handler.post(updatePositionRunnable);
        } else {
            playButton.setImageResource(R.drawable.play_v);
            handler.removeCallbacks(updatePositionRunnable);
        }
    }

    public MyMediaController(Context context) {
        this(context, null);

    }

    public MyMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    GestureDetector mGestureDetector;
    private void init() {
        mGestureDetector = new GestureDetector(getContext(), new MyOnGestureListener());
        View.inflate(getContext(), R.layout.control_layout, this);
        controlLayoutRl = (RelativeLayout) findViewById(R.id.control_layout_rl);
        cancelImageView = (ImageView) findViewById(R.id.cancel_iv);
        playButton = (ImageView) findViewById(R.id.controller_play_iv);
        play1 = (ImageView) findViewById(R.id.play1_iv);
        bottomControllers = (LinearLayout) findViewById(R.id.controller_bottom);
        topControllers = (LinearLayout) findViewById(R.id.controller_top);
        totalTimeTv = (TextView) findViewById(R.id.controller_total_time);
        passedTimeTv = (TextView) findViewById(R.id.controller_passed_time);
//        play1.setClickable(true);
//        play1.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//            }
//        });
        play1.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                videoView.start();
                play1.setVisibility(INVISIBLE);
                updatePausePauseUI();
                bottomControllers.setVisibility(INVISIBLE);
                topControllers.setVisibility(INVISIBLE);
                return true;
            }
        });
        playButton.setOnClickListener(playPauseOcl);
        cancelImageView.setClickable(true);
        cancelImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCallbackListener.close();
            }
        });
        seekBar = (SeekBar) findViewById(R.id.controller_position_sb);
        bottomControllers.setVisibility(INVISIBLE);
        topControllers.setVisibility(INVISIBLE);
        seekBar.setOnSeekBarChangeListener(positionSbListener);
//        controlLayoutRl.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (show) {
//                    show=false;
//                    hide();
//                } else {
//                    show=true;
//                    show();
//                }
//
//            }
//        });
//        controlLayoutRl.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                CustomToast.show(getContext(), "gsdfagag", CustomToast.LENGTH_LONG);
//                return true;
//            }
//        });
    }

    public void setVideoView(VideoView videoView) {
        this.videoView = videoView;
        videoView.setOnPreparedListener(preparedListener);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                play1.setVisibility(VISIBLE);
                playButton.setImageResource(R.drawable.play_v);

            }
        });
    }

    public void setVideoPath(String videoPath, boolean isSilentPlay) {
        this.isSilentPlay = isSilentPlay;
        videoView.setVideoPath(videoPath);
    }

    public void setCloseCallbackListener(ViewPhotosActivity.CloseCalback closeCallbackListener) {
        this.closeCallbackListener = closeCallbackListener;
    }

    public void setCloseCallbackListener(ViewUDTPhotosActivity.CloseCalback closeCallbackListener) {
        this.closeUDTCallbackListener = closeCallbackListener;
    }

    public void onStart() {

//        handler.post(updatePositionRunnable);


    }

    public void onStop() {

        handler.removeCallbacks(updatePositionRunnable);

    }

    private Runnable updatePositionRunnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = videoView.getCurrentPosition();
            if (!isTouchingPositionSb) {
                seekBar.setProgress(currentPosition);
            }
            handler.postDelayed(this, 100);
        }
    };

    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            show();
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            closeCallbackListener.longPress();
            super.onLongPress(e);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                show();
//                break;
            case MotionEvent.ACTION_UP:
                hide();
                break;

        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        bottomControllers.setVisibility(VISIBLE);
        topControllers.setVisibility(VISIBLE);
        return true;
    }


    private void hide() {
        handler.postDelayed(hideRunnable, 3000);
        isGoingHide = true;
    }

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            isGoingHide = false;
            hideAnimator = getHideAnimator();
            hideAnimator.start();
        }
    };

    private void show() {
        if (isGoingHide) {
            handler.removeCallbacks(hideRunnable);
        }
        if (isShowing) {
            return;
        }
        if (isHiding) {
            hideAnimator.cancel();
        }
        showAnimator = getShowAnimator();
        showAnimator.start();
    }

    private Animator getShowAnimator() {
        ObjectAnimator showTopAnimator = ObjectAnimator.ofFloat(topControllers, "translationY", topControllers.getTranslationY(), 0);
        showTopAnimator.setDuration(10);
        ObjectAnimator showBottomAnimator = ObjectAnimator.ofFloat(bottomControllers, "translationY", bottomControllers.getTranslationY(), 0);
        showBottomAnimator.setDuration(10);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(showTopAnimator, showBottomAnimator);
        animatorSet.addListener(showAnimatorLisener);
        return animatorSet;
    }


    private Animator.AnimatorListener showAnimatorLisener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            isShowing = false;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            isShowing = true;
        }
    };

    private Animator.AnimatorListener hideAnimatorLisener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            isHiding = false;
        }

        @Override
        public void onAnimationStart(Animator animation) {
            isHiding = true;
        }
    };

    private Animator getHideAnimator() {
        ObjectAnimator hideTopAnimator = ObjectAnimator.ofFloat(topControllers, "translationY", topControllers.getTranslationY(), -topControllers.getHeight());
        hideTopAnimator.setDuration(10);
        ObjectAnimator hideBottomAnimator = ObjectAnimator.ofFloat(bottomControllers, "translationY", bottomControllers.getTranslationY(), bottomControllers.getHeight());
        hideBottomAnimator.setDuration(10);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(hideTopAnimator, hideBottomAnimator);
        animatorSet.addListener(hideAnimatorLisener);
        return animatorSet;
    }

    public void stopVideoPlay(){
        if(videoView == null){
            return;
        }
        if (videoView.isPlaying()) {
            videoView.pause();
            videoView.seekTo(0);
            play1.setVisibility(VISIBLE);
            playButton.setImageResource(R.drawable.play_v);
        }
    }
}
