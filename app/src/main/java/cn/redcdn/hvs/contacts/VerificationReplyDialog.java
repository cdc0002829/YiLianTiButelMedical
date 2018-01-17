package cn.redcdn.hvs.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.ContactTransmitConfig;
import cn.redcdn.hvs.im.manager.FriendsManager;
import cn.redcdn.hvs.util.CustomToast;

/**
 * Created by Administrator on 2017/5/5 0005.
 */

public class VerificationReplyDialog extends BaseActivity {

    private Button btnCancel;
    private Button btnSend;
    private EditText etContent;
    private TextView titleTextView;
    public static final String KEY_DIALOG_TYPE = "KEY_DIALOG_TYPE"; // 0:回复消息 1:验证申请
    private int dlgType;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_verification_reply);
        initUI();
        initData();
        initListener();
    }

    private void initUI() {
        btnCancel = (Button) findViewById(R.id.tv_verification_dialog_left_button);
        btnSend = (Button) findViewById(R.id.tv_verification_dialog_right_button);
        etContent = (EditText) findViewById(R.id.et_verification_dialog_content);
        titleTextView = (TextView)findViewById(R.id.tv_verification_dialog_title);
    }

    private void initData() {
        dlgType = getIntent().getIntExtra(KEY_DIALOG_TYPE,0);
        if(dlgType == 0){
            titleTextView.setText(getString(R.string.revert));
        }else{
            titleTextView.setTextSize(getResources().getDimension(R.dimen.x10));
            btnCancel.setTextSize(getResources().getDimension(R.dimen.x12));
            btnSend.setTextSize(getResources().getDimension(R.dimen.x12));
            titleTextView.setText(getString(R.string.verification_title_string) + '\n' + '\n' + getString(R.string.you_need_send_verification_request) +
                    getString(R.string.other_through_been_friend));
            etContent.setHint(getString(R.string.input_verification_request));
            ViewGroup.LayoutParams p=etContent.getLayoutParams();
            p.height = getResources().getDisplayMetrics().heightPixels/10;
            etContent.setLayoutParams(p);
        }

    }

    private void initListener() {
        btnCancel.setOnClickListener(mbtnHandleEventListener);
        btnSend.setOnClickListener(mbtnHandleEventListener);
        etContent.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
                return (event.getKeyCode()==KeyEvent.KEYCODE_ENTER);
            }
        });
    }

    @Override
    public void todoClick(int id) {
        super.todoClick(id);
        switch (id) {
            case R.id.tv_verification_dialog_left_button:
                finish();
                break;
            case R.id.tv_verification_dialog_right_button:
                if (NetConnectHelper.getNetWorkType(VerificationReplyDialog.this) == NetConnectHelper.NETWORKTYPE_INVALID) {
                    CustomToast.show(VerificationReplyDialog.this, getString(R.string.net_error_check_internet),
                            Toast.LENGTH_LONG);
                }else if(null != getIntent().getExtras()
                        && null != getIntent().getExtras().getString("nubeNumber")
                        && FriendsManager.getInstance()
                        .getFriendRelationByNubeNumber(getIntent().getExtras().getString("nubeNumber"))
                        ==FriendsManager.RELATION_TYPE_BOTH){
                    CustomToast.show(VerificationReplyDialog.this, getString(R.string.have_been_good_friend),
                            Toast.LENGTH_LONG);
                    finish();
                }else{
                    String text = etContent.getText().toString().trim();
                    if(TextUtils.isEmpty(text)){
                        text=getString(R.string.request_add_friend);
                    }
                    Intent intent = new Intent();
                    intent.putExtra("reply",text);
                    setResult(ContactTransmitConfig.RESULT_REPLY_CODE, intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }

}
