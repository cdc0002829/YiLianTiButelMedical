package cn.redcdn.hvs.contacts.contact;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.redcdn.buteldataadapter.DataExpand;
import cn.redcdn.datacenter.meetingmanage.CreateMeeting;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.LetterInfo;
import cn.redcdn.hvs.contacts.contact.butelDataAdapter.ContactSetImp;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.contacts.contact.interfaces.ContactCallback;
import cn.redcdn.hvs.contacts.contact.interfaces.ResponseEntry;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.SideBar;
import cn.redcdn.hvs.util.youmeng.AnalysisConfig;
import cn.redcdn.log.CustomLog;

import static android.content.ContentValues.TAG;
import static cn.redcdn.hvs.MedicalApplication.context;
import static cn.redcdn.hvs.util.CommonUtil.getString;


public class HoldMutiMeetingActivity extends BaseActivity {
    // 会诊邀请人视讯号列表，手机号
    private ArrayList<String> phoneId = new ArrayList<String>();
    private LinearLayout llViewPage = null;
    private final int CONTACT_LIST_COLUMN = 8;
    private TextView tvTemp = null;
    private String[] indexStr = { "A", "B", "C", "D", "E", "F", "G", "H",
        "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z", "#" };
    private ContactSetImp mContactSetImp = null;
    private Map<String, DataExpand> mExpandMap;
    private Button btnMutiPeopleMeeting = null;
    private ListView lvMutiPeopleList = null;
    private Button btnMutiBack = null;
    private MutiListViewAdapter holdMeetingAdapter;
    private List<Contact> mMutiTopList = new ArrayList<Contact>();
    private int inviteCount = 0;
    /**** 定义通讯录的ContactPagerAdapterList */
    private ContactPagerAdapterList mContactPagerAdapterList = null;
    /**** 定义通讯录的mViewPagerList */
    private ViewPager mViewPagerList = null;
    private TextView tvSelect = null;
    /*** 定义消息类型 */
    private final int MSG_UPDATAUI = 0x77770000;
    private final int MSG_LOADINGDATA = 0x77770001;
    private CreateMeeting create = null;
    private SideBar mSideBar;
    private List<LetterInfo> letterInfoList= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 去标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        AddContactActivity.recommendCount = 0;
        setContentView(R.layout.activity_heldmutipeoplemeeting);
        start();
    }


    private void start() {
        CustomLog.i(TAG, "start");
        mHandler.sendEmptyMessage(MSG_LOADINGDATA);
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_LOADINGDATA:
                    initMutiData();
                    break;
                case MSG_UPDATAUI:
                    initMutiPeopleMeeting();

                default:
                    break;
            }

        }


        ;
    };


    private void initMutiData() {

        ContactManager.getInstance(HoldMutiMeetingActivity.this).getAllContacts(
            new ContactCallback() {

                @Override
                public void onFinished(ResponseEntry result) {
                    CustomLog.i(TAG, "onFinish! status: " + result.status
                        + " | content: " + result.content);
                    if (result.status >= 0) {
                        mContactSetImp = (ContactSetImp) result.content;
                        updatemExpandMap();
                        mHandler.sendEmptyMessage(MSG_UPDATAUI);
                    }
                }
            }, true);

    }

    private void initMutiPeopleMeeting() {
        mSideBar = (SideBar)findViewById(R.id.sidebar_holdmultimeeting_fragment);
        llViewPage = (LinearLayout) findViewById(R.id.llviewpage);
        tvTemp = (TextView) findViewById(R.id.tvtemp);
        tvTemp.bringToFront();
        tvTemp.setOnClickListener(mbtnHandleEventListener);
        tvSelect = (TextView) findViewById(R.id.tvselect);
        btnMutiBack = (Button) findViewById(R.id.btnmutiback);
        btnMutiPeopleMeeting = (Button) findViewById(R.id.btnmutipeoplemeeting);
        btnMutiPeopleMeeting.setClickable(false);
        btnMutiPeopleMeeting.setOnClickListener(mbtnHandleEventListener);
        btnMutiBack.setOnClickListener(mbtnHandleEventListener);
        lvMutiPeopleList = (ListView) findViewById(R.id.lvmutipeoplelist);
        lvMutiPeopleList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                CustomLog.d(TAG, "ItemClick     " + position);
                ImageView ivSelected = (ImageView) view.findViewById(R.id.cbselect);
                ImageView ivUnSelected = (ImageView) view.findViewById(R.id.cbunselect);
                if (mContactSetImp != null) {
                    CustomLog.d(TAG, "newMutiContact size=" + mContactSetImp.getCount()
                        + "inviteCount=" + inviteCount);
                    if (!mExpandMap.get(((Contact) mContactSetImp.getItem(position))
                        .getNubeNumber()).isSelected) {
                        ivSelected.setVisibility(View.INVISIBLE);
                        ivUnSelected.setVisibility(View.VISIBLE);
                        if (inviteCount == 0) {
                            llViewPage.setVisibility(View.VISIBLE);
                            btnMutiPeopleMeeting.setClickable(true);
                            btnMutiPeopleMeeting.setTextColor(getResources().getColor(R.color.select_linkman_btn_ok_color));
                        }
                        btnMutiPeopleMeeting.setClickable(true);
                        Log.d("itemSelected  ", "count = " + inviteCount + "position = "
                            + position + "mMutiList.get(position)"
                            + ((Contact) mContactSetImp.getItem(position)).getNubeNumber()
                            + ((Contact) mContactSetImp.getItem(position)).getPicUrl());
                        inviteCount++;
                        mMutiTopList.add((Contact) mContactSetImp.getItem(position));
                        if (inviteCount > 99) {
                            btnMutiPeopleMeeting.setText(getString(R.string.btn_ok) + "(99+)");
                        } else {
                            btnMutiPeopleMeeting.setText(getString(R.string.btn_ok) + "(" + inviteCount + ")");
                        }
                        phoneId.add(((Contact) mContactSetImp.getItem(position))
                            .getNubeNumber());
                        mExpandMap.get(((Contact) mContactSetImp.getItem(position))
                            .getNubeNumber()).isSelected = true;
                        holdMeetingAdapter.updateExpandMap(mExpandMap);
                    } else {
                        ivSelected.setVisibility(View.VISIBLE);
                        ivUnSelected.setVisibility(View.INVISIBLE);
                        phoneId.remove(((Contact) mContactSetImp.getItem(position))
                            .getNubeNumber());
                        if (inviteCount > 0) {
                            inviteCount--;
                            mMutiTopList.remove((Contact) mContactSetImp.getItem(position));
                            if (mContactPagerAdapterList != null) {
                                mContactPagerAdapterList.notifyDataSetChanged();
                            }
                            if (inviteCount > 99) {
                                btnMutiPeopleMeeting.setText(getString(R.string.btn_ok) + "(99+)");
                            } else {
                                btnMutiPeopleMeeting.setText(getString(R.string.btn_ok) + "(" + inviteCount + ")");
                            }
                            CustomLog.i(
                                "itemdisSelected  ",
                                "count = "
                                    + inviteCount
                                    + "position = "
                                    + position
                                    + "newMutiContact.get(position)"
                                    + ((Contact) mContactSetImp.getItem(position))
                                    .getNubeNumber());

                        }

                        if (inviteCount == 0) {
                            CustomLog.d(TAG, "test gone...............");
                            btnMutiPeopleMeeting.setClickable(false);
                            llViewPage.setVisibility(View.GONE);
                            btnMutiPeopleMeeting.setClickable(false);
                            btnMutiPeopleMeeting.setTextColor(getResources().getColor(R.color.select_linkman_btn_disable_color));
                            btnMutiPeopleMeeting.setText(getString(R.string.btn_ok));
                        }
                        CustomLog.i(TAG, "inviteCount=" + inviteCount);
                        mExpandMap.get(((Contact) mContactSetImp.getItem(position))
                            .getNubeNumber()).isSelected = false;
                        holdMeetingAdapter.updateExpandMap(mExpandMap);
                    }
                    if (mContactPagerAdapterList != null) {
                        mContactPagerAdapterList.notifyDataSetChanged();
                    }
                }

            }
        });
        mViewPagerList = (ViewPager) findViewById(R.id.invite_list);
        phoneId.clear();
        holdMeetingAdapter = new MutiListViewAdapter(this);
        holdMeetingAdapter.addDataSet(mContactSetImp);
        holdMeetingAdapter.addExpandMap(mExpandMap);
        lvMutiPeopleList.setAdapter(holdMeetingAdapter);
        initListAdapter();
    }


    @Override
    public void todoClick(int id) {
        super.todoClick(id);
        switch (id) {
            case R.id.tvtemp:
                break;
            case R.id.btnmutiback:
                if (create != null) {
                    create.cancel();
                }
                finish();
                break;
            case R.id.btnmutipeoplemeeting:
                if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())){
                    CustomToast.show(HoldMutiMeetingActivity.this, getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
                    break;
                }
                if (inviteCount > 0) {
                    MobclickAgent.onEvent(MedicalApplication.shareInstance()
                            .getApplicationContext(),
                        AnalysisConfig.MULTIPERSON_MEETING_BY_CONTACT);
                    createMeeting();
                } else {
                    CustomToast.show(HoldMutiMeetingActivity.this, getString(R.string.no_select_linkman_cannot_consultation), 1);
                }
                break;
            default:
                break;
        }
    }


    private void createMeeting() {
        CustomLog.i(TAG, "MainActivity::createMeeting() 正在创建会诊！");
        HoldMutiMeetingActivity.this.showLoadingView(getString(R.string.creating_consultation),
            new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    HoldMutiMeetingActivity.this.removeLoadingView();
                    if (create != null) {
                        create.cancel();
                    }
                }
            });
        newExecCreateMeeting();
    }


    private void newExecCreateMeeting() {
        //被邀请人员列表中不应该包含自己
        // phoneId.add(AccountManager.getInstance(HoldMutiMeetingActivity.this)
        //     .getAccountInfo().nube);
        CustomLog.d(TAG, phoneId.toString());
        for (int i = 0; i < phoneId.size(); i++) {
            phoneId.get(i).toString();
        }

        int i = MedicalMeetingManage.getInstance().createMeeting(TAG, phoneId, new MedicalMeetingManage.OnCreateMeetingListener() {
            @Override
            public void onCreateMeeting(int valueCode, final cn.redcdn.jmeetingsdk.MeetingInfo meetingInfo) {
                removeLoadingView();
                if (valueCode == 0) {
                    MedicalMeetingManage.getInstance().joinMeeting(meetingInfo.meetingId, new MedicalMeetingManage.OnJoinMeetingListener() {
                        @Override
                        public void onJoinMeeting(String valueDes, int valueCode) {
                            MedicalMeetingManage manager = MedicalMeetingManage.getInstance();
                            manager.inviteMeeting(phoneId, meetingInfo.meetingId);
                        }
                    });
                }else{
                    CustomToast.show(HoldMutiMeetingActivity.this,getString(R.string.creat_consultation_fail),CustomToast.LENGTH_SHORT);
                }
            }
        });
        if (i == 0) {
            removeLoadingView();
            showLoadingView(getString(R.string.conveneing_consultation));
        }else {
            removeLoadingView();
            CustomToast.show(this, getString(R.string.convene_consultation_fail), CustomToast.LENGTH_SHORT);
        }

    }


    private void initListAdapter() {
        CustomLog.i("initListAdapter", "initListAdapter");
        mContactPagerAdapterList = null;
        mContactPagerAdapterList = new ContactPagerAdapterList(this, mMutiTopList,
            CONTACT_LIST_COLUMN, 1, false);
        mViewPagerList.setAdapter(mContactPagerAdapterList);

        // 设置需要显示的索引栏内容
        mSideBar.setLetter(indexStr);
        // 设置需要显示的提示框
        mSideBar.setTextView(tvSelect);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = CommonUtil.getLetterPosition(letterInfoList, s);
                if (position != -1) {
                    lvMutiPeopleList.setSelection(position);
                }
                mSideBar.setBackgroundColor(Color.parseColor("#e3e4e5"));
            }
        });

    }


    private void updatemExpandMap() {
        mExpandMap = new HashMap<String, DataExpand>();
        for (int i = 0; i < mContactSetImp.getCount(); i++) {
            DataExpand expand = new DataExpand();
            expand.isSelected = false;
            mExpandMap.put(((Contact) mContactSetImp.getItem(i)).getNubeNumber(),
                expand);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {

        super.onBackPressed();
        if (create != null) {
            create.cancel();
        }
        this.finish();
    }

}
