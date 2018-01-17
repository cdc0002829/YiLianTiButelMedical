package cn.redcdn.hvs.officialaccounts.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONObject;

import cn.redcdn.datacenter.offaccscenter.MDSAppFocusoffacc;
import cn.redcdn.datacenter.offaccscenter.MDSAppGetOffAccInfo;
import cn.redcdn.datacenter.offaccscenter.data.OffAccdetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.im.view.CustomDialog;
import cn.redcdn.hvs.im.view.CustomDialog1;
import cn.redcdn.hvs.officialaccounts.DingYueActivity;
import cn.redcdn.hvs.officialaccounts.activity.OfficialMainActivity;
import cn.redcdn.hvs.officialaccounts.listener.DingyueDisplayImageListener;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;


/**
 * Created by thinkpad on 2017/1/24.
 */

public class IntroFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "IntroFragment";
    private NestedScrollView scrollView;
    private Button subscribe_btn;
    private ImageView official_Imag;
    private TextView official_name, subscribe_fanscount;
    private Dialog officialDialog;
    private Context context;
    private RelativeLayout back, more;
    private String officialAccountId;
    private DingyueDisplayImageListener mDisplayImageListener = null;
    private ImageView bg_imag;
    private int optType;
    private TextView subscrbe_text;
    private View inflate;
    private RelativeLayout subscribe_rl, officialcard_rl, cancel_rl;
    private WebView webView;
    private String webviewUrl;
    private int isSubscribe;
    private Dialog dialog;
    private Button btn_more, btn_back;
    private OfficialMainActivity officialMainActivity;

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initData();
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_intro_view, container, false);
        scrollView = (NestedScrollView) view.findViewById(R.id.scrollView);
        btn_back = (Button) view.findViewById(R.id.btn_back);
        btn_more = (Button) view.findViewById(R.id.btn_more);
        back = (RelativeLayout) view.findViewById(R.id.back);
        more = (RelativeLayout) view.findViewById(R.id.more);
        subscribe_btn = (Button) view.findViewById(R.id.subscribe_btn);

        official_Imag = (ImageView) view.findViewById(R.id.official_Imag);
        official_Imag.setOnClickListener(this);
        bg_imag = (ImageView) view.findViewById(R.id.bg_imag);

        official_name = (TextView) view.findViewById(R.id.official_name);
        subscribe_fanscount = (TextView) view.findViewById(R.id.subscribe_fanscount);

        webView = (WebView) view.findViewById(R.id.webview);
        webView.setHorizontalScrollBarEnabled(false);//设置水平滚动条
        webView.getSettings().setTextZoom(100);
        officialDialog = new Dialog(context, R.style.ActionSheetDialogStyle);
        //填充布局
        inflate = LayoutInflater.from(context).inflate(R.layout.officialdialog, null);
        //初始化控件
        subscrbe_text = (TextView) inflate.findViewById(R.id.subscrbe_text);
        subscribe_rl = (RelativeLayout) inflate.findViewById(R.id.subscribe_rl);
        officialcard_rl = (RelativeLayout) inflate.findViewById(R.id.officialcard_rl);
        cancel_rl = (RelativeLayout) inflate.findViewById(R.id.cancel_rl);

        subscribe_rl.setOnClickListener(this);
        officialcard_rl.setOnClickListener(this);
        cancel_rl.setOnClickListener(this);
        officialDialog.setContentView(inflate);
        officialMainActivity = (OfficialMainActivity) getActivity();
        setListener();
        return view;
    }

    @Override
    protected void initData() {

        Bundle bundle = getArguments();
        if (bundle != null) {
            officialAccountId = bundle.getString("officialAccountId");
        }
        findDate();

    }

    @Override
    protected void setListener() {
        back.setOnClickListener(this);
        more.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        btn_more.setOnClickListener(this);
        subscribe_btn.setOnClickListener(this);
    }

    public void showDialog() {

        Window window = officialDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        officialDialog.setCanceledOnTouchOutside(true);
        WindowManager windowManager = getActivity().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = officialDialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        lp.height = (int) ((0.3) * display.getHeight()); // 设置高度
        officialDialog.show();
    }


    public static IntroFragment createInstance(int itmeCount) {
        IntroFragment introFragment = new IntroFragment();
        Bundle bundle = new Bundle();
        return introFragment;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back: //返回
                getActivity().finish();
                break;
            case R.id.more://更多
                showDialog();
                break;
            case R.id.subscribe_btn://订阅按钮
                AccountManager.TouristState touristState = AccountManager.getInstance(getActivity()).getTouristState();
                if (touristState != AccountManager.TouristState.TOURIST_STATE) {
                    subscribe_btn.setVisibility(View.INVISIBLE);
                }
                optType = 2;
                IsSubscribe();
                break;
            case R.id.subscribe_rl://订阅
                //订阅按钮消失
                if (isSubscribe == 1) {//表示未订阅
                    optType = 2;

                } else if (isSubscribe == 2) {//表示已订阅
                    optType = 1;
                }
                IsSubscribe();
                officialDialog.dismiss();
                break;
            case R.id.cancel_rl: //取消
                officialDialog.dismiss();
                break;
            case R.id.officialcard_rl://公众号名片
                Intent intent = new Intent(MedicalApplication.getContext(), DingYueActivity.class);
                intent.putExtra("officialAccountId", officialAccountId);
                context.startActivity(intent);
                officialDialog.dismiss();
                break;
            case R.id.btn_back://返回键
                getActivity().finish();
                break;
            case R.id.btn_more://更多
                showDialog();
                break;
            case R.id.official_Imag:
                Intent inten = new Intent(MedicalApplication.getContext(), DingYueActivity.class);
                inten.putExtra("officialAccountId", officialAccountId);
                context.startActivity(inten);
                break;
        }
    }

    private void IsSubscribe() {
        AccountManager.TouristState touristState = AccountManager.getInstance(getActivity()).getTouristState();
        if (touristState== AccountManager.TouristState.TOURIST_STATE){
            CustomDialog1.Builder builder = new CustomDialog1.Builder(getActivity());
            builder.setMessage(getString(R.string.only_register_can_user_login_again));
            builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    subscribe_btn.setVisibility(View.VISIBLE);
                }
            });

            builder.setNegativeButton(R.string.login_or_register,
                    new android.content.DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            MedicalApplication.shareInstance().clearTaskStack();
                            Intent intent = new Intent();
                            intent.setClass(getActivity(), LoginActivity.class);
                            startActivity(intent);
                        }
                    });
            CustomDialog customDialog = builder.create();
            customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    subscribe_btn.setVisibility(View.VISIBLE);
                }
            });
            customDialog.show();
        }else {
            showLoadingView(getString(R.string.loading_collection), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    CustomLog.d(TAG, "取消加载");
                }
            }, true);
            MDSAppFocusoffacc focusoffacc = new MDSAppFocusoffacc() {
                @Override
                protected void onSuccess(JSONObject responseContent) {
                    super.onSuccess(responseContent);
                    removeLoadingView();
                    if (optType == 1) {
                        subscrbe_text.setText(R.string.subscribe_button);
                        subscribe_btn.setVisibility(View.VISIBLE);
                        isSubscribe = 1;
                        CustomToast.show(MedicalApplication.context,getString(R.string.subscribe_have_cancled),CustomToast.LENGTH_SHORT);
                    } else if (optType == 2) {
                        subscrbe_text.setText(R.string.cancle__subscribe);
                        subscribe_btn.setVisibility(View.INVISIBLE);
                        isSubscribe = 2;
                        CustomToast.show(MedicalApplication.context,getString(R.string.subscribe_success),CustomToast.LENGTH_SHORT);
                    }
                }

                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                    if (statusCode == MDS_TOKEN_DISABLE) {
                        AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                    } else {
                        CustomToast.show(MedicalApplication.context, statusInfo, Toast.LENGTH_LONG);
                    }
                    removeLoadingView();
                    officialMainActivity.finish();
                }

            };
            focusoffacc.appFocusoffacc(AccountManager.getInstance(MedicalApplication.context).getToken(), officialAccountId, optType);
        }

    }


    ImageLoader imageLoader = ImageLoader.getInstance();

    private void findDate() {
        showLoadingView(getString(R.string.loading_collection), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeLoadingView();
                CustomLog.d(TAG, "正在加载公众号简介页面");
            }
        }, true);
        MDSAppGetOffAccInfo offAccInfo = new MDSAppGetOffAccInfo() {
            @Override
            protected void onSuccess(OffAccdetailInfo responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                officialAccountId = responseContent.getId();//公众号ID
                official_name.setText(responseContent.getName());//公众号名字
                responseContent.getFunctionIntroduction();//简介
                subscribe_fanscount.setText(responseContent.getSubscribeNumber());//订阅数
                webviewUrl = responseContent.getIntroduction();
                Long currentTime = System.currentTimeMillis() ;
                webView.loadDataWithBaseURL("file:///android_asset/editor.css", "<head><link rel=\"stylesheet\" href=\"editor.css\"></head><body> " + webviewUrl + " </body><html>", "text/html", "utf-8", null);
                CustomLog.d(TAG, "webviewUrl" + webviewUrl);
                mDisplayImageListener = new DingyueDisplayImageListener();
                //加载公众号log图片
                DisplayImageOptions options1 = new DisplayImageOptions.Builder()
                        .showStubImage(R.drawable.dingyue_mainhead)//设置图片在下载期间显示的图片
                        .showImageForEmptyUri(R.drawable.dingyue_mainhead)//设置图片Uri为空或是错误的时候显示的图片
                        .showImageOnFail(R.drawable.dingyue_mainhead)//设置图片加载/解码过程中错误时候显示的图片
                        .cacheInMemory(true)//是否緩存都內存中
                        .cacheOnDisc(true)//是否緩存到sd卡上
                        .displayer(new RoundedBitmapDisplayer(0))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                        .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                        .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                        .build();
                imageLoader.displayImage(responseContent.getLogoUrl(),
                        official_Imag,
                        options1,
                        mDisplayImageListener);
                //加载公众号背景图片
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .showStubImage(R.drawable.official_main)//设置图片在下载期间显示的图片
                        .showImageForEmptyUri(R.drawable.official_main)//设置图片Uri为空或是错误的时候显示的图片
                        .showImageOnFail(R.drawable.official_main)//设置图片加载/解码过程中错误时候显示的图片
                        .cacheInMemory(true)//是否緩存都內存中
                        .cacheOnDisc(true)//是否緩存到sd卡上
                        .displayer(new RoundedBitmapDisplayer(0))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                        .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                        .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                        .build();
                if (responseContent.getThemeBackgroundUrl() != null) {
                    imageLoader.displayImage(responseContent.getThemeBackgroundUrl(),
                            bg_imag,
                            options,
                            mDisplayImageListener);
                }
                isSubscribe = responseContent.getIsSubscribe();
                if (isSubscribe == 1) {
                    subscrbe_text.setText(R.string.subscribe_button);
                    subscribe_btn.setVisibility(View.VISIBLE);
                } else if (isSubscribe == 2) {//表示已订阅
                    subscrbe_text.setText(R.string.cancle__subscribe);
                    subscribe_btn.setVisibility(View.INVISIBLE);
                   // CustomToast.show(MedicalApplication.context,"订阅成功",CustomToast.LENGTH_SHORT);

                }

            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(getActivity(), statusInfo, Toast.LENGTH_LONG);
                }
                removeLoadingView();
                officialMainActivity.finish();
            }

        };
        offAccInfo.appGetOffAccInfo(AccountManager.getInstance(MedicalApplication.context).getMdsToken(), officialAccountId);
    }


    public void showLoadingView(String message,
                                final DialogInterface.OnCancelListener listener, boolean cancelAble) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
        dialog = cn.redcdn.hvs.util.CommonUtil.createLoadingDialog(getActivity(), message, listener);
        dialog.setCancelable(cancelAble);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onCancel(dialog);
                }
                return false;
            }
        });
        try {
            dialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }

    }

    protected void removeLoadingView() {

        CustomLog.i(TAG, "MeetingActivity::removeLoadingView()");

        if (dialog != null) {

            dialog.dismiss();

            dialog = null;
        }
    }
}
