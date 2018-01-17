package cn.redcdn.hvs.im.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.redcdn.datacenter.medicalcenter.MDSAppUploadGroupNotice;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.DateUtil;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/3/7.
 */

public class GroupAnnouncementActivity extends BaseActivity {

    private final String TAG = "GroupQRCodeActivity";

    public final static String GROUP_ID = "group_id";// 必须传入的值,群id
    public final static String ISLEADDER = "leader";//1 表示是群主，0表示不是群主
    public final static String ANNOUNCEMENT = "announcement";//群公告的内容
    public final static String LEADER_HEADURL = "leader_headurl";//群主头像
    public final static String LEADER_NAME = "leadername";//群主姓名
    public final static String LOADINGTIME = "loadingtime";//群公告更新时间
    public final static String NEW_ANNOUNCEMENT = "new_ANNOUNCEMENT";//新的公告内容
    private final static int ACTIVITY_GROUP_ANNOUNCEMENTEDITTEXT=202;
    private final int resultcode = 1;

    private TextView onlyLeaderSee, leaderName, publishTimeTv;
    private ImageView headimage;

    private TitleBar titleBar;
    private TextView announcementTV;


    private String mGroupId, announceContent, groupLeaderName;
    private String groupLeaderHeadUrl, announcePublishTime;
    private boolean isGroupLeader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.d(TAG, "oncreate begin");
        setContentView(R.layout.activity_group_announcement);
        initWidget();
        initData();
    }

    private void initWidget() {
        titleBar = getTitleBar();
        titleBar.setTitle(R.string.group_notices);
        titleBar.enableBack();
        onlyLeaderSee = (TextView) findViewById(R.id.group_announcement_text);     //群主编辑提示
        leaderName = (TextView) findViewById(R.id.group_announcement_leaderName); //群主姓名
        publishTimeTv = (TextView) findViewById(R.id.group_accouncement_data);         //群公告发布时间
        headimage = (ImageView) findViewById(R.id.group_announcement_headimage);  //群主头像

        headimage.setOnClickListener(mbtnHandleEventListener);
        announcementTV = (TextView) findViewById(R.id.group_announcement_tv);
    }


    private void initData() {
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra(GROUP_ID);
        announceContent = intent.getStringExtra(ANNOUNCEMENT);
        announcePublishTime = intent.getStringExtra(LOADINGTIME);
        groupLeaderName = intent.getStringExtra(LEADER_NAME);
        groupLeaderHeadUrl = intent.getStringExtra(LEADER_HEADURL);
        isGroupLeader = intent.getBooleanExtra(ISLEADDER, false);

        leaderName.setText(groupLeaderName);
        Glide.with(this)
                .load(groupLeaderHeadUrl)
                .placeholder(R.drawable.contact_head_default_man)
                .into(headimage);

        if (isGroupLeader) {
            groupLeaderOperation();
        } else {
            groupMemberOperation();
        }
    }

    private void groupLeaderOperation() {
            announcementTV.setText(announceContent);
            if (announcePublishTime.length() == 0) {
                publishTimeTv.setText("");
            } else {
                String tempStr = DateUtil.getDateTimeByFormatAndMs(Long.parseLong(announcePublishTime), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_N);
                publishTimeTv.setText(tempStr);
            }
            titleBar.enableRightBtn(getString(R.string.edit), 0, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Intent intent=new Intent(GroupAnnouncementActivity.this,GroupAnnouncementEditActivity.class);
                    intent.putExtra(GROUP_ID,mGroupId);
                    intent.putExtra(ANNOUNCEMENT,announceContent);
                    startActivityForResult(intent,ACTIVITY_GROUP_ANNOUNCEMENTEDITTEXT);

                }
            });
        }



    private void groupMemberOperation() {
        announcementTV.setVisibility(View.VISIBLE);
        if (announceContent.length() > 0) {
            announcementTV.setText(announceContent);
            onlyLeaderSee.setVisibility(View.VISIBLE);

        } else {
            showTipDialog();
            announcementTV.setText(R.string.now_not_announcement);
        }
        if (announcePublishTime.length() == 0) {
            publishTimeTv.setText("");
        } else {
            String tempStr = DateUtil.getDateTimeByFormatAndMs(Long.parseLong(announcePublishTime), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_N);
            publishTimeTv.setText(tempStr);
        }
    }



    private void showTipDialog() {
        final CustomDialog tipDlg = new CustomDialog(GroupAnnouncementActivity.this);
        String tip = getString(R.string.only_group_manager) + groupLeaderName + getString(R.string.can_edit_group_annoucement);
        tipDlg.setTip(tip);

        tipDlg.removeCancelBtn();
        tipDlg.setOkBtnText(getString(R.string.iknow));
        tipDlg.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                tipDlg.dismiss();
                CustomLog.d(TAG, "没有群公告，关闭弹窗");
                finish();
            }
        });
        tipDlg.show();
    }


    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.group_announcement_headimage:
                Intent intentheadimage = new Intent(GroupAnnouncementActivity.this, OpenBigImageActivity.class);
                intentheadimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                intentheadimage.putExtra(OpenBigImageActivity.DATE_URL, groupLeaderHeadUrl);
                startActivity(intentheadimage);
                break;
            default:
                break;
        }
    }

    public void Dialog() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data==null){
            CustomLog.d(TAG,"data==null");
            return;
        }
        switch (requestCode){
            case ACTIVITY_GROUP_ANNOUNCEMENTEDITTEXT:
                String newAnnouncement=data.getStringExtra(GroupAnnouncementActivity.NEW_ANNOUNCEMENT);
                data.putExtra(GroupAnnouncementActivity.NEW_ANNOUNCEMENT, newAnnouncement);
                GroupAnnouncementActivity.this.setResult(resultcode, data);
                GroupAnnouncementActivity.this.finish();
                break;
            default:
                break;
        }
    }
}
