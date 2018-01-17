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
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.redcdn.datacenter.cdnuploadimg.CdnUploadDataInfo;
import cn.redcdn.datacenter.medicalcenter.MDSAppSubmitUserInf;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.cdnmanager.UploadManager;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.profiles.helper.DocumentsHelper;
import cn.redcdn.hvs.profiles.listener.DisplayImageListener;
import cn.redcdn.hvs.util.BitmapUtils;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;
import cn.redcdn.network.httprequest.HttpErrorCode;

import static android.media.MediaRecorder.VideoSource.CAMERA;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_ACCOUNT_IS_EXISTED;
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.profiles.activity.MyFileCardActivity.HEAD_ICON_DIC;

public class MedicalApproveActivity extends cn.redcdn.hvs.base.BaseActivity {
    private Button btn_next, btn_doctor, btn_medical, btn_back;
    private ImageView inputimage;
    private CameraImageDialog cid = null;
    private File headIconFile;// 相册或者拍照保存的文件
    private String croppedIconFilepath = null;// 压缩后图片位置
    private String inputdate = "";
    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;
    private String tag = MedicalApproveActivity.class.getName();
    public static final String KEY_FILE_ABSOLUTELY = "key_file_absolutely";
    public static final String KEY_FILE_CROPPEDICON_PATH = "key_file_croppedicon_path";
    private DisplayImageListener mDisplayImageListener;




