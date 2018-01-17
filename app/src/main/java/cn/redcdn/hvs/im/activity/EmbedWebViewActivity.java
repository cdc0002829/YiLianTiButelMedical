package cn.redcdn.hvs.im.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.butel.connectevent.utils.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.adapter.ChatListAdapter;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.officialaccounts.jsinterface.JSLocalObj;
import cn.redcdn.hvs.util.TitleBar;

/**
 * Desc
 * Created by wangkai on 2017/3/1.
 */

public class EmbedWebViewActivity extends BaseActivity implements View.OnClickListener {

    public static String KEY_PARAMETER_URL = "EmbedWebViewActivity.loadurl";
    public static String KEY_PARAMETER_TITLE = "EmbedWebViewActivity.title";
    private TitleBar titleBar = null;
    private WebView webview = null;
    private Button btnback;
    private TextView tvtitle;
    private String url = null;
    private String title = null;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.begin("");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        btnback = (Button) findViewById(R.id.btnback);
        tvtitle = (TextView) findViewById(R.id.tvtitle);
        btnback.setOnClickListener(this);

        // 加载需要显示的网页
        url = getIntent().getStringExtra(KEY_PARAMETER_URL);
        title = getIntent().getStringExtra(KEY_PARAMETER_TITLE);

        initTitleBar();

        webview = (WebView) this.findViewById(R.id.webview);


        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setIcon(R.drawable.internal_app_icon);

        // 设置WebView属性
        WebSettings setting = webview.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setLoadWithOverviewMode(true);
        setting.setUseWideViewPort(true);
        setting.setBuiltInZoomControls(true);

        // 解决有的网页打不开问题，例如：http://m.uczzd.cn/webapp/webview/article/news.
        //	html?aid=15734212603657049026&cid=100&zzd_from=uc-iflow&uc_param_str=dnnivebichfrmin
        //	tcpgieiwidsud&recoid=13905452870463883551&readId=478333611b0deb91bdc9963e939
        //	dcecd&rd_type=share&tt_from=uc_btn&pagetype=share&refrd_id=8d34f7fe1b4895c5405
        //	eb480584ae65b&app=uc-iflow&ve=1.8.0.0&sn=7269617270936565216
        //  TODO:MLK 2015.10.10 通过设置useragent可以打开上面的链接，但会导致今日头条页面装载的是PC的页面，不是期望的Web页面
        //  推测：服务器端根据不同的agent动态返回不同的页面元素,网页打不开是因为无法处理'ucweb:'自定义的Schema
        //  可在shouldOverrideUrlLoading方法中对自定义的Schema做过滤，页面亦可展示完整
        //  故删除此处的UserAgent设置
        //  setting.setUserAgentString("Chrome");

        setting.setDomStorageEnabled(true);
        setting.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        // 设置Web视图
        webview.setWebViewClient(new CustomWebViewClient());
        webview.setDownloadListener(new MyWebViewDownLoadListener());

        webview.addJavascriptInterface(new JSLocalObj(this) {
            @Override
            public void onChooseContent(String contentId, String contentName, int type, int permissions) {

            }

            @Override
            public void onExpandVideoWindow(int operation) {

            }
        },JSLocalObj.JS_INTERFACE_NAME);

        // TODO:
        if (ChatListAdapter.ACTIVITY_FLAG.equals(title)) {
            WebChromeClient webChromeClient = new WebChromeClient() {
                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);

                   tvtitle.setText(title);
                }

            };
            webview.setWebChromeClient(webChromeClient);
        }

        if (!TextUtils.isEmpty(url)) {
            LogUtil.d("加载需要显示的网页,url=" + url);
            webview.loadUrl(url);
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    String r = view.getTitle();
                    if (r.equals("")) {
                        tvtitle.setText(R.string.collection_xiangqing);
                    } else {
                        tvtitle.setText(r);
                    }
                }
            });
        } else {
//			webview.loadUrl("file:///android_asset/html/index.html");
            webview.loadUrl("http://www.baidu.com/");
        }
        // String title = getIntent().getStringExtra(KEY_PARAMETER_TITLE);
        // if(!TextUtils.isEmpty(title)){
        // getSupportActionBar().setTitle(title);
        // }
        LogUtil.end("");
    }

    @Override
    protected void onResume() {
        LogUtil.begin("");
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webview.onResume(); // 恢复网页中正在播放的视频
        }
        LogUtil.end("");
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webview.onPause(); // 暂停网页中正在播放的视频
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果网页中有音乐在播放需要暂停
        // 下面两句代码不能加，否则会出现页面不能完全加载完。
