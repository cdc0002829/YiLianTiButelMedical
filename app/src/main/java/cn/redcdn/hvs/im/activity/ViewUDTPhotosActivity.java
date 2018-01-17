package cn.redcdn.hvs.im.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.activity.ViewImages.PhotoView;
import cn.redcdn.hvs.im.activity.ViewImages.PhotoViewAttacher;
import cn.redcdn.hvs.im.bean.FileTaskBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.PhotoBean;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.fileTask.ChangeUIInterface;
import cn.redcdn.hvs.im.fileTask.DownloadTaskManager;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.util.ViewPages;
import cn.redcdn.hvs.im.view.BottomMenuWindow;
import cn.redcdn.hvs.im.view.CircleProgressBar;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.im.view.MedicalAlertDialog;
import cn.redcdn.hvs.im.view.MyMediaController;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <dt>ViewPhotosActivity.java</dt>
 * <dd>Description:图片浏览界面</dd>
 * <dd>Copyright: Copyright (C) 2014</dd>
 * <dd>Company: 安徽青牛信息技术有限公司</dd>
 * <dd>CreateDate: 2014-3-17 上午10:21:05</dd>
 * <dd>modify:更多-转发：采用本地分享的交互设计 on 2015-9-9 by wxy</dd>
 */

public class ViewUDTPhotosActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ViewPhotosActivity";

    public static final String KEY_PHOTOS_LIST = "photos_list";
    public static final String KEY_PHOTOS_SELECT_INDEX = "photos_select_index";
    public static final String KEY_REMOTE_FILE = "key_remote_file";
    public static final String KEY_VIDEO_FILE = "key_video_file";
    public static final String KEY_VIDEO_LEN = "key_video_len";
    public static final String KEY_COLLECTION_TYPE = "key_collection_type";
    public static final String KEY_COLLECTION_SCAN = "key_collection_scan";
    public static final String KEY_DISABLE_LONG_CLICK = "key_disable_long_click";
    private boolean disableLongClick = false;

    private Context mContext = null;
    private RelativeLayout rootViewContainer = null;

    private ViewPages mViewPager = null;
    private List<String> mListImage = null;
    private List<PhotoBean> mListPhoto = null;

    private boolean isRemoteFile = false;
    private boolean isVideoFile = false;
    private SamplePagerAdapter mAdapter = null;

    private TextView mTextViewInfo = null;
    private static int mSize = 0;

    private int selectedIndex = -1;


    private int len = 0;
    private String str;
    // 图片的存储目录
    private File PHOTO_DIR;
    private File VIDEO_DIR;
    private String filePath = "";

    private LayoutInflater layoutInflater = null;

    // 下载图片进度列表，便于对象销毁后再次显示时重新绑定
    private SparseArray<ProgressListener> progressListArray = new SparseArray<ProgressListener>();
    // // 显示页是否加载到内存
    // private SparseBooleanArray pageLoadedArray = new SparseBooleanArray();
    // private SparseArray<View> pages = new SparseArray<View>();

    private static int downLoadExceptWifi = 0;
    private CommonDialog downLoadDlg = null;

    private boolean isFromAlarm = false;
    private String[] times;
    private TextView takeTimeText = null;
    private long entryTime = 0;
    //0表示从消息页面跳转，1表示从收藏页面跳转
    private int collectionType = 0;
    //true 表示可以收藏  false不可以收藏
    private boolean canCollect = true;
    private MyMediaController currMediaControl = null;
    //静音播放
    private boolean isSilentPlay;
    //静音播放
    public static final String SILENT_PLAY = "SILENT_PLAY";

    /**
     * 聊天页面返回标记
     */
    public static final String KEY_CHAT_BACK_FLAG = "key_chat_back_flag";
    private static final String VOICE_PREFS_NAME = "VoicePrefsFile";
    private int originalPageIndex = -1;//最开始选择的页面index，用于第一次播放设置静音播放
    //语音消息扬声器播放模式
    private static final boolean SPEAKER = true;
    //语音消息听筒播放模式
    private static final boolean HEADSET = false;
    private Boolean playMode;
    private AudioManager audioManager;

    private HashMap<String, Boolean> downLoadingFailed =
            new HashMap<String, Boolean>(); //下载失败过的 如断网

    private boolean isAlreadyVideo = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.viewphotos);
        CustomLog.d(TAG, "onCreate begin");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
