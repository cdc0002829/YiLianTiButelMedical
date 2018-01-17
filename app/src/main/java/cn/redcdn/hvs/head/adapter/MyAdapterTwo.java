package cn.redcdn.hvs.head.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.data.MainPageInfo;
import cn.redcdn.hvs.R;

/**
 * Created by Administrator on 2017/11/21.
 */

public class MyAdapterTwo extends RecyclerView.Adapter implements View.OnClickListener{
    private List<MainPageInfo> mData;
    private List<Boolean> isClicks;

    public MyAdapterTwo() {
    }

    public void setData(List<MainPageInfo> data) {
        mData = data;
        isClicks = new ArrayList<>();
        for(int i = 0;i<mData.size();i++){
            isClicks.add(false);
        }
        if (mData.size()>=2){
            isClicks.set(1,true);
        }
        notifyDataSetChanged();
    }

    public void setData(List<MainPageInfo> data,int index) {
        mData = data;
        isClicks = new ArrayList<>();
        for(int i = 0;i<mData.size();i++){
            isClicks.add(false);
        }
        if (mData.size()>=2){
            isClicks.set(index,true);
        }
        notifyDataSetChanged();
    }

    private OnRecyclerViewItemClickListeer mOnItemClickListener = null;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item,null);
        view.findViewById(R.id.ll).setOnClickListener(this);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        holder.itemView.setTag(mData.get(position));
        viewHolder.mTextView.setText(mData.get(position).getDtName());
        if(isClicks.get(position)){
            viewHolder.mTextView.setTextColor(Color.parseColor("#3AABCB"));
        }else {
            viewHolder.mTextView.setTextColor(Color.parseColor("#ffffff"));
        }
        if (position==mData.size()-1){
            viewHolder.line_view.setVisibility(View.GONE);
        }else {
            viewHolder.line_view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0:mData.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextView;
        public View line_view;
        public ViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.text_content);
            line_view = (View) itemView.findViewById(R.id.line_view);
        }
    }

    @Override
    public void onClick(View view) {
        for(int i = 0; i <isClicks.size();i++){
            isClicks.set(i,false);
        }
            isClicks.set(mData.indexOf((MainPageInfo)view.getTag()),true);
        notifyDataSetChanged();
        mOnItemClickListener.onItemClick(view, (MainPageInfo) view.getTag());
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListeer listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemClickListeer {
        void onItemClick(View view, MainPageInfo data);
    }
}
