package cn.redcdn.hvs.contacts.contact;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.sax.RootElement;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import com.umeng.analytics.MobclickAgent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.redcdn.datacenter.medicalcenter.MDSAppSearchUsers;
import cn.redcdn.datacenter.medicalcenter.data.MDSDetailInfo;
import cn.redcdn.datacenter.meetingmanage.CreateMeeting;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.VerificationActivity;
import cn.redcdn.hvs.contacts.VerificationReplyDialog;
import cn.redcdn.hvs.contacts.contact.ContactDeleteDialog.NoClickListener;
import cn.redcdn.hvs.contacts.contact.ContactDeleteDialog.OkClickListener;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.contacts.contact.manager.RecommendManager;
import cn.redcdn.hvs.contacts.manager.AddFriendCallback;
import cn.redcdn.hvs.contacts.manager.DeleteFriendCallback;
import cn.redcdn.hvs.contacts.manager.FriendSync;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.im.bean.FriendInfo;
import cn.redcdn.hvs.im.bean.StrangerMessage;
import cn.redcdn.hvs.im.bean.ThreadsBean;
import cn.redcdn.hvs.im.column.StrangerMessageTable;
import cn.redcdn.hvs.im.dao.NoticesDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.im.view.CustomDialog1;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.hvs.util.youmeng.AnalysisConfig;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.R.id.iamgehead;


public class ContactCardActivity extends BaseActivity implements Serializable {

    // 会诊邀请人视讯号列表，手机号
    private ArrayList<String> phoneId = new ArrayList<String>();
    private String[] invitedPhones = new String[1];
    private RoundImageView iamgeHead = null;
    private Contact mContact = null;
    private Button btnContactCardBack = null;
    private Button btnContactCardDel = null;
    private RelativeLayout rlContactInfo = null;
    private TextView tvContactName = null;
    private TextView tvNubeDetail = null;
    private TextView tvPhoneDetail = null;
    private TextView tvMeetingRoom = null;
    private Button ibStartMeeting = null;
    private TextView tvPhone = null;
    private CreateMeeting create = null;
    private DisplayImageListener mDisplayImageListener = null;
    private RelativeLayout rlContactPhoneInfo = null;
    private RelativeLayout rlContactCardContent = null;
    private RelativeLayout rlSendMessage = null;
    private RelativeLayout rlBlank = null;
    private TextView tvUnit;
    private TextView tvDepartment;
    private TextView tvProfessional;
    private TextView tvOfficeTel;
    private TextView tvUnitType;
    private TextView tvDepartmentType;
    private TextView tvProfessionalType;
    private TextView tvOfficeTelType;
    private Button sendChatMsgBt;
    private Button btnAddFriend;
    private int searchType = -1;  //搜索方式： 1:手机号 2：邮箱 3.视讯号 4.二维码扫描 5.群内添加 6.陌生人聊天添加 7.手机联系人添加
    private RelativeLayout rlJoinMeeting;
    private RelativeLayout rlAddFriend;
    private RelativeLayout rlContent;
    private RelativeLayout rlDepartment;
    private RelativeLayout rlPositionalTitle;
    private RelativeLayout rlDepartmentPhoneNumber;
    private View vBelowDepartmenTel;
    private View vBelowPositionalTitle;
    private View vBelowDepartment;
    private MDSAppSearchUsers searchUsers = null;
    private RelativeLayout rlVerification;
    private TextView tvVerificationMessage;
    private Button btnVerificationReply;
    private final int MSG_NO_USER = 500;
    private final int MSG_SET_TEXT = 503;
    private final int MSG_FRIEND_CHAT = 504;
    private FriendInfo friendInfo;
    private String groupName;
    private FriendRelationObserver observeFriendRelation;
    private StrangerMessageObserver observeStrangeRelation;
    private int currentFriendRelation;
    private String mNubeNumber;
    private boolean isFromHpuContact = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_NO_USER:
                    rlVerification.setVisibility(View.GONE);
                    btnAddFriend.setVisibility(View.GONE);
                    break;
                case MSG_SET_TEXT:
                    String nubeNumber;

                    if(null!=getIntent().getExtras()&&null!=getIntent().getExtras().getString("nubeNumber")){
                        nubeNumber = getIntent().getExtras().getString("nubeNumber");
                    }else{
                        nubeNumber = mContact.getNubeNumber();
                    }

                    Cursor c = FriendsManager.getInstance().getFriendValidationMsg(nubeNumber);
                    int result = FriendsManager.getInstance().getFriendRelationByNubeNumber(nubeNumber);

