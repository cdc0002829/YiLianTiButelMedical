package cn.redcdn.hvs.head.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.HPUGetMainPageInfo;
import cn.redcdn.datacenter.hpucenter.data.HPUHomeListInfo;
import cn.redcdn.datacenter.hpucenter.data.MainPageInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.medicalcenter.data.MDSHomeCustomInfo;
import cn.redcdn.datacenter.medicalcenter.data.MDSHomelistInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.MedicalApplication;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.contacts.contact.manager.ContactManager;
import cn.redcdn.hvs.head.adapter.GalleryAdapter;
import cn.redcdn.hvs.head.adapter.HeadAdapter;
import cn.redcdn.hvs.head.adapter.MyAdapterTwo;
import cn.redcdn.hvs.head.javabean.OfficialAccountsBean;
import cn.redcdn.hvs.head.javabean.PicBean;
import cn.redcdn.hvs.head.manager.FullyLinearLayoutManager;
import cn.redcdn.hvs.head.view.CustomPopWindow;
import cn.redcdn.hvs.officialaccounts.activity.ArticleActivity;
import cn.redcdn.hvs.officialaccounts.activity.OfficialMainActivity;
import cn.redcdn.hvs.officialaccounts.activity.OrderActivity;
import cn.redcdn.hvs.officialaccounts.activity.RecommondActivity;
import cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity;
import cn.redcdn.hvs.util.CommonUtil;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.hvs.head.adapter.HeadAdapter.BIG_IMAGE_TYPE;
import static cn.redcdn.hvs.head.adapter.HeadAdapter.LITTLE_IMAGE_TYPE;
import static cn.redcdn.hvs.head.adapter.HeadAdapter.TITLE_TYPE;
import static cn.redcdn.hvs.officialaccounts.activity.VideoPublishActivity.INTENT_DATA_ARTICLE_ID;


/**
 * Created by Administrator on 2017/7/25.
 */

public class HeadFragment extends BaseFragment implements OnRefreshListener {
    public static final int NO_RE_STATECODE = 1;
    private View contentView = null;
    private RecyclerView recyclerview;
    private GalleryAdapter mAdapter;
    private List<OfficialAccountsBean> mDatas;
    private RecyclerView recyclerViewSecond;
    //    private TextView requestData;
    private List<MDSHomelistInfo> homeRecommendList;
    private List<MDSHomelistInfo> subList;
    private List<MDSHomelistInfo> homeOfficialAccountList;
    private List<MDSHomeCustomInfo> homeCustomList;
    private LinearLayout llmain;
    private TextView getDataFail;
    private Banner mBanner;
    private ArrayList<String> imageItems;
    private ArrayList<String> mTitles;
    private Button btn_more;
    private RelativeLayout more;
    private View pop_list_contentView;
    private CustomPopWindow mListPopWindow;
    RecyclerView recyclerViewOther;
    private MyAdapterTwo adapter;
    private HeadAdapter headAdapter;
    private boolean fail = false;
    private String selectNow = "";
    public static final String YIMEITI = "yimeiti";

    private List<PicBean> picBeen;
    private RefreshLayout swiper;


    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.head_fragment,
                container, false);
        pop_list_contentView = LayoutInflater.from(getContext()).inflate(R.layout.pop_list, null);
        mListPopWindow = new CustomPopWindow.PopupWindowBuilder(getActivity())
                .setView(pop_list_contentView)
                .setFocusable(true)
                .setOutsideTouchable(true)
                .create();
        recyclerViewOther = (RecyclerView) pop_list_contentView.findViewById(R.id.recyclerView);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewOther.setLayoutManager(manager);
