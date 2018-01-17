package cn.redcdn.hvs.meeting;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.HPUGetMyAllListByDate;
import cn.redcdn.datacenter.hpucenter.data.CSLInfo;
import cn.redcdn.datacenter.hpucenter.data.MyAllCslListInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInfomation;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInfomation;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.HoldMutiMeetingActivity;
import cn.redcdn.hvs.im.activity.SelectLinkManActivity;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.dao.DtNoticesDao;
import cn.redcdn.hvs.im.provider.ProviderConstant;
import cn.redcdn.hvs.meeting.activity.InputMeetingPasswordDialog;
import cn.redcdn.hvs.meeting.activity.MeetingRoomActivity;
import cn.redcdn.hvs.meeting.activity.ReserveMeetingRoomActivity;
import cn.redcdn.hvs.meeting.activity.ReserveSuccessActivity;
import cn.redcdn.hvs.meeting.adapter.HistoryListAdapter;
import cn.redcdn.hvs.meeting.adapter.MedicalExpandableListViewAdapter;
import cn.redcdn.hvs.meeting.adapter.MeetingListAdapter;
import cn.redcdn.hvs.meeting.bean.Genre;
import cn.redcdn.hvs.meeting.bean.JointCoonsultatioonRoomBean;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.requesttreatment.ReserveDTActivity;
import cn.redcdn.hvs.responsedt.activity.DepartmentResponseDtActivity;
import cn.redcdn.hvs.revolutiondt.activity.RevolutionDTRoomActivity;
import cn.redcdn.hvs.udtcenter.activity.UDTRoomActivity;
import cn.redcdn.hvs.udtroom.configs.UDTDataConstant;
import cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.PopDialogActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.jmeetingsdk.MeetingInfo;
import cn.redcdn.jmeetingsdk.MeetingItem;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

import static android.R.attr.data;
import static com.tencent.open.utils.Global.getSharedPreferences;

/**
 * Created by chenghb on 2017/7/11.
 */

public class MeetingFragment extends BaseFragment implements View.OnClickListener, View.OnTouchListener {
    private static final int MSG_MESSAGE_SHARE_CONTACT_CHANGED =100 ;
    private View contentView = null;
    private LinearLayout noMeetingLayout = null;
    private LinearLayout netRequestionLayout = null;
    private LinearLayout refreshBtn = null;
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
    private Button joinMeeting;
    private MeetingFragment.State state = MeetingFragment.State.NONE;
    private Boolean isInputID;
    //历史记录
    private HistoryListAdapter adapter;
    private boolean isFirstLoading = true;
    private LinearLayout linearLayout;
    private ScrollView scrollView;
    private RelativeLayout seeDoctorRl, ReceiveDoctorRl, JointClinicRl,zhuanZhenRl;
    private ExpandableListView expandRecyclerview;
    private RefreshLayout swiper;
    private Genre genre1;
    private Genre genre;
    private MedicalExpandableListViewAdapter expendAdapter;
    private DtNoticesDao dtNoticesDao;
    private ShareContactObserver shareContactObserver;
    private SmartRefreshLayout refresh;
    private List<CSLInfo> cslInfos;
    private List<CSLInfo> seekInfos;
    private View myLine;


    enum MeetingListResult {
        meetinglist, noMeeting, netRequestion, waitting
    }

    public enum State {
        NONE, CREATEING, JION, GETNOWMEETING, GETMEETINGINFO
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dtNoticesDao = new DtNoticesDao(getActivity());
        contentView = inflater.inflate(R.layout.meeting_activity_my_consulting_room, null);
        linearLayout = (LinearLayout) contentView.findViewById(R.id.LinearLayout1);
        scrollView = (ScrollView) contentView.findViewById(R.id.scrollview);
        initView();
        if(shareContactObserver == null){
            shareContactObserver = new ShareContactObserver();
            getActivity().getContentResolver().registerContentObserver(ProviderConstant.NETPHONE_HPU_NOTICE_URI,true,shareContactObserver);
        }
        return contentView;
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void onStart() {
        super.onStart();
    }