                    if((null != getIntent().getExtras()
                        && null != getIntent().getExtras().getString("contactFragment"))||
                        ContactManager.getInstance(ContactCardActivity.this).checkNubeIsCustomService(
                        nubeNumber)){
                        rlVerification.setVisibility(View.GONE);
                    }else{
                        if(null!=c&&c.moveToNext()){
                            if(result==FriendInfo.RELATION_TYPE_BOTH){
                                rlVerification.setVisibility(View.GONE);
                            }else{
                                rlVerification.setVisibility(View.VISIBLE);
                            }
                            tvVerificationMessage.setText("");
                            int i = 0;

                            List<String> messageList = new ArrayList<String>();

                                do{
                                    i++;

                                    String name = MedicalApplication.getContext().getString(R.string.no_name);

                                    if(c.getInt(c.getColumnIndex(StrangerMessageTable.MSG_DIRECTION))==StrangerMessageTable.SEND){
                                        name = AccountManager.getInstance(ContactCardActivity.this).getName();
                                    }else if(c.getInt(c.getColumnIndex(StrangerMessageTable.MSG_DIRECTION))==StrangerMessageTable.RECIEVE){
                                        name = c.getString(c.getColumnIndex(StrangerMessageTable.STRANGER_NAME));
                                    }

                                    messageList.add(name+"："
                                            +c.getString(c.getColumnIndex(StrangerMessageTable.MSG_CONTENT)));

                                }
                                while(c.moveToNext() && i<3);

                            Collections.reverse(messageList);

                            for(int j = 0; j<messageList.size(); j++){
                                String text;
                                if(messageList.get(j).length()>57){

                                    text = messageList.get(j).replace(messageList.get(j),messageList.get(j).substring(0,56)+"...");

                                }else{
                                    text = messageList.get(j);
                                }
                                tvVerificationMessage.append(text+"\n");
                            }

                        }else if(null!=c&&!c.moveToNext()){
                            if(result==FriendInfo.RELATION_TYPE_NONE||result==FriendInfo.RELATION_TYPE_BOTH){
                                rlVerification.setVisibility(View.GONE);
                            }else{
                                rlVerification.setVisibility(View.VISIBLE);
                            }
                        }else{
                            rlVerification.setVisibility(View.GONE);
                        }
                    }
                    break;
                case MSG_FRIEND_CHAT:
                    //发送“已是好友，可以聊天”消息
                    NoticesDao noticeDao = new NoticesDao(ContactCardActivity.this);
                    noticeDao.createAddFriendMsgTip(mContact.getName(),mContact.getNubeNumber());
//                    enterChatActivity();
                    break;
                default:
                    break;
            }
          }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contactcard);
        init();
    }

    private void init(){
        mDisplayImageListener = new DisplayImageListener();
        rlContent = (RelativeLayout) findViewById(R.id.rlcontent);
        rlContent.setVisibility(View.GONE);
        btnContactCardBack = (Button) findViewById(R.id.btncontactcardback);
        btnContactCardBack.setOnClickListener(mbtnHandleEventListener);
        btnContactCardDel = (Button) findViewById(R.id.btncontactcarddel);

        if(null != getIntent().getExtras() && null != getIntent().getExtras().getString("groupName")) {
            groupName = getIntent().getExtras().getString("groupName");
        }

        if (null != getIntent().getExtras() && null != getIntent().getExtras().getString("searchType")) {
            searchType = Integer.valueOf(getIntent().getExtras().getString("searchType"));
        }

        if (null != getIntent().getExtras()
                && null != getIntent().getExtras().getString("nubeNumber")) {
            String nubeNumber = getIntent().getExtras().getString("nubeNumber");
            Contact contact = (ContactManager.getInstance(ContactCardActivity.this)
                    .getContactInfoByNubeNumber(nubeNumber));
            boolean isExit = ContactManager.getInstance(this).isContactExist(nubeNumber);
            if (isExit){
                handleData(nubeNumber,contact);
            }else {
                List<Contact> mList = (ContactManager.getInstance(ContactCardActivity.this).queryHpuContactByNube(nubeNumber));
                if (mList.size()>0){
                    mContact = mList.get(0);
                    isFromHpuContact = true;
                    mNubeNumber = nubeNumber;
                    initContactCardPage(mContact);
                }else {
                    handleData(nubeNumber,contact);
                }
            }
        } else if (null != getIntent().getExtras()
                && null != getIntent().getExtras().getSerializable("contact")) {
            mContact = (Contact) getIntent().getExtras().getSerializable("contact");
            CustomLog.d(TAG, "mContact ..." + mContact.toString());
            if (mContact!=null){
              boolean isExit =  ContactManager.getInstance(this).isContactExist(mContact.getNubeNumber());
                if (isExit){
                    handleData(mContact.getNubeNumber(),mContact);
                }else{
                    List<Contact> mList = ContactManager.getInstance(ContactCardActivity.this).queryHpuContactByNube(mContact.getNubeNumber());
                    if (mList.size()>0){
                        mContact = mList.get(0);
                        isFromHpuContact = true;
                        mNubeNumber = mContact.getNubeNumber();
                        initContactCardPage(mContact);
                    }else {
                        handleData(mContact.getNubeNumber(),mContact);
                    }
                }
            }else {
                handleData(mContact.getNubeNumber(),mContact);
            }

        }else if (null != getIntent().getExtras()
                && null != getIntent().getExtras().getSerializable("hpuContact")){
            mContact = (Contact) getIntent().getExtras().getSerializable("hpuContact");
            CustomLog.d(TAG,"从医联体联系人点击过来 mcontact...."+mContact.toString());
            isFromHpuContact = true;
            mNubeNumber = mContact.getNubeNumber();
            initContactCardPage(mContact);
        }

    }

    private void handleData(String nubeNumber,Contact contact){
        mNubeNumber = nubeNumber;
        FriendsManager.getInstance().setMesRead(nubeNumber);
        String[] arraylist = { nubeNumber };
        FriendInfo friendInfo = FriendsManager.getInstance().getFriendByNubeNumber(nubeNumber);
        if (ContactManager.getInstance(this).checkNubeIsCustomService(nubeNumber)) {
            mContact = new Contact();
            mContact.setNubeNumber(nubeNumber);
            mContact.setName(getString(R.string.video_custom_service));
            initCustomServiceView(nubeNumber);
        } else if (null != contact.getContactId()
                && !contact.getContactId().isEmpty()
                &&null!=contact.getHeadUrl()
                &&(null!=contact.getNickname()
                ||null!=contact.getName())
                &&null!=contact.getWorkUnit()
                &&null!=String.valueOf(contact.getWorkUnitType())
                &&null!=contact.getDepartment()
                &&null!=contact.getProfessional()
                &&null!=contact.getOfficeTel()
                &&(null!=contact.getNumber()
                ||null!=contact.getEmail())
                &&null!=String.valueOf(contact.getUserFrom())) {
            mContact = new Contact();
            mContact.setHeadUrl(contact.getHeadUrl());
            mContact.setNickname(contact.getNickname());
            mContact.setName(contact.getNickname());
            mContact.setNubeNumber(nubeNumber);
            mContact.setWorkUnit(contact.getWorkUnit());
            mContact.setWorkUnitType(contact.getWorkUnitType());
            mContact.setDepartment(contact.getDepartment());
            mContact.setProfessional(contact.getProfessional());
            mContact.setOfficeTel(contact.getOfficeTel());
            mContact.setNumber(contact.getNumber());
            mContact.setEmail(contact.getEmail());
            mContact.setUserFrom(contact.getUserFrom());
            initContactCardPage(mContact);
        }else if(null!=friendInfo
                &&null!=friendInfo.getHeadUrl()
                &&null!=friendInfo.getName()
                &&null!=friendInfo.getWorkUnit()
                &&null!=friendInfo.getWorkUnitType()
                &&null!=friendInfo.getDepartment()
                &&null!=friendInfo.getProfessional()
                &&null!=friendInfo.getOfficeTel()
                &&(null!=friendInfo.getNumber()
                ||null!=friendInfo.getEmail())
                &&null!=String.valueOf(friendInfo.getUserFrom())){
            mContact = new Contact();
            mContact.setHeadUrl(friendInfo.getHeadUrl());
            mContact.setNickname(friendInfo.getName());
            mContact.setName(friendInfo.getName());
            mContact.setNubeNumber(nubeNumber);
            mContact.setWorkUnit(friendInfo.getWorkUnit());
            mContact.setWorkUnitType(Integer.valueOf(friendInfo.getWorkUnitType()));
            mContact.setDepartment(friendInfo.getDepartment());
            mContact.setProfessional(friendInfo.getProfessional());
            mContact.setOfficeTel(friendInfo.getOfficeTel());
            mContact.setUserFrom(friendInfo.getUserFrom());
            if(null!=friendInfo.getNumber()){
                mContact.setNumber(friendInfo.getNumber());
            }
            if(null!=friendInfo.getEmail()){
                mContact.setEmail(friendInfo.getEmail());
            }
            initContactCardPage(mContact);
        } else {
            searchUser(3, arraylist); //通过视讯号搜索用户信息
        }
    }

    //搜索用户信息
    private void searchUser(final int type, String[] content) {
        searchUsers = new MDSAppSearchUsers() {
            @Override
            protected void onSuccess(List<MDSDetailInfo> responseContent) {
                ContactCardActivity.this.removeLoadingView();
                List<MDSDetailInfo> list = responseContent;
                Contact contact = new Contact();
                if (list != null && list.size() > 0) {
                    contact.setContactId(list.get(0).getUid());
                    contact.setHeadUrl(list.get(0).getHeadThumUrl());
                    contact.setNickname(list.get(0).getNickName());
                    contact.setName(list.get(0).getNickName());
                    contact.setNubeNumber(list.get(0).getNubeNumber());
                    contact.setWorkUnit(list.get(0).getWorkUnit());
                    contact.setWorkUnitType(Integer.valueOf(list.get(0).getWorkUnitType()));
                    contact.setDepartment(list.get(0).getDepartment());
                    contact.setProfessional(list.get(0).getProfessional());
                    contact.setOfficeTel(list.get(0).getOfficTel());

                    if (null != list.get(0).getMobile() && !list.get(0).getMobile().isEmpty()) {//手机号
                        contact.setNumber(list.get(0).getMobile());
                    } else if (null != list.get(0).getMail() && !list.get(0).getMail().isEmpty()) {//邮箱号
                        contact.setEmail(list.get(0).getMail());
                    }

                } else {
                    CustomToast.show(ContactCardActivity.this, getString(R.string.the_user_not_exit), Toast.LENGTH_LONG);
                    mHandler.sendEmptyMessage(MSG_NO_USER);
                }

                //searchType 搜索方式： 1:手机号 2：邮箱 3.视讯号 4.二维码扫描 5.群内添加 6.陌生人聊天添加 7.手机联系人添加
                //isAddFrom  用户来源： 0:视讯号搜索 1:手机通讯录好友推荐 2:手机号搜索 3:邮箱搜索 4:二维码扫描 5:群内添加 6:陌生人聊天添加

                mContact = contact;
                initContactCardPage(mContact);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                ContactCardActivity.this.removeLoadingView();
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(ContactCardActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(ContactCardActivity.this, statusInfo, Toast.LENGTH_LONG);
                }

            }

        };

        ContactCardActivity.this.showLoadingView(getString(R.string.loading_collection), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                CustomToast.show(ContactCardActivity.this, getString(R.string.load_cancel),
                    Toast.LENGTH_LONG);
            }
        });
        searchUsers.appSearchUsers(AccountManager.getInstance(this).getToken(), type, content);
    }

    //创建会诊
    private void createMeeting() {
        CustomLog.i(TAG, "HomeActivity::createMeeting() 正在创建会诊！");
        String LoadingString = "";
        if (ContactManager.getInstance(this).checkNubeIsCustomService(mContact.getNubeNumber())) {
            LoadingString = getString(R.string.creating_video_call);
        } else {
            LoadingString = getString(R.string.creating_consultation);
        }

        ContactCardActivity.this.showLoadingView(LoadingString,
            new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    ContactCardActivity.this.removeLoadingView();
                    if (create != null) {
                        create.cancel();
                    }
                }
            });
        newExecCreateMeeting();
    }


    private void newExecCreateMeeting() {
        if (TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
            int i = MedicalMeetingManage.getInstance().createMeeting(TAG, phoneId, new MedicalMeetingManage.OnCreateMeetingListener() {
                @Override
                public void onCreateMeeting(int code, final cn.redcdn.jmeetingsdk.MeetingInfo meetingInfo) {
                    if (code == 0) {
                        CustomLog.i(TAG, "meetingInfo==" + meetingInfo.meetingId);
                        removeLoadingView();
                        MedicalMeetingManage.getInstance().joinMeeting(meetingInfo.meetingId, new MedicalMeetingManage.OnJoinMeetingListener() {
                            @Override
                            public void onJoinMeeting(String valueDes, int valueCode) {
                                ArrayList<String> list = new ArrayList<String>();
                                list.add(mContact.getNubeNumber());
                                MedicalMeetingManage manager = MedicalMeetingManage.getInstance();
                                manager.inviteMeeting(list, meetingInfo.meetingId);
                            }
                        });
                    } else {
                        removeLoadingView();
                        CustomToast.show(ContactCardActivity.this, getString(R.string.creat_consultation_fail), CustomToast.LENGTH_SHORT);
                    }
                }
            });
            if (i == 0) {
                removeLoadingView();
                if (ContactManager.getInstance(this).checkNubeIsCustomService(mContact.getNubeNumber())) {
                    showLoadingView(getString(R.string.conveneing_video_call));
                } else {
                    showLoadingView(getString(R.string.conveneing_consultation));
                }
            }else {
                removeLoadingView();
                if (ContactManager.getInstance(this).checkNubeIsCustomService(mContact.getNubeNumber())) {
                    CustomToast.show(this, getString(R.string.convene_video_call_fail), CustomToast.LENGTH_SHORT);
                } else {
                    CustomToast.show(this, getString(R.string.convene_consultation_fail), CustomToast.LENGTH_SHORT);
                }

            }
        }else {
            removeLoadingView();
            CustomToast.show(ContactCardActivity.this,getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
        }
    }

    //初始化视频客服名片view
    private void initCustomServiceView(String nubeNumber) {
        rlContent.setVisibility(View.VISIBLE);
        btnContactCardDel.setVisibility(View.INVISIBLE);
        iamgeHead = (RoundImageView) findViewById(iamgehead);
        iamgeHead.setImageResource(R.drawable.contact_customservice);
        tvMeetingRoom = (TextView) findViewById(R.id.tvmeetingroom);
        tvMeetingRoom.setText(R.string.video_custom_service);
        tvNubeDetail = (TextView) findViewById(R.id.tv_nubenumber_number);
        tvNubeDetail.setText(nubeNumber);
        rlContactCardContent = (RelativeLayout) findViewById(R.id.rlsecondcontant);
        rlContactCardContent.setVisibility(View.INVISIBLE);
        rlBlank = (RelativeLayout) findViewById(R.id.rl_contact_blank);
        rlBlank.setVisibility(View.INVISIBLE);
        rlAddFriend = (RelativeLayout) findViewById(R.id.rl_add_friend);
        rlAddFriend.setVisibility(View.INVISIBLE);
        sendChatMsgBt = (Button) findViewById(R.id.btn_sendmessage);
        sendChatMsgBt.setOnClickListener(mbtnHandleEventListener);
        ibStartMeeting = (Button) findViewById(R.id.ibstartmeeting);
        ibStartMeeting.setText(R.string.video_call);
        ibStartMeeting.setOnClickListener(mbtnHandleEventListener);
        rlVerification = (RelativeLayout) findViewById(R.id.rl_verification_content);
        rlVerification.setVisibility(View.GONE);
    }

    //初始化联系人名片view
    private void initContactCardPage(Contact newContact) {
        FriendInfo info = null;
        //如果FriendInfo中缺少信息，则添加信息
        if(null!=newContact){

            info = FriendsManager.getInstance().getFriendByNubeNumber(newContact.getNubeNumber());
            currentFriendRelation = FriendsManager.getInstance().getFriendRelationByNubeNumber(mContact.getNubeNumber());
            if(null!=info){
                if(null!=newContact.getName()){
                    info.setName(newContact.getName());
                }
                if(null!=newContact.getHeadUrl()){
                    info.setHeadUrl(newContact.getHeadUrl());
                }
                if(null!=newContact.getEmail()){
                    info.setEmail(newContact.getEmail());
                }
                if(null!=String.valueOf(newContact.getWorkUnitType())){
                    info.setWorkUnitType(String.valueOf(newContact.getWorkUnitType()));
                }
                if(null!=newContact.getWorkUnit()){
                    info.setWorkUnit(newContact.getWorkUnit());
                }
                if(null!=newContact.getDepartment()){
                    info.setDepartment(newContact.getDepartment());
                }
                if(null!=newContact.getProfessional()){
                    info.setProfessional(newContact.getProfessional());
                }
                if(null!=newContact.getOfficeTel()){
                    info.setOfficeTel(newContact.getOfficeTel());
                }

                if(null!=String.valueOf(newContact.getUserFrom())){
                CustomLog.d(TAG,"newContact.getUserFrom():"+String.valueOf(newContact.getUserFrom()));
                }

                if(null!=String.valueOf(info.getUserFrom())){
                CustomLog.d(TAG,"info.getUserFrom():"+String.valueOf(info.getUserFrom()));
                }

                if(null!=newContact.getNumber()){
                    info.setNumber(newContact.getNumber());
                }

                FriendsManager.getInstance().modifyFriendInfo(info);

            }
        }

        rlContent.setVisibility(View.VISIBLE);
        rlDepartment = (RelativeLayout) findViewById(R.id.rl_contact_department);
        rlPositionalTitle = (RelativeLayout) findViewById(R.id.rl_contact_positionaltitle);
        rlDepartmentPhoneNumber = (RelativeLayout) findViewById(R.id.rl_contact_departmentphonenumber);
        vBelowDepartmenTel =  findViewById(R.id.v_below_department_tel);
        vBelowPositionalTitle =  findViewById(R.id.v_line_positionaltitle_bottom);
        vBelowDepartment =  findViewById(R.id.v_line_department_bottom);
        rlAddFriend = (RelativeLayout) findViewById(R.id.rl_add_friend);
        rlJoinMeeting = (RelativeLayout) findViewById(R.id.rlendcontent);
        btnAddFriend = (Button) findViewById(R.id.btn_add_friend);
        tvUnit = (TextView) findViewById(R.id.tv_hospital_detail);
        tvDepartment = (TextView) findViewById(R.id.tv_department_detail);
        tvProfessional = (TextView) findViewById(R.id.tv_positionaltitle_detail);
        tvOfficeTel = (TextView) findViewById(R.id.tv_departmentphonenumber_detail);
        tvUnitType = (TextView) findViewById(R.id.tv_hospital);
        tvDepartmentType = (TextView) findViewById(R.id.tv_department);
        tvProfessionalType = (TextView) findViewById(R.id.tv_positionaltitle);
        tvOfficeTelType = (TextView) findViewById(R.id.tv_departmentphonenumber);
        iamgeHead = (RoundImageView) findViewById(iamgehead);
        tvPhone = (TextView) findViewById(R.id.tvphone);
        rlContactInfo = (RelativeLayout) findViewById(R.id.rlcontactinfo);
        tvContactName = (TextView) findViewById(R.id.tvcontactname);
        tvNubeDetail = (TextView) findViewById(R.id.tv_nubenumber_number);
        tvPhoneDetail = (TextView) findViewById(R.id.tvphonedetail);
        ibStartMeeting = (Button) findViewById(R.id.ibstartmeeting);
        sendChatMsgBt = (Button) findViewById(R.id.btn_sendmessage);
        tvMeetingRoom = (TextView) findViewById(R.id.tvmeetingroom);
        rlContactPhoneInfo = (RelativeLayout) findViewById(R.id.rlcontactphoneinfo);
        rlContactCardContent = (RelativeLayout) findViewById(R.id.rlsecondcontant);
        rlSendMessage = (RelativeLayout) findViewById(R.id.rl_sendmessage);
        rlBlank = (RelativeLayout) findViewById(R.id.rl_contact_blank);
        rlVerification = (RelativeLayout) findViewById(R.id.rl_verification_content);
        tvVerificationMessage = (TextView) findViewById(R.id.tv_verification_message);
        btnVerificationReply = (Button) findViewById(R.id.btn_verification_reply);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(mContact.getHeadUrl(), iamgeHead,
            MedicalApplication.shareInstance().options, mDisplayImageListener);
        btnContactCardDel.setOnClickListener(mbtnHandleEventListener);
        tvOfficeTel.setOnClickListener(mbtnHandleEventListener);
        btnAddFriend.setOnClickListener(mbtnHandleEventListener);
        tvPhoneDetail.setOnClickListener(mbtnHandleEventListener);
        ibStartMeeting.setOnClickListener(mbtnHandleEventListener);
        sendChatMsgBt.setOnClickListener(mbtnHandleEventListener);
        btnVerificationReply.setOnClickListener(mbtnHandleEventListener);

        if (newContact.getNumber() != null && !newContact.getNumber().isEmpty()
            && (searchType == 1 || searchType == 7 || newContact.getUserFrom() == 2 || newContact.getUserFrom() == 1)
                ||info != null && info.getNumber()!=null && info.getUserFrom() == 2) {
            tvPhone.setText(R.string.my_phone_id);
            if (newContact.getNumber() != null && !newContact.getNumber().isEmpty()) {
                tvPhoneDetail.setText(newContact.getNumber());
            } else {
                tvPhoneDetail.setText(R.string.nothing);
            }
            tvPhoneDetail.setClickable(true);
            tvPhone.setVisibility(View.VISIBLE);
            tvPhoneDetail.setVisibility(View.VISIBLE);
            //显示手机号
            tvPhoneDetail.setText(newContact.getNumber());
            if (null != RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(newContact.getNumber())) {
                //通过手机推荐联系人的手机号查找并显示手机联系人的名字
                String id = RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(newContact.getNumber());
                String name = RecommendManager.getNameByRawId(getApplicationContext(), id);
                if (name != null && !name.equals("")) {
                    rlContactInfo.setVisibility(View.VISIBLE);
                    tvContactName.setText(name);
                } else {
                    rlContactInfo.setVisibility(View.INVISIBLE);
                }
            }
        } else if (newContact.getEmail() != null && !newContact.getEmail().isEmpty()
            && (searchType == 2 || newContact.getUserFrom() == 3)
                ||info != null && info.getEmail()!=null && info.getUserFrom() == 3) {
            tvPhone.setText(R.string.email);
            if (newContact.getEmail() != null && !newContact.getEmail().isEmpty()) {
                tvPhoneDetail.setText(newContact.getEmail());
            } else {
                tvPhoneDetail.setText(R.string.nothing);
            }
            tvPhoneDetail.setClickable(false);
            tvPhone.setVisibility(View.VISIBLE);
            tvPhoneDetail.setVisibility(View.VISIBLE);
            //显示邮箱
            tvPhoneDetail.setText(newContact.getEmail());
        }else if((newContact.getUserFrom() == Contact.USER_FROM_SEND_OR_ACCEPT||
                newContact.getUserFrom() == Contact.USER_FROM_SYNC)&&searchType!=4&&searchType!=3){
            if (newContact.getNumber() != null && !newContact.getNumber().isEmpty()) {
                tvPhone.setText(R.string.my_phone_id);
                tvPhoneDetail.setText(newContact.getNumber());
                tvPhoneDetail.setClickable(true);
                tvPhone.setVisibility(View.VISIBLE);
                tvPhoneDetail.setVisibility(View.VISIBLE);
                //显示手机号
                tvPhoneDetail.setText(newContact.getNumber());
                if (null != RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(newContact.getNumber())) {
                    //通过手机推荐联系人的手机号查找并显示手机联系人的名字
                    String id = RecommendManager.getInstance(getApplicationContext()).getRawIdByMobile(newContact.getNumber());
                    String name = RecommendManager.getNameByRawId(getApplicationContext(), id);
                    if (name != null && !name.equals("")) {
                        rlContactInfo.setVisibility(View.VISIBLE);
                        tvContactName.setText(name);
                    } else {
                        rlContactInfo.setVisibility(View.INVISIBLE);
                    }
                }
            } else if (newContact.getEmail() != null && !newContact.getEmail().isEmpty()) {
                tvPhone.setText(R.string.email);
                tvPhoneDetail.setText(newContact.getEmail());
                tvPhoneDetail.setClickable(false);
                tvPhone.setVisibility(View.VISIBLE);
                tvPhoneDetail.setVisibility(View.VISIBLE);
                //显示邮箱
                tvPhoneDetail.setText(newContact.getEmail());
            } else {
                //隐藏手机号和邮箱的一栏
                tvPhoneDetail.setClickable(false);
                tvPhone.setVisibility(View.INVISIBLE);
                tvPhoneDetail.setVisibility(View.INVISIBLE);
                rlContactPhoneInfo.setVisibility(View.INVISIBLE);
                rlContactPhoneInfo.setVisibility(View.GONE);
                rlContactInfo.setVisibility(View.INVISIBLE);
            }
        }

        else {
            //隐藏手机号和邮箱的一栏
            if (isFromHpuContact){
                tvPhone.setText("医联体");
                tvPhoneDetail.setText(newContact.getPhuName());
                tvPhoneDetail.setClickable(false);
                tvPhone.setVisibility(View.VISIBLE);
                tvPhoneDetail.setVisibility(View.VISIBLE);
            }else {
                tvPhoneDetail.setClickable(false);
                tvPhone.setVisibility(View.INVISIBLE);
                tvPhoneDetail.setVisibility(View.INVISIBLE);
                rlContactPhoneInfo.setVisibility(View.INVISIBLE);
                rlContactPhoneInfo.setVisibility(View.GONE);
                rlContactInfo.setVisibility(View.INVISIBLE);
            }
        }

        if (newContact.getNickname() == null || newContact.getNickname().isEmpty()) {
            newContact.setNickname(getString(R.string.no_name));
        }
        tvMeetingRoom.setText(newContact.getNickname());
        tvNubeDetail.setText(newContact.getNubeNumber());
        //
        phoneId.clear();

        if (newContact.getNumber() != null && !newContact.getNumber().isEmpty()) {
            invitedPhones[0] = newContact.getNumber();
        }
        phoneId.add(AccountManager.getInstance(ContactCardActivity.this)
            .getAccountInfo().nube);
        phoneId.add(tvNubeDetail.getText().toString());
        if (tvPhoneDetail.getText() != null) {
            invitedPhones[0] = tvPhoneDetail.getText().toString();
        } else {
            invitedPhones[0] = "";
        }

        //医院、公司
        if (newContact.getWorkUnit() != null && !newContact.getWorkUnit().isEmpty()) {
            tvUnit.setText(newContact.getWorkUnit());
        } else {
            tvUnit.setText(R.string.nothing);
        }

        //科室、部门
        if (newContact.getDepartment() != null && !newContact.getDepartment().isEmpty()) {
            tvDepartment.setText(newContact.getDepartment());
        } else {
            tvDepartment.setText(R.string.nothing);
        }

        //职称、职位
        if (newContact.getProfessional() != null && !newContact.getProfessional().isEmpty()) {
            tvProfessional.setText(newContact.getProfessional());
        } else {
                tvProfessional.setText(R.string.nothing);
        }

        //科室电话、公司电话
        if (newContact.getOfficeTel() != null && !newContact.getOfficeTel().isEmpty()) {
            tvOfficeTel.setText(newContact.getOfficeTel());
        } else {
                tvOfficeTel.setText(R.string.nothing);
        }

        if (newContact.getWorkUnitType() == 1) {
            tvUnitType.setText(R.string.hospital);
            tvDepartmentType.setText(R.string.room);
            tvProfessionalType.setText(R.string.profession);
            tvOfficeTelType.setText(R.string.room_phone);

        } else if (newContact.getWorkUnitType() == 2) {
            tvUnitType.setText(R.string.company);
            tvDepartmentType.setText(R.string.bumen);
            tvProfessionalType.setText(R.string.job);
            tvOfficeTelType.setText(R.string.company_telephone);
        }

        handleView();

        if (ContactManager.getInstance(this).checkNubeIsCustomService(
            newContact.getNubeNumber())) {
            //是视频客服
            btnContactCardDel.setVisibility(View.GONE);
            rlContactCardContent.setVisibility(View.INVISIBLE);
            rlAddFriend.setVisibility(View.INVISIBLE);
            rlSendMessage.setVisibility(View.VISIBLE);
            rlJoinMeeting.setVisibility(View.VISIBLE);
            iamgeHead.setImageResource(R.drawable.contact_customservice);
            ibStartMeeting.setText(R.string.video_call);
            rlVerification.setVisibility(View.GONE);
        } else {
            iamgeHead.setOnClickListener(mbtnHandleEventListener);
        }

        mHandler.sendEmptyMessage(MSG_SET_TEXT);

        if (observeFriendRelation == null) {
            observeFriendRelation = new FriendRelationObserver();
            getContentResolver().registerContentObserver(
                    ProviderConstant.Friend_Relation_URI, true,
                    observeFriendRelation);
        }

        if (observeStrangeRelation == null) {
            observeStrangeRelation = new StrangerMessageObserver();
            getContentResolver().registerContentObserver(
                    ProviderConstant.Strange_Message_URI, true,
                    observeStrangeRelation);
        }

    }

    private void handleView(){
        if(ContactManager.getInstance(this).checkNubeIsCustomService(mContact.getNubeNumber())){
        }else if (null != (ContactManager.getInstance(ContactCardActivity.this)
                .getContactInfoByNubeNumber(mContact.getNubeNumber())).getContactId()
                && !(ContactManager.getInstance(ContactCardActivity.this)
                .getContactInfoByNubeNumber(mContact.getNubeNumber())).getContactId().isEmpty()
                && null != getIntent().getExtras()
                && null != getIntent().getExtras().getString("contactFragment")) {
            //在通讯录中
            friendVeiw();
            rlVerification.setVisibility(View.GONE);
        } else {
            //不在通讯录中
            int result = FriendsManager.getInstance().getFriendRelationByNubeNumber(mContact.getNubeNumber());
            if(result==FriendInfo.RELATION_TYPE_BOTH){
                //已经是好友
                friendVeiw();
            }else if(result==FriendInfo.RELATION_TYPE_NEGATIVE){
                //被请求加为好友状态
                strangerView();
                btnAddFriend.setText(R.string.pass_validation);
                btnAddFriend.setClickable(true);
            }else if(result==FriendInfo.RELATION_TYPE_NONE){
                //无关系状态
                strangerView();
                btnAddFriend.setText(R.string.contact_add_friend_btn);
                btnAddFriend.setClickable(true);
            }else if(result==FriendInfo.RELATION_TYPE_POSITIVE){
                //请求加为好友状态
                strangerView();
//                btnAddFriend.setText("等待验证消息");
//                btnAddFriend.setClickable(false);
                btnAddFriend.setText(R.string.contact_add_friend_btn);
                btnAddFriend.setClickable(true);
            }
        }
    }

    private void friendVeiw(){
        //好友
        rlSendMessage.setVisibility(View.VISIBLE);
        rlJoinMeeting.setVisibility(View.VISIBLE);
        rlAddFriend.setVisibility(View.INVISIBLE);
        btnContactCardDel.setVisibility(View.VISIBLE);
        rlVerification.setVisibility(View.GONE);

        rlDepartment.setVisibility(View.VISIBLE);
        rlPositionalTitle.setVisibility(View.VISIBLE);
        rlDepartmentPhoneNumber.setVisibility(View.VISIBLE);
        vBelowDepartmenTel.setVisibility(View.VISIBLE);
        vBelowPositionalTitle.setVisibility(View.VISIBLE);
        vBelowDepartment.setVisibility(View.VISIBLE);
        if(isFromHpuContact){
            rlDepartmentPhoneNumber .setVisibility(View.GONE);
            rlPositionalTitle.setVisibility(View.GONE);
            btnContactCardDel.setVisibility(View.INVISIBLE);
            if(mNubeNumber.equals(AccountManager.getInstance(this).getNube())){
                rlSendMessage.setVisibility(View.INVISIBLE);
                rlJoinMeeting.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void strangerView(){
        //陌生人
        rlSendMessage.setVisibility(View.INVISIBLE);
        rlJoinMeeting.setVisibility(View.INVISIBLE);
        rlAddFriend.setVisibility(View.VISIBLE);
        btnContactCardDel.setVisibility(View.INVISIBLE);
        rlDepartment.setVisibility(View.GONE);
        rlPositionalTitle.setVisibility(View.GONE);
        rlDepartmentPhoneNumber.setVisibility(View.GONE);
        vBelowDepartmenTel.setVisibility(View.GONE);
        vBelowPositionalTitle.setVisibility(View.GONE);
        vBelowDepartment.setVisibility(View.GONE);

        if (!ContactManager.getInstance(this).checkNubeIsCustomService(
                mContact.getNubeNumber())) {
            //不是视频客服的陌生人，名字前几位显示为**
            tvMeetingRoom.setText(mContact.getNickname()
                    .replace(mContact.getNickname()
                            .substring(0, mContact.getNickname().length() - 1), "**"));
        }

        Cursor c = FriendsManager.getInstance().getFriendValidationMsg(mContact.getNubeNumber());

//        if(null!=c){
//            rlVerification.setVisibility(View.VISIBLE);
//        }else{
//            rlVerification.setVisibility(View.GONE);
//        }

    }

    @Override
    public void todoClick(int id) {
        super.todoClick(id);
        switch (id) {
            case R.id.tv_departmentphonenumber_detail://给科室、公司打电话
                if (!tvOfficeTel.getText().toString().isEmpty()) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //判断是否有拨打电话的权限
                        if (ContactCardActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE) ==
                            PackageManager.PERMISSION_GRANTED) {
                            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + tvOfficeTel.getText().toString()));
                            startActivity(i);
                        } else {

                        }
                    } else {
                        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                            + tvOfficeTel.getText().toString()));
                        startActivity(i);
                    }

                }
                break;
            case R.id.iamgehead://头像点击放大
                if (null != mContact.getHeadUrl() && !mContact.getHeadUrl().equals("")) {
                    Intent intent_inputimage = new Intent(ContactCardActivity.this, OpenBigImageActivity.class);
                    intent_inputimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                    intent_inputimage.putExtra(OpenBigImageActivity.DATE_URL, mContact.getHeadUrl());
                    startActivity(intent_inputimage);
                } else {
                    CustomToast.show(ContactCardActivity.this, getString(R.string.pic_address_null), 1);
                }
                break;
            case R.id.btn_add_friend://添加好友
                AccountManager.TouristState touristState = AccountManager.getInstance(ContactCardActivity.this).getTouristState();
                if (touristState == AccountManager.TouristState.TOURIST_STATE) {
                    CustomDialog1.Builder builder = new CustomDialog1.Builder(ContactCardActivity.this);
                    builder.setMessage(R.string.only_register_can_user_login_again);
                    builder.setPositiveButton(R.string.btn_cancle, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton(R.string.login_or_register,
                            new android.content.DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Intent intent = new Intent();
                                    intent.setClass(ContactCardActivity.this, LoginActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            });
                    builder.create().show();
                } else {
                    int result = FriendsManager.getInstance().getFriendRelationByNubeNumber(mContact.getNubeNumber());

                    if(result==FriendInfo.RELATION_TYPE_BOTH){
                        //已经是好友
                    }else if(result==FriendInfo.RELATION_TYPE_NEGATIVE){
                        //被请求加为好友状态
                        if(mContact.getNubeNumber().equals(AccountManager.getInstance(ContactCardActivity.this).getNube())){
                            CustomToast.show(ContactCardActivity.this, R.string.cannot_add_self_friend,Toast.LENGTH_LONG);
                        }else{
                            setFriendInfo();
                            friendInfo.setRelationType(FriendInfo.RELATION_TYPE_BOTH);

                            ContactCardActivity.this.showLoadingView(getString(R.string.adding_friend), new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    dialog.dismiss();
                                    CustomToast.show(ContactCardActivity.this, getString(R.string.cancle_add),
                                            Toast.LENGTH_LONG);
                                    FriendSync.getInstance(ContactCardActivity.this).cancelAdd();
                                }
                            });

                            FriendSync.getInstance(ContactCardActivity.this).addFriend(friendInfo,new AddFriendCallback(){
                                @Override
                                public void onFinished(ResponseEntry result) {
                                    ContactCardActivity.this.removeLoadingView();
                                    if(result.status==0){
                                        CustomLog.d(TAG,getString(R.string.add_friend_seccess));
                                        CustomToast.show(ContactCardActivity.this,getString(R.string.add_friend_seccess), Toast.LENGTH_LONG);
                                        mHandler.sendEmptyMessage(MSG_FRIEND_CHAT);
                                    }else if(result.status==-3){
                                        CustomLog.d(TAG,"好友已存在");
                                        CustomToast.show(ContactCardActivity.this,getString(R.string.add_friend_seccess), Toast.LENGTH_LONG);
                                    }else{
                                        CustomLog.d(TAG,"添加好友失败 result.status="+result.status);
                                        CustomToast.show(ContactCardActivity.this,getString(R.string.add_friend_fail),Toast.LENGTH_LONG);
                                    }
                                }
                            });
                        }

                    }else if(result==FriendInfo.RELATION_TYPE_NONE){
                        //无关系状态
                        if(mContact.getNubeNumber().equals(AccountManager.getInstance(ContactCardActivity.this).getNube())){
                            CustomToast.show(ContactCardActivity.this, getString(R.string.cannot_add_self_friend),Toast.LENGTH_LONG);
                        }else{
                            Intent addFriendIntent = new Intent();
                            addFriendIntent.setClass(this, VerificationActivity.class);
                            addFriendIntent.putExtra("groupName",groupName);
                            addFriendIntent.putExtra("nubeNumber",mContact.getNubeNumber());
                            startActivityForResult(addFriendIntent,ContactTransmitConfig.REQUEST_VERIFICATION_CODE);
                        }
                    }else if(result==FriendInfo.RELATION_TYPE_POSITIVE){
                        //请求加为好友状态
                        if(mContact.getNubeNumber().equals(AccountManager.getInstance(ContactCardActivity.this).getNube())){
                            CustomToast.show(ContactCardActivity.this, getString(R.string.cannot_add_self_friend),Toast.LENGTH_LONG);
                        }else{
                            Intent addFriendIntent = new Intent();
                            addFriendIntent.setClass(this, VerificationActivity.class);
                            addFriendIntent.putExtra("groupName",groupName);
                            addFriendIntent.putExtra("nubeNumber",mContact.getNubeNumber());
//                        addFriendIntent.putExtra("retry","retry");
                            startActivityForResult(addFriendIntent,ContactTransmitConfig.REQUEST_VERIFICATION_CODE);
                        }
                    }
                }
                break;
            case R.id.tvphonedetail: //给手机号打电话
                if (!tvPhoneDetail.getText().toString().isEmpty()) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //判断是否有拨打电话的权限
                        if (ContactCardActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE) ==
                            PackageManager.PERMISSION_GRANTED) {
                            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + tvPhoneDetail.getText().toString()));
                            startActivity(i);
                        } else {

                        }
                    } else {
                        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                            + tvPhoneDetail.getText().toString()));
                        startActivity(i);
                    }

                }
                break;
            case R.id.ibstartmeeting://创建会诊
                MobclickAgent
                    .onEvent(MedicalApplication.shareInstance().getApplicationContext(),
                        AnalysisConfig.CLICK_MEETING_IN_CONTACTCARD);
                createMeeting();
                break;
            case R.id.btn_sendmessage://发送消息
                enterChatActivity();
                break;

            case R.id.btncontactcarddel://删除联系人
                if (null != mContact.getNubeNumber()) {
                    showDelContactDialog();
                }
                break;
            case R.id.btncontactcardback://返回
                if (create != null) {
                    create.cancel();
                }
                if (searchUsers != null) {
                    searchUsers.cancel();
                }
                ContactCardActivity.this.removeLoadingView();
                Intent intent = new Intent();
                setResult(ContactTransmitConfig.RESULT_CARD_CODE, intent);

                this.finish();
                break;
            case R.id.btn_verification_reply:
                Intent replyIntent = new Intent();
                replyIntent.setClass(ContactCardActivity.this,VerificationReplyDialog.class);
                replyIntent.putExtra("nubeNumber",mContact.getNubeNumber());
                startActivityForResult(replyIntent,ContactTransmitConfig.REQUEST_REPLY_CODE);
                break;
            default:
                break;
        }
    }

    //删除联系人dialog
    private void showDelContactDialog() {
        final ContactDeleteDialog cdd = new ContactDeleteDialog(
            ContactCardActivity.this, R.style.contact_del_dialog);
        cdd.setOkClickListener(new OkClickListener() {
            @Override
            public void clickListener() {

                ContactCardActivity.this.showLoadingView(getString(R.string.deleteing_friend), new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        CustomToast.show(ContactCardActivity.this, getString(R.string.cancel_delete),
                                Toast.LENGTH_LONG);
                        FriendSync.getInstance(ContactCardActivity.this).cancelDelete();
                    }
                });

                //删除好友
                FriendSync.getInstance(ContactCardActivity.this)
                        .deleteFriend(mContact.getNubeNumber(),
                        new DeleteFriendCallback(){
                            @Override
                            public void onFinished(ResponseEntry result) {
                                ContactCardActivity.this.removeLoadingView();
                                if(result.status==0){
                                    CustomLog.d(TAG,"删除好友成功");
                                    //删除好友后删除联系人
                                    deleteContact();
                                }else if(result.status==-2){
                                    CustomLog.d(TAG,"好友已删除，删除联系人");
                                    //删除联系人
                                    deleteContact();
                                }else if(result.status==-4){
                                    CustomLog.d(TAG,"好友不存在，删除联系人");
                                    //删除联系人
                                    deleteContact();
                                }
                                else{
                                    CustomLog.d(TAG,"删除好友失败 result.status="+result.status);
                                    cdd.dismiss();
                                    CustomToast.show(ContactCardActivity.this,getString(R.string.delete_fail),Toast.LENGTH_LONG);
                                }
                            }
                        });
            }
        });
        cdd.setNoClickListener(new NoClickListener() {
            @Override
            public void clickListener() {
                cdd.dismiss();
            }
        });
        Window window = cdd.getWindow();
        window.setGravity(Gravity.BOTTOM);
        cdd.setCanceledOnTouchOutside(true);
        cdd.show();
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = cdd.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        lp.height = (int) (0.22 * display.getHeight()); // 设置高度
        cdd.getWindow().setAttributes(lp);
        WindowManager.LayoutParams wlp = cdd.getWindow().getAttributes();
        wlp.dimAmount = 0.4f;
        cdd.getWindow().setAttributes(wlp);
        cdd.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    private void deleteContact(){
        ContactManager.getInstance(ContactCardActivity.this)
                .logicDeleteContactByNubeNumber(/*ContactManager.getInstance(ContactCardActivity.this)
                                .getContactInfoByNubeNumber(mContact.getNubeNumber())
                                .getContactId()*/mContact.getNubeNumber(),
                        new ContactCallback() {
                            @Override
                            public void onFinished(ResponseEntry result) {
                                CustomLog.i(TAG, "onFinish! status: " + result.status
                                        + " | content: " + result.content);
                                if (result.status >= 0) {
                                    CustomLog.d(TAG, "删除联系人" + mContact.getNubeNumber());
                                    KeyEventWrite
                                            .write(KeyEventConfig.DELETE_CONTACT_FROMDB
                                                    + "_ok"
                                                    + "_"
                                                    + AccountManager.getInstance(
                                                    MedicalApplication.shareInstance()
                                                            .getApplicationContext())
                                                    .getAccountInfo().nube);
                                    CustomToast.show(ContactCardActivity.this, getString(R.string.delete_success), 1);
                                    Intent intent = getIntent();
                                    setResult(ContactTransmitConfig.RESULT_CARD_CODE, intent);

                                    deleThread();
                                    finish();
                                } else {
                                    KeyEventWrite
                                            .write(KeyEventConfig.DELETE_CONTACT_FROMDB
                                                    + "_fail"
                                                    + "_"
                                                    + AccountManager.getInstance(
                                                    MedicalApplication.shareInstance()
                                                            .getApplicationContext())
                                                    .getAccountInfo().nube + "_"
                                                    + result.status + " | content: " + result.content);
                                    CustomLog.d(TAG, "删除联系人" + mContact.getNubeNumber());
                                    CustomToast.show(ContactCardActivity.this, getString(R.string.delete_fail), 1);
                                }
                            }

                        });
    }

    //进入聊天界面
    private void enterChatActivity() {
        if (mContact.getNubeNumber() == null) {
            CustomToast.show(this, getString(R.string.nub_exit), 1);
            return;
        }

        if (mContact.getNubeNumber().length() == 0) {
            CustomToast.show(this, getString(R.string.nub_exit), 1);
            return;
        }

        Intent i = new Intent(ContactCardActivity.this, ChatActivity.class);
        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
            ChatActivity.VALUE_NOTICE_FRAME_TYPE_NUBE);
        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES,
            mContact.getNubeNumber());
        i.putExtra(ChatActivity.KEY_CONVERSATION_SHORTNAME,
            mContact.getName());
        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,
            ChatActivity.VALUE_CONVERSATION_TYPE_SINGLE);
        startActivity(i);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause(){
        super.onPause();
        FriendsManager.getInstance().setMesRead(mNubeNumber);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (observeFriendRelation != null) {
            getContentResolver().unregisterContentObserver(observeFriendRelation);
            observeFriendRelation = null;
        }

        if (observeStrangeRelation != null) {
            getContentResolver().unregisterContentObserver(observeStrangeRelation);
            observeStrangeRelation = null;
        }

    }


    @Override
    public void onBackPressed() {

        if (create != null) {
            create.cancel();
        }

        if (searchUsers != null) {
            searchUsers.cancel();
        }

        ContactCardActivity.this.removeLoadingView();
        Intent intent = new Intent();
        setResult(ContactTransmitConfig.RESULT_CARD_CODE, intent);
        super.onBackPressed();

        this.finish();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        CustomLog.d(TAG, "resultfrom" + resultCode);

        if(resultCode == ContactTransmitConfig.RESULT_REPLY_CODE){
            String reply = data.getStringExtra("reply");
            mHandler.sendEmptyMessage(MSG_SET_TEXT);
            MedicalApplication.getFileTaskManager().sendStrangerMsg(AccountManager.getInstance(this).getNube(),
                    mContact.getNubeNumber(),AccountManager.getInstance(this).getName(),
                    AccountManager.getInstance(this).getAccountInfo().getHeadThumUrl(),reply,true);

            StrangerMessage strangerMessage= new StrangerMessage(
                    mContact.getNubeNumber(),
                    mContact.getHeadUrl(),
                    mContact.getName(),
                    0,
                    reply,
                    String.valueOf(System.currentTimeMillis()),1);

            FriendsManager.getInstance().addStrangerMsg(strangerMessage);

        }

        if(resultCode == ContactTransmitConfig.RESULT_VERIFICATION_CODE){
            String message = data.getStringExtra("message");
            mHandler.sendEmptyMessage(MSG_SET_TEXT);
            setFriendInfo();
            friendInfo.setRelationType(FriendInfo.RELATION_TYPE_POSITIVE);

            MedicalApplication.getFileTaskManager().sendStrangerMsg(AccountManager.getInstance(this).getNube(),
                    mContact.getNubeNumber(),AccountManager.getInstance(this).getName(),
                    AccountManager.getInstance(this).getAccountInfo().getHeadThumUrl(),message,false);

            StrangerMessage strangerMessage= new StrangerMessage(
                    mContact.getNubeNumber(),
                    mContact.getHeadUrl(),
                    mContact.getName(),
                    0,
                    message,
                    String.valueOf(System.currentTimeMillis()),1);

            int addStrangerMessageResult = FriendsManager.getInstance().addStrangerMsg(strangerMessage);

            if(addStrangerMessageResult==0){
                CustomLog.d(TAG,"addStrangerMsg 成功");
            }else{
                CustomLog.d(TAG,"addStrangerMsg 失败， addStrangerMessageResult："+addStrangerMessageResult);
            }

            int addFriendResult = FriendsManager.getInstance().addFriend(friendInfo);

            if(addFriendResult==0){
                CustomLog.d(TAG,"addFriend 成功");
            }else{
                CustomLog.d(TAG,"addFriend 失败， addFriendResult："+addFriendResult);
            }

        }
    }

    private void setFriendInfo(){
        friendInfo = new FriendInfo();
        friendInfo.setNubeNumber(mContact.getNubeNumber());
        friendInfo.setName(mContact.getName());
        friendInfo.setHeadUrl(mContact.getHeadUrl());
        if(null!=mContact.getEmail()){
            friendInfo.setEmail(mContact.getEmail());
        }
        if(null!=mContact.getNumber()){
            friendInfo.setNumber(mContact.getNumber());
        }
        friendInfo.setWorkUnitType(String.valueOf(mContact.getWorkUnitType()));
        friendInfo.setWorkUnit(mContact.getWorkUnit());
        friendInfo.setDepartment(mContact.getDepartment());
        friendInfo.setProfessional(mContact.getProfessional());
        friendInfo.setOfficeTel(mContact.getOfficeTel());
        if (searchType == 1) {
            mContact.setUserFrom(2);
        } else if (searchType == 2) {
            mContact.setUserFrom(3);
        } else if (searchType == 3) {
            mContact.setUserFrom(0);
        } else if (searchType == 4) {
            mContact.setUserFrom(4);
        } else if (searchType == 5) {
            mContact.setUserFrom(5);
        } else if (searchType == 6) {
            mContact.setUserFrom(6);
        } else if (searchType == 7) {
            mContact.setUserFrom(1);
        }
        friendInfo.setUserFrom(Integer.valueOf(mContact.getUserFrom()));
        friendInfo.setIsDeleted(FriendInfo.NOT_DELETE);

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
            CustomLog.d(TAG,"好友关系数据库数据发生变更");

            if(null!=mContact.getNubeNumber()
                    &&currentFriendRelation!=FriendsManager.getInstance().getFriendRelationByNubeNumber(mContact.getNubeNumber())){
                init();
            }
        }

    }

    /**
     * 监听陌生人消息表
     */
    private class StrangerMessageObserver extends ContentObserver{

        public StrangerMessageObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG,"陌生人消息数据库数据发生变更");

            if(null != getIntent().getExtras()
                    && null != getIntent().getExtras().getString("contactFragment")){
            }else{
                mHandler.sendEmptyMessage(MSG_SET_TEXT);
            }

        }
    }

    //删除该好友时，同时删除与好友相关的会话
    private void deleThread(){
        ThreadsDao dao = new ThreadsDao(ContactCardActivity.this);
        ThreadsBean bean =  dao.getThreadByRecipentIds(mContact.getNubeNumber());
        if(bean != null){
            dao.deleteThread(bean.getId());
        }
    }

}
