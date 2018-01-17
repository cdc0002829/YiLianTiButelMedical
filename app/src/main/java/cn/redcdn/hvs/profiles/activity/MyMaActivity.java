package cn.redcdn.hvs.profiles.activity;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import cn.redcdn.datacenter.medicalcenter.MDSAppGetPersonQrCodeUrl;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.profiles.collection.SaveImageUtils;
import cn.redcdn.hvs.profiles.listener.DisplayImageListener;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Created by Administrator on 2017/3/10.
 */

public class MyMaActivity extends BaseActivity {
    private DisplayImageListener mDisplayImageListener = null;

    private ImageView erweimaIcon;
    private ImageView erweimaIv;
    private TextView erweimaName;
    private TextView erweimaNun;
    private CardView cardView;

    //左上角坐标
    private int x1;
    private int y1;

    //右下角坐标
    private int x2;
    private int y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myma_activity);
        mDisplayImageListener = new DisplayImageListener();
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.QR_code));
        titleBar.enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdialog();
            }
        });
        initView();
    }

    private void initView() {
        erweimaIcon = (ImageView) findViewById(R.id.erweiam_text_icon);
        erweimaIv = (ImageView) findViewById(R.id.erweima_iv);
        erweimaName = (TextView) findViewById(R.id.erweiam_text_name);
        erweimaNun = (TextView) findViewById(R.id.erweima_text_num);
        cardView = (CardView) findViewById(R.id.cardview);
        setNameAndIcon();
        setErWeiMa();
    }

    private void setErWeiMa() {
        MDSAppGetPersonQrCodeUrl mdsAppGetPersonQrCodeUrl=new MDSAppGetPersonQrCodeUrl() {
            @Override
            protected void onSuccess(String responseContent) {
                super.onSuccess(responseContent);
                if (responseContent != null && !responseContent.equalsIgnoreCase("")) {
                    CustomLog.d(TAG, getString(R.string.show_QR_code));
                    AccountManager.getInstance(MyMaActivity.this).setqrCodeUrl(responseContent);
                    showErweima(responseContent);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomToast.show(MyMaActivity.this,getString(R.string.network_not_good), 5000);
                erweimaIcon.setImageResource(R.drawable.head);
                if(statusCode==MDS_TOKEN_DISABLE){
                    AccountManager.getInstance(MyMaActivity.this).tokenAuthFail(statusCode);
                }else {
                    CustomToast.show(MyMaActivity.this, statusInfo , Toast.LENGTH_LONG);
                }

            }
        };
        mdsAppGetPersonQrCodeUrl.getPersonQrCodeUrl(AccountManager.getInstance(this)
                .getAccountInfo().getAccessToken());
    }

    private void setNameAndIcon() {
        MDSAccountInfo info = AccountManager.getInstance(getApplicationContext())
                .getAccountInfo();
        if (info.headThumUrl != null && !info.headThumUrl.equalsIgnoreCase("")) {
            CustomLog.d(TAG, "显示图片");
            show(info.headThumUrl);
        } else {
            erweimaIcon.setImageResource(R.drawable.head);
        }
        String nickName = "";
        if (AccountManager.getInstance(getApplicationContext())
                .getAccountInfo() != null)
            nickName = AccountManager.getInstance(getApplicationContext())
                    .getAccountInfo().nickName;
        if (nickName != null && !nickName.equalsIgnoreCase(""))
            erweimaName.setText(nickName);
        else
            erweimaName.setText(getString(R.string.no_name));
        if (AccountManager.getInstance(getApplicationContext())
                .getAccountInfo() != null)
            erweimaNun.setText(getString(R.string.video_num)+ AccountManager.getInstance(getApplicationContext())
                    .getAccountInfo().nube);
    }

    private void show(final String str) {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, erweimaIcon, MedicalApplication.shareInstance().options, mDisplayImageListener);
    }

    private void showErweima(final String str) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.doctor_default)//设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.doctor_default)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.doctor_default)//设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//是否緩存都內存中
                .cacheOnDisc(true)//是否緩存到sd卡上
                .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, erweimaIv, options, mDisplayImageListener);
    }


    private View inflate;
    private TextView choosePhoto;
    private TextView takePhoto;
    private Dialog dialog;

    private void showdialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.erweima_dialog, null);
        //初始化控件
        choosePhoto = (TextView) inflate.findViewById(R.id.save_tv);
        takePhoto = (TextView) inflate.findViewById(R.id.cancle_tv);
        choosePhoto.setOnClickListener(mbtnHandleEventListener);
        takePhoto.setOnClickListener(mbtnHandleEventListener);
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

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.save_tv:
                savePhoto(AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().qrCodeUrl);
                dialog.dismiss();
                break;
            case R.id.cancle_tv:
                dialog.dismiss();
                break;
        }
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int[] position = new int[2];
        cardView.getLocationOnScreen(position);
        x1 = position[0];
        y1 = position[1];

        x2 = cardView.getWidth();
        y2 = cardView.getHeight();

    }

    private void savePhoto(final String imgsUrl) {
        View view = this.getWindow().getDecorView();
        view.buildDrawingCache();

        final Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), x1,
                y1, x2, y2);
        if (imgsUrl != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        final Bitmap myBitmap = Glide.with(MyMaActivity.this)//上下文
//                                .load(imgsUrl)//url
//                                .asBitmap() //必须
//                                .centerCrop()
//                                .into(500, 500)
//                                .get();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SaveImageUtils.saveImageToGallerys(MyMaActivity.this, bmp);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
