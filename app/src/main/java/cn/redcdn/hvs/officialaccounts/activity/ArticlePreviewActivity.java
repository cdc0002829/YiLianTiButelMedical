package cn.redcdn.hvs.officialaccounts.activity;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import cn.redcdn.datacenter.offaccscenter.MDSAppGetArticleInfo;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.log.CustomLog;


/**
 * 　　　　　　　　┏┓　　　┏┓+ +
 * 　　　　　　　┏┛┻━━━┛┻┓ + +
 * 　　　　　　　┃　　　　　　　┃
 * 　　　　　　　┃　　　━　　　┃ ++ + + +
 * 　　　　　　 ████━████ ┃+
 * 　　　　　　　┃　　　　　　　┃ +
 * 　　　　　　　┃　　　┻　　　┃
 * 　　　　　　　┃　　　　　　　┃ + +
 * 　　　　　　　┗━┓　　　┏━┛
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃ + + + +
 * 　　　　　　　　　┃　　　┃　　　　Code is far away from bug with the animal protecting
 * 　　　　　　　　　┃　　　┃ + 　　　　神兽保佑,代码无bug
 * 　　　　　　　　　┃　　　┃
 * 　　　　　　　　　┃　　　┃　　+
 * 　　　　　　　　　┃　 　　┗━━━┓ + +
 * 　　　　　　　　　┃ 　　　　　　　┣┓
 * 　　　　　　　　　┃ 　　　　　　　┏┛
 * 　　　　　　　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　　　　　　　┃┫┫　┃┫┫
 * 　　　　　　　　　　┗┻┛　┗┻┛+ + + +
 * Created by chenghb on 2017/5/15.
 */

public class ArticlePreviewActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = ArticlePreviewActivity.class.getName();
    public final static String INTENT_DATA_ARTICLE_ID = "INTENT_DATA_ARTICLE_ID";
    private Button btn_back;
    private WebView articleWeb;
    private String webviewUrl = ""; //H5 页面地址
    private String articleId; //文章ID：
    private Button refresh;
    private LinearLayout no_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articlepreview);

        articleId = getIntent().getStringExtra(INTENT_DATA_ARTICLE_ID);
        CustomLog.d(TAG, "articleId" + articleId);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        articleWeb = (WebView) findViewById(R.id.articleWeb);
        no_content = (LinearLayout) findViewById(R.id.no_content);
        refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(this);
        initWebView();//初始化webview
//        if (articleId == "") {
//            no_content.setVisibility(View.VISIBLE);
//            articleWeb.setVisibility(View.INVISIBLE);
//        } else {
//            no_content.setVisibility(View.INVISIBLE);
//            articleWeb.setVisibility(View.VISIBLE);
//        }
        setWebviewUrl(articleId);


    }

    private void initWebView() {
        WebSettings webSettings = articleWeb.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        articleWeb.clearCache(true);
        articleWeb.destroyDrawingCache();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setTextZoom(100);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.refresh:
                setWebviewUrl(articleId);
                articleWeb.setWebViewClient(new WebViewClient() {
                    /*
                     *这个方法防止点击webview超链接
                     * */
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return true;
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        //加载完成,隐藏loading
                        refresh.clearAnimation();
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        //加载过程中显示loading
                        refresh.startAnimation(getRotateAnimation());
                    }

                });

                CustomLog.d(TAG, "刷新" + webviewUrl);
                break;
        }
    }

    private void setWebviewUrl(String articleId) {
        /**
         * http://testmedical.butel.com:8189/mws/externalservice/generateHTML?
         * id=9e41df813864441393e88a0589b901cf
         * &origin=pc
         * */
        //测试网

        Long currentTime = System.currentTimeMillis();
        webviewUrl = SettingData.Article_Preview_Url + "id=" + articleId + "&origin=pc?timestamp=" + currentTime;
        CustomLog.d(TAG, "webviewUrl" + webviewUrl);
        articleWeb.loadUrl(webviewUrl);
        CustomLog.d(TAG, "webviewUrl" + webviewUrl);


    }


    // 设置进度圈的动画
    private RotateAnimation mRotateAnimation;

    private RotateAnimation getRotateAnimation() {
        mRotateAnimation = null;
        mRotateAnimation = new RotateAnimation(0.0F, 360.0F, 1, 0.5F, 1,
                0.5F);
        mRotateAnimation.setFillAfter(false);
        mRotateAnimation.setRepeatCount(Animation.INFINITE);
        mRotateAnimation.setDuration(1000);
        mRotateAnimation.setInterpolator(new LinearInterpolator());

        return mRotateAnimation;
    }
}
