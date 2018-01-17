package cn.redcdn.hvs.officialaccounts.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.offaccscenter.MDSAppGetArticleInfo;
import cn.redcdn.datacenter.offaccscenter.data.VideoInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.activity.EmbedWebViewActivity;
import cn.redcdn.hvs.officialaccounts.jsinterface.JSLocalObj;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.im.activity.EmbedWebViewActivity.KEY_PARAMETER_URL;

/**
 * Created by chenghb on 2017/5/4.
 */

public class ArticleActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = ArticleActivity.class.getName();
    public final static String INTENT_DATA_ARTICLE_ID = "INTENT_DATA_ARTICLE_ID";
    private MDSAppGetArticleInfo getArticleInfo;
    private WebView mWebView;
    private Button btn_back;
    private String webviewUrl = ""; //H5 页面地址
    private int accessType; //1.不加密 2.加密
    private String accessPassword; //加密密码
    private String articleId; //文章ID：根据文章ID获取视频ID、视频名称、H5地址、是否加密、视频类型
    private ImageView webLoading;
    private RelativeLayout article_rl;
    private LinearLayout no_content;
    private TextView article_delate;
    private String versionNo = "2.7.0";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        MedicalApplication.addDestoryActivity(ArticleActivity.this, VideoPublishActivity.ARTICLE_ACTIVITY);
        articleId = getIntent().getStringExtra(INTENT_DATA_ARTICLE_ID);
        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(this);
        webLoading = (ImageView) findViewById(R.id.webLoading);
        article_rl = (RelativeLayout) findViewById(R.id.article_rl);
        mWebView = (WebView) findViewById(R.id.articleWeb);
        no_content = (LinearLayout) findViewById(R.id.no_content);
        article_delate = (TextView) findViewById(R.id.article_delate);
        initWebView();//初始化webview
        requestArticleInfo();//请求内容详情页接口
    }

    private void requestArticleInfo() {

        getArticleInfo = new MDSAppGetArticleInfo() {

            @Override
            protected void onSuccess(VideoInfo responseContent) {
                super.onSuccess(responseContent);

                webviewUrl = responseContent.getWebViewUrl();
                CustomLog.d(TAG, "webviewUrl" + webviewUrl);
                accessType = responseContent.getAccessType();//加密类型
                accessPassword = responseContent.getAccessPassword();//加密密码
                CustomLog.d(TAG, "accessPassword" + accessPassword);
                Long currentTime = System.currentTimeMillis();
                mWebView.loadUrl(webviewUrl + "&timestamp=" + currentTime);
                CustomLog.d(TAG, "accessPassword" + accessPassword);
                mWebView.setWebViewClient(new WebViewClient() {
                    /*
                     *这个方法防止点击webview超链接
                     * */
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent intent = new Intent(ArticleActivity.this, EmbedWebViewActivity.class);
                        CustomLog.d(TAG, "超链接" + url);
                        intent.putExtra(KEY_PARAMETER_URL, url);
                        startActivity(intent);
                        return true;


                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        //加载完成,隐藏loading
                        hideWebLoading();
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        //加载过程中显示loading
                        showWebLoading();
                    }

                });
                handlePasswdDialog();//密码输入框
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(MedicalApplication.getContext()).tokenAuthFail(statusCode);
                } else if (statusCode == MDSErrorCode.MDS_EXTERNAL_REWUEDT_UNUAUAL) {
                    mWebView.setVisibility(View.INVISIBLE);
                    no_content.setVisibility(View.VISIBLE);
                    article_delate.setText(R.string.artical_delate);
                    article_rl.setVisibility(View.INVISIBLE);
                } else {
                    CustomToast.show(MedicalApplication.getContext(), statusInfo, Toast.LENGTH_LONG);
                    finish();
                }
            }
        };

        getArticleInfo.appGetArticleInfo(AccountManager.getInstance(MedicalApplication.context).getMdsToken(), articleId,versionNo);
    }

    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mWebView.clearCache(true);
        mWebView.destroyDrawingCache();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setTextZoom(100);
        mWebView.addJavascriptInterface(new JSLocalObj(ArticleActivity.this) {
            @Override
            public void onChooseContent(String contentId, String contentName, int type, int permissions) {

            }

            @Override
            public void onExpandVideoWindow(int operation) {

            }
        }, JSLocalObj.JS_INTERFACE_NAME);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
        }
    }

    private void handlePasswdDialog() {
        //当加密类型为不加密,直接跳转到VideoPublishActivity
        SharedPreferences preferences = getSharedPreferences("data", MODE_APPEND);
        SharedPreferences.Editor editor = getSharedPreferences("data", MODE_APPEND).edit();
        //新拼接的字符串 nube+文章id
        String newKey = AccountManager.getInstance(MedicalApplication.context).getNube() + "_" + articleId;
        CustomLog.d(TAG, "newPass" + newKey);
        String newPassWord = preferences.getString(newKey, "");

        /**
         * 当加密类型==1 或者是sp取出的密码和获取到的密码相等的话
         * */
        if (accessType == 1 || accessPassword.equalsIgnoreCase(newPassWord)) {

        } else {
            editor.remove(newKey);//如果密码有做修改,删除对应项是调用editor.remove(articleId+nube),
            Intent intentDialigActivity = new Intent(ArticleActivity.this, DialogActivity.class);
            intentDialigActivity.putExtra(INTENT_DATA_ARTICLE_ID, articleId);
            intentDialigActivity.putExtra("accessPassword", accessPassword);
            CustomLog.d(TAG, "accessPassword" + accessPassword);
            startActivity(intentDialigActivity);
        }
    }

    //显示加载webview的loading的动画
    private void showWebLoading() {
        article_rl.setVisibility(View.VISIBLE);
        webLoading.post(new Runnable() {
            @Override
            public void run() {
                webLoading.startAnimation(getRotateAnimation());
            }
        });
    }

    //隐藏webview的loading动画
    private void hideWebLoading() {
        if (article_rl.getVisibility() != View.GONE) {
            article_rl.setVisibility(View.GONE);
            webLoading.clearAnimation();
        }
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
