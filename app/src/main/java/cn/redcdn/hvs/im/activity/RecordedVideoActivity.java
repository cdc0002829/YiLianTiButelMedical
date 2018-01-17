package cn.redcdn.hvs.im.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.manager.FileManager;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.CommonUtil;
import java.io.File;
import java.math.BigDecimal;

/**
 * <dl>
 * <dt>RecordedVideoActivity.java</dt>
 * <dd>Description:拍摄视频结果界面</dd>
 * <dd>Copyright: Copyright (C) 2014</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014-3-26 下午2:10:31</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class RecordedVideoActivity extends BaseActivity {

    /** 视频文件路径 */
    public static final String KEY_VIDEO_FILE_PATH = "key_video_file_path";
    /** 视频文件时长 */
    public static final String KEY_VIDEO_FILE_DURATION = "key_video_file_duration";
    //    /** 视频宽度 */
    //    public static final String KEY_VIDEO_WIDTH = "key_video_width";
    //    /** 视频高度 */
    //    public static final String KEY_VIDEO_HEIGHT = "key_video_height";

    // 视频文件路径
    private String videoFilePath = "";
    // 视频文件时长：秒
    private int duration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // logBegin();
        setContentView(R.layout.recorded_video);

        videoFilePath = getIntent().getStringExtra(KEY_VIDEO_FILE_PATH);
        duration = getIntent().getIntExtra(KEY_VIDEO_FILE_DURATION, 0);

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
                Intent i = new Intent(RecordedVideoActivity.this,
                    PlayVideoActivity.class);
                i.putExtra(KEY_VIDEO_FILE_PATH, videoFilePath);
                i.putExtra(KEY_VIDEO_FILE_DURATION, duration);
                startActivity(i);
            }
        });
        Button reRecord = (Button) findViewById(R.id.re_record);
        reRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                CustomLog.d(TAG,"进行视频的重新录制");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                CommonDialog reRecordDlg = new CommonDialog(
                    RecordedVideoActivity.this, getLocalClassName(), 123);
                // reRecordDlg.setTitle("重新拍摄");
                reRecordDlg.setMessage(getString(R.string.reshoot_video));
                reRecordDlg.setCancleButton(null, R.string.btn_cancle);
                reRecordDlg.setPositiveButton(
                    new CommonDialog.BtnClickedListener() {
                        @Override
                        public void onBtnClicked() {
                            Intent intent = new Intent();
                            intent.putExtra("cancelType", 1);
                            setResult(RESULT_CANCELED, intent);
                            // 删除.mp4文件
                            deleteMP4File();
                            // 重新录视频
                            finish();
                        }
                    }, R.string.btn_ok);
                reRecordDlg.showDialog();
            }
        });
        TextView sizeDuration = (TextView) findViewById(R.id.size_duration);

        if (!TextUtils.isEmpty(videoFilePath)) {
            Bitmap imageBit = FileManager.createVideoThumbnail(videoFilePath);
            if (imageBit != null) {
                image.setImageBitmap(imageBit);
            }
            sizeDuration.setText(CommonUtil.secToTime(duration) + "  "
                + getFileSizeDispStr(new File(videoFilePath)));
        }

        getTitleBar().setBack(null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doBack();
            }
        });
        getTitleBar().setTitle(getString(R.string.chat_video_record));
        getTitleBar().enableRightBtn(getString(R.string.btn_send), 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d(TAG,"视频拍摄完成，点击完成按钮");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }

                // 返回拍摄结果
                //                Intent intent = new Intent();
                //                intent.putExtra("filePath", videoFilePath);
                setResult(RESULT_OK, getIntent());
                finish();
            }
        });
    }

    private void doBack() {
        CommonDialog reRecordDlg = new CommonDialog(RecordedVideoActivity.this,
            getLocalClassName(), 124);
        // reRecordDlg.setTitle("取消拍摄");
        reRecordDlg.setMessage(getString(R.string.whether_give_up_vedio));
        reRecordDlg.setCancleButton(null, R.string.btn_cancle);
        reRecordDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {
            @Override
            public void onBtnClicked() {
                // 取消拍摄
                CustomLog.d(TAG,"取消视频拍摄");
                Intent intent = new Intent();
                intent.putExtra("cancelType", 0);
                setResult(RESULT_CANCELED, intent);
                // 删除mp4文件
                deleteMP4File();
                finish();
            }
        }, R.string.btn_ok);
        reRecordDlg.showDialog();
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
     * @author: zhaguitao
     * @Title: getFileSizeDispStr
     * @Description: 显示视频文件大小
     * @param file
     * @return
     * @date: 2014-4-1 下午1:52:17
     */
    private String getFileSizeDispStr(File file) {
        CustomLog.d(TAG,"显示视频文件大小");
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
}
