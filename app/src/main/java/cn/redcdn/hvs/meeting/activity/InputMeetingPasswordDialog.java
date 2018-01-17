package cn.redcdn.hvs.meeting.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.redcdn.datacenter.meetingmanage.CheckMeetingPwd;
import cn.redcdn.datacenter.meetingmanage.data.CheckMeetingPwdInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.im.activity.ChatActivity;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.log.CustomLog;
import cn.redcdn.meetinginforeport.InfoReportManager;
import cn.redcdn.meetinginforeport.InfoReportManager.JoinMeetingStyle;
import cn.redcdn.network.httprequest.HttpErrorCode;
import cn.redcdn.util.CustomToast;
import com.redcdn.keyeventwrite.KeyEventConfig;
import com.redcdn.keyeventwrite.KeyEventWrite;

public class InputMeetingPasswordDialog extends BaseActivity implements View.OnClickListener {
	
	private String tag = InputMeetingPasswordDialog.class.getName();
	private TextView cancelBtn;
	private TextView confirmBtn;
	private EditText contentEt;
	private String Pwd = null;
	
	private String accesstoken;
	private String nubeNumber;
	private String nickName;
	private String meetingId;
	private String nube;
	private String isInputID;
	private String groupid;
	private Boolean needGroupid;
	private TextView passwordTitle;
		
	@Override
	protected void onCreate(Bundle savedInstanceState){
	  CustomLog.d(tag, "InputMeetingPasswordDialog onCreate");	
	  super.onCreate(savedInstanceState);	
	  setContentView(R.layout.dialog_input_meeting_password);	
	  initWidget();
	}
	
