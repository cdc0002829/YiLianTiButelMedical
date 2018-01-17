package cn.redcdn.hvs.requesttreatment;

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
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.butel.connectevent.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.redcdn.datacenter.cdnuploadimg.CdnUploadDataInfo;
import cn.redcdn.datacenter.hpucenter.HPUUpdatePatient;
import cn.redcdn.datacenter.hpucenter.data.CurInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.cdnmanager.UploadManager;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.im.util.SendCIVMDTUtil;
import cn.redcdn.hvs.profiles.dialog.CameraImageDialog;
import cn.redcdn.hvs.profiles.listener.DisplayImageListener;
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

/**
 * Created by Administrator on 2017/11/18 0018.
 */

public class PatientConditionActivity extends BaseActivity {

    private Context mContext;
    private TitleBar titleBar;
    private TextView completeView;
    private CameraImageDialog cid = null;
    private CameraImageDialog cid1 = null;
    public static final String HEAD_ICON_DIC = Environment.getExternalStorageDirectory()
        + File.separator + "ipNetPhone" + File.separator + "headIcon";
    private final int IMAGE_CODE = 0;
    private File headIconFile;// 相册或者拍照保存的文件
    private File mcroppedIconFile = null; //裁剪后的头像文件
    public static final String CROPPED_ICON_FILE = "cropped_head_icon.jpg";
    private LinearLayout informationLayout;
    private LinearLayout ageLayout;
    private LinearLayout genderLayout;
    private LinearLayout heightLayout;
    private LinearLayout weightLayout;
    private AlertDialog dialog;
    private EditText abstractEditText;
    private EditText inspectionEditText;
    private EditText toBeSolvedEditText;
    private final int MSG_MESSAGE_IMAGE_NUMBER_CHANGED = 702;
    private DisplayImageListener mDisplayImageListener = null;
    private TextView ageView;
    private TextView genderView;
    private TextView heightView;
    private TextView weightView;
    private Button backBtn;
    private String mAge;
    private String mGender;
    private String mHeight;
    private String mWeight;
    private String mAbstract;
    private String mInspection;
    private String mCheck;
    private String mToBeSolved;
    private NewCurInfo newCurInfo = new NewCurInfo();
    private LoopView pickerscrlllviewAge;
    private LoopView pickerscrlllviewGender;
    private LoopView pickerscrlllviewHeight;
    private LoopView pickerscrlllviewWeight;
    private List<String> list1;
    private List<String> list2;
    private List<String> list3;
    private List<String> list4;
    private TextView pickerCancelView;
    private TextView pickerConfirmView;
    private String mPickContentAge;
    private String mPickContentGender;
    private String mPickContentHeight;
    private String mPickContentWeight;
    private int currentPicSize = 0;
    private ViewPager mViewPagerList = null;
    private ImagePagerAdapterList mImagePagerAdapterList = null;
    private static final int IMAGE_LIST_COLUMN = 4;
    private List<Contact> mMutiTopList = new ArrayList<Contact>();
    private int sentPicNumber = 0;
    private boolean uploadFinished = true;
    private boolean updateFinished = false;
    private boolean clickComplete = false;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_condition);
        this.getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initWidget();
    }


    private void initWidget() {
        mContext = this;
        mViewPagerList = (ViewPager) findViewById(R.id.patient_condition_check_list);
        newCurInfo = (NewCurInfo) getIntent().getSerializableExtra("newCurInfo");
        initTitleBar();
        completeView = (TextView) findViewById(R.id.tv_patient_condition_complete);
        cid = new CameraImageDialog(PatientConditionActivity.this,
            R.style.contact_del_dialog,
            mContext.getString(R.string.patient_condition_take_photo),
            mContext.getString(R.string.patient_condition_select_from_gallery)
            , 2, true,
            mContext.getString(R.string.patient_condition_add_picture));
        cid1 = new CameraImageDialog(PatientConditionActivity.this,
            R.style.contact_del_dialog,
            "",
            mContext.getString(R.string.patient_condition_delete_picture)
            , 1, false, "");
        informationLayout = (LinearLayout) findViewById(R.id.ll_information);
        ageLayout = (LinearLayout) findViewById(R.id.ll_information_age);
        genderLayout = (LinearLayout) findViewById(R.id.ll_information_gender);
        heightLayout = (LinearLayout) findViewById(R.id.ll_information_height);
        weightLayout = (LinearLayout) findViewById(R.id.ll_information_weight);
        ageView = (TextView) findViewById(R.id.tv_age);
        genderView = (TextView) findViewById(R.id.tv_gender);
        heightView = (TextView) findViewById(R.id.tv_height);
        weightView = (TextView) findViewById(R.id.tv_weight);
        backBtn = (Button) findViewById(R.id.back_btn_big);
        abstractEditText = (EditText) findViewById(R.id.et_abstract);
        // abstractEditText.setFilters(
        //     new InputFilter[] { inputFilter, new InputFilter.LengthFilter(500) });
        inspectionEditText = (EditText) findViewById(R.id.et_inspection);
        // inspectionEditText.setFilters(
        //     new InputFilter[] { inputFilter, new InputFilter.LengthFilter(500) });
        toBeSolvedEditText = (EditText) findViewById(R.id.et_to_be_solved);
        // toBeSolvedEditText.setFilters(
        //     new InputFilter[] { inputFilter, new InputFilter.LengthFilter(500) });
        backBtn.setOnClickListener(mbtnHandleEventListener);
        completeView.setOnClickListener(mbtnHandleEventListener);
        informationLayout.setOnClickListener(mbtnHandleEventListener);
        mDisplayImageListener = new DisplayImageListener();
        if (!TextUtils.isEmpty(newCurInfo.getAge())) {
            mAge = newCurInfo.getAge();
            ageView.setText(mAge);
            ageView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(newCurInfo.getGender())) {
            mGender = newCurInfo.getGender();
            genderView.setText(mGender);
            genderView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(newCurInfo.getHeight())) {
            mHeight = newCurInfo.getHeight();
            heightView.setText(mHeight);
            heightView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(newCurInfo.getWeight())) {
            mWeight = newCurInfo.getWeight();
            weightView.setText(mWeight);
            weightView.setTextColor(getResources().getColor(R.color.btn_color_black));
        }
        if (!TextUtils.isEmpty(newCurInfo.getAbstract())) {
            mAbstract = newCurInfo.getAbstract();
            abstractEditText.setText(mAbstract);
        }
        if (!TextUtils.isEmpty(newCurInfo.getInspection())) {
            mInspection = newCurInfo.getInspection();
            inspectionEditText.setText(mInspection);
        }

        if (!TextUtils.isEmpty(newCurInfo.getToBeSolved())) {
            mToBeSolved = newCurInfo.getToBeSolved();
            toBeSolvedEditText.setText(mToBeSolved);
        }

        mImagePagerAdapterList = null;

        Contact defaultImage = new Contact();
        defaultImage.setHeadUrl("");
        defaultImage.setNubeNumber("");
        mMutiTopList.add(defaultImage);

        mImagePagerAdapterList = new ImagePagerAdapterList(this, mMutiTopList,
            IMAGE_LIST_COLUMN, 1, false, new ItemClickListener() {
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

        if (!TextUtils.isEmpty(newCurInfo.getCheck())) {
            mCheck = newCurInfo.getCheck();
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


    private void initTitleBar() {
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(
            mContext.getString(R.string.patient_condition_write_patient)
                + newCurInfo.getName()
                + mContext.getString(R.string.patient_condition_condition));
    }


    private void showPickerDialog(final String[] dataList1,
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
            @Override public void onClick(View v) {

                if(TextUtils.isEmpty(mAge)){
                    ageView.setText(mContext.getString(R.string.patient_condition_age));
                    ageView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                }else{
                    ageView.setText(mAge);
                    ageView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                if(TextUtils.isEmpty(mGender)){
                    genderView.setText(mContext.getString(R.string.patient_condition_gender));
                    genderView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                }else{
                    genderView.setText(mGender);
                    genderView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                if(TextUtils.isEmpty(mHeight)){
                    heightView.setText(mContext.getString(R.string.patient_condition_height));
                    heightView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                }else{
                    heightView.setText(mHeight);
                    heightView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                if(TextUtils.isEmpty(mWeight)){
                    weightView.setText(mContext.getString(R.string.patient_condition_weight));
                    weightView.setTextColor(getResources().getColor(R.color.reserve_dt_content_hint_color));
                } else{
                    weightView.setText(mWeight);
                    weightView.setTextColor(getResources().getColor(R.color.btn_color_black));
                }

                dialog.dismiss();
            }
        });
        pickerConfirmView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
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

        if(!TextUtils.isEmpty(mAge)){
            for(int a=0; a < dataList1.length; a++){
                if(mAge.equals(dataList1[a])){
                    pickerscrlllviewAge.setInitPosition(a);
                    break;
                }
            }
        }else{
            pickerscrlllviewAge.setInitPosition(43);
        }

        if(!TextUtils.isEmpty(mGender)){
            for(int b=0; b < dataList2.length; b++){
                if(mGender.equals(dataList2[b])){
                    pickerscrlllviewGender.setInitPosition(b);
                    break;
                }
            }
        }else{
            pickerscrlllviewGender.setInitPosition(0);
        }

        if(!TextUtils.isEmpty(mHeight)){
            for(int c=0; c < dataList3.length; c++){
                if(mHeight.equals(dataList3[c])){
                    pickerscrlllviewHeight.setInitPosition(c);
                    break;
                }
            }
        }else{
            pickerscrlllviewHeight.setInitPosition(90);
        }

        if(!TextUtils.isEmpty(mWeight)){
            for(int d=0; d < dataList4.length; d++){
                if(mWeight.equals(dataList4[d])){
                    pickerscrlllviewWeight.setInitPosition(d);
                    break;
                }
            }
        }else{
            pickerscrlllviewWeight.setInitPosition(30);
        }

        pickerscrlllviewAge.setListener(new OnItemSelectedListener() {
            @Override public void onItemSelected(int index) {
                mPickContentAge = list1.get(index);
            }
        });

        pickerscrlllviewGender.setListener(new OnItemSelectedListener() {
            @Override public void onItemSelected(int index) {
                mPickContentGender = list2.get(index);
            }
        });

        pickerscrlllviewHeight.setListener(new OnItemSelectedListener() {
            @Override public void onItemSelected(int index) {
                mPickContentHeight = list3.get(index);
            }
        });

        pickerscrlllviewWeight.setListener(new OnItemSelectedListener() {
            @Override public void onItemSelected(int index) {
                mPickContentWeight = list4.get(index);
            }
        });

    }


    private void showBackDialog() {
        final CustomDialog backDialog = new CustomDialog(mContext);
        backDialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                if (newCurInfo != null && newCurInfo.getToUDT()) {
                    Intent intent = new Intent();
                    setResult(RESULT_CANCELED, intent);
                    finish();
                } else {
                    finish();
                }

            }
        });
        backDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
            @Override
            public void onClick(CustomDialog customDialog) {
                backDialog.dismiss();
            }
        });
        backDialog.setOkBtnText(mContext.getString(R.string.patient_condition_continue));
        backDialog.setCancelBtnText(mContext.getString(R.string.patient_condition_exit));
        backDialog.setTip(mContext.getString(R.string.patient_condition_backdialog_tip));
        backDialog.show();
    }


    // 设置回退
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            showBackDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.back_btn_big:
                showBackDialog();
                break;
            case R.id.tv_patient_condition_complete:

                clickComplete = true;

                if (abstractEditText == null ||
                    TextUtils.isEmpty(abstractEditText.getText().toString())) {
                    mAbstract = "";
                } else {
                    mAbstract = abstractEditText.getText().toString();
                }

                if (inspectionEditText == null ||
                    TextUtils.isEmpty(inspectionEditText.getText().toString())) {
                    mInspection = "";
                } else {
                    mInspection = inspectionEditText.getText().toString();
                }

                if (toBeSolvedEditText == null ||
                    TextUtils.isEmpty(toBeSolvedEditText.getText().toString())) {
                    mToBeSolved = "";
                } else {
                    mToBeSolved = toBeSolvedEditText.getText().toString();
                }
                updatePatientInfo(true);
                break;
            case R.id.ll_information:
                clickInformation();
                break;
            default:
                break;
        }
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

        String[] list2 = { mContext.getString(R.string.patient_condition_male),
            mContext.getString(R.string.patient_condition_female) };

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

        showPickerDialog(list1, list2, list3, list4);
    }


    private void updatePatientInfo(boolean showLoading) {
        final HPUUpdatePatient up = new HPUUpdatePatient() {
            @Override
            protected void onSuccess(CurInfo responseContent) {
                super.onSuccess(responseContent);
                updateFinished = true;
                if(uploadFinished&&updateFinished){
                    removeLoadingView();
                    CustomLog.d(TAG, "HPUUpdatePatient,onSuccess");
                    if (newCurInfo != null && newCurInfo.getToUDT()) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(PatientConditionActivity.this, ReserveDTSuccessActivity.class);
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable("newCurInfo", newCurInfo);
                        intent.putExtras(mBundle);
                        startActivity(intent);
                        finish();
                    }
                }
            }


            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.d(TAG, "HPUUpdatePatient,onFail,statusCode:" + String.valueOf(statusCode)                    + " statusInfo" + statusInfo);

                removeLoadingView();
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(PatientConditionActivity.this)
                        .tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(PatientConditionActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };

        if(uploadFinished){
            try {
                for (int a = 0; a < mMutiTopList.size() - 1; a++) {
                    if (TextUtils.isEmpty(mCheck)) {
                        mCheck = mMutiTopList.get(a).getHeadUrl();
                    } else {
                        if(!TextUtils.isEmpty(mMutiTopList.get(a).getHeadUrl())){
                            mCheck = mCheck + "," + mMutiTopList.get(a).getHeadUrl();
                        }
                    }
                }

                CustomLog.d(TAG, "mCheck:" + mCheck);
            } catch (Exception e) {
                CustomLog.e(TAG, "error!" + e.toString());
            }
            clickComplete = false;
            up.updatePatient(AccountManager.getInstance(mContext).getMdsToken(),
                newCurInfo.getId(), newCurInfo.getName(),
                newCurInfo.getCardType(), mAge, newCurInfo.getCard(), newCurInfo.getMobile(),
                mGender, mHeight, mWeight, mAbstract, mInspection, mCheck, mToBeSolved);

            mCheck = "";
        }

        if(showLoading){
            showLoadingView(mContext.getString(R.string.reserve_treatment_loading),
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        clickComplete = false;
                        removeLoadingView();
                        up.cancel();
                        CustomToast.show(getApplicationContext(),
                            getString(R.string.reserve_treatment_success_cancel), Toast.LENGTH_SHORT);
                    }
                });
        }

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
                for(int i=0;i<mMutiTopList.size();i++){
                    if(mMutiTopList.get(i).getNubeNumber().equals(path)){
                        mMutiTopList.get(i).setHeadUrl(dataInfo.filepath);
                    }
                }
                sentPicNumber++;
                if(sentPicNumber==selectedPicList.size()){
                    uploadFinished = true;
                    if(clickComplete){
                        updatePatientInfo(false);
                    }
                }

                String filepath = dataInfo.getFilepath();
                if (filepath == null) {
                    CustomToast.show(getApplicationContext(), getString(R.string.upload_pic_fail),
                        Toast.LENGTH_SHORT);
                }

                if(uploadFinished&&updateFinished){
                    removeLoadingView();
                    CustomLog.d(TAG, "HPUUpdatePatient,onSuccess");
                    if (newCurInfo != null && newCurInfo.getToUDT()) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(PatientConditionActivity.this, ReserveDTSuccessActivity.class);
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable("newCurInfo", newCurInfo);
                        intent.putExtras(mBundle);
                        startActivity(intent);
                        finish();
                    }
                }

            }

            @Override
            public void onProgress(int persent) {
            }

            @Override
            public void onFailed(int statusCode, String msg) {
                sentPicNumber++;
                if(sentPicNumber==selectedPicList.size()){
                    uploadFinished = true;
                    if(clickComplete){
                        updatePatientInfo(false);
                    }
                }

                if (HttpErrorCode.checkNetworkError(statusCode)) {
                    CustomToast.show(PatientConditionActivity.this,
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
    public  File getThumFile(File file){
        File tmpFile = null;
        try{
            tmpFile = new Compressor(this).compressToFile(file);
        }catch(IOException e){
            CustomLog.d(TAG,"压缩图片异常  error="+e.getMessage());
            tmpFile = file;
            return  tmpFile;
        }
        return tmpFile;
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


    private void showDialog() {
        cid.setCameraClickListener(new CameraImageDialog.CameraClickListener() {
            @Override
            public void clickListener() {
                boolean result = CommonUtil.selfPermissionGranted(PatientConditionActivity.this, Manifest.permission.CAMERA);
                if (!result) {
                    // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PermissionGen.with(PatientConditionActivity.this)
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
                SendCIVMDTUtil.sendDTPatientPic(PatientConditionActivity.this);
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
        final CustomDialog dialog = new CustomDialog(PatientConditionActivity.this);
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
                intent.setData(Uri.parse("package:" + PatientConditionActivity.this.getPackageName()));
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
