package cn.redcdn.hvs.accountoperate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.info.Province;


/**
 * Created by dell on 2017/2/24.
 */

public class HospitalSelectAdapter extends BaseAdapter {
    private ArrayList<Province> arrayList;
    private Context context;

    public HospitalSelectAdapter(ArrayList<Province> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    public void setArrayList(ArrayList<Province> arrayList) {
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
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.hospital_item, null);
            holder = new ViewHolder();
            holder.textView = (TextView)view.findViewById(R.id.hospital_text);
            view.setTag(holder);
        } else {
            holder = (ViewHolder)view.getTag();
        }
        Province pro = arrayList.get(i);
        holder.textView.setText(pro.getHospitalprovince());
        return view;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
