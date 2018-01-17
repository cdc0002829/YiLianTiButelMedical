package cn.redcdn.hvs.head.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.medicalcenter.MDSAppGetHomeList;
import cn.redcdn.datacenter.medicalcenter.data.MDSHomeCustomInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSHomeInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSHomelistInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.head.adapter.GalleryAdapter;
import cn.redcdn.hvs.head.adapter.HeadAdapter;
import cn.redcdn.hvs.head.javabean.OfficialAccountsBean;
import cn.redcdn.hvs.head.javabean.PicBean;
import cn.redcdn.hvs.head.manager.FullyLinearLayoutManager;
import cn.redcdn.hvs.officialaccounts.activity.ArticleActivity;
import cn.redcdn.hvs.officialaccounts.activity.OfficialMainActivity;
import cn.redcdn.hvs.officialaccounts.activity.OrderActivity;
import cn.redcdn.hvs.officialaccounts.activity.RecommondActivity;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;
import static cn.redcdn.hvs.head.adapter.HeadAdapter.BIG_IMAGE_TYPE;
import static cn.redcdn.hvs.head.adapter.HeadAdapter.LITTLE_IMAGE_TYPE;
import static cn.redcdn.hvs.head.adapter.HeadAdapter.TITLE_TYPE;
import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;

/**
 * Created by Administrator on 2017/7/27.
 */

public class HeadActivity extends BaseActivity {

