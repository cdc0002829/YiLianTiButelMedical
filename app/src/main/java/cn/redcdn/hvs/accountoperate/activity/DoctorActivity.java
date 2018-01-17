package cn.redcdn.hvs.accountoperate.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.redcdn.datacenter.cdnuploadimg.CdnUploadDataInfo;
import cn.redcdn.datacenter.medicalcenter.MDSAppGetCitys;
import cn.redcdn.datacenter.medicalcenter.MDSAppGetDepartments;
import cn.redcdn.datacenter.medicalcenter.MDSAppGetHospitals;
import cn.redcdn.datacenter.medicalcenter.MDSAppGetProvinces;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchDepartments;
import cn.redcdn.datacenter.medicalcenter.MDSAppSearchHospitals;
import cn.redcdn.datacenter.medicalcenter.MDSAppSubmitUserInf;
import cn.redcdn.datacenter.medicalcenter.data.MDSCityInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSDepartmentInfoA;
import cn.redcdn.datacenter.medicalcenter.data.MDSDepartmentInfoB;
import cn.redcdn.datacenter.medicalcenter.data.MDSHospitalInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSProviceInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.adapter.DepartmentBAdapter;
import cn.redcdn.hvs.accountoperate.adapter.DepartmentSelectAdapter;
import cn.redcdn.hvs.accountoperate.adapter.HospitalSelectAdapter;
import cn.redcdn.hvs.accountoperate.adapter.PositionSelectAdapter;
import cn.redcdn.hvs.accountoperate.adapter.SearchAdapter;
import cn.redcdn.hvs.accountoperate.adapter.SearchOtherAdapter;
import cn.redcdn.hvs.accountoperate.info.Department;
import cn.redcdn.hvs.accountoperate.info.Position;
import cn.redcdn.hvs.accountoperate.info.Province;
import cn.redcdn.hvs.cdnmanager.UploadManager;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.im.view.CustomDialog1;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.hvs.profiles.activity.ClipPictureActivity;
import cn.redcdn.hvs.profiles.helper.DocumentsHelper;
import cn.redcdn.hvs.profiles.listener.DisplayImageListener;
import cn.redcdn.hvs.util.BitmapUtils;
import cn.redcdn.hvs.util.CameraImageDialog;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

import static android.media.MediaRecorder.VideoSource.CAMERA;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_ACCOUNT_IS_EXISTED;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.R.id.doctor_hospital_select_title;


/**
 * Created by thinkpad on 2017/2/21.
 */
public class DoctorActivity extends cn.redcdn.hvs.base.BaseActivity {
    public boolean from = false;
    public boolean state = false;
    public int selectState = 0;
    public static final int SELECT_HOSPITAL = 1;
    public static final int SELECT_DEPARTMENT = 2;
    public static final int SELECT_PROFESSINAL = 3;
    public static final int FIND_DEPARTMENT = 4;
    private static final int FIND_HOSPITAL = 5;
    private EditText ET_name, ET_officeNumber, hospital_search, department_search;
    private TextView TV_hospital, TV_office, TV_jobName, video_number, TV_notice, name_text_hintname, name_text_hintnameafter, hospital_select_title, department_select_title;
    private Button btn_tojobName, btn_toOffice, btn_tohospital, register_back, btn_next, hospital_backbtn, hospital_closebtn, department_backbnt, department_closebtn;
    protected final String TAG = getClass().getName();
    private CameraImageDialog cid;
    private ImageView inputimage;
    private RoundImageView btn_head;
    private String croppedIconFilepath = null;// 压缩后图片位置
    private String inputdate = "";
    private File headIconFile = null;// 相册或者拍照保存的文件
    private final int IMAGE_CODE = 0;
    private final String IMAGE_TYPE = "image/*";
    public static final String KEY_FILE_ABSOLUTELY = "key_file_absolutely";
    public static final String KEY_FILE_CROPPEDICON_PATH = "key_file_croppedicon_path";
    private File croppedIconFile;//压缩后文件
    private TextWatcher mTextWatcher;
    private TextWatcher mTextWatcher1;
    private int editStart;
    private int editStart1;
    private int editEnd1;
    private int editEnd;
    private int MAX_COUNT = 16;
    private LinearLayout name;
    private LinearLayout hospital_select;
    private LinearLayout doctor_linearlayout;
    private ListView listView;
    private HospitalSelectAdapter adapter_Province;
    private Province pro1;
    private Province pro2;
    private Province pro3;
    private EditText hospitaledit;

    private LinearLayout department_select;
    private DepartmentSelectAdapter adapter_Department;
    private DepartmentBAdapter adapterb_Department;
    private EditText departmentedit;
    private Department dep1;

    private LinearLayout position_select;
    private ArrayList<Position> arrayList_position;
    private PositionSelectAdapter adapter_position;
    private Position position;
    private LinearLayout maxLinearlayout;


    private RelativeLayout title;
    private TitleBar titleBar;
    private String id;
    private String token = AccountManager.getInstance(MedicalApplication.shareInstance()).getMdsToken();
    private int countid;
    private int close_hospital = 0;
    private int hospital_nowselect = 1;
    private int departmrnt_nowselect = 1;
    private int position_nowselect = 1;
    private int city_twice_enter = 0;
    private int hospital_twice_enter = 0;
    private ArrayList<Province> listprovince;
    private ArrayList<Province> listcity;
    private ArrayList<Province> listhospital;
    private String hospital_province_text, hospital_city_text, hospital_text;
    private String departmenta_text, departmentb_text;
    private String position_text;
    private List<MDSProviceInfo> ProvinceInfo;
    private List<MDSCityInfo> CityInfo;
    private List<MDSHospitalInfo> HospitalInfo;

    private List<MDSDepartmentInfoA> departmentainfo;
    private List<MDSDepartmentInfoB> departmentbinfo;

    public static final String HEAD_ICON_DIC = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "ipNetPhone"
            + File.separator + "headIcon";

    private DisplayImageListener mDisplayImageListener = null;
    private LinearLayout doctorNumberLl;
    private LinearLayout chooseHos;
    private String hospitald;
    private String hospital_tex;
    private CardView cd1;
    private CardView cd2;
    private LinearLayout officeLl;
    private LinearLayout jobLl;
    private List<MDSDepartmentInfoB> infoBlist;
    private SearchOtherAdapter searchAdapterOther;

    private File mcroppedIconFile = null; //裁剪后的头像文件

