package cn.redcdn.hvs.udtcenter.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.redcdn.datacenter.CommonCode;
import cn.redcdn.datacenter.hpucenter.data.CSLInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.udtroom.configs.UDTDataConstant;
import cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity;
import cn.redcdn.log.CustomLog;
import java.util.ArrayList;

/**
 * Created by dell on 2017/11/21.
 */

public class ResponseDtFragmentRecyAdapter extends RecyclerView.Adapter<ResponseDtFragmentRecyAdapter.MyViewHolder> {
    private static final String TAG = ResponseDtFragmentRecyAdapter.class.getName();
    private ArrayList<CSLInfo> mArrayList;
    private Context mContext;

    public ResponseDtFragmentRecyAdapter(ArrayList<CSLInfo> arrayList, Context context) {
        mArrayList = arrayList;
        mContext = context;
    }


    @Override
    public ResponseDtFragmentRecyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.response_dt_item, parent, false);

        return new MyViewHolder(view);
    }

    /**
     * 把数据填充到控件中
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ResponseDtFragmentRecyAdapter.MyViewHolder holder, final int position) {
        CustomLog.d(TAG, "患者姓名" + mArrayList.get(position).getPatientName());
        holder.patientNameText.setText(mArrayList.get(position).getPatientName() + mContext.getString(R.string.udt_room));
        holder.illText.setVisibility(View.GONE);
        CustomLog.d(TAG, "求诊者医院" + mArrayList.get(position).getRequestHosp());
        holder.requestDoctorHospital.setText(mArrayList.get(position).getRequestHosp());
        CustomLog.d(TAG, "求诊者部门" + mArrayList.get(position).getRequestDep());
        holder.requestDoctorDepartment.setText(mArrayList.get(position).getRequestDep());
        CustomLog.d(TAG, "求诊者姓名" + mArrayList.get(position).getRequestName());
        holder.requestDoctorName.setText(mArrayList.get(position).getRequestName());
        String month_day = mArrayList.get(position).getSchedulDate().substring(4);
        String month = month_day.substring(0, 2);
        String day = month_day.substring(2, 4);
        String date = month + mContext.getString(R.string.month) + day + mContext.getString(R.string.day);
        CustomLog.d(TAG, "日期" + mArrayList.get(position).getSchedulDate());
        holder.requestDate.setText(date);
        CustomLog.d(TAG, "时间段" + mArrayList.get(position).getRange());
        holder.requestTime.setText(mArrayList.get(position).getRange());
        // 若是求诊医生，将 fromText 设置为 To:, 默认为 From:
        if (mArrayList.get(position).getRequestNubeNumber().equals(AccountManager.getInstance(mContext).getNube())){
            holder.fromText.setText("To:");
        }
//        holder.reservationNumber.setText(mArrayList.get(position).getState() + "");
        if (mArrayList.get(position).getState() == CommonCode.SEEK_STATE_NOW) {
            //接诊中
            CustomLog.d(TAG, "接诊中");
            holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_blue));
            holder.reservationNumber.setText(mContext.getString(R.string.responsing_dt));

        } else if (mArrayList.get(position).getState() == CommonCode.SEEK_STATE_NOT) {
            //待接诊
            CustomLog.d(TAG, "待接诊");
            holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_yellow));
            holder.reservationNumber.setText(mContext.getString(R.string.wait_response_dt));

        } else if (mArrayList.get(position).getState() == CommonCode.SEEK_STATE_END) {
            //结束
            CustomLog.d(TAG, "结束");
            holder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
            holder.reservationNumber.setText(mContext.getString(R.string.ended));
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
        private TextView requestDoctorHospital;
        private TextView requestDoctorDepartment;
        private TextView requestDoctorName;
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
            requestDoctorHospital = (TextView) itemView.findViewById(R.id.request_doctor_hospital);
            requestDoctorDepartment = (TextView) itemView.findViewById(R.id.request_doctor_department);
            requestDoctorName = (TextView) itemView.findViewById(R.id.request_doctor_name);
            requestDate = (TextView) itemView.findViewById(R.id.response_doctor_name);
            requestTime = (TextView) itemView.findViewById(R.id.state_text);
            fromText = (TextView) itemView.findViewById(R.id.response_dt_item_from);
        }
    }
}
