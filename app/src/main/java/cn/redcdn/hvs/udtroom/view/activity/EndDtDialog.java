package cn.redcdn.hvs.udtroom.view.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.log.CustomLog;

public class EndDtDialog extends BaseActivity {
    private LinearLayout twoBtnLayout;
    private LinearLayout oneBtnLayout;
    private TextView infoTextView;
    private Button centerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jmeetingsdk_custom_operate_dialog);
        twoBtnLayout = (LinearLayout) findViewById(R.id.two_button_layout);
        oneBtnLayout = (LinearLayout) findViewById(R.id.one_button_layout);
        infoTextView = (TextView) findViewById(R.id.qn_operate_dialog_body);
        centerBtn = (Button) findViewById(R.id.qn_operae_dialog_center_button);
        twoBtnLayout.setVisibility(View.GONE);
        oneBtnLayout.setVisibility(View.VISIBLE);
        infoTextView.setText(getString(R.string.response_person_has_finished_dt));
        centerBtn.setText(getString(R.string.iknow));
        centerBtn.setOnClickListener(mbtnHandleEventListener);
        centerBtn.setFocusable(true);
        centerBtn.requestFocus();
        setFinishOnTouchOutside(false);
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.qn_operae_dialog_center_button:
                CustomLog.d(TAG, "点击centerBtn，跳转到AppraiseDialog,EndDtDialog.this.finish();");
                Intent intent = new Intent(EndDtDialog.this,AppraiseDialog.class);
                intent.putExtra(AppraiseDialog.APPRAISEDIALOG_IS_REQUEST_FLAg,true);
                intent.putExtra(AppraiseDialog.APPRAISEDIALOG_DT_ID,getIntent().getStringExtra(AppraiseDialog.APPRAISEDIALOG_DT_ID));
                startActivity(intent);
                EndDtDialog.this.finish();
                break;
            default:
                break;
        }
    }
}
