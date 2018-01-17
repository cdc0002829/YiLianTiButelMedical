package cn.redcdn.hvs.forceoffline;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.AccountManager.LoginListener;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.IMConstant;
import cn.redcdn.hvs.im.agent.AppP2PAgentManager;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;


public class ForceOfflineDialog extends BaseActivity implements
        View.OnClickListener {
    private String tag = ForceOfflineDialog.class.getName();

    Button reLoginBtn;
    Button ignoreBtn;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(tag, "ForceOfflineDialog onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jmeetingsdk_custom_operate_dialog);

        reLoginBtn = (Button) findViewById(R.id.qn_operae_dialog_right_button);
        reLoginBtn.setOnClickListener(this);
        reLoginBtn.setFocusable(true);
        reLoginBtn.setText(R.string.login_again);
        reLoginBtn.requestFocus();

        ignoreBtn = (Button) findViewById(R.id.qn_operae_dialog_left_button);
        ignoreBtn.setOnClickListener(this);
        ignoreBtn.setText(R.string.iknow);
        ignoreBtn.setFocusable(true);
        setFinishOnTouchOutside(false);

        TextView infoView = (TextView) findViewById(R.id.qn_operate_dialog_body);
        infoView.setText(R.string.have_offline_login_again);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qn_operae_dialog_right_button:
                CustomLog.i(tag, "user click relogin  button, try to relogin");

                showLoadingView(getString(R.string.logining_again), new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        AccountManager.getInstance(ForceOfflineDialog.this).cancelLogin();
                        dialog.dismiss();
                        CustomToast.show(ForceOfflineDialog.this, getString(R.string.again_login_fail),
                                CustomToast.LENGTH_LONG);
                        Intent i = new Intent();
                        i.setClass(ForceOfflineDialog.this, LoginActivity.class);
                        startActivity(i);
                        ForceOfflineDialog.this.finish();
                    }
                });
                AccountManager.getInstance(ForceOfflineDialog.this)
                        .registerLoginCallback(new LoginListener() {

                            @Override
                            public void onLoginSuccess(MDSAccountInfo account) {
                                CustomToast.show(ForceOfflineDialog.this, getString(R.string.again_login_success_connect_im),
                                        CustomToast.LENGTH_LONG);
                                AppP2PAgentManager.p2pAgentMgr.init();
                                if(!IMConstant.isP2PConnect){
                                    AppP2PAgentManager.p2pAgentMgr.connectIMService();
                                }
                                removeLoadingView();
                                ForceOfflineDialog.this.finish();
                            }

                            @Override
                            public void onLoginFailed(int errorCode, String msg) {
                                CustomLog.e(TAG, "重新登录失败!  errorMsg: " + msg);
                                removeLoadingView();
                                CustomToast.show(ForceOfflineDialog.this, getString(R.string.again_login_fail),
                                        CustomToast.LENGTH_LONG);
                                Intent i = new Intent();
                                i.setClass(ForceOfflineDialog.this, LoginActivity.class);
                                startActivity(i);
                                ForceOfflineDialog.this.finish();
                            }
                        });
                AccountManager.getInstance(this).login();

                break;
            case R.id.qn_operae_dialog_left_button:
                CustomLog.i(tag, "user click ignore button, shoud exit application");
                AccountManager.getInstance(this).logout();
                ForceOfflineDialog.this.finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStart() {
        CustomLog.d(tag, "ForceOfflineDialog activity init");

        super.onStart();
    }

    @Override
    protected void onStop() {
        CustomLog.i(tag, "handle ForceOfflineDialog onStop method");
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        CustomLog.i(tag, "ignore backpress key");
        return;
    }
}