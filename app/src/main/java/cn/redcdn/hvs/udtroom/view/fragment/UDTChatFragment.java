package cn.redcdn.hvs.udtroom.view.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.datacenter.hpucenter.data.CSLRoomDetailInfo;
import cn.redcdn.datacenter.hpucenter.data.HPUCommonCode;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInfomation;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInfomation;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.VerificationReplyDialog;
import cn.redcdn.hvs.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.activity.MultiImageChooserActivity;
import cn.redcdn.hvs.im.activity.SelectGroupMemeberActivity;
import cn.redcdn.hvs.im.activity.SelectLinkManActivity;
import cn.redcdn.hvs.im.activity.UDTChatInputFragment;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.bean.ButelPAVExInfo;
import cn.redcdn.hvs.im.bean.ButelVcardBean;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.NoticesBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.StrangerMessage;
import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.column.NoticesTable;
import cn.redcdn.hvs.im.common.CommonWaitDialog;
import cn.redcdn.hvs.im.common.ThreadPoolManger;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NetPhoneDaoImpl;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.CollectionManager;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.receiver.NetWorkChangeReceiver;
import cn.redcdn.hvs.im.task.QueryDTNoticeAsyncTask;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.im.util.ListSort;
import cn.redcdn.hvs.im.util.PinyinUtil;
import cn.redcdn.hvs.im.util.PlayerManager;
import cn.redcdn.hvs.im.util.SendCIVMDTUtil;
import cn.redcdn.hvs.im.util.SendCIVMUtil;
import cn.redcdn.hvs.im.util.WakeLockHelper;
import cn.redcdn.hvs.im.view.BottomMenuWindow;
import cn.redcdn.hvs.im.view.CommonDialog;
import cn.redcdn.hvs.im.view.MedicalAlertDialog;
import cn.redcdn.hvs.im.view.SharePressableImageView;
import cn.redcdn.hvs.im.view.VoiceTipView;
import cn.redcdn.hvs.meeting.activity.ReserveSuccessActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.profiles.activity.CollectionActivity;
import cn.redcdn.hvs.udtroom.adapter.UDTChatListAdapter;
import cn.redcdn.hvs.udtroom.configs.UDTGlobleData;
import cn.redcdn.hvs.udtroom.repository.RemoteDataSource;
import cn.redcdn.hvs.udtroom.util.DateUtils;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.NotificationUtil;
import cn.redcdn.jmeetingsdk.JMeetingAgent;
import cn.redcdn.log.CustomLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.OVER_SCROLL_NEVER;
import static cn.redcdn.hvs.MedicalApplication.getFileTaskManager;
import static cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity.END_DT_BROADCAST;
import static cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity.RECEIVER_END_DT_BROADCAST;

/**
 * 联合会诊室图文聊天 Fragment
 */
