package cn.redcdn.hvs.officialaccounts.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.offaccscenter.MDSAppGetFocusOffAccArticles;
import cn.redcdn.datacenter.offaccscenter.MDSAppGetSubscribeOffAccs;
import cn.redcdn.datacenter.offaccscenter.data.MDSFocusPageInfo;
import cn.redcdn.datacenter.offaccscenter.data.MDSfocusOffAccArtcleInfo;
import cn.redcdn.datacenter.offaccscenter.data.OffAccdetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.head.adapter.GalleryAdapter;
import cn.redcdn.hvs.head.javabean.OfficialAccountsBean;
import cn.redcdn.hvs.head.manager.FullyLinearLayoutManager;
import cn.redcdn.hvs.officialaccounts.DingYueActivity;
import cn.redcdn.hvs.officialaccounts.adapter.OrderFragmentRecyAdapter;
import cn.redcdn.hvs.officialaccounts.fragment.OrderFragment;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Created by Administrator on 2017/7/28.
 */

public class OrderActivity extends BaseActivity {
    private static final String TAG = OrderFragment.class.getName();
    private XRecyclerView order_content_Recy;
    public OrderFragmentRecyAdapter recyAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    public List<MDSfocusOffAccArtcleInfo> focusList;
    private int totalSize;
    private final static int PAGE_ITEM_SIZE = 10;
    private View view;
    private MDSAppGetFocusOffAccArticles mMdsGetFocOffAccArts;
    private LinearLayout no_content;
    private TextView requestData;
    private RecyclerView recyclerview;
    private GalleryAdapter mAdapter;
    private List<OfficialAccountsBean> mDatas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_fragment);

        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(R.string.officialaccounttitle);
        recyclerview=new RecyclerView(OrderActivity.this);
        int pxFor20dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        int pxFor15dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, pxFor20dp,0, pxFor15dp);
        recyclerview.setLayoutParams(layoutParams);
