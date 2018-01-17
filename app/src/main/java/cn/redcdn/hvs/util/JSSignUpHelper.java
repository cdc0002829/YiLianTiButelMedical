package cn.redcdn.hvs.util;

import android.webkit.WebView;
import android.widget.TextView;
import cn.redcdn.log.CustomLog;

/**
 * Designed by guoyx on 2017/9/14.
 *
 * Native 请求注册 JS 代码
 */

public class JSSignUpHelper {
    private static final String TAG = JSSignUpHelper.class.getSimpleName();
    private WebView webView;
    private TextView textView;


    public void setWebview(WebView webview) {
        this.webView = webview;
    }
    


    public void onPaymentFinished(String obj) {
        CustomLog.d(TAG,"onPaymentFinished " + obj);
        if (webView != null) {
            webView.loadUrl("javascript:onPaymentFinished('" + obj  + "')");
        } else {
            CustomLog.d(TAG, "WebView 没有初始化");
        }
    }


    public void onEnterGroupFinshed(String obj) {
        CustomLog.d(TAG,"onEnterGroupFinshed " + obj);
        if (webView != null) {
            webView.loadUrl("javascript:onEnterGroupFinshed('" + obj + "')");
        } else {
            CustomLog.d(TAG, "WebView 没有初始化");
        }
    }
}