public class UDTChatFragment extends BaseFragment
    implements UDTChatListAdapter.CallbackInterface,
    UDTChatListAdapter.MeetingLinkClickListener
    , UDTGlobleData.DateChangeListener {

    private static final String TAG = UDTChatFragment.class.getSimpleName();

    // // 上一次查询的时间
    // 添加好友task
    // 群聊ID
    public static final String KEY_GROUP_ID = "KEY_GROUP_ID";

    // 消息转发，改走 本地照片分享逻辑
    public static final int ACTION_FORWARD_NOTICE = 1105;

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

    public static final String KEY_SERVICE_NUBE_INFO = "ServiceNubeInfo";
    //语音消息扬声器播放模式
    private static final boolean SPEAKER = true;

    //语音消息听筒播放模式
    private static final boolean HEADSET = false;
    /**
     * 添加为好友
     */

    // 界面类型(单聊，群聊)
    private int frameType = 2;
    // 当前会话ID(单聊,群聊)
    private String convstId = "";

    // 聊天类型(单聊，群聊)
    private int conversationType = VALUE_CONVERSATION_TYPE_MULTI;
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
    private UDTChatListAdapter chatAdapter = null;
    // Load ImageData
    private View headerLoadingView = null;

    private View headerRoot = null;

    private Handler mHandler = new Handler();
    // 自身视讯号
    private String selfNubeNumber = "";
    // 数据变更监听
    private UDTChatFragment.MyContentObserver observer = null;
    // 系统camera拍照或拍视频文件路径
    private String cameraFilePath = "";
    // 待转发的消息ID
    private String forwardNoticeId = null;
    // 官方帐号

    // 添加好友的nube号码
    private String addFriendNube = "";

    // 输入区域
    private UDTChatInputFragment inputFragment;
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
    private UDTChatFragment.GroupMemberObserver observeGroupMember;

    // 群成员表监听器
    private UDTChatFragment.GroupObserver groupObserve;

    private UDTChatFragment.FriendRelationObserver observeFriendRelation;
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

    private DtNoticesDao dtNoticeDao = null;
    private ThreadsDao threadDao = null;
    private GroupDao groupDao = null;

    //多选操作模式下的相关widget和数据对象
    private RelativeLayout moreOpLayout = null;
    private ImageButton forwardBtn = null;
    private ImageButton collectBtn = null;
    private ImageButton delBtn = null;

    private TextView newNoticeNum;

    private boolean titlebackbtn = false;
    private Button backbtn;
    private TextView backtext;
    private LinearLayout chatlayout;
    private LinearLayout addMeetinglayout;
    private TextView addMeetingThemeTxt;

    //修改后的群名称
    // private String nameForModify = "";
    private Boolean newNoticeNumflag = false;

    private boolean isPlaying = false;
    //初始化屏幕亮暗帮助类
    private WakeLockHelper helper;
    //初始化
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
    private BroadcastReceiver netReceiver;
    private UDTChatFragment fragmentContext;
    private BroadcastReceiver dtEndBroadcastReceiver;
    private LinearLayout chatEmptyView;

    private UDTGlobleData mUDTGlobleData;


    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.i(TAG, "onCreate()");
        fragmentContext = this;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        CustomLog.i(TAG, "onCreateView()");

        View contentView = inflater.inflate(R.layout.fragment_udtchat_layout, container, false);

        convstId = mUDTGlobleData.getDTGroupID();
        groupId = convstId;

        targetNubeNumber = convstId;

        initEnvironment(contentView);
        initMoreOpWidget(contentView);
        initCommonWidget(contentView);
        initView(contentView);

        return contentView;

    }


    private void initEnvironment(View v) {
        CustomLog.i(TAG, "initEnvironment()");

        dtNoticeDao = new DtNoticesDao(getActivity());
        threadDao = new ThreadsDao(getActivity());
        groupDao = new GroupDao(getActivity());

        inputFragment = new UDTChatInputFragment();
        headerLoadingView = getActivity().getLayoutInflater().inflate(
            R.layout.page_load_header, null);
        headerRoot = headerLoadingView.findViewById(R.id.header_root);
        selfNubeNumber = AccountManager.getInstance(getActivity()).getAccountInfo().nube;
        helper = new WakeLockHelper();

    }


    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CustomLog.i(TAG, "onActivityCreated");

        if (savedInstanceState == null) {
            frameType = 2;
            back_flag = true;
            if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST) {
                // 群聊类型，配置初始化参数

                conversationType = VALUE_NOTICE_FRAME_TYPE_LIST;
                targetShortName = null;
                convstExtInfo = "{\"draftText\":\"\"}";
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
    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }


    @Override protected void setListener() {

    }


    @Override protected void initData() {

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
    public void onStart() {
        super.onStart();
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            initReceiver();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        CustomLog.i(TAG, "onResume()");

        CommonUtil.hideSoftInputFromWindow(getActivity());
        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            dateList.clear();

            if (!TextUtils.isEmpty(groupId)) {
                dateList.putAll(groupDao.queryGroupMembers(groupId));

                String value = MedicalApplication.getPreference()
                    .getKeyValue(DaoPreference.PrefType.KEY_CHAT_REMIND_LIST, "");
                if (value != null && value.contains(groupId)) {
                    value = value.replace(groupId + ";", "");
                    MedicalApplication.getPreference().setKeyValue(
                        DaoPreference.PrefType.KEY_CHAT_REMIND_LIST, value);
                }
            }
        }
        isSelectReceiver = false;
        cancelNotifacation();

    }


    @Override
    public void onStop() {
        super.onStop();
        cleanCheckData();
        removeLoadingView();

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
        CustomLog.d(TAG, "onDestory()");
        super.onDestroy();
        cleanCheckData();
        if (observer != null) {
            getActivity().getContentResolver().unregisterContentObserver(observer);
            observer = null;
        }

        if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
            String value = MedicalApplication.getPreference().getKeyValue(
                DaoPreference.PrefType.KEY_CHAT_REMIND_LIST, "");
            if (value != null && value.contains(groupId)) {
                value = value.replace(groupId + ";", "");
                MedicalApplication.getPreference().setKeyValue(
                    DaoPreference.PrefType.KEY_CHAT_REMIND_LIST, value);
            }
        }

        if (observeGroupMember != null) {
            getActivity().getContentResolver().unregisterContentObserver(observeGroupMember);
            observeGroupMember = null;
        }

        if (groupObserve != null) {
            getActivity().getContentResolver().unregisterContentObserver(groupObserve);
            groupObserve = null;
        }

        if (chatAdapter != null) {
            chatAdapter.onDestroy();
            chatAdapter = null;
        }

        if (observeFriendRelation != null) {
            getActivity().getContentResolver().unregisterContentObserver(observeFriendRelation);
            observeFriendRelation = null;
        }

        if (searchUsers != null) {
            searchUsers.cancel();
        }
        if (netReceiver != null) {
            getActivity().unregisterReceiver(netReceiver);
        }

        if (dtEndBroadcastReceiver != null) {
            getActivity().unregisterReceiver(dtEndBroadcastReceiver);
        }

        mUDTGlobleData.removeListener(this);

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


    /**
     * 分页查询，必须一次查询完成后才能开始下次查询，所以队列中只能保存一个类型
     * 而范围查询，只要起始时间改变了，就需要重新查询
     */
    private void queryNoticeData(int queryType) {

        // 接诊后第一次进入图文会诊界面，由于 dt_notice 数据库为空，处理为不查询数据
        if (TextUtils.isEmpty(groupId)) {
            return;
        }

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
        }
    }


    // 线程安全的并发查询队列
    private List<String> queryList = new CopyOnWriteArrayList<String>();
    // 查询数据Runnable
    private UDTChatFragment.QueryRunnable queryRunnable = null;


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
                    Intent i = new Intent(getActivity(), ReserveSuccessActivity.class);
                    i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, exInfo);
                    getActivity().startActivity(i);
                } else {
                    int isSuccess = MedicalMeetingManage.getInstance().joinMeeting(meetingId,
                        new MedicalMeetingManage.OnJoinMeetingListener() {
                            @Override
                            public void onJoinMeeting(String valueDes, int valueCode) {
                                if (valueCode < 0) {
                                    CustomToast.show(getActivity(),
                                        getString(R.string.join_consultation_fail), 1);
                                }
                            }
                        });

                    if (isSuccess == 0) {

                    } else if (isSuccess == -9992) {
                        CustomToast.show(getActivity(), getString(R.string.login_checkNetworkError),
                            1);
                    } else {
                        CustomToast.show(getActivity(), getString(R.string.join_consultation_fail),
                            1);
                    }
                }
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                removeLoadingView();
                CustomToast.show(getActivity(), getString(R.string.get_meeting_info_fail),
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
            CustomToast.show(getActivity(), getString(R.string.get_meeting_info_fail),
                CustomToast.LENGTH_SHORT);
        }
    }


    /**
     * 监听外部会诊状态变化
     */
    @Override public void onDateChanged() {
        CustomLog.i(TAG, "onDateChanged()");
        int DTState = mUDTGlobleData.getState();

        if (DTState == HPUCommonCode.SEEK_STATE_NOW) {
            setRemoteGroupID(mUDTGlobleData.getDTId());
            CustomLog.i(TAG, "groupID from dataChanged =  " + groupId);
        }

    }


    private void setRemoteGroupID(final String dtId) {
        CustomLog.i(TAG, "getRemoteGroupID()");

        new RemoteDataSource().getRemoteCSLRoomDetailData(
            AccountManager.getInstance(getActivity()).getMdsToken(), dtId,
            new RemoteDataSource.DataCallback() {
                @Override public void onSuccess(CSLRoomDetailInfo data) {
                    CustomLog.i(TAG, "RemoteDataSource getGroupID success");
                    groupId = data.groupId;

                    CustomLog.i(TAG, "current group ID : " + groupId);

                    if (!groupId.equals("")) {
                        // 显示输入框
                        getChildFragmentManager().beginTransaction()
                            .replace(R.id.udt_input_line, inputFragment).commit();
                    }
                }


                @Override public void onFailed(int statusCode, String statusInfo) {
                    CustomLog.e(TAG, "groupID 未获取到" + statusInfo + " | " + statusCode);
                    CustomToast.show(getActivity(), "图文会诊初始化失败，请稍后重试", CustomToast.LENGTH_LONG);
                }
            });
    }


    /**
     * 设置 UDT 全局数据
     */
    public void setUDTGlobleData(UDTGlobleData data) {
        CustomLog.i(TAG, "setUDTGlobleData()");
        CustomLog.i(TAG, "groupID from OutSide =  " + data.getDTGroupID());
        if (data.getDTGroupID().equals("")) {
            CustomToast.show(getActivity(), "会诊室初始化失败，请稍后重试", CustomToast.LENGTH_LONG);
        }

        this.mUDTGlobleData = data;
        mUDTGlobleData.addListener(this);
    }


    class QueryRunnable implements Runnable {

        public int queryType = 0;
        public long recvTimeBg = 0l;


        public void run() {
            CustomLog.i(TAG, "QueryRunnable :: run()");

            CustomLog.d(TAG, "查询会话消息:" + convstId);
            if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                convstId = groupId;
            }
            // 查询动态数据
            QueryDTNoticeAsyncTask task = new QueryDTNoticeAsyncTask(
                getActivity(), convstId, queryType, getRecvTimeBegin(),
                IMConstant.NOTICE_PAGE_CNT);
            task.setQueryTaskListener(new QueryDTNoticeAsyncTask.QueryTaskPostListener() {
                @Override
                public void onQuerySuccess(Cursor cursor) {
                    CustomLog.d(TAG, "QueryDTNoticeAsyncTask onQuerySuccess");
                    inputFragment.setVoiceInfo(voiceTipView, cursor);
                    if (chatAdapter != null) {

                        if (cursor.getCount() == 0) {
                            CustomLog.i(TAG, "NoticeList no data");
                            // 没有查询到数据
                            headerRoot.setPadding(0,
                                0, 0, 0);
                            headerRoot.setVisibility(View.INVISIBLE);
                            chatEmptyView.setVisibility(View.VISIBLE); // 显示数据为空界面
                        } else {
                            // 查询到了数据
                            CustomLog.i(TAG, "NoticeList data Found");

                            if (queryType == QueryDTNoticeAsyncTask.QUERY_TYPE_PAGE) {
                                CustomLog.i(TAG, "QueryDTNoticeAsyncTask.QUERY_TYPE_PAGE");

                                chatEmptyView.setVisibility(View.GONE);
                                // 分页查询的场合
                                int pageCursorCnt = cursor.getCount();
                                CustomLog.d(TAG, "QueryDTNoticeAsyncTask:"
                                    + pageCursorCnt);
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

                                chatAdapter.mergeLastPageCursor(cursor);

                                if (pageCursorCnt > 0) {
                                    // 定位到最下面一条
                                    noticeListView.setSelection(pageCursorCnt);
                                }

                            } else if (queryType == QueryDTNoticeAsyncTask.QUERY_TYPE_COND) {
                                CustomLog.i(TAG, "QueryDTNoticeAsyncTask.QUERY_TYPE_COND");
                                headerRoot.setPadding(0,
                                    0, 0, 0);
                                headerRoot.setVisibility(View.INVISIBLE);
                                // 范围查询的场合
                                chatEmptyView.setVisibility(View.GONE);

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

                    }
                    // 初始化im面板
                    if (firstFlag) {
                        firstFlag = false;
                        if ((cursor == null || cursor.getCount() == 0)) {
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
                    CustomLog.d(TAG, "QueryDTNoticeAsyncTask onQueryFailure");
                    CustomToast.show(MedicalApplication.shareInstance(), R.string.load_msg_fail,
                        Toast.LENGTH_SHORT);
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


    private void initCommonWidget(View v) {
        noticeListView = (ListView) v.findViewById(R.id.notice_listview);
        chatlayout = (LinearLayout) v.findViewById(R.id.chat_linearlayout);
        backbtn = (Button) chatlayout.findViewById(R.id.back_btn);
        backtext = (TextView) chatlayout.findViewById(R.id.back_str);

        chatAdapter = new UDTChatListAdapter(getActivity(), null, dtNoticeDao,
            targetNubeNumber, targetShortName);
        chatAdapter.setDTGlobleData(mUDTGlobleData); //kevin add
        chatAdapter.setSelfNubeNumber(selfNubeNumber);
        chatAdapter.setCallbackInterface((UDTChatListAdapter.CallbackInterface) this);
        chatAdapter.setMeetingLinkClickListener(this);
        chatAdapter.setConversationType(conversationType);

        noticeListView.addHeaderView(headerLoadingView);
        noticeListView.setAdapter(chatAdapter);
        noticeListView.setOverScrollMode(OVER_SCROLL_NEVER);

        String nube = "";
        if (conversationType == VALUE_CONVERSATION_TYPE_SINGLE) {
            nube = targetNubeNumber;
        } else {
            nube = groupId;
        }
        CustomLog.d(TAG, nube + "");

        voiceTipView = new VoiceTipView(getActivity());
        inputFragment.mContext = getActivity();
        inputFragment.setNubeNum(nube);
        inputFragment.setListview(noticeListView);

        inputFragment.callback = new UDTChatInputFragment.SendCallbackInterface() {

            @Override
            public boolean onSendTxtMsg(final String txtMsg) {
                CustomLog.i(TAG, "onSendTxtMsg()");

                // 发送文字
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
                                        ShowNameUtil.NameElement element = ShowNameUtil
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

                            uuid = sendDTGroupIMMessage(txtMsgType, str);

                            selectNameList.clear();
                            selectNubeList.clear();
                        } else {
                            CustomLog.i(TAG, "VALUE_CONVERSATION_SINGLE");

                            // 插入发送记录
                            uuid = dtNoticeDao
                                .createSendFileNotice(
                                    selfNubeNumber,
                                    getReceivers(),
                                    null,
                                    "",
                                    FileTaskManager.NOTICE_TYPE_TXT_SEND,
                                    txtMsg, convstId, null);
                        }
                        getFileTaskManager()
                            .addDTTask(uuid, null);
                    }
                }).start();
                return true;
            }


            @Override
            public void onSendPic() {
                SendCIVMDTUtil.sendPic(fragmentContext);
            }


            @Override
            public void onSendPicFromCamera() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendPicFromCamera 不是好友");
                    return;
                }
                SendCIVMUtil.sendPicFromCamera(getActivity());
            }


            @Override
            public void onSendVideo() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendVideo 不是好友");
                    return;
                }
                SendCIVMUtil.sendVideo(getActivity());
            }


            @Override
            public void onSendVcard() {
                if (!isFriend()) {
                    CustomLog.d(TAG, targetShortName + "onSendVcard 不是好友");
                    return;
                }
                SendCIVMUtil.sendVcard(getActivity());
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
                            uuid = dtNoticeDao
                                .createSendFileNotice(
                                    selfNubeNumber,
                                    groupId,
                                    localFiles,
                                    "",
                                    FileTaskManager.NOTICE_TYPE_AUDIO_SEND,
                                    "", groupId, extInfo);
                        } else {
                            // 插入发送记录
                            uuid = dtNoticeDao
                                .createSendFileNotice(
                                    selfNubeNumber,
                                    getReceivers(),
                                    localFiles,
                                    "",
                                    FileTaskManager.NOTICE_TYPE_AUDIO_SEND,
                                    "", convstId, extInfo);
                        }
                        getFileTaskManager()
                            .addDTTask(uuid, null);
                    }
                }).start();
            }


            @Override
            public void onSelectGroupMemeber() {
                CustomLog.d(TAG, "onSelectGroupMemeber 选择回复的人");
                // 选择回复的人
                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    Intent intent = new Intent(getActivity(),
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
                if (receiver.length() == 0) {
                    CustomLog.d(TAG, "收藏的received 为空字符串");
                    return;
                }
                Intent intent = new Intent(getActivity(), CollectionActivity.class);
                intent.putExtra(CollectionActivity.KEY_RECEIVER, receiver);

                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    // 群成员人数
                    groupMemberSize = groupDao.queryGroupMemberCnt(targetNubeNumber);
                    intent.putExtra("headUrl", groupDao.getGroupHeadUrl(targetNubeNumber));
                    intent.putExtra("chatNames", groupDao.getGroupNameByGid(targetNubeNumber));
                    intent.putExtra("chatNumber", groupMemberSize);
                    intent.putExtra("chatType", "group");

                } else {
                    Contact contact = ContactManager.getInstance(getActivity())
                        .getContactInfoByNubeNumber(targetNubeNumber);
                    intent.putExtra("headUrl", contact.getHeadUrl());
                    intent.putExtra("chatNames", targetShortName);
                    intent.putExtra("chatType", "single");
                }
                getActivity().startActivity(intent);
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
                        queryNoticeData(QueryDTNoticeAsyncTask.QUERY_TYPE_ALL);
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
                CommonUtil.hideSoftInputFromWindow(getActivity());
                inputFragment.setHide();
                return false;
            }
        });

    }


    /**
     * 发送群组会诊消息
     */
    private String sendDTGroupIMMessage(int txtMsgType, String groupMsg) {
        CustomLog.i(TAG, "sendDTGroupIMMessage()");

        String uuid;
        String dtExtendContent = "";

        JSONObject dtExtendInfo = new JSONObject();
        try {
            dtExtendInfo.put("medicalComboMsg", 1);
            dtExtendContent = dtExtendInfo.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        uuid = dtNoticeDao
            .createSendFileNotice(
                selfNubeNumber,
                groupId,
                null,
                "",
                txtMsgType,
                groupMsg, groupId, dtExtendContent);
        return uuid;
    }


    public void initView(View v) {

        addMeetinglayout = (LinearLayout) v.findViewById(R.id.add_meeting_line);
        addMeetinglayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtil.hideSoftInputFromWindow(getActivity());
            }
        });
        addMeetingThemeTxt = (TextView) v.findViewById(R.id.add_meeting_theme);
        chatEmptyView = (LinearLayout) v.findViewById(R.id.chat_empty_layout);

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
                    getChildFragmentManager().beginTransaction()
                        .remove(inputFragment).commit();
                    targetShortName = getString(R.string.str_butel_name);
                    convstExtInfo = "";

                } else if (SettingData.getInstance().adminNubeNum.equals(targetNubeNumber)) {
                    inputFragment.isShowing = false;
                    getChildFragmentManager().beginTransaction()
                        .remove(inputFragment).commit();
                    targetShortName = getAdminNickName();
                    convstExtInfo = "";
                } else if (conversationType == VALUE_CONVERSATION_TYPE_SINGLE) {
                    // 单人聊天
                    inputFragment.isShowing = true;
                    getChildFragmentManager().beginTransaction()
                        .replace(R.id.udt_input_line, inputFragment).commit();

                    //如果是系统 Nube 不显示个人详情页按钮
                    if (!targetNubeNumber.equals(SYS_NUBE)
                        || !targetNubeNumber.equals(SettingData.getInstance().adminNubeNum)) {

                    }

                    //单人聊天时，监听好友关系表
                    if (observeFriendRelation == null) {
                        observeFriendRelation = new FriendRelationObserver();
                        getActivity().getContentResolver().registerContentObserver(
                            ProviderConstant.Friend_Relation_URI, true,
                            observeFriendRelation);
                    }

                } else if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    // 群发
                    inputFragment.isShowing = true;
                    int DTState = mUDTGlobleData.getState();
                    if (DTState != HPUCommonCode.SEEK_STATE_END &&
                        !groupId.equals("")) {
                        getChildFragmentManager().beginTransaction()
                            .replace(R.id.udt_input_line, inputFragment).commit();
                    }

                    // 群发名称
                    chatAdapter.setNoticeType(conversationType, groupId);
                    targetShortName = getGroupNameTitle();
                    if (observeGroupMember == null) {
                        observeGroupMember = new GroupMemberObserver();
                        getActivity().getContentResolver().registerContentObserver(
                            ProviderConstant.NETPHONE_GROUP_URI, true,
                            observeGroupMember);
                    }

                    if (groupObserve == null) {
                        groupObserve = new GroupObserver();
                        getActivity().getContentResolver().registerContentObserver(
                            ProviderConstant.NETPHONE_GROUP_MEMBER_URI, true,
                            groupObserve);
                    }
                    if (!isGroupMember) {
                    } else {

                    }
                }

                // 草稿文字填充
                initDraftText();

                if (observer == null) {
                    observer = new MyContentObserver();
                    getActivity().getContentResolver().registerContentObserver(
                        ProviderConstant.NETPHONE_HPU_NOTICE_URI, true, observer);
                }

                if (frameType == VALUE_NOTICE_FRAME_TYPE_LIST) {
                    // 初始化
                    SoftInput = false;
                    firstFlag = true;
                    setRecvTimeBegin(0);
                    if (chatAdapter != null) {
                        chatAdapter.clearData();
                    }

                    // 查询聊天数据
                    queryNoticeData(QueryDTNoticeAsyncTask.QUERY_TYPE_ALL);

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

        chatEmptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // 隐藏输入法及素材面板
                CommonUtil.hideSoftInputFromWindow(getActivity());
                inputFragment.setHide();
                return false;
            }

        });
    }


    private void cancelNotifacation() {
        CustomLog.i(TAG, "cancelNotifacation()");
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
        CustomLog.i(TAG, "initDraftText()");

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
                ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(
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


    private class MyContentObserver extends ContentObserver {
        public MyContentObserver() {
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
                queryNoticeData(QueryDTNoticeAsyncTask.QUERY_TYPE_COND);
            } else if (mergeInt == 2) {
                queryNoticeData(QueryDTNoticeAsyncTask.QUERY_TYPE_ALL);
            }
        }
    }


    private synchronized int mergeThreads() {
        CustomLog.i(TAG, "mergeThreads()");

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
    public void onActivityResult(int requestCode, int resultCode,
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
                        SendCIVMUtil.onSendVideoFromCameraBack(getActivity(), data,
                            selfNubeNumber, groupId, groupId);
                    } else if (data.getExtras().getInt("OK_TYPE", 0) == 0) {
                        SendCIVMUtil.onSendPicFromCameraBack(getActivity(), selfNubeNumber,
                            groupId, groupId);
                    } else {
                    }
                } else {
                    if (data.getExtras().getInt("OK_TYPE", 0) == 1) {
                        SendCIVMUtil.onSendVideoFromCameraBack(getActivity(), data,
                            selfNubeNumber, getReceivers(), convstId);
                    } else if (data.getExtras().getInt("OK_TYPE", 0) == 0) {
                        SendCIVMUtil.onSendPicFromCameraBack(getActivity(), selfNubeNumber,
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
                        SendCIVMUtil.onSendVideoFromNativeBack(getActivity(), data,
                            selfNubeNumber, groupId, groupId);
                    } else {
                        SendCIVMUtil.onSendVideoFromNativeBack(getActivity(), data,
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
                        SendCIVMDTUtil.onSendPicFromNativeBack(getActivity(), intent1,
                            selfNubeNumber, groupId, groupId);
                        SendCIVMUtil.onSendVideoFromNativeBack(getActivity(), intent2,
                            selfNubeNumber, groupId, groupId);
                    } else {
                        SendCIVMUtil.onSendPicFromNativeBack(getActivity(), intent1,
                            selfNubeNumber, getReceivers(), convstId);
                        SendCIVMUtil.onSendVideoFromNativeBack(getActivity(), intent2,
                            selfNubeNumber, getReceivers(), convstId);
                    }
                } else {
                    if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                        SendCIVMUtil.onSendPicFromNativeBack(getActivity(), data,
                            selfNubeNumber, groupId, groupId);
                    } else {
                        SendCIVMUtil.onSendPicFromNativeBack(getActivity(), data,
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
                    SendCIVMUtil.onSendVideoFromNativeBack(getActivity(), data,
                        selfNubeNumber, groupId, groupId);
                } else {
                    SendCIVMUtil.onSendVideoFromNativeBack(getActivity(), data,
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
                ShowNameUtil.NameElement element = ShowNameUtil.getNameElement(name, niName,
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
        CustomLog.i(TAG, "sendVcardBack()");

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

        ContactFriendBean Info = new NetPhoneDaoImpl(getActivity())
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
        CustomLog.d(TAG, "showSendVcardDialog()");

        LayoutInflater inflater = LayoutInflater
            .from(getActivity());
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
            Contact contact = ContactManager.getInstance(getActivity())
                .getContactInfoByNubeNumber(targetNubeNumber);
            headUrl = contact.getHeadUrl();
            Glide.with(this).load(headUrl).placeholder(R.drawable.default_head_image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(icon.shareImageview);
            nameView.setText(targetShortName);
            numView.setVisibility(View.GONE);
        }

        CommonDialog conDlg = new CommonDialog(getActivity(),
            getActivity().getLocalClassName(), 300);
        conDlg.addView(selfView);
        conDlg.setTitleVisible(getString(R.string.send_to));
        conDlg.setTransmitCardInfo(bean.getNickname(), Integer.parseInt(bean.getNubeNumber()));

        conDlg.setCancleButton(null, R.string.btn_cancle);
        conDlg.setPositiveButton(new CommonDialog.BtnClickedListener() {
            @Override
            public void onBtnClicked() {
                CustomLog.d(TAG, "点击确定");
                if (conversationType == VALUE_CONVERSATION_TYPE_MULTI) {
                    SendCIVMUtil.onSendVcardBack(getActivity(), bean,
                        selfNubeNumber, groupId, groupId);
                } else {
                    SendCIVMUtil.onSendVcardBack(getActivity(), bean,
                        selfNubeNumber, getReceivers(), convstId);
                }
                CustomToast.show(getActivity(), getString(R.string.toast_sent), 1);
            }
        }, R.string.btn_send);

        if (!getActivity().isFinishing()) {
            conDlg.showDialog();
        }
    }


    private String getReceivers() {
        CustomLog.i(TAG, "getReceivers()");

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


    @Override
    public void onMsgDelete(String uuid, long receivedTime, int dataCnt) {
        CustomLog.i(TAG, "onMsgDelete");
        CustomLog.d(TAG, "删除消息:" + uuid);
        getFileTaskManager().cancelTask(uuid);
        if (dataCnt == 1) {
            // 最后一条消息删除时，需要更新会话表lastTime
            dtNoticeDao.deleteLastNotice(uuid, convstId, receivedTime);
        } else {
            dtNoticeDao.deleteNotice(uuid);
        }
    }


    @Override
    public void onMsgForward(String uuid, String sender, int msgType,
                             int msgStatus, String localPath) {}


    @Override
    public void onMsgForward(String uuid, String sender, int msgType, int msgStatus, String localPath, String txt, String vcardName, String vcardNumber, String creator, String meetingRoomId, String meetingTopic, String date, String hms) {}


    @Override
    public void onMoreClick(String uuid, int msgType, int msgStatus, boolean checked) {}


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
        CustomLog.i(TAG, "preCheckDataforDel()");

        Map<String, NoticesBean> uidMap = null;
        if (chatAdapter != null) {
            uidMap = chatAdapter.getCheckedDataMap();
        }
        if (uidMap != null) {
            List<String> validList = new ArrayList<String>();
            for (String key : uidMap.keySet()) {
                validList.add(key);
                CustomLog.d(TAG, "uid = " + key);
            }

            delDialog(validList, chatAdapter.getCount());
        }
    }


    private void invalidCollectDialog(final List<NoticesBean> dataList) {
        CommonDialog confDlg = new CommonDialog(getActivity(),
            getActivity().getLocalClassName(), 12351);
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

        if (!getActivity().isFinishing()) {
            confDlg.showDialog();
        }
    }


    private void delDialog(final List<String> uidList, int listCount) {
        final MedicalAlertDialog menuDlg = new MedicalAlertDialog(getActivity());
        menuDlg.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                CustomLog.d(TAG, "批量删除所选消息");
                dtNoticeDao.deleteNotices(uidList);
                cleanCheckData();
            }
        }, getActivity().getString(R.string.chat_delete));
        menuDlg.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                menuDlg.dismiss();
            }
        }, getActivity().getString(R.string.btn_cancle));
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
                        CustomLog.e(TAG, "收藏聊天记录，解析json出错");
                    }
                }
            }
            CollectionManager.getInstance().addCollectionByNoticesBeans(dataList);
            Toast.makeText(getActivity(), R.string.have_collected,
                Toast.LENGTH_SHORT).show();
        }
        cleanCheckData();
    }


    private void cleanCheckData() {
        if (chatAdapter != null) {
            chatAdapter.cleanCheckedData();
        }
    }


    private void initMoreOpWidget(View v) {

        moreOpLayout = (RelativeLayout) v.findViewById(R.id.more_op_layout);
        forwardBtn = (ImageButton) v.findViewById(R.id.chat_more_forward_btn);
        forwardBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {}
        });
        collectBtn = (ImageButton) v.findViewById(R.id.chat_more_collect_btn);
        collectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                preCheckDataforCollection();
            }
        });
        delBtn = (ImageButton) v.findViewById(R.id.chat_more_del_btn);
        delBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                preCheckDataforDel();
            }
        });
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
        }
    }


    private class GroupObserver extends ContentObserver {

        public GroupObserver() {
            super(new Handler());
        }


        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.i(TAG, "GroupObserver :: onChange()");
            CustomLog.d(TAG, "t_multi_chat_users 群成员表数据库数据发生变更");
        }
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
    public void reBookMeeting() {}


    @Override
    public void reCreatMeeting() {}


    @Override
    public void addNewFriend() {
        int relationCode = FriendsManager.getInstance()
            .getFriendRelationByNubeNumber(targetNubeNumber);
        if (relationCode == FriendsManager.RELATION_TYPE_BOTH) {
            //            CustomToast.show(getActivity(),targetShortName + "已经是您的好友",CustomToast.LENGTH_LONG);
            CustomToast.show(getActivity(), getString(R.string.have_been_friend),
                CustomToast.LENGTH_LONG);
            return;
        }
        Intent intent = new Intent(getActivity(), VerificationReplyDialog.class);
        intent.putExtra(VerificationReplyDialog.KEY_DIALOG_TYPE, 1);  // 0从名片页进入 1：从IM页面进入
        intent.putExtra("nubeNumber", targetNubeNumber);
        startActivityForResult(intent, ContactTransmitConfig.REQUEST_REPLY_CODE);
    }


    private boolean isFriend() {
        if (conversationType != VALUE_CONVERSATION_TYPE_MULTI) {
            if (ContactManager.getInstance(getActivity())
                .checkNubeIsCustomService(targetNubeNumber)) {
                CustomLog.d(TAG, targetNubeNumber + "是客服账号，可以直接发送消息");
                return true;
            }

            int relationCode = FriendsManager.getInstance()
                .getFriendRelationByNubeNumber(targetNubeNumber);
            if (relationCode != FriendsManager.RELATION_TYPE_BOTH) {
                String tmpStr = targetShortName
                    + getString(R.string.start_friend_approve_send_approve_request) +
                    getString(R.string.pass_request_chat_send_request);
                dtNoticeDao.createAddFriendTxt("", targetNubeNumber, "", null, "",
                    FileTaskManager.NOTICE_TYPE_DESCRIPTION, tmpStr, convstId, null
                    , System.currentTimeMillis() + "");
                return false;
            }
            return true;
        }

        return true;
    }


    private void sendAddNewFriendMsg(String msgContent) {
        MDSAccountInfo loginUserInfo = AccountManager.getInstance(getActivity()).getAccountInfo();
        MedicalApplication.getFileTaskManager().sendStrangerMsg(loginUserInfo.getNube(),
            targetNubeNumber, loginUserInfo.getNickName(),
            loginUserInfo.getHeadThumUrl(), msgContent, false);

        Contact contact = ContactManager.getInstance(getActivity())
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
            CustomLog.i(TAG, "FriendRelationObserver :: onChange()");
            CustomLog.d(TAG, "好友关系数据库数据发生变更");
        }

    }


    private String getAdminNickName() {
        SharedPreferences preferences = getActivity().
            getSharedPreferences(ChatActivity.KEY_SERVICE_NUBE_INFO, MODE_PRIVATE);
        return preferences.getString("USERNAME", "10000");
    }


    /**
     * 初始化广播接收器
     */
    private void initReceiver() {
        netReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // 判断当前网络状态
                if (intent.getIntExtra(NetWorkChangeReceiver.NET_TYPE, 0) == 0) {
                    CustomLog.d(TAG, "网络断开");
                } else {
                    CustomLog.d(TAG, "网络连接");
                }
            }
        };
        IntentFilter filter = new IntentFilter(NetWorkChangeReceiver.NET_CHANGE);
        getActivity().registerReceiver(netReceiver, filter);

        dtEndBroadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (action.equals(RECEIVER_END_DT_BROADCAST)) {
                    CustomLog.i(TAG, "receive end dt broadcast");
                    inputFragment.hideChatInputLayout();
                } else if (action.equals(END_DT_BROADCAST)) {
                    CustomLog.i(TAG, "receive end dt broadcast");
                    inputFragment.hideChatInputLayout();
                }

            }
        };
        IntentFilter filter2 = new IntentFilter(RECEIVER_END_DT_BROADCAST);
        IntentFilter filter3 = new IntentFilter(END_DT_BROADCAST);

        getActivity().registerReceiver(dtEndBroadcastReceiver, filter2);
        getActivity().registerReceiver(dtEndBroadcastReceiver, filter3);

    }


    /**
     * 到达了正确的接诊时间
     * 用例：预约时间早于等于服务器时间才认为可以接诊
     */
    private boolean arriveScheduleTime() {
        CustomLog.i(TAG, "arriveScheduleTime()");

        int scheduleTime = Integer.valueOf(mUDTGlobleData.getSchedulDate());
        String serverTimeStamp = mUDTGlobleData.getServerDate();
        int serverTime = Integer.valueOf(DateUtils.timeStamp2Date(serverTimeStamp));

        if (scheduleTime == serverTime) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isInputPanelVisible() {
        if (inputFragment != null) {
            return inputFragment.isPanelVisible();
        } else {
            return false;
        }
    }


    public void setInputPanelHide() {
        CustomLog.i(TAG, "setInputPanelHide()");

        if (inputFragment != null) {
            inputFragment.setHide();
        }
    }

}
