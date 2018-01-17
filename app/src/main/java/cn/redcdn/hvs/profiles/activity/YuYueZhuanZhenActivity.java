package cn.redcdn.hvs.profiles.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
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

import com.butel.connectevent.utils.LogUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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

import cn.redcdn.datacenter.cdnuploadimg.CdnUploadDataInfo;
import cn.redcdn.datacenter.hpucenter.HPUCreateSubscribeTfCsl;
import cn.redcdn.datacenter.hpucenter.HPUGetDTlist;
import cn.redcdn.datacenter.hpucenter.HPUGetDepartments;
import cn.redcdn.datacenter.hpucenter.HPUGetInstitutions;
import cn.redcdn.datacenter.hpucenter.HPUGetTfScheduls;
import cn.redcdn.datacenter.hpucenter.data.CertypeInfo;
import cn.redcdn.datacenter.hpucenter.data.DTInfo;
import cn.redcdn.datacenter.hpucenter.data.DepartmentInfo;
import cn.redcdn.datacenter.hpucenter.data.InstitutionInfo;
import cn.redcdn.datacenter.hpucenter.data.TFDoctorInfo;
import cn.redcdn.datacenter.hpucenter.data.TFRangeType;
import cn.redcdn.datacenter.hpucenter.data.TFSchedInfo;
import cn.redcdn.datacenter.hpucenter.data.TFSectionInfo;
import cn.redcdn.datacenter.hpucenter.data.TFdetailInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.cdnmanager.UploadManager;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.head.activity.ConfirmActivity;
import cn.redcdn.hvs.head.activity.ReferralActivity;
import cn.redcdn.hvs.im.util.SendCIVMDTUtil;
import cn.redcdn.hvs.profiles.dialog.CameraImageDialog;
import cn.redcdn.hvs.requesttreatment.ImagePagerAdapterList;
import cn.redcdn.hvs.requesttreatment.PatientConditionActivity;
import cn.redcdn.hvs.requesttreatment.loopview.LoopView;
import cn.redcdn.hvs.requesttreatment.loopview.OnItemSelectedListener;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;
import id.zelory.compressor.Compressor;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static android.media.MediaRecorder.VideoSource.CAMERA;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.head.activity.ReferralActivity.REFERRAL;
import static cn.redcdn.hvs.requesttreatment.ReserveDTActivity.containsLetter;

/**
 * Created by Administrator on 2018/1/2.
 */

public class YuYueZhuanZhenActivity extends BaseActivity {
    private Context mContext;
    private TitleBar titleBar;
    private RelativeLayout hpsLayout;
    private RelativeLayout hospitalLayout;
    private RelativeLayout departmentLayout;
    private TextView testTextView;
    private RelativeLayout timeLayout;
    private TextView hpsContentView;
    private TextView hospitalContentView;
    private TextView departmentContentView;
    private TextView timeContentView;
    private EditText nameEditText;
    private EditText phoneEditText;
    private List<DTInfo> dtInfoList = new ArrayList<DTInfo>();
    private List<InstitutionInfo> institutionInfoList = new ArrayList<InstitutionInfo>();
    private List<DepartmentInfo> departmentInfoList = new ArrayList<DepartmentInfo>();
    private String mSelectedDtId;
    private AlertDialog dialog;
    private TextView pickerTitleTextView;
    private ListView pickerscrlllview; // 滚动选择器
    private TextView pickerCancelView;
    private String mPickContent;
    private String mSelectedInstitutionId;
    private String mSelectedDepartmentId;
    private String deptId;//门诊id
    private String schedulId;//门诊排班id
    private String doctorId;////医生id
    private String deptName;
    private String schedName;
    private String doctorName;
    private List<TFSectionInfo> departInfoList;
    private List<TFRangeType> rangeInfoList;//门诊集合
    private List<TFDoctorInfo> doctorInfoList;//医生列表
    private TFSchedInfo mResponseContent;
    private List<String> departAdapter;
    private List<String> departIdList;
    private LoopView departLoopView;
    private String selectedDepartName;
    private String selectedDepartId;
    private TextView tv_loading;
    RelativeLayout rl_udt_content;
    private TextView dateTextView;
    private List<String> rangeAdapter;
    private List<String> rangIdList;
    private List<String> rangFlagList;
    private LoopView rangeLoopView;
    private String selectedRangeName;
    private String selectedRangeId;
    private String selectedEnableFlag;
    private List<String> doctorAdpater;
    private List<String> docterIdList;
    private LoopView doctorLoopView;
    private String selectedDoctorName;
    private String selectedDoctorId;
    private RelativeLayout decreaseDateLayout;
    private RelativeLayout increaseDateLayout;
    private TextView cancelTextView;
    private TextView confirmTextView;
    private int currentYear;
    private int currentMonth;
    private int currentDay;
    private String date;
    private String increaseTime;
    private LinearLayout informationLayout;
    private LoopView pickerscrlllviewAge;
    private LoopView pickerscrlllviewGender;
    private LoopView pickerscrlllviewHeight;
    private LoopView pickerscrlllviewWeight;
    private TextView pickerConfirmView;
    private String mAge;
    private String mPickContentAge;
    private String mGender;
    private String mHeight;
    private String mWeight;
    private String mPickContentGender;
    private String mPickContentHeight;
    private String mPickContentWeight;
    private TextView ageView;
    private TextView genderView;
    private TextView heightView;
    private TextView weightView;
    private List<String> list1;
    private List<String> list2;
    private List<String> list3;
    private List<String> list4;
    private EditText abstractEditText;
    private EditText inspectionEditText;
    private EditText toBeSolvedEditText;
    private String patientName;
    private String patientPhone;
    private String mAbstract;
    private String id;
    private ViewPager mViewPagerList = null;
    private ImagePagerAdapterList mImagePagerAdapterList = null;
    private List<Contact> mMutiTopList = new ArrayList<Contact>();
    private static final int IMAGE_LIST_COLUMN = 4;
    private final int MSG_MESSAGE_IMAGE_NUMBER_CHANGED = 702;
    public static final String HEAD_ICON_DIC = Environment.getExternalStorageDirectory()
            + File.separator + "ipNetPhone" + File.separator + "headIcon";
    private File headIconFile;// 相册或者拍照保存的文件
    private CameraImageDialog cid = null;
    private CameraImageDialog cid1 = null;
    private File mcroppedIconFile = null; //裁剪后的头像文件
    public static final String CROPPED_ICON_FILE = "cropped_head_icon.jpg";
    private int currentPicSize = 0;
    private int sentPicNumber = 0;
    private boolean uploadFinished = true;
    private boolean clickComplete = false;
    private boolean updateFinished = false;
    private String mCheck;

    /**
     * 状态码说明
     */

    public static final int HPU_NOT_EXIT_ID = -908; //诊疗号不存在

    public static final int HPU_NOT_EXIT_DEP = -909; // 科室ID不存在

    public static final int HPU_NOT_WAIT_STATUS = -910; // 诊疗号状态不是待接诊

    public static final int HPU_NOT_NOW_STATUS = -911; //诊疗号状态不是接诊中

