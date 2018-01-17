package cn.redcdn.hvs.head.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.head.javabean.OfficialAccountsBean;

/**
 * Created by Administrator on 2017/7/27.
 */

public class GalleryAdapter extends
        RecyclerView.Adapter<GalleryAdapter.ViewHolder> implements View.OnClickListener {
    Context context;
    private LayoutInflater mInflater;
    private List<OfficialAccountsBean> mDatas;
    private OnRecyclerViewItemClickListener mOnItemClickListener;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, OfficialAccountsBean data);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public GalleryAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<OfficialAccountsBean> datats) {
        this.mDatas = datats;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        mOnItemClickListener.onItemClick(v, (OfficialAccountsBean) v.getTag());
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }

        RelativeLayout relativeLayout;
        ImageView mImg;
        TextView mTxt;
    }

    @Override
    public int getItemCount() {
        return mDatas!=null?mDatas.size():0;
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.activity_recycler_item,
                viewGroup, false);
        RelativeLayout officialAcountsRl = (RelativeLayout) view.findViewById(R.id.official_accounts_relative_rl);
        officialAcountsRl.setOnClickListener(this);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.mImg = (ImageView) view
                .findViewById(R.id.id_index_gallery_item_image);
        viewHolder.mTxt = (TextView) view
                .findViewById(R.id.id_index_gallery_item_text);
        return viewHolder;
    }


    /**
     * 设置值
     */
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        viewHolder.itemView.findViewById(R.id.official_accounts_relative_rl).setTag(mDatas.get(i));
        if (mDatas.get(i).getInformation().equals(MedicalApplication.getContext().getString(R.string.all))) {
            Glide.with(context).load(R.drawable.all_official).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.doctor_default).into(viewHolder.mImg);
        }else if (mDatas.get(i).getInformation().equals(MedicalApplication.getContext().getString(R.string.suscribe))){
            Glide.with(context).load(R.drawable.dingyue_official).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.doctor_default).into(viewHolder.mImg);
        } else {
            Glide.with(context).load(mDatas.get(i).getPicUrl()).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.doctor_default).into(viewHolder.mImg);
        }
        viewHolder.mTxt.setText(mDatas.get(i).getInformation());
    }

}
