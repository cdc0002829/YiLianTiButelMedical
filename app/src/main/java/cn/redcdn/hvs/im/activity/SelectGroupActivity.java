package cn.redcdn.hvs.im.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.im.adapter.SelectGroupAdapter;
import cn.redcdn.hvs.im.bean.ContactFriendBean;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.util.TitleBar;
import java.util.List;

/**
 * Desc  选择一个群，进行聊天
 * Created by wangkai on 2017/3/4.
 */

public class SelectGroupActivity extends BaseActivity {

    private ListView groupListView = null;
    private SelectGroupAdapter groupAdapter = null;
    private GroupDao mGroupDao;
    List<ContactFriendBean> groupList = null;
    private SelectGroupAdapter groupListAdapter = null;
    private LayoutInflater inflater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group);
        initWidget();
        initData();
    }


    private void initWidget() {
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        titleBar.setTitle(getString(R.string.select_a_group));
        groupListView = (ListView) findViewById(R.id.lv_group);
        inflater = getLayoutInflater();
    }


    private void initData() {
        mGroupDao = new GroupDao(this);
        groupList = mGroupDao.queryAllGroup();
        if(groupList != null){
            groupAdapter = new SelectGroupAdapter(this, groupList, inflater,mGroupDao);
            groupListView.setAdapter(groupAdapter);
        }
        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ContactFriendBean bean = groupList.get(position);
                Intent i = new Intent(SelectGroupActivity.this, ChatActivity.class);
                i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                        ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
                i.putExtra(ChatActivity.KEY_CONVERSATION_ID, bean.getNubeNumber());
                i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,  ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
                i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, bean.getNubeNumber());
                startActivity(i);
                finish();
            }
        });

    }
}
