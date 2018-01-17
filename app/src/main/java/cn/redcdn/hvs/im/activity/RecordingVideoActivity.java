package cn.redcdn.hvs.im.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.util.CameraHelper;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.CommonUtil;
import com.butel.connectevent.utils.LogUtil;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * <dl>
 * <dt>RecordVideoActivity.java</dt>
 * <dd>Description:视频录制界面</dd>
 * <dd>Copyright: Copyright (C) 2014</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014-3-31 下午3:08:16</dd>
 * </dl>
 *
 * @author zhaguitao
 */
public class RecordingVideoActivity extends BaseActivity implements
    OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = "RecordingVideoActivity";

    public static final int REQUEST_CODE_RESULT = 1234;

    /** 默认视频宽度 */
    private static final int DEFAULT_VIDEO_WIDTH = 480;
    /** 默认视频高度 */
    private static final int DEFAULT_VIDEO_HEIGHT = 640;

    /** 视频可录制最大时长：秒 */
    private static final int VIDEO_MAX_DURATION = 30;
    /** 录制倒计时：秒 */
    private static final int REC_COUNTDOWN = 10;

    // 已录制时长
    private TextView recordedTimeTv;
    // 剩余录制时长
    private TextView restTimeTv;
    // 转换摄像头按钮
    private Button changeCameraBtn;
    // 拍摄按钮
    private Button recordBtn;
    private RelativeLayout recordtitle;

    // 显示视频的控件
    private SurfaceView surfaceView;

    // 录制视频的类
    private MediaRecorder mRecorder;
    // Camera
    private Camera mCamera;

    // 摄像头类型：参考Camera.CameraInfo.CAMERA_FACING_BACK，Camera.CameraInfo.CAMERA_FACING_FRONT
    private int cameraType = -1;
    // 视频宽度
    private int videoWidth = DEFAULT_VIDEO_WIDTH;
    // 视频高度
    private int videoHeight = DEFAULT_VIDEO_HEIGHT;
    //
    private boolean isRecording = false;
    // record time handler
    private Handler recordTimeHandler;
    // 已录制时长
    private int recorderedDuration = -1;
    // 录制的视频文件
    private File recordedFile = null;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // logBegin();
        // // 设置全屏
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 拍摄视频时，窗口一直是高亮显示
        this.getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.recording_video);

        recordTimeHandler = new Handler();

        getTitleBar().enableBack();
        getTitleBar().setTitle(R.string.vedio_take);

        recordBtn = (Button) findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(this);
        changeCameraBtn = (Button) findViewById(R.id.change_camera);
        changeCameraBtn.setOnClickListener(this);
        recordedTimeTv = (TextView) findViewById(R.id.recorded_time);
        restTimeTv = (TextView) findViewById(R.id.rest_time);
        recordtitle= (RelativeLayout) findViewById(R.id.recording_video_title);
        surfaceView = (SurfaceView) this.findViewById(R.id.surface_view);
        // 设置该组件让屏幕不会自动关闭
        surfaceView.getHolder().setKeepScreenOn(true);
        // setType
        surfaceView.getHolder()
            .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(this);
        surfaceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomLog.d("TAG","视屏点击事件");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                if (getTitleBar().isShowing()) {
                    getTitleBar().hide();
                    recordtitle.setVisibility(View.VISIBLE);
                } else {
                    getTitleBar().show();
                    recordtitle.setVisibility(View.GONE);
                }
            }
        });

        // logEnd();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomLog.d("TAG","onActivityResult");
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_RESULT) {
                setResult(RESULT_OK, data);
                RecordingVideoActivity.this.finish();
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_CODE_RESULT) {
                int cancelType = data.getIntExtra("cancelType", 0);
                if (cancelType == 1) {
                    surfaceView.setVisibility(View.VISIBLE);
                    // 初始化状态，可再次开始录制
                    recorderedDuration = -1;
                    recordedTimeTv.setVisibility(View.GONE);
                    restTimeTv.setVisibility(View.GONE);
                    changeCameraBtn.setVisibility(View.VISIBLE);
                    recordBtn
                        .setBackgroundResource(R.drawable.btn_video_begin_record_seletor);
                } else {
                    RecordingVideoActivity.this.finish();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomLog.d("TAG","onResume");
    }

    @Override
    protected void onStart() {
        super.onStart();
        CustomLog.d("TAG","onStart");
    }

    @Override
    protected void onPause() {
        CustomLog.d("TAG","onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        CustomLog.d("TAG","onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        CustomLog.d("TAG","onDestroy");
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            cameraType = -1;
        }
        super.onDestroy();
    }

    private void completeRecording(int onSuccess) {
        // logBegin("completeRecording:" + onSuccess);
        if (isRecording) {
            try {
                mRecorder.stop();
            } catch (Exception e) {
                CustomLog.e("mRecorder出现操作异常", String.valueOf(e));
            }

            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;

            recordTimeHandler.removeCallbacks(timeRun);
            isRecording = false;

            CommonUtil.scanFileAsync(RecordingVideoActivity.this,
                recordedFile.getAbsolutePath());

            if (onSuccess == 0) {
                // 跳转到拍摄结果界面
                Intent i = new Intent(RecordingVideoActivity.this,
                    RecordedVideoActivity.class);
                i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_PATH,
                    recordedFile.getAbsolutePath());
                i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_DURATION,
                    recorderedDuration);
                // i.putExtra(RecordedVideoActivity.KEY_VIDEO_WIDTH,
                // videoWidth);
                // i.putExtra(RecordedVideoActivity.KEY_VIDEO_HEIGHT,
                // videoHeight);
                startActivityForResult(i, REQUEST_CODE_RESULT);
                surfaceView.setVisibility(View.GONE);
            } else if (onSuccess == -1) {
                // 结束录制，可再次开始录制
                recorderedDuration = -1;
                recordedTimeTv.setVisibility(View.GONE);
                restTimeTv.setVisibility(View.GONE);
                changeCameraBtn.setVisibility(View.VISIBLE);
                recordBtn
                    .setBackgroundResource(R.drawable.btn_video_begin_record_seletor);
            } else if (onSuccess == -2) {
                // 录制失败，结束录制
                Toast.makeText(RecordingVideoActivity.this,
                    R.string.takevedio_fale, Toast.LENGTH_SHORT).show();
                recorderedDuration = -1;
                finish();
            }
        }
        // logEnd();
    }

    /**
     * 录制过程中,时间变化
     */
    private Runnable timeRun = new Runnable() {

        @Override
        public void run() {
            recorderedDuration++;
            recordedTimeTv.setText(recorderedDuration + "");
            if (recorderedDuration > VIDEO_MAX_DURATION - REC_COUNTDOWN) {
                restTimeTv.setVisibility(View.VISIBLE);
                restTimeTv.setText(getString(R.string.vedio_continue,
                    VIDEO_MAX_DURATION - recorderedDuration));
            }
            if (recorderedDuration >= VIDEO_MAX_DURATION) {

                // 结束录制
                completeRecording(0);

                if (mCamera != null) {
                    mCamera.lock();
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                    cameraType = -1;
                }
            } else {
                recordTimeHandler.postDelayed(timeRun, 1000);
            }
        }
    };

    private static long lastClickTime;

    /**
     * @author: zhaguitao
     * @Title: isFastDoubleClick
     * @Description: 防止按钮快速点击导致多次处理
     * @return
     * @date: 2014-3-13 下午5:31:47
     * @modifyDate:2014-12-17:此处与通用方法略有不同，通用方法中时间为1s.
     *                                                为了防止快速开始，结束时，导致MediaRecorder
     *                                                .stop()方法
     *                                                抛出RuntimeException
     *                                                ，修改时间为1.5s
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 2000) {
            LogUtil.d("快速点击");
            return true;
        }
        lastClickTime = time;
        return false;
    }

    @SuppressLint({ "InlinedApi", "NewApi" })
    @Override
    public void onClick(View v) {
        if (isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.record_btn:
                // 录制视频
                if (!isRecording) {
                    try {
                        if (!CommonUtil.checkExternalStorage(
                            RecordingVideoActivity.this, true)) {
                            return;
                        }
                        if (mCamera == null) {
                            return;
                        }
                        File videoFolder = new File(Environment
                            .getExternalStorageDirectory().getPath()
                            + File.separator
                            + IMConstant.APP_ROOT_FOLDER
                            + File.separator + IMConstant.VIDEO_FOLDER);
                        if (!videoFolder.exists()) {
                            boolean success = videoFolder.mkdirs();
                            if (!success) {
                                Toast.makeText(RecordingVideoActivity.this,
                                    R.string.create_vedio_false, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        // 调用Runable
                        recordTimeHandler.post(timeRun);

                        changeCameraBtn.setVisibility(View.GONE);
                        recordBtn
                            .setBackgroundResource(R.drawable.btn_video_end_record_seletor);

                        // 关闭预览
                        mCamera.stopPreview();

                        // 获取当前时间,作为视频文件的文件名
                        String nowTime = DateUtil
                            .getCurrentTimeSpecifyFormat(DateUtil.FORMAT_YYYYMMDDHHMMSSSSS);
                        // 声明视频文件对象
                        recordedFile = new File(videoFolder, nowTime + ".mp4");
                        // 创建此视频文件
                        recordedFile.createNewFile();
                        Log.d(TAG, "视频文件: " + recordedFile.getAbsolutePath());

                        mRecorder = new MediaRecorder();

                        Camera.Parameters parameters = mCamera.getParameters();
                        // parameters.set("orientation", "portrait");
                        if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            // 设置保存的视频文件竖向播放
                            mRecorder.setOrientationHint(90);
                        } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            // 设置保存的视频文件竖向播放
                            Integer degree = CameraHelper.FRONT_CAMERA_HINT_DEGREE_MAP
                                .get(Build.MODEL.toUpperCase());
                            if (degree != null) {
                                mRecorder.setOrientationHint(degree);
                            } else {
                                mRecorder.setOrientationHint(270);
                            }
                        }

                        mCamera.setParameters(parameters);

                        mCamera.unlock();
                        mRecorder.setCamera(mCamera);
                        // 视频源
                        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        // 录音源为麦克风
                        mRecorder
                            .setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        // 输出格式为mp4
                        mRecorder
                            .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        // 视频尺寸
                        mRecorder.setVideoSize(videoWidth, videoHeight);
                        // 视频帧频率
                        // mailk delete 20140710 此参数不好适配，使用系统默认的值
                        // coolpad 2.3.5 800*480 30s 1.7MB
                        // samaung 4.1.2 800*480 30s 2.2MB
                        // Nexus4 4.4.2 1280*768 30s 2.6MB
                        // 文件大小 和 流畅度 都在范围类
                        // Log.e("",
                        // "getPreviewFrameRate = "+parameters.getPreviewFrameRate());

                        CamcorderProfile lowQualityProf = null;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            lowQualityProf = CamcorderProfile
                                .get(CamcorderProfile.QUALITY_LOW);
                        } else {

                            if (CamcorderProfile
                                .hasProfile(CamcorderProfile.QUALITY_480P)) {
                                lowQualityProf = CamcorderProfile
                                    .get(CamcorderProfile.QUALITY_480P);
                            } else if (CamcorderProfile
                                .hasProfile(CamcorderProfile.QUALITY_CIF)) {
                                lowQualityProf = CamcorderProfile
                                    .get(CamcorderProfile.QUALITY_CIF);
                            } else if (CamcorderProfile
                                .hasProfile(CamcorderProfile.QUALITY_QCIF)) {
                                lowQualityProf = CamcorderProfile
                                    .get(CamcorderProfile.QUALITY_QCIF);
                            } else if (CamcorderProfile
                                .hasProfile(CamcorderProfile.QUALITY_HIGH)) {
                                lowQualityProf = CamcorderProfile
                                    .get(CamcorderProfile.QUALITY_HIGH);
                            } else if (CamcorderProfile
                                .hasProfile(CamcorderProfile.QUALITY_LOW)) {
                                lowQualityProf = CamcorderProfile
                                    .get(CamcorderProfile.QUALITY_LOW);
                            }
                            // else if
                            // (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P))
                            // {
                            // lowQualityProf =
                            // CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                            // }else if
                            // (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P))
                            // {
                            // lowQualityProf =
                            // CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                            // }else if
                            // (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA))
                            // {
                            // lowQualityProf =
                            // CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA);
                            // }else if
                            // (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_2160P))
                            // {
                            // lowQualityProf =
                            // CamcorderProfile.get(CamcorderProfile.QUALITY_2160P);
                            // }

                        }
                        try {
                            if (lowQualityProf != null) {
                                Log.d("RecordingVideoActivity", "videoFrameRate:"
                                    + lowQualityProf.videoFrameRate);
                                mRecorder
                                    .setVideoFrameRate(lowQualityProf.videoFrameRate);
                            }
                        } catch (Exception e) {
                            LogUtil.e("mRecorder.setVideoFrameRate(15)", e);
                        }
                        // 视频编码
                        // 视频编码的比特率，值越大，视频越清晰，不设置的话，视频会很模糊
                        if (lowQualityProf != null) {
                            Log.d("RecordingVideoActivity", "videoBitRate:"
                                + lowQualityProf.videoBitRate);
                            mRecorder
                                .setVideoEncodingBitRate(lowQualityProf.videoBitRate);
                        } else {
                            LogUtil.d(" EncodingBitRate  =  600000  正常的情况这个Case无法运行到");
                            mRecorder.setVideoEncodingBitRate(600000);
                        }
                        /**
                         * 1、H264的编码OPENCORE和最新的stagefright都是支持的，不过都是BASE PROFILE的，
                         * 因为更高的profile编码是要专利费的，所以也就没有实现了
                         *
                         * 2、由于264的算法更加复杂，程序实现烦琐，运行它需要更多的处理器和内存资源。
                         * 在相同的系统下，可能可以跑起四路MPEG4，两路263，却不一定跑得起一路264（当然跟这个程序的效率有关）。
                         * 因此，运行264对系统要求是比较高的。 其次，由于264的实现更加灵活，它把一些实现留给了厂商自己去实现，
                         * 虽然这样给实现带来了很多好处，但是不同产品之间互通成了很大的问题，
                         * 造成了通过A公司的编码器编出的数据，必须通过A公司的解码器去解这样尴尬的事情
                         *
                         * MX2使用H263编码，导致录制的视频花屏，故此处使用264
                         */
                        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                        // 音频编码
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);// TODO:兼容IOS的视频播放

                        // 设置profile了，就不需要设置setOutputFormat，setVideoSize，setVideoEncoder，setVideoEncoder等
                        // 但由于此处需要指定videosize，故不能使用setProfile
                        // mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_CIF));

                        mRecorder.setMaxDuration(30000);
                        mRecorder.setOutputFile(recordedFile.getAbsolutePath());
                        // 预览
                        mRecorder.setPreviewDisplay(surfaceView.getHolder()
                            .getSurface());
                        // 准备录像
                        mRecorder.prepare();
                        // 开始录像
                        mRecorder.start();
                        // 设置文本框可见
                        recordedTimeTv.setVisibility(View.VISIBLE);
                        // 改变录制状态为正在录制
                        isRecording = true;
                    } catch (IOException e) {
                        LogUtil.e(TAG, e);
                        recordFailure();
                    } catch (IllegalStateException e) {
                        LogUtil.e(TAG, e);
                        recordFailure();
                    } catch (Exception e) {
                        LogUtil.e(TAG, e);
                        recordFailure();
                    }
                } else {

                    completeRecording(0);

                    // 停止录制视频
                    if (mCamera != null) {
                        try {
                            mCamera.unlock();
                            mCamera.stopPreview();
                            mCamera.release();
                        } catch (RuntimeException e) {
                            CustomLog.e("isRecording = false e:", String.valueOf(e));
                        } finally {
                            mCamera = null;
                            cameraType = -1;
                        }
                    }
                }
                break;
            case R.id.change_camera:
                // 切换摄像头
                if (mCamera == null || cameraType == -1) {
                    return;
                } else {
                    mCamera.stopPreview();
                    mCamera.release();
                    mCamera = null;
                }
                try {
                    // 开启相机
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    int cameraCount = Camera.getNumberOfCameras();
                    for (int i = 0; i < cameraCount; i++) {
                        Camera.getCameraInfo(i, cameraInfo);
                        if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK
                            && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            // 由后置摄像头转为前置摄像头
                            mCamera = Camera.open(i);
                            if (mCamera != null) {
                                cameraType = Camera.CameraInfo.CAMERA_FACING_FRONT;
                            }
                            break;
                        } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT
                            && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            // 由前置摄像头转为后置摄像头
                            mCamera = Camera.open(i);
                            if (mCamera != null) {
                                cameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
                            }
                            break;
                        }
                    }
                    if (mCamera == null) {
                        mCamera = Camera.open();
                        if (mCamera != null) {
                            cameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
                        }
                    }

                    startCameraPreview(surfaceView.getHolder());
                } catch (Exception e) {
                    LogUtil.e(TAG, e);
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                        cameraType = -1;
                    }
                    Toast.makeText(RecordingVideoActivity.this, R.string.switch_false,
                        Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void recordFailure() {

        completeRecording(-2);

        // 录制失败
        if (mCamera != null) {
            try {
                mCamera.unlock();
                mCamera.stopPreview();
                mCamera.release();
            } catch (RuntimeException e) {
                CustomLog.e("recordFailure e:", String.valueOf(e));
            } finally {
                mCamera = null;
                cameraType = -1;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // logBegin("surfaceCreated...");
        openCamera(holder);
        // logEnd();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        CustomLog.d(TAG,"surfaceChanged...width:" + width + "|height:" + height);
        // LogUtil.d(TAG, "surfaceChanged camera preview-size:"
        // + mCamera.getParameters().getPreviewSize().height + "*"
        // + mCamera.getParameters().getPreviewSize().width);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CustomLog.d(TAG,"surfaceDestroyed...");

        //
        completeRecording(-1);

        // 关闭预览并释放资源
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            cameraType = -1;
        }
    }

    private void openCamera(SurfaceHolder holder) {
        try {
            // 开启相机
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            if (cameraCount >= 2) {
                // 2个及以上摄像头时，可切换摄像头
                changeCameraBtn.setVisibility(View.VISIBLE);
            } else {
                changeCameraBtn.setVisibility(View.GONE);
            }
            for (int i = 0; i < cameraCount; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                // 默认打开后置摄像头
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    mCamera = Camera.open(i);
                    if (mCamera != null) {
                        cameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    break;
                }
            }

            if (mCamera == null) {
                mCamera = Camera.open();
                if (mCamera != null) {
                    cameraType = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
            }

            startCameraPreview(holder);
        } catch (Exception e) {
            CustomLog.e(TAG, String.valueOf(e));
            //TODO 弹出对话框
            // IMCommonUtil.alertPermissionDialog(this, new CommonDialog.BtnClickedListener() {
            //
            //         @Override
            //         public void onBtnClicked() {
            //             // TODO Auto-generated method stub
            //             finish();
            //         }
            //     }, null,
            //     R.string.permission_camera_hint);
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
                cameraType = -1;
            }
            Toast.makeText(RecordingVideoActivity.this, R.string.open_camera_fail,
                Toast.LENGTH_SHORT).show();
        }
    }

    private void startCameraPreview(SurfaceHolder holder) throws IOException {
        if (mCamera != null) {
            // 由于设置显示方向为竖屏，此处需要设置显示角度
            // 某些手机竖屏下CameraInfo.orientation为0不是90（ZTE-N880E），
            // 因此CameraHelper.setCameraDisplayOrientation方法不会进行旋转90度。
            // 但实际上摄像头确实需要旋转90度，故此处暂固定旋转90度
            mCamera.setDisplayOrientation(90);
            // CameraHelper.setCameraDisplayOrientation(
            // RecordingVideoActivity.this, cameraType, mCamera);

            Camera.Parameters parameters = mCamera.getParameters();
            CustomLog.d(TAG,"camera before parameters:" + parameters.flatten());

            parameters.set("orientation", "portrait");

            videoWidth = DEFAULT_VIDEO_WIDTH;
            videoHeight = DEFAULT_VIDEO_HEIGHT;

            // 得到一个最接近默认尺寸的尺寸
            Size approachSize = getOptimalPreviewSize(
                parameters.getSupportedPreviewSizes(), videoWidth,
                videoHeight);
            // Size approachSize = getOptimalPreviewSize(
            // parameters.getSupportedPreviewSizes(), videoHeight,
            // videoWidth);
            if (approachSize.width != videoWidth
                || approachSize.height != videoHeight) {
                videoWidth = approachSize.width;
                videoHeight = approachSize.height;
            }

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView
                .getLayoutParams();
            int screenWidth = IMCommonUtil
                .getDeviceSize(RecordingVideoActivity.this).x;
            int screenHeight = IMCommonUtil
                .getDeviceSize(RecordingVideoActivity.this).y;
            float screenRatio = screenHeight * 1.0f / screenWidth;
            float videoRatio = videoWidth * 1.0f / videoHeight;
            if (videoRatio < screenRatio) {
                // 视频高宽比小于屏幕高宽比，则预览区域（surfaceView）放大到屏幕宽度
                params.width = videoHeight * screenWidth / videoHeight;
                params.height = videoWidth * screenWidth / videoHeight;
            } else {
                // 视频高宽比大于屏幕高宽比，则预览区域（surfaceView）放大到屏幕高度
                params.width = videoHeight * screenHeight / videoWidth;
                params.height = videoWidth * screenHeight / videoWidth;
            }
            // 屏幕为竖屏，显示角度旋转了90度，因此此处需要将宽高对调设置
            surfaceView.setLayoutParams(params);

            CustomLog.d(TAG,"surfaceCreated videoWidth=" + videoWidth + "|videoHeight="
                + videoHeight);
            parameters.setPreviewSize(videoWidth, videoHeight);

            List<String> supportedFocusModes = parameters
                .getSupportedFocusModes();
            if (supportedFocusModes
                .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                // 视频拍摄，设置连续自动对焦
                parameters
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (supportedFocusModes
                .contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // 视频拍摄，设置自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            supportedFocusModes = null;

            parameters.setPreviewFormat(ImageFormat.NV21);
            parameters.setPreviewFrameRate(30);

            // 设置这个参数，会导致界面图像被拉伸或挤压变形
            // if (Build.VERSION.SDK_INT >=
            // Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // // 4.0加入的一个功能
            // // Using this hint can allow MediaRecorder.start() to start
            // faster or with fewer glitches on output
            // // This should be called before starting preview for the best
            // result, but can be changed while the preview is active
            // parameters.setRecordingHint(true);
            // }

            mCamera.setParameters(parameters);
            CustomLog.d(TAG,"camera after parameters:" + mCamera.getParameters().flatten());

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } else {
            Toast.makeText(RecordingVideoActivity.this, R.string.unuse_camera,
                Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @author: zhaguitao
     * @Title: getOptimalPreviewSize
     * @Description: 获取一个与期望宽高相近的尺寸
     * @param sizes
     * @param w
     * @param h
     * @return
     * @date: 2014-3-25 下午5:31:30
     */
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        // 由于摄像头旋转了90度，因此此处需要将目标宽高跟支持宽高对调比较
        int targetHeight = w;
        int targetWidth = h;

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (size.width == targetWidth && size.height == targetHeight) {
                    return size;
                } else if (Math.abs(size.width - targetWidth) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - targetWidth);
                }
            }
        }
        return optimalSize;
    }
}