//        mPhotoView = (PhotoView) findViewById(R.id.photoView);
        this.mContext = this;
        isSilentPlay = getIntent().getBooleanExtra(SILENT_PLAY, false);
        this.layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        SharedPreferences voiceSettings = getSharedPreferences(VOICE_PREFS_NAME, MODE_PRIVATE);
        //第二个参数是 SharedPreference 不存在时返回的默认值
        playMode = voiceSettings.getBoolean("VOICE_PLAY_MODE", SPEAKER);

        Bundle boudle = getIntent().getExtras();
        isRemoteFile = boudle.getBoolean(KEY_REMOTE_FILE);
        isVideoFile = boudle.getBoolean(KEY_VIDEO_FILE);

        str = getString(R.string.photos);
        PHOTO_DIR = new File(Environment.getExternalStorageDirectory() + "/medicalhvs/picture");
        VIDEO_DIR = new File(Environment.getExternalStorageDirectory() + "/medicalhvs/video");

        collectionType = boudle.getInt(KEY_COLLECTION_TYPE, 0);
        canCollect = boudle.getBoolean(KEY_COLLECTION_SCAN, false);
        disableLongClick = boudle.getBoolean(KEY_DISABLE_LONG_CLICK, false);

        if (isRemoteFile) {
            mListPhoto = boudle.getParcelableArrayList(KEY_PHOTOS_LIST);
            if (null != mListPhoto)
                mSize = mListPhoto.size();
        } else {
            mListImage = boudle.getStringArrayList(KEY_PHOTOS_LIST);
            if (null != mListImage)
                mSize = mListImage.size();
        }


        RelativeLayout container = (RelativeLayout) findViewById(R.id.image_container);
        rootViewContainer = (RelativeLayout) findViewById(R.id.view_container);

        mViewPager = new ViewPages(this);
        container.addView(mViewPager, 0);

        mTextViewInfo = (TextView) findViewById(R.id.text_info);
        rootViewContainer.bringChildToFront(mTextViewInfo);

        // 显示报警照片拍摄时间-wxy
        takeTimeText = (TextView) findViewById(R.id.take_time);
        if (isFromAlarm) {
            rootViewContainer.bringChildToFront(takeTimeText);
        }
        initControl();
        isAlreadyVideo = false;
        CustomLog.d(TAG, "onCreate end");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initControl() {
        mAdapter = new SamplePagerAdapter();

        mViewPager.setOnPageChangeListener(pageListener);
        mViewPager.setAdapter(mAdapter);
        if (selectedIndex == -1) {
            selectedIndex = getIntent().getIntExtra(KEY_PHOTOS_SELECT_INDEX, 0);
        }
        originalPageIndex = selectedIndex;
        mViewPager.setCurrentItem(selectedIndex);
        onImgPageSelected(selectedIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        CustomLog.d(TAG, "onStop begin");
        if (isFromAlarm) {
            long endTime = System.currentTimeMillis();
            int duration = (int) ((endTime - entryTime) / 1000);
//            MobclickAgent.onEventValue(getBaseContext(),
//                    UmengEventConstant.EVENT_ALARM_IMAGE_DURATION, null,
//                    duration);
        }
        if (currMediaControl != null) {
            currMediaControl.stopVideoPlay();
            currMediaControl = null;
        }
        CustomLog.d(TAG, "onStop end");
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        CustomLog.d(TAG, "onDestroy begin");
        mViewPager.clearAnimation();
        mViewPager.destroyDrawingCache();
        mViewPager.removeAllViews();

        if (downLoadDlg != null && downLoadDlg.isShowing()) {
            downLoadDlg.dismiss();
            downLoadDlg = null;
        }

        downLoadExceptWifi = 0;
        mContext = null;

        if (dlObjList != null) {
            dlObjList.clear();
            dlObjList = null;
        }
        if (playMode == SPEAKER) {

        } else {
            audioManager.setSpeakerphoneOn(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
        }
        CustomLog.d(TAG, "onDestroy end");
    }

    private void onImgPageSelected(int index) {
        mTextViewInfo.setText((index + 1) + "/" + mSize);
        if (isFromAlarm) {
            takeTimeText.setText(times[index]);
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    public class SamplePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mListPhoto.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, final int position) {

            CustomLog.d(TAG, "SamplePagerAdapter instantiateItem:" + position);

            final PhotoBean photo = mListPhoto.get(position);

            final View downloadingView = layoutInflater.inflate(R.layout.view_photo_downloading
                    , container, false);

            container.addView(downloadingView, 0, new LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            boolean exist = isValidImagePath(photo.getLocalPath());
            if (!exist) {
                if (TextUtils.isEmpty(photo.getRemoteUrl())) {
                    downloadingView.findViewById(R.id.no_pic_line)
                            .setVisibility(View.VISIBLE);
                    return downloadingView;
                }
            }

            downloadingView.findViewById(R.id.no_pic_line).setVisibility(View.GONE);

            final VideoView videoView = (VideoView) downloadingView.findViewById(R.id.video_view);
            final MyMediaController myMediaController = (MyMediaController) downloadingView.findViewById(R.id.plyer_mmc);
            final ImageView videoIconView = (ImageView) downloadingView.findViewById(R.id.video_icon);
            final FrameLayout viewContainer = (FrameLayout) downloadingView.findViewById(R.id.container);
            final PhotoView photoViewThumbnail = new PhotoView(container.getContext());
            viewContainer.addView(photoViewThumbnail, 0, new LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            downloadingView.findViewById(R.id.delate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            downloadingView.findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IMCommonUtil.playVideo(mContext, photo.getLocalPath());
                }
            });
            photoViewThumbnail.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float x, float y) {
                    CustomLog.d(TAG, "photoViewThumbnail.setOnViewTapListener");
                    if (isRemoteFile && mListPhoto != null
                            && mListPhoto.size() > 0) {
                        final PhotoBean photo = mListPhoto.get(position);
                        if (photo != null
                                && photo.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {

                            if (downLoadingFailed.get(photo.getTaskId()) != null && downLoadingFailed.get(photo.getTaskId()) == true) {

                                downLoad(downloadingView, photo.getTaskId(),
                                        photo.getRemoteUrl(), photoViewThumbnail,
                                        position);
                                return;

                            }


                            String localPathf = photo.getLocalPath();
                            if (!TextUtils.isEmpty(localPathf)
                                    && !localPathf.endsWith(".temp")) {
                                File locVidFile = new File(localPathf);
                                if (locVidFile.exists()) {
                                    if (photo.getLocalPath().toLowerCase().endsWith(".wmv")
                                            || photo.getLocalPath().toLowerCase().endsWith(".avi")
                                            || photo.getLocalPath().toLowerCase().endsWith(".asf")) {
                                        downloadingView.findViewById(R.id.error_video_type_layout).setVisibility(View.VISIBLE);
                                        downloadingView.findViewById(R.id.error_video_type_layout).setVisibility(View.VISIBLE);
                                        TextView videoName = (TextView) downloadingView.findViewById(R.id.video_name_tv);
                                        String sourceFileName = photo.getLocalPath().substring(photo.getLocalPath()
                                                .lastIndexOf("/") + 1);
                                        videoName.setText(sourceFileName);
                                        downloadingView.findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                IMCommonUtil.playVideo(mContext, photo.getLocalPath());
                                            }
                                        });
                                    } else {
                                        downloadingView.findViewById(R.id.error_video_type_layout).setVisibility(View.INVISIBLE);
                                        videoView.setVisibility(View.VISIBLE);
                                        myMediaController.setVisibility(View.VISIBLE);
                                        viewContainer.setVisibility(View.GONE);
                                        videoIconView.setVisibility(View.GONE);

                                        myMediaController.setVideoView(videoView);
                                        myMediaController.setVideoPath(photo.getLocalPath(), isSilentPlay);
                                        currMediaControl = myMediaController;

                                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {
                                                CustomLog.d(TAG, "视频播放完成");
                                                currMediaControl = null;
                                                videoView.setVisibility(View.GONE);
                                                myMediaController.setVisibility(View.GONE);
                                                viewContainer.setVisibility(View.VISIBLE);
                                                videoIconView.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                            @Override
                                            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                                                downloadingView.findViewById(R.id.error_video_type_layout).setVisibility(View.VISIBLE);
                                                TextView videoName = (TextView) downloadingView.findViewById(R.id.video_name_tv);
                                                String sourceFileName = photo.getLocalPath().substring(photo.getLocalPath()
                                                        .lastIndexOf("/") + 1);
                                                videoName.setText(sourceFileName);
                                                return true;
                                            }
                                        });
                                        myMediaController.setCloseCallbackListener(new CloseCalback() {
                                            @Override
                                            public void close() {
                                                finish();
                                                if (videoView != null) {
                                                    videoView.suspend();
                                                }
                                            }

                                            @Override
                                            public void longPress() {
                                                if(!disableLongClick){

                                                }
                                            }
                                        });
                                    }
                                    return;
                                }
                            }
                        } else {
                            finish();
                        }
                    }


                    if (NetConnectHelper.getNetWorkType(mContext) == NetConnectHelper.NETWORKTYPE_INVALID) {
                        CustomToast.show(mContext, getString(R.string.net_error_wait_try_again), Toast.LENGTH_SHORT);
                        finish();
                    } else {

                    }
                    if (downloadingView.findViewById(R.id.delate).getVisibility() == View.GONE) {
                        downloadingView.findViewById(R.id.delate).setVisibility(View.VISIBLE);
                    } else {
                        downloadingView.findViewById(R.id.delate).setVisibility(View.GONE);
                    }

                }
            });

            photoViewThumbnail.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    CustomLog.d(TAG, "photoViewThumbnail.setOnLongClickListener");
