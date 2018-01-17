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
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.redcdn.datacenter.cdnuploadimg.CdnUploadDataInfo;
import cn.redcdn.datacenter.medicalcenter.MDSAppSubmitUserInf;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
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
import static cn.redcdn.hvs.R.id.name_hint_nameafter;


/**
 * Created by thinkpad on 2017/2/21.
 */
public class MedicalActivity extends cn.redcdn.hvs.base.BaseActivity {
    public boolean from = false;
    private File croppedIconFile;//压缩后文件
    public static final String KEY_FILE_CROPPEDICON_PATH = "key_file_croppedicon_path";
    private String croppedIconFilepath = null;// 压缩后图片位置
    public static final String KEY_FILE_ABSOLUTELY = "key_file_absolutely";
    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;
    private File headIconFile = null;// 相册或者拍照保存的文件
    private Button button_next;
    private LinearLayout medical_name;
    private EditText ET_name, medical_officeTel, medical_position_edit, medical_department_edit, medical_company_edit;
    private TextWatcher mTextWatcher;
    private TextView name_text_hintname;
    private TextView name_text_hintnameafter;
    private int editStart;
    private int editEnd;
    private int MAX_COUNT = 16;
    private LinearLayout medicalNumberLl;
    private CameraImageDialog cid;
    public static final String HEAD_ICON_DIC = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "ipNetPhone"
            + File.separator + "headIcon";
    private DisplayImageListener mDisplayImageListener = null;
    private RoundImageView headRv;

    private File mcroppedIconFile = null; //裁剪后的头像文件
    public static final String CROPPED_ICON_FILE = "cropped_head_icon.jpg";
    /**
     * 拍照裁剪照片requestcode 3:相机  4:相册 5:裁剪后的照片
     */
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final int REQUEST_ALBUM_PHOTO = 4;
    private static final int PHOTO_PICKED_WITH_DATA = 5;
    private String workType;

