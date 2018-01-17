package cn.redcdn.hvs.accountoperate.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Created by thinkpad on 2017/2/20.
 */

public class CardTypeActivity extends cn.redcdn.hvs.base.BaseActivity{
    private ImageView btn_doctor,btn_medical;
    protected final String TAG = getClass().getName();
    private  ImageView  btn_doctor_select,btn_medical_select;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardtype);
        initView();
        TitleBar titleBar = getTitleBar();
        titleBar.setTitle(getString(R.string.choose_type));
        titleBar.setTitleTextColor(Color.BLACK);
    }

    private void initView() {
        btn_doctor = (ImageView) findViewById(R.id.btn_doctor);
        btn_medical = (ImageView) findViewById(R.id.btn_medical);
        btn_doctor_select= (ImageView) findViewById(R.id.btn_doctor_select);
        btn_medical_select= (ImageView) findViewById(R.id.btn_medical_select);

        btn_doctor.setOnClickListener(mbtnHandleEventListener);
        btn_medical.setOnClickListener(mbtnHandleEventListener);
    }




    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i){
            case R.id.btn_medical:
                btn_medical_select.setVisibility(View.VISIBLE);
                btn_doctor_select.setVisibility(View.GONE);
                //单位类型(1：医院、2：公司)
                String workUnitType1 = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getWorkUnitType();
                Intent intent_medical = new Intent(CardTypeActivity.this,MedicalActivity.class);
                intent_medical.putExtra("workUnitType",workUnitType1);
                intent_medical.putExtra("workType",String.valueOf(2));
                startActivity(intent_medical);
                CustomLog.d(TAG,"点击跳转到医疗从业者界面");
                break;
            case R.id.btn_doctor:
                btn_doctor_select.setVisibility(View.VISIBLE);
                btn_medical_select.setVisibility(View.GONE);
                //单位类型(1：医院、2：公司)
                String workUnitType2 = AccountManager.getInstance(MedicalApplication.context).getAccountInfo().getWorkUnitType();
                Intent intent_docter = new Intent(CardTypeActivity.this,DoctorActivity.class);
                intent_docter.putExtra("workUnitType",workUnitType2);
                intent_docter.putExtra("workType",String.valueOf(1));
                startActivity(intent_docter);
                CustomLog.d(TAG,"点击跳转到医生界面");
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(CardTypeActivity.this,LoginActivity.class);
        startActivity(intent);
        CardTypeActivity.this.finish();
    }
}