//                            CustomToast.show(mContext,"长按事件产生",CustomToast.LENGTH_SHORT);
                    if(!disableLongClick){
                    }
                    return false;
                }
            });


            videoView.setVisibility(View.GONE);
            myMediaController.setVisibility(View.GONE);
            viewContainer.setVisibility(View.VISIBLE);
            if (photo.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
                videoIconView.setVisibility(View.VISIBLE);
            } else {
                videoIconView.setVisibility(View.GONE);
            }


            if (exist) {
                if (photo.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
                    Glide.with(ViewUDTPhotosActivity.this)
                            .load(photo.getLittlePicUrl())
                            .placeholder(R.drawable.empty_photo)
                            .error(R.drawable.empty_photo)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .crossFade().into(photoViewThumbnail);
                } else {
                    Glide.with(ViewUDTPhotosActivity.this)
                            .load(photo.getLocalPath())
                            .placeholder(R.drawable.empty_photo)
                            .error(R.drawable.empty_photo)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .crossFade().into(photoViewThumbnail);
                    //图片当做播放过
                    isAlreadyVideo = true;
                }
                if (!isAlreadyVideo) {
                    if (photo.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
                        String localPathf = photo.getLocalPath();
                        if (!TextUtils.isEmpty(localPathf)
                                && !localPathf.endsWith(".temp")) {
                            File locVidFile = new File(localPathf);
                            if (locVidFile.exists()) {
                                if (photo.getLocalPath().toLowerCase().endsWith(".wmv")
                                        || photo.getLocalPath().toLowerCase().endsWith(".avi")
                                        || photo.getLocalPath().toLowerCase().endsWith(".asf")) {
                                    downloadingView.findViewById(R.id.error_video_type_layout).setVisibility(View.VISIBLE);
                                    TextView videoName = (TextView) downloadingView.findViewById(R.id.video_name_tv);
                                    String sourceFileName = photo.getLocalPath().substring(photo.getLocalPath()
                                            .lastIndexOf("/") + 1);
                                    videoName.setText(sourceFileName);
                                    downloadingView.findViewById(R.id.play_btn).setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            IMCommonUtil.playVideo(mContext, photo.getLocalPath());
                                        }
                                    });
                                } else {
                                    downloadingView.findViewById(R.id.error_video_type_layout).setVisibility(View.INVISIBLE);
                                    videoView.setVisibility(View.VISIBLE);
                                    myMediaController.setVisibility(View.VISIBLE);
                                    viewContainer.setVisibility(View.GONE);
                                    videoIconView.setVisibility(View.GONE);

                                    myMediaController.setVideoView(videoView);
                                    myMediaController.setVideoPath(photo.getLocalPath(), isSilentPlay);
                                    currMediaControl = myMediaController;
                                    isAlreadyVideo = true;

                                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            CustomLog.d(TAG, "视频播放完成");
                                            currMediaControl = null;
                                            videoView.setVisibility(View.GONE);
                                            myMediaController.setVisibility(View.GONE);
                                            viewContainer.setVisibility(View.VISIBLE);
                                            videoIconView.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    myMediaController.setCloseCallbackListener(new CloseCalback() {
                                        @Override
                                        public void close() {
                                            finish();
                                            if (videoView != null) {
                                                videoView.suspend();
                                            }
                                        }

                                        @Override
                                        public void longPress() {
//                                            CustomToast.show(mContext, "长按事件产生", CustomToast.LENGTH_SHORT);
                                            if(!disableLongClick){
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }


            } else {
                //没有下载的视频也认为播放过
                isAlreadyVideo = true;
                CustomLog.d(TAG, "图片暂未下载的场合，先显示缩略图，同时开始下载原图");
                // 开始下载原图或视频

                // 图片暂未下载的场合，先显示缩略图，同时开始下载原图
                String thumbnailUrl = photo.getLittlePicUrl();
                if (!TextUtils.isEmpty(thumbnailUrl)) {
                    Glide.with(ViewUDTPhotosActivity.this).load(thumbnailUrl)
                            .placeholder(R.drawable.empty_photo)
                            .error(R.drawable.empty_photo)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .crossFade().into(photoViewThumbnail);
                }

                if (!TextUtils.isEmpty(photo.getRemoteUrl())) {

                    downLoad(downloadingView, photo.getTaskId(),
                            photo.getRemoteUrl(), photoViewThumbnail,
                            position);
                }

            }


            return downloadingView;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
            // pageLoadedArray.put(position, false);
            // pages.remove(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private boolean isValidImagePath(String imagePath) {
        if (TextUtils.isEmpty(imagePath) || imagePath.endsWith(".temp")) {
            return false;
        }
        File file = new File(imagePath);
        if (!file.exists()) {
            return false;
        }
        if (file.length() == 0) {
            file.delete();
            return false;
        }
        return true;
    }

    ViewPager.OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int arg0) {
            selectedIndex = mViewPager.getCurrentItem();
            CustomLog.d(TAG, "pageListener:onPageSelected:" + arg0 + "|" + selectedIndex);
            onImgPageSelected(selectedIndex);
            if (currMediaControl != null) {
                currMediaControl.stopVideoPlay();
                currMediaControl = null;
            }
            //当前选择的页面 isSilentPlay保持不变
            if (selectedIndex == originalPageIndex) {
                //重新初始化，用于下次滑动到改页面，使用扬声器播放
                originalPageIndex = -1;
            } else {
                isSilentPlay = false;
            }

            if (isRemoteFile) {
                PhotoBean photo = mListPhoto.get(selectedIndex);
                final String localPath = photo.getLocalPath();
                if (isValidImagePath(localPath)
                        || !TextUtils.isEmpty(photo.getRemoteUrl())) {
                    // 有数据源的场合，才显示更多按钮
//                    titleBar.setRightBtnVisibility(View.VISIBLE);
                } else {
//                    titleBar.setRightBtnVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    public interface CloseCalback {
        public void close();

        public void longPress();
    }

    private Handler callbackHandler = new Handler() {
        public void handleMessage(Message msg) {
            CustomLog.d(TAG, "callbackHandler msg =" + msg.what);
            switch (msg.what) {
                case 1:
                    final DownLoadObject obj = (DownLoadObject) msg.obj;

                    if (NetConnectHelper.NETWORKTYPE_WIFI == NetConnectHelper.getNetWorkType(mContext)) {
                        // 当前是wifi连接，直接下载
                        attachProgressView(obj.downLoadingView, obj.id, obj.remoteUrl,
                                obj.photoView, obj.position);
                    } else {
                        if (NetConnectHelper.NETWORKTYPE_INVALID == NetConnectHelper.getNetWorkType(mContext)) {
                            // 当前无网络连接，放弃下载
//                            Toast.makeText(ViewPhotosActivity.this,
//                                    getString(R.string.setting_internet),
//                                    Toast.LENGTH_SHORT).show();
//                            finish();

                            break;
                        }
                        // 视频大小大于3M时候出现提醒框
                        if (!isVideoFile) {
                            attachProgressView(obj.downLoadingView, obj.id, obj.remoteUrl,
                                    obj.photoView, obj.position);
                            break;
                        }
                        if (downLoadExceptWifi == 0) {
                            downLoadExceptWifi = -1;

                            // 非wifi连接下，提示是否继续下载
                            downLoadDlg = new CommonDialog(ViewUDTPhotosActivity.this,
                                    getLocalClassName(), 104);
                            downLoadDlg.setMessage(R.string.load_tip);
                            downLoadDlg.setCancelable(false);
                            downLoadDlg.setCancleButton(new CommonDialog.BtnClickedListener() {
                                @Override
                                public void onBtnClicked() {
                                    downLoadExceptWifi = 2;
                                }
                            }, R.string.cancel_message);
                            downLoadDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {
                                @Override
                                public void onBtnClicked() {
                                    downLoadExceptWifi = 1;
                                    // attachProgressView(obj.progressLine, obj.id,
                                    // obj.remoteUrl, obj.photoView, obj.position);
                                    resentDownloadMsg();
                                }
                            }, R.string.confirm_message);
                            downLoadDlg.showDialog();
                        } else if (downLoadExceptWifi == 1) {
                            // 用户选择，非wifi时，下载
                            Toast.makeText(ViewUDTPhotosActivity.this,
                                    getString(R.string.load_tips),
                                    Toast.LENGTH_SHORT).show();
                            attachProgressView(obj.downLoadingView, obj.id,
                                    obj.remoteUrl, obj.photoView, obj.position);
                        } else if (downLoadExceptWifi == 2) {
                            // 用户选择，非wifi时，不下载
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void downLoad(final View downLoadview, final String id,
                          final String remoteUrl, final PhotoView photoView,
                          final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (downLoadExceptWifi == -1) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                DownLoadObject obj = new DownLoadObject();
                obj.downLoadingView = downLoadview;
                obj.id = id;
                obj.remoteUrl = remoteUrl;
                obj.photoView = photoView;
                obj.position = position;

                if (dlObjList != null) {
                    dlObjList.add(obj);
                } else {
                    dlObjList = new ArrayList<DownLoadObject>();
                    dlObjList.add(obj);
                }

                Message msg = callbackHandler.obtainMessage();
                msg.what = 1;
                msg.obj = obj;
                callbackHandler.sendMessage(msg);
            }
        }).start();
    }

    /***
     * 因目前‘非WIFI下，下载弹出框’仅在page 0页面出现一次，其他page不弹出
     * 而handler消息在instantiateItem回调方法中及时性的send完成； 以致点对话框中的同意按钮仅对page 0页面的原图进行下载；
     * 故用list暂存数据，点‘同意’后再重新发handler消息
     */
    private ArrayList<DownLoadObject> dlObjList = new ArrayList<DownLoadObject>();

    private void resentDownloadMsg() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (dlObjList != null && dlObjList.size() > 0) {
                    DownLoadObject obj = dlObjList.get(0);
                    Message msg = callbackHandler.obtainMessage();
                    msg.what = 1;
                    msg.obj = obj;
                    callbackHandler.sendMessage(msg);
                    if (dlObjList.size() > 1) {
                        for (int i = 1; i < dlObjList.size(); i++) {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            obj = dlObjList.get(i);
                            msg = callbackHandler.obtainMessage();
                            msg.what = 1;
                            msg.obj = obj;
                            callbackHandler.sendMessage(msg);
                        }
                    }
                }
            }
        }).start();
    }

    private class DownLoadObject {
        public View downLoadingView;
        public String id;
        public String remoteUrl;
        public PhotoView photoView;
        public int position;
    }

    private void attachProgressView(View downLoadView, String id,
                                    String remoteUrl, PhotoView photoView, int position) {
        CustomLog.d(TAG, "id:" + id + " | remoteUrl:" + remoteUrl + " | position:"
                + position);
        ProgressListener progressListener = progressListArray.get(position);
        if (progressListener != null) {
            progressListener.reBindWidget(downLoadView, photoView);

            if (downLoadingFailed.get(mListPhoto.get(position).getTaskId()) != null &&
                    downLoadingFailed.get(mListPhoto.get(position).getTaskId()) == true) {
                MedicalApplication.getFileTaskManager().addSingleFileDownloadTask(
                        id, remoteUrl, true, progressListener);
            }
        } else {
            progressListener = new ProgressListener(downLoadView, photoView);
            progressListener.setParams(id, remoteUrl, position);
            progressListArray.put(position, progressListener);
            // fileTaskMgr中管理文件下载进度（已经开始了，绑定一下；未开始，则开始下载并绑定）
            if (collectionType == 0) {
                MedicalApplication.getFileTaskManager().addSingleFileDownloadTask(
                        id, remoteUrl, true, progressListener);
            } else {
                PhotoBean bean = mListPhoto.get(position);
                if (mListPhoto.size() == 1) {
                    DownloadTaskManager.getInstance(this).downloadFile(id, "",
                            true, progressListener, 0);
                } else {
                    DownloadTaskManager.getInstance(this).downloadFile(id,
                            remoteUrl, true, progressListener, position);
                }
            }
        }
    }

    /**
     * <dl>
     * <dt>ViewPhotosActivity.java</dt>
     * <dd>Description:文件下载进度监控</dd>
     * <dd>Copyright: Copyright (C) 2014</dd>
     * <dd>Company: 安徽青牛信息技术有限公司</dd>
     * <dd>CreateDate: 2014-1-6 下午4:37:44</dd>
     * </dl>
     *
     * @author zhaguitao
     */
    private class ProgressListener extends ChangeUIInterface {
        private WeakReference<View> progressLineReference;
        private WeakReference<PhotoView> photoViewReference;

        // private String id = "";
        // private String remoteUrl = "";
        private int position = -1;

        public void setParams(String id, String remoteUrl, int position) {
            // this.id = id;
            // this.remoteUrl = remoteUrl;
            this.position = position;
        }

        public ProgressListener(View downLoadview, PhotoView photoView) {
            progressLineReference = new WeakReference<View>(downLoadview);
            photoViewReference = new WeakReference<PhotoView>(photoView);
        }

        public void reBindWidget(View progressLine, PhotoView photoView) {
            progressLineReference = new WeakReference<View>(
                    progressLine);
            photoViewReference = new WeakReference<PhotoView>(photoView);
        }

        public void onStart(FileTaskBean bean) {
            // 开始文件任务
            CustomLog.d(TAG, "onStart:" + position);
            downLoadingFailed.put(mListPhoto.get(position).getTaskId(), false);

            final View downloadView = progressLineReference.get();
            if (downloadView != null) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        LinearLayout progressLine = (LinearLayout) downloadView.findViewById(R.id.loading_line);
                        progressLine.setVisibility(View.VISIBLE);
                        downloadView.findViewById(R.id.video_icon).setVisibility(View.GONE);
                    }
                });
            }
        }

        public void onProcessing(FileTaskBean bean, long current, long total) {
            // 文件任务进度
            CustomLog.d(TAG, "onProcessing:" + position + "|" + current + "/" + total);

            if (current < 0 || total <= 0) {
                CustomLog.d(TAG, "onProcessing:数据不合法，不做更新");
                return;
            }

            final float pro = current / (total * 1.0f);

            final View downloadView = progressLineReference.get();

            if (downloadView != null) {
                final LinearLayout progressLine = (LinearLayout) downloadView.findViewById(R.id.loading_line);
                final ImageView videoIconIV = (ImageView) downloadView.findViewById(R.id.video_icon);
                if (progressLine != null) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (progressLine.getVisibility() != View.VISIBLE) {
                                progressLine.setVisibility(View.VISIBLE);
                            }
                            if (videoIconIV.getVisibility() != View.GONE) {
                                videoIconIV.setVisibility(View.GONE);
                            }

                            CircleProgressBar progressBar = (CircleProgressBar) progressLine
                                    .findViewById(R.id.circle_progress_bar);
                            NumberFormat numFormat = NumberFormat
                                    .getNumberInstance();
                            numFormat.setMaximumFractionDigits(0);
                            int progressInt = Integer.parseInt(numFormat
                                    .format(pro * 100));
                            progressBar.setmProgress(progressInt);

                        }
                    });

                    progressLine.setVisibility(View.VISIBLE);
                }


            }
        }

        public void onSuccess(FileTaskBean bean, final String result) {
            // 文件任务成功完成
            CustomLog.d(TAG, "onSuccess:" + position + "|" + result);

            final View downloadView = progressLineReference.get();
            final LinearLayout progressLine = (LinearLayout) downloadView.findViewById(R.id.loading_line);
            if (progressLine != null) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressLine.setVisibility(View.GONE);
                        downloadView.findViewById(R.id.video_icon).setVisibility(View.GONE);
                    }
                });
            }

            final PhotoBean photoBean = mListPhoto.get(position);
            downLoadingFailed.remove(photoBean.getTaskId());
            photoBean.setLocalPath(result);
            mListPhoto.set(position, photoBean);
            final PhotoView photoView = photoViewReference.get();
            if (photoView != null && mContext != null) {
                if (photoBean.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
//                    final PhotoView photoViewThumbnail = new PhotoView(mContext);
                    if (photoBean.getLocalPath().toLowerCase().endsWith(".wmv")
                            || photoBean.getLocalPath().toLowerCase().endsWith(".avi")
                            || photoBean.getLocalPath().toLowerCase().endsWith(".asf")) {
                        downloadView.findViewById(R.id.error_video_type_layout).setVisibility(View.VISIBLE);
                        TextView videoName = (TextView) downloadView.findViewById(R.id.video_name_tv);
                        String sourceFileName = photoBean.getLocalPath().substring(photoBean.getLocalPath()
                                .lastIndexOf("/") + 1);
                        videoName.setText(sourceFileName);
                    } else {

                        final VideoView videoView = (VideoView) downloadView.findViewById(R.id.video_view);
                        final MyMediaController myMediaController = (MyMediaController) downloadView.findViewById(R.id.plyer_mmc);
                        final ImageView videoIconView = (ImageView) downloadView.findViewById(R.id.video_icon);


                        final FrameLayout viewContainer = (FrameLayout) downloadView.findViewById(R.id.container);
//                    viewContainer.addView(photoViewThumbnail, 0, new LayoutParams(
//                            RelativeLayout.LayoutParams.MATCH_PARENT,
//                            RelativeLayout.LayoutParams.MATCH_PARENT));

                        videoView.setVisibility(View.VISIBLE);
                        myMediaController.setVisibility(View.VISIBLE);
                        viewContainer.setVisibility(View.GONE);
                        videoIconView.setVisibility(View.GONE);
//                        delate.setVisibility(View.GONE);
                        downloadView.findViewById(R.id.delate).setVisibility(View.GONE);
                        myMediaController.setVideoView(videoView);
                        myMediaController.setVideoPath(result, isSilentPlay);
                        currMediaControl = myMediaController;

                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                CustomLog.d(TAG, "视频播放完成");
                                currMediaControl = null;
                                videoView.setVisibility(View.GONE);
                                myMediaController.setVisibility(View.GONE);
                                viewContainer.setVisibility(View.VISIBLE);
                                videoIconView.setVisibility(View.VISIBLE);
                            }
                        });
                        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                                downloadView.findViewById(R.id.error_video_type_layout).setVisibility(View.VISIBLE);
                                TextView videoName = (TextView) downloadView.findViewById(R.id.video_name_tv);
                                String sourceFileName = photoBean.getLocalPath().substring(photoBean.getLocalPath()
                                        .lastIndexOf("/") + 1);
                                videoName.setText(sourceFileName);
                                return true;
                            }
                        });
                        myMediaController.setCloseCallbackListener(new ViewUDTPhotosActivity.CloseCalback() {
                            @Override
                            public void close() {
                                finish();
                                if (videoView != null) {
                                    videoView.suspend();
                                }
                            }

                            @Override
                            public void longPress() {

                            }
                        });
                    }

                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // 文件下载完成后显示原图
                            Glide.with(ViewUDTPhotosActivity.this)
                                    .load(result)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .crossFade().into(photoView);
                        }
                    });
                }
            }
        }

        public void onFailure(FileTaskBean bean, Throwable error, String msg) {
            // 文件任务失败
            CustomLog.d(TAG, "onFailure:" + position + "|" + msg);
            CustomToast.show(mContext, getString(R.string.net_error_wait_try_again), Toast.LENGTH_SHORT);
            downLoadingFailed.put(mListPhoto.get(position).getTaskId(), true);
            final View downloadView = progressLineReference.get();
            final LinearLayout progressLine = (LinearLayout) downloadView.findViewById(R.id.progress_line);
            if (progressLine != null) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        progressLine.setVisibility(View.GONE);
                    }
                });
            }
        }

    }

    /**
     * @author: qn-lihs
     * @Title: savePhotoToSDCard
     * @Description: 保存当前的图片到SD卡
     * @date: 2013-10-30 下午2:21:45
     */
    private void savePhotoToSDCard(PhotoBean bean) {
        if (mSize > 0) {
            try {
                final String fromFilePath = getFilePath();
                filePath = getDesFilePath(fromFilePath, bean);
                if (!TextUtils.isEmpty(filePath)) {
                    if (new File(filePath).exists()) {
//                        alertMassage(getString(R.string.picture_exist,
//                                "\"" + PHOTO_DIR.getAbsolutePath() + "\""));
                        CustomToast.show(this, getString(R.string.have_save_local_album), Toast.LENGTH_SHORT);
                        return;
                    }
                } else {
//                    alertMassage(getString(R.string.copy_fail));
                    CustomToast.show(this, getString(R.string.save_fail_wait_try_again), Toast.LENGTH_SHORT);
                    return;
                }
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        boolean isSuccess = saveLocalFile(fromFilePath);
                        Message msg = myHandler.obtainMessage();
                        msg.obj = isSuccess;
                        msg.what = 1;
                        myHandler.sendMessage(msg);
                    }
                }).start();
            } catch (Exception e) {
                CustomLog.e(TAG, e.toString());
                Message msg = myHandler.obtainMessage();
                msg.obj = false;
                msg.what = 1;
                myHandler.sendMessage(msg);
            }
        } else {
            CustomLog.d(TAG, "无法保存图片");
        }
    }

    private String getFilePath() {
        if (isRemoteFile) {
            return mListPhoto.get(mViewPager.getCurrentItem()).getLocalPath();
        } else {
            return mListImage.get(mViewPager.getCurrentItem());
        }
    }

    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if ((Boolean) msg.obj) {
//                        alertMassage(getString(R.string.save_picture_tip, filePath));
                        // 发广播刷新系统媒体库
                        CustomToast.show(ViewUDTPhotosActivity.this, getString(R.string.have_save_local_album), Toast.LENGTH_SHORT);
                        IMCommonUtil.scanFileAsync(ViewUDTPhotosActivity.this, filePath);
                    } else {
                        if (!TextUtils.isEmpty(filePath)) {
                            File file = new File(filePath);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private boolean saveLocalFile(String copyFilePath) {

        if (TextUtils.isEmpty(copyFilePath)) {
            return false;
        } else {
            CustomLog.d(TAG, "saveLocalFile 源文件路径：" + copyFilePath + "|" + "目标文件路径："
                    + filePath);
            FileOutputStream os = null;
            FileInputStream in = null;
            try {
                os = new FileOutputStream(filePath);
                in = new FileInputStream(copyFilePath);
                byte[] buffer = new byte[8 * 1024];
                int c = -1;
                if (in != null) {
                    while ((c = in.read(buffer)) > 0) {
                        os.write(buffer, 0, c);
                    }
                }
                os.flush();
                return true;
            } catch (OutOfMemoryError e) {
                CustomLog.d(TAG, "os.write and os.flush出现异常" + e.toString());
            } catch (Exception e) {
            } finally {
                try {
                    if (in != null) {
                        in.close();
                        os = null;
                    }
                    if (os != null) {
                        os.close();
                        os = null;
                    }
                } catch (Exception e2) {
                    CustomLog.d(TAG, "in.close and os.close出现异常" + e2.toString());
                }
            }
        }
        return false;
    }

    private String getDesFilePath(String fromPhotoPath, PhotoBean bean) {
//        PhotoBean photo = mListPhoto.get(position);
        if (TextUtils.isEmpty(fromPhotoPath)) {
            return "";
        }
        try {
            if (!PHOTO_DIR.exists()) {
                PHOTO_DIR.mkdirs();
            }
            if (!VIDEO_DIR.exists()) {
                VIDEO_DIR.mkdirs();
            }
            String sourceFileName = fromPhotoPath.substring(fromPhotoPath
                    .lastIndexOf("/") + 1);
            if (bean.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
                return VIDEO_DIR.getAbsolutePath() + "/" + sourceFileName;
            } else {
                return PHOTO_DIR.getAbsolutePath() + "/" + sourceFileName;
            }
        } catch (Exception e) {

        }
        return "";
    }

    private void alertMassage(String id) {
        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_SHORT).show();
    }

    private void selectContact() {

        PhotoBean forwardBean = mListPhoto.get(mViewPager.getCurrentItem());
        String forwardNoticeId = forwardBean.getTaskId();

        CustomLog.d(TAG, "从浏览图片页面转发图片:forwardNoticeId=" + forwardNoticeId);

        Intent i = new Intent(this, ShareLocalActivity.class);
        i.putExtra(ShareLocalActivity.KEY_ACTION_FORWARD, true);
        i.putExtra(ShareLocalActivity.MSG_ID, forwardNoticeId);
        i.putExtra("chatForwardPath",forwardBean.getLittlePicUrl());
        i.putExtra("chatForwardType",forwardBean.getType());
        startActivity(i);

        // 返回到消息列表
        finish();
    }

    private void showDialog() {
        final PhotoBean bean = mListPhoto.get(mViewPager.getCurrentItem());

        final MedicalAlertDialog dialog = new MedicalAlertDialog(ViewUDTPhotosActivity.this);
        if (bean.getLocalPath().toLowerCase().endsWith(".wmv")
                || bean.getLocalPath().toLowerCase().endsWith(".avi")
                || bean.getLocalPath().toLowerCase().endsWith(".asf")) {
            dialog.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    IMCommonUtil.playVideo(mContext, bean.getLocalPath());
                }
            }, getString(R.string.use_other_app));

            dialog.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    selectContact();
                    CustomLog.d(TAG, getString(R.string.chat_forward));
                }
            }, getString(R.string.chat_forward));
            final NoticesBean noticebean = new NoticesDao(ViewUDTPhotosActivity.this)
                    .getNoticeById(bean.getTaskId());
            dialog.addButtonThird(new BottomMenuWindow.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    CollectionManager.getInstance()
                            .addCollectionByNoticesBean(
                                    ViewUDTPhotosActivity.this,
                                    noticebean);
                    CustomLog.d(TAG, "收藏");
                }
            }, getString(R.string.collect_str));

            if (bean.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
                CustomLog.d(TAG, "浏览类型:视频?图片" + bean.getType());
                dialog.addButtonForth(new BottomMenuWindow.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        CustomLog.d(TAG, "onMenuClicked");
                        // 存储到本地
                        savePhotoToSDCard(bean);
                    }
                }, getString(R.string.save_video_me));
            } else {
                dialog.addButtonForth(new BottomMenuWindow.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        CustomLog.d(TAG, "onMenuClicked");
                        // 存储到本地
                        savePhotoToSDCard(bean);

                    }
                }, getString(R.string.save_pic));
            }
            dialog.addButtonFive(new BottomMenuWindow.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    dialog.dismiss();
                }
            }, getString(R.string.btn_cancle));

        } else {

            dialog.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    selectContact();
                    CustomLog.d(TAG, "转发");
                }
            }, getString(R.string.chat_forward));
            final NoticesBean noticebean = new NoticesDao(ViewUDTPhotosActivity.this)
                    .getNoticeById(bean.getTaskId());
            dialog.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    CollectionManager.getInstance()
                            .addCollectionByNoticesBean(
                                    ViewUDTPhotosActivity.this,
                                    noticebean);
                    CustomLog.d(TAG, "收藏");
                }
            }, getString(R.string.collect_str));

            if (bean.getType() == FileTaskManager.NOTICE_TYPE_VEDIO_SEND) {
                CustomLog.d(TAG, "浏览类型:视频?图片" + bean.getType());
                dialog.addButtonThird(new BottomMenuWindow.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        CustomLog.d(TAG, "onMenuClicked");
                        // 存储到本地
                        savePhotoToSDCard(bean);

                    }
                }, getString(R.string.save_video_me));
            } else {
                dialog.addButtonThird(new BottomMenuWindow.MenuClickedListener() {
                    @Override
                    public void onMenuClicked() {
                        CustomLog.d(TAG, "onMenuClicked");
                        // 存储到本地
                        savePhotoToSDCard(bean);

                    }
                }, getString(R.string.save_pic));
            }


            dialog.addButtonForth(new BottomMenuWindow.MenuClickedListener() {
                @Override
                public void onMenuClicked() {
                    dialog.dismiss();
                }
            }, getString(R.string.btn_cancle));
        }
        dialog.show();
    }

    @Override
    public void finish() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
                ,WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        myHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewUDTPhotosActivity.super.finish();
                overridePendingTransition(0,0);
            }
        },100);
    }
}