//		webview.pauseTimers();
//		webview.stopLoading();
        webview.loadData("<a></a>", "text/html", "utf-8");
    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        //super.onBackPressed();
        if (webview.canGoBack()) {
            webview.goBack();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnback:
                if (webview.canGoBack()) {
                    webview.goBack();
                } else {
                    finish();
                }
                break;
        }
    }

    private final class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!TextUtils.isEmpty(url)) {
                //TODO:仅对关心的schema做过滤，其他的都交由webView自由处理(return false)
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_CALL,
                            Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("mailto:")) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO,
                            Uri.parse(url));
                    startActivity(intent);
                    return true;
                } else if (url.startsWith("buteltel:")) {
                    LogUtil.d("buteltel:点击可视客服");
                    //OutCallUtil.makeServiceNumberCall(EmbedWebViewActivity.this);

                    String number = "";
//                    number = NetPhoneApplication.getPreference().getKeyValue(
//                            PrefType.CUSTOM_SERVICE_PHONE, "");
                    String name = ShowNameUtil.getShowName(number);
                    Intent intent = new Intent(EmbedWebViewActivity.this,
                            ChatActivity.class);
                    intent.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                            ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
                    intent.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, number);
                    intent.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME,
                            name);
                    intent.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                            ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE);
                    startActivity(intent);

                    return true;
                }
//
//                else if (url.startsWith("sinaweibo:")) {
//                	//新浪微博自定义的Schema,不能解析加载，所以直接跳过。
//					return true;
//                } else if (url.startsWith("ucweb:")) {
//                	//UC浏览器自定义的Schema,不能解析加载，所以直接跳过。
//					return true;
//                } else {
//                	view.loadUrl(url);
//                }
            }
            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // Auto-generated method stub
            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }
    }

    private void initTitleBar() {
//        titleBar = getTitleBar();
//        titleBar.setBack("", new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                // TODO Auto-generated method stub
//                if (webview.canGoBack()) {
//                    webview.goBack();
//                } else {
//                    finish();
//                }
//            }
//        });
        if (ChatListAdapter.ACTIVITY_FLAG.equals(title)) {
            tvtitle.setText("");
        } else {
            if (getString(R.string.help_feedback).equals(title)) {
                tvtitle.setText(title);
            } else {
                if (!TextUtils.isEmpty(title)) {
                    tvtitle.setText(title);
                } else {
                    tvtitle.setText("");
                }
            }
        }
    }

    private void showTitleMore() {
//        LogUtil.begin("点击 更多 按钮");
//        List<MenuInfo> mMoreinfo = new ArrayList<MenuInfo>();//更多按钮操作
//        mMoreinfo.add(new MenuInfo(R.drawable.feedback_icon, "意见反馈", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LogUtil.d("意见反馈");
//                Intent intent = new Intent(EmbedWebViewActivity.this, FeedbackActivity.class);
//                startActivity(intent);
//            }
//        }));
//
//        mMoreinfo.add(new MenuInfo(R.drawable.butel_secret_book_more, "更多", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                LogUtil.d("更多");
//                String regstatus = NetPhoneApplication.getPreference().getKeyValue(
//                        PrefType.KEY_FOR_SIP_REG_OK, "false");
//                boolean netConnect= CommonUtil.isNetworkAvailable(EmbedWebViewActivity.this);
//                logD("checkService regstatus:" + regstatus
//                        +"|CommonUtil.isNetworkAvailable(getActivity())="+netConnect);
//                if (Boolean.valueOf(regstatus) && netConnect) {
//                    //AppP2PAgentManager.getInstance().makeCall("00000000", 1, "");
//                    Intent intent = new Intent(EmbedWebViewActivity.this, MijiActivity.class);
//                    startActivity(intent);
//                }else{
//                    CommonUtil.showToast("通话服务正在连接，请稍后重试");
//                }
//            }
//        }));
//
//        PopDialogActivity.setMenuInfo(mMoreinfo);
//        startActivity(new Intent(this, PopDialogActivity.class));
//        LogUtil.end("");
    }
//	public TitleBar getTitleBar() {
//		if (titleBar == null) {
//			titleBar = new TitleBar(this,
//					((ViewGroup) findViewById(android.R.id.content))
//							.getChildAt(0));
//		}
//		return titleBar;
//	}

    private final class CustomJavaScriptInterface {
        public CustomJavaScriptInterface() {
        }

        public void clickonAndroid(final String order) {
            // mHandler.post(newRunnable(){
            // @Override
            // public void run(){
            // jsonText="{"name":""+order+""}";
            // wv.loadUrl("javascript:wave("+jsonText+")");
            // }
            // });
        }

        public void makeCall(String json) {
            LogUtil.d("json: " + json);
            JSONObject obj;
            try {
                obj = new JSONObject(json);
                String nube = obj.getString("nube");
                String cad = obj.getString("cad");
                int type = obj.getInt("type");
//                int calltype = OutCallUtil.CT_SIP_AV;
//                switch (type) {
//                    case 1:
//                        calltype = OutCallUtil.CT_SIP_AUDIO;
//                        break;
//                    case 2:
//                        calltype = OutCallUtil.CT_SIP_AV;
//                        break;
//
//                    default:
//                        break;
//                }
//                OutCallUtil.makeNormalCall(EmbedWebViewActivity.this, nube, calltype,nube,cad);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private final class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            // message就是wave函数里alert的字符串，这样你就可以在android客户端里对这个数据进行处理
            result.confirm();
            return true;
        }

    }

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent,
                                    String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
}
