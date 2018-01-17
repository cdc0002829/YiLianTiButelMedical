package cn.redcdn.hvs.accountoperate.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.accountoperate.info.Position;

/**
 * Created by dell on 2017/2/27.
 */

public class PositionSelectAdapter extends BaseAdapter {
    private ArrayList<Position> arrayList;
    private Context context;

    public PositionSelectAdapter(ArrayList<Position> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
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
        Position position = arrayList.get(i);
        holder.textView.setText(position.getChoose_Position());
        return view;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
