package cn.redcdn.hvs.im.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.datacenter.medicalcenter.MDSAPPGetGroupCsl;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSGroupCslInfo;
import cn.redcdn.datacenter.meetingmanage.CheckMeetingPwd;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInfomation;
import cn.redcdn.datacenter.meetingmanage.data.CheckMeetingPwdInfo;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInfomation;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.HomeActivity;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.VerificationReplyDialog;
import cn.redcdn.hvs.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.adapter.ChatListAdapter;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.bean.ButelPAVExInfo;
import cn.redcdn.hvs.im.bean.ButelVcardBean;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ShowNameUtil.NameElement;
import cn.redcdn.hvs.im.bean.StrangerMessage;
import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.common.CommonWaitDialog;
import cn.redcdn.hvs.im.common.ThreadPoolManger;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NetPhoneDaoImpl;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.manager.GroupChatInterfaceManager;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.receiver.NetWorkChangeReceiver;
import cn.redcdn.hvs.im.task.QueryConvstNoticeAsyncTask;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.util.ListSort;
import cn.redcdn.hvs.im.util.PinyinUtil;
import cn.redcdn.hvs.im.util.PlayerManager;
import cn.redcdn.hvs.im.util.SendCIVMUtil;
import cn.redcdn.hvs.im.util.WakeLockHelper;
import cn.redcdn.hvs.im.view.BottomMenuWindow;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.im.view.MedicalAlertDialog;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.im.view.VoiceTipView;
import cn.redcdn.hvs.meeting.activity.InputMeetingPasswordDialog;
import cn.redcdn.hvs.meeting.activity.ReserveSuccessActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.profiles.activity.CollectionActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.NotificationUtil;
import cn.redcdn.hvs.util.StringUtil;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.jmeetingsdk.JMeetingAgent;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import static android.hardware.Sensor.TYPE_PROXIMITY;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.OVER_SCROLL_NEVER;
import static android.view.View.VISIBLE;
import static cn.redcdn.hvs.MedicalApplication.getFileTaskManager;
import static cn.redcdn.hvs.im.activity.GroupChatDetailActivity.KEY_NUMBER;

/**
 * Desc    聊天页面  单聊、群聊
 * Created by wangkai on 2017/2/24.
 */