    public static final int HPU_NOT_SELF_CSL = -912; //诊疗号不属于自己接诊

    public static final int HPU_NOT_SELF_SEEK = -913; //诊疗号不是自己求诊

    public static final int HPU_HTTP_EMPTY = -914; //HTTP请求返回值空

    public static final int HPU_APONITNUM_OVER = -915; //预约号己满,无法预约

    public static final int HPU_NOTIN_SCHEDULING = -916; //不在排班医生中

    public static final int HPU_DATE_FORMAT_ERROR = -917; //日期FORMAT错误:{0}

    public static final int HPU_DTID_NOT_ALL = -918; //机构Id不能是全部机构

    public static final int HPU_NUBE_NOT_EXIT = -919; //nube号不存在

    public static final int HPU_PARAM_IS_EMPTY = -920; //排班ID或科室ID、排班日期、时间段标识为空

    public static final int HPU_NO_SCHED_DCTOR = -921; //没有排班医生

    public static final int HPU_DEP_HAS_SHCHED = -922; //科室、排班日期、时间段已排班

    public static final int HPU_LESS_APOINTNUM = -923; //放号不能小于预约数

    public static final int HPU_SHCEDID_NOT_EXIT = -924; //排班ID不存在

    public static final int HPU_DTSTAUS_NOT_END = -925; //诊疗单ID状态不是结束

    public static final int HPU_DT_HAS_ACCEPTED = -926; //诊疗单ID已接诊

    public static final int HPU_DTID_NOT_EXIST = -927; //转诊单号不存在:{0}

    public static final int HPU_PASSWORD_ERROR = -928; //密码不正确

    public static final int HPU_DTID_HAS_TF = -929; //诊疗号已转诊

    public static final int HPU_REQUEST_OUT_ERROR = -930; //外部请求返回异常错误码

    public static final int HPU_PARAMS_IS_EMPTY = -931; //参数列表为空:{0}

    public static final int HPU_AC_PW_ERROR = -932; //排班ID不存在

    public static final int HPU_TFSHCEDID_NOT_EXIT = -933; //转诊排班ID不存在

    public static final int HPU_DOCTOR_NOT_SHCED = -934; //医生不在当前排班中

    public static final int HPU_CSL_SELF = -935; //自己求诊不能自己接诊

    public static final int HPU_CDN_UPLOAD_ERROR = -936; //CDN文件上传失败

    public static final int HPU_SHCEDID_HAS_APPOINT = -937; //排班己被预约
    public static final int PAIBAN_OUTDATE = -947; //排班己被预约
    private TFdetailInfo data=null;

    public interface ItemClickListener {
        void onClick();

