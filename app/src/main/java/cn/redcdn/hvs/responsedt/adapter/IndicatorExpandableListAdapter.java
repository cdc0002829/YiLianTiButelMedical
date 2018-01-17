package cn.redcdn.hvs.responsedt.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.redcdn.datacenter.CommonCode;
import cn.redcdn.datacenter.hpucenter.HPUAcceptCsl;
import cn.redcdn.datacenter.hpucenter.data.DepCslInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.udtroom.configs.UDTDataConstant;
import cn.redcdn.hvs.udtroom.view.activity.UDTChatRoomActivity;
import cn.redcdn.hvs.util.CustomDialog;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;


public class IndicatorExpandableListAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "IndicatorExpandableListAdapter";
    private List<String> groupData;
    private List<String> hpuName;
    private List<DepCslInfo> childData;
    //                用于存放Indicator的集合
    private SparseArray<ImageView> mIndicators;
    private onGroupExpandedListener mOnGroupExpandedListener;
    private Context mContext;
    private Dialog dialog = null;
    private String dtId = "";
    private onRefreshListen mrefreshListen;

    public IndicatorExpandableListAdapter(List<String> groupData, List<String> hpuName, List<DepCslInfo> childData, Context context) {
        this.groupData = groupData;
        this.childData = childData;
        this.hpuName = hpuName;
        mIndicators = new SparseArray<>();
        mContext = context;
        CustomLog.d(TAG, "groupData size" + groupData.size());
        CustomLog.d(TAG, "hpuName size" + hpuName.size());
        CustomLog.d(TAG, "childData size" + childData.size());
    }

    public void setOnGroupExpandedListener(onGroupExpandedListener onGroupExpandedListener) {
        mOnGroupExpandedListener = onGroupExpandedListener;
    }

    // 根据分组的展开闭合状态设置指示器
    public void setIndicatorState(int groupPosition, boolean isExpanded) {
        if (isExpanded) {
            mIndicators.get(groupPosition).setImageResource(R.drawable.ic_expand_less);
        } else {
            mIndicators.get(groupPosition).setImageResource(R.drawable.ic_expand_more);
        }
    }

    @Override
    public int getGroupCount() {
        return groupData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childData.get(groupPosition).getCslInfos().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupData.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childData.get(groupPosition).getCslInfos().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * 对GroupView进行处理
     *
     * @param groupPosition
     * @param isExpanded
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expandablelistadapter_item, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.timeText = (TextView) convertView.findViewById(R.id.time);
            groupViewHolder.hpuNameText = (TextView) convertView.findViewById(R.id.hpu_name_textview);
            groupViewHolder.ivIndicator = (ImageView) convertView.findViewById(R.id.iv_indicator);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.timeText.setText(groupData.get(groupPosition));
        if (AccountManager.getInstance(mContext).getHpuList().size() > 1) {
            groupViewHolder.hpuNameText.setText(hpuName.get(groupPosition));
            groupViewHolder.hpuNameText.setVisibility(View.VISIBLE);
        } else {
            groupViewHolder.hpuNameText.setVisibility(View.GONE);
        }
        //      把位置和图标添加到Map
        mIndicators.put(groupPosition, groupViewHolder.ivIndicator);
        //      根据分组状态设置Indicator
        setIndicatorState(groupPosition, isExpanded);
        return convertView;
    }

    /**
     * 对ChildView进行处理
     *
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View
            convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.response_dt_item, parent, false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.responseLay = (RelativeLayout) convertView.findViewById(R.id.response_dt_item_lay);
            childViewHolder.reservationNumberImage = (ImageView) convertView.findViewById(R.id.response_dt_item_image);
            childViewHolder.reservationNumber = (TextView) convertView.findViewById(R.id.reservation_number_text);
            childViewHolder.patientNameText = (TextView) convertView.findViewById(R.id.patient_name_text);
            childViewHolder.illText = (TextView) convertView.findViewById(R.id.ill_text);
            childViewHolder.requestDoctorHospital = (TextView) convertView.findViewById(R.id.request_doctor_hospital);
            childViewHolder.requestDoctorDepartment = (TextView) convertView.findViewById(R.id.request_doctor_department);
            childViewHolder.requestDoctorName = (TextView) convertView.findViewById(R.id.request_doctor_name);
            childViewHolder.responseDoctorName = (TextView) convertView.findViewById(R.id.response_doctor_name);
            childViewHolder.stateText = (TextView) convertView.findViewById(R.id.state_text);
            childViewHolder.responseDtBtn = (Button) convertView.findViewById(R.id.response_dt_btn);
            convertView.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }

        if (childData.get(groupPosition).getCslInfos().get(childPosition).getState() == CommonCode.SEEK_STATE_NOW) {
            //接诊中
            if (childData.get(groupPosition).getCslInfos().get(childPosition).responseNubeNumber.equals(AccountManager.getInstance(mContext).getNube())) {
                childViewHolder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_blue));
                CustomLog.d(TAG, "本人接诊中，置蓝");
            } else {
                childViewHolder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
                CustomLog.d(TAG, "别人接诊中，置灰");
            }
            childViewHolder.responseDoctorName.setVisibility(View.VISIBLE);
            childViewHolder.stateText.setVisibility(View.VISIBLE);
            childViewHolder.responseDtBtn.setVisibility(View.GONE);
            childViewHolder.stateText.setText(mContext.getString(R.string.responsing_dt));

        } else if (childData.get(groupPosition).getCslInfos().get(childPosition).getState() == CommonCode.SEEK_STATE_NOT) {
            //待接诊
            int systemTime = Integer.valueOf(getDateFromSeconds(String.valueOf(childData.get(groupPosition).getCslInfos().get(childPosition).getCurSystemTime())));
            int schedulDate = Integer.valueOf(childData.get(groupPosition).getCslInfos().get(childPosition).getSchedulDate());
            if (childData.get(groupPosition).getCslInfos().get(childPosition).getRequestNubeNumber().equals(AccountManager.getInstance(mContext).getNube())) {
                childViewHolder.responseDoctorName.setVisibility(View.VISIBLE);
                childViewHolder.stateText.setVisibility(View.VISIBLE);
                childViewHolder.responseDtBtn.setVisibility(View.GONE);
            } else {
                if (schedulDate <= systemTime) {
                    childViewHolder.responseDoctorName.setVisibility(View.GONE);
                    childViewHolder.stateText.setVisibility(View.GONE);
                    childViewHolder.responseDtBtn.setVisibility(View.VISIBLE);
                } else {
                    childViewHolder.responseDoctorName.setVisibility(View.VISIBLE);
                    childViewHolder.stateText.setVisibility(View.VISIBLE);
                    childViewHolder.responseDtBtn.setVisibility(View.GONE);
                }
            }
            childViewHolder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_yellow));
            CustomLog.d(TAG, "待接诊，置黄");
            childViewHolder.stateText.setText(mContext.getString(R.string.wait_response_dt));

        } else if (childData.get(groupPosition).getCslInfos().get(childPosition).getState() == CommonCode.SEEK_STATE_END) {
            //结束
            childViewHolder.reservationNumberImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.response_dt_item_round_gray));
            childViewHolder.responseDoctorName.setVisibility(View.VISIBLE);
            childViewHolder.stateText.setVisibility(View.VISIBLE);
            childViewHolder.responseDtBtn.setVisibility(View.GONE);
            CustomLog.d(TAG, "已结束，置灰");
            childViewHolder.stateText.setText(mContext.getString(R.string.ended));
        }
        CustomLog.d(TAG, "预约号" + childData.get(groupPosition).getCslInfos().get(childPosition).getCurNum());
        childViewHolder.reservationNumber.setText(childData.get(groupPosition).getCslInfos().get(childPosition).getCurNum() + "号");
        CustomLog.d(TAG, "患者姓名" + childData.get(groupPosition).getCslInfos().get(childPosition).getPatientName());
        childViewHolder.patientNameText.setText(childData.get(groupPosition).getCslInfos().get(childPosition).getPatientName());
        CustomLog.d(TAG, "主诉" + childData.get(groupPosition).getCslInfos().get(childPosition).getChief());
        childViewHolder.illText.setText(childData.get(groupPosition).getCslInfos().get(childPosition).getChief());
        CustomLog.d(TAG, "请求者医院" + childData.get(groupPosition).getCslInfos().get(childPosition).getRequestHosp());
        childViewHolder.requestDoctorHospital.setText(childData.get(groupPosition).getCslInfos().get(childPosition).getRequestHosp());
        CustomLog.d(TAG, "请求者科室" + childData.get(groupPosition).getCslInfos().get(childPosition).getRequestDep());
        childViewHolder.requestDoctorDepartment.setText(childData.get(groupPosition).getCslInfos().get(childPosition).getRequestDep());
        CustomLog.d(TAG, "请求者姓名" + childData.get(groupPosition).getCslInfos().get(childPosition).getRequestName());
        childViewHolder.requestDoctorName.setText(childData.get(groupPosition).getCslInfos().get(childPosition).getRequestName());
        CustomLog.d(TAG, "接诊人姓名" + childData.get(groupPosition).getCslInfos().get(childPosition).getResponseName());
        childViewHolder.responseDoctorName.setText(childData.get(groupPosition).getCslInfos().get(childPosition).getResponseName());
        final String tip = mContext.getString(R.string.you_will_response_dt) + "\n" + childData.get(groupPosition).getCslInfos().get(childPosition).getRequestHosp() + childData.get(groupPosition).getCslInfos().get(childPosition).getRequestDep() + mContext.getString(R.string.patient) + childData.get(groupPosition).getCslInfos().get(childPosition).getPatientName() + mContext.getString(R.string.udt_whether_response).trim();
        childViewHolder.responseDtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dtId = childData.get(groupPosition).getCslInfos().get(childPosition).getId();
                final CustomDialog dialog = new CustomDialog(mContext);
                dialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                    @Override
                    public void onClick(CustomDialog customDialog) {
                        CustomLog.d(TAG, "setOkBtnOnClickListener 诊疗号：" + childData.get(groupPosition).getCslInfos().get(childPosition).getId());
                        final HPUAcceptCsl hpuAcceptCsl = new HPUAcceptCsl() {
                            @Override
                            protected void onSuccess(JSONObject responseContent) {
                                super.onSuccess(responseContent);
                                removeLoadingView();
                                CustomLog.d(TAG, "HPUAcceptCsl||onSuccess");
                                MedicalApplication.getFileTaskManager().sendChangeDtStateMsg(0,
                                        AccountManager.getInstance(mContext).getNube(),
                                        childData.get(groupPosition).getCslInfos().get(childPosition).getRequestNubeNumber(),
                                        dtId,
                                        AccountManager.getInstance(mContext).getAccountInfo().headThumUrl,
                                        AccountManager.getInstance(mContext).getName());
                                Intent intent = new Intent(mContext, UDTChatRoomActivity.class);
                                intent.putExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG, dtId);
                                mContext.startActivity(intent);

                            }

                            @Override
                            protected void onFail(int statusCode, String statusInfo) {
                                super.onFail(statusCode, statusInfo);
                                removeLoadingView();
                                CustomLog.e(TAG, "HPUAcceptCsl||onFail");
                                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                                    AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                                } else if (statusCode == MDSErrorCode.MDS_GROUP_NOTICE_EMPTY) {
                                    final CustomDialog dtCancelDialog = new CustomDialog(mContext);
                                    dtCancelDialog.removeCancelBtn();
                                    dtCancelDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                                        @Override
                                        public void onClick(CustomDialog customDialog) {
                                            CustomLog.d(TAG, "dtCancelDialog setOkBtnOnClickListener");
                                            dtCancelDialog.dismiss();
                                            mrefreshListen.refresh();
                                        }
                                    });
                                    dtCancelDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                                    dtCancelDialog.setTip(mContext.getString(R.string.dt_have_cancle));
                                    dtCancelDialog.show();
                                } else if (statusCode == MDSErrorCode.MDS_OFFACCID_IS_FALSE) {
                                    final CustomDialog canNotResponseDialog = new CustomDialog(mContext);
                                    canNotResponseDialog.removeCancelBtn();
                                    canNotResponseDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                                        @Override
                                        public void onClick(CustomDialog customDialog) {
                                            CustomLog.d(TAG, "canNotResponseDialog setOkBtnOnClickListener");
                                            canNotResponseDialog.dismiss();
                                        }
                                    });
                                    canNotResponseDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                                    canNotResponseDialog.setTip(mContext.getString(R.string.not_be_scheduled_not_response_dt));
                                    canNotResponseDialog.show();
                                } else if (statusCode == MDSErrorCode.MDS_REPEAT_FOUCS_OFFACC) {
                                    final CustomDialog canNotRepetitionResponseDialog = new CustomDialog(mContext);
                                    canNotRepetitionResponseDialog.removeCancelBtn();
                                    canNotRepetitionResponseDialog.setOkBtnOnClickListener(new CustomDialog.OKBtnOnClickListener() {
                                        @Override
                                        public void onClick(CustomDialog customDialog) {
                                            CustomLog.d(TAG, "canNotResponseDialog setOkBtnOnClickListener");
                                            canNotRepetitionResponseDialog.dismiss();
                                            mrefreshListen.refresh();
                                        }
                                    });
                                    canNotRepetitionResponseDialog.setCenterBtnText(mContext.getString(R.string.iknow));
                                    canNotRepetitionResponseDialog.setTip(mContext.getString(R.string.not_repetition_response_dt));
                                    canNotRepetitionResponseDialog.show();
                                } else {
                                    CustomToast.show(mContext, statusInfo, CustomToast.LENGTH_LONG);
                                }
                            }
                        };

                        showLoadingView(mContext.getString(R.string.wait), new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                removeLoadingView();
                                CustomLog.d(TAG, "取消接受诊疗");
                                hpuAcceptCsl.cancel();
                            }
                        }, true);
                        dialog.dismiss();
                        hpuAcceptCsl.accept(AccountManager.getInstance(mContext).getMdsToken(), childData.get(groupPosition).getCslInfos().get(childPosition).getId());
                    }
                });
                dialog.setCancelBtnOnClickListener(new CustomDialog.CancelBtnOnClickListener() {
                    @Override
                    public void onClick(CustomDialog customDialog) {
                        // TODO Auto-generated method stub
                        CustomLog.d(TAG, "确认诊疗dialog   setCancelBtnOnClickListener");
                        dialog.dismiss();
                    }
                });
                dialog.setTip(tip);
                dialog.setOkBtnText(mContext.getString(R.string.confirm_dt));
                dialog.setCancelBtnText(mContext.getString(R.string.cancel));
                dialog.show();

            }
        });
        childViewHolder.responseLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, UDTChatRoomActivity.class);
                intent.putExtra(UDTDataConstant.UDT_ROOM_DT_ID_FLAG, childData.get(groupPosition).getCslInfos().get(childPosition).getId());
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        CustomLog.d(TAG, "onGroupExpanded() called with: groupPosition = [" + groupPosition + "]");
        if (mOnGroupExpandedListener != null) {
            mOnGroupExpandedListener.onGroupExpanded(groupPosition);
        }
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        CustomLog.d(TAG, "onGroupCollapsed() called with: groupPosition = [" + groupPosition + "]");
    }

    /**
     * GroupView包含的控件
     */
    private static class GroupViewHolder {
        TextView timeText;
        TextView hpuNameText;
        ImageView ivIndicator;
    }

    /**
     * ChildView包含的控件
     */
    private static class ChildViewHolder {
        RelativeLayout responseLay;
        ImageView reservationNumberImage;
        TextView reservationNumber;
        TextView patientNameText;
        TextView illText;
        TextView requestDoctorHospital;
        TextView requestDoctorDepartment;
        TextView requestDoctorName;
        TextView responseDoctorName;
        TextView stateText;
        Button responseDtBtn;
    }

    private void showLoadingView(String message,
                                 final DialogInterface.OnCancelListener listener, boolean cancelAble) {
        CustomLog.i(TAG, "::showLoadingDialog() msg: " + message);
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception ex) {
            CustomLog.d(TAG, "::showLoadingView()" + ex.toString());
        }
        dialog = cn.redcdn.hvs.util.CommonUtil.createLoadingDialog(mContext, message,
                listener);
        dialog.setCancelable(cancelAble);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onCancel(dialog);
                }
                return false;
            }
        });
        try {
            dialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "::showLoadingView()" + ex.toString());
        }
    }

    protected void removeLoadingView() {

        CustomLog.i(TAG, "::removeLoadingView()");

        if (dialog != null) {

            dialog.dismiss();

            dialog = null;

        }

    }

    /**
     * 秒数转化为日期
     */
    public static String getDateFromSeconds(String seconds) {
        if (seconds == null)
            return " ";
        else {
            Date date = new Date();
            try {
                date.setTime(Long.parseLong(seconds) * 1000);
            } catch (NumberFormatException nfe) {

            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(date);
        }
    }

    public void setOnRefreshListen(onRefreshListen onRefreshListen) {
        mrefreshListen = onRefreshListen;
    }


    public interface onRefreshListen {
        void refresh();
    }
}
