package cn.redcdn.hvs.udtroom.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import cn.redcdn.datacenter.hpucenter.HPUGetCslRoomDetail;
import cn.redcdn.datacenter.hpucenter.HPUStopCsl;
import cn.redcdn.datacenter.hpucenter.data.CSLRoomDetailInfo;
import cn.redcdn.datacenter.hpucenter.data.HPUCommonCode;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.udtroom.adapter.UDTPagerAdapter;
import cn.redcdn.hvs.udtroom.configs.UDTDataConstant;
import cn.redcdn.hvs.udtroom.configs.UDTGlobleData;
import cn.redcdn.hvs.udtroom.view.fragment.UDTChatFragment;
import cn.redcdn.hvs.udtroom.view.fragment.UDTDetailFragment;
import cn.redcdn.hvs.udtroom.widget.UDTViewPager;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class UDTChatRoomActivity extends BaseActivity
    implements UDTGlobleData.DateChangeListener, View.OnClickListener {
    public static final String END_DT_BROADCAST = "UDTChatRoomActivity_end_dt";
    public static final String SUBMIT_DT_SUGGEST_BROADCAST
        = "UDTChatRoomActivity_submit_dt_suggest";
    public static final String END_DT_DIALOG_DT_ID = "EndDtDialog_dt_id";
    private static final String TAG = UDTChatRoomActivity.class.getSimpleName();
    private static final int UDT_ROOM_DETAIL_FRAGMENT = 0;
    private static final int UDT_ROOM_CHAT_FRAGMENT = 1;
    public static final String REFERRAL_DT_DIALOG_DT_TYPE = "REFERRAL_DT_DIALOG_DT_TYPE";
    public static final String REFERRAL_DT_DIALOG_REFERRAL_ID = "REFERRAL_DT_DIALOG_REFERRAL_ID";
    public static final String REFERRAL_DT_DIALOG_DT_ID = "REFERRAL_DT_DIALOG_DT_ID";
    private static final int UDTCHATFRAGMNET_SHOWN = 1;
    public static final String SUBMIT_PARISE_BROADCAST = "SUBMIT_PARISE_BROADCAST";
    public static final String RECEIVER_END_DT_BROADCAST = "RECEIVER_END_DT_BROADCAST";
    // 诊疗 ID
    private String dtID;
    private boolean initFragmentFlag = false; //是否初始化Fragment
    private Button patient;
    private Button chat;
    private View patientView;
    private View chatView;
    private ImageButton end_consultation;
    private RelativeLayout reLayout_videoCall;
    private UDTPagerAdapter pagerAdapter;
    private UDTViewPager udtViewPager;

    private int currentShownFragment = -1;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            CustomLog.i(TAG, "receive broadcast");
            String action = intent.getAction();
            final String end_dialog_dt_id = intent.getStringExtra(END_DT_DIALOG_DT_ID);
            if (action.equals(END_DT_BROADCAST)) {
                if (!end_dialog_dt_id.equals(dtID)) {
                    // 广播要与诊疗室匹配
                    return;
                }
                CustomLog.d(TAG, "收到END_DT_BROADCAST");
                final CustomDialog endDtSuggestDialog = new CustomDialog(UDTChatRoomActivity.this);
                endDtSuggestDialog.removeCancelBtn();
                endDtSuggestDialog.setCenterBtnText(getString(R.string.iknow));
                endDtSuggestDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                    @Override
                    public void onClick(CustomDialog customDialog) {
                        endDtSuggestDialog.dismiss();
                        if (udtDetailFragment == null) {
                            CustomLog.d(TAG,
                                "submitDtSuggestDialog setOkBtnOnClickListener udtDetailFragment == null 不能让scrollview跳到底部");
                        } else {
                            requestData();
                            udtViewPager.setCurrentItem(0);
                            patientView.setVisibility(View.VISIBLE);
                            chatView.setVisibility(View.INVISIBLE);
                            patient.setTextColor(getResources().getColor(R.color.udt_press));
                            chat.setTextColor(getResources().getColor(R.color.udt_no_press));
                        }
                        Intent intent = new Intent(UDTChatRoomActivity.this, AppraiseDialog.class);
                        intent.putExtra(AppraiseDialog.APPRAISEDIALOG_IS_REQUEST_FLAg, true);
                        CustomLog.d(TAG, "end_dialog_dt_id: " + end_dialog_dt_id);
                        intent.putExtra(AppraiseDialog.APPRAISEDIALOG_DT_ID, end_dialog_dt_id);
                        startActivity(intent);
                    }
                });
                endDtSuggestDialog.setTip(getString(R.string.response_person_has_finished_dt));
                endDtSuggestDialog.show();
            } else if (action.equals(SUBMIT_DT_SUGGEST_BROADCAST)) {
                CustomLog.d(TAG, "收到SUBMIT_DT_SUGGEST_BROADCAST");
                final String referralDtId = intent.getStringExtra(REFERRAL_DT_DIALOG_DT_ID);
                if (!referralDtId.equals(dtID)) {
                    // 广播要与诊疗室匹配
                    return;
                }
                final CustomDialog submitDtSuggestDialog = new CustomDialog(
                    UDTChatRoomActivity.this);
                submitDtSuggestDialog.removeCancelBtn();
                submitDtSuggestDialog.setCenterBtnText(getString(R.string.iknow));
                submitDtSuggestDialog.setOkBtnOnClickListener(
                    new CustomDialog.OKBtnOnClickListener() {
                        @Override
                        public void onClick(CustomDialog customDialog) {
                            submitDtSuggestDialog.dismiss();
                            if (udtDetailFragment == null) {
                                CustomLog.d(TAG,
                                    "submitDtSuggestDialog setOkBtnOnClickListener udtDetailFragment == null 不能让scrollview跳到底部");
                            } else {
                                requestData();
                                udtViewPager.setCurrentItem(0);
                                patientView.setVisibility(View.VISIBLE);
                                chatView.setVisibility(View.INVISIBLE);
                                patient.setTextColor(getResources().getColor(R.color.udt_press));
                                chat.setTextColor(getResources().getColor(R.color.udt_no_press));
                                udtDetailFragment.ObtainFocus();
                            }

                        }
                    });
                submitDtSuggestDialog.setTip(getString(R.string.watch_dt_suggest));
                submitDtSuggestDialog.show();
            } else if (action.equals(SUBMIT_PARISE_BROADCAST)) {
                final String appriseDtId = intent.getStringExtra(
                    AppraiseDialog.APPRAISEDIALOG_DT_ID);
                if (!appriseDtId.equals(dtID)) {
                    // 广播要与诊疗室匹配
                    return;
                }
                CustomLog.i(TAG, "收到诊疗评价广播");
                requestData();
            }
        }
    };
    private UDTDetailFragment udtDetailFragment;
    private UDTChatFragment udtChatFragment;

    private UDTGlobleData mUDTGlobleData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.i(TAG, "onCreate()");
        setContentView(R.layout.activity_udt_chat_room);

        Intent dataIntent = getIntent();
        dtID = dataIntent.getStringExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG);
        if (TextUtils.isEmpty(dtID)) {
            CustomLog.e(TAG, "诊疗单号error!");
            finish();
        } else {
            mUDTGlobleData = new UDTGlobleData();
            mUDTGlobleData.init(AccountManager.getInstance(this).getNube(), dtID);
            initUI();
            requestData();
        }
        initBroadcase();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUDTGlobleData.removeListener(this);
        mUDTGlobleData.release();
        unregisterReceiver(broadcastReceiver);
    }


    private void requestData() {
        CustomLog.i(TAG, "requestData()");

        final HPUGetCslRoomDetail roomDetail = new HPUGetCslRoomDetail() {
            @Override protected void onSuccess(CSLRoomDetailInfo data) {
                super.onSuccess(data);
                CustomLog.i(TAG, "HPUGetCslRoomDetail onSuccess()");
                removeLoadingView();
                mUDTGlobleData.updateCSLRoomDetailInfo(data);
                if (!initFragmentFlag) {
                    updateUI();
                    initFragments();
                    mUDTGlobleData.addListener(UDTChatRoomActivity.this);
                }

            }


            @Override protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.i(TAG, "HPUGetCslRoomDetail onFail()");
                CustomLog.e(TAG, "statusCode = " + statusCode + "statusInfo = " + statusInfo);
                removeLoadingView();

                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(UDTChatRoomActivity.this).logout();
                    CustomToast.show(UDTChatRoomActivity.this,
                        getString(R.string.udt_notkon_loginagain),
                        CustomToast.LENGTH_LONG);
                    finish();
                } else {
                    CustomToast.show(UDTChatRoomActivity.this,
                        getString(R.string.udt_obtain_fail_news),
                        CustomToast.LENGTH_LONG);
                }
                if (!initFragmentFlag) {
                    finish();
                }
            }
        };

        roomDetail.getcslroomdetail(AccountManager.getInstance(this).getMdsToken(), dtID);

        showLoadingView(getApplicationContext().getString(R.string.reserve_treatment_loading),
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    CustomToast.show(getApplicationContext(),
                        getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                    CustomLog.i(TAG, "getcslroomdetail has bean cancel");
                    roomDetail.cancel();
                    finish();
                }
            });
    }


    private void initBroadcase() {
        CustomLog.d(TAG, "注册广播");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(END_DT_BROADCAST);
        intentFilter.addAction(SUBMIT_DT_SUGGEST_BROADCAST);
        intentFilter.addAction(SUBMIT_PARISE_BROADCAST);
        registerReceiver(broadcastReceiver, intentFilter);
    }


    private void initUI() {
        patient = (Button) findViewById(R.id.patient_dt_tab_btn);
        chat = (Button) findViewById(R.id.chat_tab_btn);
        patient.setOnClickListener(this);
        chat.setOnClickListener(this);

        patientView = findViewById(R.id.patient_dt_tab_btn_view);
        chatView = findViewById(R.id.chat_tab_btn_view);

        end_consultation = (ImageButton) findViewById(R.id.end_consultation);
        end_consultation.setOnClickListener(this);

        reLayout_videoCall = (RelativeLayout) findViewById(R.id.reLayout_videoCall);
        reLayout_videoCall.setOnClickListener(this);
        udtViewPager = (UDTViewPager) findViewById(R.id.udt_room_view_pager);
    }


    private void updateUI() {
        configTitleBar(mUDTGlobleData.getPatientName()+"的联合会诊室");
        UDTGlobleData.DOCTOR_TYPE type = mUDTGlobleData.getDoctorType();
        if (type == UDTGlobleData.DOCTOR_TYPE.OTHER) {
            hideChatTab();
        }
        hideDTMeetingBtn();
        hideEndDtBtn();
        switch (mUDTGlobleData.getState()) {
            case HPUCommonCode.SEEK_STATE_NOT: //待接诊
                hideChatTab();
                hideDTMeetingBtn();
                hideEndDtBtn();
                break;
            case HPUCommonCode.SEEK_STATE_NOW: //接诊中
                if (type != UDTGlobleData.DOCTOR_TYPE.OTHER) {
                    showChatTab();
                    showDTMeetingBtn();
                    if (type == UDTGlobleData.DOCTOR_TYPE.RESPONSE) {
                        showEndDtBtn();
                    }
                }
                break;
            case HPUCommonCode.SEEK_STATE_END: //接诊结束
                hideDTMeetingBtn();
                hideEndDtBtn();
                if (type != UDTGlobleData.DOCTOR_TYPE.OTHER) {
                    chat.setText(R.string.udt_treatment_history);
                    showChatTab();
                }
                break;
        }
    }


    /**
     * 配置 TitleBar
     */
    private void configTitleBar(String title) {
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(title);
    }


    private void hideChatTab() {
        findViewById(R.id.main_layout_second).setVisibility(View.GONE);
        udtViewPager.lockPaging(true);
    }


    private void showChatTab() {
        findViewById(R.id.main_layout_second).setVisibility(View.VISIBLE);
        udtViewPager.lockPaging(false);
    }


    private void hideEndDtBtn() {
        end_consultation.setVisibility(View.GONE);
    }


    private void showEndDtBtn() {
        end_consultation.setVisibility(View.VISIBLE);
    }


    private void hideDTMeetingBtn() {
        reLayout_videoCall.setVisibility(View.GONE);
    }


    private void showDTMeetingBtn() {
        reLayout_videoCall.setVisibility(View.VISIBLE);
    }


    /**
     * 初始化患者诊疗单 Fragment, 初始化 图文会诊 Fragment,
     * 并将其添加至 FragmentList
     */
    private void initFragments() {
        CustomLog.i(TAG, "initFragments()");
        initFragmentFlag = true;
        List<Fragment> fragmentList = new ArrayList<>();

        udtDetailFragment = new UDTDetailFragment();
        udtChatFragment = new UDTChatFragment();

        fragmentList.add(udtDetailFragment);
        fragmentList.add(udtChatFragment);

        boundDataWithFragment(fragmentList);

        initViewPager(fragmentList);
    }


    /**
     * 将 Activity 获取的数据绑定到 Fragment 中
     */
    private void boundDataWithFragment(List<Fragment> fragmentList) {
        Bundle udtDataBundle = new Bundle();
        udtDataBundle.putString(UDTDataConstant.UDT_FRAGMENT_DATA_FLAG, dtID);
        fragmentList.get(UDT_ROOM_DETAIL_FRAGMENT).setArguments(udtDataBundle);
        fragmentList.get(UDT_ROOM_DETAIL_FRAGMENT);

        CustomLog.d(TAG, "groupID = " + mUDTGlobleData.getDTGroupID());
        udtChatFragment.setUDTGlobleData(mUDTGlobleData);
        udtDetailFragment.setUDTGlobleData(mUDTGlobleData);

    }


    /**
     * 初始化 ViewPager, 包括绑定适配器，设置不可滑动
     *
     * @param fragmentList fragment 容器
     */
    private void initViewPager(List<Fragment> fragmentList) {
        CustomLog.i(TAG, "initViewPager()");

        pagerAdapter = new UDTPagerAdapter(getSupportFragmentManager(), fragmentList);
        UDTViewPager udtViewPager = (UDTViewPager) findViewById(R.id.udt_room_view_pager);
        UDTPagerAdapter pagerAdapter = new UDTPagerAdapter(getSupportFragmentManager(),
            fragmentList);
        udtViewPager.setAdapter(pagerAdapter);
        udtViewPager.setOffscreenPageLimit(fragmentList.size());

        udtViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }


            @Override
            public void onPageSelected(int position) {
                CustomLog.i(TAG, "position");
                currentShownFragment = position;
                if (position == 0) {
                    patientView.setVisibility(View.VISIBLE);
                    chatView.setVisibility(View.INVISIBLE);
                    patient.setTextColor(getResources().getColor(R.color.udt_press));
                    chat.setTextColor(getResources().getColor(R.color.udt_no_press));
                } else if (position == 1) {
                    patientView.setVisibility(View.INVISIBLE);
                    chatView.setVisibility(View.VISIBLE);
                    patient.setTextColor(getResources().getColor(R.color.udt_no_press));
                    chat.setTextColor(getResources().getColor(R.color.udt_press));
                }

            }


            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.patient_dt_tab_btn:
                udtViewPager.setCurrentItem(0);
                patientView.setVisibility(View.VISIBLE);
                chatView.setVisibility(View.INVISIBLE);
                patient.setTextColor(getResources().getColor(R.color.udt_press));
                chat.setTextColor(getResources().getColor(R.color.udt_no_press));
                break;
            case R.id.chat_tab_btn:
                udtViewPager.setCurrentItem(1);
                patientView.setVisibility(View.INVISIBLE);
                chatView.setVisibility(View.VISIBLE);
                patient.setTextColor(getResources().getColor(R.color.udt_no_press));
                chat.setTextColor(getResources().getColor(R.color.udt_press));
                break;
            case R.id.end_consultation:
                if (!TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
                    final CustomDialog donotEndDialog = new CustomDialog(UDTChatRoomActivity.this);
                    donotEndDialog.removeCancelBtn();
                    donotEndDialog.setCenterBtnText(getString(R.string.iknow));
                    donotEndDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                        @Override
                        public void onClick(CustomDialog customDialog) {
                            donotEndDialog.dismiss();
                        }
                    });
                    donotEndDialog.setTip(getString(R.string.udt_chat_room_activity_not_finish_dt));
                    donotEndDialog.show();
                } else {
                    //结束会诊的逻辑
                    String advice = mUDTGlobleData.getAdvice();
                    String trAdvice = mUDTGlobleData.getTransferAdvice();
                    if (mUDTGlobleData.getDTResult() == 1 || mUDTGlobleData.getDTResult() == 2) {
                        showEndDialog();
                    } else {
                        showCommentDialog();
                    }

                }
                break;
            case R.id.reLayout_videoCall:
                //视频会诊
                joinMeeing(mUDTGlobleData.getMeetingId());
                break;
            default:
                break;
        }
    }


    /**
     * 发送结束诊疗广播，使接诊者能够及时响应
     */
    private void sendReceiveerEndDTBroadcast() {
        CustomLog.i(TAG, "sendEndDTBroadcast()");

        Intent intent = new Intent();
        intent.setAction(RECEIVER_END_DT_BROADCAST);
        sendBroadcast(intent);

    }


    private void showCommentDialog() {
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                udtViewPager.setCurrentItem(0);
                udtDetailFragment.ObtainFocus();
                dialog.cancel();
            }

        });
        dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dialog.cancel();

            }
        });
        dialog.setTip(getString(R.string.please_write_dt_suggest));
        dialog.setOkBtnText(getString(R.string.to_write));
        dialog.setCancelBtnText(getString(R.string.cancel));
        dialog.show();

    }


    private void showEndDialog() {
        final String tip = getString(R.string.udt_isStop_treat);
        final CustomDialog dialog = new CustomDialog(this);
        dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                final HPUStopCsl stopCsl = new HPUStopCsl() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        removeLoadingView();
                        requestData();
                        sendReceiveerEndDTBroadcast();
                        MedicalApplication.getFileTaskManager().sendChangeDtStateMsg(1,
                            mUDTGlobleData.getResponseNubeNumber(),
                            mUDTGlobleData.getRequestNubeNumber(),
                            dtID,
                            mUDTGlobleData.getResponseHeadThumUrl(),
                            mUDTGlobleData.getResponseName());
                        Intent intent = new Intent(UDTChatRoomActivity.this, AppraiseDialog.class);
                        intent.putExtra(AppraiseDialog.APPRAISEDIALOG_IS_REQUEST_FLAg, false);
                        intent.putExtra(AppraiseDialog.APPRAISEDIALOG_DT_ID, dtID);
                        startActivity(intent);
                    }


                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        removeLoadingView();
                        CustomLog.i(TAG,"结束诊疗失败::statusCode"+statusCode+statusInfo);
                        if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                            AccountManager.getInstance(MedicalApplication.getContext()).tokenAuthFail(statusCode);
                        } else {
                            CustomToast.show(MedicalApplication.getContext(), statusInfo, Toast.LENGTH_SHORT);
                        }
                    }

                };
                showLoadingView(getString(R.string.udt_pleaseWait),
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            removeLoadingView();
                            CustomLog.d(TAG, "结束诊疗");
                            stopCsl.cancel();
                        }
                    }, true);

                dialog.cancel();
                stopCsl.stop(
                    AccountManager.getInstance(MedicalApplication.getContext()).getMdsToken(),
                    dtID);

            }
        });
        dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                // TODO Auto-generated method stub
                CustomLog.d(TAG, "setCancelBtnOnClickListener");
                dialog.cancel();
            }
        });
        dialog.setTip(tip);
        dialog.setOkBtnText(getString(R.string.udt_endingTreat));
        dialog.setCancelBtnText(this.getString(R.string.cancel));
        dialog.show();
    }


    private void joinMeeing(String meetingId) {
        int i = MedicalMeetingManage.getInstance().joinMeeting(meetingId,
            new MedicalMeetingManage.OnJoinMeetingListener() {
                @Override
                public void onJoinMeeting(String valueDes, int valueCode) {
                    removeLoadingView();
                }
            });
        if (i == 0) {

            showLoadingView(getString(R.string.join_consultation),
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        removeLoadingView();
                        CustomLog.d(TAG, "取消加入会诊");
                    }
                }, true);
        } else {
            CustomToast.show(UDTChatRoomActivity.this, getString(R.string.join_consultation_fail),
                CustomToast.LENGTH_SHORT);
        }
    }


    @Override
    public void onDateChanged() {
        updateUI();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CustomLog.i(TAG, "onKeyDown()");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentShownFragment == UDTCHATFRAGMNET_SHOWN) {
                if (udtChatFragment.isInputPanelVisible()) {
                    udtChatFragment.setInputPanelHide();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}