        void onLongClick(View view, int position);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_MESSAGE_IMAGE_NUMBER_CHANGED:
                    int position = msg.arg1;
                    mMutiTopList.remove(position);
                    mImagePagerAdapterList.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    InputFilter inputFilter = new InputFilter() {

        Pattern pattern = Pattern.compile("[^a-zA-Z\\u4E00-\\u9FA5_]");

        @Override
        public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
            Matcher matcher = pattern.matcher(charSequence);
            if (!matcher.find()) {
                return null;
            } else {
                CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_chinese_character_pinyin_only), CustomToast.LENGTH_LONG);
                return "";
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yuyue_treatment);
        id = getIntent().getStringExtra(REFERRAL);
        data = (TFdetailInfo) getIntent().getSerializableExtra("data");
        initWidget();
        if (data != null) {
            CustomLog.d("YuYueZhuanZhenAcivity", "dtName" + data.dtName + "dtId" + data.dtId + "transferHosp" + data.transferHosp + "UnionOrgId" + data.getUnionOrgId() + "transferDept" + data.transferDept + "deptId" + data.deptId);
            initData(data);
        }
    }

    private void initData(TFdetailInfo data) {
        if (!TextUtils.isEmpty(data.getPatientAge())) {
            mAge = data.getPatientAge();
            ageView.setText(mAge);
            ageView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(data.getPatientSex())) {
            mGender = data.getPatientSex();
            genderView.setText(mGender);
            genderView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(data.getHeight())) {
            mHeight = data.getHeight();
            heightView.setText(mHeight);
            heightView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(data.getWeight())) {
            mWeight = data.getWeight();
            weightView.setText(mWeight);
            weightView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(data.dtName) && !TextUtils.isEmpty(data.dtId)) {
            hpsContentView.setText(data.dtName);
            hpsContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
            mSelectedDtId = data.dtId;
        }
        if (!TextUtils.isEmpty(data.transferHosp) && !TextUtils.isEmpty(data.unionOrgId)) {
            hospitalContentView.setText(data.transferHosp);
            hospitalContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
            mSelectedInstitutionId = data.unionOrgId;
        }
        if (!TextUtils.isEmpty(data.transferDept) && !TextUtils.isEmpty(data.deptId)) {
            departmentContentView.setText(data.transferDept);
            departmentContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
            mSelectedDepartmentId = data.deptId;
        }
        if (!TextUtils.isEmpty(data.transferSchedulDate) && !TextUtils.isEmpty(data.transferSchedulId)) {
            String result = data.transferSchedulDate.substring(0, 4);
            String substring = data.transferSchedulDate.substring(4, 6);
            String substring1 = data.transferSchedulDate.substring(data.transferSchedulDate.length() - 2, data.transferSchedulDate.length());
            String leiXing = null;
            if (!TextUtils.isEmpty(data.sectionType)) {
                switch (data.sectionType) {
                    case "1":
                        leiXing = getString(R.string.normal_outpatient);
                        break;
                    case "2":
                        leiXing = getString(R.string.subtropical_high_outpatient);
                        break;
                    case "3":
                        leiXing = getString(R.string.zhuanjia_outpatient);
                        break;
                    default:
                        break;
                }
            }
            if (leiXing != null) {
                timeContentView.setText(substring + MedicalApplication.getContext().getString(R.string.month) + substring1 + MedicalApplication.getContext().getString(R.string.day)+" "+ data.transferRangeName+" " + leiXing);
            } else {
                timeContentView.setText(substring + MedicalApplication.getContext().getString(R.string.month) + substring1 + MedicalApplication.getContext().getString(R.string.day)+" " + data.transferRangeName);
            }
            timeContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
            schedulId = data.transferSchedulId;
            doctorId = data.transferUserId;
        }
        if (!TextUtils.isEmpty(data.patientName)) {
            nameEditText.setText(data.patientName);
            patientName = data.patientName;
        }
        if (!TextUtils.isEmpty(data.patientMobile)) {
            phoneEditText.setText(data.patientMobile);
            patientPhone = data.patientMobile;
        }
        if (!TextUtils.isEmpty(data.chief)) {
            abstractEditText.setText(data.chief);
            mAbstract = data.chief;
        }
        if (!TextUtils.isEmpty(data.physical)) {
            inspectionEditText.setText(data.physical);
        }
        if (!TextUtils.isEmpty(data.problem)) {
            toBeSolvedEditText.setText(data.problem);
        }
        if (!TextUtils.isEmpty(data.assCheckUrl)) {
            mCheck = data.assCheckUrl;
            if (!TextUtils.isEmpty(mCheck)) {
                String[] urls = mCheck.split(",");
                for (int i = 0; i < urls.length; i++) {

                    Contact c = new Contact();
                    c.setHeadUrl(urls[i]);
                    c.setNubeNumber("");
                    mMutiTopList.add(mMutiTopList.size() - 1, c);

                    mImagePagerAdapterList.notifyDataSetChanged();
                }
            }
            mCheck = "";
        }
    }

    private void initWidget() {
        mContext = this;
        initTitleBar();
        cid = new CameraImageDialog(YuYueZhuanZhenActivity.this,
                R.style.contact_del_dialog,
                mContext.getString(R.string.patient_condition_take_photo),
                mContext.getString(R.string.patient_condition_select_from_gallery)
                , 2, true,
                mContext.getString(R.string.patient_condition_add_picture));
        cid1 = new CameraImageDialog(YuYueZhuanZhenActivity.this,
                R.style.contact_del_dialog,
                "",
                mContext.getString(R.string.patient_condition_delete_picture)
                , 1, false, "");
        mViewPagerList = (ViewPager) findViewById(R.id.patient_condition_check_list);
        Contact defaultImage = new Contact();
        defaultImage.setHeadUrl("");
        defaultImage.setNubeNumber("");
        mMutiTopList.add(defaultImage);
        mImagePagerAdapterList = new ImagePagerAdapterList(this, mMutiTopList,
                IMAGE_LIST_COLUMN, 1, false, new PatientConditionActivity.ItemClickListener() {
            @Override
            public void onClick() {
                showDialog();
            }


            @Override
            public void onLongClick(View view, int position) {
                showDeleteImageDialog(view, position);
            }
        });
        mViewPagerList.setAdapter(mImagePagerAdapterList);
        inspectionEditText = (EditText) findViewById(R.id.et_inspection);
        toBeSolvedEditText = (EditText) findViewById(R.id.et_to_be_solved);
        abstractEditText = (EditText) findViewById(R.id.et_abstract);
        informationLayout = (LinearLayout) findViewById(R.id.ll_information);
        ageView = (TextView) findViewById(R.id.tv_age);
        genderView = (TextView) findViewById(R.id.tv_gender);
        heightView = (TextView) findViewById(R.id.tv_height);
        weightView = (TextView) findViewById(R.id.tv_weight);
        hpsLayout = (RelativeLayout) findViewById(R.id.rl_hps); //选择医联体
        hospitalLayout = (RelativeLayout) findViewById(R.id.rl_hospital); //选择医院
        departmentLayout = (RelativeLayout) findViewById(R.id.rl_department); //选择科室
        timeLayout = (RelativeLayout) findViewById(R.id.rl_time); //排版
        testTextView = (TextView) findViewById(R.id.tv_test); //预约下一步
        hpsContentView = (TextView) findViewById(R.id.tv_hps_content);
        hospitalContentView = (TextView) findViewById(R.id.tv_hospital_content);
        departmentContentView = (TextView) findViewById(R.id.tv_department_content);
        timeContentView = (TextView) findViewById(R.id.tv_time_content);
        nameEditText = (EditText) findViewById(R.id.et_name);
        nameEditText.setFilters(new InputFilter[]{inputFilter, new InputFilter.LengthFilter(5)});
        phoneEditText = (EditText) findViewById(R.id.et_phone);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        hpsLayout.setOnClickListener(mbtnHandleEventListener);
        hospitalLayout.setOnClickListener(mbtnHandleEventListener);
        departmentLayout.setOnClickListener(mbtnHandleEventListener);
        timeLayout.setOnClickListener(mbtnHandleEventListener);
        testTextView.setOnClickListener(mbtnHandleEventListener);
        informationLayout.setOnClickListener(mbtnHandleEventListener);
        if (data==null){
            getDTListFromLocal();
        }
    }

    private void showDialog() {
        cid.setCameraClickListener(new CameraImageDialog.CameraClickListener() {
            @Override
            public void clickListener() {
                boolean result = CommonUtil.selfPermissionGranted(YuYueZhuanZhenActivity.this, Manifest.permission.CAMERA);
                if (!result) {
                    // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionGen.with(YuYueZhuanZhenActivity.this)
                                .addRequestCode(100)
                                .permissions(Manifest.permission.CAMERA)
                                .request();
                    } else {
                        openAppDetails(getString(R.string.no_photo_permission));
                    }
                } else {
                    camera();
                }
            }
        });
        cid.setPhoneClickListener(new CameraImageDialog.PhoneClickListener() {
            @Override
            public void clickListener() {
                SendCIVMDTUtil.sendDTPatientPic(YuYueZhuanZhenActivity.this);
            }
        });
        cid.setNoClickListener(new CameraImageDialog.NoClickListener() {
            @Override
            public void clickListener() {
                cid.dismiss();
            }
        });
        Window window = cid.getWindow();
        window.setGravity(Gravity.BOTTOM);
        cid.setCanceledOnTouchOutside(true);
        cid.show();
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = cid.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        lp.height = (int) (0.3 * display.getHeight()); // 设置高度
        cid.getWindow().setAttributes(lp);
    }

    private void showDeleteImageDialog(final View v, final int position) {
        cid1.setPhoneClickListener(new CameraImageDialog.PhoneClickListener() {
            @Override
            public void clickListener() {
                Message msg = new Message();
                msg.what = MSG_MESSAGE_IMAGE_NUMBER_CHANGED;
                msg.arg1 = position;
                mHandler.sendMessage(msg);
                cid1.dismiss();
            }
        });
        cid1.setNoClickListener(new CameraImageDialog.NoClickListener() {
            @Override
            public void clickListener() {
                cid1.dismiss();
            }
        });
        Window window = cid1.getWindow();
        window.setGravity(Gravity.BOTTOM);
        cid1.setCanceledOnTouchOutside(true);
        cid1.show();
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = cid1.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        lp.height = (int) (0.2 * display.getHeight()); // 设置高度
        cid1.getWindow().setAttributes(lp);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLog.e(TAG, "onActivityResult");
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CAMERA:
                CustomLog.d(TAG, "进入拍照后currentMyPid===="
                        + android.os.Process.myPid());
                CustomLog.d(TAG, "onActivityResult..CAMERA..headIconFile.getPath()="
                        + headIconFile.getPath());
                mcroppedIconFile = new File(HEAD_ICON_DIC, CROPPED_ICON_FILE);
                if (mcroppedIconFile.exists()) {
                    mcroppedIconFile.delete();
                }
                final ArrayList<String> cameraPicList = new ArrayList<String>();
                cameraPicList.add(headIconFile.getPath());
                cid.dismiss();
                currentPicSize = mMutiTopList.size() - 1;
                sentPicNumber = 0;
                uploadFinished = false;
                upLoad(headIconFile.getPath(), cameraPicList);
                CustomLog.d(TAG, "拍照后的图片进行上传");
                break;
            case SendCIVMDTUtil.ACTION_SHARE_PIC_FROM_NATIVE:
                CustomLog.d(TAG, "选择图片 返回");
                cid.dismiss();
                currentPicSize = mMutiTopList.size() - 1;
                sendPic(data);
                break;
            default:
                break;

        }
    }

    private boolean sendPic(Intent data) {

        if (data == null) {
            LogUtil.d("data==null");
            return false;
        }

        final ArrayList<String> selectedPicList = data.getExtras()
                .getStringArrayList(Intent.EXTRA_STREAM);
        if (selectedPicList == null || selectedPicList.size() == 0) {
            LogUtil.d("selectedPicList为空");
            return false;
        }

        sentPicNumber = 0;
        uploadFinished = false;
        for (String picPath : selectedPicList) {
            upLoad(picPath, selectedPicList);
        }

        return true;
    }

    private void upLoad(final String path, final ArrayList<String> selectedPicList) {

        UploadManager.UploadImageListener listener = new UploadManager.UploadImageListener() {
            @Override
            public void onSuccess(CdnUploadDataInfo dataInfo) {
                CustomLog.d(TAG, "上传头像回调的URL = " + dataInfo.filepath);
                for (int i = 0; i < mMutiTopList.size(); i++) {
                    if (mMutiTopList.get(i).getNubeNumber().equals(path)) {
                        mMutiTopList.get(i).setHeadUrl(dataInfo.filepath);
                    }
                }
                sentPicNumber++;
                if (sentPicNumber == selectedPicList.size()) {
                    uploadFinished = true;
                    if (clickComplete) {
                        if (id == null) {
                            updatePatientInfo(false);
                        } else {
                            updatePatientInfo1(false);
                        }
                    }
                }

                String filepath = dataInfo.getFilepath();
                if (filepath == null) {
                    CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_fail),
                            Toast.LENGTH_SHORT);
                }

                if (uploadFinished && updateFinished) {
                    removeLoadingView();
                    Intent intent = new Intent();
                    intent.setClass(YuYueZhuanZhenActivity.this, ConfirmActivity.class);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onProgress(int persent) {
            }

            @Override
            public void onFailed(int statusCode, String msg) {
                sentPicNumber++;
                if (sentPicNumber == selectedPicList.size()) {
                    uploadFinished = true;
                    if (clickComplete) {
                        if (id == null) {
                            updatePatientInfo(false);
                        } else {
                            updatePatientInfo1(false);
                        }
                    }
                }

                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(YuYueZhuanZhenActivity.this,
                            getString(R.string.login_checkNetworkError),
                            Toast.LENGTH_LONG);
                    return;
                }
                if (statusCode == SettingData.getInstance().tokenUnExist
                        || statusCode == SettingData.getInstance().tokenInvalid) {
                    CustomToast.show(getApplicationContext(), getString(R.string.token_fail),
                            Toast.LENGTH_SHORT);
                    AccountManager.getInstance(getApplicationContext()).tokenAuthFail(
                            statusCode);
                }
                CustomToast.show(getApplicationContext(),
                        getString(R.string.upload_pic_fail) + "=" + statusCode,
                        Toast.LENGTH_SHORT);
            }
        };

        if (selectedPicList.size() + currentPicSize > 9) {
            CustomToast.show(getApplicationContext(),
                    mContext.getString(R.string.patient_condition_at_most_nine_pictures),
                    Toast.LENGTH_SHORT);
        } else {
            Contact c = new Contact();
            c.setHeadUrl("");
            c.setNubeNumber(path);
            mMutiTopList.add(mMutiTopList.size() - 1, c);
            mImagePagerAdapterList.notifyDataSetChanged();
            UploadManager.getInstance().uploadImage(getThumFile(new File(path)), listener);
        }

    }

    private void updatePatientInfo(boolean showLoading) {
        final HPUCreateSubscribeTfCsl hpuCreateSubscribeTfCsl = new HPUCreateSubscribeTfCsl() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                updateFinished = true;
                if (uploadFinished && updateFinished) {
                    removeLoadingView();
                    CustomLog.d(TAG, "HPUUpdatePatient,onSuccess");
                    Intent intent = new Intent();
                    intent.setClass(YuYueZhuanZhenActivity.this, ConfirmActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e("YuYueZhuanZhenActivity", "statusCode" + statusCode + "|" + "statusCode" + statusInfo);
                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(YuYueZhuanZhenActivity.this).tokenAuthFail(statusCode);
                } else {
                    switch (statusCode) {
                        case HPU_NOT_EXIT_ID:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_EXIT_DEP:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "科室ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_WAIT_STATUS:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号状态不是待接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_NOW_STATUS:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号状态不是接诊中", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_SELF_CSL:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号不属于自己接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_SELF_SEEK:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号不是自己求诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_HTTP_EMPTY:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "HTTP请求返回值空", Toast.LENGTH_LONG);
                            break;
                        case HPU_APONITNUM_OVER:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "预约号己满,无法预约", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOTIN_SCHEDULING:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "不在排班医生中", Toast.LENGTH_LONG);
                            break;
                        case HPU_DATE_FORMAT_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "日期错误", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTID_NOT_ALL:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "机构Id不能是全部机构", Toast.LENGTH_LONG);
                            break;
                        case HPU_NUBE_NOT_EXIT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "nube号不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_PARAM_IS_EMPTY:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班ID或科室ID、排班日期、时间段标识为空", Toast.LENGTH_LONG);
                            break;
                        case HPU_NO_SCHED_DCTOR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "没有排班医生", Toast.LENGTH_LONG);
                            break;
                        case HPU_DEP_HAS_SHCHED:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "科室、排班日期、时间段已排班", Toast.LENGTH_LONG);
                            break;
                        case HPU_LESS_APOINTNUM:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "放号不能小于预约数", Toast.LENGTH_LONG);
                            break;
                        case HPU_SHCEDID_NOT_EXIT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTSTAUS_NOT_END:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗单ID状态不是结束", Toast.LENGTH_LONG);
                            break;
                        case HPU_DT_HAS_ACCEPTED:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗单ID已接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTID_NOT_EXIST:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "转诊单号不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_PASSWORD_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "密码不正确", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTID_HAS_TF:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号已转诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_REQUEST_OUT_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "外部请求返回异常错误码", Toast.LENGTH_LONG);
                            break;
                        case HPU_PARAMS_IS_EMPTY:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "参数列表为空", Toast.LENGTH_LONG);
                            break;
                        case HPU_AC_PW_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_TFSHCEDID_NOT_EXIT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "转诊排班ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_DOCTOR_NOT_SHCED:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "医生不在当前排班中", Toast.LENGTH_LONG);
                            break;
                        case HPU_CSL_SELF:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "自己求诊不能自己接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_CDN_UPLOAD_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "CDN文件上传失败", Toast.LENGTH_LONG);
                            break;
                        case HPU_SHCEDID_HAS_APPOINT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班己被预约", Toast.LENGTH_LONG);
                            break;
                        case PAIBAN_OUTDATE:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班己过期", Toast.LENGTH_LONG);
                            break;
                        default:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    mContext.getString(R.string.reserve_treatment_get_data_failed), Toast.LENGTH_LONG);
                            break;
                    }

                }
            }
        };
        CustomLog.d(TAG, "first_uploadFinished:" + uploadFinished);
        if (uploadFinished) {
            try {
                for (int i = 0; i < mMutiTopList.size() - 1; i++) {
                    if (TextUtils.isEmpty(mCheck)) {
                        mCheck = mMutiTopList.get(i).getHeadUrl();
                    } else {
                        if (!TextUtils.isEmpty(mMutiTopList.get(i).getHeadUrl())) {
                            mCheck = mCheck + "," + mMutiTopList.get(i).getHeadUrl();
                        }
                    }
                }
                CustomLog.d(TAG, "first_mCheck:" + mCheck);
            } catch (Exception e) {
                CustomLog.e(TAG, "error!" + e.toString());
            }
            clickComplete = false;
            hpuCreateSubscribeTfCsl.createSubscribeTf(AccountManager.getInstance(mContext).getMdsToken(), mSelectedInstitutionId, mSelectedDepartmentId, schedulId, doctorId, patientName, patientPhone, mAge, mGender, mHeight, mWeight, mAbstract, inspectionEditText.getText().toString(), mCheck, toBeSolvedEditText.getText().toString());
            mCheck = "";
        }

        if (showLoading) {
            showLoadingView(mContext.getString(R.string.patient_condition_loading),
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            clickComplete = false;
                            removeLoadingView();
                            hpuCreateSubscribeTfCsl.cancel();
                            CustomToast.show(getApplicationContext(),
                                    getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                        }
                    });
        }
    }

    private void updatePatientInfo1(boolean showLoading) {
        final HPUCreateSubscribeTfCsl hpuCreateSubscribeTfCsl = new HPUCreateSubscribeTfCsl() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                updateFinished = true;
                if (uploadFinished && updateFinished) {
                    MedicalApplication.destoryActivity(ReferralActivity.REFERRAL_ACTIVITY);
                    removeLoadingView();
                    Intent intent = new Intent();
                    intent.setClass(YuYueZhuanZhenActivity.this, ConfirmActivity.class);
                    startActivity(intent);
                    YuYueZhuanZhenActivity.this.finish();
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomLog.e("YuYueZhuanZhenActivity", "statusCode" + statusCode + "|" + "statusCode" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(YuYueZhuanZhenActivity.this).tokenAuthFail(statusCode);
                } else {
                    switch (statusCode) {
                        case HPU_NOT_EXIT_ID:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_EXIT_DEP:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "科室ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_WAIT_STATUS:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号状态不是待接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_NOW_STATUS:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号状态不是接诊中", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_SELF_CSL:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号不属于自己接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOT_SELF_SEEK:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号不是自己求诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_HTTP_EMPTY:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "HTTP请求返回值空", Toast.LENGTH_LONG);
                            break;
                        case HPU_APONITNUM_OVER:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "预约号己满,无法预约", Toast.LENGTH_LONG);
                            break;
                        case HPU_NOTIN_SCHEDULING:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "不在排班医生中", Toast.LENGTH_LONG);
                            break;
                        case HPU_DATE_FORMAT_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "日期错误", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTID_NOT_ALL:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "机构Id不能是全部机构", Toast.LENGTH_LONG);
                            break;
                        case HPU_NUBE_NOT_EXIT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "nube号不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_PARAM_IS_EMPTY:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班ID或科室ID、排班日期、时间段标识为空", Toast.LENGTH_LONG);
                            break;
                        case HPU_NO_SCHED_DCTOR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "没有排班医生", Toast.LENGTH_LONG);
                            break;
                        case HPU_DEP_HAS_SHCHED:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "科室、排班日期、时间段已排班", Toast.LENGTH_LONG);
                            break;
                        case HPU_LESS_APOINTNUM:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "放号不能小于预约数", Toast.LENGTH_LONG);
                            break;
                        case HPU_SHCEDID_NOT_EXIT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTSTAUS_NOT_END:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗单ID状态不是结束", Toast.LENGTH_LONG);
                            break;
                        case HPU_DT_HAS_ACCEPTED:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗单ID已接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTID_NOT_EXIST:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "转诊单号不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_PASSWORD_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "密码不正确", Toast.LENGTH_LONG);
                            break;
                        case HPU_DTID_HAS_TF:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "诊疗号已转诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_REQUEST_OUT_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "外部请求返回异常错误码", Toast.LENGTH_LONG);
                            break;
                        case HPU_PARAMS_IS_EMPTY:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "参数列表为空", Toast.LENGTH_LONG);
                            break;
                        case HPU_AC_PW_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_TFSHCEDID_NOT_EXIT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "转诊排班ID不存在", Toast.LENGTH_LONG);
                            break;
                        case HPU_DOCTOR_NOT_SHCED:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "医生不在当前排班中", Toast.LENGTH_LONG);
                            break;
                        case HPU_CSL_SELF:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "自己求诊不能自己接诊", Toast.LENGTH_LONG);
                            break;
                        case HPU_CDN_UPLOAD_ERROR:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "CDN文件上传失败", Toast.LENGTH_LONG);
                            break;
                        case HPU_SHCEDID_HAS_APPOINT:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    "排班己被预约", Toast.LENGTH_LONG);
                            break;
                        default:
                            CustomToast.show(YuYueZhuanZhenActivity.this,
                                    mContext.getString(R.string.reserve_treatment_get_data_failed), Toast.LENGTH_LONG);
                            break;
                    }
                }
            }
        };
        CustomLog.d(TAG, "twice_uploadFinished:" + uploadFinished);
        if (uploadFinished) {
            try {
                for (int a = 0; a < mMutiTopList.size() - 1; a++) {
                    if (TextUtils.isEmpty(mCheck)) {
                        mCheck = mMutiTopList.get(a).getHeadUrl();
                    } else {
                        if (!TextUtils.isEmpty(mMutiTopList.get(a).getHeadUrl())) {
                            mCheck = mCheck + "," + mMutiTopList.get(a).getHeadUrl();
                        }
                    }
                }
                CustomLog.d(TAG, "twice_mCheck:" + mCheck);
            } catch (Exception e) {
                CustomLog.e(TAG, "error!" + e.toString());
            }
            clickComplete = false;
            hpuCreateSubscribeTfCsl.reCreateSubscribeTf(AccountManager.getInstance(mContext).getMdsToken(), id, mSelectedInstitutionId, mSelectedDepartmentId, schedulId, doctorId, patientName, patientPhone, mAge, mGender, mHeight, mWeight, mAbstract, inspectionEditText.getText().toString(), mCheck, toBeSolvedEditText.getText().toString());
            mCheck = "";
        }

        if (showLoading) {
            showLoadingView(mContext.getString(R.string.patient_condition_loading),
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            clickComplete = false;
                            removeLoadingView();
                            hpuCreateSubscribeTfCsl.cancel();
                            CustomToast.show(getApplicationContext(),
                                    getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                        }
                    });
        }
    }

    public File getThumFile(File file) {
        File tmpFile = null;
        try {
            tmpFile = new Compressor(this).compressToFile(file);
        } catch (IOException e) {
            CustomLog.d(TAG, "压缩图片异常  error=" + e.getMessage());
            tmpFile = file;
            return tmpFile;
        }
        return tmpFile;
    }

    private void getDTListFromLocal() {
        final HPUGetDTlist gdt = new HPUGetDTlist() {
            @Override
            protected void onSuccess(List<DTInfo> responseContent) {
                super.onSuccess(responseContent);
                dtInfoList = responseContent;
                if (dtInfoList != null && dtInfoList.size() > 0 && dtInfoList.get(0).getId() != null) {
                    mSelectedDtId = dtInfoList.get(0).getId();
                }
                if (dtInfoList != null && dtInfoList.size() > 0 && dtInfoList.get(0).getName() != null) {
                    hpsContentView.setText(dtInfoList.get(0).getName());
                    hpsContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
            }
        };
        gdt.getdtlist(AccountManager.getInstance(mContext).getMdsToken());
    }


    private void initTitleBar() {
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(mContext.getString(R.string.yuyuezhuanzhen));
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.rl_hps:
                getDTList();
                break;
            case R.id.rl_hospital:
                if (mSelectedDtId != null) {
                    getHospitalList();
                } else {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_please_select_hpu), Toast.LENGTH_LONG);
                }
                break;
            case R.id.rl_department:
                if (mSelectedInstitutionId != null) {
                    getDepartmentList();
                } else {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_please_select_institution), Toast.LENGTH_LONG);
                }
                break;
            case R.id.rl_time:
                timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                if (mSelectedDepartmentId == null) {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_please_select_department), Toast.LENGTH_LONG);
                } else {
                    showArrangementDialog();
                }
                break;
            case R.id.ll_information:
                clickInformation();
                break;
            case R.id.tv_test:
                patientName = nameEditText.getText().toString();
                patientPhone = phoneEditText.getText().toString();
                mAbstract = abstractEditText.getText().toString();
                if (TextUtils.isEmpty(mSelectedDtId)) {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_please_select_hpu), Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(mSelectedInstitutionId)) {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_please_select_institution), Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(mSelectedDepartmentId)) {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_please_select_department), Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(schedulId)) {
                    CustomToast.show(mContext, "请选择排班", Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(patientName)) {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_please_select_patient_name), Toast.LENGTH_LONG);
                    return;
                } else if (!TextUtils.isEmpty(patientName) && containsLetter(patientName)) {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_name_letter_only), Toast.LENGTH_LONG);
                    return;
                } else if (!TextUtils.isEmpty(patientPhone) && !isPhoneNumber(patientPhone)) {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_patient_phone_format_invalid), Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(mAbstract)) {
                    CustomToast.show(mContext, mContext.getString(R.string.please_zhusu), Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(inspectionEditText.getText().toString())) {
                    CustomToast.show(mContext, "请填写查体", Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(toBeSolvedEditText.getText().toString())) {
                    CustomToast.show(mContext, "请填写待解问题", Toast.LENGTH_LONG);
                    return;
                } else if (TextUtils.isEmpty(patientPhone)) {
                    CustomToast.show(mContext, "请填写手机号码", Toast.LENGTH_LONG);
                    return;
                } else {
                    clickComplete = true;
                    if (id == null) {
                        updatePatientInfo(true);
                    } else {
                        updatePatientInfo1(true);
                    }
                }
                break;
            default:
                break;
        }
    }


    private boolean isPhoneNumber(String phoneNumber) {
        boolean isValid = false;
        CharSequence inputStr = phoneNumber;
        String expression = "^(0|86|17951)?(13[0-9]|15[0-9]|17[0-9]|18[0-9]|14[0-9])[0-9]{8}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    private void clickInformation() {
        List<String> firstList = new ArrayList<String>();
        for (int a = 0; a <= 36; a++) {
            firstList.add(
                    String.valueOf(a) + mContext.getString(R.string.patient_condition_age_month));
        }
        for (int b = 4; b <= 120; b++) {
            firstList.add(
                    String.valueOf(b) + mContext.getString(R.string.patient_condition_age_year));
        }
        String[] list1 = (String[]) firstList.toArray(new String[firstList.size()]);

        String[] list2 = {mContext.getString(R.string.patient_condition_male),
                mContext.getString(R.string.patient_condition_female)};

        List<String> secondList = new ArrayList<String>();
        secondList.add("<10cm");
        for (int c = 11; c <= 250; c++) {
            secondList.add(String.valueOf(c) + "cm");
        }
        secondList.add(">250cm");
        String[] list3 = (String[]) secondList.toArray(new String[secondList.size()]);

        List<String> thirdList = new ArrayList<String>();
        thirdList.add("<1kg");
        for (int d = 1; d <= 300; d++) {
            thirdList.add(String.valueOf(d) + "kg");
        }
        thirdList.add(">300kg");
        String[] list4 = (String[]) thirdList.toArray(new String[thirdList.size()]);

        showPickerDialog1(list1, list2, list3, list4);
    }

    private void showPickerDialog1(final String[] dataList1,
                                   final String[] dataList2,
                                   final String[] dataList3,
                                   final String[] dataList4) {
        dialog = new AlertDialog.Builder(this).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.patient_condition_picker);
        pickerscrlllviewAge = (LoopView) window.findViewById(
                R.id.patient_condition_pickerscrlllview_age);
        pickerscrlllviewGender = (LoopView) window.findViewById(
                R.id.patient_condition_pickerscrlllview_gender);
        pickerscrlllviewHeight = (LoopView) window.findViewById(
                R.id.patient_condition_pickerscrlllview_height);
        pickerscrlllviewWeight = (LoopView) window.findViewById(
                R.id.patient_condition_pickerscrlllview_weight);
        pickerCancelView = (TextView) window.findViewById(R.id.tv_patient_condition_picker_cancel);
        pickerConfirmView = (TextView) window.findViewById(
                R.id.tv_patient_condition_picker_confirm);

        if (!TextUtils.isEmpty(mAge)) {
            mPickContentAge = mAge;
        } else {
            mPickContentAge = dataList1[43];
        }

        if (!TextUtils.isEmpty(mGender)) {
            mPickContentGender = mGender;
        } else {
            mPickContentGender = dataList2[0];
        }

        if (!TextUtils.isEmpty(mHeight)) {
            mPickContentHeight = mHeight;
        } else {
            mPickContentHeight = dataList3[90];
        }

        if (!TextUtils.isEmpty(mWeight)) {
            mPickContentWeight = mWeight;
        } else {
            mPickContentWeight = dataList4[30];
        }

        pickerCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(mAge)) {
                    ageView.setText(mContext.getString(R.string.patient_condition_age));
                    ageView.setTextColor(getResources().getColor(R.color.my_text_color));
                } else {
                    ageView.setText(mAge);
                    ageView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                if (TextUtils.isEmpty(mGender)) {
                    genderView.setText(mContext.getString(R.string.patient_condition_gender));
                    genderView.setTextColor(getResources().getColor(R.color.my_text_color));
                } else {
                    genderView.setText(mGender);
                    genderView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                if (TextUtils.isEmpty(mHeight)) {
                    heightView.setText(mContext.getString(R.string.patient_condition_height));
                    heightView.setTextColor(getResources().getColor(R.color.my_text_color));
                } else {
                    heightView.setText(mHeight);
                    heightView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                if (TextUtils.isEmpty(mWeight)) {
                    weightView.setText(mContext.getString(R.string.patient_condition_weight));
                    weightView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                } else {
                    weightView.setText(mWeight);
                    weightView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                dialog.dismiss();
            }
        });
        pickerConfirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAge = mPickContentAge;
                ageView.setText(mAge);
                ageView.setTextColor(getResources().getColor(R.color.btn_color_black));
                mGender = mPickContentGender;
                genderView.setText(mGender);
                genderView.setTextColor(getResources().getColor(R.color.btn_color_black));
                mHeight = mPickContentHeight;
                heightView.setText(mHeight);
                heightView.setTextColor(getResources().getColor(R.color.btn_color_black));
                mWeight = mPickContentWeight;
                weightView.setText(mWeight);
                weightView.setTextColor(getResources().getColor(R.color.btn_color_black));
                dialog.dismiss();
            }
        });
        list1 = new ArrayList<String>();
        for (int i = 0; i < dataList1.length; i++) {
            list1.add(dataList1[i]);
        }
        list2 = new ArrayList<String>();
        for (int i = 0; i < dataList2.length; i++) {
            list2.add(dataList2[i]);
        }
        list3 = new ArrayList<String>();
        for (int i = 0; i < dataList3.length; i++) {
            list3.add(dataList3[i]);
        }
        list4 = new ArrayList<String>();
        for (int i = 0; i < dataList4.length; i++) {
            list4.add(dataList4[i]);
        }

        pickerscrlllviewAge.setItems(list1);
        pickerscrlllviewGender.setItems(list2);
        pickerscrlllviewHeight.setItems(list3);
        pickerscrlllviewWeight.setItems(list4);

        if (!TextUtils.isEmpty(mAge)) {
            for (int a = 0; a < dataList1.length; a++) {
                if (mAge.equals(dataList1[a])) {
                    pickerscrlllviewAge.setInitPosition(a);
                    break;
                }
            }
        } else {
            pickerscrlllviewAge.setInitPosition(43);
        }

        if (!TextUtils.isEmpty(mGender)) {
            for (int b = 0; b < dataList2.length; b++) {
                if (mGender.equals(dataList2[b])) {
                    pickerscrlllviewGender.setInitPosition(b);
                    break;
                }
            }
        } else {
            pickerscrlllviewGender.setInitPosition(0);
        }

        if (!TextUtils.isEmpty(mHeight)) {
            for (int c = 0; c < dataList3.length; c++) {
                if (mHeight.equals(dataList3[c])) {
                    pickerscrlllviewHeight.setInitPosition(c);
                    break;
                }
            }
        } else {
            pickerscrlllviewHeight.setInitPosition(90);
        }

        if (!TextUtils.isEmpty(mWeight)) {
            for (int d = 0; d < dataList4.length; d++) {
                if (mWeight.equals(dataList4[d])) {
                    pickerscrlllviewWeight.setInitPosition(d);
                    break;
                }
            }
        } else {
            pickerscrlllviewWeight.setInitPosition(30);
        }

        pickerscrlllviewAge.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                mPickContentAge = list1.get(index);
            }
        });

        pickerscrlllviewGender.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                mPickContentGender = list2.get(index);
            }
        });

        pickerscrlllviewHeight.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                mPickContentHeight = list3.get(index);
            }
        });

        pickerscrlllviewWeight.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                mPickContentWeight = list4.get(index);
            }
        });

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

    private void updateSchedulUI(boolean isShowFlag) {
        if (isShowFlag) {
            rl_udt_content.setVisibility(View.VISIBLE);
            tv_loading.setVisibility(View.INVISIBLE);
        } else {
            rl_udt_content.setVisibility(View.GONE);
            tv_loading.setVisibility(View.VISIBLE);
        }
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
                    tv_loading.setText("加载中");
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
                CustomLog.d(TAG, "YUYUE,onFail,statusCode:" + String.valueOf(statusCode)
                        + " statusInfo" + statusInfo);
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(mContext, statusInfo, Toast.LENGTH_SHORT);
                }
                CustomLog.i(TAG, "requestTranData::失败");
            }
        };
        tfScheduls.getscheduls(AccountManager.getInstance(mContext).getMdsToken(), mSelectedDepartmentId, time);
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


    private void showArrangementDialog() {
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
                    String month1;
                    String day1;
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
                            timeContentView.setText(String.valueOf(currentMonth)
                                    + mContext.getString(R.string.reserve_treatment_month)
                                    + String.valueOf(currentDay)
                                    + mContext.getString(R.string.reserve_treatment_day)+" " + schedName+" " + deptName+" " + doctorName);
                        } else {
                            timeContentView.setText(String.valueOf(currentMonth)
                                    + mContext.getString(R.string.reserve_treatment_month)
                                    + String.valueOf(currentDay)
                                    + mContext.getString(R.string.reserve_treatment_day)+" " + schedName+" "+ deptName);
                        }
                        timeContentView.setTextColor(getResources().getColor(R.color.black));
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

    private void getDepartmentList() {
        final HPUGetDepartments gd = new HPUGetDepartments() {
            @Override
            protected void onSuccess(List<DepartmentInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "HPUGetInstitutions,onSuccess");
                CustomLog.d(TAG, "responseContent.size():" + String.valueOf(responseContent.size()));
                departmentInfoList = responseContent;
                if (responseContent != null && responseContent.size() > 0) {
                    showPickerDialog(null, null, departmentInfoList, null, "department");
                } else {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_no_department), Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG, "HPUGetDepartments,onFail,statusCode:" + String.valueOf(statusCode)
                        + " statusInfo" + statusInfo);
                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(YuYueZhuanZhenActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(YuYueZhuanZhenActivity.this,
                            mContext.getString(R.string.reserve_treatment_get_data_failed), Toast.LENGTH_LONG);
                }
            }
        };
        gd.getdepartments(AccountManager.getInstance(mContext).getMdsToken(), mSelectedInstitutionId);
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

    private void getHospitalList() {
        final HPUGetInstitutions gi = new HPUGetInstitutions() {
            @Override
            protected void onSuccess(List<InstitutionInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "HPUGetInstitutions,onSuccess");
                CustomLog.d(TAG, "responseContent.size():" + String.valueOf(responseContent.size()));
                institutionInfoList = responseContent;
                if (responseContent != null && responseContent.size() > 0) {
                    showPickerDialog(null, institutionInfoList, null, null, "hospital");
                } else {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_no_hospital), Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG, "HPUGetInstitutions,onFail,statusCode:" + String.valueOf(statusCode)
                        + " statusInfo" + statusInfo);
                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(YuYueZhuanZhenActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(YuYueZhuanZhenActivity.this,
                            mContext.getString(R.string.reserve_treatment_get_data_failed), Toast.LENGTH_LONG);
                }
            }
        };
        gi.getinstitution(AccountManager.getInstance(mContext).getMdsToken(), mSelectedDtId);
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

    private void getDTList() {
        final HPUGetDTlist gdt = new HPUGetDTlist() {
            @Override
            protected void onSuccess(List<DTInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "HPUGetDTlist,onSuccess");
                CustomLog.d(TAG, "responseContent.size():" + String.valueOf(responseContent.size()));
                dtInfoList = responseContent;

                if (responseContent != null && responseContent.size() > 0) {
                    showPickerDialog(dtInfoList, null, null, null, "hps");
                } else {
                    CustomToast.show(mContext, mContext.getString(R.string.reserve_treatment_no_hps), Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG, "HPUGetDTlist,onFail,statusCode:" + String.valueOf(statusCode)
                        + " statusInfo" + statusInfo);
                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(YuYueZhuanZhenActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(YuYueZhuanZhenActivity.this,
                            mContext.getString(R.string.reserve_treatment_get_data_failed), Toast.LENGTH_LONG);
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

    private void showPickerDialog(final List<DTInfo> list1,
                                  final List<InstitutionInfo> list2,
                                  final List<DepartmentInfo> list3,
                                  final List<CertypeInfo> list4, final String type) {
        dialog = new AlertDialog.Builder(this).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_arrangement_diagram_picker);
        pickerTitleTextView = (TextView) window.findViewById(R.id.tv_arrangement_diagram_picker_title);
        pickerscrlllview = (ListView) window.findViewById(R.id.request_pickerscrlllview);
        pickerCancelView = (TextView) window.findViewById(R.id.iv_request_picker_cancel);
        mPickContent = null;

        if (type.equals("hps")) {
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
        } else if (type.equals("hospital")) {
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
            setDialogHeight(list2.size(), count);
        } else if (type.equals("department")) {
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
            setDialogHeight(list3.size(), count);
        }

        DialogAdatper adapter = new DialogAdatper(YuYueZhuanZhenActivity.this, list1, list2, list3, list4, type);
        pickerscrlllview.setAdapter(adapter);

        pickerscrlllview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type.equals("hps")) {
                    if (mSelectedDtId == null || !mSelectedDtId.equals(list1.get(position).getId())) {
                        mSelectedInstitutionId = null;
                        hospitalContentView.setText(mContext.getString(R.string.reserve_treatment_select_hospital));
                        hospitalContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                        mSelectedDepartmentId = null;
                        departmentContentView.setText(mContext.getString(R.string.reserve_treatment_select_department));
                        departmentContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                        timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                        timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                    }
                    mPickContent = list1.get(position).getName();
                    mSelectedDtId = list1.get(position).getId();
                    hpsContentView.setText(mPickContent);
                    hpsContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                } else if (type.equals("hospital")) {
                    if (mSelectedInstitutionId == null || !mSelectedInstitutionId.equals(list2.get(position).getId())) {
                        mSelectedDepartmentId = null;
                        departmentContentView.setText(mContext.getString(R.string.reserve_treatment_select_department));
                        departmentContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                        timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                        timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                    }
                    mPickContent = list2.get(position).getName();
                    mSelectedInstitutionId = list2.get(position).getId();
                    hospitalContentView.setText(mPickContent);
                    hospitalContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                } else if (type.equals("department")) {
                    if (mSelectedDepartmentId == null || !mSelectedDepartmentId.equals(list3.get(position).getId())) {
                        timeContentView.setText(mContext.getString(R.string.reserve_treatment_schedule));
                        timeContentView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                    }
                    mPickContent = list3.get(position).getName();
                    mSelectedDepartmentId = list3.get(position).getId();
                    departmentContentView.setText(mPickContent);
                    departmentContentView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }
                dialog.dismiss();
            }
        });

        pickerCancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(true);// 使除了dialog以外的地方不能被点击
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
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
            if (mType.equals("hps")) {
                return mList1.size();
            } else if (mType.equals("hospital")) {
                return mList2.size();
            } else if (mType.equals("department")) {
                return mList3.size();
            } else if (mType.equals("certype")) {
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
            DialogAdatper.ViewHolder holder = new DialogAdatper.ViewHolder();
            if (null == convertView) {
                convertView = mInflater.inflate(R.layout.reserve_dialog_item, null);
                holder.dialogText = (TextView) convertView.findViewById(R.id.tv_reserve_dialog_text);
                holder.mView = (ImageView) convertView.findViewById(R.id.mView);
                convertView.setTag(holder);
            } else {
                holder = (DialogAdatper.ViewHolder) convertView.getTag();
            }

            if (mType.equals("hps")) {
                holder.dialogText.setText(mList1.get(position).getName());
                if (mList1!=null&&mList1.size()>0){
                    if (position==mList1.size()-1){
                        holder.mView.setVisibility(View.GONE);
                    }else {
                        holder.mView.setVisibility(View.VISIBLE);
                    }
                }
            } else if (mType.equals("hospital")) {
                holder.dialogText.setText(mList2.get(position).getName());
                if (mList2!=null&&mList2.size()>0){
                    if (position==mList2.size()-1){
                        holder.mView.setVisibility(View.GONE);
                    }else {
                        holder.mView.setVisibility(View.VISIBLE);
                    }
                }
            } else if (mType.equals("department")) {
                holder.dialogText.setText(mList3.get(position).getName());
                if (mList3!=null&&mList3.size()>0){
                    if (position==mList3.size()-1){
                        holder.mView.setVisibility(View.GONE);
                    }else {
                        holder.mView.setVisibility(View.VISIBLE);
                    }
                }
            } else if (mType.equals("certype")) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = 100)
    public void openCameraSuccess() {
        camera();
    }

    @PermissionFail(requestCode = 100)
    public void openCameraFail() {
        openAppDetails(getString(R.string.no_photo_permission));
    }

    private void openAppDetails(String tip) {
        final CustomDialog dialog = new CustomDialog(YuYueZhuanZhenActivity.this);
        dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dialog.dismiss();
            }
        });
        dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                dialog.dismiss();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + YuYueZhuanZhenActivity.this.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    CustomLog.d(TAG, "跳转到设置权限界面异常 Exception：" + ex.getMessage());
                }
            }
        });
        dialog.setTip(tip + getString(R.string.permission_setting));
        dialog.setCenterBtnText(getString(R.string.iknow));
        dialog.setOkBtnText(getString(R.string.permission_handsetting));
        dialog.show();
    }

    private void camera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            initHeadIconFile();
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            Uri imageUri;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                imageUri = Uri.fromFile(headIconFile);
            } else {
                imageUri = FileProvider.getUriForFile(this, "com.jph.takephoto.fileprovider",
                        headIconFile);//通过FileProvider创建一个content类型的Uri
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, CAMERA);
        } else {
            CustomToast.show(getApplicationContext(), getString(R.string.open_save_per),
                    Toast.LENGTH_SHORT);
        }
    }

    private void initHeadIconFile() {
        headIconFile = new File(HEAD_ICON_DIC);
        if (!headIconFile.exists()) {
            headIconFile.mkdirs();
        }
        headIconFile = new File(HEAD_ICON_DIC, "nube_photo" + System.currentTimeMillis() + ".jpg");
    }
}
