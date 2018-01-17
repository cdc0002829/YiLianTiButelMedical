package cn.redcdn.hvs.accountoperate.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.TitleBar;

public class AuditingActivity extends cn.redcdn.hvs.base.BaseActivity{
    private TextView Review_NUM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditing);
        init();

    }

    private void init(){
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.cetify));
        titleBar.setTitleTextColor(Color.BLACK);
        Review_NUM= (TextView) findViewById(R.id.auding_tv);
        Review_NUM.setOnClickListener(mbtnHandleEventListener);
        Review_NUM.setText(SettingData.getInstance().REVIEW_NUM);
       titleBar.setBack("", new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               AccountManager.getInstance(getApplicationContext()).logout();
               Intent intent=new Intent(AuditingActivity.this,LoginActivity.class);
               startActivity(intent);
               AuditingActivity.this.finish();
           }
       });
    }


    @Override
    public void todoClick(int i) {
        switch (i) {
            case R.id.auding_tv:
                if (!SettingData.getInstance().REVIEW_NUM.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(AuditingActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                            Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                    + SettingData.getInstance().REVIEW_NUM));
                            startActivity(i1);
                        }else{
                        }
                    }else{
                        Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + SettingData.getInstance().REVIEW_NUM));
                        startActivity(i1);
                    }

                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AccountManager.getInstance(getApplicationContext()).logout();
        Intent intent = new Intent(AuditingActivity.this, LoginActivity.class);
        startActivity(intent);
        AuditingActivity.this.finish();
    }
}
