package cn.redcdn.hvs.im.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;

import cn.redcdn.datacenter.groupchat.GroupAddoneself;
import cn.redcdn.datacenter.medicalcenter.data.MDSAccountInfo;
import cn.redcdn.hvs.AccountManager;
import cn.redcdn.hvs.R;
import cn.redcdn.hvs.base.BaseActivity;
import cn.redcdn.hvs.config.SettingData;
import cn.redcdn.hvs.im.UrlConstant;
import cn.redcdn.hvs.im.bean.GroupBean;
import cn.redcdn.hvs.im.bean.GroupMemberBean;
import cn.redcdn.hvs.im.bean.ShowNameUtil;
import cn.redcdn.hvs.im.dao.GroupDao;
import cn.redcdn.hvs.im.dao.ThreadsDao;
import cn.redcdn.hvs.im.manager.GroupChatInterfaceManager;
import cn.redcdn.hvs.profiles.view.RoundImageView;
import cn.redcdn.hvs.util.CustomToast;
import cn.redcdn.hvs.util.TitleBar;
import cn.redcdn.log.CustomLog;

import static cn.redcdn.datacenter.medicalcenter.MDSErrorCode.MDS_TOKEN_DISABLE;

/**
 * Created by Administrator on 2017/3/10.
 */
public class GroupAddActivity extends BaseActivity implements GroupChatInterfaceManager.GroupInterfaceListener{

    private final String TAG = "GroupAddActivity";

    public static final String GROUP_ID="groupid";
    public static final String GROUP_ID_FROM="saoyisao";
    private String mGroupId;
    private String groupIdFrom;

    private Context mContext;
    private GroupBean mGroupBean;
    private GroupDao mGroupDao;
    private ThreadsDao mThreadDao;
    private RoundImageView groupHeadView = null;
    private TextView tvGroupName = null;
    private TextView tvGroupMemberCount = null;
    private Button addGroupBt = null;
    private GroupChatInterfaceManager mInterfaceManager=null;
    private LinkedHashMap<String,GroupMemberBean> memberDateList = new LinkedHashMap<String, GroupMemberBean>();//显示数据
    private MDSAccountInfo loginUserInfo = null;
    private boolean isEnterGroupRequest = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_add_activity);
        initView();
        initData();
    }

    private void initView() {
        TitleBar titleBar = getTitleBar();
        titleBar.enableBack();
        groupHeadView = (RoundImageView)findViewById(R.id.group_head_image);
        tvGroupName = (TextView)findViewById(R.id.tv_group_name);
        tvGroupMemberCount = (TextView)findViewById(R.id.tv_group_member);
        addGroupBt = (Button)findViewById(R.id.add_group_bt);
        addGroupBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(memberDateList.containsKey(loginUserInfo.getNube())){
//                    CustomLog.d(TAG,"用户属于该群，直接进入");
//                    enterChatActivity();
//                }else{
                CustomLog.d(TAG,"用户不属于该群，加入该群后进入");
                addGroupRequest();
//                }
            }
        });
    }

    private void updateView(){
        mGroupBean=mGroupDao.queryGroup(mGroupId);
        memberDateList = mGroupDao.queryGroupMembers(mGroupId);
        String tempGroupName = "";
        if(mGroupBean.getgName().length() > 0){
            tempGroupName = mGroupBean.getgName();
        }else {
            for (Iterator it =  memberDateList.keySet().iterator();it.hasNext();)
            {
                Object key = it.next();
                GroupMemberBean bean = memberDateList.get(key);
                if(bean.getNickName().length() > 0){
                    tempGroupName = tempGroupName + bean.getNickName() + "、";
                }else{
                    tempGroupName = tempGroupName + bean.getNubeNum();
                }
            }
            //群名称最长显示 15 个
            if(tempGroupName.length() > 15){
                tempGroupName = tempGroupName.substring(0,15);
            }else {
                tempGroupName = tempGroupName.substring(0,tempGroupName.length() - 1);
            }
        }
        tvGroupName.setText(tempGroupName);
        tvGroupMemberCount.setText(getString(R.string.a_total_of) + memberDateList.size() + getString(R.string.people));
        Glide.with(this)
                .load(mGroupBean.getHeadUrl())
                .placeholder(R.drawable.group_icon)
                .into(groupHeadView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingView(getString(R.string.query_group_info));
        mInterfaceManager.queryGroupDetail(mGroupId);
    }

    private void initData() {
        Intent intent = getIntent();
        mGroupId = intent.getStringExtra(GROUP_ID);
        CustomLog.d(TAG,"the groupID is " + mGroupId);
        loginUserInfo = AccountManager.getInstance(this).getAccountInfo();
        groupIdFrom = intent.getStringExtra(GROUP_ID_FROM);
        groupHeadView.setImageResource(R.drawable.group_icon);
        mGroupDao = new GroupDao(this);
        mThreadDao = new ThreadsDao(this);
        mContext = this;
        CustomLog.d(TAG,"查询群详情,groupId:" + mGroupId);
        if (mInterfaceManager==null){
            mInterfaceManager=new GroupChatInterfaceManager(GroupAddActivity.this, GroupAddActivity.this);
        }

        if(!mThreadDao.isExistThread(mGroupId)){
            mThreadDao.createThreadFromGroup(mGroupId);
        }
    }

    private void enterChatActivity(){
        Intent i = new Intent(GroupAddActivity.this, ChatActivity.class);
        i.putExtra(ChatActivity.KEY_NOTICE_FRAME_TYPE,
                ChatActivity.VALUE_NOTICE_FRAME_TYPE_LIST);
        i.putExtra(ChatActivity.KEY_CONVERSATION_ID, mGroupId);
        i.putExtra(ChatActivity.KEY_CONVERSATION_TYPE,  ChatActivity.VALUE_CONVERSATION_TYPE_MULTI);
        i.putExtra(ChatActivity.KEY_CONVERSATION_NUBES, mGroupId);
        startActivity(i);
        finish();
    }

    private void addGroupRequest(){
        showLoadingView(getString(R.string.adding_group), new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                removeLoadingView();
            }
        },true);

        mInterfaceManager.groupAddOneself(mGroupId,"");

