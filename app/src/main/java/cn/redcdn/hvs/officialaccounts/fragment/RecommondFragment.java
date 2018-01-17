package cn.redcdn.hvs.officialaccounts.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.offaccscenter.MDSAppGetRecommendOffAccs;
import cn.redcdn.datacenter.offaccscenter.data.MDSRecommedOffaccInfo;
import cn.redcdn.datacenter.offaccscenter.data.RecommedPageInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.officialaccounts.adapter.RecommendAdapter;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Created by ${chenghb} on 2017/2/23.
 */
public class RecommondFragment extends BaseFragment {
    private static final String TAG = RecommondFragment.class.getName();
    private XRecyclerView recommond_recyview;
    private RecommendAdapter recommdAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Context mContext;
    public List<MDSRecommedOffaccInfo> mRecommedList;//每个item信息集合
    private int totalSize;
    private final static int PAGE_ITEM_SIZE = 10;
    private LinearLayout no_content;
    private MDSAppGetRecommendOffAccs mMdsGetRecommendOff;
    private FragmentActivity activity;

    private int curCount;
    private TextView requestData;
    private int maxPagerNum = 1;
    private int curPageNum = 0;

    @Override
    public void onAttach(Context context) {
        this.mContext = context;
        super.onAttach(context);
    }

    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.recommond_fragment, null);
        recommond_recyview = (XRecyclerView) view.findViewById(R.id.recommond_recyview);
        no_content = (LinearLayout) view.findViewById(R.id.no_content);
        requestData = (TextView) view.findViewById(R.id.requestData);
        activity = getActivity();
        initView();
        setListener();
        return view;
    }


    @Override
    protected void initView() {
        super.initView();
        recommond_recyview.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        recommond_recyview.setLayoutManager(mLinearLayoutManager);
        recommond_recyview.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        recommond_recyview.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        recommond_recyview.setArrowImageView(R.drawable.iconfont_downgrey);
        mRecommedList = new ArrayList<MDSRecommedOffaccInfo>();
        recommdAdapter = new RecommendAdapter(mRecommedList, mContext, recommond_recyview, getActivity());
        recommond_recyview.setAdapter(recommdAdapter);

    }

    @Override
    protected void setListener() {

        recommond_recyview.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                recommond_recyview.setLoadingMoreEnabled(false);
                CustomLog.d(TAG, "RecommondFragment:: 触发下拉刷新");
                if (mMdsGetRecommendOff == null) {
                    findDate(false);
                } else {
                    CustomLog.d(TAG, "RecommondFragment::onRefresh() 获取中");
                }
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomLog.d(TAG, "OrderFragment:: 触发加载更多");
                        if (mMdsGetRecommendOff == null) {
                            recommdAdapter.notifyDataSetChanged();

                            loadMoreData();
                        } else {
                            CustomLog.d(TAG, "OrderFragment:: 数据获取中");
                        }
                    }
                }, 1000);
            }
        });
    }

    @Override
    protected void initData() {
    }

    private void loadMoreData() {
        findDate(true);
    }

    //供父Fragment调用
    public void updateState(boolean flag) {
        if (mRecommedList != null && mRecommedList.size() == 0) {
            findDate(false);
        }
    }

    /**
     * 获取加载数据
     *
     * @param isAppend 是否扩展。 true: 请求到的数据进行扩展显示；false: 请求到的数据替换原有数据
     **/
    private void findDate(final boolean isAppend) {

        mMdsGetRecommendOff = new MDSAppGetRecommendOffAccs() {
            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(getActivity(), statusInfo, Toast.LENGTH_LONG);
                }
                recommond_recyview.refreshComplete();
                recommond_recyview.loadMoreComplete();
                mMdsGetRecommendOff = null;
                if (mRecommedList.size() == 0) {
                    no_content.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.INVISIBLE);
                } else {
                    no_content.setVisibility(View.GONE);
                    recommond_recyview.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            protected void onSuccess(RecommedPageInfo responseContent) {
                super.onSuccess(responseContent);

                recommond_recyview.refreshComplete();
                recommond_recyview.loadMoreComplete();
                mMdsGetRecommendOff = null;
                totalSize = responseContent.getTotalSize();
                maxPagerNum = totalSize / PAGE_ITEM_SIZE;
                if (totalSize % PAGE_ITEM_SIZE != 0) {
                    maxPagerNum++;
                }
                CustomLog.d(TAG, "maxPagerNum" + maxPagerNum);
                CustomLog.d(TAG, "totalSize" + totalSize);
                int countSize = responseContent.getRecommedList().size();
                CustomLog.d(TAG, "countSize" + countSize);
                if (!isAppend) {
                    mRecommedList.clear();
                }

                if (isAppend) {
                    recommdAdapter.notifyDataSetChanged();
                    if (mRecommedList.size() == 0) {
                        no_content.setVisibility(View.VISIBLE);
                        requestData.setVisibility(View.INVISIBLE);
                    } else {
                        no_content.setVisibility(View.GONE);
                        recommond_recyview.setVisibility(View.VISIBLE);
                        requestData.setVisibility(View.INVISIBLE);
                    }

                }
                if (curPageNum > maxPagerNum){

                    return;
                }
                for (int i = 0; i < countSize; i++) {
                    mRecommedList.add(responseContent.getRecommedList().get(i));
                }

                if (mRecommedList.size() == 0) {
                    no_content.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.INVISIBLE);
                } else {
                    requestData.setVisibility(View.INVISIBLE);
                    no_content.setVisibility(View.GONE);
                    recommond_recyview.setVisibility(View.VISIBLE);
                }
                recommond_recyview.setLoadingMoreEnabled(true);
                recommdAdapter.notifyDataSetChanged();
            }
        };

        if (isAppend) {
                curPageNum++;
        } else {
            curPageNum = 1;
        }
        CustomLog.d(TAG,"请求接口外的curPageNum"+curPageNum);
        mMdsGetRecommendOff.appGetRecommedOffAccs(AccountManager.getInstance(mContext).getMdsToken()
                , curPageNum, PAGE_ITEM_SIZE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private Dialog dialog = null;

    public void showLoadingView(String message,
                                final DialogInterface.OnCancelListener listener, boolean cancelAble) {
        CustomLog.i(TAG, "MeetingActivity::showLoadingDialog() msg: " + message);
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
        dialog = cn.redcdn.hvs.util.CommonUtil.createLoadingDialog(getActivity(), message, listener);
        dialog.setCancelable(cancelAble);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    listener.onCancel(dialog);
                }
                return false;
            }
        });
        try {
            dialog.show();
        } catch (Exception ex) {
            CustomLog.d(TAG, "BaseActivity::showLoadingView()" + ex.toString());
        }
    }

    protected void removeLoadingView() {

        CustomLog.i(TAG, "MeetingActivity::removeLoadingView()");

        if (dialog != null) {

            dialog.dismiss();

            dialog = null;

        }
    }
}
