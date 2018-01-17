package cn.redcdn.hvs.profiles.activity;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by Administrator on 2017/12/28.
 */

public class CheckPermissionUtils {
    private static final String TAG = "CheckPermissionUtils";
    private static CheckPermissionUtils checkPermissionUtils = new CheckPermissionUtils();
    static final int SAMPLE_RATE_IN_HZ = 44100;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
            AudioFormat.ENCODING_PCM_16BIT);
    private AudioRecord mAudioRecord;
    boolean isGetVoiceRun;
    private Object mLock;
    private int count=0;
    private boolean isHasPermission;
    private CheckPermissionUtils() {
        mLock = new Object();
    }


    public static CheckPermissionUtils getinstance() {
        if (checkPermissionUtils == null) {
            checkPermissionUtils = new CheckPermissionUtils();
        }
        return checkPermissionUtils;
    }


    public boolean isHasAudioRecordingPermission(Context context) {
        isHasPermission=false;
        count=0;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null) {
        }
        isGetVoiceRun = true;
        try {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
            mAudioRecord.startRecording();
            short[] buffer = new short[BUFFER_SIZE];
            while (isGetVoiceRun) {

                count++;

                if (count++>10) {
                    isGetVoiceRun=false;
                }
                //r是实际读取的数据长度，一般而言r会小于buffersize
                int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                long v = 0;
                // 将 buffer 内容取出，进行平方和运算
                for (int i = 0; i < buffer.length; i++) {
                    v += buffer[i] * buffer[i];
                }
                // 平方和除以数据总长度，得到音量大小。
                double mean = v / (double) r;
                double volume = 10 * Math.log10(mean);
                if (v>0&&r>0) {

//有录音

                    isHasPermission=true;

                    return isHasPermission;
                }
                // 大概一秒十次
                synchronized (mLock) {
                    try {
                        mLock.wait(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
            //   }
            //}).start();

        } catch (Exception e) {
        }
        return isHasPermission;
    }
}
