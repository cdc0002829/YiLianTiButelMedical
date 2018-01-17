package cn.redcdn.hvs.revolutiondt.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.redcdn.datacenter.hpucenter.data.HPUCommonCode;
import cn.redcdn.datacenter.hpucenter.data.TFInfo;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.head.activity.ReferralActivity;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;

/**
 * Created by dell on 2017/11/28.
 */

public class RevolutionDTRecyAdapter extends RecyclerView.Adapter<RevolutionDTRecyAdapter.MyViewHolder> {
    private String TAG = getClass().getName();
    private ArrayList<TFInfo> revolutionList;
    private Context mContext;

    public RevolutionDTRecyAdapter(ArrayList<TFInfo> arrayList, Context context) {
        revolutionList = arrayList;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.response_dt_item, parent, false);
        return new MyViewHolder(view);
    }
    /**
     * 对控件进行相应的赋值
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        CustomLog.d(TAG, "患者姓名" + revolutionList.get(position).getPatientName());
        holder.patientNameText.setText(revolutionList.get(position).getPatientName());
        holder.illText.setVisibility(View.GONE);
        holder.fromText.setText(mContext.getString(R.string.move_to));
        CustomLog.d(TAG, "接诊者医院" + revolutionList.get(position).getTransferHosp());
        holder.responseDoctorHospital.setText(revolutionList.get(position).getTransferHosp());
        CustomLog.d(TAG, "接诊者部门" + revolutionList.get(position).getTransferDept());
        holder.responseDoctorDepartment.setText(revolutionList.get(position).getTransferDept());
        CustomLog.d(TAG, "接诊者类型" + revolutionList.get(position).getTransferDept());
        if (revolutionList.get(position).getSectionType().equals(HPUCommonCode.TF_DEP_PROFESSION)) {
            CustomLog.d(TAG, "专家姓名" + revolutionList.get(position).getExpertName());
            holder.responseDoctorName.setText(revolutionList.get(position).getExpertName());
        } else if (revolutionList.get(position).getSectionType().equals(HPUCommonCode.TF_DEP_BETTER)) {
            CustomLog.d(TAG, "副高门诊" + revolutionList.get(position).getExpertName());
            holder.responseDoctorName.setText(mContext.getString(R.string.subtropical_high_outpatient));
        } else {
            CustomLog.d(TAG, "普通门诊" + revolutionList.get(position).getExpertName());
            holder.responseDoctorName.setText(mContext.getString(R.string.normal_outpatient));
        }
        if (!TextUtils.isEmpty(revolutionList.get(position).getTransferSchedulDate())) {
            String month_day = revolutionList.get(position).getTransferSchedulDate().substring(4);
            String month = month_day.substring(0, 2);
            String day = month_day.substring(2, 4);
            String date = month + mContext.getString(R.string.month) + day + mContext.getString(R.string.day);
            CustomLog.d(TAG, "日期" + revolutionList.get(position).getTransferSchedulDate());
            holder.requestDate.setText(date);
        }
        CustomLog.d(TAG, "上午下午的标识" + revolutionList.get(position).getTransferRangeFlg());
        if (revolutionList.get(position).getTransferRangeFlg().equals(HPUCommonCode.RANGE_MORING)){
            holder.requestTime.setText(mContext.getString(R.string.forenoon));
        } else {
            holder.requestTime.setText(mContext.getString(R.string.afternoon));
        }

        if (TextUtils.isEmpty(revolutionList.get(position).getState())) {
            CustomLog.d(TAG, "状态为null");
        } else {
            if (revolutionList.get(position).getState().equals(HPUCommonCode.TF_READY)) {
                //待就诊
                CustomLog.d(TAG, "待就诊");
                holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
                holder.reservationNumber.setText(mContext.getString(R.string.wait_visit));

            } else if (revolutionList.get(position).getState().equals(HPUCommonCode.TF_ARLEADY)) {
                //已就诊
                CustomLog.d(TAG, "已就诊");
                holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
                holder.reservationNumber.setText(mContext.getString(R.string.have_visit));
            }else if (revolutionList.get(position).getState().equals(HPUCommonCode.TF_IS_VERIFING)){
                //审核中
                CustomLog.d(TAG, "审核中");
                holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
                holder.reservationNumber.setText(mContext.getString(R.string.is_verifing));

            }else if (revolutionList.get(position).getState().equals(HPUCommonCode.TF_NOT_PASS)){
                //未通过
                CustomLog.d(TAG, "未通过");
                holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_red));
                holder.reservationNumber.setText(mContext.getString(R.string.not_pass));
            }
        }
        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ReferralActivity.class);
                intent.putExtra(ReferralActivity.REFERRAL, revolutionList.get(position).getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return revolutionList.size();
    }

    /**
     * 初始化控件
     */

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout item_layout;
        private ImageView reservationNumberImage;
        private TextView reservationNumber;
        private TextView patientNameText;
        private TextView illText;
        private TextView responseDoctorHospital;
        private TextView responseDoctorDepartment;
        private TextView responseDoctorName;
        private TextView requestDate;
        private TextView requestTime;
        private TextView fromText;

        public MyViewHolder(View itemView) {
            super(itemView);
            init(itemView);
        }

        private void init(View itemView) {
            item_layout = (RelativeLayout) itemView.findViewById(R.id.response_dt_item_lay);
            reservationNumberImage = (ImageView) itemView.findViewById(R.id.response_dt_item_image);
            reservationNumber = (TextView) itemView.findViewById(R.id.reservation_number_text);
            patientNameText = (TextView) itemView.findViewById(R.id.patient_name_text);
            illText = (TextView) itemView.findViewById(R.id.ill_text);
            responseDoctorHospital = (TextView) itemView.findViewById(R.id.request_doctor_hospital);
            responseDoctorDepartment = (TextView) itemView.findViewById(R.id.request_doctor_department);
            responseDoctorName = (TextView) itemView.findViewById(R.id.request_doctor_name);
            requestDate = (TextView) itemView.findViewById(R.id.response_doctor_name);
            requestTime = (TextView) itemView.findViewById(R.id.state_text);
            fromText = (TextView) itemView.findViewById(R.id.response_dt_item_from);
        }
    }
}
