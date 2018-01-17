package cn.redcdn.hvs.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;

import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.adapter.SelectGroupMemberAdapter;
import cn.redcdn.hvs.im.adapter.SelectGroupMemberAdapter.SelectCallBack;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.util.IMCommonUtil;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

/**
 * Desc
 * Created by wangkai on 2017/3/1.
 */

public class SelectGroupMemeberActivity extends BaseActivity {

    private final String TAG = "SelectGroupMemeberActivity";
    private SelectGroupMemberAdapter mAdapter = null;
    private GroupDao groupDao = null;
    private ArrayList<GroupMemberBean> memberBeanMap = new ArrayList<GroupMemberBean>();
    public final static String SELECT_GROUPID = "select_groupid";
    private String groupId = "";
    private ListView mListview;
    private String mySelfNueNumber = "";
    public final static String START_RESULT_NUMBER = "start_result_number";
    public final static String START_RESULT_NAME = "start_result_name";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CustomLog.d(TAG,"onCreate begin");
        setContentView(R.layout.select_groupmemeber_layout);
        groupDao = new GroupDao(this);
        groupId = getIntent().getStringExtra(SELECT_GROUPID);
        mySelfNueNumber = AccountManager.getInstance(this).getNube();
        mListview = (ListView) findViewById(R.id.select_conversation_list);
        initTitleBar();
        mAdapter = new SelectGroupMemberAdapter(this, memberBeanMap,
                IMCommonUtil.getDeviceSize(this).x);
        mAdapter.setCallBack(new SelectCallBack() {
            @Override
            public void selectMember(String nubeNumder, String showName) {
                // TODO Auto-generated method stub
                CustomLog.d(TAG,"点击选择回复的联系人返回" + showName + nubeNumder);
                Intent intent = new Intent();
                intent.putExtra(START_RESULT_NUMBER, nubeNumder);
                intent.putExtra(START_RESULT_NAME, showName);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mListview.setAdapter(mAdapter);
        mListview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                CustomLog.d(TAG,"onScrollStateChanged");
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    CustomLog.d(TAG,"列表正在滚动...");
                    // list列表滚动过程中，暂停图片上传下载
                } else {
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });

    }

    private void initTitleBar() {
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(R.string.select_revert_person);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    public void initData() {
        if (!groupId.isEmpty()) {
            memberBeanMap = groupDao.queryAllGroupMembers(groupId,
                    mySelfNueNumber);
            mAdapter.appendPageData(memberBeanMap);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
