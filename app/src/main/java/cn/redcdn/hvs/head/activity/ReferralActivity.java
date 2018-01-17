package cn.redcdn.hvs.head.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.HPUGetTfDetailInfo;
import cn.redcdn.datacenter.hpucenter.data.TFdetailInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.contacts.contact.interfaces.Contact;
import cn.redcdn.hvs.profiles.activity.YuYueZhuanZhenActivity;
import cn.redcdn.hvs.requesttreatment.ImagePagerAdapterList;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.OpenBigImageActivity;
import cn.redcdn.hvs.util.TitleBar;

/**
 * Created by Administrator on 2017/11/29.
 */

public class ReferralActivity extends BaseActivity {
    public static final String REFERRAL = "referral";

    private ViewPager viewPager = null;
    private LinearLayout ll_patient_condition_viewpager;
    private LinearLayout my_fuzhu;
    private View my_view;
    private TitleBar titleBar;
    private TextView patientNameTv;
    private ImageView phoneView;
    private LinearLayout phoneLl;
    private TextView ageTv;
    private TextView reTijiao;
    private TextView sex;
    private TextView height;
    private TextView weight;
    private TextView birth;
    private TextView jianhurenTv;
    private TextView shenfenzhengTv;
    private TextView zhusuTv;
    private TextView shenheyisheng;
    private CheckBox local_treat;//本地治疗
    private CheckBox transfer_treat;//转诊
    private TextView daijiewentiTv;
    private TextView tv_check_local;
    private TextView tv_exchange;
    private TextView chatiTv;
    private TextView qiuZhenYiYaun;
    private TextView qiuZhenYiYaun1;
    private TextView qiuZhenYiSheng;
    private TextView qiuZhenYiSheng1;
    private TextView qiuZhenYiShengKeShi;
    private TextView qiuZhenYiShengKeShi1;
    private TextView qiuZhenYiShengKeSh;
    private TextView jieZhenYiYuan;
    private TextView jieZhenYiSheng;
    private TextView jieZhenYiShengKeShi;
    private TextView jieZhenYiShengKeSh;
    private TextView jieZhenRiQi;
    private TextView jieZhenShiDuan;
    private TextView zhenLiaoJianYi;
    private TextView zhenLiaoZhaiYao;
    private TextView zhuanZhenYiYuan;
    private TextView zhuanZhenKeShi;
    private TextView leiXing;
    private TextView zhuanZhenYiSheng;
    private TextView zhuanZhenShiJian;
    private TextView zhuanZhenShiJian1;
    private ImageView erweima;
    private LinearLayout jianhurenLl;
    private ImageView jianhurenView;
    private LinearLayout jianhurenCardLl;
    private ImageView jianhurenCardView;
    private TextView zhenJianLeiXing;
    private LinearLayout huanzheshengao;
    private ImageView huanzheshengaoView;
    private LinearLayout zhuanzhenyishengLl;
    private ImageView zhuanzhenyishengView;
    private LinearLayout jiuzhenqingkuang;
    private TextView jiuzhenqingkaungTv;
    private TextView jiuzhenqingkuangRiqi;
    private String imageUrl = "";
    private String id;
    private LinearLayout lL;
    private LinearLayout lianheLl;
    private LinearLayout lla;
    public final static String REFERRAL_ACTIVITY= "REFERRAL_ACTIVITY";
    private TFdetailInfo mResponseContent=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);
        MedicalApplication.addDestoryActivity(ReferralActivity.this, ReferralActivity.REFERRAL_ACTIVITY);
        titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.zhuanzhendan));
        titleBar.setTitleTextColor(Color.BLACK);
        initView();
        getData();
    }

    private void getData() {
        Intent intent = getIntent();
        id = intent.getStringExtra(REFERRAL);
        if (!TextUtils.isEmpty(id)) {
            final HPUGetTfDetailInfo hpuGetTfDetailInfo = new HPUGetTfDetailInfo() {
                @Override
                protected void onSuccess(TFdetailInfo responseContent) {
                    super.onSuccess(responseContent);
                    mResponseContent=responseContent;
                    removeLoadingView();
                    int transferState = responseContent.getTransferType();
                    if (1==transferState){
                        transfer_treat.setChecked(true);
                        local_treat.setChecked(false);
                        tv_check_local.setTextColor(Color.parseColor("#cacaca"));
                    }
                    else if (2==transferState){
                        transfer_treat.setChecked(false);
                        local_treat.setChecked(true);
                        tv_exchange.setTextColor(Color.parseColor("#cacaca"));
                    }
                    if (!TextUtils.isEmpty(responseContent.assCheckUrl)){
                        String url[] = responseContent.assCheckUrl.split(",");
                        List<Contact> contactList = new ArrayList<>();
                        for (int i = 0; i < url.length; i++) {
                            Contact contact = new Contact();
                            contact.setHeadUrl(url[i]);
                            contactList.add(contact);
                        }
                        ImagePagerAdapterList adapterList = new ImagePagerAdapterList(ReferralActivity.this, contactList, 4, 1, false, null);
                        adapterList.disableLongClick();
                        viewPager.setAdapter(adapterList);
                    }else {
                        ll_patient_condition_viewpager.setVisibility(View.GONE);
                        my_fuzhu.setVisibility(View.GONE);
                        my_view.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(responseContent.transferState) && (Integer.parseInt(responseContent.transferState) == 1)) {
                        jiuzhenqingkaungTv.setText("待就诊");
                        Drawable drawable = getResources().getDrawable(R.drawable.yjiuzhen);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        jiuzhenqingkaungTv.setCompoundDrawables(null, drawable, null, null);
                        jiuzhenqingkuangRiqi.setText("转诊申请成功，已向患者发送转诊通知短信。\n请在红云医疗诊疗版（PC）进行打印转诊单，并交予患者。");
                    } else if (!TextUtils.isEmpty(responseContent.transferState) && (Integer.parseInt(responseContent.transferState) == 2)) {
                        Drawable drawable = getResources().getDrawable(R.drawable.yjiuzhen);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        jiuzhenqingkaungTv.setCompoundDrawables(null, drawable, null, null);
                        jiuzhenqingkaungTv.setText(getString(R.string.yijiuzhen));
                        if (responseContent.confirmDate != "null" && !TextUtils.isEmpty(responseContent.confirmDate)) {
                            long l = Long.parseLong(responseContent.confirmDate + "000");
                            Date d = new Date(l);
                            SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日HH:mm");
                            String format1 = format.format(d);
                            jiuzhenqingkuangRiqi.setText("患者已于" + format1 + "挂号就诊");
                        }
                    } else if (!TextUtils.isEmpty(responseContent.transferState) && (Integer.parseInt(responseContent.transferState) == 3)) {
                        jiuzhenqingkaungTv.setText("审核中");
                        Drawable drawable = getResources().getDrawable(R.drawable.shenhezhong);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        jiuzhenqingkaungTv.setCompoundDrawables(null, drawable, null, null);
                        jiuzhenqingkuangRiqi.setText("您的转诊申请已提交，预计1个工作日完成审核，请您耐心等待!");
                    } else if (!TextUtils.isEmpty(responseContent.transferState) && (Integer.parseInt(responseContent.transferState) == 4)) {
                        jiuzhenqingkaungTv.setText("未通过");
                        reTijiao.setVisibility(View.VISIBLE);
                        Drawable drawable = getResources().getDrawable(R.drawable.weitongguo);
                        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                        jiuzhenqingkaungTv.setCompoundDrawables(null, drawable, null, null);
                        jiuzhenqingkuangRiqi.setText("原因：" + responseContent.reason);
                    }
                    if (!TextUtils.isEmpty(responseContent.getPatientName())) {
                        patientNameTv.setText(responseContent.getPatientName());
                    }
                    if (!TextUtils.isEmpty(responseContent.getPatientAge())) {
                        huanzheshengao.setVisibility(View.VISIBLE);
                        huanzheshengaoView.setVisibility(View.VISIBLE);
                    } else {
                        huanzheshengao.setVisibility(View.GONE);
                        huanzheshengaoView.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(responseContent.getPatientAge())) {
                        ageTv.setText(responseContent.getPatientAge() + ",");
                    }
                    if (!TextUtils.isEmpty(responseContent.getPatientSex())) {
                        sex.setText(responseContent.getPatientSex() + "性,");
                    }
                    if (!TextUtils.isEmpty(responseContent.getHeight())) {
                        height.setText(responseContent.getHeight() + ",");
                    }
                    if (!TextUtils.isEmpty(responseContent.getWeight())) {
                        weight.setText(responseContent.getWeight());
                    }
                    if (!TextUtils.isEmpty(responseContent.getPatientMobile())) {
                        birth.setText(responseContent.getPatientMobile());
                        phoneLl.setVisibility(View.VISIBLE);
                        phoneView.setVisibility(View.VISIBLE);
                    }else {
                        phoneLl.setVisibility(View.GONE);
                        phoneView.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(responseContent.getGuardName())) {
                        jianhurenTv.setText(responseContent.getGuardName());
                        jianhurenLl.setVisibility(View.VISIBLE);
                        jianhurenView.setVisibility(View.VISIBLE);
                    } else {
                        jianhurenLl.setVisibility(View.GONE);
                        jianhurenView.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(responseContent.getGuardCard())) {
                        shenfenzhengTv.setText(responseContent.getGuardCard());
                        jianhurenCardLl.setVisibility(View.VISIBLE);
                        jianhurenCardView.setVisibility(View.VISIBLE);
                    } else {
                        jianhurenCardLl.setVisibility(View.GONE);
                        jianhurenCardView.setVisibility(View.GONE);
                    }
                    if (!TextUtils.isEmpty(responseContent.getChief())) {
                        zhusuTv.setText(responseContent.getChief());
                    } else {
                        zhusuTv.setText(getString(R.string.zanwuzhusu));
                        zhusuTv.setTextColor(Color.parseColor("#8D8D8D"));
                    }
                    if (!TextUtils.isEmpty(responseContent.responseName)) {
                        shenheyisheng.setText(responseContent.responseName+" "+responseContent.responseDep+" "+responseContent.responseHosp);
                    } else {
                        shenheyisheng.setText("后台审核");
                    }
                    if (!TextUtils.isEmpty(responseContent.getProblem())) {
                        daijiewentiTv.setText(responseContent.getProblem());
                    } else {
                        daijiewentiTv.setText(getString(R.string.udt_no_resolve_problem));
                        daijiewentiTv.setTextColor(Color.parseColor("#8D8D8D"));
                    }
                    if (!TextUtils.isEmpty(responseContent.getPhysical())) {
                        chatiTv.setText(responseContent.getPhysical());
                    } else {
                        chatiTv.setText(getString(R.string.udt_no_exambody_news));
                        chatiTv.setTextColor(Color.parseColor("#8D8D8D"));
                    }
                    if (!TextUtils.isEmpty(responseContent.requestHosp)) {
                        qiuZhenYiYaun.setText(responseContent.requestHosp);
                    }
                    if (!TextUtils.isEmpty(responseContent.appointHosp)) {
                        qiuZhenYiYaun1.setText(responseContent.appointHosp);
                    }
                    if (!TextUtils.isEmpty(responseContent.requestName)) {
                        qiuZhenYiSheng.setText(responseContent.requestName);
                    }
                    if (!TextUtils.isEmpty(responseContent.appointName)) {
                        qiuZhenYiSheng1.setText(responseContent.appointName);
                    }
                    if (!TextUtils.isEmpty(responseContent.requestDep)) {
                        qiuZhenYiShengKeShi.setText(responseContent.requestDep);
                        qiuZhenYiShengKeSh.setText(responseContent.requestDep);
                    }
                    if (!TextUtils.isEmpty(responseContent.appointDept)) {
                        qiuZhenYiShengKeShi1.setText(responseContent.appointDept);
                    }
                    if (!TextUtils.isEmpty(responseContent.responseHosp)) {
                        jieZhenYiYuan.setText(responseContent.responseHosp);
                    }
                    if (!TextUtils.isEmpty(responseContent.responseName)) {
                        jieZhenYiSheng.setText(responseContent.responseName);
                    }
                    if (!TextUtils.isEmpty(responseContent.responseDep)) {
                        jieZhenYiShengKeShi.setText(responseContent.responseDep);
                        jieZhenYiShengKeSh.setText(responseContent.responseDep);
                    }
                    if (!"".equals(responseContent.schedulDate) && responseContent.schedulDate.length() >= 8) {
                        String substring = responseContent.schedulDate.substring(4, 6);
                        String substring1 = responseContent.schedulDate.substring(responseContent.schedulDate.length() - 2, responseContent.schedulDate.length());
                        jieZhenRiQi.setText(substring + MedicalApplication.getContext().getString(R.string.month) + substring1 + MedicalApplication.getContext().getString(R.string.day));
                    }
                    if (!TextUtils.isEmpty(responseContent.rangeNumber)) {
                        jieZhenShiDuan.setText(responseContent.rangeNumber);
                    }
                    if (!TextUtils.isEmpty(responseContent.transferFlg)) {
                        if (Integer.parseInt(responseContent.transferFlg) == 1) {
                            zhenLiaoJianYi.setText(getString(R.string.bendi));
                        } else if (Integer.parseInt(responseContent.transferFlg) == 2) {
                            zhenLiaoJianYi.setText(R.string.udt_transfer_treatmeat);
                        }
                    }
                    if (!TextUtils.isEmpty(responseContent.auditAdvice)) {
                        zhenLiaoZhaiYao.setText(responseContent.auditAdvice);
                    } else {
                        zhenLiaoZhaiYao.setText("暂无意见");
                        zhenLiaoZhaiYao.setTextColor(Color.parseColor("#8D8D8D"));
                    }
                    if (!TextUtils.isEmpty(responseContent.transferHosp)) {
                        zhuanZhenYiYuan.setText(responseContent.transferHosp);
                    }
                    if (!TextUtils.isEmpty(responseContent.transferDept)){
                        zhuanZhenKeShi.setText(responseContent.transferDept);
                    }
                    if (!TextUtils.isEmpty(responseContent.sectionType)) {
                        switch (responseContent.sectionType) {
                            case "1":
                                leiXing.setText(getString(R.string.normal_outpatient));
                                break;
                            case "2":
                                leiXing.setText( getString(R.string.subtropical_high_outpatient));
                                break;
                            case "3":
                                leiXing.setText(getString(R.string.zhuanjia_outpatient) );
                                break;
                            default:
                                break;
                        }
                    }
                    if (!TextUtils.isEmpty(responseContent.expertName)) {
                        zhuanZhenYiSheng.setText(responseContent.expertName);
                        zhuanzhenyishengLl.setVisibility(View.VISIBLE);
                        zhuanzhenyishengView.setVisibility(View.VISIBLE);
                    } else {
                        zhuanzhenyishengLl.setVisibility(View.GONE);
                        zhuanzhenyishengView.setVisibility(View.GONE);
                    }
                    if (!"".equals(responseContent.transferSchedulDate) && responseContent.transferSchedulDate.length() >= 8 && !TextUtils.isEmpty(responseContent.transferRangeName)) {
                        String result=responseContent.transferSchedulDate.substring(0,4);
                        String substring = responseContent.transferSchedulDate.substring(4, 6);
                        String substring1 = responseContent.transferSchedulDate.substring(responseContent.transferSchedulDate.length() - 2, responseContent.transferSchedulDate.length());
                        zhuanZhenShiJian.setText(result+MedicalApplication.getContext().getString(R.string.reserve_treatment_year)+substring + MedicalApplication.getContext().getString(R.string.month) + substring1 + MedicalApplication.getContext().getString(R.string.day) + responseContent.transferRangeName);
                    }
                    if (!"".equals(responseContent.appointDate) && !TextUtils.isEmpty(responseContent.appointDate)) {
                        long l = Long.parseLong(responseContent.appointDate + "000");
                        Date d = new Date(l);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
                        String format1 = format.format(d);
                        zhuanZhenShiJian1.setText(format1);
                    }
                    if (!TextUtils.isEmpty(responseContent.transferQrCode)) {
                        imageUrl = responseContent.transferQrCode;
                        Glide.with(ReferralActivity.this).load(responseContent.transferQrCode).placeholder(R.drawable.ic_launcher).error(R.drawable.ic_launcher).into(erweima);
                    }
                    if (TextUtils.isEmpty(responseContent.transferState) || "null".equals(responseContent.transferState)) {
                        jiuzhenqingkuang.setVisibility(View.GONE);
                    } else {
                        jiuzhenqingkuang.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                protected void onFail(int statusCode, String statusInfo) {
                    super.onFail(statusCode, statusInfo);
                    removeLoadingView();
                    if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                        AccountManager.getInstance(ReferralActivity.this).tokenAuthFail(statusCode);
                    }
                    CustomToast.show(ReferralActivity.this, getString(R.string.reserve_treatment_get_data_failed), CustomToast.LENGTH_SHORT);
                    ReferralActivity.this.finish();
                }
            };
            hpuGetTfDetailInfo.getdetailInfo(AccountManager.getInstance(ReferralActivity.this).getMdsToken(), id);
            showLoadingView(getString(R.string.loading), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    hpuGetTfDetailInfo.cancel();
                    ReferralActivity.this.finish();
                }
            }, true);
        }

    }

    private void initView() {
        ll_patient_condition_viewpager= (LinearLayout) findViewById(R.id.ll_patient_condition_viewpager);
        my_fuzhu= (LinearLayout) findViewById(R.id.my_fuzhu);
        my_view= (View) findViewById(R.id.my_view);
        viewPager = (ViewPager) findViewById(R.id.patient_condition_check_list);
        viewPager.setHorizontalScrollBarEnabled(false);
        viewPager.setVerticalScrollBarEnabled(false);
        phoneView = (ImageView) findViewById(R.id.iphone_view);
        reTijiao = (TextView) findViewById(R.id.re_tijiao);
        reTijiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(ReferralActivity.this, YuYueZhuanZhenActivity.class);
                intent.putExtra(REFERRAL,id);
                if (mResponseContent!=null){
                    intent.putExtra("data", mResponseContent);
                }
                startActivity(intent);
            }
        });
        phoneLl = (LinearLayout) findViewById(R.id.phone_ll);
        patientNameTv = (TextView) findViewById(R.id.patient_name_tv);
        ageTv = (TextView) findViewById(R.id.age);
        sex = (TextView) findViewById(R.id.sex);
        height = (TextView) findViewById(R.id.height);
        weight = (TextView) findViewById(R.id.weight);
        birth = (TextView) findViewById(R.id.chusheng_tv);
        jianhurenTv = (TextView) findViewById(R.id.jianhuren);
        shenfenzhengTv = (TextView) findViewById(R.id.shenfenzheng);
        zhusuTv = (TextView) findViewById(R.id.zhusu_tv);
        shenheyisheng = (TextView) findViewById(R.id.shenheyisheng);
        local_treat = (CheckBox) findViewById(R.id.local_treat);
        transfer_treat = (CheckBox)findViewById(R.id.transfer_treat);
        daijiewentiTv = (TextView) findViewById(R.id.daijiewenti_tv);
        tv_check_local = (TextView) findViewById(R.id.tv_check_local);
        tv_exchange = (TextView) findViewById(R.id.tv_exchange);
        chatiTv = (TextView) findViewById(R.id.chati_tv);
        qiuZhenYiYaun = (TextView) findViewById(R.id.qiuzhenyiyuan);
        qiuZhenYiYaun1 = (TextView) findViewById(R.id.qiuzhenyiyuan1);
        qiuZhenYiSheng = (TextView) findViewById(R.id.qiuzhenyisheng);
        qiuZhenYiSheng1 = (TextView) findViewById(R.id.qiuzhenyisheng1);
        qiuZhenYiShengKeShi = (TextView) findViewById(R.id.qiuzhenyishengkeshi);
        qiuZhenYiShengKeShi1 = (TextView) findViewById(R.id.qiuzhenyishengkeshi1);
        qiuZhenYiShengKeSh = (TextView) findViewById(R.id.qiuzhenyishengkesh);
        jieZhenYiYuan = (TextView) findViewById(R.id.jiezhenyiyuan);
        jieZhenYiSheng = (TextView) findViewById(R.id.jiezhenyisheng);
        jieZhenYiShengKeShi = (TextView) findViewById(R.id.jiezhenyishengkeshi);
        jieZhenYiShengKeSh = (TextView) findViewById(R.id.jiezhenyishengkesh);
        jieZhenRiQi = (TextView) findViewById(R.id.jiezhenriqi);
        jieZhenShiDuan = (TextView) findViewById(R.id.jiezhenshiduan);
        zhenLiaoJianYi = (TextView) findViewById(R.id.zhenliaojianyi);
        zhenLiaoZhaiYao = (TextView) findViewById(R.id.zhenliaozhaiyao);
        zhuanZhenYiYuan = (TextView) findViewById(R.id.zhuanzhenyiyuan);
        zhuanZhenKeShi = (TextView) findViewById(R.id.zhuanzhenkeshi);
        leiXing = (TextView) findViewById(R.id.menzhenleixing);
        zhuanZhenYiSheng = (TextView) findViewById(R.id.zhuanzhenyisheng);
        zhuanZhenShiJian = (TextView) findViewById(R.id.zhuanzhenshijian);
        zhuanZhenShiJian1 = (TextView) findViewById(R.id.zhuanzhenshijian1);
        erweima = (ImageView) findViewById(R.id.erweima);
        erweima.setOnClickListener(mbtnHandleEventListener);
        jianhurenLl = (LinearLayout) findViewById(R.id.jianhuren_ll);
        jianhurenView = (ImageView) findViewById(R.id.jianhuren_view);
        jianhurenCardLl = (LinearLayout) findViewById(R.id.jianhuren_card_ll);
        jianhurenCardView = (ImageView) findViewById(R.id.jianhurencard_view);
        zhenJianLeiXing = (TextView) findViewById(R.id.zhemhjianleixing);
        huanzheshengao = (LinearLayout) findViewById(R.id.huanzheshengao);
        huanzheshengaoView = (ImageView) findViewById(R.id.huanzheshengao_view);
        zhuanzhenyishengLl = (LinearLayout) findViewById(R.id.zhuanzhenyisheng_ll);
        zhuanzhenyishengView = (ImageView) findViewById(R.id.zhuanzhenyisheng_view);
        jiuzhenqingkuang = (LinearLayout) findViewById(R.id.jiuzhenqingkuang_ll);
        jiuzhenqingkaungTv = (TextView) findViewById(R.id.jiuzheniqngkaung_tv);
        jiuzhenqingkuangRiqi = (TextView) findViewById(R.id.jiuzhenqingkaung_riqi);
        lL = (LinearLayout) findViewById(R.id.ll);
        lianheLl = (LinearLayout) findViewById(R.id.lianhe_ll);
        lla = (LinearLayout) findViewById(R.id.lla);
        lianheLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (lla.getVisibility()==View.GONE){
                   lla.setVisibility(View.VISIBLE);
               }else {
                   lla.setVisibility(View.GONE);
               }
            }
        });
    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i) {
            case R.id.erweima:
                if (!imageUrl.equals("")) {
                    Intent intent = new Intent(this, OpenBigImageActivity.class);
                    intent.putExtra(OpenBigImageActivity.DATE_TYPE, OpenBigImageActivity.DATE_TYPE_Internet);
                    intent.putExtra(OpenBigImageActivity.DATE_URL, imageUrl);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }
}