    public static final String CROPPED_ICON_FILE = "cropped_head_icon.jpg";
    /**
     * 拍照裁剪照片requestcode 3:相机  4:相册 5:裁剪后的照片
     */
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final int REQUEST_ALBUM_PHOTO = 4;
    private static final int PHOTO_PICKED_WITH_DATA = 5;
    private String workType;
    private LinearLayout otherHospitalLl;
    private ImageView line;
    private EditText hospitalOtherET;
    private String nameOld;
    private String hospitalOld;
    private String departmentOld;
    private String positionOld;
    private String officeTelOld;
    private String headUrlOld;
    private String hospitalOtherOld;
    private String remarks;
    private String workUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor);
        //初始化布局
        titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.finish));
        titleBar.setTitleTextColor(Color.BLACK);
        titleBar.setBack("", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from) {
                    String nameNew = ET_name.getText().toString();
                    String hospitalNew = TV_hospital.getText().toString();
                    String departmentNew = TV_office.getText().toString();
                    String positionNew = TV_jobName.getText().toString();
                    String officeTelNew = ET_officeNumber.getText().toString();
                    String hospitalOtherNew = hospitalOtherET.getText().toString();
                    String headUrlNew = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                    if (nameOld.equals(nameNew) && hospitalOld.equals(hospitalNew) && departmentOld.equals(departmentNew) && positionOld.equals(positionNew) && officeTelOld.equals(officeTelNew) && headUrlOld.equals(headUrlNew) && hospitalOtherOld.equals(hospitalOtherNew)) {
                        DoctorActivity.this.finish();
                    } else {
                        CustomDialog1.Builder builder = new CustomDialog1.Builder(DoctorActivity.this);
                        builder.setMessage(getString(R.string.giveup) + "?");
                        builder.setPositiveButton(getString(R.string.give_up), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                DoctorActivity.this.finish();
                            }
                        });
                        builder.setNegativeButton(getString(R.string.countinue),
                                new android.content.DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.create().show();
                    }
                } else {
                    DoctorActivity.this.finish();
                }
            }
        });
        initView();
        if (savedInstanceState != null) {
            String path = (String) savedInstanceState.getString(KEY_FILE_ABSOLUTELY);
            String croppedIconPath = (String) savedInstanceState
                    .getString(KEY_FILE_CROPPEDICON_PATH);
            if (!TextUtils.isEmpty(path)) {
                headIconFile = new File(path);
            }
            if (!TextUtils.isEmpty(croppedIconPath)) {
                croppedIconFile = new File(croppedIconPath);
            }
        }
        mDisplayImageListener = new DisplayImageListener();
        Intent intent = getIntent();
        if (intent != null) {
            from = intent.getBooleanExtra("from_modify", false);
            if (from) {
                nameOld = ET_name.getText().toString();
                hospitalOld = TV_hospital.getText().toString();
                departmentOld = TV_office.getText().toString();
                positionOld = TV_jobName.getText().toString();
                officeTelOld = ET_officeNumber.getText().toString();
                headUrlOld = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                hospitalOtherOld = hospitalOtherET.getText().toString();
            }
        }
    }

    private void initView() {
        line = (ImageView) findViewById(R.id.line);
        chooseHos = (LinearLayout) findViewById(R.id.choose_hos);
        otherHospitalLl = (LinearLayout) findViewById(R.id.other_hospital);
        hospitalOtherET = (EditText) findViewById(R.id.TV_hospital_other);
        officeLl = (LinearLayout) findViewById(R.id.office_ll);
        officeLl.setOnClickListener(mbtnHandleEventListener);
        jobLl = (LinearLayout) findViewById(R.id.job_ll);
        jobLl.setOnClickListener(mbtnHandleEventListener);
        chooseHos.setOnClickListener(mbtnHandleEventListener);
        hospital_search = (EditText) findViewById(R.id.doctor_hospital_select_edit);
        hospital_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    state = true;
                    selectState = FIND_HOSPITAL;
                    notifyStartSearching(hospital_search.getText().toString());
                }
                return true;
            }
        });
        hospital_select_title = (TextView) findViewById(doctor_hospital_select_title);
        hospital_backbtn = (Button) findViewById(R.id.hospital_backbtn);
        hospital_closebtn = (Button) findViewById(R.id.hospital_closebtn);
        department_select_title = (TextView) findViewById(R.id.doctor_department_select_title);
        department_search = (EditText) findViewById(R.id.doctor_department_select_edit);
        department_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    state = true;
                    selectState = FIND_DEPARTMENT;
                    notifyStartSearchingOne(department_search.getText().toString());
                }
                return true;
            }
        });
        department_backbnt = (Button) findViewById(R.id.department_backbnt);
        department_closebtn = (Button) findViewById(R.id.department_closebtn);
        title = (RelativeLayout) findViewById(R.id.doctor_titlebar);
        name = (LinearLayout) findViewById(R.id.doctor_include_name);
        doctor_linearlayout = (LinearLayout) findViewById(R.id.doctor_linearlayout);
        hospital_select = (LinearLayout) findViewById(R.id.hospital_select);
        maxLinearlayout = (LinearLayout) findViewById(R.id.maxLinearlayout);
        name_text_hintname = (TextView) name.findViewById(R.id.name_hint_name);
        name_text_hintnameafter = (TextView) name.findViewById(R.id.name_hint_nameafter);
        ET_name = (EditText) name.findViewById(R.id.ET_name);
        String nickName = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getNickName();
        String workUnitType = getIntent().getStringExtra("workUnitType");
        workType = getIntent().getStringExtra("workType");
        if (!nickName.equals("") && workUnitType.equals(String.valueOf(1))) {
            ET_name.setText(nickName);
            name_text_hintname.setVisibility(View.GONE);
            name_text_hintnameafter.setVisibility(View.GONE);
        }
        department_select = (LinearLayout) findViewById(R.id.department_select);
        position_select = (LinearLayout) findViewById(R.id.position_select);
        doctorNumberLl = (LinearLayout) findViewById(R.id.doctor_number_tv);
        doctorNumberLl.setOnClickListener(mbtnHandleEventListener);
        ((TextView) (findViewById(R.id.doctor_number_tv).findViewById(R.id.video_number))).setText(AccountManager.getInstance(MedicalApplication.context).getNube());
        Editable etext = ET_name.getText();
        Selection.setSelection(etext, etext.length());
        String etname = ET_name.getText().toString();
        if (etname.equals(null)) {
            name_text_hintname.setVisibility(View.VISIBLE);
            name_text_hintnameafter.setVisibility(View.VISIBLE);
        }

        ET_officeNumber = (EditText) findViewById(R.id.ET_officeNumber);
        String officTel = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getOfficTel();
        if (!officTel.equals("") && workUnitType.equals(String.valueOf(1))) {
            ET_officeNumber.setText(officTel);
        }
        ET_officeNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        ET_officeNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        workUnit = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getWorkUnit();
        TV_hospital = (TextView) findViewById(R.id.TV_hospital);
        if (!workUnit.equals("") && workUnitType.equals(String.valueOf(1))) {
            TV_hospital.setText(workUnit);
        }
        String department = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getDepartment();
        TV_office = (TextView) findViewById(R.id.TV_office);
        if (!department.equals("") && workUnitType.equals(String.valueOf(1))) {
            TV_office.setText(department);
        }
        TV_jobName = (TextView) findViewById(R.id.TV_jobName);
        String professional = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getProfessional();
        if (!professional.equals("") && workUnitType.equals(String.valueOf(1))) {
            TV_jobName.setText(professional);
        }
        remarks = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getRemarks();
        if (!remarks.equals("") && workUnitType.equals(String.valueOf(1))) {
            line.setVisibility(View.VISIBLE);
            otherHospitalLl.setVisibility(View.VISIBLE);
            hospitalOtherET.setText(remarks);
        }
        TV_notice = (TextView) findViewById(R.id.TV_notice);
        video_number = (TextView) findViewById(R.id.video_number);


        btn_tojobName = (Button) findViewById(R.id.btn_tojobName);
        btn_toOffice = (Button) findViewById(R.id.btn_toOffice);
        btn_tohospital = (Button) findViewById(R.id.btn_tohospital);
        register_back = (Button) findViewById(R.id.back_btn);
        btn_head = (RoundImageView) findViewById(R.id.btn_head);
        showHead(AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl());
        btn_next = (Button) findViewById(R.id.btn_doctor_next);

        register_back.setOnClickListener(mbtnHandleEventListener);
        btn_next.setOnClickListener(mbtnHandleEventListener);


        listprovince = new ArrayList<Province>();
        listcity = new ArrayList<Province>();
        listhospital = new ArrayList<Province>();


        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                name_text_hintname.setVisibility(View.GONE);
                name_text_hintnameafter.setVisibility(View.GONE);
