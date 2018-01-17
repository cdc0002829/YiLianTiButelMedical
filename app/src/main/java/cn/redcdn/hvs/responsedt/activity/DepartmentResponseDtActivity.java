package cn.redcdn.hvs.responsedt.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.HPUGetListcslsBydate;
import cn.redcdn.datacenter.hpucenter.data.CSLInfo;
import cn.redcdn.datacenter.hpucenter.data.DepCslInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.responsedt.adapter.IndicatorExpandableListAdapter;
import cn.redcdn.hvs.udtroom.configs.UDTDataConstant;
import cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static com.unionpay.sdk.ab.mContext;

public class DepartmentResponseDtActivity extends BaseActivity {
    private ExpandableListView expandableListView;
    private IndicatorExpandableListAdapter adapter;
    private LinearLayout loading_lay;
    private TextView loading_text;
    //    private SwipeRefreshLayout swipeRefreshLayout;
    private List<String> groupName;
    private List<String> hpuName;
    private List<DepCslInfo> childName;
    private TitleBar titleBar;
    private String selectdate;
    private String nowDate;
    private ImageView dateBtn;
    private boolean isFristGetData = true;
    private boolean isDatachange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department_response_dt);
        initView();
    }

    private void initView() {
        titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.today_response_dt_table));
        titleBar.enableBack();
//        titleBar.enableRightBtn("", R.drawable.date_btn, new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
        dateBtn = (ImageView) findViewById(R.id.date_btn);
        dateBtn.setOnClickListener(mbtnHandleEventListener);
        loading_lay = (LinearLayout) findViewById(R.id.loading_layout);
        loading_text = (TextView) findViewById(R.id.loading_text);
        selectdate = getDateNow();
        groupName = new ArrayList<String>();
        hpuName = new ArrayList<String>();
        childName = new ArrayList<DepCslInfo>();
//        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipefreshlayout);
        expandableListView = (ExpandableListView) findViewById(R.id.expandable_list);
        adapter = new IndicatorExpandableListAdapter(groupName, hpuName, childName, DepartmentResponseDtActivity.this);
        initData(selectdate);
        expandableListView.setAdapter(adapter);
        expandableListView.setGroupIndicator(null);
        //  设置分组项的点击监听事件
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                CustomLog.d(TAG, "onGroupClick: groupPosition:" + groupPosition + ", id:" + id);
                boolean groupExpanded = parent.isGroupExpanded(groupPosition);
                adapter.setIndicatorState(groupPosition, groupExpanded);
                // 请务必返回 false，否则分组不会展开
                return false;
            }
        });

        //  设置子选项点击监听事件
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                CustomLog.d(TAG, "onChildClick groupPosition:" + groupPosition + ",childPosition" + childPosition + "id" + id);
//                Intent intent = new Intent(DepartmentResponseDtActivity.this, UDTChatRoomActivity.class);
//                intent.putExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG, childName.get(groupPosition).getCslInfos().get(childPosition).getId());
//                startActivity(intent);
                return true;
            }
        });
        adapter.setOnRefreshListen(new IndicatorExpandableListAdapter.onRefreshListen() {
            @Override
            public void refresh() {
                initData(selectdate);
            }
        });
//        swipeRefreshLayout.setOnRefreshListener(this);
    }

