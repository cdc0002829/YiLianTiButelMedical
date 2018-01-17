package cn.redcdn.hvs.accountoperate.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;

/**
 * Created by thinkpad on 2017/2/13.
 * 邮箱注册页面
 */
public class MailRegisterActivity extends cn.redcdn.hvs.base.BaseActivity {
    private Button register_back, register_next_btn, btn_declare;
    private EditText register_mail_edit, register_pwd_edit;
    private RadioButton rb_read;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_register);
        initView();
    }

    private void initView() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.email_register));
        titleBar.setTitleTextColor(Color.BLACK);
        titleBar.enableBack();

        register_back = (Button) findViewById(R.id.back_btn);
        register_next_btn = (Button) findViewById(R.id.register_next_btn);
        //返回手机注册页面
        register_back.setOnClickListener(mbtnHandleEventListener);
        //下一步
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
                Intent intent_phone = new Intent();
                intent_phone.setClass(MailRegisterActivity.this,PhoneRegisterActivity.class);
                startActivity(intent_phone);
                MailRegisterActivity.this.finish();
                break;
            case R.id.register_next_btn:
                Intent intent_code = new Intent();
                intent_code.setClass(MailRegisterActivity.this,RegisterCheckCodeActivity.class);
                startActivity(intent_code);
                MailRegisterActivity.this.finish();
                break;
        }
    }

    private void execNext() {
        if (register_pwd_edit.getText().length()<6){
            CustomToast.show(MailRegisterActivity.this,getString(R.string.pwd_wrong_type), Toast.LENGTH_SHORT);
            return;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent();
        i.setClass(MailRegisterActivity.this, PhoneRegisterActivity.class);
        startActivity(i);
    }
}
