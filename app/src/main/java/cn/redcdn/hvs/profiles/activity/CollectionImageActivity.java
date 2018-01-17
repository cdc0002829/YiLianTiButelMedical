package cn.redcdn.hvs.profiles.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.datacenter.collectcenter.DeleteCollectItems;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.collection.CollectionFileManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.hvs.profiles.collection.SaveImageUtils;
import cn.redcdn.hvs.profiles.listener.MyDisplayImageListener;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/3/7.
 */

public class CollectionImageActivity extends BaseActivity {
    public static final String COLLECTION_IMAGE_DATA = "collection_image_data";
    private DataBodyInfo bean;
    MyDisplayImageListener mDisplayImageListener = null;
    private TextView collectionName;
    private TextView timeTxt;
    private RoundImageView collectionIcon;
    private ImageView imageIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_image);
        mDisplayImageListener = new MyDisplayImageListener();
        initData();
        initView();
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.collection_xiangqing));
        titleBar.enableBack();
        titleBar.enableRightBtn("", R.drawable.meeting_title_more, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    private void initView() {
        collectionName = (TextView) findViewById(R.id.collection_text_name);
        timeTxt = (TextView) findViewById(R.id.collection_text_time);
        collectionIcon = (RoundImageView) findViewById(R.id.collection_text_icon);
        imageIv = (ImageView) findViewById(R.id.image_collection_iv);
        imageIv.setOnClickListener(mbtnHandleEventListener);
        imageIv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog();
                return true;
            }
        });
        setNameAndIcon();
    }

    private void setNameAndIcon() {
        String groupName = bean.getGroupName();
        if (bean.getForwarderName() != null) {
            if (!groupName.equals("")) {
                collectionName.setText(bean.getForwarderName() + "—" + groupName);
            } else {
                collectionName.setText(bean.getForwarderName());
            }
        }

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(bean.getForwarderHeaderUrl(),
                collectionIcon,
                MedicalApplication.shareInstance().options,
                mDisplayImageListener);
        String collecTime = bean.getCollecTime() + "000";
        long l = Long.parseLong(collecTime);
        Date d = new Date(l);
        SimpleDateFormat format = new SimpleDateFormat(getString(R.string.date));
        Date curDate = new Date(System.currentTimeMillis());
        String format1 = format.format(curDate);
        if (format.format(d).equals(format1)) {
            timeTxt.setText(getString(R.string.today_collect));
        } else {
            timeTxt.setText(getString(R.string.collect_in) + format.format(d));
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//是否緩存都內存中
                .cacheOnDisc(true)//是否緩存到sd卡上
//                    .displayer(new RoundedBitmapDisplayer(20))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        imageLoader.displayImage(bean.getRemoteUrl(),
                imageIv,
                options,
                mDisplayImageListener);
    }

    private void initData() {
        Intent i = getIntent();
        bean = (DataBodyInfo) i.getSerializableExtra(COLLECTION_IMAGE_DATA);
    }

    private View inflate;
    private TextView zhuanfa;
    private TextView cacleZhuanfa;
    private TextView deleteZhuanfa;
    private TextView saveTv;
    private Dialog dialog;

    private void showDialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.zhuanfa, null);
        //初始化控件
        zhuanfa = (TextView) inflate.findViewById(R.id.zhuanfa_tv);
        deleteZhuanfa = (TextView) inflate.findViewById(R.id.delete_zhuanfa_tv);
        cacleZhuanfa = (TextView) inflate.findViewById(R.id.cancle_zhuanfa_tv);
        saveTv = (TextView) inflate.findViewById(R.id.save_tv);
        saveTv.setOnClickListener(mbtnHandleEventListener);
        zhuanfa.setOnClickListener(mbtnHandleEventListener);
        deleteZhuanfa.setOnClickListener(mbtnHandleEventListener);
        cacleZhuanfa.setOnClickListener(mbtnHandleEventListener);
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
            case R.id.zhuanfa_tv:
                dialog.dismiss();
                CustomLog.d(TAG, "转发图片");
                CollectionFileManager.getInstance().onCollectMsgForward(
                        CollectionImageActivity.this, bean);
                break;
            case R.id.delete_zhuanfa_tv:
                DeleteCollectItems deleteCollectItems = new DeleteCollectItems() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_suc), 5000);
                        CollectionManager.getInstance().deleteCollectionById(bean.getCollectionId());
                        CollectionImageActivity.this.finish();
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_fail), 5000);
                    }
                };
                String id = bean.getCollectionId();
                String nube = AccountManager.getInstance(this)
                        .getAccountInfo().getNube();
                String accessToken = AccountManager.getInstance(this)
                        .getAccountInfo().getAccessToken();
                deleteCollectItems.deleteCollectionItems(nube, id, accessToken);
                dialog.dismiss();
                break;
            case R.id.cancle_zhuanfa_tv:
                dialog.dismiss();
                break;
            case R.id.image_collection_iv:
                Intent intent_inputimage = new Intent(this, OpenBigImageActivity.class);
                intent_inputimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                intent_inputimage.putExtra(OpenBigImageActivity.DATE_URL, bean.getRemoteUrl());
                startActivity(intent_inputimage);
                break;
            case R.id.save_tv:
                savePhoto(bean.getRemoteUrl());
                dialog.dismiss();
                break;
        }
    }

    private void savePhoto(final String remoteUrl) {
        if (remoteUrl != null) {

            showLoadingView(getString(R.string.saving), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                }
            });

            Glide.with(CollectionImageActivity.this).load(remoteUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    SaveImageUtils.saveImageToGallerys(CollectionImageActivity.this, resource);
                    removeLoadingView();
                }
            });
        }
    }

}
