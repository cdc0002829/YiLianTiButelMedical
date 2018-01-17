package cn.redcdn.hvs.im.activity; /**
 * <dl>
 * <dt>GroupChatDetailActivity.java</dt>
 * <dd>Description:群聊信息页面</dd>
 * <dd>Copyright: Copyright (C) 2011</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2015-6-11 上午11:36:34</dd>
 * </dl>
 *
 * @author niuben
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.datacenter.medicalcenter.MDSAPPGetGroupCsl;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.medicalcenter.MDSUserGetGrougNotice;
import cn.redcdn.datacenter.medicalcenter.data.MDSGroupCslInfo;
import cn.redcdn.datacenter.medicalcenter.data.NoticeInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.ContactCardActivity;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.adapter.GroupChatMembersIconAdapter;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.bean.GroupBean;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.hvs.im.column.GroupMemberTable;
import cn.redcdn.hvs.im.column.ThreadsTable;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.fileTask.FileTaskManager;
import cn.redcdn.hvs.im.manager.GroupChatInterfaceManager;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;
import cn.redcdn.hvs.im.util.ListSort;
import cn.redcdn.hvs.im.util.PinyinUtil;
import cn.redcdn.hvs.im.view.BottomMenuWindow;
import cn.redcdn.hvs.im.view.CustomGridView;
import cn.redcdn.hvs.im.view.MedicalAlertDialog;
import cn.redcdn.hvs.im.view.SwitchButton;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import com.butel.connectevent.utils.LogUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.MedicalApplication.getFileTaskManager;

public class GroupChatDetailActivity extends BaseActivity implements OnClickListener
    ,
    SwitchButton.OnCheckedChangeListener,
    GroupChatMembersIconAdapter.OnGroupChatMembersIconListener,
    GroupChatInterfaceManager.GroupInterfaceListener,
    OnTouchListener {

    /**
     * 进入该页面必传参数
     *	Intent intent = new Intent(ChatActivity.this,GroupChatDetailActivity.class);
     * intent.putExtra(GroupChatDetailActivity.KEY_CHAT_TYPE,GroupChatDetailActivity.VALUE_SINGLE);
     * intent.putExtra(GroupChatDetailActivity.KEY_NUMBER,targetNubeNumber);
     */
    public static final String KEY_NUMBER = "NUMBER_ID";
    public static final String KEY_CHAT_TYPE = "KEY_CHAT_TYPE";
    public static final String KEY_GETGROUPCSLFAIL= "KEY_GETGROUPCSLFAIL";
    public static final String KEY_MEETINGNUB = "MEETINGNUB";
    public static final String KEY_MEETINGTHEME = "MEETINGTHEME";
    public static final String KEY_MEETINGPASSWORD = "MEETINGPASSWORD";

    public static final String VALUE_SINGLE = "SINGLE";
    public static final String VALUE_GROUP = "GROUP";
    public static final String VALUE_GETGROUPCSLFAIL = "GETGROUPCSLFAIL";
    public static final String VALUE_GETGROUPCSLSUCCESS = "GETGROUPCSLSUCCESS";

    private boolean Conversation = true;  //与当前联系人没有会话消息时，设置不可点击 SwitchButton

    TitleBar titleBar;
    /**
     * 数据
     */
    private CustomGridView myGridView;
    //	private ImageFetcher mImageFetcher = null;	//加载头像
    private GroupChatMembersIconAdapter adapter;
    private LinkedHashMap<String, GroupMemberBean> dateList
        = new LinkedHashMap<String, GroupMemberBean>();//显示数据

    private String mChatType;
    private String mNumberId = null;
    private String mMeetingNub = "";
    private String mMeetingPassword = "";
    private String mMeetingTheme = "";
    private String mGetGroupCslFail;

    private GroupBean mGroupBean;
    private GroupDao mGroupDao;
    private NoticesDao mNoticesDao;
    private ThreadsDao mThreadsDao;
    private ThreadsBean currThreadBean;
    private String currChatId; //当前的会话id

    private RelativeLayout groupName_RelativeLayout, groupQRCodeLayout;
    private RelativeLayout groupAnnouncementLayout;
    private RelativeLayout groupMeetingLayout;
    private TextView showGroupName;
    private SwitchButton dontDisturb_QnSwitch, msgSetTop_QnSwitch, saveContact_QNSwitch;
    private RelativeLayout clearGroupChatMessage_RelativeLayout;
    private Button quit_Btn;

    private LinearLayout groupChatDetails1;
    private LinearLayout saveToContactLayout;

    private static String KEY_DONT_DISTURB = "dont_disturb";
    private static String KEY_NUBE_NUMBER = "nube_number";
    private static String KEY_IS_THREAD_TOP = "thread_top_flag";
    private static String KEY_SAVE_GROUP_TO_CONTACT = "save_group_to_contact";

    //	private View textEntryView;//编辑群聊名称Dialog
    //	private EditText editGroupName;
    //	private ImageView iv_delete;
    private View lineview1;

    public final static int EDIT_NAME = 0;//编辑群名称
    public final static int CLEAR_MESSAGE = 1;//清空消息
    public final static int QUIT_BY_SELF = 2;//自己退群
    public final static int QUIT_BY_LARDER = 3;//移除成员
    public final static int ADD_BY_OTHER = 4;//添加成员
    public final static int REMOVED_LOADER_TO_OTHER = 5;//移交群主权限给群成员
    public final static int DONT_DISTURB = 6;//免打扰设置
    public final static int JUMP_TO_CONTACTDETAIL = 7;//跳转到联系人详情页面
    public final static int CREAT_GROUP = 8;//创建群
    public final static int QUERY_GROUP_DETAIL = 9;//查询群详情

    public final static int SET_MSG_TOP = 10;
    public final static int SAVE_TO_CONTACT = 11;

    public String nextLoaderNube = "";//移交群主给群成员的nube号

    private GroupChatInterfaceManager mInterfaceManager = null;

    private final static int ACTIVITY_RESULT_REMOVED_LOADER_TO_OTHER = 100;
    private final static int ACTIVITY_RESULT_ADD_BY_OTHER = 101;
    private final static int ACTIVITY_RESULT_MODIFY_GROUP_NAME = 200;
    private final static int ACTIVITY_GROUP_ANNOUNCEMENT = 201;
    private final static int ACTIVITY_GROUP_ANNOUNCEMENTEDIT = 202;
    private final static int ACTIVITY_GROUP_MEETING = 203;

    private boolean isAlreadySaveContact = false;

    private ContactManager mContactManage;
    private String loginNubeNum = "";
    private TextView groupAnnouncement, groupDefaultNotice,groupMeetingNub;
    private NoticeInfo groupAnnounceInfo;
    private boolean isExitGroup = false;//标识是否退出群聊，如果是，删除通讯录中的群聊，不做提示


    /**
     * 本页面所有需要交互的操作，均在此处理
     */
    private String nameFormModify = "";

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EDIT_NAME:
                    nameFormModify = msg.getData().getString(ModifyActivity.KEY_OF_VALUE).trim();
                    LogUtil.d("nameFormModify=" + nameFormModify);
                    String groupName = TextUtils.isEmpty(mGroupBean.getgName())
                                       ? ""
                                       : mGroupBean.getgName();
                    if (groupName.equals(nameFormModify)) {
                        LogUtil.d("群聊名称未变化，不需要调用接口");
                    } else {
                        LogUtil.d("调用异步接口，修改群名称");
                        if (mInterfaceManager == null) {
                            mInterfaceManager = new GroupChatInterfaceManager(
                                GroupChatDetailActivity.this, GroupChatDetailActivity.this);
                        }
                        showLoadingView(getString(R.string.changing_group_chat_name));
                        mInterfaceManager.editGroupInfo(nameFormModify, mNumberId);
                    }
                    break;
                case CLEAR_MESSAGE:
                    CustomLog.d(TAG, "清空聊天记录");
                    if (mChatType.equals(VALUE_SINGLE)) {
                        mNoticesDao.deleteAllNoticesInConversation(
                            mThreadsDao.getThreadByRecipentIds(mNumberId).getId());
                    } else {//群聊是的Threads==mNumberId
                        mNoticesDao.deleteAllNoticesInConversation(mNumberId);
                    }
                    CustomToast.show(GroupChatDetailActivity.this, getString(R.string.operation_success), 1);
                    break;
                case QUIT_BY_SELF:
                    LogUtil.d("调用异步接口，主动退出");
                    if (mInterfaceManager == null) {
                        mInterfaceManager = new GroupChatInterfaceManager(
                            GroupChatDetailActivity.this, GroupChatDetailActivity.this);
                    }
                    showLoadingView(getString(R.string.exitting_group_chat), new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            removeLoadingView();
                        }
                    });
                    mInterfaceManager.quiteGroup(mNumberId);
                    break;
                case QUIT_BY_LARDER:
                    LogUtil.d("调用异步接口，移除成员");
                    if (mInterfaceManager == null) {
                        mInterfaceManager = new GroupChatInterfaceManager(
                            GroupChatDetailActivity.this, GroupChatDetailActivity.this);
                    }
                    ArrayList<String> userList = new ArrayList<String>();
                    userList.add(msg.getData().getString(KEY_NUBE_NUMBER));
                    showLoadingView(getString(R.string.deleteing_group_people));
                    mInterfaceManager.delUser(mNumberId, userList);
                    break;
                case ADD_BY_OTHER:
                    LogUtil.d("调用异步接口，添加成员");
                    if (mInterfaceManager == null) {
                        mInterfaceManager = new GroupChatInterfaceManager(
                            GroupChatDetailActivity.this, GroupChatDetailActivity.this);
                    }
                    showLoadingView(getString(R.string.adding_group_people));
                    mInterfaceManager.addUser(mNumberId,
                        msg.getData().getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE));
                    break;
                case REMOVED_LOADER_TO_OTHER:
                    LogUtil.d("调用异步接口，移交群主权限给群成员，并退出群");
                    if (mInterfaceManager == null) {
                        mInterfaceManager = new GroupChatInterfaceManager(
                            GroupChatDetailActivity.this, GroupChatDetailActivity.this);
                    }
                    showLoadingView(getString(R.string.exitting_group_chat), new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            removeLoadingView();
                        }
                    });
                    mInterfaceManager.quiteGroup(mNumberId, nextLoaderNube);
                    break;
                case DONT_DISTURB:
                    boolean ischeced = msg.getData().getBoolean(KEY_DONT_DISTURB);
                    LogUtil.d("免打扰设置为：" + ischeced);
                    String value = MedicalApplication.getPreference()
                        .getKeyValue(PrefType.KEY_CHAT_DONT_DISTURB_LIST, "");
                    String distrubFlag = "0";
                    if (ischeced) {//免打扰
                        LogUtil.d("已设置免打扰，设置开关状态为true");
                        value = value + ";" + mNumberId + ";";
                        distrubFlag = ThreadsTable.DISTRUB_NO;
                    } else {
                        LogUtil.d("未设置免打扰，设置开关状态为false");
                        value = value.replace(";" + mNumberId + ";", "");
                        distrubFlag = ThreadsTable.DISTRUB_YES;
                    }
                    mThreadsDao.updateDoNotDisturb(currChatId, distrubFlag);
                    MedicalApplication.getPreference()
                        .setKeyValue(PrefType.KEY_CHAT_DONT_DISTURB_LIST, value);
                    break;
                case SET_MSG_TOP:
                    boolean isChecked = msg.getData().getBoolean(KEY_IS_THREAD_TOP);
                    CustomLog.d(TAG, "置顶状态为" + isChecked);
                    int topFlag = 0;
                    if (isChecked) {
                        CustomLog.d(TAG, "会话已设置为置顶状态为，开关状态为true");
                        topFlag = ThreadsTable.TOP_YES;
                    } else {
                        CustomLog.d(TAG, "会话未设置为置顶状态为，开关状态为false");
                        topFlag = ThreadsTable.TOP_NO;
                    }
                    mThreadsDao.updateTop(currChatId, topFlag);
                    break;
                case SAVE_TO_CONTACT:
                    boolean isSaveToContact = msg.getData().getBoolean(KEY_SAVE_GROUP_TO_CONTACT);
                    CustomLog.d(TAG, "保存到通讯录" + isSaveToContact);
                    if (isSaveToContact) {
                        CustomLog.d(TAG, "群组保存到通讯录，开关状态为true");
                        GroupChatDetailActivity.this.showLoadingView(getString(R.string.keeping_group));
                        String tempGroupName = "";
                        if (mGroupBean.getgName().length() > 0) {
                            tempGroupName = mGroupBean.getgName();
                        } else {
                            tempGroupName = mGroupDao.getGroupNameByGid(mGroupBean.getGid());
                        }
                        saveGroupToContact(mGroupBean.getGid(), tempGroupName,
                            mGroupBean.getHeadUrl());

                    } else {
                        CustomLog.d(TAG, "群组不保存到通讯录，开关状态为false");
                        GroupChatDetailActivity.this.showLoadingView(getString(R.string.cancel_keeping));
                        deleteGroupFromContact(mGroupBean.getGid());
                    }
                    break;

                case JUMP_TO_CONTACTDETAIL:
                    LogUtil.d("跳转到个人名片页面");
                    Intent intent = new Intent(GroupChatDetailActivity.this,
                        ContactCardActivity.class);
                    intent.putExtra("nubeNumber", msg.getData().getString(KEY_NUBE_NUMBER));
                    intent.putExtra("searchType", "5"); // 5:群内添加
                    if( mGroupBean != null && mGroupBean.getGid().length() > 0){
                        intent.putExtra("groupName",mGroupDao.getGroupNameByGid(mGroupBean.getGid()));
                    }
                    startActivity(intent);
                    break;
                case CREAT_GROUP:
                    LogUtil.d("调用异步接口，创建群");
                    if (mInterfaceManager == null) {
                        mInterfaceManager = new GroupChatInterfaceManager(
                            GroupChatDetailActivity.this, GroupChatDetailActivity.this);
                    }
                    ArrayList<String> nubes = msg.getData()
                        .getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE);
                    nubes.add(mNumberId);//返回的数据不包括原来的数据
                    showLoadingView(getString(R.string.createing_group));
                    mInterfaceManager.createGroup("", nubes);
                    break;
                case QUERY_GROUP_DETAIL:
                    LogUtil.d("调用异步接口，查询群详情");
                    if (mInterfaceManager == null) {
                        mInterfaceManager = new GroupChatInterfaceManager(
                            GroupChatDetailActivity.this, GroupChatDetailActivity.this);
                    }
                    showLoadingView(getString(R.string.querying_group_info), new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {

                            removeLoadingView();
                        }
                    });
                    mInterfaceManager.queryGroupDetail(mNumberId);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 保留群组到通讯录
     * @param groupId
     * @param groupName
     * @param headUrl
     */
    private void saveGroupToContact(String groupId, String groupName, String headUrl) {
        ContactManager.getInstance(GroupChatDetailActivity.this)
            .saveGroupToContacts(groupId, groupName, headUrl
                , new ContactCallback() {
                    @Override
                    public void onFinished(ResponseEntry result) {
                        GroupChatDetailActivity.this.removeLoadingView();
                        CustomLog.i(TAG, "onFinish! status: " + result.status
                            + " | content: " + result.content);
                        if (result.status >= 0) {
                            isAlreadySaveContact = true;
                            CustomLog.i(TAG, "addtoLocalContact success");
                            CustomToast.show(GroupChatDetailActivity.this, getString(R.string.add_success), 1);
                        } else {
                            CustomLog.i(TAG, "onFinish! status: " + result.status
                                + " | content: " + result.content);
                            CustomToast.show(GroupChatDetailActivity.this, getString(R.string.add_fail), 1);
                            saveContact_QNSwitch.setChecked(false);
                        }
                    }
                });
    }


    private void deleteGroupFromContact(String gid) {
        ContactManager.getInstance(GroupChatDetailActivity.this)
            .removeGroupFromContacts(gid,
                new ContactCallback() {
                    @Override
                    public void onFinished(ResponseEntry result) {
                        isAlreadySaveContact = false;
                        GroupChatDetailActivity.this.removeLoadingView();
                        CustomLog.i(TAG, "onFinish! status: " + result.status
                            + " | content: " + result.content);
                        if (result.status >= 0) {
                            CustomLog.d(TAG, "取消保存通讯录成功");
                            if (!isExitGroup) {
                                CustomToast.show(GroupChatDetailActivity.this, getString(R.string.cancel_save_contacts_success), 1);
                            }
                        } else {
                            CustomLog.d(TAG, "取消保存通讯录失败");
                            if (!isExitGroup) {
                                CustomToast.show(GroupChatDetailActivity.this, getString(R.string.cancel_save_contacts_fail), 1);
                            }
                            saveContact_QNSwitch.setChecked(true);
                        }
                    }

                });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate begin");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_chat_detial);
        initData();
        initView();
        getAnnouncement();
        CustomLog.d(TAG, "onCreate end");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Activity.RESULT_CANCELED == resultCode) {
            return;
        }
        if (intent == null) {
            CustomLog.d(TAG, "返回intent==null");
            return;
        }
        Bundle bundle = intent.getExtras();
        switch (requestCode) {
            case ACTIVITY_RESULT_MODIFY_GROUP_NAME:
                Message msg1 = new Message();
                msg1.what = EDIT_NAME;
                msg1.setData(bundle);
                mHandler.sendMessage(msg1);
                break;
            case ACTIVITY_RESULT_REMOVED_LOADER_TO_OTHER:
                if (bundle == null ||
                    bundle.getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE) == null
                    || bundle.getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE).size() !=
                    1) {
                    CustomLog.d(TAG, "移交群主，设置的是单选，返回的数据不合法");
                } else {
                    CustomLog.d(TAG, "移交群主返回数据合法");
                    nextLoaderNube = bundle.getStringArrayList(
                        SelectLinkManActivity.START_RESULT_NUBE).get(0);
                    showCommonDialog(REMOVED_LOADER_TO_OTHER);
                }
                break;
            case ACTIVITY_RESULT_ADD_BY_OTHER:
                if (bundle == null ||
                    bundle.getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE) == null) {
                    CustomLog.d(TAG, "添加联系人，设置的是多选，返回的数据为空");
                } else {
                    CustomLog.d(TAG, "添加联系人返回数据合法");
                    Message msg = new Message();
                    if (mChatType.equals(VALUE_SINGLE)) {
                        msg.what = CREAT_GROUP;
                    } else {
                        msg.what = ADD_BY_OTHER;
                    }
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                }
                break;
            case ACTIVITY_GROUP_ANNOUNCEMENT:
                CustomToast.show(GroupChatDetailActivity.this, getString(R.string.send_group_announcement), CustomToast.LENGTH_SHORT);
                String newAnnouncement = intent.getStringExtra(
                    GroupAnnouncementActivity.NEW_ANNOUNCEMENT);
                groupAnnouncement.setText(newAnnouncement);
                groupAnnouncement.setVisibility(View.VISIBLE);
                groupDefaultNotice.setVisibility(View.GONE);
                groupAnnounceInfo.setNoticeContent(newAnnouncement);
                //                    Date dt= new Date();
                //                    long time= dt.getTime();
                //                    groupAnnounceInfo.setCreateTime((int)time);
                sendGroupMsg(newAnnouncement);
                break;
            case ACTIVITY_GROUP_ANNOUNCEMENTEDIT:
                CustomToast.show(GroupChatDetailActivity.this, getString(R.string.send_group_announcement), CustomToast.LENGTH_SHORT);
                String newAnnouncementedit = intent.getStringExtra(
                    GroupAnnouncementActivity.NEW_ANNOUNCEMENT);
                groupAnnouncement.setText(newAnnouncementedit);
                groupAnnouncement.setVisibility(View.VISIBLE);
                groupDefaultNotice.setVisibility(View.GONE);
                sendGroupMsg(newAnnouncementedit);
                if (groupAnnounceInfo == null) {//解决：没有公告时候，发布公告回到群消息列表页面，再点击群公告的时候进入群公告崩溃的问题
                    getAnnouncement();
                } else {
                    groupAnnounceInfo.setNoticeContent(newAnnouncementedit);
                    Date dt = new Date();
                    long time = dt.getTime();
                    groupAnnounceInfo.setCreateTime(time);
                }

                break;
            case ACTIVITY_GROUP_MEETING:
                String meetingNub = intent.getStringExtra(GroupMeetingActivity.MEETING_NUB);
                String meetingTheme = intent.getStringExtra(GroupMeetingActivity.MEETING_THEME);
                String meetingPassword = intent.getStringExtra(GroupMeetingActivity.MEETING_PASSWORD);
                if (meetingNub.equals("")){
                    groupMeetingNub.setText(R.string.no_config);
                    mMeetingNub = "";
                }else {
                    groupMeetingNub.setText(meetingNub);
                    mMeetingNub = meetingNub;
                }
                mMeetingTheme = meetingTheme;
                mMeetingPassword = meetingPassword;
            default:
                break;
        }
    }


    private void initData() {
        LogUtil.begin("");
        mContactManage = ContactManager.getInstance(this);
        mGroupDao = new GroupDao(this);
        mNoticesDao = new NoticesDao(this);
        mThreadsDao = new ThreadsDao(this);
        mChatType = getIntent().getStringExtra(KEY_CHAT_TYPE);
        mNumberId = getIntent().getStringExtra(KEY_NUMBER);
        loginNubeNum = AccountManager.getInstance(MedicalApplication.getContext()).getNube();

        CustomLog.d(TAG, "KEY_CHAT_TYPE=" + mChatType + "|KEY_NUMBER=" + mNumberId);
        if (mChatType.equals(VALUE_GROUP)) {
            currChatId = mNumberId;
            Contact contact = ContactManager.getInstance(GroupChatDetailActivity.this)
                .getContactInfoByNubeNumber(currChatId);
            if(contact != null){
                if (contact.getContactId() != null && contact.getContactId().length() > 0) {
                    isAlreadySaveContact = true;
                } else {
                    isAlreadySaveContact = false;
                }
            }else{
                isAlreadySaveContact = false;
            }
            mHandler.sendEmptyMessage(QUERY_GROUP_DETAIL);
            mGetGroupCslFail = getIntent().getStringExtra(KEY_GETGROUPCSLFAIL);
            if (mGetGroupCslFail.equals(VALUE_GETGROUPCSLFAIL)) {
                MDSAPPGetGroupCsl mdsappGetGroupCsl = new MDSAPPGetGroupCsl() {
                    @Override
                    protected void onSuccess(MDSGroupCslInfo responseContent) {
                        super.onSuccess(responseContent);
                        mGetGroupCslFail = "";
                        CustomLog.d(TAG, "mdsappGetGroupCsl   onSuccess" + responseContent.cslRoomNo + responseContent.cslSubject);
                        mMeetingNub = responseContent.cslRoomNo;
                        groupMeetingNub.setText(mMeetingNub);
                        mGetGroupCslFail = GroupChatDetailActivity.VALUE_GETGROUPCSLSUCCESS;
                        if (responseContent.cslSubject != null) {
                            mMeetingTheme = responseContent.cslSubject;
                        }
                        if (responseContent.cslPassword != null) {
                            mMeetingPassword = responseContent.cslPassword;
                        }
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        mMeetingNub = "";
                        mMeetingPassword = "";
                        mMeetingTheme = "";
                        CustomLog.d(TAG, "mdsappGetGroupCsl   onFail" + statusInfo);
//                meetingNub = GroupChatDetailActivity.VALUE_MEETINGNUB;
                        if (statusCode != MDSErrorCode.MDS_NOTEXIT_GRPOUPCSL) {
//                        CustomToast.show(GroupChatDetailActivity.this,statusInfo,CustomToast.LENGTH_SHORT);
                            mGetGroupCslFail = GroupChatDetailActivity.VALUE_GETGROUPCSLFAIL;
                        } else {
                            mGetGroupCslFail = "";
                        }
                    }
                };
                CustomLog.d(TAG, "mdsappGetGroupCsl");
                mdsappGetGroupCsl.getGroupCsl(AccountManager.getInstance(this).getMdsToken(), mNumberId);
            } else if (mGetGroupCslFail.equals(VALUE_GETGROUPCSLSUCCESS)) {
                mMeetingNub = getIntent().getStringExtra(KEY_MEETINGNUB);
                mMeetingPassword = getIntent().getStringExtra(KEY_MEETINGPASSWORD);
                mMeetingTheme = getIntent().getStringExtra(KEY_MEETINGTHEME);
            }
        } else {

            ThreadsBean threadsBean = mThreadsDao.getThreadByRecipentIds(mNumberId);
            if (threadsBean == null) {
                currChatId = null;
                currThreadBean = null;
                CustomLog.d(TAG, "当前会话id为 null");

            } else {
                currChatId = threadsBean.getId();
                CustomLog.d(TAG, "当前会话id为" + currChatId);
            }


        }
        currThreadBean = mThreadsDao.getThreadById(currChatId);

        LogUtil.end("");
    }


    private void initView() {
        LogUtil.begin("");
        titleBar = getTitleBar();//标题栏
        titleBar.enableBack();
        lineview1 = findViewById(R.id.chat_name_line_bottom);
        adapter = new GroupChatMembersIconAdapter(this, this);
        //头像区域
        myGridView = (CustomGridView) findViewById(R.id.multi_icons_gridview);
        myGridView.setAdapter(adapter);
        myGridView.setOnClickListener(this);
        //群操作布局  群聊名称、群二维码、群公告
        groupChatDetails1 = (LinearLayout) findViewById(R.id.group_chat_details1);
        //群名称
        groupName_RelativeLayout = (RelativeLayout) findViewById(
            R.id.group_chat_name_relativelayout);
        groupName_RelativeLayout.setOnClickListener(this);
        groupName_RelativeLayout.setOnTouchListener(this);
        showGroupName = (TextView) findViewById(R.id.group_chat_name_textview);
        //群二维码
        groupQRCodeLayout = (RelativeLayout) findViewById(R.id.group_qrcode_layout);
        groupQRCodeLayout.setOnClickListener(this);
        groupQRCodeLayout.setOnTouchListener(this);
        //群公告
        groupAnnouncementLayout = (RelativeLayout) findViewById(R.id.group_accouncement_layout);
        groupAnnouncementLayout.setOnClickListener(this);
        groupAnnouncementLayout.setOnTouchListener(this);
        //群会诊
        groupMeetingLayout = (RelativeLayout) findViewById(R.id.group_meeting_layout);
        groupMeetingLayout.setOnClickListener(this);
        groupMeetingLayout.setOnTouchListener(this);

        //清空群记录
        clearGroupChatMessage_RelativeLayout = (RelativeLayout) findViewById(
            R.id.clear_group_chat_message_relativelayout);
        clearGroupChatMessage_RelativeLayout.setOnClickListener(this);
        clearGroupChatMessage_RelativeLayout.setOnTouchListener(this);
        //消息免打扰
        dontDisturb_QnSwitch = (SwitchButton) findViewById(R.id.dont_disturb_switch);
        dontDisturb_QnSwitch.setOnBackgroundResource(R.drawable.switch_on);
        dontDisturb_QnSwitch.setOffBackgroundResource(R.drawable.switch_off);
        dontDisturb_QnSwitch.setOnCheckedChangeListener(this);

        //消息置顶
        msgSetTop_QnSwitch = (SwitchButton) findViewById(R.id.msg_set_top_switch);
        msgSetTop_QnSwitch.setOnBackgroundResource(R.drawable.switch_on);
        msgSetTop_QnSwitch.setOffBackgroundResource(R.drawable.switch_off);
        msgSetTop_QnSwitch.setOnCheckedChangeListener(this);

        //保存到通讯录
        saveContact_QNSwitch = (SwitchButton) findViewById(R.id.save_conteact_switch);
        saveContact_QNSwitch.setOnBackgroundResource(R.drawable.switch_on);
        saveContact_QNSwitch.setOffBackgroundResource(R.drawable.switch_off);
        saveContact_QNSwitch.setOnCheckedChangeListener(this);

        if (MedicalApplication.getPreference()
            .getKeyValue(PrefType.KEY_CHAT_DONT_DISTURB_LIST, "")
            .indexOf(";" + mNumberId + ";") >= 0) {
            CustomLog.d(TAG, "已设置免打扰，设置开关状态为true");
            dontDisturb_QnSwitch.setChecked(true);
        } else {
            CustomLog.d(TAG, "未设置免打扰，设置开关状态为false");
            dontDisturb_QnSwitch.setChecked(false);
        }

        if (currThreadBean == null) {
            CustomLog.d(TAG, "currThreadBean是空");
            if (mChatType.equals(VALUE_SINGLE)){
                Conversation = false;
            }
        } else {
            if (currThreadBean.getTop() == 0) {           //设置置顶聊天
                msgSetTop_QnSwitch.setChecked(false);
            } else {
                msgSetTop_QnSwitch.setChecked(true);
            }
        }

        if (isAlreadySaveContact) {
            saveContact_QNSwitch.setChecked(true);
        } else {
            saveContact_QNSwitch.setChecked(false);
        }

        //是否保存到通讯录
        saveToContactLayout = (LinearLayout) findViewById(R.id.save_to_conteact_layout);
        //退出群聊
        quit_Btn = (Button) findViewById(R.id.quit_group_chat_btn);
        quit_Btn.setOnClickListener(this);

        groupAnnouncement = (TextView) findViewById(R.id.tv_group_announce);
        groupDefaultNotice = (TextView) findViewById(R.id.default_notice_tv);
        groupMeetingNub = (TextView) findViewById(R.id.default_group_meeting);
        if ((!mMeetingNub.equals("")) && mGetGroupCslFail.equals(VALUE_GETGROUPCSLSUCCESS)){
            groupMeetingNub.setText(mMeetingNub);
        }
        LogUtil.end("");
    }


    private void showCommonDialog(final int type) {
//        final CommonDialog mDialog = new CommonDialog(this, getLocalClassName(), 414);
        final CustomDialog mDialog=new CustomDialog(GroupChatDetailActivity.this);
        String title = getResources().getString(R.string.reminder);
        String cancel = getResources().getString(R.string.cancel_message);
        String positive = getResources().getString(R.string.confirm_message);
        boolean stillShow = false;
        switch (type) {
            case CLEAR_MESSAGE:
                CustomLog.d(TAG, "弹出清空聊天记录对话框");
                title = getString(R.string.clear_chat_history);
                mDialog.setTip(getString(R.string.clear_chat_record));
                break;
            case QUIT_BY_SELF:
                CustomLog.d(TAG, "弹出退出群聊对话框");
                title = getString(R.string.exit_group_chat);
                mDialog.setTip(getString(R.string.whether_exit_group_chat));
                break;
            case REMOVED_LOADER_TO_OTHER:
                title = "退出群聊";
                mDialog.setTip(getString(R.string.group_manager_move_to) + dateList.get(nextLoaderNube).getDispName() +
                    getString(R.string.if_exit_group_chat));
                CustomLog.d(TAG, "移交群主对话框");
            default:
                break;
        }
//        mDialog.setStillShow(stillShow);
//        //		mDialog.setTitle(title);
//        mDialog.setCancleButton(new BtnClickedListener() {
//            @Override
//            public void onBtnClicked() {
//                CustomLog.d(TAG, "点击取消按钮");
//            }
//        }, cancel);
//        mDialog.setPositiveButton(new BtnClickedListener() {
//            @Override
//            public void onBtnClicked() {
//                CustomLog.d(TAG, "点击确定按钮");
//                mHandler.sendEmptyMessage(type);
//            }
//        }, positive);
//        mDialog.showDialog();
//        cd.setTip("确定退出注册？");
        mDialog.setOkBtnText(positive);
        mDialog.setCancelBtnText(cancel);
        mDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {

            @Override
            public void onClick(CustomDialog customDialog) {
//                GroupChatDetailActivity.this.finish();
                mHandler.sendEmptyMessage(type);
                mDialog.dismiss();
            }
        });
        mDialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {

            @Override
            public void onClick(CustomDialog customDialog) {
                CustomLog.d(TAG, "点击取消按钮");
                mDialog.dismiss();
            }
        });
        mDialog.show();
    }


    private void updateView() {
        //数据变化时，更新页面
        LogUtil.begin("");
        dateList.clear();
        if (mChatType.equals(VALUE_GROUP)) {
            mGroupBean = mGroupDao.queryGroup(mNumberId);
            dateList.putAll(mGroupDao.queryGroupMembers(mNumberId));
            CustomLog.d(TAG, "dateList.size()=" + dateList.size());
            titleBar.setTitle(getString(R.string.group_chat_info) + "(" + dateList.size() + ")");
            //群主删除最后一个人时，没有显示加号，需要点一下页面才恢复
            //添加判断如果这种情况直接回复
            if(dateList.size() == 1 && adapter != null){
                adapter.setCanRemoved(false);
            }
            groupChatDetails1.setVisibility(View.VISIBLE);
            saveToContactLayout.setVisibility(View.VISIBLE);
            lineview1.setVisibility(View.VISIBLE);
            quit_Btn.setVisibility(View.VISIBLE);
            if (mGroupBean != null) {
                CustomLog.d(TAG, "mGroupBean!=null，更新画面");
                CustomLog.d(TAG,"群 id" + mGroupBean.getGid());
                adapter.setGroupData(dateList, mGroupBean.getMgrNube());
                showGroupName.setText(
                    TextUtils.isEmpty(mGroupBean.getgName()) ? getString(R.string.no_name) : mGroupBean.getgName());//群名称
            } else {
                CustomLog.d(TAG, "mGroupBean == null，不更新画面，等待接口返回");
            }
        } else {
            //设置单聊个人详细信息
            dateList.putAll(getSingleData());
            adapter.setSingleData(dateList);
            titleBar.setTitle(getString(R.string.chat_info));
            lineview1.setVisibility(View.GONE);
            groupChatDetails1.setVisibility(View.GONE);
            saveToContactLayout.setVisibility(View.GONE);
            quit_Btn.setVisibility(View.INVISIBLE);
        }
        LogUtil.end("");
    }


    private Map<String, GroupMemberBean> getSingleData() {
        Contact userInfo = mContactManage.getContactInfoByNubeNumber(mNumberId);
        GroupMemberBean mSingedate = new GroupMemberBean();
        String headUrl = "";
        String name = "";
        String nickName = "";
        String number = "";
        int gender = GroupMemberTable.GENDER_MALE;
        if (userInfo != null) {
            if (ContactManager.getInstance(this).checkNubeIsCustomService(mNumberId)){
                name = getString(R.string.video_custom_service);
                headUrl = "CustomService";
            }else {
                headUrl = userInfo.getHeadUrl();
                name = userInfo.getName();
                nickName = userInfo.getNickname();
                number = userInfo.getNumber();
                gender = userInfo.getSex() == 1
                         ? GroupMemberTable.GENDER_MALE
                         : GroupMemberTable.GENDER_FEMALE;
            }

        }
        mSingedate.setHeadUrl(headUrl);
        mSingedate.setName(name);
        mSingedate.setNickName(nickName);
        mSingedate.setPhoneNum(number);
        mSingedate.setGender(gender);
        mSingedate.setShowName(ShowNameUtil.getShowName(
            ShowNameUtil.getNameElement(name, nickName, number, mNumberId)));
        mSingedate.setNubeNum(mNumberId);
        Map<String, GroupMemberBean> singdate = new HashMap<String, GroupMemberBean>();
        singdate.put(mNumberId, mSingedate);
        return singdate;
    }


    @Override
    protected void onResume() {
        LogUtil.begin("");
        super.onResume();
        updateView();
        if (mChatType.equals(VALUE_GROUP)){
            if (loginNubeNum.equals(mGroupBean.getMgrNube())){
                groupMeetingLayout.setVisibility(View.VISIBLE);
            }else {
                groupMeetingLayout.setVisibility(View.GONE);
            }
        }
        LogUtil.end("");
    }


    private void onFinish() {
        this.finish();
    }


    @Override
    public void onPause() {
        CustomLog.d(TAG, "onPause begin");
        super.onPause();
        CustomLog.d(TAG, "onPause end");
    }


    @Override
    public void onDestroy() {
        CustomLog.d(TAG, "onDestroy begin");
        super.onDestroy();
        CustomLog.d(TAG, "onDestroy end");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.multi_icons_gridview://点击myGridView空白区域，显示添加和删除
                if (adapter != null) {
                    adapter.setCanRemoved(false);
                }
                break;
            case R.id.group_chat_name_relativelayout://修改群名称
                CustomLog.d(TAG, "点击修改群名称");
                if (!isGroupMember()) {
                    showToast(getString(R.string.no_make_remove_group_chat));
                    break;
                }
                //                MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_MODIFYNAME);
                Intent intent = new Intent(GroupChatDetailActivity.this, ModifyActivity.class);
                intent.putExtra(ModifyActivity.KEY_OF_TYPE,
                    ModifyActivity.VALUE_OF_TYPE_MODIFY_GROUP_NAME);
                intent.putExtra(ModifyActivity.KEY_OF_VALUE,
                    TextUtils.isEmpty(mGroupBean.getgName()) ? "" : mGroupBean.getgName());
                startActivityForResult(intent, ACTIVITY_RESULT_MODIFY_GROUP_NAME);
                break;
            case R.id.clear_group_chat_message_relativelayout://清空聊天记录
                if (!Conversation) {
                    CustomToast.show(GroupChatDetailActivity.this, getString(R.string.no_chat_info_cannot_click), 1);
                    return;
                }
                CustomLog.d(TAG, "点击清空聊天记录");
                //                if (mChatType.equals(VALUE_SINGLE)){
                //                    MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_PERSON_CLEAR_MESSAGE);
                //                }else{
                //                    MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_CLEARMESSAGE);
                //                }
                showCommonDialog(CLEAR_MESSAGE);
                break;
            case R.id.quit_group_chat_btn://主动退出群聊
                CustomLog.d(TAG, "点击退出群聊");
                if (!isGroupMember()) {
                    showToast(getString(R.string.no_make_remove_group_chat));
                    break;
                }

                if (loginNubeNum.equals(mGroupBean.getMgrNube())) {
                    if (dateList.size() == 1) {//20150623 只有群主一人，直接退出，与ios保持一致
                        CustomLog.d(TAG, "只有群主一人，直接弹出退出对话框");
                        //                        MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_QUIT);
                        showCommonDialog(QUIT_BY_SELF);
                    } else {
                        showAlertDialog();
                    }
                } else {
                    //                    MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_QUIT);
                    showCommonDialog(QUIT_BY_SELF);
                }
                break;
            case R.id.group_qrcode_layout:
                Intent groupQRCodeIntent = new Intent(GroupChatDetailActivity.this,
                    GroupQRCodeActivity.class);
                groupQRCodeIntent.putExtra(GroupQRCodeActivity.GROUP_ID, mNumberId);
                Bundle bundle = new Bundle();
                bundle.putSerializable(GroupQRCodeActivity.GROUP_BEAN, mGroupBean);
                groupQRCodeIntent.putExtras(bundle);
                startActivity(groupQRCodeIntent);
                break;
            case R.id.group_accouncement_layout:
                Intent announcementIntent = null;
                if (loginNubeNum.equals(mGroupBean.getMgrNube())) {
                    if (groupAnnounceInfo == null) {
                        announcementIntent = new Intent(GroupChatDetailActivity.this,
                            GroupAnnouncementEditActivity.class);
                    } else if (groupAnnounceInfo != null) {
                        announcementIntent = new Intent(GroupChatDetailActivity.this,
                            GroupAnnouncementActivity.class);
                    }
                } else if (!loginNubeNum.equals(mGroupBean.getMgrNube())) {
                    announcementIntent = new Intent(GroupChatDetailActivity.this,
                        GroupAnnouncementActivity.class);
                }
                //                Intent announcementIntent = new Intent(GroupChatDetailActivity.this,GroupAnnouncementActivity.class);
                announcementIntent.putExtra(GroupAnnouncementActivity.GROUP_ID, mNumberId);
                if (loginNubeNum.equals(mGroupBean.getMgrNube())) {
                    announcementIntent.putExtra(GroupAnnouncementActivity.ISLEADDER, true);
                } else {
                    announcementIntent.putExtra(GroupAnnouncementActivity.
                        ISLEADDER, false);
                }
                if (groupAnnounceInfo != null) {
                    announcementIntent.putExtra(GroupAnnouncementActivity.ANNOUNCEMENT,
                        groupAnnounceInfo.getNoticeContent());
                    announcementIntent.putExtra(GroupAnnouncementActivity.LOADINGTIME,
                        groupAnnounceInfo.getCreateTime() + "");
                } else {
                    announcementIntent.putExtra(GroupAnnouncementActivity.ANNOUNCEMENT, "");
                    announcementIntent.putExtra(GroupAnnouncementActivity.LOADINGTIME, "");
                }

                GroupMemberBean itemBean = dateList.get(mGroupBean.getMgrNube());

                if (itemBean == null){
                    CustomToast.show(GroupChatDetailActivity.this,getString(R.string.group_manager_remove_canot_edit_announcement),1);
                }else {
                    announcementIntent.putExtra(GroupAnnouncementActivity.LEADER_HEADURL,
                        itemBean.getHeadUrl());
                    announcementIntent.putExtra(GroupAnnouncementActivity.LEADER_NAME,
                        itemBean.getNickName());
                    startActivityForResult(announcementIntent, ACTIVITY_GROUP_ANNOUNCEMENTEDIT);
                }
                break;
            case R.id.group_meeting_layout:
                Intent groupMeetingIntent = new Intent(GroupChatDetailActivity.this,GroupMeetingActivity.class);
                groupMeetingIntent.putExtra(KEY_MEETINGNUB,mMeetingNub);
                groupMeetingIntent.putExtra(KEY_MEETINGTHEME,mMeetingTheme);
                groupMeetingIntent.putExtra(KEY_NUMBER,mNumberId);
                groupMeetingIntent.putExtra(KEY_MEETINGPASSWORD,mMeetingPassword);
                groupMeetingIntent.putExtra(KEY_GETGROUPCSLFAIL,mGetGroupCslFail);
                startActivityForResult(groupMeetingIntent,ACTIVITY_GROUP_MEETING);
            default:
                break;
        }
    }


    private void showAlertDialog() {//	移交群主后退出/直接退出
        CustomLog.d(TAG, "显示 移交群主后退出/直接退出 对话框");
        final MedicalAlertDialog menuDlg = new MedicalAlertDialog(this);
        menuDlg.addButtonFirst(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                CustomLog.d(TAG, "点击移交群主后退出");
                if (CommonUtil.isFastDoubleClick()) {
                    return;
                }
                //                MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_HANDEDOVERMANAGER);
                CustomLog.d(TAG, "跳转到SelectLinkManActivity页面");
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(GroupChatDetailActivity.this,
                            SelectLinkManActivity.class);
                        intent.putExtra(SelectLinkManActivity.OPT_FLAG,
                            SelectLinkManActivity.OPT_HAND_OVER_START_FOR_RESULT);
                        intent.putExtra(SelectLinkManActivity.AVITVITY_TITLE, getString(R.string.select_hand_over_group_people));
                        intent.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
                            SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                        intent.putExtra(SelectLinkManActivity.KEY_IS_SIGNAL_SELECT, false);
                        intent.putExtra(SelectLinkManActivity.KEY_SINGLE_CLICK_BACK, true);
                        intent.putExtra(SelectLinkManActivity.HAND_OVER_MASTER_LIST,
                            GroupMemberToContactsBean());
                        startActivityForResult(intent, ACTIVITY_RESULT_REMOVED_LOADER_TO_OTHER);
                    }
                });
                thread.start();
            }
        }, getString(R.string.hand_over_exit));
        menuDlg.addButtonSecond(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
                CustomLog.d(TAG, "点击直接退出");
                //                MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_QUIT);
                showCommonDialog(QUIT_BY_SELF);
            }
        }, getString(R.string.exit));
        menuDlg.addButtonThird(new BottomMenuWindow.MenuClickedListener() {
            @Override
            public void onMenuClicked() {
//                CustomLog.d(TAG, "点击直接退出");
                //                MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_QUIT);
                menuDlg.dismiss();
            }
        }, getString(R.string.btn_cancle));
        menuDlg.show();

    }


    @Override
    public void onCheckedChanged(SwitchButton cb, boolean isChecked) {

        if (!Conversation) {
            msgSetTop_QnSwitch.setChecked(false);
            dontDisturb_QnSwitch.setChecked(false);
            CustomToast.show(GroupChatDetailActivity.this, getString(R.string.no_chat_info_cannot_click), 1);
            return;
        }
        switch (cb.getId()) {
            case R.id.dont_disturb_switch:
                CustomLog.d(TAG, "点击 消息免打扰 开关");
                Message msg = new Message();
                msg.what = DONT_DISTURB;
                Bundle msgBundle = new Bundle();
                msgBundle.putBoolean(KEY_DONT_DISTURB, isChecked);
                msg.setData(msgBundle);
                mHandler.sendMessage(msg);
                break;
            case R.id.msg_set_top_switch: {
                CustomLog.d(TAG, "点击 置顶聊天 开关");
                Message topMsg = new Message();
                topMsg.what = SET_MSG_TOP;
                Bundle topMsgBundle = new Bundle();
                topMsgBundle.putBoolean(KEY_IS_THREAD_TOP, isChecked);
                topMsg.setData(topMsgBundle);
                mHandler.sendMessage(topMsg);
                break;
            }
            case R.id.save_conteact_switch: {
                CustomLog.d(TAG, "点击 保存到通讯录 开关");
                Message saveMsg = new Message();
                saveMsg.what = SAVE_TO_CONTACT;
                Bundle saveMsgBundle = new Bundle();
                saveMsgBundle.putBoolean(KEY_SAVE_GROUP_TO_CONTACT, isChecked);
                saveMsg.setData(saveMsgBundle);
                mHandler.sendMessage(saveMsg);
            }

            default:
                break;
        }
    }


    @SuppressWarnings("rawtypes")
    @Override
    public void onAddMember() {
        if (mChatType.equals(VALUE_GROUP) && (!isGroupMember())) {
            showToast(getString(R.string.no_make_remove_group_chat));
            return;
        }
        //        MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_ADDMEMBER);
        CustomLog.d(TAG, "点击添加按钮，跳转到SelectLinkManActivity页面");
        Intent intent = new Intent(GroupChatDetailActivity.this, SelectLinkManActivity.class);
        intent.putExtra(SelectLinkManActivity.OPT_FLAG,
            SelectLinkManActivity.OPT_INVITE_START_FOR_RESULT);
        // 极会议版本，增加一个参数
        intent.putExtra(SelectLinkManActivity.ACTIVTY_PURPOSE,
            SelectLinkManActivity.GROUP_INVITE);
        intent.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
            SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
        intent.putExtra(SelectLinkManActivity.KEY_IS_SIGNAL_SELECT, true);
        ArrayList<String> nubeList = new ArrayList<String>();
        if (mChatType.equals(VALUE_SINGLE)) {
            nubeList.add(mNumberId);//单聊
        } else {
            Iterator iter = dateList.entrySet().iterator();
            while (iter.hasNext()) {
                nubeList.add((String) ((Map.Entry) iter.next()).getKey());
            }
        }
        intent.putStringArrayListExtra(SelectLinkManActivity.KEY_SELECTED_NUBENUMBERS, nubeList);
        startActivityForResult(intent, ACTIVITY_RESULT_ADD_BY_OTHER);
    }


    @Override
    public void onRemovedMember(String nubeNumber) {
        if (!isGroupMember()) {
            showToast(getString(R.string.no_make_remove_group_chat));
            return;
        }
        CustomLog.d(TAG, "点击联系人头像，移除联系人");
        //        MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_REMOVEMEMBER);
        Message msg = new Message();
        msg.what = QUIT_BY_LARDER;
        Bundle msgBundle = new Bundle();
        msgBundle.putString(KEY_NUBE_NUMBER, nubeNumber);
        msg.setData(msgBundle);
        mHandler.sendMessage(msg);
    }


    @Override
    public void onJumoToContactDetail(String nubeNumber) {
        CustomLog.d(TAG, "点击联系人头像，跳转到联系人详情页面");
        //        MobclickAgent.onEvent(GroupChatDetailActivity.this,UmengEventConstant.EVENT_GROUP_TODETIAL);
        Message msg = new Message();
        msg.what = JUMP_TO_CONTACTDETAIL;
        Bundle msgBundle = new Bundle();
        msgBundle.putString(KEY_NUBE_NUMBER, nubeNumber);
        msg.setData(msgBundle);
        mHandler.sendMessage(msg);
    }


    @Override
    public void onResult(String _interfaceName, boolean successOrfaliure,
                         String Reslut) {
        CustomLog.d(TAG, "接口" + _interfaceName + "返回信息" + Reslut);
        removeLoadingView();
        if (successOrfaliure == true) {//成功
            if (UrlConstant.METHOD_ADD_USERS.equals(_interfaceName)) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        updateView();
                    }
                });

            } else if (UrlConstant.METHOD_CREATE_GROUP.equals(_interfaceName)) {
                CustomLog.d(TAG, "跳转到消息页面");
                //                MobclickAgent.onEvent(GroupChatDetailActivity.this,
                //                        UmengEventConstant.EVENT_PERSON_TO_GROUP);
                Intent i = new Intent(GroupChatDetailActivity.this, ChatActivity.class);
                i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                    ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
                i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, Reslut);
                i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
                    ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
                i.putExtra(ChatActivity.KEY_CONVERSATION_ID, Reslut);
                i.putExtra(ChatActivity.KEY_CONVERSATION_EXT, "");
                startActivity(i);
                onFinish();
            } else if (UrlConstant.METHOD_DEL_USERS.equals(_interfaceName)) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // 0020135:
                        // 【V1.7.1.9】在群聊信息界面删除联系人，界面卡顿在正在移除群成员界面，一直处于这个界面；
                        // 日志中报错:Only the original thread that created a view
                        // hierarchy can touch its views.
                        updateView();
                    }
                });
            } else if (UrlConstant.METHOD_EDIT_GROUP.equals(_interfaceName)) {
                CustomLog.d(TAG, "修改群名称成功，更新页面");
                mGroupBean.setgName(nameFormModify.trim());
                showGroupName.setText(nameFormModify.trim());
                updateGroupNameToDB();
                showToast(getString(R.string.change_group_chat_name_success));
            } else if (UrlConstant.METHOD_QUERY_GROUP_DETAIL.equals(_interfaceName)) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        updateView();
                    }
                });
            } else if (UrlConstant.METHOD_QUITE_GROUP.equals(_interfaceName)) {
                if (isAlreadySaveContact) {
                    isExitGroup = true;
                    deleteGroupFromContact(mNumberId);
                }

                onFinish();
            }
        } else {//失败

//            CustomToast.show(GroupChatDetailActivity.this, "调用接口失败", 1);

            if (UrlConstant.METHOD_ADD_USERS.equals(_interfaceName)) {

            } else if (UrlConstant.METHOD_CREATE_GROUP.equals(_interfaceName)) {

            } else if (UrlConstant.METHOD_DEL_USERS.equals(_interfaceName)) {

            } else if (UrlConstant.METHOD_EDIT_GROUP.equals(_interfaceName)) {

            } else if (UrlConstant.METHOD_QUERY_GROUP_DETAIL.equals(_interfaceName)) {

            } else if (UrlConstant.METHOD_QUITE_GROUP.equals(_interfaceName)) {

            }
        }
    }


    private void updateGroupNameToDB() {
        mContactManage.updateGroupName(mNumberId, nameFormModify, new ContactCallback() {
            @Override
            public void onFinished(ResponseEntry result) {
                CustomLog.d(TAG, "updateGroupName result.status:" + result.status);
            }
        });
    }


    private ArrayList<ContactFriendBean> GroupMemberToContactsBean() {
        ArrayList<ContactFriendBean> List = new ArrayList<ContactFriendBean>();
        ContactFriendBean data;
        Iterator<Entry<String, GroupMemberBean>> iter = dateList.entrySet().iterator();
        while (iter.hasNext()) {
            GroupMemberBean bean = iter.next().getValue();
            if (!bean.getNubeNum().equals(mGroupBean.getMgrNube())) {
                data = new ContactFriendBean();
                data.setHeadUrl(bean.getHeadUrl());
                data.setName(bean.getDispName());
                data.setNickname(bean.getNickName());
                data.setNumber(bean.getPhoneNum());
                data.setNubeNumber(bean.getNubeNum());
                data.setSex((bean.getGender() == GroupMemberTable.GENDER_MALE
                             ? GroupMemberTable.GENDER_MALE
                             : GroupMemberTable.GENDER_FEMALE) + "");
                data.setPym((PinyinUtil.getPinYin(bean.getDispName()).toUpperCase()));
                List.add(data);
            }
        }
        ListSort<ContactFriendBean> listSort = new ListSort<ContactFriendBean>();
        listSort.Sort(List, "getPym", null);
        return List;
    }


    private boolean isGroupMember() {
        String loginNubeNum = AccountManager.getInstance(MedicalApplication.getContext()).getNube();
        boolean isGroupMember = mGroupDao.isGroupMember(mNumberId, loginNubeNum);
        CustomLog.d(TAG, "isGroupMember=" + isGroupMember);
        return isGroupMember;
    }


    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        CustomLog.d(TAG, text);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.color.list_active_bg);
            switch (v.getId()) {
                case R.id.group_chat_name_relativelayout:
                    //				lineview1.setVisibility(View.INVISIBLE);
                    break;
                case R.id.clear_group_chat_message_relativelayout:
                    //				lineview1.setVisibility(View.INVISIBLE);
                    //				lineview2.setVisibility(View.INVISIBLE);
                    break;
                case R.id.group_qrcode_layout:
                    break;
                case R.id.group_accouncement_layout:
                    break;
                case R.id.group_meeting_layout:
                    break;
                default:
                    break;
            }
        } else if (!(event.getAction() == MotionEvent.ACTION_MOVE)) {
            v.setBackgroundResource(R.color.color_white);
            //			lineview1.setVisibility(View.VISIBLE);
            //			lineview2.setVisibility(View.VISIBLE);
        }
        return false;
    }


    private void getAnnouncement() {

        final MDSUserGetGrougNotice groupNoticeRequest = new MDSUserGetGrougNotice() {
            @Override
            protected void onSuccess(NoticeInfo responseContent) {
                CustomLog.d(TAG, "群公告为:" + responseContent.getNoticeContent());
                groupAnnounceInfo = responseContent;
                groupAnnounceInfo.setCreateTime(responseContent.getCreateTime() * 1000);
                groupAnnouncement.setVisibility(View.VISIBLE);
                groupDefaultNotice.setVisibility(View.GONE);
                groupAnnouncement.setText(responseContent.getNoticeContent());
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                CustomLog.d(TAG, "获取群公告失败,statusCode:" + statusCode + " " + statusInfo);
                groupAnnouncement.setVisibility(View.GONE);
                groupDefaultNotice.setVisibility(View.VISIBLE);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(GroupChatDetailActivity.this)
                        .tokenAuthFail(statusCode);
                } else {
                    CustomLog.d(TAG, "statusCode:" + statusCode + " statusInfo" + statusInfo);
                }

            }
        };

        String loginUserToken = AccountManager.getInstance(this).getMdsToken();
        groupNoticeRequest.appGetGroupNotice(loginUserToken, mNumberId);
    }


    private void sendGroupMsg(String strAnnouncement) {

        String remindStr = "";
        try {
            JSONObject txtObj = new JSONObject();
            txtObj.put("text",getString(R.string.all_of_people) + strAnnouncement);
            txtObj.put("subtype","remind_notice");
            remindStr = txtObj.toString();
        }catch (Exception e){
            CustomLog.d(TAG,"sendGroupMsg" + e.toString());
        }

        final String finalRemindStr = remindStr;
        new Thread(new Runnable() {

            @Override
            public void run() {
                String uuid = "";
                // 插入发送记录
                uuid = mNoticesDao
                    .createSendFileNotice(
                        loginNubeNum,
                        mNumberId,
                        null,
                        "",
                        FileTaskManager.NOTICE_TYPE_REMIND_SEND,
                            finalRemindStr, mNumberId, null);
                getFileTaskManager()
                    .addTask(uuid, null);
            }
        }).start();

    }
}