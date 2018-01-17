package cn.redcdn.hvs.officialaccounts.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;
import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.VIDEO_PUBLISH_ACTIVITY;

/**
 * Created by ${chenghb} on 2017/3/4.
 */

public class DialogActivity extends Activity implements View.OnClickListener {
    private static final String TAG = DialogActivity.class.getName();
    private EditText pwd;
    private Button btn_toVideoPublic;
    SharedPreferences sp;
    private String nube;
    private String articleId;//文章ID
    private String EncipherPwd;
    private String passWord;
    private String nubeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        pwd = (EditText) findViewById(R.id.pwd);

        btn_toVideoPublic = (Button) findViewById(R.id.btn_toVideoPublic);
        btn_toVideoPublic.setOnClickListener(this);
        btn_toVideoPublic.setClickable(false);
        Intent intent = getIntent();
        articleId = intent.getStringExtra(INTENT_DATA_ARTICLE_ID);
        EncipherPwd = intent.getStringExtra("accessPassword");
        CustomLog.e(TAG, "EncipherPwd" + EncipherPwd);
        pwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (pwd.getText() != null && !pwd.getText().toString().equalsIgnoreCase("")) {
                    btn_toVideoPublic.setClickable(true);
                } else {
                    btn_toVideoPublic.setClickable(false);
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        passWord = pwd.getText().toString();
        CustomLog.e(TAG, "passWord" + passWord);
        switch (view.getId()) {
            case R.id.btn_toVideoPublic:
                if (passWord.equalsIgnoreCase(EncipherPwd)) {
                    //做密码逻辑处理
                    Intent intent = new Intent(DialogActivity.this, VideoPublishActivity.class);
                    finish();
                    SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_APPEND);
                    //获取nubeId
                    nubeId = AccountManager.getInstance(this).getNube();
                    String key = nubeId + "_" + articleId ;//nube+文章id 做key值,密码做value值
                    CustomLog.e(TAG, "key" + key);
                    SharedPreferences.Editor editor = getSharedPreferences("data", MODE_APPEND).edit();
                    editor.putString(key, passWord);
                    editor.apply();
                } else {
                    CustomToast.show(getApplicationContext(), "密码输入错误", 1);
                }

                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MedicalApplication.destoryActivity(VideoPublishActivity.VIDEO_PUBLISH_ACTIVITY);
            MedicalApplication.destoryActivity(VideoPublishActivity.ARTICLE_ACTIVITY);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
