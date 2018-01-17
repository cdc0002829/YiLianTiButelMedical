package cn.redcdn.hvs.profiles.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.redcdn.datacenter.collectcenter.DataBodyInfo;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.im.view.RoundImageView;
import cn.redcdn.hvs.profiles.activity.XCRoundImageView;
import cn.redcdn.hvs.profiles.listener.MyDisplayImageListener;


/**
 * Created by Administrator on 2017/5/2.
 */

public class CollectionChatListAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener {
    public static final int IMAGE_TYPE = 2;
    public static final int VEDIO_TYPE = 3;
    public static final int AUDIO_TYPE = 7;
    public static final int WORD_TYPE = 8;
    public static final int ARTICAL_TYPE = 30;
    public static final int CARD_TYPE = 4;
    public List<DataBodyInfo> mCollectionBeanList;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnLongViewItemClickListener mOnLongClickListener = null;
    MyDisplayImageListener mDisplayImageListener = null;
    private boolean date;

    public interface OnLongViewItemClickListener {
        void longClick(View view, DataBodyInfo data);
    }

    public void setLongItemClickListener(OnLongViewItemClickListener listener) {
        this.mOnLongClickListener = listener;
    }

    public void setDate(boolean date) {
        this.date = date;
    }

    public void setData(List<DataBodyInfo> mCollection) {
        this.mCollectionBeanList = mCollection;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mDisplayImageListener = new MyDisplayImageListener();
        if (viewType == WORD_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_chat_word, null);
            return new WordViewHolder(itemView);
        } else if (viewType == AUDIO_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_chat_audio, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            return new AudioViewHolder(itemView);
        } else if (viewType == VEDIO_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_chat_video, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            return new VideoViewHolder(itemView);
        } else if (viewType == IMAGE_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_chat_image, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            itemView.findViewById(R.id.item_cardview).setOnLongClickListener(this);
            return new ImageViewHolder(itemView);
        } else if (viewType == ARTICAL_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_chat_artical, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            return new ArticleViewHolder(itemView);
        } else if (viewType == CARD_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_chat_card, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            return new CardViewHolder(itemView);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DataBodyInfo productBean = mCollectionBeanList.get(position);
        holder.itemView.findViewById(R.id.item_cardview).setTag(mCollectionBeanList.get(position));
        if (holder instanceof WordViewHolder) {
            ((WordViewHolder) holder).bind(productBean);
        } else if (holder instanceof AudioViewHolder) {
            ((AudioViewHolder) holder).bind(productBean);
        } else if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(productBean);
        } else if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).bind(productBean);
        } else if (holder instanceof ArticleViewHolder) {
            ((ArticleViewHolder) holder).bind(productBean);
        } else if (holder instanceof CardViewHolder) {
            ((CardViewHolder) holder).bind(productBean);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mCollectionBeanList.get(position).type;
    }

    @Override
    public int getItemCount() {
        return (mCollectionBeanList == null) ? 0 : mCollectionBeanList.size();
    }

    @Override
    public boolean onLongClick(View view) {
        mOnLongClickListener.longClick(view, (DataBodyInfo) view.getTag());
        return true;
    }

    public static abstract class AllViewHolder extends RecyclerView.ViewHolder {

        public AllViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(DataBodyInfo productInfo);

    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, DataBodyInfo data);
    }

    @Override
    public void onClick(View view) {
        mOnItemClickListener.onItemClick(view, (DataBodyInfo) view.getTag());
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    private class WordViewHolder extends AllViewHolder {

        RoundImageView headIv;
        TextView nameTv;
        TextView timeTv;
        TextView contentTv;

        public WordViewHolder(View itemView) {
            super(itemView);
            headIv = (RoundImageView) itemView.findViewById(R.id.head_iv);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            contentTv = (TextView) itemView.findViewById(R.id.content_tv);
        }

        public void bind(DataBodyInfo mInfo) {
            nameTv.setText(mInfo.getForwarderName());
            contentTv.setText(mInfo.getTxt());
            String collecTime = mInfo.getMessageTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.data1));
            if (date) {
                timeTv.setText(format.format(d).substring(format.format(d).length()-6));
            } else {
                timeTv.setText(format.format(d));
            }

            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headIv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }
    }

    private class CardViewHolder extends AllViewHolder {

        RoundImageView headIv;
        TextView nameTv;
        TextView timeTv;
        TextView collectionCardName;
        TextView collectionCardId;
        ImageView imageChatIcon;


        public CardViewHolder(View itemView) {
            super(itemView);
            headIv = (RoundImageView) itemView.findViewById(R.id.head_iv);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            collectionCardName = (TextView) itemView.findViewById(R.id.collection_card_name);
            collectionCardId = (TextView) itemView.findViewById(R.id.collection_card_id);
            imageChatIcon = (ImageView) itemView.findViewById(R.id.image_chat_icon);
        }

        public void bind(DataBodyInfo mInfo) {
            nameTv.setText(mInfo.getForwarderName());
            String collecTime = mInfo.getMessageTime() + "000";
            collectionCardName.setText(mInfo.getCardname());
            collectionCardId.setText(mInfo.getCardCode());
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.data1));
            if (date) {
                timeTv.setText(format.format(d).substring(format.format(d).length()-6));
            } else {
                timeTv.setText(format.format(d));
            }
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                    .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                    .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                    .cacheInMemory(true)//是否緩存都內存中
                    .cacheOnDisc(true)//是否緩存到sd卡上
