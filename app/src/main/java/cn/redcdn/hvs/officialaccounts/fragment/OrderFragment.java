package cn.redcdn.hvs.officialaccounts.fragment;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.offaccscenter.MDSAppGetFocusOffAccArticles;
import cn.redcdn.datacenter.offaccscenter.data.MDSFocusPageInfo;
import cn.redcdn.datacenter.offaccscenter.data.MDSfocusOffAccArtcleInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.officialaccounts.adapter.OrderFragmentRecyAdapter;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Created by ${chenghb} on 2017/2/23.
 */
public class OrderFragment extends BaseFragment {
    private static final String TAG = OrderFragment.class.getName();
    private XRecyclerView order_content_Recy;
    public OrderFragmentRecyAdapter recyAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Context mContext;
    public List<MDSfocusOffAccArtcleInfo> focusList;
    private int totalSize;
    private final static int PAGE_ITEM_SIZE = 10;
    private View view;
    private MDSAppGetFocusOffAccArticles mMdsGetFocOffAccArts;
    private LinearLayout no_content;
    private TextView requestData;
    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = View.inflate(getActivity(), R.layout.order_fragment, null);
        order_content_Recy = (XRecyclerView) view.findViewById(R.id.order_content_Recy);
        no_content = (LinearLayout) view.findViewById(R.id.no_content);
        requestData = (TextView) view.findViewById(R.id.requestData);
        initView();
        setListener();
        return view;
    }


    @Override
    protected void initView() {
        super.initView();
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        order_content_Recy.setLayoutManager(mLinearLayoutManager);
        order_content_Recy.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        order_content_Recy.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        order_content_Recy.setArrowImageView(R.drawable.iconfont_downgrey);
        focusList = new ArrayList<MDSfocusOffAccArtcleInfo>();
        recyAdapter = new OrderFragmentRecyAdapter(focusList, mContext, order_content_Recy);
        order_content_Recy.setAdapter(recyAdapter);
    }

    @Override
    protected void setListener() {
        order_content_Recy.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                order_content_Recy.setLoadingMoreEnabled(false);
                CustomLog.d(TAG, "OrderFragment:: 触发下拉刷新");
                if (mMdsGetFocOffAccArts == null) {
                    findData(false);
                } else {
                    CustomLog.d(TAG, "OrderFragment::onRefresh() 获取中");
                }
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

    protected void initData() {
    }

    //供父fragment调用
    public void updateState(boolean flag) {
        if (focusList != null && focusList.size() == 0) {
            findData(true);
        }
    }

    /**
     * 获取加载数据
     *
     * @param isAppend 是否扩展。 true: 请求到的数据进行扩展显示；false: 请求到的数据替换原有数据
     */
    public void findData(final boolean isAppend) {
        mMdsGetFocOffAccArts = new MDSAppGetFocusOffAccArticles() {
            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(getActivity(), statusInfo, Toast.LENGTH_LONG);
                }
                order_content_Recy.refreshComplete();
                order_content_Recy.loadMoreComplete();
                mMdsGetFocOffAccArts = null;
                if (focusList.size()  == 0) {
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
        mMdsGetFocOffAccArts.appGetFocusOffAccArticles(AccountManager.getInstance(getActivity()).getMdsToken(), requestPageNumber, requestCount);
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
