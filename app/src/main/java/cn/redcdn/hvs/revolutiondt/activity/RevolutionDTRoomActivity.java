package cn.redcdn.hvs.revolutiondt.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import cn.redcdn.datacenter.hpucenter.HPUGetMyallTfList;
import cn.redcdn.datacenter.hpucenter.data.TFInfo;
import cn.redcdn.datacenter.medicalcenter.MDSErrorCode;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.profiles.activity.YuYueZhuanZhenActivity;
import cn.redcdn.hvs.revolutiondt.adapter.RevolutionDTRecyAdapter;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

public class RevolutionDTRoomActivity extends BaseActivity implements OnRefreshListener {
    private SmartRefreshLayout revolutionSmartLay;
    private RecyclerView revolutionRecyclerView;
    private LinearLayout revolutionLoadingLay;
    private TextView revolutionLoadingText;
    private LinearLayoutManager revolutionLinearLayoutManager;
    private ArrayList<TFInfo> tfInfoArrayList;
    private RevolutionDTRecyAdapter revolutionDTRecyAdapter;
    private boolean isShowLoading = true;
    private Boolean isShowGetDataFail = true;
    private ImageView revolution_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revolution_dtroom);
        initView();
    }

    /**
     * 初始化 ui
     */
    private void initView() {
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.revolution_center));
        revolutionSmartLay = (SmartRefreshLayout) findViewById(R.id.revolution_dt_smart_refresh);
        revolutionSmartLay.setOnRefreshListener(this);
        revolutionLoadingLay = (LinearLayout) findViewById(R.id.revolution_dtroom_loading_layout);
        revolutionLoadingText = (TextView) findViewById(R.id.revolution_dtroom_loading_text);
        revolutionRecyclerView = (RecyclerView) findViewById(R.id.revolution_dt_Recy);
        revolution_btn = (ImageView) findViewById(R.id.revolution_btn);
        revolution_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(RevolutionDTRoomActivity.this,YuYueZhuanZhenActivity.class);
                startActivity(intent);
            }
        });
        revolutionLinearLayoutManager = new LinearLayoutManager(RevolutionDTRoomActivity.this);
        revolutionRecyclerView.setLayoutManager(revolutionLinearLayoutManager);
        tfInfoArrayList = new ArrayList<TFInfo>();
        revolutionDTRecyAdapter = new RevolutionDTRecyAdapter(tfInfoArrayList, RevolutionDTRoomActivity.this);
        revolutionRecyclerView.setAdapter(revolutionDTRecyAdapter);

    }

    /**
     * 下拉刷新
     *
     * @param refreshlayout
     */
    @Override
    public void onRefresh(RefreshLayout refreshlayout) {
        CustomLog.d(TAG, "RequestDtFragment:: 触发下拉刷新");
        initData();
    }

    /**
     * 获取数据
     */

    private void initData() {
        CustomLog.d(TAG, "initData HPUGetMyallTfList");
        final HPUGetMyallTfList hpuGetMyallTfList = new HPUGetMyallTfList() {
            @Override
            protected void onSuccess(List<TFInfo> responseContent) {
                super.onSuccess(responseContent);
                removeLoadingView();
                CustomLog.d(TAG, "HPUGetMyallTfList onSuccess");
                isShowLoading = false;
                isShowGetDataFail = false;
                revolutionSmartLay.finishRefresh();
                if (tfInfoArrayList.size() != 0) {
                    tfInfoArrayList.clear();
                }
                if (responseContent.size() != 0) {
                    for (int i = 0; i < responseContent.size(); i++) {
                        tfInfoArrayList.add(responseContent.get(i));
                    }
                    revolutionDTRecyAdapter.notifyDataSetChanged();
                    revolutionLoadingLay.setVisibility(View.GONE);
                    revolutionSmartLay.setVisibility(View.VISIBLE);
                    revolutionRecyclerView.setVisibility(View.VISIBLE);
                } else {
                    revolutionLoadingLay.setVisibility(View.VISIBLE);
                    revolutionLoadingText.setText(getString(R.string.no_revolution_data));
                    revolutionSmartLay.setVisibility(View.GONE);
                    revolutionRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onFail(int statusCode, String statusInfo) {
                super.onFail(statusCode, statusInfo);
                removeLoadingView();
                revolutionSmartLay.finishRefresh(false);
                if (isShowGetDataFail) {
                    revolutionLoadingLay.setVisibility(View.VISIBLE);
                    revolutionLoadingText.setText(getString(R.string.jiazai_fail));
                    revolutionSmartLay.setVisibility(View.VISIBLE);
                    revolutionRecyclerView.setVisibility(View.GONE);
                } else {
                    revolutionLoadingLay.setVisibility(View.GONE);
                    revolutionSmartLay.setVisibility(View.VISIBLE);
                    revolutionRecyclerView.setVisibility(View.VISIBLE);
                }
                CustomLog.d(TAG, "HPUGetMyallTfList||onFail||statusCode: " + statusCode + "statusInfo: " + statusInfo);
                if (statusCode == MDSErrorCode.MDS_TOKEN_DISABLE) {
                    AccountManager.getInstance(RevolutionDTRoomActivity.this).tokenAuthFail(statusCode);
                } else {
                    CustomToast.show(RevolutionDTRoomActivity.this, statusInfo, CustomToast.LENGTH_LONG);
                }
            }
        };
        if (isShowLoading) {
            showLoadingView(getString(R.string.wait), new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    removeLoadingView();
                    CustomLog.d(TAG, "取消获取转诊中心列表");
                    hpuGetMyallTfList.cancel();
                    RevolutionDTRoomActivity.this.finish();
                }
            }, true);
        }
        hpuGetMyallTfList.getList(AccountManager.getInstance(RevolutionDTRoomActivity.this).getMdsToken());
    }

    /**
     * 在onResume中获取数据是为了从UDTChatRoomActivity中回来的时候，刷新数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
}
