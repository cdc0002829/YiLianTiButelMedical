package cn.redcdn.hvs.profiles.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.collection.CollectionFileManager;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;

/**
 * Created by Administrator on 2017/5/25.
 */

public class BigVideoActivity extends BaseActivity {

    private VideoView videoView;
    private String videoPath;
    private RelativeLayout rl;
    private DataBodyInfo bean;
    private String split1;
    private String remoteUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_big);
        final AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 11, 0);
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        videoView = (VideoView) findViewById(R.id.video_v);
        rl = (RelativeLayout) findViewById(R.id.rl);
        videoPath = getIntent().getStringExtra("video_path");
        bean = (DataBodyInfo) getIntent().getSerializableExtra("data");
        remoteUrl = bean.getRemoteUrl();
        String remoteUrl = bean.getRemoteUrl();
        String[] split = remoteUrl.split("\\.");
        split1 = split[split.length - 1];
        videoView.setVideoPath(videoPath);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });

        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BigVideoActivity.this.finish();
                if (videoView != null) {
                    videoView.suspend();
                }
            }
        });
        rl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog();
                return true;
            }
        });
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.zhuanfa_tv:
                CollectionFileManager.getInstance().onCollectMsgForward(
                        BigVideoActivity.this, bean);
                dialog.dismiss();
                break;
            case R.id.save_tv:
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
                if (file.exists()) {
                    CustomToast.show(BigVideoActivity.this, getString(R.string.save_video), CustomToast.LENGTH_LONG);
                } else {
                    new LoadVideo1().execute(bean.getRemoteUrl());
                }
                dialog.dismiss();
                break;
            case R.id.cancle_zhuanfa_tv:
                dialog.dismiss();
                break;
        }
    }

    private View inflate;
    private TextView savaTv;
    private TextView zhuanfa;
    private TextView cacleZhuanfa;
    private Dialog dialog;

    private void showDialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.big_video_dialog, null);
        //初始化控件
        zhuanfa = (TextView) inflate.findViewById(R.id.zhuanfa_tv);
        cacleZhuanfa = (TextView) inflate.findViewById(R.id.cancle_zhuanfa_tv);
        savaTv = (TextView) inflate.findViewById(R.id.save_tv);
        savaTv.setOnClickListener(mbtnHandleEventListener);
        zhuanfa.setOnClickListener(mbtnHandleEventListener);
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

    public class LoadVideo1 extends AsyncTask<String, Integer, Void> {
        private String s;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            CustomToast.show(BigVideoActivity.this, getString(R.string.download_vedio), CustomToast.LENGTH_SHORT);
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
            removeLoadingView();
            CustomToast.show(BigVideoActivity.this, getString(R.string.save_vedio_to_medicalhvs), CustomToast.LENGTH_LONG);
            scanIntoMediaStore(BigVideoActivity.this, Environment
                    .getExternalStorageDirectory().getAbsolutePath() + "/medicalhvs/video" + "/" + CommonUtil.string2MD5(remoteUrl) + "." + split1);
        }
    }

    public static void scanIntoMediaStore(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(filePath)));
        context.sendBroadcast(intent);
    }

}
