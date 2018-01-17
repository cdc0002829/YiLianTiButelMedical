package cn.redcdn.hvs.profiles.voicedetection;

import android.content.Context;
import android.media.AudioManager;
import android.os.Environment;

import cn.redcdn.log.CustomLog;

/**
 * <dl>
 * <dt>AudioDetectHelper.java</dt>
 * <dd>Description:声音检测帮助类</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 红云融通技术有限公司</dd>
 * <dd>CreateDate: 2014-11-5 下午8:15:19</dd>
 * </dl>
 *
 * @author WX
 */
public class AudioDetectHelper implements IMediaHelperCb{

    public final static int ERR_NOISE_TOO_LOUD = parseAudioDelay.PR_NOISERR;
    public final static int ERR_VOLUME_TOO_LOW = parseAudioDelay.PR_LOWVOL;
    public final static int ERR_FILE_ERROR = parseAudioDelay.PR_FILEERR;
    public final static int ERR_PR_AEC = parseAudioDelay.PR_AEC;

    public final static String RECORD_FILE_SAVE_PATH = Environment
            .getExternalStorageDirectory().getPath() + "/meeting/asyRecord.pcm";

    private static AudioDetectHelper mAudioDetectHelper;

    private AudioDetectCallBack mAudioDetectCallBack;
    // 录音文件存储目录
//	private String mRecordFileSavedDirectory = FileDirectoryHelp.getFileDirectory();

    private AudioManager mAudioManager;
    // 初始音量值
    private int initVolume = 0;

    private Context mContext;

    private AudioDetectHelper(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mContext = context;
    }

    public static AudioDetectHelper getInstance(Context context) {
        if (mAudioDetectHelper == null) {
            mAudioDetectHelper = new AudioDetectHelper(context);
        }

        return mAudioDetectHelper;
    }

    public void release() {
        CustomLog.e("AudioDetectHelper","release");
        MediaHelper3.instance(mContext).release();
        mContext = null;
        mAudioManager = null;
        mAudioDetectCallBack = null;
        mAudioDetectHelper = null;
    }

    public void setAudioDetectCallBack(AudioDetectCallBack callBack) {
        this.mAudioDetectCallBack = callBack;
    }

    /**
     * @Title: setRecordFileSavedDirectory
     * @Description: 设置录音文件存储目录，注意最后面不需要加斜杠“/”
     * @param path
     * @date: 2014-11-5 下午8:22:45
     */
//	public void setRecordFileSavedDirectory(String path) {
//		this.mRecordFileSavedDirectory = path;
//	}

    /**
     * @Title: startRecord
     * @Description: 开始检测
     * @date: 2014-11-5 下午8:14:59
     */
    public void startDetect() {
        // 先录音
        startRecord();
    }

    private void startRecord() {
//		LogUtil.begin();
        setAudioParams();// 打开免提，音量开到最大
        MediaHelper3.instance(mContext).setCallback(this);
        MediaHelper3.instance(mContext).syncPlayAndRecord(RECORD_FILE_SAVE_PATH);
//		LogUtil.end();
    }

    /**
     * @Title: startAnalize
     * @Description: 开始分析录音文件
     * @date: 2014-11-5 下午8:17:47
     */
    private void startAnalize() {
//		LogUtil.begin();
        int ret = parseAudioDelay.parse(RECORD_FILE_SAVE_PATH);
//		LogUtil.d("ret:" + ret);
        CustomLog.i("AudioDetectHelper", "AudioDetectHelper::startAnalize() 声音检测结果ret " + ret);
        if (ret > 0) {// ret为系统时延
            if (mAudioDetectCallBack != null) {
                mAudioDetectCallBack.onDetectSuccess(ret);
//				NetPhoneApplication.getPreference().setKeyValue(PrefType.KEY_ECHO_TAIL, String.valueOf(ret));
//				NetPhoneApplication.getPreference().setKeyValue(PrefType.HAS_AUDIO_DETECTED, "true");
//				// 每次检测成功都上传
//				startUploadRecordFile();
            }
        } else if (ret == parseAudioDelay.PR_NOISERR) {// 噪音过大，选择安静环境重测
            if (mAudioDetectCallBack != null) {
                mAudioDetectCallBack.onDetectFail(ERR_NOISE_TOO_LOUD);
            }
        } else if (ret == parseAudioDelay.PR_LOWVOL) {// 音量过低，调高音量重测
            if (mAudioDetectCallBack != null) {
                mAudioDetectCallBack.onDetectFail(ERR_VOLUME_TOO_LOW);
            }
        } else if (ret == parseAudioDelay.PR_FILEERR) {// 文件错误
            if (mAudioDetectCallBack != null) {
                mAudioDetectCallBack.onDetectFail(ERR_FILE_ERROR);
            }
        } else if (ret == parseAudioDelay.PR_AEC) {// 设备自带回声检测
            if (mAudioDetectCallBack != null) {
                mAudioDetectCallBack.onDetectFail(ERR_PR_AEC);
            }
        }
    }

    private void setAudioParams() {
        // 保存初始音量值，检测结束后根据该值进行恢复
        initVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        // 打开扬声器
        mAudioManager.setSpeakerphoneOn(true);
        // 设置音量值为最大
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                0);
    }

    private void restoreAudioParams() {
        // 关闭扬声器
        mAudioManager.setSpeakerphoneOn(false);
        // 恢复音量为初始值
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, initVolume, 0);
    }

    /**
     * @Title: notifyEvt
     * @Description: 录音完成后的回调接口
     * @param id：目前只会返回synplayrecord_end
     * @param data:暂时不用
     * @date 2014-11-7 上午10:14:28
     */
    @Override
    public void notifyEvt(int id, String data) {
        if (id == synplayrecord_end) {
            // 声音采集完毕，重置音量
            restoreAudioParams();
            // 开始分析
            startAnalize();
        }
    }

    @Override
    public void callBack() {
        // TODO Auto-generated method stub

    }

    public interface AudioDetectCallBack {

        /**
         * @Title: onDetectSuccess
         * @Description: 检测成功回调
         * @param ret ：延时值
         */
        public void onDetectSuccess(int ret);

        /**
         * @Title: onDetectFail
         * @Description: 检测失败回调
         * @param reason ：目前有三种取值，在AudioDetectHelper类中有相应常量定义
         * 	1.ERR_NOISE_TOO_LOUD：噪音太大；2.ERR_VOLUME_TOO_LOW：音量太低；3.ERR_FILE_ERROR：文件错误
         */
        public void onDetectFail(int reason);
    }

    /**
     * @Title: startUploadRecordFile
     * @Description: 上传录音文件及其它信息
     * @date: 2014-11-10 下午3:40:13
     */
    public void startUploadRecordFile() {
//		new UploadRecordFileAsyncTask().execute(RECORD_FILE_SAVE_PATH);
    }


}
