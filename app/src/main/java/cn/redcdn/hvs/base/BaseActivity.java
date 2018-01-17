package cn.redcdn.hvs.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.analytics.MobclickAgent;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.LinkedHashMap;
import java.util.List;

import cn.redcdn.crash.Crash;
import cn.redcdn.datacenter.hpucenter.HPUGetDTlist;
import cn.redcdn.datacenter.hpucenter.data.DTInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.offaccscenter.MDSAppGetOffAccInfo;
import cn.redcdn.datacenter.offaccscenter.data.OffAccdetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.HomeActivity;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.boot.SplashActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.activity.GroupAddActivity;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.officialaccounts.DingYueActivity;
import cn.redcdn.hvs.officialaccounts.activity.ArticlePreviewActivity;
import cn.redcdn.hvs.profiles.activity.OutDateActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogcatFileManager;

import static cn.redcdn.hvs.officialaccounts.activity.ArticlePreviewActivity.INTENT_DATA_ARTICLE_ID;
import static cn.redcdn.hvs.profiles.ProfilesFragment.ARTICLE_PREVIEW;
import static cn.redcdn.hvs.profiles.ProfilesFragment.GROUP_TYPE;
import static cn.redcdn.hvs.profiles.ProfilesFragment.PERSON_TYPE;
import static cn.redcdn.hvs.profiles.ProfilesFragment.WE_TYPE;

public class BaseActivity extends AppCompatActivity {
    protected final String TAG = getClass().getName();
    private Dialog mLoadingDialog = null;


    /* 标示退出应用，在某些页面需要点击两次直接退出应用 */
    private boolean isExit = false;

    private boolean twiceToExit = false;
    private static final int MSG_EXIT = 0x00101010;

    private boolean isHandleEvent = false;
    public View.OnClickListener mbtnHandleEventListener = null;
    private static final int IsHandleMsg = 99;

    private TitleBar titleBar;

