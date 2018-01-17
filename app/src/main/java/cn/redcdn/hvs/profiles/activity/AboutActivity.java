package cn.redcdn.hvs.profiles.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.appinstall.InstallCallBackListerner;
import cn.redcdn.hvs.appinstall.MeetingVersionManager;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Created by Administrator on 2017/2/24.
 */



public class AboutActivity extends BaseActivity {


    private RelativeLayout check;
    private TextView txtVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_layout);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.my_about));
        titleBar.enableBack();
        initWidget();
        initAppVersionName();
    }

    private void initAppVersionName() {
        try {
            PackageManager pm = AboutActivity.this.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(
                    AboutActivity.this.getPackageName(), 0);
            String version = pi.versionName;
            if (version != null && version.length() > 0) {
                txtVersion.setText("V" + version);
            }
        } catch (Exception e) {
            CustomLog.e("AboutActivity", "initAppVersionName Exception");
        }
    }

    private void initWidget() {

        txtVersion = (TextView) findViewById(R.id.app_version);

        check = (RelativeLayout) findViewById(R.id.check_app);

        check.setOnClickListener(mbtnHandleEventListener);




    }

    @Override
    public void todoClick(int i) {
        // TODO Auto-generated method stub
        super.todoClick(i);
        switch (i) {
            case R.id.check_app:
                String list = MeetingVersionManager.getInstance().getChangelist();
                if (list != null && !list.equals("")) {
                    list = getString(R.string.newFunction)+"\r\n" + list;
                } else {
                    list = getString(R.string.foundtobeupdatedintime);
                }
                if (NetConnectHelper.getNetWorkType(getApplicationContext()) == -1) {
                    CustomToast.show(AboutActivity.this, getString(R.string.login_checkNetworkError),
                            Toast.LENGTH_LONG);
                } else {
                    if (!MeetingVersionManager.getInstance().isHasInstall(
                            AboutActivity.this)) {


                        InstallCallBackListerner appVersionCheckListener = new InstallCallBackListerner() {

                            @Override
                            public void needForcedInstall() {
                                // TODO Auto-generated method stub
                                AboutActivity.this.removeLoadingView();
                            }

                            @Override
                            public void needOptimizationInstall() {
                                // TODO Auto-generated method stub
                                AboutActivity.this.removeLoadingView();
                                CustomToast.show(AboutActivity.this, getString(R.string.find_new_version_down), 1);
                            }

                            @Override
                            public void noNeedInstall() {
                                // TODO Auto-generated method stub
                                AboutActivity.this.removeLoadingView();
                                CustomToast.show(AboutActivity.this, getString(R.string.now_is_latest_version), 1);
                            }

                            @Override
                            public void errorCondition(int error) {
                                // TODO Auto-generated method stub
                                AboutActivity.this.removeLoadingView();
                                CustomToast.show(AboutActivity.this, getString(R.string.now_is_latest_version)/*String.valueOf(error)*/, 1);
                            }

                        };

                        AboutActivity.this.showLoadingView(getString(R.string.checking_version), new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                dialog.dismiss();
                                MeetingVersionManager.getInstance().cancelCheckVersion();
                                CustomToast.show(AboutActivity.this, getString(R.string.cancel_check_version),Toast.LENGTH_LONG);
                            }
                        });

                        MeetingVersionManager.getInstance().checkOrInstall(AboutActivity.this.getApplicationContext(),
                                appVersionCheckListener);

                    } else {

                    }
                }
                break;
            case R.id.app_webview:
                Intent itent = new Intent();
                startActivity(itent);
                break;
        }

    }
}
