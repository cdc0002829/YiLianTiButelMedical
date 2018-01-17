package cn.redcdn.hvs.profiles.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import cn.redcdn.datacenter.cdnuploadimg.CdnUploadDataInfo;
import cn.redcdn.datacenter.medicalcenter.MDSAppUpdatePhoto;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.cdnmanager.UploadManager;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.AddContactActivity;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.hvs.profiles.dialog.CameraImageDialog;
import cn.redcdn.hvs.profiles.helper.DocumentsHelper;
import cn.redcdn.hvs.profiles.listener.DisplayImageListener;
import cn.redcdn.hvs.util.BitmapUtils;
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
import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.AccountManager.getInstance;
import static cn.redcdn.hvs.R.id.department;
import static cn.redcdn.hvs.R.id.modify_password_rl;

/**
 * Created by Administrator on 2017/2/24.
 */

public class MyFileCardActivity extends BaseActivity {
    protected final String TAG = getClass().getName();
    public static final String KEY_FILE_ABSOLUTELY = "key_file_absolutely";
    public static final String KEY_FILE_CROPPEDICON_PATH = "key_file_croppedicon_path";
    private File headIconFile;// 相册或者拍照保存的文件
    private File croppedIconFile = null;// 压缩后文件
    private DisplayImageListener mDisplayImageListener = null;
    private RoundImageView headIv;
    private TextView myName;
    private Button backButton;
    private TextView accountId;
    private TextView phoneNum;
    private TextView hospital;
    private TextView room;
    private TextView profession;
    private TextView roomPhone;
    private TextView mobile;
    private TextView certificationText;
    private RelativeLayout imageRl;
    private RelativeLayout nameRl;
    private RelativeLayout accountnumRl;
    private RelativeLayout scanRl;
    private RelativeLayout hospitalRl;
    private RelativeLayout roomRl;
    private RelativeLayout professionRl;
    private RelativeLayout roomphoneRl;
    private RelativeLayout certificationRl;
    private RelativeLayout modifyPasswordRl;
    private Button mycardExitButton;
    private CameraImageDialog cid = null;
    public static final String HEAD_ICON_DIC = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "ipNetPhone"
            + File.separator + "headIcon";
    private final String IMAGE_TYPE = "image/*";
    private final int IMAGE_CODE = 0;
    private String croppedIconFilepath = null;// 压缩后图片位置
    private File mcroppedIconFile = null; //裁剪后的头像文件
    private TextView company;
    private TextView mDepartment;
    private TextView mJob;
    private TextView mJobTelephone;

