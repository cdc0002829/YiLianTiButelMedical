package cn.redcdn.hvs.meeting.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.bean.BookMeetingExInfo;
import cn.redcdn.hvs.meeting.meetingManage.MedicalMeetingManage;
import cn.redcdn.hvs.meeting.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.jmeetingsdk.MeetingInfo;
import cn.redcdn.log.CustomLog;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReserveMeetingRoomActivity extends BaseActivity implements
    View.OnClickListener {

    private String tag = ReserveMeetingRoomActivity.class.getName();
    private TitleBar titleBar;
    private Button reserverBtn;
    private EditText reservetitleEdit;
    private Button reserveDateBtn;
    private Button reserveTimeBtn;
    private ArrayList<String> mUserList;
    private String mGid; //用于标注群聊发起预约会议的gid
    private String mTopic;
    private Boolean isCreateMeeting = false;


    private TextWatcher mTextWatcher = new TextWatcher() {
        private int editStart;
        private int editEnd;
        private int MAX_COUNT = 20;


        public void afterTextChanged(Editable s) {
            editStart = reservetitleEdit.getSelectionStart();
            editEnd = reservetitleEdit.getSelectionEnd();
            if (editEnd != 0) {
                reservetitleEdit.removeTextChangedListener(mTextWatcher);
                while (calculateLength(s.toString()) > MAX_COUNT) {
                    s.delete(editStart - 1, editEnd);
                    editStart--;
                    editEnd--;
                }
                reservetitleEdit.setSelection(editStart);
                reservetitleEdit.addTextChangedListener(mTextWatcher);
            }
        }


        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }


        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_activity_reserve_meeting_room);
        initView();
        mUserList = getIntent().getStringArrayListExtra("userlist");
        if (null != mUserList && mUserList.size() > 1) {
            mGid = getIntent().getStringExtra("gid");
        }
        if (null == mUserList) {
            mUserList = new ArrayList<String>();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void initView() {
        initTitleBar();
        reserverBtn = (Button) findViewById(R.id.reserver_meeting_btn);
        reserveDateBtn = (Button) findViewById(R.id.reserve_dateBtn);
        reserveTimeBtn = (Button) findViewById(R.id.reserve_timeBtn);
        reserverBtn.setOnClickListener(this);
        reserveDateBtn.setOnClickListener(this);
        reserveTimeBtn.setOnClickListener(this);
        reservetitleEdit = (EditText) findViewById(R.id.meetingtitle_edit);
        String nickName = cn.redcdn.hvs.AccountManager.getInstance(this).getAccountInfo().nickName;
        if (TextUtils.isEmpty(nickName)) {
            reservetitleEdit.setText(R.string.no_name_order_consultation);
        } else {
            reservetitleEdit.setText(CommonUtil.getLimitSubstring(nickName, 10) + getString(R.string.is_order_consultation));
        }
        reservetitleEdit.addTextChangedListener(mTextWatcher);
        reserveDateBtn.setText(getDateNow());
        reserveTimeBtn.setText(getTimeNow());
    }


    private void initTitleBar() {
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.subscribe_consultation));
    }


    private void reserveMeeting() {
        Pattern p = Pattern
            .compile("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]");
        Matcher m = p.matcher(reservetitleEdit.getText().toString().trim());
        if (m.find()) {
            CustomToast.show(getApplicationContext(), getString(R.string.theme_format_error),
                Toast.LENGTH_SHORT);
            return;
        }

        if (getPreciseTime() < System.currentTimeMillis()) {
            CustomToast.show(getApplicationContext(), getString(R.string.start_time_canot_less_current_time),
                Toast.LENGTH_SHORT);
            return;
        }
        if (!reservetitleEdit.getText().toString().equalsIgnoreCase("")) {
            mTopic = reservetitleEdit.getText().toString();
        } else {
            String nickName = cn.redcdn.hvs.AccountManager.getInstance(this)
                .getAccountInfo().nickName;
            if (TextUtils.isEmpty(nickName)) {
                mTopic = getString(R.string.no_name_order_meeting);
            } else {
                mTopic = CommonUtil.getLimitSubstring(nickName, 10) + getString(R.string.is_order_meeting);
            }
        }

        int ret = MedicalMeetingManage.getInstance()
            .createBookMeeting(TAG, new ArrayList<String>(), mTopic, getPreciseTime() / 1000 + "",
                new MedicalMeetingManage.OnReserveMeetingListener() {
                    @Override
                    public void OnReserveMeeting(int valueCode, MeetingInfo meetingInfo) {
                        isCreateMeeting = false;
                        removeLoadingView();
                        // CustomLog.i(TAG,
                        //     "OnReserveMeeting::meetingInfo.creatorId==" + meetingInfo.creatorId);
                        if (valueCode == 0) {
                            // MeetingItemInfo
                            //     info = (MeetingItemInfo) intent.getSerializableExtra("meetinfo");
                            // ArrayList<String> userlist = intent.getStringArrayListExtra("userlist");
                            // DaoPreference
                            //     dao = NetPhoneApplication.getPreference();
                            String number = AccountManager
                                .getInstance(ReserveMeetingRoomActivity.this)
                                .getAccountInfo()
                                .getNube();
                            String nickname = AccountManager.getInstance(
                                ReserveMeetingRoomActivity.this).getAccountInfo().nickName;
                            BookMeetingExInfo meetinfo = new  BookMeetingExInfo();
                            meetinfo.setBookNube(number);
                            meetinfo.setBookName(nickname);
                            meetinfo.setMeetingRoom(meetingInfo.meetingId);
                            meetinfo.setMeetingTheme(mTopic);
                            meetinfo.setMeetingTime(getPreciseTime());
                            meetinfo.setHasMeetingPassWord(0);
                            meetinfo.setMeetingUrl(MedicalMeetingManage.JMEETING_INVITE_URL);
                            //预约会议室成功后，发送预约消息
                            MedicalMeetingManage manager = MedicalMeetingManage.getInstance();
                            // manager.setContext(ReserveMeetingRoomActivity.this);
                            manager.sendBookMeetingMsgs(meetinfo, mUserList,mGid);

                            CustomLog.i(TAG, meetinfo.getBookName() + meetinfo.getMeetingRoom() +
                                meetinfo.getMeetingTheme());
                            Intent i = new Intent(ReserveMeetingRoomActivity.this,
                                ReserveSuccessActivity.class);
                            i.putExtra(ReserveSuccessActivity.KEY_BOOK_MEETING_EXINFO, meetinfo);
                            startActivity(i);
                            finish();
                        } else {

                        }
                    }
                });
        CustomLog.i(TAG,"创建预约会议的同步返回ret=="+ret);
        switch (ret) {
            case 0:
                showLoadingView(getString(R.string.ordering_consultation), new DialogInterface.OnCancelListener() {
                    @Override public void onCancel(DialogInterface dialog) {
                        dialog.cancel();
                    }
                });
                isCreateMeeting = true;
                break;
            case -6:
                CommonUtil.showToast(getString(R.string.login_checkNetworkError));
                break;
            default:
                CommonUtil.showToast(getString(R.string.server_connect_error_wait_try));
                break;
        }
    }


    private long getPreciseTime() {
        Calendar dayc1 = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date daystart;
        try {
            String dt = reserveDateBtn.getText() + " "
                + reserveTimeBtn.getText() + ":00";
            CustomLog.e(tag, dt + "=dt");
            daystart = df.parse(dt);
            dayc1.setTime(daystart); // 得到的dayc1就是你需要的calendar了
            CustomLog.e(tag, dayc1.getTimeInMillis() + "");
            CustomLog.e(tag, dayc1.getTime() + "=====");
            CustomLog.e(tag, System.currentTimeMillis() + "=====");
            return (dayc1.getTimeInMillis());
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private int calculateLength(String etstring) {
        char[] ch = etstring.toCharArray();
        int varlength = 0;
        for (int i = 0; i < ch.length; i++) {
            if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F)
                || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) {
                varlength = varlength + 2;
            } else {
                varlength++;
            }
        }
        return varlength;
    }


    private String getDateNow() {
        long mill = System.currentTimeMillis() + 60 * 60 * 1000;
        String time = "";
        String createTimeStr = new java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss ").format(new java.util.Date(mill));
        Calendar c = getCalendar(createTimeStr);
        int createDay = c.get(Calendar.DATE);
        int createYear = c.get(Calendar.YEAR);
        int createHour = c.get(Calendar.HOUR_OF_DAY);
        CustomLog.e(tag, "createHour=" + createHour);
        CustomLog.e(tag, "createl=" + mill);
        int createMonth = c.get(Calendar.MONTH) + 1;
        time = createYear + "";
        if (createMonth < 10) {
            time = time + "-0" + createMonth;
        } else {
            time = time + "-" + createMonth;
        }
        if (createDay < 10) {
            time = time + "-0" + createDay;
        } else {
            time = time + "-" + createDay;
        }
        return time;
    }


    private String getTimeNow() {
        long mill = System.currentTimeMillis() + 60 * 60 * 1000;//自动加一个小时
        String time = "";
        String createTimeStr = new java.text.SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss ").format(new java.util.Date(mill));
        Calendar c = getCalendar(createTimeStr);
        int createHour = c.get(Calendar.HOUR_OF_DAY);
        CustomLog.e(getLogTag(), "createHour=" + createHour);
        CustomLog.e(getLogTag(), "createl=" + mill);
        int createMinute = c.get(Calendar.MINUTE);
        if (createHour < 10) {
            time = "0" + createHour;
        } else {
            time = createHour + "";
        }
        if (createMinute < 10) {
            time = time + ":0" + createMinute;
        } else {
            time = time + ":" + createMinute;
        }
        return time;
    }


    public Calendar getCalendar(String datestr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = GregorianCalendar.getInstance();
        try {
            calendar.setTime(sdf.parse(datestr));
        } catch (ParseException e) {
            CustomLog.e(tag, "getCalendar  " + e.toString());
        }
        return calendar;
    }


    public class MyDatePickerDialog implements
        DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            String month = "";
            if (monthOfYear + 1 < 10) {
                month = "0" + (monthOfYear + 1);
            } else {
                month = monthOfYear + 1 + "";
            }
            String day = "";
            if (dayOfMonth < 10) {
                day = "0" + dayOfMonth;
            } else {
                day = dayOfMonth + "";
            }
            reserveDateBtn.setText(year + "-" + month + "-" + day);
        }
    }


    public class MyTimePickerDialog implements
        TimePickerDialog.OnTimeSetListener {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            String hour = "";
            if (hourOfDay < 10) {
                hour = "0" + hourOfDay;
            } else {
                hour = hourOfDay + "";
            }
            String min = "";
            if (minute < 10) {
                min = "0" + minute;
            } else {
                min = minute + "";
            }
            reserveTimeBtn.setText(hour + ":" + min);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reserve_dateBtn:
                String dateStr = reserveDateBtn.getText().toString().trim();
                String[] str = dateStr.split("-");
                MyDatePickerDialog myDatePickerDialog = new MyDatePickerDialog();
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    myDatePickerDialog, Integer.parseInt(str[0]),
                    Integer.parseInt(str[1]) - 1, Integer.parseInt(str[2]));
                datePickerDialog.show(); // 显示日期设置对话框
                break;
            case R.id.reserve_timeBtn:
                String timeStr = reserveTimeBtn.getText().toString().trim();
                String[] s = timeStr.split(":");
                MyTimePickerDialog myTimePickerDialog = new MyTimePickerDialog();
                TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    myTimePickerDialog, Integer.parseInt(s[0]),
                    Integer.parseInt(s[1]), true);
                timePickerDialog.show();
                break;
            case R.id.reserver_meeting_btn:
                reserveMeeting();
                break;
            default:
                break;
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        CustomLog.i(TAG, "点击退出键");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    protected void onBack() {
        if (isCreateMeeting) {
            removeLoadingView();
            isCreateMeeting = false;
            MedicalMeetingManage.getInstance().cancelCreateMeeting(
                ReserveMeetingRoomActivity.class.getName());
        } else {
            finish();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        removeLoadingView();
    }

    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    //     CustomToast.show(this, "点击退出键", CustomToast.LENGTH_SHORT);
    //     CustomLog.i(TAG, "点击退出键");
    //     if (keyCode == KeyEvent.KEYCODE_BACK) {
    //         onBack();
    //         return true;
    //     }
    //     return super.onKeyDown(keyCode, event);
    // }
    //
    // protected void onBack() {
    //     if (waitDialog != null && waitDialog.isShowing()) {
    //         removeLoadingView();
    //         MedicalMeetingManage.getInstance().cancelCreateMeeting(
    //             ConsultingRoomActivity.class.getName());
    //     } else {
    //         finish();
    //     }
    // }

}