//    @Override
//    public void onRefresh() {
//        CustomLog.d(TAG, "onRefresh 下拉刷新");
//        swipeRefreshLayout.setRefreshing(true);
//        initData("");
////        Handler handler = new Handler();
////        handler.postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                CustomToast.show(DepartmentResponseDtActivity.this, "刷新完成", CustomToast.LENGTH_SHORT);
////                swipeRefreshLayout.setRefreshing(false);
////            }
////        }, 3000);//3秒后执行Runnable中的run方法
//    }

    /**
     * 日期选择器
     */
    public class MyDatePickerDialog implements
            DatePickerDialog.OnDateSetListener {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            String month = "";
            String titlemonth = "";
            if (monthOfYear + 1 < 10) {
                month = "0" + (monthOfYear + 1);

            } else {
                month = monthOfYear + 1 + "";
            }
            titlemonth = monthOfYear + 1 + "";
            String day = "";
            String titleday = "";
            if (dayOfMonth < 10) {
                day = "0" + dayOfMonth;

            } else {
                day = dayOfMonth + "";
            }
            titleday = dayOfMonth + "";
//            reserveDateBtn.setText(year + "-" + month + "-" + day);
            String dateStr = year + month + day;
            CustomLog.d(TAG, "选择的时间是：" + dateStr);
            if (!nowDate.equals(dateStr)) {
                titleBar.setTitle(titlemonth + getString(R.string.month) + titleday + getString(R.string.day) + getString(R.string.response_dt_table));
            } else {
                titleBar.setTitle(getString(R.string.today_response_dt_table));
            }
            if (!selectdate.equals(dateStr)) {
                isDatachange = true;
                initData(year + month + day);
                selectdate = dateStr;
            }
        }
    }

    /**
     * 获取当前的系统日期
     * @return  yyyyMMdd形式的字符串
     */
    private String getDateNow() {
        CustomLog.d(TAG, "getDateNow()");
        long mill = System.currentTimeMillis();
        String time = "";
        String createTimeStr = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss ").format(new java.util.Date(mill));
        Calendar c = getCalendar(createTimeStr);
        int createDay = c.get(Calendar.DATE);
        int createYear = c.get(Calendar.YEAR);
        int createHour = c.get(Calendar.HOUR_OF_DAY);
        CustomLog.d(TAG, "createHour=" + createHour);
        CustomLog.d(TAG, "createl=" + mill);
        int createMonth = c.get(Calendar.MONTH) + 1;
        time = createYear + "";
        if (createMonth < 10) {
            time = time + "0" + createMonth;
        } else {
            time = time + "" + createMonth;
        }
        if (createDay < 10) {
            time = time + "0" + createDay;
        } else {
            time = time + "" + createDay;
        }
        nowDate = time;
        return time;
    }

    /**
     * 获取当前系统的时间
     * @return  hh：mm:ss形式的字符串
     */
    private String getTimeNow() {
        CustomLog.d(TAG, "getTimeNow()");
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

    /**
     * 把时间转化成yyyy-MM-dd HH:mm:ss形式的
     * @param datestr
     * @return
     */
    public Calendar getCalendar(String datestr) {
        CustomLog.d(TAG, "getCalendar datestr:" + datestr);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = GregorianCalendar.getInstance();
        try {
            calendar.setTime(sdf.parse(datestr));
        } catch (ParseException e) {
            CustomLog.e(TAG, "getCalendar  " + e.toString());
        }
        return calendar;
    }

    /**
     * 获取数据
     * @param date
     */
    private void initData(String date) {
        CustomLog.d(TAG, "initData" + "|date:" + date);
        final HPUGetListcslsBydate hpuGetListcslsBydate = new HPUGetListcslsBydate() {
            @Override
            protected void onSuccess(List<DepCslInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "HPUGetListcslsBydate onSuccess");
//                swipeRefreshLayout.setRefreshing(false);
                if (groupName.size() != 0 || hpuName.size() != 0 || childName.size() != 0) {
                    groupName.clear();
                    hpuName.clear();
                    childName.clear();
                }
//                int count = responseContent.size() / 4;
//                for (int i = 0; i < count; i++) {
//                    groupName.add("9:00~10:00");
//                    groupName.add("10:00~11:00");
//                    groupName.add("13:30~14:30");
//                    groupName.add("14:30~15:30");
//                }
                for (int j = 0; j < responseContent.size(); j++) {
                    if (responseContent.get(j).getCslInfos() != null) {
                        if (responseContent.get(j).getCslInfos().size() != 0) {
                            groupName.add(responseContent.get(j).getRangeNumber());
                            hpuName.add(responseContent.get(j).getDtName());
                            childName.add(responseContent.get(j));
                        }
                    } else {
                        CustomLog.d(TAG, "responseContent.get(" + j + ").getCslInfos() 为null");
                    }
                }
//                String time_now[] = getTimeNow().split(":");
//                int time_hour = Integer.valueOf(time_now[0]);
//                int time_min = Integer.valueOf(time_now[1]);
//                int groupNum;
//                if (time_hour <= 10) {
//                    groupNum = 0;
//                } else if (time_hour <= 11) {
//                    groupNum = 1;
//                } else if (time_hour <= 14 && time_min <= 30) {
//                    groupNum = 2;
//                } else if (time_hour <= 15 && time_min <= 30) {
//                    groupNum = 3;
//                } else {
//                    groupNum = 0;
//                }
//                for (int k = 0; k < count; k++) {
//                    expandableListView.expandGroup(groupNum + (k * 4));
//                }
                if (childName.size() == 0) {
                    loading_lay.setVisibility(View.VISIBLE);
                    loading_text.setText(getString(R.string.no_response_info));
                    expandableListView.setVisibility(View.GONE);
                } else {
                    loading_lay.setVisibility(View.GONE);
                    expandableListView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
                for (int k = 0; k < childName.size(); k++) {
                    expandableListView.expandGroup(k);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
//                swipeRefreshLayout.setRefreshing(false);
                removeLoadingView();
                CustomLog.d(TAG, "HPUGetListcslsBydate onFail statusCode" + statusCode + "|statusInfo" + statusInfo);
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DepartmentResponseDtActivity.this).tokenAuthFail(statusCode);
                } else {
                    loading_lay.setVisibility(View.VISIBLE);
                    loading_text.setText(getString(R.string.get_response_info_fail_try_again));
                    expandableListView.setVisibility(View.GONE);
                    CustomToast.show(DepartmentResponseDtActivity.this, statusInfo, CustomToast.LENGTH_SHORT);
                }
            }
        };
        if (isFristGetData||isDatachange) {
            if (isDatachange){
                isDatachange = false;
            }
            showLoadingView(getString(R.string.wait), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    removeLoadingView();
                    CustomLog.d(TAG, "取消获取某日本科室排班列表");
                    hpuGetListcslsBydate.cancel();
                    DepartmentResponseDtActivity.this.finish();
                }
            }, true);
        }
        CustomLog.d(TAG, "hpuGetListcslsBydate.getcsllist|token: " + AccountManager.getInstance(DepartmentResponseDtActivity.this).getMdsToken() + "|date: " + date);
//        hpuGetListcslsBydate.getcsllist("a977fd57-1e42-4a4e-a401-ee915cddf7b4_e16557a20eae4093874c3faff893a62e", "20171129");
        hpuGetListcslsBydate.getcsllist(AccountManager.getInstance(DepartmentResponseDtActivity.this).getMdsToken(), date);
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.date_btn:
                MyDatePickerDialog myDatePickerDialog = new MyDatePickerDialog();
                DatePickerDialog datePickerDialog = new DatePickerDialog(DepartmentResponseDtActivity.this,
                        myDatePickerDialog, Integer.parseInt(selectdate.substring(0, 4)),
                        Integer.parseInt(selectdate.substring(4, 6)) - 1, Integer.parseInt(selectdate.substring(6, 8)));
                datePickerDialog.show(); // 显示日期设置对话框
                break;
            default:
                break;
        }
    }

    /**
     * 在onResume中获取数据是为了从UDTChatRoomActivity中回来的时候，刷新数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isFristGetData) {
            isFristGetData = false;
        } else {
            initData(selectdate);
        }
    }
}
