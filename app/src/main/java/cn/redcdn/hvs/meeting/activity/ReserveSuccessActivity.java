package cn.redcdn.hvs.meeting.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.meetingmanage.GetMeetingInvitationSMS;
import cn.redcdn.datacenter.meetingmanage.ModifyMeetingInviters;
import cn.redcdn.datacenter.meetingmanage.data.MeetingInvitationSMSInfo;
import cn.redcdn.datacenter.meetingmanage.data.ResponseEmpty;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.activity.SelectLinkManActivity;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.im.common.CommonWaitDialog;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.meeting.util.DateUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.jmeetingsdk.MeetingManager;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import java.util.ArrayList;
import java.util.List;

import static cn.redcdn.hvs.MedicalApplication.context;

public class ReserveSuccessActivity extends BaseActivity implements View.OnClickListener {

    public static final String KEY_BOOK_MEETING_EXINFO = "book_meeting_exinfo";
    private static final int REQUEST_SELECT_LINK = 9527;
    private TitleBar titleBar;
    private TextView tvReserveMeetingId;
    private TextView tvReserveMeetingTitle;
    private TextView tvReserveMeetingDate;
    private TextView tvReserveMeetingTime;
    private TextView tvReserveMeetingPerson;
    /**
     * 上个页面传递过来的会议信息
     */
    private BookMeetingExInfo meetinfo;
    private LinearLayout weixinLayout;
    private LinearLayout messageLayout;
    private LinearLayout copyLayout;
    private String shareContent = null;
    private Button joinMeetingButton;
    private LinearLayout shareToFriends;
    //微信邀请声明
    public static String APP_ID = "wx075e76791e3ec1a8"; //微信appid
    public static IWXAPI api;

