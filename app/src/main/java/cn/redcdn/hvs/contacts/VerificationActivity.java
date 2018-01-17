package cn.redcdn.hvs.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.util.CustomToast;

/**
 * Created by Administrator on 2017/5/3 0003.
 */

public class VerificationActivity extends BaseActivity {

    private Button btnBack;
    private Button btnSend;
    private EditText etContent;
    private Button btnDelete;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        initUI();
        initData();
        initListener();
    }

    private void initUI() {
        btnBack = (Button) findViewById(R.id.verification_back);
        btnSend = (Button) findViewById(R.id.verification_send);
        etContent = (EditText) findViewById(R.id.verification_content);
        btnDelete = (Button) findViewById(R.id.verification_delete);
    }

    private void initData() {
        if(null != getIntent().getExtras() && null != getIntent().getExtras().getString("groupName")) {
            groupName = getIntent().getExtras().getString("groupName");
            etContent.setText(getString(R.string.i_am_group_chat)+groupName+getString(R.string.the)+ AccountManager.getInstance(this).getName());
        }else{
            etContent.setText(getString(R.string.i_am)+ AccountManager.getInstance(this).getName());
        }

        etContent.setSelection(etContent.getText().length());

    }

    private void initListener() {
        btnBack.setOnClickListener(mbtnHandleEventListener);
        btnSend.setOnClickListener(mbtnHandleEventListener);
        btnDelete.setOnClickListener(mbtnHandleEventListener);
        etContent.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                return (event.getKeyCode()==KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    @Override
    public void todoClick(int id){
        super.todoClick(id);
        switch(id){
            case R.id.verification_back:
                finish();
                break;
            case R.id.verification_send:
                if (NetConnectHelper.getNetWorkType(VerificationActivity.this) == NetConnectHelper.NETWORKTYPE_INVALID) {
                    CustomToast.show(VerificationActivity.this, getString(R.string.net_error_check_internet),
                            Toast.LENGTH_LONG);
                }else if(null != getIntent().getExtras()
                        && null != getIntent().getExtras().getString("nubeNumber")
                        && FriendsManager.getInstance()
                        .getFriendRelationByNubeNumber(getIntent().getExtras().getString("nubeNumber"))
                        ==FriendsManager.RELATION_TYPE_BOTH){
                    CustomToast.show(VerificationActivity.this, getString(R.string.have_been_good_friend),
                            Toast.LENGTH_LONG);
                    finish();
                }else{
                    String text = etContent.getText().toString().trim();
                    if(TextUtils.isEmpty(text)){
                        text=getString(R.string.request_add_friend);
                    }
                    CustomToast.show(this,R.string.toast_sent,Toast.LENGTH_LONG);

                    if(null != getIntent().getExtras() && null != getIntent().getExtras().getString("retry")){
                        Intent intent = new Intent();
                        intent.putExtra("reply",text);
                        setResult(ContactTransmitConfig.RESULT_REPLY_CODE, intent);
                        finish();
                    }else{
                        Intent intent = new Intent();
                        intent.putExtra("message",text);
                        setResult(ContactTransmitConfig.RESULT_VERIFICATION_CODE, intent);
                        finish();
                    }
                }
                break;
            case R.id.verification_delete:
                etContent.setText("");
                break;
            default:
                break;
        }
    }




}
