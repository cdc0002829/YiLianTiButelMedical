package cn.redcdn.hvs.im.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.task.AsyncTasks;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import com.butel.connectevent.base.CommonConstant;
import com.butel.connectevent.utils.LogUtil;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cn.redcdn.hvs.im.preference.DaoPreference.PrefType;

import static android.R.attr.password;

/**
 * <dl>
 * <dt>ModifyActivity.java</dt>
 * <dd>Description:修改备注名、昵称、密码、群聊名称</dd>
 * <dd>Company: 北京红云融通技术有限公司</dd>
 * <dd>CreateDate: 2015-11-6 17:44:34</dd>
 * </dl>
 *
 * @author niuben
 */

public class ModifyActivity extends BaseActivity {

    private final String TAG = "ModifyActivity";

    public final static String KEY_OF_TYPE = "key_of_type";// 必须传入的值
    public final static int VALUE_OF_TYPE_MODIFY_NOTE_NAME = 0;// 修改备注名
    public final static int VALUE_OF_TYPE_MODIFY_NICK_NAME = 1;// 修改昵称
    public final static int VALUE_OF_TYPE_MODIFY_PWD = 2;// 修改密码
    public final static int VALUE_OF_TYPE_MODIFY_GROUP_NAME = 3;// 群聊名称

    public final static String KEY_OF_VALUE = "VALUE";// 传入修改前的值的key

    private int mType = VALUE_OF_TYPE_MODIFY_NOTE_NAME;
    private String mValue = "";
    private EditText mEditText;
    private TextView mTextView;
    private int mMaxLength = -1;