    // 视图
    private View contentView = null;
    private RecyclerView recyclerview;
    private GalleryAdapter mAdapter;
    private List<OfficialAccountsBean> mDatas = new ArrayList<>();
    private RecyclerView recyclerViewSecond;
    private List<String> imageItems;
    private List<PicBean> picBeen = new ArrayList<>();
    private String token;
    private List<MDSHomelistInfo> homeRecommendList;
    private List<MDSHomelistInfo> subList;
    private List<MDSHomelistInfo> homeOfficialAccountList;
    private TextView requestData;
    private List<MDSHomeCustomInfo> homeCustomList;
    private ImageButton imageBtn;
    private LinearLayout llMain;
    private TextView getDataFail;
    private Banner mBanner;
    private List<String> mTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head);
        requestData = (TextView) findViewById(R.id.requestData);
        getDataFail = (TextView) findViewById(R.id.getdata_fail);
        initDatas();
        initWidget();
        getDataFail.setOnClickListener(mbtnHandleEventListener);
    }

    public class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            //Glide 加载图片简单用法
            Glide.with(MedicalApplication.getContext()).load(path).placeholder(R.drawable.hillbackground).error(R.drawable.hillbackground).centerCrop().into(imageView);
        }
    }

    private void initWidget() {
        imageBtn = (ImageButton) findViewById(R.id.back);
        imageBtn.setOnClickListener(mbtnHandleEventListener);
        llMain = (LinearLayout) findViewById(R.id.ll_main);
        mBanner = (Banner) findViewById(R.id.banner);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        mBanner.setImageLoader(new GlideImageLoader());
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        FullyLinearLayoutManager linearLayoutManager = new FullyLinearLayoutManager(HeadActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerview.setNestedScrollingEnabled(false);
        //设置布局管理器
        recyclerview.setLayoutManager(linearLayoutManager);

        recyclerViewSecond = (RecyclerView) findViewById(R.id.recyclerView_second);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(HeadActivity.this, 2, GridLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerViewSecond.setLayoutManager(gridLayoutManager);
    }

    private void initDatas() {
        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        if (!token.equals("")) {
            getdata(token);
        }

    }

    @Override
    public void todoClick(int i) {
        super.todoClick(i);
        switch (i){
            case R.id.back:
                HeadActivity.this.finish();
                break;
            case R.id.getdata_fail:
                getdata(token);
                break;
        }
    }

    private void getdata(String token) {
        final MDSAppGetHomeList mdsAppGetHomeList = new MDSAppGetHomeList() {
            @Override
            protected void onSuccess(MDSHomeInfo responseContent) {
                super.onSuccess(responseContent);
                requestData.setVisibility(View.GONE);
                getDataFail.setVisibility(View.GONE);
                llMain.setVisibility(View.VISIBLE);
                homeRecommendList = responseContent.getHomeRecommendList();
                imageItems = new ArrayList<>();
                mTitles = new ArrayList<>();
                if (homeRecommendList!=null&&homeRecommendList.size()>4){
                    subList = homeRecommendList.subList(0, 4);
                    for (int i = 0; i < subList.size(); i++) {
                        imageItems.add(subList.get(i).getShowPic());
                        mTitles.add(subList.get(i).getShowName());
                    }
                }else if (homeRecommendList!=null&&homeRecommendList.size()>0&&homeRecommendList.size()<=4){
                    for (int i = 0; i < homeRecommendList.size(); i++) {
                        imageItems.add(homeRecommendList.get(i).getShowPic());
                        mTitles.add(homeRecommendList.get(i).getShowName());
                    }
                }
                mBanner.setImages(imageItems);
                mBanner.setBannerTitles(mTitles);
                mBanner.setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int position) {
                        int articleType;
                        String id;
                        if (subList==null){
                            articleType=  homeRecommendList.get(position).getArticleType();
                            id=homeRecommendList.get(position).getId();
                        }else {
                            articleType = subList.get(position).getArticleType();
                            id=subList.get(position).getId();
                        }
                        if (articleType == 1) {
                            Intent intent = new Intent(MedicalApplication.getContext(), ArticleActivity.class);
                            intent.putExtra(INTENT_DATA_ARTICLE_ID, id);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(MedicalApplication.getContext(), VideoPublishActivity.class);
                            intent.putExtra(INTENT_DATA_ARTICLE_ID, id);
                            startActivity(intent);
                        }
                    }
                });
                //设置自动轮播，默认为true
                mBanner.isAutoPlay(false);
                //设置指示器位置（当banner模式中有指示器时）
                mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                //banner设置方法全部调用完毕时最后调用
                mBanner.start();
                homeOfficialAccountList = responseContent.getHomeOfficialAccountList();
                if (homeOfficialAccountList != null) {
                    if (homeOfficialAccountList.size() > 0) {
                        for (int i = 0; i < homeOfficialAccountList.size(); i++) {
                            mDatas.add(new OfficialAccountsBean(homeOfficialAccountList.get(i).getShowPic(), homeOfficialAccountList.get(i).getShowName(), homeOfficialAccountList.get(i).getId()));
                        }
                        mDatas.add(  new OfficialAccountsBean("", getString(R.string.all)));
                        //设置适配器
                        mAdapter = new GalleryAdapter(HeadActivity.this);
                        mAdapter.setData(mDatas);
                        mAdapter.setOnItemClickListener(new GalleryAdapter.OnRecyclerViewItemClickListener() {
                            @Override
                            public void onItemClick(View view, OfficialAccountsBean data) {
                                if (data.getInformation().contains(getString(R.string.all))) {
                                    Intent intent = new Intent();
                                    intent.setClass(HeadActivity.this, RecommondActivity.class);
                                    startActivity(intent);
                                } else if (data.getInformation().contains(getString(R.string.suscribe))) {
                                    Intent intent = new Intent();
                                    intent.setClass(HeadActivity.this, OrderActivity.class);
                                    startActivity(intent);
                                } else {
                                    Intent intent_public = new Intent(HeadActivity.this, OfficialMainActivity.class);
                                    intent_public.putExtra("officialAccountId", data.getOffaccId());
                                    intent_public.putExtra("officialName", data.getInformation());
                                    startActivity(intent_public);
                                }
                            }
                        });
                        recyclerview.setAdapter(mAdapter);
                    }
                }

                homeCustomList = responseContent.getHomeCustomList();
                for (int i = 0; i < homeCustomList.size(); i++) {
                    int showMode = homeCustomList.get(i).getShowMode();
                    if (showMode==1){
                        MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomList.get(i);
                        picBeen.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE));
                        List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                        for (int j = 0; j < articleList.size(); j++) {
                            MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                            picBeen.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                        }
                    }
                }
                HeadAdapter headAdapter = new HeadAdapter(HeadActivity.this);
                headAdapter.setData(picBeen);
                recyclerViewSecond.setAdapter(headAdapter);
                headAdapter.setOnItemClickListener(new HeadAdapter.OnRecyclerViewItemClickListener() {
                    @Override
                    public void onItemClick(View view, PicBean data) {
                        switch (data.getType()) {
                            case BIG_IMAGE_TYPE:
                                if (data.getArticleType() == 1) {
                                    Intent intent = new Intent(MedicalApplication.getContext(), ArticleActivity.class);
                                    intent.putExtra(INTENT_DATA_ARTICLE_ID, data.getArticleId());
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(MedicalApplication.getContext(), VideoPublishActivity.class);
                                    intent.putExtra(INTENT_DATA_ARTICLE_ID, data.getArticleId());
                                    startActivity(intent);
                                }
                                break;
                            case TITLE_TYPE:

                                break;
                            case LITTLE_IMAGE_TYPE:
                                if (data.getArticleType() == 1) {
                                    Intent intent = new Intent(MedicalApplication.getContext(), ArticleActivity.class);
                                    intent.putExtra(INTENT_DATA_ARTICLE_ID, data.getArticleId());
                                    startActivity(intent);
                                } else {
                                    Intent intent = new Intent(MedicalApplication.getContext(), VideoPublishActivity.class);
                                    intent.putExtra(INTENT_DATA_ARTICLE_ID, data.getArticleId());
                                    startActivity(intent);
                                }
                                break;
                        }
                    }
                });
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(MedicalApplication.getContext()).tokenAuthFail(statusCode);
                }
                requestData.setVisibility(View.GONE);
                getDataFail.setVisibility(View.VISIBLE);
            }
        };
        mdsAppGetHomeList.getHomeList(token);
    }
}
