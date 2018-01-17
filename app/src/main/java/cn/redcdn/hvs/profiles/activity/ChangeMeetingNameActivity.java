package cn.redcdn.hvs.profiles.activity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.redcdn.datacenter.enterprisecenter.SetAccountAttr;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

/**
 * Created by Administrator on 2017/2/24.
 */

public class ChangeMeetingNameActivity extends BaseActivity {
    private EditText meetingNameEdit = null;
    private Button saveBtn = null;
    private Button backBtn = null;

    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setContentView(R.layout.activity_changemeetingname);
        meetingNameEdit = (EditText) findViewById(R.id.attendmeeting_name_edit);
        if (AccountManager.getInstance(getApplicationContext()).getAccountInfo().nickName.equals("")) {
            AccountManager.getInstance(getApplicationContext()).getAccountInfo().nickName = getString(R.string.no_name);
        }
        meetingNameEdit.setText(AccountManager.getInstance(getApplicationContext()).getAccountInfo().nickName);
        Editable etext = meetingNameEdit.getText();
        Selection.setSelection(etext, etext.length());
        meetingNameEdit.addTextChangedListener(mTextWatcher);

        saveBtn = (Button) findViewById(R.id.changename_sure_btn);
        backBtn = (Button) findViewById(R.id.changemeetingname_back);
        saveBtn.setOnClickListener(mbtnHandleEventListener);
        backBtn.setOnClickListener(mbtnHandleEventListener);
    }

    private void changename_sure_func() {

        final String newmeetingname = meetingNameEdit.getText().toString().trim();
        String token = AccountManager.getInstance(getApplicationContext()).getToken();
        // 提示窗口
        final SetAccountAttr setaccountatt = new SetAccountAttr() {
            @Override
            protected void onSuccess(cn.redcdn.datacenter.meetingmanage.data.ResponseEmpty responseContent) {
                super.onSuccess(responseContent);
                // 移除提示窗口
                ChangeMeetingNameActivity.this.removeLoadingView();
                // 修改新的参会名称，返回到MyFileCardActivity后会自动刷新出来
                AccountManager.getInstance(getApplicationContext()).updateAccountName(newmeetingname);
//          TODO      MeetingManager.getInstance().setAccountName(newmeetingname);
                ChangeMeetingNameActivity.this.finish();
                // 提示窗口
                CustomToast.show(ChangeMeetingNameActivity.this,
                        getString(R.string.change_meeting_name_success), Toast.LENGTH_LONG);
                CustomLog.d("ChangeMeetingName: ", "OnSuccess");
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                ChangeMeetingNameActivity.this.removeLoadingView();
                CustomLog.e("ChangeMeetingName: ",statusInfo);

                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(ChangeMeetingNameActivity.this, getString(R.string.login_checkNetworkError),
                            Toast.LENGTH_LONG);
                    return;
                }
                if(statusCode== SettingData.getInstance().tokenUnExist||statusCode==SettingData.getInstance().tokenInvalid)
                {
                    AccountManager.getInstance(getApplicationContext()).tokenAuthFail(statusCode);
                }

                CustomToast.show(ChangeMeetingNameActivity.this,
                        getString(R.string.change_meeting_name_fail), Toast.LENGTH_LONG);
            }
        };
        ChangeMeetingNameActivity.this.showLoadingView(getString(R.string.changeing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                if (setaccountatt != null)
                    setaccountatt.cancel();
                CustomToast.show(ChangeMeetingNameActivity.this,
                        getString(R.string.cancel_change_meeting_name), Toast.LENGTH_LONG);
            }
        });

        setaccountatt.setAccountAttr(newmeetingname, token);

    }

    @Override
    public void todoClick(int i) {
        // TODO Auto-generated method stub
        super.todoClick(i);
        switch (i) {
            case R.id.changemeetingname_back:
                ChangeMeetingNameActivity.this.finish();
                break;
            case R.id.changename_sure_btn:
                changename_sure_func();
                break;
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        private int editStart;
        private int editEnd;
        private int MAX_COUNT = 16;

        public void afterTextChanged(Editable s) {
            if (meetingNameEdit.getText().toString().isEmpty()) {
                saveBtn.setTextColor(Color.parseColor("#235164"));
                saveBtn.setClickable(false);
            } else {
                saveBtn.setTextColor(Color.parseColor("#4ec4dd"));
                saveBtn.setClickable(true);
            }
            editStart = meetingNameEdit.getSelectionStart();
            editEnd = meetingNameEdit.getSelectionEnd();
            meetingNameEdit.removeTextChangedListener(mTextWatcher);
            while (calculateLength(s.toString()) > MAX_COUNT) {
                s.delete(editStart - 1, editEnd);
                editStart--;
                editEnd--;
            }
            meetingNameEdit.setSelection(editStart);
            meetingNameEdit.addTextChangedListener(mTextWatcher);
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

    };

    private int calculateLength(String etstring) {
        char[] ch = etstring.toCharArray();

        int varlength = 0;
        for (int i = 0; i < ch.length; i++) {
            if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F)
                    || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {
                varlength = varlength + 2;
            } else {
                varlength++;
            }
        }
        return varlength;
    }

}
