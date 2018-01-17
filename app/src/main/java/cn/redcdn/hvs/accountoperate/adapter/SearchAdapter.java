package cn.redcdn.hvs.accountoperate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.redcdn.datacenter.medicalcenter.data.MDSHospitalInfo;
import cn.redcdn.hvs.R;

/**
 * Created by Administrator on 2017/3/16.
 */

public class SearchAdapter extends BaseAdapter {
    private List<MDSHospitalInfo> arrayList;
    private Context context;

    public SearchAdapter(Context context, List<MDSHospitalInfo> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public void setArrayList(List<MDSHospitalInfo> arrayList) {
        this.arrayList = arrayList;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        DepartmentSelectAdapter.ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.hospital_item, null);
            holder = new DepartmentSelectAdapter.ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.hospital_text);
            view.setTag(holder);
        } else {
            holder = (DepartmentSelectAdapter.ViewHolder) view.getTag();
        }
        MDSHospitalInfo department = arrayList.get(i);
        holder.textView.setText(department.getHospitalName());
        return view;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
