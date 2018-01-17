package cn.redcdn.hvs.meeting.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.meeting.bean.Genre;
import cn.redcdn.hvs.meeting.bean.JointCoonsultatioonRoomBean;
import cn.redcdn.hvs.responsedt.adapter.onGroupExpandedListener;

/**
 * Created by Administrator on 2017/11/27.
 */

public class MedicalExpandableListViewAdapter extends BaseExpandableListAdapter {
    public static final int ADMISSIONS_VIEW_TYPE = 1;
    public static final int WAITING_ADMISSIONS_VIEW_TYPE = 2;
    public static final int END_VIEW_TYPE = 3;
    // 班级的集合
    private List<Genre> medicals;
    private SparseArray<ImageView> mIndicators;

    // 创建布局使用
    private Context mContext;
    private onGroupExpandedListener mOnGroupExpandedListener;


    public MedicalExpandableListViewAdapter(Context mContext) {
        this.mContext = mContext;
        mIndicators = new SparseArray<>();
    }

    public void setIndicatorState(int groupPosition, boolean isExpanded) {
        if (isExpanded) {
            mIndicators.get(groupPosition).setImageResource(R.drawable.arrow_blue_up);
        } else {
            mIndicators.get(groupPosition).setImageResource(R.drawable.arrow_blue_down);
        }
    }


