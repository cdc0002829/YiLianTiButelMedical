package cn.redcdn.hvs.profiles.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.activity.DoctorActivity;
import cn.redcdn.hvs.accountoperate.activity.MedicalActivity;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.profiles.listener.DisplayImageListener;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.AccountManager.getInstance;

/**
 * Created by Administrator on 2017/3/11.
 */
public class CertificationActivity extends BaseActivity {

    private TextView Review_NUM;
    private ImageView imageIv;
    private DisplayImageListener mDisplayImageListener = null;
    private TextView modifyTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certifycation);
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        String workUnitType = getInstance(getApplicationContext())
                .getAccountInfo().getWorkUnitType();
        if (!workUnitType.equals("")){
            if (workUnitType.equals("2")){
                titleBar.setTitle(getString(R.string.medical_people_certification));
            }else {
                titleBar.setTitle(getString(R.string.doctor_certified));
            }
        }
        initView();
        initData();
        mDisplayImageListener=new DisplayImageListener();
    }

    private void initView() {
        Review_NUM= (TextView) findViewById(R.id.auding_tv);
        Review_NUM.setOnClickListener(mbtnHandleEventListener);
        Review_NUM.setText(SettingData.getInstance().REVIEW_NUM);
        imageIv = (ImageView) findViewById(R.id.image);
        modifyTv = (TextView) findViewById(R.id.modify_tv);
        modifyTv.setOnClickListener(mbtnHandleEventListener);
    }

    private void initData() {
      String certificatePreview=  AccountManager.getInstance(this)
                .getAccountInfo().certificateThum;
        if (certificatePreview != null && !certificatePreview.equalsIgnoreCase("")) {
            CustomLog.d(TAG, "显示图片");
            show(certificatePreview);
            imageIv.setOnClickListener(mbtnHandleEventListener);
        }
    }

    private void show(String image) {
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
        imageLoader.displayImage(image, imageIv, options, mDisplayImageListener);
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i){
            case R.id.image:
                Intent intent_inputimage = new Intent(this, OpenBigImageActivity.class);
                intent_inputimage.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                intent_inputimage.putExtra(OpenBigImageActivity.DATE_URL,  AccountManager.getInstance(this)
                        .getAccountInfo().getCertificateThum());
                startActivity(intent_inputimage);
            break;
            case R.id.modify_tv:
                if (AccountManager.getInstance(this).getAccountInfo().getWorkUnitType().equals(String.valueOf(1))) {
                    Intent intent=new Intent(this, DoctorActivity.class);
                    intent.putExtra("from_modify",true);
                    intent.putExtra("workUnitType",String.valueOf(1));
                    intent.putExtra("workType",String.valueOf(1));
                    startActivity(intent);
                }else {
                    Intent intent=new Intent(this, MedicalActivity.class);
                    intent.putExtra("from_modify",true);
                    intent.putExtra("workUnitType",String.valueOf(2));
                    intent.putExtra("workType",String.valueOf(2));
                    startActivity(intent);
                }
                break;
            case R.id.auding_tv:
                if (!SettingData.getInstance().REVIEW_NUM.isEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if(CertificationActivity.this.checkSelfPermission(Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED) {
                            Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                    + SettingData.getInstance().REVIEW_NUM));
                            startActivity(i1);
                        }else{
                        }
                    }else{
                        Intent i1 = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                                + SettingData.getInstance().REVIEW_NUM));
                        startActivity(i1);
                    }

                }
                break;
        }
    }
}
