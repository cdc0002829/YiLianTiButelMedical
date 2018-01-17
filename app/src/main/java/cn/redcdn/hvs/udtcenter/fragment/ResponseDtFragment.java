package cn.redcdn.hvs.udtcenter.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.HPUGetMyRequestList;
import cn.redcdn.datacenter.hpucenter.HPUGetMyResponseList;
import cn.redcdn.datacenter.hpucenter.data.CSLInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.udtcenter.activity.UDTRoomActivity;
import cn.redcdn.hvs.udtcenter.adapter.RequestDtFragmentRecyAdapter;
import cn.redcdn.hvs.udtcenter.adapter.ResponseDtFragmentRecyAdapter;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

/**
 * Created by dell on 2017/11/20.
 */

public class ResponseDtFragment extends BaseFragment implements OnRefreshListener {
    private static final String TAG = ResponseDtFragment.class.getName();
    private RecyclerView responseRecy;
    private LinearLayout loadingLay;
    private TextView loadingText;
    private LinearLayoutManager mLinearLayoutManager;
    private View view;
    private Context mContext;
    private ResponseDtFragmentRecyAdapter adapter;
    private ArrayList<CSLInfo> arrayList;
    private SwipeRefreshLayout responseSwipe;
    private SmartRefreshLayout smartRefreshLayout;
    private Boolean isShowLoadingFlag = true;
    private Boolean isShowGetDataFail = true;
    private Boolean isFirstShowFragment = true;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.response_dt_fragment_layout, null);
        initView();
        return view;
    }

    /**
     * 初始化控件
     */
    protected void initView() {
        CustomLog.d(TAG, "initView");
        responseRecy = (RecyclerView) view.findViewById(R.id.response_dt_fragment_Recy);
        smartRefreshLayout = (com.scwang.smartrefresh.layout.SmartRefreshLayout) view.findViewById(R.id.response_smart_refresh);
        smartRefreshLayout.setOnRefreshListener(this);
        loadingLay = (LinearLayout) view.findViewById(R.id.response_dt_fragment_loading_layout);
        loadingText = (TextView) view.findViewById(R.id.response_dt_fragment_loading_text);
//        responseRecy.setLoadingMoreEnabled(false);
//        responseSwipe = (SwipeRefreshLayout) view.findViewById(R.id.response_dt_fragment_swipe);
//        responseSwipe.setOnRefreshListener(this);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        responseRecy.setLayoutManager(mLinearLayoutManager);
//        responseRecy.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
//        responseRecy.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
//        responseRecy.setArrowImageView(R.drawable.iconfont_downgrey);
//        responseRecy.refreshComplete();
        arrayList = new ArrayList<CSLInfo>();
        adapter = new ResponseDtFragmentRecyAdapter(arrayList, mContext);

        responseRecy.setAdapter(adapter);
        UDTRoomActivity udtRoomActivity = (UDTRoomActivity) getActivity();
        udtRoomActivity.setOnShowResponseDtFragmentListen(new UDTRoomActivity.onShowResponseDtFragmentListen() {
            @Override
            public void ShowResponseDtFragmentListen() {
                if (isFirstShowFragment) {
                    initData();
                } else {

                }
            }
        });
    }

    /**
     * 监听器
     */
    @Override
    protected void setListener() {
        CustomLog.d(TAG, "setListener");
//        responseRecy.setLoadingListener(new XRecyclerView.LoadingListener() {
//            @Override
//            public void onRefresh() {
//                responseRecy.setLoadingMoreEnabled(false);
//                CustomLog.d(TAG, "RequestFragment:: 触发下拉刷新");
//                isShowLoadingFlag = false;
//                initData();
//            }
//
//            @Override
//            public void onLoadMore() {
//
////                new Handler().postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
////                        CustomLog.d(TAG, "OrderFragment:: 触发加载更多");
//////                        if (mMdsGetFocOffAccArts == null) {
//////                            recyAdapter.notifyDataSetChanged();
//////                            loadMoreData();
//////                        } else {
//////                            CustomLog.d(TAG, "OrderFragment:: 数据获取中");
//////                        }
////                    }
////                }, 1000);
//            }
//        });
    }

    /**
     * 获取数据
     */
    @Override
    protected void initData() {
        final HPUGetMyResponseList hpuGetMyResponseList = new HPUGetMyResponseList() {
            @Override
            protected void onSuccess(List<CSLInfo> responseContent) {
                super.onSuccess(responseContent);
                CustomLog.d(TAG, "hpuGetMyResponseList onSuccess");
                isShowGetDataFail = false;
                isFirstShowFragment = false;
                if (isShowLoadingFlag) {
                    removeLoadingView();
                    isShowLoadingFlag = false;
                    smartRefreshLayout.finishRefresh();
                } else {
//                    responseRecy.refreshComplete();
                    smartRefreshLayout.finishRefresh();
                }
                if (arrayList.size() != 0) {
                    arrayList.clear();
                }
                if (responseContent.size() != 0) {
                    for (int i = 0; i < responseContent.size(); i++) {
                        arrayList.add(responseContent.get(i));
                    }
                    adapter.notifyDataSetChanged();
                    loadingLay.setVisibility(View.GONE);
                    responseRecy.setVisibility(View.VISIBLE);
                    smartRefreshLayout.setVisibility(View.VISIBLE);
                } else {
                    loadingLay.setVisibility(View.VISIBLE);
                    loadingText.setText(mContext.getString(R.string.no_dt_data));
                    responseRecy.setVisibility(View.GONE);
                    smartRefreshLayout.setVisibility(View.GONE);

                }

            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                if (isShowLoadingFlag) {
                    removeLoadingView();
                } else {
//                    responseRecy.refreshComplete();
                    smartRefreshLayout.finishRefresh(false);
                }
                if (isShowGetDataFail) {
                    loadingLay.setVisibility(View.VISIBLE);
                    loadingText.setText(mContext.getString(R.string.jiazai_fail));
                    responseRecy.setVisibility(View.GONE);
                    smartRefreshLayout.setVisibility(View.VISIBLE);
                } else {
                    loadingLay.setVisibility(View.GONE);
                    responseRecy.setVisibility(View.VISIBLE);
                    smartRefreshLayout.setVisibility(View.VISIBLE);
                }
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(mContext, statusInfo, CustomToast.LENGTH_SHORT);
                }

                CustomLog.e(TAG, "hpuGetMyResponseList onFail statusCode:" + statusCode + "statusInfo" + statusInfo);
            }
        };
        if (isShowLoadingFlag) {
            showLoadingView(getString(R.string.wait), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    removeLoadingView();
                    CustomLog.d(TAG, "取消获取我的接诊列表");
                    hpuGetMyResponseList.cancel();
                    if (Activity.class.isInstance(mContext)) {
                        // 转化为activity，然后finish就行了
                        Activity activity = (Activity) mContext;
                        activity.finish();
                    }
                }
            }, true);
        }
        CustomLog.d(TAG, "HPUGetMyResponseList.getresponselist");
//        hpuGetMyResponseList.getresponselist("a977fd57-1e42-4a4e-a401-ee915cddf7b4_e16557a20eae4093874c3faff893a62e");
        hpuGetMyResponseList.getresponselist(AccountManager.getInstance(mContext).getMdsToken());
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    /**
     * 下拉刷新
     *
     * @param refreshlayout
     */
    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        CustomLog.d(TAG, "RequestDtFragment:: 触发下拉刷新");
        isShowLoadingFlag = false;
        initData();
    }


//    @Override
//    public void onRefresh() {
//        responseSwipe.setRefreshing(true);
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CustomToast.show(getActivity(), "刷新完成", CustomToast.LENGTH_SHORT);
//                responseSwipe.setRefreshing(false);
//            }
//        }, 3000);//3秒后执行Runnable中的run方法
//    }

    /**
     * 当从UDTChatRoomActivity回来的时候，可以刷新数据
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isShowLoadingFlag) {

        } else {
            initData();
        }
    }
}