//                    .displayer(new RoundedBitmapDisplayer(20))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                    .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                    .build();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headIv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
            imageLoader.displayImage(mInfo.getCardUrl(),
                    imageChatIcon,
                    options,
                    mDisplayImageListener);
        }
    }

    private class AudioViewHolder extends AllViewHolder {

        RoundImageView headIv;
        TextView nameTv;
        TextView timeTv;
        TextView audioLength;

        public AudioViewHolder(View itemView) {
            super(itemView);
            headIv = (RoundImageView) itemView.findViewById(R.id.head_iv);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            audioLength = (TextView) itemView.findViewById(R.id.collection_audio_length);
        }

        public void bind(DataBodyInfo mInfo) {
            nameTv.setText(mInfo.getForwarderName());
            String collecTime = mInfo.getMessageTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.data1));
            if (date) {
                timeTv.setText(format.format(d).substring(format.format(d).length()-6));
            } else {
                timeTv.setText(format.format(d));
            }
            int duration = mInfo.getDuration();
            audioLength.setText(duration +MedicalApplication.getContext().getString(R.string.second));
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headIv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }
    }

    private class VideoViewHolder extends AllViewHolder {
        RoundImageView headIv;
        TextView nameTv;
        TextView timeTv;
        TextView videoLength;
        ImageView videoThumb;

        public VideoViewHolder(View itemView) {
            super(itemView);
            headIv = (RoundImageView) itemView.findViewById(R.id.head_iv);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            videoLength = (TextView) itemView.findViewById(R.id.collection_video_length);
            videoThumb = (ImageView) itemView.findViewById(R.id.image_chat_icon);
        }


        public void bind(DataBodyInfo mInfo) {
            String collecTime = mInfo.getMessageTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.data1));
            if (date) {
                timeTv.setText(format.format(d).substring(format.format(d).length()-6));
            } else {
                timeTv.setText(format.format(d));
            }
            nameTv.setText(mInfo.getForwarderName());
            videoLength.setText(mInfo.getDuration() + MedicalApplication.getContext().getString(R.string.second));
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                    .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                    .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                    .cacheInMemory(true)//是否緩存都內存中
                    .cacheOnDisc(true)//是否緩存到sd卡上
//                    .displayer(new RoundedBitmapDisplayer(20))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                    .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                    .build();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headIv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
            imageLoader.displayImage(mInfo.getThumbnailRemoteUrl(),
                    videoThumb,
                   options,
                    mDisplayImageListener);
        }
    }

    private class ImageViewHolder extends AllViewHolder {

        TextView nameTv;
        RoundImageView headIv;
        TextView timeTv;
        ImageView chatImageIv;

        public ImageViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            headIv = (RoundImageView) itemView.findViewById(R.id.head_iv);
            timeTv = (TextView) itemView.findViewById(R.id.chat_image_tv);
            chatImageIv = (ImageView) itemView.findViewById(R.id.chat_image_iv);
        }

        public void bind(DataBodyInfo mInfo) {
            String collecTime = mInfo.getMessageTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.data1));
            if (date) {
                timeTv.setText(format.format(d).substring(format.format(d).length()-6));
            } else {
                timeTv.setText(format.format(d));
            }
            nameTv.setText(mInfo.getForwarderName());
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                    .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                    .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                    .cacheInMemory(true)//是否緩存都內存中
                    .cacheOnDisc(true)//是否緩存到sd卡上
//                    .displayer(new RoundedBitmapDisplayer(20))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                    .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                    .build();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headIv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
            imageLoader.displayImage(mInfo.getRemoteUrl(),
                    chatImageIv,
                    options,
                    mDisplayImageListener);
        }
    }

    private class ArticleViewHolder extends AllViewHolder {

        TextView nameTv;
        TextView timeTv;
        TextView contentTv;
        TextView authorTv;
        XCRoundImageView headIv;
        ImageView imageIv;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.name_tv);
            timeTv = (TextView) itemView.findViewById(R.id.time_tv);
            contentTv = (TextView) itemView.findViewById(R.id.content_tv);
            authorTv = (TextView) itemView.findViewById(R.id.author_tv);
            headIv = (XCRoundImageView) itemView.findViewById(R.id.head_iv);
            imageIv = (ImageView) itemView.findViewById(R.id.image_iv);
        }

        public void bind(DataBodyInfo mInfo) {
            String collecTime = mInfo.getMessageTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.data1));
            if (date) {
                timeTv.setText(format.format(d).substring(format.format(d).length()-6));
            } else {
                timeTv.setText(format.format(d));
            }
            nameTv.setText(mInfo.getForwarderName());
            contentTv.setText(mInfo.getTitle());
            authorTv.setText(mInfo.getName());
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                    .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                    .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                    .cacheInMemory(true)//是否緩存都內存中
                    .cacheOnDisc(true)//是否緩存到sd卡上
//                    .displayer(new RoundedBitmapDisplayer(20))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                    .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                    .build();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headIv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
            imageLoader.displayImage(mInfo.getPreviewUrl(),
                    imageIv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }
    }
}
