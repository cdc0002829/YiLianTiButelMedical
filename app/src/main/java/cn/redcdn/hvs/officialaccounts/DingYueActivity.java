package cn.redcdn.hvs.officialaccounts;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import cn.redcdn.datacenter.offaccscenter.MDSAppFocusoffacc;
import cn.redcdn.datacenter.offaccscenter.MDSAppGetOffAccInfo;
import cn.redcdn.datacenter.offaccscenter.data.OffAccdetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.im.view.CustomDialog1;
import cn.redcdn.hvs.officialaccounts.activity.OfficialMainActivity;
import cn.redcdn.hvs.profiles.activity.XCRoundImageView;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Created by Administrator on 2017/3/1.
 */

public class DingYueActivity extends BaseActivity {

    private ImageView imageOne;
    private TextView nameTitile;
    private TextView nubeTv;
    private TextView functionTv;
    private TextView accountMain;
    private TextView telephone;
    private TextView jianjie;
    private ImageView imageTwo;
    private RelativeLayout gongZhongHao;
    private Button dingyueButton;
    private Intent intent;
    private Boolean subscribeState;
    private String officialAccountId;
    private String officialName;
    private DisplayImageListener mDisplayImageListener = null;
    private String logoUrl;
    private String servicePhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dingyue);
        initView();
        subscribeState = false;
        mDisplayImageListener = new DisplayImageListener();

    }

    private void initView() {
        imageOne = (XCRoundImageView) findViewById(R.id.imageone_iv);
        imageOne.setOnClickListener(mbtnHandleEventListener);
        nameTitile = (TextView) findViewById(R.id.name_titile_tv);
        nubeTv = (TextView) findViewById(R.id.nube_tv);
        functionTv = (TextView) findViewById(R.id.function);
        accountMain = (TextView) findViewById(R.id.account_main_tv);
        telephone = (TextView) findViewById(R.id.telephone_tv);
        jianjie = (TextView) findViewById(R.id.jianjie_tv);
        imageTwo = (ImageView) findViewById(R.id.imagetwo_iv);
        gongZhongHao = (RelativeLayout) findViewById(R.id.gongzhonghao_rl);
        dingyueButton = (Button) findViewById(R.id.dingyuejia_btn);
        gongZhongHao.setOnClickListener(mbtnHandleEventListener);
        dingyueButton.setOnClickListener(mbtnHandleEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initstatus();
    }

    private void initstatus() {

        intent = getIntent();

        if (intent.getStringExtra("officialAccountId") != null) {
            officialAccountId = intent.getStringExtra("officialAccountId");
            officialName = intent.getStringExtra("officialName");
            findOfficialDate();

        } else {
            DingYueActivity.this.finish();
        }
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.telephone_tv:
                if (!servicePhone.isEmpty()) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (DingYueActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                            Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                    + servicePhone));
                            startActivity(i1);
                        } else {
                            //
                        }
                    } else {
                        Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + servicePhone));
                        startActivity(i1);
                    }
                }
                break;
            case R.id.imageone_iv:
                if (logoUrl != null && logoUrl.length() != 0) {
                    Intent intent_inputimage = new Intent(this, OpenBigImageActivity.class);
                    intent_inputimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                    intent_inputimage.putExtra(OpenBigImageActivity.DATE_URL, logoUrl);
                    startActivity(intent_inputimage);
                }
                break;
            case R.id.gongzhonghao_rl:
                Intent intent = new Intent(DingYueActivity.this, OfficialMainActivity.class);
                intent.putExtra("officialAccountId", officialAccountId);
                intent.putExtra("officialName", officialName);
                startActivity(intent);
                break;
            case R.id.dingyuejia_btn:
                AccountManager.TouristState touristState = AccountManager.getInstance(DingYueActivity.this).getTouristState();
                if (touristState == AccountManager.TouristState.TOURIST_STATE) {
                    CustomDialog1.Builder builder = new CustomDialog1.Builder(DingYueActivity.this);
                    builder.setMessage(R.string.only_register_can_user_login_again);
                    builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton(R.string.login_or_register,
                            new android.content.DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent();
                                    intent.setClass(DingYueActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }
                            });
                    builder.create().show();
                } else {
                    final MDSAppFocusoffacc mdsAppFocusoffacc = new MDSAppFocusoffacc() {
                        @Override
                        protected void onSuccess(JSONObject responseContent) {
                            super.onSuccess(responseContent);
                            removeLoadingView();
                            CustomToast.show(DingYueActivity.this, R.string.subscribe_success, 7000);
                            dingyueButton.setVisibility(View.GONE);
                            subscribeState = true;
                        }

                        @Override
                        protected void onFail(int statusCode, String statusInfo) {
                            super.onFail(statusCode, statusInfo);
                            removeLoadingView();
                            CustomToast.show(DingYueActivity.this, getString(R.string.subscribe_fail_subscribe_again), 7000);
                            dingyueButton.setVisibility(View.VISIBLE);
                            subscribeState = false;
                        }
                    };
                    DingYueActivity.this.showLoadingView(getString(R.string.subscribing), new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                            mdsAppFocusoffacc.cancel();
                            CustomToast.show(getApplicationContext(), getString(R.string.cancle__subscribe), Toast.LENGTH_SHORT);
                        }
                    });
                    mdsAppFocusoffacc.appFocusoffacc(AccountManager.getInstance(getApplicationContext()).getMdsToken(), officialAccountId, 2);
                }
                break;
            case R.id.dy_tv:
                subscribe();
                dialog.dismiss();
                break;
            case R.id.cancle_tv:
                dialog.dismiss();
                break;
        }
    }

    private void subscribe() {
        if (subscribeState == false) {
            AccountManager.TouristState touristState = AccountManager.getInstance(DingYueActivity.this).getTouristState();
            if (touristState == AccountManager.TouristState.TOURIST_STATE) {
                CustomDialog1.Builder builder = new CustomDialog1.Builder(DingYueActivity.this);
                builder.setMessage(R.string.only_register_can_user_login_again);
                builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(R.string.login_or_register,
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent();
                                intent.setClass(DingYueActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                builder.create().show();
            } else {
                final MDSAppFocusoffacc mdsAppFocusoffacc = new MDSAppFocusoffacc() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        removeLoadingView();
                        CustomToast.show(DingYueActivity.this, R.string.subscribe_success, 7000);
                        dingyueButton.setVisibility(View.GONE);
                        subscribeState = true;
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        removeLoadingView();
                        CustomToast.show(DingYueActivity.this, R.string.subscribe_fail_subscribe_again, 7000);
                        dingyueButton.setVisibility(View.VISIBLE);
                    }
                };
                DingYueActivity.this.showLoadingView(getString(R.string.subscribing), new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        mdsAppFocusoffacc.cancel();
                        CustomToast.show(getApplicationContext(), R.string.cancle__subscribe, Toast.LENGTH_SHORT);
                    }
                });
                mdsAppFocusoffacc.appFocusoffacc(AccountManager.getInstance(getApplicationContext()).getMdsToken(), officialAccountId, 2);
            }
        } else {
            final MDSAppFocusoffacc mdsAppFocusoffacc = new MDSAppFocusoffacc() {
                @Override
                protected void onSuccess(JSONObject responseContent) {
                    super.onSuccess(responseContent);
                    removeLoadingView();
                    CustomToast.show(DingYueActivity.this, R.string.subscribe_have_cancled, 7000);
                    dingyueButton.setVisibility(View.VISIBLE);
                    subscribeState = false;
                }

                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    removeLoadingView();
                    CustomToast.show(DingYueActivity.this, getString(R.string.cancle_subscribe_fail_cancle_subscribe_again), 7000);
                    dingyueButton.setVisibility(View.GONE);
                }
            };
            DingYueActivity.this.showLoadingView(getString(R.string.cancle_subscribing), new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    mdsAppFocusoffacc.cancel();
                    CustomToast.show(getApplicationContext(), getString(R.string.stop_cancle_subscribe), Toast.LENGTH_SHORT);
                }
            });
            mdsAppFocusoffacc.appFocusoffacc(AccountManager.getInstance(getApplicationContext()).getMdsToken(), officialAccountId, 1);
        }
    }

    private View inflate;
    private TextView choosePhoto;
    private TextView takePhoto;
    private Dialog dialog;

    private void showDialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.bottom_dialog, null);
        //初始化控件
        choosePhoto = (TextView) inflate.findViewById(R.id.dy_tv);
        takePhoto = (TextView) inflate.findViewById(R.id.cancle_tv);
        choosePhoto.setOnClickListener(mbtnHandleEventListener);
        takePhoto.setOnClickListener(mbtnHandleEventListener);
        if (subscribeState == true) {
            choosePhoto.setText(R.string.not_subscribe);
            choosePhoto.setTextColor(Color.RED);
        } else {
            choosePhoto.setText(R.string.subscribe_button);
        }
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

    ImageLoader imageLoader = ImageLoader.getInstance();

    //公众号详细信息接口
    private void findOfficialDate() {
        final MDSAppGetOffAccInfo offAccInfo = new MDSAppGetOffAccInfo() {
            @Override
            protected void onSuccess(OffAccdetailInfo responseContent) {
                super.onSuccess(responseContent);
                DingYueActivity.this.removeLoadingView();
                int isSubscribe = responseContent.getIsSubscribe();
                if (isSubscribe == 1) {
                    subscribeState = false;
                    dingyueButton.setVisibility(View.VISIBLE);
                } else if (isSubscribe == 2) {
                    subscribeState = true;
                    dingyueButton.setVisibility(View.GONE);
                }
                logoUrl = responseContent.getLogoUrl();
                officialAccountId = responseContent.getId();
                nameTitile.setText(responseContent.getName());
                functionTv.setText(responseContent.getFunctionIntroduction());
                servicePhone = responseContent.getServiceTel();
                telephone.setText(responseContent.getServiceTel());
                telephone.setOnClickListener(mbtnHandleEventListener);
                CustomLog.e(TAG, "telephone" + responseContent.getSubscribeNumber());
                accountMain.setText(responseContent.getEntName());
                jianjie.setText(responseContent.getArticleIntro());
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .showStubImage(R.drawable.gongzhonghaomingpian)//设置图片在下载期间显示的图片
                        .showImageForEmptyUri(R.drawable.gongzhonghaomingpian)//设置图片Uri为空或是错误的时候显示的图片
                        .showImageOnFail(R.drawable.gongzhonghaomingpian)//设置图片加载/解码过程中错误时候显示的图片
                        .cacheInMemory(true)//是否緩存都內存中
                        .cacheOnDisc(true)//是否緩存到sd卡上
                        .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                        .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                        .build();
                DisplayImageOptions options1 = new DisplayImageOptions.Builder()
                        .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                        .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                        .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                        .cacheInMemory(true)//是否緩存都內存中
                        .cacheOnDisc(true)//是否緩存到sd卡上
                        .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                        .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                        .build();
                imageLoader.displayImage(responseContent.getLogoUrl(),
                        imageOne,
                        options,
                        mDisplayImageListener);

                imageLoader.displayImage(responseContent.getArticlePreviewUrl(),
                        imageTwo,
                        options1,
                        mDisplayImageListener);
                TitleBar titleBar = getTitleBar();
                titleBar.setTitle(responseContent.getName());
                titleBar.enableBack();
                titleBar.enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog();
                    }
                });
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                DingYueActivity.this.removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DingYueActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(DingYueActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
                CustomToast.show(getApplicationContext(), getString(R.string.loading_data_fail),
                        Toast.LENGTH_SHORT);
                DingYueActivity.this.finish();
            }
        };

        offAccInfo.appGetOffAccInfo(AccountManager.getInstance(MedicalApplication.context).getMdsToken(), officialAccountId);

        DingYueActivity.this.showLoadingView(getString(R.string.loading_collection), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                offAccInfo.cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancle_loading), Toast.LENGTH_SHORT);
                DingYueActivity.this.finish();
            }
        });
    }
}
