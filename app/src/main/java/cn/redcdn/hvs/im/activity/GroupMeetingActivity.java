package cn.redcdn.hvs.im.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.redcdn.datacenter.medicalcenter.MDSAPPBindGroupCsl;
import cn.redcdn.datacenter.medicalcenter.MDSAPPGetGroupCsl;
import cn.redcdn.datacenter.medicalcenter.MDSAPPUnbindGroupCsl;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.medicalcenter.data.MDSGroupCslInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.profiles.view.SlideSwitch;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

public class GroupMeetingActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {
    private SlideSwitch groupMeetingControl;
    private RelativeLayout titleLayout;
    private Button titlerightbtn;
    private EditText meetingNubEdit;
    private EditText meetingThemeEdit;
    private EditText meetingPswEdit;
    private int resultcode = 1;
    public final static String MEETING_NUB = "MEETING_NUB";//会议号
    public final static String MEETING_THEME = "MEETING_THEME";//会议主题
    public final static String MEETING_PASSWORD = "MEETING_PASSWORD";//会议密码
    private String mMeetingNub = "";
    private String mMeetingTheme = "";
    private String mMeetingPsw = "";
    private String mNumberId;
    private String mGetGroupCslFail;
    private Boolean bind;
    private Button meetingIDDeleteBtn;
    private Button pwdDeleteBtn;
    private Button themeDeleteBtn;
    private Boolean isMeetingNub =false;
    //输入表情前的光标位置
    private int cursorPos;
    //输入表情前EditText中的文本
    private String inputAfterText;
    //是否重置了EditText的内容
    private boolean resetText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_meeting);
        initView();
        initData();
        InputFilter[] emojiFilters = {emojiFilter};
        meetingThemeEdit.setFilters(new InputFilter[]{emojiFilter});
    }
    private void initView(){
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.group_meeting));
        titleBar.enableBack();
        titleBar.enableRightBtn(getString(R.string.complete), 0, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        mMeetingNub = meetingNubEdit.getText().toString().trim();
                        mMeetingTheme = meetingThemeEdit.getText().toString();
                        mMeetingPsw = meetingPswEdit.getText().toString().trim();

//                        if (mMeetingNub.length() != 8) {
//                            CustomToast.show(GroupMeetingActivity.this, R.string.consult_meetingid_cannot_more_than_8_size, CustomToast.LENGTH_SHORT);
//                        }else  if (mMeetingTheme.length() >20){
//                            CustomToast.show(GroupMeetingActivity.this, R.string.consult_meetingtheme_cannot_more_than_20_size, CustomToast.LENGTH_SHORT);
//                        }else if (mMeetingPsw.length() != 0 && mMeetingPsw.length() != 6){
//                            CustomToast.show(GroupMeetingActivity.this, R.string.consult_meetingpwd_cannot_more_than_6_size, CustomToast.LENGTH_SHORT);
//                        } else
                        if (mMeetingNub.length() == 8){
                            if (bind) {
                                if (mMeetingPsw.length() != 0 && mMeetingPsw.length() != 6){
                                 CustomToast.show(GroupMeetingActivity.this, R.string.consult_meetingpwd_cannot_more_than_6_size, CustomToast.LENGTH_SHORT);
                                } else {
                                    MDSAPPBindGroupCsl mdsappBindGroupCsl = new MDSAPPBindGroupCsl() {
                                        @Override
                                        protected void onSuccess(JSONObject responseContent) {
                                            super.onSuccess(responseContent);
                                            removeLoadingView();
                                            CustomLog.d(TAG, "onSuccess");
                                            Intent intent = new Intent(GroupMeetingActivity.this, GroupChatDetailActivity.class);
                                            intent.putExtra(MEETING_NUB, meetingNubEdit.getText().toString());
                                            intent.putExtra(MEETING_THEME, meetingThemeEdit.getText().toString());
                                            intent.putExtra(MEETING_PASSWORD, meetingPswEdit.getText().toString());
                                            GroupMeetingActivity.this.setResult(resultcode, intent);
                                            GroupMeetingActivity.this.finish();
                                        }

                                        @Override
                                        protected void onFail(int statusCode, String statusInfo) {
                                            super.onFail(statusCode, statusInfo);
                                            removeLoadingView();
                                            CustomLog.d(TAG, "onFail" + "statusCode" + statusCode);
                                            CustomToast.show(GroupMeetingActivity.this, statusInfo, CustomToast.LENGTH_SHORT);
                                        }
                                    };
                                    showLoadingView(getString(R.string.binding_wait));
                                    CustomLog.d(TAG, "mdsappBindGroupCsl 绑定中");
                                    mdsappBindGroupCsl.bindGroupCsl(AccountManager.getInstance(GroupMeetingActivity.this).getMdsToken(), mNumberId, mMeetingNub, mMeetingTheme, mMeetingPsw);
                                }
                                }else {
                                showLoadingView(getString(R.string.unbinding_wait));
                                CustomLog.d(TAG, "mdsappUnbindGroupCsl 解绑中");
                                MDSAPPUnbindGroupCsl mdsappUnbindGroupCsl = new MDSAPPUnbindGroupCsl() {
                                    @Override
                                    protected void onSuccess(JSONObject responseContent) {
                                        super.onSuccess(responseContent);
                                        removeLoadingView();
                                        CustomLog.d(TAG, "onSuccess");
                                        Intent intent = new Intent(GroupMeetingActivity.this, GroupChatDetailActivity.class);
                                        intent.putExtra(MEETING_NUB, "");
                                        intent.putExtra(MEETING_THEME, "");
                                        intent.putExtra(MEETING_PASSWORD, "");
                                        GroupMeetingActivity.this.setResult(resultcode, intent);
                                        GroupMeetingActivity.this.finish();
                                    }

                                    @Override
                                    protected void onFail(int statusCode, String statusInfo) {
                                        super.onFail(statusCode, statusInfo);
                                        removeLoadingView();
                                        CustomLog.d(TAG, "onFail" + "statusCode" + statusCode);
                                        CustomToast.show(GroupMeetingActivity.this, statusInfo, CustomToast.LENGTH_SHORT);
                                    }
                                };
                                mdsappUnbindGroupCsl.unbindGroupCsl(AccountManager.getInstance(GroupMeetingActivity.this).getMdsToken(), mNumberId);
                            }

                    }
                }
        });
        groupMeetingControl = (SlideSwitch) findViewById(R.id.set_group_meeting);
        groupMeetingControl.setChecked(false);
        bind = false;
        groupMeetingControl.SetOnChangedListener(new SlideSwitch.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
                changeGroupMeetingSettingStats(!checkState);
            }
        });
        titleLayout = (RelativeLayout) findViewById(R.id.title);
        titlerightbtn = (Button) titleLayout.findViewById(R.id.right_btn);
        meetingNubEdit = (EditText) findViewById(R.id.group_meeting_number_edittext);
        meetingThemeEdit = (EditText) findViewById(R.id.group_meeting_theme_edittext);
        meetingPswEdit = (EditText) findViewById(R.id.group_meeting_password_edittext);
        meetingIDDeleteBtn = (Button) findViewById(R.id.group_meeting_meetingid_delete);
        meetingIDDeleteBtn.setOnClickListener(this);
        pwdDeleteBtn = (Button) findViewById(R.id.group_meeting_password_delete);
        pwdDeleteBtn.setOnClickListener(this);
        themeDeleteBtn = (Button) findViewById(R.id.group_meeting_theme_delete);
        themeDeleteBtn.setOnClickListener(this);
        meetingNubEdit.setOnFocusChangeListener(this);
        meetingThemeEdit.setOnFocusChangeListener(this);
        meetingPswEdit.setOnFocusChangeListener(this);

    }
    private void initData(){
        mNumberId = getIntent().getStringExtra(GroupChatDetailActivity.KEY_NUMBER);
        mGetGroupCslFail = getIntent().getStringExtra(GroupChatDetailActivity.KEY_GETGROUPCSLFAIL);
        if (mGetGroupCslFail.equals(GroupChatDetailActivity.VALUE_GETGROUPCSLFAIL)){
            MDSAPPGetGroupCsl mdsappGetGroupCsl = new MDSAPPGetGroupCsl() {
                @Override
                protected void onSuccess(MDSGroupCslInfo responseContent) {
                    super.onSuccess(responseContent);
                    mGetGroupCslFail = "";
                    CustomLog.d(TAG,"mdsappGetGroupCsl   onSuccess" + responseContent.cslRoomNo + responseContent.cslSubject);
                    mMeetingNub = responseContent.cslRoomNo;
                    meetingNubEdit.setText(mMeetingNub);
                    if (responseContent.cslSubject != null){
                        mMeetingTheme = responseContent.cslSubject;
                        meetingThemeEdit.setText(mMeetingTheme);
                    }
                    if (responseContent.cslPassword != null){
                        mMeetingPsw = responseContent.cslPassword;
                        meetingPswEdit.setText(mMeetingPsw);
                    }
                }

                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    CustomLog.d(TAG,"mdsappGetGroupCsl   onFail" +statusInfo);
                    mMeetingNub = "";
                    mMeetingTheme = "";
                    mMeetingPsw = "";
//                meetingNub = GroupChatDetailActivity.VALUE_MEETINGNUB;
                    if (statusCode != MDSErrorCode.MDS_NOTEXIT_GRPOUPCSL){
//                        CustomToast.show(GroupMeetingActivity.this,statusInfo,CustomToast.LENGTH_SHORT);
                        GroupMeetingActivity.this.finish();
                    }
                }
            };
            CustomLog.d(TAG,"mdsappGetGroupCsl");
            mdsappGetGroupCsl.getGroupCsl(AccountManager.getInstance(this).getMdsToken(),mNumberId);
        }else {
            mMeetingNub = getIntent().getStringExtra(GroupChatDetailActivity.KEY_MEETINGNUB);
            mMeetingTheme = getIntent().getStringExtra(GroupChatDetailActivity.KEY_MEETINGTHEME);
            mMeetingPsw = getIntent().getStringExtra(GroupChatDetailActivity.KEY_MEETINGPASSWORD);
        }
        if ((!mMeetingNub.equals("")) && !mGetGroupCslFail.equals(GroupChatDetailActivity.VALUE_GETGROUPCSLFAIL)){
            meetingNubEdit.setText(mMeetingNub);
            groupMeetingControl.setChecked(true);
            bind = true;
        }else {
            groupMeetingControl.setChecked(false);
            bind = false;
        }
        if (!mMeetingTheme.equals("")){
            meetingThemeEdit.setText(mMeetingTheme);
        }
        if (!mMeetingPsw.equals("")){
            meetingPswEdit.setText(mMeetingPsw);
        }
        if (meetingNubEdit.getText().toString().length() == 8){
            titlerightbtn.setTextColor(Color.parseColor("#49afcc"));
            isMeetingNub = true;
        }else {
            titlerightbtn.setTextColor(Color.parseColor("#222625"));
            isMeetingNub = false;
        }
        meetingNubEdit.setSelection(meetingNubEdit.getText().length());
        meetingNubEdit.addTextChangedListener(meetingIdTextWatcher);
        meetingThemeEdit.setSelection(meetingThemeEdit.getText().length());
        meetingThemeEdit.addTextChangedListener(themeTextWatcher);
        meetingPswEdit.setSelection(meetingPswEdit.getText().length());
        meetingPswEdit.addTextChangedListener(pwdTextWatcher);
