package cn.redcdn.hvs.profiles.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
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
import cn.redcdn.hvs.profiles.collection.EmojiconTextView;
import cn.redcdn.hvs.profiles.listener.MyDisplayImageListener;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/3/6.
 */

public class CollectionWordActivity extends BaseActivity {
    public static final String COLLECTION_TEXT_DATA = "collection_file_data";
    private DataBodyInfo bean;
    private TextView collectionName;
    private EmojiconTextView collectionContent;
    private RoundImageView collectionIcon;
    private TextView timeTxt;
    MyDisplayImageListener mDisplayImageListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_word);
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

    private View inflate;
    private TextView zhuanfa;
    private TextView cacleZhuanfa;
    private TextView deleteZhuanfa;
    private Dialog dialog;

    private void showDialog() {
        dialog = new Dialog(this, R.style.ActionSheetDialogStyle);
        //填充对话框的布局
        inflate = LayoutInflater.from(this).inflate(R.layout.zhuanfa_dialog1, null);
        //初始化控件
        zhuanfa = (TextView) inflate.findViewById(R.id.zhuanfa_tv);
        deleteZhuanfa = (TextView) inflate.findViewById(R.id.delete_zhuanfa_tv);
        cacleZhuanfa = (TextView) inflate.findViewById(R.id.cancle_zhuanfa_tv);
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

    private void initView() {
        collectionName = (TextView) findViewById(R.id.collection_text_name);
        collectionContent = (EmojiconTextView) findViewById(R.id.collection_textdetail_view);
        collectionIcon = (RoundImageView) findViewById(R.id.collection_text_icon);
        timeTxt = (TextView) findViewById(R.id.collection_text_time);
        setNameAndIcon();
    }

    private void setNameAndIcon() {
        String groupName = bean.getGroupName();
        if (bean.getForwarderName() != null) {
            if (!groupName.equals("")) {
                collectionName.setText(bean.getForwarderName()+"—"+groupName);
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        Date curDate=new Date(System.currentTimeMillis());
        String format1 = format.format(curDate);
        if (format.format(d).equals(format1)){
            timeTxt.setText(getString(R.string.today_collect));
        }else {
            timeTxt.setText(getString(R.string.collect_in) + format.format(d));
        }
        collectionContent.setText(bean.getTxt()
        );

    }


    private void initData() {
        Intent i = getIntent();
        bean = (DataBodyInfo) i.getSerializableExtra(COLLECTION_TEXT_DATA);
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.zhuanfa_tv:
                dialog.dismiss();
                CustomLog.d(TAG, "转发文本");
                CollectionFileManager.getInstance().onCollectMsgForward(
                        CollectionWordActivity.this, bean);
                break;
            case R.id.delete_zhuanfa_tv:

                DeleteCollectItems deleteCollectItems = new DeleteCollectItems() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        CustomToast.show(getApplicationContext(), getString(R.string.delete_collection_suc), 5000);
                        CollectionManager.getInstance().deleteCollectionById(bean.getCollectionId());
                        CollectionWordActivity.this.finish();
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        CustomToast.show(getApplicationContext(),getString(R.string.delete_collection_fail), 5000);
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

        }
    }

    private String getBodyText() {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            jsonObject.put("txt", bean.getTxt());
            jsonObject.putOpt("ForwarderName", bean.getFileName());
            jsonObject.putOpt("ForwarderHeaderUrl", bean.getForwarderHeaderUrl());
            return jsonArray.toString();

        } catch (Exception e) {
            CustomLog.d("addNewMsgToBody", "解析json 出错");
        }
        return "";
    }
}
