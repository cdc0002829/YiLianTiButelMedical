package cn.redcdn.hvs.profiles.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.profiles.view.SlideSwitch;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

/**
 * Created by Administrator on 2017/2/24.
 */

public class SettingActivity extends BaseActivity {

    private SlideSwitch meetContro;

    private SlideSwitch webContro;

    private SlideSwitch downloadContro;


    private RelativeLayout voice_detect_rl;

    private RelativeLayout setResolutionRl;

    private TextView voice_detect_result;


    private TextView tvMeetingSettingTitle;

    private RelativeLayout rlAutoSpeakSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initWidget();
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.my_setting));
        titleBar.enableBack();
    }

    private void initWidget() {

        meetContro = (SlideSwitch) findViewById(R.id.set_meet);
        voice_detect_result = (TextView) findViewById(R.id.voice_detect_result);
        webContro = (SlideSwitch) findViewById(R.id.set_web);
        downloadContro = (SlideSwitch) findViewById(R.id.set_download);
        voice_detect_rl = (RelativeLayout) findViewById(R.id.goto_voice_detect_rl);
        setResolutionRl = (RelativeLayout) findViewById(R.id.set_resolution_rl);
        voice_detect_rl.setOnClickListener(mbtnHandleEventListener);
        meetContro.SetOnChangedListener(new SlideSwitch.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
                changemeetSettingStats(MedicalApplication.shareInstance().getMeetingSetting());
            }
        });

        webContro.SetOnChangedListener(new SlideSwitch.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
                changeWebSettingStats(MedicalApplication.shareInstance().getWebSetting());
            }
        });
        downloadContro.SetOnChangedListener(new SlideSwitch.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
                changeDownloadSettingStats(MedicalApplication.shareInstance().getDownloadSetting());
            }
        });
        setResolutionRl.setOnClickListener(mbtnHandleEventListener);
        if (MedicalApplication.shareInstance().getMeetingSetting()) {
            meetContro.setChecked(true);
        } else {
            meetContro.setChecked(false);
        }

        if (MedicalApplication.shareInstance().getWebSetting()) {
            webContro.setChecked(true);
        } else {
            webContro.setChecked(false);
        }
        if (MedicalApplication.shareInstance().getDownloadSetting()) {
            downloadContro.setChecked(true);
        } else {
            downloadContro.setChecked(false);
        }
        SharedPreferences sharedPreferences = getSharedPreferences("VDS", Activity.MODE_PRIVATE);
        String voiceDetectStatus = sharedPreferences.getString("vds", "");
        int hasVoiceDetect = sharedPreferences.getInt("hasVoiceDetect", 0);
        System.out.println("vds: " + voiceDetectStatus + "  hasVoiceDetect: " + hasVoiceDetect);
        if (voiceDetectStatus.equals("") || voiceDetectStatus.equals(getString(R.string.deny))) {
            voice_detect_result.setVisibility(View.VISIBLE);

        } else {
            voice_detect_result.setTextColor(0xFF3CB744);
            voice_detect_result.setText(voiceDetectStatus);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.goto_voice_detect_rl:
                CustomLog.d(TAG, "开启声音检测");
                File mfile = new File(Environment.getExternalStorageDirectory().getPath() + "/meeting/asyRecord.pcm");
                if (mfile.exists()) {
                    CustomLog.d(TAG, "asyRecord.pcm存在");
                    if (mfile.isFile()) {
                        mfile.delete();
                        CustomLog.d(TAG, "asyRecord.pcm删除");
                    }
                } else {
                    CustomLog.d(TAG, "asyRecord.pcm不存在");
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    boolean result = CommonUtil.selfPermissionGranted(SettingActivity.this, Manifest.permission.RECORD_AUDIO);
                    if (!result) {
                        PermissionGen.with(SettingActivity.this)
                                .addRequestCode(100)
                                .permissions(Manifest.permission.RECORD_AUDIO)
                                .request();
                    } else {
                        Intent in = new Intent();
                        in.setClass(SettingActivity.this, VoiceDetectActivity.class);
                        startActivity(in);
                        SettingActivity.this.finish();
                    }
                } else {
                    boolean hasPermission = CheckPermissionUtils.getinstance().isHasAudioRecordingPermission(SettingActivity.this);
                    if (hasPermission) {
                        Intent in = new Intent();
                        in.setClass(SettingActivity.this, VoiceDetectActivity.class);
                        startActivity(in);
                        SettingActivity.this.finish();
                    } else {
                        openAppDetails(getString(R.string.no_voice_permission));
                    }
                }


                break;
            case R.id.set_resolution_rl:
                boolean result =isHasPermission(SettingActivity.this);
                if (!result) {
                    // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionGen.with(SettingActivity.this)
                                .addRequestCode(200)
                                .permissions(Manifest.permission.CAMERA)
                                .request();
                    } else {
                        openAppDetails(getString(R.string.no_photo_permission));
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setClass(SettingActivity.this, SettingResolutionActivity.class);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }


    // 音频获取源
    public int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    public int sampleRateInHz = 44100;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    public int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    public int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    public int bufferSizeInBytes = 0;

    /**
     * 判断是是否有录音权限
     */
    public boolean isHasPermission(Context context) {
        bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        //开始录制音频
        try {
            // 防止某些手机崩溃，例如联想
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        /**
         * 根据开始录音判断是否有录音权限
         */
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            return false;
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

        return true;
    }


    private void changeWebSettingStats(boolean open) {
        if (open) {
            webContro.setChecked(false);
            saveSetting("webSetting", false);
        } else {
            webContro.setChecked(true);
            saveSetting("webSetting", true);
        }
    }

    private void saveSetting(String setting, boolean isOpen) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("setting", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(setting, isOpen);
        editor.commit();
//        CustomToast.show(this,"isopen="+isOpen,7000);
        MedicalMeetingManage.getInstance().setIsAllowNetJoinMeeting(isOpen);
    }

    private void changeDownloadSettingStats(boolean open) {
        if (open) {
            downloadContro.setChecked(false);
            saveSetting("downloadSetting", false);
        } else {
            downloadContro.setChecked(true);
            saveSetting("downloadSetting", true);
        }
    }

    private void changemeetSettingStats(boolean open) {
        if (open) {
            meetContro.setChecked(false);
            saveSetting("meetingSetting", false);
        } else {
            meetContro.setChecked(true);
            saveSetting("meetingSetting", true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = 100)
    public void openAudioSuccess() {
        Intent in = new Intent();
        in.setClass(SettingActivity.this, VoiceDetectActivity.class);
        startActivity(in);
        SettingActivity.this.finish();
    }

    @PermissionFail(requestCode = 100)
    public void openAudioFail() {
        openAppDetails(getString(R.string.no_voice_permission));
    }

    @PermissionSuccess(requestCode = 200)
    public void openCameraSuccess() {
        Intent intent = new Intent();
        intent.setClass(SettingActivity.this, SettingResolutionActivity.class);
        startActivity(intent);
    }

    @PermissionFail(requestCode = 200)
    public void openCameraFail() {
        openAppDetails(getString(R.string.no_photo_permission));
    }

    private void openAppDetails(String tip) {
        final CustomDialog dialog = new CustomDialog(SettingActivity.this);
        dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dialog.dismiss();
            }
        });
        dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + SettingActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    CustomLog.d(TAG, "跳转到设置权限界面异常 Exception：" + ex.getMessage());
                }
            }
        });
        dialog.setTip(tip + getString(R.string.permission_setting));
        dialog.setCenterBtnText(getString(R.string.iknow));
        dialog.setOkBtnText(getString(R.string.permission_handsetting));
        dialog.show();
    }
}
