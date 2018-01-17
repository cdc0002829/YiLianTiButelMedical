package cn.redcdn.hvs.udtcenter.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jsoup.select.Evaluator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import cn.redcdn.datacenter.CommonCode;
import cn.redcdn.datacenter.hpucenter.data.CSLInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.udtcenter.activity.UDTRoomActivity;
import cn.redcdn.hvs.udtcenter.fragment.RequestDtFragment;
import cn.redcdn.hvs.udtroom.configs.UDTDataConstant;
import cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity;
import cn.redcdn.log.CustomLog;


/**
 * Created by dell on 2017/11/20.
 */

public class RequestDtFragmentRecyAdapter extends RecyclerView.Adapter<RequestDtFragmentRecyAdapter.MyViewHolder> {
    private static final String TAG = RequestDtFragmentRecyAdapter.class.getName();
    private ArrayList<CSLInfo> mArrayList;
    private Context mContext;

    public RequestDtFragmentRecyAdapter(ArrayList<CSLInfo> arrayList, Context context) {
        mArrayList = arrayList;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.response_dt_item, parent, false);

        return new MyViewHolder(view);
    }

    /**
     * 把数据填充到控件中
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        CustomLog.d(TAG, "患者姓名" + mArrayList.get(position).getPatientName());
        holder.patientNameText.setText(mArrayList.get(position).getPatientName() + mContext.getString(R.string.udt_room));
        holder.illText.setVisibility(View.GONE);
        holder.fromText.setText("To:");
        CustomLog.d(TAG, "接诊者医院" + mArrayList.get(position).getResponseHosp());
        holder.responseDoctorHospital.setText(mArrayList.get(position).getResponseHosp());
        CustomLog.d(TAG, "接诊者部门" + mArrayList.get(position).getResponseDep());
        holder.responseDoctorDepartment.setText(mArrayList.get(position).getResponseDep());
        CustomLog.d(TAG, "接诊者姓名" + mArrayList.get(position).getResponseName());
        holder.responseDoctorName.setText(mArrayList.get(position).getResponseName());
        String month_day = mArrayList.get(position).getSchedulDate().substring(4);
        String month = month_day.substring(0, 2);
        String day = month_day.substring(2, 4);
        String date = month + mContext.getString(R.string.month) + day + mContext.getString(R.string.day);
        CustomLog.d(TAG, "日期" + mArrayList.get(position).getSchedulDate());
        holder.requestDate.setText(date);
        holder.requestTime.setText(mArrayList.get(position).getRange());
//        if(mArrayList.get(position).getState())
//        holder.reservationNumber.setText(mArrayList.get(position).getState());
        if (TextUtils.isEmpty(mArrayList.get(position).getState() + "")) {
            CustomLog.d(TAG, "状态为null");
        } else {
            if (mArrayList.get(position).getState() == CommonCode.SEEK_STATE_NOW) {
                //接诊中
                CustomLog.d(TAG, "接诊中");
                holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_blue));
                holder.reservationNumber.setText(mContext.getString(R.string.responsing_dt));

            } else if (mArrayList.get(position).getState() == CommonCode.SEEK_STATE_NOT) {
                //待接诊
                CustomLog.d(TAG, "待接诊");
                holder.reservationNumber.setText(mContext.getString(R.string.wait_response_dt));
                if (getDateNow().equals((mArrayList.get(position).getSchedulDate()))) {
                    holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_yellow));
                } else {
                    holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
                }

            } else if (mArrayList.get(position).getState() == CommonCode.SEEK_STATE_END) {
                //结束
                CustomLog.d(TAG, "结束");
                holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
                holder.reservationNumber.setText(mContext.getString(R.string.ended));
            }
        }
        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UDTChatRoomActivity.class);
                intent.putExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG, mArrayList.get(position).getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
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

    /**
     * 获取当前系统的日期（和服务器返回的时间进行对比，如果不是相同的，不显示接诊按钮）
     *
     * @return
     */
    private String getDateNow() {
        long mill = System.currentTimeMillis() + 60 * 60 * 1000;
        String time = "";
        String createTimeStr = new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss ").format(new java.util.Date(mill));
        Calendar c = getCalendar(createTimeStr);
        int createDay = c.get(Calendar.DATE);
        int createYear = c.get(Calendar.YEAR);
        int createHour = c.get(Calendar.HOUR_OF_DAY);
        CustomLog.d(TAG, "createHour=" + createHour);
        CustomLog.d(TAG, "createl=" + mill);
        int createMonth = c.get(Calendar.MONTH) + 1;
        time = createYear + "";
        if (createMonth < 10) {
            time = time + "0" + createMonth;
        } else {
            time = time + "" + createMonth;
        }
        if (createDay < 10) {
            time = time + "0" + createDay;
        } else {
            time = time + "" + createDay;
        }
        return time;
    }

    public Calendar getCalendar(String datestr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = GregorianCalendar.getInstance();
        try {
            calendar.setTime(sdf.parse(datestr));
        } catch (ParseException e) {
            CustomLog.e(TAG, "getCalendar  " + e.toString());
        }
        return calendar;
    }
}
