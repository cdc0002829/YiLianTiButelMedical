package cn.redcdn.hvs.boot;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.AccountManager.LoginListener;
import cn.redcdn.hvs.AccountManager.LoginState;
import cn.redcdn.hvs.HomeActivity;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.LoginActivity;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomDialog.OKBtnOnClickListener;
import cn.redcdn.hvs.util.youmeng.AnalysisConfig;
import cn.redcdn.log.CustomLog;
import com.umeng.analytics.MobclickAgent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    // 标识请求的权限
    public static final int PERMISSIONS_REQUEST_CODE = 0;

    private BootManager mBootManager;
    private String urlMeetingId;//短信链接中的会议号


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN) @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        allowTwiceToExit();
        parseIntent();

        permissionApply();

    }


    /**
     * 检查那几个权限需要申请，并分类申请
     */
    private void permissionApply() {
        CustomLog.i(TAG, "permissionApply()");

        BootPermissionManager mBootPermissionManager = new BootPermissionManager(this);

        Boolean resultWrite = CommonUtil.selfPermissionGranted(SplashActivity.this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Boolean resultRead = CommonUtil.selfPermissionGranted(SplashActivity.this,
            Manifest.permission.READ_EXTERNAL_STORAGE);
        Boolean resultPhone = CommonUtil.selfPermissionGranted(SplashActivity.this,
            Manifest.permission.READ_PHONE_STATE);
        Boolean resultAccount = CommonUtil.selfPermissionGranted(SplashActivity.this,
            Manifest.permission.GET_ACCOUNTS);

        if (!(resultRead && resultWrite) && resultPhone && resultAccount) {
            mBootPermissionManager.requestPermissionA();
        } else if ((resultRead && resultWrite) && !resultPhone && resultAccount) {
            mBootPermissionManager.requestPermissionB();
        } else if ((resultRead && resultWrite) && resultPhone && !resultAccount) {
            mBootPermissionManager.requestPermissionC();
        } else if (!(resultRead && resultWrite) && !resultPhone && resultAccount) {
            mBootPermissionManager.requestPermissionAB();
        } else if (!(resultRead && resultWrite) && resultPhone && !resultAccount) {
            mBootPermissionManager.requestPermissionAC();
        } else if ((resultRead && resultWrite) && !resultPhone && !resultAccount) {
            mBootPermissionManager.requestPermissionBC();
        } else if (!(resultRead && resultWrite) && !resultPhone && !resultAccount) {
            mBootPermissionManager.requestPermissionABC();
        } else {
            bootApp();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        CustomLog.i(TAG, "onRequestPermissionsResult()");

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        showPermissionDialog(i, permissions);
                        return;
                    }
                }
                bootApp();
            }

        }
    }


    /**
     * 启动应用
     */
    private void bootApp() {
        CustomLog.i(TAG, "bootApp()");

        if (!MedicalApplication.shareInstance().getInitStatus()) {
            boot(); // 如果程序未启动，执行启动逻辑
        } else {
            SplashActivity.this.onBootSuccess();
        }
    }


    private void parseIntent() {
        CustomLog.i(TAG, "parseIntent()");

        // 判断是否通过短信链接启动应用
        CustomLog.i(TAG, "parseIntent::");
        Intent intent = getIntent();
        if (intent != null) {
            String url = intent.getDataString();
            CustomLog.i(TAG, "判断是否是通过短信链接启动应用: " + url);
            if (url != null && (url.startsWith("http") ||
                url.startsWith(MedicalApplication.getContext().getPackageName()))) {
                try {
                    //短信链接内容中获取的meetingId
                    urlMeetingId = url.substring(
                        (MedicalApplication.getContext().getPackageName() + "://").length(),
                        url.length());
                    if (isNumeric(urlMeetingId)) {
                        MobclickAgent.onEvent(SplashActivity.this,
                            AnalysisConfig.JOINMEETING_BY_MESSAGE);
                        MedicalApplication.shareInstance().setIsFromMessageLink(true);
                    } else {
                        urlMeetingId = "";
                    }
                    CustomLog.i(TAG, "通过短信链接获取到的 meetingId:" + urlMeetingId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } else {
            CustomLog.i(TAG, "SplashActivity::parseIntent 不是通过短信链接启动");
        }
    }


    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        parseIntent();
        if (MedicalApplication.shareInstance().getInitStatus()) {
            onBootSuccess();
        }
    }


    /**
     * 启动成功 获取登录状态，已登录的情况下检查是否是通过短信链接启动，如果是，则跳转到会诊列表页面
     */
    private void onBootSuccess() {
        CustomLog.i(TAG, "SplashActivity::onBootSuccess 启动成功");
        if (mBootManager != null) {
            mBootManager.release();
            mBootManager = null;
        }
        MedicalApplication.shareInstance().setInit(true);
        LoginState loginState = AccountManager.getInstance(getApplicationContext())
            .getLoginState();
        CustomLog.i(TAG, "SplashActivity::onBootSuccess 登录状态:  " + loginState);
        if (loginState == LoginState.ONLINE) {
            Intent i = new Intent(SplashActivity.this, HomeActivity.class);
            if (urlMeetingId != null) {
                i.putExtra("urlMeetingId", urlMeetingId);
            }
            startActivity(i);
            finish();
        } else if (loginState == LoginState.OFFLINE) {
            login();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        CustomLog.i(TAG, "onResume()");

        if (mBootManager != null &&
            mBootManager.getCurrentStep() == BootManager.MSG_CHECK_APP_VERSION) {
            CustomLog.d(TAG, "SplashActivity::onResume 继续执行检测应用版本");
            mBootManager.retry(mBootManager.getCurrentStep());
        }
    }


    private void onBootFailed(final int step, final int errorLevel,
                              final String errorMsg) {
        CustomLog.e(TAG,
            "启动出错!  errorMsg: " + errorMsg + "  errorLevel: " + errorLevel + " step: " + step);
        if (MedicalApplication.shareInstance().getInitStatus()) {
            CustomLog.e(TAG, "启动已完成，不处理启动出错信息!");
        }
        CustomDialog dialog = new CustomDialog(this);
        dialog.setOkBtnOnClickListener(new OKBtnOnClickListener() {

            @Override
            public void onClick(CustomDialog customDialog) {
                MedicalApplication.shareInstance().exit();
            }
        });
        dialog.setTip(getString(R.string.networkAbnormalPleaseCheck));
        dialog.removeCancelBtn();
        dialog.setCancelable(false);
        dialog.setOkBtnText(getString(R.string.btn_ok));
        dialog.show();
    }


    private void login() {
        CustomLog.i(TAG, "login()");

        AccountManager.getInstance(MedicalApplication.shareInstance())
            .registerLoginCallback(new LoginListener() {

                @Override
                public void onLoginFailed(int errorCode, String msg) {
                    CustomLog.d(TAG, "自动登录出错!  errorMsg: " + msg);
                    Intent i = new Intent();
                    i.setClass(SplashActivity.this, LoginActivity.class);
                    if (urlMeetingId != null && urlMeetingId != "") {
                        i.putExtra("urlMeetingId", urlMeetingId);
                    }
                    startActivity(i);
                    finish();
                }


                @Override
                public void onLoginSuccess(MDSAccountInfo account) {
                    //此处增加声音检测，防止首次登录成功后立即2次返回，未进行检测
                    SharedPreferences sharedPreferences = getSharedPreferences("VDS",
                        Activity.MODE_PRIVATE);
                    int hasVoiceDetect = sharedPreferences.getInt("hasVoiceDetect", 0);
                    System.out.println("hasVoiceDetect = " + hasVoiceDetect);
                    if (hasVoiceDetect == 1) {
                        CustomLog.i(TAG, "已经检测过，进入主页");
                        MobclickAgent.onEvent(SplashActivity.this, AnalysisConfig.ACCESS_HOME);
                        // switchToMainActivity();
                        Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                        if (urlMeetingId != null && urlMeetingId != "") {
                            i.putExtra("urlMeetingId", urlMeetingId);
                        }
                        startActivity(i);
                        finish();
                    } else {
                        System.out.println("未检测过,进行检测");
                        Intent i = new Intent(SplashActivity.this, HomeActivity.class);
                        if (urlMeetingId != null && urlMeetingId != "") {
                            i.putExtra("urlMeetingId", urlMeetingId);
                        }
                        startActivity(i);
                        finish();
                        // switchToMainActivity();
                    }
                }
            });
        AccountManager.getInstance(MedicalApplication.shareInstance()).login();
    }


    /**
     * 执行应用启动逻辑
     */
    private void boot() {
        CustomLog.i(TAG, "boot()");

        mBootManager = new BootManager(this) {
            @Override
            public void onBootSuccess() {
                SplashActivity.this.onBootSuccess();
            }


            @Override
            public void onBootFailed(int step, int errorCode, String errorMsg) {
                SplashActivity.this.onBootFailed(step, errorCode, errorMsg);
            }
        };
        mBootManager.start();
    }


    /**
     * 展示设置权限Dialog
     */
    private void showPermissionDialog(int index, String[] permissions) {
        CustomLog.i(TAG, "showPermissionDialog()");

        String dialogContent = "";
        if (permissions[index].equalsIgnoreCase(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            dialogContent = getString(R.string.no_storage_permission_dialog_content);
        } else if (permissions[index].equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE)) {
            dialogContent = getString(R.string.no_phone_state_permission_dialog_content);
        } else if (permissions[index].equalsIgnoreCase(Manifest.permission.GET_ACCOUNTS)) {
            dialogContent = getString(R.string.no_get_account_permission_dialog_content);
        }

        final CustomDialog cd = new CustomDialog(SplashActivity.this);
        cd.setTip(dialogContent);
        cd.setCancelBtnText(getString(R.string.btn_cancle));
        cd.setOkBtnText(getString(R.string.go_to_setting));
        cd.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                MedicalApplication.shareInstance().exit();
            }
        });
        cd.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override public void onClick(CustomDialog customDialog) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                try {
                    cd.dismiss();
                    startActivity(intent);
                    MedicalApplication.shareInstance().exit();
                } catch (Exception e) {
                    CustomLog.e(TAG, "go to permission apply " + e.toString());
                }
            }
        });
        cd.show();
    }
}
