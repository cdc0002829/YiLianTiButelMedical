package cn.redcdn.hvs.accountoperate.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.datacenter.enterprisecenter.data.DeviceType;
import cn.redcdn.datacenter.meetingmanage.data.ResponseEmpty;
import cn.redcdn.datacenter.usercenter.GetNubeNumberList;
import cn.redcdn.datacenter.usercenter.RegisterAccount;
import cn.redcdn.datacenter.usercenter.data.NubeNumberInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;


public class PhoneRegisterActivity extends cn.redcdn.hvs.base.BaseActivity {
    private EditText numEditText = null;
    private EditText pwdEidtText = null;
    private Button nextBtn = null;
    private Button backBtn = null;
    private TextView webviewtext;
    private TextView phone_to_email_text;
    private String nubeNumber;
    private String tag = PhoneRegisterActivity.class.getCanonicalName();
    private GetNubeNumberList gnl;
    private RegisterAccount ra;
    private CheckBox agreementCb;
    private Boolean isCheck=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_register);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.swift_login));
        titleBar.setTitleTextColor(Color.BLACK);
        titleBar.enableBack();
        numEditText = (EditText) findViewById(R.id.register_num_edit);
        numEditText.addTextChangedListener(new watcher());
        pwdEidtText = (EditText) findViewById(R.id.register_pwd_edit);
        pwdEidtText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if ((numEditText.getText() != null && !numEditText.getText().toString()
                        .equalsIgnoreCase(""))
                        && (pwdEidtText.getText() != null && !pwdEidtText
                        .getText().toString().equalsIgnoreCase(""))) {
                    nextBtn.setBackgroundResource(R.drawable.button_selector);
                    nextBtn.setClickable(true);
                } else {
                    nextBtn.setClickable(false);
                    nextBtn.setBackgroundResource(R.drawable.button_btn_notclick);
                }

            }

        });
        backBtn = (Button) findViewById(R.id.back_btn);
        nextBtn = (Button) findViewById(R.id.register_next_btn);
        webviewtext= (TextView) findViewById(R.id.phone_register_webview);
        phone_to_email_text= (TextView) findViewById(R.id.phone_to_emailtext);
        agreementCb= (CheckBox) findViewById(R.id.agreement_cb);
        agreementCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    isCheck=true;
                }else{
                    isCheck=false;
                }
            }
        });
        backBtn.setOnClickListener(mbtnHandleEventListener);
        nextBtn.setOnClickListener(mbtnHandleEventListener);
        webviewtext.setOnClickListener(mbtnHandleEventListener);
        phone_to_email_text.setOnClickListener(mbtnHandleEventListener);
        nextBtn.setBackgroundResource(R.drawable.button_btn_notclick);
        nextBtn.setClickable(false);
    }



    @Override
    public void todoClick(int i) {
        // TODO Auto-generated method stub
        super.todoClick(i);
        switch (i) {
            case R.id.register_next_btn:
                execNext();
                break;

            case R.id.back_btn:
                CustomLog.v(tag, ".................");
                final CustomDialog cd=new CustomDialog(PhoneRegisterActivity.this);
                cd.setTip(getString(R.string.config_quit_login));
                cd.setOkBtnText(getString(R.string.btn_ok));
                cd.setCancelBtnText(getString(R.string.btn_cancle));
                cd.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {

                    @Override
                    public void onClick(CustomDialog customDialog) {
                        PhoneRegisterActivity.this.finish();
                    }
                });
                cd.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {

                    @Override
                    public void onClick(CustomDialog customDialog) {
                        cd.dismiss();
                    }
                });
                cd.show();
                break;
            case R.id.phone_register_webview:
                Intent intentwebview=new Intent(PhoneRegisterActivity.this,ServiceAgreementActivity.class);
                startActivity(intentwebview);
                break;
            case R.id.phone_to_emailtext:
                Intent intent_toemail=new Intent(PhoneRegisterActivity.this,EmailRegisterActivity.class);
                startActivity(intent_toemail);
        }
    }



    private boolean inValidNum(String num) {
        boolean is = false;
        if (numEditText.getText().toString().length() < 11) {
            return is;
        }

        Pattern p = Pattern
                .compile("^((1))\\d{10}$");
        Matcher m = p.matcher(num);
        if (m.matches())
            is = true;
        return is;
    }

    private void execNext() {
        if (isCheck==false){
            CustomToast.show(this,getString(R.string.please_indentify_agreement),7000);
            return;
        }

        if (!inValidNum(numEditText.getText().toString())) {
            CustomToast.show(PhoneRegisterActivity.this,getString(R.string.phone_wrong_type),
                    Toast.LENGTH_LONG);
            return;
        }
        if (pwdEidtText.getText().toString().length() < 6) {
            CustomToast.show(PhoneRegisterActivity.this, getString(R.string.pwd_wrong_type),
                    Toast.LENGTH_LONG);
            return;
        }
        PhoneRegisterActivity.this.showLoadingView(getString(R.string.registering),new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                if(gnl!=null)
                    gnl.cancel();
                if(ra!=null)
                    ra.cancel();
            }
        });

        gnl = new GetNubeNumberList() {

            @Override
            protected void onSuccess(NubeNumberInfo responseContent) {
                KeyEventWrite.write(KeyEventConfig.GET_NUBE
                        + "_ok" + "_"
                        +  AccountManager.getInstance(
                        MedicalApplication.shareInstance().getApplicationContext())
                        .getAccountInfo().nube);
                nubeNumber = responseContent.nubeNumber;
                CustomLog.v(tag, "GetNubeNumberList onSuccess nubeNumber="
                        + nubeNumber);

                ra = new RegisterAccount() {

                    @Override
                    protected void onSuccess(ResponseEmpty responseContent) {
                        KeyEventWrite.write(KeyEventConfig.SEND_CHECKCODE
                                + "_ok" + "_"
                                +  AccountManager.getInstance(
                                MedicalApplication.shareInstance().getApplicationContext())
                                .getAccountInfo().nube);
                        CustomLog.v(tag, "RegisterAccount onSuccess");
                        PhoneRegisterActivity.this.removeLoadingView();
                        CustomToast.show(PhoneRegisterActivity.this,
                                getString(R.string.phone_check), Toast.LENGTH_LONG);
                        Intent i = new Intent();
                        i.putExtra("account", numEditText.getText().toString());
                        i.putExtra("nubenumber", nubeNumber);
                        i.putExtra("pwd", pwdEidtText.getText().toString());
                        i.setClass(PhoneRegisterActivity.this,
                                RegisterCheckCodeActivity.class);
                        startActivity(i);
                        PhoneRegisterActivity.this.finish();

                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        KeyEventWrite.write(KeyEventConfig.SEND_CHECKCODE + "_fail" + "_"
                                + AccountManager.getInstance(
                                MedicalApplication.shareInstance().getApplicationContext())
                                .getAccountInfo().nube + "_"
                                + statusCode);
                        CustomLog.v(tag, "RegisterAccount onfail statusCode="
                                + statusCode + ",statusInfo=" + statusInfo);
                        PhoneRegisterActivity.this.removeLoadingView();
                        if (HttpErrorCode.checkNetworkError(statusCode)) {
                            CustomToast.show(PhoneRegisterActivity.this,
                                    getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                            return;
                        }
                        if(NetConnectHelper.getNetWorkType(MedicalApplication.getContext()) == NetConnectHelper.NETWORKTYPE_INVALID)
                        {
                            CustomToast.show(PhoneRegisterActivity.this,
                                   getString( R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                            return;
                        }
                        if (statusCode == -432) {

                            CustomToast.show(PhoneRegisterActivity.this,
                                    getString(R.string.login_mobile), Toast.LENGTH_LONG);
                            return;
                        }
                        if (statusCode == -452) {
                            CustomToast.show(PhoneRegisterActivity.this,
                                    getString(R.string.most_account_register),
                                    Toast.LENGTH_LONG);
                            return;
                        }
                        if (statusCode == -79) {
                            CustomToast.show(PhoneRegisterActivity.this,
                                   getString( R.string.register_failed),
                                    Toast.LENGTH_LONG);
                            return;
                        }
                        // 其他状态还不确定
                        CustomToast.show(PhoneRegisterActivity.this, getString(R.string.register_failed_me)+"="
                                + statusCode, Toast.LENGTH_LONG);

                    }
                };

                ra.registerAccount(
                        "mobile",
                        numEditText.getText().toString(),
                        CommonUtil.string2MD5(pwdEidtText.getText().toString()),
                        nubeNumber, "mobinp_JIHY", SettingData.AUTH_PRODUCT_ID,DeviceType.ANDROID_JIHY,RegisterAccount.ProductType_HVS);

            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                KeyEventWrite.write(KeyEventConfig.GET_NUBE + "_fail" + "_"
                        + AccountManager.getInstance(
                        MedicalApplication.shareInstance().getApplicationContext())
                        .getAccountInfo().nube + "_"
                        + statusCode);
                CustomLog.v(tag, "GetNubeNumberList onfail statusCode="
                        + statusCode);
                PhoneRegisterActivity.this.removeLoadingView();
                if(NetConnectHelper.getNetWorkType(MedicalApplication.getContext()) == NetConnectHelper.NETWORKTYPE_INVALID)
                {
                    CustomToast.show(PhoneRegisterActivity.this,
                            getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                    return;
                }
                CustomToast.show(PhoneRegisterActivity.this, getString(R.string.register_failed_me)+"="+statusCode,
                        Toast.LENGTH_LONG);
            }
        };
        gnl.getNubeNumberList("mobinp_JIHY", "mobile",SettingData.getInstance().AppKey,6);
    }

    class watcher implements TextWatcher {

        @Override
        public void afterTextChanged(Editable arg0) {

        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
            if ((numEditText.getText() != null && !numEditText.getText().toString()
                    .equalsIgnoreCase(""))
                    && (pwdEidtText.getText() != null && !pwdEidtText
                    .getText().toString().equalsIgnoreCase(""))) {
                nextBtn.setBackgroundResource(R.drawable.button_selector);
                nextBtn.setClickable(true);
            } else {
                nextBtn.setClickable(false);
                nextBtn.setBackgroundResource(R.drawable.button_btn_notclick);
            }

        }
    }

    @Override
    public void onBackPressed() {
        PhoneRegisterActivity.this.removeLoadingView();
        if(gnl!=null)
            gnl.cancel();
        if(ra!=null)
            ra.cancel();
        super.onBackPressed();
        this.finish();
    }

}
