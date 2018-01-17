package cn.redcdn.hvs.accountoperate.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

import cn.redcdn.commonutil.NetConnectHelper;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.datacenter.meetingmanage.data.ResponseEmpty;
import cn.redcdn.datacenter.usercenter.ActivateAccount;
import cn.redcdn.datacenter.usercenter.ReSendActivateCode;
import cn.redcdn.datacenter.usercenter.data.UserInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;


public class RegisterCheckCodeActivity extends BaseActivity {
	private Button checkCodeBackBtn = null;
	private Button checkCodeBtn = null;
	private TextView checkCodeTimeTV = null;
	private EditText checkCodeEdit = null;
	private Button registerFinishBtn = null;
	private String nubeNumber = null;
	private String account = null;
	private String tag = RegisterCheckCodeActivity.class.getCanonicalName();
	private TimeCount tc = null;
	private String pwd = null;
	private ReSendActivateCode rac;
	private ActivateAccount aa;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CustomLog.d(TAG, "onCreate:" + this.toString());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registercheckcode);
		account = getIntent().getStringExtra("account");
		nubeNumber = getIntent().getStringExtra("nubenumber");
		pwd = getIntent().getStringExtra("pwd");
		initWidget();
		initStatus();
	}

	private void initWidget() {
		checkCodeBackBtn = (Button) this
				.findViewById(R.id.registercheckcode_back);
		checkCodeBtn = (Button) this.findViewById(R.id.checkcode_btn);
		checkCodeTimeTV = (TextView) this.findViewById(R.id.checkcodetime_tv);
		checkCodeEdit = (EditText) this.findViewById(R.id.checkcode_edit);

		checkCodeEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if ((checkCodeEdit.getText() != null && !checkCodeEdit
						.getText().toString().equalsIgnoreCase(""))) {
					registerFinishBtn
							.setBackgroundResource(R.drawable.button_selector);
					registerFinishBtn.setClickable(true);
				} else {
					registerFinishBtn.setClickable(false);
					registerFinishBtn
							.setBackgroundResource(R.drawable.button_btn_notclick);
				}

			}
		});
		registerFinishBtn = (Button) this
				.findViewById(R.id.register_finish_btn);
		registerFinishBtn.setClickable(false);
		registerFinishBtn.setBackgroundResource(R.drawable.button_btn_notclick);

		checkCodeBackBtn.setOnClickListener(mbtnHandleEventListener);
		registerFinishBtn.setOnClickListener(mbtnHandleEventListener);
		checkCodeBtn.setOnClickListener(mbtnHandleEventListener);
	}


	@Override
	public void todoClick(int i) {
		// TODO Auto-generated method stub
		super.todoClick(i);
		switch (i) {
			case R.id.registercheckcode_back:
				if (checkCodeEdit.getText() != null) {
					final CustomDialog cd=new CustomDialog(RegisterCheckCodeActivity.this);
					cd.setTip(getString(R.string.config_quit_login));
					cd.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {

						@Override
						public void onClick(CustomDialog customDialog) {

							Intent i = new Intent();
							i.setClass(RegisterCheckCodeActivity.this,
									LoginActivity.class);
							startActivity(i);

							RegisterCheckCodeActivity.this.finish();
						}
					});
					cd.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {

						@Override
						public void onClick(CustomDialog customDialog) {
							cd.dismiss();
						}
					});
					cd.show();
					return;
				}
				RegisterCheckCodeActivity.this.finish();
				break;
			case R.id.register_finish_btn:
				if(((checkCodeEdit.getText() != null && !checkCodeEdit
						.getText().toString().equalsIgnoreCase(""))))
					accountActivate();
				break;
			case R.id.checkcode_btn:
				reSendActivateCode();
				break;

		}
	}

	private void reSendActivateCode() {
		RegisterCheckCodeActivity.this.showLoadingView(getString(R.string.getting),
				new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						if (rac != null)
							rac.cancel();

					}
				});
		rac = new ReSendActivateCode() {

			@Override
			protected void onSuccess(ResponseEmpty responseContent) {
				KeyEventWrite.write(KeyEventConfig.SEND_CHECKCODE
						+ "_ok" + "_"
						+  AccountManager.getInstance(
						MedicalApplication.shareInstance().getApplicationContext())
						.getAccountInfo().nube);
				CustomLog.v(tag,
						"ReSendActivateCode onSuccess responseContent= "
								+ responseContent);
				RegisterCheckCodeActivity.this.removeLoadingView();
				checkCodeBtn.setClickable(false);
				checkCodeBtn.setTextColor(Color.parseColor("#c8cacc"));
				checkCodeTimeTV.setVisibility(View.VISIBLE);
				tc = new TimeCount(60000, 1000);
				tc.start();
			}

			@Override
			protected void onFail(int statusCode, String statusInfo) {
				KeyEventWrite.write(KeyEventConfig.SEND_CHECKCODE + "_fail" + "_"
						+ AccountManager.getInstance(
						MedicalApplication.shareInstance().getApplicationContext())
						.getAccountInfo().nube + "_"
						+ statusCode);
				CustomLog.v(tag, "ReSendActivateCode onFail statusCode="
						+ statusCode);
				RegisterCheckCodeActivity.this.removeLoadingView();
				if (HttpErrorCode.checkNetworkError(statusCode)) {
					CustomToast.show(RegisterCheckCodeActivity.this,
							getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
					return;
				}
				if (NetConnectHelper.getNetWorkType(MedicalApplication.getContext()) == NetConnectHelper.NETWORKTYPE_INVALID) {
					CustomToast.show(RegisterCheckCodeActivity.this,
							getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
					return;
				}
				if (statusCode == -452) {
					CustomToast.show(RegisterCheckCodeActivity.this,
							getString(R.string.most_account_register),
							Toast.LENGTH_LONG);
					return;
				}
				if (statusCode == -401) {
					CustomToast.show(RegisterCheckCodeActivity.this,
							getString(R.string.account_activated_fail), Toast.LENGTH_LONG);
					return;
				}
				if (statusCode == -404) {
					CustomToast.show(RegisterCheckCodeActivity.this,
							getString(R.string.activated_code_wrong), Toast.LENGTH_LONG);
					return;
				} else {
					CustomToast.show(RegisterCheckCodeActivity.this, getString(R.string.activated_code_fail)+"="
							+ statusCode, Toast.LENGTH_LONG);
				}


			}
		};
		rac.reSendActivateCode(account, SettingData.AUTH_PRODUCT_ID,ReSendActivateCode.ProductType_HVS,"","http://www.baidu.com");
	}

	private void accountActivate() {
		String checkCode = checkCodeEdit.getText().toString();
		if (checkCode != null && !checkCode.equalsIgnoreCase("")) {
			if (checkCode.length() < 6)
				CustomToast.show(RegisterCheckCodeActivity.this, getString(R.string.six_code),
						Toast.LENGTH_LONG);
			else {
				RegisterCheckCodeActivity.this.showLoadingView(getString(R.string.testing),
						new OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								if (aa != null)
									aa.cancel();
							}
						});
				aa = new ActivateAccount() {

					@Override
					protected void onSuccess(UserInfo responseContent) {
						KeyEventWrite.write(KeyEventConfig.ACTIVE_INFO
								+ "_ok" + "_"
								+  AccountManager.getInstance(
								MedicalApplication.shareInstance().getApplicationContext())
								.getAccountInfo().nube);
						RegisterCheckCodeActivity.this.removeLoadingView();
						CustomLog.e(tag, "ActivateAccount  onSuccess ");
						if (tc != null)
							tc.cancel();

						CustomToast.show(RegisterCheckCodeActivity.this,
								getString(R.string.register_success), Toast.LENGTH_LONG);
						RegisterCheckCodeActivity.this.showLoadingView(getString(R.string.auto_logining),
								new OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										AccountManager.getInstance(
												MedicalApplication
														.shareInstance())
												.cancelLogin();
										Intent in = new Intent();
										in.setClass(
												RegisterCheckCodeActivity.this,
												LoginActivity.class);
										startActivity(in);
										RegisterCheckCodeActivity.this.finish();

									}
								});
						AccountManager.getInstance(
								MedicalApplication.shareInstance())
								.registerLoginCallback(new AccountManager.LoginListener() {

									@Override
									public void onLoginFailed(int errorCode,
															  String msg) {
										KeyEventWrite.write(KeyEventConfig.LOGIN_INFO + "_fail" + "_"
												+ AccountManager.getInstance(
												MedicalApplication.shareInstance().getApplicationContext())
												.getAccountInfo().nube + "_"
												+ errorCode);
										if (errorCode==MDSErrorCode.MDS_NUBE_NOT_EXIST){
											Intent in = new Intent();
											in.setClass(
													RegisterCheckCodeActivity.this,
													CardTypeActivity.class);
											startActivity(in);
											RegisterCheckCodeActivity.this.finish();
										}else {
											CustomToast.show(RegisterCheckCodeActivity.this,getString(R.string.auto_login_fail),CustomToast.LENGTH_LONG);
											Intent intentLogin = new Intent();
											intentLogin.setClass(
													RegisterCheckCodeActivity.this,
													LoginActivity.class);
											startActivity(intentLogin);
											RegisterCheckCodeActivity.this.finish();
										}

									}

									@Override
									public void onLoginSuccess(MDSAccountInfo account) {
										RegisterCheckCodeActivity.this
												.removeLoadingView();
										KeyEventWrite.write(KeyEventConfig.LOGIN_INFO
												+ "_ok" + "_"
												+  AccountManager.getInstance(
												MedicalApplication.shareInstance().getApplicationContext())
												.getAccountInfo().nube);


									}
								});

						AccountManager.getInstance(
								MedicalApplication.shareInstance()).login(
								account, pwd);

					}

					@Override
					protected void onFail(int statusCode, String statusInfo) {
						KeyEventWrite.write(KeyEventConfig.ACTIVE_INFO + "_fail" + "_"
								+ AccountManager.getInstance(
								MedicalApplication.shareInstance().getApplicationContext())
								.getAccountInfo().nube + "_"
								+ statusCode);
						RegisterCheckCodeActivity.this.removeLoadingView();
						CustomLog.e(tag, "ActivateAccount  onFail statusCode="
								+ statusCode);
						if (HttpErrorCode.checkNetworkError(statusCode)) {
							CustomToast.show(RegisterCheckCodeActivity.this,
									getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
							return;
						}
						if (NetConnectHelper.getNetWorkType(MedicalApplication.getContext()) == NetConnectHelper.NETWORKTYPE_INVALID) {
							CustomToast.show(RegisterCheckCodeActivity.this,
									getString(R.string.login_checkNetworkError), Toast.LENGTH_LONG);
							return;
						}
						if (statusCode == -452) {
							CustomToast.show(RegisterCheckCodeActivity.this,
									getString(R.string.most_account_register),
									Toast.LENGTH_LONG);
							return;
						}
						if (statusCode == -401) {
							CustomToast.show(RegisterCheckCodeActivity.this,
									getString(R.string.account_activated_fail), Toast.LENGTH_LONG);
							return;
						}
						if (statusCode == -404) {
							CustomToast.show(RegisterCheckCodeActivity.this,
									getString(R.string.activated_code_wrong), Toast.LENGTH_LONG);
							return;
						} else {
							CustomToast.show(RegisterCheckCodeActivity.this,
									getString(R.string.account_activated_fail)+"=" + statusCode, Toast.LENGTH_LONG);
						}

					}
				};
				aa.activateAccount(account, checkCode,
						SettingData.AUTH_PRODUCT_ID);
			}
		} else {
			CustomToast.show(RegisterCheckCodeActivity.this, getString(R.string.code_not_empty),
					Toast.LENGTH_LONG);
		}
	}

	private void initStatus() {
		checkCodeBtn.setClickable(false);
		checkCodeBtn.setTextColor(Color.parseColor("#c8cacc"));
		tc = new TimeCount(60000, 1000);
		tc.start();

	}

	class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {

			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
			CustomLog.v(tag, "计时器");
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			checkCodeBtn.setClickable(true);
			checkCodeBtn.setTextColor(Color.parseColor("#35b7c6"));
			checkCodeTimeTV.setVisibility(View.GONE);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			checkCodeTimeTV.setText("(" + millisUntilFinished / 1000 + ")");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		RegisterCheckCodeActivity.this.removeLoadingView();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = new Intent();
		i.setClass(RegisterCheckCodeActivity.this, LoginActivity.class);
		startActivity(i);
		this.finish();
	}

}
