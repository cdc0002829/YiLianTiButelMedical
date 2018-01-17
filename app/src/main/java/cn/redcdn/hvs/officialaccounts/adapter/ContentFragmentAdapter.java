package cn.redcdn.hvs.officialaccounts.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.redcdn.datacenter.offaccscenter.data.MDSfocusOffAccArtcleInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.contacts.contact.diaplayImageListener.DisplayImageListener;
import cn.redcdn.hvs.meeting.util.DateUtil;
import cn.redcdn.hvs.officialaccounts.activity.ArticleActivity;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;

import cn.redcdn.hvs.officialaccounts.listener.DingyueDisplayImageListener;
import cn.redcdn.log.CustomLog;

import static android.content.Context.MODE_APPEND;
import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;

/**
 * Created by ${chenghb} on 2017/2/27.
 */

public class ContentFragmentAdapter extends RecyclerView.Adapter<ContentFragmentAdapter.ViewHolder> {
    private static final String TAG = ContentFragmentAdapter.class.getName();

    private Context mContext;

    public static final int LOADMORE = 0;
    public static final int NORMAL = 1;
    private int visibleItemCount;
    private int totalItemCount;
    private int firstVisibleItem;
    private boolean loading = false; //标识是否在做上滑加载更多.ture:正在执行； false：结束执行
    private List<MDSfocusOffAccArtcleInfo> focusList;
    private DingyueDisplayImageListener mDisplayImageListener = null;

    public ContentFragmentAdapter(List<MDSfocusOffAccArtcleInfo> list, Context context, RecyclerView recyclerView) {
        this.focusList = list;
        this.mContext = context;
        final LinearLayoutManager mLayoutManager;

    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout content_liner;
        private TextView content_topic, content_brief, public_time, visit_count;
        private ImageView lock, content_Image;


        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            init(itemView, viewType);
        }

        private void init(View itemView, int viewType) {
            visit_count = (TextView) itemView.findViewById(R.id.visit_count);
            content_liner = (LinearLayout) itemView.findViewById(R.id.content_liner);
            public_time = (TextView) itemView.findViewById(R.id.public_time);
            content_Image = (ImageView) itemView.findViewById(R.id.content_Image);
            lock = (ImageView) itemView.findViewById(R.id.lock);
            content_liner = (LinearLayout) itemView.findViewById(R.id.content_liner);
            content_topic = (TextView) itemView.findViewById(R.id.content_topic);
            content_brief = (TextView) itemView.findViewById(R.id.content_brief);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_recy_item, parent, false);


