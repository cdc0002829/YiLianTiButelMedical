package cn.redcdn.hvs.im.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.util.CameraHelper;
import cn.redcdn.hvs.im.util.SendCIVMUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.CommonUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static java.lang.System.currentTimeMillis;

public class RecordingVideoAndPictureActivity extends Activity implements
    OnClickListener, SurfaceHolder.Callback {

    private static final String TAG = RecordingVideoAndPictureActivity.class.getSimpleName();

    private static final int REC_COUNTDOWN = 10;//录制倒计时 :秒
    private static final int VIDEO_MAX_DURATION = 30;//视频可录制最大时长：秒
    private static final int DEFAULT_VIDEO_WIDTH = 720;// 默认视频宽度
    public static final int REQUEST_CODE_RESULT = 1234;
    private static final int DEFAULT_VIDEO_HEIGHT = 1280;//默认视频高度
    private TextView xsp_cancel; //取消录制
    private TextView xsp_title_middle_text;//小视频标题的中间文字
    private Button changeCameraBtn;  // 转换摄像头按钮
    private Button recordBtn;// 拍摄按钮
    private RelativeLayout recordtitle;
    private SurfaceView surfaceView;  // 显示视频的控件
    private MediaRecorder mRecorder;// 录制视频的类
    private Camera mCamera; // Camera
    private int cameraType = -1;
    // 摄像头类型：参考Camera.CameraInfo.CAMERA_FACING_BACK，Camera.CameraInfo.CAMERA_FACING_FRONT
    private int videoWidth = DEFAULT_VIDEO_WIDTH;// 视频宽度
    private int videoHeight = DEFAULT_VIDEO_HEIGHT;// 视频高度
    private boolean isRecording = false;
    private Handler recordTimeHandler; // record time handler
    private int recorderedDuration = -1;// 已录制时长
    private File recordedFile = null; // 录制的视频文件
    private static Point deviceSize = null;

    float x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    RelativeLayout layout;
    TextView picText, videoText;
    private int recordType = 0; //拍照?小视频
    byte[] picdata;
    TextView rePicture, sendPicture;//重新拍摄,发送图片
    ImageView previewImage = null;

    boolean atRepicture = false;
    int re_btn_width;
    String where = "";//拍照?小视频拍摄
    String videoCachePath = "";//视频路径
    boolean isInterrept = false;
    private OrientationEventListener mScreenOrientationEventListener;
    private int mScreenExifOrientation = 0;
    private int shutterOrientation = 0;
    private RelativeLayout previewRl;
    private Bitmap bmp;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.i(TAG, "onCreate()");

        setContentView(R.layout.recording_video_picture);

        where = getIntent().getExtras().getString("where");
        // 拍摄视频时，窗口一直是高亮显示
        this.getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        layout = (RelativeLayout) findViewById(R.id.layout_switch);
        picText = (TextView) findViewById(R.id.picture_text);
        picText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                youhua();
            }
        });
        videoText = (TextView) findViewById(R.id.video_text);
        videoText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                zuohua();
            }
        });

        previewImage = (ImageView) findViewById(R.id.preview_image);
        previewRl = (RelativeLayout) findViewById(R.id.preview_ll);
        rePicture = (TextView) findViewById(R.id.re_picture);//重新拍摄
        sendPicture = (TextView) findViewById(R.id.send_picture);//发送照片
        rePicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //surfaceView.setVisibility(View.VISIBLE);
                previewRl.setVisibility(View.GONE);
                previewImage.setVisibility(View.GONE);
                mCamera.startPreview();
                rePicture.setVisibility(View.GONE);
                sendPicture.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
                atRepicture = false;
                recordBtn.setVisibility(View.VISIBLE);
            }
        });
        sendPicture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (picdata != null && picdata.length != 0) {
                    bmp = getBitmap();

                    File file = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath() +
                            File.separator + "Medical/takePhoto/",
                        java.lang.System.currentTimeMillis() + ".jpg");
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs(); // 创建文件夹
                    }
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(
                            new FileOutputStream(file));
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, bos); // 向缓冲区之中压缩图片
                        bos.flush();
                        bos.close();
                    } catch (Exception e) {
                    }

                    SendCIVMUtil.cameraFilePath = file.getAbsolutePath();
                    Intent data = new Intent();
                    data.putExtra("OK_TYPE", 0);
                    setResult(RESULT_OK, data);
                }
                RecordingVideoAndPictureActivity.this.finish();
            }
        });

        recordTimeHandler = new Handler();

        recordBtn = (Button) findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(this);
        changeCameraBtn = (Button) findViewById(R.id.change_camera);
        changeCameraBtn.setOnClickListener(this);
        recordtitle = (RelativeLayout) findViewById(R.id.recording_video_title);

        xsp_cancel = (TextView) findViewById(R.id.xsp_cancel);
        xsp_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //RecordingVideoAndPictureActivity.this.finish();
                doBack();
            }
        });
        xsp_title_middle_text = (TextView) findViewById(R.id.xsp_title_middle_text);

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

            }
        });
        surfaceView.setOnTouchListener(new MyOnTouchListener());
        ImageView imageView8 = (ImageView) findViewById(R.id.imageView8);
        imageView8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        recordtitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        System.out.println("where=" + where);
        CustomLog.d(TAG, "where:" + where);
        getOrientation();
    }


    private Bitmap getBitmap() {
        CustomLog.i(TAG, "getBitmap()");

        Bitmap bmp = BitmapFactory.decodeByteArray(picdata, 0, picdata.length);
        if (shutterOrientation == 90) {
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bmp = rotateBitmapByDegree(bmp, 90, false);
            } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                bmp = rotateBitmapByDegree(bmp, -90, true);
            } else {
            }
        } else if (shutterOrientation == 270) {
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bmp = rotateBitmapByDegree(bmp, 270, false);
            } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                bmp = rotateBitmapByDegree(bmp, -270, true);
            } else {
            }
        } else if (shutterOrientation == 180) {
            if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                bmp = rotateBitmapByDegree(bmp, 180, false);
            } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                bmp = rotateBitmapByDegree(bmp, -180, true);
            } else {
            }
        } else {
            CustomLog.d(TAG, "横屏方向，不需要修改图片方向");
        }
        return bmp;
    }


    private void getOrientation() {
        CustomLog.i(TAG, "getOrientation()");

        mScreenOrientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {
                // i的范围是0～359
                // 屏幕左边在顶部的时候 i = 90;
                // 屏幕顶部在底部的时候 i = 180;
                // 屏幕右边在底部的时候 i = 270;
                // 正常情况默认i = 0;

                if (45 <= i && i < 135) {
                    mScreenExifOrientation = 180;
                } else if (135 <= i && i < 225) {
                    mScreenExifOrientation = 270;
                } else if (225 <= i && i < 315) {
                    mScreenExifOrientation = 0;
                } else {
                    mScreenExifOrientation = 90;
                }
            }
        };
        mScreenOrientationEventListener.enable();

    }


    class MyOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //继承了Activity的onTouchEvent方法，直接监听点击事件
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //当手指按下的时候
                x1 = event.getX();
                y1 = event.getY();
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                //当手指离开的时候
                x2 = event.getX();
                y2 = event.getY();
                if (x1 - x2 > 100) {
                    if (!atRepicture && !isRecording) {
                        zuohua();
                    }
                } else if (x2 - x1 > 100) {
                    if (!atRepicture && !isRecording) {
                        youhua();
                    }
                }
            }
            return onTouchEvent(event);
        }
    }


    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree, boolean fan) {
        CustomLog.i(TAG, "rotateBitmapByDegree()");

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


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        CustomLog.i(TAG, "windowsFocusChanged()");

        int tw = picText.getWidth();
        int ttw = videoText.getWidth();
        re_btn_width = recordBtn.getWidth();
        RelativeLayout.LayoutParams layoutParams
            = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.width = re_btn_width * 2;
        layout.setLayoutParams(layoutParams);
        layout.setPadding(re_btn_width / 2 - tw / 2, 0, re_btn_width / 2 - ttw / 2, 5);

        if ("video".equals(where)) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(layout, "TranslationX", 0,
                -1 * re_btn_width);
            animator.setDuration(1);
            animator.start();
            videoText.setTextColor(Color.YELLOW);
            picText.setTextColor(Color.WHITE);
            xsp_title_middle_text.setVisibility(View.VISIBLE);
            recordBtn.setBackgroundResource(R.drawable.btn_video_begin_record_seletor);
            recordType = 1;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomLog.i(TAG, "onActivityResult()");

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_RESULT) {
                data.putExtra("OK_TYPE", 1);
                setResult(RESULT_OK, data);
                RecordingVideoAndPictureActivity.this.finish();
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_CODE_RESULT) {
                int cancelType = data.getIntExtra("cancelType", 0);
                if (cancelType == 1) {
                    layout.setVisibility(View.VISIBLE);
                    surfaceView.setVisibility(View.VISIBLE);
                    // 初始化状态，可再次开始录制
                    recorderedDuration = -1;
                    changeCameraBtn.setVisibility(View.VISIBLE);
                    xsp_title_middle_text.setText(R.string.recording_video_title);
                    xsp_title_middle_text.setCompoundDrawables(null, null, null, null);
                    recordBtn.setBackgroundResource(R.drawable.btn_video_begin_record_seletor);
                } else {
                    RecordingVideoAndPictureActivity.this.finish();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        CustomLog.i(TAG, "onResume()");

        if (isInterrept) {
            Intent i = new Intent(RecordingVideoAndPictureActivity.this,
                RecordFinishVideoActivity.class);
            i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_PATH,
                recordedFile.getAbsolutePath());
            i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_DURATION,
                recorderedDuration);
            startActivityForResult(i, REQUEST_CODE_RESULT);
            surfaceView.setVisibility(View.GONE);
            isInterrept = false;
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onPause() {
        super.onPause();
        CustomLog.d(TAG, "onPause");
        if (isRecording) {
            isInterrept = true;
            try {
                mRecorder.stop();
            } catch (Exception e) {
            }

            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;

            recordTimeHandler.removeCallbacks(timeRun);
            isRecording = false;

            CommonUtil.scanFileAsync(RecordingVideoAndPictureActivity.this,
                recordedFile.getAbsolutePath());
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        CustomLog.i(TAG, "onStop");
    }


    @Override
    protected void onDestroy() {
        CustomLog.i(TAG, "onDestroy");
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
        mScreenOrientationEventListener.disable();
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void completeRecording(int onSuccess) {
        CustomLog.i(TAG, "completeRecording()");

        if (isRecording) {
            try {
                mRecorder.stop();
            } catch (Exception e) {
            }

            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;

            recordTimeHandler.removeCallbacks(timeRun);
            isRecording = false;

            CommonUtil.scanFileAsync(RecordingVideoAndPictureActivity.this,
                recordedFile.getAbsolutePath());

            if (onSuccess == 0) {
                // 跳转到拍摄结果界面
                Intent i = new Intent(RecordingVideoAndPictureActivity.this,
                    RecordFinishVideoActivity.class);
                i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_PATH,
                    recordedFile.getAbsolutePath());
                i.putExtra(RecordedVideoActivity.KEY_VIDEO_FILE_DURATION,
                    recorderedDuration);
                startActivityForResult(i, REQUEST_CODE_RESULT);
                surfaceView.setVisibility(View.GONE);
            } else if (onSuccess == -1) {
                // 结束录制，可再次开始录制
                recorderedDuration = -1;
                changeCameraBtn.setVisibility(View.VISIBLE);
                recordBtn
                    .setBackgroundResource(R.drawable.btn_video_begin_record_seletor);
            } else if (onSuccess == -2) {
                // 录制失败，结束录制
                Toast.makeText(RecordingVideoAndPictureActivity.this,
                    R.string.recorde_fail, Toast.LENGTH_SHORT).show();
                recorderedDuration = -1;
                finish();
            }
        }
    }


    /**
     * 录制过程中,时间变化
     */
    private Runnable timeRun = new Runnable() {

        @Override
        public void run() {
            recorderedDuration++;
            Drawable drawable = getResources().getDrawable(R.drawable.djshd);
            drawable.setBounds(0, 0, 15, 15);
            xsp_title_middle_text.setCompoundDrawables(drawable, null, null, null);
            xsp_title_middle_text.setCompoundDrawablePadding(6);
            xsp_title_middle_text.setText((VIDEO_MAX_DURATION - recorderedDuration + "\'\'"));
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
     * @date: 2014-3-13 下午5:31:47
     * @modifyDate:2014-12-17:此处与通用方法略有不同，通用方法中时间为1s. 为了防止快速开始，结束时，导致MediaRecorder
     * .stop()方法
     * 抛出RuntimeException
     * ，修改时间为1.5s
     */
    public static boolean isFastDoubleClick() {
        CustomLog.i(TAG, "isFastDoubleClick()");

        long time = currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 2000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    private void startRecordVideo() {
        CustomLog.i(TAG, "startRecordVideo()");

        if (recordType == 0) {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    shutterOrientation = mScreenExifOrientation;
                    picdata = bytes;
                    camera.stopPreview();
                    bmp = getBitmap();
                    previewImage.setVisibility(View.VISIBLE);
                    previewRl.setVisibility(View.VISIBLE);
                    previewImage.setImageBitmap(bmp);

                }
            });
            rePicture.setVisibility(View.VISIBLE);
            sendPicture.setVisibility(View.VISIBLE);
            layout.setVisibility(View.INVISIBLE);
            atRepicture = true;
            recordBtn.setVisibility(View.INVISIBLE);
        } else {
            // 录制视频
            if (!isRecording) {
                layout.setVisibility(View.INVISIBLE);
                try {
                    if (!CommonUtil.checkExternalStorage(
                        RecordingVideoAndPictureActivity.this, true)) {
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
                            Toast.makeText(RecordingVideoAndPictureActivity.this,
                                R.string.create_camera_fail, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    // 调用Runable
                    recordTimeHandler.post(timeRun);

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
                    videoCachePath = recordedFile.getAbsolutePath();
                    Log.d(TAG, "视频文件: " + recordedFile.getAbsolutePath());

                    mRecorder = new MediaRecorder();

                    Camera.Parameters parameters = mCamera.getParameters();
                    // parameters.set("orientation", "portrait");
                    if (mScreenExifOrientation == 90) {
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
                    } else if (mScreenExifOrientation == 270) {
                        if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            mRecorder.setOrientationHint(270);
                        } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            mRecorder.setOrientationHint(90);
                        } else {
                        }
                    } else if (mScreenExifOrientation == 180) {
                        if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            mRecorder.setOrientationHint(180);
                        } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            mRecorder.setOrientationHint(180);
                        } else {

                        }
                    } else {
                        if (cameraType == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            ;
                        } else if (cameraType == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                        } else {

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

                    CamcorderProfile lowQualityProf = null;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        lowQualityProf = CamcorderProfile
                            .get(CamcorderProfile.QUALITY_LOW);
                    } else {

                        if (CamcorderProfile
                            .hasProfile(CamcorderProfile.QUALITY_720P)) {
                            lowQualityProf = CamcorderProfile
                                .get(CamcorderProfile.QUALITY_720P);
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
                        } else if
                            (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
                            lowQualityProf =
                                CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                        } else if
                            (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
                            lowQualityProf =
                                CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                        } else if
                            (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
                            lowQualityProf =
                                CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA);
                        } else if
                            (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_2160P)) {
                            lowQualityProf =
                                CamcorderProfile.get(CamcorderProfile.QUALITY_2160P);
                        }

                    }

                    try {
                        if (lowQualityProf != null) {
                            CustomLog.d("", "videoFrameRate:"
                                + lowQualityProf.videoFrameRate);
                            mRecorder
                                .setVideoFrameRate(lowQualityProf.videoFrameRate);
                        }
                    } catch (Exception e) {
                        CustomLog.e(TAG, e.toString());
                    }
                    // 视频编码
                    // 视频编码的比特率，值越大，视频越清晰，不设置的话，视频会很模糊
                    mRecorder.setVideoEncodingBitRate(1000000);
                    mRecorder.setAudioEncodingBitRate(1000000);
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
                    mRecorder.setAudioEncoder(
                        MediaRecorder.AudioEncoder.AAC);// TODO:兼容IOS的视频播放

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
                    //  recordedTimeTv.setVisibility(View.VISIBLE);

                    //设置转换按钮不可见
                    changeCameraBtn.setVisibility(View.GONE);
                    // 改变录制状态为正在录制
                    isRecording = true;
                } catch (IOException e) {
                    recordFailure();
                    CustomLog.e(TAG, e.toString());
                } catch (IllegalStateException e) {
                    recordFailure();
                    CustomLog.e(TAG, e.toString());
                } catch (Exception e) {
                    recordFailure();
                    CustomLog.e(TAG, e.toString());
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
                        Log.e("isRecording = false e:", String.valueOf(e));
                    } finally {
                        mCamera = null;
                        cameraType = -1;
                    }
                }
            }
        }
    }


    @SuppressLint({ "InlinedApi", "NewApi" })
    @Override
    public void onClick(View v) {
        if (isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.record_btn:
                startRecordVideo();
                break;
            case R.id.change_camera:
                CustomLog.i(TAG, "changeCamera");
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
                    if (mCamera != null) {
                        mCamera.release();
                        mCamera = null;
                        cameraType = -1;
                    }
                    Toast.makeText(RecordingVideoAndPictureActivity.this, R.string.swith_fail,
                        Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }


    private void recordFailure() {
        CustomLog.i(TAG, "recordFailure()");

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
        CustomLog.i(TAG, "surfaceCreated()");
        openCamera(holder);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        CustomLog.d(TAG, "surfaceChanged() :: width:" + width + " | height:" + height);
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CustomLog.d(TAG, "surfaceDestroyed()");

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
        CustomLog.i(TAG, "openCamera()");
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
            if (mCamera != null) {
                mCamera.release();
                mCamera = null;
                cameraType = -1;
            }
            CustomToast.show(RecordingVideoAndPictureActivity.this, R.string.open_camera,
                Toast.LENGTH_SHORT);
        }
    }


    private void startCameraPreview(SurfaceHolder holder) throws IOException {
        CustomLog.i(TAG, "startCameraPreview()");

        if (mCamera != null) {
            // 由于设置显示方向为竖屏，此处需要设置显示角度
            // 某些手机竖屏下CameraInfo.orientation为0不是90（ZTE-N880E），
            // 因此CameraHelper.setCameraDisplayOrientation方法不会进行旋转90度。
            // 但实际上摄像头确实需要旋转90度，故此处暂固定旋转90度
            mCamera.setDisplayOrientation(90);
            // CameraHelper.setCameraDisplayOrientation(
            // RecordingVideoAndPictureActivity.this, cameraType, mCamera);

            Camera.Parameters parameters = mCamera.getParameters();

            parameters.set("orientation", "portrait");

            videoWidth = DEFAULT_VIDEO_WIDTH;
            videoHeight = DEFAULT_VIDEO_HEIGHT;

            // 得到一个最接近默认尺寸的尺寸
            Size approachSize = getOptimalPreviewSize(
                parameters.getSupportedPreviewSizes(), videoWidth,
                videoHeight);
            if (approachSize.width != videoWidth
                || approachSize.height != videoHeight) {
                videoWidth = approachSize.width;
                videoHeight = approachSize.height;
            }

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceView
                .getLayoutParams();
            int screenWidth =
                getDeviceSize(RecordingVideoAndPictureActivity.this).x;
            int screenHeight =
                getDeviceSize(RecordingVideoAndPictureActivity.this).y;
            float screenRatio = screenHeight * 1.0f / screenWidth;
            float videoRatio = videoWidth * 1.0f / videoHeight;
            //            if (videoRatio < screenRatio) {
            //                // 视频高宽比小于屏幕高宽比，则预览区域（surfaceView）放大到屏幕宽度
            //                params.width = videoHeight * screenWidth / videoHeight;
            //                params.height = videoWidth * screenWidth / videoHeight;
            //            } else {
            //                // 视频高宽比大于屏幕高宽比，则预览区域（surfaceView）放大到屏幕高度
            //                params.width = videoHeight * screenHeight / videoWidth;
            //                params.height = videoWidth * screenHeight / videoWidth;
            //            }
            //            // 屏幕为竖屏，显示角度旋转了90度，因此此处需要将宽高对调设置
            //            surfaceView.setLayoutParams(params);

            CustomLog.d(TAG, "surfaceCreated videoWidth=" + videoWidth + "|videoHeight="
                + videoHeight);
            parameters.setPreviewSize(videoWidth, videoHeight);

            List<String> supportedFocusModes = parameters.getSupportedFocusModes();
            List<Size> sizes = parameters.getSupportedPictureSizes();

            //获取摄像头支持的分辨率 ,如果包含(1280,720)就设置为这个,否则设置为最接近(1280,720)的分辨率
            int dif = 9999;
            int index = 0;
            for (int i = 0; i < sizes.size(); i++) {
                int i1 = sizes.get(i).width;
                int i2 = +sizes.get(i).height;
                int n = (Math.abs((sizes.get(i).width) - 1280) +
                    Math.abs((sizes.get(i).height) - 720));
                if (n < dif) {
                    dif = n;
                    index = i;
                }
            }
            parameters.setPictureSize(sizes.get(index).width, sizes.get(index).height);
            int w = sizes.get(index).width;
            int h = sizes.get(index).height;
            CustomLog.d(TAG, "实际摄像头获取的宽:" + w + "高:" + h);

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
            Log.d(TAG, "camera after parameters:" + mCamera.getParameters().flatten());

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } else {
            Toast.makeText(RecordingVideoAndPictureActivity.this, R.string.useless_camera,
                Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * @author: zhaguitao
     * @Title: getOptimalPreviewSize
     * @Description: 获取一个与期望宽高相近的尺寸
     * @date: 2014-3-25 下午5:31:30
     */
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        CustomLog.i(TAG, "getOptimalPreviewSize()");

        if (sizes == null) {
            return null;
        }

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


    public static Point getDeviceSize(Context context) {
        CustomLog.i(TAG, "getDeviceSize()");

        if (deviceSize == null) {
            deviceSize = new Point(0, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                ((WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getSize(deviceSize);
            } else {
                Display display = ((WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
                deviceSize.x = display.getWidth();
                deviceSize.y = display.getHeight();
                display = null;
            }
        }
        return deviceSize;
    }


    public void zuohua() {
        CustomLog.i(TAG, "zuohua()");

        if (recordType == 0) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(layout, "TranslationX", 0,
                -1 * re_btn_width);
            animator.setDuration(300);
            animator.start();
            videoText.setTextColor(Color.YELLOW);
            picText.setTextColor(Color.WHITE);
            xsp_title_middle_text.setVisibility(View.VISIBLE);
            recordBtn.setBackgroundResource(R.drawable.btn_video_begin_record_seletor);
            mCamera.stopPreview();
            mCamera.startPreview();
            recordType = 1;
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void youhua() {
        CustomLog.i(TAG, "youhua()");

        if (recordType == 1) {
            ObjectAnimator.ofFloat(layout, "TranslationX", -1 * re_btn_width, 0)
                .setDuration(300)
                .start();
            picText.setTextColor(Color.YELLOW);
            videoText.setTextColor(Color.WHITE);
            xsp_title_middle_text.setVisibility(View.GONE);
            recordBtn.setBackgroundResource(R.drawable.takepicture);
            mCamera.stopPreview();
            mCamera.startPreview();
            recordType = 0;
        }
    }


    private void doBack() {
        CustomLog.i(TAG, "doBack()");
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        // 删除mp4文件
        deleteMP4File();
        finish();
    }


    private void deleteMP4File() {
        CustomLog.i(TAG, "deleteMP4File()");

        if (!TextUtils.isEmpty(videoCachePath)) {
            File recordedFile = new File(videoCachePath);
            if (recordedFile.exists()) {
                recordedFile.delete();
            }
        }
    }
}
