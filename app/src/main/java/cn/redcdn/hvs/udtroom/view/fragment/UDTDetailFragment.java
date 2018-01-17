package cn.redcdn.hvs.udtroom.view.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import cn.redcdn.datacenter.hpucenter.HPUAcceptCsl;
import cn.redcdn.datacenter.hpucenter.HPUCanelCsl;
import cn.redcdn.datacenter.hpucenter.HPUCreateTftm;
import cn.redcdn.datacenter.hpucenter.HPUGetTfScheduls;
import cn.redcdn.datacenter.hpucenter.HPUGetTransferDeps;
import cn.redcdn.datacenter.hpucenter.HPUSubmitAdvice;
import cn.redcdn.datacenter.hpucenter.data.CSLRoomDetailInfo;
import cn.redcdn.datacenter.hpucenter.data.HPUCommonCode;
import cn.redcdn.datacenter.hpucenter.data.TFCommonInfo;
import cn.redcdn.datacenter.hpucenter.data.TFDoctorInfo;
import cn.redcdn.datacenter.hpucenter.data.TFRangeType;
import cn.redcdn.datacenter.hpucenter.data.TFSchedInfo;
import cn.redcdn.datacenter.hpucenter.data.TFSectionInfo;
import cn.redcdn.datacenter.hpucenter.data.TFdepInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.head.activity.ReferralActivity;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.hvs.officialaccounts.listener.DingyueDisplayImageListener;
import cn.redcdn.hvs.requesttreatment.ImagePagerAdapterList;
import cn.redcdn.hvs.requesttreatment.NewCurInfo;
import cn.redcdn.hvs.requesttreatment.NewPickerScrollView;
import cn.redcdn.hvs.requesttreatment.PatientConditionActivity;
import cn.redcdn.hvs.requesttreatment.Pickers;
import cn.redcdn.hvs.requesttreatment.loopview.LoopView;
import cn.redcdn.hvs.requesttreatment.loopview.OnItemSelectedListener;
import cn.redcdn.hvs.udtroom.configs.UDTGlobleData;
import cn.redcdn.hvs.udtroom.repository.RemoteDataSource;
import cn.redcdn.hvs.udtroom.view.activity.AppraiseDialog;
import cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.log.CustomLog;

import com.example.xlhratingbar_lib.XLHRatingBar;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_ACCOUNT_HAS_AUDITED;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_ACCOUNT_IS_EXISTED;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_FAILED_CREATE_GQRCODE;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_OFFACC_NOT_EXIST;
import static cn.redcdn.hvs.head.activity.ReferralActivity.REFERRAL;

/**
 * @author guoyx
 *         <p>
 *         联合诊疗室患者诊疗单 Fragment
 */
