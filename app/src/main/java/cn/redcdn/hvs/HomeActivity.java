package cn.redcdn.hvs;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.datacenter.config.ConstConfig;
import cn.redcdn.datacenter.hpucenter.HPUGetDTlist;
import cn.redcdn.datacenter.hpucenter.data.DTInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInfomation;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInfomation;
import cn.redcdn.hvs.accountoperate.activity.AuditErrorActivity;
import cn.redcdn.hvs.accountoperate.activity.AuditingActivity;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.base.FragmentFactory;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.head.fragment.HeadFragment;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.work.MessageReceiveAsyncTask;
import cn.redcdn.hvs.meeting.activity.InputMeetingPasswordDialog;
import cn.redcdn.hvs.meeting.activity.ReserveSuccessActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.util.BadgeUtil;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;
import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.redcdn.hvs.accountoperate.activity.LoginActivity.UNAGREE_REASON;
import static cn.redcdn.hvs.contacts.ContactsFragment.mSideBar;
import static cn.redcdn.hvs.contacts.ContactsFragment.tvSelect;

public class HomeActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    private ViewPager main_ViewPager;
    private RadioGroup main_tab_RadioGroup;
    private RadioButton rb_message, rb_contacts, rb_head, rb_me, rb_meeting;

    private final String TAG = "HomeActivity";
    private static final String TAG_HEAD = HeadFragment.class.getName();
    private static final String CHAT_ACTIVITY = ChatActivity.class.getName();
    public static final int TAB_INDEX_CHAT = 0;
    // 记录顶层操作，当前fragment的TAG，上面定义的4个固定TAG
    private static String currentTag = "";

    //    private final int REQUEST_READ_PHONE_STATE = 111;

    private NoticesDao noticeDao = null;
    private TextView newNoticeNum = null;
    private ImageView newMsgFlag = null;
    private TextView dtNewNoticeNum = null; //未读会诊消息 个数
    private MessageObserver msgObserver = null;
    public static final String TAB_INDICATOR_INDEX = "HomeActivity.indicator.index";
    public static final String TAB_INDEX_MESSAGE = "CHAT_ACTIVITY";
    public int index;
    public static Boolean isFromChatActivity = false;
    private TextView tvNewFriendsNumber = null;
    private StrangerMessageObserver observeStrangeRelation;
    private final int MSG_MESSAGE_NUMBER_CHANGED = 701;
    private DTNociceObserver observerDTNoticeNum;
    private DtNoticesDao dtNoticesDao = null;
    private final int MSG_DTNOTICE_NUMBER_CHANGED = 801; //会诊消息发生变化

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_MESSAGE_NUMBER_CHANGED:
                    int newFriendsCount = FriendsManager.getInstance().getNotReadMsgSize();
                    CustomLog.d(TAG, "MSG_MESSAGE_NUMBER_CHANGED getNotReadMsgSize");
                    if (null == String.valueOf(newFriendsCount)) {
                        CustomLog.d(TAG, "null==MSG_MESSAGE_NUMBER_CHANGED newFriendsCount");
                        newFriendsCount = 0;
                    } else if (newFriendsCount < 0) {
                        CustomLog.d(TAG, "MSG_MESSAGE_NUMBER_CHANGED newFriendsCount<0");
                        newFriendsCount = 0;
                    } else {
                        CustomLog.d(TAG, "MSG_MESSAGE_NUMBER_CHANGED newFriendsCount:" + newFriendsCount);
                    }
                    showNewFriendsCount(newFriendsCount);
                    break;
                case MSG_DTNOTICE_NUMBER_CHANGED:
                    showDtNotice(); //
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 为了解决上传收藏服务器失败没有重试入口的问题
        // 此行代码的作用是重新同步收藏表中的未同步消息至收藏服务器
        // TODO 以后会通过设计任务优先队列实现自动重试，或在 UI 层添加重试入口
        // CollectionUpdateWorkManager.getInstance().startUpdate();


        allowTwiceToExit();
        noticeDao = new NoticesDao(this);
        //初始化布局
        InitView();

        //初始化viewPager
        InitViewPager();

        //        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        //
        //        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
        //            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        //        } else {
        //            //TODO
        //        }

        CustomLog.d(TAG, "[性能监控] ==> 启动SIP服务 start ");
        // 启动CallManageService（SIP服务）
        if (Build.CPU_ABI.equalsIgnoreCase("armeabi-v7a")) {
            //            new Thread(new Runnable() {
            //                @Override
            //                public void run() {
            //                    // TODO Auto-generated method stub
            //                    CustomLog.d(TAG, "AppP2PAgentManager 开始启动");
            //                    AppP2PAgentManager.init();
            //                }
            //            }).start();
            AppP2PAgentManager.init();
            AppP2PAgentManager.getInstance().connectIMService();

        } else {
            CommonUtil.showToast(getString(R.string.cpu_version_too_low));
            CustomLog.d(TAG, "Toast:CPU ABI版本过低,启动聊天服务失败");
        }
        CustomLog.d(TAG, "[性能监控] ==> 启动SIP服务 end ");

        if (msgObserver == null) {
            msgObserver = new MessageObserver();
            getContentResolver().registerContentObserver(
                    ProviderConstant.NETPHONE_NOTICE_URI, true, msgObserver);
        }

        //应用重启重新发送未完成的消息
        MedicalApplication.getFileTaskManager().updateRunningTask2Fail();

        IntentFilter filter = new IntentFilter();
        filter.addAction("NoticeCountBroaddcase");
        this.registerReceiver(noticeChangeReceive, filter);
        processData();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processData();
    }


    BroadcastReceiver noticeChangeReceive = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("NoticeCountBroaddcase")) {
                int count = intent.getIntExtra("newNoticeCount", 0);
                CustomLog.d(TAG, "updateNoticeCount");
                updateNoticesInfo(count);
            }
        }
    };

    //    @Override
    //    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    //        switch (requestCode) {
    //            case REQUEST_READ_PHONE_STATE:
    //                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
    //                    //TODO
    //                    CustomLog.d(TAG,"权限申请成功");
    //                }
    //                break;
    //
    //            default:
    //                break;
    //        }
    //    }


    private void InitViewPager() {
        main_ViewPager = (ViewPager) findViewById(R.id.main_ViewPager);
        //填充ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        main_ViewPager.setAdapter(adapter);
        //设置当前为第一个页面
        main_ViewPager.setCurrentItem(2);
        //设置viewPager的页面监听器
        main_ViewPager.setOnPageChangeListener(new ViewPagerListener());
        main_ViewPager.setOffscreenPageLimit(4);
    }


    private void InitView() {
        main_tab_RadioGroup = (RadioGroup) findViewById(R.id.main_tab_RadioGroup);

        int btnWidth = (int) getResources().getDimension(R.dimen.x50);
        int btnHeight = (int) getResources().getDimension(R.dimen.y50);

        rb_message = (RadioButton) findViewById(R.id.rb_message);
        Drawable drawable_message = getResources().getDrawable(R.drawable.message_button);
        drawable_message.setBounds(0, 0, btnWidth, btnHeight);
        rb_message.setCompoundDrawables(null, drawable_message, null, null);

        rb_contacts = (RadioButton) findViewById(R.id.rb_contacts);
        Drawable drawable_contacts = getResources().getDrawable(R.drawable.subscribe_button);
        drawable_contacts.setBounds(0, 0, btnWidth, btnHeight);
        rb_contacts.setCompoundDrawables(null, drawable_contacts, null, null);

        rb_head = (RadioButton) findViewById(R.id.rb_head);
        Drawable drawable_head = getResources().getDrawable(R.drawable.meeting_button);
        drawable_head.setBounds(0, 0, btnWidth, btnHeight);
        rb_head.setCompoundDrawables(null, drawable_head, null, null);

        rb_meeting = (RadioButton) findViewById(R.id.rb_meeting);
        Drawable drawable_meeting = getResources().getDrawable(R.drawable.contact_button);
        drawable_meeting.setBounds(0, 0, btnWidth, btnHeight);
        rb_meeting.setCompoundDrawables(null, drawable_meeting, null, null);

        rb_me = (RadioButton) findViewById(R.id.rb_me);
        Drawable drawable_me = getResources().getDrawable(R.drawable.me_button);
        drawable_me.setBounds(0, 0, btnWidth, btnHeight);
        rb_me.setCompoundDrawables(null, drawable_me, null, null);

        main_tab_RadioGroup.check(R.id.rb_head);

        main_tab_RadioGroup.setOnCheckedChangeListener(this);
        newNoticeNum = (TextView) this.findViewById(R.id.total_new_notice_num);
        tvNewFriendsNumber = (TextView) findViewById(R.id.total_new_friends_num);
        newMsgFlag = (ImageView) findViewById(R.id.new_msg_flag);
        dtNewNoticeNum = (TextView) findViewById(R.id.total_dt_notice_num);


        int newFriendsCount = FriendsManager.getInstance().getNotReadMsgSize();

        CustomLog.d(TAG, "getNotReadMsgSize");
        if (null == String.valueOf(newFriendsCount)) {
            CustomLog.d(TAG, "null == String.valueOf(newFriendsCount)");
            newFriendsCount = 0;
        } else if (newFriendsCount < 0) {
            CustomLog.d(TAG, "newFriendsCount<0,newFriendsCount:" + newFriendsCount);
            newFriendsCount = 0;
        } else {
            CustomLog.d(TAG, "newFriendsCount:" + newFriendsCount);
        }
        showNewFriendsCount(newFriendsCount);
        showDtNotice();

        if (observeStrangeRelation == null) {
            observeStrangeRelation = new StrangerMessageObserver();
            getContentResolver().registerContentObserver(
                    ProviderConstant.Strange_Message_URI, true,
                    observeStrangeRelation);
        }

        if (observerDTNoticeNum == null){
            observerDTNoticeNum = new DTNociceObserver();
            getContentResolver().registerContentObserver(ProviderConstant.NETPHONE_HPU_NOTICE_URI,true,observerDTNoticeNum);
        }


    }


    @Override
    protected void showLoadingView(String message) {
        super.showLoadingView(message);
    }


    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        //获取当前被选中的RadioButton的ID,用于改变viewPager的页面
        int current = 0;
        switch (i) {
            case R.id.rb_head:
                current = 2;
                currentTag = TAG_HEAD;
                break;
            case R.id.rb_meeting:
                current = 1;
                break;
            case R.id.rb_message:
                current = 0;
                break;
            case R.id.rb_contacts:
                current = 3;
                break;
            case R.id.rb_me:
                current = 4;
                break;
        }
        if (main_ViewPager.getCurrentItem() != current) {
            main_ViewPager.setCurrentItem(current);
        }
    }


    private class ViewPagerListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (null != tvSelect) {
                tvSelect.setVisibility(View.INVISIBLE);
            }
            if (null != mSideBar) {
                mSideBar.setBackgroundColor(Color.parseColor("#00000000"));
            }
        }


        @Override
        public void onPageSelected(int position) {
            //获取当前页面用于改变radioButton的状态
            int current = main_ViewPager.getCurrentItem();
            switch (current) {
                case 2:
                    main_tab_RadioGroup.check(R.id.rb_head);
                    break;
                case 1:
                    main_tab_RadioGroup.check(R.id.rb_meeting);
                    break;
                case 0:
                    main_tab_RadioGroup.check(R.id.rb_message);
                    break;
                case 3:
                    main_tab_RadioGroup.check(R.id.rb_contacts);
                    break;
                case 4:
                    main_tab_RadioGroup.check(R.id.rb_me);
                    break;
                default:
                    break;
            }
        }


        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }


    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private String[] rbs;

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            rbs = CommonUtil.getStringArray(R.array.rb_names);
        }


        /*返回每一页需要的fragment*/
        @Override
        public Fragment getItem(int position) {
                return FragmentFactory.create(position);
        }


        @Override
        public int getCount() {
            return rbs.length;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return rbs[position];
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // updateNoticesInfo();
        showMessageFragment();
        //清除smarticon
        MessageReceiveAsyncTask.newMsgCount = 0;
        BadgeUtil.setBadgeCount(this, 0);
        HPUGetDTlist getDTlist = new HPUGetDTlist(){
            @Override
            protected void onSuccess(List<DTInfo> responseContent) {
                super.onSuccess(responseContent);
                if (compareDtlist(AccountManager.getInstance(HomeActivity.this).hpuList,responseContent)){
                    AccountManager.getInstance(HomeActivity.this).setNeedRefresh(true);
                    ContactManager.getInstance(HomeActivity.this).doShareContactSync();
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG,"getDtList onfail statusCode"+statusCode + "statusInfo " +statusInfo);
            }
        };

        getDTlist.getdtlist(AccountManager.getInstance(HomeActivity.this).getMdsToken());

    }


    private void showNewFriendsCount(int count) {

        if (count == 0) {
            tvNewFriendsNumber.setVisibility(View.INVISIBLE);
        } else {
            if (count > 99) {
                tvNewFriendsNumber.setText("99+");
            } else {
                tvNewFriendsNumber.setText(String.valueOf(count));
            }
            tvNewFriendsNumber.setVisibility(View.VISIBLE);
        }

    }

    private void showDtNoticeCount(int count) {

        if (count == 0) {
            dtNewNoticeNum.setVisibility(View.INVISIBLE);
        } else {
            if (count > 99) {
                dtNewNoticeNum.setText("99+");
            } else {
                dtNewNoticeNum.setText(String.valueOf(count));
            }
            dtNewNoticeNum.setVisibility(View.VISIBLE);
        }

    }


    private void showMessageFragment() {

        if (isFromChatActivity) {
            main_ViewPager.setCurrentItem(0);
            isFromChatActivity = false;
        }

    }


    /**
     * @Title: getCurrentTag
     * @Description: 返回当前显示页面tag
     * @return: String
     */
    public String getCurrentTag() {
        return currentTag;
    }


    private void updateNoticesInfo(int count) {
        // count = noticeDao.getNewNoticeCount();
        if (count == 0) {
            newNoticeNum.setVisibility(View.INVISIBLE);
            newMsgFlag.setVisibility(View.INVISIBLE);
        } else if (count == NoticesDao.NO_DISTRUB_FLAG) {
            newNoticeNum.setVisibility(View.INVISIBLE);
            newMsgFlag.setVisibility(View.VISIBLE);
        } else {
            if (count > 99) {
                // newNoticeNum
                //     .setBackgroundResource(R.drawable.chat_unread_count_bar);
                newNoticeNum.setText(R.string.main_bottom_count_99);
            } else {
                // newNoticeNum
                //     .setBackgroundResource(R.drawable.chat_unread_count_bar);
                newNoticeNum.setText(String.valueOf(count));
            }
            newNoticeNum.setVisibility(View.VISIBLE);
            newMsgFlag.setVisibility(View.INVISIBLE);
        }

    }


    private class MessageObserver extends ContentObserver {

        public MessageObserver() {
            super(new Handler());
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // updateNoticesInfo();
        }
    }

    /**
     * 监听会诊消息数据库
     */
    private class  DTNociceObserver extends ContentObserver{
        public DTNociceObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG, "会诊消息数据库数据发生变更");
            mHandler.sendEmptyMessage(MSG_DTNOTICE_NUMBER_CHANGED);
        }

    }
    private void showDtNotice(){
        int newNoticeCount = 0;
        if (dtNoticesDao == null){
            dtNoticesDao = new DtNoticesDao(this);
        }
        try {
            newNoticeCount =  dtNoticesDao.queryALLUnreadNotice();
        }catch (Exception e){
            CustomLog.e(TAG,"getDtNoticeCount fail message = " +e.getMessage());
        }
        showDtNoticeCount(newNoticeCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(noticeChangeReceive);
        if (observeStrangeRelation != null) {
            getContentResolver().unregisterContentObserver(observeStrangeRelation);
            observeStrangeRelation = null;
        }
        if (observerDTNoticeNum!=null){
            getContentResolver().unregisterContentObserver(observerDTNoticeNum);
            observerDTNoticeNum = null;
        }
    }


    /**
     * 判断是否通过短信链接启动以决定是否需要跳转到对应页面
     */
    private void processData() {
        boolean isMeetingRoomRunning = MeetingManager.getInstance()
                .getMeetingRoomRunningState();
        boolean isFromMessageLink = MedicalApplication.shareInstance()
                .getIsFromMessageLink();
        CustomLog.i(TAG, "是否通过短信链接启动：" + isFromMessageLink + " | 会诊室页面是否正在运行："
                + isMeetingRoomRunning);
        if (isFromMessageLink && !isMeetingRoomRunning) {
            MDSAccountInfo info = AccountManager.getInstance(HomeActivity.this).getAccountInfo();
            String meetingId = getIntent().getStringExtra("urlMeetingId");
            if (info != null) {
                if (meetingId != null) {
                    execJoinMeeting(meetingId, true);
                } else {
                    CustomLog.e(TAG, "urlMeetingId为空");
                }
            }
        } else if (isFromMessageLink && isMeetingRoomRunning) {
            //是否加入新的会议的对话框
            final CustomDialog dialog = new CustomDialog(this);
            dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                @Override
                public void onClick(CustomDialog customDialog) {
                    // TODO Auto-generated method stub
                    //加入新的会议
                    dialog.dismiss();
                    MDSAccountInfo info = AccountManager.getInstance(HomeActivity.this).getAccountInfo();
                    String meetingId = getIntent().getStringExtra("urlMeetingId");
                    if (info != null) {
                        if (meetingId != null) {
                            execJoinMeeting(meetingId, true);
                        } else {
                            CustomLog.e(TAG, "urlMeetingId为空");
                        }
                    }
                }

            });

            dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
                @Override
                public void onClick(CustomDialog customDialog) {
                    // TODO Auto-generated method stub
                    //回到之前的会议
                    dialog.dismiss();
                    MDSAccountInfo info = AccountManager.getInstance(HomeActivity.this).getAccountInfo();
                    int meetingId = getIntent().getIntExtra(
                            ConstConfig.MEETING_ID, 0);
                    if (info != null) {
                        MeetingManager.getInstance().joinMeeting(info.accessToken, info.getNube(), info.nickName, meetingId);
                    }
                }

            });

            if (SettingData.runDevice == SettingData.RunDevice.TV) {
                dialog.setTip(getString(R.string.is_join_new_meeting));
            } else {
                dialog.setTip(getString(R.string.is_join_new_consultation));
            }
            dialog.setOkBtnText(getString(R.string.yes));
            dialog.setCancelBtnText(getString(R.string.no));
            dialog.setCancelable(false);
            dialog.show();

        } else if (isMeetingRoomRunning) {
            MDSAccountInfo info = AccountManager.getInstance(HomeActivity.this).getAccountInfo();
            int meetingId = getIntent().getIntExtra(
                    ConstConfig.MEETING_ID, 0);
            if (info != null) {
                MeetingManager.getInstance().joinMeeting(info.accessToken, info.getNube(), info.nickName, meetingId);
            }
        }

        MedicalApplication.shareInstance().setIsFromMessageLink(false);
    }


    private void execJoinMeeting(final String str, final boolean addPut) {
        CustomLog.v(TAG, "execJoinMeeting");
        final GetMeetingInfomation gi = new GetMeetingInfomation() {
            @Override
            protected void onSuccess(MeetingInfomation responseContent) {
                super.onSuccess(responseContent);
                HomeActivity.this.removeLoadingView();
                MDSAccountInfo info = AccountManager.getInstance(
                        MedicalApplication.shareInstance().getApplicationContext()).getAccountInfo();
                String nube = AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().getNube();
                String meetingId = str;
                String meetingHost = responseContent.meetingHost;
                if (responseContent.meetingStatus == 3) {//会议已经结束
                    CustomToast
                            .show(getApplicationContext(), getString(R.string.consultation_has_ended), Toast.LENGTH_SHORT);
                    return;
                }
                if (info != null) {
                    if (responseContent.meetingType == 2) {//预约会议
                        BookMeetingExInfo meetingInfo = new BookMeetingExInfo();
                        meetingInfo.setBookNube(responseContent.terminalAccount);
                        meetingInfo.setBookName(responseContent.terminalAccountName);
                        meetingInfo.setMeetingRoom(String.valueOf(responseContent.meetingId));
                        meetingInfo.setMeetingTheme(responseContent.topic);
                        meetingInfo.setMeetingUrl(MedicalMeetingManage.JMEETING_INVITE_URL);
                        meetingInfo.setMeetingTime(Long.parseLong(responseContent.yyBeginTime) * 1000);
                        meetingInfo.setHasMeetingPassWord(responseContent.hasMeetingPwd);
                        Intent i = new Intent(HomeActivity.this,
                                ReserveSuccessActivity.class);
                        i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, meetingInfo);
                        startActivity(i);
                    } else {//及时会议
                        if (responseContent.hasMeetingPwd == 0) {//无会议密码  0
                            joinMeeting(String.valueOf(responseContent.meetingId));
                        } else if (responseContent.hasMeetingPwd == 1) {//有会议密码  1
                            if (String.valueOf(meetingHost).equalsIgnoreCase(info.getNube())) {//我召开的
                                joinMeeting(String.valueOf(responseContent.meetingId));
                            } else {//不是我召开的
                                Intent iv = new Intent();
                                iv.setClass(HomeActivity.this, InputMeetingPasswordDialog.class);
                                //TODO 不知道作用
                                iv.putExtra("accesstoken", info.accessToken);
                                iv.putExtra("nubeNumber", info.nube);
                                iv.putExtra("nickName", info.nickName);
                                iv.putExtra("meetingId", meetingId);
                                iv.putExtra("nube", nube);
                                iv.putExtra("isInputID", String.valueOf(false));
                                startActivity(iv);
                            }
                        } else {
                            joinMeeting(String.valueOf(responseContent.meetingId));
                        }
                    }
                }
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);

                KeyEventWrite.write(KeyEventConfig.CHECK_MEETINGNUM_INVALID
                        + "_fail"
                        + "_"
                        + AccountManager.getInstance(
                        MedicalApplication.shareInstance().getApplicationContext())
                        .getAccountInfo().getNube() + "_" + statusCode);

                HomeActivity.this.removeLoadingView();

                if (statusCode == -906) {
                    String InvalidAccountText;
                    if (SettingData.runDevice == SettingData.RunDevice.TV) {
                        InvalidAccountText = getString(R.string.meeting_nub_error);
                    } else {
                        InvalidAccountText = getString(R.string.consultation_nub_error);
                    }

                    CustomToast.show(getApplicationContext(), InvalidAccountText, Toast.LENGTH_SHORT);
                    return;
                }
                if (statusCode == -999) {
                    CustomToast.show(getApplicationContext(), getString(R.string.system_error),
                            Toast.LENGTH_SHORT);
                }
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(HomeActivity.this, getString(R.string.login_checkNetworkError),
                            Toast.LENGTH_LONG);
                    return;
                }
                if (statusCode == SettingData.getInstance().tokenUnExist
                        || statusCode == SettingData.getInstance().tokenInvalid) {
                    AccountManager.getInstance(getApplicationContext()).tokenAuthFail(
                            statusCode);
                }

                CustomToast.show(getApplicationContext(), getString(R.string.join_consultation_fail_code) + statusCode,
                        Toast.LENGTH_SHORT);
            }

        };
        getMeetingInfo(str, gi);
    }


    private void getMeetingInfo(final String str, final GetMeetingInfomation gi) {
        CustomLog.i(TAG, " getMeetingInfo::获取MedicalMeetingManage初始化状态" + MedicalMeetingManage.getInstance().getInitState());
        switch (MedicalMeetingManage.getInstance().getInitState()) {
            case INITIALIZING:
                showLoading(gi);
                MedicalMeetingManage.getInstance().addInitListener(new MedicalMeetingManage.OnInitListener() {
                    @Override
                    public void onInit(String valueDes, int valueCode) {
                        if (valueCode == 0) {
                            gi.getMeetingInfomation(Integer.parseInt(str));
                        } else {
                            HomeActivity.this.removeLoadingView();
                        }
                    }
                });
                break;
            case SUCCESS:
                showLoading(gi);
                gi.getMeetingInfomation(Integer.parseInt(str));
                break;
            case NONE:
            case FAILED:
                break;
        }
    }


    private void showLoading(final GetMeetingInfomation gi) {
        HomeActivity.this.showLoadingView(getString(R.string.get_consultation_info), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                gi.cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_get_consultation_info), Toast.LENGTH_SHORT);
            }
        });
    }


    private void joinMeeting(String meetingId) {
        int i = MedicalMeetingManage.getInstance().joinMeeting(meetingId,
                new MedicalMeetingManage.OnJoinMeetingListener() {
                    @Override
                    public void onJoinMeeting(String valueDes, int valueCode) {
                        //TODO 加入会议的返回键处理
                        removeLoadingView();
                    }
                });
        if (i == 0) {
            showLoadingView(getString(R.string.join_consultation), new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    dialog.dismiss();
                    MedicalMeetingManage.getInstance().cancelJoinMeeting("HomeActivity");
                    CustomToast.show(getApplicationContext(), getString(R.string.cancel_join_consultation), Toast.LENGTH_SHORT);
                }
            });
        }else {
            CustomToast.show(HomeActivity.this,getString(R.string.join_consultation_fail) ,
                    CustomToast.LENGTH_SHORT);
        }
    }

    /**
     * 监听陌生人消息表
     */
    private class StrangerMessageObserver extends ContentObserver {

        public StrangerMessageObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG, "陌生人消息数据库数据发生变更");
            mHandler.sendEmptyMessage(MSG_MESSAGE_NUMBER_CHANGED);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        int state = AccountManager.getInstance(MedicalApplication.shareInstance()).getAccountInfo().getState();
        if (state == 1) {
            HomeActivity.this.finish();
            Intent intent = new Intent(HomeActivity.this, AuditingActivity.class);
            startActivity(intent);
        } else if (state == 3) {
            HomeActivity.this.finish();
            String unagreeReason = AccountManager.getInstance(MedicalApplication.shareInstance()).getAccountInfo().getReason();
            Intent intent = new Intent(HomeActivity.this, AuditErrorActivity.class);
            intent.putExtra(UNAGREE_REASON, unagreeReason);
            startActivity(intent);
        }
    }

    private boolean compareDtlist(List<DTInfo> aList,List<DTInfo> bList){
        boolean falg = false;
        if (aList.size()!=bList.size()){
            falg = true;
        }else {
            if (aList.size()>0 && bList.size()>0){
                Map<String,DTInfo> map = new HashMap<>();
                for (DTInfo info:bList){
                    map.put(info.getId(),info);
                }
                for (DTInfo info:aList){
                    if (!map.containsKey(info.getId())){
                        falg = true;
                    }
                }
                }
            }

        return falg;

    }


}
