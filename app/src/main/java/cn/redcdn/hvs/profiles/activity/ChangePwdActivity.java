package cn.redcdn.hvs.profiles.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.usercenter.ChangePassword;
import cn.redcdn.datacenter.usercenter.data.UserInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.HomeActivity;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.AuditErrorActivity;
import cn.redcdn.hvs.accountoperate.activity.AuditingActivity;
import cn.redcdn.hvs.accountoperate.activity.CardTypeActivity;
import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.im.preference.DaoPreference;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

/**
 * Created by Administrator on 2017/2/24.
 */

public class ChangePwdActivity extends BaseActivity {
    private Button backBtn = null;
    private EditText pwdET = null;
    private Button saveBtn = null;
    private String tag = ChangePwdActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setContentView(R.layout.activitity_changepwd);
        pwdET = (EditText) findViewById(R.id.newpwd_edit);
        backBtn = (Button) findViewById(R.id.changepwd_back);
        saveBtn = (Button) findViewById(R.id.changepwd_sure_btn);
        saveBtn.setTextColor(Color.parseColor("#656a72"));
        saveBtn.setClickable(false);
        pwdET.addTextChangedListener(new TextWatcher() {

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
                if (pwdET.getText().toString().isEmpty()) {
                    saveBtn.setTextColor(Color.parseColor("#656a72"));
                    saveBtn.setClickable(false);
                } else {
                    saveBtn.setTextColor(Color.parseColor("#aa656a72"));
                    saveBtn.setClickable(true);
                }

            }
        });
        backBtn.setOnClickListener(mbtnHandleEventListener);
        saveBtn.setOnClickListener(mbtnHandleEventListener);
    }

    @Override
    public void todoClick(int i) {
        // TODO Auto-generated method stub
        super.todoClick(i);
        switch (i) {
            case R.id.changepwd_back:
                ChangePwdActivity.this.finish();
                break;
            case R.id.changepwd_sure_btn:
                String pwdEditString = pwdET.getText().toString();
                if (pwdEditString != null && !pwdEditString.equalsIgnoreCase(""))
                    changePwd();
                break;
        }
    }


    private void changePwd() {

        final String pwdEditString = pwdET.getText().toString();
        if (pwdEditString != null && !pwdEditString.equalsIgnoreCase("")) {
            if (pwdEditString.length() < 6)
                CustomToast.show(ChangePwdActivity.this, getString(R.string.pwd_not_six),
                        Toast.LENGTH_LONG);
            else {
                final ChangePassword cp = new ChangePassword() {

                    @Override
                    protected void onSuccess(UserInfo responseContent) {
                        KeyEventWrite.write(KeyEventConfig.CHANGEPASSWORD
                                + "_ok" + "_"
                                + AccountManager.getInstance(
                                MedicalApplication.shareInstance().getApplicationContext())
                                .getAccountInfo().nube);
                        ChangePwdActivity.this.removeLoadingView();
                        CustomLog.v(tag,
                                "ResetPassword onSuccess responseContent="
                                        + responseContent.mobile + ","
                                        + responseContent.password);
                        CustomToast.show(ChangePwdActivity.this, getString(R.string.modify_pwd_suc),
                                Toast.LENGTH_LONG);

                        AccountManager.getInstance(MedicalApplication.shareInstance())
                                .registerLoginCallback(new AccountManager.LoginListener() {

                                    @Override
                                    public void onLoginFailed(int errorCode, String msg) {
                                        ChangePwdActivity.this.removeLoadingView();
                                        if (HttpErrorCode.checkNetworkError(errorCode)) {
                                            CustomToast.show(ChangePwdActivity.this,
                                                    getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                                            return;
                                        }

                                        if (NetConnectHelper.getNetWorkType(MedicalApplication.shareInstance()) == NetConnectHelper.NETWORKTYPE_INVALID) {
                                            CustomToast.show(ChangePwdActivity.this,
                                                    getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                                            return;
                                        }
                                        if (errorCode == -909) { //未提交审核资料
                                            Intent intent = new Intent(ChangePwdActivity.this, CardTypeActivity.class);
                                            startActivity(intent);
                                            ChangePwdActivity.this.finish();
                                            return;
                                        }

                                        CustomToast.show(ChangePwdActivity.this, msg, Toast.LENGTH_LONG);
                                        ChangePwdActivity.this.finish();
                                        Intent loginIntent = new Intent();
                                        loginIntent.setClass(ChangePwdActivity.this, LoginActivity.class);
                                        startActivity(loginIntent);
                                        if (msg.contains("账号或密码有误")) {
//                                            pwdEdit.setText("");
                                        }
                                    }

                                    @Override
                                    public void onLoginSuccess(
                                            MDSAccountInfo account) {


                                        int state = AccountManager.getInstance(MedicalApplication.shareInstance()).getAccountInfo().getState();
                                        //审核状态 (1：待审核、2：审核通过、3：未通过)
                                        if (state == 1) {
                                            ChangePwdActivity.this.removeLoadingView();
                                            CustomLog.e(TAG, "未审核");
                                            Intent intent = new Intent(ChangePwdActivity.this, AuditingActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (state == 2) {
                                            ChangePwdActivity.this.removeLoadingView();
                                            CustomLog.e(TAG, "登录成功");
                                            AppP2PAgentManager.p2pAgentMgr.init();
                                            if(!IMConstant.isP2PConnect){
                                                CustomLog.d(TAG,"重新登录IM");
                                                AppP2PAgentManager.p2pAgentMgr.connectIMService();
                                            }
                                            Intent intent = new Intent(ChangePwdActivity.this, HomeActivity.class);
                                            intent.putExtra("", "");
                                            startActivity(intent);
                                            finish();
                                        } else if (state == 3) {
                                            ChangePwdActivity.this.removeLoadingView();
                                            CustomLog.e(TAG, "审核未通过");
                                            Intent intent = new Intent(ChangePwdActivity.this, AuditErrorActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }


                                        DaoPreference daoPre = MedicalApplication.getPreference();

                                        daoPre.setKeyValue(DaoPreference.PrefType.LOGIN_NUMBER_CHANGE, account.getNube());
                                        daoPre.setKeyValue(DaoPreference.PrefType.BEFORE_LOGIN_NUMBER, account.getNube());
                                        daoPre.setKeyValue(DaoPreference.PrefType.USER_NICKNAME, account.getNickName());
                                        daoPre.setKeyValue(DaoPreference.PrefType.LOGIN_NUBENUMBER, account.getNube());
                                        daoPre.setKeyValue(DaoPreference.PrefType.LOGIN_MOBILE, account.getMobile());
                                        ;
                                        daoPre.setKeyValue(DaoPreference.PrefType.LOGIN_ACCESSTOKENID, account.getAccessToken());


                                    }

                                });
                        ChangePwdActivity.this.showLoadingView(getString(R.string.logining), new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.dismiss();
                                AccountManager.getInstance(
                                        MedicalApplication.shareInstance()).cancelLogin();
                                CustomToast.show(ChangePwdActivity.this, getString(R.string.login_cancel),
                                        Toast.LENGTH_LONG);

                            }
                        });
                        AccountManager.getInstance(MedicalApplication.shareInstance())
                                .login(responseContent.nubeNumber,
                                        pwdET.getText().toString());
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        KeyEventWrite.write(KeyEventConfig.CHANGEPASSWORD + "_fail" + "_"
                                + AccountManager.getInstance(
                                MedicalApplication.shareInstance().getApplicationContext())
                                .getAccountInfo().nube + "_"
                                + statusCode);
                        ChangePwdActivity.this.removeLoadingView();
                        CustomLog.v(tag, "ResetPassword onFail statusCode="
                                + statusCode);
                        if (HttpErrorCode.checkNetworkError(statusCode)) {
                            CustomToast.show(ChangePwdActivity.this,
                                    getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
                            return;
                        }

                        if (statusCode == SettingData.getInstance().tokenUnExist
                                || statusCode == SettingData.getInstance().tokenInvalid) {
                            CustomToast.show(getApplicationContext(),
                                    getString(R.string.token_fail), Toast.LENGTH_SHORT);
                            AccountManager.getInstance(getApplicationContext())
                                    .tokenAuthFail(statusCode);
                            return;
                        }
                        if (statusCode == -201) {
                            CustomToast.show(getApplicationContext(),
                                    getString(R.string.token_fail), Toast.LENGTH_SHORT);
                            AccountManager.getInstance(getApplicationContext())
                                    .tokenAuthFail(statusCode);
                            return;
                        }
                        CustomToast.show(ChangePwdActivity.this, getString(R.string.change_password_faliure)+"="
                                + statusCode, Toast.LENGTH_LONG);

                    }
                };
                ChangePwdActivity.this.showLoadingView(getString(R.string.uping),
                        new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.dismiss();
                                cp.cancel();
                                CustomToast.show(ChangePwdActivity.this,
                                        getString(R.string.cancel_modify_pwd), Toast.LENGTH_LONG);
                            }
                        });

                cp.changePassword(CommonUtil.string2MD5(pwdEditString),
                        AccountManager.getInstance(getApplicationContext())
                                .getToken());

            }
        } else {
            CustomToast.show(ChangePwdActivity.this, getString(R.string.password_can_not_be_empty),
                    Toast.LENGTH_LONG);
        }
    }

}
