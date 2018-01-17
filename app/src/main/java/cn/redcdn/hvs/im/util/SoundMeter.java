package cn.redcdn.hvs.im.util;

import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.log.CustomLog;
import java.io.File;
import java.io.IOException;

public class SoundMeter {

    private final String TAG = "SoundMeter";
    private static final String AUDIO_FILE_SUFFIX = ".acc";

    //    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    //    private double mEMA = 0.0;
    private String rcdFilePath = "";
    //    private Context mContext = null;


    public SoundMeter() {
    }


    public boolean start() {
        CustomLog.d(TAG, "soundMeter start");
        if (mRecorder == null) {
            try {
                mRecorder = new MediaRecorder();
                mRecorder.setOnErrorListener(new OnErrorListener() {
                    @Override
                    public void onError(MediaRecorder mr, int what, int extra) {
                        CustomLog.d(TAG, "SoundMeter mediarecorder onerror:" + what
                            + "|" + extra);
                    }
                });
                mRecorder.setOnInfoListener(new OnInfoListener() {
                    @Override
                    public void onInfo(MediaRecorder mr, int what, int extra) {
                        CustomLog.d(TAG, "SoundMeter mediarecorder onInfo:" + what
                            + "|" + extra);
                    }
                });
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mRecorder.setAudioSamplingRate(8000);
                mRecorder.setAudioEncodingBitRate(16000);
                rcdFilePath = FileTaskManager.getRecordDir() + File.separator +
                    System.currentTimeMillis() + AUDIO_FILE_SUFFIX;
                mRecorder.setOutputFile(rcdFilePath);
                mRecorder.prepare();
                mRecorder.start();
                //                mEMA = 0.0;
            } catch (IllegalStateException e) {
                CustomLog.e(TAG, "IllegalStateException" + e.toString());
                return false;
            } catch (IOException e) {
                CustomLog.e(TAG, "IOException" + e.toString());
                return false;
            } catch (Exception e) {
                CustomLog.e(TAG, "Exception" + e.toString());
                return false;
            }
        }

        return true;
    }


    public void stop() {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
            }
        } catch (Exception e) {
            CustomLog.e(TAG, "SoundMeter stop exception" + e.toString());
        }
        if (mRecorder != null) {
            // 此处只能先reset，不能release
            // release后，会导致权限对话框点击确定或者拒绝后，系统操作底层崩溃
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }


    public String getRcdFilePath() {
        return this.rcdFilePath;
    }

    //    public void pause() {
    //        if (mRecorder != null) {
    //            mRecorder.stop();
    //        }
    //    }
    //
    //    public void start() {
    //        if (mRecorder != null) {
    //            mRecorder.start();
    //        }
    //    }

    //    public double getAmplitude() {
    //        if (mRecorder != null) {
    //        	return (mRecorder.getMaxAmplitude() / 2700.0);
    //        }
    //        else
    //            return 0;
    //
    //    }
    //
    //    public double getAmplitudeEMA() {
    //        double amp = getAmplitude();
    //        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
    //        return mEMA;
    //    }
}