    String name;
    String nube;
    String accessToken;
    Intent intent;
    String hospital;
    String department;
    String position;
    String officeTel;
    private RelativeLayout photoRl;
    private int workUnitType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_approve);
        init();
        Intent intent = getIntent();

        nube = AccountManager.getInstance(MedicalApplication.context).getNube();
        accessToken = AccountManager.getInstance(MedicalApplication.shareInstance()).getToken();
        name = intent.getStringExtra("name");
        hospital = intent.getStringExtra("company");
        department = intent.getStringExtra("department");
        position = intent.getStringExtra("position");
        officeTel = intent.getStringExtra("officeTel");
        workUnitType = Integer.valueOf(intent.getStringExtra("workType"));
    }

    private void init() {
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.medical_people_certification));
        titleBar.setTitleTextColor(Color.BLACK);
        titleBar.enableBack();
        photoRl = (RelativeLayout) findViewById(R.id.photo_rl);
        btn_next = (Button) findViewById(R.id.doctor_approve_next_btn);
        btn_back = (Button) findViewById(R.id.back_btn);
        btn_doctor = (Button) findViewById(R.id.btn_doctor);
        btn_medical = (Button) findViewById(R.id.btn_medical);
        inputimage = (ImageView) findViewById(R.id.doctor_approve_inputimage);
        showHead(AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getCertificatePreview());
        btn_next.setOnClickListener(mbtnHandleEventListener);
        btn_back.setOnClickListener(mbtnHandleEventListener);

        btn_doctor.setOnClickListener(mbtnHandleEventListener);
        btn_medical.setOnClickListener(mbtnHandleEventListener);
        photoRl.setOnClickListener(mbtnHandleEventListener);
        mDisplayImageListener = new DisplayImageListener();
    }


    private void showHead(final String str) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.doctor_approve_input_image)//设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.doctor_approve_input_image)//片加载/解码过程中错误时候显示的图片设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.doctor_approve_input_image)//设置图
                .cacheInMemory(true)//是否緩存都內存中
                .cacheOnDisc(true)//是否緩存到sd卡上
                .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, inputimage,
                options, mDisplayImageListener);
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.back_btn:
                MedicalApproveActivity.this.finish();
                break;
            case R.id.doctor_approve_next_btn:
                //提交审核信息
                submitUserInfo();
                break;
            case R.id.doctor_approve_inputimage:
                Intent intent_inputimage = new Intent(MedicalApproveActivity.this, OpenBigImageActivity.class);
                intent_inputimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                intent_inputimage.putExtra(OpenBigImageActivity.DATE_URL, AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getCertificatePreview());
                startActivity(intent_inputimage);
                break;
            case R.id.btn_doctor:
                Intent intent_doctorimage = new Intent(this, OpenBigImageActivity.class);
                intent_doctorimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_value_image);
                intent_doctorimage.putExtra(OpenBigImageActivity.DATE_value_image, R.drawable.doctor_hand_big);
                startActivity(intent_doctorimage);
                break;
            case R.id.btn_medical:
                Intent intent_medicalimage = new Intent(this, OpenBigImageActivity.class);
                intent_medicalimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_value_image);
                intent_medicalimage.putExtra(OpenBigImageActivity.DATE_value_image, R.drawable.mycard3);
                startActivity(intent_medicalimage);
                break;
            case R.id.photo_rl:
                showDialog();
                break;
            default:
                break;
        }
    }

    private void submitUserInfo() {
        final MDSAppSubmitUserInf submitUserInf = new MDSAppSubmitUserInf() {
            @Override
            protected void onSuccess(JSONObject responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "审核资料提交成功");
                Intent intent = new Intent(MedicalApproveActivity.this, AuditingActivity.class);
                startActivity(intent);
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                CustomLog.e(TAG, "资料审核提交失败 statusCode: " + statusCode + " msg: " + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(MedicalApproveActivity.this).tokenAuthFail(statusCode);
                } else if (statusCode == MDS_ACCOUNT_IS_EXISTED) {
                    CustomToast.show(MedicalApproveActivity.this, getString(R.string.your_account_verify), Toast.LENGTH_LONG);
                    Intent intent = new Intent(MedicalApproveActivity.this, LoginActivity.class);
                    startActivity(intent);
                    MedicalApproveActivity.this.finish();
                } else {
                    CustomToast.show(MedicalApproveActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };

        MedicalApproveActivity.this.showLoadingView(getString(R.string.submit_verify), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                submitUserInf.cancel();
                CustomToast.show(getApplicationContext(),getString(R.string.cancel_submit_verify), Toast.LENGTH_SHORT);
            }
        });

        String headUrl = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getHeadPreviewUrl();
        String previewUrl = headUrl;
        int accoutType = 0;
        if (!AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getAccountType() .equals("")) {
            accoutType = Integer.valueOf(AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getAccountType());
        }

        String phone = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getMobile();
        String email = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getMail();
        String certPreUrl= AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getCertificatePreview();
        String certUrl=AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getCertificateThum();

        if (certPreUrl == null || certPreUrl.length() == 0) {
            CustomToast.show(MedicalApproveActivity.this, getString(R.string.upload_mingpian), CustomToast.LENGTH_LONG);
            removeLoadingView();
            return;
        }

        submitUserInf.appSubmitUserInfo(accessToken, headUrl, previewUrl, name, phone, email, accoutType, nube, hospital, workUnitType, department, position, officeTel, certUrl, certPreUrl);
//        PackageManager packageManager = getPackageManager();
//        String packageName = getPackageName();
//        // getPackageName()是你当前类的包名，0代表是获取版本信息
//        PackageInfo packInfo = null;
//        try {
//            packInfo = packageManager.getPackageInfo(packageName,0);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        String version = packInfo.versionName;
//        String[] split = version.split("\\.");
//        String versionNo=split[0]+"."+split[1];
//        CustomLog.d(this.tag, "appSubmitUserInfo token=" + accessToken + "headThuml =" + headUrl + "headPreviewUrl =" + previewUrl + "name =" + name + "phone =" + phone + "email =" + email + "accountType =" + accountType + "nube =" + nube + "hospital =" + hospital + "workUnitType =" + workUnitType + "department=" + department + "professional=" + position + "officetel =" + officeTel + "certUrl=" + certUrl + "certPreUrl=" + certPreUrl+"versionNo="+versionNo);
//        submitUserInf.newAppSubmitUserInfo(accessToken, headUrl, previewUrl, name, phone, email, accoutType, nube, hospital, workUnitType, department, position, officeTel, certUrl, certPreUrl,versionNo);
    }


    private void showDialog() {

        cid = new CameraImageDialog(MedicalApproveActivity.this,
                R.style.contact_del_dialog);

        cid.setCameraClickListener(new CameraImageDialog.CameraClickListener() {

            @Override
            public void clickListener() {
                boolean result = CommonUtil.selfPermissionGranted(MedicalApproveActivity.this, Manifest.permission.CAMERA);
                if (!result){
                    CustomToast.show(MedicalApproveActivity.this, getString(R.string.open_camera_per), CustomToast.LENGTH_SHORT);
                }
                camera();
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

    private void camera() {
            String state = Environment.getExternalStorageState();
            if (state.equals(Environment.MEDIA_MOUNTED)) {
                initHeadIconFile();
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                Uri imageUri;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M){
                    imageUri = Uri.fromFile(headIconFile);
                }else{
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

    private void photoFile() {
            String state = Environment.getExternalStorageState();
            if (state.equals(Environment.MEDIA_MOUNTED)) {

                Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);

                getAlbum.setType(IMAGE_TYPE);
                startActivityForResult(getAlbum, IMAGE_CODE);

            } else {
                CustomToast.show(getApplicationContext(),getString(R.string.open_save_per),
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

            croppedIconFilepath = getThumPath(headIconFile.getPath(), 400);
            upLoad(headIconFile.getPath());
        } else {
            if (requestCode == IMAGE_CODE) {
                String filePath = "";
                ContentResolver resolver = getContentResolver();
                Uri originalUri = data.getData(); // 获得图片的uri
                if (originalUri != null) {
                    filePath = DocumentsHelper.getPath(MedicalApproveActivity.this,
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
                if (filePath == null || filePath.equalsIgnoreCase("")) {
                    CustomToast.show(getApplicationContext(), getString(R.string.can_not_get_pic_path),
                            Toast.LENGTH_SHORT);
                    return;
                }
                if (BitmapUtils.isImageType(filePath)) {
                    upLoad(filePath);

                } else
                    CustomToast.show(getApplicationContext(), getString(R.string.file_type_error),
                            Toast.LENGTH_SHORT);
            }
        }
    }


    private void upLoad(String path) {
        UploadManager.UploadImageListener listener = new UploadManager.UploadImageListener() {
            @Override
            public void onSuccess(CdnUploadDataInfo dataInfo) {
                MedicalApproveActivity.this.removeLoadingView();
                String filepath = dataInfo.getFilepath();
                if (filepath == null) {
                    CustomToast.show(getApplicationContext(), getString(R.string.upload_certify_pic_fail),
                            Toast.LENGTH_SHORT);
                    return;
                }
                CustomToast.show(getApplicationContext(), getString(R.string.upload_certify_pic_suc),
                        Toast.LENGTH_SHORT);
                cid.dismiss();
                AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().setCertificatePreview(filepath);
                AccountManager.getInstance(getApplicationContext())
                        .getAccountInfo().setCertificateThum(filepath);
                show(filepath);
            }

            @Override
            public void onProgress(int persent) {

            }

            @Override
            public void onFailed(int statusCode, String msg) {
                MedicalApproveActivity.this.removeLoadingView();
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(MedicalApproveActivity.this, getString(R.string.login_checkNetworkError),
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
                CustomToast.show(getApplicationContext(), getString(R.string.upload_certify_pic_fail)+"=" + statusCode,
                        Toast.LENGTH_SHORT);
            }
        };
        UploadManager.getInstance().uploadImage(new File(path), listener);


        MedicalApproveActivity.this.showLoadingView(getString(R.string.up_pic), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.cancel_up_pic), Toast.LENGTH_SHORT);
            }
        });

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
        inputimage.setOnClickListener(mbtnHandleEventListener);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, inputimage,
                options, mDisplayImageListener);
    }

    private String getThumPath(String oldPath, int bitmapMaxWidth) {
        return BitmapUtils.getThumPath(oldPath, bitmapMaxWidth);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CustomLog.e(tag, "onSaveInstanceState");
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
    public void onBackPressed() {
        super.onBackPressed();
        MedicalApproveActivity.this.finish();
    }
}