//        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
       FullyLinearLayoutManager linearLayoutManager = new FullyLinearLayoutManager(OrderActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerview.setNestedScrollingEnabled(false);
        //设置布局管理器
        recyclerview.setLayoutManager(linearLayoutManager);
        order_content_Recy = (XRecyclerView) findViewById(R.id.order_content_Recy);
        no_content = (LinearLayout) findViewById(R.id.no_content);
        requestData = (TextView) findViewById(R.id.requestData);
        initView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        findOfficial();
        findData(false);
    }

    private void findOfficial() {
        MDSAppGetSubscribeOffAccs mdsAppGetSubscribeOffAccs=new MDSAppGetSubscribeOffAccs() {
            @Override
            protected void onSuccess(List<OffAccdetailInfo> responseContent) {
                super.onSuccess(responseContent);
                if (responseContent!=null){
                    if (responseContent.size()>0){
                        for (int i = 0; i < responseContent.size(); i++) {
                            OffAccdetailInfo offAccdetailInfo = responseContent.get(i);
                            OfficialAccountsBean officialAccountsBean = new OfficialAccountsBean(offAccdetailInfo.getLogoUrl(), offAccdetailInfo.getName(), offAccdetailInfo.getId());
                            mDatas.add(officialAccountsBean);
                            //设置适配器
                            mAdapter = new GalleryAdapter(OrderActivity.this);
                            mAdapter.setData(mDatas);
                            recyclerview.setAdapter(mAdapter);
                            mAdapter.setOnItemClickListener(new GalleryAdapter.OnRecyclerViewItemClickListener() {
                                @Override
                                public void onItemClick(View view, OfficialAccountsBean data) {
                                    Intent intent_card = new Intent();
                                    intent_card.setClass(OrderActivity.this, DingYueActivity.class);
                                    intent_card.putExtra("officialAccountId", data.getOffaccId());
                                    intent_card.putExtra("officialName", data.getInformation());
                                    startActivity(intent_card);
                                }
                            });
                        }
                    }else {
                        mDatas.clear();
                        //设置适配器
                        mAdapter = new GalleryAdapter(OrderActivity.this);
                        mAdapter.setData(mDatas);
                        recyclerview.setAdapter(mAdapter);
                    }
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(OrderActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(OrderActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
            }
        };
        mdsAppGetSubscribeOffAccs.appGetSubscribeOffAccs(AccountManager.getInstance(OrderActivity.this).getMdsToken());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatas.clear();
    }

    private void setListener() {
        mLinearLayoutManager = new LinearLayoutManager(OrderActivity.this);
        order_content_Recy.setLayoutManager(mLinearLayoutManager);
        order_content_Recy.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        order_content_Recy.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        order_content_Recy.setArrowImageView(R.drawable.iconfont_downgrey);
        focusList = new ArrayList<MDSfocusOffAccArtcleInfo>();
        recyAdapter = new OrderFragmentRecyAdapter(focusList, OrderActivity.this, order_content_Recy);
        order_content_Recy.setAdapter(recyAdapter);
        order_content_Recy.addHeaderView(recyclerview);
    }

    private void initView() {
        order_content_Recy.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomLog.d(TAG, "OrderFragment:: 触发下拉刷新");
                        order_content_Recy.setLoadingMoreEnabled(false);
                        if (mMdsGetFocOffAccArts == null) {
                            findData(false);
                        } else {
                            CustomLog.d(TAG, "OrderFragment::onRefresh() 获取中");
                        }
                    }
                },1000);
            }

            @Override
            public void onLoadMore() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomLog.d(TAG, "OrderFragment:: 触发加载更多");
                        if (mMdsGetFocOffAccArts == null) {
                            recyAdapter.notifyDataSetChanged();
                            loadMoreData();
                        } else {
                            CustomLog.d(TAG, "OrderFragment:: 数据获取中");
                        }
                    }
                }, 1000);
            }
        });
    }

    //计算pageNumber,触发加载数据,如果当前显示条目小于一页显示值，则继续请求第一页；
    private void loadMoreData() {
        findData(true);
    }


    /**
     * 获取加载数据
     *
     * @param isAppend 是否扩展。 true: 请求到的数据进行扩展显示；false: 请求到的数据替换原有数据
     */
    public void  findData(final boolean isAppend) {
        mMdsGetFocOffAccArts = new MDSAppGetFocusOffAccArticles() {
            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(OrderActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(OrderActivity.this, statusInfo, Toast.LENGTH_LONG);
                }
                order_content_Recy.refreshComplete();
                order_content_Recy.loadMoreComplete();
                mMdsGetFocOffAccArts = null;
                if (focusList.size() == 0) {
                    no_content.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.INVISIBLE);
                } else {
                    no_content.setVisibility(View.INVISIBLE);
                    order_content_Recy.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.INVISIBLE);
                }
            }


            @Override
            protected void onSuccess(MDSFocusPageInfo responseContent) {
                super.onSuccess(responseContent);
                order_content_Recy.refreshComplete();
                order_content_Recy.loadMoreComplete();
                mMdsGetFocOffAccArts = null;
                totalSize = responseContent.getTotalSize();

                int countSize = responseContent.getFocusList().size();
                if (!isAppend) {
                    focusList.clear();
                }
                //如果服务器返回的总数据<= 拿到的总数据的时候
                if (totalSize <= focusList.size()) {
                    //CustomToast.show(mContext, "没有更多数据", Toast.LENGTH_SHORT);
                    recyAdapter.notifyDataSetChanged();
                    if (focusList.size() == 0) {
                        no_content.setVisibility(View.VISIBLE);
                        requestData.setVisibility(View.INVISIBLE);
                    } else {
                        no_content.setVisibility(View.INVISIBLE);
                        order_content_Recy.setVisibility(View.VISIBLE);
                        requestData.setVisibility(View.INVISIBLE);
                    }
                    return;
                }

                for (int i = 0; i < countSize; i++) {
                    focusList.add(responseContent.getFocusList().get(i));
                }
                if (focusList.size() == 0) {
                    no_content.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.INVISIBLE);

                } else {
                    no_content.setVisibility(View.INVISIBLE);
                    order_content_Recy.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.INVISIBLE);
                }

                order_content_Recy.setLoadingMoreEnabled(true);
                recyAdapter.notifyDataSetChanged();
            }
        };
        //计算请求页数和请求数目
        int curCount = 0;
        int requestPageNumber = 1;
        int requestCount = PAGE_ITEM_SIZE;
        if (isAppend) {
            curCount = focusList.size(); //当前显示条目(剔除假数据)
            requestPageNumber = curCount / PAGE_ITEM_SIZE + 1; //请求页数
            requestCount = PAGE_ITEM_SIZE - (curCount % PAGE_ITEM_SIZE); //请求个数
        }
        mMdsGetFocOffAccArts.appGetFocusOffAccArticles(AccountManager.getInstance(OrderActivity.this).getMdsToken(), requestPageNumber, requestCount);
    }

}