public class UDTDetailFragment extends BaseFragment
        implements UDTGlobleData.DateChangeListener {
    private static final String TAG = UDTDetailFragment.class.getSimpleName();
    private static final int MSG_DEPS = 3001;
    private static final int MSG_SCHEDUILS = 3002;
    public static final int UDT_CODE = 3003;
    private static final int MSG_ADD_SCHEDUILS = 3004;
    private static final int MSG_DECREA_SCHEDUILS = 3005;
    // 诊疗 ID
    private String dtID;
    Button executeBtn;//撤销诊疗/接诊/结束诊疗
    TextView scheduleTimeTxt;// 预约某月某日
    TextView scheduleTimeSeconds;//预约时间段
    TextView reserveNumTxt;// 预约号
    TextView curDTStateTxt;//接诊状态

    // 辅助检查界面
    LinearLayout auxiliaryExamLayout;

    // 提交诊疗建议
    LinearLayout submitOpinionLayout;
    // 求诊者，诊疗建议刷新按钮
    Button dtOpinionRefreshBtn;
    // 诊疗建议提交按钮
    Button dtOpinionSubmitBtn;

    private CSLRoomDetailInfo roomDetailInfo;

    private UDTGlobleData mUDTGlobleData;

    /**
     * 选择按钮
     */
    private LinearLayout ll_checkboxGroup;
    private CheckBox local_treat;//本地治疗
    private CheckBox transfer_treat;//转诊
    private boolean isLocalAdv = false;

    //本地治疗
    private FrameLayout Re_localTreat;
    private EditText edt_local;
    private TextView tv_local;
    private TextView local;

    //转诊
    private RelativeLayout Re_tranDep;
    private RelativeLayout Re_tranSchedul;
    private LinearLayout ll_transfer;
    private TextView tv_office;//科室
    private RelativeLayout btn_select_office;
    private TextView transfer_date;//门诊
    private TextView tv_transfer_moment;//上午或者下午
    private TextView tv_transfer_doctor;//医生
    private RelativeLayout btn_transfer_date;
    private EditText Edit_transfer;//转诊建议
    private TextView Text_TransferAdv;
    private Context mContext;
    private TextView exchange;
    private ImageView im_trDep, imag_TraSched;

    //求诊方
    cn.redcdn.hvs.im.view.RoundImageView reqDocImag;//求诊医生头像
    TextView reqDocName;//求诊医生姓名
    TextView reqDocJob;//求诊医生职称
    TextView reqDepart;//求诊科室
    TextView reqHospital;//求诊医院

    //接诊方
    cn.redcdn.hvs.im.view.RoundImageView respDocImag;
    TextView respDocName;
    TextView respDocJob;
    TextView respDepart;
    TextView respHospital;

    //患者基本信息
    TextView patientName;
    TextView patientIDcard;
    TextView patientGuardian;
    TextView guardianIDcard;
    TextView guardianPhone;
    TextView patientPhone;

    //患者详情
    LinearLayout LL_guardian_ID;
    LinearLayout LL_guardian_name;
    LinearLayout ll_patientPhoneFath;
    LinearLayout LL_guardian_phone;
    RelativeLayout Re_patientDetail;
    Button editButton;//编辑患者资料按钮
    TextView patientAge;
    TextView patientSex;
    TextView patientHigh;
    TextView patientWeight;
    TextView mainContent;//主诉
    TextView bodyChecked;//查体

    //辅助检查
    TextView tvExam;
    LinearLayout layout;


    //目前待解决问题
    TextView tvWaitRePro;

    //
    private ScrollView mScrollview;
    private DingyueDisplayImageListener mDisplayImageListener = null;
    private ImageLoader imageLoader;
    private String token;
    private UDTChatRoomActivity udtChatRoomActivity;
    private String time;

    //评价
    LinearLayout ll_doctorComment;
    Button btn_response;
    XLHRatingBar need_RatingBar;
    TextView tv_needComment;

    Button btn_request;
    XLHRatingBar help_RatingBar;
    TextView tv_helpComment;
    LinearLayout btn_transfer;
    private AlertDialog dialog;
    private View grayView;

    private String transferId = "";
    private ImagePagerAdapterList adapterList;
    private List<Contact> contactList = new ArrayList<>();
    private static final int IMAGE_LIST_COLUMN = 4;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DEPS:
                    List<TFdepInfo> list = (List<TFdepInfo>) msg.obj;
                    showOfficeDialog(list);
                    break;

                default:
                    break;
            }
        }
    };

    private RelativeLayout decreaseDateLayout;
    private RelativeLayout increaseDateLayout;
    private TextView dateTextView;
    private LoopView departLoopView;
    private LoopView rangeLoopView;
    private LoopView doctorLoopView;
    private TextView cancelTextView;
    private TextView confirmTextView;
    private String selectedDepartName;
    private String selectedRangeName;
    private String selectedDoctorName;
    private String selectedDepartId;
    private String selectedRangeId;
    private String selectedDoctorId;
    private String deptId;//门诊id
    private String schedulId;//门诊排班id
    private String doctorId;////医生id
    private String deptName;
    private String schedName;
    private String doctorName;
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private List<TFSectionInfo> departInfoList;
    private List<TFRangeType> rangeInfoList;//门诊集合
    private List<TFDoctorInfo> doctorInfoList;//医生列表
    private ViewPager viewPager;
    private TextView tv_loading;
    RelativeLayout rl_udt_content;
    private TFSchedInfo mResponseContent;
    private List<String> departAdapter;
    private List<String> rangeAdapter;
    private List<String> doctorAdpater;
    private String mTime;
    private String mOffice;
    private String mData;
    private List<String> departIdList;
    private List<String> docterIdList;
    private List<String> rangIdList;
    private String date;
    private List<String> rangFlagList;
    private String selectedEnableFlag;
    private String increaseTime;

    private void showArrangementDialog() {
        deptId = "";
        schedulId = "";
        doctorId = "";
        deptName = "";
        schedName = "";
        doctorName = "";
        dialog = new AlertDialog.Builder(mContext).create();
        dialog.show();
        Window window = dialog.getWindow();
        dialog.setContentView(R.layout.udt_arrangement_diagram);
        requestTranData(getStringDateShort());
        decreaseDateLayout = (RelativeLayout) window.findViewById(R.id.rl_udt_decrease_date);
        increaseDateLayout = (RelativeLayout) window.findViewById(R.id.rl_udt_increase_date);
        tv_loading = (TextView) window.findViewById(R.id.tv_loading);
        rl_udt_content = (RelativeLayout) window.findViewById(R.id.rl_udt_content);
        updateSchedulUI(false);
        dateTextView = (TextView) window.findViewById(R.id.tv_udt_title);
        departLoopView = (LoopView) window.findViewById(
                R.id.udt_pickerscrlllview_time);
        rangeLoopView = (LoopView) window.findViewById(
                R.id.udt_pickerscrlllview_department);
        doctorLoopView = (LoopView) window.findViewById(
                R.id.udt_pickerscrlllview_name);
        cancelTextView = (TextView) window.findViewById(R.id.tv_udt_cancel);
        confirmTextView = (TextView) window.findViewById(R.id.tv_udt_confirm);
        departLoopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                selectedDepartName = departAdapter.get(index);
                selectedDepartId = departIdList.get(index);
                updataRangeData(selectedDepartId);

            }
        });
        rangeLoopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                selectedRangeName = rangeAdapter.get(index);
                selectedRangeId = rangIdList.get(index);
                selectedEnableFlag = rangFlagList.get(index);
                upDoctorInfo(selectedRangeId);
            }
        });
        doctorLoopView.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                selectedDoctorName = doctorAdpater.get(index);
                selectedDoctorId = docterIdList.get(index);
            }
        });

        dateTextView.setText(getWeek(getStringDateShort()) + " " +
                getStringDateShort().substring(4, 6) + getString(R.string.udt_month) + getStringDateShort().substring(6, 8) +
                getString(R.string.udt_day));

        decreaseDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentDay <= 1) {
                    if (currentMonth <= 1) {
                        currentYear--;
                        currentMonth = 12;
                        currentDay = 31;
                    } else {
                        currentMonth--;
                        if (currentMonth == 1 || currentMonth == 3 || currentMonth == 5 ||
                                currentMonth == 7
                                || currentMonth == 8 || currentMonth == 10 || currentMonth == 12) {
                            currentDay = 31;
                        } else if (currentMonth == 3 || currentMonth == 6 || currentMonth == 9 ||
                                currentMonth == 11) {
                            currentDay = 30;
                        } else if (currentMonth == 2) {
                            if (currentYear % 4 == 0) {
                                currentDay = 29;
                            } else if (currentYear % 4 != 0) {
                                currentDay = 28;
                            }
                        }
                    }
                } else {
                    String  month1;
                    String  day1;
                    if (currentMonth < 10) {
                      month1 = "0" + String.valueOf(currentMonth);
                    } else {
                        month1 = String.valueOf(currentMonth);
                    }
                    if (currentDay < 10) {
                        day1 = "0" + String.valueOf(currentDay);
                    } else {
                        day1 = String.valueOf(currentDay);
                    }
                    int a = Integer.parseInt(String.valueOf(currentYear) + String.valueOf(month1) + String.valueOf(day1));
                    int b = Integer.parseInt(getStringDateShort());
                    if (a <= b) {
                        CustomToast.show(mContext, "不可选择早于当日的排班", Toast.LENGTH_SHORT);
                    } else {
                        currentDay--;

                        //TODO:调“根据时间获取排班”接口
                        String year = "0";
                        String month = "0";
                        String day = "0";
                        year = String.valueOf(currentYear);
                        mData = String.valueOf(currentMonth)
                                + mContext.getString(R.string.reserve_treatment_month)
                                + String.valueOf(currentDay)
                                + mContext.getString(R.string.reserve_treatment_day);

                        if (currentMonth < 10) {
                            month = "0" + String.valueOf(currentMonth);
                        } else {
                            month = String.valueOf(currentMonth);
                        }
                        if (currentDay < 10) {
                            day = "0" + String.valueOf(currentDay);
                        } else {
                            day = String.valueOf(currentDay);
                        }
                        date = year + month + day;
                        dateTextView.setText(
                                getWeek(date) + " " +
                                        String.valueOf(currentMonth)
                                        + mContext.getString(R.string.reserve_treatment_month)
                                        + String.valueOf(currentDay)
                                        + mContext.getString(R.string.reserve_treatment_day));
                        updateSchedulUI(false);
                        requestTranData(date);
                    }

                }
            }
        });

        increaseDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((currentDay == 29 && currentMonth == 2 && (currentYear % 4 == 0))
                        || (currentDay == 28 && currentMonth == 2 && (currentYear % 4 != 0)))
                        || (currentDay == 30 && (currentMonth == 4 || currentMonth == 6
                        || currentMonth == 9 || currentMonth == 11))
                        ||
                        (currentDay >= 31 && (currentMonth == 1 | currentMonth == 3 || currentMonth == 5
                                || currentMonth == 7 || currentMonth == 8 || currentMonth == 10 ||
                                currentMonth == 12))) {
                    if (currentMonth >= 12) {
                        currentYear++;
                        currentMonth = 1;
                        currentDay = 1;
                    } else {
                        currentMonth++;
                        currentDay = 1;
                    }
                } else {
                    currentDay++;
                }


                //TODO:调“根据时间获取排班”接口
                String year = "0";
                String month = "0";
                String day = "0";
                year = String.valueOf(currentYear);
                mData = String.valueOf(currentMonth)
                        + mContext.getString(R.string.reserve_treatment_month)
                        + String.valueOf(currentDay)
                        + mContext.getString(R.string.reserve_treatment_day);
                if (currentMonth < 10) {
                    month = "0" + String.valueOf(currentMonth);
                } else {
                    month = String.valueOf(currentMonth);
                }
                if (currentDay < 10) {
                    day = "0" + String.valueOf(currentDay);
                } else {
                    day = String.valueOf(currentDay);
                }

                increaseTime = year + month + day;
                dateTextView.setText(getWeek(increaseTime) + " " +

                        String.valueOf(currentMonth)
                        + mContext.getString(R.string.reserve_treatment_month)
                        + String.valueOf(currentDay)
                        + mContext.getString(R.string.reserve_treatment_day));
                updateSchedulUI(false);
                requestTranData(increaseTime);

            }
        });

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        confirmTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (departInfoList.size() != 0) {
                    if (selectedEnableFlag.equals("2")) {
                        deptId = selectedDepartId;
                        schedulId = selectedRangeId;
                        doctorId = selectedDoctorId;
                        deptName = selectedDepartName;
                        schedName = selectedRangeName;
                        doctorName = selectedDoctorName;
                        if (deptName.equals("专家门诊")) {
                            tv_transfer_doctor.setText(doctorName);
                        } else {
                            tv_transfer_doctor.setText("");
                        }
                        transfer_date.setText(String.valueOf(currentMonth)
                                + mContext.getString(R.string.reserve_treatment_month)
                                + String.valueOf(currentDay)
                                + mContext.getString(R.string.reserve_treatment_day));
                        tv_transfer_moment.setText(schedName);
                        tv_office.setText(mOffice + "(" + deptName + ")");
                        dialog.dismiss();
                    } else if (selectedEnableFlag.equals("1")) {
                        CustomToast.show(mContext, "不能选择当前排班", Toast.LENGTH_SHORT);
                    }

                } else {
                    CustomToast.show(mContext, "排班为空,不能选择", Toast.LENGTH_LONG);
                }
            }
        });


        long mill = System.currentTimeMillis() + 60 * 60 * 1000;
        String createTimeStr = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss ").format(new java.util.Date(mill));
        java.util.Calendar c = getCalendar(createTimeStr);

        currentYear = c.get(java.util.Calendar.YEAR);
        currentMonth = c.get(java.util.Calendar.MONTH) + 1;
        currentDay = c.get(java.util.Calendar.DAY_OF_MONTH);
    }


    private void updateSchedulUI(boolean isShowFlag) {
        if (isShowFlag) {
            rl_udt_content.setVisibility(View.VISIBLE);
            tv_loading.setVisibility(View.INVISIBLE);
        } else {
            rl_udt_content.setVisibility(View.GONE);
            tv_loading.setVisibility(View.VISIBLE);
        }
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


    private String depId;
    private List<TFdepInfo> tFdepInfoList;


    private void showOfficeDialog(List<TFdepInfo> list) {
        depId = "";

        Button btn_cancel;
        ListView listView;
        LinearLayout office_ll;
        OfficeAdapter adapter;
        final AlertDialog officeDialog = new AlertDialog.Builder(mContext).create();
        officeDialog.show();
        Window window = officeDialog.getWindow();
        officeDialog.setContentView(R.layout.office_dialog);
        office_ll = (LinearLayout) window.findViewById(R.id.office_ll);
        btn_cancel = (Button) window.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                officeDialog.dismiss();
            }
        });
        listView = (ListView) window.findViewById(R.id.listview);
        tFdepInfoList = new ArrayList<>();
        tFdepInfoList.clear();
        for (int i = 0; i < list.size(); i++) {
            tFdepInfoList.add(list.get(i));
        }
        if (tFdepInfoList.size() == 1) {
            ViewGroup.LayoutParams params = office_ll.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) getResources().getDimension(R.dimen.y258);
            office_ll.setLayoutParams(params);

        } else if (tFdepInfoList.size() == 2) {
            ViewGroup.LayoutParams params = office_ll.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) getResources().getDimension(R.dimen.y328);
            office_ll.setLayoutParams(params);
        } else if (tFdepInfoList.size() == 3) {
            ViewGroup.LayoutParams params = office_ll.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) getResources().getDimension(R.dimen.y398);
            office_ll.setLayoutParams(params);
        } else if (tFdepInfoList.size() == 4) {
            ViewGroup.LayoutParams params = office_ll.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) getResources().getDimension(R.dimen.y468);
            office_ll.setLayoutParams(params);
        } else if (tFdepInfoList.size() > 5 || list.size() == 5) {
            ViewGroup.LayoutParams params = office_ll.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) getResources().getDimension(R.dimen.y538);
            office_ll.setLayoutParams(params);
        }
        adapter = new OfficeAdapter(tFdepInfoList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv_office.setText(tFdepInfoList.get(position).getName());
                mOffice = tFdepInfoList.get(position).getName();
                depId = tFdepInfoList.get(position).getId();
                officeDialog.dismiss();

            }
        });

    }


    private class OfficeAdapter extends BaseAdapter {
        private List<TFdepInfo> list;


        public OfficeAdapter(List<TFdepInfo> infoList) {
            this.list = infoList;
        }


        @Override
        public int getCount() {
            return list.size();
        }


        @Override
        public Object getItem(int position) {
            return list.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            OfficeViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.office_item, null);
                viewHolder = new OfficeViewHolder();
                viewHolder.office = (TextView) convertView.findViewById(R.id.office);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (OfficeViewHolder) convertView.getTag();
            }
            String office = list.get(position).getName();
            if (!TextUtils.isEmpty(office)) {
                viewHolder.office.setText(office);
            }
            return convertView;
        }
    }


    /**
     * 将 Fragment 界面变化事件回调给 Activity
     */
    @Override
    public void onAttach(Context context) {
        this.mContext = context;
        super.onAttach(context);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.i(TAG, "onCreate()");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        CustomLog.i(TAG, "onCreateView");
        udtChatRoomActivity = (UDTChatRoomActivity) getActivity();
        View contentView = inflater.inflate(R.layout.udtroom_detail_layout, container, false);
        dtID = mUDTGlobleData.getDTId();
        token = AccountManager.getInstance(mContext).getMdsToken();
        mDisplayImageListener = new DingyueDisplayImageListener();
        imageLoader = ImageLoader.getInstance();
        time = getStringDateShort();
        initView(contentView);
        updateUI();
        initBroadcast();
        return contentView;
    }


    private void requestData() {
        new RemoteDataSource().getRemoteCSLRoomDetailData(token, dtID,
                new RemoteDataSource.DataCallback() {
                    @Override
                    public void onSuccess(CSLRoomDetailInfo data) {
                        mUDTGlobleData.updateCSLRoomDetailInfo(data);
                    }


                    @Override
                    public void onFailed(int statusCode, String statusInfo) {

                    }
                });
    }


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return null;
    }


    @Override
    protected void setListener() {
    }


    @Override
    protected void initData() {

    }


    /**
     * 初始化所有控件信息
     */
    private void initView(View v) {
        grayView = v.findViewById(R.id.grayView);
        viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        viewPager.setHorizontalScrollBarEnabled(false);
        viewPager.setVerticalScrollBarEnabled(false);
        im_trDep = (ImageView) v.findViewById(R.id.im_trDep);
        imag_TraSched = (ImageView) v.findViewById(R.id.imag_TraSched);
        mScrollview = (ScrollView) v.findViewById(R.id.mScrollView);
        mScrollview.setVerticalScrollBarEnabled(false);
        mScrollview.setHorizontalScrollBarEnabled(false);
        //求诊方
        reqDocImag = (RoundImageView) v.findViewById(R.id.req_doctor_img);
        reqDocName = (TextView) v.findViewById(R.id.req_doctor_name_txt);
        reqDocJob = (TextView) v.findViewById(R.id.req_doctor_job_title);
        reqDepart = (TextView) v.findViewById(R.id.req_departments);
        reqHospital = (TextView) v.findViewById(R.id.req_doctor_department_txt);

        //接诊方
        respDocImag = (RoundImageView) v.findViewById(R.id.resp_doctor_img);
        respDocName = (TextView) v.findViewById(R.id.resp_doctor_name_txt);
        respDocJob = (TextView) v.findViewById(R.id.recep_doctor_job_title);
        respDepart = (TextView) v.findViewById(R.id.resp_department);
        respHospital = (TextView) v.findViewById(R.id.resp_doctor_department_txt);

        //患者基本信息
        LL_guardian_phone = (LinearLayout) v.findViewById(R.id.LL_guardian_phone);
        LL_guardian_ID = (LinearLayout) v.findViewById(R.id.LL_guardian_ID);
        LL_guardian_name = (LinearLayout) v.findViewById(R.id.LL_guardian_name);
        patientName = (TextView) v.findViewById(R.id.patient_name_txt);
        patientIDcard = (TextView) v.findViewById(R.id.patient_ID_card_txt);
        patientGuardian = (TextView) v.findViewById(R.id.guardian_txt);
        guardianIDcard = (TextView) v.findViewById(R.id.guardian_ID_card_txt);
        guardianPhone = (TextView) v.findViewById(R.id.cell_phone_num_txt);
        patientPhone = (TextView) v.findViewById(R.id.patient_PhoneNumber);

        //评价
        ll_doctorComment = (LinearLayout) v.findViewById(R.id.ll_doctorComment);
        btn_response = (Button) v.findViewById(R.id.btn_response);
        need_RatingBar = (XLHRatingBar) v.findViewById(R.id.need_RatingBar);
        tv_needComment = (TextView) v.findViewById(R.id.tv_needComment);

        btn_request = (Button) v.findViewById(R.id.btn_request);
        help_RatingBar = (XLHRatingBar) v.findViewById(R.id.help_RatingBar);
        tv_helpComment = (TextView) v.findViewById(R.id.tv_helpComment);

        btn_response.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AppraiseDialog.class);
                intent.putExtra(AppraiseDialog.APPRAISEDIALOG_IS_REQUEST_FLAg, false);
                intent.putExtra(AppraiseDialog.APPRAISEDIALOG_DT_ID, dtID);
                startActivity(intent);

            }
        });

        btn_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AppraiseDialog.class);
                intent.putExtra(AppraiseDialog.APPRAISEDIALOG_IS_REQUEST_FLAg, true);
                intent.putExtra(AppraiseDialog.APPRAISEDIALOG_DT_ID, dtID);
                startActivity(intent);
            }
        });

        //患者详情
        ll_patientPhoneFath = (LinearLayout) v.findViewById(R.id.ll_patientPhoneFath);
        Re_patientDetail = (RelativeLayout) v.findViewById(R.id.Re_patientDetail);
        editButton = (Button) v.findViewById(R.id.edit_patient_detail_btn);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                NewCurInfo newCurInfo = new NewCurInfo();
                newCurInfo.setCurNum(mUDTGlobleData.getCurNum());
                newCurInfo.setId(dtID);
                newCurInfo.setSchedulDate(mUDTGlobleData.getSchedulDate());
                newCurInfo.setName(mUDTGlobleData.getPatientName());
                newCurInfo.setCardType(mUDTGlobleData.getCardType());
                newCurInfo.setCard(mUDTGlobleData.getPatientCardNum());
                newCurInfo.setMobile(mUDTGlobleData.getPatientMobile());
                newCurInfo.setAge(mUDTGlobleData.getPatientAge());
                newCurInfo.setGender(mUDTGlobleData.getPatientSex());
                newCurInfo.setHeight(mUDTGlobleData.getPatientHeight());
                newCurInfo.setWeight(mUDTGlobleData.getPatientWeight());
                newCurInfo.setToBeSolved(mUDTGlobleData.getProblem());
                newCurInfo.setCheck(mUDTGlobleData.getAssCheckUrl());
                newCurInfo.setAbstract(mUDTGlobleData.getPatientChief());
                newCurInfo.setInspection(mUDTGlobleData.getPhysical());
                newCurInfo.setToUDT(true);
                intent.setClass(mContext, PatientConditionActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("newCurInfo", newCurInfo);
                intent.putExtras(mBundle);
                startActivityForResult(intent, UDT_CODE);

            }
        });
        patientAge = (TextView) v.findViewById(R.id.patient_age);
        patientSex = (TextView) v.findViewById(R.id.patient_sex_txt);
        patientHigh = (TextView) v.findViewById(R.id.patient_height_txt);
        patientWeight = (TextView) v.findViewById(R.id.patient_weight_txt);
        mainContent = (TextView) v.findViewById(R.id.main_appeal_txt);
        bodyChecked = (TextView) v.findViewById(R.id.body_check_txt);

        //辅助检查
        tvExam = (TextView) v.findViewById(R.id.tv_exam);
        layout = (LinearLayout) v.findViewById(R.id.ll_examHelp);

        //目前待解决问题
        tvWaitRePro = (TextView) v.findViewById(R.id.tv_waitResproblems);

        executeBtn = (Button) v.findViewById(R.id.udt_execute_btn);
        executeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickButtonState();
            }
        });
        dtOpinionRefreshBtn = (Button) v.findViewById(R.id.opinion_refresh_btn);
        dtOpinionSubmitBtn = (Button) v.findViewById(R.id.dt_opinion_submit);

        dtOpinionSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLocalAdv) {
                    submitLocalDTOpinion();
                } else {
                    submitTransferDTOpinion();
                }
            }
        });
        scheduleTimeTxt = (TextView) v.findViewById(R.id.response_dt_time_txt);
        scheduleTimeSeconds = (TextView) v.findViewById(R.id.respone_dt_timequantum);
        curDTStateTxt = (TextView) v.findViewById(R.id.cur_dt_state_txt);
        reserveNumTxt = (TextView) v.findViewById(R.id.reserve_num_txt);
        auxiliaryExamLayout = (LinearLayout) v.findViewById(R.id.auxiliary_exam_layout);
        submitOpinionLayout = (LinearLayout) v.findViewById(R.id.udt_opinion_layout);
        //本地治疗
        Re_localTreat = (FrameLayout) v.findViewById(R.id.Re_localTreat);
        edt_local = (EditText) v.findViewById(R.id.udt_room_edit_txt);