    private GroupDao mGroupDao;
    private String mGroupId;
    private MDSAccountInfo loginUserInfo = null;
    private LinkedHashMap<String, GroupMemberBean> memberDateList = new LinkedHashMap<String, GroupMemberBean>();//显示数据

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_EXIT:
                    isExit = false;
                    break;
            }
        }
    };


    @SuppressWarnings("deprecation")
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() > 1) {

            if (this.toString().contains("ClipPictureActivity") || this.toString().contains("ViewPhotosActivity") ) {

            } else {
                if (ev.getAction() != MotionEvent.ACTION_POINTER_DOWN
                        && ev.getAction() != MotionEvent.ACTION_POINTER_UP
                        && ev.getAction() != MotionEvent.ACTION_DOWN
                        && ev.getAction() != MotionEvent.ACTION_UP
                        && ev.getAction() != MotionEvent.ACTION_MOVE
                        && ev.getAction() != MotionEvent.ACTION_CANCEL) {

                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);

        mbtnHandleEventListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isHandleEvent == true) {
                    System.out.println("触摸过快,返回");
                    return;
                } else {
                    System.out.println("触摸成功,isHandleEvent = true");
                    todoClick(v.getId());
                    isHandleEvent = true;
                    Message msg = Message.obtain();
                    msg.what = IsHandleMsg;
                    msg.obj = v.getId();
                    isHandleEventhandler.sendMessageDelayed(msg, 200);
                }
            }

        };

    }

    public void todoClick(int i) {

    }

    public TitleBar getTitleBar() {
        if (titleBar == null) {
            titleBar = new TitleBar(this,
                    ((ViewGroup) findViewById(android.R.id.content))
                            .getChildAt(0));
        }
        return titleBar;
    }

    Handler isHandleEventhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == IsHandleMsg) {
                isHandleEvent = false;
                System.out.println("200ms到时，isHandleEvent = false");
            }
        }
    };


    @Override
    protected void onStart() {
        CustomLog.d(TAG, "onStart:" + this.toString());
        //对当前是否正在进行会议进行判断如果正在开会那么进入会议
        if (!this.toString().contains("SplashActivity")) {
            if (!MedicalApplication.shareInstance().getInitStatus()) {
                // CustomLog.e(TAG, "onStart 应用程序未启动，kill-self");
                //android.os.Process.killProcess(android.os.Process.myPid());
                LogcatFileManager.getInstance().setLogDir(SettingData.LogRootDir);
                LogcatFileManager.getInstance().start(getPackageName());

                Crash crash = new Crash();
                crash.setDir(SettingData.LogRootDir);
                crash.init(this, getPackageName());

                CustomLog.e(TAG, "onStart 应用程序未启动，重新执行启动逻辑");
                boolean readCache = AccountManager.getInstance(getApplicationContext()).readLoginCache();
                if (readCache == true) {
                    CustomLog.d(TAG, "onStart 应用程序未启动，读到登录缓存数据,使用缓存数据重建现场");
                    MedicalApplication.shareInstance().recoverApplication();
                } else {
                    CustomLog.e(TAG, "onStart 应用程序未启动，且未读到登录缓存数据");
                    Intent intent = new Intent();
                    intent.setClass(this, SplashActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
//            if (AccountManager.getInstance(this).getLoginState() == AccountManager.LoginState.ONLINE) {
//                MedicalMeetingManage.getInstance().resumeMeeting();
//            }
            if (AccountManager.getInstance(this).getLoginState() == AccountManager.LoginState.ONLINE) {
                if( !MedicalMeetingManage.getInstance().getFloatingViewShowType()) {
                    MedicalMeetingManage.getInstance().resumeMeeting();
                }
            }
        }

        MedicalApplication.shareInstance().insertActivity(this);
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (!this.toString().contains("MainActivity")
                && !this.toString().contains("SplashActivity")
                && !this.toString().contains("MeetingRoomActivity")
                && !this.toString().contains("Dialog")) {
//            boolean isMeetingRoomRunning = MeetingManager.getInstance()
//                    .getMeetingRoomRunningState();
//            CustomLog.d(TAG, "onRestart isMeetingRoomRunning "+isMeetingRoomRunning);
//            if (isMeetingRoomRunning) {
//                // 切换到会诊室页面
//                CustomLog.d(TAG, "onResume: 会诊室界面运行中，切换到会诊室界面");
//                //Intent i = new Intent();
//                //i.setClass(this, MeetingRoomActivity.class);
//                //startActivity(i);
//                AccountInfo info = AccountManager.getInstance(BaseActivity.this).getAccountInfo();
//                int meetingId = getIntent().getIntExtra(
//                        ConstConfig.MEETING_ID,0);
//                if(info!=null)
//                    MeetingManager.getInstance().joinMeeting(info.accesstoken, info.nubeNumber, info.nickName, meetingId);
//            }
        }
    }

    @Override
    protected void onPause() {
        CustomLog.d(TAG, "onPause:" + this.toString());
        super.onPause();
        //隐藏软键盘
        CommonUtil.hideSoftInputFromWindow(this);
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        CustomLog.d(TAG, "onResume:" + this.toString());
        super.onResume();
        MobclickAgent.onResume(this);
//        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
    }

    @Override
    protected void onStop() {
        CustomLog.d(TAG, "onStop:" + this.toString());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        CustomLog.d(TAG, "onDestroy:" + this.toString());
        MedicalApplication.shareInstance().deleteActivity(this);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        CustomLog.d(TAG, "onNewIntent:" + this.toString());
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (twiceToExit) {
            exit();
        } else {
            super.onBackPressed();
        }
    }

    public void allowTwiceToExit() {
        twiceToExit = true;
    }

    private void exit() {
        moveTaskToBack(true);
//        if (!isExit) {
//            isExit = true;
//            CustomToast.show(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT);
//            mHandler.sendEmptyMessageDelayed(MSG_EXIT, 2000);
//        } else {
//            MedicalApplication.shareInstance().exit();
//        }
    }

    protected void onMenuBtnPressed() {
        CustomLog.d(TAG, "BaseActivity::onMenuBtnPressed()");
    }

    //joinMeeting(String token, String accountID, String accountName,
    //   int meetingID)
    protected void switchToMeetingRoomActivity(String token, String accountID, String accountName,
                                               int meetingId, String adminId) {
        CustomLog.d(TAG,
                "BaseActivity::switchToMeetingRoomActivity() 切换到会诊室页面. meetingId: "
                        + meetingId + " | adminId: " + adminId);
//        MeetingManager.getInstance().init(getApplicationContext());
//        MeetingManager.getInstance().joinMeeting(token, accountID, accountName, meetingId);
        //Intent i = new Intent();
        //i.setClass(this, MeetingRoomActivity.class);
        //i.putExtra(ConstConfig.MEETING_ID, meetingId);
        //i.putExtra(ConstConfig.ADMIN_PHONE_ID, adminId);
        //startActivity(i);

    }

    protected void switchToMainActivity() {
        Intent i = new Intent(BaseActivity.this, HomeActivity.class);
        startActivity(i);
    }

    protected void showLoadingView(String message) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            mLoadingDialog = CommonUtil.createLoadingDialog(this, message);
            mLoadingDialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }

    protected void showLoadingView(String message,
                                   DialogInterface.OnCancelListener listener) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            mLoadingDialog = CommonUtil.createLoadingDialog(this, message, listener);

            mLoadingDialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }

    protected void showLoadingView(String message,
                                   final DialogInterface.OnCancelListener listener, boolean cancelAble) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
        mLoadingDialog = CommonUtil.createLoadingDialog(this, message, listener);
        mLoadingDialog.setCancelable(cancelAble);
        mLoadingDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onCancel(dialog);
                }
                return false;
            }
        });
        try {
            mLoadingDialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }

    protected void removeLoadingView() {
        CustomLog.i(TAG, "MeetingActivity::removeLoadingView()");
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            try {
                mLoadingDialog.dismiss();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mLoadingDialog = null;
        }
    }

    protected String getLogTag() {
        return this.getClass().getSimpleName();
    }

    /**
     * 公共解析二维码方法
     */
    public void parseBarCodeResult(Intent data) {
        //处理扫描结果（在界面上显示）
        if (data != null) {
//                parseBarCodeResult(data);
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                String result = bundle.getString(CodeUtils.RESULT_STRING);
                CustomLog.d(TAG, "解析的二维码字符串为:" + result);
//                    Toast.makeText(getActivity(), "解析结果:" + result, Toast.LENGTH_LONG).show();
                String[] split = result.split("\\?");
//                    String https = split[0];
//                    CustomLog.e("TAG", https);
//                    if (https.equals(HTTPS)) {
                if (split.length < 2) {
                    CustomToast.show(getApplicationContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }
                String[] split1 = split[1].split("=");
                if (split1.length < 2) {
                    CustomToast.show(getApplicationContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }
                String s = split1[1];
                String[] split2 = s.split("_");
                if (split2.length < 3) {
                    CustomToast.show(getApplicationContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }
                mGroupId = split2[1];
                CustomLog.e("TAG", split2[0]);
                CustomLog.e("TAG", split2[1]);
                CustomLog.e("TAG", split2[2]);
//                        if (https.contains(HTTPS)) {
                if (split2[0].equals(PERSON_TYPE)) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), ContactCardActivity.class);
                    intent.putExtra("nubeNumber", split2[1]);
                    intent.putExtra("searchType", "4");
                    startActivity(intent);

                } else if (split2[0].equals(GROUP_TYPE)) {
                    long nowTime = System.currentTimeMillis();
                    long startTime = Long.parseLong(split2[2]);
                    long time = nowTime - startTime;
                    long days = time / (1000 * 60 * 60 * 24);
                    if (days >= 7) {
                        Intent outDateIntent = new Intent();
                        outDateIntent.setClass(getApplicationContext(), OutDateActivity.class);
                        startActivity(outDateIntent);
                    } else {
                        mGroupDao = new GroupDao(getApplicationContext());
                        loginUserInfo = AccountManager.getInstance(getApplicationContext()).getAccountInfo();
                        ;
                        memberDateList = mGroupDao.queryGroupMembers(mGroupId);
                        if (memberDateList.containsKey(loginUserInfo.getNube())) {
                            CustomLog.d(TAG, "用户属于该群，直接进入");
                            enterChatActivity();
                        } else {
                            Intent personIntent = new Intent();
                            personIntent.putExtra(GroupAddActivity.GROUP_ID, split2[1]);
                            personIntent.putExtra(GroupAddActivity.GROUP_ID_FROM, GroupAddActivity.GROUP_ID_FROM);
                            personIntent.setClass(getApplicationContext(), GroupAddActivity.class);
                            startActivity(personIntent);
                        }
                    }

                } else if (split2[0].equals(WE_TYPE)) {
                    MDSAppGetOffAccInfo mdsAppGetOffAccInfo = new MDSAppGetOffAccInfo() {
                        @Override
                        protected void onSuccess(OffAccdetailInfo responseContent) {
                            super.onSuccess(responseContent);
                            String id = responseContent.getId();
                            Intent intentWeChat = new Intent();
                            intentWeChat.putExtra("officialAccountId", id);
                            intentWeChat.setClass(getApplicationContext(), DingYueActivity.class);
                            startActivity(intentWeChat);
                        }

                        @Override
                        protected void onFail(int statusCode, String statusInfo) {
                            super.onFail(statusCode, statusInfo);
                            CustomToast.show(getApplicationContext(), getString(R.string.officialAccountsNotExist), 8000);
                            return;
                        }
                    };
                    mdsAppGetOffAccInfo.appGetOffAccInfo(AccountManager.getInstance(getApplicationContext())
                            .getAccountInfo().getToken(), split2[1]);
                } else if (split2[0].equals(ARTICLE_PREVIEW)) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), ArticlePreviewActivity.class);
                    intent.putExtra(INTENT_DATA_ARTICLE_ID, split2[1]);
                    startActivity(intent);
                } else {
                    CustomToast.show(getApplicationContext(), getString(R.string.noOurCompanyQRcode), 8000);
                    return;
                }

            } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {                ;
                CustomToast.show(getApplicationContext(), getString(R.string.parseTheQrcodeFailed), 8000);
            }
        }

    }

    private void enterChatActivity() {
        Intent i = new Intent(getApplicationContext(), ChatActivity.class);
        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
        i.putExtra(ChatActivity.KEY_CONVERSATION_ID, mGroupId);
        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE, ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, mGroupId);
        startActivity(i);
        finish();
    }



}
