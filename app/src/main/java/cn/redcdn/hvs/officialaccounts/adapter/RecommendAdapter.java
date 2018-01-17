package cn.redcdn.hvs.officialaccounts.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.redcdn.datacenter.offaccscenter.data.ArtcleInfo;
import cn.redcdn.datacenter.offaccscenter.data.MDSRecommedOffaccInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.officialaccounts.DingYueActivity;
import cn.redcdn.hvs.officialaccounts.activity.ArticleActivity;
import cn.redcdn.hvs.officialaccounts.activity.OfficialMainActivity;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;
import cn.redcdn.hvs.officialaccounts.listener.DingyueDisplayImageListener;
import cn.redcdn.log.CustomLog;

import static android.content.Context.MODE_APPEND;
import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;


public class RecommendAdapter extends RecyclerView.Adapter<RecommendAdapter.MyHolder> {
    private static final String TAG = RecommendAdapter.class.getName();
    FragmentActivity activity;
    private Context mContext;
    public boolean loading;
    public List<MDSRecommedOffaccInfo> mRecommedList;
    private DingyueDisplayImageListener mDisplayImageListener = null;
    private TextView articleTitle;//文章标题
    private TextView publishTime; //发布时间
    private ImageView lock;//加密锁
    private ImageView articleImg;//文章图片
    private float tvWidth;
    private TextView publishTime2;
    private ImageView lock2;

    public RecommendAdapter(List<MDSRecommedOffaccInfo> recommedList, Context context, RecyclerView recyclerView, FragmentActivity activity) {
        this.mRecommedList = recommedList;
        this.mContext = context;
        this.activity = activity;
        mDisplayImageListener = new DingyueDisplayImageListener();
    }


    class MyHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;
        /***
         * 公众号信息
         */
        private ImageView recommond_headImag; // 公众号头像
        private TextView officalname; // 公众号名称
        private TextView introduction; // 简介
        private LinearLayout btn_tomainPage;


        public MyHolder(View itemView, int viewType) {
            super(itemView);
            initView(itemView);
        }