public class ChatActivity extends BaseActivity implements ChatListAdapter.CallbackInterface,
    ChatListAdapter.MeetingLinkClickListener
    , SensorEventListener {
    private static final String TAG = ChatActivity.class.getSimpleName();

    // // 上一次查询的时间
    // 添加好友task
    // 群聊ID
    public static final String KEY_GROUP_ID = "KEY_GROUP_ID";
    // 群聊名称
    public static final String KEY_DROUP_NAME = "KEY_DROUP_NAME";

    // 消息转发，改走 本地照片分享逻辑
    public static final int ACTION_FORWARD_NOTICE = 1105;

    //用户来源是陌生人，用户来源 0:视讯号搜索，1:手机通讯录好友推荐，2:手机号搜索， 3:邮箱搜索,
    //  4:二维码扫描,  5:群内添加,  6:陌生人聊天添加
    private static final int ADD_USER_TYPE = 6;
    /**
     * 消息界面类型
     */
    public static final String KEY_NOTICE_FRAME_TYPE = "key_notice_frame_type";
    /**
     * 新消息界面
     */
    public static final int VALUE_NOTICE_FRAME_TYPE_NEW = 1;
    /**
     * 聊天列表界面
     */
    public static final int VALUE_NOTICE_FRAME_TYPE_LIST = 2;

    /**
     * 通过nube好友发送消息
     */
    public static final int VALUE_NOTICE_FRAME_TYPE_NUBE = 3;
    /**
     * 聊天类型
     */
    public static final String KEY_CONVERSATION_TYPE = "key_conversation_type";
    /**
     * 单人聊天
     */
    public static final int VALUE_CONVERSATION_TYPE_SINGLE = 1;

    /**
     * 群发
     */
    public static final int VALUE_CONVERSATION_TYPE_MULTI = 2;
    /**
     * 会话ID
     */
    public static final String KEY_CONVERSATION_ID = "key_conversation_id";
    /**
     * 会话扩展信息
     */
    public static final String KEY_CONVERSATION_EXT = "key_conversation_ext";
    /**
     * 会话对象视讯号
     */
    public static final String KEY_CONVERSATION_NUBES = "key_conversation_nubes";
    /**
     * 聊天对象名称
     */
    public static final String KEY_CONVERSATION_SHORTNAME = "key_conversation_shortname";
    /**
     * 聊天页面返回标记
     */
    public static final String KEY_CHAT_BACK_FLAG = "key_chat_back_flag";
    private static final String VOICE_PREFS_NAME = "VoicePrefsFile";

    public static final String KEY_SERVICE_NUBE_INFO = "ServiceNubeInfo";
    //语音消息扬声器播放模式
    private static final boolean SPEAKER = true;

    //语音消息听筒播放模式
    private static final boolean HEADSET = false;
    public static final int PERMISSIONS_REQUEST_CAMERA_CODE = 0;
    public static final int PERMISSIONS_REQUEST_AUDIO_RECORD_CODE = 1;
    /**
     * 添加为好友
     */

    // 界面类型(单聊，群聊)
    private int frameType = VALUE_NOTICE_FRAME_TYPE_NEW;
    // 当前会话ID(单聊,群聊)
    private String convstId = "";

    // 聊天类型(单聊，群聊)
    private int conversationType = VALUE_CONVERSATION_TYPE_SINGLE;
    //系统 Nube
    private static final String SYS_NUBE = "10000";
    // 聊天对象视讯号
    private String targetNubeNumber = "";
    // 聊天对象名称(单聊)
    private String targetShortName = "";

    // 会话扩展信息(单聊，群聊)
    private String convstExtInfo = "";

    // 列表listview
    private ListView noticeListView = null;
    // 列表适配器
    private ChatListAdapter chatAdapter = null;
    // Load ImageData
    private View headerLoadingView = null;

    private View headerRoot = null;

    // private long lastQueryTime = 0l;
    private Handler mHandler = new Handler();
    // 自身视讯号
    private String selfNubeNumber = "";
    // 数据变更监听
    private NoticesTableObserver observer = null;
    // 系统camera拍照或拍视频文件路径
    private String cameraFilePath = "";
    // 待转发的消息ID
    private String forwardNoticeId = null;
    // 官方帐号
    //    private String butelPubNubeNum = "";
    // 添加好友的nube号码
    private String addFriendNube = "";

    //    private AsyncTasks addLinkmanTask = null;

    // 输入区域
    private ChatInputFragment inputFragment = new ChatInputFragment();
    // 收件人视频号码
    public ArrayList<String> receiverNumberLst = new ArrayList<String>();

    // 收件人名称
    public Map<String, String> receiverNameMap = new HashMap<String, String>();
    // 是否选择联系人
    private boolean isSelectReceiver = false;

    // 创建群界面是否要保留界面
    private boolean isSaveDraft = true;
    // 全部消息的起始时间
    private long recvTimeBegin = 0l;
    private Object recvTimeLock = new Object();
    // 群组id
    private String groupId = "";
    // 群组表监听器
    private GroupMemberObserver observeGroupMember;

    // 群成员表监听器
    private GroupObserver groupObserve;

    private FriendRelationObserver observeFriendRelation;
    // 存放群成员人数
    private int groupMemberSize = 0;
    // 是否是群成员
    private boolean isGroupMember = false;
    private ArrayList<String> selectNubeList = new ArrayList<String>();
    // 收件人名称
    public ArrayList<String> selectNameList = new ArrayList<String>();
    private LinkedHashMap<String, GroupMemberBean> dateList
        = new LinkedHashMap<String, GroupMemberBean>();// 显示数据
    protected CommonWaitDialog waitDialog;
    // 页面返回标记
    private boolean back_flag = true;
    // 软件盘是否弹起的判断标记
    private boolean SoftInput = false;
    // 第一次进入页面标记
    private boolean firstFlag = true;

    private NoticesDao noticeDao = null;
    private ThreadsDao threadDao = null;
    private GroupDao groupDao = null;

    //多选操作模式下的相关widget和数据对象
    private RelativeLayout moreOpLayout = null;
    private ImageButton forwardBtn = null;
    private ImageButton collectBtn = null;
    private ImageButton delBtn = null;

    private Context mContext = this;
    private TextView newNoticeNum;

    private TitleBar titleBar;
    private boolean titlebackbtn = false;
    private Button backbtn;
    private TextView backtext;
    private LinearLayout chatlayout;
    private LinearLayout addMeetinglayout;
    private TextView addMeetingThemeTxt;

    //修改后的群名称
    // private String nameForModify = "";
    private Boolean newNoticeNumflag = false;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean isPlaying = false;
    //初始化屏幕亮暗帮助类
    private WakeLockHelper helper;
    //初始化
    //    private AudioManagerHelper audioHelper;
    private String audioPath;
    SharedPreferences voiceMsgSettings;
    private PlayerManager playerManager = PlayerManager.getManager();
    private VoiceTipView voiceTipView;
    MDSAppSearchUsers searchUsers = null;
    private String getGroupClsFail = "";
    private String meetingNub = "";
    private String meetingTheme = "";
    private String meetingPaw = "";
    private JMeetingAgent agent;
    private int getGroupCslCount = 0;
    private BroadcastReceiver netReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        noticeDao = new NoticesDao(this);
        threadDao = new ThreadsDao(this);
        groupDao = new GroupDao(this);
        headerLoadingView = getLayoutInflater().inflate(
            R.layout.page_load_header, null);
        headerRoot = headerLoadingView.findViewById(R.id.header_root);
        selfNubeNumber = AccountManager.getInstance(this).getAccountInfo().nube;
        helper = new WakeLockHelper();
        // 消息界面类型
        if (savedInstanceState == null) {
            frameType = getIntent().getIntExtra(KEY_NOTICE_FRAME_TYPE,
                VALUE_NOTICE_FRAME_TYPE_NEW);
            back_flag = getIntent().getBooleanExtra(KEY_CHAT_BACK_FLAG, true);
            if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST) {
                convstId = getIntent().getStringExtra(KEY_CONVERSATION_ID);
                conversationType = getIntent().getIntExtra(
                    KEY_CONVERSATION_TYPE, VALUE_CONVERSATION_TYPE_SINGLE);
                targetNubeNumber = getIntent().getStringExtra(
                    KEY_CONVERSATION_NUBES);
                groupId = convstId;
                targetShortName = getIntent().getStringExtra(
                    KEY_CONVERSATION_SHORTNAME);
                convstExtInfo = getIntent()
                    .getStringExtra(KEY_CONVERSATION_EXT);

            } else if (frameType == VALUE_NOTICE_FRAME_TYPE_NUBE) {
                targetNubeNumber = getIntent().getStringExtra(
                    KEY_CONVERSATION_NUBES);
                targetShortName = getIntent().getStringExtra(
                    KEY_CONVERSATION_SHORTNAME);
            }
        } else {
            Bundle paramsBundle = savedInstanceState.getBundle("params");
            if (paramsBundle != null) {
                cameraFilePath = paramsBundle.getString("cameraFilePath");
                forwardNoticeId = paramsBundle.getString("forwardNoticeId");
                addFriendNube = paramsBundle.getString("addFriendNube");
                receiverNumberLst = paramsBundle
                    .getStringArrayList("receiverNumberLst");
                receiverNameMap = (Map<String, String>) paramsBundle
                    .getSerializable("receiverNameMap");
                selectNameList = paramsBundle
                    .getStringArrayList("selectNameList");
                selectNubeList = paramsBundle
                    .getStringArrayList("selectNubeList");
                groupId = paramsBundle.getString(KEY_GROUP_ID);
                frameType = paramsBundle.getInt(KEY_NOTICE_FRAME_TYPE,
                    VALUE_NOTICE_FRAME_TYPE_NEW);
                if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST) {
                    convstId = paramsBundle.getString(KEY_CONVERSATION_ID);
                    conversationType = paramsBundle.getInt(
                        KEY_CONVERSATION_TYPE,
                        VALUE_CONVERSATION_TYPE_SINGLE);
                    groupId = paramsBundle.getString(KEY_GROUP_ID);
                    targetNubeNumber = paramsBundle
                        .getString(KEY_CONVERSATION_NUBES);
                    targetShortName = paramsBundle
                        .getString(KEY_CONVERSATION_SHORTNAME);
                    convstExtInfo = paramsBundle
                        .getString(KEY_CONVERSATION_EXT);
                } else if (frameType == VALUE_NOTICE_FRAME_TYPE_NUBE) {
                    targetNubeNumber = paramsBundle
                        .getString(KEY_CONVERSATION_NUBES);
                    targetShortName = paramsBundle
                        .getString(KEY_CONVERSATION_SHORTNAME);
                }
            }
        }
        initWidget();
        initMoreOpWidget();

        initView();
        searchServiceNubeInfo(SettingData.getInstance().adminNubeNum);
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CustomLog.d(TAG, "onNewIntent begin");
        // 消息界面类型
        frameType = intent.getIntExtra(KEY_NOTICE_FRAME_TYPE,
            VALUE_NOTICE_FRAME_TYPE_NEW);
        if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST) {
            convstId = intent.getStringExtra(KEY_CONVERSATION_ID);
            conversationType = intent.getIntExtra(KEY_CONVERSATION_TYPE,
                VALUE_CONVERSATION_TYPE_SINGLE);
            targetNubeNumber = intent.getStringExtra(KEY_CONVERSATION_NUBES);
            CustomLog.d(TAG, "onNewIntent groupId:" + groupId + frameType
                + conversationType);
            groupId = targetNubeNumber;
            if (inputFragment != null) {
                CustomLog.d(TAG, "onNewIntent groupId:" + groupId);
                inputFragment.changedata(groupId);
            }
            targetShortName = intent.getStringExtra(KEY_CONVERSATION_SHORTNAME);
            convstExtInfo = intent.getStringExtra(KEY_CONVERSATION_EXT);
        } else if (frameType == VALUE_NOTICE_FRAME_TYPE_NUBE) {
            conversationType = intent.getIntExtra(KEY_CONVERSATION_TYPE,
                VALUE_CONVERSATION_TYPE_SINGLE);
            targetNubeNumber = intent.getStringExtra(KEY_CONVERSATION_NUBES);
            targetShortName = intent.getStringExtra(KEY_CONVERSATION_SHORTNAME);
            groupId = targetNubeNumber;
            if (inputFragment != null) {
                CustomLog.d(TAG, "onNewIntent groupId:" + targetNubeNumber);
                inputFragment.changedata(targetNubeNumber);
            }
        }

        //        if (addLinkmanTask != null) {
        //            addLinkmanTask.dismissDialog();
        //        }

        if (conversationType == VALUE_CONVERSATION_TYPE_SINGLE) {
            if (observeGroupMember != null) {
                getContentResolver().unregisterContentObserver(observeGroupMember);
                observeGroupMember = null;
            }

            if (groupObserve != null) {
                getContentResolver().unregisterContentObserver(groupObserve);
                groupObserve = null;
            }
        }

        initView();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        CustomLog.d(TAG, "onSaveInstanceState begin");
        Bundle bundle = new Bundle();
        bundle.putString("cameraFilePath", cameraFilePath);
        bundle.putString("forwardNoticeId", forwardNoticeId);
        bundle.putString("addFriendNube", addFriendNube);
        bundle.putStringArrayList("receiverNumberLst", receiverNumberLst);
        bundle.putStringArrayList("selectNubeList", selectNubeList);
        bundle.putStringArrayList("selectNameList", selectNameList);
        bundle.putSerializable("receiverNameMap",
            (Serializable) receiverNameMap);

        bundle.putInt(KEY_NOTICE_FRAME_TYPE, frameType);
        if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST) {
            bundle.putString(KEY_CONVERSATION_ID, convstId);
            bundle.putInt(KEY_CONVERSATION_TYPE, conversationType);
            bundle.putString(KEY_CONVERSATION_NUBES, targetNubeNumber);
            bundle.putString(KEY_GROUP_ID, groupId);
            bundle.putString(KEY_CONVERSATION_SHORTNAME, targetShortName);
            bundle.putString(KEY_CONVERSATION_EXT, convstExtInfo);
        } else if (frameType == VALUE_NOTICE_FRAME_TYPE_NUBE) {
            bundle.putString(KEY_CONVERSATION_NUBES, targetNubeNumber);
            bundle.putString(KEY_CONVERSATION_SHORTNAME, targetShortName);
        }

        outState.putBundle("params", bundle);

        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            getGroupcsl();
            initReceiver();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        setTitleInfo();
        CommonUtil.hideSoftInputFromWindow(ChatActivity.this);
        CustomLog.d(TAG, "onResume begin");
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            dateList.clear();
            dateList.putAll(groupDao.queryGroupMembers(groupId));
            String value = MedicalApplication.getPreference()
                .getKeyValue(PrefType.KEY_CHAT_REMIND_LIST, "");
            if (value != null && value.contains(groupId)) {
                value = value.replace(groupId + ";", "");
                MedicalApplication.getPreference().setKeyValue(
                    DaoPreference.PrefType.KEY_CHAT_REMIND_LIST, value);
            }
            if (groupDao.isGroupMember(groupId, selfNubeNumber)) {
                getTitleBar().enableRightBtn(null,
                    R.drawable.multi_send_btn_selector,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 跳转到群发收件人界面
                            if (CommonUtil.isFastDoubleClick()) {
                                return;
                            }

                            if (groupDao.isGroupMember(groupId,
                                selfNubeNumber)) {
                                // 是群成员，跳转

                                // 群发，跳到群发联系人页面
                                Intent intent = new Intent(
                                    ChatActivity.this,
                                    GroupChatDetailActivity.class);
                                intent.putExtra(
                                    GroupChatDetailActivity.KEY_CHAT_TYPE,
                                    GroupChatDetailActivity.VALUE_GROUP);
                                intent.putExtra(
                                    KEY_NUMBER,
                                    groupId);
                                intent.putExtra(GroupChatDetailActivity.KEY_GETGROUPCSLFAIL,
                                    getGroupClsFail);
                                intent.putExtra(GroupChatDetailActivity.KEY_MEETINGNUB, meetingNub);
                                intent.putExtra(GroupChatDetailActivity.KEY_MEETINGTHEME,
                                    meetingTheme);
                                intent.putExtra(GroupChatDetailActivity.KEY_MEETINGPASSWORD,
                                    meetingPaw);
                                ChatActivity.this.startActivity(intent);

                                CustomLog.d(TAG, "点击右上角群聊图标，跳转到群聊信息页");
                            }
                        }
                    });
            } else {
                getTitleBar().setRightBtnVisibility(INVISIBLE);
            }

            if (!groupDao.existGroup(groupId)) {
                CustomLog.d(TAG, "onResume 群组未在数据库中，需在查询后，重新生成");
                new GroupChatInterfaceManager(MedicalApplication.getContext())
                    .queryGroupDetail(groupId);
                this.finish();
            }
        } else {
            isShowRightButton();
        }
        isSelectReceiver = false;
        // 第一次进入聊天界面，还没有聊天记录的场合，进入界面后，需要自动打开选择面板
        if (!firstFlag) {
            if (SoftInput) {
                inputFragment.showSelectlayout();
            }
        }
        cancelNotifacation();

        //注册传感器监听器
        sensorManager.registerListener(this, proximitySensor,
            SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public void onPause() {
        super.onPause();
        CustomLog.i(TAG, "onPause()");

        //取消注册传感器监听器
        sensorManager.unregisterListener(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        CustomLog.i(TAG, "onStop()");

        hideMoreOpLayout();
        cleanCheckData();
        removeLoadingView();
        //		inputFragment.onStop();

        if (chatAdapter != null) {
            chatAdapter.onStop();
        }

        if (isSelectReceiver) {
            // 选择收件人的场合，不需要保存草稿
            isSelectReceiver = false;
        } else {
            saveDraftTxt();
        }
    }


    @Override
    public void onDestroy() {
        CustomLog.d(TAG, "onDestory begin");
        super.onDestroy();
        cleanCheckData();
        if (observer != null) {
            getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }

        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            String value = MedicalApplication.getPreference().getKeyValue(
                PrefType.KEY_CHAT_REMIND_LIST, "");
            if (value != null && value.contains(groupId)) {
                value = value.replace(groupId + ";", "");
                MedicalApplication.getPreference().setKeyValue(
                    PrefType.KEY_CHAT_REMIND_LIST, value);
            }
        }

        if (observeGroupMember != null) {
            getContentResolver().unregisterContentObserver(observeGroupMember);
            observeGroupMember = null;
        }

        if (groupObserve != null) {
            getContentResolver().unregisterContentObserver(groupObserve);
            groupObserve = null;
        }

        if (chatAdapter != null) {
            chatAdapter.onDestroy();
            chatAdapter = null;
        }

        if (observeFriendRelation != null) {
            getContentResolver().unregisterContentObserver(observeFriendRelation);
            observeFriendRelation = null;
        }

        if (searchUsers != null) {
            searchUsers.cancel();
        }
        if (netReceiver != null) {
            mContext.unregisterReceiver(netReceiver);
        }
        CustomLog.d(TAG, "onDestory end");
    }


    private void saveDraftTxt() {
        if (!isSaveDraft) {
            return;
        }
        // 保存草稿
        String draftTxt = inputFragment.obtainInputTxt();
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            ArrayList<String> resList = new ArrayList<String>();
            resList = IMCommonUtil.getList(draftTxt);
            for (int i = 0; i < resList.size(); i++) {

                for (int j = 0; j < selectNameList.size(); j++) {
                    if (resList.get(i).equals(selectNameList.get(j))) {
                        draftTxt = draftTxt.replace(resList.get(i),
                            selectNubeList.get(j));
                        break;
                    }
                }
            }
        }

        if (TextUtils.isEmpty(convstId)) {
            if (TextUtils.isEmpty(draftTxt)) {
                // 还没有会话的场合，没有草稿信息，无需保存
                return;
            } else {
                threadDao.saveDraft(getReceivers(), draftTxt);
            }
        } else {
            // 有会话的场合，退出时，需更新草稿信息
            threadDao.saveDraftById(convstId, draftTxt);
        }
    }


    private void queryNoticeData(int queryType) {
        // 分页查询，必须一次查询完成后才能开始下次查询，所以队列中只能保存一个类型
        // 而范围查询，只要起始时间改变了，就需要重新查询
        String queryKey = queryType + "_" + getRecvTimeBegin();
        if (queryList.contains(queryKey)) {
            // 查询队列里有相同的查询请求，则放弃查询
            return;
        }

        // 队列为空，加入队列并启动线程；队列不为空，则加入队列，等待线程执行完成后启动下一个线程
        synchronized (queryList) {
            if (queryList.isEmpty()) {
                queryList.add(queryKey);
                queryRunnable = new QueryRunnable();
                queryRunnable.queryType = queryType;
                queryRunnable.recvTimeBg = getRecvTimeBegin();
                mHandler.postDelayed(queryRunnable, 100);
            } else {
                queryList.add(queryKey);
            }
        }
    }


    private long getRecvTimeBegin() {
        synchronized (recvTimeLock) {
            return recvTimeBegin;
        }
    }


    private void setRecvTimeBegin(long rTb) {
        synchronized (recvTimeLock) {
            recvTimeBegin = rTb;
            CustomLog.d(TAG, "IM 消息起始查询时间 = " + recvTimeBegin);
        }
    }


    // 线程安全的并发查询队列
    private List<String> queryList = new CopyOnWriteArrayList<String>();
    // 查询数据Runnable
    private QueryRunnable queryRunnable = null;


    /**
     * 当有新的数据时 android framework 层调用
     */

    @Override
    public void onSensorChanged(final SensorEvent event) {

        float value = event.values[0];

        if (playerManager.isPlaying()) {
            if (value == proximitySensor.getMaximumRange()) {
                //当前是扬声器模式，启用扬声器
                if (getVoicePref() == SPEAKER) {
                    playerManager.changeToSpeaker();
                    helper.setScreenOn();
                    //Toast : 已从听筒切换回扬声器播放，实质是自定义 View
                    IMCommonUtil.makeModeChangeToast(mContext, getString(R.string.revert_speaker),
                        R.drawable.speaker_on);
                }
            } else {
                playerManager.changeToReceiver();
                helper.setScreenOff();
            }
        } else {
            if (value == proximitySensor.getMaximumRange()) {
                playerManager.changeToSpeaker();
                helper.setScreenOn();
            }
        }
        //
        //        //如果当前有语音播放
        //        if (isPlaying) {
        //            //改变语音消息播放模式
        //            changeVoicePlayMode(event);
        //        }
    }

    //    private void changeVoicePlayMode(SensorEvent event) {
    //        float distance = event.values[0];
    //
    //        // 用户远离听筒，音频外放，亮屏 -> 扬声器模式
    //        if (distance >= proximitySensor.getMaximumRange()) {
    //
    //            helper.setScreenOn();
    //
    //            //当前是扬声器模式，启用扬声器
    //            if (getVoicePref() == SPEAKER) {
    //                audioHelper.enableSpeaker();
    //                //Toast : 已从听筒切换回扬声器播放，实质是自定义 View
    //                IMCommonUtil.makeModeChangeToast(mContext, getString(R.string.revert_speaker),
    //                        R.drawable.speaker_on);
    //            }
    //
    //        } else {    // 用户贴近听筒，切换音频到听筒输出，并且熄屏防误触 ->听筒模式
    //
    //            helper.setScreenOff();
    //            audioHelper.initMediaPlayer();
    //
    //            chatAdapter.onStop();
    //            audioHelper.enableHeadSet();
    //
    //            String audioPath = getMediaAudioPath();
    //            audioHelper.replayAudio(audioPath);
    //        }
    //    }


    /**
     * 设置 AudioManageHelper 中的 AudioPath
     */
    //    private String getMediaAudioPath() {
    //        SharedPreferences voiceSettings = getSharedPreferences(VOICE_PREFS_NAME, MODE_PRIVATE);
    //        String audioPath = voiceSettings.getString("VOICE_PATH",null);
    //        return audioPath;
    //    }
    public boolean getVoicePref() {
        SharedPreferences voiceSettings = getSharedPreferences(VOICE_PREFS_NAME, MODE_PRIVATE);
        Boolean playMode = voiceSettings.getBoolean("VOICE_PLAY_MODE",
            SPEAKER);  //第二个参数是 SharedPreference 不存在时返回的默认值

        return playMode;
    }


    /**
     * 当距离传感器精度发生变化时 android framework 层调用
     */
    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void meetingLinkClick(final String meetingId) {
        GetMeetingInfomation meetingInfomation = new GetMeetingInfomation() {
            @Override
            protected void onSuccess(MeetingInfomation responseContent) {
                removeLoadingView();
                if (responseContent.meetingType == 2) {

                    final BookMeetingExInfo exInfo = new BookMeetingExInfo();
                    exInfo.setBookNube(responseContent.terminalAccount);
                    exInfo.setBookName(responseContent.terminalAccountName);
                    exInfo.setMeetingRoom(responseContent.meetingId + "");
                    exInfo.setMeetingTheme(responseContent.topic);
                    exInfo.setMeetingTime(Long.parseLong(responseContent.yyBeginTime) * 1000);
                    exInfo.setMeetingUrl(MedicalMeetingManage.JMEETING_INVITE_URL);
                    exInfo.setHasMeetingPassWord(responseContent.hasMeetingPwd);
                    Intent i = new Intent(mContext, ReserveSuccessActivity.class);
                    i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, exInfo);
                    mContext.startActivity(i);
                } else {
                    int isSuccess = MedicalMeetingManage.getInstance().joinMeeting(meetingId,
                        new MedicalMeetingManage.OnJoinMeetingListener() {
                            @Override
                            public void onJoinMeeting(String valueDes, int valueCode) {
                                if (valueCode < 0) {
                                    CustomToast.show(mContext,
                                        getString(R.string.join_consultation_fail), 1);
                                }
                            }
                        });

                    if (isSuccess == 0) {
                        // CustomToast.show(mContext, "加入会诊成功", 1);
                    } else if (isSuccess == -9992) {
                        CustomToast.show(mContext, getString(R.string.login_checkNetworkError), 1);
                    } else {
                        CustomToast.show(mContext, getString(R.string.join_consultation_fail), 1);
                    }
                }
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                removeLoadingView();
                CustomToast.show(mContext, getString(R.string.get_meeting_info_fail),
                    CustomToast.LENGTH_SHORT);
            }
        };
        int result = meetingInfomation.getMeetingInfomation(Integer.valueOf(meetingId));
        if (result == 0) {
            showLoadingView(getString(R.string.querying_consultation_info),
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //    TODO 可以下此处进行撤销加入会议
                        removeLoadingView();
                    }
                });
        } else {
            CustomToast.show(mContext, getString(R.string.get_meeting_info_fail),
                CustomToast.LENGTH_SHORT);
        }
    }


    class QueryRunnable implements Runnable {

        public int queryType = 0;
        public long recvTimeBg = 0l;


        public void run() {
            CustomLog.d(TAG, "查询会话消息:" + convstId);
            if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                convstId = groupId;
            }
            // 查询动态数据
            QueryConvstNoticeAsyncTask task = new QueryConvstNoticeAsyncTask(
                ChatActivity.this, convstId, queryType, getRecvTimeBegin(),
                IMConstant.NOTICE_PAGE_CNT);

            task.setQueryTaskListener(new QueryConvstNoticeAsyncTask.QueryTaskPostListener() {
                @Override
                public void onQuerySuccess(Cursor cursor) {
                    CustomLog.d(TAG, "QueryConvstNoticeAsyncTask onQuerySuccess");
                    updateNoticesInfo();
                    inputFragment.setVoiceInfo(voiceTipView, cursor);
                    if (chatAdapter != null) {

                        if (queryType == QueryConvstNoticeAsyncTask.QUERY_TYPE_PAGE) {
                            // 分页查询的场合
                            if (cursor == null) {
                                // 没有查询到数据
                                headerRoot.setPadding(0,
                                    0, 0, 0);
                                headerRoot.setVisibility(View.INVISIBLE);
                            } else {
                                int pageCursorCnt = cursor.getCount();
                                if (pageCursorCnt < IMConstant.NOTICE_PAGE_CNT) {
                                    // 数据量小于一页数据的场合，没有上一页数据了
                                    headerRoot.setPadding(0,
                                        0, 0, 0);
                                    headerRoot.setVisibility(View.INVISIBLE);
                                } else {
                                    headerRoot.setPadding(0, 0, 0, 0);
                                    headerRoot.setVisibility(View.VISIBLE);
                                }

                                if (cursor.moveToFirst()) {
                                    setRecvTimeBegin(cursor.getLong(cursor
                                        .getColumnIndex(NoticesTable.NOTICE_COLUMN_SENDTIME)));
                                }

                                int oldCnt = chatAdapter.getCount();
                                chatAdapter.mergeLastPageCursor(cursor);
                                int newCnt = chatAdapter.getCount();

                                CustomLog.d("QueryConvstNoticeAsyncTask | oldCnt = ", oldCnt + ""
                                    + " | newCnt =  " + newCnt);

                                if (pageCursorCnt > 0) {
                                    // 定位到最下面一条
                                    noticeListView.setSelection(pageCursorCnt);
                                }
                            }

                        } else if (queryType == QueryConvstNoticeAsyncTask.QUERY_TYPE_COND) {
                            // 范围查询的场合

                            if (cursor != null && cursor.moveToFirst()) {
                                setRecvTimeBegin(cursor.getLong(cursor
                                    .getColumnIndex(NoticesTable.NOTICE_COLUMN_SENDTIME)));
                            }

                            int oldCnt = chatAdapter.getCount();
                            chatAdapter.changeCursor(cursor);
                            int newCnt = chatAdapter.getCount();
                            if (newCnt > oldCnt) {
                                CustomLog.d(TAG, "消息查询结果：oldCnt=" + oldCnt
                                    + " | newCnt=" + newCnt);
                                // 有新消息的场合，定位到最下面一条
                                noticeListView.setSelection(newCnt - 1);
                            }
                        }
                    }
                    // 初始化im面板
                    if (firstFlag) {
                        firstFlag = false;
                        if ((cursor == null || cursor.getCount() == 0)) {
                            inputFragment.showSelectlayout();
                            SoftInput = true;
                        } else {
                            SoftInput = false;
                        }
                    } else {
                        if (cursor.getCount() > 0) {
                            SoftInput = false;
                        }
                    }
                    afterQuery(queryType, recvTimeBg);
                    noticeListView.removeFooterView(voiceTipView);
                }


                @Override
                public void onQueryFailure() {
                    CustomLog.d(TAG, "QueryConvstNoticeAsyncTask onQueryFailure");
                    updateNoticesInfo();
                    Toast.makeText(ChatActivity.this, R.string.load_msg_fail,
                        Toast.LENGTH_SHORT).show();
                    afterQuery(queryType, recvTimeBg);
                }
            });
            task.executeOnExecutor(ThreadPoolManger.THREAD_POOL_EXECUTOR, "");
        }
    }


    ;


    private void afterQuery(int queryType, long recvTb) {
        String queryKey = queryType + "_" + recvTb;
        queryList.remove(queryKey);

        synchronized (queryList) {
            if (!queryList.isEmpty()) {
                // 查询队列
                String key = queryList.get(0);
                String[] keys = key.split("_");

                // 继续下一个查询
                queryRunnable = new QueryRunnable();
                queryRunnable.queryType = Integer.parseInt(keys[0]);
                queryRunnable.recvTimeBg = Long.parseLong(keys[1]);
                mHandler.postDelayed(queryRunnable, 100);
            }
        }
    }


    private void initWidget() {
        setVoicePlayMode();
        noticeListView = (ListView) findViewById(R.id.notice_listview);
        chatlayout = (LinearLayout) findViewById(R.id.chat_linearlayout);
        backbtn = (Button) chatlayout.findViewById(R.id.back_btn);
        backtext = (TextView) chatlayout.findViewById(R.id.back_str);
        // receiversLine = (RelativeLayout) findViewById(R.id.receivers_line);
        // receiverInput = (EditText) findViewById(R.id.receiver_input);
        // receiverInputFocus = (TextView) findViewById(R.id.on_receiver_input);

        chatAdapter = new ChatListAdapter(ChatActivity.this, null, noticeDao,
            targetNubeNumber, targetShortName);
        chatAdapter.setSelfNubeNumber(selfNubeNumber);
        chatAdapter.setCallbackInterface((ChatListAdapter.CallbackInterface) this);
        chatAdapter.setMeetingLinkClickListener(this);
        chatAdapter.setTitlebar(getTitleBar());
        chatAdapter.setConversationType(conversationType);

        noticeListView.addHeaderView(headerLoadingView);
        noticeListView.setAdapter(chatAdapter);
        noticeListView.setOverScrollMode(OVER_SCROLL_NEVER);

        newNoticeNum = (TextView) this.findViewById(R.id.total_new_notice_num);

        //更新消息未读数
        //        updateNoticesInfo();

        String nube = "";
        if (conversationType == VALUE_CONVERSATION_TYPE_SINGLE) {
            nube = targetNubeNumber;
        } else {
            nube = groupId;
        }
        CustomLog.d(TAG, nube + "");

        voiceTipView = new VoiceTipView(this);
        inputFragment.mContext = ChatActivity.this;
        inputFragment.setNubeNum(nube);
        inputFragment.setListview(noticeListView);

        inputFragment.callback = new ChatInputFragment.SendCallbackInterface() {

            @Override
            public boolean onSendTxtMsg(final String txtMsg) {
                CustomLog.i(TAG, "onSendTxtMsg()");

                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendTxtMsg 不是好友");
                    return false;
                }
                // 发送文字
                // MobclickAgent.onEvent(ChatActivity.this,
                //         UmengEventConstant.EVENT_SEND_TEXT);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String uuid = "";
                        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                            CustomLog.i(TAG, "VALUE_CONVERSATION_TYPE_MULTI");
                            // 插入发送记录
                            ArrayList<String> resList = new ArrayList<String>();
                            ArrayList<String> lastList = new ArrayList<String>();
                            resList = IMCommonUtil.getList(txtMsg);

                            //确认发送消息的类型
                            int txtMsgType = FileTaskManager.NOTICE_TYPE_TXT_SEND;
                            if (resList.size() > 0) {
                                txtMsgType = FileTaskManager.NOTICE_TYPE_REMIND_ONE_SEND;
                            }
                            if (resList.size() == 0) {
                                txtMsgType = FileTaskManager.NOTICE_TYPE_TXT_SEND;
                            }

                            lastList.addAll(resList);
                            String str = "";
                            str = new String(txtMsg);
                            boolean repalceflg = true;
                            for (int i = 0; i < resList.size(); i++) {

                                for (int j = 0; j < selectNameList
                                    .size(); j++) {
                                    if (resList.get(i).equals(
                                        selectNameList.get(j))) {
                                        str = str.replace(
                                            resList.get(i),
                                            selectNubeList.get(j));
                                        repalceflg = false;
                                        lastList.remove(0);
                                        break;
                                    }
                                }
                            }
                            if (lastList != null
                                && lastList.size() != 0) {
                                for (int i = 0; i < lastList.size(); i++) {
                                    ArrayList<GroupMemberBean> beanList = groupDao
                                        .queryAllGroupMembers(
                                            groupId,
                                            selfNubeNumber);
                                    for (int j = 0; j < beanList.size(); j++) {
                                        NameElement element = ShowNameUtil
                                            .getNameElement(
                                                beanList.get(j)
                                                    .getName(),
                                                beanList.get(j)
                                                    .getNickName(),
                                                beanList.get(j)
                                                    .getPhoneNum(),
                                                beanList.get(j)
                                                    .getNubeNum());
                                        String MName = ShowNameUtil
                                            .getShowName(element);
                                        if (lastList
                                            .get(i)
                                            .equals("@"
                                                + MName
                                                + IMConstant.SPECIAL_CHAR)) {
                                            str = str.replace(
                                                lastList.get(i),
                                                "@"
                                                    + beanList
                                                    .get(j)
                                                    .getNubeNum()
                                                    + IMConstant.SPECIAL_CHAR);
                                            repalceflg = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (resList.size() == 0 && repalceflg) {
                                str = txtMsg;
                            }
                            uuid = noticeDao
                                .createSendFileNotice(
                                    selfNubeNumber,
                                    groupId,
                                    null,
                                    "",
                                    txtMsgType,
                                    str, groupId, null);

                            selectNameList.clear();
                            selectNubeList.clear();
                        } else {
                            CustomLog.i(TAG, "VALUE_CONVERSATION_TYPE_SINGLE");

                            // 插入发送记录
                            uuid = noticeDao
                                .createSendFileNotice(
                                    selfNubeNumber,
                                    getReceivers(),
                                    null,
                                    "",
                                    FileTaskManager.NOTICE_TYPE_TXT_SEND,
                                    txtMsg, convstId, null);
                        }
                        getFileTaskManager()
                            .addTask(uuid, null);
                    }
                }).start();
                return true;
            }


            @Override
            public void onSendPic() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendPic 不是好友");
                    return;
                }
                SendCIVMUtil.sendPic(ChatActivity.this);
            }


            @Override
            public void onSendPicFromCamera() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendPicFromCamera 不是好友");
                    return;
                }
                SendCIVMUtil.sendPicFromCamera(ChatActivity.this);
            }


            @Override
            public void onSendVideo() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendVideo 不是好友");
                    return;
                }
                SendCIVMUtil.sendVideo(ChatActivity.this);
            }


            @Override
            public void onSendVcard() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendVcard 不是好友");
                    return;
                }
                SendCIVMUtil.sendVcard(ChatActivity.this);
            }


            @Override
            public boolean doPreSendCheck() {
                return true;
            }


            @Override
            public void onSendAudio(final String rcdFilePah,
                                    final int rcdLenth) {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendAudio 不是好友");
                    return;
                }

                //                MobclickAgent.onEvent(ChatActivity.this,
                //                        UmengEventConstant.EVENT_SEND_AUDIO);
                // 发送音频
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        ButelPAVExInfo extInfo = new ButelPAVExInfo();
                        extInfo.setDuration(rcdLenth);

                        List<String> localFiles = new ArrayList<String>();
                        localFiles.add(rcdFilePah);

                        String uuid = "";
                        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                            // 插入发送记录
                            uuid = noticeDao
                                .createSendFileNotice(
                                    selfNubeNumber,
                                    groupId,
                                    localFiles,
                                    "",
                                    FileTaskManager.NOTICE_TYPE_AUDIO_SEND,
                                    "", groupId, extInfo);
                        } else {
                            // 插入发送记录
                            uuid = noticeDao
                                .createSendFileNotice(
                                    selfNubeNumber,
                                    getReceivers(),
                                    localFiles,
                                    "",
                                    FileTaskManager.NOTICE_TYPE_AUDIO_SEND,
                                    "", convstId, extInfo);
                        }
                        getFileTaskManager()
                            .addTask(uuid, null);
                    }
                }).start();
            }


            @Override
            public void onSelectGroupMemeber() {
                CustomLog.d(TAG, "onSelectGroupMemeber 选择回复的人");
                // 选择回复的人
                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    Intent intent = new Intent(ChatActivity.this,
                        SelectLinkManActivity.class);
                    intent.putExtra(
                        SelectLinkManActivity.OPT_FLAG,
                        SelectLinkManActivity.OPT_HAND_OVER_START_FOR_RESULT);
                    intent.putExtra(
                        SelectLinkManActivity.AVITVITY_TITLE,
                        getString(R.string.select_revert_person));
                    intent.putExtra(
                        SelectLinkManActivity.ACTIVITY_FLAG,
                        SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                    intent.putExtra(
                        SelectLinkManActivity.KEY_IS_SIGNAL_SELECT,
                        false);
                    intent.putExtra(
                        SelectLinkManActivity.KEY_SINGLE_CLICK_BACK,
                        true);
                    intent.putExtra(
                        SelectLinkManActivity.HAND_OVER_MASTER_LIST,
                        GroupMemberToContactsBean());
                    intent.putExtra(
                        SelectGroupMemeberActivity.SELECT_GROUPID,
                        groupId);
                    startActivityForResult(intent,
                        ACTION_FORWARD_NOTICE);
                }
            }


            @Override
            public void onAudioCall() {
            }


            @Override
            public void onVedioCall() {
            }


            @Override
            public void onMeetingCall() {
                showMeetingAlertDialog();
            }


            @Override
            public void onAudioRecStart() {
                // 重新播放
                if (chatAdapter != null) {
                    chatAdapter.stopCurAuPlaying();
                }
            }


            @Override
            public void onShareCollection() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onShareCollection 不是好友");
                    return;
                }
                String receiver = "";
                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    receiver = groupId;
                } else {
                    receiver = getReceivers();
                }
                //                CollectionManager.getInstance().goToSharedCollectionActivity(ChatActivity.this,receiver);
                if (receiver.length() == 0) {
                    CustomLog.d(TAG, "收藏的received 为空字符串");
                    return;
                }
                Intent intent = new Intent(mContext, CollectionActivity.class);
                intent.putExtra(CollectionActivity.KEY_RECEIVER, receiver);

                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    // 群成员人数
                    groupMemberSize = groupDao.queryGroupMemberCnt(targetNubeNumber);
                    intent.putExtra("headUrl", groupDao.getGroupHeadUrl(targetNubeNumber));
                    intent.putExtra("chatNames", groupDao.getGroupNameByGid(targetNubeNumber));
                    intent.putExtra("chatNumber", groupMemberSize);
                    intent.putExtra("chatType", "group");

                } else {
                    Contact contact = ContactManager.getInstance(ChatActivity.this)
                        .getContactInfoByNubeNumber(targetNubeNumber);
                    intent.putExtra("headUrl", contact.getHeadUrl());
                    intent.putExtra("chatNames", targetShortName);
                    intent.putExtra("chatType", "single");
                }
                mContext.startActivity(intent);
            }
        };

        noticeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    CustomLog.d(TAG, "列表正在滚动...");
                    // list列表滚动过程中，暂停图片上传下载
                } else {
                }
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 滚动停止的场合，加载更多数据
                    int firstVP = noticeListView.getFirstVisiblePosition();
                    CustomLog.d(TAG, "列表停止滚动...FirstVisiblePosition:" + firstVP);
                    if (firstVP == 0
                        && headerRoot.getVisibility() == View.VISIBLE) {
                        queryNoticeData(QueryConvstNoticeAsyncTask.QUERY_TYPE_PAGE);
                    }
                }
            }


            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

        noticeListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 隐藏输入法及素材面板
                CommonUtil.hideSoftInputFromWindow(ChatActivity.this);
                inputFragment.setHide();
                return false;
            }
        });
        getTitleBar().setBack(null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waitDialog != null && waitDialog.isShowing()) {
                    //                    hideWaitDialog();
                    removeLoadingView();
                    // ButelMeetingManager.getInstance().cancelCreateMeeting(
                    //     ButelContactDetailActivity.class.getName());
                }
                exitActivity();
            }
        });

        //确认传感器是否存在
        identifySensor();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        CustomLog.i(TAG, "onRequestPermissionsResult()");

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showPermissionDialog(Manifest.permission.CAMERA);
                }
            }
            break;
            case PERMISSIONS_REQUEST_AUDIO_RECORD_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showPermissionDialog(Manifest.permission.RECORD_AUDIO);
                }
            }
            break;

        }
    }


    /**
     * 展示设置权限Dialog
     */
    private void showPermissionDialog(String permission) {
        CustomLog.i(TAG, "showPermissionDialog()");

        String dialogContent = "";
        if (permission.equalsIgnoreCase(Manifest.permission.CAMERA)) {
            dialogContent = getString(R.string.no_camera_permission_dialog_content);
        } else if (permission.equalsIgnoreCase(Manifest.permission.RECORD_AUDIO)) {
            dialogContent = getString(R.string.no_audio_permission_dialog_content);
        }
        final CustomDialog cd = new CustomDialog(ChatActivity.this);
        cd.setTip(dialogContent);
        cd.setCancelBtnText(getString(R.string.btn_cancle));
        cd.setOkBtnText(getString(R.string.go_to_setting));
        cd.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                cd.dismiss();
            }
        });
        cd.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override public void onClick(CustomDialog customDialog) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                try {
                    cd.dismiss();
                    startActivity(intent);
                } catch (Exception e) {
                    CustomLog.e(TAG, "go to permission apply " + e.toString());
                }
            }
        });
        cd.show();
    }


    private void setVoicePlayMode() {
        if (getVoicePref() == HEADSET) {
            getTitleBar().showHeadsetView(true);
        } else {
            getTitleBar().showHeadsetView(false);
        }

    }


    private void identifySensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(TYPE_PROXIMITY);

        if (proximitySensor == null) {
            CustomToast.show(this, getString(R.string.no_range_sensor_not_user_function), 1);
            CustomLog.d(TAG, "该手机没有距离传感器，无法使用播放语音消息听筒模式部分功能");
        } else {

        }

    }


    protected void showMeetingAlertDialog() {
        CustomLog.d(TAG, "显示 立即召开/预约会议室 对话框");
        final MedicalAlertDialog menuDlg = new MedicalAlertDialog(this);
        menuDlg.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "立即召开时 不是好友");
                    return;
                }
                if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                    CustomToast.show(mContext, getString(R.string.is_video_meeting) +
                            MedicalMeetingManage.getInstance().getActiveMeetingId(),
                        CustomToast.LENGTH_SHORT);
                    return;
                }
                conveneMeeting();
            }
        }, getString(R.string.immediately_convence));

        menuDlg.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "预约会诊室时 不是好友");
                    return;
                }
                bookMeeting();
            }
        }, getString(R.string.subscribe_consultation));
        menuDlg.addButtonThird(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                menuDlg.dismiss();
            }
        }, getString(R.string.btn_cancle));
        menuDlg.show();
    }


    protected void conveneMeeting() {
        CustomLog.d(TAG, "conveneMeeting begin,点击 立即召开");
        if (!isFriend()) {
            CustomLog.d(TAG, "conveneMeeting 不是好友");
            return;
        }
        if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
            CustomToast.show(ChatActivity.this, getString(R.string.is_video_meeting) +
                MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
            return;
        }
        String targetId = targetNubeNumber;
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            targetId = groupId;
        }
        if (SendCIVMUtil.conveneMeeting(ChatActivity.this, TAG,
            conversationType, targetId, selfNubeNumber)) {
            CustomLog.d(TAG, "准备创建会议");

        }
        CustomLog.d(TAG, "conveneMeeting end");
    }


    private void bookMeeting() {
        CustomLog.d(TAG, "bookMeeting begin,点击 预约会议室");
        if (!isFriend()) {
            CustomLog.d(TAG, "bookMeeting 不是好友");
            return;
        }
        String targetId = targetNubeNumber;
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            targetId = groupId;
        }
        SendCIVMUtil.bookMeeting(ChatActivity.this, targetId,
            conversationType == VALUE_CONVERSATION_TYPE_MULTI);
        CustomLog.d(TAG, "bookMeeting end");
    }


    private void exitActivity() {

        //如果有正在播放的音频，停止播放
        if (chatAdapter != null) {
            chatAdapter.onStop();
        }

        // 2015-01-29如果这个会话界面处于栈中的根节点，即为栈中唯一节点时，此时点击返回键，跳转到消息列表界面；否则直接finish该界面，返回跳转前界面
        // 2015-12-08 消息页面点击 返回，都回到消息列表页面
        // if (this.isTaskRoot()) {
        if (back_flag) {
            HomeActivity.isFromChatActivity = true;
            Intent tempintent = new Intent(this, HomeActivity.class);
            tempintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            tempintent.putExtra(HomeActivity.TAB_INDICATOR_INDEX,
                HomeActivity.TAB_INDEX_MESSAGE);
            tempintent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(tempintent);

        }
        finish();
    }

    //	private void onRecvInptFocusOut() {
    //		receiverInput.setVisibility(View.GONE);
    //		receiverInputFocus.setVisibility(View.VISIBLE);
    //		if (receiverNumberLst.size() == 0) {
    //			receiverInputFocus.setText("");
    //		} else if (receiverNumberLst.size() == 1) {
    //			receiverInputFocus.setText(receiverNameMap.get(receiverNumberLst
    //					.get(0)));
    //		} else {
    //			receiverInputFocus.setText(getString(R.string.receiver_tip,
    //					getReceiverDispName(receiverNameMap.get(receiverNumberLst
    //							.get(0))), receiverNumberLst.size() - 1));
    //		}
    //	}


    private void initView() {
        addMeetinglayout = (LinearLayout) findViewById(R.id.add_meeting_line);
        addMeetinglayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtil.hideSoftInputFromWindow(ChatActivity.this);
                checkPassword();
            }
        });
        addMeetingThemeTxt = (TextView) findViewById(R.id.add_meeting_theme);
        switch (frameType) {
            case VALUE_NOTICE_FRAME_TYPE_NUBE: {
                // 单聊
                conversationType = VALUE_CONVERSATION_TYPE_SINGLE;

                // 根据nube号查询会话信息
                // 已产生会话，则并入已有会话
                // 未产生会话，则继续监听数据库后，直到产生会话
                if (mergeThreads() == 0) {
                    convstId = "";
                    convstExtInfo = "";
                    // 清空列表数据
                    if (chatAdapter != null) {
                        chatAdapter.changeCursor(null);
                    }
                }
                //第一次进入会话页面
                inputFragment.setVoiceInfo(voiceTipView, null);

            }
            case VALUE_NOTICE_FRAME_TYPE_LIST: {
                // 聊天列表的场合，隐藏收件人区域

                if (SYS_NUBE.equals(targetNubeNumber)) {
                    // 官方帐号，隐藏输入框，禁止回复
                    inputFragment.isShowing = false;
                    getSupportFragmentManager().beginTransaction()
                        .remove(inputFragment).commit();
                    targetShortName = getString(R.string.str_butel_name);
                    convstExtInfo = "";
                    getTitleBar().setSubTitle(null);
                    getTitleBar().setRightBtnVisibility(View.INVISIBLE);

                } else if (SettingData.getInstance().adminNubeNum.equals(targetNubeNumber)) {
                    inputFragment.isShowing = false;
                    getSupportFragmentManager().beginTransaction()
                        .remove(inputFragment).commit();
                    targetShortName = getAdminNickName();
                    convstExtInfo = "";
                    getTitleBar().setSubTitle(null);
                    getTitleBar().setRightBtnVisibility(View.INVISIBLE);
                } else if (conversationType == VALUE_CONVERSATION_TYPE_SINGLE) {
                    // 单人聊天
                    inputFragment.isShowing = true;
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.input_line, inputFragment).commit();
                    getTitleBar().setSubTitle(null);

                    //如果是系统 Nube 不显示个人详情页按钮
                    if (!targetNubeNumber.equals(SYS_NUBE)
                        || !targetNubeNumber.equals(SettingData.getInstance().adminNubeNum)) {

                        getTitleBar().enableRightBtn(null,
                            R.drawable.single_convst_btn_selector,
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 跳转到个人名片界面
                                    if (CommonUtil.isFastDoubleClick()) {
                                        return;
                                    }

                                    Intent intent = new Intent(ChatActivity.this,
                                        GroupChatDetailActivity.class);
                                    intent.putExtra(
                                        GroupChatDetailActivity.KEY_CHAT_TYPE,
                                        GroupChatDetailActivity.VALUE_SINGLE);
                                    intent.putExtra(
                                        KEY_NUMBER,
                                        targetNubeNumber);
                                    startActivity(intent);

                                    CustomLog.d(TAG, "点击单人聊天图标，跳转到个人 聊天信息页");
                                }
                            });
                    }

                    //单人聊天时，监听好友关系表
                    if (observeFriendRelation == null) {
                        observeFriendRelation = new FriendRelationObserver();
                        getContentResolver().registerContentObserver(
                            ProviderConstant.Friend_Relation_URI, true,
                            observeFriendRelation);
                    }

                } else if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    if (!groupDao.existGroup(groupId)) {
                        CustomLog.d(TAG, "initView 群组未在数据库中，需在查询后，重新生成");
                        CustomToast.show(mContext, getString(R.string.group_info_not_init_wait_try),
                            1);
                    }
                    // 群发
                    inputFragment.isShowing = true;
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.input_line, inputFragment).commit();
                    // 群发名称
                    chatAdapter.setNoticeType(conversationType, groupId);
                    targetShortName = getGroupNameTitle();
                    if (observeGroupMember == null) {
                        observeGroupMember = new GroupMemberObserver();
                        getContentResolver().registerContentObserver(
                            ProviderConstant.NETPHONE_GROUP_URI, true,
                            observeGroupMember);
                    }

                    if (groupObserve == null) {
                        groupObserve = new GroupObserver();
                        getContentResolver().registerContentObserver(
                            ProviderConstant.NETPHONE_GROUP_MEMBER_URI, true,
                            groupObserve);
                    }
                    getTitleBar().setSubTitle("(" + groupMemberSize + ")");
                    //				findViewById(R.id.input_layout).setVisibility(View.VISIBLE);
                    // String[] receivers = targetNubeNumber.split(";");
                    if (!isGroupMember) {
                        getTitleBar().setRightBtnVisibility(INVISIBLE);
                    } else {
                        getTitleBar().enableRightBtn(null,
                            R.drawable.multi_send_btn_selector,
                            new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 跳转到群发收件人界面
                                    if (CommonUtil.isFastDoubleClick()) {
                                        return;
                                    }
                                    if (groupDao.isGroupMember(groupId,
                                        selfNubeNumber)) {
                                        // 是群成员，跳转

                                        // 群发，跳到群发联系人页面
                                        Intent intent = new Intent(
                                            ChatActivity.this,
                                            GroupChatDetailActivity.class);
                                        intent.putExtra(
                                            GroupChatDetailActivity.KEY_CHAT_TYPE,
                                            GroupChatDetailActivity.VALUE_GROUP);
                                        intent.putExtra(
                                            KEY_NUMBER,
                                            groupId);
                                        intent.putExtra(GroupChatDetailActivity.KEY_GETGROUPCSLFAIL,
                                            getGroupClsFail);
                                        intent.putExtra(GroupChatDetailActivity.KEY_MEETINGNUB,
                                            meetingNub);
                                        intent.putExtra(GroupChatDetailActivity.KEY_MEETINGTHEME,
                                            meetingTheme);
                                        intent.putExtra(GroupChatDetailActivity.KEY_MEETINGPASSWORD,
                                            meetingPaw);
                                        ChatActivity.this.startActivity(intent);

                                        CustomLog.d(TAG, "点击群发图标，跳转到群发收件人界面");
                                    }
                                }
                            });
                    }
                }
                if (ContactManager.getInstance(this).checkNubeIsCustomService(targetNubeNumber)) {
                    getTitleBar().setTitle(R.string.video_custom_service);
                } else {
                    getTitleBar().setTitle(targetShortName);
                }

                // 草稿文字填充
                initDraftText();

                if (observer == null) {
                    observer = new NoticesTableObserver();
                    getContentResolver().registerContentObserver(
                        ProviderConstant.NETPHONE_NOTICE_URI, true, observer);
                }

                if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST) {
                    // 初始化
                    SoftInput = false;
                    firstFlag = true;
                    setRecvTimeBegin(0);
                    if (chatAdapter != null) {
                        chatAdapter.clearData();
                    }
                    queryNoticeData(QueryConvstNoticeAsyncTask.QUERY_TYPE_PAGE);
                } else {
                    // 隐藏分页加载等待view
                    headerRoot.setPadding(0, -headerRoot.getHeight(), 0, 0);
                    headerRoot.setVisibility(View.INVISIBLE);
                    SoftInput = true;
                    firstFlag = false;
                }

                // 去除状态栏通知
                cancelNotifacation();
            }
            break;
        }
    }


    private void setTitleInfo() {
        if (conversationType == VALUE_CONVERSATION_TYPE_SINGLE) {
            setNotDisturbViewMainTitle();
        } else if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            //更新群名称和群成员个数
            String gruopName = getGroupNameTitle();
            if (!gruopName.equals(targetShortName)) {
                targetShortName = gruopName;
                getTitleBar().setTitle(targetShortName);
            }
            int groupSize = groupDao.queryGroupMemberCnt(groupId);
            if (groupSize != groupMemberSize) {
                getTitleBar().setSubTitle("(" + groupSize + ")");
                groupMemberSize = groupSize;
            }
            setNotDisturbViewSubTitle();
        }
    }


    private void setNotDisturbViewMainTitle() {

        if (MedicalApplication.getPreference()
            .getKeyValue(PrefType.KEY_CHAT_DONT_DISTURB_LIST, "")
            .indexOf(";" + targetNubeNumber + ";") >= 0) {
            getTitleBar().showSlientViewSingle(true);

        } else {
            getTitleBar().showSlientViewSingle(false);

        }
    }


    private void setNotDisturbViewSubTitle() {
        if (MedicalApplication.getPreference()
            .getKeyValue(PrefType.KEY_CHAT_DONT_DISTURB_LIST, "")
            .indexOf(";" + targetNubeNumber + ";") >= 0) {
            getTitleBar().showSlientViewGroup(true);
        } else {
            getTitleBar().showSlientViewGroup(false);
        }
    }


    private void cancelNotifacation() {
        if (conversationType == VALUE_CONVERSATION_TYPE_SINGLE) {
            NotificationUtil.cancelNewMsgNotifacation(targetNubeNumber);
            // 清除未接来电通知
            NotificationUtil.cancelNotifacationById(targetNubeNumber);
        } else if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            int notifyId = NotificationUtil.getGroupNotifyID(groupId);
            NotificationUtil.cancelNewMsgNotifacation(notifyId + "");
        }
    }


    private void initDraftText() {
        // 草稿文字填充
        String draftTxt = "";
        try {
            if (!TextUtils.isEmpty(convstExtInfo)) {
                JSONObject extObj = new JSONObject(convstExtInfo);
                draftTxt = extObj.optString("draftText");
            }
        } catch (Exception e) {
            CustomLog.d(TAG, "草稿信息解析失败" + e.toString());
        }

        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            ArrayList<String> dispNubeList = new ArrayList<String>();
            dispNubeList = CommonUtil.getDispList(draftTxt);
            for (int i = 0; i < dispNubeList.size(); i++) {
                GroupMemberBean gbean = groupDao.queryGroupMember(groupId,
                    dispNubeList.get(i));
                NameElement element = ShowNameUtil.getNameElement(
                    gbean.getName(), gbean.getNickName(),
                    gbean.getPhoneNum(), gbean.getNubeNum());
                String MName = ShowNameUtil.getShowName(element);
                selectNameList.add("@" + MName + IMConstant.SPECIAL_CHAR);
                selectNubeList.add("@" + dispNubeList.get(i)
                    + IMConstant.SPECIAL_CHAR);
                draftTxt = draftTxt.replace("@" + dispNubeList.get(i)
                    + IMConstant.SPECIAL_CHAR, "@" + MName
                    + IMConstant.SPECIAL_CHAR);
            }
        }

        if (!inputFragment.obtainInputTxt().equals(draftTxt)) {
            inputFragment.setDraftTxt(draftTxt);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 点击back键时，如果底部面板弹出，则隐藏，否则退出程序
        CustomLog.i(TAG, "onKeyDown()");

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (chatAdapter != null && chatAdapter.isMultiCheckMode()) {
                hideMoreOpLayout();
                cleanCheckData();
                return true;
            }

            if (waitDialog != null && waitDialog.isShowing()) {
            }

            if (inputFragment.isPanelVisible()) {
                inputFragment.setHide();
                return true;
            } else {
                exitActivity();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 监听 t_notices 表数据变化
     */
    private class NoticesTableObserver extends ContentObserver {
        public NoticesTableObserver() {
            super(new Handler());
        }


        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            CustomLog.d(TAG, "消息数据库数据发生变更1:" + selfChange + "|" + uri.toString());
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG, "消息数据库数据发生变更2:" + selfChange);

            int mergeInt = mergeThreads();
            // 刷新界面显示
            if (mergeInt == 1) {
                queryNoticeData(QueryConvstNoticeAsyncTask.QUERY_TYPE_COND);
            } else if (mergeInt == 2) {
                queryNoticeData(QueryConvstNoticeAsyncTask.QUERY_TYPE_PAGE);
            }
        }
    }


    private synchronized int mergeThreads() {
        if (frameType == VALUE_NOTICE_FRAME_TYPE_NUBE) {
            ThreadsBean th = threadDao.getThreadByRecipentIds(targetNubeNumber);
            if (th != null) {
                CustomLog.d("TAG", "已产生会话，则并入已有会话");
                // 已产生会话，则并入已有会话
                convstId = th.getId();
                convstExtInfo = th.getExtendInfo();
                frameType = VALUE_NOTICE_FRAME_TYPE_LIST;
                return 2;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CustomLog.i(TAG, "onActivityResult()");

        if (Activity.RESULT_CANCELED == resultCode) {
            return;
        }

        switch (requestCode) {
            case SendCIVMUtil.ACTION_SHARE_PIC_FROM_CAMERA:
            case SendCIVMUtil.ACTION_SHARE_VIDEO_FROM_CAMERA:
                CustomLog.d(TAG, "拍摄视频 返回");
                if (!isFriend()) {
                    CustomLog.d(TAG, "ACTION_SHARE_VIDEO_FROM_CAMERA 不是好友");
                    return;
                }
                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    if (data.getExtras().getInt("OK_TYPE", 0) == 1) {
                        SendCIVMUtil.onSendVideoFromCameraBack(this, data,
                            selfNubeNumber, groupId, groupId);
                    } else if (data.getExtras().getInt("OK_TYPE", 0) == 0) {
                        SendCIVMUtil.onSendPicFromCameraBack(this, selfNubeNumber,
                            groupId, groupId);
                    } else {
                    }
                } else {
                    if (data.getExtras().getInt("OK_TYPE", 0) == 1) {
                        SendCIVMUtil.onSendVideoFromCameraBack(this, data,
                            selfNubeNumber, getReceivers(), convstId);
                    } else if (data.getExtras().getInt("OK_TYPE", 0) == 0) {
                        SendCIVMUtil.onSendPicFromCameraBack(this, selfNubeNumber,
                            getReceivers(), convstId);
                    } else {
                    }

                }
                break;
            case SendCIVMUtil.ACTION_SHARE_PIC_FROM_NATIVE:
                CustomLog.d(TAG, "选择图片 返回");
                int anInt = data.getExtras().getInt(MultiImageChooserActivity.KEY_CHOOSER_TYPE);
                if (anInt == (MultiImageChooserActivity.CHOOSER_TYPE_VIDEO)) {
                    if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                        SendCIVMUtil.onSendVideoFromNativeBack(this, data,
                            selfNubeNumber, groupId, groupId);
                    } else {
                        SendCIVMUtil.onSendVideoFromNativeBack(this, data,
                            selfNubeNumber, getReceivers(), convstId);
                    }
                } else if (anInt == (MultiImageChooserActivity.CHOOSER_TYPE_VIDEO_IMAGE)) {
                    ArrayList<String> selectedVideoList = data.getExtras()
                        .getStringArrayList(Intent.EXTRA_STREAM);
                    ArrayList<String> mList = new ArrayList<>();
                    ArrayList<String> tList = new ArrayList<>();
                    for (int i = 0; i < selectedVideoList.size(); i++) {
                        if (selectedVideoList.get(i).contains(".rm") ||
                            selectedVideoList.get(i).contains(".flv") ||
                            selectedVideoList.get(i).contains(".mp4") ||
                            selectedVideoList.get(i).contains(".mkv") ||
                            selectedVideoList.get(i).contains(".avi") ||
                            selectedVideoList.get(i).contains(".3gp") ||
                            selectedVideoList.get(i).contains(".ts") ||
                            selectedVideoList.get(i).contains(".wmv") ||
                            selectedVideoList.get(i).contains(".mov") ||
                            selectedVideoList.get(i).contains(".asf")) {
                            mList.add(selectedVideoList.get(i));
                        } else {
                            tList.add(selectedVideoList.get(i));
                        }
                    }
                    Intent intent1 = new Intent();
                    intent1.putStringArrayListExtra(Intent.EXTRA_STREAM, tList);
                    Intent intent2 = new Intent();
                    intent2.putStringArrayListExtra(Intent.EXTRA_STREAM, mList);
                    if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                        SendCIVMUtil.onSendPicFromNativeBack(this, intent1,
                            selfNubeNumber, groupId, groupId);
                        SendCIVMUtil.onSendVideoFromNativeBack(this, intent2,
                            selfNubeNumber, groupId, groupId);
                    } else {
                        SendCIVMUtil.onSendPicFromNativeBack(this, intent1,
                            selfNubeNumber, getReceivers(), convstId);
                        SendCIVMUtil.onSendVideoFromNativeBack(this, intent2,
                            selfNubeNumber, getReceivers(), convstId);
                    }
                } else {
                    if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                        SendCIVMUtil.onSendPicFromNativeBack(this, data,
                            selfNubeNumber, groupId, groupId);
                    } else {
                        SendCIVMUtil.onSendPicFromNativeBack(this, data,
                            selfNubeNumber, getReceivers(), convstId);
                    }
                }
                break;
            case SendCIVMUtil.ACTION_SHARE_VIDEO_FROM_NATIVE:
                CustomLog.d(TAG, "选择视频 返回");
                if (!isFriend()) {
                    CustomLog.d(TAG, "ACTION_SHARE_VIDEO_FROM_NATIVE 不是好友");
                    return;
                }
                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    SendCIVMUtil.onSendVideoFromNativeBack(this, data,
                        selfNubeNumber, groupId, groupId);
                } else {
                    SendCIVMUtil.onSendVideoFromNativeBack(this, data,
                        selfNubeNumber, getReceivers(), convstId);
                }
                break;
            case SendCIVMUtil.ACTION_SHARE_VCARD:
                if (!isFriend()) {
                    CustomLog.d(TAG, "ACTION_SHARE_VCARD 不是好友");
                    return;
                }
                sendVcardBack(data);
                break;
            case SendCIVMUtil.ACTION_FOR_RESERVE_MEETING:
                CustomLog.d(TAG, "预约会议结束后，返回到chat页面");
                break;
            case ACTION_FORWARD_NOTICE:
                if (data == null) {
                    return;
                }
                Bundle bundle = data.getExtras();
                String nubeNumb = bundle.getStringArrayList(
                    SelectLinkManActivity.START_RESULT_NUBE).get(0);
                String name = bundle.getStringArrayList(
                    SelectLinkManActivity.START_RESULT_NAME).get(0);
                String niName = bundle.getStringArrayList(
                    SelectLinkManActivity.START_RESULT_NICKNAME).get(0);
                String nuber = bundle.getStringArrayList(
                    SelectLinkManActivity.START_RESULT_NUMBER).get(0);
                NameElement element = ShowNameUtil.getNameElement(name, niName,
                    nuber, nubeNumb);
                String nicName = ShowNameUtil.getShowName(element);
                selectNubeList.add("@" + nubeNumb + IMConstant.SPECIAL_CHAR);
                selectNameList.add("@" + nicName + IMConstant.SPECIAL_CHAR);
                inputFragment.setSpecialtxt(nicName);
                break;
            case ContactTransmitConfig.REQUEST_REPLY_CODE:
                String msgContent = data.getStringExtra("reply");
                CustomLog.d(TAG, "发送好友验证消息返回");
                sendAddNewFriendMsg(msgContent);
                break;
        }
    }


    /**
     * 选择完名片，返回到本页的操作
     */
    private void sendVcardBack(Intent data) {
        // CustomLog.begin("");
        if (data == null) {
            CustomLog.d(TAG, "data == null");
            return;
        }

        ArrayList<String> nubeNumebrs = data.getExtras().getStringArrayList(
            SelectLinkManActivity.START_RESULT_NUBE);
        String nubeNumber = "";
        if (nubeNumebrs != null && nubeNumebrs.size() > 0) {
            nubeNumber = nubeNumebrs.get(0);
        }
        if (TextUtils.isEmpty(nubeNumber)) {
            tipToast(getString(R.string.date_illegal));
            return;
        }

        ContactFriendBean Info = new NetPhoneDaoImpl(this)
            .queryFriendInfoByNube(nubeNumber);
        ButelVcardBean extInfo = new ButelVcardBean();
        extInfo.setUserId(Info.getContactId());
        extInfo.setNubeNumber(Info.getNubeNumber());
        extInfo.setHeadUrl(Info.getHeadUrl());
        extInfo.setNickname(Info.getNickname());
        extInfo.setPhoneNumber(Info.getNumber());
        extInfo.setSex(Info.getSex());
        showSendVcardDialog(extInfo);

    }


    /**
     * 显示二次 确认对话框
     */
    private void showSendVcardDialog(final ButelVcardBean bean) {
        CustomLog.d(TAG, "showSendVcardDialog begin");

        LayoutInflater inflater = LayoutInflater
            .from(ChatActivity.this);
        View selfView = inflater.inflate(
            R.layout.share_confirm_dialog_view, null);//分享确认弹框布局文

        TextView nameView = (TextView) selfView.findViewById(R.id.name_txt);
        TextView numView = (TextView) selfView
            .findViewById(R.id.recv_num_field);
        SharePressableImageView icon = (SharePressableImageView) selfView
            .findViewById(R.id.contact_icon);

        String headUrl = "";
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            // 群成员人数
            groupMemberSize = groupDao.queryGroupMemberCnt(targetNubeNumber);
            numView.setVisibility(View.VISIBLE);
            numView.setText(groupMemberSize + getString(R.string.person));
            headUrl = groupDao.getGroupHeadUrl(targetNubeNumber);
            Glide.with(this).load(headUrl).placeholder(R.drawable.group_icon)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(icon.shareImageview);
            nameView.setText(groupDao.getGroupNameByGid(targetNubeNumber));

        } else {
            Contact contact = ContactManager.getInstance(ChatActivity.this)
                .getContactInfoByNubeNumber(targetNubeNumber);
            headUrl = contact.getHeadUrl();
            Glide.with(this).load(headUrl).placeholder(R.drawable.default_head_image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(icon.shareImageview);
            nameView.setText(targetShortName);
            numView.setVisibility(View.GONE);
        }

        CommonDialog conDlg = new CommonDialog(ChatActivity.this,
            getLocalClassName(), 300);
        conDlg.addView(selfView);
        conDlg.setTitleVisible(getString(R.string.send_to));
        conDlg.setTransmitCardInfo(bean.getNickname(), Integer.parseInt(bean.getNubeNumber()));

        conDlg.setCancleButton(null, R.string.btn_cancle);
        conDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {
            @Override
            public void onBtnClicked() {
                CustomLog.d(TAG, "点击确定");
                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    SendCIVMUtil.onSendVcardBack(ChatActivity.this, bean,
                        selfNubeNumber, groupId, groupId);
                } else {
                    SendCIVMUtil.onSendVcardBack(ChatActivity.this, bean,
                        selfNubeNumber, getReceivers(), convstId);
                }
                CustomToast.show(mContext, getString(R.string.toast_sent), 1);
            }
        }, R.string.btn_send);

        if (!isFinishing()) {
            conDlg.showDialog();
        }
    }


    private String getReceivers() {
        if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST
            || frameType == VALUE_NOTICE_FRAME_TYPE_NUBE) {
            return targetNubeNumber;
        } else {
            // 新建消息的场合，接收者为收件人输入框数据
            if (receiverNumberLst != null && receiverNumberLst.size() > 0) {
                String nubes = "";
                for (String nubeNum : receiverNumberLst) {
                    nubes = nubes + nubeNum + ";";
                }
                return nubes.substring(0, nubes.length() - 1);
            } else {
                return "";
            }
        }
    }

    //	private String getReceiverDispName(String name) {
    //		if (TextUtils.isEmpty(name)) {
    //			return "";
    //		} else if (name.length() > 10) {
    //			return name.substring(0, 10) + "...";
    //		} else {
    //			return name;
    //		}
    //	}


    @Override
    public void onMsgDelete(String uuid, long receivedTime, int dataCnt) {
        CustomLog.d(TAG, "删除消息:" + uuid);
        getFileTaskManager().cancelTask(uuid);
        if (dataCnt == 1) {
            // 最后一条消息删除时，需要更新会话表lastTime
            noticeDao.deleteLastNotice(uuid, convstId, receivedTime);
        } else {
            noticeDao.deleteNotice(uuid);
        }
    }


    @Override
    public void onMsgForward(String uuid, String sender, int msgType,
                             int msgStatus, String localPath) {
        CustomLog.d(TAG, "转发消息:" + uuid);

        forwardNoticeId = uuid;
        Intent i = new Intent(this, ShareLocalActivity.class);
        i.putExtra(ShareLocalActivity.KEY_ACTION_FORWARD, true);
        i.putExtra(ShareLocalActivity.MSG_ID, forwardNoticeId);
        i.putExtra("chatForwardPath", localPath);
        i.putExtra("chatForwardType", msgType);
        startActivity(i);
        // 修改转发结束逻辑：消息转发后，界面退回到当前聊天界面，成功发送并toast显示：已发送
    }


    @Override
    public void onMsgForward(String uuid, String sender, int msgType, int msgStatus, String localPath, String txt, String vcardName, String vcardNumber, String creator, String meetingRoomId, String meetingTopic, String date, String hms) {
        forwardNoticeId = uuid;
        Intent i = new Intent(this, ShareLocalActivity.class);
        i.putExtra(ShareLocalActivity.KEY_ACTION_FORWARD, true);
        i.putExtra(ShareLocalActivity.MSG_ID, forwardNoticeId);
        i.putExtra("chatForwardPath", localPath);
        i.putExtra("chatForwardType", msgType);
        i.putExtra("chatForwardTxt", txt);   //文本内容、文章title存储于该字段、聊天记录的title -- A和B的聊天记录
        i.putExtra("chatForwardVcardName", vcardName);
        i.putExtra("chatForwardVcardNumber", vcardNumber);
        i.putExtra("chatForwardCreator", creator);
        i.putExtra("chatForwardMeetingRoomId", meetingRoomId);
        i.putExtra("chatForwardMeetingTopic", meetingTopic);
        i.putExtra("chatForwardDate", date);
        i.putExtra("chatForwardHms", hms);
        startActivity(i);
    }


    @Override
    public void onMoreClick(String uuid, int msgType, int msgStatus, boolean checked) {
        showMoreOpLayout();
        if (chatAdapter != null && chatAdapter.hasCheckedData()) {
            enableMoreOp(true);
            titlebackbtn = true;
            newNoticeNumflag = true;
            if (titlebackbtn) {
                backbtn.setVisibility(GONE);
                newNoticeNum.setVisibility(GONE);
                backtext.setVisibility(View.VISIBLE);
                backtext.setText(R.string.btn_cancle);
                backtext.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (chatAdapter != null && chatAdapter.isMultiCheckMode()) {
                            hideMoreOpLayout();
                            cleanCheckData();
                        }
                        titlebackbtn = false;
                        newNoticeNumflag = false;
                        backbtn.setVisibility(View.VISIBLE);
                        //                        updateNoticesInfo();
                        backtext.setVisibility(GONE);
                        getTitleBar().setBack(null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (CommonUtil.isFastDoubleClick()) {
                                    return;
                                }
                                if (waitDialog != null && waitDialog.isShowing()) {
                                    //                    hideWaitDialog();
                                    removeLoadingView();
                                    // ButelMeetingManager.getInstance().cancelCreateMeeting(
                                    //     ButelContactDetailActivity.class.getName());
                                }
                                exitActivity();
                            }
                        });
                    }
                });
            }

        } else {
            enableMoreOp(false);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //点击屏幕，取消键盘
        if (inputFragment.isShowing) {
            CommonUtil.hideSoftInputFromWindow(ChatActivity.this);
            inputFragment.setHide();
        }
        return inputFragment.handleRecordLayoutTouchEvent(event);
    }


    private void preCheckDataforForward() {
        final MedicalAlertDialog menuDlg = new MedicalAlertDialog(this);
        menuDlg.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                batchForward(0);
            }
        }, mContext.getString(R.string.one_by_one_forward));
        menuDlg.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                batchForward(1);
            }
        }, mContext.getString(R.string.combine_forward));
        menuDlg.addButtonThird(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                menuDlg.dismiss();
            }
        }, mContext.getString(R.string.btn_cancle));
        menuDlg.show();
    }


    private void batchForward(int forwardtype) {
        LinkedHashMap<String, NoticesBean> uidMap = null;
        if (chatAdapter != null) {
            uidMap = chatAdapter.getCheckedDataMap();
        }
        if (uidMap != null) {
            boolean hasInvalidData = false;
            List<String> validList = new ArrayList<String>();
            List<Map.Entry<String, NoticesBean>> noticesListArray =
                new ArrayList<Map.Entry<String, NoticesBean>>(uidMap.entrySet());

            Collections.sort(noticesListArray, new Comparator<Map.Entry<String, NoticesBean>>() {

                public int compare(Map.Entry<String, NoticesBean> o1,
                                   Map.Entry<String, NoticesBean> o2) {
                    Long time1 = o1.getValue().getReceivedTime() > 0 ? o1.getValue()
                        .getReceivedTime()
                                                                     : o1.getValue().getSendTime();
                    Long time2 = o2.getValue().getReceivedTime() > 0 ? o2.getValue()
                        .getReceivedTime()
                                                                     : o2.getValue().getSendTime();
                    if (time1 > time2) {
                        return 1;
                    }
                    return -1;
                }
            });
            for (int i = 0; i < noticesListArray.size(); i++) {
                Map.Entry<String, NoticesBean> entry = noticesListArray.get(i);
                String uid = entry.getKey();
                NoticesBean bean = entry.getValue();
                CustomLog.d(TAG, "uid = " + uid);
                boolean valid = true;
                if (bean != null) {
                    int type = bean.getType();
                    int status = bean.getStatus();
                    CustomLog.d(TAG, "type = " + type + " status=" + status);
                    //0：逐条转发  1：合并转发
                    if (forwardtype == 0) {
                        if (type == FileTaskManager.NOTICE_TYPE_AUDIO_SEND
                            || type == FileTaskManager.NOTICE_TYPE_ATTACHMENT_FILE) {
                            hasInvalidData = true;
                            valid = false;
                        }
                    } else {
                        if (type == FileTaskManager.NOTICE_TYPE_AUDIO_SEND
                            || type == FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND
                            || type == FileTaskManager.NOTICE_TYPE_ATTACHMENT_FILE
                            || type == FileTaskManager.NOTICE_TYPE_MEETING_INVITE
                            || type == FileTaskManager.NOTICE_TYPE_MEETING_BOOK) {
                            hasInvalidData = true;
                            valid = false;
                        }
                    }

                    if (status != FileTaskManager.TASK_STATUS_SUCCESS) {
                        hasInvalidData = true;
                        valid = false;
                    }
                    if (valid) {
                        validList.add(uid);
                    }
                } else {
                    continue;
                }
            }
            if (hasInvalidData) {
                //弹提醒对话框
                invalidForwardDialog(validList, forwardtype);
            } else {
                //直接转发
                doForwardWork(validList, forwardtype);
            }
        }

    }


    private void preCheckDataforCollection() {
        Map<String, NoticesBean> uidMap = null;
        if (chatAdapter != null) {
            uidMap = chatAdapter.getCheckedDataMap();
        }
        if (uidMap != null) {
            boolean hasInvalidData = false;
            List<NoticesBean> validList = new ArrayList<NoticesBean>();
            Iterator<Map.Entry<String, NoticesBean>> entries = uidMap.entrySet()
                .iterator();
            while (entries.hasNext()) {
                Map.Entry<String, NoticesBean> entry = entries.next();
                String uid = entry.getKey();
                NoticesBean bean = entry.getValue();

                CustomLog.d(TAG, "uid = " + uid);
                boolean valid = true;

                if (bean != null) {
                    int type = bean.getType();
                    int status = bean.getStatus();
                    CustomLog.d(TAG, "type = " + type + " status=" + status);
                    if (type == FileTaskManager.NOTICE_TYPE_VCARD_SEND
                        || type == FileTaskManager.NOTICE_TYPE_RECORD
                        || type == FileTaskManager.NOTICE_TYPE_MEETING_INVITE
                        || type == FileTaskManager.NOTICE_TYPE_MEETING_BOOK) {
                        hasInvalidData = true;
                        valid = false;
                    }

                    if (valid) {
                        validList.add(bean);
                    }
                } else {
                    continue;
                }

            }

            if (hasInvalidData) {
                //弹提醒对话框
                invalidCollectDialog(validList);
            } else {
                //直接收藏
                doCollectWork(validList);
            }
        }
    }


    private void preCheckDataforDel() {
        Map<String, NoticesBean> uidMap = null;
        if (chatAdapter != null) {
            uidMap = chatAdapter.getCheckedDataMap();
        }
        if (uidMap != null) {
            //boolean hasInvalidData = false;
            List<String> validList = new ArrayList<String>();
            for (String key : uidMap.keySet()) {
                validList.add(key);
                CustomLog.d(TAG, "uid = " + key);
            }

            delDialog(validList, chatAdapter.getCount());
        }
    }


    private void invalidForwardDialog(final List<String> uidList, final int forwardType) {
        // CustomLog.begin("");
        CommonDialog confDlg = new CommonDialog(ChatActivity.this,
            getLocalClassName(), 12350);
        confDlg.setCancelable(false);
        if (forwardType == 0) {
            confDlg.setMessage(getString(R.string.voice_not_send_message_not_forwarding));
        } else {
            confDlg.setMessage(getString(R.string.not_combine_forwarding));
        }

        confDlg.setCancleButton(null, R.string.btn_cancle);
        confDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {
            @Override
            public void onBtnClicked() {
                CustomLog.d(TAG, "点击转发");
                if (uidList.size() > 0) {
                    doForwardWork(uidList, forwardType);
                } else {
                    CustomToast.show(mContext, getString(R.string.no_valid_forward_msg),
                        CustomToast.LENGTH_LONG);
                }

            }
        }, R.string.chat_forward);

        if (!isFinishing()) {
            confDlg.showDialog();
        }
    }


    private void invalidCollectDialog(final List<NoticesBean> dataList) {
        // CustomLog.begin("");
        CommonDialog confDlg = new CommonDialog(ChatActivity.this,
            getLocalClassName(), 12351);
        confDlg.setCancelable(false);
        confDlg.setTitle(R.string.send_Vcard_dialog_title);
        confDlg.setMessage(getString(R.string.not_collect));
        confDlg.setCancleButton(null, R.string.btn_cancle);
        confDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {
            @Override
            public void onBtnClicked() {
                CustomLog.d(TAG, "点击收藏");
                doCollectWork(dataList);
            }
        }, R.string.collect_str);

        if (!isFinishing()) {
            confDlg.showDialog();
        }
        // CustomLog.end("");
    }


    private void delDialog(final List<String> uidList, int listCount) {
        final MedicalAlertDialog menuDlg = new MedicalAlertDialog(this);
        menuDlg.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                CustomLog.d(TAG, "批量删除所选消息");
                noticeDao.deleteNotices(uidList);
                hideMoreOpLayout();
                cleanCheckData();
            }
        }, mContext.getString(R.string.chat_delete));
        menuDlg.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                menuDlg.dismiss();
            }
        }, mContext.getString(R.string.btn_cancle));
        menuDlg.show();
    }


    private void doCollectWork(List<NoticesBean> dataList) {
        if (dataList != null && dataList.size() > 0) {
            for (int i = 0; i < dataList.size(); i++) {
                NoticesBean bean = dataList.get(i);
                if (bean.getType() == FileTaskManager.NOTICE_TYPE_ARTICAL_SEND) {
                    JSONArray newBodyArray = new JSONArray();
                    try {
                        JSONObject newBodyObj = new JSONObject();
                        JSONArray bodyArray = new JSONArray(bean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            JSONObject tmpObj = bodyObj.optJSONObject("articleInfo");
                            newBodyObj.put("ArticleId", tmpObj.optString("articleId"));
                            newBodyObj.put("title", tmpObj.optString("title"));
                            newBodyObj.put("previewUrl", tmpObj.optString("previewUrl"));
                            newBodyObj.put("introduction", tmpObj.optString("introduction"));
                            newBodyObj.put("articleType", tmpObj.optInt("articleType"));
                            newBodyObj.put("name", tmpObj.optString("officeName"));
                            newBodyObj.put("isforwarded", 1);
                            newBodyArray.put(0, newBodyObj);
                        }
                    } catch (Exception e) {
                        CustomLog.e(TAG, "批量收藏文章被选择" + e.toString());
                    }
                    bean.setBody(newBodyArray.toString());
                } else if (bean.getType() == FileTaskManager.NOTICE_TYPE_CHATRECORD_SEND) {
                    try {
                        JSONArray bodyArray = new JSONArray(bean.getBody());
                        if (bodyArray != null && bodyArray.length() > 0) {
                            JSONObject bodyObj = bodyArray.optJSONObject(0);
                            JSONArray detailArray = bodyObj.optJSONArray("chatrecordInfo");
                            for (int j = 0; j < detailArray.length(); j++) {
                                JSONObject tmpObj = detailArray.optJSONObject(j);
                                if (tmpObj.optInt("type") == FileTaskManager.NOTICE_TYPE_TXT_SEND) {
                                    tmpObj.put("txt", tmpObj.optString("text"));
                                } else if (tmpObj.optInt("type") ==
                                    FileTaskManager.NOTICE_TYPE_ARTICAL_SEND) {
                                    tmpObj.put("offAccLogoUrl", tmpObj.optString("previewUrl"));
                                }

                            }
                        }
                        bean.setBody(bodyArray.toString());
                    } catch (Exception e) {
                        CustomLog.e("ChatListAdapter", "收藏聊天记录，解析json出错");
                    }
                }
            }
            CollectionManager.getInstance().addCollectionByNoticesBeans(dataList);
            Toast.makeText(ChatActivity.this, R.string.have_collected,
                Toast.LENGTH_SHORT).show();
        }
        hideMoreOpLayout();
        cleanCheckData();
    }


    private void doForwardWork(List<String> uidList, int forwardType) {
        if (uidList != null && uidList.size() > 0) {
            Intent i = new Intent(this, ShareLocalActivity.class);
            i.putExtra(ShareLocalActivity.KEY_ACTION_FORWARD, true);
            i.putExtra(ShareLocalActivity.MSG_ID,
                StringUtil.list2String(uidList, ','));
            i.putExtra(ShareLocalActivity.FORWARD_TYPE, forwardType);
            i.putExtra("chatForwardType",
                FileTaskManager.NOTICE_TYPE_MANY_MSG_FORWARD); //定义 -2 描述逐条、合并的转发
            i.putExtra("noticeNum", uidList.size());
            //转发 烛天转发、合并转发 需要显示  xxx的聊天记录，如果是群的话，只显示群名称
            if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                String gruopName = groupDao.getGroupNameByGid(groupId);
                i.putExtra("me", "");
                i.putExtra("theOther", gruopName);
            } else {
                i.putExtra("me", AccountManager.getInstance(this).getAccountInfo().nickName);
                i.putExtra("theOther", targetShortName);
            }

            startActivity(i);
        }
    }


    private void cleanCheckData() {
        if (chatAdapter != null) {
            chatAdapter.cleanCheckedData();
        }
    }


    private void showMoreOpLayout() {
        if (moreOpLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        if (chatAdapter != null && !chatAdapter.isMultiCheckMode()) {
            chatAdapter.setMultiCheckMode(true);
            if (inputFragment != null && inputFragment.isShowing) {
                inputFragment.setHide();
                inputFragment.setEditHide();
                //				getSupportFragmentManager().beginTransaction()
                //                .hide(inputFragment).commit();
            }
            moreOpLayout.setVisibility(View.VISIBLE);
        }
    }


    private void hideMoreOpLayout() {
        if (chatAdapter != null && chatAdapter.isMultiCheckMode()) {
            moreOpLayout.setVisibility(GONE);
            chatAdapter.setMultiCheckMode(false);

            if (inputFragment != null && inputFragment.isShowing) {
                inputFragment.setEditShow();
            }
            backbtn.setVisibility(View.VISIBLE);
            updateNoticesInfo();
            backtext.setVisibility(GONE);
            getTitleBar().setBack(null, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CommonUtil.isFastDoubleClick()) {
                        return;
                    }
                    if (waitDialog != null && waitDialog.isShowing()) {
                        //                    hideWaitDialog();
                        removeLoadingView();
                        // ButelMeetingManager.getInstance().cancelCreateMeeting(
                        //     ButelContactDetailActivity.class.getName());
                    }
                    exitActivity();
                }
            });

        }
    }


    private void initMoreOpWidget() {

        moreOpLayout = (RelativeLayout) this.findViewById(R.id.more_op_layout);
        forwardBtn = (ImageButton) this.findViewById(R.id.chat_more_forward_btn);
        forwardBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Toast.makeText(getBaseContext(), "转发", Toast.LENGTH_SHORT).show();
                preCheckDataforForward();
            }
        });
        collectBtn = (ImageButton) this.findViewById(R.id.chat_more_collect_btn);
        collectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Toast.makeText(getBaseContext(), "收藏", Toast.LENGTH_SHORT).show();
                preCheckDataforCollection();
            }
        });
        delBtn = (ImageButton) this.findViewById(R.id.chat_more_del_btn);
        delBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Toast.makeText(getBaseContext(), "删除", Toast.LENGTH_SHORT).show();
                preCheckDataforDel();
            }
        });
    }


    private void enableMoreOp(boolean enable) {
        forwardBtn.setEnabled(enable);
        collectBtn.setEnabled(enable);
        delBtn.setEnabled(enable);
    }


    /*
     * 监听群信息和群成员表
     */
    private class GroupMemberObserver extends ContentObserver {

        public GroupMemberObserver() {
            super(new Handler());
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.i(TAG, "GroupMemberObserver::onChange()");

            CustomLog.d(TAG, "t_multi_chat_groups 群组数据库数据发生变更");
            setTitleInfo();
            dateList.clear();
            dateList.putAll(groupDao.queryGroupMembers(groupId));
        }
    }


    private class GroupObserver extends ContentObserver {

        public GroupObserver() {
            super(new Handler());
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG, "t_multi_chat_users 群成员表数据库数据发生变更");
            setTitleInfo();
            if (groupDao.isGroupMember(groupId, selfNubeNumber)) {
                getTitleBar().enableRightBtn(null,
                    R.drawable.multi_send_btn_selector,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 跳转到群发收件人界面
                            if (CommonUtil.isFastDoubleClick()) {
                                return;
                            }

                            if (groupDao.isGroupMember(groupId,
                                selfNubeNumber)) {
                                // 是群成员，跳转

                                // 群发，跳到群发联系人页面
                                Intent intent = new Intent(
                                    ChatActivity.this,
                                    GroupChatDetailActivity.class);
                                intent.putExtra(
                                    GroupChatDetailActivity.KEY_CHAT_TYPE,
                                    GroupChatDetailActivity.VALUE_GROUP);
                                intent.putExtra(
                                    KEY_NUMBER,
                                    groupId);
                                intent.putExtra(GroupChatDetailActivity.KEY_GETGROUPCSLFAIL,
                                    getGroupClsFail);
                                intent.putExtra(GroupChatDetailActivity.KEY_MEETINGNUB, meetingNub);
                                intent.putExtra(GroupChatDetailActivity.KEY_MEETINGTHEME,
                                    meetingTheme);
                                intent.putExtra(GroupChatDetailActivity.KEY_MEETINGPASSWORD,
                                    meetingPaw);
                                ChatActivity.this.startActivity(intent);

                                CustomLog.d(TAG, "点击群发图标，跳转到群发收件人界面");
                            }
                        }
                    });
            } else {
                getTitleBar().setRightBtnVisibility(INVISIBLE);
            }
        }
    }


    /*
     * 获取群名称表
     */
    private String getGroupName() {
        String gruopName = groupDao.getGroupNameByGid(groupId);
        //如果群名称超过15字，截取，以便显示免打扰铃铛
        gruopName = checkGroupNameLength(gruopName);
        isGroupMember = groupDao.isGroupMember(groupId, selfNubeNumber);
        groupMemberSize = groupDao.queryGroupMemberCnt(groupId);
        return gruopName;
    }


    private String getGroupNameTitle() {
        String gruopName = groupDao.getGroupNameByGidTitle(groupId);
        //如果群名称超过15字，截取，以便显示免打扰铃铛
        gruopName = checkGroupNameLength(gruopName);
        isGroupMember = groupDao.isGroupMember(groupId, selfNubeNumber);
        return gruopName;
    }


    private String checkGroupNameLength(String groupName) {
        if (groupName.length() > 8) {
            groupName = groupName.substring(0, 8) + "...";
            return groupName;
        }
        return groupName;
    }


    @Override
    public void onSetSelectMemeber(String name, String nube) {
        // 选择@回復的成員
        selectNubeList.add("@" + nube + IMConstant.SPECIAL_CHAR);
        selectNameList.add("@" + name + IMConstant.SPECIAL_CHAR);
        inputFragment.setSpecialtxt("@" + name);
    }


    private ArrayList<ContactFriendBean> GroupMemberToContactsBean() {
        ArrayList<ContactFriendBean> List = new ArrayList<ContactFriendBean>();
        ContactFriendBean data;
        Iterator<Map.Entry<String, GroupMemberBean>> iter = dateList.entrySet()
            .iterator();
        while (iter.hasNext()) {
            GroupMemberBean bean = iter.next().getValue();
            if (!bean.getNubeNum().equals(selfNubeNumber)) {
                data = new ContactFriendBean();
                data.setHeadUrl(bean.getHeadUrl());
                data.setName(bean.getDispName());
                data.setNickname(bean.getNickName());
                data.setNumber(bean.getPhoneNum());
                data.setNubeNumber(bean.getNubeNum());
                data.setSex(
                    (bean.getGender() == GroupMemberTable.GENDER_MALE ? GroupMemberTable.GENDER_MALE
                                                                      : GroupMemberTable.GENDER_FEMALE)
                        + "");
                data.setPym(PinyinUtil.getPinYin(bean.getDispName())
                    .toUpperCase());
                List.add(data);
            }
        }
        ListSort<ContactFriendBean> listSort = new ListSort<ContactFriendBean>();
        listSort.Sort(List, "getPym", null);
        return List;
    }


    private static void tipToast(String txt) {
        CommonUtil.showToast(txt);
        CustomLog.d(TAG, txt);
    }


    @Override
    public void reBookMeeting() {
        bookMeeting();
    }


    @Override
    public void reCreatMeeting() {
        conveneMeeting();
    }


    @Override
    public void addNewFriend() {
        int relationCode = FriendsManager.getInstance()
            .getFriendRelationByNubeNumber(targetNubeNumber);
        if (relationCode == FriendsManager.RELATION_TYPE_BOTH) {
            //            CustomToast.show(mContext,targetShortName + "已经是您的好友",CustomToast.LENGTH_LONG);
            CustomToast.show(mContext, getString(R.string.have_been_friend),
                CustomToast.LENGTH_LONG);
            return;
        }
        Intent intent = new Intent(mContext, VerificationReplyDialog.class);
        intent.putExtra(VerificationReplyDialog.KEY_DIALOG_TYPE, 1);  // 0从名片页进入 1：从IM页面进入
        intent.putExtra("nubeNumber", targetNubeNumber);
        startActivityForResult(intent, ContactTransmitConfig.REQUEST_REPLY_CODE);
    }


    private void updateNoticesInfo() {
        if (newNoticeNumflag) {
            newNoticeNum.setVisibility(GONE);
        } else {
            int count = noticeDao.getNewNoticeCount();
            if (count == 0) {
                newNoticeNum.setVisibility(View.INVISIBLE);
            } else {
                if (count > 99) {
                    newNoticeNum
                        .setBackgroundResource(R.drawable.butel_new_msg_flag);
                    newNoticeNum.setText(R.string.main_bottom_count_99);
                } else {
                    newNoticeNum
                        .setBackgroundResource(R.drawable.chat_unread_count);
                    newNoticeNum.setText(String.valueOf(count));
                }
                newNoticeNum.setVisibility(View.VISIBLE);
            }
        }
    }


    private boolean isFriend() {
        if (conversationType != VALUE_CONVERSATION_TYPE_MULTI) {
            if (ContactManager.getInstance(mContext).checkNubeIsCustomService(targetNubeNumber)) {
                CustomLog.d(TAG, targetNubeNumber + "是客服账号，可以直接发送消息");
                return true;
            }

            int relationCode = FriendsManager.getInstance()
                .getFriendRelationByNubeNumber(targetNubeNumber);
            if (relationCode != FriendsManager.RELATION_TYPE_BOTH) {
                String tmpStr = targetShortName
                    + getString(R.string.start_friend_approve_send_approve_request) +
                    getString(R.string.pass_request_chat_send_request);
                noticeDao.createAddFriendTxt("", targetNubeNumber, "", null, "",
                    FileTaskManager.NOTICE_TYPE_DESCRIPTION, tmpStr, convstId, null
                    , System.currentTimeMillis() + "");
                return false;
            }
            return true;
        }

        return true;
    }


    private void sendAddNewFriendMsg(String msgContent) {
        MDSAccountInfo loginUserInfo = AccountManager.getInstance(mContext).getAccountInfo();
        MedicalApplication.getFileTaskManager().sendStrangerMsg(loginUserInfo.getNube(),
            targetNubeNumber, loginUserInfo.getNickName(),
            loginUserInfo.getHeadThumUrl(), msgContent, false);

        Contact contact = ContactManager.getInstance(ChatActivity.this)
            .getContactInfoByNubeNumber(targetNubeNumber);

        StrangerMessage strangerMessage = new StrangerMessage(
            targetNubeNumber,
            contact.getHeadUrl(),
            targetShortName,
            0,
            msgContent,
            String.valueOf(System.currentTimeMillis()), 1);

        int addResutl = FriendsManager.getInstance().addStrangerMsg(strangerMessage);
        if (addResutl == 0) {
            CustomLog.d(TAG, "addStrangerMsg 成功");
        } else {
            CustomLog.d(TAG, "addStrangerMsg 失败， addStrangerMessageResult：" + addResutl);
        }

        FriendInfo friendInfo = new FriendInfo();
        friendInfo.setNubeNumber(contact.getNubeNumber());
        friendInfo.setName(contact.getName());
        friendInfo.setHeadUrl(contact.getHeadUrl());
        friendInfo.setEmail(contact.getEmail());
        friendInfo.setWorkUnitType(String.valueOf(contact.getWorkUnitType()));
        friendInfo.setWorkUnit(contact.getWorkUnit());
        friendInfo.setDepartment(contact.getDepartment());
        friendInfo.setProfessional(contact.getProfessional());
        friendInfo.setOfficeTel(contact.getOfficeTel());
        friendInfo.setUserFrom(Integer.valueOf(contact.getUserFrom()));
        friendInfo.setIsDeleted(FriendInfo.NOT_DELETE);
        friendInfo.setRelationType(FriendInfo.RELATION_TYPE_POSITIVE);
        int addFriendResult = FriendsManager.getInstance().addFriend(friendInfo);
        if (addFriendResult == 0) {
            CustomLog.d(TAG, "addFriend 成功");
        } else {
            CustomLog.d(TAG, "addFriend 失败， addFriendResult：" + addFriendResult);
        }
    }


    /**
     * 监听好友关系表
     */
    private class FriendRelationObserver extends ContentObserver {

        public FriendRelationObserver() {
            super(new Handler());
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG, "好友关系数据库数据发生变更");
            isShowRightButton();
        }

    }


    private void isShowRightButton() {
        int relationCode = FriendsManager.getInstance()
            .getFriendRelationByNubeNumber(targetNubeNumber);
        if (relationCode != FriendsManager.RELATION_TYPE_BOTH) {
            getTitleBar().setRightBtnVisibility(INVISIBLE);
        } else {
            getTitleBar().setRightBtnVisibility(VISIBLE);
        }
    }


    /**
     * 添加对方为好友，先调searchAccount查询该好友的详细信息，然后插入好友表
     */
    private void searchServiceNubeInfo(String nubeNumber) {
        CustomLog.i(TAG, "searchServiceNubeInfo()");

        if (nubeNumber.length() == 0) {
            CustomLog.e(TAG, "service nubeNuber是空");
            return;
        }
        searchUsers = new MDSAppSearchUsers() {
            @Override
            protected void onSuccess(List<MDSDetailInfo> responseContent) {

                if (responseContent == null) {
                    CustomLog.e(TAG, "searchServiceNubeInfo responseContent is null");
                    return;
                } else if (responseContent.size() == 0) {
                    CustomLog.e(TAG, "searchServiceNubeInfo responseContent size is 0");
                    return;
                }
                saveAdminNubeInfo(responseContent.get(0));
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.e(TAG, "searchServiceNubeInfo errorCode:" + statusCode
                    + " statusInfo:" + statusInfo);
            }

        };

        //3 表示按照视讯号搜索
        int searchType = 3;
        searchUsers.appSearchUsers(AccountManager.getInstance(this)
            .getToken(), searchType, new String[] { nubeNumber });
    }


    private void saveAdminNubeInfo(MDSDetailInfo info) {
        CustomLog.d(TAG, "saveServiceNubeInfo headUrl:" + info.getHeadThumUrl()
            + " username:" + info.getNickName());
        SharedPreferences preferences = mContext
            .getSharedPreferences(KEY_SERVICE_NUBE_INFO, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("HEAD_URL", info.getHeadThumUrl());
        editor.putString("USERNAME", info.getNickName());
        CustomLog.d(TAG, "adminUrl:" + info.getHeadThumUrl() + " adminName:" + info.getNickName());
        editor.commit();
    }


    private String getAdminNickName() {
        SharedPreferences preferences = mContext.
            getSharedPreferences(ChatActivity.KEY_SERVICE_NUBE_INFO, MODE_PRIVATE);
        return preferences.getString("USERNAME", "10000");
    }


    private void getGroupcsl() {
        CustomLog.i(TAG, "getGroupcsl()");
        MDSAPPGetGroupCsl mdsappGetGroupCsl = new MDSAPPGetGroupCsl() {
            @Override
            protected void onSuccess(MDSGroupCslInfo responseContent) {
                super.onSuccess(responseContent);
                CustomLog.d(TAG, "mdsappGetGroupCsl   onSuccess" + responseContent.cslRoomNo +
                    responseContent.cslSubject);
                if (!responseContent.cslSubject.equals("")) {
                    addMeetingThemeTxt.setText("【" + responseContent.cslSubject + "】");
                } else {
                    addMeetingThemeTxt.setText(R.string.chat_add_meeting_first);
                }
                addMeetinglayout.setVisibility(VISIBLE);
                meetingNub = responseContent.cslRoomNo;
                meetingPaw = responseContent.cslPassword;
                getGroupClsFail = GroupChatDetailActivity.VALUE_GETGROUPCSLSUCCESS;
                meetingTheme = responseContent.cslSubject;
                getGroupCslCount = 0;

            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                meetingNub = "";
                meetingTheme = "";
                meetingPaw = "";
                CustomLog.d(TAG, "mdsappGetGroupCsl   onFail" + statusInfo);
                addMeetinglayout.setVisibility(GONE);
                //                meetingNub = GroupChatDetailActivity.VALUE_MEETINGNUB;
                if (statusCode != MDSErrorCode.MDS_NOTEXIT_GRPOUPCSL) {
                    //                    CustomToast.show(mContext,R.string.network_reload,CustomToast.LENGTH_SHORT);
                    getGroupClsFail = GroupChatDetailActivity.VALUE_GETGROUPCSLFAIL;
                    if (getGroupCslCount < 3) {
                        getGroupCslCount++;
                        getGroupcsl();
                    }
                }
            }
        };
        mdsappGetGroupCsl.getGroupCsl(AccountManager.getInstance(this).getMdsToken(), groupId);
    }


    private void joinMeeting(String meetingId, String groupid) {
        if (MedicalMeetingManage.getInstance().getActiveMeetingId().equals(meetingId) ||
            TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
            int i = MedicalMeetingManage.getInstance().joinMeeting(meetingId, groupid,
                new MedicalMeetingManage.OnJoinMeetingListener() {
                    @Override
                    public void onJoinMeeting(String valueDes, int valueCode) {
                        //TODO 加入会议的返回键处理
                        removeLoadingView();
                    }
                });
            if (i == 0) {
                showLoadingView(getString(R.string.join_consultation),
                    new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            dialog.dismiss();
                            MedicalMeetingManage.getInstance().cancelJoinMeeting("HomeActivity");
                            CustomToast.show(getApplicationContext(),
                                getString(R.string.cancel_get_consultation_info),
                                Toast.LENGTH_SHORT);
                        }
                    });
            } else {
                CustomToast.show(ChatActivity.this, getString(R.string.join_consultation_fail),
                    CustomToast.LENGTH_SHORT);
            }
        } else {
            CustomToast.show(mContext, getString(R.string.is_video_meeting) +
                MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
        }
    }


    private void checkPassword() {
        CustomLog.i(TAG, "checkPassword()");
        GetMeetingInfomation getMeetingInfomation = new GetMeetingInfomation() {
            @Override
            protected void onSuccess(MeetingInfomation responseContent) {
                super.onSuccess(responseContent);
                CustomLog.d(TAG, "GetMeetingInfomation onSuccess");
                if (responseContent.meetingStatus == 3) {//会议已经结束
                    removeLoadingView();
                    String MeetingEndText;
                    MeetingEndText = getString(R.string.consultation_has_ended);
                    CustomToast
                        .show(getApplicationContext(), MeetingEndText, Toast.LENGTH_SHORT);
                    return;
                } else {
                    if (responseContent.hasMeetingPwd == 0) {
                        //没有密码
                        removeLoadingView();
                        CustomLog.d(TAG, " GetMeetingInfomation会议没有密码");
                        joinMeeting(meetingNub, groupId);
                    } else if (responseContent.hasMeetingPwd == 1) {
                        //有密码
                        CustomLog.d(TAG, "GetMeetingInfomation会议没有密码 会议有密码");
                        CheckMeetingPwd checkMeetingPwd = new CheckMeetingPwd() {
                            @Override
                            protected void onSuccess(CheckMeetingPwdInfo responseContent) {
                                super.onSuccess(responseContent);
                                removeLoadingView();
                                CustomLog.d(TAG, "CheckMeetingPwd onSuccess");
                                joinMeeting(meetingNub, groupId);
                            }


                            @Override
                            protected void onFail(int statusCode, String statusInfo) {
                                super.onFail(statusCode, statusInfo);
                                removeLoadingView();
                                CustomLog.d(TAG, "CheckMeetingPwd onFail" + statusInfo);
                                passwordError();
                            }
                        };
                        if (responseContent.meetingHost.equals(
                            AccountManager.getInstance(ChatActivity.this).getToken())) {
                            removeLoadingView();
                            joinMeeting(meetingNub, groupId);
                        } else if (meetingPaw.equals("")) {
                            removeLoadingView();
                            passwordError();
                        } else {
                            checkMeetingPwd.checkMeetingPwd(
                                AccountManager.getInstance(ChatActivity.this).getToken(),
                                Integer.valueOf(meetingNub), meetingPaw);
                            CustomLog.d(TAG, "CheckMeetingPwd");
                        }
                    }
                }
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                if (statusCode == -906) {
                    CustomToast.show(mContext, getString(R.string.consultation_nub_error),
                        CustomToast.LENGTH_SHORT);
                } else {
                    CustomToast.show(mContext, statusInfo, CustomToast.LENGTH_SHORT);
                }
                CustomLog.d(TAG, "getMeetingInfomation onFail" + statusInfo);
            }
        };
        showLoadingView(getString(R.string.wait), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

                removeLoadingView();
            }
        });
        getMeetingInfomation.getMeetingInfomation(Integer.valueOf(meetingNub));
        CustomLog.d(TAG, "getMeetingInfomation");

    }


    private void passwordError() {
        CustomLog.d(TAG, "密码错误");
        MDSAccountInfo info = AccountManager.getInstance(
            getApplicationContext()).getAccountInfo();
        String nube = AccountManager.getInstance(getApplicationContext())
            .getAccountInfo().getNube();
        Boolean isInputID = true;
        Intent intent = new Intent(ChatActivity.this, InputMeetingPasswordDialog.class);
        intent.putExtra("accesstoken", info.accessToken);
        intent.putExtra("nubeNumber", info.nube);
        intent.putExtra("nickName", info.nickName);
        intent.putExtra("meetingId", meetingNub);
        intent.putExtra("nube", nube);
        //isInputID 判断是否是从输入框输入的会议号
        intent.putExtra("isInputID", String.valueOf(isInputID));
        intent.putExtra("needGroupId", true);
        intent.putExtra("groupId", groupId);
        startActivity(intent);
    }


    private void initReceiver() {
        netReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //                判断当前网络状态
                if (intent.getIntExtra(NetWorkChangeReceiver.NET_TYPE, 0) == 0) {
                    CustomLog.d(TAG, "网络断开");
                } else {
                    CustomLog.d(TAG, "网络连接");
                    if (getGroupClsFail.equals(GroupChatDetailActivity.VALUE_GETGROUPCSLFAIL)) {
                        getGroupcsl();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(NetWorkChangeReceiver.NET_CHANGE);
        mContext.registerReceiver(netReceiver, filter);
    }

}