    public void setData(List<Genre> data) {
        this.medicals = data;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return medicals.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return medicals.get(groupPosition).jointCoonsultatioonRoomBeanList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return medicals.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return medicals.get(groupPosition).jointCoonsultatioonRoomBeanList.get(childPosition);
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupViewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_genre, parent, false);
            groupViewHolder = new GroupHolder();
            groupViewHolder.genreTitle = (TextView) convertView.findViewById(R.id.list_item_genre_name);
            groupViewHolder.mIndicator = (ImageView) convertView.findViewById(R.id.iv_indicator);
            convertView.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupHolder) convertView.getTag();
        }
        groupViewHolder.genreTitle.setText(medicals.get(groupPosition).getTitile());
        //      把位置和图标添加到Map
        mIndicators.put(groupPosition, groupViewHolder.mIndicator);
        //      根据分组状态设置Indicator
        setIndicatorState(groupPosition, isExpanded);
        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        int type = medicals.get(groupPosition).getJointCoonsultatioonRoomBeanList().get(childPosition).getType();
        JointCoonsultatioonRoomBean bean = medicals.get(groupPosition).getJointCoonsultatioonRoomBeanList().get(childPosition);
        switch (type) {
            case ADMISSIONS_VIEW_TYPE:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_joint, parent, false);
                TextView childTextViewAdmission = (TextView) convertView.findViewById(R.id.list_item_doctor_name1);
                TextView newMessage = (TextView) convertView.findViewById(R.id.total_new_notice_ing_num);
                TextView dateTvAdmission = (TextView) convertView.findViewById(R.id.ing_date);
                TextView hospitalAdmission = (TextView) convertView.findViewById(R.id.ing_hospital);
                TextView departmentAdmission = (TextView) convertView.findViewById(R.id.ing_department);
                TextView nameAdmission = (TextView) convertView.findViewById(R.id.ing_name);
                TextView timeAdmission = (TextView) convertView.findViewById(R.id.ing_time);
                TextView to = (TextView) convertView.findViewById(R.id.to);
                if (bean.getZhenliaoState()==2){
                    to.setText(MedicalApplication.getContext().getString(R.string.from));
                }
                if (!"".equals(bean.getDate()) && bean.getDate().length() >= 8) {
                    String substring = bean.getDate().substring(4, 6);
                    String substring1 = bean.getDate().substring(bean.getDate().length() - 2, bean.getDate().length());
                    dateTvAdmission.setText(substring + MedicalApplication.getContext().getString(R.string.month) + substring1 + MedicalApplication.getContext().getString(R.string.day));
                }
                if (0==bean.getUnreadNotice()){
                    newMessage.setVisibility(View.INVISIBLE);
                }else {
                    newMessage.setText(bean.getUnreadNotice()+"");
                    newMessage.setVisibility(View.VISIBLE);
                }
                if (!"".equals(bean.getName())) {
                    childTextViewAdmission.setText(bean.getName());
                }
                if (!"".equals(bean.getHospital())) {
                    hospitalAdmission.setText(bean.getHospital());
                }
                if (!"".equals(bean.getDepartment())) {
                    departmentAdmission.setText(bean.getDepartment());
                }
                if (!"".equals(bean.getDoctorName())) {
                    nameAdmission.setText(bean.getDoctorName());
                }
                String range = bean.getRange();
                if (!"".equals(range)) {
                    timeAdmission.setText(range);
                }
                return convertView;
            case WAITING_ADMISSIONS_VIEW_TYPE:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_waiting, parent, false);
                TextView newMessage1 = (TextView) convertView.findViewById(R.id.total_new_notice_wait_num);
                TextView childTextViewWait = (TextView) convertView.findViewById(R.id.list_item_doctor_name);
                TextView dateTvWait = (TextView) convertView.findViewById(R.id.wait_date);
                TextView waitHospital = (TextView) convertView.findViewById(R.id.wait_hospital);
                TextView waitDepartment = (TextView) convertView.findViewById(R.id.wait_department);
                TextView waitName = (TextView) convertView.findViewById(R.id.wait_name);
                TextView waitTime = (TextView) convertView.findViewById(R.id.wait_time);
                TextView to1 = (TextView) convertView.findViewById(R.id.to);
                if (bean.getZhenliaoState()==2){
                    to1.setText(MedicalApplication.getContext().getString(R.string.from));
                }
                if (!"".equals(bean.getDate()) && bean.getDate().length() >= 8) {
                    String substring = bean.getDate().substring(4, 6);
                    String substring1 = bean.getDate().substring(bean.getDate().length() - 2, bean.getDate().length());
                    dateTvWait.setText(substring + MedicalApplication.getContext().getString(R.string.month) + substring1 + MedicalApplication.getContext().getString(R.string.day));
                }
                if (0==bean.getUnreadNotice()){
                    newMessage1.setVisibility(View.INVISIBLE);
                }else {
                    newMessage1.setText(bean.getUnreadNotice()+"");
                    newMessage1.setVisibility(View.VISIBLE);
                }
                if (!"".equals(bean.getName())) {
                    childTextViewWait.setText(bean.getName());
                }
                if (!"".equals(bean.getHospital())) {
                    waitHospital.setText(bean.getHospital());
                }
                if (!"".equals(bean.getDepartment())) {
                    waitDepartment.setText(bean.getDepartment());
                }
                if (!"".equals(bean.getDoctorName())) {
                    waitName.setText(bean.getDoctorName());
                }
                if (!"".equals(bean.getRange())) {
                    waitTime.setText(bean.getRange());
                }
                return convertView;
            case END_VIEW_TYPE:
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_end, parent, false);
                TextView childTextViewEnd = (TextView) convertView.findViewById(R.id.list_item_doctor_name);
                TextView newMessage2 = (TextView) convertView.findViewById(R.id.total_new_notice_wait_num);
                TextView dateTvEnd = (TextView) convertView.findViewById(R.id.end_date);
                TextView endHospital = (TextView) convertView.findViewById(R.id.end_hospital);
                TextView endDepartment = (TextView) convertView.findViewById(R.id.end_department);
                TextView endName = (TextView) convertView.findViewById(R.id.end_name);
                TextView endTime = (TextView) convertView.findViewById(R.id.end_time);
                TextView to2 = (TextView) convertView.findViewById(R.id.to);
                if (bean.getZhenliaoState()==2){
                    to2.setText(MedicalApplication.getContext().getString(R.string.from));
                }
                if (!"".equals(bean.getDate()) && bean.getDate().length() >= 8) {
                    String substring = bean.getDate().substring(4, 6);
                    String substring1 = bean.getDate().substring(bean.getDate().length() - 2, bean.getDate().length());
                    dateTvEnd.setText(substring + MedicalApplication.getContext().getString(R.string.month) + substring1 + MedicalApplication.getContext().getString(R.string.day));
                }
                if (0==bean.getUnreadNotice()){
                    newMessage2.setVisibility(View.INVISIBLE);
                }else {
                    newMessage2.setText(bean.getUnreadNotice()+"");
                    newMessage2.setVisibility(View.VISIBLE);
                }
                if (!"".equals(bean.getName())) {
                    childTextViewEnd.setText(bean.getName());
                }
                if (!"".equals(bean.getHospital())) {
                    endHospital.setText(bean.getHospital());
                }
                if (!"".equals(bean.getDepartment())) {
                    endDepartment.setText(bean.getDepartment());
                }
                if (!"".equals(bean.getDoctorName())) {
                    endName.setText(bean.getDoctorName());
                }
                if (!"".equals(bean.getRange())) {
                    endTime.setText(bean.getRange());
                }
                return convertView;
            default:
                throw new IllegalArgumentException("Invalid viewType");
        }
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private class GroupHolder {
        private TextView genreTitle;
        private ImageView mIndicator;
    }

    private class ChildHolder1 {
        private TextView childTextView;
        private TextView dateTv;
        private TextView hospital;
        private TextView department;
        private TextView name;
        private TextView time;
    }

    private class ChildHolder2 {
        private TextView childTextView;
        private TextView dateTv;
        private TextView waitHospital;
        private TextView waitDepartment;
        private TextView waitName;
        private TextView waitTime;
    }

    private class ChildHolder3 {
        private TextView childTextView;
        private TextView dateTv;
        private TextView endHospital;
        private TextView endDepartment;
        private TextView endName;
        private TextView endTime;
    }
}