//        recyclerViewOther.getBackground().setAlpha(200);
        adapter = new MyAdapterTwo();
        recyclerViewOther.setAdapter(adapter);
        adapter.setOnItemClickListener(new MyAdapterTwo.OnRecyclerViewItemClickListeer() {
            @Override
            public void onItemClick(View view, MainPageInfo data) {
                if (data.getDtName().equals(MedicalApplication.getContext().getString(R.string.medical_main))) {
                    if (selectNow.equals(YIMEITI)) {
                        if (mListPopWindow != null) {
                            mListPopWindow.dissmiss();
                        }
                        return;
                    }
                    selectNow = YIMEITI;
                    recyclerview.setVisibility(View.VISIBLE);
                    if (imageItems.size() > 0 && mTitles.size() > 0) {
                        mBanner.setOnBannerListener(new OnBannerListener() {
                            @Override
                            public void OnBannerClick(int position) {
                                int articleType;
                                final String id;
                                articleType = homeRecommendList.get(position).getArticleType();
                                id = homeRecommendList.get(position).getId();
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
                        mBanner.update(imageItems, mTitles);
                        mBanner.isAutoPlay(false);
                        //设置指示器位置（当banner模式中有指示器时）
                        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                        mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                    } else {
                        mBanner.setOnBannerListener(null);
                        List list = new ArrayList();
                        List list2 = new ArrayList();
                        list.add("");
                        list2.add("");
                        mBanner.update(list, list2);
                        mBanner.isAutoPlay(false);
                        //设置指示器位置（当banner模式中有指示器时）
                        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                        mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                    }
                    headAdapter.setData(picBeen);
                } else if (!selectNow.equals(data.getDtId())) {
                    selectNow = data.getDtId();
                    recyclerview.setVisibility(View.GONE);
                    final List<MDSHomelistInfo> homeRecommendListOther = data.getHomeRecommendList();
                    List<MDSHomeCustomInfo> homeCustomListOther = data.getHomeCustomList();
                    List<String> imagePathOther = new ArrayList<String>();
                    List<String> titileOther = new ArrayList<String>();
                    if (homeRecommendListOther != null && homeRecommendListOther.size() > 0) {
                        for (int i = 0; i < homeRecommendListOther.size(); i++) {
                            String showPic = homeRecommendListOther.get(i).getShowPic();
                            String showName = homeRecommendListOther.get(i).getShowName();
                            imagePathOther.add(showPic);
                            titileOther.add(showName);
                            mBanner.setOnBannerListener(new OnBannerListener() {
                                @Override
                                public void OnBannerClick(int position) {
                                    int articleType;
                                    final String id;
                                    articleType = homeRecommendListOther.get(position).getArticleType();
                                    id = homeRecommendListOther.get(position).getId();
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
                        }
                        mBanner.update(imagePathOther, titileOther);
                        mBanner.isAutoPlay(false);
                        //设置指示器位置（当banner模式中有指示器时）
                        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                        mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                    } else {
                        mBanner.setOnBannerListener(null);
                        List list = new ArrayList();
                        List list2 = new ArrayList();
                        list.add("");
                        list2.add("");
                        mBanner.update(list, list2);
                        mBanner.isAutoPlay(false);
                        //设置指示器位置（当banner模式中有指示器时）
                        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                        mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                    }

                    List<PicBean> picBeenOther = new ArrayList<>();
                    if (homeCustomListOther != null && homeCustomListOther.size() > 0) {
                        for (int i = 0; i < homeCustomListOther.size(); i++) {
                            MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomListOther.get(i);
                            picBeenOther.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                            List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                            for (int j = 0; j < articleList.size(); j++) {
                                MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                                picBeenOther.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                            }
                        }
                    }
                    headAdapter.setData(picBeenOther);
                }
                if (mListPopWindow != null) {
                    mListPopWindow.dissmiss();
                }
            }
        });
        getDataFail = (TextView) contentView.findViewById(R.id.getdata_fail);
        btn_more = (Button) contentView.findViewById(R.id.btn_more);
        more = (RelativeLayout) contentView.findViewById(R.id.more);
        btn_more.setOnClickListener(mbtnHandleEventListener);
        more.setOnClickListener(mbtnHandleEventListener);
        mBanner = (Banner) contentView.findViewById(R.id.banner);
        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        mBanner.setImageLoader(new GlideImageLoader());
        initWidget(contentView);
        SharedPreferences huancunSharedPreferences = MedicalApplication.getContext().getSharedPreferences("huancun_sharedpreferences", Context.MODE_PRIVATE);
        String huancun = huancunSharedPreferences.getString(AccountManager.getInstance(getActivity()).getNube(), "");
        if (!huancun.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(huancun);
                HPUHomeListInfo hpuHomeListInfo = HPUHomeListInfo.analyseInfo(jsonObject);
                setBenDi(hpuHomeListInfo);
                int netype = CommonUtil.GetNetype(getActivity());
                if (netype != -1) {
                    getdataFirst1();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            getdataFirst();
        }
        return contentView;
    }

    private void setBenDi(HPUHomeListInfo saveData) {
        getDataFail.setVisibility(View.GONE);
        llmain.setVisibility(View.VISIBLE);
        swiper.finishRefresh();
        AccountManager.getInstance(getActivity()).setHpuHomeListInfo(saveData);
        if (AccountManager.getInstance(getActivity()).getHpuHomeListInfo() != null) {
            homeRecommendList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeRecommendList();
            imageItems = new ArrayList<>();
            mTitles = new ArrayList<>();
            if (homeRecommendList != null && homeRecommendList.size() > 0) {
                for (int i = 0; i < homeRecommendList.size(); i++) {
                    imageItems.add(homeRecommendList.get(i).getShowPic());
                    mTitles.add(homeRecommendList.get(i).getShowName());
                }
            }
            homeOfficialAccountList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeOfficialAccountList();
            if (homeOfficialAccountList != null && homeOfficialAccountList.size() > 0) {
                mDatas = new ArrayList<>();
                for (int i = 0; i < homeOfficialAccountList.size(); i++) {
                    mDatas.add(new OfficialAccountsBean(homeOfficialAccountList.get(i).getShowPic(), homeOfficialAccountList.get(i).getShowName(), homeOfficialAccountList.get(i).getId()));
                }
                mDatas.add(new OfficialAccountsBean("", getActivity().getString(R.string.suscribe)));
                mDatas.add(new OfficialAccountsBean("", getActivity().getString(R.string.all)));
                mAdapter.setData(mDatas);
            }
            homeCustomList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeCustomList();
            if (homeCustomList != null && homeCustomList.size() > 0) {
                picBeen = new ArrayList<>();
                for (int i = 0; i < homeCustomList.size(); i++) {
                    MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomList.get(i);
                    picBeen.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                    List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                    for (int j = 0; j < articleList.size(); j++) {
                        MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                        picBeen.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                    }
                }
            }
        }

        int dtFlag = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getDtFlag();

        if (1 == dtFlag) {
            AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.NORELATION_STATE);
            recyclerview.setVisibility(View.VISIBLE);
            more.setVisibility(View.GONE);
            btn_more.setVisibility(View.GONE);
            if (imageItems.size() > 0 && mTitles.size() > 0) {
                mBanner.setImages(imageItems);
                mBanner.setBannerTitles(mTitles);
            }
            mBanner.setOnBannerListener(new OnBannerListener() {
                @Override
                public void OnBannerClick(int position) {
                    int articleType;
                    final String id;
                    articleType = homeRecommendList.get(position).getArticleType();
                    id = homeRecommendList.get(position).getId();
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
            mBanner.isAutoPlay(false);
            mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
            mBanner.setIndicatorGravity(BannerConfig.RIGHT);
            mBanner.start();
            headAdapter.setData(picBeen);
        } else if (2 == dtFlag) {
            AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.RELATION_STATE);
            more.setVisibility(View.VISIBLE);
            btn_more.setVisibility(View.VISIBLE);
            List<MainPageInfo> pageInfos = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getPageInfos();
            if (pageInfos != null && pageInfos.size() > 0) {
                MainPageInfo mainPageInfoFirst = pageInfos.get(0);
                MainPageInfo mainPageInfoMedicalMain = new MainPageInfo();
                mainPageInfoMedicalMain.setDtName(MedicalApplication.getContext().getString(R.string.medical_main));
                pageInfos.add(0, mainPageInfoMedicalMain);
                adapter.setData(pageInfos);
                final List<MDSHomelistInfo> homeRecommendListMy = mainPageInfoFirst.getHomeRecommendList();
                List<String> titielList = new ArrayList<>();
                List<String> imageStringList = new ArrayList<>();
                if (homeRecommendListMy != null && homeRecommendListMy.size() > 0) {
                    for (int i = 0; i < homeRecommendListMy.size(); i++) {
                        imageStringList.add(homeRecommendListMy.get(i).getShowPic());
                        titielList.add(homeRecommendListMy.get(i).getShowName());
                    }
                    mBanner.setImages(imageStringList);
                    mBanner.setBannerTitles(titielList);
                    mBanner.setOnBannerListener(new OnBannerListener() {
                        @Override
                        public void OnBannerClick(int position) {
                            int articleType;
                            final String id;
                            articleType = homeRecommendListMy.get(position).getArticleType();
                            id = homeRecommendListMy.get(position).getId();
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
                } else {
                    mBanner.setOnBannerListener(null);
                    List list = new ArrayList();
                    List list2 = new ArrayList();
                    list.add("");
                    list2.add("");
                    mBanner.setImages(list);
                    mBanner.setBannerTitles(list2);
                }

                //设置自动轮播，默认为true
                mBanner.isAutoPlay(false);
                //设置指示器位置（当banner模式中有指示器时）
                mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                //banner设置方法全部调用完毕时最后调用
                mBanner.start();

                List<MDSHomeCustomInfo> homeCustomListMy = mainPageInfoFirst.getHomeCustomList();


                if (homeCustomListMy != null && homeCustomListMy.size() > 0) {
                    List<PicBean> picBeenMy = new ArrayList<>();
                    for (int i = 0; i < homeCustomListMy.size(); i++) {
                        MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomListMy.get(i);
                        picBeenMy.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                        List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                        for (int j = 0; j < articleList.size(); j++) {
                            MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                            picBeenMy.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                        }
                    }
                    headAdapter.setData(picBeenMy);
                }
            }
        }
    }

    @Override
    public void todoClick(int id) {
        super.todoClick(id);
        switch (id) {
            case R.id.more:
                showDialog();
                break;
            case R.id.btn_more:
                showDialog();
                break;
            default:
                break;
        }
    }

    private void showDialog() {
        mListPopWindow.showAsDropDown(btn_more, -(int) MedicalApplication.getContext().getResources().getDimension(R.dimen.x265), (int) MedicalApplication.getContext().getResources().getDimension(R.dimen.y10));
    }

    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        getdata();
    }


    public class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            //Glide 加载图片简单用法
            Glide.with(MedicalApplication.getContext()).load(path).skipMemoryCache(true) .dontAnimate().diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(R.drawable.hillbackground).error(R.drawable.hillbackground).into(imageView);
        }
    }

    private void getdataFirst1() {
        HPUGetMainPageInfo hpuGetMainPageInfo = new HPUGetMainPageInfo() {
            @Override
            protected void onSuccess(HPUHomeListInfo responseContent) {
                super.onSuccess(responseContent);
                SharedPreferences huancunSharedPreferences = MedicalApplication.getContext().getSharedPreferences("huancun_sharedpreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor s1 = huancunSharedPreferences.edit();
                s1.putString(AccountManager.getInstance(getActivity()).getNube(), responseContent.getMainJson().toString());
                s1.commit();
                swiper.finishRefresh();
                AccountManager.getInstance(getActivity()).setHpuHomeListInfo(responseContent);
                getDataFail.setVisibility(View.GONE);
                llmain.setVisibility(View.VISIBLE);
                if (AccountManager.getInstance(getActivity()).getHpuHomeListInfo() != null) {
                    homeRecommendList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeRecommendList();
                    imageItems = new ArrayList<>();
                    mTitles = new ArrayList<>();
                    if (homeRecommendList != null && homeRecommendList.size() > 0) {
                        for (int i = 0; i < homeRecommendList.size(); i++) {
                            imageItems.add(homeRecommendList.get(i).getShowPic());
                            mTitles.add(homeRecommendList.get(i).getShowName());
                        }
                    }
                    homeOfficialAccountList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeOfficialAccountList();
                    if (homeOfficialAccountList != null && homeOfficialAccountList.size() > 0) {
                        mDatas = new ArrayList<>();
                        for (int i = 0; i < homeOfficialAccountList.size(); i++) {
                            mDatas.add(new OfficialAccountsBean(homeOfficialAccountList.get(i).getShowPic(), homeOfficialAccountList.get(i).getShowName(), homeOfficialAccountList.get(i).getId()));
                        }
                        mDatas.add(new OfficialAccountsBean("", MedicalApplication.getContext().getString(R.string.suscribe)));
                        mDatas.add(new OfficialAccountsBean("", getActivity().getString(R.string.all)));
                        mAdapter.setData(mDatas);
                    }
                    homeCustomList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeCustomList();
                    if (homeCustomList != null && homeCustomList.size() > 0) {
                        picBeen = new ArrayList<>();
                        for (int i = 0; i < homeCustomList.size(); i++) {
                            MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomList.get(i);
                            picBeen.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                            List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                            for (int j = 0; j < articleList.size(); j++) {
                                MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                                picBeen.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                            }
                        }
                    }
                }

                int dtFlag = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getDtFlag();
                if (1 == dtFlag) {
                    selectNow = YIMEITI;
                    AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.NORELATION_STATE);
                    recyclerview.setVisibility(View.VISIBLE);
                    more.setVisibility(View.GONE);
                    btn_more.setVisibility(View.GONE);
                    if (imageItems.size() > 0 && mTitles.size() > 0) {
                        mBanner.update(imageItems, mTitles);
                    }
                    mBanner.setOnBannerListener(new OnBannerListener() {
                        @Override
                        public void OnBannerClick(int position) {
                            int articleType;
                            final String id;
                            articleType = homeRecommendList.get(position).getArticleType();
                            id = homeRecommendList.get(position).getId();
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
                    mBanner.isAutoPlay(false);
                    mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                    mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                    headAdapter.setData(picBeen);
                } else if (2 == dtFlag) {
                    AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.RELATION_STATE);
                    more.setVisibility(View.VISIBLE);
                    btn_more.setVisibility(View.VISIBLE);
                    List<MainPageInfo> pageInfos = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getPageInfos();
                    if (pageInfos != null && pageInfos.size() > 0) {
                        MainPageInfo mainPageInfoFirst = pageInfos.get(0);
                        selectNow = mainPageInfoFirst.dtId;
                        MainPageInfo mainPageInfoMedicalMain = new MainPageInfo();
                        mainPageInfoMedicalMain.setDtName(MedicalApplication.getContext().getString(R.string.medical_main));
                        pageInfos.add(0, mainPageInfoMedicalMain);
                        adapter.setData(pageInfos);
                        final List<MDSHomelistInfo> homeRecommendListMy = mainPageInfoFirst.getHomeRecommendList();
                        List<String> titielList = new ArrayList<>();
                        List<String> imageStringList = new ArrayList<>();
                        if (homeRecommendListMy != null && homeRecommendListMy.size() > 0) {
                            for (int i = 0; i < homeRecommendListMy.size(); i++) {
                                imageStringList.add(homeRecommendListMy.get(i).getShowPic());
                                titielList.add(homeRecommendListMy.get(i).getShowName());
                            }
                            mBanner.update(imageStringList, titielList);
                            mBanner.setOnBannerListener(new OnBannerListener() {
                                @Override
                                public void OnBannerClick(int position) {
                                    int articleType;
                                    final String id;
                                    articleType = homeRecommendListMy.get(position).getArticleType();
                                    id = homeRecommendListMy.get(position).getId();
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
                        } else {
                            mBanner.setOnBannerListener(null);
                            List list = new ArrayList();
                            List list2 = new ArrayList();
                            list.add("");
                            list2.add("");
                            mBanner.setImages(list);
                            mBanner.setBannerTitles(list2);
                        }

                        //设置自动轮播，默认为true
                        mBanner.isAutoPlay(false);
                        //设置指示器位置（当banner模式中有指示器时）
                        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                        mBanner.setIndicatorGravity(BannerConfig.RIGHT);

                        List<MDSHomeCustomInfo> homeCustomListMy = mainPageInfoFirst.getHomeCustomList();


                        if (homeCustomListMy != null && homeCustomListMy.size() > 0) {
                            List<PicBean> picBeenMy = new ArrayList<>();
                            for (int i = 0; i < homeCustomListMy.size(); i++) {
                                MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomListMy.get(i);
                                picBeenMy.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                                List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                                for (int j = 0; j < articleList.size(); j++) {
                                    MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                                    picBeenMy.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                                }
                            }
                            headAdapter.setData(picBeenMy);
                        }
                    }
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                getDataFail.setText(MedicalApplication.getContext().getString(R.string.jiazai_fail));
                fail = true;
                swiper.finishRefresh(false);
                CustomLog.e("HeadFragment", "第1次请求首页数据失败|statuscode=" + statusCode + "|statusInfo=" + statusInfo);
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                }
            }
        };
        hpuGetMainPageInfo.getmainInfo(AccountManager.getInstance(getActivity()).getMdsToken()
                , "", "", ContactManager.getInstance(getActivity()).getDTListFromData().size());
        CustomLog.i("HeadFragment", "第1次请求首页数据getmainInfo|token=" + AccountManager.getInstance(getActivity()).getMdsToken() );
    }

    private void getdataFirst() {
        HPUGetMainPageInfo hpuGetMainPageInfo = new HPUGetMainPageInfo() {
            @Override
            protected void onSuccess(HPUHomeListInfo responseContent) {
                super.onSuccess(responseContent);
                SharedPreferences huancunSharedPreferences = MedicalApplication.getContext().getSharedPreferences("huancun_sharedpreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor s1 = huancunSharedPreferences.edit();
                s1.putString(AccountManager.getInstance(getActivity()).getNube(), responseContent.getMainJson().toString());
                s1.commit();
                swiper.finishRefresh();
                CustomLog.i("HeadFragment", "第1次请求首页数据成功");
                AccountManager.getInstance(getActivity()).setHpuHomeListInfo(responseContent);
                getDataFail.setVisibility(View.GONE);
                llmain.setVisibility(View.VISIBLE);
                if (AccountManager.getInstance(getActivity()).getHpuHomeListInfo() != null) {
                    homeRecommendList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeRecommendList();
                    imageItems = new ArrayList<>();
                    mTitles = new ArrayList<>();
                    if (homeRecommendList != null && homeRecommendList.size() > 0) {
                        for (int i = 0; i < homeRecommendList.size(); i++) {
                            imageItems.add(homeRecommendList.get(i).getShowPic());
                            mTitles.add(homeRecommendList.get(i).getShowName());
                        }
                    }
                    homeOfficialAccountList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeOfficialAccountList();
                    if (homeOfficialAccountList != null && homeOfficialAccountList.size() > 0) {
                        mDatas = new ArrayList<>();
                        for (int i = 0; i < homeOfficialAccountList.size(); i++) {
                            mDatas.add(new OfficialAccountsBean(homeOfficialAccountList.get(i).getShowPic(), homeOfficialAccountList.get(i).getShowName(), homeOfficialAccountList.get(i).getId()));
                        }
                        mDatas.add(new OfficialAccountsBean("", getActivity().getString(R.string.suscribe)));
                        mDatas.add(new OfficialAccountsBean("", getActivity().getString(R.string.all)));
                        mAdapter.setData(mDatas);
                    }
                    homeCustomList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeCustomList();
                    if (homeCustomList != null && homeCustomList.size() > 0) {
                        picBeen = new ArrayList<>();
                        for (int i = 0; i < homeCustomList.size(); i++) {
                            MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomList.get(i);
                            picBeen.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                            List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                            for (int j = 0; j < articleList.size(); j++) {
                                MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                                picBeen.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                            }
                        }
                    }
                }

                int dtFlag = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getDtFlag();
                if (1 == dtFlag) {
                    selectNow = YIMEITI;
                    AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.NORELATION_STATE);
                    recyclerview.setVisibility(View.VISIBLE);
                    more.setVisibility(View.GONE);
                    btn_more.setVisibility(View.GONE);
                    if (imageItems.size() > 0 && mTitles.size() > 0) {
                        mBanner.setImages(imageItems);
                        mBanner.setBannerTitles(mTitles);
                    }
                    mBanner.setOnBannerListener(new OnBannerListener() {
                        @Override
                        public void OnBannerClick(int position) {
                            int articleType;
                            final String id;
                            articleType = homeRecommendList.get(position).getArticleType();
                            id = homeRecommendList.get(position).getId();
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
                    mBanner.isAutoPlay(false);
                    mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                    mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                    mBanner.start();
                    headAdapter.setData(picBeen);
                } else if (2 == dtFlag) {
                    AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.RELATION_STATE);
                    more.setVisibility(View.VISIBLE);
                    btn_more.setVisibility(View.VISIBLE);
                    List<MainPageInfo> pageInfos = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getPageInfos();
                    if (pageInfos != null && pageInfos.size() > 0) {
                        MainPageInfo mainPageInfoFirst = pageInfos.get(0);
                        selectNow = mainPageInfoFirst.dtId;
                        MainPageInfo mainPageInfoMedicalMain = new MainPageInfo();
                        mainPageInfoMedicalMain.setDtName(MedicalApplication.getContext().getString(R.string.medical_main));
                        pageInfos.add(0, mainPageInfoMedicalMain);
                        adapter.setData(pageInfos);
                        final List<MDSHomelistInfo> homeRecommendListMy = mainPageInfoFirst.getHomeRecommendList();
                        List<String> titielList = new ArrayList<>();
                        List<String> imageStringList = new ArrayList<>();
                        if (homeRecommendListMy != null && homeRecommendListMy.size() > 0) {
                            for (int i = 0; i < homeRecommendListMy.size(); i++) {
                                imageStringList.add(homeRecommendListMy.get(i).getShowPic());
                                titielList.add(homeRecommendListMy.get(i).getShowName());
                            }
                            mBanner.setImages(imageStringList);
                            mBanner.setBannerTitles(titielList);
                            mBanner.setOnBannerListener(new OnBannerListener() {
                                @Override
                                public void OnBannerClick(int position) {
                                    int articleType;
                                    final String id;
                                    articleType = homeRecommendListMy.get(position).getArticleType();
                                    id = homeRecommendListMy.get(position).getId();
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
                        } else {
                            mBanner.setOnBannerListener(null);
                            List list = new ArrayList();
                            List list2 = new ArrayList();
                            list.add("");
                            list2.add("");
                            mBanner.setImages(list);
                            mBanner.setBannerTitles(list2);
                        }

                        //设置自动轮播，默认为true
                        mBanner.isAutoPlay(false);
                        //设置指示器位置（当banner模式中有指示器时）
                        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                        mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                        //banner设置方法全部调用完毕时最后调用
                        mBanner.start();

                        List<MDSHomeCustomInfo> homeCustomListMy = mainPageInfoFirst.getHomeCustomList();


                        if (homeCustomListMy != null && homeCustomListMy.size() > 0) {
                            List<PicBean> picBeenMy = new ArrayList<>();
                            for (int i = 0; i < homeCustomListMy.size(); i++) {
                                MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomListMy.get(i);
                                picBeenMy.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                                List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                                for (int j = 0; j < articleList.size(); j++) {
                                    MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                                    picBeenMy.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                                }
                            }
                            headAdapter.setData(picBeenMy);
                        }
                    }
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                getDataFail.setText(MedicalApplication.getContext().getString(R.string.jiazai_fail));
                getDataFail.setVisibility(View.VISIBLE);
                fail = true;
                swiper.finishRefresh(false);
//                more.setVisibility(View.GONE);
//                btn_more.setVisibility(View.GONE);
                CustomLog.e("HeadFragment", "第1次请求首页数据失败|statuscode=" + statusCode + "|statusInfo=" + statusInfo);
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                }
            }
        };
        hpuGetMainPageInfo.getmainInfo(AccountManager.getInstance(getActivity()).getMdsToken()
                , "", "", ContactManager.getInstance(getActivity()).getDTListFromData().size());
        getDataFail.setVisibility(View.VISIBLE);
    }


    private void getdata() {
        HPUGetMainPageInfo hpuGetMainPageInfo = new HPUGetMainPageInfo() {
            @Override
            protected void onSuccess(HPUHomeListInfo responseContent) {
                super.onSuccess(responseContent);
                AccountManager.getInstance(getActivity()).setNeedRefresh(false);
                SharedPreferences huancunSharedPreferences = MedicalApplication.getContext().getSharedPreferences("huancun_sharedpreferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor s1 = huancunSharedPreferences.edit();
                s1.putString(AccountManager.getInstance(getActivity()).getNube(), responseContent.getMainJson().toString());
                s1.commit();
                swiper.finishRefresh();
                CustomLog.i("HeadFragment", "第刷新首页数据成功");
                HPUHomeListInfo hpuHomeListInfo = AccountManager.getInstance(getActivity()).getHpuHomeListInfo();
                getDataFail.setVisibility(View.GONE);
                llmain.setVisibility(View.VISIBLE);
                if (hpuHomeListInfo == null) {
                    AccountManager.getInstance(getActivity()).setHpuHomeListInfo(responseContent);
                } else {
                    if (responseContent.getPageInfos().size() > 0) {
                        AccountManager.getInstance(getActivity()).setHpuHomeListInfo(responseContent);
                    } else {
                        AccountManager.getInstance(getActivity()).getHpuHomeListInfo().dtFlag = responseContent.dtFlag;
                        AccountManager.getInstance(getActivity()).getHpuHomeListInfo().homeRecommendList = responseContent.homeRecommendList;
                        AccountManager.getInstance(getActivity()).getHpuHomeListInfo().homeOfficialAccountList = responseContent.homeOfficialAccountList;
                        AccountManager.getInstance(getActivity()).getHpuHomeListInfo().homeCustomList = responseContent.homeCustomList;
                    }
                }
                homeRecommendList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().homeRecommendList;
                imageItems = new ArrayList<>();
                mTitles = new ArrayList<>();
                if (homeRecommendList != null && homeRecommendList.size() > 0) {
                    for (int i = 0; i < homeRecommendList.size(); i++) {
                        imageItems.add(homeRecommendList.get(i).getShowPic());
                        mTitles.add(homeRecommendList.get(i).getShowName());
                    }
                }

                homeOfficialAccountList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeOfficialAccountList();

                if (homeOfficialAccountList != null && homeOfficialAccountList.size() > 0) {
                    mDatas = new ArrayList<>();
                    for (int i = 0; i < homeOfficialAccountList.size(); i++) {
                        mDatas.add(new OfficialAccountsBean(homeOfficialAccountList.get(i).getShowPic(), homeOfficialAccountList.get(i).getShowName(), homeOfficialAccountList.get(i).getId()));
                    }
                    mDatas.add(new OfficialAccountsBean("", MedicalApplication.getContext().getString(R.string.suscribe)));
                    mDatas.add(new OfficialAccountsBean("", getActivity().getString(R.string.all)));
                    mAdapter.setData(mDatas);
                }

                homeCustomList = AccountManager.getInstance(getActivity()).getHpuHomeListInfo().getHomeCustomList();
                picBeen = new ArrayList<>();
                for (int i = 0; i < homeCustomList.size(); i++) {
                    MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomList.get(i);
                    picBeen.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                    List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                    for (int j = 0; j < articleList.size(); j++) {
                        MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                        picBeen.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                    }
                }

                int dtFlag = responseContent.getDtFlag();



                if (1 == dtFlag) {
                    selectNow=YIMEITI;
                    AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.NORELATION_STATE);
                    recyclerview.setVisibility(View.VISIBLE);
                    more.setVisibility(View.GONE);
                    btn_more.setVisibility(View.GONE);
                    if (imageItems.size() > 0 && mTitles.size() > 0) {
                        if (fail) {
                            mBanner.setImages(imageItems);
                            mBanner.setBannerTitles(mTitles);
                            mBanner.setOnBannerListener(new OnBannerListener() {
                                @Override
                                public void OnBannerClick(int position) {
                                    int articleType;
                                    final String id;
                                    articleType = homeRecommendList.get(position).getArticleType();
                                    id = homeRecommendList.get(position).getId();
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
                            mBanner.isAutoPlay(false);
                            //设置指示器位置（当banner模式中有指示器时）
                            mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                            mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                            mBanner.start();
                        } else {
                            mBanner.update(imageItems, mTitles);
                            mBanner.setOnBannerListener(new OnBannerListener() {
                                @Override
                                public void OnBannerClick(int position) {
                                    int articleType;
                                    final String id;
                                    articleType = homeRecommendList.get(position).getArticleType();
                                    id = homeRecommendList.get(position).getId();
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
                        }
                    } else {
                        mBanner.setOnBannerListener(null);
                        List list = new ArrayList();
                        List list2 = new ArrayList();
                        list.add("");
                        list2.add("");
                        mBanner.update(list, list2);
                    }
                    headAdapter.setData(picBeen);
                } else if (2 == dtFlag) {
                    more.setVisibility(View.VISIBLE);
                    btn_more.setVisibility(View.VISIBLE);
                    MainPageInfo mainPageInfoFirst=new MainPageInfo();
                    AccountManager.getInstance(getActivity()).setRelationState(AccountManager.RelationState.RELATION_STATE);
                    List<MainPageInfo> pageInfos = responseContent.getPageInfos();
                    if (pageInfos != null && pageInfos.size() > 0) {
                        if (selectNow.equals(YIMEITI)){
                            MainPageInfo mainPageInfoMedicalMain = new MainPageInfo();
                            mainPageInfoMedicalMain.setDtName(MedicalApplication.getContext().getString(R.string.medical_main));
                            pageInfos.add(0, mainPageInfoMedicalMain);
                            adapter.setData(pageInfos,0);
                        }else {
                            List<String> mList=new ArrayList<>();
                            for (MainPageInfo pageInfo : pageInfos) {
                                mList.add(pageInfo.getDtId());
                            }
                            if (mList.contains(selectNow)){
                                mainPageInfoFirst=pageInfos.get(mList.indexOf(selectNow));
                                MainPageInfo mainPageInfoMedicalMain = new MainPageInfo();
                                mainPageInfoMedicalMain.setDtName(MedicalApplication.getContext().getString(R.string.medical_main));
                                pageInfos.add(0, mainPageInfoMedicalMain);
                                adapter.setData(pageInfos,pageInfos.indexOf(mainPageInfoFirst));
                            }else {
                               mainPageInfoFirst = pageInfos.get(0);
                                selectNow=mainPageInfoFirst.dtId;
                                MainPageInfo mainPageInfoMedicalMain = new MainPageInfo();
                                mainPageInfoMedicalMain.setDtName(MedicalApplication.getContext().getString(R.string.medical_main));
                                pageInfos.add(0, mainPageInfoMedicalMain);
                                adapter.setData(pageInfos);
                            }
                            final List<MDSHomelistInfo> homeRecommendListMy = mainPageInfoFirst.getHomeRecommendList();
                            List<String> titielList = new ArrayList<>();
                            List<String> imageStringList = new ArrayList<>();
                            if (homeRecommendListMy != null && homeRecommendListMy.size() > 0) {
                                for (int i = 0; i < homeRecommendListMy.size(); i++) {
                                    imageStringList.add(homeRecommendListMy.get(i).getShowPic());
                                    titielList.add(homeRecommendListMy.get(i).getShowName());
                                }
                                if (fail) {
                                    mBanner.setImages(imageStringList);
                                    mBanner.setBannerTitles(titielList);
                                    mBanner.setOnBannerListener(new OnBannerListener() {
                                        @Override
                                        public void OnBannerClick(int position) {
                                            int articleType;
                                            final String id;
                                            articleType = homeRecommendListMy.get(position).getArticleType();
                                            id = homeRecommendListMy.get(position).getId();
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
                                    mBanner.isAutoPlay(false);
                                    //设置指示器位置（当banner模式中有指示器时）
                                    mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE_INSIDE);
                                    mBanner.setIndicatorGravity(BannerConfig.RIGHT);
                                    mBanner.start();
                                } else {
                                    mBanner.update(imageStringList, titielList);
                                    mBanner.setOnBannerListener(new OnBannerListener() {
                                        @Override
                                        public void OnBannerClick(int position) {
                                            int articleType;
                                            final String id;
                                            articleType = homeRecommendListMy.get(position).getArticleType();
                                            id = homeRecommendListMy.get(position).getId();
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
                                }
                            } else {
                                mBanner.setOnBannerListener(null);
                                List list = new ArrayList();
                                List list2 = new ArrayList();
                                list.add("");
                                list2.add("");
                                mBanner.update(list, list2);
                            }

                            List<MDSHomeCustomInfo> homeCustomListMy = mainPageInfoFirst.getHomeCustomList();
                            List<PicBean> picBeenMy = new ArrayList<>();
                            if (homeCustomListMy != null && homeCustomListMy.size() > 0) {
                                for (int i = 0; i < homeCustomListMy.size(); i++) {
                                    MDSHomeCustomInfo mdsHomeCustomInfo = homeCustomListMy.get(i);
                                    picBeenMy.add(new PicBean(mdsHomeCustomInfo.getCustomName(), TITLE_TYPE, mdsHomeCustomInfo.getOfficalId()));
                                    List<MDSHomelistInfo> articleList = mdsHomeCustomInfo.getArticleList();
                                    for (int j = 0; j < articleList.size(); j++) {
                                        MDSHomelistInfo mdsHomelistInfo = articleList.get(j);
                                        picBeenMy.add(new PicBean(mdsHomelistInfo.getShowPic(), mdsHomelistInfo.getShowPicType(), mdsHomelistInfo.getShowName(), mdsHomelistInfo.getId(), mdsHomelistInfo.getArticleType()));
                                    }
                                }
                            }
                            headAdapter.setData(picBeenMy);
                        }
                    }
                }
                fail = false;
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                getDataFail.setText(MedicalApplication.getContext().getString(R.string.jiazai_fail));
                swiper.finishRefresh(false);
                CustomLog.i("HeadFragment", "第2次请求首页数据失败|statuscode=" + statusCode + "|statusInfo=" + statusInfo);
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                }
            }
        };
        hpuGetMainPageInfo.getmainInfo(AccountManager.getInstance(getActivity()).getMdsToken(), "", "", ContactManager.getInstance(getActivity()).getDTListFromData().size());
        CustomLog.i("HeadFragment", "第2次请求首页数据getmainInfo|token=" + AccountManager.getInstance(getActivity()).getMdsToken()  );
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initWidget(View view) {
        swiper = (RefreshLayout) view.findViewById(R.id.swipe_refresh);
        swiper.setOnRefreshListener(this);
        llmain = (LinearLayout) view.findViewById(R.id.ll_main);
        recyclerview = (RecyclerView) view.findViewById(R.id.recyclerview);
        FullyLinearLayoutManager linearLayoutManager = new FullyLinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //设置布局管理器
        recyclerview.setLayoutManager(linearLayoutManager);
        mAdapter = new GalleryAdapter(getActivity());
        recyclerview.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new GalleryAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, OfficialAccountsBean data) {
                if (data.getInformation().contains(getActivity().getString(R.string.all))) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), RecommondActivity.class);
                    startActivity(intent);
                } else if (data.getInformation().contains(getActivity().getString(R.string.suscribe))) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), OrderActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent_public = new Intent(getActivity(), OfficialMainActivity.class);
                    intent_public.putExtra("officialAccountId", data.getOffaccId());
                    intent_public.putExtra("officialName", data.getInformation());
                    startActivity(intent_public);
                }
            }
        });
        recyclerViewSecond = (RecyclerView) view.findViewById(R.id.recyclerView_second);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2, GridLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerViewSecond.setLayoutManager(gridLayoutManager);
        headAdapter = new HeadAdapter(getActivity());
        recyclerViewSecond.setAdapter(headAdapter);
        headAdapter.setOnItemClickListener(new HeadAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, PicBean data) {
                switch (data.getType()) {
                    case BIG_IMAGE_TYPE:
                        if (data.getArticleId().equals("")) {
                            return;
                        }
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
                        if (data.getOfficialId().equals("")) {
                            return;
                        }
                        Intent intentOffical = new Intent(getActivity(), OfficialMainActivity.class);
                        intentOffical.putExtra("officialAccountId", data.getOfficialId());
                        startActivity(intentOffical);
                        break;
                    case LITTLE_IMAGE_TYPE:
                        if (data.getArticleId().equals("")) {
                            return;
                        }
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
    protected void setListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            boolean needRefresh = AccountManager.getInstance(getActivity()).isNeedRefresh();
            CustomLog.d("HeadFragment", "数据刷新参数needRefresh=" + needRefresh);
            if (needRefresh) {
                getdata();
            }
        } else if (!isVisibleToUser) {

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
