package cn.redcdn.hvs.head.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import cn.redcdn.hvs.R;
import cn.redcdn.hvs.head.javabean.PicBean;

/**
 * Created by Administrator on 2017/7/26.
 */

public class HeadAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    public static final int BIG_IMAGE_TYPE = 1;
    public static final int TITLE_TYPE = 3;
    public static final int LITTLE_IMAGE_TYPE = 2;
    private Context context;
    private List<PicBean> mCollectionBeanList;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public HeadAdapter(Context activity) {
        this.context = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == BIG_IMAGE_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_big_image, null);
            itemView.findViewById(R.id.relative_rl).setOnClickListener(this);
            return new BigImageViewHolder(itemView);
        } else if (viewType == TITLE_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_title, null);
            itemView.findViewById(R.id.relative_rl).setOnClickListener(this);
            return new TitleViewHolder(itemView);
        } else if (viewType == LITTLE_IMAGE_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_little_image, null);
            itemView.findViewById(R.id.relative_rl).setOnClickListener(this);
            return new LittleImageViewHolder(itemView);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PicBean productBean = mCollectionBeanList.get(position);
        holder.itemView.findViewById(R.id.relative_rl).setTag(mCollectionBeanList.get(position));
        if (holder instanceof BigImageViewHolder) {
            ( (BigImageViewHolder) holder).bind(productBean);
        } else if (holder instanceof LittleImageViewHolder) {
            ((LittleImageViewHolder) holder).bind(productBean);
        } else if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).bind(productBean);
        }
    }

    @Override
    public int getItemCount() {
        return (mCollectionBeanList == null) ? 0 : mCollectionBeanList.size();
    }

    public void setData(List<PicBean> mCollection) {
        this.mCollectionBeanList = mCollection;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mCollectionBeanList.get(position).getType();
    }

    @Override
    public void onClick(View v) {
        mOnItemClickListener.onItemClick(v, (PicBean) v.getTag());
    }

    private class BigImageViewHolder extends ProductViewHolder {
        ImageView bigImage;
        TextView bigText;


        public BigImageViewHolder(View itemView) {
            super(itemView);
            bigImage = (ImageView) itemView.findViewById(R.id.big_image);
            bigText = (TextView) itemView.findViewById(R.id.big_text);
        }

        @Override
        public void bind(PicBean productInfo) {
            Glide.with(context).load(productInfo.getPicUrl()).placeholder(R.drawable.hillbackground).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.hillbackground).into(bigImage);
            bigText.setText(productInfo.getInformation());
        }
    }

    public static abstract class ProductViewHolder extends RecyclerView.ViewHolder {

        public ProductViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(PicBean productInfo);

    }

    private class TitleViewHolder extends ProductViewHolder {


        TextView titleTv;

        public TitleViewHolder(View itemView) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.title_tv);
        }

        @Override
        public void bind(PicBean productInfo) {
            titleTv.setText(productInfo.getTitle());
        }
    }

    private class LittleImageViewHolder extends ProductViewHolder {
        ImageView littleIamge;
        TextView littleText;

        public LittleImageViewHolder(View itemView) {
            super(itemView);
            littleIamge = (ImageView) itemView.findViewById(R.id.little_image);
            littleText = (TextView) itemView.findViewById(R.id.little_text);
        }

        @Override
        public void bind(PicBean productInfo) {
            Glide.with(context).load(productInfo.getPicUrl()).placeholder(R.drawable.hillbackground).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).error(R.drawable.hillbackground).centerCrop().into(littleIamge);
            littleText.setText(productInfo.getInformation());
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if(manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    switch (type){
                        case BIG_IMAGE_TYPE:
                            return 2;

                        case TITLE_TYPE:
                            return 2;

                        case LITTLE_IMAGE_TYPE:
                            return 1;

                        default:
                            return 2;
                    }
                }
            });
        }
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, PicBean data);
    }

}
