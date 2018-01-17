package cn.redcdn.hvs.profiles;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.util.EnterGroupUtil;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;
import cn.redcdn.hvs.officialaccounts.jsinterface.JSLocalObj;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.JSSignUpHelper;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;
import com.pingplusplus.android.Pingpp;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;

import static com.unionpay.sdk.ab.mContext;

public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = SignUpActivity.class.getSimpleName();

    public static String KEY_PARAMETER_URL = "SignUpActivity.loadurl";
    public static String KEY_PARAMETER_ARTICLE_ID = "INTENT_DATA_ARTICLE_ID";
    private static final int MSG_SHOW_TOAST = 4;
    private TextView tvtitle;
    private String url = null;
    private String title = null;
    private String currRequestId;
    JSSignUpHelper jsSignUpHelper = null;
    private WebView mWebView;
    private Button btnback;
    private String webTitle = "";

    private Activity activity;

    private int webviewIndex = 0;
    private HashMap titleMap;
    private String articalID;
    EnterGroupUtil enterGroupUtil = null;
    private String paymentState = "";
    private Handler mHandler;
    private final int TIMEOUT_ERROR = 5247;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private final int TIMEOUT = 10000;
    private LinearLayout timeOutView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_SHOW_TOAST:
                        CustomToast.show(getApplicationContext(), msg.obj.toString(),
                            Toast.LENGTH_LONG);
                        break;
                    case TIMEOUT_ERROR:
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer.purge();
                            mTimer = null;
                        }
                        if (mTimerTask != null) {
                            mTimerTask.cancel();
                            mTimerTask = null;
                        }

                        if (mWebView.getProgress() < 100) {
                            //加载超时界面
                            loadTimeOutView();
                        }



                    default:
                }
            }
        };

        activity = this;

        tvtitle = (TextView) findViewById(R.id.tvtitle);

        btnback = (Button) findViewById(R.id.btnback);
        btnback.setOnClickListener(this);

        // 加载需要显示的网页
        url = getIntent().getStringExtra(KEY_PARAMETER_URL);
        articalID = getIntent().getStringExtra(KEY_PARAMETER_ARTICLE_ID);

        timeOutView = (LinearLayout) findViewById(R.id.time_out_view);

        // 设置WebView属性
        mWebView = (WebView) findViewById(R.id.sign_up_webview);
        WebSettings setting = mWebView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setLoadWithOverviewMode(true);
        setting.setUseWideViewPort(true);
        setting.setBuiltInZoomControls(false);
        setting.setSupportZoom(false);
        setting.setDisplayZoomControls(false);
        setting.setDomStorageEnabled(true);
        setting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mWebView.addJavascriptInterface(new SignUpJSInterface(), JSLocalObj.JS_INTERFACE_NAME);

        titleMap = new HashMap();

        if (!TextUtils.isEmpty(url)) {
            LogUtil.d("加载需要显示的网页,url=" + url);
            mWebView.loadUrl(url);
            mWebView.setWebViewClient(new WebViewClient() {
                @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);

                    CustomLog.d(TAG, "current webview URL : " + url);

                    webviewIndex++;
                    if (titleMap.containsKey(webviewIndex)) {
                        String title = (String) titleMap.get(webviewIndex);
                        tvtitle.setText(title);
                    } else {
                        tvtitle.setText("");
                    }

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
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    tvtitle.setText(webTitle);

                    if (mWebView.getProgress() == 100) {
                        timeOutView.setVisibility(View.GONE);
                    }

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


                @Override
                public void onReceivedError(WebView view, int errorCode,
                                            String description, String failingUrl) {
                    // Auto-generated method stub
                    super.onReceivedError(view, errorCode, description, failingUrl);

                    timeOutView.setVisibility(View.VISIBLE);
                    TextView textView = (TextView) timeOutView.findViewById(R.id.article_delate);
                    textView.setText("网络错误");

                    CustomLog.d(TAG, "onReceivedError() start");
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer.purge();
                    }
                }

            });
        } else {
            CustomLog.d("SignUpActivity", "URL 为空");
        }

        jsSignUpHelper = new JSSignUpHelper();
        jsSignUpHelper.setWebview(mWebView);

    }


    /**
     * 加载请求失败的逻辑
     */
    private void loadTimeOutView() {
        CustomLog.d(TAG,"loadTimeOutView");
        timeOutView.setVisibility(View.VISIBLE);
    }


    @Override public void onClick(View v) {
        CustomLog.d(TAG,"onClick");
        switch (v.getId()) {
            case R.id.btnback:
                preperExit();
                break;
        }
    }


    class SignUpJSInterface {
        private static final String TAG_JS = "javascript";


        /**
         * 确认支付
         */
        @JavascriptInterface
        public void confirmPayment(String payInfo) {
            CustomLog.d(TAG, "confirmPayment received");

            if (TextUtils.isEmpty(payInfo)) {
                showMsg("请求出错", "请检查URL", "URL无法获取charge");
                return;
            }
            CustomLog.d("service return: ", payInfo);
            String chargeData = "";
            try {
                JSONObject dataObject = new JSONObject(payInfo);
                chargeData = dataObject.getString("payInfo");
                currRequestId = dataObject.optString("requestId", "");

            } catch (Exception ex) {
                Log.e("charge", ex.getMessage());
            }
            CustomLog.d(TAG, "parse chargeData:" + chargeData + " requestId:" + currRequestId);
            //除QQ钱包外，其他渠道调起支付方式：
            //参数一：Activity  当前调起支付的Activity
            //参数二：data  获取到的charge或order的JSON字符串

            if (TextUtils.isEmpty(chargeData)) {
                CustomLog.e(TAG, "支付信息为空");
                return;
            }
            Pingpp.createPayment(SignUpActivity.this, chargeData);
        }


        /**
         * 进入筹备群组
         */
        @JavascriptInterface
        public void enterPreparingGroup(String groupJson) {
            CustomLog.d(TAG, "enterPreparingGroup start ");
            String groupId = "";
            try {
                JSONObject object = new JSONObject(groupJson);
                groupId = object.optString("groupId");
                currRequestId = object.optString("requestId", "");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (TextUtils.isEmpty(groupId)) {
                CustomLog.e(TAG, "groupId is null");
                return;
            }
            CustomLog.d(TAG, "enterPreparingGroup gid:" + groupId
                + " requestId:" + currRequestId);
            if (enterGroupUtil == null) {
                enterGroupUtil = new EnterGroupUtil(SignUpActivity.this, groupId);
                enterGroupUtil.setEnterGroupListener(new EnterGroupUtil.EnterGroupListener() {
                    @Override
                    public void OnSuccess() {
                        onReportSucc();
                        activity.finish();
                    }


                    @Override
                    public void OnFailed(int statusCode, String statusInfo) {
                        onReportFailed(statusCode, statusInfo);
                    }
                });
            }
            enterGroupUtil.enterGroup();
        }


        @JavascriptInterface
        public void signUpCompleted() {
            CustomLog.d(TAG, "signUpCompleted()");
            jumpToTargetView();
            activity.finish();
        }


        @JavascriptInterface
        public void setPayProgressTitle(String titleJson) {
            CustomLog.d(TAG, "setPayProgressTitle");
            String title = "";
            try {
                JSONObject titleJsonObj = new JSONObject(titleJson);
                title = titleJsonObj.getString("title");
                titleMap.put(webviewIndex, title);
                webTitle = title;

                CustomLog.d(TAG, title);

            } catch (JSONException e) {
                e.printStackTrace();
                CustomLog.e(TAG, e.toString());
            }

        }


        @JavascriptInterface
        public void showToast(String toastMsg) {
            CustomLog.d(TAG, "showToast:" + toastMsg);
            if (!TextUtils.isEmpty(toastMsg)) {
                Message msg = new Message();
                msg.what = MSG_SHOW_TOAST;
                msg.obj = toastMsg;
                mHandler.sendMessage(msg);
            }
        }


        @JavascriptInterface
        public void writeLog(String param) {
            CustomLog.d(TAG,"writeLog start");
            try {
                JSONObject logJSON = new JSONObject(param);
                int type = logJSON.getInt("type");
                String msg = logJSON.getString("msg");

                if (type == 0) {
                    CustomLog.i(TAG_JS, msg);
                } else if (type == 1) {
                    CustomLog.d(TAG_JS, msg);
                } else if (type == -1) {
                    CustomLog.e(TAG_JS, msg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                CustomLog.e(TAG, e.toString());
            }
        }

    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        CustomLog.d(TAG,"onBackPressed");
        preperExit();
    }


    private void preperExit() {
        switch (paymentState) {
            case "success":
                jumpToTargetView();
                finish();
                break;
            case "fail":
                jumpToTargetView();
                finish();
                break;
            case "cancel":
                jumpToTargetView();
                finish();
                break;
            case "invalid":
                jumpToTargetView();
                finish();
                break;
            default:
                navigateWebView();

        }
    }


    private void navigateWebView() {
        if (mWebView.canGoBack()) {
            webviewIndex = webviewIndex - 2;
            mWebView.goBack();
        } else {
            jumpToTargetView();
            finish();
        }
    }


    private void onReportSucc() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestId", currRequestId);
            jsonObject.put("rc", 0);
            jsonObject.put("desc", "加入群组成功");
//            jsSignUpHelper.onEnterGroupFinshed(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void onReportFailed(int errorCode, String desc) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("requestId", currRequestId);
            jsonObject.put("rc", errorCode);
            jsonObject.put("desc", desc);
            jsSignUpHelper.onEnterGroupFinshed(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void showMsg(String title, String msg1, String msg2) {
        String str = title;
        if (null != msg1 && msg1.length() != 0) {
            str += "\n" + msg1;
        }
        if (null != msg2 && msg2.length() != 0) {
            str += "\n" + msg2;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(str);
        builder.setTitle("提示");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }


    /**
     * onActivityResult 获得支付结果，如果支付成功，服务器会收到ping++ 服务器发送的异步通知。
     * -1 payment failed
     * -2 user canceld
     * -3 payment plugin not installed
     * result结果：
     * "success" - payment succeed
     * "fail"    - payment failed
     * "cancel"  - user canceld
     * "invalid" - payment plugin not installed
     *
     * 最终支付成功根据异步通知为准
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //支付页面返回处理
        if (requestCode == Pingpp.REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");
                paymentState = result;
                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
                CustomLog.d(TAG, "支付回调：result:" + result + " errorMsg:"
                    + errorMsg + " " + extraMsg);
                onReportPaymentResult(result, "errorMsg:" + errorMsg + " extraMsg:" + extraMsg);
            }
        } else {
            CustomLog.d(TAG, "payReslut requestCode:" + requestCode + " resultCode:" + requestCode);
        }
    }


    /**
     * @param result 0:success -1:fail -2:cancel -3:invalid
     */
    private void onReportPaymentResult(String result, String resultDesc) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("requestId", currRequestId);
            switch (result) {
                case "success":
                    jsonObject.put("rc", 0);
                    jsonObject.put("desc", "支付成功");
                    break;
                case "fail":
                    jsonObject.put("rc", -1);
                    jsonObject.put("desc", resultDesc);
                    break;
                case "cancel":
                    jsonObject.put("rc", -2);
                    jsonObject.put("desc", resultDesc);
                    break;
                case "invalid":
                    jsonObject.put("rc", -3);
                    jsonObject.put("desc", resultDesc);
                    break;
                default:
                    jsonObject.put("rc", -2);
                    jsonObject.put("desc", resultDesc);
            }
            jsSignUpHelper.onPaymentFinished(jsonObject.toString());

        } catch (JSONException e) {
            CustomLog.e(TAG, "onReportPaymentResult error " + e.toString());
        }
    }


    private void jumpToTargetView() {
        Intent intent = new Intent(SignUpActivity.this, VideoPublishActivity.class);
        intent.putExtra(KEY_PARAMETER_ARTICLE_ID, articalID);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CustomLog.d(TAG, "onDestory");
        currRequestId = "";
        jsSignUpHelper = null;
        activity = null;
        enterGroupUtil = null;
        timeOutView.setVisibility(View.GONE);
    }
}
