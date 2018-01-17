package cn.redcdn.hvs.requesttreatment;

/**
 * Created by Administrator on 2017/11/18 0018.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.redcdn.datacenter.hpucenter.HPUCreateUionDT;
import cn.redcdn.datacenter.hpucenter.HPUGetDTlist;
import cn.redcdn.datacenter.hpucenter.HPUGetDepartments;
import cn.redcdn.datacenter.hpucenter.HPUGetInstitutions;
import cn.redcdn.datacenter.hpucenter.HPUGetSchedulBydate;
import cn.redcdn.datacenter.hpucenter.HPUGetlisrcards;
import cn.redcdn.datacenter.hpucenter.data.CertypeInfo;
import cn.redcdn.datacenter.hpucenter.data.CurInfo;
import cn.redcdn.datacenter.hpucenter.data.DTInfo;
import cn.redcdn.datacenter.hpucenter.data.DepartmentInfo;
import cn.redcdn.datacenter.hpucenter.data.InstitutionInfo;
import cn.redcdn.datacenter.hpucenter.data.SchedulInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.profiles.view.SlideSwitch;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.hvs.util.Validator;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.hpucenter.data.HPUCommonCode.SENDSMS_FLAG_NO;
import static cn.redcdn.datacenter.hpucenter.data.HPUCommonCode.SENDSMS_FLAG_YES;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_ACCOUNT_NOT_EXIST;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_SEARCHTYPE_OUT_RANGE;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.R.id.rl_id_type;

public class ReserveDTActivity extends BaseActivity {

    private Context mContext;
    private TitleBar titleBar;
    private TextView testTextView;
    private AlertDialog dialog;
    private ListView arrangeListView;
    private RelativeLayout hpsLayout;
    private RelativeLayout hospitalLayout;
    private RelativeLayout departmentLayout;
    private RelativeLayout timeLayout;
    private RelativeLayout idTypeLayout;
    private TextView titleView;
    private RelativeLayout topLayout;
    private TextView topView;
    private TextView reserveView;
    private RelativeLayout topCancelView;
    private TextView certypeView;
    private RelativeLayout titleLayout;
    private TextView hpsContentView;
    private TextView hospitalContentView;
    private TextView departmentContentView;
    private TextView timeContentView;
    private RelativeLayout decreaseDateView;
    private RelativeLayout increaseDateView;
    private EditText nameEditText;
    private EditText idEditText;
    private EditText phoneEditText;
    private EditText guardianNameEditText;
    private EditText guardianIdEditText;
    private EditText guardianPhoneEditText;
    private String patientName;
    private String patientId;
    private String patientPhone;
    private String guardianName;
    private String guardianId;
    private String guardianPhone;
    private int Year;
    private int Month;
    private int Day;
    private int Week;
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private int currentWeek;
    private List<DTInfo> dtInfoList = new ArrayList<DTInfo>();
    private List<InstitutionInfo> institutionInfoList = new ArrayList<InstitutionInfo>();
    private List<DepartmentInfo> departmentInfoList = new ArrayList<DepartmentInfo>();
    private List<SchedulInfo> scheduleInfoList = new ArrayList<SchedulInfo>();
    private List<CertypeInfo> certypeInfoList = new ArrayList<CertypeInfo>();
    private CurInfo curInfo = new CurInfo();
    private final int MSG_FIRST_DT = 1111;
    private final int MSG_INSTITUTION_LIST = 2222;
    private final int MSG_DEPARTMENT_LIST = 3333;
    private final int MSG_SCHEDULE_LIST = 4444;
    private final int MSG_CERTYPE_LIST = 5555;
    private final int MSG_CURINFO = 6666;
    private final int TOUCH_ITEM = 7777;
    private final int MSG_DT_LIST = 8888;
    private String mSelectedDtId;
    private String mSelectedInstitutionId;
    private String mSelectedDepartmentId;
    private String mSelectedScheduleId;
    private String mSelectedScheduleTime;
    private String mSelectedCerType;
    private String mTempScheduleId;
    private String mTempScheduleTime;
    private String mTempScheduleInfo;
    private int mPosition = -1;

    private TextView pickerTitleTextView;
    private ListView pickerscrlllview; // 滚动选择器
    private TextView pickerCancelView;
    private String mPickContent;

    private SlideSwitch sendMessageSwitch;
    private boolean sendMessageStatus = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_FIRST_DT:
                    String firstDT = (String)msg.obj;
                    hpsContentView.setText(firstDT);
                    hpsContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                    break;
                case MSG_INSTITUTION_LIST:
                    List<InstitutionInfo> list1 = (List<InstitutionInfo>)msg.obj;
                    showPickerDialog(null,list1,null,null,"hospital");
                    break;
                case MSG_DEPARTMENT_LIST:
                    List<DepartmentInfo> list2 = (List<DepartmentInfo>)msg.obj;
                    showPickerDialog(null,null,list2,null,"department");
                    break;
                case MSG_SCHEDULE_LIST:
                    if((boolean)msg.obj){
                        showArrangementDialog(true);
                    }else{
                        showArrangementDialog(false);
                    }
                    break;
                case MSG_CERTYPE_LIST:
                    List<CertypeInfo> list4 = (List<CertypeInfo>)msg.obj;
                    showPickerDialog(null,null,null,list4,"certype");
                    break;
                case MSG_CURINFO:
                    CurInfo info = (CurInfo)msg.obj;
                    NewCurInfo newCurInfo = new NewCurInfo();
                    newCurInfo.setCurNum(info.getCurNum());
                    newCurInfo.setId(info.getId());
                    newCurInfo.setSchedulDate(info.getSchedulDate());
                    newCurInfo.setName(patientName);
                    newCurInfo.setCardType(mSelectedCerType);
                    newCurInfo.setCard(patientId);
                    newCurInfo.setMobile(patientPhone);
                    newCurInfo.setTime(mSelectedScheduleTime);

                    if(sendMessageStatus){
                        CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_message_is_sent),Toast.LENGTH_LONG);
                    }

                    Intent intent = new Intent();
                    intent.setClass(ReserveDTActivity.this,PatientConditionActivity.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putSerializable("newCurInfo",newCurInfo);
                    intent.putExtras(mBundle);
                    startActivity(intent);
                    finish();

                case TOUCH_ITEM:
                    showArrangementDialog(false);
                    break;

                case MSG_DT_LIST:
                    List<DTInfo> list5 = (List<DTInfo>)msg.obj;
                    showPickerDialog(list5,null,null,null,"hps");
                   break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_treatment);
        initWidget();
    }

    private void initWidget() {
        mContext = this;
        initTitleBar();
        hpsLayout = (RelativeLayout) findViewById(R.id.rl_hps);
        hospitalLayout = (RelativeLayout) findViewById(R.id.rl_hospital);
        departmentLayout = (RelativeLayout) findViewById(R.id.rl_department);
        timeLayout = (RelativeLayout) findViewById(R.id.rl_time);
        testTextView = (TextView) findViewById(R.id.tv_test);
        idTypeLayout = (RelativeLayout) findViewById(rl_id_type);
        certypeView = (TextView) findViewById(R.id.tv_id);
        hpsContentView = (TextView) findViewById(R.id.tv_hps_content);
        hospitalContentView = (TextView) findViewById(R.id.tv_hospital_content);
        departmentContentView = (TextView) findViewById(R.id.tv_department_content);
        timeContentView = (TextView) findViewById(R.id.tv_time_content);
        nameEditText = (EditText) findViewById(R.id.et_name);
        nameEditText.setFilters(new InputFilter[]{inputFilter,new InputFilter.LengthFilter(5)});
        idEditText = (EditText) findViewById(R.id.et_id);
        idEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        idEditText.setFilters(new InputFilter[]{idInputFilter,new InputFilter.LengthFilter(18)});
        phoneEditText = (EditText) findViewById(R.id.et_phone);
        guardianNameEditText = (EditText) findViewById(R.id.et_guardian);
        guardianNameEditText.setFilters(new InputFilter[]{inputFilter,new InputFilter.LengthFilter(5)});
        guardianIdEditText = (EditText) findViewById(R.id.et_guardian_id);
        guardianIdEditText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        guardianIdEditText.setFilters(new InputFilter[]{idInputFilter,new InputFilter.LengthFilter(18)});
        guardianPhoneEditText = (EditText) findViewById(R.id.et_guardian_phone);
        sendMessageSwitch = (SlideSwitch) findViewById(R.id.switch_send_message);
        sendMessageSwitch.setChecked(true);
        sendMessageSwitch.SetOnChangedListener(new SlideSwitch.OnChangedListener() {
            @Override
            public void OnChanged(boolean checkState) {
                if(sendMessageStatus){
                    sendMessageSwitch.setChecked(false);
                    sendMessageStatus = false;
                }else{
                    sendMessageSwitch.setChecked(true);
                    sendMessageStatus = true;
                }
            }
        });

        this.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        hpsLayout.setOnClickListener(mbtnHandleEventListener);
        hospitalLayout.setOnClickListener(mbtnHandleEventListener);
        departmentLayout.setOnClickListener(mbtnHandleEventListener);
        timeLayout.setOnClickListener(mbtnHandleEventListener);
        testTextView.setOnClickListener(mbtnHandleEventListener);
        idTypeLayout.setOnClickListener(mbtnHandleEventListener);

        long mill = System.currentTimeMillis();
        String createTimeStr = new java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss ").format(new java.util.Date(mill));
        java.util.Calendar c = getCalendar(createTimeStr);

        Year = c.get(java.util.Calendar.YEAR);
        Month = c.get(java.util.Calendar.MONTH) + 1;
        Day = c.get(java.util.Calendar.DAY_OF_MONTH);
        Week = c.get(Calendar.DAY_OF_WEEK);
        currentYear = Year;
        currentMonth = Month;
        currentDay = Day;
        currentWeek = Week;

        certypeView.setText(mContext.getString(R.string.reserve_treatment_id));
        mSelectedCerType = "1";
        getDTListFromLocal();
    }

    public static boolean containsLetter(String str) {
        String regex = ".*[a-zA-z].*";
        boolean isLetter =str.matches(regex);
        return isLetter;
    }

    InputFilter inputFilter=new InputFilter() {

        Pattern pattern = Pattern.compile("[^a-zA-Z\\u4E00-\\u9FA5_]");
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            Matcher matcher=  pattern.matcher(charSequence);
            if(!matcher.find()){
                return null;
            }else{
                CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_chinese_character_pinyin_only),CustomToast.LENGTH_LONG);
                return "";
            }

        }
    };

    InputFilter idInputFilter=new InputFilter() {

        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            Matcher matcher=  pattern.matcher(charSequence);
            if(!matcher.find()){
                return null;
            }else{
                CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_number_character_only),CustomToast.LENGTH_LONG);
                return "";
            }

        }
    };

    private void getDTListFromLocal(){
        final HPUGetDTlist gdt = new HPUGetDTlist(){
            @Override
            protected void onSuccess(List<DTInfo> responseContent){
                super.onSuccess(responseContent);
                dtInfoList = responseContent;
                if(dtInfoList!=null&&dtInfoList.size()>0&&dtInfoList.get(0).getId()!=null){
                    mSelectedDtId = dtInfoList.get(0).getId();
                }
                if(dtInfoList!=null&&dtInfoList.size()>0&&dtInfoList.get(0).getName()!=null){
                    Message msg = new Message();
                    msg.what = MSG_FIRST_DT;
                    msg.obj = dtInfoList.get(0).getName();
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
            }
        };
        gdt.getdtlist(AccountManager.getInstance(mContext).getMdsToken());
    }


    private void getDTList(){
        final HPUGetDTlist gdt = new HPUGetDTlist(){
            @Override
            protected void onSuccess(List<DTInfo> responseContent){
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG,"HPUGetDTlist,onSuccess");
                CustomLog.d(TAG,"responseContent.size():"+String.valueOf(responseContent.size()));
                dtInfoList = responseContent;

                if(responseContent!=null&&responseContent.size()>0){
                    Message msg = new Message();
                    msg.what = MSG_DT_LIST;
                    msg.obj = dtInfoList;
                    mHandler.sendMessage(msg);
                }else{
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_no_hps),Toast.LENGTH_LONG);
                }

            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG,"HPUGetDTlist,onFail,statusCode:"+String.valueOf(statusCode)
                +" statusInfo"+statusInfo);
                removeLoadingView();
                if(statusCode==MDS_TOKEN_DISABLE){
                    AccountManager.getInstance(ReserveDTActivity.this).tokenAuthFail(statusCode);
                }else{
                    CustomToast.show(ReserveDTActivity.this,
                        mContext.getString(R.string.reserve_treatment_get_data_failed),Toast.LENGTH_LONG);
                }
            }
        };
        gdt.getdtlist(AccountManager.getInstance(mContext).getMdsToken());
        showLoadingView(mContext.getString(R.string.reserve_treatment_loading),
        new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeLoadingView();
                gdt.cancel();
                CustomToast.show(getApplicationContext(),
                    getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.rl_hps:
                getDTList();
                break;
            case R.id.rl_hospital:
                if(mSelectedDtId!=null){
                    getHospitalList();
                }else{
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_hpu),Toast.LENGTH_LONG);
                }
                break;
            case R.id.rl_department:
                if(mSelectedInstitutionId!=null){
                    getDepartmentList();
                }else{
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_institution),Toast.LENGTH_LONG);
                }
                break;
            case R.id.rl_time:
                mTempScheduleId = null;
                mTempScheduleTime = null;
                mSelectedScheduleId = null;
                mSelectedScheduleTime = null;
                timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                if(mSelectedDepartmentId==null){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_department),Toast.LENGTH_LONG);
                }else{
                    currentYear = Year;
                    currentMonth = Month;
                    currentDay = Day;
                    currentWeek = Week;
                    getScheduleByDate(true);
                }
                break;
            case rl_id_type:
                getCertype();
                break;
            case R.id.tv_test:
                patientName = nameEditText.getText().toString();
                patientId = idEditText.getText().toString();
                patientPhone = phoneEditText.getText().toString();
                guardianName = guardianNameEditText.getText().toString();
                guardianId = guardianIdEditText.getText().toString();
                guardianPhone = guardianPhoneEditText.getText().toString();

                if(TextUtils.isEmpty(mSelectedDtId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_hpu),Toast.LENGTH_LONG);
                }else if(TextUtils.isEmpty(mSelectedInstitutionId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_institution),Toast.LENGTH_LONG);
                }else if(TextUtils.isEmpty(mSelectedDepartmentId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_department),Toast.LENGTH_LONG);
                }else if(TextUtils.isEmpty(mSelectedScheduleId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_reserve_time),Toast.LENGTH_LONG);
                }/*else if(TextUtils.isEmpty(mSelectedCerType)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_certype),Toast.LENGTH_LONG);
                }*/else if(TextUtils.isEmpty(patientName)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_patient_name),Toast.LENGTH_LONG);
                }else if(!TextUtils.isEmpty(patientName)&&containsLetter(patientName)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_name_letter_only),Toast.LENGTH_LONG);
                }/*else if(TextUtils.isEmpty(patientId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_patient_id),Toast.LENGTH_LONG);
                }*/else if((mSelectedCerType!=null&&mSelectedCerType.equals("1"))
                    &&!TextUtils.isEmpty(patientId)&&!Validator.isIDCard(patientId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_patient_id_format_invalid),Toast.LENGTH_LONG);
                }else if((mSelectedCerType!=null&&mSelectedCerType.equals("2"))
                    &&TextUtils.isEmpty(guardianName)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_guardian_name),Toast.LENGTH_LONG);
                }else if(!TextUtils.isEmpty(guardianName)&&containsLetter(guardianName)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_name_letter_only),Toast.LENGTH_LONG);
                }else if((mSelectedCerType!=null&&mSelectedCerType.equals("2"))
                    &&TextUtils.isEmpty(guardianId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_guardian_id),Toast.LENGTH_LONG);
                }else if((!TextUtils.isEmpty(guardianId)||!TextUtils.isEmpty(guardianPhone))
                    &&TextUtils.isEmpty(guardianName)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_please_select_guardian_name),Toast.LENGTH_LONG);
                }else if(!TextUtils.isEmpty(guardianId)&&!Validator.isIDCard(guardianId)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_guardian_id_format_invalid),Toast.LENGTH_LONG);
                }else if(!TextUtils.isEmpty(patientPhone)&&!isPhoneNumber(patientPhone)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_patient_phone_format_invalid),Toast.LENGTH_LONG);
                }else if(!TextUtils.isEmpty(guardianPhone)&&!isPhoneNumber(guardianPhone)){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_guardian_phone_format_invalid),Toast.LENGTH_LONG);
                }else if(TextUtils.isEmpty(patientPhone)&&sendMessageStatus){
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_no_patient_phone),Toast.LENGTH_LONG);
                }
                else if(mSelectedDepartmentId!=null&&mSelectedScheduleId!=null) {
                    if(TextUtils.isEmpty(guardianName)) {
                        guardianName = "";
                    }
                    if(TextUtils.isEmpty(guardianId)) {
                        guardianId = "";
                    }
                    if(TextUtils.isEmpty(guardianPhone)) {
                        guardianPhone = "";
                    }
                        createDT();
                }
                break;
            default:
                break;
        }
    }


    private boolean isPhoneNumber(String phoneNumber) {
        boolean isValid = false;
        CharSequence inputStr = phoneNumber;
        String expression = "1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }


    private void getHospitalList(){
        final HPUGetInstitutions gi = new HPUGetInstitutions(){
            @Override
            protected void onSuccess(List<InstitutionInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG,"HPUGetInstitutions,onSuccess");
                CustomLog.d(TAG,"responseContent.size():"+String.valueOf(responseContent.size()));
                institutionInfoList = responseContent;
                if(responseContent!=null&&responseContent.size()>0){
                    Message msg = new Message();
                    msg.what = MSG_INSTITUTION_LIST;
                    msg.obj = responseContent;
                    mHandler.sendMessage(msg);
                }else{
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_no_hospital),Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG,"HPUGetInstitutions,onFail,statusCode:"+String.valueOf(statusCode)
                    +" statusInfo"+statusInfo);
                removeLoadingView();
                if(statusCode==MDS_TOKEN_DISABLE){
                    AccountManager.getInstance(ReserveDTActivity.this).tokenAuthFail(statusCode);
                }else{
                    CustomToast.show(ReserveDTActivity.this,
                        mContext.getString(R.string.reserve_treatment_get_data_failed),Toast.LENGTH_LONG);
                }
            }
        };
        gi.getinstitution(AccountManager.getInstance(mContext).getMdsToken(),mSelectedDtId);
        showLoadingView(mContext.getString(R.string.reserve_treatment_loading),
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    gi.cancel();
                    CustomToast.show(getApplicationContext(),
                        getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                }
            });
    }


    private void getDepartmentList(){
        final HPUGetDepartments gd = new HPUGetDepartments(){
            @Override
            protected void onSuccess(List<DepartmentInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG,"HPUGetInstitutions,onSuccess");
                CustomLog.d(TAG,"responseContent.size():"+String.valueOf(responseContent.size()));
                departmentInfoList = responseContent;
                if(responseContent!=null&&responseContent.size()>0){
                    Message msg = new Message();
                    msg.what = MSG_DEPARTMENT_LIST;
                    msg.obj = responseContent;
                    mHandler.sendMessage(msg);
                }else{
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_no_department),Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG,"HPUGetDepartments,onFail,statusCode:"+String.valueOf(statusCode)
                    +" statusInfo"+statusInfo);
                removeLoadingView();
                if(statusCode==MDS_TOKEN_DISABLE){
                    AccountManager.getInstance(ReserveDTActivity.this).tokenAuthFail(statusCode);
                }else{
                    CustomToast.show(ReserveDTActivity.this,
                        mContext.getString(R.string.reserve_treatment_get_data_failed),Toast.LENGTH_LONG);
                }
            }
        };
        gd.getdepartments(AccountManager.getInstance(mContext).getMdsToken(),mSelectedInstitutionId);
        showLoadingView(mContext.getString(R.string.reserve_treatment_loading),
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    gd.cancel();
                    CustomToast.show(getApplicationContext(),
                        getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                }
            });
    }


    private void getScheduleByDate(final boolean showDialog){
        final HPUGetSchedulBydate gsbd = new HPUGetSchedulBydate(){
            @Override
            protected void onSuccess(List<SchedulInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG,"HPUGetInstitutions,onSuccess");
                CustomLog.d(TAG,"responseContent.size():"+String.valueOf(responseContent.size()));
                scheduleInfoList = responseContent;
                if(responseContent!=null&&responseContent.size()>0){
                    Message msg = new Message();
                    msg.what = MSG_SCHEDULE_LIST;
                    msg.obj = showDialog;
                    mHandler.sendMessage(msg);
                }else{
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_no_arrangement),Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG,"HPUGetSchedulBydate,onFail,statusCode:"+String.valueOf(statusCode)
                    +" statusInfo"+statusInfo);
                removeLoadingView();
                if(statusCode==MDS_TOKEN_DISABLE){
                    AccountManager.getInstance(ReserveDTActivity.this).tokenAuthFail(statusCode);
                }else{
                    CustomToast.show(ReserveDTActivity.this,
                        mContext.getString(R.string.reserve_treatment_get_data_failed),Toast.LENGTH_LONG);
                }
            }
        };

        mPosition = -1;
        String year="0";
        String month="0";
        String day="0";
        year = String.valueOf(currentYear);

        if(currentMonth<10){
            month = "0"+String.valueOf(currentMonth);
        }else{
            month = String.valueOf(currentMonth);
        }
        if(currentDay<10){
            day = "0"+String.valueOf(currentDay);
        }else{
            day =  String.valueOf(currentDay);
        }

        String date = year+month+day;
        CustomLog.d(TAG,"date:"+date);
        gsbd.getschedulBydate(AccountManager.getInstance(mContext).getMdsToken(),mSelectedDepartmentId,date);
        showLoadingView(mContext.getString(R.string.reserve_treatment_loading),
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    gsbd.cancel();
                    CustomToast.show(getApplicationContext(),
                        getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                }
            });
    }


    private void getCertype(){
        final HPUGetlisrcards glc = new HPUGetlisrcards(){
            @Override
            protected void onSuccess(List<CertypeInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "HPUGetlisrcards,onSuccess");
                CustomLog.d(TAG, "responseContent.size():" + String.valueOf(responseContent.size()));
                certypeInfoList = responseContent;
                if(responseContent!=null&&responseContent.size()>0){
                    Message msg = new Message();
                    msg.what = MSG_CERTYPE_LIST;
                    msg.obj = responseContent;
                    mHandler.sendMessage(msg);
                }else{
                    CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_no_certype),Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG,"HPUGetlisrcards,onFail,statusCode:"+String.valueOf(statusCode)
                    +" statusInfo"+statusInfo);
                removeLoadingView();
                if(statusCode==MDS_TOKEN_DISABLE){
                    AccountManager.getInstance(ReserveDTActivity.this).tokenAuthFail(statusCode);
                }else{
                    CustomToast.show(ReserveDTActivity.this,
                        mContext.getString(R.string.reserve_treatment_get_data_failed),Toast.LENGTH_LONG);
                }
            }
        };
        glc.getlistcards();
        showLoadingView(mContext.getString(R.string.reserve_treatment_loading),
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    glc.cancel();
                    CustomToast.show(getApplicationContext(),
                        getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                }
            });
    }


    private void createDT(){
        final HPUCreateUionDT cdt = new HPUCreateUionDT(){
            @Override
            protected void onSuccess(CurInfo responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "HPUCreateUionDT,onSuccess");
                curInfo = responseContent;

                Message msg = new Message();
                msg.what = MSG_CURINFO;
                msg.obj = responseContent;
                mHandler.sendMessage(msg);

            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG,"HPUCreateUionDT,onFail,statusCode:"+String.valueOf(statusCode)
                    +" statusInfo"+statusInfo);
                removeLoadingView();
                if(statusCode==MDS_TOKEN_DISABLE){
                    AccountManager.getInstance(ReserveDTActivity.this).tokenAuthFail(statusCode);
                }else if(statusCode==MDS_SEARCHTYPE_OUT_RANGE){
                    final CustomDialog dtCancelDialog = new CustomDialog(mContext);
                    dtCancelDialog.removeCancelBtn();
                    dtCancelDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                        @Override
                        public void onClick(CustomDialog customDialog) {
                            CustomLog.d(TAG, "MDS_SEARCHTYPE_OUT_RANGE onClick");
                            dtCancelDialog.dismiss();
                        }
                    });
                    dtCancelDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                    dtCancelDialog.setTip(mContext.getString(R.string.reserve_treatment_uesd_up));
                    dtCancelDialog.show();
                }else if(statusCode==MDS_ACCOUNT_NOT_EXIST){
                    final CustomDialog dtCancelDialog = new CustomDialog(mContext);
                    dtCancelDialog.removeCancelBtn();
                    dtCancelDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                        @Override
                        public void onClick(CustomDialog customDialog) {
                            CustomLog.d(TAG, "MDS_ACCOUNT_NOT_EXIST onClick");
                            dtCancelDialog.dismiss();
                        }
                    });
                    dtCancelDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                    dtCancelDialog.setTip(mContext.getString(R.string.reserve_treatment_arrangement_changed));
                    dtCancelDialog.show();
                }
                else{
                    CustomToast.show(ReserveDTActivity.this,statusInfo,Toast.LENGTH_LONG);
                }
            }
        };

        int sendMessage;

        if(sendMessageStatus){
            sendMessage = SENDSMS_FLAG_YES;
        }else{
            sendMessage = SENDSMS_FLAG_NO;
        }
        CustomLog.d(TAG,"sendMessage:"+String.valueOf(sendMessage));
        cdt.createUionDT(AccountManager.getInstance(mContext).getMdsToken(),
            mSelectedDepartmentId,mSelectedScheduleId,
            patientName,mSelectedCerType,
            patientId,patientPhone,
            guardianName,guardianId,guardianPhone,mSelectedInstitutionId,sendMessage);
        showLoadingView(mContext.getString(R.string.reserve_treatment_loading),
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    removeLoadingView();
                    cdt.cancel();
                    CustomToast.show(getApplicationContext(),
                        getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                }
            });

    }

    private void initTitleBar() {
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(mContext.getString(R.string.reserve_treatment_reserve_dt));
    }

    private void setDialogHeight(int size, int count) {

        switch (size) {
            case 0:
                break;
            case 1:
                if (count>0){
                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(1, 1);
                    lp1.width = ListView.LayoutParams.MATCH_PARENT;
                    lp1.height = (getResources().getDimensionPixelOffset(R.dimen.y70)+getResources().getDimensionPixelOffset(R.dimen.y37)*count);
                    pickerscrlllview.setLayoutParams(lp1);
                }else {
                    LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(1, 1);
                    lp1.width = ListView.LayoutParams.MATCH_PARENT;
                    lp1.height = getResources().getDimensionPixelOffset(R.dimen.y70);
                    pickerscrlllview.setLayoutParams(lp1);
                }
                break;
            case 2:
                if (count>0){
                    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(1, 1);
                    lp2.width = ListView.LayoutParams.MATCH_PARENT;
                    lp2.height = (getResources().getDimensionPixelOffset(R.dimen.y140)+getResources().getDimensionPixelOffset(R.dimen.y37)*count);
                    pickerscrlllview.setLayoutParams(lp2);
                }else {
                    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(1, 1);
                    lp2.width = ListView.LayoutParams.MATCH_PARENT;
                    lp2.height = getResources().getDimensionPixelOffset(R.dimen.y140);
                    pickerscrlllview.setLayoutParams(lp2);
                }
                break;
            case 3:
                if (count>0){
                    LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(1, 1);
                    lp3.width = ListView.LayoutParams.MATCH_PARENT;
                    lp3.height = (getResources().getDimensionPixelOffset(R.dimen.y210)+getResources().getDimensionPixelOffset(R.dimen.y37)*count);
                    pickerscrlllview.setLayoutParams(lp3);
                }else{
                    LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(1, 1);
                    lp3.width = ListView.LayoutParams.MATCH_PARENT;
                    lp3.height = getResources().getDimensionPixelOffset(R.dimen.y210);
                    pickerscrlllview.setLayoutParams(lp3);
                }
                break;
            case 4:
                if (count>0){
                    LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams(1, 1);
                    lp4.width = ListView.LayoutParams.MATCH_PARENT;
                    lp4.height = (getResources().getDimensionPixelOffset(R.dimen.y280)+getResources().getDimensionPixelOffset(R.dimen.y37)*count);
                    pickerscrlllview.setLayoutParams(lp4);
                }else {
                    LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams(1, 1);
                    lp4.width = ListView.LayoutParams.MATCH_PARENT;
                    lp4.height = getResources().getDimensionPixelOffset(R.dimen.y280);
                    pickerscrlllview.setLayoutParams(lp4);
                }
                break;
            case 5:
                if (count>0){
                    LinearLayout.LayoutParams lp5 = new LinearLayout.LayoutParams(1, 1);
                    lp5.width = ListView.LayoutParams.MATCH_PARENT;
                    lp5.height = (getResources().getDimensionPixelOffset(R.dimen.y350)+getResources().getDimensionPixelOffset(R.dimen.y37)*count);
                    pickerscrlllview.setLayoutParams(lp5);
                }else {
                    LinearLayout.LayoutParams lp5 = new LinearLayout.LayoutParams(1, 1);
                    lp5.width = ListView.LayoutParams.MATCH_PARENT;
                    lp5.height = getResources().getDimensionPixelOffset(R.dimen.y350);
                    pickerscrlllview.setLayoutParams(lp5);
                }

                break;
            default:
                LinearLayout.LayoutParams lp6 = new LinearLayout.LayoutParams(1, 1);
                lp6.width = ListView.LayoutParams.MATCH_PARENT;
                lp6.height = getResources().getDimensionPixelOffset(R.dimen.y350);
                pickerscrlllview.setLayoutParams(lp6);
                break;
        }

    }

    private void showPickerDialog(final List<DTInfo> list1,
                                  final List<InstitutionInfo> list2,
                                  final List<DepartmentInfo> list3,
                                  final List<CertypeInfo> list4,final String type) {
        dialog = new AlertDialog.Builder(this).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_arrangement_diagram_picker);
        pickerTitleTextView = (TextView) window.findViewById(R.id.tv_arrangement_diagram_picker_title);
        pickerscrlllview = (ListView) window.findViewById(R.id.request_pickerscrlllview);
        pickerCancelView = (TextView) window.findViewById(R.id.iv_request_picker_cancel);
        mPickContent = null;

        if(type.equals("hps")){
            int count=0;
            if (list1!=null&&list1.size()>0){
                for (int i = 0; i < list1.size(); i++) {
                    String name = list1.get(i).getName();
                    if (name.length()>18){
                        count+=1;
                    }
                }
            }
            pickerTitleTextView.setText(mContext.getString(R.string.reserve_treatment_select_hpu));
            setDialogHeight(list1.size(),count);
        }else if(type.equals("hospital")){
            int count=0;
            if (list2!=null&&list2.size()>0){
                for (int i = 0; i < list2.size(); i++) {
                    String name = list2.get(i).getName();
                    if (name.length()>18){
                        count+=1;
                    }
                }
            }
            pickerTitleTextView.setText(mContext.getString(R.string.reserve_treatment_select_hospital));
            setDialogHeight(list2.size(),count);
        }else if(type.equals("department")){
            int count=0;
            if (list3!=null&&list3.size()>0){
                for (int i = 0; i < list3.size(); i++) {
                    String name = list3.get(i).getName();
                    if (name.length()>18){
                        count+=1;
                    }
                }
            }
            pickerTitleTextView.setText(mContext.getString(R.string.reserve_treatment_select_department));
            setDialogHeight(list3.size(),count);
        }else if(type.equals("certype")){
            pickerTitleTextView.setText(mContext.getString(R.string.reserve_treatment_selecet_certype));
            setDialogHeight(list4.size(),0);
        }

        DialogAdatper adapter = new DialogAdatper(ReserveDTActivity.this,list1,list2,list3,list4,type);
        pickerscrlllview.setAdapter(adapter);

        pickerscrlllview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(type.equals("hps")){
                    if(mSelectedDtId==null||!mSelectedDtId.equals(list1.get(position).getId())){
                        mSelectedInstitutionId = null;
                        hospitalContentView.setText(mContext.getString(R.string.reserve_treatment_select_hospital));
                        hospitalContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                        mSelectedDepartmentId = null;
                        departmentContentView.setText(mContext.getString(R.string.reserve_treatment_select_department));
                        departmentContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                        mTempScheduleId = null;
                        mTempScheduleTime = null;
                        mSelectedScheduleId = null;
                        mSelectedScheduleTime = null;
                        timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                        timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                    }
                    mPickContent = list1.get(position).getName();
                    mSelectedDtId = list1.get(position).getId();
                    hpsContentView.setText(mPickContent);
                    hpsContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }else if (type.equals("hospital")){
                    if(mSelectedInstitutionId==null||!mSelectedInstitutionId.equals(list2.get(position).getId())){
                        mSelectedDepartmentId = null;
                        departmentContentView.setText(mContext.getString(R.string.reserve_treatment_select_department));
                        departmentContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                        mTempScheduleId = null;
                        mTempScheduleTime = null;
                        mSelectedScheduleId = null;
                        mSelectedScheduleTime = null;
                        timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                        timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                    }
                    mPickContent = list2.get(position).getName();
                    mSelectedInstitutionId = list2.get(position).getId();
                    hospitalContentView.setText(mPickContent);
                    hospitalContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }else if (type.equals("department")){
                    if(mSelectedDepartmentId==null||!mSelectedDepartmentId.equals(list3.get(position).getId())){
                        mTempScheduleId = null;
                        mTempScheduleTime = null;
                        mSelectedScheduleId = null;
                        mSelectedScheduleTime = null;
                        timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                        timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                    }
                    mPickContent = list3.get(position).getName();
                    mSelectedDepartmentId = list3.get(position).getId();
                    departmentContentView.setText(mPickContent);
                    departmentContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }else if (type.equals("certype")){
                    mPickContent = list4.get(position).getCerName();
                    mSelectedCerType = list4.get(position).getCerType();
                    certypeView.setText(mPickContent);
                }
                dialog.dismiss();
            }
        });

        pickerCancelView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);// 使除了dialog以外的地方不能被点击
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override public void onCancel(DialogInterface dialog) {
            }
        });
    }

    private void showArrangementDialog(boolean show){

        if(show){
            dialog = new AlertDialog.Builder(this).create();
            dialog.show();
            Window window = dialog.getWindow();
            window.setContentView(R.layout.dialog_arrangement_diagram);
            arrangeListView = (ListView) window.findViewById(R.id.lv_arrangement);
            titleView = (TextView) window.findViewById(R.id.tv_title);
            topLayout = (RelativeLayout) window.findViewById(R.id.rl_top);
            topView = (TextView) window.findViewById(R.id.tv_top);
            reserveView = (TextView) window.findViewById(R.id.tv_reserve);
            topCancelView = (RelativeLayout) window.findViewById(R.id.rl_top_cancel);
            titleLayout = (RelativeLayout) window.findViewById(R.id.rl_title);
            decreaseDateView = (RelativeLayout) window.findViewById(R.id.rl_decrease_date);
            increaseDateView = (RelativeLayout) window.findViewById(R.id.rl_increase_date);

            ArrangementAdapter adapter = new ArrangementAdapter(ReserveDTActivity.this,scheduleInfoList);
            arrangeListView.setAdapter(adapter);

            decreaseDateView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {

                    if(currentYear==Year&&currentMonth==Month&&currentDay==Day){
                        CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_can_not_select_before_today),Toast.LENGTH_LONG);

                    }else{
                        if(currentDay<=1){
                            if(currentMonth<=1){
                                currentYear--;
                                currentMonth=12;
                                currentDay=31;
                            }else{
                                currentMonth--;
                                if(currentMonth==1||currentMonth==3||currentMonth==5||currentMonth==7
                                    ||currentMonth==8||currentMonth==10||currentMonth==12){
                                    currentDay=31;
                                }else if(currentMonth==3||currentMonth==6||currentMonth==9||currentMonth==11){
                                    currentDay=30;
                                }else if(currentMonth==2){
                                    if(currentYear%4==0){
                                        currentDay=29;
                                    }else if(currentYear%4!=0){
                                        currentDay=28;
                                    }
                                }
                            }
                        }else{
                            currentDay--;
                        }
                        if(currentWeek==1){
                            currentWeek=7;
                        }else{
                            currentWeek--;
                        }
                        titleView.setText(
                            getWeek(currentWeek)+" "
                                + String.valueOf(currentMonth)
                                + mContext.getString(R.string.reserve_treatment_month)
                                + String.valueOf(currentDay)
                                + mContext.getString(R.string.reserve_treatment_day));

                        getScheduleByDate(false);
                    }

                }

            });
            increaseDateView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if(((currentDay==29&&currentMonth==2&&(currentYear%4==0))
                        ||(currentDay==28&&currentMonth==2&&(currentYear%4!=0)))
                        ||(currentDay==30&&(currentMonth==4||currentMonth==6
                        ||currentMonth==9||currentMonth==11))
                        ||(currentDay>=31&&(currentMonth==1|currentMonth==3||currentMonth==5
                        ||currentMonth==7||currentMonth==8||currentMonth==10||currentMonth==12))){
                        if(currentMonth>=12){
                            currentYear++;
                            currentMonth=1;
                            currentDay=1;
                        }else{
                            currentMonth++;
                            currentDay=1;
                        }
                    }else{
                        currentDay++;
                    }
                    if(currentWeek==7){
                        currentWeek=1;
                    }else{
                        currentWeek++;
                    }
                    titleView.setText(
                        getWeek(currentWeek)+" "
                            + String.valueOf(currentMonth)
                            + mContext.getString(R.string.reserve_treatment_month)
                            + String.valueOf(currentDay)
                            + mContext.getString(R.string.reserve_treatment_day));

                    getScheduleByDate(false);

                }
            });
            arrangeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mTempScheduleInfo = getWeek(currentWeek)+" "
                                + String.valueOf(currentMonth)
                                + mContext.getString(R.string.reserve_treatment_month)
                                + String.valueOf(currentDay)
                                + mContext.getString(R.string.reserve_treatment_day)+" "
                            +scheduleInfoList.get(position).getRange();
                        if(!scheduleInfoList.get(position).getEnableFLg().equals("2")){
                            CustomToast.show(mContext,mContext.getString(R.string.reserve_treatment_time_not_avaiable),Toast.LENGTH_LONG);
                        }else{
                            mPosition = position;
                            Message msg = new Message();
                            msg.what = TOUCH_ITEM;
                            mHandler.sendMessage(msg);
                            mTempScheduleId = scheduleInfoList.get(position).getSchedulId();
                            mTempScheduleTime = scheduleInfoList.get(position).getRange();
                        }
                }
    });
            dialog.setCanceledOnTouchOutside(true);// 使除了dialog以外的地方不能被点击
            topCancelView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            reserveView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    mSelectedScheduleId = mTempScheduleId;
                    mSelectedScheduleTime =  mTempScheduleTime;
                    if(mSelectedScheduleId==null){
                        timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                        timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                    }else {
                        timeContentView.setText(mTempScheduleInfo);
                        timeContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                    }
                    dialog.dismiss();
                }
            });

                topLayout.setVisibility(View.VISIBLE);
                topView.setText(mContext.getString(R.string.reserve_treatment_schedule_diagram));
                titleView.setText(
                    getWeek(currentWeek)+" "
                        + String.valueOf(currentMonth)
                        + mContext.getString(R.string.reserve_treatment_month)
                        + String.valueOf(currentDay)
                        + mContext.getString(R.string.reserve_treatment_day));
                reserveView.setVisibility(View.VISIBLE);
                titleLayout.setVisibility(View.VISIBLE);

        }else{
            ArrangementAdapter adapter = new ArrangementAdapter(ReserveDTActivity.this,scheduleInfoList);
            arrangeListView.setAdapter(adapter);
        }

    }

    private String getWeek(int week){
        String Week = "";
        switch(week){
            case 1:
                Week = mContext.getString(R.string.reserve_treatment_sunday);
                break;
            case 2:
                Week = mContext.getString(R.string.reserve_treatment_monday);
                break;
            case 3:
                Week = mContext.getString(R.string.reserve_treatment_tuesday);
                break;
            case 4:
                Week = mContext.getString(R.string.reserve_treatment_wednesday);
                break;
            case 5:
                Week = mContext.getString(R.string.reserve_treatment_thursday);
                break;
            case 6:
                Week = mContext.getString(R.string.reserve_treatment_friday);
                break;
            case 7:
                Week = mContext.getString(R.string.reserve_treatment_saturday);
                break;
        }
        return Week;
    }

    public java.util.Calendar getCalendar(String datestr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Calendar calendar = GregorianCalendar.getInstance();
        try {
            calendar.setTime(sdf.parse(datestr));
        } catch (ParseException e) {
            CustomLog.e(TAG, "getCalendar  " + e.toString());
        }
        return calendar;
    }


    private class DialogAdatper extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<DTInfo> mList1;
        private List<InstitutionInfo> mList2;
        private List<DepartmentInfo> mList3;
        private List<CertypeInfo> mList4;
        private String mType;

        public DialogAdatper(Context context,
                                  List<DTInfo> list1,
                                  List<InstitutionInfo> list2,
                                  List<DepartmentInfo> list3,
                                  List<CertypeInfo> list4,
                                  String type) {
            mInflater = LayoutInflater.from(context);
            mList1 = list1;
            mList2 = list2;
            mList3 = list3;
            mList4 = list4;
            mType = type;
        }
        @Override
        public int getCount() {
            if(mType.equals("hps")){
                return mList1.size();
            }else if(mType.equals("hospital")){
                return mList2.size();
            }else if(mType.equals("department")){
                return mList3.size();
            }else if(mType.equals("certype")){
                return mList4.size();
            } else {
                return 0;
            }
        }
        @Override
        public Object getItem(int arg0) {
            return null;
        }
        @Override
        public long getItemId(int arg0) {
            return 0;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.reserve_dialog_item, null);
                holder.dialogText = (TextView) convertView.findViewById(R.id.tv_reserve_dialog_text);
                holder.mView = (ImageView) convertView.findViewById(R.id.mView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if(mType.equals("hps")){
                holder.dialogText.setText(mList1.get(position).getName());
                if (mList1!=null&&mList1.size()>0){
                    if (position==mList1.size()-1){
                        holder.mView.setVisibility(View.GONE);
                    }else {
                        holder.mView.setVisibility(View.VISIBLE);
                    }
                }
            }else if(mType.equals("hospital")){
                holder.dialogText.setText(mList2.get(position).getName());
                if (mList2!=null&&mList2.size()>0){
                    if (position==mList2.size()-1){
                        holder.mView.setVisibility(View.GONE);
                    }else {
                        holder.mView.setVisibility(View.VISIBLE);
                    }
                }
            }else if(mType.equals("department")){
                holder.dialogText.setText(mList3.get(position).getName());
                if (mList3!=null&&mList3.size()>0){
                    if (position==mList3.size()-1){
                        holder.mView.setVisibility(View.GONE);
                    }else {
                        holder.mView.setVisibility(View.VISIBLE);
                    }
                }
            }else if(mType.equals("certype")){
                holder.dialogText.setText(mList4.get(position).getCerName());
                if (mList4!=null&&mList4.size()>0){
                    if (position==mList4.size()-1){
                        holder.mView.setVisibility(View.GONE);
                    }else {
                        holder.mView.setVisibility(View.VISIBLE);
                    }
                }
            }

            return convertView;
        }
        class ViewHolder {
            TextView dialogText;
            ImageView mView;
        }
    }

    private class ArrangementAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<SchedulInfo> mScheduleList;

        public ArrangementAdapter(Context context,List<SchedulInfo> list) {
            mInflater = LayoutInflater.from(context);
            mScheduleList = list;
        }
        @Override
        public int getCount() {
            return mScheduleList.size();
        }
        @Override
        public Object getItem(int arg0) {
            return null;
        }
        @Override
        public long getItemId(int arg0) {
            return 0;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = new ViewHolder();;
            if (null == convertView) {
                    convertView = mInflater.inflate(R.layout.arrangement_item, null);
                    holder.rlItem = (RelativeLayout) convertView.findViewById(R.id.rl_arrangement_item);
                    holder.tvTime = (TextView)convertView.findViewById(R.id.tv_time_time);
                    holder.tvNumber = (TextView)convertView.findViewById(R.id.tv_time_number);
                    holder.tvCurNumber = (TextView)convertView.findViewById(R.id.tv_time_curnumber);
                    holder.tvName = (TextView)convertView.findViewById(R.id.tv_time_name);
                    holder.vLine = convertView.findViewById(R.id.v_line);
                    holder.ivSelectedDot = (ImageView) convertView.findViewById(R.id.iv_selected_dot);
                    convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

                holder.tvTime.setVisibility(View.VISIBLE);
                holder.tvNumber.setVisibility(View.VISIBLE);
                holder.tvCurNumber.setVisibility(View.VISIBLE);
                holder.tvName.setVisibility(View.VISIBLE);
                holder.vLine.setVisibility(View.VISIBLE);
                holder.tvTime.setText(mScheduleList.get(position).getRange());
                holder.tvNumber.setText("/"+mScheduleList.get(position).getNum());
                if(!mScheduleList.get(position).getCurNum().equals(mScheduleList.get(position).getNum())){
                    holder.tvCurNumber.setText(mContext.getString(R.string.reserve_treatment_arranged)+" " + mScheduleList.get(position).getCurNum());
                }else{
                    if(mScheduleList.get(position).getNum().equals("0")){
                        holder.tvCurNumber.setText(mContext.getString(R.string.reserve_treatment_not_arranged)+" " + mScheduleList.get(position).getCurNum());
                    }else{
                        holder.tvCurNumber.setText(mContext.getString(R.string.reserve_treatment_used_up)+" " + mScheduleList.get(position).getCurNum());
                    }

                }

                String name = mScheduleList.get(position).getUserNames().replace(","," ");
                holder.tvName.setText(name);

                if(mPosition==position){
                    holder.ivSelectedDot.setVisibility(View.VISIBLE);
                    if(!mScheduleList.get(position).getEnableFLg().equals("2")){
                        holder.tvTime.setTextColor(getResources().getColor(R.color.gray_five));
                        holder.tvNumber.setTextColor(getResources().getColor(R.color.gray_five));
                        holder.tvCurNumber.setTextColor(getResources().getColor(R.color.gray_five));
                        holder.tvName.setTextColor(getResources().getColor(R.color.gray_five));
                    }else{
                        holder.tvTime.setTextColor(getResources().getColor(R.color.btn_color_blue));
                        holder.tvCurNumber.setTextColor(getResources().getColor(R.color.btn_color_blue));
                        holder.tvNumber.setTextColor(getResources().getColor(R.color.btn_color_blue));
                        holder.tvName.setTextColor(getResources().getColor(R.color.btn_color_blue));
                    }
                }else{
                    holder.ivSelectedDot.setVisibility(View.GONE);
                    if(!mScheduleList.get(position).getEnableFLg().equals("2")){
                        holder.tvTime.setTextColor(getResources().getColor(R.color.gray_five));
                        holder.tvNumber.setTextColor(getResources().getColor(R.color.gray_five));
                        holder.tvCurNumber.setTextColor(getResources().getColor(R.color.gray_five));
                        holder.tvName.setTextColor(getResources().getColor(R.color.gray_five));
                    }else{
                        holder.tvTime.setTextColor(getResources().getColor(R.color.btn_color_black));
                        holder.tvNumber.setTextColor(getResources().getColor(R.color.btn_color_black));
                        holder.tvCurNumber.setTextColor(getResources().getColor(R.color.btn_color_black));
                        holder.tvName.setTextColor(getResources().getColor(R.color.btn_color_black));
                    }
                }

            return convertView;
        }
        class ViewHolder {
            TextView tvTime;
            TextView tvCurNumber;
            TextView tvNumber;
            TextView tvName;
            View vLine;
            RelativeLayout rlItem;
            ImageView ivSelectedDot;
        }

    }

}

