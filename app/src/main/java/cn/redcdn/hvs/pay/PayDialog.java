package cn.redcdn.hvs.pay;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.head.fragment.HeadFragment;
import cn.redcdn.hvs.im.activity.EmbedWebViewActivity;
import cn.redcdn.hvs.im.activity.ViewImages.PhotoView;
import cn.redcdn.hvs.im.activity.ViewImages.PhotoViewAttacher;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;
import cn.redcdn.hvs.profiles.SignUpActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.log.CustomLog;

public class PayDialog extends BaseActivity implements
        View.OnClickListener {
    private String tag = PayDialog.class.getName();

    ImageButton reLoginBtn;
    Button ignoreBtn;
    private String articleId; //文章ID：根据文章ID获取视频ID、视频名称、H5地址、是否加密、视频类型
    private String activityUrl;
    private String activityPic;
    private ImageView originalIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_dialog);
        reLoginBtn = (ImageButton) findViewById(R.id.del);
        reLoginBtn.setOnClickListener(this);
        ignoreBtn = (Button) findViewById(R.id.ma);
        ignoreBtn.setOnClickListener(this);
        articleId = getIntent().getStringExtra(VideoPublishActivity.INTENT_DATA_ARTICLE_ID);
        activityUrl = getIntent().getStringExtra(SignUpActivity.KEY_PARAMETER_URL);
        activityPic = getIntent().getStringExtra(VideoPublishActivity.PAY_DIALOG_IMAGE);
        originalIcon = (ImageView) findViewById(R.id.pay_dialog_imageView);
        MedicalApplication.addDestoryActivity(PayDialog.this,VideoPublishActivity.PAY_DIALOG_ACTIVITY);
        if (!TextUtils.isEmpty(activityPic)){
            Glide.with(this).load(activityPic)
                .placeholder(R.drawable.paydialog_default_pic)
                .error(R.drawable.paydialog_error_pic)
                            .into(originalIcon);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent =new Intent();
        switch (v.getId()) {
            case R.id.del:
                finish();
                TimerTask task = new TimerTask(){
                    public void run(){
                        //execute the task
                        MedicalApplication.destoryActivity(VideoPublishActivity.VIDEO_PUBLISH_ACTIVITY);
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 10);
                break;
            case R.id.ma:
                CustomLog.d(TAG,"点击我要观看，跳往SignUpActivity");
                if (CommonUtil.getNetWorkType(PayDialog.this) == -1){
                    CustomToast.show(PayDialog.this,getString(R.string.net_error_wait_try_again),CustomToast.LENGTH_SHORT);
                }else {
                    intent.setClass(PayDialog.this, SignUpActivity.class);
                    intent.putExtra(SignUpActivity.KEY_PARAMETER_URL, activityUrl);
                    intent.putExtra(VideoPublishActivity.INTENT_DATA_ARTICLE_ID, articleId);
                    startActivity(intent);
                    finish();
                    MedicalApplication.destoryActivity(VideoPublishActivity.VIDEO_PUBLISH_ACTIVITY);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TimerTask task = new TimerTask(){
            public void run(){
                //execute the task
                MedicalApplication.destoryActivity(VideoPublishActivity.VIDEO_PUBLISH_ACTIVITY);
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 10);
    }

}