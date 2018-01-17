package cn.redcdn.hvs.udtcenter.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
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
import cn.redcdn.datacenter.hpucenter.data.CSLInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.datacenter.offaccscenter.data.MDSfocusOffAccArtcleInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.officialaccounts.adapter.OrderFragmentRecyAdapter;
import cn.redcdn.hvs.officialaccounts.fragment.OrderFragment;
import cn.redcdn.hvs.responsedt.activity.DepartmentResponseDtActivity;
import cn.redcdn.hvs.udtcenter.adapter.RequestDtFragmentRecyAdapter;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

/**
 * Created by dell on 2017/11/20.
 */

public class RequestDtFragment extends BaseFragment implements OnRefreshListener {
    private static final String TAG = RequestDtFragment.class.getName();
    private RecyclerView requestRecy;
    private LinearLayoutManager mLinearLayoutManager;
    private LinearLayout loadingLay;
    private TextView loadingText;
    private View view;
    private Context mContext;
    private RequestDtFragmentRecyAdapter adapter;
    private ArrayList<CSLInfo> arrayList;
    private SwipeRefreshLayout requestSwipe;
    private SmartRefreshLayout smartRefreshLayout;
    private Boolean isShowLoadingFlag = true;
    private Boolean isShowGetDataFail = true;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.request_dt_fragment_layout, null);
        initView();
        return view;
    }

    /**
     * 初始化控件
     */
    protected void initView() {
        CustomLog.d(TAG, "initView");
        requestRecy = (RecyclerView) view.findViewById(R.id.request_dt_fragment_Recy);
        loadingLay = (LinearLayout) view.findViewById(R.id.request_dt_fragment_loading_layout);
        loadingText = (TextView) view.findViewById(R.id.request_dt_fragment_loading_text);
//        requestRecy.setLoadingMoreEnabled(false);
//        requestRecy.setPullRefreshEnabled(false);
        smartRefreshLayout = (com.scwang.smartrefresh.layout.SmartRefreshLayout) view.findViewById(R.id.request_smart_refresh);
        smartRefreshLayout.setOnRefreshListener(this);
//        requestSwipe = (SwipeRefreshLayout) view.findViewById(R.id.request_dt_fragment_swipe);
//        requestSwipe.setOnRefreshListener(this);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        requestRecy.setLayoutManager(mLinearLayoutManager);
//        requestRecy.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
//        requestRecy.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
//        requestRecy.setArrowImageView(R.drawable.iconfont_downgrey);
        arrayList = new ArrayList<CSLInfo>();
        adapter = new RequestDtFragmentRecyAdapter(arrayList, mContext);
        initData();
        requestRecy.setAdapter(adapter);
    }

    @Override
    protected void setListener() {
        CustomLog.d(TAG, "setListener");
//        requestRecy.setLoadingListener(new XRecyclerView.LoadingListener() {
//            @Override
//            public void onRefresh() {
//                requestRecy.setLoadingMoreEnabled(false);
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
        final HPUGetMyRequestList hpuGetMyRequestList = new HPUGetMyRequestList() {
            @Override
            protected void onSuccess(List<CSLInfo> responseContent) {
                super.onSuccess(responseContent);
                CustomLog.d(TAG, "hpuGetMyRequestList||onSuccess");
                isShowGetDataFail = false;
                if (isShowLoadingFlag) {
                    removeLoadingView();
                    isShowLoadingFlag = false;
                    smartRefreshLayout.finishRefresh();
                } else {
//                    requestRecy.refreshComplete();
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
                    requestRecy.setVisibility(View.VISIBLE);
                    smartRefreshLayout.setVisibility(View.VISIBLE);
                } else {
                    loadingLay.setVisibility(View.VISIBLE);
                    loadingText.setText(mContext.getString(R.string.no_dt_data));
                    requestRecy.setVisibility(View.GONE);
                    smartRefreshLayout.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                if (isShowLoadingFlag) {
                    removeLoadingView();
                } else {
//                    requestRecy.refreshComplete();
                    smartRefreshLayout.finishRefresh(false);
                }
                if (isShowGetDataFail) {
                    loadingLay.setVisibility(View.VISIBLE);
                    loadingText.setText(mContext.getString(R.string.jiazai_fail));
                    requestRecy.setVisibility(View.GONE);
                    smartRefreshLayout.setVisibility(View.VISIBLE);
                } else {
                    loadingLay.setVisibility(View.GONE);
                    requestRecy.setVisibility(View.VISIBLE);
                    smartRefreshLayout.setVisibility(View.VISIBLE);
                }
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(mContext).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(mContext, statusInfo, CustomToast.LENGTH_SHORT);
                }

                CustomLog.e(TAG, "HPUGetMyRequestList onFail statusCode:" + statusCode + "statusInfo:" + statusInfo);
            }
        };
        if (isShowLoadingFlag) {
            showLoadingView(getString(R.string.wait), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    removeLoadingView();
                    CustomLog.d(TAG, "取消获取我的求诊列表");
                    hpuGetMyRequestList.cancel();
                    if (Activity.class.isInstance(mContext)) {
                        // 转化为activity，然后finish就行了
                        Activity activity = (Activity) mContext;
                        activity.finish();
                    }

                }
            }, true);
        }
        CustomLog.d(TAG, "HPUGetMyRequestList.getmyseekslist");
        hpuGetMyRequestList.getmyseekslist(AccountManager.getInstance(mContext).getMdsToken());
//        hpuGetMyRequestList.getmyseekslist("a747e29a-3e7f-462b-bda6-209ab23e28f2_e16557a20eae4093874c3faff893a62e");

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
        CustomLog.d(TAG, "RequestFragment:: 触发下拉刷新");
        isShowLoadingFlag = false;
        initData();
    }


//    @Override
//    public void onRefresh() {
//        requestSwipe.setRefreshing(true);
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                CustomToast.show(getActivity(), "刷新完成", CustomToast.LENGTH_SHORT);
//                requestSwipe.setRefreshing(false);
//            }
//        }, 3000);//3秒后执行Runnable中的run方法
//
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