    private TitleBar titleBar;
    private Button rightbtn;
    private LinearLayout liner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG,"onCreate begin");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.butel_modify_activity);
        initData();
        initView();
        updateViewByData();

        CustomLog.d(TAG,"onCreate end");
    }

    private void initTopTitle(String title,int type) {
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.enableRightBtn(
                getResources().getString(R.string.butel_modify_save), 0,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (CommonUtil.isFastDoubleClick()) {
                            return;
                        }
                        CustomLog.d(TAG,"点击保存");
                        doSave();
                    }
                });

        if (type == VALUE_OF_TYPE_MODIFY_NOTE_NAME) {
            titleBar.setTitle(title);
            if (TextUtils.isEmpty(mValue)) {
                titleBar.setRightBtnClicked(false);
            }
        } else if (type == VALUE_OF_TYPE_MODIFY_NICK_NAME) {
            titleBar.setTitle(title);
            if (TextUtils.isEmpty(mValue)) {
                titleBar.setRightBtnClicked(false);
            }
        } else if (type == VALUE_OF_TYPE_MODIFY_PWD) {
            titleBar.setTitle(title);
            titleBar.setRightBtnClicked(false);
        } else if (type == VALUE_OF_TYPE_MODIFY_GROUP_NAME) {
            titleBar.setTitle(title);
            if (TextUtils.isEmpty(mValue)) {
                titleBar.setRightBtnClicked(false);
            }
        }
    }

    private void initData() {
        Intent intent = getIntent();
        mValue = intent.getStringExtra(KEY_OF_VALUE);
        mType = intent.getIntExtra(KEY_OF_TYPE, 0);
        CustomLog.d(TAG,"the key_of_type is " + mType + "key_of_value is " + mValue);
    }

    private void initView() {
        mEditText = (EditText) findViewById(R.id.butel_modify_edittext);
        mTextView = (TextView) findViewById(R.id.butel_modify_textview);
        liner= (LinearLayout) findViewById(R.id.butel_modify_linearlayout);
        rightbtn= (Button) liner.findViewById(R.id.right_btn);
    }

    private void updateViewByData() {
        switch (mType) {
            case VALUE_OF_TYPE_MODIFY_NOTE_NAME:
                upDataNoteNameView();
                break;
            case VALUE_OF_TYPE_MODIFY_NICK_NAME:
                upDataNickNameView();
                break;
            case VALUE_OF_TYPE_MODIFY_GROUP_NAME:
                upDataGroupNameView();
                break;
            case VALUE_OF_TYPE_MODIFY_PWD:
                upDataPWDView();
                break;
            default:
                break;
        }
    }

    private void upDataNoteNameView(){
        initTopTitle(getString(R.string.butel_modify_note_name),VALUE_OF_TYPE_MODIFY_NOTE_NAME);
        mMaxLength= IMConstant.NOTE_NAME_MAX_LENGTH;
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setText(getString(R.string.most)+mMaxLength+getString(R.string.make_up));
        InputFilter[] filters = { new InputFilter.LengthFilter(mMaxLength) };
        mEditText.setFilters(filters);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditText.addTextChangedListener(mTextWatcher);
        mEditText.setText(mValue);
        mEditText.setSelection(mEditText.getText().length());
        showInputMethod();
    }

    private void upDataNickNameView(){
        initTopTitle(getString(R.string.change_name),VALUE_OF_TYPE_MODIFY_NICK_NAME);
        mMaxLength=IMConstant.NICK_NAME_MAX_LENGTH;
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setText(getString(R.string.most)+mMaxLength+getString(R.string.make_up));
        InputFilter[] filters = { new InputFilter.LengthFilter(mMaxLength) };
        mEditText.setFilters(filters);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditText.addTextChangedListener(mTextWatcher);
        mEditText.setText(mValue);
        mEditText.setSelection(mEditText.getText().length());
        showInputMethod();
    }

    private void upDataGroupNameView(){
        initTopTitle(getString(R.string.group_chat_name),VALUE_OF_TYPE_MODIFY_GROUP_NAME);
        mMaxLength=IMConstant.GROUP_NAME_MAX_LENGTH;
        mTextView.setVisibility(View.VISIBLE);
        mTextView.setText(getString(R.string.most)+mMaxLength+getString(R.string.make_up));
        InputFilter[] filters = { new InputFilter.LengthFilter(mMaxLength) };
        mEditText.setFilters(filters);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditText.setText(mValue);
        mEditText.addTextChangedListener(mTextWatcher);
        mEditText.setSelection(mEditText.getText().length());
        showInputMethod();
    }

    private void upDataPWDView(){
        initTopTitle(getString(R.string.modify_password),VALUE_OF_TYPE_MODIFY_PWD);
        mTextView.setVisibility(View.INVISIBLE);
        mEditText.setText("");
        mEditText.setHint(getString(R.string.input_newpwd));
        InputFilter[] filter = { new InputFilter.LengthFilter(6) };
        mEditText.setFilters(filter);
        mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mEditText.addTextChangedListener(mTextWatcher);
        showInputMethod();
    }

    private void showInputMethod() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) mEditText
                        .getContext().getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(mEditText, 0);
            }
        }, 300);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        private int mCursorPos;// 输入表情前的光标位置
        private String inputAfterText;// 输入表情前EditText中的文本
        private boolean resetText = false;// 是否重置了EditText的内容

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            if (resetText == false) {
                mCursorPos = mEditText.getSelectionEnd();
                inputAfterText = s.toString();
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (resetText == false) {
                if (!TextUtils.isEmpty(s.toString())) {
                    if (containsEmoji(s.toString())) {
                        showErrorToast();
                        resetText = true;
                        mEditText.setText(inputAfterText);
                        mEditText.setSelection(mCursorPos);// 先定光标
                    } else {
                        if (mMaxLength > 0&& IMCommonUtil.getStringLength(s.toString()) > mMaxLength) {
                            LogUtil.d("长度超过"+mMaxLength+"个字符，不允许输入");
                            resetText = true;
                            String sub=IMCommonUtil.getSubStringByMaxLength(mEditText.getText().toString(), mMaxLength);
                            mEditText.setText(sub);
                            mEditText.setSelection(sub.length());// 先定光标
                        }
                    }
                }
            } else {
                resetText = false;
            }
                setButtonState();
            String text = (mEditText.getText().toString()).trim();
            if (TextUtils.isEmpty(text)) {
                titleBar.setRightBtnClicked(false);
            } else {
                titleBar.setRightBtnClicked(true);
            }
        }
        private void setButtonState(){
            if (mEditText.getText().toString().equals(mValue)||mEditText.getText().toString().equals("")) {
                rightbtn.setTextColor(Color.parseColor("#222625"));
            } else {
                rightbtn.setTextColor(Color.parseColor("#49afcc"));
            }
        }
        private void showErrorToast() {
            switch (mType) {
                case VALUE_OF_TYPE_MODIFY_NOTE_NAME:
                    showToast(getString(R.string.note_name_format_invalid));
                    break;
                case VALUE_OF_TYPE_MODIFY_NICK_NAME:
                    showToast(getString(R.string.nickname_format_invalid));
                    break;
                default:
                    break;
            }
        }

        /**
         * 检测是否有emoji表情
         *
         * @param source
         * @return
         */
        public boolean containsEmoji(String source) {
            int len = source.length();
            for (int i = 0; i < len; i++) {
                char codePoint = source.charAt(i);
                if (!isEmojiCharacter(codePoint)) {// 如果不能匹配,则该字符是Emoji表情
                    return true;
                }
            }
            return false;
        }

        /**
         * 判断是否是Emoji
         *
         * @param codePoint
         *            比较的单个字符
         * @return
         */
        private boolean isEmojiCharacter(char codePoint) {
            return (codePoint == 0x0) || (codePoint == 0x9)
                    || (codePoint == 0xA) || (codePoint == 0xD)
                    || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
                    || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
                    || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
        }


        /**
         * 判定输入中英文、数字
         *
         * @param c
         * @return
         */
        private boolean isLegal(String c) {
            Pattern pattern = Pattern.compile("[a-zA-Z0-9\u4e00-\u9fa5]+");
            Matcher matcher = pattern.matcher(c);
            return matcher.matches();
        }
    };

    protected void showToast(String string) {
        LogUtil.d(string);
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        CustomLog.d(TAG,"onResume begin");
        super.onResume();
        CustomLog.d(TAG,"onResume end");
    }

    @Override
    protected void onStop() {
        CustomLog.d(TAG,"onStop begin");
        super.onStop();
        CustomLog.d(TAG,"onStop end");
    }

    @Override
    protected void onDestroy() {
        CustomLog.d(TAG,"onDestroy begin");
        super.onDestroy();
        CustomLog.d(TAG,"onDestroy end");
    }

    private void doSave() {
        String text = mEditText.getText().toString().trim();
        switch (mType) {
            case VALUE_OF_TYPE_MODIFY_NOTE_NAME:
                LogUtil.d("VALUE_OF_TYPE_MODIFY_NOTE_NAME");
                Intent intent = new Intent();
                intent.putExtra(KEY_OF_VALUE, text);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case VALUE_OF_TYPE_MODIFY_NICK_NAME:
                String nickName = MedicalApplication.getPreference().getKeyValue(
                        PrefType.USER_NICKNAME, "");
                if (text.equals(nickName)) {
                    finish();
                    break;
                }
                changNickName(text);
                break;
            case VALUE_OF_TYPE_MODIFY_PWD:
                String password = MedicalApplication.getPreference().getKeyValue(
                        PrefType.LOGIN_PASSWORD, "");
                if (text.equals(password)) {
                    finish();
                    break;
                }
                // add by zzwang:添加对密码长度的处理
                if (!TextUtils.isEmpty(text)) {
                    if (text.length() != 6) {
                        showToast(CommonUtil
                                .getString(R.string.password_at_least_six));
                        return;
                    }
                } else {
                    showToast(CommonUtil
                            .getString(R.string.password_can_not_be_empty));
                    return;
                }
                changPWD(text);
                break;
            case VALUE_OF_TYPE_MODIFY_GROUP_NAME:
                LogUtil.d("VALUE_OF_TYPE_MODIFY_GROUP_NAME");
                Intent intent1 = new Intent();
                intent1.putExtra(KEY_OF_VALUE, text);
                setResult(RESULT_OK, intent1);
                finish();
                break;
            default:
                break;
        }
    }

    private void changNickName(final String nickname) {
//        String accessToken = MedicalApplication.getPreference().getKeyValue(
//                PrefType.LOGIN_ACCESSTOKENID, "");
//        AsyncTasks invokeTask = new AsyncTasks(
//                UrlConstant.getCommUrl(PrefType.KEY_BAIKU_PASSPORT_URL),
//                GetInterfaceParams.getUserInfoParams(accessToken, nickname, "",
//                        ""),
//                CommonUtil.getString(R.string.modified_user_info_wait_dialog),
//                this, true, true, CommonConstant.ACCESSTOKEN_INVALID);
//        invokeTask.setListenerResult(new ListenerResult() {
//
//            @Override
//            public void getResluts(String reslut) {
//
//                // 修改用户信息接口调用成功
//                LogUtil.d("修改用户信息接口返回" + reslut);
//                try {
//                    JSONObject json = new JSONObject(reslut);
//                    if (CommonConstant.SUCCESS_RESLUT.equals(json
//                            .getString("status"))) {
//                        MedicalApplication.getPreference().setKeyValue(
//                                PrefType.USER_NICKNAME, nickname);
//                        showToast("昵称修改成功");
////                        ButelMeetingManager.getInstance().setCurrentUser(
////                                nickname);
//                        finish();
//                    } else if ("-1".equals(json.getString("status"))) {
//                        showToast("昵称格式无效");
//                    } else {
//                        showToast(json.getString("message"));
//                    }
//                } catch (Exception e) {
//                    CustomLog.d(TAG,"修改用户信息失败" + e.toString());
//                }
//            }
//        });
//        invokeTask.setListenerFaliureResult(new AsyncTasks.ListenerFaliureResult() {
//            @Override
//            public void getResluts(String msg, boolean alerted) {
//                if (!alerted) {
//                    showToast(CommonUtil
//                            .getString(R.string.modified_user_info_fail));
//                }
//            }
//        });
//        invokeTask.exeuteTask();
    }

    private void changPWD(final String password) {
//        String token = MedicalApplication.getPreference().getKeyValue(
//                PrefType.LOGIN_ACCESSTOKENID, "");
//        if (!TextUtils.isEmpty(password)) {
//            // 开始调用修改密码接口
//            LogUtil.d("加密后的新密码：" + MD5.computeOnce(password));
//            AsyncTasks invokeTask = new AsyncTasks(
//                    UrlConstant.getCommUrl(PrefType.KEY_BAIKU_PASSPORT_URL),
//                    GetInterfaceParams.getUpdatePasswordParams(password, token),
//                    getResources().getString(
//                            R.string.modified_user_info_change_password), this,
//                    true, true, CommonConstant.ACCESSTOKEN_INVALID);
//            invokeTask.setListenerResult(new ListenerResult() {
//                @Override
//                public void getResluts(String reslut) {
//                    // 修改用户信息接口调用成功
//                    try {
//                        JSONObject json = new JSONObject(reslut);
//                        if (CommonConstant.SUCCESS_RESLUT.equals(json
//                                .optString("status"))) {
//                            // *********************** start by wangyf
//                            // ***********************
//                            if (json.optString("user") != null) {
//                                JSONObject user = new JSONObject(json
//                                        .optString("user"));
//                                NetPhoneApplication.getPreference()
//                                        .setKeyValue(PrefType.LOGIN_NUBENUMBER,
//                                                user.optString("nubeNumber"));
//                            }
//                            // *********************** end by wangyf
//                            // ***********************
//                            /**
//                             * 修改密码成功后，保存密码到sharepreference并且退出界面
//                             */
//                            showToast(AndroidUtil
//                                    .getString(R.string.change_password_success));
//                            NetPhoneApplication.getPreference().setKeyValue(
//                                    PrefType.LOGIN_PASSWORD, password);
//                            // TODO 因后台原因，须在需改密码成功后再次登录下，更新本地的token
//                            // new Thread(new Runnable() {
//                            // @Override
//                            // public void run() {
//                            // SyncLoginAction.getAccessTokenID();
//                            // }
//                            // }).start();
//                            AppAuthManager.invalidToken("", 1, true);
//                            finish();
//                        } else {
//                            showToast(AndroidUtil
//                                    .getString(R.string.change_password_faliure));
//                        }
//                    } catch (Exception e) {
//                        logE("修改密码信息失败", e);
//                    }
//                }
//            });
//            invokeTask.setListenerFaliureResult(new ListenerFaliureResult() {
//                @Override
//                public void getResluts(String msg, boolean alerted) {
//                    if (!alerted) {
//                        showToast(AndroidUtil
//                                .getString(R.string.change_password_faliure));
//                    }
//                }
//            });
//            invokeTask.exeuteTask();
//        }
    }

}
