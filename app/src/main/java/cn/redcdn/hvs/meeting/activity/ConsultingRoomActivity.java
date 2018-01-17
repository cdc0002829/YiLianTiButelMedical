package cn.redcdn.hvs.meeting.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInfo;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInfomation;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInfomation;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.HoldMutiMeetingActivity;
import cn.redcdn.hvs.im.activity.SelectLinkManActivity;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.meeting.adapter.HistoryListAdapter;
import cn.redcdn.hvs.meeting.adapter.MeetingListAdapter;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.meeting.util.LogUtil;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.PopDialogActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.jmeetingsdk.MeetingInfo;
import cn.redcdn.jmeetingsdk.MeetingItem;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

import static android.R.attr.data;
import static android.content.ContentValues.TAG;
import static cn.redcdn.hvs.MedicalApplication.context;

public class ConsultingRoomActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private LinearLayout noMeetingLayout = null;
    private LinearLayout netRequestionLayout = null;
    private LinearLayout refreshBtn = null;
    private String token = null;
    private List<GetMeetingInfo> list = null;
    //没有用到的就删除
    // private Button meetingListBack = null;
    // private Boolean isAddCliclk = false;
    // private TextView meetingAddTv = null;
    // private ImageView iv = null;
    // private MeetingListAdapter ma = null;
    // private List<MeetingItem> mDatas;
    private ListView historyLv;
    private List<String> historyMeeting;
    private boolean hasHostoryMeetingList = false;
    private String[] historyList;
    private boolean fisrtInitHostory = true;
    private List<String> middleList = null;
    private String TAG = getClass().getName();
    private RecyclerView mRecyclerView;
    private MeetingListAdapter mAdapter;
    private RelativeLayout conferenceMeetingLayout;
    private EditText meetingidInputEdit;
    /**
     * 选择联系人返回
     */
    private static final int REQUEST_SELECT_LINK = 1;
    //用于右上角下拉菜单
    private List<PopDialogActivity.MenuInfo> moreInfo;
    // private MedicalMeetingManage medicalmeetingmanage;
    private Button joinMeeting;
    private State state = State.NONE;
    private Boolean isInputID;
    //历史记录
    private HistoryListAdapter adapter;


    enum MeetingListResult {
        meetinglist, noMeeting, netRequestion, waitting
    }


    public enum State {
        NONE, CREATEING, JION, GETNOWMEETING, GETMEETINGINFO
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {//设置布局添加监听
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_activity_my_consulting_room);
        initTitelBar();
        initView();
    }


    @Override protected void onStart() {
        super.onStart();
        getNowmeetings();
    }


    private void initTitelBar() {
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(R.string.consult_meeting_txt);
        titleBar.enableRightBtn("", R.drawable.meeting_title_more,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CommonUtil.isFastDoubleClick()) {
                        return;
                    }
                    showMoreTitle();
                }
            });
    }


    private void initView() {
        noMeetingLayout = (LinearLayout) findViewById(R.id.nomeeting_layout);
        netRequestionLayout = (LinearLayout) findViewById(R.id.netquestion_layout);
        refreshBtn = (LinearLayout) findViewById(R.id.meetingrefresh_btn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNowmeetings();
            }
        });
        // medicalmeetingmanage = MedicalMeetingManage.getInstance();
        conferenceMeetingLayout = (RelativeLayout) findViewById(R.id.conference_meeting_layout);
        conferenceMeetingLayout.setOnClickListener(this);
        joinMeeting = (Button) findViewById(R.id.meeting_consult_meetining_join_meeting);
        joinMeeting.setOnClickListener(this);
        meetingidInputEdit = (EditText) findViewById(R.id.meetingid_input_edit);
        // meetingidInputEdit.setOnClickListener(this);
        meetingidInputEdit.setOnTouchListener(this);
        meetingidInputEdit.setEnabled(true);
        meetingidInputEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }


            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                if (!(TextUtils.getTrimmedLength(meetingidInputEdit.getText().toString()) == 8)) {
                    joinMeeting.setEnabled(false);
                } else {
                    joinMeeting.setEnabled(true);
                }
            }
        });
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setOnTouchListener(this);
        findViewById(R.id.activity_my_consulting_room).setOnClickListener(this);
        findViewById(R.id.activity_my_consulting_room).setOnTouchListener(this);
        historyLv = (ListView) findViewById(R.id.meeting_history_lv);
        historyLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                meetingidInputEdit.setText(middleList.get(arg2));
                CharSequence text = meetingidInputEdit.getText();
                if (text instanceof Spannable) {
                    Spannable spanText = (Spannable) text;
                    Selection.setSelection(spanText, text.length());
                }
                // historyLv.bringToFront();
                historyLv.setVisibility(View.INVISIBLE);
            }
        });
        if (fisrtInitHostory) {
            initHostoryList();
            fisrtInitHostory = false;
        }
        setResultUI(MeetingListResult.waitting, new ArrayList<MeetingItem>());
    }


    @Override protected void onResume() {
        super.onResume();

    }


    private void getNowmeetings() {
        CustomLog.i(TAG, "获取会议邀请列表getNowmeetings");
        int result = MedicalMeetingManage.getInstance().getNowMeetings(
            new MedicalMeetingManage.OnGetNowMeetignListener() {
                @Override
                public void onGetNowMeeting(final List<MeetingItem> meetingInfos, int valueCode) {
                    CustomLog.i(TAG, "获取会议列表回调::onGetNowMeeting");
                    removeLoadingView();
                    state = State.NONE;
                    if (meetingInfos != null) {
                        int size = meetingInfos.size();
                        if (size > 0) {
                            setResultUI(MeetingListResult.meetinglist, meetingInfos);
                        } else {
                            setResultUI(MeetingListResult.noMeeting, meetingInfos);
                        }
                    } else {
                        setResultUI(MeetingListResult.waitting, meetingInfos);
                    }
                    // if (valueCode == 0) {
                    //     if (meetingInfos == null || meetingInfos.size() == 0) {
                    //         CustomToast.show(ConsultingRoomActivity.this, "暂无会诊", CustomToast.LENGTH_SHORT);
                    //         return;
                    //     }
                    //     mDatas = new ArrayList<MeetingItem>();
                    //     mDatas = meetingInfos;
                    //     mRecyclerView.setAdapter(
                    //         mAdapter = new MeetingListAdapter(ConsultingRoomActivity.this,
                    //             meetingInfos, ConsultingRoomActivity.this));
                    //     mAdapter.setOnItemClickListener(
                    //         new MeetingListAdapter.OnRecyclerViewItemClickListener() {
                    //             @Override
                    //             public void onItemClick(View view, MeetingItem meetingItem) {
                    //                 CustomLog.i(TAG, "加入会议，会议号为==" + data);
                    //                 if (meetingItem.meetingType == 2) {
                    //                     switchToDetail(meetingItem);
                    //                 } else if (meetingItem.meetingType == 1) {
                    //                     joinMeeing(meetingItem.meetingId);
                    //                 }
                    //             }
                    //         });
                    // } else {
                    //     CustomToast.show(ConsultingRoomActivity.this, "获取会诊列表失败", CustomToast.LENGTH_SHORT);
                    // }
                }
            });
        if (result == 0) {
            showLoadingView(getString(R.string.getting_consultation_tab), new DialogInterface.OnCancelListener() {
                @Override public void onCancel(DialogInterface dialog) {
                }
            });
        } else {
            if (result == -6) {//网络异常
                setResultUI(MeetingListResult.netRequestion, new ArrayList<MeetingItem>());
            } else {//其他原因获取会议列表失败
                setResultUI(MeetingListResult.waitting, new ArrayList<MeetingItem>());
            }
        }
    }


    private void switchToDetail(MeetingItem meetingitem) {
        BookMeetingExInfo meetingfo = new BookMeetingExInfo();
        meetingfo.setBookNube(meetingitem.creatorId);
        meetingfo.setBookName(meetingitem.creatorName);
        meetingfo.setMeetingRoom(meetingitem.meetingId);
        meetingfo.setMeetingTheme(meetingitem.topic);
        meetingfo.setMeetingTime(Long.parseLong(meetingitem.createTime) * 1000);
        meetingfo.setHasMeetingPassWord(meetingitem.hasMeetingPwd);
        Intent i = new Intent(ConsultingRoomActivity.this,
            ReserveSuccessActivity.class);
        i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, meetingfo);
        startActivity(i);
    }


    private void joinMeeing(String meetingId) {
        int i = MedicalMeetingManage.getInstance().joinMeeting(meetingId,
            new MedicalMeetingManage.OnJoinMeetingListener() {
                @Override
                public void onJoinMeeting(String valueDes, int valueCode) {
                    state = State.NONE;
                    removeLoadingView();
                }
            });
        if (i == 0) {
            state = State.JION;
            showLoadingView(getString(R.string.join_consultation));
        } else {
            CustomToast.show(ConsultingRoomActivity.this, getString(R.string.join_consultation_fail),
                CustomToast.LENGTH_SHORT);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.conference_meeting_layout://召开会诊
                historyLv.setVisibility(View.INVISIBLE);
                CustomLog.e(TAG, "Medicalmeetingmanage getInstance()");
                AccountManager mAccountManager = AccountManager.getInstance(
                    MedicalApplication.getContext());
                MDSAccountInfo mAccountInfo = mAccountManager.getAccountInfo();
                if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())){
                    CustomToast.show(ConsultingRoomActivity.this,getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(),CustomToast.LENGTH_SHORT);
                }else {
                    int i = MedicalMeetingManage.getInstance().createMeeting(TAG, new ArrayList<String>(),
                            new MedicalMeetingManage.OnCreateMeetingListener() {
                                @Override
                                public void onCreateMeeting(int valueCode, MeetingInfo meetingInfo) {
                                    if (valueCode == 0) {
                                        CustomLog.i(TAG, "meetingInfo==" + meetingInfo.meetingId);
                                        int res = MedicalMeetingManage.getInstance().joinMeeting(meetingInfo.meetingId,
                                                new MedicalMeetingManage.OnJoinMeetingListener() {
                                                    @Override
                                                    public void onJoinMeeting(String valueDes, int valueCode) {
                                                        state = State.NONE;
                                                        removeLoadingView();
                                                    }
                                                });
                                        if (res == 0) {
                                            state = State.JION;
                                        } else {
                                            CustomToast.show(ConsultingRoomActivity.this,getString(R.string.join_consultation_fail) ,
                                                    CustomToast.LENGTH_SHORT);
                                            state = State.NONE;
                                            removeLoadingView();
                                        }
                                    } else {
                                        state = State.NONE;
                                        removeLoadingView();
                                    }
                                }
                            });
                    if (i == 0) {
                        state = State.CREATEING;
                        showLoadingView(getString(R.string.conveneing_consultation), new DialogInterface.OnCancelListener() {
                            @Override public void onCancel(DialogInterface dialog) {
                            }
                        });
                    }else if (i == MedicalMeetingManage.NOCAMERAPERMISSION){
                        CustomLog.d(TAG, "没有相机权限");
                        CustomToast.show(MedicalApplication.getContext(), context.getResources().getString(R.string.please_turn_on_camera_permissions), CustomToast.LENGTH_SHORT);
                    }
                    else if (i == MedicalMeetingManage.NETWORKINVISIBLE) {
                        CustomToast.show(ConsultingRoomActivity.this, getString(R.string.login_checkNetworkError),
                                Toast.LENGTH_LONG);
                        return;
                    } else {
                        CustomToast.show(this, getString(R.string.convene_consultation_fail), CustomToast.LENGTH_SHORT);
                        CustomLog.i(TAG, "召开会诊失败 i==" + i);
                    }
                }

                break;
            case R.id.meeting_consult_meetining_join_meeting:
                historyLv.setVisibility(View.INVISIBLE);
                String number = meetingidInputEdit.getText().toString();
                if (number.length() != 8) {
                    CustomToast.show(this, getString(R.string.consult_meetingid_cannot_more_than_8_size), CustomToast.LENGTH_SHORT);
                } else {
                    // hideInputView();
                    meetingidInputEdit.setText("");
                    isInputID = true;
                    getMeetingInfo(number, isInputID);
                }
                break;
            case R.id.activity_my_consulting_room:
                InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                historyLv.setVisibility(View.INVISIBLE);
                // historyLv.bringToFront();
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CustomLog.i(TAG, "点击退出键");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    protected void onBack() {
        //TODO
        removeLoadingView();
        if (state == State.CREATEING) {
            MedicalMeetingManage.getInstance().cancelCreateMeeting(
                ConsultingRoomActivity.class.getName());
            state = State.NONE;
        } else if (state == State.JION) {
            MedicalMeetingManage.getInstance().cancelJoinMeeting(getClass().getName());
            state = State.NONE;
        } else if (state == State.GETNOWMEETING) {
            MedicalMeetingManage.getInstance().cancelGetNowMeetings(getClass().getName());
            state = State.NONE;
        } else if (state == State.NONE) {
            finish();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        removeLoadingView();
        state = State.NONE;
    }


    /**
     * 展示标题右侧三点图标内容
     */
    private void showMoreTitle() {
        if (moreInfo == null) {
            moreInfo = new ArrayList<PopDialogActivity.MenuInfo>();
            moreInfo.add(
                new PopDialogActivity.MenuInfo(R.drawable.meeting_consult_meeting_activity_multi,
                    getString(R.string.mutipeoplemeeting_titile_string),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // if (CommonUtil.isFastDoubleClick()) {
                            //     return;
                            // }
                            //TODO:新需求，新建消息跳入选择联系人页面--add at 15/6/18
                            Intent iHoldMeeting = new Intent();
                            iHoldMeeting.setClass(ConsultingRoomActivity.this,
                                HoldMutiMeetingActivity.class);
                            // iHoldMeeting.putExtra("selector", selector);
                            // MedicalApplication.shareInstance().setContactSetImp(mContactSetImp);
                            // CustomToast.show(ConsultingRoomActivity.this, "startActivity HoldMutiMeetingActivity",
                            //     CustomToast.LENGTH_SHORT);
                            startActivity(iHoldMeeting);
                            // Intent i = new Intent();
                            // i.setClass(ConsultingRoomActivity.this,
                            //         HoldMutiMeetingActivity.class);
                            // i.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
                            //     SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                            // // 极会议-增加一个参数区别入口
                            // i.putExtra(SelectLinkManActivity.ACTIVTY_PURPOSE,
                            //     SelectLinkManActivity.NEW_MSG);
                            // i.putExtra(SelectLinkManActivity.KEY_IS_SIGNAL_SELECT,
                            //     true);
                            // i.putStringArrayListExtra(
                            //     SelectLinkManActivity.KEY_SELECTED_NUBENUMBERS,
                            //     new ArrayList<String>());
                            // startActivityForResult(i, REQUEST_SELECT_LINK);
                        }
                    }));
            moreInfo.add(
                new PopDialogActivity.MenuInfo(R.drawable.meeting_consult_meeting_actiivty_reserve,
                    getString(R.string.subscribe_consultation),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(ConsultingRoomActivity.this,
                                ReserveMeetingRoomActivity.class);
                            i.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
                                SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                            startActivityForResult(i, REQUEST_SELECT_LINK);
                            // Intent i = new Intent(ConsultingRoomActivity.this,
                            //     FriendRelationActivity.class);
                            // startActivity(i);
                        }
                    }));
        }
        PopDialogActivity.setMenuInfo(moreInfo);
        startActivity(new Intent(ConsultingRoomActivity.this, PopDialogActivity.class));
    }


    private void getMeetingInfo(final String meetingIdStr, final boolean addPut) {
        final GetMeetingInfomation gi = new GetMeetingInfomation() {
            @Override
            protected void onSuccess(MeetingInfomation responseContent) {
                super.onSuccess(responseContent);
                state = State.NONE;
                ConsultingRoomActivity.this.removeLoadingView();
                //TODO 不知道作用
                // isAddCliclk = false;
                String nube = AccountManager.getInstance(getApplicationContext())
                    .getAccountInfo().getNube();
                String meetingId = meetingIdStr;
                String meetingHost = responseContent.meetingHost;
                int position = 0;
                //将会议记录添加到sharePreference
                if (addPut) {
                    boolean has = false;//是否已存在会议记录中
                    for (int i = 0; i < historyMeeting.size(); i++) {//判断会议列表中术后存在该条记录
                        if (historyMeeting.get(i).equalsIgnoreCase(meetingId)) {//不考虑大小写
                            has = true;
                            position = i;
                            break;
                        }
                    }
                    if (!has) {//会议记录中不存在该条记录
                        hasHostoryMeetingList = true;
                        SharedPreferences sharedPreferences = getSharedPreferences(
                            "HistoryMeetingId", Activity.MODE_PRIVATE);
                        String meetingid = sharedPreferences.getString("MeetingID", "");
                        if (historyMeeting.size() >= 5) {
                            meetingid = meetingid.substring(9) + "|" + meetingId;
                            historyMeeting.remove(0);
                        } else {
                            if (historyMeeting.size() == 0) {
                                meetingid = meetingId;
                            } else {
                                meetingid = meetingid + "|" + meetingId;
                            }
                        }
                        historyMeeting.add(meetingId);
                        SharedPreferences mySharedPreferences = getSharedPreferences(
                            "HistoryMeetingId", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = mySharedPreferences.edit();
                        editor.putString("MeetingID", meetingid);
                        editor.commit();
                        historyList = sharedPreferences.getString("MeetingID", "").split(
                            "|");
                        middleList.clear();
                        for (int i = historyMeeting.size() - 1; i >= 0; i--) {
                            middleList.add(historyMeeting.get(i));
                        }

                        adapter.notifyDataSetChanged();
                    } else {//历史记录中包含该记录   置顶功能
                        if (historyMeeting.size() != 1) {
                            historyMeeting.remove(position);
                            historyMeeting.add(meetingId);
                            middleList.clear();
                            for (int i = historyMeeting.size() - 1; i >= 0; i--) {
                                middleList.add(historyMeeting.get(i));
                            }
                            adapter.notifyDataSetChanged();
                            String meetingid = "";
                            for (int i = 0; i < historyMeeting.size(); i++) {
                                if (i == 0) {
                                    meetingid = historyMeeting.get(0);
                                } else {
                                    meetingid = meetingid + "|" + historyMeeting.get(i);
                                }
                            }
                            SharedPreferences mySharedPreferences = getSharedPreferences(
                                "HistoryMeetingId", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mySharedPreferences.edit();
                            editor.putString("MeetingID", meetingid);
                            editor.commit();
                        }
                    }
                }

                if (responseContent.meetingStatus == 3) {//会议已经结束
                    String MeetingEndText;
                    MeetingEndText = getString(R.string.consultation_has_ended);
                    CustomToast
                        .show(getApplicationContext(), MeetingEndText, Toast.LENGTH_SHORT);
                    return;
                }
                CustomLog.e(TAG, "responseContent.meetingStatus==="
                    + responseContent.meetingStatus);
                CustomLog.e(TAG, "responseContent.expectStarttime==="
                    + responseContent.expectStarttime);
                MDSAccountInfo info = AccountManager.getInstance(
                    getApplicationContext()).getAccountInfo();
                if (info != null) {
                    if (responseContent.meetingType == 2) {//预约会议
                        BookMeetingExInfo meetingfo = new BookMeetingExInfo();
                        meetingfo.setBookNube(responseContent.terminalAccount);
                        meetingfo.setBookName(responseContent.terminalAccountName);
                        meetingfo.setMeetingRoom(String.valueOf(responseContent.meetingId));
                        meetingfo.setMeetingTheme(responseContent.topic);
                        meetingfo.setMeetingUrl(MedicalMeetingManage.JMEETING_INVITE_URL);
                        // meetingfo.setMeetingTime(Long.parseLong(responseContent.expectStarttime) * 1000);
                        meetingfo.setMeetingTime(Long.parseLong(responseContent.yyBeginTime) * 1000);
                        meetingfo.setHasMeetingPassWord(responseContent.hasMeetingPwd);
                        Intent i = new Intent(ConsultingRoomActivity.this,
                            ReserveSuccessActivity.class);
                        i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, meetingfo);
                        startActivity(i);
                    } else {//及时会议
                        if (responseContent.hasMeetingPwd == 0) {//无会议密码  0
                            joinMeeing(String.valueOf(responseContent.meetingId));
                            meetingidInputEdit.setText("");
                        } else if (responseContent.hasMeetingPwd == 1) {//有会议密码  1
                            if (String.valueOf(meetingHost).equalsIgnoreCase(info.getNube())) {//我召开的
                                joinMeeing(String.valueOf(responseContent.meetingId));
                                meetingidInputEdit.setText("");
                            } else {//不是我召开的
                                Intent iv = new Intent();
                                iv.setClass(ConsultingRoomActivity.this, InputMeetingPasswordDialog.class);
                                //TODO 不知道作用
                                iv.putExtra("accesstoken", info.accessToken);
                                iv.putExtra("nubeNumber", info.nube);
                                iv.putExtra("nickName", info.nickName);
                                iv.putExtra("meetingId", meetingId);
                                iv.putExtra("nube", nube);
                                //isInputID 判断是否是从输入框输入的会议号
                                iv.putExtra("isInputID", String.valueOf(isInputID));
                                iv.putExtra("needGroupId", false);
                                iv.putExtra("groupId", "");
                                startActivity(iv);
                            }
                        } else {
                            joinMeeing(String.valueOf(responseContent.meetingId));
                            meetingidInputEdit.setText("");
                            // switchToMeetingRoomActivity(info.accesstoken,info.nubeNumber,info.nickName,Integer.parseInt(meetingId), nube);
                            // setMeetingInfo(meetingId);
                        }
                    }
                    //         switchToMeetingRoomActivity(info.accesstoken,info.nubeNumber,info.nickName,Integer.parseInt(meetingId), nube);
                    //switchToMeetingRoomActivity(Integer.parseInt(meetingId), nube);
                    //          setMeetingInfo(meetingId);
                }
                //        finish();
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                KeyEventWrite.write(KeyEventConfig.CHECK_MEETINGNUM_INVALID
                    + "_fail"
                    + "_"
                    + AccountManager.getInstance(
                    getApplicationContext())
                    .getAccountInfo().nube + "_" + statusCode);
                ConsultingRoomActivity.this.removeLoadingView();
                state = State.NONE;
                //TODO 不知道作用
                // isAddCliclk = false;
                CustomLog.v(TAG, "VerifyMeetingNo onFail statusCode= " + statusCode);
                if (statusCode == -906) {
                    //TODO 不知道作用
                    // isAddCliclk = false;
                    String InvalidAccountText;
                    if (SettingData.runDevice == SettingData.RunDevice.TV) {
                        InvalidAccountText = getString(R.string.meeting_nub_error);
                    } else {
                        InvalidAccountText = getString(R.string.consultation_nub_error);
                    }
                    CustomToast
                        .show(getApplicationContext(), InvalidAccountText, Toast.LENGTH_SHORT);
                    return;
                }
                // if (statusCode == -999) {
                // CustomToast.show(getApplicationContext(), "系统错误",
                // Toast.LENGTH_SHORT);
                // }
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(ConsultingRoomActivity.this, getString(R.string.login_checkNetworkError),
                        Toast.LENGTH_LONG);
                    return;
                }
                if (statusCode == SettingData.getInstance().tokenUnExist
                    || statusCode == SettingData.getInstance().tokenInvalid) {
                    AccountManager.getInstance(getApplicationContext()).tokenAuthFail(
                        statusCode);
                }
                String JoinMeetingFailText;
                if (SettingData.runDevice == SettingData.RunDevice.TV) {
                    JoinMeetingFailText = getString(R.string.join_meeting_fail_code);
                } else {
                    JoinMeetingFailText = getString(R.string.join_consultation_fail);
                }
                CustomToast.show(getApplicationContext(), JoinMeetingFailText + statusCode,
                    Toast.LENGTH_SHORT);
            }
        };
        int result = gi.getMeetingInfomation(Integer.valueOf(meetingIdStr));
        if (result == 0) {
            state = State.GETMEETINGINFO;
            showLoadingView(getString(R.string.querying_consultation_info), new DialogInterface.OnCancelListener() {
                @Override public void onCancel(DialogInterface dialog) {
                    //    TODO 可以下此处进行撤销加入会议
                }
            });
        } else {
            CustomToast.show(ConsultingRoomActivity.this, getString(R.string.get_meeting_info_fail), CustomToast.LENGTH_SHORT);
        }

    }


    /**
     * 手动输入会议号加入会议
     * 根据需求，需查询会议的开始时间，若不是当天开始，则给与提示不直接加入会议
     */
    private void joinMeetingByMeetID(String meetid) {
        // CommonUtil.hideSoftInputFromWindow(getActivity());
        LogUtil.testD_JMeetingManager("meetId: " + meetid);
        if (null != meetid) {
            GetMeetingInfomation info = new GetMeetingInfomation() {
                @Override
                protected void onSuccess(MeetingInfomation responseContent) {
                    super.onSuccess(responseContent);
                    state = State.NONE;
                    removeLoadingView();
                    if (responseContent != null) {
                        //仅当是预约会议时，才进行时间有效性的判断
                        // if(responseContent.meetingType == 2){
                        //     try {
                        //         int dif = DateUtil.realDateIntervalDay(1000*Long.valueOf(responseContent.yyBeginTime), System.currentTimeMillis());
                        //         LogUtil.testD_JMeetingManager("dif: " + dif);
                        //         if(dif > 0){
                        //             CommonUtil.showToast("预约会议尚未开始");
                        //         }else{
                        //             ButelMeetingManager.getInstance().addJoinMeetingMap(meetid, UmengEventConstant.EVENT_MEET_JOIN1);
                        //             int ret = ButelMeetingManager.getInstance().joinMeeting(meetid);
                        //             switch (ret) {
                        //                 case -6:
                        //                     CommonUtil.showToast("网络不给力，请检查网络！");
                        //                     break;
                        //                 case 0:
                        //                     needLoadTag = true;
                        //                     break;
                        //
                        //                 default:
                        //                     CommonUtil.showToast("服务器连接异常，请稍后再试！");
                        //                     break;
                        //             }
                        //         }
                        //     } catch (NumberFormatException e) {
                        //         CommonUtil.showToast("服务器连接异常，请稍后再试！");
                        //         LogUtil.testD_JMeetingManager(e.getMessage().toString());
                        //     }
                        // }else{
                        // int rst = medicalmeetingmanage.joinMeeting(String.valueOf(responseContent.meetingId),
                        //     new MedicalMeetingManage.OnJoinMeetingListener() {
                        //         @Override
                        //         public void onJoinMeeting(String valueDes, int valueCode) {
                        //             state = State.NONE;
                        //             meetingidInputEdit.setText("");
                        //             removeLoadingView();
                        //         }
                        //     });
                        // if (rst == 0) {
                        //     state = State.JION;
                        // }
                        if (responseContent.meetingType == 2) {
                            BookMeetingExInfo meetingfo = new BookMeetingExInfo();
                            meetingfo.setBookNube(responseContent.terminalAccount);
                            meetingfo.setBookName(responseContent.terminalAccountName);
                            meetingfo.setMeetingRoom(String.valueOf(responseContent.meetingId));
                            meetingfo.setMeetingTheme(responseContent.topic);
                            meetingfo.setMeetingTime(Long.parseLong(responseContent.expectStarttime) * 1000);
                            Intent i = new Intent(ConsultingRoomActivity.this,
                                ReserveSuccessActivity.class);
                            i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, meetingfo);
                            startActivity(i);
                        } else if (responseContent.meetingType == 1) {
                            joinMeeing(String.valueOf(responseContent.meetingId));
                            meetingidInputEdit.setText("");
                        }
                    } else {
                        CustomToast.show(ConsultingRoomActivity.this, getString(R.string.get_consultation_info_fail), CustomToast.LENGTH_SHORT);
                    }
                }


                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    state = State.NONE;
                    removeLoadingView();
                    switch (statusCode) {
                        case -906:
                            CommonUtil.showToast(getString(R.string.consultation_nub_error));
                            break;
                        default:
                            CommonUtil.showToast(getString(R.string.server_connect_error_wait_try));
                            break;
                    }
                    LogUtil.testD_JMeetingManager("GetMeetingInfomation code: " + statusCode);
                }
            };
            int result = info.getMeetingInfomation(Integer.valueOf(meetid));
            if (result == 0) {
                state = State.GETMEETINGINFO;
                showLoadingView(getString(R.string.querying_consultation_info), new DialogInterface.OnCancelListener() {
                    @Override public void onCancel(DialogInterface dialog) {

                    }
                });
            }
        }
    }


    private void setResultUI(MeetingListResult mr, List<MeetingItem> meetingInfos) {
        switch (mr) {
            case meetinglist://有会议记录
                netRequestionLayout.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                // mDatas = new ArrayList<MeetingItem>();
                // mDatas = meetingInfos;
                mRecyclerView.setAdapter(
                    mAdapter = new MeetingListAdapter(meetingInfos, ConsultingRoomActivity.this));
                mAdapter.setOnItemClickListener(
                    new MeetingListAdapter.OnRecyclerViewItemClickListener() {
                        @Override
                        public void onItemClick(View view, MeetingItem meetingItem) {
                            CustomLog.i(TAG, "加入会议，会议号为==" + data);
                            historyLv.setVisibility(View.INVISIBLE);
                            if (meetingItem.meetingType == 2) {
                                switchToDetail(meetingItem);
                            } else if (meetingItem.meetingType == 1) {
                                joinMeeing(meetingItem.meetingId);
                            }
                        }
                    });
                noMeetingLayout.setVisibility(View.INVISIBLE);
                netRequestionLayout.setVisibility(View.INVISIBLE);
                break;
            case noMeeting: //没有会议记录
                noMeetingLayout.setVisibility(View.VISIBLE);
                netRequestionLayout.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);
                break;
            case netRequestion: //没有网络
                netRequestionLayout.setVisibility(View.VISIBLE);
                noMeetingLayout.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);
                break;
            case waitting:
                netRequestionLayout.setVisibility(View.INVISIBLE);
                noMeetingLayout.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.INVISIBLE);
                break;
        }

    }


    private void initHostoryList() {
        historyMeeting = new ArrayList<String>();
        middleList = new ArrayList<String>();
        historyList = new String[5];
        SharedPreferences sharedPreferences = getSharedPreferences(
            "HistoryMeetingId", Activity.MODE_PRIVATE);
        String meetingid = sharedPreferences.getString("MeetingID", "");//存放sharePrefence中的字符串
        // adapter = new HistoryListAdapter(historyMeeting, this);
        if (meetingid.equals("")) {
            hasHostoryMeetingList = false;
            adapter = new HistoryListAdapter(middleList, this);
            historyLv.setAdapter(adapter);
        } else {
            hasHostoryMeetingList = true;
            historyList = meetingid.split("\\|");
            for (int i = 0; i < historyList.length; i++) {
                historyMeeting.add(historyList[i]);
            }
            for (int i = historyMeeting.size() - 1; i >= 0; i--) {
                middleList.add(historyMeeting.get(i));
            }
            adapter = new HistoryListAdapter(middleList, this);
            historyLv.setAdapter(adapter);
            // historyLv.bringToFront();
            historyLv.setVisibility(View.INVISIBLE);
        }
    }


    @Override public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP ) {
            switch (v.getId()) {
                // case R.id.id_recyclerview:
                case R.id.activity_my_consulting_room:
                    InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    historyLv.setVisibility(View.INVISIBLE);
                    break;
                case R.id.meetingid_input_edit:
                    if (hasHostoryMeetingList) {
                        if (historyLv.getVisibility() == View.VISIBLE) {
                            historyLv.setVisibility(View.INVISIBLE);
                        } else {
                            historyLv.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN||event.getAction() == MotionEvent.ACTION_MOVE) {
            switch (v.getId()) {
                case R.id.id_recyclerview:
                    InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    historyLv.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        return false;
    }
}