    private String nameOld;
    private String companyOld;
    private String departmentOld;
    private String positionOld;
    private String officeTelOld;
    private String headUrlOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical);
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.finish));
        titleBar.setTitleTextColor(Color.BLACK);
        titleBar.setBack("", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (from) {
                    String nameNew = ET_name.getText().toString().trim();
                    String companyNew = medical_company_edit.getText().toString().trim();
                    String departmentNew = medical_department_edit.getText().toString().trim();
                    String positionNew = medical_position_edit.getText().toString().trim();
                    String officeTelNew = medical_officeTel.getText().toString().trim();
                    String headUrlNew = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                    if (nameOld.equals(nameNew) && companyOld.equals(companyNew) && departmentOld.equals(departmentNew) && positionOld.equals(positionNew) && officeTelOld.equals(officeTelNew) && headUrlOld.equals(headUrlNew)) {
                        MedicalActivity.this.finish();
                    } else {
                        CustomDialog1.Builder builder = new CustomDialog1.Builder(MedicalActivity.this);
                        builder.setMessage(getString(R.string.giveup_modify_content));
                        builder.setPositiveButton(getString(R.string.give_up), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                MedicalActivity.this.finish();
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
                    MedicalActivity.this.finish();
                }
            }
        });
        init();
        Intent intent = getIntent();
        if (intent != null) {
            from = intent.getBooleanExtra("from_modify", false);
            if (from) {
                nameOld = ET_name.getText().toString().trim();
                companyOld = medical_company_edit.getText().toString().trim();
                departmentOld = medical_department_edit.getText().toString().trim();
                positionOld = medical_position_edit.getText().toString().trim();
                officeTelOld = medical_officeTel.getText().toString().trim();
                headUrlOld = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
            }
        }
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
    }

    private void init() {
        String workUnitType = getIntent().getStringExtra("workUnitType");
        workType = getIntent().getStringExtra("workType");
        medical_name = (LinearLayout) findViewById(R.id.medical_include_name);
        medical_company_edit = (EditText) findViewById(R.id.medical_company_edit);
        String workUnit = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getWorkUnit();
        if (!workUnit.equals("") && workUnitType.equals(String.valueOf(2))) {
            medical_company_edit.setText(workUnit);
        }
        medical_department_edit = (EditText) findViewById(R.id.medical_department_edit);
        String department = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getDepartment();
        if (!department.equals("") && workUnitType.equals(String.valueOf(2))) {
            medical_department_edit.setText(department);
        }
        medical_position_edit = (EditText) findViewById(R.id.medical_position_edit);
        String professional = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getProfessional();
        if (!professional.equals("") && workUnitType.equals(String.valueOf(2))) {
            medical_position_edit.setText(professional);
        }
        medical_officeTel = (EditText) findViewById(R.id.medical_officeTel);
        String officTel = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getOfficTel();
        if (!officTel.equals("") && workUnitType.equals(String.valueOf(2))) {
            medical_officeTel.setText(officTel);
        }
        medical_officeTel.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        medical_position_edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        medical_department_edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        medical_company_edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        medical_officeTel.setInputType(InputType.TYPE_CLASS_NUMBER);
        ET_name = (EditText) medical_name.findViewById(R.id.ET_name);
        name_text_hintname = (TextView) medical_name.findViewById(R.id.name_hint_name);
        name_text_hintnameafter = (TextView) medical_name.findViewById(name_hint_nameafter);
        String nickName = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getNickName();
        if (!nickName.equals("") && workUnitType.equals(String.valueOf(2))) {
            ET_name.setText(nickName);
            name_text_hintname.setVisibility(View.GONE);
            name_text_hintnameafter.setVisibility(View.GONE);
        }
        Editable etext = ET_name.getText();
        Selection.setSelection(etext, etext.length());
        button_next = (Button) findViewById(R.id.btn_medical_next);
        button_next.setOnClickListener(mbtnHandleEventListener);
        medical_name = (LinearLayout) findViewById(R.id.medical_include_name);
        medicalNumberLl = (LinearLayout) findViewById(R.id.medical_number_tv);
        headRv = (RoundImageView) medicalNumberLl.findViewById(R.id.btn_head);
        showHead(AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl());
        medicalNumberLl.setOnClickListener(mbtnHandleEventListener);

        ((TextView) (findViewById(R.id.medical_number_tv).findViewById(R.id.video_number))).setText(AccountManager.getInstance(MedicalApplication.context).getNube());

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
                if (ET_name.getText().toString().isEmpty()) {
                    button_next.setBackgroundResource(R.drawable.button_btn_notclick);
                } else {
                    button_next.setClickable(true);
                    button_next.setBackgroundResource(R.drawable.button_selector);
                }
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
        ET_name.addTextChangedListener(mTextWatcher);
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
        imageLoader.displayImage(str, headRv,
                options, mDisplayImageListener);
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
            case R.id.btn_medical_next:
                if (from) {
                    String name1 = ET_name.getText().toString().trim();
                    String company1 = medical_company_edit.getText().toString().trim();
                    String department1 = medical_department_edit.getText().toString().trim();
                    String position1 = medical_position_edit.getText().toString().trim();
                    String officeTel1 = medical_officeTel.getText().toString().trim();
                    String headUrl1 = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                    if (nameOld.equals(name1) && companyOld.equals(company1) && departmentOld.equals(department1) && positionOld.equals(position1) && officeTelOld.equals(officeTel1) && headUrlOld.equals(headUrl1)) {
                        MedicalActivity.this.finish();
                    } else {
                        String name = ET_name.getText().toString().trim();
                        String company = medical_company_edit.getText().toString().trim();
                        String department = medical_department_edit.getText().toString().trim();
                        String position = medical_position_edit.getText().toString().trim();
                        String officeTel = medical_officeTel.getText().toString().trim();
                        String headUrl = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                        if (headUrl == null || headUrl.length() == 0) {
                            AccountManager.getInstance(MedicalApplication.context).getAccountInfo().setHeadThumUrl("http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg");
                        }

                        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(company) || TextUtils.isEmpty(department) || TextUtils.isEmpty(position)|| TextUtils.isEmpty(officeTel)) {
                            CustomToast.show(MedicalActivity.this, getString(R.string.content_no_empty), Toast.LENGTH_LONG);
                            return;
                        }

                        if (!isPhoneNumber(officeTel)) {
                            CustomToast.show(MedicalActivity.this,getString(R.string.telephone_form_unright), Toast.LENGTH_LONG);
                            return;
                        }
                        //提交审核信息
                        submitUserInfo(name,company,department,position,officeTel,workType);
                    }
                } else {
                    String name = ET_name.getText().toString().trim();
                    String company = medical_company_edit.getText().toString().trim();
                    String department = medical_department_edit.getText().toString().trim();
                    String position = medical_position_edit.getText().toString().trim();
                    String officeTel = medical_officeTel.getText().toString().trim();
                    String headUrl = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
                    if (headUrl == null || headUrl.length() == 0) {
                        AccountManager.getInstance(MedicalApplication.context).getAccountInfo().setHeadThumUrl("http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg");
                    }

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(company) || TextUtils.isEmpty(department) || TextUtils.isEmpty(position)|| TextUtils.isEmpty(officeTel)) {
                        CustomToast.show(MedicalActivity.this, getString(R.string.content_no_empty), Toast.LENGTH_LONG);
                        return;
                    }

                    if (!isPhoneNumber(officeTel)) {
                        CustomToast.show(MedicalActivity.this, getString(R.string.telephone_form_unright), Toast.LENGTH_LONG);
                        return;
                    }
                    //提交审核信息
                    submitUserInfo(name,company,department,position,officeTel,workType);
                }
                break;
            case R.id.medical_number_tv:
                showDialog();
                break;
            default:
                break;
        }
    }

    private void submitUserInfo(String name, String company, String department, String position, String officeTel, String workType) {
        final MDSAppSubmitUserInf submitUserInf = new MDSAppSubmitUserInf() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "审核资料提交成功");
                Intent intent = new Intent(MedicalActivity.this, AuditingActivity.class);
                startActivity(intent);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomLog.e(TAG, "资料审核提交失败 statusCode: " + statusCode + " msg: " + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(MedicalActivity.this).tokenAuthFail(statusCode);
                } else if (statusCode == MDS_ACCOUNT_IS_EXISTED) {
                    CustomToast.show(MedicalActivity.this, getString(R.string.your_account_verify), Toast.LENGTH_LONG);
                    Intent intent = new Intent(MedicalActivity.this, LoginActivity.class);
                    startActivity(intent);
                    MedicalActivity.this.finish();
                } else {
                    CustomToast.show(MedicalActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };

        MedicalActivity.this.showLoadingView(getString(R.string.submit_verify), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                submitUserInf.cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_submit_verify), Toast.LENGTH_SHORT);
            }
        });

        String headUrl = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
        if (headUrl.equals("")){
            headUrl="http://vodtv.butel.com/d76c91ace2b640c19d1ba71671e49181.jpg";
        }
        String previewUrl = headUrl;
        int accoutType = 0;
        if (!AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getAccountType() .equals("")) {
            accoutType = Integer.valueOf(AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getAccountType());
        }

        String phone = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getMobile();
        String email = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getMail();
        String accessToken = AccountManager.getInstance(MedicalApplication.shareInstance()).getToken();
        String nube = AccountManager.getInstance(MedicalApplication.context).getNube();
        submitUserInf.appSubmitUserInfo(accessToken, headUrl, previewUrl, name, phone, email, accoutType, nube, company, Integer.valueOf(workType), department, position, officeTel,"http://vodtv.butel.com/ac68e4ef3359489bb1e25cb9a9fb80f4.png", "http://vodtv.butel.com/ac68e4ef3359489bb1e25cb9a9fb80f4.png");
    }

    private boolean isPhoneNumber(String phoneNumber) {
        boolean isValid = false;
        String expression = "1([\\d]{10})|((\\+[0-9]{2,4})?\\(?[0-9]+\\)?-?)?[0-9]{7,8}";
        CharSequence inputStr = phoneNumber;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    private void showDialog() {


        cid = new cn.redcdn.hvs.util.CameraImageDialog(MedicalActivity.this,
                R.style.contact_del_dialog);

        cid.setCameraClickListener(new cn.redcdn.hvs.util.CameraImageDialog.CameraClickListener() {

            @Override
            public void clickListener() {
                boolean result = CommonUtil.selfPermissionGranted(MedicalActivity.this, Manifest.permission.CAMERA);
                if (!result) {
                    // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionGen.with(MedicalActivity.this)
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
        cid.setPhoneClickListener(new cn.redcdn.hvs.util.CameraImageDialog.PhoneClickListener() {

            @Override
            public void clickListener() {
                photoFile();
            }
        });
        cid.setNoClickListener(new cn.redcdn.hvs.util.CameraImageDialog.NoClickListener() {

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
                    filePath = DocumentsHelper.getPath(MedicalActivity.this,
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


    private void upLoad(String path) {
        UploadManager.UploadImageListener listener = new UploadManager.UploadImageListener() {
            @Override
            public void onSuccess(CdnUploadDataInfo dataInfo) {
                MedicalActivity.this.removeLoadingView();
                String filepath = dataInfo.getFilepath();
                if (filepath == null) {
                    CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_fail),
                            Toast.LENGTH_SHORT);
                    return;
                }
                CustomToast.show(getApplicationContext(),getString(R.string.load_pic_suc),
                        Toast.LENGTH_SHORT);
                cid.dismiss();
                AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().setHeadThumUrl(filepath);
                AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().setHeadPreviewUrl(filepath);
                show(filepath);
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
                imageLoader.displayImage(str, headRv,
                        options, mDisplayImageListener);
            }

            @Override
            public void onProgress(int persent) {

            }

            @Override
            public void onFailed(int statusCode, String msg) {
                MedicalActivity.this.removeLoadingView();
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(MedicalActivity.this, getString(R.string.login_checkNetworkError),
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
                CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_fail)+"=" + statusCode,
                        Toast.LENGTH_SHORT);
            }
        };
        UploadManager.getInstance().uploadImage(new File(path), listener);


        MedicalActivity.this.showLoadingView(getString(R.string.upload_pic_ing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(),getString(R.string.upload_pic_cacel), Toast.LENGTH_SHORT);
            }
        });

    }

    private String getThumPath(String oldPath, int bitmapMaxWidth) {
        return BitmapUtils.getThumPath(oldPath, bitmapMaxWidth);
    }

    @Override
    public void onBackPressed() {
        if (from) {
            String nameNew = ET_name.getText().toString().trim();
            String companyNew = medical_company_edit.getText().toString().trim();
            String departmentNew = medical_department_edit.getText().toString().trim();
            String positionNew = medical_position_edit.getText().toString().trim();
            String officeTelNew = medical_officeTel.getText().toString().trim();
            String headUrlNew = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
            if (nameOld.equals(nameNew) && companyOld.equals(companyNew) && departmentOld.equals(departmentNew) && positionOld.equals(positionNew) && officeTelOld.equals(officeTelNew) && headUrlOld.equals(headUrlNew)) {
                MedicalActivity.this.finish();
            } else {
                CustomDialog1.Builder builder = new CustomDialog1.Builder(MedicalActivity.this);
                builder.setMessage(getString(R.string.giveup_modify_content));
                builder.setPositiveButton(getString(R.string.give_up), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MedicalActivity.this.finish();
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
            MedicalActivity.this.finish();
        }
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
                intent.setData(Uri.parse("package:" + MedicalActivity.this.getPackageName()));
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
