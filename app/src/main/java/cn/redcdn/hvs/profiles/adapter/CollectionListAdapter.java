package cn.redcdn.hvs.profiles.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import cn.redcdn.log.CustomLog;


/**
 * Created by Administrator on 2017/3/2.
 */

public class CollectionListAdapter extends RecyclerView.Adapter implements View.OnClickListener, View.OnLongClickListener {
    private CollectionDataListCallBack mCallBack;

    public static final int IMAGE_TYPE = 2;
    public static final int VEDIO_TYPE = 3;
    public static final int AUDIO_TYPE = 7;
    public static final int WORD_TYPE = 8;
    public static final int ARTICAL_TYPE = 30;
    public static final int CHAT_TYPE = 31;
    public static final int CARD_TYPE = 4;

    MyDisplayImageListener mDisplayImageListener = null;
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private OnLongViewItemClickListener mOnLongClickListener = null;

    public CollectionListAdapter(
            CollectionDataListCallBack callBack) {
        mCallBack = callBack;
    }


    @Override
    public int getItemCount() {
        return mCollectionBeanList == null ? 0 : mCollectionBeanList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mCollectionBeanList.get(position).type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mDisplayImageListener = new MyDisplayImageListener();
        if (viewType == WORD_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_word, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            itemView.findViewById(R.id.item_cardview).setOnLongClickListener(this);
            return new WordViewHolder(itemView);
        } else if (viewType == AUDIO_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_audio, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            itemView.findViewById(R.id.item_cardview).setOnLongClickListener(this);
            return new AudioViewHolder(itemView);
        } else if (viewType == IMAGE_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_image, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            itemView.findViewById(R.id.item_cardview).setOnLongClickListener(this);
            return new ImageViewHolder(itemView);
        } else if (viewType == VEDIO_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_video, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            itemView.findViewById(R.id.item_cardview).setOnLongClickListener(this);
            return new VideoViewHolder(itemView);
        } else if (viewType == ARTICAL_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_article, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            itemView.findViewById(R.id.item_cardview).setOnLongClickListener(this);
            return new ArticleViewHolder(itemView);
        } else if (viewType == CHAT_TYPE) {
            View itemView = View.inflate(parent.getContext(), R.layout.item_chat, null);
            itemView.findViewById(R.id.item_cardview).setOnClickListener(this);
            itemView.findViewById(R.id.item_cardview).setOnLongClickListener(this);
            return new ChatViewHolder(itemView);
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
        } else if (holder instanceof ChatViewHolder) {
            ((ChatViewHolder) holder).bind(productBean);
        }
    }

    private class ChatViewHolder extends ProductViewHolder {
        TextView collectionChatName;
        TextView collectionChatWord;
        TextView collectionChatNameTwo;
        TextView collectionChatWordTwo;
        RelativeLayout wordOperateRl;
        RoundImageView headRv;
        TextView timeTv;
        TextView nameTv;