    private int hasPassword;
    // 加载框视图
    private CommonWaitDialog mWaitDialog;
    private Boolean isJoinMeeting=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册到微信
        api = WXAPIFactory.createWXAPI(this, APP_ID, true);
        api.registerApp(APP_ID);
        setContentView(R.layout.meeting_activity_reserve_success);
        getIntentData();
        initView();
        initData();
    }


    /**
     * 获取预约会议传递过来的会议相关信息
     * TODO 考虑从其他页面跳转过来的情况
     */
    private void getIntentData() {
        meetinfo = (BookMeetingExInfo) getIntent().getSerializableExtra(KEY_BOOK_MEETING_EXINFO);
    }


    private void initData() {
        if (meetinfo != null) {
            tvReserveMeetingId.setText(meetinfo.getMeetingRoom());
            tvReserveMeetingTitle.setText(meetinfo.getMeetingTheme());
            tvReserveMeetingDate.setText(
                DateUtil.getDateTimeByFormatAndMs(meetinfo.getMeetingTime(),
                    DateUtil.FORMAT_YYYY_MM_DD));
            tvReserveMeetingTime.setText(
                DateUtil.getDateTimeByFormatAndMs(meetinfo.getMeetingTime(),
                    DateUtil.FORMAT_HH_MM));
            if (meetinfo.getBookNube().equals(AccountManager.getInstance(this).getAccountInfo().getNube())) {
                shareToFriends.setVisibility(View.VISIBLE);
            }
            if (TextUtils.isEmpty(meetinfo.getBookName())) {
                tvReserveMeetingPerson.setText(R.string.no_name);
            } else {
                tvReserveMeetingPerson.setText(meetinfo.getBookName());
            }
            hasPassword = meetinfo.getHasMeetingPassWord();
        }
    }


    private void initView() {
        initTitleBar();
        weixinLayout = (LinearLayout) findViewById(R.id.reseve_weixinshare_layout);
        messageLayout = (LinearLayout) findViewById(R.id.reseve_messageshare_layout);
        copyLayout = (LinearLayout) findViewById(R.id.reseve_copyshare_layout);
        tvReserveMeetingId = (TextView) findViewById(R.id.reserve_meetingid_tv);
        tvReserveMeetingTitle = (TextView) findViewById(R.id.reserve_meetingtitle_tv);
        tvReserveMeetingDate = (TextView) findViewById(R.id.reserve_meetingdate_tv);
        tvReserveMeetingTime = (TextView) findViewById(R.id.reserve_meetingtime_tv);
        tvReserveMeetingPerson = (TextView) findViewById(R.id.reserve_meetingchairman_tv);
        joinMeetingButton = (Button) findViewById(R.id.join_meeting_btn);
        shareToFriends = (LinearLayout) findViewById(R.id.share_to_friends);
        findViewById(R.id.reseve_friendshare_layout).setOnClickListener(this);
        joinMeetingButton.setOnClickListener(this);
        weixinLayout.setOnClickListener(this);
        messageLayout.setOnClickListener(this);
        copyLayout.setOnClickListener(this);
    }


    private void initTitleBar() {
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.order_info));
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();
        isJoinMeeting=false;
    }


    private void getMeetingInvitationSMS(final int tag) {
        if (null == shareContent) {
            GetMeetingInvitationSMS ss = new GetMeetingInvitationSMS() {
                @Override
                protected void onSuccess(
                    MeetingInvitationSMSInfo responseContent) {
                    super.onSuccess(responseContent);
                    CustomLog.i(TAG, " 分享信息成功返回 responseContent == " + responseContent);
                    removeLoadingView();
                    shareContent = responseContent.invitationSMS;
                    switch (tag) {
                        case 1:
                            if (isWeixinAvilible()) {
                                shareLiveByWx(shareContent);
                            }
                            break;
                        case 2:
                            shareLiveBySms(shareContent);
                            break;
                        case 3:
                            shareLiveByCopy(getApplicationContext(), shareContent);
                            break;
                    }
                }


                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    removeLoadingView();
                    CustomLog.i(TAG, "getMeetingInvitationSMS statusCode : " + statusCode);
                    // if (HttpErrorCode
                    //     .checkNetworkError(statusCode)) {
                    //     CustomToast.show(ReserveSuccessActivity.this,
                    //         "网络不给力，请重试！", Toast.LENGTH_LONG);
                    //     return;
                    // } else {
                    //     CustomToast.show(ReserveSuccessActivity.this,
                    //         "获取分享内容失败！", Toast.LENGTH_LONG);
                    // }
                }
            };
            if (meetinfo != null) {
                int ret;
                //sdk要求修改：当nickname为空时，getMeetingInvitationSMS的inviteName传nube号，不能传空
                if (TextUtils.isEmpty(meetinfo.getBookName())) {
                    ret = ss.getMeetingInvitationSMS(MedicalApplication.shareInstance().SMS_HVS,
                        GetMeetingInvitationSMS.MEETINGTYPE_RESERVE,
                        Integer.valueOf(meetinfo.getMeetingRoom()),
                        meetinfo.getBookNube(), meetinfo.getBookNube());
                } else {
                    ret = ss.getMeetingInvitationSMS(MedicalApplication.shareInstance().SMS_HVS,
                        GetMeetingInvitationSMS.MEETINGTYPE_RESERVE,
                        Integer.valueOf(meetinfo.getMeetingRoom()),
                        meetinfo.getBookNube(), meetinfo.getBookName());
                }
                if (ret == 0) {
                    showLoadingView(getString(R.string.getting_share_info));
                } else {
                    CustomToast.show(ReserveSuccessActivity.this,
                        getString(R.string.get_share_info_fail), Toast.LENGTH_LONG);
                    CustomLog.i(TAG, "getMeetingInvitationSMS ret : " + ret);
                }
            }
        }
    }


    //微信分享
    private  boolean isWeixinAvilible() {
        final PackageManager packageManager = this.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        CustomToast.show(ReserveSuccessActivity.this, getString(R.string.weixin_version_low_havenot), CustomToast.LENGTH_LONG);
        return false;
    }


    private void shareLiveByWx(String content) {

        WXTextObject textObject = new WXTextObject();  //初始化WXTextObject对象，填写分享的文本内容
        textObject.text = content;
        WXMediaMessage msg = new WXMediaMessage();//用WXTextObject对象初始化一个WXMedicalMessage对象
        msg.mediaObject = textObject;
        msg.description = content;
        SendMessageToWX.Req req = new SendMessageToWX.Req();  //构造一个Req
        req.message = msg; //transaction字段用于唯一标识一个请求
        req.scene = SendMessageToWX.Req.WXSceneSession;//分享到好友会话
        api.sendReq(req);  //调用api接口发送数据到微信
    }


    private void shareLiveBySms(String content) {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", content);
        startActivity(intent);
    }


    private void shareLiveByCopy(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context
            .getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
        CustomToast.show(context, getString(R.string.toast_copy_ok), CustomToast.LENGTH_SHORT);
    }

    // 提供给IM使用   分享直播
     private void shareLiveByFriend(List<String> userList) {
         if (meetinfo != null) {
             MedicalMeetingManage.getInstance()
                 .sendBookMeetingMsgs(meetinfo, (ArrayList<String>) userList, null);//发送IM预约信息
             ModifyMeetingInviters mm = new ModifyMeetingInviters() {//在对方会议邀请列表中添加这条会议
                 @Override
                 protected void onSuccess(ResponseEmpty responseContent) {
                     super.onSuccess(responseContent);
                     removeLoadingView();
                 }
                 @Override
                 protected void onFail(int statusCode, String statusInfo) {
                     super.onFail(statusCode, statusInfo);
                     removeLoadingView();
                     CustomLog.d(TAG,"modifymeetingInviter statusCode= " + statusCode);
                     if (HttpErrorCode.checkNetworkError(statusCode)) {
                         CustomToast.show(ReserveSuccessActivity.this,
                             getString(R.string.net_error_try), Toast.LENGTH_LONG);
                         return;
                     } else {
                         CustomToast.show(ReserveSuccessActivity.this,
                             getString(R.string.share_keshi_friend_fail), Toast.LENGTH_LONG);
                     }
                 }
             };
             DaoPreference dao = DaoPreference.getInstance(this);
             String token = AccountManager.getInstance(this).getAccountInfo().getAccessToken();
             int ret = mm.modifymeetingInviter(2,//1表示及时会议  |  2表示预约会议
                 MeetingManager.MEETING_APP_BUTEL_CONSULTATION, token,
                 Integer.valueOf(meetinfo.getMeetingRoom()), userList, new ArrayList<String>());
             if (ret == 0) {
               showLoadingView(getString(R.string.shareing));
             } else {
                 CustomToast.show(ReserveSuccessActivity.this,
                     getString(R.string.share_keshi_friend_fail), Toast.LENGTH_LONG);
                 CustomLog.d(TAG,"modifymeetingInviter ret= " + ret);
             }
         }
     }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reseve_copyshare_layout:
                if (shareContent == null) {
                    getMeetingInvitationSMS(3);
                } else {
                    shareLiveByCopy(getApplicationContext(), shareContent);
                }
                break;
            case R.id.reseve_messageshare_layout:
                if (shareContent == null) {
                    getMeetingInvitationSMS(2);
                } else {
                    shareLiveBySms(shareContent);
                }
                break;
            case R.id.reseve_weixinshare_layout:
                if (shareContent == null) {
                    getMeetingInvitationSMS(1);
                } else {
                    if (isWeixinAvilible()) {
                        shareLiveByWx(shareContent);
                    }
                }
                break;
            case R.id.reseve_friendshare_layout:
                Intent i = new Intent(ReserveSuccessActivity.this,
                    SelectLinkManActivity.class);
                i.putExtra(SelectLinkManActivity.ACTIVITY_FLAG,
                    SelectLinkManActivity.AVITVITY_START_FOR_RESULT);
                i.putExtra(SelectLinkManActivity.ACTIVTY_PURPOSE,
                    SelectLinkManActivity.J_MEETING);
                i.putExtra(SelectLinkManActivity.KEY_IS_SIGNAL_SELECT,
                    true);
                i.putStringArrayListExtra(
                    SelectLinkManActivity.KEY_SELECTED_NUBENUMBERS,
                    new ArrayList<String>());
                startActivityForResult(i, REQUEST_SELECT_LINK);
                break;
            case R.id.join_meeting_btn:
                MDSAccountInfo info = AccountManager.getInstance(
                    getApplicationContext()).getAccountInfo();
                if (hasPassword == 0) {//无会议密码
                    joinMeeting();
                } else if (hasPassword == 1) {//有会议密码
                    if (String.valueOf(meetinfo.getBookNube()).equalsIgnoreCase(info.getNube())) {//我召开的
                        joinMeeting();
                    } else {//不是我召开的
                        Intent iv = new Intent();
                        iv.setClass(ReserveSuccessActivity.this, InputMeetingPasswordDialog.class);
                        //TODO 不知道作用
                        iv.putExtra("accesstoken", info.accessToken);
                        iv.putExtra("nubeNumber", info.nube);
                        iv.putExtra("nickName", info.nickName);
                        iv.putExtra("meetingId", meetinfo.getMeetingRoom());
                        iv.putExtra("nube", info.nube);
                        //isInputID 判断是否是从输入框输入的会议号
                        iv.putExtra("isInputID", String.valueOf(false));
                        startActivity(iv);
                    }
                }
                break;
            default:
                break;
        }

    }


    private void joinMeeting() {
        if (MedicalMeetingManage.getInstance().getActiveMeetingId().equals(tvReserveMeetingId.getText().toString())||TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
            int res = MedicalMeetingManage
                    .getInstance()
                    .joinMeeting(tvReserveMeetingId.getText().toString(),
                            new MedicalMeetingManage.OnJoinMeetingListener() {
                                @Override
                                public void onJoinMeeting(String valueDes, int valueCode) {
                                    isJoinMeeting = false;
                                    removeLoadingView();
                                }
                            });
            switch (res) {
                case 0:
                    showLoadingView(getString(R.string.joining_consultation));
                    isJoinMeeting = true;
                    break;
                case MedicalMeetingManage.NETWORKINVISIBLE:
                    CustomToast.show(ReserveSuccessActivity.this, getString(R.string.login_checkNetworkError),
                            CustomToast.LENGTH_SHORT);
                    break;
                default:
                    CustomToast.show(ReserveSuccessActivity.this, getString(R.string.join_meeting_fail),
                            CustomToast.LENGTH_SHORT);
            }
        }else {
            CustomToast.show(ReserveSuccessActivity.this, getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(), CustomToast.LENGTH_SHORT);
        }
    }
    //	@Override
    	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    		super.onActivityResult(requestCode, resultCode, data);
    		if (resultCode == Activity.RESULT_OK) {
    			switch (requestCode) {
    			// TODO:建群
    			case REQUEST_SELECT_LINK:
                    if (data == null||"".equals(data)) {
                        return;
                    }
    				Bundle selRes = data.getExtras();
    				if (selRes != null) {
    					final ArrayList<String> selectNickNames = selRes
    							.getStringArrayList(SelectLinkManActivity.START_RESULT_NICKNAME);
    					final ArrayList<String> selectName = selRes
    							.getStringArrayList(SelectLinkManActivity.START_RESULT_NAME);
    					final ArrayList<String> selectNumber = selRes
    							.getStringArrayList(SelectLinkManActivity.START_RESULT_NUMBER);
    					final ArrayList<String> receiverNumberLst = selRes
    							.getStringArrayList(SelectLinkManActivity.START_RESULT_NUBE);

    					if (selectName == null
    							|| selectName.size() != receiverNumberLst.size()) {
    						CustomLog.d(TAG,"选择收件人返回数据不整合");
    						return;
    					}
    					if (selectNickNames == null
    							|| selectNickNames.size() != receiverNumberLst
    									.size()) {
                            CustomLog.d(TAG,"选择收件人返回数据不整合");
    						return;
    					}
    					if (selectNumber == null
    							|| selectNumber.size() != receiverNumberLst.size()) {
                            CustomLog.d(TAG,"选择收件人返回数据不整合");
    						return;
    					}
    					shareLiveByFriend(receiverNumberLst);
    				}
    				break;
    			default:
    				break;
    			}
    		}
    	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CustomLog.i(TAG, "点击返回键");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void onBack() {
        removeLoadingView();
    if(isJoinMeeting){
        MedicalMeetingManage.getInstance().cancelJoinMeeting(getClass().getName());
        isJoinMeeting=false;
    }else{
        finish();
    }
    }

}
