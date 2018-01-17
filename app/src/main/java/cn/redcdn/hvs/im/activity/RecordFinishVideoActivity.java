package cn.redcdn.hvs.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.butel.connectevent.utils.CommonUtil;

import java.io.File;
import java.math.BigDecimal;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.manager.FileManager;


/**
 * <dl>
 * <dt>RecordFinishVideoActivity.java</dt>
 * <dd>Description:拍摄视频结果界面</dd>
 * <dd>Copyright: Copyright (C) 2014</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014-3-26 下午2:10:31</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class RecordFinishVideoActivity extends Activity {

    /**
     * 视频文件路径
     */
    public static final String KEY_VIDEO_FILE_PATH = "key_video_file_path";
    /**
     * 视频文件时长
     */
    public static final String KEY_VIDEO_FILE_DURATION = "key_video_file_duration";
    //    /** 视频宽度 */
    //    public static final String KEY_VIDEO_WIDTH = "key_video_width";
    //    /** 视频高度 */
    //    public static final String KEY_VIDEO_HEIGHT = "key_video_height";
    public static final String KEY_VIDEO_ORITEATION = "key_video_oriteation";
    // 视频文件路径
    private String videoFilePath = "";
    // 视频文件时长：秒
    private int duration = 0;

    private static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // logBegin();
        setContentView(R.layout.record_finish_video);

        videoFilePath = getIntent().getStringExtra(KEY_VIDEO_FILE_PATH);
        duration = getIntent().getIntExtra(KEY_VIDEO_FILE_DURATION, 0);

        TextView xsp_cancel_recorded = (TextView) findViewById(R.id.xsp_cancel_recorded);
        xsp_cancel_recorded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doBack();
            }
        });
        TextView xsp_title_middle_text_recorded = (TextView) findViewById(R.id.xsp_title_middle_text_recorded);

        ImageView image = (ImageView) findViewById(R.id.recorded_video);
        ImageView videoIcon = (ImageView) findViewById(R.id.video_icon);
        videoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // logD("点击视频播放，跳转到视频播放界面");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                // 播放视频
                Intent i = new Intent(RecordFinishVideoActivity.this,
                        PlayVideoActivity.class);
                i.putExtra(KEY_VIDEO_FILE_PATH, videoFilePath);
                i.putExtra(KEY_VIDEO_FILE_DURATION, duration);

                startActivity(i);
            }
        });
        final TextView reRecord = (TextView) findViewById(R.id.re_record);
        reRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                reRecord.setTextColor(getResources().getColor(R.color.press_color));
                Log.d(TAG, "进行视频的重新录制");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                AlertDialog.Builder reRecordDlg = new AlertDialog.Builder(
                        RecordFinishVideoActivity.this);
                // reRecordDlg.setTitle("重新拍摄");
                reRecordDlg.setMessage(getString(R.string.reshoot_video));
                reRecordDlg.setNegativeButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                reRecordDlg.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //
                        Intent intent = new Intent();
                        intent.putExtra("cancelType", 1);
                        setResult(RESULT_CANCELED, intent);
                        // 删除.mp4文件
                        deleteMP4File();
                        // 重新录视频
                        finish();

                        //
                    }
                });
                reRecordDlg.show();
            }
        });
        //TextView sizeDuration = (TextView) findViewById(R.id.size_duration);
        final TextView send_recorded = (TextView) findViewById(R.id.send_recorded);
        send_recorded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_recorded.setTextColor(getResources().getColor(R.color.press_color));
                Intent intent = new Intent();
                intent.putExtra("filePath", videoFilePath);
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
        if (!TextUtils.isEmpty(videoFilePath)) {
            Bitmap imageBit = FileManager.createVideoThumbnail(videoFilePath);
            if (imageBit != null) {
                image.setImageBitmap(imageBit);
            }
//            sizeDuration.setText(CommonUtil.secToTime(duration) + "  "
//                + getFileSizeDispStr(new File(videoFilePath)));
            xsp_title_middle_text_recorded.setText(duration + "\'\'");
        }

//        getTitleBar().setBack(null, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                doBack();
//            }
//        });
//        getTitleBar().setTitle("拍摄视频");
//        getTitleBar().enableRightBtn("发送", 0, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG,"视频拍摄完成，点击完成按钮");
//                if (CommonUtil.isFastDoubleClick()) {
//                    return;
//                }
//
//                // 返回拍摄结果
//                //                Intent intent = new Intent();
//                //                intent.putExtra("filePath", videoFilePath);
//                setResult(RESULT_OK, getIntent());
//                finish();
//            }
//        });
    }

    private void doBack() {
        AlertDialog.Builder reRecordDlg = new AlertDialog.Builder(RecordFinishVideoActivity.this);
        // reRecordDlg.setTitle("取消拍摄");
        reRecordDlg.setMessage(getString(R.string.whether_give_up_vedio));
        reRecordDlg.setNegativeButton(getString(R.string.btn_cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        reRecordDlg.setPositiveButton(getString(R.string.btn_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "取消视频拍摄");
                Intent intent = new Intent();
                intent.putExtra("cancelType", 0);
                setResult(RESULT_CANCELED, intent);
                // 删除mp4文件
                deleteMP4File();
                finish();
            }
        });
        reRecordDlg.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * @param file
     * @return
     * @author: zhaguitao
     * @Title: getFileSizeDispStr
     * @Description: 显示视频文件大小
     * @date: 2014-4-1 下午1:52:17
     */
    private String getFileSizeDispStr(File file) {
        Log.d(TAG, "显示视频文件大小");
        if (file == null || !file.exists()) {
            return "0B";
        } else {
            long fileLen = file.length();
            if (fileLen / (1024 * 1024) >= 1) {
                BigDecimal size = new BigDecimal(fileLen * 1.0f / (1024 * 1024));
                return size.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()
                        + "MB";
            } else {
                BigDecimal size = new BigDecimal(fileLen * 1.0f / 1024);
                return size.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue()
                        + "KB";
            }
        }
    }

    /**
     * 放弃本次拍摄时，首先删除本次生成的mp4文件
     */
    private void deleteMP4File() {
        if (!TextUtils.isEmpty(videoFilePath)) {
            File recordedFile = new File(videoFilePath);
            if (recordedFile.exists()) {
                recordedFile.delete();
            }
        }
    }

    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree, boolean fan) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        if (fan) {
            matrix.postScale(-1, 1);
        }
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                    bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

}