        public ChatViewHolder(View itemView) {
            super(itemView);
            collectionChatName = (TextView) itemView.findViewById(R.id.collection_chat_name);
            collectionChatWord = (TextView) itemView.findViewById(R.id.collection_chat_word);
            collectionChatNameTwo = (TextView) itemView.findViewById(R.id.collection_chat_name_two);
            collectionChatWordTwo = (TextView) itemView.findViewById(R.id.collection_chat_word_two);
            wordOperateRl = (RelativeLayout) itemView.findViewById(R.id.collection_operator);
            headRv = (RoundImageView) wordOperateRl.findViewById(R.id.collection_operator_headimageview);
            timeTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_time);
            nameTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_name);
        }


        @Override
        public void bind(DataBodyInfo mInfo) {
            String collecTime = mInfo.getCollecTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.date));
            timeTv.setText(format.format(d));
            if (mInfo.getCombineName() != null) {
                nameTv.setText(mInfo.getCombineName());
            }
            List<DataBodyInfo> combineInfoList = mInfo.getCombineInfoList();
            if (combineInfoList.size() == 1) {
                DataBodyInfo dataBodyInfo = combineInfoList.get(0);
                if (dataBodyInfo.getType() == VEDIO_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.str_video_thread));
                } else if (dataBodyInfo.getType() == IMAGE_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.str_pic_thread));
                } else if (dataBodyInfo.getType() == WORD_TYPE) {
                    collectionChatWord.setText(dataBodyInfo.getTxt());
                } else if (dataBodyInfo.getType() == CARD_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.str_vcard_thread));
                } else if (dataBodyInfo.getType() == ARTICAL_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.artical_content));
                }
                collectionChatName.setText(dataBodyInfo.getForwarderName());
                collectionChatWordTwo.setVisibility(View.GONE);
                collectionChatNameTwo.setVisibility(View.GONE);
            } else {
                List<DataBodyInfo> dataBodyInfos = combineInfoList.subList(0, 2);

                DataBodyInfo dataBodyInfo = dataBodyInfos.get(0);
                if (dataBodyInfo.getType() == VEDIO_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.str_video_thread));
                } else if (dataBodyInfo.getType() == IMAGE_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.str_pic_thread));
                } else if (dataBodyInfo.getType() == WORD_TYPE) {
                    collectionChatWord.setText(dataBodyInfo.getTxt());
                } else if (dataBodyInfo.getType() == CARD_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.str_vcard_thread));
                } else if (dataBodyInfo.getType() == ARTICAL_TYPE) {
                    collectionChatWord.setText(MedicalApplication.getContext().getString(R.string.artical_content));
                }
                DataBodyInfo dataBodyInfo1 = dataBodyInfos.get(1);
                if (dataBodyInfo1.getType() == VEDIO_TYPE) {
                    collectionChatWordTwo.setText(MedicalApplication.getContext().getString(R.string.str_video_thread));
                } else if (dataBodyInfo1.getType() == IMAGE_TYPE) {
                    collectionChatWordTwo.setText(MedicalApplication.getContext().getString(R.string.str_pic_thread));
                } else if (dataBodyInfo1.getType() == WORD_TYPE) {
                    collectionChatWordTwo.setText(dataBodyInfo1.getTxt());
                } else if (dataBodyInfo1.getType() == CARD_TYPE) {
                    collectionChatWordTwo.setText(MedicalApplication.getContext().getString(R.string.str_vcard_thread));
                } else if (dataBodyInfo1.getType() == ARTICAL_TYPE) {
                    collectionChatWordTwo.setText(MedicalApplication.getContext().getString(R.string.artical_content));
                }
                collectionChatName.setText(dataBodyInfos.get(0).getForwarderName());
                collectionChatNameTwo.setText(dataBodyInfos.get(1).getForwarderName());
            }

            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mInfo.getCombineHeadUrl(),
                    headRv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }

    }


    private class AudioViewHolder extends ProductViewHolder {
        RelativeLayout wordOperateRl;
        TextView timeTv;
        TextView nameTv;
        TextView audioTime;
        RoundImageView headRv;


        public AudioViewHolder(View itemView) {
            super(itemView);
            wordOperateRl = (RelativeLayout) itemView.findViewById(R.id.collection_operator);
            nameTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_name);
            timeTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_time);
            headRv = (RoundImageView) wordOperateRl.findViewById(R.id.collection_operator_headimageview);
            audioTime = (TextView) itemView.findViewById(R.id.collection_audio_length);
        }

        @Override
        public void bind(DataBodyInfo mInfo) {
            String groupName = mInfo.getGroupName();
            if (mInfo.getForwarderName() != null) {
                if (!groupName.equals("")) {
                    nameTv.setText(groupName);
                } else {
                    nameTv.setText(mInfo.getForwarderName());
                }
            }
            String collecTime = mInfo.getCollecTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.date));
            timeTv.setText(format.format(d));
            int duration = mInfo.getDuration();
            audioTime.setText(duration +MedicalApplication.getContext().getString(R.string.second));
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headRv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }


    }

    private class ImageViewHolder extends ProductViewHolder {
        RelativeLayout wordOperateRl;
        TextView timeTv;
        TextView nameTv;
        ImageView singleIv;
        RoundImageView headRv;

        public ImageViewHolder(View itemView) {
            super(itemView);
            wordOperateRl = (RelativeLayout) itemView.findViewById(R.id.collection_operator);
            nameTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_name);
            timeTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_time);
            singleIv = (ImageView) itemView.findViewById(R.id.collection_single_imageview);
            headRv = (RoundImageView) itemView.findViewById(R.id.collection_operator_headimageview);
        }


        @Override
        public void bind(DataBodyInfo mInfo) {
            String collecTime = mInfo.getCollecTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.date));
            timeTv.setText(format.format(d));
            String groupName = mInfo.getGroupName();
            if (mInfo.getForwarderName() != null) {
                if (!groupName.equals("")) {
                    nameTv.setText(groupName);
                } else {
                    nameTv.setText(mInfo.getForwarderName());
                }
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
            imageLoader.displayImage(mInfo.getRemoteUrl(),
                    singleIv,
                    options,
                    mDisplayImageListener);
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headRv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);

        }

    }

    private class VideoViewHolder extends ProductViewHolder {

        RelativeLayout videoOperateRl;
        TextView nameTv;
        TextView timeTv;
        ImageView vedioPreview;
        TextView timeLongTv;
        RoundImageView headRv;

        public VideoViewHolder(View itemView) {
            super(itemView);
            videoOperateRl = (RelativeLayout) itemView.findViewById(R.id.collection_operator);
            nameTv = (TextView) videoOperateRl.findViewById(R.id.collection_operator_name);
            timeTv = (TextView) videoOperateRl.findViewById(R.id.collection_operator_time);
            vedioPreview = (ImageView) itemView.findViewById(R.id.collection_vedio_view);
            timeLongTv = (TextView) itemView.findViewById(R.id.time_tv);
            headRv = (RoundImageView) itemView.findViewById(R.id.collection_operator_headimageview);
        }


        @Override
        public void bind(DataBodyInfo mInfo) {
            String collecTime = mInfo.getCollecTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.date));
            timeTv.setText(format.format(d));
            String groupName = mInfo.getGroupName();
            if (mInfo.getForwarderName() != null) {
                if (!groupName.equals("")) {
                    nameTv.setText(groupName);
                } else {
                    nameTv.setText(mInfo.getForwarderName());
                }
            }
            int bb = mInfo.getDuration();
            CustomLog.e("ss", bb + "");
            timeLongTv.setText(mInfo.getDuration() + MedicalApplication.getContext().getString(R.string.second));
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                    .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                    .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                    .cacheInMemory(true)//是否緩存都內存中
                    .cacheOnDisc(true)//是否緩存到sd卡上
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                    .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                    .build();
            imageLoader.displayImage(mInfo.getThumbnailRemoteUrl(),
                    vedioPreview,
                    options,
                    mDisplayImageListener);
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headRv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }

    }


    private class ArticleViewHolder extends ProductViewHolder {
        RelativeLayout articalOperateRl;
        TextView nameTv;
        TextView zhuozhe;
        TextView timeTv;
        ImageView imageIv;
        TextView artical;
        XCRoundImageView headRv;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            articalOperateRl = (RelativeLayout) itemView.findViewById(R.id.collection_operator);
            nameTv = (TextView) articalOperateRl.findViewById(R.id.collection_operator_name);
            timeTv = (TextView) articalOperateRl.findViewById(R.id.collection_operator_time);
            imageIv = (ImageView) itemView.findViewById(R.id.image_iv);
            zhuozhe = (TextView) itemView.findViewById(R.id.zhuozhe);
            artical = (TextView) itemView.findViewById(R.id.collection_article_view);
            headRv = (XCRoundImageView) itemView.findViewById(R.id.collection_operator_headimageview);
        }

        @Override
        public void bind(DataBodyInfo mInfo) {
            if (mInfo.getIsforwarded() == 1) {
                zhuozhe.setText(mInfo.getName());
                nameTv.setText(mInfo.getForwarderName());
            } else {
                zhuozhe.setText(mInfo.getName());
                nameTv.setText(mInfo.getName());
            }
            String collecTime = mInfo.getCollecTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.date));
            timeTv.setText(format.format(d));
            artical.setText(mInfo.getTitle());
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showStubImage(R.drawable.hillbackground)//设置图片在下载期间显示的图片
                    .showImageForEmptyUri(R.drawable.hillbackground)//设置图片Uri为空或是错误的时候显示的图片
                    .showImageOnFail(R.drawable.hillbackground)//设置图片加载/解码过程中错误时候显示的图片
                    .cacheInMemory(true)//是否緩存都內存中
                    .cacheOnDisc(true)//是否緩存到sd卡上
                    .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                    .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                    .build();
            imageLoader.displayImage(mInfo.getPreviewUrl(),
                    imageIv,
                    options,
                    mDisplayImageListener);
            imageLoader.displayImage(mInfo.getOffAccLogoUrl(),
                    headRv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }

    }

    private class WordViewHolder extends ProductViewHolder {
        TextView contentEv;
        RelativeLayout wordOperateRl;
        RoundImageView headRv;
        TextView timeTv;
        TextView nameTv;


        public WordViewHolder(View itemView) {
            super(itemView);
            contentEv = (TextView) itemView.findViewById(R.id.collection_text_view);
            wordOperateRl = (RelativeLayout) itemView.findViewById(R.id.collection_operator);
            headRv = (RoundImageView) wordOperateRl.findViewById(R.id.collection_operator_headimageview);
            timeTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_time);
            nameTv = (TextView) wordOperateRl.findViewById(R.id.collection_operator_name);
        }


        @Override
        public void bind(DataBodyInfo mInfo) {
            String collecTime = mInfo.getCollecTime() + "000";
            long l = Long.parseLong(collecTime);
            Date d = new Date(l);
            SimpleDateFormat format = new SimpleDateFormat(MedicalApplication.getContext().getString(R.string.date));
            timeTv.setText(format.format(d));
            String groupName = mInfo.getGroupName();
            if (mInfo.getForwarderName() != null) {
                if (!groupName.equals("")) {
                    nameTv.setText(groupName);
                } else {
                    nameTv.setText(mInfo.getForwarderName());
                }
            }

            contentEv.setText(mInfo.getTxt());
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mInfo.getForwarderHeaderUrl(),
                    headRv,
                    MedicalApplication.shareInstance().options,
                    mDisplayImageListener);
        }

    }


    public List<DataBodyInfo> mCollectionBeanList;


    public void setData(List<DataBodyInfo> mCollection) {
        this.mCollectionBeanList = mCollection;
        if (mCallBack != null) {
            mCallBack.onDataSizeChanged(getCount());
        }
        notifyDataSetChanged();
    }


    public int getCount() {
        return (mCollectionBeanList == null) ? 0 : mCollectionBeanList.size();
    }


    public static abstract class ProductViewHolder extends RecyclerView.ViewHolder {

        public ProductViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(DataBodyInfo productInfo);

    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, DataBodyInfo data);
    }


    public interface OnLongViewItemClickListener {
        void longClick(View view, DataBodyInfo data);
    }

    @Override
    public void onClick(View view) {
        mOnItemClickListener.onItemClick(view, (DataBodyInfo) view.getTag());
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setLongItemClickListener(OnLongViewItemClickListener listener) {
        this.mOnLongClickListener = listener;
    }

    @Override
    public boolean onLongClick(View view) {
        mOnLongClickListener.longClick(view, (DataBodyInfo) view.getTag());
        return true;
    }

    public interface CollectionDataListCallBack {
        void onDataSizeChanged(int count);
    }

}
