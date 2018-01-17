package cn.redcdn.hvs.profiles.voicedetection;

/**
 * Created by Administrator on 2017/2/27.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.profiles.dialog.SoundCheckDialog;
import cn.redcdn.hvs.profiles.view.RingProgressBar;
import cn.redcdn.log.CustomLog;

/**
 * 声音检测manager
 *
 * @author LeeDong
 *
 */
@SuppressLint({ "InflateParams", "HandlerLeak" })
public final class SoundCheckManager {
    /** 日志 */
    private static final String TAG = SoundCheckManager.class.getName();
    /** 检测声音返回成功消息码 */
    private static final int MSG_SOUND_CHECKED_SUCCESS = 1000;
    /** 检测声音返回失败消息码 */
    private static final int MSG_SOUND_CHECKED_FAILURE = 1001;
    /** 检测声音异常返回消息码 */
    private static final int MSG_SOUND_CHECKED_EXCEPTION = 1002;

    private static final int MSG_SOUND_CHECKING_PROGRESS = 1003;
    /** 单例实例 */
    private static SoundCheckManager sInstance = null;

    private static String FILE_DIR = null;

    private static final String FILE_NAME = "echoconfig.txt";

    /** 当前状态 */
    private CheckState mCheckState;
    /** 当前窗口 */
    private SoundCheckDialog mSoundCheckDialog;
    /** 上下文 */
    private Context mContext;

    /** 声音检测初始界面 */
    private View mContainerInitView;
    /** 声音正在检测界面 */
    private View mContainerCheckingView;
    /** 声音检测成功界面 */
    private View mContainerCheckedSuccessView;
    /** 声音检测失败界面 */
    private View mContainerCheckedFailureView;
    /** 整个页面布局 */
    private View mContainer;
    /** 声音检测界面 开始检测按钮和重新检测按钮响应监听者 */
    private OnCheckSoundListener mOnCheckSoundListener;
    /** 检测窗口消失时候的回调接口 */
    private DialogInterface.OnDismissListener mOnDismissListener;

    private static final int WAIT_TIME_AFTER_SUCCESS = 5;

    private RingProgressBar mRingProgressBar;

    private Timer mSoundCheckingProgressTimer;
    private TimerTask mSoundCheckingProgressTimerTask;

    private Timer mSoundCheckedSuccessTimer;
    private TimerTask mSoundCheckedSuccessTimerTask;
    private TextView mSoundCheckedSuccessTipTextView;