        return new ViewHolder(view, viewType);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        MDSfocusOffAccArtcleInfo byOffAcc = focusList.get(position);
        if (focusList != null) {
            holder.content_Image.setVisibility(View.VISIBLE);
            holder.content_brief.setText(focusList.get(position).getInstroduction());//文章简介
            CustomLog.d(TAG, "contentTAG" + focusList.get(position).getInstroduction());
            holder.content_topic.setText(focusList.get(position).getArticleTitle());//标题
            timeUtils(holder, position);//时间戳
            visitCount(holder, position);//访问次数
            displayImage(holder, position, byOffAcc);//加载图片
            isLock(holder, position);//是否加锁
            holder.content_liner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (focusList.get(position).getArticleType() == 1) {
                        Intent intent = new Intent(mContext, ArticleActivity.class);
                        intent.putExtra(INTENT_DATA_ARTICLE_ID, focusList.get(position).getArticleId());
                        mContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, VideoPublishActivity.class);
                        intent.putExtra(INTENT_DATA_ARTICLE_ID, focusList.get(position).getArticleId());
                        mContext.startActivity(intent);
                    }
                }
            });

        }
    }

    private void isLock(ViewHolder holder, int position) {
        SharedPreferences preferences = mContext.getSharedPreferences("data", MODE_APPEND);
        //新拼接的字符串
        String newKey = AccountManager.getInstance(MedicalApplication.context).getNube() + "_" + focusList.get(position).getArticleId();
        String newPwd = preferences.getString(newKey, "");
        //当加密类型为不加密或者已经输入过密码 则不显示锁
        if (focusList.get(position).getIsEncipher() == 1 || focusList.get(position).getEncipherPwd().equalsIgnoreCase(newPwd)) {
            holder.lock.setVisibility(View.INVISIBLE);
        } else {
            holder.lock.setVisibility(View.VISIBLE);
        }
    }

    private void displayImage(ViewHolder holder, int position, MDSfocusOffAccArtcleInfo byOffAcc) {

        ImageLoader imageLoader = ImageLoader.getInstance();
        mDisplayImageListener = new DingyueDisplayImageListener();


        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.image)//设置图片在下载期间显示的图片
                .showImageForEmptyUri(R.drawable.image)//设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.image)//设置图片加载/解码过程中错误时候显示的图片
                .cacheInMemory(true)//是否緩存都內存中
                .cacheOnDisc(true)//是否緩存到sd卡上
                .displayer(new RoundedBitmapDisplayer(0))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                .build();
        imageLoader.displayImage(focusList.get(position).getShowImgUrl(),
                holder.content_Image,
                options,
                mDisplayImageListener);

    }

    private void visitCount(ViewHolder holder, int position) {
        long times = focusList.get(position).getPlayCount();
//          long times = 1111111111;
        /**
         * double   f   =   111231.5585;
         BigDecimal   b   =   new   BigDecimal(f);
         double   f1   =   b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
         *
         * **/

        if (times >= 10000) {
            if (times >= 100000000) {
                double f = times / 100000000.0f;
                BigDecimal b = new BigDecimal(f);
                double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                holder.visit_count.setText(mContext.getString(R.string.visit) + f1 + "" + mContext.getString(R.string.one_hundred_million));
            } else {
                double f = times / 10000.0f;
                BigDecimal b = new BigDecimal(f);
                double f1 = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                holder.visit_count.setText(mContext.getString(R.string.visit) + f1 + "" + mContext.getString(R.string.ten_thousand));
            }
        } else {
            holder.visit_count.setText(mContext.getString(R.string.visit) + times + "" + "次");//访问次数
        }
    }

    private void timeUtils(ViewHolder holder, int position) {
        //获取当前时间
        Long currentTime = System.currentTimeMillis() / 1000;
        //获取发布时间
        Long publishTime = Long.valueOf(focusList.get(position).getPublishTime());
        SimpleDateFormat sdf = new SimpleDateFormat(mContext.getString(R.string.year_months_day));
        String times = sdf.format(new Date(publishTime * 1000L));
        Long timeDistance = currentTime - publishTime;
        if (timeDistance >= 31536000) {
            //如果超过一年就显示yy年MM月dd日
            holder.public_time.setText(times);
        } else if (timeDistance >= 604800 && timeDistance < 31536000) {
            //大于一周小于一年显示MM月dd日
            SimpleDateFormat mDays = new SimpleDateFormat(mContext.getString(R.string.months_day));
            String monDay = mDays.format(new Date(publishTime * 1000L));
            holder.public_time.setText(monDay);
        } else if (timeDistance >= 172800 && timeDistance < 604800) {
            //大于48小时小于一周 显示星期几
            SimpleDateFormat format = new SimpleDateFormat("EEEE");
            String week = format.format(new Date(publishTime * 1000L));
            holder.public_time.setText(week);
        } else if (timeDistance >= 86400 && timeDistance < 172800) {
            //大于24小时小于48小时 显示昨天
            holder.public_time.setText(R.string.date_yesterday);
        } else if (timeDistance >= 3600 && timeDistance <= 86400) {
            //大于1小时小于一天
            holder.public_time.setText((int) ((currentTime - publishTime) / 3600) + "" + mContext.getString(R.string.hours_ago));
        } else if (timeDistance >= 900 && timeDistance <= 3600) {
            //大于15分钟小于 一小时
            holder.public_time.setText((int) (timeDistance / 60) + "" + mContext.getString(R.string.minutes_ago));
        } else {
            holder.public_time.setText(R.string.just);
        }
    }


//    @Override
//    public int getItemViewType(int position) {
//        if (focusList.get(position) == null) {
//            return LOADMORE;
//        } else {
//            return NORMAL;
//        }
//    }

    @Override
    public int getItemCount() {
        return focusList.size();
    }
}