	private void initWidget() {
		// TODO Auto-generated method stub
		accesstoken = getIntent().getStringExtra("accesstoken");
		nubeNumber = getIntent().getStringExtra("nubeNumber");
		nickName = getIntent().getStringExtra("nickName");
		meetingId = getIntent().getStringExtra("meetingId");
		groupid = getIntent().getStringExtra("groupId");
		needGroupid =getIntent().getBooleanExtra("needGroupId",false);
		nube = getIntent().getStringExtra("nube");
		isInputID = getIntent().getStringExtra("isInputID");
		
		passwordTitle = (TextView) findViewById(R.id.tv_input_meeting_password_title);
		
		  cancelBtn = (TextView) findViewById(R.id.input_meeting_password_dialog_left_button);	  
		  cancelBtn.setOnClickListener(this);
		  cancelBtn.setFocusable(true);
		  
		  confirmBtn = (TextView) findViewById(R.id.input_meeting_password_dialog_right_button);
		  confirmBtn.setOnClickListener(this);
		  confirmBtn.setFocusable(true);
		  confirmBtn.requestFocus();
		  
		  setFinishOnTouchOutside(false);
		  
		  contentEt = (EditText) findViewById(R.id.et_input_meeting_password_content); 
		  
			if(SettingData.runDevice== SettingData.RunDevice.TV){
				passwordTitle.setText(R.string.input_consultation_password);
				contentEt.setHint(R.string.consultation_password);
			}
			
		  
		  confirmBtn.setClickable(false);
		  
		  contentEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				
				if(contentEt.getText()!=null
				&&!contentEt.getText().toString().equalsIgnoreCase("")){
					confirmBtn.setBackgroundResource(R.drawable.custom_dialog_right_btn_selector);
					confirmBtn.setClickable(true);
				}else {
					confirmBtn.setClickable(false);
				}
				
			}

			  
		  });
		
	}

	@SuppressWarnings("unused")
	@SuppressLint("ShowToast") @Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {		
		case R.id.input_meeting_password_dialog_left_button:
			CustomLog.i(tag, "click cancel button");
			InputMeetingPasswordDialog.this.finish();
			break;
			
		case R.id.input_meeting_password_dialog_right_button:
			CustomLog.i(tag, "click confirm button");
			
			String password = contentEt.getText().toString();
			
			execCheckMeetingPassword(accesstoken,meetingId,password);
			
			break;
			
		default:
			
			break;
		
		}
	}
	
	
	private void execCheckMeetingPassword(String token,
			String id, String pwd) {
		CustomLog.v(tag, "execCheckMeetingPassword");
		final CheckMeetingPwd cmd = new CheckMeetingPwd(){
			
			@Override
			protected void onSuccess(CheckMeetingPwdInfo responseContent){
				super.onSuccess(responseContent);
				InputMeetingPasswordDialog.this.removeLoadingView();
				int rc =responseContent.rc;
				String rd =responseContent.rd;
				
				if(rc==0){
//					CustomToast.show(InputMeetingPasswordDialog.this,rd,Toast.LENGTH_LONG);
					
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);    
			        imm.hideSoftInputFromWindow(contentEt.getWindowToken(), 0) ;
					
					InputMeetingPasswordDialog.this.finish();
					if (MedicalMeetingManage.getInstance().getActiveMeetingId().equals(meetingId) || TextUtils.isEmpty(MedicalMeetingManage.getInstance().getActiveMeetingId())) {
						if (needGroupid) {
							joinMeeting(meetingId, groupid);
						} else {
							joinMeeing(meetingId);
						}
					}else {
						CustomToast.show(InputMeetingPasswordDialog.this,getString(R.string.is_video_meeting) + MedicalMeetingManage.getInstance().getActiveMeetingId(),CustomToast.LENGTH_SHORT);
					}
					// switchToMeetingRoomActivity(accesstoken,nubeNumber,nickName,Integer.parseInt(meetingId), nube);
					// setMeetingInfo(meetingId);
					
				}
			
			}	
			
//		@Override
//		protected void onSuccess(Map<String,Object>  map ){
//			super.onSuccess(map);
//			InputMeetingPasswordDialog.this.removeLoadingView();
//			int rc =(Integer) map.get("rc");
//			String rd =(String) map.get("rd");
//			
//			if(rc==0){
////				CustomToast.show(InputMeetingPasswordDialog.this,rd,Toast.LENGTH_LONG);
//				
//				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);    
//		        imm.hideSoftInputFromWindow(contentEt.getWindowToken(), 0) ;
//				
//				InputMeetingPasswordDialog.this.finish();
//				
//				switchToMeetingRoomActivity(accesstoken,nubeNumber,nickName,Integer.parseInt(meetingId), nube);
//				setMeetingInfo(meetingId);
//				
//			}
//		
//		}
			
		@Override 
		protected void onFail(int statusCode, String statusInfo){
			super.onFail(statusCode, statusInfo);
			InputMeetingPasswordDialog.this.removeLoadingView();

	        KeyEventWrite.write(KeyEventConfig.CHECK_MEETINGNUM_INVALID
	            + "_fail"
	            + "_"
	            + AccountManager.getInstance(
	                MedicalApplication.shareInstance().getApplicationContext())
	                .getAccountInfo().getNube() + "_" + statusCode);
	        CustomLog.v(tag, "VerifyMeetingNo onFail statusCode= " + statusCode);

	        if (statusCode == -906) {
	          CustomToast
	              .show(getApplicationContext(), getString(R.string.consultation_nub_error), Toast.LENGTH_SHORT);
	          return;
	        }
	         if (statusCode == -999) {
	         CustomToast.show(getApplicationContext(), getString(R.string.system_error),
	         Toast.LENGTH_SHORT);
	         return;
	         }
	         if (statusCode == -928) {
		         CustomToast.show(getApplicationContext(), getString(R.string.password_error),
		         Toast.LENGTH_SHORT);
		         return;
		         }
	        if (HttpErrorCode.checkNetworkError(statusCode)) {
	          CustomToast.show(InputMeetingPasswordDialog.this, getString(R.string.login_checkNetworkError),
	              Toast.LENGTH_LONG);
	          return;
	        }
	        if (statusCode == SettingData.getInstance().tokenUnExist
	            || statusCode == SettingData.getInstance().tokenInvalid) {
	          AccountManager.getInstance(getApplicationContext()).tokenAuthFail(
	              statusCode);
	        }
	        CustomToast.show(getApplicationContext(), getString(R.string.join_consultation_fail_code) + statusCode,
	            Toast.LENGTH_SHORT);
			
	        contentEt.setText("");
	        
		}
		
		};
		
		InputMeetingPasswordDialog.this.showLoadingView(getString(R.string.in_verification), new OnCancelListener() {

		      @Override
		      public void onCancel(DialogInterface dialog) {
		        dialog.dismiss();
		        cmd.cancel();
		        CustomToast.show(getApplicationContext(), getString(R.string.verification_cancel), Toast.LENGTH_SHORT);
		      }
		    });
		
		cmd.checkMeetingPwd(token, Integer.parseInt(id), pwd);
		
	}

	private void setMeetingInfo(String meetingid) {
	    if (isInputID.equals("true")) {
	      InfoReportManager.startMeeting(JoinMeetingStyle.InputId,
	          Integer.parseInt(meetingid));
	    } else if (isInputID.equals("false")) {
	      InfoReportManager.startMeeting(JoinMeetingStyle.FromList,
	          Integer.parseInt(meetingid));
	    } else if(isInputID.equals("null")){
	       InfoReportManager.startMeeting(JoinMeetingStyle.Invited, 
	    	  Integer.parseInt(meetingid));
	    }
	  }
	private void joinMeeing(String meetingId) {
		int i = MedicalMeetingManage.getInstance().joinMeeting(meetingId,
			new MedicalMeetingManage.OnJoinMeetingListener() {
				@Override
				public void onJoinMeeting(String valueDes, int valueCode) {
					// state = ConsultingRoomActivity.State.NONE;
					removeLoadingView();
				}
			});
		if (i == 0) {
			// state = ConsultingRoomActivity.State.JION;
			showLoadingView(getString(R.string.join_consultation));
		} else {
			cn.redcdn.hvs.util.CustomToast.show(InputMeetingPasswordDialog.this, getString(R.string.join_consultation_fail),
				cn.redcdn.hvs.util.CustomToast.LENGTH_SHORT);
		}
	}
	private void joinMeeting(String meetingId,String groupid) {

			int i = MedicalMeetingManage.getInstance().joinMeeting(meetingId, groupid,
					new MedicalMeetingManage.OnJoinMeetingListener() {
						@Override
						public void onJoinMeeting(String valueDes, int valueCode) {
							//TODO 加入会议的返回键处理
							removeLoadingView();
						}
					});
			if (i == 0) {
				showLoadingView(getString(R.string.join_consultation), new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						dialog.dismiss();
						MedicalMeetingManage.getInstance().cancelJoinMeeting("HomeActivity");
						cn.redcdn.hvs.util.CustomToast.show(getApplicationContext(), getString(R.string.cancel_get_consultation_info), Toast.LENGTH_SHORT);
					}
				});
			} else {
				cn.redcdn.hvs.util.CustomToast.show(InputMeetingPasswordDialog.this, getString(R.string.join_consultation_fail),
						cn.redcdn.hvs.util.CustomToast.LENGTH_SHORT);
			}

	}

}