//        if (meetingNubEdit.getText().toString().equals("")) {
//            meetingIDDeleteBtn.setVisibility(View.GONE);
//        } else {
//            meetingIDDeleteBtn.setVisibility(View.VISIBLE);
//        }
//        if (meetingPswEdit.getText().toString().equals("")) {
//            pwdDeleteBtn.setVisibility(View.GONE);
//        } else {
//            pwdDeleteBtn.setVisibility(View.VISIBLE);
//        }
//
//        if (meetingThemeEdit.getText().toString().equals("")) {
//            themeDeleteBtn.setVisibility(View.GONE);
//        } else {
//            themeDeleteBtn.setVisibility(View.VISIBLE);
//        }
    }


    private void changeGroupMeetingSettingStats(boolean open) {
        if (open) {
            groupMeetingControl.setChecked(false);
            bind = false;
        } else {
            groupMeetingControl.setChecked(true);
            bind = true;
        }
    }


    private TextWatcher meetingIdTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            setMeetingIDButtonState();
        }


    };
    private TextWatcher pwdTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            setPwdButtonState();
        }


    };
    private TextWatcher themeTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            setThemeButtonState();
        }


    };

    private void setMeetingIDButtonState(){
            if (meetingNubEdit.getText().toString().equals("")) {
//                titlerightbtn.setTextColor(Color.parseColor("#222625"));
                meetingIDDeleteBtn.setVisibility(View.GONE);
            } else {
//                titlerightbtn.setTextColor(Color.parseColor("#49afcc"));
                meetingIDDeleteBtn.setVisibility(View.VISIBLE);
            }
        if (meetingNubEdit.getText().toString().length() == 8){
            isMeetingNub = true;
        }else {
            isMeetingNub = false;
        }
        if (isMeetingNub){
            titlerightbtn.setTextColor(Color.parseColor("#49afcc"));
        }else {
            titlerightbtn.setTextColor(Color.parseColor("#222625"));
        }
    }
    private void setPwdButtonState(){
        if (meetingPswEdit.getText().toString().equals("")) {
//            titlerightbtn.setTextColor(Color.parseColor("#222625"));
            pwdDeleteBtn.setVisibility(View.GONE);
        } else {
//            titlerightbtn.setTextColor(Color.parseColor("#49afcc"));
            pwdDeleteBtn.setVisibility(View.VISIBLE);
        }
    }
    private void setThemeButtonState(){
        if (meetingThemeEdit.getText().toString().equals("")) {
//            titlerightbtn.setTextColor(Color.parseColor("#222625"));
            themeDeleteBtn.setVisibility(View.GONE);
        } else {
//            titlerightbtn.setTextColor(Color.parseColor("#49afcc"));
            themeDeleteBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.group_meeting_meetingid_delete :
                mMeetingNub = "";
                meetingNubEdit.setText(mMeetingNub);
                break;
            case R.id.group_meeting_password_delete:
                mMeetingPsw = "";
                meetingPswEdit.setText(mMeetingPsw);
                break;
            case R.id.group_meeting_theme_delete:
                mMeetingTheme = "";
                meetingThemeEdit.setText(mMeetingTheme);
                break;
            default:
                break;
        }

    }

    @Override
    public void onFocusChange(View view, boolean b) {
        switch (view.getId()){
            case R.id.group_meeting_number_edittext :
                if (b && !meetingNubEdit.getText().toString().equals("")){
                    meetingIDDeleteBtn.setVisibility(View.VISIBLE);
                }else {
                    meetingIDDeleteBtn.setVisibility(View.GONE);
                }
                break;
            case R.id.group_meeting_password_edittext :
                if (b && !meetingPswEdit.getText().toString().equals("")){
                    pwdDeleteBtn.setVisibility(View.VISIBLE);
                }else {
                    pwdDeleteBtn.setVisibility(View.GONE);
                }
                break;
            case R.id.group_meeting_theme_edittext :
                if (b && !meetingThemeEdit.getText().toString().equals("")){
                    themeDeleteBtn.setVisibility(View.VISIBLE);
                }else {
                    themeDeleteBtn.setVisibility(View.GONE);
                }
                break;
            default:
                break;
        }

    }


    InputFilter emojiFilter = new InputFilter() {
        Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                CustomToast.show(GroupMeetingActivity.this,getString(R.string.unsupport_input_emoji_face),CustomToast.LENGTH_SHORT);
                return "";
            }
            return null;
        }
    };

}
