package cn.redcdn.hvs.accountoperate.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

public class ServiceAgreementActivity extends BaseActivity {
    private WebView servicewebView;
    private Dialog mLoadingDialog = null;
    private ProgressDialog dialog;
    private String HELP_URL;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private final int TIMEOUT_ERROR = 5247;
    private final int TIMEOUT = 10000;
    private String tag = ServiceAgreementActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_agreement);
        servicewebView = (WebView) findViewById(R.id.service_agreement_webview);
        init();
    }

    @Override
    protected void showLoadingView(String message,
                                   DialogInterface.OnCancelListener listener) {
        CustomLog.i(TAG, "HelpFeedbackActivity::showLoadingDialog() msg: " + message);
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
        mLoadingDialog = CommonUtil.createLoadingDialog(this, message, listener);
        try {
            mLoadingDialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }

    protected void removeLoadingView() {
        CustomLog.i(TAG, "HelpFeedbackActivity::removeLoadingView()");
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == TIMEOUT_ERROR) {
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                    mTimer = null;
                }
                if (mTimerTask != null) {
                    mTimerTask.cancel();
                    mTimerTask = null;
                }
                if (servicewebView.getProgress() < 100) {
                    System.out.println("服务协议 ，webView加载超时");
                    ServiceAgreementActivity.this.removeLoadingView();
                    CustomToast.show(ServiceAgreementActivity.this, getString(R.string.web_not), Toast.LENGTH_LONG);
                    ServiceAgreementActivity.this.finish();
                }
                if (servicewebView.getProgress() == 100) {
                    ServiceAgreementActivity.this.removeLoadingView();
                }

            }
        }

        ;


    };

    protected void init() {

        servicewebView = (WebView) this.findViewById(R.id.service_agreement_webview);
        servicewebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                CustomLog.e("shouldOverrideUrlLoading", "二级页面");
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                super.onPageStarted(view, url, favicon);
                System.out.println("服务协议： onPageStarted ");
                ServiceAgreementActivity.this.showLoadingView(getString(R.string.data_loading), new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        CustomToast.show(ServiceAgreementActivity.this,
                               getString(R.string.cancel_load), Toast.LENGTH_LONG);
                    }
                });

                mTimer = new Timer();
                mTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = TIMEOUT_ERROR;
                        mHandler.sendMessage(msg);
                    }
                };
                mTimer.schedule(mTimerTask, TIMEOUT);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // TODO Auto-generated method stub
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                }
                CustomToast.show(ServiceAgreementActivity.this, getString(R.string.please_check_web), Toast.LENGTH_LONG);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
                System.out.println("服务协议  onPageFinished");
                ServiceAgreementActivity.this.removeLoadingView();
                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                    mTimer = null;
                }

                if (mTimerTask != null) {
                    mTimerTask.cancel();
                    mTimerTask = null;
                }
            }

        });
        servicewebView.loadUrl(SettingData.getInstance().SERVICEAGREEMENT_URL);
    }

    @Override
    public void todoClick(int i) {
        // TODO Auto-generated method stub
        super.todoClick(i);
        switch (i) {

        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        CustomLog.d(tag, "服务协议页面onPause");
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        CustomLog.d(tag, "服务协议页面finish");
    }

    // 设置回退
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (servicewebView.canGoBack()) {
                servicewebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);

    }

}
