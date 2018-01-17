package cn.redcdn.hvs.udtroom.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.redcdn.datacenter.hpucenter.HPUReview;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import com.example.xlhratingbar_lib.XLHRatingBar;
import org.json.JSONObject;

public class AppraiseDialog extends BaseActivity {
    public static final String APPRAISEDIALOG_IS_REQUEST_FLAg = "is_request_flag";
    public static final String APPRAISEDIALOG_DT_ID = "dt_ID";
    private XLHRatingBar appraiseRatingBar;
    private Button submitBtn;
    private Button waitBtn;
    private EditText evaluationDetailsText;
    private TextView appraiseServiceText;
    private boolean isRequest;
    private String dtId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appraise_dialog);
        initView();
        initData();
    }


    private void initView() {
        CustomLog.d(TAG, "initView()");
        appraiseRatingBar = (XLHRatingBar) findViewById(R.id.appraise_ratingBar);
        submitBtn = (Button) findViewById(R.id.appraise_dialog_right_button);
        submitBtn.setOnClickListener(mbtnHandleEventListener);
        waitBtn = (Button) findViewById(R.id.appraise_dialog_left_button);
        waitBtn.setOnClickListener(mbtnHandleEventListener);
        evaluationDetailsText = (EditText) findViewById(R.id.evaluation_details_text);
        appraiseServiceText = (TextView) findViewById(R.id.appraise_service_text);
        appraiseRatingBar.setOnRatingChangeListener(new XLHRatingBar.OnRatingChangeListener() {
            @Override
            public void onChange(int countSelected) {
                CustomLog.d(TAG, "选择星评" + countSelected);
            }
        });
    }


    private void initData() {
        CustomLog.d(TAG, "initData()");
        if (getIntent().getBooleanExtra(APPRAISEDIALOG_IS_REQUEST_FLAg, true)) {
            isRequest = true;
            appraiseServiceText.setText(getString(R.string.appraise_response_service));
        } else {
            isRequest = false;
            appraiseServiceText.setText(getString(R.string.appraise_request_service));
        }
        dtId = getIntent().getStringExtra(APPRAISEDIALOG_DT_ID);
        CustomLog.d(TAG, "dtId" + dtId);
    }


    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.appraise_dialog_right_button:
                CustomLog.d(TAG, "点击提交评价按钮");
                String appraiseAount = String.valueOf(appraiseRatingBar.getCountSelected());
                if (appraiseAount.equals("0")) {
                    CustomLog.d(TAG, "没有星评，不能提交");
                    CustomToast.show(AppraiseDialog.this, getString(R.string.choose_appraise),
                        CustomToast.LENGTH_LONG);
                } else {
                    String appraiseContent = evaluationDetailsText.getText().toString().trim();
                    final HPUReview hpuReview = new HPUReview() {
                        @Override
                        protected void onSuccess(JSONObject responseContent) {
                            super.onSuccess(responseContent);
                            CustomLog.d(TAG, "HPUReview||onSuccess");
                            CustomToast.show(AppraiseDialog.this,
                                getString(R.string.appraise_success), CustomToast.LENGTH_LONG);
                            removeLoadingView();

                            sendSubmitPriseBroadcast();

                            AppraiseDialog.this.finish();
                        }


                        @Override
                        protected void onFail(int statusCode, String statusInfo) {
                            super.onFail(statusCode, statusInfo);
                            removeLoadingView();
                            if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                                AccountManager.getInstance(AppraiseDialog.this)
                                    .tokenAuthFail(statusCode);
                            } else {
                                CustomToast.show(AppraiseDialog.this, statusInfo,
                                    CustomToast.LENGTH_LONG);
                            }
                            CustomLog.d(TAG,
                                "HPUReview||onFail  statusCode" + statusCode + "statusInfo" +
                                    statusInfo);
                        }
                    };
                    showLoadingView(getString(R.string.wait),
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                removeLoadingView();
                                CustomLog.d(TAG, "取消提交评价");
                                hpuReview.cancel();
                            }
                        }, true);
                    if (isRequest) {
                        CustomLog.d(TAG, "求诊者提交评价");
                        hpuReview.seekreview(
                            AccountManager.getInstance(AppraiseDialog.this).getMdsToken(), dtId,
                            appraiseAount, appraiseContent);
                    } else {
                        CustomLog.d(TAG, "接诊者提交评价");
                        hpuReview.cslreview(
                            AccountManager.getInstance(AppraiseDialog.this).getMdsToken(), dtId,
                            appraiseAount, appraiseContent);
                    }
                }

                break;
            case R.id.appraise_dialog_left_button:
                CustomLog.d(TAG, "点击稍后评价按钮");
                AppraiseDialog.this.finish();
                break;
            default:
                break;
        }
    }


    /**
     * 发送诊疗评价提交广播
     */
    private void sendSubmitPriseBroadcast() {
        Intent intent = new Intent();
        intent.setAction(UDTChatRoomActivity.SUBMIT_PARISE_BROADCAST);
        intent.putExtra(APPRAISEDIALOG_DT_ID, dtId);
        getApplicationContext().sendBroadcast(intent);
    }
}