//                if (ET_name.getText().toString().isEmpty()) {
//                    btn_next.setBackgroundResource(R.drawable.button_btn_notclick);
//                } else {
//                    btn_next.setClickable(true);
//                    btn_next.setBackgroundResource(R.drawable.button_selector);
//                }
                editStart = ET_name.getSelectionStart();
                editEnd = ET_name.getSelectionEnd();
                ET_name.removeTextChangedListener(mTextWatcher);
                while (calculateLength(editable.toString()) > MAX_COUNT) {
                    editable.delete(editStart - 1, editEnd);
                    editStart--;
                    editEnd--;
                }
                ET_name.setSelection(editStart);
                ET_name.addTextChangedListener(mTextWatcher);
            }
        };
        mTextWatcher1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                if (hospitalOtherET.getText().toString().isEmpty()) {
//                    btn_next.setBackgroundResource(R.drawable.button_btn_notclick);
//                } else {
//                    btn_next.setClickable(true);
//                    btn_next.setBackgroundResource(R.drawable.button_selector);
//                }
                editStart1 = hospitalOtherET.getSelectionStart();
                editEnd1 = hospitalOtherET.getSelectionEnd();
                hospitalOtherET.removeTextChangedListener(mTextWatcher1);
                while (calculateLength(editable.toString()) > 52) {
                    editable.delete(editStart1 - 1, editEnd1);
                    editStart1--;
                    editEnd1--;
                }
                hospitalOtherET.setSelection(editStart1);
                hospitalOtherET.addTextChangedListener(mTextWatcher1);
            }
        };
        ET_name.addTextChangedListener(mTextWatcher);
        hospitalOtherET.addTextChangedListener(mTextWatcher1);
    }

    private void showHead(final String str) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.head)//设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.head)//片加载/解码过程中错误时候显示的图片设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.head)//设置图
                .cacheInMemory(true)//是否緩存都內存中
                .cacheOnDisc(true)//是否緩存到sd卡上
                .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, btn_head,
                options, mDisplayImageListener);
    }

    private void notifyStartSearchingOne(String s) {
        if (s == null || s.length() == 0) {
            CustomToast.show(DoctorActivity.this, getString(R.string.find_no_block), CustomToast.LENGTH_LONG);
            return;
        }
        departmrnt_nowselect = 1;
        listView = (ListView) findViewById(R.id.doctor_department_list);
        departmentedit = (EditText) findViewById(R.id.doctor_department_select_edit);
        department_closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setVisibility(View.VISIBLE);
                hospital_select.setVisibility(View.GONE);
                department_select.setVisibility(View.GONE);
                doctor_linearlayout.setVisibility(View.VISIBLE);
                state = false;
                selectState = 0;
            }
        });
        department_backbnt.setVisibility(View.INVISIBLE);
        department_backbnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                departmrnt_nowselect--;
                if (departmrnt_nowselect == 1) {
                    department_select_title.setText("");
                    department_backbnt.setVisibility(View.GONE);
                    listView.setAdapter(searchAdapterOther);
                }
            }
        });
        department_select_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                departmrnt_nowselect--;
                if (departmrnt_nowselect == 1) {
                    department_select_title.setText("");
                    department_backbnt.setVisibility(View.GONE);
                    listView.setAdapter(searchAdapterOther);
                }
            }
        });
        MDSAppSearchDepartments mdsAppSearchDepartments = new MDSAppSearchDepartments() {
            @Override
            protected void onSuccess(final List<MDSDepartmentInfoA> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                searchAdapterOther = new SearchOtherAdapter(DoctorActivity.this, responseContent);
                searchAdapterOther.notifyDataSetChanged();
                listView.setAdapter(searchAdapterOther);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        if (departmrnt_nowselect == 1) {
                            departmrnt_nowselect++;
                            department_backbnt.setVisibility(View.VISIBLE);
                            String class_a_departmentName = responseContent.get(position).getClass_a_departmentName();
                            department_select_title.setVisibility(View.VISIBLE);
                            department_select_title.setText(class_a_departmentName);
                            infoBlist = responseContent.get(position).getInfoBlist();
                            DepartmentBAdapter adapterbDepartment = new DepartmentBAdapter(infoBlist, DoctorActivity.this);
                            adapterbDepartment.notifyDataSetChanged();
                            listView.setAdapter(adapterbDepartment);
                        } else if (departmrnt_nowselect == 2) {
                            String departmentName = infoBlist.get(position).getDepartmentName();
                            TV_office.setText(departmentName);
                            department_select_title.setVisibility(View.VISIBLE);
                            department_select_title.setText(departmentName);
                            title.setVisibility(View.VISIBLE);
                            hospital_select.setVisibility(View.GONE);
                            department_select.setVisibility(View.GONE);
                            position_select.setVisibility(View.GONE);
                            doctor_linearlayout.setVisibility(View.VISIBLE);
                            state = false;
                        }
                    }
                });
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DoctorActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(DoctorActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };
        DoctorActivity.this.showLoadingView(getString(R.string.searching), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_search), Toast.LENGTH_SHORT);
            }
        });
        mdsAppSearchDepartments.appSearchDepartment(AccountManager.getInstance(MedicalApplication.getContext()).getAccountInfo().getToken(), hospitald, s);
    }

    private void notifyStartSearching(String s) {
        if (s == null || s.length() == 0) {
            CustomToast.show(DoctorActivity.this, getString(R.string.find_no_block), CustomToast.LENGTH_LONG);
            return;
        }
        listView = (ListView) findViewById(R.id.doctor_hospital_list);
        hospitaledit = (EditText) findViewById(R.id.doctor_hospital_select_edit);

        hospital_closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setVisibility(View.VISIBLE);
                hospital_select.setVisibility(View.GONE);
                doctor_linearlayout.setVisibility(View.VISIBLE);
                state = false;
                selectState = 0;
            }
        });
        hospital_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hospital_nowselect--;
                if (hospital_nowselect == 1) {
                    adapter_Province.setArrayList(listprovince);
                    hospital_select_title.setText("");
                    hospital_backbtn.setVisibility(View.GONE);
                } else if (hospital_nowselect == 2) {
                    adapter_Province.setArrayList(listcity);
                    hospital_select_title.setText(hospital_province_text);
                }
            }
        });
        hospital_select_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hospital_nowselect--;
                if (hospital_nowselect == 1) {
                    adapter_Province.setArrayList(listprovince);
                    hospital_select_title.setText("");
                    hospital_backbtn.setVisibility(View.GONE);
                } else if (hospital_nowselect == 2) {
                    adapter_Province.setArrayList(listcity);
                    hospital_select_title.setText(hospital_province_text);
//                    hospital_search.setText("搜索" + hospital_province_text + "的医院");
                }
            }
        });
        hospital_select_title.setVisibility(View.INVISIBLE);
        hospital_backbtn.setVisibility(View.INVISIBLE);
        MDSAppSearchHospitals mdsAppSearchHospitals = new MDSAppSearchHospitals() {
            @Override
            protected void onSuccess(final List<MDSHospitalInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                SearchAdapter searchAdapter = new SearchAdapter(DoctorActivity.this, responseContent);
                searchAdapter.notifyDataSetChanged();
                listView.setAdapter(searchAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        hospitald = responseContent.get(position).getHospitalId();
                        hospital_tex = responseContent.get(position).getHospitalName();
                        TV_hospital.setText(hospital_tex);
                        CustomLog.d(TAG, "hospital信息" + TV_hospital.getText().toString());
                        title.setVisibility(View.VISIBLE);
                        hospital_select.setVisibility(View.GONE);
                        department_select.setVisibility(View.GONE);
                        position_select.setVisibility(View.GONE);
                        doctor_linearlayout.setVisibility(View.VISIBLE);
                        TV_office.setText("");
                        state = false;
                    }
                });
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DoctorActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(DoctorActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
//                CustomToast.show(getApplicationContext(), "搜索失败", Toast.LENGTH_SHORT);
            }
        };
        DoctorActivity.this.showLoadingView(getString(R.string.searching), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_search), Toast.LENGTH_SHORT);
            }
        });
        mdsAppSearchHospitals.appSearchHospitals(AccountManager.getInstance(MedicalApplication.getContext()).getAccountInfo().getToken(), s);


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

    @Override
    public void todoClick(int i) {
        switch (i) {
            case R.id.choose_hos://跳转到医院选择界面
                state = true;
                CustomLog.e(TAG, "跳转到医院选择界面");
                selectState = SELECT_HOSPITAL;
                hospital_nowselect = 1;
                hospital_show();
                break;
            case R.id.office_ll://跳转到科室选择界面
                state = true;
                selectState = SELECT_DEPARTMENT;
                departmrnt_nowselect = 1;
//                if (hospitald == null) {
//                    CustomToast.show(DoctorActivity.this, "请先选择医院", CustomToast.LENGTH_SHORT);
//                } else {
//                    department_show();
//                }
                if (TextUtils.isEmpty(TV_hospital.getText().toString())) {
                    CustomToast.show(DoctorActivity.this, getString(R.string.choose_hospital_first), CustomToast.LENGTH_SHORT);
                } else {
                    department_show();
                }
                break;
            case R.id.job_ll://跳转到职称选择界面
                state = true;
                selectState = SELECT_PROFESSINAL;
                position_show();
                break;
            case R.id.back_btn://返回到选择身份类型界面
                finish();
                break;
            case R.id.doctor_number_tv://点击头像
                showDialog();
                break;
            case R.id.btn_doctor_next:
                if (from) {
                    String name1 = ET_name.getText().toString();
                    String hospital1 = TV_hospital.getText().toString();
                    String department1 = TV_office.getText().toString();
                    String position1 = TV_jobName.getText().toString();
                    String officeTel1 = ET_officeNumber.getText().toString();
                    String headUrl1 = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                    String hospitalOther1 = hospitalOtherET.getText().toString();
                    if (nameOld.equals(name1) && hospitalOld.equals(hospital1) && departmentOld.equals(department1) && positionOld.equals(position1) && officeTelOld.equals(officeTel1) && headUrlOld.equals(headUrl1) && hospitalOtherOld.equals(hospitalOther1)) {
                        DoctorActivity.this.finish();
                    } else {
                        String name = ET_name.getText().toString().trim();
                        String hospital = TV_hospital.getText().toString();
                        String department = TV_office.getText().toString();
                        String position = TV_jobName.getText().toString();
                        String officeTel = ET_officeNumber.getText().toString().trim();
                        String headUrl = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                        if (headUrl == null || headUrl.length() == 0) {
                            AccountManager.getInstance(MedicalApplication.context).getAccountInfo().setHeadThumUrl("http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg");
                        }
                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(hospital) || TextUtils.isEmpty(department)) {
                            CustomToast.show(DoctorActivity.this, getString(R.string.content_no_empty), Toast.LENGTH_LONG);
                            return;
                        }
                        if (!officeTel.equals(getString(R.string.unsetting))) {
                            if (!officeTel.equals("") && !isPhoneNumber(officeTel)) {
                                CustomToast.show(DoctorActivity.this, getString(R.string.telephone_form_unright), Toast.LENGTH_LONG);
                                return;
                            }
                        }
                        if (otherHospitalLl.getVisibility() == View.GONE) {
                            perfectInfo();
                        } else {
                            if (TextUtils.isEmpty(hospitalOtherET.getText().toString().trim())) {
                                CustomToast.show(DoctorActivity.this, getString(R.string.remarks_no_empty), Toast.LENGTH_LONG);
                            } else {
                                perfectInfoOther();
                            }
                        }
                    }
                } else {
                    String name = ET_name.getText().toString().trim();
                    String hospital = TV_hospital.getText().toString();
                    String department = TV_office.getText().toString();
                    String position = TV_jobName.getText().toString();
                    String officeTel = ET_officeNumber.getText().toString().trim();
                    String headUrl = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                    if (headUrl == null || headUrl.length() == 0) {
                        AccountManager.getInstance(MedicalApplication.context).getAccountInfo().setHeadThumUrl("http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg");
                    }
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(hospital) || TextUtils.isEmpty(department)) {
                        CustomToast.show(DoctorActivity.this, getString(R.string.content_no_empty), Toast.LENGTH_LONG);
                        return;
                    }
                    if (!officeTel.equals(getString(R.string.unsetting))) {
                        if (!officeTel.equals("") && !isPhoneNumber(officeTel)) {
                            CustomToast.show(DoctorActivity.this, getString(R.string.telephone_form_unright), Toast.LENGTH_LONG);
                            return;
                        }
                    }
                    if (otherHospitalLl.getVisibility() == View.GONE) {
                        perfectInfo();
                    } else {
                        if (TextUtils.isEmpty(hospitalOtherET.getText().toString())) {
                            CustomToast.show(DoctorActivity.this, getString(R.string.remarks_no_empty), Toast.LENGTH_LONG);
                        } else {
                            perfectInfoOther();
                        }
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
        String expression = "1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    private void perfectInfo() {
        String name = ET_name.getText().toString().trim();
        String hospital = TV_hospital.getText().toString();
        String department = TV_office.getText().toString();
        String position = TV_jobName.getText().toString();
        String officeTel = ET_officeNumber.getText().toString().trim();
        if (position.equals("")) {
            position = getString(R.string.unsetting);
        }
        if (officeTel.equals("")) {
            officeTel = getString(R.string.unsetting);
        }
        String remarks = "";
        //提交审核信息
        submitUserInfo(name, hospital, department, position, officeTel, remarks);
    }

    private void submitUserInfo(String name, String hospital, String department, String position, String officeTel, String remarks) {

        final MDSAppSubmitUserInf submitUserInf = new MDSAppSubmitUserInf() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "审核资料提交成功");
                Intent intent = new Intent(DoctorActivity.this, AuditingActivity.class);
                startActivity(intent);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomLog.d(TAG, "资料审核提交失败 statusCode: " + statusCode + " msg: " + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DoctorActivity.this).tokenAuthFail(statusCode);
                } else if (statusCode == MDS_ACCOUNT_IS_EXISTED) {
                    CustomToast.show(DoctorActivity.this, getString(R.string.your_account_verify), Toast.LENGTH_LONG);
                    Intent intent = new Intent(DoctorActivity.this, LoginActivity.class);
                    startActivity(intent);
                    DoctorActivity.this.finish();
                } else {
                    CustomToast.show(DoctorActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };
        DoctorActivity.this.showLoadingView(getString(R.string.submit_verify), new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        submitUserInf.cancel();
                        CustomToast.show(getApplicationContext(), getString(R.string.cancel_submit_verify), Toast.LENGTH_SHORT);
                    }
                }
        );
        String headUrl = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
        if (headUrl.equals("")) {
            headUrl = "http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg";
        }
        String previewUrl = headUrl;
        int accoutType = 0;
        if (!AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getAccountType().equals("")) {
            accoutType = Integer.valueOf(AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getAccountType());
        }
        String phone = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getMobile();
        String email = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getMail();
        String accessToken = AccountManager.getInstance(MedicalApplication.shareInstance()).getToken();
        String nube = AccountManager.getInstance(MedicalApplication.context).getNube();
        submitUserInf.newappSubmitUserInfo(accessToken, headUrl, previewUrl, name, phone, email, accoutType, nube, hospital, Integer.valueOf(workType), department, position, officeTel, "http://vodtv.butel.com/ac68e4ef3359489bb1e25cb9a9fb80f4.png", "http://vodtv.butel.com/ac68e4ef3359489bb1e25cb9a9fb80f4.png", remarks);
    }

    private void perfectInfoOther() {
        String name = ET_name.getText().toString().trim();
        String hospital = TV_hospital.getText().toString();
        String department = TV_office.getText().toString();
        String position = TV_jobName.getText().toString();
        String officeTel = ET_officeNumber.getText().toString().trim();
        String remarks = hospitalOtherET.getText().toString().trim();
        if (position.equals("")) {
            position = getString(R.string.unsetting);
        }
        if (officeTel.equals("")) {
            officeTel = getString(R.string.unsetting);
        }
        submitUserInfo(name, hospital, department, position, officeTel, remarks);
    }

    private void upLoad(String path) {
        UploadManager.UploadImageListener listener = new UploadManager.UploadImageListener() {
            @Override
            public void onSuccess(CdnUploadDataInfo dataInfo) {
                DoctorActivity.this.removeLoadingView();
                String filepath = dataInfo.getFilepath();
                if (filepath == null) {
                    CustomToast.show(getApplicationContext(), getString(R.string.load_pic_fail),
                            Toast.LENGTH_SHORT);
                    return;
                }
                CustomToast.show(getApplicationContext(), getString(R.string.load_pic_suc),
                        Toast.LENGTH_SHORT);
                cid.dismiss();
                AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().setHeadThumUrl(filepath);
                AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().setHeadPreviewUrl(filepath);
                show(filepath);
            }

            @Override
            public void onProgress(int persent) {

            }

            @Override
            public void onFailed(int statusCode, String msg) {
                DoctorActivity.this.removeLoadingView();
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(DoctorActivity.this, getString(R.string.login_checkNetworkError),
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
                CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_fail) + "=" + statusCode,
                        Toast.LENGTH_SHORT);
            }
        };
        UploadManager.getInstance().uploadImage(new File(path), listener);


        DoctorActivity.this.showLoadingView(getString(R.string.upload_pic_ing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_cacel), Toast.LENGTH_SHORT);
            }
        });

    }


    private void hospital_show() {
        listView = (ListView) findViewById(R.id.doctor_hospital_list);
        hospitaledit = (EditText) findViewById(R.id.doctor_hospital_select_edit);

        hospital_closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setVisibility(View.VISIBLE);
                hospital_select.setVisibility(View.GONE);
                doctor_linearlayout.setVisibility(View.VISIBLE);
                selectState = 0;
                state = false;
            }
        });
        hospital_backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hospital_nowselect--;
                if (hospital_nowselect == 1) {
                    adapter_Province.setArrayList(listprovince);
                    hospital_select_title.setText("");
                    hospital_backbtn.setVisibility(View.GONE);
                } else if (hospital_nowselect == 2) {
                    adapter_Province.setArrayList(listcity);
                    hospital_select_title.setText(hospital_province_text);
                }
            }
        });
        hospital_select_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hospital_nowselect--;
                if (hospital_nowselect == 1) {
                    adapter_Province.setArrayList(listprovince);
                    hospital_select_title.setText("");
                    hospital_backbtn.setVisibility(View.GONE);
                } else if (hospital_nowselect == 2) {
                    adapter_Province.setArrayList(listcity);
                    hospital_select_title.setText(hospital_province_text);
//                    hospital_search.setText("搜索" + hospital_province_text + "的医院");
                }
            }
        });
        hospital_select_title.setVisibility(View.INVISIBLE);
        hospital_backbtn.setVisibility(View.INVISIBLE);
        if (hospital_nowselect == 1) {
            gethospitalprovince();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (hospital_nowselect == 1) {
                    id = ProvinceInfo.get(i).getProviceId();
                    hospital_province_text = ProvinceInfo.get(i).getProviceName();
                    hospital_select_title.setVisibility(View.VISIBLE);
                    hospital_select_title.setText(hospital_province_text);
                    listView.setSelection(0);
//                    hospital_search.setText("搜索" + hospital_province_text + "的医院");
                    gethospitalcity();

                } else if (hospital_nowselect == 2) {
                    id = CityInfo.get(i).getCityId();
                    hospital_city_text = CityInfo.get(i).getCityName();
                    hospital_select_title.setVisibility(View.VISIBLE);
                    hospital_select_title.setText(hospital_city_text);
//                    hospital_search.setText("搜索" + hospital_city_text + "的医院");
                    listView.setSelection(0);
                    gethospital();
                } else if (hospital_nowselect == 3) {
                    hospitald = HospitalInfo.get(i).getHospitalId();
                    hospital_text = HospitalInfo.get(i).getHospitalName();
                    TV_hospital.setText(hospital_text);
                    if (hospital_text != null && hospital_text.length() > 4) {
                        String otherHospital = hospital_text.substring(hospital_text.length() - 4);
                        if (otherHospital.equals("其他医院")) {
                            if (!hospital_text.equals(workUnit)) {
                                hospitalOtherET.setText("");
                            } else {
                                hospitalOtherET.removeTextChangedListener(mTextWatcher1);
                                hospitalOtherET.setText(remarks);
                                hospitalOtherET.addTextChangedListener(mTextWatcher1);
                            }
                            otherHospitalLl.setVisibility(View.VISIBLE);
                            line.setVisibility(View.VISIBLE);
                        } else {
                            otherHospitalLl.setVisibility(View.GONE);
                            line.setVisibility(View.GONE);
                        }
                    }
                    state = false;
                    selectState = 0;
                    CustomLog.d(TAG, "hospital信息" + TV_hospital.getText().toString());
                    title.setVisibility(View.VISIBLE);
                    hospital_select.setVisibility(View.GONE);
                    department_select.setVisibility(View.GONE);
                    position_select.setVisibility(View.GONE);
                    doctor_linearlayout.setVisibility(View.VISIBLE);
                }

            }
        });

    }


    private void department_show() {
        listView = (ListView) findViewById(R.id.doctor_department_list);
        departmentedit = (EditText) findViewById(R.id.doctor_department_select_edit);
        department_closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title.setVisibility(View.VISIBLE);
                hospital_select.setVisibility(View.GONE);
                department_select.setVisibility(View.GONE);
                doctor_linearlayout.setVisibility(View.VISIBLE);
                state = false;
                selectState = 0;
            }
        });

        department_backbnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                departmrnt_nowselect--;
                if (departmrnt_nowselect == 1) {
                    department_select_title.setText("");
                    department_backbnt.setVisibility(View.GONE);
                    listView.setAdapter(adapter_Department);
                }
            }
        });
        department_select_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                departmrnt_nowselect--;
                if (departmrnt_nowselect == 1) {
                    department_select_title.setText("");
                    department_backbnt.setVisibility(View.GONE);
                    listView.setAdapter(adapter_Department);
                }
            }
        });
        department_select_title.setVisibility(View.INVISIBLE);
        department_backbnt.setVisibility(View.INVISIBLE);
        if (departmrnt_nowselect == 1) {
            getdepartmenta();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (departmrnt_nowselect == 1) {
                    countid = i;
                    CustomLog.d(TAG, i + "");
                    getdepartmentb();
                    String departmentName = departmentainfo.get(i).getClass_a_departmentName();
                    department_select_title.setVisibility(View.VISIBLE);
                    department_select_title.setText(departmentName);

                } else if (departmrnt_nowselect == 2) {
                    departmentb_text = departmentbinfo.get(i).getDepartmentName();
                    TV_office.setText(departmentb_text);
                    department_select_title.setVisibility(View.VISIBLE);
                    department_select_title.setText(departmentb_text);
                    title.setVisibility(View.VISIBLE);
                    hospital_select.setVisibility(View.GONE);
                    department_select.setVisibility(View.GONE);
                    position_select.setVisibility(View.GONE);
                    doctor_linearlayout.setVisibility(View.VISIBLE);
                    state = false;
                    selectState = 0;
                }
            }
        });

    }

    private void position_show() {
        titleBar.setTitle(getString(R.string.profession));
        titleBar.setTitleTextColor(Color.BLACK);
//        titleBar.setBack("", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                titleBar.setTitleTextColor(Color.BLACK);
//                titleBar.setTitle("完善资料");
//                titleBar.enableBack();
//                title.setVisibility(View.VISIBLE);
//                hospital_select.setVisibility(View.GONE);
//                department_select.setVisibility(View.GONE);
//                position_select.setVisibility(View.GONE);
//                doctor_linearlayout.setVisibility(View.VISIBLE);
//                state = false;
//                selectState = 0;
//            }
//        });
        listView = (ListView) findViewById(R.id.doctor_position_list);
        arrayList_position = new ArrayList<Position>();
        initposition();
        adapter_position = new PositionSelectAdapter(arrayList_position, DoctorActivity.this);
        listView.setAdapter(adapter_position);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                title.setVisibility(View.VISIBLE);
                titleBar.setTitle(getString(R.string.finish));
                titleBar.setTitleTextColor(Color.BLACK);
                position_text = arrayList_position.get(i).getChoose_Position();
                TV_jobName.setText(position_text);
                hospital_select.setVisibility(View.GONE);
                department_select.setVisibility(View.GONE);
                position_select.setVisibility(View.GONE);
                doctor_linearlayout.setVisibility(View.VISIBLE);
                state = false;
                selectState = 0;
            }
        });
        title.setVisibility(View.VISIBLE);
        hospital_select.setVisibility(View.GONE);
        department_select.setVisibility(View.GONE);
        position_select.setVisibility(View.VISIBLE);
        doctor_linearlayout.setVisibility(View.GONE);

    }

    private void gethospitalprovince() {

        MDSAppGetProvinces provinces = new MDSAppGetProvinces() {
            @Override
            protected void onSuccess(List<MDSProviceInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                listprovince.clear();
                int count = responseContent.size();
                for (int x = 0; x < count; x++) {
                    ProvinceInfo = responseContent;
                    MDSProviceInfo mdsProviceInfo = responseContent.get(x);
                    CustomLog.d(TAG, " Provice" + id);
                    Province pro = new Province();
                    pro.setHospitalprovince(mdsProviceInfo.getProviceName());
                    listprovince.add(pro);
                }
                CustomLog.d(TAG, "listprovince" + listprovince.size());
                adapter_Province = new HospitalSelectAdapter(listprovince, DoctorActivity.this);
                listView.setAdapter(adapter_Province);
                title.setVisibility(View.GONE);
                hospital_select.setVisibility(View.VISIBLE);
                doctor_linearlayout.setVisibility(View.GONE);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomToast.show(DoctorActivity.this, getString(R.string.get_province_fail), Toast.LENGTH_LONG);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DoctorActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(DoctorActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };
        DoctorActivity.this.showLoadingView(getString(R.string.geting_province), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_get_province), Toast.LENGTH_SHORT);
            }
        });
        provinces.appGetProvinces(token);

    }

    private void gethospitalcity() {

        MDSAppGetCitys citys = new MDSAppGetCitys() {
            @Override
            protected void onSuccess(List<MDSCityInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                listcity.clear();
                CityInfo = responseContent;
                int count = responseContent.size();
                for (int x = 0; x < count; x++) {
                    MDSCityInfo mdsCityInfo = responseContent.get(x);
                    Province procity = new Province();
                    procity.setHospitalprovince(mdsCityInfo.getCityName());
                    listcity.add(procity);
                }

                adapter_Province.setArrayList(listcity);

                CustomLog.d(TAG, "listcity" + listcity.size());
                adapter_Province.notifyDataSetChanged();
                hospital_nowselect++;
                hospital_backbtn.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                hospital_select.setVisibility(View.VISIBLE);
                doctor_linearlayout.setVisibility(View.GONE);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomToast.show(DoctorActivity.this, getString(R.string.get_city_fail), Toast.LENGTH_LONG);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DoctorActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(DoctorActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };
        DoctorActivity.this.showLoadingView(getString(R.string.get_city_ing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_get_city), Toast.LENGTH_SHORT);
            }
        });
        citys.appGetCitys(token, id);

    }


    private void gethospital() {
        final MDSAppGetHospitals hospitals = new MDSAppGetHospitals() {
            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomToast.show(DoctorActivity.this, getString(R.string.get_hos_fail), Toast.LENGTH_LONG);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DoctorActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(DoctorActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }

            @Override
            protected void onSuccess(List<MDSHospitalInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                listhospital.clear();
                HospitalInfo = responseContent;
                int count = responseContent.size();

                for (int x = 0; x < count; x++) {
                    MDSHospitalInfo mdsHospitalInfo = responseContent.get(x);
                    Province procity = new Province();
                    procity.setHospitalprovince(mdsHospitalInfo.getHospitalName());
                    listhospital.add(procity);
                }
                adapter_Province.setArrayList(listhospital);
                adapter_Province.notifyDataSetChanged();
                hospital_nowselect++;
                hospital_backbtn.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                hospital_select.setVisibility(View.VISIBLE);
                doctor_linearlayout.setVisibility(View.GONE);
            }
        };
        DoctorActivity.this.showLoadingView(getString(R.string.get_hos_ing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_get_hospital), Toast.LENGTH_SHORT);
            }
        });
        hospitals.appGetHospitals(token, id);

    }


    private void getdepartmenta() {
        MDSAppGetDepartments departments = new MDSAppGetDepartments() {
            @Override
            protected void onSuccess(List<MDSDepartmentInfoA> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                departmentainfo = responseContent;
                adapter_Department = new DepartmentSelectAdapter(departmentainfo, DoctorActivity.this);
                listView.setAdapter(adapter_Department);
                title.setVisibility(View.GONE);
                hospital_select.setVisibility(View.GONE);
                department_select.setVisibility(View.VISIBLE);
                doctor_linearlayout.setVisibility(View.GONE);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomToast.show(DoctorActivity.this, getString(R.string.get_room_fail), Toast.LENGTH_LONG);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(DoctorActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(DoctorActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };
        DoctorActivity.this.showLoadingView(getString(R.string.get_room_ing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_get_room), Toast.LENGTH_SHORT);
            }
        });
        departments.appGetDepartment(token, hospitald);

    }

    private void getdepartmentb() {
        departmentbinfo = departmentainfo.get(countid).getInfoBlist();
        adapterb_Department = new DepartmentBAdapter(departmentbinfo, DoctorActivity.this);
        adapterb_Department.setArrayList(departmentbinfo);
        listView.setAdapter(adapterb_Department);
        department_backbnt.setVisibility(View.VISIBLE);
        title.setVisibility(View.GONE);
        hospital_select.setVisibility(View.GONE);
        department_select.setVisibility(View.VISIBLE);
        doctor_linearlayout.setVisibility(View.GONE);
        departmrnt_nowselect++;
    }


    private void showDialog() {


        cid = new CameraImageDialog(DoctorActivity.this,
                R.style.contact_del_dialog);

        cid.setCameraClickListener(new CameraImageDialog.CameraClickListener() {

            @Override
            public void clickListener() {
                boolean result = CommonUtil.selfPermissionGranted(DoctorActivity.this, Manifest.permission.CAMERA);
                if (!result) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionGen.with(DoctorActivity.this)
                                .addRequestCode(100)
                                .permissions(Manifest.permission.CAMERA)
                                .request();
                    } else {
                        openAppDetails(getString(R.string.no_photo_permission));
                    }
                }else {
                    camera();
                }
            }
        });
        cid.setPhoneClickListener(new CameraImageDialog.PhoneClickListener() {

            @Override
            public void clickListener() {
                photoFile();
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

    private void photoFile() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
            getAlbum.setType(IMAGE_TYPE);
            startActivityForResult(getAlbum, IMAGE_CODE);
        } else {
                CustomToast.show(getApplicationContext(), getString(R.string.open_save_per),
                        Toast.LENGTH_SHORT);
        }
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
                imageUri = FileProvider.getUriForFile(this, "com.jph.takephoto.fileprovider", headIconFile);//通过FileProvider创建一个content类型的Uri
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
        headIconFile = new File(HEAD_ICON_DIC, "nube_photo"
                + System.currentTimeMillis() + ".jpg");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLog.e(TAG, "onActivityResult");
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == CAMERA) {
            CustomLog.d(TAG, "进入拍照后currentMyPid===="
                    + android.os.Process.myPid());
            CustomLog.d(TAG, "onActivityResult..CAMERA..headIconFile.getPath()="
                    + headIconFile.getPath());
            mcroppedIconFile = new File(HEAD_ICON_DIC, CROPPED_ICON_FILE);
            if (mcroppedIconFile.exists()) {
                mcroppedIconFile.delete();
            }
            doCropPhoto(headIconFile);
//            croppedIconFilepath = getThumPath(headIconFile.getPath(), 400);
//            upLoad(getThumPath(headIconFile.getPath(), 400));
        } else {
            if (requestCode == IMAGE_CODE) {
                mcroppedIconFile = new File(HEAD_ICON_DIC, CROPPED_ICON_FILE);
                if (mcroppedIconFile.exists()) {
                    mcroppedIconFile.delete();
                }
                String filePath = "";
                ContentResolver resolver = getContentResolver();
                Uri originalUri = data.getData(); // 获得图片的uri
                if (originalUri != null) {
                    filePath = DocumentsHelper.getPath(DoctorActivity.this,
                            originalUri);
                }

                if (TextUtils.isEmpty(filePath)) {
                    filePath = data.getDataString();
                    if (!TextUtils.isEmpty(filePath) && filePath.startsWith("file:///")) {
                        filePath = filePath.replace("file://", "");
                        try {
                            // java的文件系统是linux,而编码格式是UTF-8的编码格式
                            filePath = URLDecoder.decode(filePath, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (TextUtils.isEmpty(filePath)) {

                } else {
                    doCropPhoto(new File(filePath));
                }
                if (filePath == null || filePath.equalsIgnoreCase("")) {
                    CustomToast.show(getApplicationContext(), getString(R.string.can_not_get_pic_path),
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (BitmapUtils.isImageType(filePath)) {
//                    upLoad(getThumPath(filePath, 400));

                } else
                    CustomToast.show(getApplicationContext(), getString(R.string.file_type_error),
                            Toast.LENGTH_SHORT);
            } else {
                if (requestCode == PHOTO_PICKED_WITH_DATA) {
                    CustomLog.d(TAG, "裁剪照片返回");
                    croppedIconFilepath = getThumPath(mcroppedIconFile.getPath(), 400);
                    upLoad(croppedIconFilepath);
                }
            }
        }
    }

    private void doCropPhoto(File f) {
        CustomLog.d(TAG, "去剪辑这个照片");
        //进入裁剪页面,此处用的是自定义的裁剪页面而不是调用系统裁剪
        Intent intent = new Intent(this, ClipPictureActivity.class);
        intent.putExtra(ClipPictureActivity.IMAGE_PATH_ORIGINAL, f.getAbsolutePath());
        intent.putExtra(ClipPictureActivity.IMAGE_PATH_AFTER_CROP, mcroppedIconFile.getAbsolutePath());

        startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);
    }


    private void initposition() {
        for (int i = 0; i < 5; i++) {
            position = new Position();
            if (i == 0) {
                position.setChoose_Position(getString(R.string.archiater));
                arrayList_position.add(position);
            } else if (i == 1) {
                position.setChoose_Position(getString(R.string.associate_archiater));
                arrayList_position.add(position);
            } else if (i == 2) {
                position.setChoose_Position(getString(R.string.attending_doctor));
                arrayList_position.add(position);
            } else if (i == 3) {
                position.setChoose_Position(getString(R.string.resident_physician));
                arrayList_position.add(position);
            } else if (i == 4) {
                position.setChoose_Position(getString(R.string.other_physicians));
                arrayList_position.add(position);
            }

        }

    }

    private String getThumPath(String oldPath, int bitmapMaxWidth) {
        return BitmapUtils.getThumPath(oldPath, bitmapMaxWidth);
    }

    @Override
    public void onBackPressed() {
        if (from) {
            String nameNew = ET_name.getText().toString();
            String hospitalNew = TV_hospital.getText().toString();
            String departmentNew = TV_office.getText().toString();
            String positionNew = TV_jobName.getText().toString();
            String officeTelNew = ET_officeNumber.getText().toString();
            String hospitalOtherNew = hospitalOtherET.getText().toString();
            String headUrlNew = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
            if (nameOld.equals(nameNew) && hospitalOld.equals(hospitalNew) && departmentOld.equals(departmentNew) && positionOld.equals(positionNew) && officeTelOld.equals(officeTelNew) && headUrlOld.equals(headUrlNew) && hospitalOtherOld.equals(hospitalOtherNew) && hospitalOtherOld.equals(hospitalOtherNew)) {
                if (!state) {
                    super.onBackPressed();
                    DoctorActivity.this.finish();
                } else {
                    if (selectState == SELECT_HOSPITAL) {
                        hospital_nowselect--;
                        if (hospital_nowselect == 1) {
                            adapter_Province.setArrayList(listprovince);
                            hospital_select_title.setText("");
                            hospital_backbtn.setVisibility(View.GONE);
                        } else if (hospital_nowselect == 2) {
                            adapter_Province.setArrayList(listcity);
                            hospital_select_title.setText(hospital_province_text);
                        } else if (hospital_nowselect <= 0) {
                            title.setVisibility(View.VISIBLE);
                            hospital_select.setVisibility(View.GONE);
                            doctor_linearlayout.setVisibility(View.VISIBLE);
                            selectState = 0;
                            state = false;
                        }
                    } else if (selectState == SELECT_DEPARTMENT) {
                        departmrnt_nowselect--;
                        if (departmrnt_nowselect == 1) {
                            department_select_title.setText("");
                            department_backbnt.setVisibility(View.GONE);
                            listView.setAdapter(adapter_Department);
                        } else if (departmrnt_nowselect <= 0) {
                            title.setVisibility(View.VISIBLE);
                            hospital_select.setVisibility(View.GONE);
                            department_select.setVisibility(View.GONE);
                            doctor_linearlayout.setVisibility(View.VISIBLE);
                            state = false;
                            selectState = 0;
                        }
                    } else if (selectState == SELECT_PROFESSINAL) {
                        title.setVisibility(View.VISIBLE);
                        hospital_select.setVisibility(View.GONE);
                        department_select.setVisibility(View.GONE);
                        position_select.setVisibility(View.GONE);
                        doctor_linearlayout.setVisibility(View.VISIBLE);
                        state = false;
                        selectState = 0;
                    } else if (selectState == FIND_DEPARTMENT) {
                        departmrnt_nowselect--;
                        if (departmrnt_nowselect == 1) {
                            department_select_title.setText("");
                            department_backbnt.setVisibility(View.GONE);
                            listView.setAdapter(searchAdapterOther);
                        } else if (departmrnt_nowselect <= 0) {
                            title.setVisibility(View.VISIBLE);
                            hospital_select.setVisibility(View.GONE);
                            department_select.setVisibility(View.GONE);
                            doctor_linearlayout.setVisibility(View.VISIBLE);
                            state = false;
                            selectState = 0;
                        }
                    } else if (selectState == FIND_HOSPITAL) {
                        title.setVisibility(View.VISIBLE);
                        hospital_select.setVisibility(View.GONE);
                        department_select.setVisibility(View.GONE);
                        doctor_linearlayout.setVisibility(View.VISIBLE);
                        selectState = 0;
                        state = false;
                    }

                }
            } else {
                CustomDialog1.Builder builder = new CustomDialog1.Builder(DoctorActivity.this);
                builder.setMessage(getString(R.string.giveup) + "?");
                builder.setPositiveButton(getString(R.string.give_up), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        DoctorActivity.this.finish();
                    }
                });
                builder.setNegativeButton(getString(R.string.countinue),
                        new android.content.DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        } else {
            if (!state) {
                super.onBackPressed();
                DoctorActivity.this.finish();
            } else {
                if (selectState == SELECT_HOSPITAL) {
                    hospital_nowselect--;
                    if (hospital_nowselect == 1) {
                        adapter_Province.setArrayList(listprovince);
                        hospital_select_title.setText("");
                        hospital_backbtn.setVisibility(View.GONE);
                    } else if (hospital_nowselect == 2) {
                        adapter_Province.setArrayList(listcity);
                        hospital_select_title.setText(hospital_province_text);
                    } else if (hospital_nowselect <= 0) {
                        title.setVisibility(View.VISIBLE);
                        hospital_select.setVisibility(View.GONE);
                        doctor_linearlayout.setVisibility(View.VISIBLE);
                        selectState = 0;
                        state = false;
                    }
                } else if (selectState == SELECT_DEPARTMENT) {
                    departmrnt_nowselect--;
                    if (departmrnt_nowselect == 1) {
                        department_select_title.setText("");
                        department_backbnt.setVisibility(View.GONE);
                        listView.setAdapter(adapter_Department);
                    } else if (departmrnt_nowselect <= 0) {
                        title.setVisibility(View.VISIBLE);
                        hospital_select.setVisibility(View.GONE);
                        department_select.setVisibility(View.GONE);
                        doctor_linearlayout.setVisibility(View.VISIBLE);
                        state = false;
                        selectState = 0;
                    }
                } else if (selectState == SELECT_PROFESSINAL) {
                    title.setVisibility(View.VISIBLE);
                    hospital_select.setVisibility(View.GONE);
                    department_select.setVisibility(View.GONE);
                    position_select.setVisibility(View.GONE);
                    doctor_linearlayout.setVisibility(View.VISIBLE);
                    state = false;
                    selectState = 0;
                } else if (selectState == FIND_DEPARTMENT) {
                    departmrnt_nowselect--;
                    if (departmrnt_nowselect == 1) {
                        department_select_title.setText("");
                        department_backbnt.setVisibility(View.GONE);
                        listView.setAdapter(searchAdapterOther);
                    } else if (departmrnt_nowselect <= 0) {
                        title.setVisibility(View.VISIBLE);
                        hospital_select.setVisibility(View.GONE);
                        department_select.setVisibility(View.GONE);
                        doctor_linearlayout.setVisibility(View.VISIBLE);
                        state = false;
                        selectState = 0;
                    }
                } else if (selectState == FIND_HOSPITAL) {
                    title.setVisibility(View.VISIBLE);
                    hospital_select.setVisibility(View.GONE);
                    department_select.setVisibility(View.GONE);
                    doctor_linearlayout.setVisibility(View.VISIBLE);
                    selectState = 0;
                    state = false;
                }

            }
        }
    }

    private void show(final String str) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.head)//设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.head)//片加载/解码过程中错误时候显示的图片设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.head)//设置图
                .cacheInMemory(true)//是否緩存都內存中
                .cacheOnDisc(true)//是否緩存到sd卡上
                .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, btn_head,
                options, mDisplayImageListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CustomLog.e(TAG, "onSaveInstanceState");
        // 针对三星4.1.2 拍照手返回 文件路径丢失，先保存文件路径，然后在oncreate方法进行恢复
        if (headIconFile != null
                && !TextUtils.isEmpty(headIconFile.getAbsolutePath())) {
            outState.putString(KEY_FILE_ABSOLUTELY, headIconFile.getAbsolutePath());
        }
        if (croppedIconFilepath != null
                && !croppedIconFilepath.equalsIgnoreCase("")) {
            outState.putString(KEY_FILE_CROPPEDICON_PATH, croppedIconFilepath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = 100)
    public void openCameraSuccess(){
        camera();
    }

    @PermissionFail(requestCode = 100)
    public void openCameraFail(){
        openAppDetails(getString(R.string.no_photo_permission));
    }

    private void openAppDetails(String tip) {
        final CustomDialog dialog = new CustomDialog(this);
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
                intent.setData(Uri.parse("package:" + DoctorActivity.this.getPackageName()));
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
}