//        edt_local.setFilters(
//                new InputFilter[]{inputFilter, new InputFilter.LengthFilter(500)});
        tv_local = (TextView) v.findViewById(R.id.tv_local_advice);
        local = (TextView) v.findViewById(R.id.tv_check_local);
        local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Re_localTreat.setVisibility(View.VISIBLE);
                ll_transfer.setVisibility(View.GONE);
                isLocalAdv = true;
                exchange.setTextColor(getResources().getColor(R.color.no_choice));
                local.setTextColor(getResources().getColor(R.color.choice));
                local_treat.setChecked(true);
                transfer_treat.setChecked(false);
            }
        });

        //转诊治疗
        Re_tranSchedul = (RelativeLayout) v.findViewById(R.id.Re_tranSchedul);
        Re_tranDep = (RelativeLayout) v.findViewById(R.id.Re_tranDep);
        exchange = (TextView) v.findViewById(R.id.tv_exchange);
        exchange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Re_localTreat.setVisibility(View.GONE);
                ll_transfer.setVisibility(View.VISIBLE);
                isLocalAdv = false;
                local.setTextColor(getResources().getColor(R.color.no_choice));
                exchange.setTextColor(getResources().getColor(R.color.choice));
                local_treat.setChecked(false);
                transfer_treat.setChecked(true);
            }
        });
        ll_transfer = (LinearLayout) v.findViewById(R.id.ll_transfer);
        tv_office = (TextView) v.findViewById(R.id.tv_appointment_office);
        btn_select_office = (RelativeLayout) v.findViewById(R.id.btn_select_office);
        transfer_date = (TextView) v.findViewById(R.id.tv_appointment_date);
        btn_transfer_date = (RelativeLayout) v.findViewById(R.id.btn_transfer_date);
        tv_transfer_moment = (TextView) v.findViewById(R.id.tv_transfer_moment);
        tv_transfer_doctor = (TextView) v.findViewById(R.id.tv_transfer_doctor);
        Edit_transfer = (EditText) v.findViewById(R.id.transfer_advices);