    private void getNowmeetings1() {
        CustomLog.i(TAG, "获取会议邀请列表getNowmeetings");
        int result = MedicalMeetingManage.getInstance().getNowMeetings(
                new MedicalMeetingManage.OnGetNowMeetignListener() {
                    @Override
                    public void onGetNowMeeting(final List<MeetingItem> meetingInfos, int valueCode) {
                        CustomLog.i(TAG, "获取会议列表回调::onGetNowMeeting");
                        isFirstLoading = false;
                        state = MeetingFragment.State.NONE;
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
                    }
                });
        if (result == 0) {

        } else {
            if (result == -6) {//网络异常
                setResultUI(MeetingListResult.netRequestion, new ArrayList<MeetingItem>());
            } else {//其他原因获取会议列表失败
                setResultUI(MeetingListResult.waitting, new ArrayList<MeetingItem>());
            }
        }
    }

    protected void initView() {
        myLine = contentView.findViewById(R.id.my_line);
        swiper = (RefreshLayout) contentView.findViewById(R.id.swipe_meeiting);
        refresh = (SmartRefreshLayout) contentView.findViewById(R.id.refresh);
        swiper.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                    getNowmeetings1();
                    swiper.finishRefresh();
            }
        });
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getDate();
            }
        });
        seeDoctorRl = (RelativeLayout) contentView.findViewById(R.id.see_doctor_rl);
        ReceiveDoctorRl = (RelativeLayout) contentView.findViewById(R.id.receive_doctor_rl);
        JointClinicRl = (RelativeLayout) contentView.findViewById(R.id.joint_clinic_rl);
        zhuanZhenRl = (RelativeLayout) contentView.findViewById(R.id.joint_zhuanzhen_rl);
        expandRecyclerview = (ExpandableListView) contentView.findViewById(R.id.expandablelist_view);
        expendAdapter = new MedicalExpandableListViewAdapter(getActivity());
        expandRecyclerview.setGroupIndicator(null);
        seeDoctorRl.setOnClickListener(this);
        ReceiveDoctorRl.setOnClickListener(this);
        JointClinicRl.setOnClickListener(this);
        zhuanZhenRl.setOnClickListener(this);
        noMeetingLayout = (LinearLayout) contentView.findViewById(R.id.nomeeting_layout);
        netRequestionLayout = (LinearLayout) contentView.findViewById(R.id.netquestion_layout);
        refreshBtn = (LinearLayout) contentView.findViewById(R.id.meetingrefresh_btn);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNowmeetings();
            }
        });
        conferenceMeetingLayout = (RelativeLayout) contentView.findViewById(R.id.conference_meeting_layout);
        conferenceMeetingLayout.setOnClickListener(this);
        joinMeeting = (Button) contentView.findViewById(R.id.meeting_consult_meetining_join_meeting);
        joinMeeting.setOnClickListener(this);
        meetingidInputEdit = (EditText) contentView.findViewById(R.id.meetingid_input_edit);
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
                if (!(TextUtils.getTrimmedLength(meetingidInputEdit.getText().toString()) == 8)) {
                    joinMeeting.setEnabled(false);
                } else {
                    joinMeeting.setEnabled(true);
                }
            }
        });
        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setOnTouchListener(this);
        contentView.findViewById(R.id.activity_my_consulting_room).setOnClickListener(this);
        contentView.findViewById(R.id.activity_my_consulting_room).setOnTouchListener(this);
        historyLv = (ListView) contentView.findViewById(R.id.meeting_history_lv);
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
                historyLv.setVisibility(View.INVISIBLE);
            }
        });
        if (fisrtInitHostory) {
            initHostoryList();
            fisrtInitHostory = false;
        }
        setResultUI(MeetingListResult.waitting, new ArrayList<MeetingItem>());
    }

    @Override
    public void onStop() {
        super.onStop();
        removeLoadingView();
        state = MeetingFragment.State.NONE;
    }

    private void initTitelBarTwo() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.meeting);
        titleBar.enableRightBtn("", R.drawable.home_selector,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtil.isFastDoubleClick()) {
                            return;
                        }
                        showMoreTitleTwo();
                    }
                });
    }

    private void showMoreTitleTwo() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), MeetingRoomActivity.class);
        startActivity(intent);
    }


    private void initTitelBar() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(R.string.my_consultation);
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
                                    //TODO:新需求，新建消息跳入选择联系人页面--add at 15/6/18
                                    Intent iHoldMeeting = new Intent();
                                    iHoldMeeting.setClass(getActivity(),
                                            HoldMutiMeetingActivity.class);
                                    startActivity(iHoldMeeting);
                                }
                            }));
            moreInfo.add(
                    new PopDialogActivity.MenuInfo(R.drawable.meeting_consult_meeting_actiivty_reserve,
                            getString(R.string.subscribe_consultation),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent i = new Intent(getActivity(),
                                            ReserveMeetingRoomActivity.class);
                                    i.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
                                            SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                                    startActivityForResult(i, REQUEST_SELECT_LINK);
                                }
                            }));
        }
        PopDialogActivity.setMenuInfo(moreInfo);
        startActivity(new Intent(getActivity(), PopDialogActivity.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.conference_meeting_layout://召开会诊
                historyLv.setVisibility(View.INVISIBLE);
                CustomLog.d(TAG, "Medicalmeetingmanage getInstance()");
                if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                    CustomToast.show(getActivity(), getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
                } else {
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
                                                        state = MeetingFragment.State.NONE;
                                                        removeLoadingView();
                                                    }
                                                });
                                        if (res == 0) {
                                            state = MeetingFragment.State.JION;
                                        } else {
                                            CustomToast.show(getActivity(), getString(R.string.join_consultation_fail),
                                                    CustomToast.LENGTH_SHORT);
                                            state = MeetingFragment.State.NONE;
                                            removeLoadingView();
                                        }
                                    } else {
                                        state = MeetingFragment.State.NONE;
                                        removeLoadingView();
                                    }
                                }
                            });
                    if (i == 0) {
                        state = MeetingFragment.State.CREATEING;


                        showLoadingView(getString(R.string.conveneing_consultation), new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {

                            }
                        }, true);
                    } else if (i == MedicalMeetingManage.NETWORKINVISIBLE) {
                        CustomToast.show(getActivity(), getString(R.string.login_checkNetworkError),
                                Toast.LENGTH_LONG);
                        return;
                    } else {
                        CustomToast.show(getActivity(), getString(R.string.convene_consultation_fail), CustomToast.LENGTH_SHORT);
                        CustomLog.i(TAG, "召开会诊失败 i==" + i);
                    }
                }
                break;
            case R.id.meeting_consult_meetining_join_meeting:
                historyLv.setVisibility(View.INVISIBLE);
                String number = meetingidInputEdit.getText().toString();
                if (number.length() != 8) {
                    CustomToast.show(getActivity(), getString(R.string.consult_meetingid_cannot_more_than_8_size), CustomToast.LENGTH_SHORT);
                } else {
                    if (MedicalMeetingManage.getInstance().getActiveMeetingId().equals(number) || TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                        meetingidInputEdit.setText("");
                        isInputID = true;
                        getMeetingInfo(number, isInputID);
                    } else {
                        CustomToast.show(getActivity(), getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
                    }
                }
                break;
            case R.id.activity_my_consulting_room:
                InputMethodManager imm = (InputMethodManager)
                        getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                historyLv.setVisibility(View.INVISIBLE);
                break;
            case R.id.see_doctor_rl:
                Intent intentAbout = new Intent();
                intentAbout.setClass(getActivity(), ReserveDTActivity.class);
                startActivity(intentAbout);
                break;
            case R.id.receive_doctor_rl:
                Intent reiceiveDoctorIntent = new Intent();
                reiceiveDoctorIntent.setClass(getActivity(), DepartmentResponseDtActivity.class);
                startActivity(reiceiveDoctorIntent);
                break;
            case R.id.joint_clinic_rl:
                Intent jointClinicIntent = new Intent();
                jointClinicIntent.setClass(getActivity(), UDTRoomActivity.class);
                startActivity(jointClinicIntent);
                break;
            case R.id.joint_zhuanzhen_rl:
                Intent jointZhuanzhenInetent =new Intent();
                jointZhuanzhenInetent.setClass(getActivity(), RevolutionDTRoomActivity.class);
                startActivity(jointZhuanzhenInetent);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            switch (v.getId()) {
                case R.id.activity_my_consulting_room:
                    InputMethodManager imm = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            switch (v.getId()) {
                case R.id.id_recyclerview:
                    InputMethodManager imm = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    historyLv.setVisibility(View.INVISIBLE);
                    break;
            }
        }
        return false;
    }

    private void getNowmeetings() {
        CustomLog.i(TAG, "获取会议邀请列表getNowmeetings");
        int result = MedicalMeetingManage.getInstance().getNowMeetings(
                new MedicalMeetingManage.OnGetNowMeetignListener() {
                    @Override
                    public void onGetNowMeeting(final List<MeetingItem> meetingInfos, int valueCode) {
                        CustomLog.i(TAG, "获取会议列表回调::onGetNowMeeting");
                        if (isFirstLoading) {
                            removeLoadingView();
                        }
                        isFirstLoading = false;
                        state = MeetingFragment.State.NONE;
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
                    }
                });
        if (result == 0) {
            if (isFirstLoading) {
                showLoadingView(getString(R.string.getting_consultation_tab), new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                }, true);
            }
        } else {
            if (result == -6) {//网络异常
                setResultUI(MeetingListResult.netRequestion, new ArrayList<MeetingItem>());
            } else {//其他原因获取会议列表失败
                setResultUI(MeetingListResult.waitting, new ArrayList<MeetingItem>());
            }
        }
    }

    private void setResultUI(MeetingListResult mr, List<MeetingItem> meetingInfos) {
        switch (mr) {
            case meetinglist://有会议记录
                netRequestionLayout.setVisibility(View.INVISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(
                        mAdapter = new MeetingListAdapter(meetingInfos, getActivity()));
                mAdapter.setOnItemClickListener(
                        new MeetingListAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, MeetingItem meetingItem) {
                                CustomLog.i(TAG, "加入会议，会议号为==" + data);
                                historyLv.setVisibility(View.INVISIBLE);
                                if (meetingItem.meetingType == 2) {
                                    switchToDetail(meetingItem);
                                } else if (meetingItem.meetingType == 1) {
                                    if (MedicalMeetingManage.getInstance().getActiveMeetingId().equals(meetingItem.meetingId) || TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                                        joinMeeing(meetingItem.meetingId);
                                    } else {
                                        CustomToast.show(getActivity(), getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
                                    }
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

    private void switchToDetail(MeetingItem meetingitem) {
        BookMeetingExInfo meetingfo = new BookMeetingExInfo();
        meetingfo.setBookNube(meetingitem.creatorId);
        meetingfo.setBookName(meetingitem.creatorName);
        meetingfo.setMeetingRoom(meetingitem.meetingId);
        meetingfo.setMeetingTheme(meetingitem.topic);
        meetingfo.setMeetingTime(Long.parseLong(meetingitem.createTime) * 1000);
        meetingfo.setHasMeetingPassWord(meetingitem.hasMeetingPwd);
        Intent i = new Intent(getActivity(),
                ReserveSuccessActivity.class);
        i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, meetingfo);
        startActivity(i);
    }

    private void joinMeeing(String meetingId) {
        int i = MedicalMeetingManage.getInstance().joinMeeting(meetingId,
                new MedicalMeetingManage.OnJoinMeetingListener() {
                    @Override
                    public void onJoinMeeting(String valueDes, int valueCode) {
                        state = MeetingFragment.State.NONE;
                        removeLoadingView();
                    }
                });
        if (i == 0) {
            state = MeetingFragment.State.JION;

            showLoadingView(getString(R.string.join_consultation), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    CustomLog.d(TAG, "取消加入会诊");
                }
            }, true);
        } else {
            CustomToast.show(getActivity(), getString(R.string.join_consultation_fail),
                    CustomToast.LENGTH_SHORT);
        }
    }

    private void getMeetingInfo(final String meetingIdStr, final boolean addPut) {
        final GetMeetingInfomation gi = new GetMeetingInfomation() {
            @Override
            protected void onSuccess(MeetingInfomation responseContent) {
                super.onSuccess(responseContent);
                state = MeetingFragment.State.NONE;
                removeLoadingView();
                //TODO 不知道作用
                // isAddCliclk = false;
                String nube = AccountManager.getInstance(getActivity())
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
                            .show(getActivity(), MeetingEndText, Toast.LENGTH_SHORT);
                    return;
                }
                CustomLog.e(TAG, "responseContent.meetingStatus==="
                        + responseContent.meetingStatus);
                CustomLog.e(TAG, "responseContent.expectStarttime==="
                        + responseContent.expectStarttime);
                MDSAccountInfo info = AccountManager.getInstance(
                        getActivity()).getAccountInfo();
                if (info != null) {
                    if (responseContent.meetingType == 2) {//预约会议
                        BookMeetingExInfo meetingfo = new BookMeetingExInfo();
                        meetingfo.setBookNube(responseContent.terminalAccount);
                        meetingfo.setBookName(responseContent.terminalAccountName);
                        meetingfo.setMeetingRoom(String.valueOf(responseContent.meetingId));
                        meetingfo.setMeetingTheme(responseContent.topic);
                        meetingfo.setMeetingUrl(MedicalMeetingManage.JMEETING_INVITE_URL);
                        meetingfo.setMeetingTime(Long.parseLong(responseContent.yyBeginTime) * 1000);
                        meetingfo.setHasMeetingPassWord(responseContent.hasMeetingPwd);
                        Intent i = new Intent(getActivity(),
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
                                iv.setClass(getActivity(), InputMeetingPasswordDialog.class);
                                //TODO 不知道作用
                                iv.putExtra("accesstoken", info.accessToken);
                                iv.putExtra("nubeNumber", info.nube);
                                iv.putExtra("nickName", info.nickName);
                                iv.putExtra("meetingId", meetingId);
                                iv.putExtra("nube", nube);
                                //isInputID 判断是否是从输入框输入的会议号
                                iv.putExtra("isInputID", String.valueOf(isInputID));
                                startActivity(iv);
                            }
                        } else {
                            joinMeeing(String.valueOf(responseContent.meetingId));
                            meetingidInputEdit.setText("");
                        }
                    }
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
                        getActivity())
                        .getAccountInfo().nube + "_" + statusCode);
                removeLoadingView();
                state = MeetingFragment.State.NONE;
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
                            .show(getActivity(), InvalidAccountText, Toast.LENGTH_SHORT);
                    return;
                }
                // if (statusCode == -999) {
                // CustomToast.show(getApplicationContext(), "系统错误",
                // Toast.LENGTH_SHORT);
                // }
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(getActivity(), getString(R.string.login_checkNetworkError),
                            Toast.LENGTH_LONG);
                    return;
                }
                if (statusCode == SettingData.getInstance().tokenUnExist
                        || statusCode == SettingData.getInstance().tokenInvalid) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(
                            statusCode);
                }
                String JoinMeetingFailText;
                if (SettingData.runDevice == SettingData.RunDevice.TV) {
                    JoinMeetingFailText = getString(R.string.join_meeting_fail_code);
                } else {
                    JoinMeetingFailText = getString(R.string.join_consultation_fail_code);
                }
                CustomToast.show(getActivity(), JoinMeetingFailText + statusCode,
                        Toast.LENGTH_SHORT);
            }
        };
        int result = gi.getMeetingInfomation(Integer.valueOf(meetingIdStr));
        if (result == 0) {
            state = MeetingFragment.State.GETMEETINGINFO;
            showLoadingView(getString(R.string.querying_consultation_info), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    //    TODO 可以下此处进行撤销加入会议
                }
            }, true);
        } else {
            CustomToast.show(getActivity(), getString(R.string.get_meeting_info_fail), CustomToast.LENGTH_SHORT);
        }

    }

    private void initHostoryList() {
        historyMeeting = new ArrayList<String>();
        middleList = new ArrayList<String>();
        historyList = new String[5];
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                "HistoryMeetingId", Activity.MODE_PRIVATE);
        String meetingid = sharedPreferences.getString("MeetingID", "");//存放sharePrefence中的字符串
        // adapter = new HistoryListAdapter(historyMeeting, this);
        if (meetingid.equals("")) {
            hasHostoryMeetingList = false;
            adapter = new HistoryListAdapter(middleList, getActivity());
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
            adapter = new HistoryListAdapter(middleList, getActivity());
            historyLv.setAdapter(adapter);
            historyLv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            AccountManager.RelationState relationState = AccountManager.getInstance(getActivity()).getRelationState();
            if (relationState == AccountManager.RelationState.NORELATION_STATE) {
                linearLayout.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
                initTitelBar();
                getNowmeetings();
            } else if (relationState == AccountManager.RelationState.RELATION_STATE) {
                linearLayout.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                refresh.setVisibility(View.VISIBLE);
                initTitelBarTwo();
                getDate();
            }
        } else {
            //相当于Fragment的onPause
            CustomLog.d(TAG, "当前状态:" + state + " class:" + getClass().getName());
            removeLoadingView();
            if (state == MeetingFragment.State.CREATEING) {
                MedicalMeetingManage.getInstance().cancelCreateMeeting(
                        getClass().getName());
                state = MeetingFragment.State.NONE;
            } else if (state == MeetingFragment.State.JION) {
                MedicalMeetingManage.getInstance().cancelJoinMeeting(getClass().getName());
                state = MeetingFragment.State.NONE;
            } else if (state == MeetingFragment.State.GETNOWMEETING) {
                MedicalMeetingManage.getInstance().cancelGetNowMeetings(getClass().getName());
                state = MeetingFragment.State.NONE;
            } else if (state == MeetingFragment.State.NONE) {

            }
        }
    }

    private void getDate() {
        final HPUGetMyAllListByDate hpuGetMyAllListByDate = new HPUGetMyAllListByDate() {

            private int unreadNotice1;
            private int unreadNotice;

            @Override
            protected void onSuccess(MyAllCslListInfo responseContent) {
                super.onSuccess(responseContent);
                refresh.finishRefresh();
                 final List<Genre> list =new ArrayList<>();
                CustomLog.i("MeetigFragment", "请求会诊数据成功");
                List<JointCoonsultatioonRoomBean> jointCoonsultatioonRoomBeenList = new ArrayList<>();
                List<JointCoonsultatioonRoomBean> jointCoonsultatioonRoomBeenList1 = new ArrayList<>();
                cslInfos = responseContent.getCslInfos();
                seekInfos = responseContent.getSeekInfos();
                if (seekInfos!=null&&cslInfos!=null){
                    if (seekInfos.size()==0&&cslInfos.size()==0){
                        myLine.setVisibility(View.GONE);
                    }
                }
                if (seekInfos != null && seekInfos.size() > 0) {
                    myLine.setVisibility(View.VISIBLE);
                    for (int i = 0; i < seekInfos.size(); i++) {
                        CSLInfo cslInfo = seekInfos.get(i);
                        int state = cslInfo.getState();
                        String dtName = cslInfo.getPatientName()+MedicalApplication.getContext().getString(R.string.udt_room);
                        String requestHosp = cslInfo.getRequestHosp();
                        String requestDep = cslInfo.getRequestDep();
                        String requestName = cslInfo.getRequestName();
                        String schedulDate = cslInfo.getSchedulDate();
                        String range = cslInfo.getRange();
                        String id = cslInfo.getId();
                        String groupId = cslInfo.getGroupId();
                        try {
                            unreadNotice = dtNoticesDao.queryUnreadNotice(groupId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        JointCoonsultatioonRoomBean bean = new JointCoonsultatioonRoomBean(dtName, Integer.valueOf(state), requestHosp, requestDep, requestName, schedulDate, id, range,unreadNotice);
                        bean.setZhenliaoState(1);
                        jointCoonsultatioonRoomBeenList.add(bean);
                    }
                    genre = new Genre(MedicalApplication.getContext().getString(R.string.my_today_seek), jointCoonsultatioonRoomBeenList);
                    list.add(genre);
                }

                if (cslInfos != null && cslInfos.size() > 0) {
                    myLine.setVisibility(View.VISIBLE);
                    for (int i = 0; i < cslInfos.size(); i++) {
                        CSLInfo cslInfo = cslInfos.get(i);
                        int state = cslInfo.getState();
                        String dtName = cslInfo.getPatientName()+MedicalApplication.getContext().getString(R.string.udt_room);
                        String responseHosp = cslInfo.getResponseHosp();
                        String responseDep = cslInfo.getResponseDep();
                        String responseName = cslInfo.getResponseName();
                        String schedulDate = cslInfo.getSchedulDate();
                        String range = cslInfo.getRange();
                        String id = cslInfo.getId();
                        String groupId = cslInfo.getGroupId();
                        try {
                            unreadNotice1 = dtNoticesDao.queryUnreadNotice(groupId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        JointCoonsultatioonRoomBean bean = new JointCoonsultatioonRoomBean(dtName, Integer.valueOf(state), responseHosp, responseDep, responseName, schedulDate, id, range,unreadNotice1);
                        bean.setZhenliaoState(2);
                        jointCoonsultatioonRoomBeenList1.add(bean);
                    }
                    genre1 = new Genre(MedicalApplication.getContext().getString(R.string.my_today_get), jointCoonsultatioonRoomBeenList1);
                    list.add(genre1);
                }
                    expendAdapter.setData(list);
                    expandRecyclerview.setAdapter(expendAdapter);
                    for(int i = 0; i < expendAdapter.getGroupCount(); i++){

                        expandRecyclerview.expandGroup(i);

                    }
                    expandRecyclerview.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                            return false;
                        }
                    });
                    //  设置子选项点击监听事件
                    expandRecyclerview.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                            Intent intent = new Intent(getActivity(), UDTChatRoomActivity.class);
                            intent.putExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG, list.get(groupPosition).getJointCoonsultatioonRoomBeanList().get(childPosition).getId());
                            startActivity(intent);
                            return true;
                        }
                    });
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                refresh.finishRefresh(false);
                CustomLog.e("MeetingFragment", "请求会诊数据失败|statuscode=" + statusCode + "|statusInfo=" + statusInfo);
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                }
            }
        };

        hpuGetMyAllListByDate.getallList(AccountManager.getInstance(getActivity()).getMdsToken(), "");
        CustomLog.i("HeadFragment", "请求会诊数据getallList|token=" + AccountManager.getInstance(getActivity()).getMdsToken());
    }

    /**
     * 监听医联体通讯录
     */
    private class ShareContactObserver extends ContentObserver {

        public ShareContactObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            CustomLog.d(TAG,"共享通讯录数据发生变更");
            mHandler.sendEmptyMessage(MSG_MESSAGE_SHARE_CONTACT_CHANGED);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MESSAGE_SHARE_CONTACT_CHANGED:
                    updataDate();
                    break;
                default:
                    break;
            }
        }
    };

    private void updataDate() {
        List<Genre> list =new ArrayList<>();
        List<JointCoonsultatioonRoomBean> jointCoonsultatioonRoomBeenList = new ArrayList<>();
        List<JointCoonsultatioonRoomBean> jointCoonsultatioonRoomBeenList1 = new ArrayList<>();
        if (seekInfos != null && seekInfos.size() > 0) {
            for (int i = 0; i < seekInfos.size(); i++) {
                CSLInfo cslInfo = seekInfos.get(i);
                int state = cslInfo.getState();
                String dtName = cslInfo.getPatientName() + MedicalApplication.getContext().getString(R.string.udt_room);
                String requestHosp = cslInfo.getRequestHosp();
                String requestDep = cslInfo.getRequestDep();
                String requestName = cslInfo.getRequestName();
                String schedulDate = cslInfo.getSchedulDate();
                String range = cslInfo.getRange();
                String id = cslInfo.getId();
                String groupId = cslInfo.getGroupId();
                int unreadNotice = 0;
                try {
                    unreadNotice = dtNoticesDao.queryUnreadNotice(groupId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JointCoonsultatioonRoomBean bean = new JointCoonsultatioonRoomBean(dtName, Integer.valueOf(state), requestHosp, requestDep, requestName, schedulDate, id, range, unreadNotice);
                bean.setZhenliaoState(1);
                jointCoonsultatioonRoomBeenList.add(bean);
            }
            genre = new Genre(MedicalApplication.getContext().getString(R.string.my_today_seek), jointCoonsultatioonRoomBeenList);
            list.add(genre);
        }

        if (cslInfos != null && cslInfos.size() > 0) {
            for (int i = 0; i < cslInfos.size(); i++) {
                CSLInfo cslInfo = cslInfos.get(i);
                int state = cslInfo.getState();
                String dtName = cslInfo.getPatientName() + MedicalApplication.getContext().getString(R.string.udt_room);
                String responseHosp = cslInfo.getResponseHosp();
                String responseDep = cslInfo.getResponseDep();
                String responseName = cslInfo.getResponseName();
                String schedulDate = cslInfo.getSchedulDate();
                String range = cslInfo.getRange();
                String id = cslInfo.getId();
                String groupId = cslInfo.getGroupId();
                int unreadNotice1 = 0;
                try {
                    unreadNotice1 = dtNoticesDao.queryUnreadNotice(groupId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JointCoonsultatioonRoomBean bean = new JointCoonsultatioonRoomBean(dtName, Integer.valueOf(state), responseHosp, responseDep, responseName, schedulDate, id, range, unreadNotice1);
                bean.setZhenliaoState(2);
                jointCoonsultatioonRoomBeenList1.add(bean);
            }
            genre1 = new Genre(MedicalApplication.getContext().getString(R.string.my_today_get), jointCoonsultatioonRoomBeenList1);
            list.add(genre1);
        }
        expendAdapter.setData(list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (shareContactObserver !=null){
            getActivity().getContentResolver().unregisterContentObserver(shareContactObserver);
            shareContactObserver = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDate();
    }
}