        private void initView(View itemView) {
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linerLayout);
            recommond_headImag = (ImageView) itemView.findViewById(R.id.recommond_headImag);
            officalname = (TextView) itemView.findViewById(R.id.officalname);
            introduction = (TextView) itemView.findViewById(R.id.introduction);
            btn_tomainPage = (LinearLayout) itemView.findViewById(R.id.btn_tomainPage);

        }
    }


    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommended_recy_item, parent, false);
        return new MyHolder(view, viewType);
    }

    ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    public void onBindViewHolder(final MyHolder holder, int position) {
        final MDSRecommedOffaccInfo recommedOffaccInfo = mRecommedList.get(position);
        final List<ArtcleInfo> artcleList = recommedOffaccInfo.getArtcleList();
        if (mRecommedList != null) {
            //设置公众号名字
            if (recommedOffaccInfo.getName() != null) {
                holder.officalname.setText(recommedOffaccInfo.getName());
            }
            //设置公众号简介
            if (recommedOffaccInfo.getIntroduction() != null) {
                holder.introduction.setText(recommedOffaccInfo.getIntroduction());
            }
            //加载公众号头像
            if (recommedOffaccInfo.getLogoUrl() != null) {
                imageLoader.displayImage(recommedOffaccInfo.getLogoUrl(),
                        holder.recommond_headImag,
                        MedicalApplication.shareInstance().options,
                        mDisplayImageListener);
            }
            //公众号头像
            holder.recommond_headImag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent_card = new Intent();
                    intent_card.setClass(mContext, DingYueActivity.class);
                    intent_card.putExtra("officialAccountId", recommedOffaccInfo.getOffaccid());
                    intent_card.putExtra("officialName", recommedOffaccInfo.getName());
                    mContext.startActivity(intent_card);
                }
            });
            //跳转到公众号主页
            holder.btn_tomainPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent_offi = new Intent(mContext, OfficialMainActivity.class);
                    intent_offi.putExtra("officialAccountId", recommedOffaccInfo.getOffaccid());//公众号id
                    intent_offi.putExtra("officialName", recommedOffaccInfo.getName());//公众号名称
                    mContext.startActivity(intent_offi);

                }
            });
            holder.linearLayout.removeAllViews();
            if (artcleList != null && !artcleList.isEmpty()) {
                for (int i = 0; i < artcleList.size(); i++) {
                    CustomLog.d(TAG, "文章个数" + artcleList.size());
                    View view = View.inflate(mContext, R.layout.articleitem, null);
                    articleTitle = (TextView) view.findViewById(R.id.articleTitle);//文章标题
                    publishTime = (TextView) view.findViewById(R.id.publishTime);//发布时间
                    lock = (ImageView) view.findViewById(R.id.lock);//锁
                    articleImg = (ImageView) view.findViewById(R.id.articleImg);//文章图片
                    publishTime2 = (TextView) view.findViewById(R.id.publishTime2);
                    lock2 = (ImageView) view.findViewById(R.id.lock2);
                    holder.linearLayout.addView(view);
                    final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                    int left = (int) mContext.getResources().getDimension(R.dimen.x20);
                    int right = (int) mContext.getResources().getDimension(R.dimen.x20);
//                    layoutParams.setMargins(left, 0, right, 0);

                    final ArtcleInfo artcleInfo = artcleList.get(i);
                    CustomLog.d(TAG, "artcleInfo" + artcleInfo);
                    DisplayImageOptions options2 = new DisplayImageOptions.Builder()
                            .showStubImage(R.drawable.image)//设置图片在下载期间显示的图片
                            .showImageForEmptyUri(R.drawable.image)//设置图片Uri为空或是错误的时候显示的图片
                            .showImageOnFail(R.drawable.image)//设置图片加载/解码过程中错误时候显示的图片
                            .cacheInMemory(true)//是否緩存都內存中
                            .cacheOnDisc(true)//是否緩存到sd卡上
                            .displayer(new RoundedBitmapDisplayer(0))//设置图片的显示方式 : 设置圆角图片  int roundPixels
                            .bitmapConfig(Bitmap.Config.RGB_565)//设置为RGB565比起默认的ARGB_8888要节省大量的内存
                            .delayBeforeLoading(100)//载入图片前稍做延时可以提高整体滑动的流畅度
                            .build();
                    imageLoader.displayImage(artcleInfo.getPreviewUrl(),
                            articleImg,
                            options2,
                            mDisplayImageListener);

                    articleTitle.setText(artcleList.get(i).getTitle());//标题
//                    articleTitle.setText("测试是上司是事实上司是事实上死死是啥测试杀死后四十我我我我我我我我我我哦");
                    publishTime(artcleInfo.getPublishTime(), publishTime);//时间
                    publishTime(artcleInfo.getPublishTime(), publishTime2);
                    SharedPreferences preferences = mContext.getSharedPreferences("data", MODE_APPEND);
                    //新拼接的字符串
                    String newKey = AccountManager.getInstance(MedicalApplication.context).getNube() + "_" + artcleInfo.getArticleId();
                    String newPwd = preferences.getString(newKey, "");

//                    final RelativeLayout.LayoutParams lockParms = (RelativeLayout.LayoutParams) lock.getLayoutParams();
//                    final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) publishTime.getLayoutParams();


                    final String rawText = articleTitle.getText().toString();//原始文本

                    //将原始文本进行拆分
                    String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");

                    final Paint tvPaint = articleTitle.getPaint();//paint,包含字体等信息
                    WindowManager wm = activity.getWindowManager();
//                    DisplayMetrics metrics = new DisplayMetrics();
//                   wm.getDefaultDisplay().getMetrics(metrics);
//                    int height = metrics.heightPixels;
//                    int width  = metrics.widthPixels;
                    int width = wm.getDefaultDisplay().getWidth();
                    int imgWidth = (int) mContext.getResources().getDimension(R.dimen.x80);
                    CustomLog.d(TAG, "图片宽度" + imgWidth);
                    if (TextUtils.isEmpty(artcleInfo.getPreviewUrl())) {
                        tvWidth = width - 2 * left - 3 * right - articleTitle.getCompoundPaddingLeft() - articleTitle.getCompoundPaddingRight();

                    } else {
                        tvWidth = width - 2 * left - 3 * right - imgWidth - articleTitle.getCompoundPaddingLeft() - articleTitle.getCompoundPaddingRight();
                    }
                    CustomLog.d(TAG, "tvWidth" + tvWidth);


                    for (String rawTextLine : rawTextLines) {
                        if (tvPaint.measureText(rawTextLine) <= tvWidth) {

                            CustomLog.d(TAG, "没换行");
                            lock.setVisibility(View.INVISIBLE);
                            publishTime.setVisibility(View.INVISIBLE);
                            lock2.setVisibility(View.VISIBLE);
                            publishTime2.setVisibility(View.VISIBLE);
                            if (artcleInfo.getIsEncipher() == 1 || artcleInfo.getEncipherPwd().equalsIgnoreCase(newPwd)) {
                                lock2.setVisibility(View.INVISIBLE);
                            } else {
                                lock2.setVisibility(View.VISIBLE);
                            }
                        } else {
                            CustomLog.d(TAG, "标题换行");
                            //当加密类型为不加密或者已经输入过密码 则不显示锁
                            if (artcleInfo.getIsEncipher() == 1 || artcleInfo.getEncipherPwd().equalsIgnoreCase(newPwd)) {
                                lock.setVisibility(View.INVISIBLE);
                            } else {
                                lock.setVisibility(View.VISIBLE);
                            }
                            publishTime.setVisibility(View.VISIBLE);
                            lock2.setVisibility(View.INVISIBLE);
                            publishTime2.setVisibility(View.INVISIBLE);
                        }
                        float NOwidth = tvPaint.measureText(rawTextLine);
                        CustomLog.d(TAG, "原文本宽度" + NOwidth + artcleList.get(i).getTitle());
                        CustomLog.d(TAG, "文本宽度" + tvWidth);
                    }
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String articleId = artcleInfo.getArticleId();
                            if (artcleInfo.getArticleType() == 1) {
                                Intent intent = new Intent(mContext, ArticleActivity.class);
                                intent.putExtra(INTENT_DATA_ARTICLE_ID, articleId);
                                mContext.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mContext, VideoPublishActivity.class);
                                intent.putExtra(INTENT_DATA_ARTICLE_ID, articleId);
                                mContext.startActivity(intent);
                            }
                        }
                    });

                }
            }
        }
    }


    private void publishTime(String pubTime, TextView tv) {
        //获取当前时间
        Long currentTime = System.currentTimeMillis() / 1000;
        //获取发布时间
        Long publishTime = Long.valueOf(pubTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yy年MM月dd日 ");
        String times = sdf.format(new Date(publishTime * 1000L));
        Long timeDistance = currentTime - publishTime;
        if (timeDistance >= 31536000) {
            //如果超过一年就显示yy年MM月dd日
            tv.setText(times);
        } else if (timeDistance >= 604800 && timeDistance < 31536000) {
            //大于一周小于一年显示MM月dd日
            SimpleDateFormat mDays = new SimpleDateFormat("MM月dd日");
            String monDay = mDays.format(new Date(publishTime * 1000L));
            tv.setText(monDay);
        } else if (timeDistance >= 172800 && timeDistance < 604800) {
            //大于48小时小于一周 显示星期几
            SimpleDateFormat format = new SimpleDateFormat("EEEE");
            String week = format.format(new Date(publishTime * 1000L));
            tv.setText(week);
        } else if (timeDistance >= 86400 && timeDistance < 172800) {
            //大于24小时小于48小时 显示昨天
            tv.setText("昨天");
        } else if (timeDistance >= 3600 && timeDistance <= 86400) {
            //大于1小时小于一天
            tv.setText((int) ((currentTime - publishTime) / 3600) + "" + "小时前");
        } else if (timeDistance >= 900 && timeDistance <= 3600) {
            //大于15分钟小于 一小时
            tv.setText((int) (timeDistance / 60) + "" + "分钟前");
        } else {
            tv.setText("刚刚");
        }
    }

    @Override
    public int getItemCount() {
        return mRecommedList.size();
    }


}
