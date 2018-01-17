package cn.redcdn.hvs.im.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.datacenter.medicalcenter.MDSAppGetGroupQrCodeBytes;
import cn.redcdn.datacenter.medicalcenter.MDSAppUpdateGroupQrCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.bean.GroupBean;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.profiles.collection.SaveImageUtils;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONObject;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Desc
 * Created by wangkai on 2017/3/7.
 */

public class GroupQRCodeActivity extends BaseActivity {

    private final String TAG = GroupQRCodeActivity.class.getSimpleName();

    //Intent Key
    public final static String GROUP_ID = "group_id";// 必须传入的值
    public final static String GROUP_BEAN = "group_bean"; //群名称

    private ImageView QRView;
    private ImageView GroupIcon;  //群头像
    private String mGroupId = "";
    private View inflate;
    private TextView savePictureTv;
    private TextView cancelDialogTv;
    private Dialog dialog;
    private String groupQRImageUrl = "";
    private TextView validTimeTxt;
    private String validTime = "";
    public static final String FORMAT_MM_DD = "MM-dd";
    private TextView groupNameTxt;
    private GroupBean mGroupBean;
    private GroupDao groupDao;

    //左上角坐标
    private int x1;
    private int y1;

    //右下角坐标
    private int x2;
    private int y2;
    private View QRlayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.d(TAG, "oncreate begin");
        setContentView(R.layout.activity_group_qrcode);

        initWidget();
        initData();
        loadQRCode(AccountManager.getInstance(this).getMdsToken(), mGroupId);
        loadGroupInfo();
        
    }


    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        int[] position = new int[2];
        QRlayout.getLocationOnScreen(position);
        x1 = position[0];
        y1 = position[1];

        x2 = QRlayout.getWidth();
        y2 = QRlayout.getHeight();

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

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SaveImageUtils.saveImageToGallerys(GroupQRCodeActivity.this, bmp);
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


    private void loadGroupInfo() {

        mGroupId = mGroupBean.getGid();
        String gruopName = groupDao.getGroupNameByGid(mGroupId);

        String groupPicUrl = mGroupBean.getHeadUrl();

        groupNameTxt.setText(gruopName);
        Glide.with(this)
            .load(groupPicUrl)
            .placeholder(R.drawable.group_icon)
            .error(R.drawable.group_icon)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade()
            .into(GroupIcon);
    }


    private void initData() {
        mGroupId = getIntent().getStringExtra(GROUP_ID);
        mGroupBean = (GroupBean) getIntent().getSerializableExtra(this.GROUP_BEAN);
        groupDao = new GroupDao(this);
    }


    private void loadQRCode(String token, String groupID) {
        final MDSAppGetGroupQrCodeBytes getbyte = new MDSAppGetGroupQrCodeBytes(){
            @Override
            protected void onSuccess(byte[] responseContent) {
                CustomLog.d(TAG, "群二维码流数据请求成功");
                //imageView 加载Bitmap对象
                QRView.setImageBitmap(getPicFromBytes(responseContent,null));
                removeLoadingView();
                super.onSuccess(responseContent);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomToast.show(getApplicationContext(), getString(R.string.load_QR_code_fail), 1);
                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(GroupQRCodeActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(GroupQRCodeActivity.this, statusInfo, Toast.LENGTH_LONG);
                }

                super.onFail(statusCode, statusInfo);
            }
        };
        getbyte.getPersonQrCodeUrl(token,groupID);
        showLoadingView(getString(R.string.loading_collection), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                removeLoadingView();
                getbyte.cancel();
            }
        });
        //设置群二维码有效期
        setQRCodeValidTime();

    }
    public static Bitmap getPicFromBytes(byte[] bytes, BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null){
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        opts);}
            else{
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);}
        return null;
    }

    private void setQRCodeValidTime() {
        //获取当前时间，加 7 天
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 7);
        Date newTime = calendar.getTime();

        validTime = DateUtil.formatDate2String(newTime, FORMAT_MM_DD);

        //显示群二维码有效期
        validTimeTxt.setText(getString(R.string.this_QRcode_7) + validTime + ") " + getString(R.string.validity) +
            getString(R.string.reenter_will_update));
    }


    private void initTitleBar() {

        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.group_chat_code));

        titleBar.enableRightBtn(null, R.drawable.title_more2x
            , new View.OnClickListener() {
                @Override public void onClick(View v) {
                    showdialog();
                }

            });

    }

    //    private void showMenuDialog() {
    //        final MedicalAlertDialog dialog = new MedicalAlertDialog(GroupQRCodeActivity.this);
    //        dialog.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
    //            @Override public void onMenuClicked() {
    //
    //                SaveImageUtil savaImageUtil = new SaveImageUtil(GroupQRCodeActivity.this);
    //                savaImageUtil.savaPicToSDCard();
    //
    //            }
    //        }, "保存图片");
    //        dialog.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
    //            @Override public void onMenuClicked() {
    //                dialog.dismiss();
    //            }
    //        }, "取消");
    //    }


    private void initWidget() {

        initTitleBar();
        initLayout();

    }


    private void initLayout() {
        QRView = (ImageView) findViewById(R.id.Image_qrcode);
        GroupIcon = (ImageView) findViewById(R.id.image_group);
        validTimeTxt = (TextView) findViewById(R.id.valid_time);
        groupNameTxt = (TextView) findViewById(R.id.group_name);
        QRlayout = findViewById(R.id.qr_download);
    }


    private void showdialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.erweima_dialog, null);
        //初始化控件
        savePictureTv = (TextView) inflate.findViewById(R.id.save_tv);
        cancelDialogTv = (TextView) inflate.findViewById(R.id.cancle_tv);
        savePictureTv.setText(R.string.save_pic);
        savePictureTv.setOnClickListener(mbtnHandleEventListener);
        cancelDialogTv.setOnClickListener(mbtnHandleEventListener);
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
                savePhoto(groupQRImageUrl);
                dialog.dismiss();
                break;
            case R.id.cancle_tv:
                dialog.dismiss();
                break;
        }
    }

}
