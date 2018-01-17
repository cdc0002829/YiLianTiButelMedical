package cn.redcdn.hvs.officialaccounts.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.offaccscenter.MDSAppGetArticlesByOffAcc;
import cn.redcdn.datacenter.offaccscenter.MDSAppGetOffAccInfo;
import cn.redcdn.datacenter.offaccscenter.data.MDSFocusPageInfo;
import cn.redcdn.datacenter.offaccscenter.data.MDSfocusOffAccArtcleInfo;
import cn.redcdn.datacenter.offaccscenter.data.OffAccdetailInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseFragment;
import cn.redcdn.hvs.officialaccounts.adapter.ContentFragmentAdapter;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Created by ${chenghb} on 2017/2/27.
 */
public class ContentFragment extends BaseFragment {
    private static final String TAG = ContentFragment.class.getName();

    private XRecyclerView content_recycler;
    private ContentFragmentAdapter adapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Context mContext;
    private int totalSize;
    private List<MDSfocusOffAccArtcleInfo> focusList;
    private String officialAccountId;
    private TextView tvtitle;
    private final static int PAGE_ITEM_SIZE = 10;
    private MDSAppGetArticlesByOffAcc mMDdsGetArticlesByOffAcc;
    private String officialName;
    private TitleBackListener mTitleBackListener;
    private Button btn_back;
    private LinearLayout no_content;

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_fragment, container, false);
        content_recycler = (XRecyclerView) view.findViewById(R.id.content_recycler);
        tvtitle = (TextView) view.findViewById(R.id.tvtitle);
        btn_back = (Button) view.findViewById(R.id.btn_back);
        no_content = (LinearLayout) view.findViewById(R.id.no_content);
        Bundle bundle = getArguments();
        if (bundle != null) {
            officialAccountId = bundle.getString("officialAccountId");
            officialName = bundle.getString("officialName");
            CustomLog.d(TAG, "officialName" + officialName);

        }
        initView();
        initData();
        setListener();
        return view;

    }


    @Override
    protected void initView() {
        super.initView();
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        content_recycler.setLayoutManager(mLinearLayoutManager);
        content_recycler.setRefreshProgressStyle(ProgressStyle.BallSpinFadeLoader);
        content_recycler.setLoadingMoreProgressStyle(ProgressStyle.BallSpinFadeLoader);
        content_recycler.setArrowImageView(R.drawable.iconfont_downgrey);
        focusList = new ArrayList<MDSfocusOffAccArtcleInfo>();
        adapter = new ContentFragmentAdapter(focusList, mContext, content_recycler);
        content_recycler.setAdapter(adapter);


    }

    public void addTitleBackListener(TitleBackListener listener) {
        mTitleBackListener = listener;
    }

    public void removeTitleBackListener() {
        mTitleBackListener = null;
    }

    @Override
    protected void setListener() {

        content_recycler.setLoadingListener(new XRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                content_recycler.setLoadingMoreEnabled(false);
                CustomLog.d(TAG, "OrderFragment:: 触发下拉刷新");
                if (mMDdsGetArticlesByOffAcc == null) {
                    findDate(false);
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
                        if (mMDdsGetArticlesByOffAcc == null) {
                            adapter.notifyDataSetChanged();
                            loadMoreData();
                        } else {
                            CustomLog.d(TAG, "OrderFragment:: 数据获取中");
                        }
                    }
                }, 1000);
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTitleBackListener.onTitleBackPress();
            }
        });
    }

    @Override
    protected void initData() {
        findDate(true);
    }


    //计算pageNumber,触发加载数据,如果当前显示条目小于一页显示值，则继续请求第一页；
    private void loadMoreData() {
        CustomLog.d(TAG, "loadMoreData");
        findDate(true);
    }


    public static Fragment createInstance(int i) {
        ContentFragment contentFragment = new ContentFragment();

        return contentFragment;
    }


    /**
     * 获取加载数据
     *
     * @param isAppend 是否扩展。 true: 请求到的数据进行扩展显示；false: 请求到的数据替换原有数据
     */
    private void findDate(final boolean isAppend) {

        MDSAppGetOffAccInfo offAccInfo = new MDSAppGetOffAccInfo() {
            @Override
            protected void onSuccess(OffAccdetailInfo responseContent) {
                super.onSuccess(responseContent);
                tvtitle.setText(responseContent.getName());
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
            }
        };
        offAccInfo.appGetOffAccInfo(AccountManager.getInstance(mContext).getToken(), officialAccountId);

        mMDdsGetArticlesByOffAcc = new MDSAppGetArticlesByOffAcc() {
            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                CustomLog.e(TAG, "onFail" + "statusCode:" + statusCode + " statusInfo:" + statusInfo);
                if (statusCode == MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(getActivity()).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(getActivity(), statusInfo, Toast.LENGTH_LONG);
                }
                content_recycler.refreshComplete();
                content_recycler.loadMoreComplete();
                mMDdsGetArticlesByOffAcc = null;
                if (focusList.size() == 0) {
                    no_content.setVisibility(View.VISIBLE);
                } else {
                    content_recycler.setVisibility(View.VISIBLE);
                    no_content.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onSuccess(MDSFocusPageInfo responseContent) {
                super.onSuccess(responseContent);

                content_recycler.loadMoreComplete();
                content_recycler.refreshComplete();
                mMDdsGetArticlesByOffAcc = null;
                totalSize = responseContent.getTotalSize();
                int countSize = responseContent.getFocusList().size();
                if (!isAppend) {
                    focusList.clear();
                }
                if (totalSize <= focusList.size()) {
                    // CustomToast.show(mContext, "没有更多数据", Toast.LENGTH_LONG);
                    adapter.notifyDataSetChanged();
                    if (focusList.size() == 0) {
                        no_content.setVisibility(View.VISIBLE);
                    } else {
                        content_recycler.setVisibility(View.VISIBLE);
                        no_content.setVisibility(View.GONE);
                    }
                    return;
                }
                for (int i = 0; i < countSize; i++) {
                    focusList.add(responseContent.getFocusList().get(i));
                    officialAccountId = responseContent.getFocusList().get(i).getOffaccId();
                    CustomLog.d(TAG, "offaccName" + responseContent.getFocusList().get(i).getOffaccName());
                }

                content_recycler.setLoadingMoreEnabled(true);
                adapter.notifyDataSetChanged();
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
        mMDdsGetArticlesByOffAcc.appGetArticlesByOffAcc(AccountManager.getInstance(mContext).getMdsToken(), officialAccountId, requestPageNumber, requestCount);
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

    public interface TitleBackListener {
        void onTitleBackPress();
    }
}
