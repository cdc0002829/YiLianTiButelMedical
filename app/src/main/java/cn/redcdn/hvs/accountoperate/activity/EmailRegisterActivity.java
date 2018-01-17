package cn.redcdn.hvs.accountoperate.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
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
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

/**
 * Created by thinkpad on 2017/2/13.
 * 邮箱注册页面
 */
public class EmailRegisterActivity extends cn.redcdn.hvs.base.BaseActivity {
    private Button register_back, register_next_btn, btn_declare;
    private EditText register_mail_edit, register_pwd_edit;
    private RadioButton rb_read;
    private GetNubeNumberList gnl;
    private RegisterAccount ra;
    private String nubeNumber;
    private CheckBox agreementCb;
    private boolean isCheck=false;
    private TextView webView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register);
        initView();
    }

    private void initView() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.email_register));
        titleBar.setTitleTextColor(Color.BLACK);
        titleBar.enableBack();

        webView = (TextView) findViewById(R.id.webviewBnt);
        webView.setOnClickListener(mbtnHandleEventListener);
        register_back = (Button) findViewById(R.id.back_btn);
        register_next_btn = (Button) findViewById(R.id.email_register_next_btn);
        agreementCb = (CheckBox) findViewById(R.id.agreement_email_cb);
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
        register_back.setOnClickListener(mbtnHandleEventListener);
        register_next_btn.setOnClickListener(mbtnHandleEventListener);
        register_next_btn.setBackgroundResource(R.drawable.button_btn_notclick);
        register_next_btn.setClickable(false);


        //邮箱账号
        register_mail_edit = (EditText) findViewById(R.id.register_mail_edit);
        register_mail_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if ((register_mail_edit.getText() != null && !register_mail_edit.getText().toString()
                        .equalsIgnoreCase(""))
                        && (register_pwd_edit.getText() != null && !register_pwd_edit
                        .getText().toString().equalsIgnoreCase(""))) {
                    register_next_btn.setBackgroundResource(R.drawable.button_selector);
                    register_next_btn.setClickable(true);
                } else {
                    register_next_btn.setClickable(false);
                    register_next_btn.setBackgroundResource(R.drawable.button_btn_notclick);
                }

            }
        });
        register_pwd_edit = (EditText) findViewById(R.id.register_pwd_edit);
        register_pwd_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if ((register_mail_edit.getText() != null && !register_mail_edit.getText().toString()
                        .equalsIgnoreCase(""))
                        && (register_pwd_edit.getText() != null && !register_pwd_edit
                        .getText().toString().equalsIgnoreCase(""))) {
                    register_next_btn.setBackgroundResource(R.drawable.button_selector);
                    register_next_btn.setClickable(true);
                } else {
                    register_next_btn.setClickable(false);
                    register_next_btn.setBackgroundResource(R.drawable.button_btn_notclick);
                }
            }
        });

    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i){
            case R.id.back_btn:
                EmailRegisterActivity.this.finish();
                break;
            case R.id.email_register_next_btn:
                execNext();
                break;
            case R.id.webviewBnt:
                Intent intentwebview=new Intent(EmailRegisterActivity.this,ServiceAgreementActivity.class);
                startActivity(intentwebview);
                break;
        }
    }


    public boolean isEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);

        return m.matches();
    }

    private void execNext() {

        if (isCheck==false){
            CustomToast.show(this,getString(R.string.please_indentify_agreement),7000);
            return;
        }

        if (!isEmail(register_mail_edit.getText().toString())) {
            CustomToast.show(EmailRegisterActivity.this, getString(R.string.email_wrong_type),
                    Toast.LENGTH_LONG);
            return;
        }
        if (register_pwd_edit.getText().toString().length() < 6) {
            CustomToast.show(EmailRegisterActivity.this, getString(R.string.pwd_wrong_type),
                    Toast.LENGTH_LONG);
            return;
        }
        EmailRegisterActivity.this.showLoadingView(getString(R.string.registering),new DialogInterface.OnCancelListener() {

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
                CustomLog.v(TAG, "GetNubeNumberList onSuccess nubeNumber="
                        + nubeNumber);
                ra = new RegisterAccount() {

                    @Override
                    protected void onSuccess(ResponseEmpty responseContent) {
                        KeyEventWrite.write(KeyEventConfig.SEND_CHECKCODE
                                + "_ok" + "_"
                                +  AccountManager.getInstance(
                                MedicalApplication.shareInstance().getApplicationContext())
                                .getAccountInfo().nube);
                        CustomLog.v(TAG, "RegisterAccount onSuccess");
                        EmailRegisterActivity.this.removeLoadingView();
                        CustomToast.show(EmailRegisterActivity.this,
                                getString(R.string.email_check), Toast.LENGTH_LONG);
                        Intent i = new Intent();
                        i.putExtra("account", register_mail_edit.getText().toString());
                        i.putExtra("nubenumber", nubeNumber);
                        i.putExtra("pwd", register_pwd_edit.getText().toString());
                        i.setClass(EmailRegisterActivity.this,
                                RegisterCheckCodeActivity.class);
                        startActivity(i);
                        EmailRegisterActivity.this.finish();

                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        KeyEventWrite.write(KeyEventConfig.SEND_CHECKCODE + "_fail" + "_"
                                + AccountManager.getInstance(
                                MedicalApplication.shareInstance().getApplicationContext())
                                .getAccountInfo().nube + "_"
                                + statusCode);
                        CustomLog.v(TAG, "RegisterAccount onfail statusCode="
                                + statusCode + ",statusInfo=" + statusInfo);
                        EmailRegisterActivity.this.removeLoadingView();
                        if (HttpErrorCode.checkNetworkError(statusCode)) {
                            CustomToast.show(EmailRegisterActivity.this,
                                    getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                            return;
                        }
                        if(NetConnectHelper.getNetWorkType(MedicalApplication.getContext()) == NetConnectHelper.NETWORKTYPE_INVALID)
                        {
                            CustomToast.show(EmailRegisterActivity.this,
                                    getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                            return;
                        }
                        if (statusCode == -432) {

                            CustomToast.show(EmailRegisterActivity.this,
                                    getString(R.string.eamil_registered), Toast.LENGTH_LONG);
                            return;
                        }
                        if (statusCode == -452) {
                            CustomToast.show(EmailRegisterActivity.this,
                                    getString(R.string.most_account_register),
                                    Toast.LENGTH_LONG);
                            return;
                        }
                        if (statusCode == -79) {
                            CustomToast.show(EmailRegisterActivity.this,
                                    getString(R.string.register_failed),
                                    Toast.LENGTH_LONG);
                            return;
                        }
                        // 其他状态还不确定
                        CustomToast.show(EmailRegisterActivity.this, getString(R.string.register_failed_me)+"="
                                + statusCode, Toast.LENGTH_LONG);

                    }
                };

                ra.registerAccount(
                        "pc",
                        register_mail_edit.getText().toString(),
                        CommonUtil.string2MD5(register_pwd_edit.getText().toString()),
                        nubeNumber, "mobinp_JIHY", SettingData.AUTH_PRODUCT_ID, DeviceType.ANDROID_JIHY,RegisterAccount.ProductType_HVS);

            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                KeyEventWrite.write(KeyEventConfig.GET_NUBE + "_fail" + "_"
                        + AccountManager.getInstance(
                        MedicalApplication.shareInstance().getApplicationContext())
                        .getAccountInfo().nube + "_"
                        + statusCode);
                CustomLog.v(TAG, "GetNubeNumberList onfail statusCode="
                        + statusCode);
                EmailRegisterActivity.this.removeLoadingView();
                if(NetConnectHelper.getNetWorkType(MedicalApplication.getContext()) == NetConnectHelper.NETWORKTYPE_INVALID)
                {
                    CustomToast.show(EmailRegisterActivity.this,
                            getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                    return;
                }
                CustomToast.show(EmailRegisterActivity.this, getString(R.string.register_failed_me)+"="+statusCode,
                        Toast.LENGTH_LONG);
            }
        };
        gnl.getNubeNumberList("mobinp_JIHY", "mobile",SettingData.getInstance().AppKey,6);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
