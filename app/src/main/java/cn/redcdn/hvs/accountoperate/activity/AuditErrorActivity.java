package cn.redcdn.hvs.accountoperate.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.TitleBar;

import static cn.redcdn.hvs.accountoperate.activity.LoginActivity.UNAGREE_REASON;


public class AuditErrorActivity extends cn.redcdn.hvs.base.BaseActivity {
    private Button nextbtn;
    private TextView Review_NUM;
    private TextView reasonTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit_error);
        reasonTv = (TextView) findViewById(R.id.reason_tv);
        Intent intent = getIntent();
        if (intent != null) {
            String reason= intent.getStringExtra(UNAGREE_REASON);
            if (reason!=null&&!reason.equals("")){
                reasonTv.setText(reason);
            }
        }
        nextbtn = (Button) findViewById(R.id.auditerrorBtn2);
       nextbtn.setOnClickListener(mbtnHandleEventListener);
        Review_NUM = (TextView) findViewById(R.id.audit_REVIEW_NUM);
        Review_NUM.setOnClickListener(mbtnHandleEventListener);
        Review_NUM.setText(getString(R.string.certify_telephone)+SettingData.getInstance().REVIEW_NUM);
        TitleBar titleBar = getTitleBar();
        titleBar.setBack("", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountManager.getInstance(getApplicationContext()).logout();
                Intent intent=new Intent(AuditErrorActivity.this,LoginActivity.class);
                startActivity(intent);
                AuditErrorActivity.this.finish();
            }
        });
        titleBar.setTitle(getString(R.string.cetify));
        titleBar.setTitleTextColor(Color.BLACK);
    }



    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.audit_REVIEW_NUM:
                if (!SettingData.getInstance().REVIEW_NUM.isEmpty()) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(AuditErrorActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                            Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse(getString(R.string.tel)
                                    + SettingData.getInstance().REVIEW_NUM));
                            startActivity(i1);
                        }else{
                            //
                        }
                    }else{
                        Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse(getString(R.string.tel)
                                + SettingData.getInstance().REVIEW_NUM));
                        startActivity(i1);
                    }

                }
                break;
            case R.id.auditerrorBtn2:
                Intent intentbut = new Intent(AuditErrorActivity.this, CardTypeActivity.class);
                startActivity(intentbut);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AccountManager.getInstance(getApplicationContext()).logout();
        AuditErrorActivity.this.finish();
    }
}