    /** 消息handler */
    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_SOUND_CHECKED_SUCCESS:
                    mCheckState = CheckState.CheckedSuccess;
                    //			MeetingApplication.shareInstance().setVoiceDetectStatus(true);
                    stopCheckingTimer();
                    updateUI();
                    String text = mSoundCheckedSuccessTipTextView.getText()
                            .toString();
                    text = text.replace("$1",
                            " (" + String.valueOf((Integer) msg.obj) + "ms)");
                    mSoundCheckedSuccessTipTextView.setText(text);
                    startCheckedSuccessTimer();
                    break;
                case MSG_SOUND_CHECKED_FAILURE:
                    mCheckState = CheckState.CheckedFailure;
                    //			MeetingApplication.shareInstance().setVoiceDetectStatus(false);
                    stopCheckingTimer();
                    updateUI();
                    break;
                case MSG_SOUND_CHECKED_EXCEPTION:
                    mCheckState = CheckState.CheckedFailure;
                    stopCheckingTimer();
                    updateUI();
                    break;
                default:
                    break;
            }
        };
    };

    /**
     * 私有构造函数
     */
    private SoundCheckManager() {
    }

    /**
     *
     * @return 单例SoundCheckManager
     */
    public synchronized static SoundCheckManager getInstance() {
        if (sInstance == null) {
            initFileDir();
            sInstance = new SoundCheckManager();
        }
        return sInstance;
    }

    public void release(){
        if (mContext != null) {
            AudioDetectHelper.getInstance(mContext).release();
        }

        mContext = null;
        mSoundCheckedSuccessTimer = null;
        mSoundCheckDialog = null;
        mContainerInitView = null;
        mContainerCheckingView = null;
        mContainerCheckedSuccessView = null;
        mContainerCheckedFailureView = null;
        mContainer = null;
        mOnCheckSoundListener = null;
        mOnDismissListener = null;
        mRingProgressBar = null;
        mSoundCheckingProgressTimer = null;
        mSoundCheckingProgressTimerTask = null;
        mSoundCheckedSuccessTimer = null;
        mSoundCheckedSuccessTimerTask = null;
        mSoundCheckedSuccessTipTextView = null;
        sInstance = null;
    }


    private static void initFileDir() {
        if (FILE_DIR == null) {
            String sdcard = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            File dir = new File(sdcard, "meeting/config");
            FILE_DIR = dir.getAbsolutePath();
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    FILE_DIR = null;
                }
            }
        }
    }

    /**
     *
     * @param context
     *            上下文
     * @param mCheckState
     *            当前检测状态
     * @param OnDismissListener
     *            窗口消失时候回调
     */
    public void show(Context context, CheckState mCheckState,
                     DialogInterface.OnDismissListener OnDismissListener) {
        CustomLog.d(TAG, "show mCheckState = " + mCheckState
                + ",OnDismissListener = " + OnDismissListener);
        if (mSoundCheckDialog != null) {
            if (!mSoundCheckDialog.isShowing()) {
                mSoundCheckDialog.show();
            }
            return;
        }
        sInstance.mCheckState = mCheckState == null ? CheckState.INIT
                : mCheckState;
        sInstance.mOnCheckSoundListener = new OnCheckSoundListener();
        this.mOnDismissListener = OnDismissListener;
        mContext = context;
        initUI();
        mSoundCheckDialog = new SoundCheckDialog(mContext,
                R.style.WindowManageDialog) {
            @Override
            public void backPressed() {
                CustomLog.d(TAG, "invoke custom onBackPressed ...");
                onBackKeyPressed();
            }

            @Override
            public void onDismiss(DialogInterface dialog) {
                stopCheckingTimer();
                if (SoundCheckManager.this.mOnDismissListener != null) {
                    CustomLog.d(TAG, "invoke custom onDissmiss ...");
                    SoundCheckManager.this.mOnDismissListener.onDismiss(dialog);
                }
            }
        };
        mSoundCheckDialog.setOnDismissListener(mSoundCheckDialog);
        mSoundCheckDialog.setContentView(mContainer);
        mSoundCheckDialog.setCancelable(false);
        mSoundCheckDialog.setCanceledOnTouchOutside(false);
        mSoundCheckDialog.show();
    }

    /**
     * 删除探测结果的保存文件
     *
     * @return 探测结果文件是否删除成功
     */
    public boolean removeDetectedResultFile() {
        initFileDir();
        File file = new File(FILE_DIR, FILE_NAME);
        if (!file.exists()) {
            return false;
        }
        if (!file.delete()) {
            return false;
        }
        return true;
    }

    /**
     * 把声音探测的结果写入文件
     *
     * @param result
     *            待写入的结果
     * @return 写入是否成功
     */
    public boolean saveDetectedResultToFile(int result) {

        // /mnt/sdcard/meeting/config/echoconfig.txt
        CustomLog.d(TAG, "saveDetectedResultToFile result=" + result);
        initFileDir();
        File file = new File(FILE_DIR, FILE_NAME);
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    return false;
                }
            } catch (IOException e) {
                CustomLog.e(TAG, "Create File:" + file.getAbsolutePath()
                        + " Failure!!!");
            }
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file)));
            bw.write(String.valueOf(result));
            bw.flush();
            CustomLog.d(TAG, "result = " + result
                    + " write into file successfully!");
        } catch (Exception e) {
            CustomLog.e(
                    TAG,
                    "encounter an exception when save detected result: "
                            + e.getMessage());
            return false;
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                    bw = null;
                } catch (IOException e) {
                    CustomLog.e(TAG,
                            "encounter an exception when close stream!");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 窗口显示时候摁返回键的处理函数
     */
    private void onBackKeyPressed() {
        if (mCheckState != CheckState.Checking) {
            stopCheckedSuccessTimer();
            hide();
        }
    }

    /**
     * 隐藏窗口
     */
    public void hide() {
        if (mSoundCheckDialog != null && mSoundCheckDialog.isShowing()) {
            mSoundCheckDialog.dismiss();
            mSoundCheckDialog = null;
            release();
        }
    }

    private void startCheckingTimer() {
        stopCheckingTimer();

        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(mContext,
                R.anim.check_animation);



        mSoundCheckingProgressTimer = new Timer();
        mSoundCheckingProgressTimerTask = new TimerTask() {
            private int counter = 1;

            @Override
            public void run() {
                CustomLog.d(TAG, "mSoundCheckingProgressTimerTask run...");
                Message message = mHandler
                        .obtainMessage(MSG_SOUND_CHECKING_PROGRESS);
                if (counter > mRingProgressBar.getMax()) {
                    CustomLog
                            .e(TAG,
                                    "checking sound consumes more than 10 seconds , directly close the check!!!");
                    mHandler.sendMessage(mHandler
                            .obtainMessage(MSG_SOUND_CHECKED_EXCEPTION));
                    return;
                }
                message.arg1 = counter++;
                mHandler.sendMessage(message);
            }
        };
        mSoundCheckingProgressTimer.schedule(mSoundCheckingProgressTimerTask,
                0, 1000);
    }

    private void stopCheckingTimer() {
        if (mSoundCheckingProgressTimer != null) {
            mSoundCheckingProgressTimer.cancel();
            mSoundCheckingProgressTimer = null;
        }
        if (mSoundCheckingProgressTimerTask != null) {
            mSoundCheckingProgressTimerTask.cancel();
            mSoundCheckingProgressTimerTask = null;
        }
    }

    private void startCheckedSuccessTimer() {
        stopCheckedSuccessTimer();
        mSoundCheckedSuccessTimer = new Timer();
        mSoundCheckedSuccessTimerTask = new TimerTask() {
            private int counter = 1;

            @Override
            public void run() {
                CustomLog.d(TAG, "mSoundCheckedSuccessTimer run...");
                if (counter <= WAIT_TIME_AFTER_SUCCESS) {
                    counter++;
                } else {
                    stopCheckedSuccessTimer();
                    hide();
                }
            }
        };
        mSoundCheckedSuccessTimer.schedule(mSoundCheckedSuccessTimerTask, 0,
                1000);
    }

    private void stopCheckedSuccessTimer() {
        if (mSoundCheckedSuccessTimer != null) {
            mSoundCheckedSuccessTimer.cancel();
            mSoundCheckedSuccessTimer = null;
        }
        if (mSoundCheckedSuccessTimerTask != null) {
            mSoundCheckedSuccessTimerTask.cancel();
            mSoundCheckedSuccessTimerTask = null;
        }
    }

    /**
     * 开始检测函数
     */
    private void startCheckSound() {
        CustomLog.d(TAG, "startCheckSound ...");
        startCheckingTimer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomLog.d(TAG, "startCheckSound thread is running ...");
                try {

                    AudioDetectHelper adh = AudioDetectHelper
                            .getInstance(mContext);
                    adh.setAudioDetectCallBack(new AudioDetectHelper.AudioDetectCallBack() {

                        @Override
                        public void onDetectSuccess(int ret) {
                            CustomLog.d(TAG, "onDetectSuccess ret = " + ret);
                            if (saveDetectedResultToFile(ret)) {
                                CustomLog.d(TAG, "write ret = " + ret
                                        + " to file " + FILE_NAME
                                        + " successfuly!");
                            } else {
                                CustomLog.e(TAG, "write ret = " + ret
                                        + " to file " + FILE_NAME + " fail!");
                            }
                            mHandler.sendMessage(mHandler.obtainMessage(
                                    MSG_SOUND_CHECKED_SUCCESS, ret));
                        }

                        @Override
                        public void onDetectFail(int ret) {
                            CustomLog.e(TAG, "onDetectFail reason = " + ret);
                            if (!removeDetectedResultFile()) {
                                CustomLog.e(TAG, "remove result file:"
                                        + FILE_DIR + "/" + FILE_NAME
                                        + " failre!");
                            } else {
                                CustomLog.d(TAG, "remove result file:"
                                        + FILE_DIR + "/" + FILE_NAME
                                        + " successfully!");
                            }
                            mHandler.sendMessage(mHandler
                                    .obtainMessage(MSG_SOUND_CHECKED_FAILURE));
                        }
                    });
                    adh.startDetect();
                } catch (Exception e) {
                    CustomLog.d(TAG, "when check sound ,catch an exception :"
                            + e.getMessage());
                    if (!removeDetectedResultFile()) {
                        CustomLog.e(TAG, "remove result file:" + FILE_DIR + "/"
                                + FILE_NAME + " failre!");
                    } else {
                        CustomLog.d(TAG, "remove result file:" + FILE_DIR + "/"
                                + FILE_NAME + " successfully!");
                    }
                    mHandler.sendMessage(mHandler
                            .obtainMessage(MSG_SOUND_CHECKED_EXCEPTION));
                }
                CustomLog.d(TAG, "startCheckSound thread is stoped ...");
            }
        }).start();
    }

    /**
     * 界面初始化
     */
    private void initUI() {
//		LayoutInflater li = LayoutInflater.from(mContext);
//		mContainer = li.inflate(R.layout.dialog_soundcheck, null);
//		mContainerInitView = mContainer.findViewById(R.id.sound_check_init);
//		mContainerCheckingView = mContainer
//				.findViewById(R.id.sound_check_checking);
//		mContainerCheckedSuccessView = mContainer
//				.findViewById(R.id.sound_check_checked_success);
//		mContainerCheckedFailureView = mContainer
//				.findViewById(R.id.sound_check_checked_failure);
//
//		mSoundCheckedSuccessTipTextView = (TextView) mContainerCheckedSuccessView
//				.findViewById(R.id.sound_check_checked_success_tv);
//
//		((Button) mContainerInitView.findViewById(R.id.sound_check_start_btn))
//				.setOnClickListener(mOnCheckSoundListener);
//
//		mRingProgressBar = (RingProgressBar) mContainerCheckingView
//				.findViewById(R.id.progressbar);
//
//		((Button) mContainerCheckedFailureView
//				.findViewById(R.id.sound_check_restart_btn))
//				.setOnClickListener(mOnCheckSoundListener);

        updateUI();
    }

    /**
     * 根据当前检测状态更新界面
     */
    private void updateUI() {
        mContainerCheckedFailureView.setVisibility(View.INVISIBLE);
        mContainerCheckedSuccessView.setVisibility(View.INVISIBLE);
        mContainerCheckingView.setVisibility(View.INVISIBLE);
        mContainerInitView.setVisibility(View.INVISIBLE);
        switch (mCheckState) {
            case INIT:
                mContainerInitView.setVisibility(View.VISIBLE);
                break;
            case Checking:
                mContainerCheckingView.setVisibility(View.VISIBLE);
                startCheckSound();
                break;
            case CheckedFailure:
                mContainerCheckedFailureView.setVisibility(View.VISIBLE);
                break;
            case CheckedSuccess:
                mContainerCheckedSuccessView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    /**
     * 检测状态
     *
     * @author LeeDong
     *
     */
    public enum CheckState {
        /** 初始化界面 */
        INIT,
        /** 正在检测界面 */
        Checking,
        /** 检测成功界面 */
        CheckedSuccess,
        /** 检测失败界面 */
        CheckedFailure
    }

    /**
     * 响应界面上点击 开始检测和重新检测listener
     *
     * @author LeeDong
     *
     */
    private class OnCheckSoundListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

            }
        }
    }
}