    public static final String CROPPED_ICON_FILE = "cropped_head_icon.jpg";
    /**
     * 拍照裁剪照片requestcode 3:相机  4:相册 5:裁剪后的照片
     */
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final int REQUEST_ALBUM_PHOTO = 4;
    private static final int PHOTO_PICKED_WITH_DATA = 5;
    private TextView hospitalOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CustomLog.d(TAG, "onCreate:" + this.toString());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myfilecard);
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
        initWidget();
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.personal_data));
        mDisplayImageListener = new DisplayImageListener();
    }

    private void initWidget() {
        hospitalOther = (TextView) findViewById(R.id.hospital_other);
        company = (TextView) findViewById(R.id.company);
        mobile = (TextView) findViewById(R.id.mobile);
        mDepartment = (TextView) findViewById(department);
        mJob = (TextView) findViewById(R.id.job);
        mJobTelephone = (TextView) findViewById(R.id.job_telephone);
        headIv = (RoundImageView) findViewById(R.id.head_iv);
        myName = (TextView) findViewById(R.id.my_name);
        backButton = (Button) findViewById(R.id.back_btn);
        accountId = (TextView) findViewById(R.id.account_id);
        phoneNum = (TextView) findViewById(R.id.phone_num);
        hospital = (TextView) findViewById(R.id.hospital);
        room = (TextView) findViewById(R.id.room);
        profession = (TextView) findViewById(R.id.profession);
        roomPhone = (TextView) findViewById(R.id.room_phonenum);
        certificationText = (TextView) findViewById(R.id.certification_context);

        imageRl = (RelativeLayout) findViewById(R.id.image_rl);
        nameRl = (RelativeLayout) findViewById(R.id.name_rl);
        accountnumRl = (RelativeLayout) findViewById(R.id.accountnum_rl);
        scanRl = (RelativeLayout) findViewById(R.id.scan_rl);
        hospitalRl = (RelativeLayout) findViewById(R.id.hospital_rl);
        roomRl = (RelativeLayout) findViewById(R.id.room_rl);
        professionRl = (RelativeLayout) findViewById(R.id.profession_rl);
        roomphoneRl = (RelativeLayout) findViewById(R.id.roomphone_rl);
        certificationRl = (RelativeLayout) findViewById(R.id.certification_rl);
        modifyPasswordRl = (RelativeLayout) findViewById(modify_password_rl);

        mycardExitButton = (Button) findViewById(R.id.mycard_exit_btn);


        imageRl.setOnClickListener(mbtnHandleEventListener);
//        nameRl.setOnClickListener(mbtnHandleEventListener);
        accountnumRl.setOnClickListener(mbtnHandleEventListener);
        scanRl.setOnClickListener(mbtnHandleEventListener);
        certificationRl.setOnClickListener(mbtnHandleEventListener);
        modifyPasswordRl.setOnClickListener(mbtnHandleEventListener);
        mycardExitButton.setOnClickListener(mbtnHandleEventListener);

        cid = new CameraImageDialog(MyFileCardActivity.this,
                R.style.contact_del_dialog);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void todoClick(int id) {
        // TODO Auto-generated method stub
        super.todoClick(id);
        switch (id) {
            case R.id.image_rl:
                showDialog();
                break;
            case R.id.name_rl:
                Intent tosetattenddata = new Intent();
                tosetattenddata.setClass(MyFileCardActivity.this,
                        ChangeMeetingNameActivity.class);
                startActivity(tosetattenddata);
                break;
            case R.id.accountnum_rl:
                Intent pd = new Intent();
                pd.setClass(MyFileCardActivity.this, PersonDataActivity.class);
                startActivity(pd);
                break;
            case R.id.scan_rl:
                Intent scan = new Intent();
                scan.setClass(MyFileCardActivity.this, MyMaActivity.class);
                startActivity(scan);
                break;
            case R.id.certification_rl:
                Intent goCertification = new Intent();
                goCertification.setClass(MyFileCardActivity.this, CertificationActivity.class);
                startActivity(goCertification);
                break;
            case R.id.modify_password_rl:
                Intent gochange = new Intent();
                gochange.setClass(MyFileCardActivity.this, ChangePwdActivity.class);
                startActivity(gochange);
                break;
            case R.id.mycard_exit_btn:
                CustomLog.d(TAG, "注销");
                final CustomDialog dialog = new CustomDialog(this);

                dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                    @Override
                    public void onClick(CustomDialog customDialog) {
                        dialog.cancel();
                        getInstance(getApplicationContext()).logout();
                        final SharedPreferences share = getSharedPreferences("lianhe_sharedpreferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = share.edit();
                        edit.putString(AccountManager.getInstance(MyFileCardActivity.this).getNube(), "");
                        edit.commit();
                    }
                });
                dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
                    @Override
                    public void onClick(CustomDialog customDialog) {
                        // TODO Auto-generated method stub
                        dialog.cancel();
                    }
                });
                dialog.setTip(getString(R.string.are_you_sure));
                dialog.setOkBtnText(getString(R.string.quit));
                dialog.setCancelBtnText(getString(R.string.cancel));
                dialog.show();
                break;


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initstatus();
    }

    private void initstatus() {
        String workUnitType = getInstance(getApplicationContext())
                .getAccountInfo().getWorkUnitType();
        String accountType = getInstance(getApplicationContext()).getAccountInfo().getAccountType();
        if (!accountType.equals("")) {
            if (accountType.equals("2")) {
                mobile.setText(getString(R.string.email));
                phoneNum.setText(getInstance(getApplicationContext()).getAccountInfo().getMail());
            }
        }
        if (!workUnitType.equals("")) {
            if (workUnitType.equals("2")) {
                company.setText(getString(R.string.company));
                mDepartment.setText(getString(R.string.bumen));
                mJob.setText(getString(R.string.job));
                mJobTelephone.setText(getString(R.string.office_phone));
            }
        }
        String nickName = getInstance(getApplicationContext())
                .getAccountInfo().nickName;
        if (nickName != null && !nickName.equalsIgnoreCase(""))
            myName.setText(nickName);
        else
            myName.setText("");

        if (getInstance(getApplicationContext()).getAccountInfo() != null
                && getInstance(getApplicationContext()).getAccountInfo().nube != null) {
            accountId.setText(getInstance(getApplicationContext())
                    .getAccountInfo().nube);
        }
        if (getInstance(getApplicationContext()).getAccountInfo() != null
                && getInstance(getApplicationContext()).getAccountInfo().workUnit != null) {
            String workUnit = getInstance(getApplicationContext()).getAccountInfo().workUnit;
            if (!TextUtils.isEmpty(getInstance(getApplicationContext()).getAccountInfo().getRemarks())) {
                hospitalOther.setVisibility(View.VISIBLE);
                hospital.setText(workUnit);
                hospitalOther.setText("(" + getInstance(getApplicationContext()).getAccountInfo().getRemarks() + ")");

            } else {
                hospitalOther.setVisibility(View.GONE);
                hospital.setText(getInstance(getApplicationContext())
                        .getAccountInfo().workUnit);
            }

        }
        if (getInstance(getApplicationContext()).getAccountInfo() != null
                && getInstance(getApplicationContext()).getAccountInfo().department != null) {
            room.setText(getInstance(getApplicationContext())
                    .getAccountInfo().department);
        }
        if (getInstance(getApplicationContext()).getAccountInfo() != null
                && getInstance(getApplicationContext()).getAccountInfo().professional != null) {
            profession.setText(getInstance(getApplicationContext())
                    .getAccountInfo().professional);
        }
        if (getInstance(getApplicationContext()).getAccountInfo() != null
                && getInstance(getApplicationContext()).getAccountInfo().officTel != null) {
            roomPhone.setText(getInstance(getApplicationContext())
                    .getAccountInfo().officTel);
        }
        if (getInstance(getApplicationContext()).getAccountInfo() != null
                && getInstance(getApplicationContext()).getAccountInfo().state != 0) {
//            int cetifyState = AccountManager.getInstance(getApplicationContext()).getAccountInfo().state;
//            if (cetifyState == 2) {
            certificationText.setText(getString(R.string.certificatied));
//            } else if (cetifyState == 1) {
//                certificationText.setText("待审核");
//            } else if (cetifyState == 3) {
//                certificationText.setText("未通过");
//            }
        }
        MDSAccountInfo info = getInstance(getApplicationContext())
                .getAccountInfo();
        CustomLog.d(TAG, " info.headUrl=" + info.headThumUrl + "");
        if (info.headThumUrl != null && !info.headThumUrl.equalsIgnoreCase("")) {
            CustomLog.v(TAG, "显示图片");
            show(info.headThumUrl);
        } else {
            headIv.setImageResource(R.drawable.doctor_default);
        }
        if (getInstance(getApplicationContext()).getAccountInfo() != null
                && !getInstance(getApplicationContext()).getAccountInfo().mobile.equals("")) {
            phoneNum.setText(
                    getInstance(getApplicationContext()).getAccountInfo().mobile);
        }

    }


    private void showDialog() {

        cid.setCameraClickListener(new CameraImageDialog.CameraClickListener() {

            @Override
            public void clickListener() {
                boolean result = CommonUtil.selfPermissionGranted(MyFileCardActivity.this, Manifest.permission.CAMERA);
                if (!result) {
                    // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionGen.with(MyFileCardActivity.this)
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

    protected void doCropPhoto(File f) {
        CustomLog.d(TAG, "去剪辑这个照片");
        //进入裁剪页面,此处用的是自定义的裁剪页面而不是调用系统裁剪
        Intent intent = new Intent(this, ClipPictureActivity.class);
        intent.putExtra(ClipPictureActivity.IMAGE_PATH_ORIGINAL, f.getAbsolutePath());
        intent.putExtra(ClipPictureActivity.IMAGE_PATH_AFTER_CROP, mcroppedIconFile.getAbsolutePath());

        startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);


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

    private void show(final String str) {
        CustomLog.v(TAG, "pathstr=" + str);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(str, headIv,
                MedicalApplication.shareInstance().options, mDisplayImageListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLog.e(TAG, "onActivityResult");
        if (resultCode != RESULT_OK) {
            return;
        }
//        if (requestCode == CAMERA) {
//            CustomLog.d(TAG, "进入拍照后currentMyPid===="
//                    + android.os.Process.myPid());
//            CustomLog.d(TAG, "onActivityResult..CAMERA..headIconFile.getPath()="
//                    + headIconFile.getPath());
//
//            croppedIconFilepath = getThumPath(headIconFile.getPath(), 400);
//            upLoad(getThumPath(headIconFile.getPath(), 400));
//        } else {
//            if (requestCode == IMAGE_CODE) {
//                String filePath = "";
//                ContentResolver resolver = getContentResolver();
//                Uri originalUri = data.getData(); // 获得图片的uri
//                if (originalUri != null) {
//                    filePath = DocumentsHelper.getPath(MyFileCardActivity.this,
//                            originalUri);
//                }
//
//                if (TextUtils.isEmpty(filePath)) {
//                    filePath = data.getDataString();
//                    if (!TextUtils.isEmpty(filePath) && filePath.startsWith("file:///")) {
//                        filePath = filePath.replace("file://", "");
//                        try {
//                            // java的文件系统是linux,而编码格式是UTF-8的编码格式
//                            filePath = URLDecoder.decode(filePath, "UTF-8");
//                        } catch (UnsupportedEncodingException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                if (filePath == null || filePath.equalsIgnoreCase("")) {
//                    CustomToast.show(getApplicationContext(), "无法获取图片路径",
//                            Toast.LENGTH_SHORT);
//                    return;
//                }
//                if (BitmapUtils.isImageType(filePath)) {
//                    upLoad(getThumPath(filePath, 400));
//
//                } else
//                    CustomToast.show(getApplicationContext(), "文件格式错误",
//                            Toast.LENGTH_SHORT);
//            }
//        }
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
                doCropPhoto(headIconFile);
//                upLoad(mcroppedIconFile.getPath());
                CustomLog.d(TAG, "拍照后的图片进行上传");
//                croppedIconFilepath = getThumPath(headIconFile.getPath(), 400);//压缩后图片的位置
//                upLoad(getThumPath(headIconFile.getPath(), 400));

                break;
            case IMAGE_CODE:
                CustomLog.d(TAG, "从相册返回");
                mcroppedIconFile = new File(HEAD_ICON_DIC, CROPPED_ICON_FILE);
                if (mcroppedIconFile.exists()) {
                    mcroppedIconFile.delete();
                }
                String filePath = "";
                ContentResolver resolver = getContentResolver();
                Uri originalUri = data.getData(); // 获得图片的uri
                if (originalUri != null) {
                    filePath = DocumentsHelper.getPath(MyFileCardActivity.this,
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
//                    upLoad(getThumPath(String.valueOf(doCropPhoto(new File(filePath))), 400));

                } else
                    CustomToast.show(getApplicationContext(), getString(R.string.file_type_error),
                            Toast.LENGTH_SHORT);

                break;
            case PHOTO_PICKED_WITH_DATA:
                CustomLog.d(TAG, getString(R.string.draping_pic));
                croppedIconFilepath = getThumPath(mcroppedIconFile.getPath(), 400);
                upLoad(croppedIconFilepath);
                break;
            default:
                break;

        }
    }

    private String getThumPath(String oldPath, int bitmapMaxWidth) {
        return BitmapUtils.getThumPath(oldPath, bitmapMaxWidth);
    }

    private void upLoad(String path) {

        UploadManager.UploadImageListener listener = new UploadManager.UploadImageListener() {

            @Override
            public void onSuccess(CdnUploadDataInfo dataInfo) {
                CustomLog.d(TAG, "上传头像回调的URL = " + dataInfo.filepath);
                MyFileCardActivity.this.removeLoadingView();
                String filepath = dataInfo.getFilepath();
                if (filepath == null) {
                    CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_fail),
                            Toast.LENGTH_SHORT);
                    return;
                }
                CustomToast.show(getApplicationContext(), getString(R.string.load_pic_suc),
                        Toast.LENGTH_SHORT);
                cid.dismiss();
                AccountManager.getInstance(getApplicationContext()).updateHeadUrl(filepath, filepath, "");
//                AccountManager.getInstance(getApplicationContext())
//                        .getAccountInfo().setHeadThumUrl(filepath);
//                AccountManager.getInstance(getApplicationContext())
//                        .getAccountInfo().setHeadPreviewUrl(filepath);
                show(filepath);
                final MDSAppUpdatePhoto mdsAppUpdatePhoto = new MDSAppUpdatePhoto() {
                    @Override
                    protected void onSuccess(String responseContent) {
                        super.onSuccess(responseContent);

                        MyFileCardActivity.this.removeLoadingView();
                    }

                    @Override
                    protected void onFail(int statusCode, String statusInfo) {
                        super.onFail(statusCode, statusInfo);
                        MyFileCardActivity.this.removeLoadingView();
                        if (HttpErrorCode.checkNetworkError(statusCode)) {
                            CustomToast.show(MyFileCardActivity.this, getString(R.string.login_checkNetworkError),
                                    Toast.LENGTH_LONG);
                            return;
                        }
                        if (statusCode == MDS_TOKEN_DISABLE) {
                            AccountManager.getInstance(MyFileCardActivity.this).tokenAuthFail(statusCode);
                        } else {
                            CustomToast.show(MyFileCardActivity.this, statusInfo, Toast.LENGTH_LONG);
                        }

                        CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_fail) + "=" + statusCode,
                                Toast.LENGTH_SHORT);
                    }
                };


                MyFileCardActivity.this.showLoadingView(getString(R.string.upload_pic_ing), new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        mdsAppUpdatePhoto.cancel();
                        CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_cacel), Toast.LENGTH_SHORT);
                    }
                });
                mdsAppUpdatePhoto.appUpdatePhoto(AccountManager.getInstance(MedicalApplication.getContext()).getAccountInfo().getAccessToken(), filepath, filepath);
            }

            @Override
            public void onProgress(int persent) {

            }

            @Override
            public void onFailed(int statusCode, String msg) {
                MyFileCardActivity.this.removeLoadingView();
                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(MyFileCardActivity.this, getString(R.string.login_checkNetworkError),
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
//        UploadManager.getInstance().init(SettingData.getInstance().CDN_AppId);
        UploadManager.getInstance().uploadImage(new File(path), listener);


        MyFileCardActivity.this.showLoadingView(getString(R.string.upload_pic_ing), new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                UploadManager.getInstance().cancel();
                CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_cacel), Toast.LENGTH_SHORT);
            }
        });

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
        final CustomDialog dialog = new CustomDialog(MyFileCardActivity.this);
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
                intent.setData(Uri.parse("package:" + MyFileCardActivity.this.getPackageName()));
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
