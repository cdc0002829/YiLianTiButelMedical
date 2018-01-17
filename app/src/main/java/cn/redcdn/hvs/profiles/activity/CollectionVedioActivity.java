package cn.redcdn.hvs.profiles.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.datacenter.collectcenter.DeleteCollectItems;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.collection.CollectionFileManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.view.CircleProgressBar;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.hvs.profiles.listener.MyDisplayImageListener;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/3/7.
 */
public class CollectionVedioActivity extends BaseActivity {

    boolean isDown = false;
    public static final String COLLECTION_VEDIO_DATA = "collection_vedio_data";

    private DataBodyInfo bean;
    // 视频播放控件
    private VideoView videoView;
    // 播放进度条
    private ProgressBar videoProgressBar;

    private TitleBar titlebar;
    private TextView savaTv;
    private ImageView videoIcon;
    private TextView videoName;
    private TextView videoTime;

    ImageLoadingListener mDisplayImageListener;

    private String remoteUrl;
    private String split1;
    private RelativeLayout collectionVideoRl;
    private LinearLayout collectionVideoLl;

    //满屏标记
    boolean isFullScreeen = false;
    private RelativeLayout titlebarVideo;
    private LinearLayout collectionVideoLl1;
    private ImageView nonVideoIv;

    private TextView firstTv;
    private TextView secondTv;
    private TextView thirdTv;
    private CircleProgressBar circle;
    private LoadVideo loadVideo;
    private ScrollView scrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_vedio);
        initView();
        videoView.setVisibility(View.VISIBLE);
        nonVideoIv.setVisibility(View.INVISIBLE);
        firstTv.setVisibility(View.INVISIBLE);
        secondTv.setVisibility(View.INVISIBLE);
        thirdTv.setVisibility(View.INVISIBLE);
        initData();

        mDisplayImageListener = new MyDisplayImageListener();
    }

    private void initView() {
        Intent i = getIntent();
        bean = (DataBodyInfo) i.getSerializableExtra(COLLECTION_VEDIO_DATA);
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        nonVideoIv = (ImageView) findViewById(R.id.non_video_iv);
        circle = (CircleProgressBar) findViewById(R.id.circle_progress_bar);
        firstTv = (TextView) findViewById(R.id.first_tv);
        secondTv = (TextView) findViewById(R.id.second_tv);
        thirdTv = (TextView) findViewById(R.id.third_tv);
        firstTv = (TextView) findViewById(R.id.first_tv);
        videoView = (VideoView) findViewById(R.id.video_view_collecion);
        videoIcon = (RoundImageView) findViewById(R.id.collection_video_icon);
        videoName = (TextView) findViewById(R.id.collection_video_name);
        videoTime = (TextView) findViewById(R.id.collection_video_time);
        collectionVideoRl = (RelativeLayout) findViewById(R.id.collection_video_rl);
        collectionVideoLl = (LinearLayout) findViewById(R.id.collection_video_ll);
        collectionVideoLl1 = (LinearLayout) findViewById(R.id.collection_video1_ll);
        titlebarVideo = (RelativeLayout) findViewById(R.id.titlebarVideo);
//        MediaController mc = new MediaController(this);//Video是我类名，是你当前的类
//        videoView.setMediaController(mc);//设置VedioView与MediaController相关联
        titlebar = getTitleBar();
        getTitleBar().enableBack();
        getTitleBar().setTitle(R.string.scan_vedio);
//        if (bean.getMessageTime().equals("")) {
        getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
//        } else {
//            getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    showDialogOther();
//                }
//            });
//        }
    }

    TextView cacleZhuanfa1;
    TextView saveTv;
    private Dialog dialog1;
    private View inflate1;

    private void showDialogOther() {
        dialog1 = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate1 = LayoutInflater.from(this).inflate(R.layout.zhuanfa_dialog3, null);
        //初始化控件
        cacleZhuanfa1 = (TextView) inflate1.findViewById(R.id.cancle1_zhuanfa_tv);
        saveTv = (TextView) inflate1.findViewById(R.id.save);
        saveTv.setOnClickListener(mbtnHandleEventListener);
        cacleZhuanfa1.setOnClickListener(mbtnHandleEventListener);
        //将布局设置给Dialog
        dialog1.setContentView(inflate1);


        //获取当前Activity所在的窗体
        Window dialogWindow = dialog1.getWindow();

        dialogWindow.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog1.show();//显示对话框
    }

    private void setNameAndIcon() {
        String groupName = bean.getGroupName();
        if (bean.getForwarderName() != null) {
            if (!groupName.equals("")) {
                videoName.setText(bean.getForwarderName() + "—" + groupName);
            } else {
                videoName.setText(bean.getForwarderName());
            }
        }
        if (!bean.getCollecTime().equals("")) {
            String collecTime = bean.getCollecTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date));
            Date curDate = new Date(System.currentTimeMillis());
            String format1 = format.format(curDate);
            if (format.format(d).equals(format1)) {
                videoTime.setText(getString(R.string.today_collect));
            } else {
                videoTime.setText(getString(R.string.collect_in) + format.format(d));
            }
        } else {
            String collecTime = bean.getMessageTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date));
            Date curDate = new Date(System.currentTimeMillis());
            String format1 = format.format(curDate);
            if (format.format(d).equals(format1)) {
                videoTime.setText(getString(R.string.today));
            } else {
                videoTime.setText(format.format(d));
            }
        }
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(bean.getForwarderHeaderUrl(),
                videoIcon,
                MedicalApplication.shareInstance().options,
                mDisplayImageListener);
    }

    String videoPath = null;

    GestureDetector mGestureDetector;

    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
            String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
            if (file.exists()) {
                Intent intent = new Intent();
                intent.setClass(CollectionVedioActivity.this, BigVideoActivity.class);
                intent.putExtra("video_path", videoPath);
                intent.putExtra("data", bean);
                startActivity(intent);
            } else {
                CustomToast.show(CollectionVedioActivity.this, getString(R.string.video_download), CustomToast.LENGTH_LONG);
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {

            super.onLongPress(e);
        }
    }

    private void initData() {
        mGestureDetector = new GestureDetector(CollectionVedioActivity.this, new MyOnGestureListener());
        remoteUrl = bean.getRemoteUrl();
        String[] split = remoteUrl.split("\\.");
        split1 = split[split.length - 1];
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
        videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
        if (file.exists()) {
            if (split1.equals("wmv") || split1.equals("avi") || split1.equals("asf")) {
                videoView.setVisibility(View.INVISIBLE);
                nonVideoIv.setVisibility(View.VISIBLE);
                firstTv.setVisibility(View.VISIBLE);
                secondTv.setVisibility(View.VISIBLE);
                thirdTv.setVisibility(View.VISIBLE);
                firstTv.setText(bean.getRemoteUrl().split("/")[bean.getRemoteUrl().split("/").length - 1]);
                if (bean.getMessageTime().equals("")) {
                    getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogOtherTwo();
                        }
                    });
                } else {
                    getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogOtherThree();
                        }
                    });
                }
            } else {
                Uri uri = Uri.parse(videoPath);
                videoView.setVideoURI(uri);
            }
        } else {
            ConnectivityManager con = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
            boolean internet = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            if (wifi | internet) {
                loadVideo = new LoadVideo();
                loadVideo.execute(bean.getRemoteUrl());

            } else {
                CustomToast.show(CollectionVedioActivity.this, getString(R.string.please_connect_web), CustomToast.LENGTH_LONG);
                CollectionVedioActivity.this.finish();
                videoView.suspend();
                return;
            }
        }
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                removeLoadingView();
                videoView.setVisibility(View.INVISIBLE);
                nonVideoIv.setVisibility(View.VISIBLE);
                firstTv.setVisibility(View.VISIBLE);
                secondTv.setVisibility(View.VISIBLE);
                thirdTv.setVisibility(View.VISIBLE);
                firstTv.setText(bean.getRemoteUrl().split("/")[bean.getRemoteUrl().split("/").length - 1]);
                if (bean.getMessageTime().equals("")) {
                    getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogOtherTwo();
                        }
                    });
                } else {
                    getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogOtherThree();
                        }
                    });
                }
                return true;
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                removeLoadingView();
                ConnectivityManager con = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
                boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
                boolean internet = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
                if (!wifi && !internet) {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                    if (file.exists()) {
                        if(loadVideo!=null && !loadVideo.isCancelled()
                                && loadVideo.getStatus() == AsyncTask.Status.RUNNING){
                            loadVideo.cancel(true);
                            loadVideo = null;
                        }
                        file.delete();
                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
                        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        ContentResolver mContentResolver = CollectionVedioActivity.this.getContentResolver();
                        String where = MediaStore.Video.Media.DATA + "='" + path + "'";
                        mContentResolver.delete(uri, where, null);
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri urii = Uri.fromFile(file);
                        intent.setData(urii);
                        CollectionVedioActivity.this.sendBroadcast(intent);
                    }
                    CustomToast.show(CollectionVedioActivity.this, getString(R.string.please_connect_web), CustomToast.LENGTH_LONG);
                    CollectionVedioActivity.this.finish();
                }
                try {
                    videoView.start();
                } catch (Exception e) {
                    videoView.setVisibility(View.INVISIBLE);
                    nonVideoIv.setVisibility(View.VISIBLE);
                    firstTv.setVisibility(View.VISIBLE);
                    secondTv.setVisibility(View.VISIBLE);
                    thirdTv.setVisibility(View.VISIBLE);

                    firstTv.setText(bean.getRemoteUrl().split("/")[bean.getRemoteUrl().split("/").length - 1]);
                }
                //音量控制,初始化定义
                final AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                videoView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mGestureDetector.onTouchEvent(event);

                        return true;
                    }
                });
            }
        });
        videoView
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer arg0) {
                        CustomLog.d("TAG", "播放完毕，退出播放界面");
                        videoView.start();
                    }
                });
        videoView.requestFocus();

        setNameAndIcon();
    }

    Dialog dialog3;
    View inflate3;
    TextView openThree;
    TextView saveThree;
    TextView cancelThree;

    private void showDialogOtherThree() {
        dialog3 = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate3 = LayoutInflater.from(this).inflate(R.layout.messege_dialog, null);
        //初始化控件
        openThree = (TextView) inflate3.findViewById(R.id.open_three);
        saveThree = (TextView) inflate3.findViewById(R.id.save_three);
        cancelThree = (TextView) inflate3.findViewById(R.id.cancel_three);
        openThree.setOnClickListener(mbtnHandleEventListener);
        saveThree.setOnClickListener(mbtnHandleEventListener);
        cancelThree.setOnClickListener(mbtnHandleEventListener);
        //将布局设置给Dialog
        dialog3.setContentView(inflate3);


        //获取当前Activity所在的窗体
        Window dialogWindow = dialog3.getWindow();

        dialogWindow.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog3.show();//显示对话框
    }

    private View inflate2;
    TextView openNoTv;
    TextView savaNoTv;
    TextView zhuanfaNoTv;
    TextView deleteNoTv;
    TextView cancleNoTv;
    Dialog dialog2;

    private void showDialogOtherTwo() {
        dialog2 = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate2 = LayoutInflater.from(this).inflate(R.layout.novideo_dialog, null);
        //初始化控件
        openNoTv = (TextView) inflate2.findViewById(R.id.open_no_tv);
        zhuanfaNoTv = (TextView) inflate2.findViewById(R.id.zhuanfa_no_tv);
        deleteNoTv = (TextView) inflate2.findViewById(R.id.delete_no_tv);
        cancleNoTv = (TextView) inflate2.findViewById(R.id.cancle_no_tv);
        savaNoTv = (TextView) inflate2.findViewById(R.id.save_no_tv);
        savaNoTv.setOnClickListener(mbtnHandleEventListener);
        zhuanfaNoTv.setOnClickListener(mbtnHandleEventListener);
        deleteNoTv.setOnClickListener(mbtnHandleEventListener);
        cancleNoTv.setOnClickListener(mbtnHandleEventListener);
        openNoTv.setOnClickListener(mbtnHandleEventListener);
        //将布局设置给Dialog
        dialog2.setContentView(inflate2);

        //获取当前Activity所在的窗体
        Window dialogWindow = dialog2.getWindow();

        dialogWindow.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog2.show();//显示对话框
    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.suspend();
        }
        if (isDown == true) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
            if (file.exists()) {
                file.delete();
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
                Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = CollectionVedioActivity.this.getContentResolver();
                String where = MediaStore.Video.Media.DATA + "='" + path + "'";
                mContentResolver.delete(uri, where, null);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri urii = Uri.fromFile(file);
                intent.setData(urii);
                CollectionVedioActivity.this.sendBroadcast(intent);
            }
            if(loadVideo!=null && !loadVideo.isCancelled()
                    && loadVideo.getStatus() == AsyncTask.Status.RUNNING){
                loadVideo.cancel(true);
                loadVideo = null;
            }

        }
    }

    private View inflate;
    private TextView zhuanfa;
    private TextView cacleZhuanfa;
    private TextView deleteZhuanfa;
    private Dialog dialog;

    private void showDialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.zhuanfa_dialog, null);
        //初始化控件
        zhuanfa = (TextView) inflate.findViewById(R.id.zhuanfa_tv);
        deleteZhuanfa = (TextView) inflate.findViewById(R.id.delete_zhuanfa_tv);
        cacleZhuanfa = (TextView) inflate.findViewById(R.id.cancle_zhuanfa_tv);
        savaTv = (TextView) inflate.findViewById(R.id.save_tv);
        savaTv.setOnClickListener(mbtnHandleEventListener);
        zhuanfa.setOnClickListener(mbtnHandleEventListener);
        deleteZhuanfa.setOnClickListener(mbtnHandleEventListener);
        cacleZhuanfa.setOnClickListener(mbtnHandleEventListener);
        //将布局设置给Dialog
        dialog.setContentView(inflate);

        //获取当前Activity所在的窗体
        Window dialogWindow = dialog.getWindow();

        dialogWindow.setGravity(Gravity.BOTTOM); //可设置dialog的位置
        dialogWindow.getDecorView().setPadding(0, 0, 0, 0); //消除边距
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.open_three:
                dialog3.dismiss();
                File file4 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                String videoPath4 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
                if (file4.exists()) {
                    IMCommonUtil.playVideo(getBaseContext(),
                            videoPath4);
                } else {
                    CustomToast.show(CollectionVedioActivity.this,getString(R.string.video_download), CustomToast.LENGTH_LONG);
                }
                break;
            case R.id.save_three:
                File file5 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                if (file5.exists()) {
                    CustomToast.show(CollectionVedioActivity.this, getString(R.string.save_video), CustomToast.LENGTH_LONG);
                } else {
                    new LoadVideo1().execute(bean.getRemoteUrl());
                }
                dialog3.dismiss();
                break;
            case R.id.cancel_three:
                dialog3.dismiss();
                break;
            case R.id.open_no_tv:
                dialog2.dismiss();
                File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
                if (file1.exists()) {
                    IMCommonUtil.playVideo(getBaseContext(),
                            videoPath);
                } else {
                    CustomToast.show(CollectionVedioActivity.this, getString(R.string.video_download), CustomToast.LENGTH_LONG);
                }
                break;
            case R.id.save_no_tv:
                File file2 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                if (file2.exists()) {
                    CustomToast.show(CollectionVedioActivity.this, getString(R.string.save_video), CustomToast.LENGTH_LONG);
                } else {
                    new LoadVideo1().execute(bean.getRemoteUrl());
                }
                dialog2.dismiss();
                break;
            case R.id.zhuanfa_no_tv:
                dialog2.dismiss();
                CollectionFileManager.getInstance().onCollectMsgForward(
                        CollectionVedioActivity.this, bean);
                break;
            case R.id.delete_no_tv:
                dialog2.dismiss();

                DeleteCollectItems deleteCollectItems = new DeleteCollectItems() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_suc), 5000);
                        CollectionManager.getInstance().deleteCollectionById(bean.getCollectionId());
                        CollectionVedioActivity.this.finish();

                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                        if (file.exists()) {
                            file.delete();
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
                            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            ContentResolver mContentResolver = CollectionVedioActivity.this.getContentResolver();
                            String where = MediaStore.Video.Media.DATA + "='" + path + "'";
                            mContentResolver.delete(uri, where, null);
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri urii = Uri.fromFile(file);
                            intent.setData(urii);
                            CollectionVedioActivity.this.sendBroadcast(intent);
                        }
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_fail), 5000);
                    }
                };
                String id = bean.getCollectionId();
                String nube = AccountManager.getInstance(this)
                        .getAccountInfo().getNube();
                String accessToken = AccountManager.getInstance(this)
                        .getAccountInfo().getAccessToken();
                deleteCollectItems.deleteCollectionItems(nube, id, accessToken);
                break;
            case R.id.zhuanfa_tv:
                dialog.dismiss();
                CollectionFileManager.getInstance().onCollectMsgForward(
                        CollectionVedioActivity.this, bean);
                break;
            case R.id.delete_zhuanfa_tv:
                DeleteCollectItems deleteCollectItems1 = new DeleteCollectItems() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        CustomToast.show(getApplicationContext(),getString(R.string.delete_collection_suc), 5000);
                        CollectionManager.getInstance().deleteCollectionById(bean.getCollectionId());
                        CollectionVedioActivity.this.finish();

                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                        if (file.exists()) {
                            file.delete();
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1;
                            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            ContentResolver mContentResolver = CollectionVedioActivity.this.getContentResolver();
                            String where = MediaStore.Video.Media.DATA + "='" + path + "'";
                            mContentResolver.delete(uri, where, null);
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri urii = Uri.fromFile(file);
                            intent.setData(urii);
                            CollectionVedioActivity.this.sendBroadcast(intent);
                        }
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        CustomToast.show(getApplicationContext(),getString(R.string.delete_collection_fail), 5000);
                    }
                };
                String id1 = bean.getCollectionId();
                String nube1 = AccountManager.getInstance(this)
                        .getAccountInfo().getNube();
                String accessToken1 = AccountManager.getInstance(this)
                        .getAccountInfo().getAccessToken();
                deleteCollectItems1.deleteCollectionItems(nube1, id1, accessToken1);
                break;
            case R.id.cancle_no_tv:
                dialog2.dismiss();
                break;
            case R.id.cancle_zhuanfa_tv:
                dialog.dismiss();
                break;
            case R.id.save_tv:
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                if (file.exists()) {
                    CustomToast.show(CollectionVedioActivity.this, getString(R.string.save_video), CustomToast.LENGTH_LONG);
                } else {
                    new LoadVideo1().execute(bean.getRemoteUrl());
                }
                dialog.dismiss();
                break;
            case R.id.cancle1_zhuanfa_tv:
                dialog1.dismiss();
                break;
            case R.id.save:
                File file3 = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                if (file3.exists()) {
                    CustomToast.show(CollectionVedioActivity.this, getString(R.string.save_video), CustomToast.LENGTH_LONG);
                } else {
                    new LoadVideo1().execute(bean.getRemoteUrl());
                }
                dialog1.dismiss();
                break;
        }
    }

    public static void scanIntoMediaStore(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(intent);
    }

    public class LoadVideo extends AsyncTask<String, Integer, Void> {
        private String s;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isDown = true;
            circle.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(String... params) {
            if(isCancelled()) return null;
            isDown = true;
            int count;
            for (int i = 0; i < params.length; i++) {
                try {
                    URL url = new URL(params[i]);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    //计算文件长度
                    int lenghtOfFile = conection.getContentLength();
                    // download the file
                    InputStream input = new BufferedInputStream(
                            url.openStream(), 8192);// 1024*8
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/medicalhvs/video"
                    );
                    if (f.isDirectory()) {
//                        System.out.println("exist!");
                    } else {
//                        System.out.println("not exist!");
                        f.mkdirs();
                    }
                    // Output stream
                    OutputStream output = new FileOutputStream(Environment
                            .getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);

                    byte data[] = new byte[1024];
                    int len1 = 0;
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count; //total = total + len1
                        publishProgress((int) ((total * 100) / lenghtOfFile));
                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                }
            }
            if(isCancelled()) return null;
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(isCancelled()) return;
            isDown = true;
            circle.setmProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isDown = false;
            circle.setVisibility(View.GONE);
            if (split1.equals("wmv") || split1.equals("avi") || split1.equals("asf")) {
                videoView.setVisibility(View.INVISIBLE);
                nonVideoIv.setVisibility(View.VISIBLE);
                firstTv.setVisibility(View.VISIBLE);
                secondTv.setVisibility(View.VISIBLE);
                thirdTv.setVisibility(View.VISIBLE);
                firstTv.setText(bean.getRemoteUrl().split("/")[bean.getRemoteUrl().split("/").length - 1]);
                if (bean.getMessageTime().equals("")) {
                    getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogOtherTwo();
                        }
                    });
                } else {
                    getTitleBar().enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogOtherThree();
                        }
                    });
                }
            } else {
                Uri uri = Uri.parse(videoPath);
                videoView.setVideoURI(uri);
            }
            scanIntoMediaStore(CollectionVedioActivity.this, Environment
                    .getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
        }
    }

    public class LoadVideo1 extends AsyncTask<String, Integer, Void> {
        private String s;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            CustomToast.show(CollectionVedioActivity.this, getString(R.string.download_vedio), CustomToast.LENGTH_SHORT);
        }

        @Override
        protected Void doInBackground(String... params) {
            int count;
            for (int i = 0; i < params.length; i++) {
                try {
                    URL url = new URL(params[i]);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();

                    // download the file
                    InputStream input = new BufferedInputStream(
                            url.openStream(), 8192);// 1024*8
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "medicalhvs"
                    );
                    if (f.isDirectory()) {

                    } else {

                        f.mkdirs();
                    }
                    // Output stream
                    OutputStream output = new FileOutputStream(Environment
                            .getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);

                    byte data[] = new byte[1024];
                    while ((count = input.read(data)) != -1) {
                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();

                } catch (Exception e) {
                    Log.e("Error: ", e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            CustomToast.show(CollectionVedioActivity.this, getString(R.string.save_video), CustomToast.LENGTH_LONG);
            scanIntoMediaStore(CollectionVedioActivity.this, Environment
                    .getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
        }
    }

}