//        Edit_transfer.setFilters(
//                new InputFilter[]{inputFilter, new InputFilter.LengthFilter(500)});
        Text_TransferAdv = (TextView) v.findViewById(R.id.tv_transfer_advices);
        btn_select_office.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTranOffSelDialog();
            }
        });
        //转诊日期...
        btn_transfer_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(tv_office.getText().toString())) {
                    CustomToast.show(mContext, getString(R.string.udt_please_select_office), Toast.LENGTH_SHORT);
                } else {
                    showArrangementDialog();
                }
            }
        });

        //checkBox
        ll_checkboxGroup = (LinearLayout) v.findViewById(R.id.ll_checkboxGroup);
        local_treat = (CheckBox) v.findViewById(R.id.local_treat);
        transfer_treat = (CheckBox) v.findViewById(R.id.transfer_treat);

        local_treat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Re_localTreat.setVisibility(View.VISIBLE);
                    ll_transfer.setVisibility(View.GONE);
                    isLocalAdv = true;
                    transfer_treat.setChecked(false);
                    local.setTextColor(getResources().getColor(R.color.choice));
                    exchange.setTextColor(getResources().getColor(R.color.no_choice));
                }
            }
        });
        transfer_treat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Re_localTreat.setVisibility(View.GONE);
                    ll_transfer.setVisibility(View.VISIBLE);
                    isLocalAdv = false;
                    local_treat.setChecked(false);
                    exchange.setTextColor(getResources().getColor(R.color.choice));
                    local.setTextColor(getResources().getColor(R.color.no_choice));
                }
            }
        });
        btn_transfer = (LinearLayout) v.findViewById(R.id.btn_transfer);
        btn_transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferId = mUDTGlobleData.getTransferId();
                Intent intent = new Intent(mContext, ReferralActivity.class);
                intent.putExtra(REFERRAL, transferId);
                startActivity(intent);
            }
        });

    }


    private void obtainTreatState() {
        int result = mUDTGlobleData.getDTResult();
        /**
         * 获取诊疗结论，目前包含两种结论：1（本地治疗）；2（转诊）
         * 0：无结论，转诊未结束
         * 1：本地治疗
         * 2：转诊
         *
         */
        switch (result) {
            case 0:
                CustomLog.i(TAG, "还未确定是本地还是转诊");
                hideTranBtnFath();
                hideCommentView();
                break;
            case 1:
                CustomLog.i(TAG, "本地诊疗");
                local_treat.setChecked(true);
                hideSubmitBtn();
                showCheckFath();
                showLocalFath();
                hideTranCheck();
                hideTranBtnFath();

                break;
            case 2:
                CustomLog.i(TAG, "转诊");
                transfer_treat.setChecked(true);
                transfer_treat.setVisibility(View.VISIBLE);
                hideLocalCheck();//隐藏本地诊疗checkbox
                hideLocalFath();//隐藏本地诊疗父布局
                showCheckFath();//显示转诊checkbox
                showTranFath();//显示转诊父布局
                showTranBtnFath();//显示转诊按钮父布局
                break;
            default:
                break;
        }
    }


    private void hideSubmitBtn() {
        dtOpinionSubmitBtn.setVisibility(View.INVISIBLE);
    }


    /**
     * 请求转诊排班信息数据
     */
    private void requestTranData(String time) {
        showLoadingView(getString(R.string.udt_loading));
        HPUGetTfScheduls tfScheduls = new HPUGetTfScheduls() {
            @Override
            protected void onSuccess(TFSchedInfo responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.i(TAG, "requestTranData::成功");

                departInfoList = new ArrayList<>();//门诊集合
                rangeInfoList = new ArrayList<>();//时间段集合
                doctorInfoList = new ArrayList<>();//医生集合

                departInfoList.clear();
                rangeInfoList.clear();
                doctorInfoList.clear();

                if (responseContent != null) {
                    mResponseContent = responseContent;

                    departInfoList = mResponseContent.getSectionInfos();//门诊

                    departAdapter = new ArrayList<String>();
                    departIdList = new ArrayList<>();
                    if (departInfoList.size() > 0) {
                        for (int i = 0; i < departInfoList.size(); i++) {
                            departAdapter.add(departInfoList.get(i).getName());
                            departIdList.add(departInfoList.get(i).getId());
                        }
                        departLoopView.setItems(departAdapter);
                        departLoopView.setInitPosition(0);
                        selectedDepartName = departInfoList.get(0).getName();
                        selectedDepartId = departInfoList.get(0).getId();
                    } else {
                        tv_loading.setText(mContext.getString(R.string.udt_rangingNews));
                        return;
                    }
                    CustomLog.i(TAG, "departInfoList::size" + departInfoList.size());
                    updataRangeData(departInfoList.get(0).getId());
                    updateSchedulUI(true);
                }


            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(mContext, statusInfo, Toast.LENGTH_SHORT);
                }
                CustomLog.i(TAG, "requestTranData::失败");
            }
        };
        tfScheduls.getscheduls(token, depId, time);
    }


    private void updataRangeData(String schDepID) {

        rangeInfoList = mResponseContent.getRangeTypesById(schDepID);//时间段信息
        rangeAdapter = new ArrayList<String>();
        rangIdList = new ArrayList<String>();
        rangFlagList = new ArrayList<String>();
        if (rangeInfoList.size() > 0) {
            for (int b = 0; b < rangeInfoList.size(); b++) {
                rangeAdapter.add(rangeInfoList.get(b).getRangeName());
                rangIdList.add(rangeInfoList.get(b).getId());
                rangFlagList.add(rangeInfoList.get(b).getEnableFlg());

            }

            rangeLoopView.setItems(rangeAdapter);
            rangeLoopView.setInitPosition(0);
            selectedRangeName = rangeInfoList.get(0).getRangeName();
            selectedRangeId = rangeInfoList.get(0).getId();
            selectedEnableFlag = rangeInfoList.get(0).getEnableFlg();
            upDoctorInfo(selectedRangeId);
        }
    }


    private void upDoctorInfo(String rangeId) {
        doctorInfoList = mResponseContent.getDoctorInfosById(rangeId);//医生信息
        doctorAdpater = new ArrayList<String>();
        docterIdList = new ArrayList<String>();
        if (doctorInfoList.size() > 0) {
            for (int c = 0; c < doctorInfoList.size(); c++) {
                doctorAdpater.add(doctorInfoList.get(c).getName());
                docterIdList.add(doctorInfoList.get(c).getId());

            }
            doctorLoopView.setItems(doctorAdpater);
            doctorLoopView.setInitPosition(0);
            selectedDoctorName = doctorInfoList.get(0).getName();
            selectedDoctorId = doctorInfoList.get(0).getId();
        }
    }


    /**
     * 转诊科室选择Dialog
     */
    private void showTranOffSelDialog() {
        showLoadingView("");
        HPUGetTransferDeps deps = new HPUGetTransferDeps() {
            @Override
            protected void onSuccess(List<TFdepInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.i(TAG, "HPUGetTransferDeps::onSuccess");
                CustomLog.i(TAG, "size:" + responseContent.size());
                if (responseContent != null && responseContent.size() > 0) {
                    Message msg = new Message();
                    msg.what = MSG_DEPS;
                    msg.obj = responseContent;
                    handler.sendMessage(msg);
                    transfer_date.setText("");
                    tv_transfer_moment.setText("");
                    tv_transfer_doctor.setText("");
                } else {
                    CustomToast.show(mContext, getString(R.string.udt_nooffice_selected), Toast.LENGTH_SHORT);
                }
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(mContext, statusInfo, Toast.LENGTH_SHORT);
                }
                CustomLog.i(TAG, "HPUGetTransferDeps::onFail");
            }
        };
        deps.getdeps(token, dtID);

    }

    public String getWeek(String sdate) {
        // 再转换为时间
        Date date = strToDate(sdate);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // int hour=c.get(Calendar.DAY_OF_WEEK);
        // hour中存的就是星期几了，其范围 1~7
        // 1=星期日 7=星期六，其他类推
        return new SimpleDateFormat("EEEE").format(c.getTime());
    }

    public Date strToDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }

    /**
     * 获取系统时间
     **/
    private static String getStringDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    private void onClickButtonState() {

        UDTGlobleData.DOCTOR_TYPE type = mUDTGlobleData.getDoctorType();
        if (type == UDTGlobleData.DOCTOR_TYPE.REQUEST) {//求诊者
            if (mUDTGlobleData.getState() == HPUCommonCode.SEEK_STATE_NOT) {

                //求诊者在待接诊状态下撤销诊疗
                showCancelDialog();
            }
        } else if (type == UDTGlobleData.DOCTOR_TYPE.RESPONSE) {//接诊者
            if (mUDTGlobleData.getState() == HPUCommonCode.SEEK_STATE_NOW) {
                CustomLog.i(TAG, "showButtonState()+接诊方:状态:接诊中");
                moveToAdviceEdit();

            } else {
                if (mUDTGlobleData.getState() == HPUCommonCode.SEEK_STATE_NOT) {
                    //接诊者在待接诊做接诊操作
                    showResponDialog();

                }
            }

        } else if (type == UDTGlobleData.DOCTOR_TYPE.OTHER) {//旁观者
            if (mUDTGlobleData.getState() == HPUCommonCode.SEEK_STATE_NOT) {
                //旁观者在待接诊状态下做接诊操作
                showResponDialog();
            }
        }

    }


    /**
     * 跳转到本地建议编辑框
     */
    private void moveToAdviceEdit() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mScrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    /**
     * 取消预约
     */
    private void showCancelDialog() {
        final String cancelTip = getString(R.string.udt_isCancel_Treatment);
        final CustomDialog dialog = new CustomDialog(mContext);
        dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                final HPUCanelCsl hpuCanelCsl = new HPUCanelCsl() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        removeLoadingView();
                        hideExecuteBtn();
                        CustomToast.show(mContext, getString(R.string.udt_cancelsuccess), Toast.LENGTH_SHORT);
                        udtChatRoomActivity.finish();

                    }


                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        removeLoadingView();
                        CustomLog.i(TAG, "撤销诊疗失败::" + statusCode + statusInfo);
                        if (statusCode == MDS_FAILED_CREATE_GQRCODE) {
                            CustomToast.show(mContext, getString(R.string.udt_only_request), Toast.LENGTH_SHORT);
                        } else if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                            AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                        } else {
                            CustomToast.show(mContext, statusInfo, Toast.LENGTH_SHORT);
                        }
                    }
                };
                showLoadingView(mContext.getString(R.string.wait),
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                removeLoadingView();
                                CustomLog.d(TAG, "取消接受诊疗");
                                hpuCanelCsl.cancel();
                            }
                        }, true);
                hpuCanelCsl.canel(token, dtID);
            }
        });
        dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                CustomLog.d(TAG, "setCancelBtnOnClickListener");
                dialog.cancel();
            }
        });
        dialog.setTip(cancelTip);
        dialog.setOkBtnText(getString(R.string.udt_cancel_appoint));
        dialog.setCancelBtnText(getString(R.string.udt_keep_appointment));
        dialog.show();
    }


    /**
     * 接诊时弹出的dialog
     **/
    private void showResponDialog() {
        final String tip = mContext.getString(R.string.you_will_response_dt) + "\n" +
                mUDTGlobleData.getRequestHosp() +
                mUDTGlobleData.getRequestDep() + mContext.getString(R.string.patient) +
                mUDTGlobleData.getPatientName() +
                mContext.getString(R.string.udt_whether_response);
        final CustomDialog dialog = new CustomDialog(mContext);
        dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                final HPUAcceptCsl acceptCsl = new HPUAcceptCsl() {
                    @Override
                    protected void onSuccess(JSONObject responseContent) {
                        super.onSuccess(responseContent);
                        CustomLog.i(TAG, "接诊成功");
                        CustomToast.show(mContext, getString(R.string.udt_response_success), Toast.LENGTH_SHORT);
                        removeLoadingView();
                        if (TextUtils.isEmpty(mUDTGlobleData.getAdvice())) {
                            executeBtn.setText(mContext.getString(R.string.udt_treat_advice));
                        } else {
                            hideExecuteBtn();
                        }

                        MedicalApplication.getFileTaskManager().sendChangeDtStateMsg(0,
                                AccountManager.getInstance(mContext).getNube(),
                                mUDTGlobleData.getRequestNubeNumber(),
                                dtID,
                                AccountManager.getInstance(mContext).getAccountInfo().headThumUrl,
                                AccountManager.getInstance(mContext).getName());
                        requestData();

                    }


                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        removeLoadingView();
                        CustomLog.i(TAG, "接诊失败" + statusCode);
                        if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                            AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                        } else if (statusCode == MDSErrorCode.MDS_GROUP_NOTICE_EMPTY) {
                            final CustomDialog dtCancelDialog = new CustomDialog(mContext);
                            dtCancelDialog.removeCancelBtn();
                            dtCancelDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                                @Override
                                public void onClick(CustomDialog customDialog) {
                                    CustomLog.d(TAG, "dtCancelDialog setOkBtnOnClickListener");
                                    dtCancelDialog.dismiss();

                                }
                            });
                            dtCancelDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                            dtCancelDialog.setTip(mContext.getString(R.string.dt_have_cancle));
                            dtCancelDialog.show();
                        } else if (statusCode == MDSErrorCode.MDS_OFFACCID_IS_FALSE) {
                            final CustomDialog canNotResponseDialog = new CustomDialog(mContext);
                            canNotResponseDialog.removeCancelBtn();
                            canNotResponseDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                                @Override
                                public void onClick(CustomDialog customDialog) {
                                    CustomLog.d(TAG, "canNotResponseDialog setOkBtnOnClickListener");
                                    canNotResponseDialog.dismiss();
                                }
                            });
                            canNotResponseDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                            canNotResponseDialog.setTip(mContext.getString(R.string.not_be_scheduled_not_response_dt));
                            canNotResponseDialog.show();
                        } else if (statusCode == MDSErrorCode.MDS_REPEAT_FOUCS_OFFACC) {
                            final CustomDialog canNotRepetitionResponseDialog = new CustomDialog(mContext);
                            canNotRepetitionResponseDialog.removeCancelBtn();
                            canNotRepetitionResponseDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                                @Override
                                public void onClick(CustomDialog customDialog) {
                                    CustomLog.d(TAG, "canNotResponseDialog setOkBtnOnClickListener");
                                    canNotRepetitionResponseDialog.dismiss();

                                }
                            });
                            canNotRepetitionResponseDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                            canNotRepetitionResponseDialog.setTip(mContext.getString(R.string.not_repetition_response_dt));
                            canNotRepetitionResponseDialog.show();
                        } else if (statusCode == MDS_ACCOUNT_IS_EXISTED) {
                            final CustomDialog canNotResponseDialog = new CustomDialog(mContext);
                            canNotResponseDialog.removeCancelBtn();
                            canNotResponseDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                                @Override
                                public void onClick(CustomDialog customDialog) {
                                    CustomLog.d(TAG, "canNotResponseDialog setOkBtnOnClickListener");
                                    canNotResponseDialog.dismiss();
                                }
                            });
                            canNotResponseDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                            canNotResponseDialog.setTip(getString(R.string.udt_no_work));
                            canNotResponseDialog.show();
                        } else {
                            CustomToast.show(mContext, getString(R.string.udt_request_failed), Toast.LENGTH_SHORT);
                        }

                    }
                };
                showLoadingView(mContext.getString(R.string.wait),
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                removeLoadingView();
                                CustomLog.d(TAG, getString(R.string.udt_cancel_reTreat));
                                acceptCsl.cancel();
                            }
                        }, true);
                dialog.cancel();
                acceptCsl.accept(token, dtID);
            }
        });
        dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {

                CustomLog.d(TAG, "setCancelBtnOnClickListener");
                dialog.cancel();
            }
        });
        dialog.setTip(tip);
        dialog.setOkBtnText(mContext.getString(R.string.confirm_dt));
        dialog.setCancelBtnText(mContext.getString(R.string.cancel));
        dialog.show();
    }


    private void hideExecuteBtn() {
        executeBtn.setVisibility(View.INVISIBLE);
    }


    private void updateUI() {
        //辅助检查
        String url[] = mUDTGlobleData.getAssCheckUrl().split(",");
        if (mUDTGlobleData.getAssCheckUrl() != null) {
            if (mUDTGlobleData.getAssCheckUrl().equals("")) {
                tvExam.setVisibility(View.INVISIBLE);
                layout.setVisibility(View.GONE);
            } else {
                tvExam.setVisibility(View.VISIBLE);
                layout.setVisibility(View.VISIBLE);
                contactList.clear();
                for (int i = 0; i < url.length; i++) {
                    Contact contact = new Contact();
                    contact.setHeadUrl(url[i]);
                    CustomLog.d(TAG, "第" + i + "张" + url[i]);
                    contactList.add(contact);
                }
                adapterList = new ImagePagerAdapterList(mContext, contactList, IMAGE_LIST_COLUMN, 1, false, null);
                adapterList.disableLongClick();

                viewPager.setAdapter(adapterList);
            }
        }


        obtainTreatState();
        if (TextUtils.isEmpty(mUDTGlobleData.getProblem())) {
            tvWaitRePro.setText(mContext.getString(R.string.udt_no_resolve_problem));
            tvWaitRePro.setTextColor(getResources().getColor(R.color.gray));
        } else {
            tvWaitRePro.setText(mUDTGlobleData.getProblem());
            tvWaitRePro.setTextColor(getResources().getColor(R.color.comment_color));
        }
        reserveNumTxt.setText(mUDTGlobleData.getCurNum() + "号");
        String time = mUDTGlobleData.getSchedulDate();
        if (time.equals(getStringDateShort())) {
            scheduleTimeTxt.setText(mContext.getString(R.string.udt_today));
        } else {
            String month = time.substring(4, 6) + mContext.getString(R.string.udt_month) + time.substring(6, 8) + mContext.getString(R.string.udt_day);
            scheduleTimeTxt.setText(month);
        }
        scheduleTimeSeconds.setText(mUDTGlobleData.getRangNumber());
        if (TextUtils.isEmpty(mUDTGlobleData.getRequestHeadThumUrl())) {
            imageLoader.displayImage("http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg",
                    reqDocImag,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        } else {
            imageLoader.displayImage(mUDTGlobleData.getRequestHeadThumUrl(),
                    reqDocImag,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }

        reqDocName.setText(mUDTGlobleData.getRequestName());
        reqDocJob.setText(mUDTGlobleData.getRequestProfessional());
        reqDepart.setText(mUDTGlobleData.getRequestDep());
        String hospital = mUDTGlobleData.getRequestHosp();
        reqHospital.setText(hospital);
        if (TextUtils.isEmpty(mUDTGlobleData.getResponseHeadThumUrl())) {
            imageLoader.displayImage("http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg",
                    respDocImag,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        } else {
            imageLoader.displayImage(mUDTGlobleData.getResponseHeadThumUrl(),
                    respDocImag,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }
        if (TextUtils.isEmpty(mUDTGlobleData.getResponseName())) {
            respDocName.setText("等待接诊医生");
            respDocName.setTextColor(getResources().getColor(R.color.comment_color));
            respDocJob.setText("");
        } else {
            respDocName.setTextColor(getResources().getColor(R.color.comment_color));
            respDocName.setText(mUDTGlobleData.getResponseName());
            respDocJob.setText(mUDTGlobleData.getResponseProfessional());
        }
        respDepart.setText(mUDTGlobleData.getResponseDep());
        respHospital.setText(mUDTGlobleData.getResponseHosp());

        patientName.setText(mUDTGlobleData.getPatientName());
        patientIDcard.setText(mUDTGlobleData.getPatientCardNum());
        if (TextUtils.isEmpty(mUDTGlobleData.getPatientMobile())) {
            ll_patientPhoneFath.setVisibility(View.GONE);
        } else {
            ll_patientPhoneFath.setVisibility(View.VISIBLE);
        }
        patientPhone.setText(mUDTGlobleData.getPatientMobile());
        if (TextUtils.isEmpty(mUDTGlobleData.getGuardName())) {
            LL_guardian_name.setVisibility(View.GONE);
        } else {
            LL_guardian_name.setVisibility(View.GONE);
        }
        patientGuardian.setText(mUDTGlobleData.getGuardName());
        if (TextUtils.isEmpty(mUDTGlobleData.getGuardCardNum())) {
            LL_guardian_ID.setVisibility(View.GONE);
        } else {
            LL_guardian_ID.setVisibility(View.GONE);
        }
        guardianIDcard.setText(mUDTGlobleData.getGuardCardNum());
        if (TextUtils.isEmpty(mUDTGlobleData.getGuardMobile())) {
            LL_guardian_phone.setVisibility(View.GONE);
        } else {
            LL_guardian_phone.setVisibility(View.GONE);
        }
        guardianPhone.setText(mUDTGlobleData.getGuardMobile());
        if (TextUtils.isEmpty(mUDTGlobleData.getPatientAge())) {
            Re_patientDetail.setVisibility(View.GONE);
        } else {
            Re_patientDetail.setVisibility(View.VISIBLE);
        }
        patientAge.setText(mUDTGlobleData.getPatientAge());
        patientSex.setText(mUDTGlobleData.getPatientSex());
        patientHigh.setText(mUDTGlobleData.getPatientHeight());
        patientWeight.setText(mUDTGlobleData.getPatientWeight());
        if (TextUtils.isEmpty(mUDTGlobleData.getPatientChief())) {
            mainContent.setText(mContext.getString(R.string.udt_no_main_views));
            mainContent.setTextColor(getResources().getColor(R.color.gray));
        } else {
            mainContent.setTextColor(getResources().getColor(R.color.comment_color));
            mainContent.setText(mUDTGlobleData.getPatientChief());
        }
        if (TextUtils.isEmpty(mUDTGlobleData.getPhysical())) {
            bodyChecked.setText(mContext.getString(R.string.udt_no_exambody_news));
            bodyChecked.setTextColor(getResources().getColor(R.color.gray));
        } else {
            bodyChecked.setTextColor(getResources().getColor(R.color.comment_color));
            bodyChecked.setText(mUDTGlobleData.getPhysical());
        }


        //判断角色
        UDTGlobleData.DOCTOR_TYPE type = mUDTGlobleData.getDoctorType();
        if (type == UDTGlobleData.DOCTOR_TYPE.REQUEST) {//求诊者
            CustomLog.i(TAG, "求诊者进入");
            hideRespCommentBtn();
            hideCommentView();
            hideSubmitBtn();
            hideRefreshButton();
            reqeustState();
        } else if (type == UDTGlobleData.DOCTOR_TYPE.RESPONSE) {//接诊方
            btn_request.setVisibility(View.INVISIBLE);
            CustomLog.i(TAG, "接诊者进入");
            hideEditButton();
            hideCommentView();
            hideRefreshButton();
            hideRefreshButton();
            responseState();

        } else if (type == UDTGlobleData.DOCTOR_TYPE.OTHER) {//旁观者
            CustomLog.i(TAG, "旁观者进入");
            hideCommentView();
            hideRefreshButton();
            hideEditButton();
            hideSubmitBtn();
            hideTranBtnFath();
            observeState();
        }
    }


    //求诊方状态
    private void reqeustState() {
        switch (mUDTGlobleData.getState()) {
            case HPUCommonCode.SEEK_STATE_NOW://接诊中
                CustomLog.i(TAG, "求诊方:状态:接诊中");
                curDTStateTxt.setText(mContext.getString(R.string.udt_responing));
                hideExecuteBtn();
                hideRefreshButton();
                hideEditButton();

                dtOpinionRefreshBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestData();
                    }
                });
                if (mUDTGlobleData.getDTResult() == 0) {
                    hideCheckFath();
                    hideTranFath();
                    showLocalFath();
                    hideLocalEdit();
                    showLocalText();
                    tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                    tv_local.setTextColor(getResources().getColor(R.color.gray));
                }
                if (mUDTGlobleData.getDTResult() == 1) {
                    hideTranFath();
                    hideLocalEdit();
                    showLocalText();
                    hideTranCheck();
                    local_treat.setChecked(true);
                    if (TextUtils.isEmpty(mUDTGlobleData.getAdvice())) {
                        tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                        tv_local.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        local_treat.setChecked(true);
                    }
                }
                if (mUDTGlobleData.getDTResult() == 2) {
                    hideLocalFath();
                    showTranFath();
                    hideTranEdit();
                    showTranText();
                    hideToRightView();
                    showTransferNews();
                    showTranBtnFath();
                    grayView.setVisibility(View.GONE);
                    transfer_treat.setChecked(true);
                    if (TextUtils.isEmpty(mUDTGlobleData.getTransferAdvice())) {
                        Text_TransferAdv.setText(mContext.getString(R.string.udt_now_no_advices));
                        tv_local.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        transfer_treat.setChecked(true);
                    }
                }
                break;
            case HPUCommonCode.SEEK_STATE_NOT://待接诊
                CustomLog.i(TAG, "求诊方:状态:待接诊");
                showEditButton();
                local_treat.setChecked(true);
                executeBtn.setText(mContext.getString(R.string.udt_cancel_app));
                curDTStateTxt.setText(R.string.udt_wait_treat);
                hideTranFath();
                hideCheckFath();
                hideTranText();
                hideLocalEdit();
                showLocalFath();
                tv_local.setVisibility(View.VISIBLE);
                tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                tv_local.setTextColor(getResources().getColor(R.color.gray));

                break;
            case HPUCommonCode.SEEK_STATE_END://接诊结束
                CustomLog.i(TAG, "求诊方:状态:已结束");
                showCommentView();
                isShowReqBtn();
                showResComment();
                hideExecuteBtn();
                hideEditButton();
                if (mUDTGlobleData.getDTResult() == 1) {
                    hideTranFath();
                    hideLocalEdit();
                    showLocalText();
                    hideTranCheck();
                    local_treat.setChecked(true);
                    if (TextUtils.isEmpty(mUDTGlobleData.getAdvice())) {
                        tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                        tv_local.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        local_treat.setChecked(true);
                    }
                }
                if (mUDTGlobleData.getDTResult() == 2) {
                    hideLocalFath();
                    showTranFath();
                    hideTranEdit();
                    showTranText();
                    showTransferNews();
                    hideToRightView();
                    grayView.setVisibility(View.GONE);
                    if (TextUtils.isEmpty(mUDTGlobleData.getTransferAdvice())) {
                        Text_TransferAdv.setText(mContext.getString(R.string.udt_now_no_advices));
                        Text_TransferAdv.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        transfer_treat.setChecked(true);
                    }
                }
                curDTStateTxt.setText(mContext.getString(R.string.udt_end));
                break;
            default:
                break;
        }
    }

    private void hideToRightView() {
        btn_select_office.setClickable(false);
        btn_transfer_date.setClickable(false);
        im_trDep.setVisibility(View.INVISIBLE);
        imag_TraSched.setVisibility(View.INVISIBLE);
    }

    private void showTransferNews() {
        if (mUDTGlobleData.getTransferSectionType() == 1) {
            tv_office.setText(mUDTGlobleData.getTransferDept() + getString(R.string.udt_normal_depart));
        } else if (mUDTGlobleData.getTransferSectionType() == 2) {
            tv_office.setText(mUDTGlobleData.getTransferDept() + getString(R.string.udt_high_depart));
        } else if (mUDTGlobleData.getTransferSectionType() == 3) {
            tv_office.setText(mUDTGlobleData.getTransferDept() + getString(R.string.udt_profess_depart));
            tv_transfer_doctor.setText(mUDTGlobleData.getTransferDoctorName());
        }
        tv_transfer_moment.setText(mUDTGlobleData.getTransferRange());
        String a = mUDTGlobleData.getTransferScheduDate();
        if (!TextUtils.isEmpty(a)){
        transfer_date.setText(a.substring(4, 6) + mContext.getString(R.string.udt_month) + mUDTGlobleData.getTransferScheduDate().substring(6, 8) + mContext.getString(R.string.udt_day));
        }
        transfer_treat.setChecked(true);

    }


    private void showResComment() {
        if (mUDTGlobleData.isRespoonseEvaluate()) {
            need_RatingBar.setVisibility(View.VISIBLE);
            btn_response.setVisibility(View.INVISIBLE);
            if (TextUtils.isEmpty(mUDTGlobleData.getResponseReview())) {
                tv_needComment.setText("未评价");
            } else {
                tv_needComment.setText(mUDTGlobleData.getResponseName() + ":" + mUDTGlobleData.getResponseReview());
                tv_needComment.setTextColor(getResources().getColor(R.color.comment_color));
            }
            need_RatingBar.setCountSelected(mUDTGlobleData.getResponseScore());
        } else {
            need_RatingBar.setVisibility(View.INVISIBLE);

        }
    }


    //接诊方状态
    private void responseState() {
        switch (mUDTGlobleData.getState()) {
            case HPUCommonCode.SEEK_STATE_NOW://接诊中
                CustomLog.i(TAG, "接诊方:接诊中");
                curDTStateTxt.setText(mContext.getString(R.string.udt_responing_now));
                if (mUDTGlobleData.getDTResult() == 0) {
                    showExecteBtn();
                    local_treat.setChecked(true);
                    showSubmitButton();
                    executeBtn.setBackgroundResource(R.drawable.button_selector);
                    executeBtn.setText(mContext.getString(R.string.udt_treatment_advice));
                    showCheckFath();
                    hideTranFath();
                    showLocalEdit();
                    hideLocalText();
                }

                //转诊
                if (mUDTGlobleData.getDTResult() == 2) {

                    hideSubmitBtn();
                    hideExecuteBtn();
                    hideTranEdit();
                    showTranText();
                    hideToRightView();
                    showTransferNews();
                    showTranBtnFath();
                    grayView.setVisibility(View.GONE);
                }
                if (mUDTGlobleData.getDTResult() == 1) {

                    hideExecuteBtn();
                    hideLocalEdit();
                    showLocalText();
                    hideSubmitBtn();
                }
                break;
            case HPUCommonCode.SEEK_STATE_NOT://待接诊
                CustomLog.i(TAG, "接诊方:待接诊");
                if (Integer.parseInt(time) == Integer.parseInt(mUDTGlobleData.getSchedulDate()) || Integer.parseInt(mUDTGlobleData.getSchedulDate()) < Integer.parseInt(time)) {
                    executeBtn.setText(mContext.getString(R.string.udt_resp_treat));
                    executeBtn.setBackgroundResource(R.drawable.response_shape);
                } else {
                    hideExecuteBtn();
                }
                curDTStateTxt.setText(mContext.getString(R.string.udt_wait_treat));
                break;
            case HPUCommonCode.SEEK_STATE_END://结束诊疗
                CustomLog.i(TAG, "接诊方:结束诊疗");
                showCommentView();
                isShowResBtn();
                showReqComment();
                hideExecuteBtn();
                curDTStateTxt.setText(mContext.getString(R.string.ended));
                //转诊
                if (mUDTGlobleData.getDTResult() == 2) {

                    hideSubmitBtn();
                    hideExecuteBtn();
                    hideTranEdit();
                    showTranText();
                    showTransferNews();
                    hideToRightView();

                    grayView.setVisibility(View.GONE);
                }
                if (mUDTGlobleData.getDTResult() == 1) {

                    hideExecuteBtn();
                    hideLocalEdit();
                    showLocalText();
                    hideSubmitBtn();
                }
                break;
            default:
                break;
        }
    }


    private void showReqComment() {
        if (mUDTGlobleData.isRequestEvaluate()) {
            help_RatingBar.setVisibility(View.VISIBLE);
            btn_request.setVisibility(View.INVISIBLE);
            if (TextUtils.isEmpty(mUDTGlobleData.getRequestReview())) {
                tv_helpComment.setText("未评价");
            } else {
                tv_helpComment.setText(mUDTGlobleData.getRequestName() + ":" + mUDTGlobleData.getRequestReview());
                tv_helpComment.setTextColor(getResources().getColor(R.color.comment_color));
            }

            help_RatingBar.setCountSelected(mUDTGlobleData.getRequestScore());
        } else {
            help_RatingBar.setVisibility(View.INVISIBLE);

        }
    }


    /**
     * 旁观者状态
     */

    private void observeState() {
        switch (mUDTGlobleData.getState()) {
            case HPUCommonCode.SEEK_STATE_NOW://接诊中
                curDTStateTxt.setText(mContext.getString(R.string.responsing_dt));
                hideExecuteBtn();
                hideRefreshButton();
                dtOpinionRefreshBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestData();
                    }
                });
                if (mUDTGlobleData.getDTResult() == 0) {
                    hideTranChoiceDS();
                    showLocalFath();
                    hideTranFath();
                    hideLocalEdit();
                    showLocalText();
                    hideCheckFath();
                    tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                    tv_local.setTextColor(getResources().getColor(R.color.gray));

                }
                if (mUDTGlobleData.getDTResult() == 1) {
                    hideTranFath();
                    hideLocalEdit();
                    showLocalText();
                    hideTranCheck();
                    local_treat.setChecked(true);
                    if (TextUtils.isEmpty(mUDTGlobleData.getAdvice())) {
                        tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                        tv_local.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        local_treat.setChecked(true);
                    }
                }
                if (mUDTGlobleData.getDTResult() == 2) {
                    hideLocalFath();
                    showTranFath();
                    hideTranEdit();
                    showTranText();
                    hideToRightView();
                    showTransferNews();
                    showTranBtnFath();
                    grayView.setVisibility(View.GONE);
                    transfer_treat.setChecked(true);
                    if (TextUtils.isEmpty(mUDTGlobleData.getTransferAdvice())) {
                        Text_TransferAdv.setText(mContext.getString(R.string.udt_now_no_advices));
                        Text_TransferAdv.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        transfer_treat.setChecked(true);
                    }
                }
                break;
            case HPUCommonCode.SEEK_STATE_NOT://待接诊
                CustomLog.i(TAG, "旁观者:诊疗状态:待接诊");
                showExecteBtn();
                if (Integer.parseInt(time) == Integer.parseInt(mUDTGlobleData.getSchedulDate()) || Integer.parseInt(mUDTGlobleData.getSchedulDate()) < Integer.parseInt(time)) {
                    executeBtn.setText(mContext.getString(R.string.udt_resp_treat));
                } else {
                    hideExecuteBtn();
                }
                executeBtn.setBackgroundResource(R.drawable.response_shape);
                curDTStateTxt.setText(mContext.getString(R.string.wait_response_dt));
                hideTranFath();
                showLocalFath();
                hideCheckFath();
                hideTranText();
                hideLocalEdit();
                showLocalText();
                tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                tv_local.setTextColor(getResources().getColor(R.color.gray));
                break;
            case HPUCommonCode.SEEK_STATE_END:// 已结束接诊
                CustomLog.i(TAG, "旁观者:诊疗状态:已结束");
                hideCommentBtn();
                hideExecuteBtn();
                showReqComment();
                showResComment();
                if (mUDTGlobleData.getDTResult() == 1) {
                    hideTranFath();
                    hideLocalEdit();
                    showLocalText();
                    hideTranCheck();
                    local_treat.setChecked(true);
                    if (TextUtils.isEmpty(mUDTGlobleData.getAdvice())) {
                        tv_local.setText(mContext.getString(R.string.udt_now_no_advices));
                        tv_local.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        local_treat.setChecked(true);
                    }
                }
                if (mUDTGlobleData.getDTResult() == 2) {
                    hideLocalFath();
                    showTranFath();
                    hideTranEdit();
                    showTranText();
                    showTransferNews();
                    showTranBtnFath();
                    grayView.setVisibility(View.GONE);
                    transfer_treat.setChecked(true);
                    hideToRightView();

                    if (TextUtils.isEmpty(mUDTGlobleData.getTransferAdvice())) {
                        Text_TransferAdv.setText(mContext.getString(R.string.udt_now_no_advices));
                        Text_TransferAdv.setTextColor(getResources().getColor(R.color.gray));
                    } else {
                        transfer_treat.setChecked(true);
                    }
                }
                curDTStateTxt.setText(mContext.getString(R.string.ended));
                break;
            default:
                break;
        }
    }


    private void showExecteBtn() {
        executeBtn.setVisibility(View.VISIBLE);
    }


    /**
     * 辅助检查图片显示
     **/


    //显示本地治疗编辑
    private void showLocalEdit() {
        edt_local.setVisibility(View.VISIBLE);
    }


    //隐藏本地治疗编辑
    private void hideLocalEdit() {
        edt_local.setVisibility(View.INVISIBLE);
    }


    //显示本地建议
    private void showLocalText() {
        tv_local.setVisibility(View.VISIBLE);
        tv_local.setText(mUDTGlobleData.getAdvice());
    }


    //隐藏本地建议
    private void hideLocalText() {
        tv_local.setVisibility(View.INVISIBLE);
    }


    //显示转诊编辑
    private void showTranEdit() {
        Edit_transfer.setVisibility(View.VISIBLE);
    }


    //隐藏转诊编辑
    private void hideTranEdit() {
        Edit_transfer.setVisibility(View.INVISIBLE);
    }


    //显示转诊建议
    private void showTranText() {
        Text_TransferAdv.setVisibility(View.VISIBLE);
        CustomLog.i(TAG, "转诊建议" + mUDTGlobleData.getTransferAdvice());
        Text_TransferAdv.setText(mUDTGlobleData.getTransferAdvice());
    }


    //隐藏转诊建议
    private void hideTranText() {
        Text_TransferAdv.setVisibility(View.INVISIBLE);
    }


    //显示本地父布局
    private void showLocalFath() {
        Re_localTreat.setVisibility(View.VISIBLE);
    }


    //隐藏转诊选择布局
    private void hideTranChoiceDS() {
        Re_tranSchedul.setVisibility(View.GONE);
        Re_tranDep.setVisibility(View.GONE);
    }


    //隐藏本地父布局
    private void hideLocalFath() {
        Re_localTreat.setVisibility(View.GONE);
    }


    //显示转诊父布局
    private void showTranFath() {
        ll_transfer.setVisibility(View.VISIBLE);

    }


    //隐藏转诊父布局
    private void hideTranFath() {
        ll_transfer.setVisibility(View.GONE);
    }


    //显示checkBox父布局
    private void showCheckFath() {
        ll_checkboxGroup.setVisibility(View.VISIBLE);
    }


    //隐藏checkBox父布局
    private void hideCheckFath() {
        ll_checkboxGroup.setVisibility(View.GONE);
    }


    //显示本地治疗check
    private void showLocalCheck() {
        local_treat.setVisibility(View.VISIBLE);
        local.setVisibility(View.VISIBLE);
    }


    //隐藏本地治疗check
    private void hideLocalCheck() {
        local_treat.setVisibility(View.GONE);
        local.setVisibility(View.GONE);
    }


    //隐藏转诊治疗check
    private void hideTranCheck() {
        transfer_treat.setVisibility(View.GONE);
        exchange.setVisibility(View.GONE);
    }


    //显示转诊治疗check
    private void showTranCheck() {
        transfer_treat.setVisibility(View.VISIBLE);
        exchange.setVisibility(View.VISIBLE);
    }


    //显示转诊按钮父布局
    private void showTranBtnFath() {
        btn_transfer.setVisibility(View.VISIBLE);
    }


    //隐藏转诊按钮父布局
    private void hideTranBtnFath() {
        btn_transfer.setVisibility(View.GONE);
    }


    //显示接诊方评价按钮
    private void showRespCommentBtn() {
        btn_response.setVisibility(View.VISIBLE);
    }


    //隐藏接诊方评价按钮
    private void hideRespCommentBtn() {
        btn_response.setVisibility(View.INVISIBLE);
    }


    //显示求诊方评价按钮
    private void showReqCommentBtn() {
        btn_request.setVisibility(View.VISIBLE);
    }


    //隐藏求诊方评价按钮
    private void hideReqCommentBtn() {
        btn_request.setVisibility(View.INVISIBLE);
    }


    /***
     * 旁观者
     * 接诊诊方
     * 隐藏编辑按钮
     */
    private void hideEditButton() {
        editButton.setVisibility(View.INVISIBLE);
    }


    /****
     * 求诊方显示编辑按钮
     */

    private void showEditButton() {
        editButton.setVisibility(View.VISIBLE);
    }


    //显示诊疗建议提交按钮
    private void showSubmitButton() {
        dtOpinionSubmitBtn.setVisibility(View.VISIBLE);
    }


    /***
     * 提交转诊诊疗建议
     */
    private void submitTransferDTOpinion() {
        String opinion = Edit_transfer.getText().toString();
        if (TextUtils.isEmpty(opinion) || opinion.equals(" ")) {
            CustomToast.show(mContext, getString(R.string.udt_hint_advice_now), Toast.LENGTH_SHORT);
        } else if (TextUtils.isEmpty(transfer_date.getText().toString()) || TextUtils.isEmpty(tv_office.getText().toString())) {
            CustomToast.show(mContext, getString(R.string.udt_please_finish_news), Toast.LENGTH_SHORT);
        } else {
            showLoadingView(getString(R.string.udt_uploading));
            HPUCreateTftm hpuCreateTftm = new HPUCreateTftm() {
                @Override
                protected void onSuccess(TFCommonInfo responseContent) {
                    super.onSuccess(responseContent);
                    removeLoadingView();
                    CustomLog.i(TAG, "创建转诊成功");
                    CustomToast.show(mContext, getString(R.string.udt_transfer_treat_success), Toast.LENGTH_SHORT);
                    hideSubmitBtn();
                    if (responseContent != null) {
                        transferId = responseContent.getTfId();
                    }
                    MedicalApplication.getFileTaskManager().sendDTResultMsg(1,
                            mUDTGlobleData.getResponseNubeNumber(),
                            mUDTGlobleData.getRequestNubeNumber(),
                            dtID,
                            transferId);
                    requestData();

                }


                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    removeLoadingView();
                    if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                        AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                    } else {
                        CustomLog.i(TAG, "创建转诊失败:" + statusCode + statusInfo);
                        CustomToast.show(mContext, getString(R.string.udt_creattransfer_falied), Toast.LENGTH_SHORT);
                    }
                }
            };

            hpuCreateTftm.create(token, dtID, schedulId, doctorId, opinion);
        }
    }


    /**
     * 是否显示接诊方评价按钮
     */
    private void isShowResBtn() {
        btn_request.setVisibility(View.GONE);
        if (mUDTGlobleData.isRespoonseEvaluate()) {
            need_RatingBar.setVisibility(View.VISIBLE);
            btn_response.setVisibility(View.INVISIBLE);
            if (TextUtils.isEmpty(mUDTGlobleData.getResponseReview())) {
                tv_needComment.setText("未评价");
                tv_needComment.setTextColor(getResources().getColor(R.color.gray));
            } else {
                tv_needComment.setText(mUDTGlobleData.getResponseName() + ":" + mUDTGlobleData.getResponseReview());
                tv_needComment.setTextColor(getResources().getColor(R.color.comment_color));
            }

            need_RatingBar.setCountSelected(mUDTGlobleData.getResponseScore());
        } else {
            btn_response.setVisibility(View.VISIBLE);
            need_RatingBar.setVisibility(View.INVISIBLE);

        }
    }


    /**
     * 隐藏评价布局
     * 待接诊时和接诊中使用
     */
    private void hideCommentView() {
        ll_doctorComment.setVisibility(View.GONE);
    }


    /***
     * 显示评价布局
     * 接诊结束时使用
     */
    private void showCommentView() {
        ll_doctorComment.setVisibility(View.VISIBLE);
    }


    /**
     * 是否显示求诊方评价按钮
     */
    private void isShowReqBtn() {
        btn_response.setVisibility(View.INVISIBLE);
        if (mUDTGlobleData.isRequestEvaluate()) {
            help_RatingBar.setVisibility(View.VISIBLE);
            btn_request.setVisibility(View.INVISIBLE);
            if (TextUtils.isEmpty(mUDTGlobleData.getRequestReview())) {
                tv_helpComment.setText("未评价");
                tv_helpComment.setTextColor(getResources().getColor(R.color.gray));
            } else {
                tv_helpComment.setText(mUDTGlobleData.getRequestName() + ":" + mUDTGlobleData.getRequestReview());
                tv_helpComment.setTextColor(getResources().getColor(R.color.comment_color));
            }
            help_RatingBar.setCountSelected(mUDTGlobleData.getRequestScore());
        } else {
            btn_request.setVisibility(View.VISIBLE);
            help_RatingBar.setVisibility(View.INVISIBLE);

        }
    }


    /**
     * 隐藏评价按钮 针对 旁观者
     */
    private void hideCommentBtn() {
        ll_doctorComment.setVisibility(View.VISIBLE);
        btn_request.setVisibility(View.INVISIBLE);
        btn_response.setVisibility(View.INVISIBLE);
        need_RatingBar.setVisibility(View.VISIBLE);
        help_RatingBar.setVisibility(View.VISIBLE);
    }


    /**
     * 旁观者/接诊方
     * 隐藏刷新按钮
     */
    private void hideRefreshButton() {
        dtOpinionRefreshBtn.setVisibility(View.INVISIBLE);
    }


    /**
     * 本地医生提交诊疗建议
     */
    private void submitLocalDTOpinion() {

        String dtOpinion = edt_local.getText().toString();
        if (TextUtils.isEmpty(dtOpinion) || dtOpinion.equals(" ")) {
            CustomToast.show(mContext, getString(R.string.udt_hint_treat_advices), Toast.LENGTH_SHORT);
        } else {
            showLoadingView(getString(R.string.udt_now_upload));
            //TODO 调用接口
            HPUSubmitAdvice hpuSubmitAdvice = new HPUSubmitAdvice() {
                @Override
                protected void onSuccess(JSONObject responseContent) {
                    super.onSuccess(responseContent);
                    removeLoadingView();
                    hideSubmitBtn();
                    CustomToast.show(mContext, getString(R.string.udt_upload_advice_success), Toast.LENGTH_SHORT);
                    MedicalApplication.getFileTaskManager().sendDTResultMsg(0,
                            mUDTGlobleData.getResponseNubeNumber(),
                            mUDTGlobleData.getRequestNubeNumber(),
                            dtID,
                            "");
                    requestData();
                    CustomLog.i(TAG, "submitLocalDTOpinion:: 本地提交诊疗建议成功");
                }


                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    removeLoadingView();
                    if (statusCode == MDS_OFFACC_NOT_EXIST) {
                        CustomToast.show(mContext, getString(R.string.udt_only_respon_ad), Toast.LENGTH_SHORT);
                    } else if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                        AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                    } else {
                        CustomToast.show(mContext, statusInfo, Toast.LENGTH_SHORT);
                    }

                    CustomLog.i(TAG,
                            "submitLocalDTOpinion:: 本地提交诊疗建议失败!statusCode:" + statusCode + statusInfo);
                }
            };
            hpuSubmitAdvice.submit(token, dtID, dtOpinion);
        }
    }


    @Override
    public void onDateChanged() {
        updateUI();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
        mUDTGlobleData.removeListener(this);
    }


    public void ObtainFocus() {
        moveToAdviceEdit();
    }


    private void showBigImage(String url) {
        if (!TextUtils.isEmpty(url)) {
            Intent intent_inputimage = new Intent(mContext, OpenBigImageActivity.class);
            intent_inputimage.putExtra(OpenBigImageActivity.DATE_TYPE,
                    OpenBigImageActivity.DATE_TYPE_Internet);
            intent_inputimage.putExtra(OpenBigImageActivity.DATE_URL, url);
            startActivity(intent_inputimage);
        }
    }


    class OfficeViewHolder {
        TextView office;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            requestData();
        }
    }

    /**
     * 设置全局数据
     *
     * @param data
     */
    public void setUDTGlobleData(UDTGlobleData data) {
        CustomLog.i(TAG, "setUDTGlobleData()");

        this.mUDTGlobleData = data;
        mUDTGlobleData.addListener(this);
    }

    private void initBroadcast() {
        CustomLog.d(TAG, "注册广播");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UDTChatRoomActivity.SUBMIT_PARISE_BROADCAST);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UDTChatRoomActivity.SUBMIT_PARISE_BROADCAST)) {
                CustomLog.i(TAG, "收到诊疗评价广播");
                final String appriseDtId = intent.getStringExtra(AppraiseDialog.APPRAISEDIALOG_DT_ID);
                if (!appriseDtId.equals(dtID)) {
                    // 广播要与诊疗室匹配
                    return;
                }
                requestData();
            }
        }
    };
    InputFilter inputFilter = new InputFilter() {
        Pattern emoji = Pattern.compile(
                "[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);


        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher emojiMatcher = emoji.matcher(source);
            if (emojiMatcher.find()) {
                CustomToast.show(mContext,
                        mContext.getString(R.string.patient_condition_not_support_enter_emoji),
                        Toast.LENGTH_LONG);
                return "";
            }
            return null;
        }
    };
}