//        GroupAddoneself request = new GroupAddoneself() {
//            @Override
//            protected void onSuccess(JSONObject responseContent) {
//                isEnterGroupRequest = true;
//                mInterfaceManager.queryGroupDetail(mGroupId);
//            }
//
//            @Override
//            protected void onFail(int statusCode, String statusInfo) {
//                CustomLog.d(TAG,"加入群组失败，请重试,statusCode:" + statusCode + " statusInfo" + statusInfo);
////                CustomToast.show(mContext,"加入群组失败，请重试",1);
//                if(statusCode==-212){
//                    isEnterGroupRequest = true;
//                    mInterfaceManager.queryGroupDetail(mGroupId);
//                }else if(statusCode==MDS_TOKEN_DISABLE){
//                    removeLoadingView();
//                    AccountManager.getInstance(GroupAddActivity.this).tokenAuthFail(statusCode);
//                }else {
//                    removeLoadingView();
//                    CustomToast.show(GroupAddActivity.this, statusInfo , Toast.LENGTH_LONG);
//                }
//
//            }
//        };
//        CustomLog.d(TAG,"addGroupRequest gid:" + mGroupId + " token:" + loginUserInfo.getAccessToken());
//        request.addOneselfToGroup(mGroupId, loginUserInfo.getAccessToken());
    }

    @Override
    public void onResult(String _interfaceName, boolean isSuccess, String result) {

        if(isSuccess){
            if(UrlConstant.METHOD_ADD_ONESELF.equals(_interfaceName)){
                CustomLog.d(TAG,"加入群组成功");
                isEnterGroupRequest = true;
                enterChatActivity();
                removeLoadingView();
            }else if(UrlConstant.METHOD_QUERY_GROUP_DETAIL.equals(_interfaceName)){
                CustomLog.d(TAG,"查询群详情成功");
                removeLoadingView();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateView();
                    }
                });
            }
        }else{
            isEnterGroupRequest = false;
            removeLoadingView();
            if(UrlConstant.METHOD_ADD_ONESELF.equals(_interfaceName)){
                CustomLog.d(TAG,"加入群组失败");
                CustomToast.show(GroupAddActivity.this,this.getString(R.string.enter_group_tip),1);
            }else if(UrlConstant.METHOD_QUERY_GROUP_DETAIL.equals(_interfaceName)) {
                CustomLog.d(TAG,"查询群信息失败");
                CustomToast.show(GroupAddActivity.this,getString(R.string.query_group_info_fail_try),1);
            }

        }



//            if(isEnterGroupRequest){
//                enterChatActivity();
//                removeLoadingView();
//            }else{
//                removeLoadingView();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        updateView();
//                    }
//                });
//            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isEnterGroupRequest){
            deletExtraThread();
        }

    }

    private void deletExtraThread(){
        if(mThreadDao.isExistThread(mGroupId)){
            mThreadDao.deleteThread(mGroupId);
        }
    }
}
